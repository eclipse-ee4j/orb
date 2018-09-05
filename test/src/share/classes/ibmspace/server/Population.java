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


public class Population implements java.io.Serializable
{
    private PlanetImpl  fPlanet;
    private long        fChildren;
    private long        fAdults;
    private long        fSeniors;
    private long        fWillDie;

    public Population (PlanetImpl planet, long initialSize)
    {
        fPlanet = planet;

        if ( initialSize <= 40 ) {
            fChildren = 0;
            fAdults = initialSize;
            fSeniors = 0;
            fWillDie = 0;
        } else {
            fChildren = initialSize / 4;
            fAdults = initialSize / 4;
            fSeniors = initialSize / 4;
            fWillDie = initialSize - (fChildren + fAdults + fSeniors);
        }
    }

    public long size ()
    {
        return (fChildren + fAdults + fSeniors + fWillDie);
    }

    public long getIdealIncome ()
    {
        return (long)((fAdults+fSeniors)/8);
    }


    public void  grow (double suitability)
    {
        long s = size ();
        double offspring = Math.min(Math.max(1.0+(double)(50000*suitability /size()),1.01),1.6);

        fChildren = (long)(fAdults * offspring);
        fSeniors = (long)(fAdults * suitability);
        fAdults = (long)(fChildren * suitability);
        fWillDie = (long)(fSeniors * suitability);

        if ( size() < 10 ) {
            fChildren = 0;
            fAdults = 10;
            fSeniors = 0;
            fWillDie = 0;
        }
    }

}
