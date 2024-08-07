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

package com.sun.corba.ee.impl.oa.poa;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

import org.omg.PortableServer.Servant;
import org.omg.PortableServer.POAPackage.WrongPolicy;

/**
 * The ActiveObjectMap maintains associations between servants and their keys. There are two variants, to support
 * whether or not multiple IDs per servant are allowed. This class supports bidirectional traversal of the key-servant
 * association. Access to an instance of this class is serialized by the POA mutex.
 */
public abstract class ActiveObjectMap {
    static class Key {
        private byte[] id;

        byte[] id() {
            return id;
        }

        Key(byte[] id) {
            this.id = id;
        }

        @Override
        public String toString() {
            StringBuilder buffer = new StringBuilder();
            for (int i = 0; i < id.length; i++) {
                buffer.append(Integer.toString((int) id[i], 16));
                if (i != id.length - 1) {
                    buffer.append(":");
                }
            }
            return buffer.toString();
        }

        @Override
        public boolean equals(java.lang.Object key) {
            if (!(key instanceof Key)) {
                return false;
            }
            Key k = (Key) key;
            if (k.id.length != this.id.length) {
                return false;
            }
            for (int i = 0; i < this.id.length; i++) {
                if (this.id[i] != k.id[i]) {
                    return false;
                }
            }
            return true;
        }

        // Use the same hash function as for String
        @Override
        public int hashCode() {
            int h = 0;
            for (int i = 0; i < id.length; i++) {
                h = 31 * h + id[i];
            }
            return h;
        }
    }

    protected POAImpl poa;

    protected ActiveObjectMap(POAImpl poa) {
        this.poa = poa;
    }

    public static ActiveObjectMap create(POAImpl poa, boolean multipleIDsAllowed) {
        if (multipleIDsAllowed) {
            return new MultipleObjectMap(poa);
        } else {
            return new SingleObjectMap(poa);
        }
    }

    private Map<Key, AOMEntry> keyToEntry = new HashMap<Key, AOMEntry>();
    private Map<AOMEntry, Servant> entryToServant = new HashMap<AOMEntry, Servant>();
    private Map<Servant, AOMEntry> servantToEntry = new HashMap<Servant, AOMEntry>();

    public final boolean contains(Servant value) {
        return servantToEntry.containsKey(value);
    }

    public final boolean containsKey(Key key) {
        return keyToEntry.containsKey(key);
    }

    /*
     * get Returns the entry assigned to the key, or creates a new entry in state INVALID if none is present.
     */
    public final AOMEntry get(Key key) {
        AOMEntry result = keyToEntry.get(key);
        if (result == null) {
            result = new AOMEntry(poa);
            putEntry(key, result);
        }

        return result;
    }

    public final Servant getServant(AOMEntry entry) {
        return entryToServant.get(entry);
    }

    public abstract Key getKey(AOMEntry value) throws WrongPolicy;

    public Key getKey(Servant value) throws WrongPolicy {
        AOMEntry entry = servantToEntry.get(value);
        return getKey(entry);
    }

    protected void putEntry(Key key, AOMEntry value) {
        keyToEntry.put(key, value);
    }

    public final void putServant(Servant servant, AOMEntry value) {
        entryToServant.put(value, servant);
        servantToEntry.put(servant, value);
    }

    protected abstract void removeEntry(AOMEntry entry, Key key);

    public final void remove(Key key) {
        AOMEntry entry = keyToEntry.remove(key);
        Servant servant = entryToServant.remove(entry);
        if (servant != null) {
            servantToEntry.remove(servant);
        }

        removeEntry(entry, key);
    }

    public abstract boolean hasMultipleIDs(AOMEntry value);

    protected void clear() {
        keyToEntry.clear();
    }

    public final Set<Key> keySet() {
        return keyToEntry.keySet();
    }
}

class SingleObjectMap extends ActiveObjectMap {
    private Map<AOMEntry, Key> entryToKey = new HashMap<AOMEntry, Key>();

    SingleObjectMap(POAImpl poa) {
        super(poa);
    }

    public Key getKey(AOMEntry value) throws WrongPolicy {
        return entryToKey.get(value);
    }

    @Override
    protected void putEntry(Key key, AOMEntry value) {
        super.putEntry(key, value);

        entryToKey.put(value, key);
    }

    public boolean hasMultipleIDs(AOMEntry value) {
        return false;
    }

    // This case does not need the key.
    protected void removeEntry(AOMEntry entry, Key key) {
        entryToKey.remove(entry);
    }

    @Override
    public void clear() {
        super.clear();
        entryToKey.clear();
    }
}

class MultipleObjectMap extends ActiveObjectMap {
    private Map<AOMEntry, Set<Key>> entryToKeys = new HashMap<AOMEntry, Set<Key>>();

    MultipleObjectMap(POAImpl poa) {
        super(poa);
    }

    public Key getKey(AOMEntry value) throws WrongPolicy {
        throw new WrongPolicy();
    }

    @Override
    protected void putEntry(Key key, AOMEntry value) {
        super.putEntry(key, value);

        Set<Key> set = entryToKeys.get(value);
        if (set == null) {
            set = new HashSet<Key>();
            entryToKeys.put(value, set);
        }
        set.add(key);
    }

    public boolean hasMultipleIDs(AOMEntry value) {
        Set<Key> set = entryToKeys.get(value);
        if (set == null) {
            return false;
        }
        return set.size() > 1;
    }

    protected void removeEntry(AOMEntry entry, Key key) {
        Set<Key> keys = entryToKeys.get(entry);
        if (keys != null) {
            keys.remove(key);
            if (keys.isEmpty()) {
                entryToKeys.remove(entry);
            }
        }
    }

    @Override
    public void clear() {
        super.clear();
        entryToKeys.clear();
    }
}
