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

package com.sun.corba.ee.impl.encoding;

import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * A class which uses writeReplace and readResolve to place a custom version into the object stream.
 */
class Gender implements Serializable {
    private static final long serialVersionUID = 0x34789521D52D7FF2L;

    final static String REPID = "RMI:com.sun.corba.ee.impl.encoding.Gender\\U0024SerializedForm:F85634868214EB9C:34789521D52D7FF2";
    final static Gender MALE = new Gender("Male");
    final static Gender FEMALE = new Gender("Female");

    private String name;

    private Gender(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Gender{" +
                "name='" + name + '\'' +
                '}';
    }

    private Object writeReplace() throws ObjectStreamException {
        if (this.equals(MALE)) {
            return SerializedForm.MALE_FORM;
        } else {
            return SerializedForm.FEMALE_FORM;
        }
    }

    private static class SerializedForm implements Serializable {

        final static SerializedForm MALE_FORM = new SerializedForm(0);
        final static SerializedForm FEMALE_FORM = new SerializedForm(1);

        private int value;

        SerializedForm(int value) {
            this.value = value;
        }

        private Object readResolve() throws ObjectStreamException {
            if (value == MALE_FORM.value) {
                return Gender.MALE;
            } else {
                return Gender.FEMALE;
            }
        }
    }
}
