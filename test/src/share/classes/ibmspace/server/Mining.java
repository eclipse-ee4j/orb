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


public class Mining implements Investment, java.io.Serializable
{
    public static double    MINING_COST = 0.1;

    private Player          fPlayer;
    private PlanetImpl      fPlanet;

    public Mining (PlanetImpl planet, Player player)
    {
        fPlayer = player;
        fPlanet = planet;
    }

    public String getName ()
    {
        return "Mining";
    }

    public void invest (long dollars)
    {
        long metal = 0;

        if ( dollars > 0 ) {
            long total = fPlanet.getMetal ();
            if ( total > 0 ) {
                double d = dollars;
                double efficiency = total / 10000.0;
                metal = (long)(d / MINING_COST * efficiency);

                if ( metal > total ) {
                    metal = total;
                }

                fPlanet.removeMetal (metal);
                fPlayer.addShipMetal (metal);
            }
        }
    }

}
