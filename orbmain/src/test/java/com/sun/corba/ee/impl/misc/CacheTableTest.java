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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.Random;

import org.junit.Test;

/**
 * CacheTable keeps its first entries in small arrays and moves to hash tables
 * once there are more. Nothing about that may be visible: these tests drive
 * it and the previous implementation with the same operations and require
 * the same answers, including for a key stored twice with different values.
 */
public class CacheTableTest {

    private static final int KEYS = 40;

    @Test
    public void multiple_resizes_preserve_aliases_and_reverse_lookups() {
        for (boolean noReverseMap : new boolean[] { false, true }) {
            CacheTable<Object> table = new CacheTable<>("test", null, noReverseMap);
            LegacyCacheTable<Object> legacy = new LegacyCacheTable<>("test", null, noReverseMap);
            Object[] keys = new Object[160];
            for (int i = 0; i < keys.length; i++) {
                keys[i] = new Object();
            }
            for (int offset = 0; offset < 600; offset++) {
                Object key = keys[offset % keys.length];
                table.put(key, offset);
                legacy.put(key, offset);
                // A repeated pair must remain a no-op, also after resizing.
                table.put(key, offset);
                legacy.put(key, offset);
                check(0, table, legacy, keys, offset + 1, noReverseMap);
            }
        }
    }

    @Test
    public void answers_like_the_hash_table_version_with_a_reverse_map() {
        for (long seed = 0; seed < 100; seed++) {
            compare(seed, false);
        }
    }

    @Test
    public void answers_like_the_hash_table_version_without_a_reverse_map() {
        for (long seed = 0; seed < 100; seed++) {
            compare(seed, true);
        }
    }

    @Test
    public void the_newest_value_of_a_duplicated_key_wins_before_and_after_the_switch() {
        CacheTable<Object> table = new CacheTable<>("test", null, false);
        Object key = new Object();
        table.put(key, 1);
        table.put(key, 2);
        assertEquals(2, table.getVal(key));
        for (int i = 0; i < 20; i++) {
            table.put(new Object(), 100 + i);
        }
        assertEquals("still the newest after moving to the hash tables", 2, table.getVal(key));
        assertSame(key, table.getKey(1));
        assertSame(key, table.getKey(2));
    }

    @Test
    public void an_absent_key_and_an_absent_value_are_reported_as_absent() {
        CacheTable<Object> table = new CacheTable<>("test", null, false);
        table.put(new Object(), 7);
        assertEquals(-1, table.getVal(new Object()));
        assertNull(table.getKey(8));
    }

    @Test(expected = org.omg.CORBA.INTERNAL.class)
    public void getKey_without_a_reverse_map_still_fails_in_small_mode() {
        CacheTable<Object> table = new CacheTable<>("test", null, true);
        table.put(new Object(), 7);
        table.getKey(7);
    }

    private static void compare(long seed, boolean noReverseMap) {
        Random random = new Random(seed);
        Object[] keys = new Object[KEYS];
        for (int i = 0; i < keys.length; i++) {
            keys[i] = new Object();
        }

        CacheTable<Object> table = new CacheTable<>("test", null, noReverseMap);
        LegacyCacheTable<Object> legacy = new LegacyCacheTable<>("test", null, noReverseMap);

        // Up to 120 puts crosses both the small capacity and the first grow.
        int operations = 1 + random.nextInt(120);
        int nextVal = 0;
        for (int op = 0; op < operations; op++) {
            Object key = keys[random.nextInt(keys.length)];
            int roll = random.nextInt(10);
            // Mostly fresh values, sometimes the key's current one again (a
            // no-op), sometimes a second value for a key (readResolve).
            int val = roll < 8 ? nextVal++ : roll == 8 ? Math.max(0, legacy.getVal(key)) : nextVal++;
            table.put(key, val);
            legacy.put(key, val);
            check(seed, table, legacy, keys, nextVal, noReverseMap);
        }
    }

    private static void check(long seed, CacheTable<Object> table, LegacyCacheTable<Object> legacy,
            Object[] keys, int vals, boolean noReverseMap) {
        for (Object key : keys) {
            assertEquals("seed " + seed, legacy.getVal(key), table.getVal(key));
            assertEquals("seed " + seed, legacy.containsKey(key), table.containsKey(key));
        }
        if (!noReverseMap) {
            for (int v = 0; v <= vals; v++) {
                assertSame("seed " + seed + " val " + v, legacy.getKey(v), table.getKey(v));
            }
        }
    }
}
