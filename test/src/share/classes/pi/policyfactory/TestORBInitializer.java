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

package pi.policyfactory;

import org.omg.PortableInterceptor.*;
import org.omg.PortableInterceptor.PolicyFactory;
import org.omg.PortableInterceptor.ORBInitInfoPackage.*;

import java.util.*;
import java.io.*;
import org.omg.CORBA.*;

public class TestORBInitializer extends LocalObject
    implements ORBInitializer
{
    public void pre_init (org.omg.PortableInterceptor.ORBInitInfo info) {
        System.out.println( "TestORBInitializer.pre_init() called..." );
        System.out.flush( );
    }

    /** pre_init registers 2 PolicyFactories with types 100, 1000 and 10000
     *  These types will be used in Positive tests to see the validity of
     *  ORB.create_policy() API.
     */
    public void post_init (org.omg.PortableInterceptor.ORBInitInfo info) {
        PolicyFactory policyFactory1000Plus = new PolicyFactoryThousandPlus( );
        PolicyFactory policyFactory100 = new PolicyFactoryHundred( );
        // Same PolicyFactory for types 1000 and 10000. create_policy() method
        // takes care of instantiating the right policy based on policy type. 
        info.register_policy_factory( 1000, policyFactory1000Plus );
        info.register_policy_factory( 10000, policyFactory1000Plus );
        info.register_policy_factory( 100, policyFactory100 );
        System.out.println( "TestORBInitializer.post_init() called..." );
        System.out.flush( );
    }
}
  
