/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.corba.ee.impl.transport;

import com.sun.corba.ee.impl.oa.poa.Policies;
import com.sun.corba.ee.spi.ior.IORTemplate;
import com.sun.corba.ee.spi.orb.ORB;

import java.net.Socket;

import org.glassfish.pfl.basic.func.UnaryVoidFunction ;

/** This version of an acceptor is the other half of CorbaAcceptorLazyImpl.
 * The idea is that AcceptOnly will NOT contribute to an IORTemplate, and will
 * actually accept from a ServerSocket (and so it must initialize the
 * server socket and close it).  The LazyImpl will contribute to an IORTemplate,
 * and will not actually accept, but does the actual processing of sockets
 * from the server socket.
 *
 * @author ken
 */
public class AcceptorAcceptOnlyImpl extends AcceptorImpl {
    private UnaryVoidFunction<Socket> operation ;

    public AcceptorAcceptOnlyImpl( ORB orb, int port,
        String name, String type, UnaryVoidFunction<Socket> operation ) {
        super( orb, port, name, type ) ;
        this.operation = operation  ;
    }

    @Override
    public void accept() {
        operation.evaluate( getAcceptedSocket() ) ;
    }

    @Override
    public void addToIORTemplate(IORTemplate iorTemplate, Policies policies, String codebase) {
        // does nothing in this case.
    }
}
