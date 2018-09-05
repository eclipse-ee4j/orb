/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.omg.PortableServer;

/**
 * Allows dynamic handling of object invocations.  POA-based DSI
 * servants inherit from the 
 * standard <code>DynamicImplementation</code> class, this class inherits 
 * from the <code>Servant</code> class. Based on IDL to Java spec.
 * CORBA V 2.3.1 ptc/00-01-08.pdf.
 */
abstract public class DynamicImplementation extends Servant {

/**
 * Receives requests issued to any CORBA object 
 * incarnated by the DSI servant and performs the processing 
 * necessary to execute the request.
 * @param <code>request</code> the request issued to the CORBA object.
 */
    abstract public void invoke(org.omg.CORBA.ServerRequest request);
}
