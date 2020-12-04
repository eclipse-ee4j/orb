/*
 * Copyright (c) 1999, 2020 Oracle and/or its affiliates. All rights reserved.
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
 * @param request the request issued to the CORBA object.
 */
    abstract public void invoke(org.omg.CORBA.ServerRequest request);
}
