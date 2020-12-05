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

public class ComplexTestObjectXXX implements Serializable
{
    static Random r = new Random();
    int fInt;
    long fLong;
    float fFloat;
    double fDouble;
    String fString;
    
    Object fHandleAsAny = null;
    
    ComplexTestObjectOne fOne = null;
    ComplexTestObjectTwo fTwo = null;

    public ComplexTestObjectXXX()
    {
        fInt = r.nextInt();
        fLong = r.nextLong();
        fFloat = r.nextFloat();
        fDouble = r.nextDouble();
        fString = new String(fInt +""+ fLong +""+ fFloat +""+ fDouble);
        fHandleAsAny = new ComplexTestObjectOne();
        fOne = new ComplexTestObjectOne();
        fTwo = new ComplexTestObjectTwo();
    }

    public boolean equals(Object o)
    {
        try
            {
                ComplexTestObjectXXX ctbo = (ComplexTestObjectXXX)o;
                return ((ctbo.fString.equals(fString)) && (ctbo.fInt == fInt) && 
                        (ctbo.fLong == fLong) && (ctbo.fFloat == fFloat) && 
                        (ctbo.fDouble == fDouble)
                        && (ctbo.fHandleAsAny.equals(fHandleAsAny))
                        && (ctbo.fOne.equals(fOne)) 
                        && (ctbo.fTwo.equals(fTwo))
                        );
            }
        catch(Exception e)
            {
                return false;
            }
    }

    public String toString()
    {
        return new String("fInt="+fInt+"; fLong="+fLong+"; fFloat="+fFloat+"; fDouble="+fDouble/*+"; fString="+fString*/);
    }
}
