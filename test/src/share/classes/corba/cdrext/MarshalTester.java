/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.cdrext;

import java.rmi.Remote;
import java.io.*;
import java.util.*;

/**
 * Class with all basic fields plus a recursive, Object,
 * and Serializable field.  Missing Externalizable field.
 * This is useful for testing CORBA type code compatibility
 * for when a parameter or return value is Object,
 * Serializable, or Externalizable.
 *
 * Note that we don't actually use the TypeCode for
 * our unmarshaling.  It might be used by another Java ORB,
 * but it would definitely be necessary for a non-Java
 * ORB if sent a MarshalTester in an Any.
 */
public class MarshalTester implements Serializable {
    public byte byteField;
    public boolean booleanField;
    public short shortField;
    public int intField;
    public long longField;
    public float floatField;
    public double doubleField;

    public char charField;
    public String stringField;
        
    public byte[] byteArrayField;
    public boolean[] booleanArrayField;
    public short[] shortArrayField;
    public int[] intArrayField;
    public long[] longArrayField;
    public float[] floatArrayField;
    public double[] doubleArrayField;

    public char[] charArrayField;
    public String[] stringArrayField;

    public MarshalTester recursiveField;

    public Object objectField;
    public Serializable serializableField;
    public Externalizable externalizableField;

    public Remote remoteField;

    public boolean equals(Object obj) {
        MarshalTester other = (MarshalTester)obj;

        return byteField == other.byteField &&
            booleanField == other.booleanField &&
            shortField == other.shortField &&
            intField == other.intField &&
            longField == other.longField &&
            floatField == other.floatField &&
            doubleField == other.doubleField &&
            charField == other.charField &&
            stringField.equals(other.stringField) &&
            Arrays.equals(byteArrayField, other.byteArrayField) &&
            Arrays.equals(booleanArrayField, other.booleanArrayField) &&
            Arrays.equals(shortArrayField, other.shortArrayField) &&
            Arrays.equals(intArrayField, other.intArrayField) &&
            Arrays.equals(longArrayField, other.longArrayField) &&
            Arrays.equals(floatArrayField, other.floatArrayField) &&
            Arrays.equals(doubleArrayField, other.doubleArrayField) &&
            Arrays.equals(charArrayField, other.charArrayField) &&
            Arrays.equals(stringArrayField, other.stringArrayField) &&
            other.recursiveField == other &&
            objectField.equals(other.objectField) &&
            serializableField.equals(other.serializableField) &&
            externalizableField.equals(other.externalizableField) &&
            remoteField.equals(other.remoteField);
    }

    /**
     * Separate initialization from constructor so we know the
     * marshaling code isn't cheating by invoking default
     * constructor.
     */
    public void init(Remote remoteField) {
        byteField = (byte)83;
        booleanField = true;
        shortField = (short)5912;
        intField = 9035;
        longField = (long)949241;
        floatField = (float)35.2;
        doubleField = (double)3590.421;

        charField = '\u6D77';
        stringField = "\u6D77\u6D77\u6D77";
            
        byteArrayField = new byte[] { (byte)241,
                                      (byte)59,
                                      (byte)59,
                                      (byte)0,
                                      (byte)53 };
            
        booleanArrayField = new boolean[] { false, true };

        shortArrayField = new short[] { (short)943,
                                        (short)0,
                                        (short)3512,
                                        (short)35 };

        intArrayField = new int[] { 35123, 943, -203012, 0, 2312 };

        longArrayField = new long[] { (long)2412,
                                      (long)-203,
                                      (long)0,
                                      (long)241 };

        floatArrayField = new float[] { (float)32.3,
                                        (float)912.231,
                                        (float)0.0,
                                        (float)234.11 };

        doubleArrayField = new double[] { (double)3412.21,
                                          (double)243.22,
                                          (double)0.0,
                                          (double)23.1 };

        charArrayField = new char[] { 'A', '\u6D77', 'x' };

        stringArrayField = new String[] { "This is a test",
                                          stringField,
                                          "This is another test" };

        recursiveField = this;

        objectField = new Integer(52);
        serializableField = new Integer(59);

        this.remoteField = remoteField;

        externalizableField 
            = new TestExternalizable(90283091824L,
                                     "I like \u65E5\u672C",
                                     59021,
                                     '\u7A7A');
    }

    public MarshalTester() {}
}
