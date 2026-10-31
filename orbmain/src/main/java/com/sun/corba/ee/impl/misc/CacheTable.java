/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Eclipse Distribution License
 * v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License v. 2.0 are satisfied: GNU General Public License v2.0
 * w/Classpath exception which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause OR GPL-2.0 WITH
 * Classpath-exception-2.0
 */

package com.sun.corba.ee.impl.misc;

import com.sun.corba.ee.spi.logging.ORBUtilSystemException;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.trace.Cdr;

import java.util.Arrays;

/**
 * This is a hash table implementation that simultaneously maps key to value and value to key. It is used for
 * marshalling and unmarshalling value types, where it is necessary to track the correspondence between object instances
 * and their offsets in a stream. It is also used for tracking indirections for Strings that represent codebases and
 * repositoryids. Since the offset is always non-negative, only non-negative values should be stored here (and storing
 * -1 will cause failures). Also note that the same key (Object) may be stored with multiple values (int offsets) due to
 * the way readResolve works (see also GlassFish issue 1605).
 *
 * <p>The entries live in parallel arrays, in the order they were put, and two open addressing indexes of entry
 * numbers find them by key and by value.
 *
 * <p>When a key is stored with several values, the key index names the newest of its entries and {@link #older}
 * links each to the previous one, so finding the key gives the newest value, and putting a pair that is already
 * present further down the chain is still recognised as a no-op. The value index names the newest entry for a value.
 *
 * <p>A table is made per stream and filled once per message, so without reuse every value heavy message grew its
 * arrays from nothing again: a profile of a 50 node list showed that copying and re-indexing, not the lookups, was
 * the cost. {@link #done()} therefore hands the arrays, emptied, to a pool kept by the calling thread, and a table
 * that outgrows its small arrays takes them from there, the way serializers keep their reference tables between
 * messages. A table that stays small never touches the pool, and one that is never done simply leaves its arrays to
 * the collector. The pool is per thread, so it takes no lock, and arrays may
 * move between threads with the streams that hold them: they carry no state once emptied.
 *
 * @since 1.1
 *
 * @author Ken Cavanaugh
 */
@Cdr
public class CacheTable<K> {
    private static final ORBUtilSystemException wrapper = ORBUtilSystemException.self;

    /**
     * Entries held before the indexes are used. A stream's cache usually sees a handful of objects - the arguments of
     * one request - and for those a linear identity scan beats hashing: it needs no indexes and no
     * System.identityHashCode, whose first call on an object is not free and writes the hash into its header.
     */
    private static final int SMALL_CAPACITY = 8;

    /** Index slots when the indexes are first built; the indexes are kept at most half full. */
    private static final int INITIAL_INDEX_SIZE = 32;
    private static final int MAX_INDEX_SIZE = 1 << 30;

    /** Arrays for more entries than this are not kept for reuse, so one large message does not pin them. */
    private static final int MAX_POOLED_CAPACITY = 1024;

    /** Emptied tables a thread keeps; a stream uses at most a few. */
    private static final int MAX_POOLED_TABLES = 8;

    /** An empty slot in an index. Occupied slots hold an entry number. */
    private static final int EMPTY = -1;

    private static final Object[] NO_KEYS = {};
    private static final int[] NO_VALS = {};

    private final boolean noReverseMap;

    // The entries, in the order they were put.
    private Object[] keys;
    private int[] vals;
    private int count;

    // Possibly from an earlier table: only meaningful once indexed.
    private boolean indexed;
    private int[] keyHashes; // mixed identity hash of each key, kept so that rebuilding never asks for it again
    private int[] older; // the previous entry with the same key, or EMPTY
    private int[] keyIndex;
    private int[] valIndex; // unused when noReverseMap

    /** Where this table's arrays go back to; null until it first gives them back. */
    private Storage storage;

    public CacheTable(String cacheType, ORB orb, boolean u) {
        noReverseMap = u;
        keys = new Object[SMALL_CAPACITY];
        vals = new int[SMALL_CAPACITY];
    }

    /** The arrays of an emptied table, while they wait in a pool. */
    private static final class Storage {
        Object[] keys;
        int[] vals;
        int[] keyHashes;
        int[] older;
        int[] keyIndex;
        int[] valIndex;

        void clear() {
            keys = null;
            vals = null;
            keyHashes = null;
            older = null;
            keyIndex = null;
            valIndex = null;
        }
    }

    /** A thread's emptied tables, newest on top. */
    private static final class Pool {
        private final Storage[] stack = new Storage[MAX_POOLED_TABLES];
        private int size;

