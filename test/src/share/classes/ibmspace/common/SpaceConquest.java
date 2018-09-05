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

// SpaceConquest is where you start.  It represents a game.  A subclass
// of this (in this project's case ibmspace.server.SpaceConquestServer)
// must be created to start a game.  Then through this interface a player
// can join or quit the game, and can also get some basic information
// about the game before or after joining.   Note that the server side
// of this game will never eliminate a player.  Therefore, either the
// player must choose to exit through the user interface, or the client
// must implement "decision logic" to force the user to quit when the
// user has no hope left of winning.  In either case, the client calls
// quitGame to quit.

// My idea is that eventually there will be another server for creating
// and enumerating games and that an associated client user interface
// would let you view a list of games and some information about these
// games before joining.  You can even visually see the galaxy before
// joining (and getGalaxyMap allows for this).


package ibmspace.common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface SpaceConquest extends Remote
{
    GameView      joinGame (String playerName) throws RemoteException;
    void          quitGame (GameView gameView) throws RemoteException;

    Planet[]      getGalaxyMap () throws RemoteException;
    int           getNumberOfPlanets () throws RemoteException;
    Planet        getPlanet (int index) throws RemoteException;
} 
