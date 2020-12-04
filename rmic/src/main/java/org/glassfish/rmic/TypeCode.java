/*
 * Copyright (c) 2018, 2020 Oracle and/or its affiliates.
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

package org.glassfish.rmic;

import static org.glassfish.rmic.tools.java.Constants.*;

public enum TypeCode {
    BOOLEAN(TC_BOOLEAN) {
        @Override
        public String format(String s) {
            return Boolean.toString(s.equals("1"));
        }
    },
    BYTE(TC_BYTE),
    CHAR(TC_CHAR) {
        @Override
        public String toValueString(Object value) {
            return value == null ? null : "L'" + asCharacter(value) + "'";
        }

        private String asCharacter(Object value) {
            return String.valueOf((char) ((Number) value).intValue());
        }
    },
    SHORT(TC_SHORT),
    INT(TC_INT),
    LONG(TC_LONG) {
        @Override
        public String format(String s) {
            return s + "L";
        }
    },
    FLOAT(TC_FLOAT) {
        @Override
        public String format(String s) {
            return s + "F";
        }
    },
    DOUBLE(TC_DOUBLE) {
        @Override
        public String format(String s) {
            return s + "D";
        }
    },
    NULL(TC_NULL),
    ARRAY(TC_ARRAY),
    CLASS(TC_CLASS),
    VOID(TC_VOID),
    METHOD(TC_METHOD),
    ERROR(TC_ERROR);

    private int tcCode;

    public int tcCode() {
        return tcCode;
    }

    TypeCode(int tcCode) {
        this.tcCode = tcCode;
    }

    String format(String s) {
        return s;
    }

    public String toValueString(Object value) {
        return value == null ? null : format(value.toString());
    }
}
