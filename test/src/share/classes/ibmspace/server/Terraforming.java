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


public class Terraforming implements Investment, java.io.Serializable
{
    // Cost in dollars to move temp 10% towards ideal
    public static double TERRAFORMING_COST = 1000.0;

    PlanetImpl    fPlanet;
    double        fIdealTemp;

    public Terraforming (PlanetImpl planet, double idealTemp)
    {
        fPlanet = planet;
        fIdealTemp = idealTemp;
    }

    public String getName ()
    {
        return "Terraforming";
    }

    public void invest (long dollars)
    {
        if ( dollars > 0 ) {
            double d = dollars;
            double temp = fPlanet.getTemp ();
            int diff = (int)(temp - fIdealTemp);
            int absdiff = Math.abs (diff);
            //int sign = diff/absdiff;

            if ( absdiff == 0 ) {
                fPlanet.setTemp (fIdealTemp);
            } else {
                double efficiency = 1.0/absdiff;
                double percentChange = d/1000.0 * 0.1 * efficiency;

                if ( percentChange > 1 ) {
                    percentChange = 1;
                }

                double newTemp = temp*(1-percentChange) + fIdealTemp*percentChange;
                fPlanet.setTemp (newTemp);
            }
        }
    }

}
