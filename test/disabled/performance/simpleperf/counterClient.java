/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package performance.simpleperf;

import java.io.PrintStream ;
import javax.rmi.PortableRemoteObject ;
import java.rmi.RemoteException ;
import java.util.Hashtable ;
import java.util.Map ;
import java.util.Properties ;

import org.omg.CORBA.Policy ;
import org.omg.PortableServer.LifespanPolicyValue ;
import org.omg.PortableServer.POA ;
import org.omg.PortableServer.POAPackage.AdapterAlreadyExists ;
import org.omg.PortableServer.POAPackage.InvalidPolicy ;
import org.omg.PortableServer.POAPackage.WrongPolicy ;
import org.omg.PortableServer.RequestProcessingPolicyValue ;
import org.omg.PortableServer.Servant ;
import org.omg.PortableServer.ServantLocator ;
import org.omg.PortableServer.ServantRetentionPolicyValue ;
import org.omg.PortableServer.ServantLocatorPackage.CookieHolder ;

import com.sun.corba.ee.spi.extension.ServantCachingPolicy ;

import com.sun.corba.ee.spi.orb.ORB ;

import com.sun.corba.ee.spi.presentation.rmi.StubAdapter ;

import com.sun.corba.ee.spi.copyobject.CopierManager ;
import com.sun.corba.ee.spi.copyobject.CopyobjectDefaults ;

import com.sun.corba.ee.impl.orbutil.newtimer.generated.TimingPoints ;
import com.sun.corba.ee.spi.misc.ORBConstants ;

import corba.framework.InternalProcess ;
import org.glassfish.pfl.dynamic.copyobject.spi.ObjectCopierFactory;
import org.glassfish.pfl.tf.timer.spi.Statistics;
import org.glassfish.pfl.tf.timer.spi.StatsEventHandler;
import org.glassfish.pfl.tf.timer.spi.Timer;
import org.glassfish.pfl.tf.timer.spi.TimerEventController;
import org.glassfish.pfl.tf.timer.spi.TimerEventHandler;
import org.glassfish.pfl.tf.timer.spi.TimerFactory;
import org.glassfish.pfl.tf.timer.spi.TimerManager;

public class counterClient implements InternalProcess
{
    private static final boolean DEBUG = false ;

    private POA createPOA( ORB org, POA rootPOA ) throws AdapterAlreadyExists, InvalidPolicy,
        WrongPolicy, RemoteException
    {
        Policy[] tpolicy = new Policy[3];
        tpolicy[0] = rootPOA.create_lifespan_policy(LifespanPolicyValue.TRANSIENT);
        tpolicy[1] = rootPOA.create_request_processing_policy(
            RequestProcessingPolicyValue.USE_SERVANT_MANAGER);
        tpolicy[2] = rootPOA.create_servant_retention_policy(
            ServantRetentionPolicyValue.NON_RETAIN) ;

        POA tpoa = rootPOA.create_POA("POA", rootPOA.the_POAManager(), tpolicy);
        counterImpl impl = new counterImpl();
        Servant servant = (Servant)(javax.rmi.CORBA.Util.getTie( impl ) ) ;
        CounterServantLocator csl = new CounterServantLocator(servant);
        tpoa.set_servant_manager(csl);

        return tpoa ;
    }

    private POA createSCPOA( ORB org, POA rootPOA, int sctype ) throws AdapterAlreadyExists, InvalidPolicy,
        WrongPolicy, RemoteException
    {
        Policy[] tpolicy = new Policy[4];
        tpolicy[0] = rootPOA.create_lifespan_policy(LifespanPolicyValue.TRANSIENT);
        tpolicy[1] = rootPOA.create_request_processing_policy(
            RequestProcessingPolicyValue.USE_SERVANT_MANAGER);
        tpolicy[2] = rootPOA.create_servant_retention_policy(
            ServantRetentionPolicyValue.NON_RETAIN) ;

        switch (sctype) {
            case ServantCachingPolicy.FULL_SEMANTICS :
                tpolicy[3] = ServantCachingPolicy.getFullPolicy() ;
                break ;
            case ServantCachingPolicy.INFO_ONLY_SEMANTICS :
                tpolicy[3] = ServantCachingPolicy.getInfoOnlyPolicy() ;
                break ;
            case ServantCachingPolicy.MINIMAL_SEMANTICS :
                tpolicy[3] = ServantCachingPolicy.getMinimalPolicy() ;
                break ;
        }

        POA tpoa = rootPOA.create_POA( "SCPOA" + sctype, rootPOA.the_POAManager(), tpolicy);
        counterImpl impl = new counterImpl();
        Servant servant = (Servant)(javax.rmi.CORBA.Util.getTie( impl ) ) ;
        CounterServantLocator csl = new CounterServantLocator(servant);
        tpoa.set_servant_manager(csl);

        return tpoa ;
    }
        
