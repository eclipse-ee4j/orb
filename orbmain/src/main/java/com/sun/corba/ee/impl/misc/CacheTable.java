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
 * numbers find them by key and by value. A table is made per stream and filled once per message, so the chained
 * version's node per entry, and the second node chain for the reverse map, were most of what a value heavy request
 * allocated; the arrays hold the same entries without an object each, and a lookup reads neighbouring slots instead
 * of following references.
 *
 * <p>When a key is stored with several values, the key index names the newest of its entries and {@link #older}
 * links each to the previous one, so finding the key gives the newest value, and putting a pair that is already
 * present further down the chain is still recognised as a no-op. The value index names the newest entry for a value.
 *
 * @since 1.1
 *
 * @author Ken Cavanaugh
 */
@Cdr
public class CacheTable<K> {
    private static final ORBUtilSystemException wrapper = ORBUtilSystemException.self;

    /**
     * Entries held before the indexes exist. A stream's cache usually sees a handful of objects - the arguments of one
     * request - and for those a linear identity scan beats hashing: it needs no indexes and no
     * System.identityHashCode, whose first call on an object is not free and writes the hash into its header.
     */
    private static final int SMALL_CAPACITY = 8;

    /** Index slots when the indexes are first built; the indexes are kept at most half full. */
    private static final int INITIAL_INDEX_SIZE = 32;
    private static final int MAX_INDEX_SIZE = 1 << 30;

    /** An empty slot in an index. Occupied slots hold an entry number. */
    private static final int EMPTY = -1;

    private final boolean noReverseMap;

    // The entries, in the order they were put.
    private Object[] keys;
    private int[] vals;
    private int count;

    // Absent until the table outgrows SMALL_CAPACITY.
    private int[] keyHashes; // mixed identity hash of each key, kept so that rebuilding never asks for it again
    private int[] older; // the previous entry with the same key, or EMPTY
    private int[] keyIndex;
    private int[] valIndex; // absent too when noReverseMap

    public CacheTable(String cacheType, ORB orb, boolean u) {
        noReverseMap = u;
        keys = new Object[SMALL_CAPACITY];
        vals = new int[SMALL_CAPACITY];
    }

    private boolean isSmall() {
        return keyIndex == null;
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
        if (count == keys.length) {
            int capacity = keys.length << 1;
            keys = Arrays.copyOf(keys, capacity);
            vals = Arrays.copyOf(vals, capacity);
            if (keyHashes != null) {
                keyHashes = Arrays.copyOf(keyHashes, capacity);
                older = Arrays.copyOf(older, capacity);
            }
        }
        keys[count] = key;
        vals[count] = val;
        return count++;
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
     * Builds both indexes at the given size from the entries, oldest first, so that each slot ends up naming the
     * newest entry for its key or value. The first build is where the keys' identity hashes are first asked for;
     * later ones reuse them.
     */
    private void buildIndexes(int size) {
        if (keyHashes == null) {
            keyHashes = new int[keys.length];
            older = new int[keys.length];
            for (int i = 0; i < count; i++) {
                keyHashes[i] = keyHash(keys[i]);
            }
        }

        keyIndex = new int[size];
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
            valIndex = new int[size];
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

    /** Empties the table, letting go of the keys it held. */
    public void done() {
        keys = new Object[SMALL_CAPACITY];
        vals = new int[SMALL_CAPACITY];
        count = 0;
        keyHashes = null;
        older = null;
        keyIndex = null;
        valIndex = null;
    }
}
