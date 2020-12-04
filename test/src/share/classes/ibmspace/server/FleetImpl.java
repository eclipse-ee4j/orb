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

package ibmspace.server;

import ibmspace.common.ShipDesign;
import ibmspace.common.ID;
import ibmspace.common.Fleet;
import ibmspace.common.Planet;
import ibmspace.common.Journey;


public class FleetImpl implements Fleet, java.io.Serializable
{
    private ID          fID;
    private ShipDesign  fDesign;
    private int         fId;
    private int         fShipsInFleet;
    private Player      fOwner;
    private int         fDamageLevel;
    private int         fFuelLevel;
    private PlanetImpl  fStation;
    private JourneyImpl fJourney;

    public FleetImpl (ShipDesign design, int shipsInFleet, Player owner)
    {
        fID = new ID ();
        fDesign = design;
        fShipsInFleet = shipsInFleet;
        fOwner = owner;
        fStation = null;
        fJourney = null;
        replentishFuel ();
    }

    //
    // Fleet Interface Methods
    //

    public ID getID ()
    {
        return fID;
    }

    public ShipDesign getDesign ()
    {
        return fDesign;
    }

    public int getNumberInFleet ()
    {
        return fShipsInFleet;
    }

    public int getMaximumRange ()
    {
        return fDesign.getTechProfile().getRange ();
    }

    public int getCurrentRange ()
    {
        if ( isSatelite() )
            return 0;
        else
            return fFuelLevel;
    }
  
    public boolean isOnJourney ()
    {
        return ( fJourney == null ? false : true );
    }

    public String toString ()
    {
        String s = String.valueOf (fShipsInFleet);
        s += " " + fDesign.getName ();

        switch ( fDesign.getType () ) {
        case ShipDesign.COLONY_SHIP:
            s += " Colony Ship ";
            break;
        case ShipDesign.SCOUT:
            s += " Scout ";
            break;
        case ShipDesign.FIGHTER:
            s += " Fighter ";
            break;
        case ShipDesign.SATELITE:
            s += " Satelite ";
            break;
        }

        s += fDesign.getTechProfile().toString();
        s += " " + String.valueOf (getCurrentRange());

        if ( isOnJourney() ) {
            s += " *";
        }

        return s;
    }


    //
    // FleetImpl Methods
    //

    public Player getOwner ()
    {
        return fOwner;
    }

    public PlanetImpl getStation ()
    {
        return fStation;
    }

    public JourneyImpl getJourney ()
    {
        return fJourney;
    }

    public void setStation (PlanetImpl station)
    {
        fStation = station;
    }

    public void setJourney (JourneyImpl journey)
    {
        fJourney = journey;
    }

    public int getShipsInFleet ()
    {
        return fShipsInFleet;
    }

    public int getSpeed ()
    {
        return fDesign.getTechProfile().getSpeed ();
    }

    public boolean isColonyShip ()
    {
        if ( fDesign.getType() == ShipDesign.COLONY_SHIP )
            return true;
        else
            return false;
    }

    public boolean isFighter ()
    {
        if ( fDesign.getType() == ShipDesign.FIGHTER )
            return true;
        else
            return false;
    }

    public boolean isSatelite ()
    {
        if ( fDesign.getType() == ShipDesign.SATELITE )
            return true;
        else
            return false;
    }

    public void move (int distance)
    {
        fFuelLevel -= distance;
        fFuelLevel = Math.max (fFuelLevel, 0);
    }

    public void replentishFuel ()
    {
        fFuelLevel = getMaximumRange ();
    }

    public long getScrapMetal ()
    {
        return fDesign.getScrapMetalPerShip() * fShipsInFleet;
    }

    public int getStrenth ()
    {
        int s = fDesign.getTechProfile().getWeapons() * fShipsInFleet;
        if ( isFighter() ) s = (int)(s * 1.5);
        return s;
    }

    public int getResistance ()
    {
        int r = fDesign.getTechProfile().getShields() * fShipsInFleet;
        if ( isSatelite() ) r = (int)(r * 1.5);
        return r;
    }

    public int getDamageLevel ()
    {
        return fDamageLevel;
    }

}
