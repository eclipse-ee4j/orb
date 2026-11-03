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

package com.sun.corba.ee.impl.io;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.Externalizable;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Test;

/**
 * A field is marshalled as a CORBA any when it is declared as Object, Serializable or Externalizable. The answer is
 * worked out once per field; it must be the one the type string gives.
 */
public class ObjectStreamFieldAnyTest {

    @Test
    public void object_serializable_and_externalizable_fields_are_anys() {
        for (Class<?> type : new Class<?>[] { Object.class, Serializable.class, Externalizable.class }) {
            ObjectStreamField field = new ObjectStreamField("f", type);
            assertTrue(type.getName(), field.isAny());
            assertTrue(type.getName(), ObjectStreamClassCorbaExt.isAny(field.getTypeString()));
        }
    }

    @Test
    public void other_fields_are_not() {
        // String has the length of Object, BigInteger and BigDecimal the
        // length of Serializable: the checks that look at lengths first.
        for (Class<?> type : new Class<?>[] { String.class, BigInteger.class, BigDecimal.class, Object[].class,
                Integer.class, int.class, long.class }) {
            assertFalse(type.getName(), new ObjectStreamField("f", type).isAny());
        }
    }
}
