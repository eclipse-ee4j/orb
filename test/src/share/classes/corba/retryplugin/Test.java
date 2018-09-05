/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

//
// Created       : 2005 Oct 05 (Wed) 13:49:03 by Harold Carr.
// Last Modified : 2005 Oct 05 (Wed) 15:08:09 by Harold Carr.
//

package corba.retryplugin;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author Harold Carr
 */
public interface Test
    extends Remote
{
    public int echo(int x)
        throws RemoteException;
}

// End of file.


