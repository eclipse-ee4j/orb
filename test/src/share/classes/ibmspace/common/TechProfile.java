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

package ibmspace.common;


public class TechProfile implements java.io.Serializable
{
    private int     fRange;
    private int     fSpeed;
    private int     fWeapons;
    private int     fShields;
    private int     fMini;

    public TechProfile (int r, int sp, int w, int sh, int m)
    {
        fRange = r;
        fSpeed = sp;
        fWeapons = w;
        fShields = sh;
        fMini = m;
    }

    public int getRange ()
    {
        return fRange;
    }

    public int getSpeed ()
    {
        return fSpeed;
    }

    public int getWeapons ()
    {
        return fWeapons;
    }

    public int getShields ()
    {
        return fShields;
    }

    public int getMini ()
    {
        return fMini;
    }

    public String toString ()
    {
        String s = "(";
        s += String.valueOf (fRange) + ",";
        s += String.valueOf (fSpeed) + ",";
        s += String.valueOf (fWeapons) + ",";
        s += String.valueOf (fShields) + ",";
        s += String.valueOf (fMini) + ",";
        s += ")";
        return s;
    }

}
