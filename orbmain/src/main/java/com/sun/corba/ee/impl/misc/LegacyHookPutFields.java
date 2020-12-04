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

package com.sun.corba.ee.impl.misc;

import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Since ObjectOutputStream.PutField methods specify no exceptions,
 * we are not checking for null parameters on put methods.
 */
class LegacyHookPutFields extends ObjectOutputStream.PutField
{
    private Map<String, Object> fields = new HashMap<String, Object>();

    /**
     * Put the value of the named boolean field into the persistent field.
     */
    public void put(String name, boolean value){
        fields.put(name, Boolean.valueOf(value));
    }
                
    /**
     * Put the value of the named char field into the persistent fields.
     */
    public void put(String name, char value){
        fields.put(name, Character.valueOf(value));
    }
                
    /**
     * Put the value of the named byte field into the persistent fields.
     */
    public void put(String name, byte value){
        fields.put(name, Byte.valueOf(value));
    }
                
    /**
     * Put the value of the named short field into the persistent fields.
     */
    public void put(String name, short value){
        fields.put(name, Short.valueOf(value));
    }
                
    /**
     * Put the value of the named int field into the persistent fields.
     */
    public void put(String name, int value){
        fields.put(name, Integer.valueOf(value));
    }
                
    /**
     * Put the value of the named long field into the persistent fields.
     */
    public void put(String name, long value){
        fields.put(name, Long.valueOf(value));
    }
                
    /**
     * Put the value of the named float field into the persistent fields.
     *
     */
    public void put(String name, float value){
        fields.put(name, Float.valueOf(value));
    }
                
    /**
     * Put the value of the named double field into the persistent field.
     */
    public void put(String name, double value){
        fields.put(name, Double.valueOf(value));
    }
                
    /**
     * Put the value of the named Object field into the persistent field.
     */
    public void put(String name, Object value){
        fields.put(name, value);
    }
                
    /**
     * Write the data and fields to the specified ObjectOutput stream.
     */
    public void write(ObjectOutput out) throws IOException {
        out.writeObject(fields);
    }
}    
