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

import java.awt.Dimension;
import java.awt.Point;
import java.util.Vector;
import ibmspace.common.ID;
import java.io.Serializable;

public class Galaxy implements Serializable
{
    private Dimension       fSize;
    private int             fNumberOfPlanets;
    private Vector          fPlanets;
    private int             fNextHomeWorld = 0;
    private PlanetImpl[]    fHomes = null;

    public Galaxy (int rows, int columns)
    {
        PlanetNames names = new PlanetNames ();
        fPlanets = new Vector ();
        int i = 0;

        for (int r=0; r<rows; r++) {
            for (int c=0; c<columns; c++) {
                double temp = PlanetImpl.RandomTemp ();
                double gravity = PlanetImpl.RandomGravity ();
                long metal = PlanetImpl.RandomMetal ();
                PlanetImpl p = new PlanetImpl (names.getName(), temp, gravity, metal);
                p.setCoordinates (new Point((r+1)*150-50,(c+1)*150-50));
                fPlanets.addElement (p);
            }
        }

        fNumberOfPlanets = rows * columns;
        fSize = new Dimension (rows*100, columns*100);

        fHomes = new PlanetImpl [fNumberOfPlanets];
        for (i=0; i<fNumberOfPlanets; i++) {
            fHomes[i] = null;
        }
    
    }

    public PlanetImpl createHomeWorldFor (Player player)
    {
        int homeIndex = 0;
        do {
            homeIndex = (int)(Math.random() * (fNumberOfPlanets-1));
        } while ( fHomes[homeIndex] != null );

        PlanetImpl home = (PlanetImpl)fPlanets.elementAt (homeIndex);
        home.setTemp (player.getIdealTemp());
        home.setGravity (player.getIdealGravity());
        home.setMetal (20000);
        fHomes[homeIndex] = home;
        return home;
    }

    public int getNumberOfPlanets ()
    {
        return fNumberOfPlanets;
    }

    public Dimension getSize ()
    {
        return fSize;
    }

    public PlanetImpl getPlanet (ID planetID)
    {
        if ( planetID != null ) {
            for (int i=0; i<fNumberOfPlanets; i++) {
                PlanetImpl p = (PlanetImpl)fPlanets.elementAt (i);
                if ( planetID.identifies (p) ) {
                    return p;
                }
            }
        }
        return null;
    }

    public Vector getPlanets ()
    {
        return fPlanets;
    }


}
