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
        initTables();
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
        // now rehash the entries into the new table
        for (int i = 0; i < oldSize; i++) {
            for (Entry<K> e = oldMap[i]; e != null; e = e.next) {
                put_table(e.key, e.val);
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
        
        Entry<K> newEntry = new Entry<K>(key, val);
        newEntry.next = map[index];
        map[index] = newEntry;
        if (!noReverseMap) {
            int rindex = hash(val);
            newEntry.rnext = rmap[rindex];
            rmap[rindex] = newEntry;
        }

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
    }
}
