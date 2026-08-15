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

import ibmspace.common.ID;
import ibmspace.common.Journey;
import ibmspace.common.Fleet;
import ibmspace.common.Planet;

public class JourneyImpl implements Journey, java.io.Serializable {
    //
    // Private Data Members
    //

    private ID fID;
    private PlanetImpl fOrigin;
    private PlanetImpl fDestination;
    private FleetImpl fFleet;
    private int fTotalDistance;
    private int fDistanceTraveled;
    private boolean fIsComplete;

    //
    // Constructor
    //

    public JourneyImpl(FleetImpl fleet, PlanetImpl origin, PlanetImpl destination) {
        fID = new ID();
        fFleet = fleet;
        fOrigin = origin;
        fDestination = destination;
        fTotalDistance = fOrigin.distanceTo(fDestination);
        fDistanceTraveled = 0;
        fIsComplete = false;
    }

    //
    // Journey Interface Methods
    //

    public ID getID() {
        return fID;
    }

    public Planet getOrigin() {
        return fOrigin;
    }

    public Planet getDestination() {
        return fDestination;
    }

    public double getPercentComplete() {
        return (double) fDistanceTraveled / (double) fTotalDistance;
    }

    //
    // JourneyImpl Methods
    //

    public FleetImpl getFleet() {
        return fFleet;
    }

    public int getTotalDistance() {
        return fTotalDistance;
    }

    public int getDistanceTraveled() {
        return fDistanceTraveled;
    }

    public int getRemainingDistance() {
        return fTotalDistance = fDistanceTraveled;
    }

    public boolean isComplete() {
        return fIsComplete;
    }

    //
    // Turn Taking
    //

    public void moveFleet() {
        if (fDistanceTraveled == 0) {
            fOrigin.removeFleet(fFleet);
            fFleet.setStation(null);
            fFleet.setJourney(this);
        }

        fDistanceTraveled += fFleet.getSpeed();
        fFleet.move(fDistanceTraveled);

        if (fDistanceTraveled >= fTotalDistance) {
            fIsComplete = true;
            fDestination.acceptOrbit(fFleet);
            fFleet.setJourney(null);
        }
    }

}
