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


public class GameProfile implements Identifiable, java.io.Serializable
{
    private String        fGalaxyName;
    private int           fNumberOfPlanets;
    private int           fNumberOfPlayers;
    private ID            fID;

    public GameProfile (String galaxyName, int numPlanets, int numPlayers)
    {
        String fGalaxyName = galaxyName;
        fNumberOfPlanets = numPlanets;
        fNumberOfPlayers = numPlayers;
        fID = new ID ();
    }

    public ID getID ()
    {
        return fID;
    }

    public String getGalaxyName ()
    {
        return fGalaxyName;
    }

    public int getNumberOfPlanets ()
    {
        return fNumberOfPlanets;
    }

    public int getNumberOfPlayers ()
    {
        return fNumberOfPlayers;
    }

}
