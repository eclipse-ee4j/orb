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

// This server models a given players view of the game.  Each
// player gets his/her own game view server from the space
// conquest server at the game's onset and then uses it to take
// all game actions and get all game status/data from then on.
// All game servers rely on the same underlying game and other
// game modeling objects the represent the same game for all
// players.  So essentially the game view server implements a
// a game security, so to speak, that only allows each player
// to see what is allowed to be seen.  Also, rather that returning
// game objects which would destroy this security, the game view
// server always returns game information by creating summary objects
// that capture only what players are allowed to see of the real
// game objects from which they are created.  All summary objects are
// part of the ibmspace.common package, e.g. BudgetSummary.

// Note:  This example is intended mainly to illustrate a non-trivial
// Java application using rmi-iiop for network communication.  It already
// uses rmi-iiop in many typical patterns of use and will be a test
// bed for more of these over time.  The game simulation, while not bad,
// has not been a primary focus of this example and can certainly be
// improved.

package ibmspace.server;

import ibmspace.common.*;
import java.util.Vector;
import java.lang.reflect.Array;
import java.rmi.RemoteException;


public class GameViewImpl implements GameView, java.io.Serializable
{
    private Player      fPlayer;
    private Game        fGame;
    private Vector      fDesigns;
    private String[]    fOwners;
    private boolean     fQuit = false;

    public GameViewImpl (Game game, Player player)
    {
        fGame = game;
        fPlayer = player;
        fDesigns = new Vector ();
        fOwners = new String [game.getNumberOfPlanets()];
        for (int i=0; i<Array.getLength(fOwners); i++) {
            fOwners[i] = "";
        }
    }

    public void test ()  throws RemoteException
    {
    }



    //
    // Working with budgets
    //

    public BudgetSummary getMainBudget () throws RemoteException
    {
        System.out.println ("getMainBudget");
        fGame.logCall ();
        return fPlayer.getBudget().createSummary ();
    }

    public BudgetSummary getTechBudget () throws RemoteException
    {
        System.out.println ("getTechBudget");
        fGame.logCall ();
        return fPlayer.getTechBudget().createSummary ();
    }

    public BudgetSummary getPlanetBudget (ID planetID) throws RemoteException
    {
        //System.out.println ("getPlanetBudget");
        fGame.logCall ();
        PlanetImpl planet = fGame.getPlanet (planetID);
        if ( planet != null && planet.getOwner() == fPlayer && planet.getSettlement() != null ) {
            Budget b = fPlayer.getPlanetBudget(planet);
            BudgetSummary bs = b.createSummary ();
            return bs;
        } else {
            return null;
        }
    }

    public void setMainBudget (BudgetSummary bs) throws RemoteException
    {
        System.out.println ("setMainBudget");
        fGame.logCall ();
        Budget b = fPlayer.getBudget();
        b.update (bs);
    }

    public void setTechBudget (BudgetSummary bs) throws RemoteException
    {
        System.out.println ("setTechBudget");
        fGame.logCall ();
        fPlayer.getTechBudget().update (bs);
    }

    public void setPlanetBudget (ID planetID, BudgetSummary bs) throws RemoteException
    {
        System.out.println ("setPlanetBudget");
        fGame.logCall ();
        PlanetImpl planet = fGame.getPlanet (planetID);
        if ( planet != null ) {
            Budget budget = fPlayer.getPlanetBudget(planet);
            if ( budget != null ) {
                budget.update (bs);
            }
        }
    }

    //
    // Working with assets
    //

    public long getShipSavings () throws RemoteException
    {
        System.out.println ("getShipSavings");
        fGame.logCall ();
        return fPlayer.getShipSavings().getSavings ();
    }

    public long getIncome () throws RemoteException
    {
        System.out.println ("getIncome");
        fGame.logCall ();
        return fGame.getIncomeFor (fPlayer);
    }

    public long getShipMetal () throws RemoteException
    {
        System.out.println ("getShipMetal");
        fGame.logCall ();
        return fPlayer.getShipMetal ();
    }

    public TechProfile getTechProfile () throws RemoteException
    {
        System.out.println ("getTechProfile");
        fGame.logCall ();
        return fPlayer.getResearchLab().getTechProfile ();
    }


    //
    // Working with ships
    //

    public ShipDesign designShip (String name, int type, TechProfile tech) throws RemoteException
    {
        System.out.println ("designShip");
        fGame.logCall ();
        ShipDesign design = new ShipDesign (name, type, tech);
        long cost = design.getDesignCost ();

        ShipSavings savings = fPlayer.getShipSavings ();

        if ( savings.getSavings() >= cost ) {
            savings.withdraw (cost);
            fDesigns.addElement (design);
            return design;
        } else {
            return null;
        }
    }

