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

// GameSurrogate is a local object that handles all communications
// with the remote server.  I only put this in one place for
// development convenience.  I also got really mimimalistic (i.e. lazy)
// with exception handling.  You'd want to do more here.


package ibmspace.client;

import ibmspace.common.*;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import java.awt.event.ActionListener;
import java.lang.reflect.Array;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.Vector;

public class GameSurrogate
{
    private SpaceConquest           fSpaceConquest;
    private GameView                fGameView;
    private Planet[]                fPlanets;
    private PlanetView[]            fPlanetViews;
    private BudgetSummary[]         fPlanetBudgets;
    private int                     fGalaxySize;
    private ActionSource            fActionSource;
    private int                     fNumberOfPlanets = 0;

    private ShipDesign              fLatestDesign = null;
    private String                  fLatestDesignName = null;

    private Vector                  fJournies;
    private Vector                  fMessages = null;

    private String                  fName = "";

    private InitialContext context ;

    public GameSurrogate (String name)
    {
        fName = name;
        fActionSource = new ActionSource ();
        fGameView = null;
        fPlanets = null;
        fPlanetViews = null;
        fPlanetBudgets = null;
        fSpaceConquest = lookupServer ();
        fJournies = new Vector ();
    }

    public SpaceConquest lookupServer ()
    {
        SpaceConquest server = null;

        // Pass system properties with -D option of java command, e.g.
        // -Djava.naming.factory.initial=<name of factory to use>
        // -Djava.naming.provider.url=iiop://<hostname>

        try
            {
                if ( System.getSecurityManager() == null ) {
                    System.setSecurityManager (new RMISecurityManager());
                }

                Hashtable<String,?> env = new Hashtable<String,String> ();

                context = new InitialContext (env);
                Object o = context.lookup ("SpaceConquest");
                server = (SpaceConquest)PortableRemoteObject.narrow (o,SpaceConquest.class);
                System.out.println ("Connected to server.");
            }
        catch (Exception e)
            {
                System.out.println ("Problem looking up server!");
                System.out.println ("exception: " + e.getMessage ());
                e.printStackTrace ();
            }

        return server;
    }

    public void joinGame ()
    {
        System.out.println ("Joining game...");
        fGameView = null;

        try
            {
                fGameView = fSpaceConquest.joinGame (fName);
                if ( fGameView != null ) fGameView.test ();
                updatePlanetMap ();
            }
        catch (RemoteException e)
            {
                System.out.println ("Problem joining game!");
                fGameView = null;
            }

        if ( fGameView == null ) {
            System.out.println ("Problem joining game: null GameView returned!");
        }
    }

    public void updatePlanetMap ()
    {
        if ( fSpaceConquest == null || fGameView == null ) return;

        int i = 0;
        int numPlanets = 0;

        try
            {
                if ( fNumberOfPlanets == 0 ) {
                    System.out.println ("Initializing planet map.");

                    fPlanets = fSpaceConquest.getGalaxyMap ();


                    /*
                      int length = fSpaceConquest.getNumberOfPlanets ();
                      fPlanets = new Planet [length];
                      for ( int p=0; p<length; p++ ) {
                      fPlanets[p] = fSpaceConquest.getPlanet (p);
                      }
                    */
        

                    fNumberOfPlanets = Array.getLength (fPlanets);
                    fPlanetViews = new PlanetView [fNumberOfPlanets];
                    fPlanetBudgets = new BudgetSummary [fNumberOfPlanets];
                    for (i=0; i<fNumberOfPlanets; i++) {
                        fPlanetViews[i] = null;
                        fPlanetBudgets[i] = null;
                    }

                }

                for (i=0; i<fNumberOfPlanets; i++) {
                    PlanetView planet = fGameView.getPlanet (fPlanets[i].getID());
                    String name = planet.getName ();
                    fPlanetBudgets[i] = fGameView.getPlanetBudget (fPlanets[i].getID());
                    if ( planet != null ) {
                        fPlanetViews[i] = planet;
                    }

                }

            }
        catch (RemoteException e)
            {
                System.out.println ("Problem updating planet map!");
                System.out.println ("RemoteException: " + e.getMessage ());
                e.printStackTrace ();
            }
    }

