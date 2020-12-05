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
// Created       : 2005 Apr 27 (Wed) 15:46:01 by Harold Carr.
// Last Modified : 2005 Jun 01 (Wed) 11:03:52 by Harold Carr.
//

package corba.folb_8_1;

import java.io.File;
import java.util.Properties;


import com.sun.corba.ee.spi.transport.TransportManager;
import com.sun.corba.ee.spi.transport.ContactInfo;

import com.sun.corba.ee.spi.orb.ORB ;
import com.sun.corba.ee.spi.transport.SocketInfo;

import com.sun.corba.ee.spi.misc.ORBConstants;

import com.sun.corba.ee.impl.transport.ContactInfoListImpl;
//import com.sun.corba.ee.impl.orbutil.newtimer.generated.TimingPoints;
import com.sun.corba.ee.spi.protocol.ClientDelegate;
import org.glassfish.pfl.tf.timer.spi.LogEventHandler;
import org.glassfish.pfl.tf.timer.spi.Timer;
import org.glassfish.pfl.tf.timer.spi.TimerEventController;
import org.glassfish.pfl.tf.timer.spi.TimerFactory;
import org.glassfish.pfl.tf.timer.spi.TimerManager;

// import corba.framework.TimerUtils ;

/**
 * @author Harold Carr
 */
public class ClientForTiming
{
    public static boolean debug = true;

    public static final int NUMBER_OF_WARMUP_LOOPS = 5;//20000;
    public static final int NUMBER_OF_TIMING_LOOPS = 5;// 5003;

    public static final String baseMsg = ClientForTiming.class.getName();
    public static I iRef;
    public static I2 i2Ref;
    public static String[] av;

    public static ORB orb;
/*
    public static TimerManager<TimingPoints> tm ;
    public static TimerFactory tf ;
    public static TimerEventController controller ;
    public static LogEventHandler log ;
    public static TimingPoints tp ;
*/
    public static Timer totalTestTime ;
    public static Timer clientInvoke ;

    public static IIOPPrimaryToContactInfoImpl primaryToContactInfo;
    public static ContactInfo serverPrimaryContactInfo;
    public static ContactInfo serverClearTextEntry;

    // These are used as self-documenting values below.
    public static final boolean FAILOVER_SUPPORT = true;
    public static final boolean FAILOVER         = true;
    public static final boolean CACHE            = true;

