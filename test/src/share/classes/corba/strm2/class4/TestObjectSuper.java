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
import java.math.*;

public class TestObjectSuper implements Serializable
{
    private static final long serialVersionUID = 6234419445336614908L;

    private int dataS1;
    private char dataS2;
    private Long dataS3;

    private transient Double optDataS1;
    private transient int optDataS2;
    private transient BigInteger optDataS3;

    public TestObjectSuper() {
        dataS1 = 23910;
        dataS2 = '\u6A5F';
        dataS3 = new Long(999211L);
        optDataS1 = new Double((double)24124.23121);
        optDataS2 = 2412;
        optDataS3 = new BigInteger("982749812479812481242148998391", 10);
    }

    public String toString() {
        return 
            (super.getClass().equals(Object.class) ? "" : super.toString())
            + " [TestObjectSuper dataS1=" + dataS1
            + ", dataS2=" + (int)dataS2
            + ", dataS3=" + dataS3
            + ", optDataS1=" + optDataS1
            + ", optDataS2=" + optDataS2
            + ", optDataS3=" + optDataS3
            + "]";
    }

    private boolean defaultedValues() {
        return dataS1 == 0 && (int)dataS2 == 0 && dataS3 == null;
    }

    public boolean equals(Object obj) {
        try {
            TestObjectSuper other = (TestObjectSuper)obj;
            if (other == null)
                return false;

            return (defaultedValues() || other.defaultedValues()) ||
                (dataS1 == other.dataS1 &&
                 dataS2 == other.dataS2 &&
                 dataS3.equals(other.dataS3) &&
                 optDataS1.equals(other.optDataS1) &&
                 optDataS2 == other.optDataS2 &&
                 optDataS3.equals(other.optDataS3));

        } catch (ClassCastException cce) {
            return false;
        }
    }

    private void writeObject(java.io.ObjectOutputStream out)
        throws IOException
    {
        out.defaultWriteObject();

        try {
            out.defaultWriteObject();
            // Should throw an error for calling this twice

            throw new IOException("Error -- should not allow defWrObj call twice");
        } catch (IOException ex) {
            // Should throw this
        }

        out.writeObject(optDataS1);
        out.writeInt(optDataS2);
        out.writeObject(optDataS3);
    }

    private void readObject(java.io.ObjectInputStream is)
        throws IOException, ClassNotFoundException 
    {
        is.defaultReadObject();

        try {
            is.defaultReadObject();
            
            // Should throw an error for calling this twice

            throw new IOException("Error -- should not allow defRdObj call twice");
        } catch (IOException ex) {
            // Should throw this
        }

/*        try {
            is.readFields();

            // Should throw an error for reading defaults twice

            throw new IOException("Error -- should not allow default read twice");

        } catch (IOException ex) {
            // Should throw this
        }
*/

        try {
            optDataS1 = (Double)is.readObject();
        } catch (OptionalDataException ode) {
            // Optional object data not present
            optDataS1 = new Double((double)24124.23121);
        }

        try {
            optDataS2 = is.readInt();
        } catch (EOFException eof) {
            // Int wasn't on wire
            optDataS2 = 2412;
        }

        try {
            optDataS3 = (BigInteger)is.readObject();
        } catch (OptionalDataException ode) {
            optDataS3 = new BigInteger("982749812479812481242148998391", 10);
        }
    }
}