    public int getNumberOfPlanets ()
    {
        if ( fGameView == null ) return 0;
        return Array.getLength (fPlanets);
    }

    public void takeTurn ()
    {
        if ( fGameView == null ) return;

        try
            {
                fMessages = fGameView.takeTurn ();
            }
        catch (RemoteException e)
            {
                System.out.println ("Problem taking turn!");
            }

        updatePlanetMap ();
        fActionSource.notifyListeners (this, "New Turn");
    }

    public Vector getMessages ()
    {
        return fMessages;
    }



    //
    // Planet Management
    //

    public ID getHome ()
    {
        if ( fGameView == null ) return null;

        try
            {
                return fGameView.getHome ();
            }
        catch (RemoteException e)
            {
                System.out.println ("Problem getting main budget summary!");
                return null;
            }
    }


    public PlanetView getPlanet (int i)
    {
        if ( fGameView == null ) return null;
        return fPlanetViews[i];
    }

    public PlanetView getPlanet (ID planetID)
    {
        int index = getPlanetIndex (planetID);
        if ( index != -1 )
            return fPlanetViews[index];
        else
            return null;
    }


    public BudgetSummary getMainBudget ()
    {
        if ( fGameView == null ) return null;

        try
            {
                return fGameView.getMainBudget ();
            }
        catch (RemoteException e)
            {
                System.out.println ("Problem getting main budget summary!");
                return null;
            }
    }


    public BudgetSummary getTechBudget ()
    {
        if ( fGameView == null ) return null;

        try
            {
                return fGameView.getTechBudget ();
            }
        catch (RemoteException e)
            {
                System.out.println ("Problem getting tect budget summary!");
                return null;
            }
    }

    public BudgetSummary getPlanetBudget (ID planetID)
    {
        int index = getPlanetIndex (planetID);
        if ( index != -1 )
            return fPlanetBudgets [index];
        else
            return null;
    }

    public void setPlanetBudget (ID planetID, BudgetSummary budget)
    {
        int index = getPlanetIndex (planetID);
        if ( index != -1 )
            fPlanetBudgets[index] = budget;
    }

    public void pushPlanetBudgetData ()
    {
        try
            {
                for (int i=0; i<fNumberOfPlanets; i++) {
                    PlanetView planet = fPlanetViews[i];
                    BudgetSummary budget = fPlanetBudgets[i];
                    if ( budget != null && planet != null ) {
                        fGameView.setPlanetBudget (planet.getID(), budget);
                    }
                }
            }
        catch (RemoteException e)
            {
                System.out.println ("Problem writing planet budget data!");
            }
    }




    public void setMainBudget (BudgetSummary budget)
    {
        if ( fGameView == null ) return;

        try
            {
                fGameView.setMainBudget (budget);
            }
        catch (RemoteException e)
            {
                System.out.println ("Problem setting main budget summary!");
            }

    }

    public void setTechBudget (BudgetSummary budget)
    {
        if ( fGameView == null ) return;

        try
            {
                fGameView.setTechBudget (budget);
            }
        catch (RemoteException e)
            {
                System.out.println ("Problem setting tech budget summary!");
            }

    }


    public TechProfile getTechProfile ()
    {
        if ( fGameView == null ) return null;

        try
            {
                return fGameView.getTechProfile ();
            }
        catch (RemoteException e)
            {
                System.out.println ("Problem getting tech profile!");
                return null;
            }
    }

    public long getShipSavings ()
    {
        if ( fGameView == null ) return 0;

        try
            {
                return fGameView.getShipSavings ();
            }
        catch (RemoteException e)
            {
                System.out.println ("Problem getting ship savings!");
                return 0;
            }
    }

    public long getShipMetal ()
    {
        if ( fGameView == null ) return 0;

        try
            {
                return fGameView.getShipMetal ();
            }
        catch (RemoteException e)
            {
                System.out.println ("Problem getting ship metal!");
                return 0;
            }
    }

