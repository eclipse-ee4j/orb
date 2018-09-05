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

import ibmspace.common.*;
import java.util.Vector;
import java.lang.reflect.Array;
import java.rmi.RemoteException;
import java.rmi.Remote;
import javax.rmi.PortableRemoteObject;

public class GameViewServer extends GameViewImpl implements RemoteGameView
{
    private Player      fPlayer;
    private Game        fGame;
    private Vector      fDesigns;
    private String[]    fOwners;
    private boolean     fQuit = false;

    public GameViewServer(Game game, Player player) throws RemoteException
    {
        super (game, player);
        PortableRemoteObject.exportObject (this);
    }

    public void quit () throws RemoteException
    {
        super.quit ();
        PortableRemoteObject.unexportObject (this);
    }

}