    public static void main(String[] argv)
    {
        // NOTE: must give to ORB.init - contains ORBInitialPort
        av = argv;

        try {

            boolean failoverSupport = false;
            boolean failover        = false;
            boolean cache           = false;

            for (int i = 0; i < av.length; ++i) {
                String x = av[i];
                System.out.print(x +" ");
                if (x.equals(Common.FAILOVER_SUPPORT)) {
                    failoverSupport = true;
                } else if (x.equals(Common.FAILOVER)) {
                    failover        = true;
                } else if (x.equals(Common.CACHE)) {
                    cache           = true;
                }
            }
            System.out.println();

            runTest(failoverSupport, failover, cache);

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

    // USE THIS FOR VERIFY THE TEST IS ACCURATE.
    // DO NOT USE THIS FOR TIMING!
    // IF YOU RUN ALL IN SAME VN SESSION THEN HotSpot OPTIMIZATIONS
    // GET TRIGGERED AT VARIOUS POINTS, MAKING LATER RESULTS DIFFERENT
    // FROM EARLIER RESULTS BECAUSE OF HOTSPOT - NOT BECAUSE OF WHAT IS BEING
    // TIMED.
    public static void runAllTests()
        throws Exception
    {
        runTest(!FAILOVER_SUPPORT, !FAILOVER, !CACHE);
        runTest( FAILOVER_SUPPORT, !FAILOVER, !CACHE);
        runTest( FAILOVER_SUPPORT, !FAILOVER,  CACHE);
        runTest( FAILOVER_SUPPORT,  FAILOVER, !CACHE);
        runTest( FAILOVER_SUPPORT,  FAILOVER,  CACHE);
    }

    public static void runTest(boolean failoverSupport,
                               boolean failover,
                               boolean cache)
        throws Exception
    {
        BEGIN(failoverSupport, failover, cache);

        // don't print to log files - including warmup runs.
        if (! debug) {
            Common.timing = true;
        }

        setup(failoverSupport, cache);

        primaryToContactInfo = (IIOPPrimaryToContactInfoImpl)
            orb.getORBData().getIIOPPrimaryToContactInfo();

        if (! debug && cache) {
            // don't print debug info.
            // Note: this must be done *AFTER* setup *BEFORE* lookup.
            // Setup to get the ORB.  But before it is used (i.e., lookup).
            primaryToContactInfo.setDebugChecked(true);
            primaryToContactInfo.setDebug(false);
        }

        lookup();

        if (failover) {
            setupFailover(cache);
        }

        ////////////////////////////////////////////////////
        // Warmup loop.

        loop(NUMBER_OF_WARMUP_LOOPS, failover, cache);

        ////////////////////////////////////////////////////
        // Timing loop.

        setTimerPoints(true);
        //controller.enter( totalTestTime ) ;

        loop(NUMBER_OF_TIMING_LOOPS, failover, cache);

        //controller.exit( totalTestTime ) ;
        setTimerPoints(false);

        File file = new File(makeFileName(failoverSupport, failover, cache));
        file.delete();
//      TimerUtils.dumpLogToFile( tf, log, file ) ;
        //log.clear() ;

        END(failoverSupport, failover, cache);
    }

    public static void setupFailover(boolean cache)
        throws Exception
    {
        dprint();
        dprint("starting setupFailover");
        dprint();

        // Careful with the tricky bug fix about ContactInfos in the map
        // pointing to a different IOR than the object on the current request.

        // Make a call on the loop object.  Why?
        // 1. Init the failover cache (when caching) with the entry to i2.
        // 2. To cause i2Ref's contact info list to be created (it's lazy).
        // Causes a conection to be established.

        dprint();
        dprint("--------- Calling i2Ref to prime pump.");
        i2Ref.foo(1);

        serverPrimaryContactInfo =
            ((ContactInfoListImpl)
             ((ClientDelegate)
              ((_I2Stub)i2Ref)._get_delegate())
              .getContactInfoList()).getPrimaryContactInfo();
        
        dprint("--------- i2Ref primaryContactInfo: " 
               + serverPrimaryContactInfo);
        dprint();

        if (cache) {

            // REVISIT: entry not found.  See README.txt
            dprint();
            dprint("finding entry for key: " + serverPrimaryContactInfo);
            dprint("in map: " + primaryToContactInfo.map);

            serverClearTextEntry = (ContactInfo)
                primaryToContactInfo.map.get(serverClearTextEntry);

            dprint("found entry: " + serverClearTextEntry);
            dprint();
        }

        // Kill a listener and its connections.
        // This assumes the one we kill is the one on which we make the request

        dprint();
        dprint("kill the IIOP_CLEAR_TEXT listener and its connections:");
        iRef.unregister(SocketInfo.IIOP_CLEAR_TEXT);
        dprint("  killed: " + Client.lastConnectionUsed);
        dprint();
        Thread.sleep(5000); // Give the server time to kill.

        if (cache) {

            // Make the map not contain an entry for the server.

            Object o=primaryToContactInfo.map.remove(serverPrimaryContactInfo);
            dprint();
            dprint("Removed from map:"
                   + "  key: "  + serverPrimaryContactInfo
                   + " entry: " + serverClearTextEntry);
        }
        dprint();
        dprint("ending setupFailover");
        dprint();
    }

    public static void revertCache()
    {
        dprint();
        dprint("revertCache");
        dprint();
        dprint("key: " + serverPrimaryContactInfo);
        Object o = primaryToContactInfo.map.remove(serverPrimaryContactInfo);
        dprint("was mapped to: " + o);
        primaryToContactInfo.map.put(serverPrimaryContactInfo,
                                     serverClearTextEntry);
        dprint("now mapped to: " 
               + primaryToContactInfo.map.get(serverClearTextEntry));
        dprint();
    }

    public static void killFailedOverToConnection()
        throws Exception
    {
        TransportManager transportManager = orb.getTransportManager();

        dprint();
        dprint("killFailedOverToConnection");
        dprint("key: " + serverPrimaryContactInfo);
        dprint("removing connection: " + Client.lastConnectionUsed);
        dprint("from connection cache BEFORE: " + 
               transportManager
                   .getOutboundConnectionCache(serverPrimaryContactInfo));
        Client.lastConnectionUsed.close();
        Thread.sleep(1000);
        dprint();
        dprint("from connection cache AFTER: " + 
               transportManager
                   .getOutboundConnectionCache(serverPrimaryContactInfo));
        dprint();
    }

    public static void loop(int times, boolean failover, boolean cache)
        throws Exception
    {
        dprint();
        dprint("starting loop");
        dprint();

        for (int i = 0 ; i < times; ++i) {
            //controller.enter( clientInvoke ) ;
            i2Ref.foo(1);
            //controller.exit( clientInvoke ) ;

            if (failover) {
                // We want the connection setup to show up in failover time.
                killFailedOverToConnection();
                if (cache) {
                    // We want failover to happen on every call.
                    revertCache();
                }
            }
        }

        dprint();
        dprint("ending loop");
        dprint();
    }

    public static void setup(boolean failoverSupport,
                             boolean cache)
        throws Exception
    {
        dprint();
        dprint("starting setup");
        dprint();

        Properties props = new Properties();
        props.setProperty( ORBConstants.TIMING_POINTS_ENABLED, "true" ) ;

        if (failoverSupport) {
            props.setProperty(ORBConstants.IOR_TO_SOCKET_INFO_CLASS_PROPERTY,
                              IORToSocketInfoImpl.class.getName());
            props.setProperty(ORBConstants.SOCKET_FACTORY_CLASS_PROPERTY,
                              SocketFactoryImpl.class.getName());
            if (cache) {
                props.setProperty(ORBConstants.IIOP_PRIMARY_TO_CONTACT_INFO_CLASS_PROPERTY,
                                  IIOPPrimaryToContactInfoImpl.class.getName());
            }
        }

        Client.setProperties(props);
        orb = (ORB) ORB.init(av, props);
/*
        tm = orb.makeTimerManager( TimingPoints.class ) ;
        tf = tm.factory() ;
        tp = tm.points() ;

        // Set up the log event handler.
        controller = tm.controller() ;
        log = tf.makeLogEventHandler( "TestLogger" ) ;
        controller.register( log ) ;

        // Create the extra timers for this test
        totalTestTime = tf.makeTimer( "totalTestTime", "Total Test Execution Time" ) ;
        clientInvoke = tf.makeTimer( "clientInvoke", "Execution Time of Client Invoke" ) ;
*/
        // Not available: tp.transport().add( totalTestTime ) ;
        // Not available: tp.transport().add( clientInvoke ) ;

        dprint();
        dprint("ending setup");
        dprint();
    }

    public static void lookup()
        throws Exception
    {
        dprint();
        dprint("starting lookup");
        dprint();

        iRef =
            IHelper.narrow(
                Common.getNameService(orb)
                    .resolve(Common.makeNameComponent(Common.serverName1)));

        i2Ref =
            I2Helper.narrow(
                Common.getNameService(orb)
                    .resolve(Common.makeNameComponent(Common.serverName2)));

        dprint();
        dprint("ending lookup");
        dprint();
    }

    public static void BEGIN(boolean failoverSupport,
                             boolean failover,
                             boolean cache)
    {
        System.out.println();
        System.out.println("------------------------------------------------");
        System.out.print("BEGIN ");
        BEGINEND(failoverSupport, failover, cache);
        System.out.println();
    }

    public static void END(boolean failoverSupport,
                           boolean failover,
                           boolean cache)
    {
        System.out.println();
        System.out.print("END ");
        BEGINEND(failoverSupport, failover, cache);
        System.out.println("------------------------------------------------");
        System.out.println();
    }

    public static void BEGINEND(boolean failoverSupport,
                                boolean failover,
                                boolean cache)
    {
        System.out.println(withOrWithout(failoverSupport, "FailoverSupport"));
        System.out.println(withOrWithout(failover, "Failover"));
        System.out.println(withOrWithout(cache, "Cache"));
    }

    public static String withOrWithout(boolean with, String msg)
    {
        if (with) {
            return msg;
        }
        return "NO" + msg;
    }

    public static String makeFileName(boolean failoverSupport,
                                      boolean failover,
                                      boolean cache)
    {
        String directory = 
            System.getProperty("output.dir")
            + System.getProperty("file.separator");
        return 
            //"/tmp/"
            directory
            + withOrWithout(failoverSupport, "FailoverSupport")
            + withOrWithout(failover, "Failover")
            + withOrWithout(cache, "Cache")
            + ".log";
    }

    public static void setTimerPoints(boolean x)
    {
        /* tp.transport not available.
        if (x)
            tp.transport().enable() ;
        else
            tp.transport().disable() ;
        */
    }

    public static void dprint()
    {
        dprint("");
    }
    public static void dprint(String msg)
    {
        if (debug) {
            System.out.println(msg);
        }
    }
}

// End of file.