    public ID /*fleet*/
        buildFleet (ShipDesign design, int num, ID stationID) throws RemoteException
    {
        System.out.println ("buildFleet - " + num + " " + design.getName());

        fGame.logCall ();
        PlanetImpl station = fGame.getPlanet (stationID);

        if ( design != null && station != null && station.getOwner() == fPlayer ) {

            long cost = design.getCostPerShip() * num;
            long metal = design.getMetalPerShip() * num;

            ShipSavings savings = fPlayer.getShipSavings ();
            long shipMetal = fPlayer.getShipMetal ();

            if ( savings.getSavings() >= cost && shipMetal >= metal ) {
                savings.withdraw (cost);
                fPlayer.removeShipMetal (metal);
                FleetImpl fleet = new FleetImpl (design, num, fPlayer);
                fGame.addFleet (fleet, station);
                return fleet.getID();
            }

        }

        return null;
    }

    public void scrapFleet (ID fleetID) throws RemoteException
    {
        System.out.println ("scrapFleet");
        fGame.logCall ();
        FleetImpl fleet = fGame.getFleet (fleetID);
        if ( fleet != null && fleet.getOwner() == fPlayer ) {
            fGame.scrapFleet (fleet);
        }
    }

    public ID /* journey */
        sendFleet (ID fleetID, ID toPlanetID) throws RemoteException
    {
        System.out.println ("sendFleet");
        fGame.logCall ();
        fGame.cancelJourneyFor (fleetID);
        FleetImpl fleet = fGame.getFleet (fleetID);
        PlanetImpl toPlanet = fGame.getPlanet (toPlanetID);

        if (  fleet != null && fleet.getStation() != null &&
              fleet.getOwner() == fPlayer && toPlanet != null ) {
            JourneyImpl journey = fGame.sendFleet (fleet, toPlanet);
            return journey.getID ();
        } else {
            return null;
        }
    }

    public Fleet getFleet (ID fleetID) throws RemoteException
    {
        System.out.println ("getFleet");
        fGame.logCall ();
        return fGame.getFleet (fleetID);
    }

    public ID[] getFleetsAt (ID planetID) throws RemoteException
    {
        System.out.println ("getFleetsAt");
        fGame.logCall ();
        PlanetImpl planet = fGame.getPlanet (planetID);
        if ( planet != null && planet.getOwner() == fPlayer ) {
            FleetImpl[] fleets = planet.getFleets ();
            int size = Array.getLength (fleets);
            ID[] ids = new ID [size];
            for (int i=0; i<size; i++) {
                ids[i] = fleets[i].getID();
            }
            System.out.println ("  - returning " + size + " fleets");
            return ids;
        }
        return null;
    }


    public Journey getJourney (ID journeyOrFleetID) throws RemoteException
    {
        System.out.println ("getJourney");
        fGame.logCall ();
        return fGame.getJourney (journeyOrFleetID);
    }


    public ID[] getAllJournies () throws RemoteException
    {
        System.out.println ("getAllJournies");
        fGame.logCall ();
        return fGame.getJourniesFor (fPlayer);
    }


    //
    // Working with planets
    //

    public ID getHome () throws RemoteException
    {
        System.out.println ("getHome");
        fGame.logCall ();
        if ( fPlayer == null ) System.out.println ("fPlayer is null!!");
        return fPlayer.getHome().getID();
    }


    public PlanetView getPlanet (ID planetID) throws RemoteException
    {
        //System.out.println ("getPlanet");
        fGame.logCall ();
        PlanetImpl planet = fGame.getPlanet (planetID);
        if ( planet != null ) {
            return new PlanetViewImpl (fPlayer, planet);
        } else {
            return null;
        }
    }

    public void abandonPlanet (ID planetID) throws RemoteException
    {
        System.out.println ("abandonPlanet");
        fGame.logCall ();
        PlanetImpl planet = fGame.getPlanet (planetID);
        if ( planet != null && planet.getOwner() == fPlayer && planet.getSettlement() != null ) {
            fGame.killSettlementAt (planet);
        }
    }

    public void quit () throws RemoteException
    {
        System.out.println ("quit");
        fGame.eliminatePlayer (fPlayer);
        fQuit = true;
    }

    //
    // Turn taking
    //

    public Vector takeTurn ()  throws RemoteException
    {
        System.out.println ("takeTurn");
        if ( fQuit == false ) {
            fGame.logCall ();
            fGame.takeTurn (fPlayer);
            return fPlayer.getMessages ();
        } else {
            Vector m = new Vector ();
            m.addElement ("You quit!  Please stop playing!");
            return m;
        }
    }


    public long getCalls () throws RemoteException
    {
        System.out.println ("getCalls");
        return fGame.getCalls ();
    }
  

    //
    // Internal Helper Methods
    //

    protected ShipDesign getDesign (String name)
    {
        // Ship designs are used internally, but are not exposed to
        // clients at this time.

        for (int i=0; i<fDesigns.size(); i++) {
            ShipDesign design = (ShipDesign)fDesigns.elementAt(i);
            if ( design.getName().equals(name) ) {
                return design;
            }
        }
        return null;
    }



}
