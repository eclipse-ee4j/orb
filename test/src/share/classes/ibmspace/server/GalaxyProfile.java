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

package ibmspace.server;

public class GalaxyProfile implements java.io.Serializable
{
    // Types
    public static final int   GRID = 0;
    public static final int   RANDOM = 1;
    public static final int   CIRCLE = 2;
    public static final int   RING = 3;
    public static final int   SPIRAL = 4;

  // Sizes
    public static final int   SMALL = 0;
    public static final int   MEDIUM = 1;
    public static final int   LARGE = 2;

    // Density
    public static final int   DENSE = 0;
    public static final int   SPARSE = 1;

    private int fType;
    private int fSize;
    private int fDensity;


    public GalaxyProfile (int type, int size, int density)
    {
        fType = type;
        fSize = size;
        fDensity = density;
    }

    public int getType ()
    {
        return fType;
    }

    public int getSize ()
    {
        return fSize;
    }

    public int getDensity ()
    {
        return fDensity;
    }
  
}
