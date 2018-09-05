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

import java.io.Serializable;

public class ShipDesign implements Serializable
{
    public static final int   COLONY_SHIP = 0;
    public static final int   SCOUT = 1;
    public static final int   FIGHTER = 2;
    public static final int   SATELITE = 3;

    private String            fName;
    private int               fType;
    private TechProfile       fTechProfile;


    public ShipDesign (String name, int type, TechProfile techProfile)
    {
        fName = name;
        fType = type;
        fTechProfile = techProfile;
    }

    public String getName ()
    {
        return fName;
    }

    public int getType ()
    {
        return fType;
    }

    public TechProfile getTechProfile ()
    {
        return fTechProfile;
    }

    public long getDesignCost ()
    {
        return 2000;
    }

    public long getCostPerShip ()
    {
        long cost = 0;

        switch ( fType )
            {
            case COLONY_SHIP:
                cost = 10000;
                break;
            case SCOUT:
                cost = 2000;
                break;
            case FIGHTER:
                cost = 5000;
                break;
            case SATELITE:
                cost = 1000;
                break;
            }

        return cost;
    }

    public long getMetalPerShip ()
    {
        long metal = 0;

        switch ( fType )
            {
            case COLONY_SHIP:
                metal = 20000;
                break;
            case SCOUT:
                metal = 500;
                break;
            case FIGHTER:
                metal = 1000;
                break;
            case SATELITE:
                metal = 200;
                break;
            }

        return metal / getTechProfile().getMini ();
    }

    public long getScrapMetalPerShip ()
    {
        return (long)(0.7 * (double)getMetalPerShip ());
    }

}
