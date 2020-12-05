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

import java.io.*;
import java.util.*;
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
    private transient List optDataS4;

    public TestObjectSuper() {
        dataS1 = 23910;
        dataS2 = '\u6A5F';
        dataS3 = new Long(999211L);
        optDataS1 = new Double((double)24124.23121);
        optDataS2 = 2412;
        optDataS3 = new BigInteger("982749812479812481242148998391", 10);

        optDataS4 = new Vector();
        optDataS4.add(optDataS1);
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
            + ", optDataS4=" + optDataS4
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
                 optDataS3.equals(other.optDataS3) &&
                 optDataS4.equals(other.optDataS4));

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
        out.writeObject(optDataS4);
    }

    private void readObject(java.io.ObjectInputStream is)
        throws IOException, ClassNotFoundException 
    {
        System.out.println("TestObjectSuper readObject begin");

        is.defaultReadObject();

        System.out.println("Read default fields");

        try {
            is.defaultReadObject();
            
            // Should throw an error for calling this twice

            throw new IOException("Error -- should not allow defRdObj call twice");
        } catch (IOException ex) {
            // Should throw this
        }

        try {
            is.readFields();

            // Should throw an error for reading defaults twice

            throw new IOException("Error -- should not allow default read twice");

        } catch (IOException ex) {
            // Should throw this
        }


        System.out.println("Reading optional Double");

        try {
            optDataS1 = (Double)is.readObject();
        } catch (OptionalDataException ode) {
            System.out.println("Defaulting");
            // Optional object data not present
            optDataS1 = new Double((double)24124.23121);
        }


        System.out.println("Reading optional int");

        try {
            optDataS2 = is.readInt();
        } catch (EOFException eof) {
            System.out.println("Defaulting");
            // Int wasn't on wire
            optDataS2 = 2412;
        }

        System.out.println("Reading optional BigInteger");

        try {
            optDataS3 = (BigInteger)is.readObject();
        } catch (OptionalDataException ode) {
            System.out.println("Defaulting");
            optDataS3 = new BigInteger("982749812479812481242148998391", 10);
        }

        System.out.println("Reading optional list");

        try {
            optDataS4 = (List)is.readObject();
        } catch (RuntimeException re) {
            re.printStackTrace();
            throw re;
        } catch (Error er) {
            er.printStackTrace();
            throw er;
        } catch (OptionalDataException ode) {

            System.out.println("Defaulting");

            optDataS4 = new Vector();
            optDataS4.add(optDataS1);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            throw ioe;
        }

        System.out.println("TestObjectSuper readObject end");
    }
}

