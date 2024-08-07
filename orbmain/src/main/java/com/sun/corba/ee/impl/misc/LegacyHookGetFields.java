/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2019 Payara Services Ltd.
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

import java.io.*;
import java.util.Hashtable;

class LegacyHookGetFields extends ObjectInputStream.GetField {
    private Hashtable<String, Object> fields = null;

    LegacyHookGetFields(Hashtable<String, Object> fields) {
        this.fields = fields;
    }

    /**
     * Get the ObjectStreamClass that describes the fields in the stream.
     */
    @Override
    public java.io.ObjectStreamClass getObjectStreamClass() {
        return null;
    }

    /**
     * Return true if the named field is defaulted and has no value in this stream.
     */
    @Override
    public boolean defaulted(String name) throws IOException, IllegalArgumentException {
        return (!fields.containsKey(name));
    }

    /**
     * Get the value of the named boolean field from the persistent field.
     */
    @Override
    public boolean get(String name, boolean defvalue) throws IOException, IllegalArgumentException {
        if (defaulted(name))
            return defvalue;
        else
            return ((Boolean) fields.get(name));
    }

    /**
     * Get the value of the named char field from the persistent fields.
     */
    @Override
    public char get(String name, char defvalue) throws IOException, IllegalArgumentException {
        if (defaulted(name))
            return defvalue;
        else
            return ((Character) fields.get(name));

    }

    /**
     * Get the value of the named byte field from the persistent fields.
     */
    @Override
    public byte get(String name, byte defvalue) throws IOException, IllegalArgumentException {
        if (defaulted(name))
            return defvalue;
        else
            return ((Byte) fields.get(name));

    }

    /**
     * Get the value of the named short field from the persistent fields.
     */
    @Override
    public short get(String name, short defvalue) throws IOException, IllegalArgumentException {
        if (defaulted(name))
            return defvalue;
        else
            return ((Short) fields.get(name));

    }

    /**
     * Get the value of the named int field from the persistent fields.
     */
    @Override
    public int get(String name, int defvalue) throws IOException, IllegalArgumentException {
        if (defaulted(name))
            return defvalue;
        else
            return ((Integer) fields.get(name));

    }

    /**
     * Get the value of the named long field from the persistent fields.
     */
    @Override
    public long get(String name, long defvalue) throws IOException, IllegalArgumentException {
        if (defaulted(name))
            return defvalue;
        else
            return ((Long) fields.get(name));

    }

    /**
     * Get the value of the named float field from the persistent fields.
     */
    @Override
    public float get(String name, float defvalue) throws IOException, IllegalArgumentException {
        if (defaulted(name))
            return defvalue;
        else
            return ((Float) fields.get(name));

    }

    /**
     * Get the value of the named double field from the persistent field.
     */
    @Override
    public double get(String name, double defvalue) throws IOException, IllegalArgumentException {
        if (defaulted(name))
            return defvalue;
        else
            return ((Double) fields.get(name)).doubleValue();

    }

    /**
     * Get the value of the named Object field from the persistent field.
     */
    @Override
    public Object get(String name, Object defvalue) throws IOException, IllegalArgumentException {
        if (defaulted(name))
            return defvalue;
        else
            return fields.get(name);

    }

    @Override
    public String toString() {
        return fields.toString();
    }
}