        Storage take() {
            if (size == 0) {
                return null;
            }
            Storage s = stack[--size];
            stack[size] = null;
            return s;
        }

        void offer(Storage s) {
            if (size < stack.length) {
                stack[size++] = s;
            }
        }
    }

    private static final ThreadLocal<Pool> POOL = ThreadLocal.withInitial(Pool::new);

    private boolean isSmall() {
        return !indexed;
    }

    /**
     * This is taken from the hash method in the JDK 6 HashMap. It is used for both the key and the value side of the
     * mapping: the values are stream offsets, whose low bits are mostly alignment.
     */
    private static int mix(int h) {
        h ^= (h >>> 20) ^ (h >>> 12);
        return h ^ (h >>> 7) ^ (h >>> 4);
    }

    private static int keyHash(Object key) {
        return mix(System.identityHashCode(key));
    }

    /**
     * Store the (key,val) pair in the hash table, unless (key,val) is already present. val must be non-negative, but
     * this is not checked.
     *
     * @param key Key for table
     * @param val Non-negative value
     */
    public final void put(K key, int val) {
        if (isSmall()) {
            putSmall(key, val);
        } else {
            putIndexed(key, val);
        }
    }

    private void putSmall(K key, int val) {
        boolean duplicate = false;
        for (int i = count - 1; i >= 0; i--) {
            if (keys[i] == key) {
                if (vals[i] == val) {
                    return;
                }
                duplicate = true;
            }
        }
        if (duplicate) {
            // Not an error: a class whose readResolve returns a canonical
            // instance can legally have the same key at several offsets. This
            // is GlassFish issue 1605. Stored anyway, so that getVal finds it.
            wrapper.duplicateIndirectionOffset();
        }

        append(key, val);
        if (count > SMALL_CAPACITY) {
            buildIndexes(INITIAL_INDEX_SIZE);
        }
    }

    private void putIndexed(K key, int val) {
        int hash = keyHash(key);
        int mask = keyIndex.length - 1;
        int slot = hash & mask;
        int newest = EMPTY;
        for (int e; (e = keyIndex[slot]) != EMPTY; slot = (slot + 1) & mask) {
            if (keys[e] == key) {
                newest = e;
                break;
            }
        }
        if (newest != EMPTY) {
            for (int e = newest; e != EMPTY; e = older[e]) {
                if (vals[e] == val) {
                    return;
                }
            }
            // See putSmall: GlassFish issue 1605.
            wrapper.duplicateIndirectionOffset();
        }

        int entry = append(key, val);
        keyHashes[entry] = hash;
        older[entry] = newest;
        keyIndex[slot] = entry;
        if (!noReverseMap) {
            indexVal(entry);
        }

        if (count > keyIndex.length >>> 1 && keyIndex.length < MAX_INDEX_SIZE) {
            buildIndexes(keyIndex.length << 1);
        }
    }

    /** Adds an entry to the arrays, growing them if they are full, and returns its number. */
    private int append(K key, int val) {
        if (count == keys.length && keys.length == SMALL_CAPACITY && storage == null) {
            takeFromPool();
        }
        if (count == keys.length) {
            int capacity = Math.max(SMALL_CAPACITY, keys.length << 1);
            keys = Arrays.copyOf(keys, capacity);
            vals = Arrays.copyOf(vals, capacity);
            if (indexed) {
                keyHashes = Arrays.copyOf(keyHashes, capacity);
                older = Arrays.copyOf(older, capacity);
            }
        }
        keys[count] = key;
        vals[count] = val;
        return count++;
    }

    /**
     * Moves the small table's entries into arrays an earlier table gave back, if the thread has any. Only a table that
     * outgrows {@link #SMALL_CAPACITY} gets here, so the tables of small messages never touch the pool.
     */
    private void takeFromPool() {
        Storage reused = POOL.get().take();
        if (reused == null) {
            return;
        }
        storage = reused;
        if (reused.keys.length > count) {
            System.arraycopy(keys, 0, reused.keys, 0, count);
            System.arraycopy(vals, 0, reused.vals, 0, count);
            keys = reused.keys;
            vals = reused.vals;
        }
        keyHashes = reused.keyHashes;
        older = reused.older;
        keyIndex = reused.keyIndex;
        valIndex = reused.valIndex;
        reused.clear();
    }