    private counterIF createCounter(ORB orb, POA tpoa)
    {
        // create an objref using POA
        byte[] id = "abcdef".getBytes();
        String intf = "" ; // new _counterImpl_Tie()._all_interfaces(tpoa,id)[0];

        org.omg.CORBA.Object obj = tpoa.create_reference_with_id(id, intf);

        counterIF counterRef 
            = (counterIF)PortableRemoteObject.narrow(obj, counterIF.class );

        return counterRef ; 
    }

    private counterIF lookupDifferentORBCounter( ORB orb, ORB orb2, POA tpoa ) 
    {
        counterIF result = createCounter( orb, tpoa ) ;

        String str = orb.object_to_string( (org.omg.CORBA.Object)result ) ;
        org.omg.CORBA.Object obj = orb2.string_to_object( str ) ;       

        return (counterIF)PortableRemoteObject.narrow( obj, counterIF.class ) ;
    }

    private counterIF lookupCounter( ORB orb, POA tpoa ) 
    {
        counterIF result = createCounter( orb, tpoa ) ;

        org.omg.CORBA.Object obj ;

        /*
        try {
            NamingContextExt nc = NamingContextExtHelper.narrow( 
                orb.resolve_initial_references( "NameService" ) ) ;

            NameComponent[] name = nc.to_name( "FooObject" ) ;
            nc.rebind( name, (org.omg.CORBA.Object)result ) ;
            obj = nc.resolve( name ) ;
        } catch (Exception exc) {
            System.out.println( exc ) ;
            exc.printStackTrace() ;
            return null ;
        } 
        */

        String str = orb.object_to_string( (org.omg.CORBA.Object)result ) ;
        obj = orb.string_to_object( str ) ;     

        return (counterIF)PortableRemoteObject.narrow( obj, counterIF.class ) ;
    }

    private static final int WARMUP = 10000 ;
    private static final int COUNT =  2000 ;

    private void warmup( counterIF counterRef ) 
        throws RemoteException {

        long value = 0 ;

        for (int i = 0; i < WARMUP; i++) {
            value += counterRef.increment(1);
        }
    }

    private void performTest(PrintStream out, counterIF counterRef, 
        String testType ) throws RemoteException
    {
        long value = 0 ;
        long time = System.nanoTime() ;

        for (int i = 0; i < COUNT; i++) {
            value += counterRef.increment(1);
        }
        
        double elapsed = System.nanoTime() - time ;

        out.println( "Test " + testType + " : " + (elapsed/COUNT)/1000 ) ;
    }

    private ORB initORB( String id, String[] args, Properties environment )
    {
        // create and initialize the ORB
        environment.setProperty( ORBConstants.TIMING_POINTS_ENABLED,
            "true" ) ;
        environment.setProperty( ORBConstants.ALLOW_LOCAL_OPTIMIZATION, 
            "true" ) ;
        if (DEBUG) {
            environment.setProperty( "com.sun.corba.ee.ORBDebug", 
                "subcontract" ) ;
        }
        environment.setProperty( ORBConstants.ORB_ID_PROPERTY, id ) ; 
        ORB orb = (ORB)org.omg.CORBA.ORB.init(args, environment);

        // Use the optimized reflective object copier for this test
        ObjectCopierFactory ocf = CopyobjectDefaults.makeReflectObjectCopierFactory( orb ) ;
        CopierManager cm = orb.getCopierManager() ;
        int defaultId = cm.getDefaultId() ;
        cm.registerObjectCopierFactory( ocf, defaultId ) ;
        
        System.out.println( "Using optimized reflective copier" ) ;

        return orb ;
    }

    private class DataBlock {
        TimerEventController controller ;
        Timer top ;
        TimingPoints tp ;
        StatsEventHandler seh ;
    }

    private DataBlock startTiming( ORB orb ) {
        DataBlock db = new DataBlock() ;
        TimerManager<TimingPoints> tm = orb.makeTimerManager(
            TimingPoints.class ) ;
        db.tp = tm.points() ;
        TimerFactory tf = tm.factory() ;
        db.controller = tm.controller() ;
        db.top = tf.makeTimer( "top", "Total time spent for making "
            + COUNT + " non-colocated invocations in the same VM" ) ;
        db.seh = tf.makeMultiThreadedStatsEventHandler( orb.getORBData().getORBId() ) ;
        db.controller.register( db.seh ) ;

        if (DEBUG) {
            TimerEventHandler tracingHandler = 
                tf.makeTracingEventHandler( "DEBUG" ) ;
            db.controller.register( tracingHandler ) ;
        }

        db.top.enable() ;

        // Enabled everything in the invocation path.
        db.tp.DynamicType().enable() ;
        db.tp.Transport().enable() ;
        db.tp.Giop().enable() ;
        db.tp.TraceServiceContext().enable() ;
        db.tp.Cdr().enable() ;

        db.seh.clear() ;

        db.controller.enter( db.top ) ;
        return db ;
    }

