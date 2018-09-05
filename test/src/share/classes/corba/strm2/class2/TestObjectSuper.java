/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

import java.io.*;

public class TestObjectSuper implements Serializable
{
    private static final long serialVersionUID = 6234419445336614908L;

    private int dataS1;
    private char dataS2;
    private Long dataS3;

    public TestObjectSuper() {
        dataS1 = 23910;
        dataS2 = '\u6A5F';
        dataS3 = new Long(999211L);
    }

    public boolean equals(Object obj) {
        try {
            TestObjectSuper other = (TestObjectSuper)obj;
            if (other == null)
                return false;

            return (defaultedValues() || other.defaultedValues()) ||
                (dataS1 == other.dataS1 &&
                 dataS2 == other.dataS2 &&
                 dataS3.equals(other.dataS3));
        } catch (ClassCastException cce) {
            return false;
        }
    }

    private boolean defaultedValues() {
        return dataS1 == 0 && (int)dataS2 == 0 && dataS3 == null;
    }

    public String toString() {
        return 
            (super.getClass().equals(Object.class) ? "" : super.toString())
            + " [TestObjectSuper dataS1=" + dataS1
            + ", dataS2=" + (int)dataS2
            + ", dataS3=" + dataS3
            + "]";
    }

    private void writeObject(java.io.ObjectOutputStream out)
        throws IOException
    {
        System.out.println("TestObjectSuper writeObject begin");

        out.defaultWriteObject();

//         try {
//             out.defaultWriteObject();
//             // Should throw an error for calling this twice

//             throw new Error("Error -- should not allow defWrObj call twice");
//         } catch (IOException ex) {
//             ex.printStackTrace();
//             // Should throw this
//         }

        System.out.println("TestObjectSuper writeObject end");
    }

    private void readObject(java.io.ObjectInputStream is)
        throws IOException, ClassNotFoundException 
    {
        System.out.println("TestObjectSuper readObject begin");

        is.defaultReadObject();

//         try {
//             is.defaultReadObject();
            
//             // Should throw an error for calling this twice

//             throw new Error("Error -- should not allow defRdObj call twice");
//         } catch (IOException ex) {
//             ex.printStackTrace();
//             // Should throw this
//         }

//         try {
//             is.readFields();

//             // Should throw an error for reading defaults twice

//             throw new Error("Error -- should not allow default read twice");

//         } catch (IOException ex) {
//             ex.printStackTrace();
//             // Should throw this
//         }

        System.out.println("TestObjectSuper readObject end");
    }
}

