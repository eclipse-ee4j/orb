/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A concurrent cache whose values are held softly.
 *
 * <p>It exists to replace two uses of
 * {@code org.glassfish.pfl.basic.concurrent.SoftCache}, which despite its
 * package name is a bare {@code HashMap} with no synchronization of its own.
 * One of those uses was safe only because every caller happened to hold an
 * external monitor; the other was read with no lock at all while other
 * threads wrote it, which is a data race on a plain HashMap - and since that
 * class also mutates its map inside {@code get}, even two concurrent readers
 * were enough to corrupt it.
 *
 * <p>Values are soft rather than strong because both caches map to, or from,
 * {@code Class} objects. A strong reference from a process wide static map to
 * an application class keeps its class loader alive forever, which in an
 * application server is a redeployment leak.
 *
 * <p>Reads take no lock. Cleared entries are evicted by {@link #purge()},
 * which callers are expected to invoke from whatever slow path they already
 * have, rather than on every read.
 *
 * @param <K> key type
 * @param <V> value type
 */
public final class ConcurrentSoftCache<K, V> {

    private final ConcurrentMap<K, Entry<K, V>> map = new ConcurrentHashMap<>();
    private final ReferenceQueue<V> cleared = new ReferenceQueue<>();

    /** A soft reference that remembers its key, so a cleared one can be evicted. */
    private static final class Entry<K, V> extends SoftReference<V> {

        private final K key;

        Entry(K key, V value, ReferenceQueue<V> queue) {
            super(value, queue);
            this.key = key;
        }
    }

    /**
     * @param key the key to look up
     * @return the value, or null if absent or already collected
     */
    public V get(K key) {
        Entry<K, V> entry = map.get(key);
        return entry == null ? null : entry.get();
    }

    /**
     * @param key the key to store under
     * @param value the value to hold softly
     */
    public void put(K key, V value) {
        map.put(key, new Entry<>(key, value, cleared));
    }

    /**
     * Drops the entries whose value has been collected.
     */
    public void purge() {
        for (Reference<? extends V> ref; (ref = cleared.poll()) != null; ) {
            @SuppressWarnings("unchecked")
            Entry<K, V> entry = (Entry<K, V>) ref;
            // Two argument remove, so an entry that a later put reinstated
            // under the same key is never evicted by this one's death.
            map.remove(entry.key, entry);
        }
    }

    /**
     * @return the number of entries, including any whose value has been
     *         collected but not yet purged
     */
    public int size() {
        return map.size();
    }
}
