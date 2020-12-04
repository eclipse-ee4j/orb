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

package corba.cdrext;

import java.util.*;
import java.io.*;

/**
 * Uses PutField/GetField
 */
public class TestObject implements Serializable
{
    private Map internalRep;

    static final long serialVersionUID = 6143829429461952693L;
    
    static final String STRING_NAME = "stringData";
    static final String BYTE_ARRAY_NAME = "byteArray";
    static final String LONG_NAME = "longData";
    static final String CHAR_NAME = "charData";
    static final String NEW_FIELD_NAME = "cello";
    static final Class NEW_FIELD_CLASS = Integer.class;

    private static final ObjectStreamField[] serialPersistentFields = { 
        new ObjectStreamField(STRING_NAME, String.class),
        new ObjectStreamField(BYTE_ARRAY_NAME, byte[].class),
        new ObjectStreamField(LONG_NAME, Long.TYPE),
        new ObjectStreamField(CHAR_NAME, Character.TYPE),
        new ObjectStreamField(NEW_FIELD_NAME, NEW_FIELD_CLASS)
    };

    public TestObject() {
        internalRep = new HashMap();

        internalRep.put(STRING_NAME, "Wookwookwook");

        byte[] data = new byte[24];
        for (int i = 0; i < data.length; i++)
            data[i] = (byte)i;
        internalRep.put(BYTE_ARRAY_NAME, data);

        internalRep.put(LONG_NAME, new Long(43));

        internalRep.put(NEW_FIELD_NAME, new Integer(4523));

        internalRep.put(CHAR_NAME, new Character('\u6D77'));
    }

    public TestObject(String str,
                      byte[] data,
                      long lg,
                      char ch,
                      Integer newField) {

        this();

        internalRep.put(STRING_NAME, new String(str));

        byte[] newArray = new byte[data.length];
        System.arraycopy(data, 0, newArray, 0, data.length);

        internalRep.put(BYTE_ARRAY_NAME, newArray);
        internalRep.put(LONG_NAME, new Long(lg));
        internalRep.put(CHAR_NAME, new Character(ch));
        internalRep.put(NEW_FIELD_NAME, newField);
    }

    public boolean equals(Object obj) {
        try {
            TestObject to = (TestObject)obj;

            if (!internalRep.get(STRING_NAME).equals(to.internalRep.get(STRING_NAME)))
                return false;

            if (!internalRep.get(LONG_NAME).equals(to.internalRep.get(LONG_NAME)))
                return false;

            if (!internalRep.get(CHAR_NAME).equals(to.internalRep.get(CHAR_NAME)))
                return false;

            byte[] thisArray = (byte[])internalRep.get(BYTE_ARRAY_NAME);
            byte[] otherArray = (byte[])to.internalRep.get(BYTE_ARRAY_NAME);

            return Arrays.equals(thisArray, otherArray);

        } catch (ClassCastException cce) {
            return false;
        }
    }

    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {

        ObjectInputStream.GetField fields = s.readFields();

        System.out.println("Got: " + fields.getClass().getName());

        String strData = (String)fields.get(STRING_NAME, null);
        byte[] byteData = (byte[])fields.get(BYTE_ARRAY_NAME, null);
        long lgData = (long)fields.get(LONG_NAME, (long)0);
        char chData = (char)fields.get(CHAR_NAME, (char)'0');
        Integer newField = (Integer)fields.get(NEW_FIELD_NAME,
                                               new Integer(0));

        if (chData == '0')
            System.out.println("chData defaulted");
        else
            System.out.println("chData not defaulted -- " + (int)chData);

        if (strData == null)
            System.out.println("String data defaulted");

        if (newField.equals(new Integer(0))) {
            System.out.println("The new field was defaulted");
        }

        internalRep = new HashMap();

        internalRep.put(STRING_NAME, strData);
        internalRep.put(BYTE_ARRAY_NAME, byteData);
        internalRep.put(LONG_NAME, new Long(lgData));
        internalRep.put(CHAR_NAME, new Character(chData));
        internalRep.put(NEW_FIELD_NAME, newField);
    }

    private void writeObject(ObjectOutputStream s) throws IOException {

        System.out.println("In writeObject");

        ObjectOutputStream.PutField fields = s.putFields();

        System.out.println("Got: " + fields.getClass().getName());

        if (internalRep.get(STRING_NAME) == null)
            System.out.println("--- String is null");
        if (internalRep.get(BYTE_ARRAY_NAME) == null)
            System.out.println("--- byte array is null");
        if (internalRep.get(LONG_NAME) == null)
            System.out.println("--- Long is null");
        if (internalRep.get(CHAR_NAME) == null)
            System.out.println("--- Char is null");
        if (internalRep.get(NEW_FIELD_NAME) == null)
            System.out.println("--- " + NEW_FIELD_NAME + " is null");

        fields.put(NEW_FIELD_NAME, internalRep.get(NEW_FIELD_NAME));
        fields.put(STRING_NAME, internalRep.get(STRING_NAME));
        fields.put(BYTE_ARRAY_NAME, internalRep.get(BYTE_ARRAY_NAME));
        fields.put(LONG_NAME, ((Long)internalRep.get(LONG_NAME)).longValue());
        fields.put(CHAR_NAME, ((Character)internalRep.get(CHAR_NAME)).charValue());
            
        s.writeFields();
    }
}
