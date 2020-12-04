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

//
// Created       : 2002 Jul 19 (Fri) 14:50:37 by Harold Carr.
// Last Modified : 2005 Jun 01 (Wed) 11:03:42 by Harold Carr.
//

package corba.folb_8_1;

import java.util.Properties;

import org.omg.CORBA.ORB;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ORBInitializer;
import org.omg.PortableInterceptor.ORBInitInfo;

import com.sun.corba.ee.spi.transport.Connection;

import com.sun.corba.ee.spi.legacy.interceptor.RequestInfoExt;
import com.sun.corba.ee.spi.transport.SocketInfo;

import com.sun.corba.ee.spi.misc.ORBConstants;

/**
 * @author Harold Carr
 */
public class Client
    extends org.omg.CORBA.LocalObject
    implements ClientRequestInterceptor, ORBInitializer
{
    public static final String baseMsg = Client.class.getName();
    public static final String NO_CONNECTION = "no connection";
    public static boolean withSticky = false;
    public static boolean foundErrors = false;
    public static Connection lastConnectionUsed;
    public static String lastSocketTypeUsed;
    public static I iRef;
    public static I2 zero1;
    public static I2 zero2;
    public static ORB orb;

    public static void setProperties(Properties props)
    {
        props.setProperty( ORBConstants.DEBUG_PROPERTY,
            "subcontract,transport" ) ;

        //
        // Debugging flags.  Generally commented out.
        //
        /*
        props.setProperty(ORBConstants.DEBUG_PROPERTY,
                          "giop,transport,subcontract");
        */

        //
        // Register the class that knows how to find the information
        // on socket types X, Y and Z installed by the server side
        // IORInterceptor.
        //

        props.setProperty(ORBConstants.IOR_TO_SOCKET_INFO_CLASS_PROPERTY,
                          IORToSocketInfoImpl.class.getName());

        //
        // Register the socket factory that knows how to create
        // Sockets of type W X Y and Z.
        //

        props.setProperty(ORBConstants.SOCKET_FACTORY_CLASS_PROPERTY,
                          SocketFactoryImpl.class.getName());

        //
        // Register a client interceptor to see what connection
        // is being used for test (using a proprietary extension).
        //

        props.setProperty(ORBConstants.PI_ORB_INITIALIZER_CLASS_PREFIX
                          + Client.class.getName(),
                          "dummy");

        if (withSticky) {
            System.out.println("Adding sticky manager");
            //
            // Register a sticky manager and make sure it sticks.
            //
            props.setProperty(ORBConstants.IIOP_PRIMARY_TO_CONTACT_INFO_CLASS_PROPERTY,
                              IIOPPrimaryToContactInfoImpl.class.getName());
        }
    }

    public static void main(String[] av)
    {
        try {

            if (! ColocatedCS.isColocated) {
                Properties props = new Properties();
                setProperties(props);
                orb = ORB.init(av, props);
            }

            runTest();

            if (foundErrors) {
                throw new Exception("foundErrors");
            }

            System.out.println();
            System.out.println(baseMsg + ".main: PASSED");
            System.out.println(baseMsg + ".main: Test complete.");

        } catch (Throwable t) {
            System.out.println(baseMsg + ".main: FAILED");
            System.out.println(baseMsg + ".main: Test complete.");
            t.printStackTrace(System.out);
            System.exit(1);
        }
    }

    private static void runTest()
        throws Exception
    {
        System.out.println("================================================");
        if (withSticky) {
            System.out.println("WITH STICKY");
        } else {
            System.out.println("WITHOUT STICKY");
        }

        /* REVISIT - move to a separate test
        //////////////////////////////////////////////////
        // ZeroPortTest

        BEGIN("ZeroPortTest");

        zero1 =
            I2Helper.narrow(
                Common.getNameService(orb)
                    .resolve(Common.makeNameComponent(Common.zero1)));

        zero2 =
            I2Helper.narrow(
                Common.getNameService(orb)
                    .resolve(Common.makeNameComponent(Common.zero2)));

        zero1.m("10");
        zero2.m("11");
        zero1.m("12");
        zero2.m("13");

        END("ZeroPortTest");
        */

        //////////////////////////////////////////////////
        // Sticky test;

        BEGIN("Sticky Test");

        iRef =
            IHelper.narrow(
                Common.getNameService(orb)
                    .resolve(Common.makeNameComponent(Common.serverName1)));

        unregister(SocketInfo.IIOP_CLEAR_TEXT, iRef, false);

        for (int i = 0; i < Common.socketTypes.length - 1; i++) {
            unregister(Common.socketTypes[i], iRef, true);
        }

        System.out.println();
        System.out.println("DONE with unregister");
        System.out.println();

        //////////////////////////////////////////////////
        //
        // Test fallback.
        // 
        // This should stick on Z if sticky manager registered.
        // Otherwise it should go to W.
        //
        // Start up a W on the server.  See if it goes to Z or W.
        //

        System.out.println();
        printSeparator("-");
        System.out.println("TESTING FALLBACK - should stick to W without sticky, Z with sticky");
        System.out.println();

        BEGIN("register W");
        iRef.register(Common.socketTypes[0]);  // Register W
        Thread.sleep(5000);
        END("register W");


        BEGIN("unregister W if no sticky, Z if sticky present");

        // In the following we really don't care whether we unregister
        // W or Z.  What we care about is which TYPE of connection the
        // unregister request goes out on.  With a sticky it should be Z.
        // Without sticky it should be W.
        // REVISIT: if we separated control (the unregister) from the check
        // it would easier to understand the test.
        if (withSticky) {
            // unregister Z.
            unregister(Common.socketTypes[Common.socketTypes.length - 1],
                       iRef, true);
        } else {
            // Unregister W.
            unregister(Common.socketTypes[0], iRef, true); 
        }

        END("unregister W if no sticky, Z if sticky present");

        END("Sticky Test");

        orb.shutdown(false);
        orb.destroy();
    }

    /**
     * This both unregisters a specific socketType AND ensure that
     * the request goes out on that same socketType.
     * REVISIT: it would be better to separate the control (the unregister)
     * from the test.
     */
    private static void unregister(String socketType, I iRef, boolean checkP)
        throws Exception
    {
        BEGIN("unregister: " + socketType);
        iRef.unregister(socketType);
        END("unregister: " + socketType);

        BEGIN("Request after " + socketType + " unregistered - still on " + socketType + " because of explicit delay at server");
        System.out.println(iRef.m("After " + socketType + " unregistered"));
        if (checkP) {
            checkSocketType(socketType);
        }
        END("Request after " + socketType + " unregistered - still on " + socketType + " because of explicit delay at server");
        Thread.sleep(5000);
    }

    private static void checkSocketType(String socketType)
    {
        if (ColocatedCS.isColocated) {
            socketType = NO_CONNECTION;
        }

        if (socketType.equals(lastSocketTypeUsed)) {
            System.out.println();
            System.out.println("====== Used correct socketType: "
                               + lastSocketTypeUsed + " ======");
            System.out.println();
        } else {
            System.out.println();
            System.out.println("++++++ ERROR: INCORRECT SOCKETYPE: "
                               + lastSocketTypeUsed 
                               + "; expected: " 
                               + socketType
                               + " ++++++");
            System.out.println();
            foundErrors = true;
        }
    }

    private static void printSeparator(String s)
    {
        for (int i = 0; i < 70; i++) {
            System.out.print(s);
        }
        System.out.println();
    }

    //
    // Interceptor operations
    //

    public String name() 
    {
        return baseMsg; 
    }

    public void destroy() 
    {
    }

    //
    // ClientRequestInterceptor operations
    //

    public void send_request(ClientRequestInfo ri)
    {
        sopCR(baseMsg, "send_request", ri);
    }

    public void send_poll(ClientRequestInfo ri)
    {
        sopCR(baseMsg, "send_poll", ri);
    }

    public void receive_reply(ClientRequestInfo ri)
    {
        sopCR(baseMsg, "receive_reply", ri);
    }

    public void receive_exception(ClientRequestInfo ri)
    {
        sopCR(baseMsg, "receive_exception", ri);
    }

    public void receive_other(ClientRequestInfo ri)
    {
        sopCR(baseMsg, "receive_other", ri);
    }

    //
    // Utilities.
    //

    public static void sopCR(String clazz, String point, ClientRequestInfo ri)
    {
        try {
            if (! Common.timing) {
                System.out.println(clazz + "." + point + " " + ri.operation());
            }
            if (ri instanceof RequestInfoExt) {
                RequestInfoExt rie = (RequestInfoExt) ri;
                if (rie.connection() != null) {
                    if (! Common.timing) {
                        System.out.println("    request on connection: " + rie.connection());
                    }
                    lastConnectionUsed = (Connection) rie.connection();
                    lastSocketTypeUsed = (String)
                        Common.portToSocketType.get(
                          new Integer(rie.connection().getSocket().getPort()));
                    if (lastSocketTypeUsed == null) {
                        // NOTE: the last one is running on an emphemeral port
                        // so it does NOT map.  Just assume it.
                        // Also assume we don't look at the primary which
                        // is also NOT mapped.
                        lastSocketTypeUsed = 
                            Common.socketTypes[Common.socketTypes.length - 1];
                    }
                } else {
                    lastSocketTypeUsed = NO_CONNECTION;
                }
            }
        } catch (Throwable e) {
            System.out.println(baseMsg + "." + point + ": unexpected exception: " + e);
            e.printStackTrace(System.out);
            System.exit(1);
        }
    }

    public void pre_init(ORBInitInfo info)
    {
        try {
            Client interceptor = new Client();
            info.add_client_request_interceptor(interceptor);
            System.out.println(baseMsg + ".pre_init");
        } catch (Throwable t) {
            System.out.println(baseMsg + ": unexpected exception: " + t);
            t.printStackTrace(System.out);
            System.exit(1);
        }
    }

    public void post_init(ORBInitInfo info)
    {
    }

    public static void BEGIN(String msg)
    {
        System.out.println();
        printSeparator("-");
        System.out.println("BEGIN " + msg);
        System.out.println();

    }

    public static void END(String msg)
    {
        System.out.println();
        System.out.println("END " + msg);
        printSeparator("-");

    }

}

// End of file.