    private void stopTiming( DataBlock db ) {
        db.controller.exit( db.top ) ;

        db.top.disable() ;
        db.tp.Cdr().disable() ;
        db.tp.DynamicType().disable() ;
        
        // Dump out timing results.
        Map<Timer,Statistics> result = db.seh.stats() ; 
        // TimerUtils.writeHtmlTable( result, db.seh.name() + "TimingData.html",
            // "Timing Data for making " + COUNT
            // + " non-colocated calls in the same VM (" 
            // + db.seh.name() + ")" ) ;
    }

    public void run(Properties environment,
                    String args[],
                    PrintStream out,
                    PrintStream err,
                    Hashtable extra) throws Exception
    {
        environment.list(out);

        try {
            ORB orb = initORB( "MainORB", args, environment ) ;
            POA rootPOA = (POA)orb.resolve_initial_references("RootPOA");
            rootPOA.the_POAManager().activate();

            POA poa = createPOA(orb, rootPOA);
            POA scpoa = createSCPOA(orb, rootPOA, ServantCachingPolicy.FULL_SEMANTICS );
            POA iiscpoa = createSCPOA(orb, rootPOA, ServantCachingPolicy.INFO_ONLY_SEMANTICS );
            POA minscpoa = createSCPOA(orb, rootPOA, ServantCachingPolicy.MINIMAL_SEMANTICS );

            out.println( "Times per invocation in microseconds:" ) ;
            out.println( 
                "-------------------------------------------------------------" ) ;
            counterIF counterRef1 = createCounter( orb, poa ) ;
            warmup( counterRef1 ) ;
            performTest(out, counterRef1, "local POA" );
            testIsLocal( counterRef1, true ) ;

            counterIF counterRef2 = createCounter( orb, scpoa ) ;

            warmup( counterRef2 ) ;
            performTest(out, counterRef2, "local POA (full servant caching)" );
            testIsLocal( counterRef2, true ) ;

            counterIF counterRef3 = lookupCounter( orb, scpoa ) ;
            warmup( counterRef3 ) ;
            performTest(out, counterRef3, "local POA (full servant caching) after resolve" ) ;
            testIsLocal( counterRef3, true ) ;

            counterIF counterRef4 = createCounter( orb, iiscpoa ) ;
            warmup( counterRef4 ) ;
            performTest(out, counterRef4, "local POA (info only servant caching)" );
            testIsLocal( counterRef4, true ) ;

            counterIF counterRef5 = lookupCounter( orb, iiscpoa ) ;
            warmup( counterRef5 ) ;
            performTest(out, counterRef5, "local POA (info only servant caching) after resolve" );
            testIsLocal( counterRef5, true ) ;

            counterIF counterRef6 = createCounter( orb, minscpoa ) ;
            warmup( counterRef6 ) ;
            performTest(out, counterRef6, "local POA (minimal servant caching)" );
            testIsLocal( counterRef6, true ) ;

            counterIF counterRef7 = lookupCounter( orb, minscpoa ) ;
            warmup( counterRef7 ) ;
            performTest(out, counterRef7, "local POA (minimal servant caching) after resolve" );
            testIsLocal( counterRef7, true ) ;

            // Note that orb2 is the client ORB
            ORB orb2 = initORB( "SecondORB", args, environment);
            counterIF counterRef8 = lookupDifferentORBCounter( orb, orb2, scpoa ) ;

            warmup( counterRef8 ) ;
            DataBlock db2 = startTiming( orb2 ) ;
            try {
                performTest(out, counterRef8, 
                    "local POA (full servant caching) after resolve in different ORB" ) ;
            } finally {
                // stopTiming( db ) ;
                stopTiming( db2 ) ;
            }

            testIsLocal( counterRef8, false ) ;
        } catch (Exception e) {
            e.printStackTrace(err);
            throw e;
        }
    }

    private void testIsLocal( counterIF counter, boolean expectedResult ) throws Exception
    {
        if (StubAdapter.isLocal( counter ) != expectedResult) {
            String msg ;
            if (expectedResult) {
                msg = "Error: expected local object, but is_local returned false";
            } else {
                msg = "Error: expected remote object, but is_local returned true";
            }
            throw new Exception( msg ) ;
        }
    }

    public static void main(String args[])
    {
        try {
            (new counterClient()).run(System.getProperties(),
                                      args,
                                      System.out,
                                      System.err,
                                      null);

        } catch (Exception e) {
            System.err.println("ERROR : " + e) ;
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }
}

class CounterServantLocator extends org.omg.CORBA.LocalObject implements ServantLocator
{
    Servant servant;

    CounterServantLocator(Servant servant)
    {
        this.servant = servant;
    }

    public Servant preinvoke(byte[] oid, POA adapter, String operation, 
                             CookieHolder the_cookie)
        throws org.omg.PortableServer.ForwardRequest
    {
        return servant ;
    }

    public void postinvoke(byte[] oid, POA adapter, String operation, 
                           java.lang.Object cookie, Servant servant)
    {
        return;
    }
}
