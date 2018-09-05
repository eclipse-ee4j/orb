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

import java.awt.Point;
import ibmspace.common.ID;
import ibmspace.common.PlanetView;



public class PlanetViewImpl implements PlanetView, java.io.Serializable
{
    private Player        fPlayer;
    private PlanetImpl    fPlanet;


    public PlanetViewImpl (Player player, PlanetImpl planet)
    {
        fPlayer = player;
        fPlanet = planet;
    }

    //
    // Planet Interface Methods
    //

    public ID getID ()
    {
        return fPlanet.getID ();
    }

    public String getName ()
    {
        return fPlanet.getName ();
    }

    public Point getCoordinates ()
    {
        return fPlanet.getCoordinates ();
    }

    public boolean hasSatelites ()
    {
        return fPlanet.hasSatelites ();
    }


    //
    // PlanetView Methods
    //

    public boolean isOwned ()
    {
        return ( fPlayer == fPlanet.getOwner() );
    }

    public long getMetal ()
    {
        return fPlanet.getMetal ();
    }

    public long getPopulation ()
    {
        if ( isOwned() ) {
            Settlement settlement = fPlanet.getSettlement ();
            if ( settlement != null ) {
                return settlement.getPopulation().size ();
            } else {
                return 0;
            }
        } else {
            return -1;
        }
    }

    public long getIncome ()
    {
        if ( isOwned() ) {
            Settlement settlement = fPlanet.getSettlement ();
            if ( settlement != null ) {
                return settlement.getIncome ();
            } else {
                return 0;
            }
        } else {
            return -1;
        }
    }

    public double getTemp ()
    {
        if ( isOwned() ) {
            return fPlayer.getRelativeTempFor (fPlanet.getTemp());
        } else {
            return -1;
        }
    }

    public double getGravity ()
    {
        if ( isOwned() ) {
            return fPlayer.getRelativeGravityFor (fPlanet.getGravity());
        } else {
            return -1;
        }
    }

    public double getSuitability ()
    {
        if ( isOwned() ) {
            return fPlanet.getSuitabilityFor (fPlayer);
        } else {
            return -1;
        }
    }

}

