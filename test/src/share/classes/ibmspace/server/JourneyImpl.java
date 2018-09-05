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

import ibmspace.common.ID;
import ibmspace.common.Journey;
import ibmspace.common.Fleet;
import ibmspace.common.Planet;


public class JourneyImpl implements Journey, java.io.Serializable
{
    //
    // Private Data Members
    //

    private ID          fID;
    private PlanetImpl  fOrigin;
    private PlanetImpl  fDestination;
    private FleetImpl   fFleet;
    private int         fTotalDistance;
    private int         fDistanceTraveled;
    private boolean     fIsComplete;

  //
  // Constructor
  //

    public JourneyImpl (FleetImpl fleet, PlanetImpl origin, PlanetImpl destination)
    {
        fID = new ID ();
        fFleet = fleet;
        fOrigin = origin;
        fDestination = destination;
        fTotalDistance = fOrigin.distanceTo (fDestination);
        fDistanceTraveled = 0;
        fIsComplete = false;
    }

    //
    // Journey Interface Methods
    //

    public ID getID ()
    {
        return fID;
    }

    public Planet getOrigin ()
    {
        return fOrigin;
    }

    public Planet getDestination ()
    {
        return fDestination;
    }

    public double getPercentComplete ()
    {
        return (double)fDistanceTraveled/(double)fTotalDistance;
    }

    //
    // JourneyImpl Methods
    //

    public FleetImpl getFleet ()
    {
        return fFleet;
    }

    public int getTotalDistance ()
    {
        return fTotalDistance;
    }

    public int getDistanceTraveled ()
    {
        return fDistanceTraveled;
    }

    public int getRemainingDistance ()
    {
        return fTotalDistance = fDistanceTraveled;
    }

    public boolean isComplete ()
    {
        return fIsComplete;
    }

    //
    // Turn Taking
    //

    public void moveFleet ()
    {
        if ( fDistanceTraveled == 0 ) {
            fOrigin.removeFleet (fFleet);
            fFleet.setStation (null);
            fFleet.setJourney (this);
        }

        fDistanceTraveled += fFleet.getSpeed ();
        fFleet.move (fDistanceTraveled);
    
        if ( fDistanceTraveled >= fTotalDistance ) {
            fIsComplete = true;
            fDestination.acceptOrbit (fFleet);
            fFleet.setJourney (null);
        }
    }

}
