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
