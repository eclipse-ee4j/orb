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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.Test;

/**
 * CacheTable keeps its first entries in a linear array and indexes them once
 * there are more. Nothing about that may be visible: these tests drive it and
 * the previous, chained implementation with the same operations and require
 * the same answers.
 *
 * <p>Except in one case, where the previous implementation has no stable
 * answer to agree with. A key stored with two values sits twice in one hash
 * chain, and the chained grow() re-put each chain in the order it walked it,
 * reversing it: getVal returned the newest value or the oldest depending on
 * how many times the table had grown. The same holds for a value stored under
 * two keys and getKey. For those, the answer required here is the newest, and
 * the previous implementation only has to have answered one of them.
 */
public class CacheTableTest {

    private static final int KEYS = 40;

    @Test
    public void multiple_resizes_preserve_aliases_and_reverse_lookups() {
        for (boolean noReverseMap : new boolean[] { false, true }) {
            CacheTable<Object> table = new CacheTable<>("test", null, noReverseMap);
            LegacyCacheTable<Object> legacy = new LegacyCacheTable<>("test", null, noReverseMap);
            Model model = new Model();
            Object[] keys = new Object[160];
            for (int i = 0; i < keys.length; i++) {
                keys[i] = new Object();
            }
            for (int offset = 0; offset < 600; offset++) {
                Object key = keys[offset % keys.length];
                table.put(key, offset);
                legacy.put(key, offset);
                model.put(key, offset);
                // A repeated pair must remain a no-op, also after resizing.
                table.put(key, offset);
                legacy.put(key, offset);
                model.put(key, offset);
                check(0, table, legacy, model, keys, offset + 1, noReverseMap);
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
        for (int i = 0; i < 200; i++) {
            table.put(new Object(), 100 + i);
            assertEquals("still the newest after indexing and growing", 2, table.getVal(key));
        }
        assertSame(key, table.getKey(1));
        assertSame(key, table.getKey(2));
    }

    @Test
    public void an_older_pair_of_a_duplicated_key_is_still_a_no_op() {
        CacheTable<Object> table = new CacheTable<>("test", null, false);
        Object key = new Object();
        for (int i = 0; i < 20; i++) {
            table.put(new Object(), 100 + i);
        }
        table.put(key, 1);
        table.put(key, 2);
        // Present further down the key's chain: must not become the newest.
        table.put(key, 1);
        assertEquals(2, table.getVal(key));
    }

    @Test
    public void an_absent_key_and_an_absent_value_are_reported_as_absent() {
        CacheTable<Object> table = new CacheTable<>("test", null, false);
        table.put(new Object(), 7);
        assertEquals(-1, table.getVal(new Object()));
        assertNull(table.getKey(8));
        for (int i = 0; i < 100; i++) {
            table.put(new Object(), 1000 + i);
        }
        assertEquals(-1, table.getVal(new Object()));
        assertNull(table.getKey(8));
    }

    @Test
    public void done_empties_the_table_and_leaves_it_usable() {
        CacheTable<Object> table = new CacheTable<>("test", null, false);
        Object[] keys = new Object[50];
        for (int i = 0; i < keys.length; i++) {
            keys[i] = new Object();
            table.put(keys[i], i);
        }
        table.done();
        assertEquals(-1, table.getVal(keys[3]));
        assertNull(table.getKey(3));
        table.put(keys[3], 9);
        assertEquals(9, table.getVal(keys[3]));
        assertSame(keys[3], table.getKey(9));
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
        Model model = new Model();

        // Up to 120 puts crosses both the small capacity and the first grow.
        int operations = 1 + random.nextInt(120);
        int nextVal = 0;
        for (int op = 0; op < operations; op++) {
            Object key = keys[random.nextInt(keys.length)];
            int roll = random.nextInt(10);
            // Mostly fresh values, sometimes the key's current one again (a
            // no-op), sometimes a second value for a key (readResolve).
            int val = roll < 8 ? nextVal++ : roll == 8 ? Math.max(0, model.getVal(key)) : nextVal++;
            table.put(key, val);
            legacy.put(key, val);
            model.put(key, val);
            check(seed, table, legacy, model, keys, nextVal, noReverseMap);
        }
    }

    private static void check(long seed, CacheTable<Object> table, LegacyCacheTable<Object> legacy, Model model,
            Object[] keys, int vals, boolean noReverseMap) {
        for (Object key : keys) {
            int expected = model.getVal(key);
            assertEquals("seed " + seed, expected, table.getVal(key));
            assertEquals("seed " + seed, expected != -1, table.containsKey(key));
            List<Integer> stored = model.valsOf(key);
            if (stored.size() <= 1) {
                assertEquals("seed " + seed, expected, legacy.getVal(key));
            } else {
                assertTrue("seed " + seed, stored.contains(legacy.getVal(key)));
            }
        }
        if (!noReverseMap) {
            for (int v = 0; v <= vals; v++) {
                Object expected = model.getKey(v);
                assertSame("seed " + seed + " val " + v, expected, table.getKey(v));
                List<Object> stored = model.keysOf(v);
                if (stored.size() <= 1) {
                    assertSame("seed " + seed + " val " + v, expected, legacy.getKey(v));
                } else {
                    Object answered = legacy.getKey(v);
                    assertTrue("seed " + seed + " val " + v, stored.stream().anyMatch(k -> k == answered));
                }
            }
        }
    }

    /** What a CacheTable holds, newest last: a pair already present is not stored again. */
    private static final class Model {
        private final Map<Object, List<Integer>> valsByKey = new IdentityHashMap<>();
        private final Map<Integer, List<Object>> keysByVal = new HashMap<>();

        void put(Object key, int val) {
            List<Integer> vals = valsByKey.computeIfAbsent(key, k -> new ArrayList<>());
            if (vals.contains(val)) {
                return;
            }
            vals.add(val);
            keysByVal.computeIfAbsent(val, v -> new ArrayList<>()).add(key);
        }

        List<Integer> valsOf(Object key) {
            return valsByKey.getOrDefault(key, List.of());
        }

        List<Object> keysOf(int val) {
            return keysByVal.getOrDefault(val, List.of());
        }

        int getVal(Object key) {
            List<Integer> vals = valsOf(key);
            return vals.isEmpty() ? -1 : vals.get(vals.size() - 1);
        }

        Object getKey(int val) {
            List<Object> keys = keysOf(val);
            return keys.isEmpty() ? null : keys.get(keys.size() - 1);
        }
    }
}