    public long getIncome ()
    {
        if ( fGameView == null ) return 0;

        try
            {
                return fGameView.getIncome ();
            }
        catch (RemoteException e)
            {
                System.out.println ("Problem getting income!");
                return 0;
            }
    }

  
    public long getCalls ()
    {
        if ( fGameView == null ) return 0;

        try
            {
                return fGameView.getCalls ();
            }
        catch (RemoteException e)
            {
                System.out.println ("Problem getting number of IIOP calls!");
                return 0;
            }
    }

    private boolean isNewerTechnology ()
    {
        if ( fLatestDesign == null ) return true;

        TechProfile current = getTechProfile ();
        TechProfile shipTech = fLatestDesign.getTechProfile ();
        if ( current.getRange() > shipTech.getRange() ) return true;
        if ( current.getSpeed() > shipTech.getSpeed() ) return true;
        if ( current.getWeapons() > shipTech.getWeapons() ) return true;
        if ( current.getShields() > shipTech.getShields() ) return true;
        if ( current.getMini() > shipTech.getMini() ) return true;
        return false;
    }


    public void buildFleet (ShipDesign design, int number, ID station)
    {
        try
            {
                fGameView.buildFleet (design, number, station);
            }
        catch (RemoteException e)
            {
                System.out.println ("Problem building ships!");
            }
    }

    public void scrapFleet (ID fleetID)
    {
        try
            {
                fGameView.scrapFleet (fleetID);
            }
        catch (RemoteException e)
            {
                System.out.println ("Problem scrapping fleet!");
            }
    }

    public ID[] getFleetsAt (ID planetID)
    {
        try
            {
                return fGameView.getFleetsAt (planetID);
            }
        catch (RemoteException e)
            {
                System.out.println ("Problem getting list of fleets at planet!");
                return null;
            }
    }

    public void sendFleet (ID fleetID, ID destinationID)
    {
        try
            {
                fGameView.sendFleet (fleetID, destinationID);
            }
        catch (RemoteException e)
            {
                System.out.println ("Problem getting list of fleets at planet!");
            }
    }


    public Fleet getFleet (ID fleetID)
    {
        try
            {
                return fGameView.getFleet (fleetID);
            }
        catch (RemoteException e)
            {
                System.out.println ("Problem getting fleet summary!");
                return null;
            }
    }


    public void updateJourneys ()
    {
        try
            {
                if ( fJournies == null ) System.out.println ("fJournies is null");
                if ( fGameView == null ) System.out.println ("fGameView is null");
                fJournies.removeAllElements ();
                ID[] journies = fGameView.getAllJournies ();
                if ( journies != null ) {
                    for (int i=0; i<Array.getLength(journies); i++) {
                        if ( journies[i] == null ) System.out.println ("journies[i] is null");
                        System.out.println ("getting journey");
                        Journey journey = fGameView.getJourney (journies[i]);
                        fJournies.addElement (journey);
                    }
                }
            }
        catch (RemoteException e)
            {
                System.out.println ("Problem updating journies!");
            }
    }

    public int getNumberOfJournies ()
    {
        return fJournies.size ();
    }

    public Journey getJourney (int i)
    {
        return (Journey)fJournies.elementAt (i);
    }

    public int getPlanetIndex (ID planetID)
    {
        if ( planetID != null ) {
            for (int i=0; i<fNumberOfPlanets; i++) {
                if (fPlanetViews[i].getID().equals(planetID)) {
                    return i;
                }
            }
        }
        return -1;
    }

    public void abandonPlanet (ID planetID)
    {
        try
            {
                fGameView.abandonPlanet (planetID);
            }
        catch (RemoteException e)
            {
            }
    }

    public void surrender ()
    {
        try
            {
                fGameView.quit ();
            }
        catch (RemoteException e)
            {
            }
    }

    public void addActionListener (ActionListener listener)
    {
        fActionSource.addActionListener (listener);
    }

    public void removeActionListener (ActionListener listener)
    {
        fActionSource.removeActionListener (listener);
    }

}
