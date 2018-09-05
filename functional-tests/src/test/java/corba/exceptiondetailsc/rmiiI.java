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
// Created       : 2003 Apr 10 (Thu) 11:30:38 by Harold Carr.
// Last Modified : 2003 Jul 28 (Mon) 09:26:35 by Harold Carr.
//

package corba.exceptiondetailsc;

import java.rmi.Remote; 
import java.rmi.RemoteException; 

public interface rmiiI
    extends 
        Remote 
{ 
    public void raiseSystemException(String x)
        throws
            RemoteException; 

    public void raiseUserException(String x)
        throws
            RemoteException,
            rmiiException;

    public void raiseRuntimeException(String x)
        throws
            RemoteException; 
}

// End of file.

