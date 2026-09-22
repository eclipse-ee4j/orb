/*
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

/** This is a hash table implementation that simultaneously maps key to value
 * and value to key.  It is used for marshalling and unmarshalling value types,
 * where it is necessary to track the correspondence between object instances
 * and their offsets in a stream.  It is also used for tracking indirections for
 * Strings that represent codebases and repositoryids.
 * Since the offset is always non-negative,
 * only non-negative values should be stored here (and storing -1 will cause
 * failures).  Also note that the same key (Object) may be stored with multiple
 * values (int offsets) due to the way readResolve works (see also GlassFish issue 1605).
 *
 * @since 1.1
 *
 * @author Ken Cavanaugh
 */
@Cdr
public class CacheTable<K> {
    private static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    private class Entry<K> {
        private K key;
        private int val;
        private Entry<K> next;  // this chains the collision list of table "map"
        private Entry<K> rnext; // this chains the collision list of table "rmap"
        public Entry(K k, int v) {
            key = k;
            val = v;
            next = null;
            rnext = null;
        }
    }

    private boolean noReverseMap;
    private String cacheType ;

    /**
     * Entries held before the hash tables exist. A stream's cache usually
     * sees a handful of objects - the arguments of one request - and for
     * those a linear identity scan beats hashing: it needs neither the two
     * tables (allocated per stream) nor System.identityHashCode, whose first
     * call on an object is not free and writes the hash into its header.
     * Entries are appended in insertion order and scanned from the newest,
     * which is the order the prepending hash chains give.
     */
    private static final int SMALL_CAPACITY = 8;
    private Object[] smallKeys = new Object[SMALL_CAPACITY];
    private int[] smallVals = new int[SMALL_CAPACITY];
    private int smallCount;

    // size must be power of 2
    private static final int INITIAL_SIZE = 64 ;
    private static final int MAX_SIZE = 1 << 30;
    private static final int INITIAL_THRESHHOLD = 48 ;
    private int size;
    private int threshhold ;
    private int entryCount;
    private Entry<K>[] map;
    private Entry<K>[] rmap;

    private ORB orb;

    public  CacheTable(String cacheType, ORB orb, boolean u) {
        this.orb = orb;
        this.cacheType = cacheType ;
        noReverseMap = u;
        size = INITIAL_SIZE;
        threshhold = INITIAL_THRESHHOLD ;
        entryCount = 0;
    }

    private boolean isSmall() {
        return smallKeys != null;
    }

    /** Moves the small entries into the hash tables, oldest first. */
    private void leaveSmallMode() {
        initTables();
        for (int i = 0; i < smallCount; i++) {
            @SuppressWarnings("unchecked")
            K key = (K) smallKeys[i];
            // Already checked for duplicates when it was put.
            insert(hash(key), key, smallVals[i]);
        }
        smallKeys = null;
        smallVals = null;
        smallCount = 0;
    }

    private void initTables() {
        map = new Entry[size];
        if (noReverseMap) {
            rmap = null;
        } else {
            rmap = new Entry[size];
        }
    }

    private void grow() {
        if (size == MAX_SIZE) {
            return;
        }

        Entry<K>[] oldMap = map;
        int oldSize = size;
        size <<= 1;
        threshhold <<= 1 ;

        initTables();
        // Re-link existing entries in the same traversal/prepend order as
        // put_table used. The pairs are already unique: checking them again
        // and allocating replacement nodes only adds work during a resize.
        for (int i = 0; i < oldSize; i++) {
            for (Entry<K> e = oldMap[i]; e != null;) {
                Entry<K> next = e.next;
                int index = hash(e.key);
                e.next = map[index];
                map[index] = e;
                if (!noReverseMap) {
                    int rindex = hash(e.val);
                    e.rnext = rmap[rindex];
                    rmap[rindex] = e;
                }
                e = next;
            }
        }
    }

    private int hashModTableSize(int h) {
        // This is taken from the hash method in the JDK 6 HashMap.
        // This is used for both the
        // key and the value side of the mapping.  It's not clear
        // how useful this is in this application, as the low-order
        // bits change a lot for both sides.
        h ^= (h >>> 20) ^ (h >>> 12) ;
        return (h ^ (h >>> 7) ^ (h >>> 4)) & (size - 1) ;
    }

    private int hash(K key) {
        return hashModTableSize(System.identityHashCode(key));
    }

    private int hash(int val) {
        return hashModTableSize(val);
    }

