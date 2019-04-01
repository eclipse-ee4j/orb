/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.corba.ee.impl.transport;

import com.sun.corba.ee.impl.oa.poa.Policies;
import com.sun.corba.ee.spi.ior.IORTemplate;
import java.net.Socket;

import com.sun.corba.ee.spi.orb.ORB;
import org.glassfish.pfl.basic.func.UnaryVoidFunction;

/**
 * This version of an acceptor is the other half of CorbaAcceptorLazyImpl. The idea is that AcceptOnly will NOT
 * contribute to an IORTemplate, and will actually accept from a ServerSocket (and so it must initialize the server
 * socket and close it). The LazyImpl will contribute to an IORTemplate, and will not actually accept, but does the
 * actual processing of sockets from the server socket.
 *
 * @author ken
 */
public class AcceptorAcceptOnlyImpl extends AcceptorImpl {
    private UnaryVoidFunction<Socket> operation;

    public AcceptorAcceptOnlyImpl(ORB orb, int port, String name, String type, UnaryVoidFunction<Socket> operation) {
        super(orb, port, name, type);
        this.operation = operation;
    }

    @Override
    public void accept() {
        operation.evaluate(getAcceptedSocket());
    }

    @Override
    public void addToIORTemplate(IORTemplate iorTemplate, Policies policies, String codebase) {
        // does nothing in this case.
    }
}