    /** Points the value index at an entry, which is the newest for its value. */
    private void indexVal(int entry) {
        int val = vals[entry];
        int mask = valIndex.length - 1;
        int slot = mix(val) & mask;
        for (int e; (e = valIndex[slot]) != EMPTY; slot = (slot + 1) & mask) {
            if (vals[e] == val) {
                break;
            }
        }
        valIndex[slot] = entry;
    }

    /**
     * Builds both indexes, at least at the given size, from the entries, oldest first, so that each slot ends up
     * naming the newest entry for its key or value. Arrays left by an earlier table are used when they are large
     * enough. The first build is where the keys' identity hashes are first asked for; later ones reuse them.
     */
    private void buildIndexes(int size) {
        if (!indexed) {
            if (keyHashes == null || keyHashes.length < keys.length) {
                keyHashes = new int[keys.length];
                older = new int[keys.length];
            }
            for (int i = 0; i < count; i++) {
                keyHashes[i] = keyHash(keys[i]);
            }
            indexed = true;
        }

        if (keyIndex == null || keyIndex.length < size) {
            keyIndex = new int[size];
        } else {
            size = keyIndex.length;
        }
        Arrays.fill(keyIndex, EMPTY);
        int mask = size - 1;
        for (int i = 0; i < count; i++) {
            Object key = keys[i];
            int slot = keyHashes[i] & mask;
            int newest = EMPTY;
            for (int e; (e = keyIndex[slot]) != EMPTY; slot = (slot + 1) & mask) {
                if (keys[e] == key) {
                    newest = e;
                    break;
                }
            }
            older[i] = newest;
            keyIndex[slot] = i;
        }

        if (!noReverseMap) {
            if (valIndex == null || valIndex.length < size) {
                valIndex = new int[size];
            }
            Arrays.fill(valIndex, EMPTY);
            for (int i = 0; i < count; i++) {
                indexVal(i);
            }
        }
    }

    public final boolean containsKey(K key) {
        return (getVal(key) != -1);
    }

    /**
     * Returns some int val where (key,val) is in this CacheTable.
     *
     * @param key Key to lookup
     * @return Value found
     */
    public final int getVal(K key) {
        if (isSmall()) {
            for (int i = count - 1; i >= 0; i--) {
                if (keys[i] == key) {
                    return vals[i];
                }
            }
            return -1;
        }

        int mask = keyIndex.length - 1;
        for (int slot = keyHash(key) & mask, e; (e = keyIndex[slot]) != EMPTY; slot = (slot + 1) & mask) {
            if (keys[e] == key) {
                return vals[e];
            }
        }
        return -1;
    }

    public final boolean containsVal(int val) {
        return (getKey(val) != null);
    }

    /**
     * Return the key where (key,val) is present in the map.
     *
     * @param val Value to lookup
     * @return Key for the value
     */
    @SuppressWarnings("unchecked")
    public final K getKey(int val) {
        if (noReverseMap) {
            throw wrapper.getKeyInvalidInCacheTable();
        }

        if (isSmall()) {
            for (int i = count - 1; i >= 0; i--) {
                if (vals[i] == val) {
                    return (K) keys[i];
                }
            }
            return null;
        }

        int mask = valIndex.length - 1;
        for (int slot = mix(val) & mask, e; (e = valIndex[slot]) != EMPTY; slot = (slot + 1) & mask) {
            if (vals[e] == val) {
                return (K) keys[e];
            }
        }
        return null;
    }

    /**
     * Empties the table, letting go of the keys it held, and gives its arrays to the calling thread's pool for the
     * next table. The table stays usable: it starts again from nothing. Call it only when the stream that owns the
     * table is finished with it.
     */
    public void done() {
        if (storage == null && !indexed && keys.length <= SMALL_CAPACITY) {
            // Never outgrew the small arrays: nothing worth keeping.
            Arrays.fill(keys, 0, count, null);
            count = 0;
            return;
        }
        Object[] k = keys;
        if (k != NO_KEYS && k.length <= MAX_POOLED_CAPACITY) {
            Arrays.fill(k, 0, count, null);
            Storage s = storage != null ? storage : new Storage();
            s.keys = k;
            s.vals = vals;
            s.keyHashes = keyHashes;
            s.older = older;
            s.keyIndex = keyIndex;
            s.valIndex = valIndex;
            POOL.get().offer(s);
        }
        storage = null;
        keys = NO_KEYS;
        vals = NO_VALS;
        count = 0;
        indexed = false;
        keyHashes = null;
        older = null;
        keyIndex = null;
        valIndex = null;
    }
}
