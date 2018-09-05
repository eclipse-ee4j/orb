/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.custom;

import java.rmi.Remote ;
import java.rmi.RemoteException ;

/**
 * Simple interface to send an Object and get it as a reply.  (Sometimes
 * replies fail even when requests don't.)
 */
public interface Verifier extends java.rmi.Remote 
{
    public Object verifyTransmission(Object input)
        throws RemoteException;
}
