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

package hopper.h4670827;

public class TestConstants {
     public static final String ORBInitialPort = "2089";

     public static final String INSServiceName = "HelloService";

     // Flavor 1: iiop version and port specified
     public static final String corbalocURL1 = 
         "corbaloc:iiop:1.2@[::FFFF:10.5.32.14]:" 
         + ORBInitialPort + "/" + INSServiceName;

     // Flavor 2: iiop version not specified and port specified
     public static final String corbalocURL2 = 
         "corbaloc:iiop:[::FFFF:10.5.32.14]:" 
         + ORBInitialPort + "/" + INSServiceName;

     // Flavor 3: iiop version specified and port not specified
     public static final String corbalocURL3 = 
         "corbaloc:iiop:1.2@[::FFFF:10.5.32.14]:" 
         + ORBInitialPort + "/" + INSServiceName;

     // Flavor 4: iiop version not specified and port not specified
     public static final String corbalocURL4 = 
         "corbaloc:iiop:[::FFFF:10.5.32.14]:" 
         + ORBInitialPort + "/" + INSServiceName;

     // Flavor 5: negative test no colon
     public static final String corbalocURL5 = 
         "corbaloc:iiop:[::FFFF:10.5.32.14]" 
         + ORBInitialPort + "/" + INSServiceName;

     // Flavor 6: negative test no bracket and no colon
     public static final String corbalocURL6 = 
         "corbaloc:iiop:[::FFFF:10.5.32.14" 
         + ORBInitialPort + "/" + INSServiceName;
     public static final String returnString = "HELLO";

     public static Object[][] data = new Object[][] {
         { "testIIOPVersionAndPort", corbalocURL1, true },
         { "testIIOPNoVersionAndPort", corbalocURL2, true },
         { "testIIOPVersionAndNoPort", corbalocURL3, true },
         { "testIIOPNoVersionAndNoPort", corbalocURL4, true },
         { "testNoColon", corbalocURL5, false },
         { "testNoColonNoBracket", corbalocURL6, false },
     } ;
}