    /** Store the (key,val) pair in the hash table, unless
     * (key,val) is already present.  Returns true if a new (key,val)
     * pair was added, else false.  val must be non-negative, but
     * this is not checked.
     * @param key Key for table
     * @param val Non-negative value
     */
    public final void put(K key, int val) {
        if (isSmall()) {
            if (!put_small(key, val)) {
                return;
            }
            entryCount++;
            if (smallCount <= SMALL_CAPACITY) {
                return;
            }
            // One entry more than the small arrays are for: move them all,
            // the new one last, to the hash tables.
            leaveSmallMode();
            if (entryCount > threshhold) {
                grow();
            }
            return;
        }
        if (put_table(key, val)) {
            entryCount++;
            if (entryCount > threshhold) {
                grow();
            }
        }
    }

    @Cdr
    private boolean put_table(K key, int val) {
        int index = hash(key);

        for (Entry<K> e = map[index]; e != null; e = e.next) {
            if (e.key == key) {
                if (e.val != val) {
                    // duplicateIndirectionOffset error here is not an error:
                    // A serializable/externalizable class that defines
                    // a readResolve method that creates a canonical representation
                    // of a value can legally have the same key occuring at
                    // multiple values.  This is GlassFish issue 1605.
                    // Note: we store this anyway, so that getVal can find the key.
                    wrapper.duplicateIndirectionOffset();
                } else {
                    // if we get here we are trying to put in the same key/val pair
                    // this is a no-op, so we just return
                    return false;
                }
            }
        }

        insert(index, key, val);
        return true;
    }

    private void insert(int index, K key, int val) {
        Entry<K> newEntry = new Entry<K>(key, val);
        newEntry.next = map[index];
        map[index] = newEntry;
        if (!noReverseMap) {
            int rindex = hash(val);
            newEntry.rnext = rmap[rindex];
            rmap[rindex] = newEntry;
        }
    }

    /**
     * The small-mode counterpart of put_table, with the same outcomes: false
     * for a (key,val) pair already present, the duplicate indirection
     * warning when the key is present with another value - that entry is
     * still stored, so the newer mapping is the one found.
     */
    private boolean put_small(K key, int val) {
        for (int i = smallCount - 1; i >= 0; i--) {
            if (smallKeys[i] == key) {
                if (smallVals[i] != val) {
                    wrapper.duplicateIndirectionOffset();
                } else {
                    return false;
                }
            }
        }

        if (smallCount == SMALL_CAPACITY) {
            // Full. Make room for this entry so that put can move all of
            // them, this one as the newest, to the hash tables.
            Object[] keys = new Object[SMALL_CAPACITY + 1];
            int[] vals = new int[SMALL_CAPACITY + 1];
            System.arraycopy(smallKeys, 0, keys, 0, SMALL_CAPACITY);
            System.arraycopy(smallVals, 0, vals, 0, SMALL_CAPACITY);
            smallKeys = keys;
            smallVals = vals;
        }
        smallKeys[smallCount] = key;
        smallVals[smallCount] = val;
        smallCount++;
        return true;
    }

    public final boolean containsKey(K key) {
        return (getVal(key) != -1);
    }

    /** Returns some int val where (key,val) is in this CacheTable.
     * @param key Key to lookup
     * @return Value found
     */
    public final int getVal(K key) {
        if (isSmall()) {
            for (int i = smallCount - 1; i >= 0; i--) {
                if (smallKeys[i] == key) {
                    return smallVals[i];
                }
            }
            return -1;
        }

        int index = hash(key);
        for (Entry<K> e = map[index]; e != null; e = e.next) {
            if (e.key == key) {
                return e.val;
            }
        }

        return -1;
    }

    public final boolean containsVal(int val) {
        return (getKey(val) != null);
    }

    /** Return the key where (key,val) is present in the map.
     * @param val Value to lookup
     * @return Key for the value
     */
    public final K getKey(int val) {
        if (noReverseMap) {
            throw wrapper.getKeyInvalidInCacheTable();
        }

        if (isSmall()) {
            for (int i = smallCount - 1; i >= 0; i--) {
                if (smallVals[i] == val) {
                    @SuppressWarnings("unchecked")
                    K key = (K) smallKeys[i];
                    return key;
                }
            }
            return null;
        }

        int index = hash(val);
        for (Entry<K> e = rmap[index]; e != null; e = e.rnext) {
            if (e.val == val) {
                return e.key;
            }
        }

        return null;
    }

    public void done() {
        map = null;
        rmap = null;
        smallKeys = null;
        smallVals = null;
        smallCount = 0;
    }
}
