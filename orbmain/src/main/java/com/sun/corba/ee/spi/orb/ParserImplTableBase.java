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

package com.sun.corba.ee.spi.orb;

import java.util.Map;
import java.util.AbstractMap;
import java.util.Set;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Properties;

import java.lang.reflect.Field;

import org.omg.CORBA.INTERNAL;

public abstract class ParserImplTableBase extends ParserImplBase {
    private final ParserData[] entries;

    public ParserImplTableBase(ParserData[] entries) {
        this.entries = entries;
        setDefaultValues();
    }

    protected PropertyParser makeParser() {
        PropertyParser result = new PropertyParser();
        for (int ctr = 0; ctr < entries.length; ctr++) {
            ParserData entry = entries[ctr];
            entry.addToParser(result);
        }

        return result;
    }

    private static final class MapEntry implements Map.Entry {
        private Object key;
        private Object value;

        public MapEntry(Object key) {
            this.key = key;
        }

        public Object getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }

        public Object setValue(Object value) {
            Object result = this.value;
            this.value = value;
            return result;
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof MapEntry))
                return false;

            MapEntry other = (MapEntry) obj;

            return (key.equals(other.key)) && (value.equals(other.value));
        }

        public int hashCode() {
            return key.hashCode() ^ value.hashCode();
        }
    }

    // Construct a map that maps field names to test or default values,
    // then use setFields from the parent class. A map is constructed
    // by implementing AbstractMap, which requires implementing the
    // entrySet() method, which requires implementing a set of
    // map entries, which requires implementing an iterator,
    // which iterates over the ParserData, extracting the
    // correct (key, value) pairs (nested typed lambda expression).
    private static class FieldMap extends AbstractMap {
        private final ParserData[] entries;
        private final boolean useDefault;

        public FieldMap(ParserData[] entries, boolean useDefault) {
            this.entries = entries;
            this.useDefault = useDefault;
        }

        public Set entrySet() {
            return new AbstractSet() {
                public Iterator iterator() {
                    return new Iterator() {
                        // index of next element to return
                        int ctr = 0;

                        public boolean hasNext() {
                            return ctr < entries.length;
                        }

                        public Object next() {
                            ParserData pd = entries[ctr++];
                            Map.Entry result = new MapEntry(pd.getFieldName());
                            if (useDefault)
                                result.setValue(pd.getDefaultValue());
                            else
                                result.setValue(pd.getTestValue());
                            return result;
                        }

                        public void remove() {
                            throw new UnsupportedOperationException();
                        }
                    };
                }

                public int size() {
                    return entries.length;
                }
            };
        }
    };

    protected void setDefaultValues() {
        Map map = new FieldMap(entries, true);
        setFields(map);
    }

    public void setTestValues() {
        Map map = new FieldMap(entries, false);
        setFields(map);
    }
}
