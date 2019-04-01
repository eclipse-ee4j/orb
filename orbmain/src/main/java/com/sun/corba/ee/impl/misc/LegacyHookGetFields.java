/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.misc;

import java.io.*;
import java.util.Hashtable;

class LegacyHookGetFields extends ObjectInputStream.GetField {
    private Hashtable fields = null;

    LegacyHookGetFields(Hashtable fields) {
        this.fields = fields;
    }

    /**
     * Get the ObjectStreamClass that describes the fields in the stream.
     */
    public java.io.ObjectStreamClass getObjectStreamClass() {
        return null;
    }

    /**
     * Return true if the named field is defaulted and has no value in this stream.
     */
    public boolean defaulted(String name) throws IOException, IllegalArgumentException {
        return (!fields.containsKey(name));
    }

    /**
     * Get the value of the named boolean field from the persistent field.
     */
    public boolean get(String name, boolean defvalue) throws IOException, IllegalArgumentException {
        if (defaulted(name))
            return defvalue;
        else
            return ((Boolean) fields.get(name)).booleanValue();
    }

    /**
     * Get the value of the named char field from the persistent fields.
     */
    public char get(String name, char defvalue) throws IOException, IllegalArgumentException {
        if (defaulted(name))
            return defvalue;
        else
            return ((Character) fields.get(name)).charValue();

    }

    /**
     * Get the value of the named byte field from the persistent fields.
     */
    public byte get(String name, byte defvalue) throws IOException, IllegalArgumentException {
        if (defaulted(name))
            return defvalue;
        else
            return ((Byte) fields.get(name)).byteValue();

    }

    /**
     * Get the value of the named short field from the persistent fields.
     */
    public short get(String name, short defvalue) throws IOException, IllegalArgumentException {
        if (defaulted(name))
            return defvalue;
        else
            return ((Short) fields.get(name)).shortValue();

    }

    /**
     * Get the value of the named int field from the persistent fields.
     */
    public int get(String name, int defvalue) throws IOException, IllegalArgumentException {
        if (defaulted(name))
            return defvalue;
        else
            return ((Integer) fields.get(name)).intValue();

    }

    /**
     * Get the value of the named long field from the persistent fields.
     */
    public long get(String name, long defvalue) throws IOException, IllegalArgumentException {
        if (defaulted(name))
            return defvalue;
        else
            return ((Long) fields.get(name)).longValue();

    }

    /**
     * Get the value of the named float field from the persistent fields.
     */
    public float get(String name, float defvalue) throws IOException, IllegalArgumentException {
        if (defaulted(name))
            return defvalue;
        else
            return ((Float) fields.get(name)).floatValue();

    }

    /**
     * Get the value of the named double field from the persistent field.
     */
    public double get(String name, double defvalue) throws IOException, IllegalArgumentException {
        if (defaulted(name))
            return defvalue;
        else
            return ((Double) fields.get(name)).doubleValue();

    }

    /**
     * Get the value of the named Object field from the persistent field.
     */
    public Object get(String name, Object defvalue) throws IOException, IllegalArgumentException {
        if (defaulted(name))
            return defvalue;
        else
            return fields.get(name);

    }

    public String toString() {
        return fields.toString();
    }
}
