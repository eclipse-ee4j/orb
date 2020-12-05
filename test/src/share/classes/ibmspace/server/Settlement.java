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

//
// A settlement represents the settlement of a planet by a player.
// Each player maitains a list of its current settlements and can
// use these to control and access the planet while settled.
//


public class Settlement implements java.io.Serializable
{
    private Player        fPlayer = null;
    private PlanetImpl    fPlanet = null;

    private Budget        fBudget = null;
    private Terraforming  fTerraforming = null;
    private Mining        fMining = null;
    private Population    fPopulation = null;

    public Settlement (Player player, PlanetImpl planet, long initialSize)
    {
        fPlayer = player;
        fPlanet = planet;

        if ( fPlanet == null ) {
            System.out.println ("fPlanet is null");
            throw new Error ("Settlement.Settlement () - fPlanet is null");
        }

        double temp = fPlayer.getIdealTemp ();

        fTerraforming = new Terraforming (fPlanet, temp);
        fMining = new Mining (fPlanet, fPlayer);
        fBudget = new Budget (fPlanet.getName());
        fBudget.addBudgetItem (new BudgetItem (fTerraforming, 50));
        fBudget.addBudgetItem (new BudgetItem (fMining, 50));

        fPopulation = new Population (fPlanet, initialSize);
    }

    public Player getOwner ()
    {
        return fPlayer;
    }

    public Budget getBudget ()
    {
        return fBudget;
    }

    public Population getPopulation ()
    {
        return fPopulation;
    }

    public long getIncome ()
    {
        if ( fPopulation == null ) System.out.println ("fPopulation is null!!");
        if ( fPlanet == null ) System.out.println ("fPlanet is null!!");
        if ( fPlayer == null ) System.out.println ("fPlayer is null!!");

        return (long)(fPopulation.getIdealIncome() * fPlanet.getSuitabilityFor(fPlayer));
    }

    public void growPopulation ()
    {
        fPopulation.grow (fPlanet.getSuitabilityFor(fPlayer));
    }

}
