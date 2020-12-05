/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
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

package javax.rmi.CORBA.serialization;

import java.util.*;
import java.io.*;

public class ComplexTestObjectOne implements Serializable
{
    static Random r = new Random();
    public int fInt;
    public long fLong;
    public float fFloat;
    public double fDouble;
    public String fString;
    public java.lang.Object fMarshalAsAny;
    
    public ComplexTestObjectOne()
    {
        fInt = 1;//r.nextInt();
        fLong = 2;//r.nextLong();
        fFloat = 3;//r.nextFloat();
        fDouble = 4;//r.nextDouble();
        fString = new String(fInt +""+ fLong +""+ fFloat +""+ fDouble);
        fMarshalAsAny = (Object)new Character('S');
    }

    public boolean equals(Object o)
    {
        try
            {
                ComplexTestObjectOne ctbo = (ComplexTestObjectOne)o;
                return ((ctbo.fString.equals(fString)) && (ctbo.fInt == fInt) && 
                        (ctbo.fLong == fLong) && (ctbo.fFloat == fFloat) && 
                        (ctbo.fDouble == fDouble) && (ctbo.fMarshalAsAny.equals(fMarshalAsAny)));
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
}
