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


public class Technology implements Investment, java.io.Serializable
{
    private String      fName;
    private int         fLevel;
    private long        fInvestment;
    private long        fRequiredInvestment;

    static private int  UNIT_INVESTMENT = 1000;

    public Technology (String name, int initialLevel)
    {
        fName = name;
        fLevel = initialLevel;
        fInvestment = 0;
        fRequiredInvestment = (fLevel+1) * UNIT_INVESTMENT;
    }

    public String getName ()
    {
        return fName;
    }

    public void invest (long investment)
    {
        // formula:  1. each level requires level* UNIT_INVESTMENT to reach
        //           2. investments are dampened by a random percentage

        fInvestment += investment * Math.random () * 1.25;
        if ( fInvestment >= fRequiredInvestment ) {
            fLevel ++;
            fInvestment -= fRequiredInvestment;
            fRequiredInvestment = (fLevel+1) * UNIT_INVESTMENT;
        }

    }

    public int getLevel ()
    {
        return fLevel;
    }

}
