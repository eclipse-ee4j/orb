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

package naming.pcosnaming;

import HelloApp.*;
import org.omg.CosNaming.*;
import org.omg.CORBA.*;
import java.util.Properties ;

public class helloClient
{
    public static void main(String args[])
    {
        try {
            // create and initialize the ORB
            ORB orb = ORB.init(args, System.getProperties());

            // get the root naming context
            org.omg.CORBA.Object objRef =
                orb.resolve_initial_references("NameService");
            NamingContext ncRef = NamingContextHelper.narrow(objRef);

            // resolve the Object Reference (Simple Case -One Level )
            NameComponent nc1 = new NameComponent("HelloObj1", "");
            NameComponent path1[] = {nc1};
            hello helloRef = helloHelper.narrow(ncRef.resolve(path1));
            helloRef.sayHello();

            // resolve the Object Reference (Little Complex Case -Two Level )
            NameComponent nc2 = new NameComponent("HelloContext1", "");

            NameComponent temppath[] = {nc2};
            NamingContext temp = (NamingContext) ncRef.resolve( temppath );
            System.out.println( "NC Resolve worked" );
            System.out.flush( );

            NameComponent nc3 = new NameComponent("HelloObj2", "");

            NameComponent temppath1[] = {nc3};
            helloRef = helloHelper.narrow(temp.resolve(temppath1));
            System.out.println( "First Resolve Worked" );
            System.out.flush( );
            helloRef.sayHello( );
/*

            NameComponent path2[] = {nc2, nc3};
            helloRef = helloHelper.narrow(ncRef.resolve(path2));
            System.out.println( "helloRef is resolved" );
            System.out.flush( );
            helloRef.sayHello();

            // resolve the Object Reference (Little Complex Case -Three Level )
            NameComponent nc4 = new NameComponent("HelloContext2", "");
            NameComponent nc5 = new NameComponent("HelloObj3", "");
            NameComponent path3[] = { nc2, nc4, nc5};
            helloRef = helloHelper.narrow(ncRef.resolve(path3));
            helloRef.sayHello();

*/
            //orb.shutdown(true);

        } catch (Exception e) {
            System.out.println("ERROR : " + e) ;
            e.printStackTrace(System.out);
            System.exit(1);
        }
    }
}
