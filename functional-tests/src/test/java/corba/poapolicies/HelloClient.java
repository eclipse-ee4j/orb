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

package corba.poapolicies;

import HelloStuff.Hello;
import HelloStuff.HelloHelper;
import Util.CreationMethods;
import Util.Factory;

public class HelloClient {
    public static Hello createHello(CreationMethods c, Factory f) {
        System.out.println("createHello");
        String id = HelloHelper.id();
        System.out.println("id: " + id);

        System.out.println("Factory class: " + f.getClass().getName());

        org.omg.CORBA.Object obj = f.create(id, "corba.poapolicies.HelloImpl", c);

        System.out.println("Created object");

        Hello result = HelloHelper.narrow(obj);
        
        System.out.println("narrowed it");

        return result;

        /*
        return HelloHelper.narrow(f.create(HelloHelper.id(),
                                           "HelloImpl",
                                           c));
        */
    }

    static final void invoke(Hello h) {
        System.out.println(h.hi());
    }
    
    public static void main(String[] args) {
        
        try {

            System.out.println("Client starting");

            Utility u = new Utility(args);
            Factory f = u.readFactory();

            System.out.println("readFactory");
            

            System.out.println("invoke 1");

            Hello h1 =
                createHello(CreationMethods.EXPLICIT_ACTIVATION_WITH_POA_ASSIGNED_OIDS,
                            f);

            System.out.println("created 1, now invoking");

            invoke(h1);

            System.out.println("invoke 2");

            Hello h2 =
                createHello(CreationMethods.EXPLICIT_ACTIVATION_WITH_USER_ASSIGNED_OIDS,
                            f);
            invoke(h2);

            System.out.println("invoke 3");

            Hello h3 =
                createHello(CreationMethods.CREATE_REFERENCE_BEFORE_ACTIVATION_WITH_POA_ASSIGNED_OIDS,
                            f);
            invoke(h3);

            System.out.println("invoke 4");

            Hello h4 =
                createHello(CreationMethods.CREATE_REFERENCE_BEFORE_ACTIVATION_WITH_USER_ASSIGNED_OIDS,
                            f);
            invoke(h4);

            System.out.println("Calling overAndOut");

            f.overAndOut();

            System.out.println("Client finished");

        } catch (Exception e) {
            System.err.println("Client level");
            e.printStackTrace();
            try {
                System.err.flush();
            } catch (Exception ex) {}
            System.exit(1);
        } 
    }
}

