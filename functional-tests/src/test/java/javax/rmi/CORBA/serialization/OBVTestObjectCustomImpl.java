/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package javax.rmi.CORBA.serialization;

import java.util.*;
import java.io.*;

public class OBVTestObjectCustomImpl extends OBVTestObjectCustom {
    private static Random r = new Random();
    
    // We mark these as transients just so we can make sure during the test that
    // only by using the marshal and unmarshal methods could this class be serialized.
    private transient int fInt;
    private transient long fLong;
    private transient float fFloat;
    private transient double fDouble;
    private transient String fString;
    
    OBVTestObjectCustomImpl(int aInt, long aLong, float aFloat, double aDouble, String aString)
    {
        fInt = aInt;
        fLong = aLong;
        fFloat = aFloat;
        fDouble = aDouble;
        fString = aString;
    }

    public OBVTestObjectCustomImpl()
    {
        fInt = r.nextInt();
        fLong = r.nextLong();
        fFloat = r.nextFloat();
        fDouble = r.nextDouble();
        fString = new String(fInt +""+ fLong +""+ fFloat +""+ fDouble);
    }

    OBVTestObjectCustomImpl(org.omg.CORBA.portable.InputStream is)
    {
        // for unmarshalling only, called by the factory
    }

    public boolean equals(Object o)
    {
        try
            {
                OBVTestObjectCustomImpl ctbo = (OBVTestObjectCustomImpl)o;
                return ((ctbo.fString.equals(fString)) && (ctbo.fInt == fInt) &&
                        (ctbo.fLong == fLong) && (ctbo.fFloat == fFloat) && (ctbo.fDouble == fDouble)
                        );
            }
        catch(Exception e)
            {
                return false;
            }
    }

    public String toString()
    {
        return new String("fInt="+fInt+"; fLong="+fLong+"; fFloat="+fFloat+"; fDouble="+fDouble+"; fString="+fString);
    }

    public void marshal (org.omg.CORBA.DataOutputStream os)
    {
        os.write_long(fInt);
        os.write_longlong(fLong);
        os.write_float(fFloat);
        os.write_double(fDouble);
        os.write_string(fString);
    }
 
    public void unmarshal (org.omg.CORBA.DataInputStream is)
    {
        fInt = is.read_long();
        fLong = is.read_longlong();
        fFloat = is.read_float();
        fDouble = is.read_double();
        fString = is.read_string();

    }
}
