/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
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
