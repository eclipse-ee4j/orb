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
// Created       : 2001 Nov 10 (Sat) 12:47:05 by Harold Carr.
// Last Modified : 2001 Nov 10 (Sat) 12:47:46 by Harold Carr.
//

package corba.hcks;

import java.rmi.Remote; 
import java.rmi.RemoteException; 

public interface rmiiAttributesI
    extends 
        Remote 
{ 
    // Test JavaBeans patterns -> IDL attributes.
    Integer getInteger()
        throws
            RemoteException;

    void    setInteger(Integer x)
        throws
            RemoteException;

    boolean isTrue()
        throws
            RemoteException;

    boolean getTrue()
        throws
            RemoteException;

    void    setTrue(boolean x)
        throws
            RemoteException;

    // Test that this is NOT a JavaBeans pattern.
    Integer get()
        throws
            RemoteException;

    void set(Integer x)
        throws
            RemoteException;
}

// End of file.
