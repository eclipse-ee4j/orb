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

//
// This server is bound with the name server and is the first stop
// for all clients joining the game.  Each client uses this server
// to (1) get the basic map of the galaxy sans specific details they
// learn about planets later during play, and (2) get a player-
// specific game view server to represent a player-specific view of
// the game.  This models the fact that each player only sees the
// parts of the galaxy they experience through play.  So, for example,
// a player only sees the current temperature, etc, of a planer when
// he/she has ships stationed there or has a colony there.

package ibmspace.server;

import ibmspace.common.GameView;
import ibmspace.common.Planet;
import ibmspace.common.SpaceConquest;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import java.lang.reflect.Array;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.util.Vector;

public class SpaceConquestServer extends PortableRemoteObject implements SpaceConquest
{
    private Game      fGame;
    private int       fRequiredPlayers = 0;
    private int       fNumberOfPlayers = 0;
    private Vector    fViews;

    public SpaceConquestServer (int numPlayers) throws RemoteException
    {
        fGame = new Game (numPlayers);
        fViews = new Vector ();
        fRequiredPlayers = numPlayers;
    }

    public SpaceConquestServer () throws RemoteException
    {
        fGame = new Game (1);
        fViews = new Vector ();
        fRequiredPlayers = 1;
    }

    private synchronized void waitForAllPlayers ()
    {
        try
            {
                if ( fNumberOfPlayers < fRequiredPlayers ) {
                    wait ();
                } else {
                    notifyAll ();
                }
            }
        catch (InterruptedException e)
            {
            }
    }

    public synchronized GameView  joinGame (String playerName) throws RemoteException
    {
        if ( fGame.isGameStarted() ) {
            // Probably should throw an exception here
            // This is to prevent players from joining after the required
            // number of players have joined and the game has started.
            System.out.println (playerName + " tried to join started game");
            return null;
        }

        fGame.logCall ();
        double idealTemp = PlanetImpl.RandomTemp ();
        double idealGravity = PlanetImpl.RandomGravity ();
        Player player = new Player (playerName, idealTemp, idealGravity);
        fGame.addPlayer (player);
        fNumberOfPlayers++;

        GameView view;

        if ( fRequiredPlayers == 1 ) {
            view = new GameViewImpl (fGame, player);
        } else {
            view = new GameViewServer (fGame, player);
        }

        fViews.addElement (view);
        waitForAllPlayers ();
        return view;
    }

    public void quitGame (GameView gameView) throws RemoteException
    {
        gameView.quit ();
        fNumberOfPlayers--;
        if ( fNumberOfPlayers == 0 ) PortableRemoteObject.unexportObject (this);
    }

    public synchronized Planet[] getGalaxyMap () throws RemoteException
    {
        Planet[] planets = fGame.createGalaxyMap ();
        return planets;
    }

    public synchronized int getNumberOfPlanets () throws RemoteException
    {
        Planet[] planets = getGalaxyMap ();
        return Array.getLength (planets);
    }

    public synchronized Planet getPlanet (int index) throws RemoteException
    {
        Planet[] planets = getGalaxyMap ();
        return planets[index];
    }

    private static InitialContext context ;

    public static void main (String[] args)
    {
        int numPlayers = 1;

        if ( Array.getLength (args) != 0 ) {
            numPlayers = (Integer.valueOf (args[0])).intValue();
        }

        try
            {
                if ( System.getSecurityManager() == null ) {
                    System.setSecurityManager (new RMISecurityManager());
                }

                SpaceConquestServer obj = new SpaceConquestServer (numPlayers);

                // Pass system properties with -D option of java command, e.g.
                // -Djava.naming.factory.initial=< name of factory to use>

                context = new InitialContext ();
                context.rebind ("SpaceConquest", obj);
                System.out.println ("SpaceConquest server bound in registry");
            }
        catch (Exception e)
            {
                System.out.println ("SpaceConquest Server Exception: " + e.getMessage());
                e.printStackTrace ();
            }
    }


}
