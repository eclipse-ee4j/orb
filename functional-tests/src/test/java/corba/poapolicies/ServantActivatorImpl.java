/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.poapolicies;

//public class ServantActivatorImpl extends org.omg.CORBA.LocalObject implements ServantActivator {

import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.ServantActivatorPOA;

public class ServantActivatorImpl extends ServantActivatorPOA {
    public Servant incarnate(byte[] oid, POA adapter)
        throws org.omg.PortableServer.ForwardRequest {
        Servant servant = new HelloImpl(oid);
        if (HelloServer.debug)
            System.out.println("ServantActivatorImpl.incarnate ("
                               +" oid = "+oid
                               +" poa = "+adapter.the_name());

        return servant;
    }

    public void etherealize(byte[] oid, POA adapter, Servant servant,
                            boolean cleanup_in_progress,
                            boolean remaining_activations) {
        if (HelloServer.debug)
            System.out.println("ServantActivatorImpl.etherealize ("
                               +" oid = "+oid
                               +" cleanup = "+cleanup_in_progress
                               +" remaining_activations = "+remaining_activations
                               +")");
    }
}
