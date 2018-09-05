/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.adapteractivator;

import org.omg.CORBA.ORB;


public class HelloImpl extends HelloPOA {

        public String sayHello() {
                System.out.println("Hello : inside sayHello()");
                return "Hello";
        }

}



class CloseImpl extends ClosePOA {

        private ORB orb=null;

        public CloseImpl(ORB orb) {
                this.orb = orb;
        }

        public void shutdown() {
                System.out.println("PoaOperation : shutDown called");
                orb.shutdown(false);
        }

}




