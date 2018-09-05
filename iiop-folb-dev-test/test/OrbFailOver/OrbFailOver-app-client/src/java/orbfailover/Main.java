/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package orbfailover;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import argparser.DefaultValue;
import argparser.Help;
import argparser.Pair;

import glassfish.AdminCommand;
import glassfish.GlassFishCluster;
import glassfish.GlassFishInstallation;
import glassfish.StandardPorts;
import java.util.HashSet;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;

import orb.folb.LocationBeanRemote;

import testtools.Test ;
import testtools.Base ;
import testtools.Post;
import testtools.Pre;

import com.sun.corba.ee.spi.orb.ORB ;
import com.sun.corba.ee.spi.protocol.ClientDelegate;
import java.lang.reflect.Field;
import javax.rmi.CORBA.Stub;
import orb.folb.Location;



/**
 * @author hv51393
 * @author kcavanaugh
 */
public class Main extends Base {
    // @EJB
    // private static LocationBeanRemote locationBean;
    private static final String CLUSTER_NAME = "c1" ;
    private static final String EJB_NAME = "TestEJB" ;
    private static final String INSTANCE_BASE_NAME = "in" ;

    private ORB orb = null ;
    private String[] orbDebugFlags = null ;

    private GlassFishInstallation  gfInstallation ;
    private AdminCommand ac ;
    private GlassFishCluster gfCluster ;
    private Map<String,GlassFishCluster.InstanceInfo> clusterInfo ;

    private static final String beanJndiName =
        "orb.folb.LocationBeanRemote";
    private static final String statefullBeanJndiName =
        "orb.folb.StatefullLocationBeanRemote";

    private static Pair<String,Integer> split(String str) {
        int index = str.indexOf( ':' ) ;
        final String first = str.substring( 0, index ) ;
        final String second = str.substring( index + 1 ) ;
        final int value = Integer.valueOf( second ) ;
        return new Pair<String,Integer>( first, value ) ;
    }

    private static List<Pair<String,Integer>> parseStringIntPair(
        List<String> args ) {

        List<Pair<String,Integer>> result =
            new ArrayList<Pair<String, Integer>>() ;
        for (String str : args) {
            result.add( split( str ) ) ;
        }
        return result ;
    }

    // Get an IIOP endpoint list from a set of instance names.
    private String getIIOPEndpointList( Set<String> instances ) {
        final StringBuilder sb = new StringBuilder() ;
        boolean first = true ;
        for (String str : instances) {
            if (first) {
                first = false ;
            } else {
                sb.append( ',' ) ;
            }

            final GlassFishCluster.InstanceInfo info =
                gfCluster.instanceInfo().get( str ) ;
            final String host = info.node() ;
            final int port = info.ports().get( StandardPorts.IIOP_LISTENER_PORT ) ;
            sb.append( host ).append( ':' ).append( port ) ;
        }

        return sb.toString() ;
    }

    // Returns string of <host>:<clear text port> separated by commas
    // for all running instances.
    private String getIIOPEndpointList() {
        return getIIOPEndpointList( gfCluster.runningInstances()) ;
    }

    private InitialContext makeIC() throws NamingException {
        return makeIC( getIIOPEndpointList() ) ;
    }

    private InitialContext makeIC( String eplist ) throws NamingException {
        InitialContext result ;
        if (eplist != null && !inst.useExternalEndpoints()) {
            final Hashtable table = new Hashtable() ;
            table.put( "com.sun.appserv.iiop.endpoints", eplist ) ;
            result = new InitialContext( table ) ;
        } else {
            result = new InitialContext() ;
        }

        note( "Created new InitialContext with endpoints " + eplist ) ;

        return result ;
    }

    private String invokeMethod( Location locBean ) {
        String result = locBean.getLocation() ;
        note( "Invocation returned " + result ) ;
        return result ;
    }

    private enum BeanType { SFSB, SLSB }

    private Location lookup( InitialContext ic )
        throws NamingException {

        return lookup( ic, BeanType.SLSB ) ;
    }

    private Location lookup( InitialContext ic, BeanType bt )
        throws NamingException {

        Location lb = (Location)ic.lookup(
            (bt == BeanType.SLSB) ? beanJndiName : statefullBeanJndiName ) ;

        getORB( lb ) ;
        note( "Looked up bean in context") ;
        return lb ;
    }

    private Location lookupUsingJavaCmp( InitialContext ic, BeanType bt )
        throws NamingException {

        Context ctx = (Context)ic.lookup( "java:comp/env/ejb" ) ;
        Location lb = (Location)ctx.lookup(
            (bt == BeanType.SLSB) ? beanJndiName : statefullBeanJndiName ) ;

        getORB( lb ) ;
        note( "Looked up bean in context") ;
        return lb ;
    }

    private Field getField( Class<?> cls, String name ) {
        Field result = null ;
        try {
            result = cls.getDeclaredField(name);
            result.setAccessible(true);
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;
    }

    private void getORB(Location lb) {
        if (orb == null) {
            try {
                final Class<?> lbClass = lb.getClass() ;
                final Field delegateField = getField( lbClass, "delegate_") ;
                final Stub stub = (Stub)delegateField.get( lb ) ;

                // final Class<?> delegateClass = delegate.getClass() ;
                // delegateSuperClass should be a CodegenStubBase
                // final Class<?> delegateSuperClass = delegateClass.getSuperclass() ;
                // final Field fld = getField( delegateSuperClass, "__delegate");
                final ClientDelegate cdel =
                    (ClientDelegate)stub._get_delegate() ;

                orb = cdel.getBroker();
                if (orbDebugFlags != null)  {
                    setORBDebug( orbDebugFlags ) ;
                }
            } catch (Exception ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void setORBDebug( String... args ) {
        if (orb != null) {
            if (args != null) {
                orb.setDebugFlags( args ) ;
            }
        } else {
            orbDebugFlags = args ;
        }
    }

    private void clearORBDebug( String... args ) {
        if (orb != null) {
            if (args != null) {
                orb.clearDebugFlags( args ) ;
            }
        } else {
            orbDebugFlags = null ;
        }
    }

    public interface Installation {
        @DefaultValue("")
        @Help("Name of directory where GlassFish is installed")
        String installDir() ;

        @DefaultValue("")
        @Help("Name of node for GlassFish DAS")
        String dasNode() ;

        @DefaultValue("")
        @Help("A list of available nodes for cluster setup: comma separed list of name:number")
        List<String> availableNodes() ;

        @DefaultValue("")
        @Help("The test EJB needed for this test")
        String testEjb() ;

        @DefaultValue( "false" )
        @Help("Set if we want to shutdown and destroy the cluster after the test")
        boolean doCleanup() ;

        @DefaultValue( "false" )
        @Help( "Set if the cluster is already setup and running in order to avoid"
        + "extra processing")
        boolean skipSetup() ;

        @DefaultValue( "5" )
        @Help( "Set the number of instances to create in the cluster.  "
            + "There must be sufficient availability declared in availableNodes "
            + "for this many instances")
        int numInstances() ;

        @DefaultValue( "false" )
        @Help( "Set this to true if you only want to set up a cluster, and do NOT"
            + "want to run any tests")
        boolean skipTests() ;

        @DefaultValue( "" )
        @Help( "Set this to the comma separated list of ORB debug flags to use"
            + " on the app server instances")
        String serverORBDebug() ;

        @DefaultValue( "false" )
        @Help( "Set this to true if the test uses only externally supplied IIOP"
            + "endpoints")
        boolean useExternalEndpoints() ;
    }

    private final Installation inst ;

    public Main( String[] args ) {
        super( args, Installation.class ) ;
        inst = getArguments( Installation.class ) ;
        try {
            List<Pair<String,Integer>> avnodes = parseStringIntPair(
                inst.availableNodes() ) ;
            gfInstallation = new GlassFishInstallation( this,
                inst.installDir(), inst.dasNode(), avnodes, false,
                inst.skipSetup() ) ;
            ac = gfInstallation.ac() ;

            gfCluster = new GlassFishCluster( gfInstallation, CLUSTER_NAME,
                inst.skipSetup()) ;
            if (!inst.skipSetup()) {
                clusterInfo = gfCluster.createInstances( INSTANCE_BASE_NAME,
                    inst.numInstances(), inst.serverORBDebug() ) ;
                gfCluster.startCluster() ;
                if (!inst.testEjb().isEmpty()) {
                    boolean availabilityEnabled = true ;
                    ac.deploy( CLUSTER_NAME, EJB_NAME, inst.testEjb(),
                               availabilityEnabled  ) ;
                }
            } else {
                gfCluster.startCluster() ;
                clusterInfo = gfCluster.instanceInfo() ;
            }

        } catch (Exception exc) {
            System.out.println( "Exception in constructor: " + exc ) ;
            exc.printStackTrace() ;
            cleanup() ;
        }
    }

    private void cleanup() {
        if (inst.doCleanup()) {
            if (ac != null) {
                ac.undeploy( CLUSTER_NAME, EJB_NAME ) ;
                ac = null ;
            }

            if (gfCluster != null) {
                gfCluster.destroyCluster() ;
                gfCluster = null ;
            }

            if (gfInstallation != null) {
                gfInstallation.destroy() ;
                gfInstallation = null ;
            }
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Main m = new Main( args );
        int result = 0 ;
        if (!m.inst.skipTests()) {
            result = 1 ;
            if (m.gfCluster != null) {
                try {
                    result = m.run() ;
                } finally {
                    m.cleanup() ;
                }
            }
        }
        System.exit( result ) ;
    }

    // Not a useful test: listing of context is broken in GF 3.1.
    // @Test( "listContextTest" )
    public void listContextTest() throws NamingException {
        InitialContext ic = makeIC() ;
        listContext( ic, 0 ) ;
    }

    private String indent( String str, int size ) {
        String fmt = String.format( "%%%d%%s", size ) ;
        return String.format( fmt, str ) ;
    }

    private void listContext( Context ctx, int level ) throws NamingException {
        final NamingEnumeration<NameClassPair> ne = ctx.list( "" ) ;
        final int size = 4*level ;
        while (ne.hasMore()) {
             final NameClassPair pair = ne.next() ;
             final String name = pair.getName() ;

             Object val = null ;
             try {
                val = ctx.lookup( name ) ;
             } catch (Exception exc) {
                val = "*UNAVAILABLE* (" + pair.getClassName() + ")" ;
             }

             final StringBuilder sb = new StringBuilder() ;
             if (val instanceof Context) {
                 final Context ctx2 = (Context)val ;
                 sb.append( name ).append( ':' ) ;
                 note( indent( sb.toString(), size ) ) ;
                 listContext( ctx2, level+1 ) ;
             } else {
                 sb.append( name ).append( "=>" ).append( val.toString() ) ;
                 note( indent( sb.toString(), size ) ) ;
             }
        }
    }

    @Test( "failover" )
    public void failOverTest() {
        final int numCalls = 150 ;
        final int stopAt = numCalls/3 ;
        final int startAt = 2*numCalls/3 ;
        String stoppedInstance = null ;

        boolean failOverDetected = false;
        try {
            InitialContext ctx = makeIC();
            Location locBean = lookup( ctx ) ;
            String origLocation = invokeMethod( locBean );
            if (origLocation == null) {
                fail( "couldn't get the instance name!");
            }
            for (int i = 1; i <= numCalls; i++) {
                String newLocation = null ;
                try {
                    newLocation = invokeMethod( locBean );
                } catch (Exception e) {
                    fail( "caught invocation exception " + e );
                }

                note( "result[" + i + "]= " + newLocation);
                if (!origLocation.equals(newLocation)) {
                    failOverDetected = true;
                }

                if (i == stopAt) {
                    stoppedInstance = newLocation ;
                    ac.stopInstance( stoppedInstance );
                }

                if (i == startAt) {
                    ac.startInstance( stoppedInstance ) ;
                    stoppedInstance = null ;
                }
            } 
        } catch (Exception exc) {
            fail( "caught naming exception " + exc ) ;
        } finally {
            if (stoppedInstance != null) {
                ac.startInstance( stoppedInstance ) ;
            }
        }
    }

    private void increment( Map<String,Integer> map, String name ) {
        Integer val = map.get( name ) ;
        if (val == null) {
            val = Integer.valueOf(1) ;
        } else {
            val = Integer.valueOf( val.intValue() + 1 ) ;
        }

        map.put( name, val ) ;
    }

    @Test( "loadbalance" )
    public void testLoadBalance( ) throws NamingException {
        doLoadBalance( gfCluster.runningInstances(), 1000 )  ;
    }

    @Test( "15768" )
    public void test15768( ) throws NamingException {
        doLoadBalanceUsingJavaCmp( gfCluster.runningInstances(), 100,
            getIIOPEndpointList() )  ;
    }

    private <T> T pick( Set<T> set ) {
        return pick( set, false ) ;
    }

    private <T> T pick( Set<T> set, boolean remove ) {
        T result = null ;
        for (T elem : set) {
            result = elem ;
            break ;
        }

        if (remove) {
            set.remove( result ) ;
        }

        return result ;
    }

    // Test scenario for issue 14762:
    // 1. Deploy the app
    // 2. Bring down the cluster
    // 3. Start one instance (firstinstance)
    // 4. Create 10 new IC, and do ejb lookups (appclient is run with
    //    endpoints having all instances host:port info)
    // 5. Bring up all remaining instances
    // 6. Kill Instance where we created ic and did lookups
    // 7. Access business methods for the ejb's (created in step 4)
    @Test( "14762" )
    public void test14762() throws NamingException {
        gfCluster.stopCluster();

        Set<String> strs = new HashSet<String>( clusterInfo.keySet() ) ;
        final String first = pick( strs, true ) ;
        final String second = pick( strs, true ) ;
        ac.startInstance(first) ;

        Set<String> initialInstances = new HashSet<String>() ;
        initialInstances.add( first ) ;
        initialInstances.add( second ) ;
        String initial = getIIOPEndpointList( initialInstances ) ;

        final List<InitialContext> ics = new ArrayList<InitialContext>() ;
        final List<Location> lbs = new ArrayList<Location>() ;
        for (int ctr=0; ctr<10; ctr++) {
            final String str = (ctr==0) ? initial : null ;
            final InitialContext ic = makeIC(str) ;
            ics.add( ic ) ;
            final Location lb = lookup( ic ) ;
            lbs.add( lb ) ;
        }
        ensure() ;
        ac.stopInstance(first);
        for (Location lb : lbs ) {
            String loc = invokeMethod( lb ) ;
            check( !loc.equals( first),
                "Location returned was stopped instance " + first) ;
        }
    }

    // Test scenario for issue 14755:
    // 1. New Initial Context
    // 2. Lookup EJB
    // 3. Call a Business method
    // 4. Find the instance that served the business method - firstinstance
    // 5. Kill that instance
    // 6. Call business method
    // 7. Now the request will be served by another instance - secondinstance
    // 8. Bring back the first instance (which we killed before)
    //    Call business method
    // 9. The request goes to firstinstance (it should go to secondinstance - since it
    //    should be sticky)
    @Test( "14755" )
    public void test14755() throws NamingException {
        InitialContext ic = makeIC() ;
        Location lb = lookup( ic ) ;
        String first = invokeMethod( lb ) ;
        ac.stopInstance(first);
        String second = invokeMethod( lb ) ;
        check( !first.equals( second ),
            "Method executed on instance that was supposed to be down " + second ) ;
        ac.startInstance( first ) ;
        String result = invokeMethod( lb ) ;
        check( result.equals( second ),
            "Request did not stick to instance " + second +
            " after original instance " + first + " restarted" ) ;
    }

    // Test scenario for issue 14766:
    // 1. start up all instances
    // 2. start up client with target-server+ A:B:C
    // 3. do new IntialContext/lookup
    // 4. do one request - ensure it works
    // 5. shutdown A
    // 6. do one request - ensure it works
    // 7. shutdown B
    // 8. do one request - ensure it works
    // 9. restart A
    // 10 do one request - ensure it works
    // 11 shutdown C
    // 12 do one request - ensure it works
    @Test( "14766")
    public void test14766() throws NamingException {
        InitialContext ctx = makeIC() ;
        Location locBean = lookup( ctx ) ;

        String loc1 = invokeMethod( locBean );
        ac.stopInstance(loc1);

        String loc2 = invokeMethod( locBean ) ;
        check( !loc1.equals(loc2), "Failover did not happen") ;
        ac.stopInstance(loc2);

        String loc3 = invokeMethod( locBean ) ;
        check( !loc3.equals(loc2), "Failover did not happen") ;
        ac.startInstance(loc1);

        String loc4 = invokeMethod( locBean ) ;
        check( loc4.equals(loc3), "No failover expected" ) ;
        ac.stopInstance(loc3);

        String loc5 = invokeMethod( locBean ) ;
        check( !loc5.equals(loc4), "Failover did not happen") ;
    }

    @Test( "lbfail" )
    public void testLBFail() throws NamingException {
        final int numCalls = 200 ;
        doLoadBalance( gfCluster.runningInstances(), numCalls )  ;

        final String inst1 = pick( gfCluster.runningInstances() ) ;
        gfCluster.stopInstance(inst1) ;
        doLoadBalance( gfCluster.runningInstances(), numCalls )  ;

        final String inst2 = pick( gfCluster.runningInstances() ) ;
        gfCluster.stopInstance(inst2) ;

        final String inst3 = pick( gfCluster.runningInstances() ) ;

        gfCluster.startInstance(inst1);
        gfCluster.sleep( 5 ) ;  // Seems to take a while to wake up?
        doLoadBalance( gfCluster.runningInstances(), numCalls )  ;

        gfCluster.stopInstance(inst3);
        doLoadBalance( gfCluster.runningInstances(), numCalls )  ;
    }

    public void doLoadBalance( Set<String> expected,
        int numCalls ) throws NamingException  {
        doLoadBalance( expected, numCalls, getIIOPEndpointList() ) ;
    }

    public Location doLoadBalance( Set<String> expected, int numCalls,
        String endpoints ) throws NamingException {
        Location current = null ;
        // XXX add checking for approximate distribution
        // according to weights
        Map<String,Integer> counts =
            new HashMap<String,Integer>() ;

        String newLocation = "" ;
        for (int i = 1; i <= numCalls ; i++) {
            InitialContext ctx = makeIC( endpoints ) ;
            current = lookup( ctx ) ;
            newLocation = invokeMethod( current );
            note( "result[" + i + "]= " + newLocation);
            increment( counts, newLocation ) ;
        }

        note( "Call distribution: (expected on instances " + expected + ")" ) ;
        for (Map.Entry<String,Integer> entry : counts.entrySet()) {
            int count = entry.getValue().intValue() ;
            note( String.format( "\tName = %20s Count = %10d",
                entry.getKey(), count ) ) ;
        }

        check( expected.equals(counts.keySet()),
            "Requests not loadbalanced across expected instances: "
            + " expected " + expected + ", actual " + counts.keySet() ) ;

        return current ;
    }

    public Location doLoadBalanceUsingJavaCmp( Set<String> expected,
        int numCalls, String endpoints ) throws NamingException {
        Location current = null ;
        // XXX add checking for approximate distribution
        // according to weights
        Map<String,Integer> counts =
            new HashMap<String,Integer>() ;

        String newLocation = "" ;
        for (int i = 1; i <= numCalls ; i++) {
            InitialContext ctx = makeIC( endpoints ) ;
            current = lookupUsingJavaCmp( ctx, BeanType.SLSB ) ;
            newLocation = invokeMethod( current );
            note( "result[" + i + "]= " + newLocation);
            increment( counts, newLocation ) ;
        }

        note( "Call distribution: (expected on instances " + expected + ")" ) ;
        for (Map.Entry<String,Integer> entry : counts.entrySet()) {
            int count = entry.getValue().intValue() ;
            note( String.format( "\tName = %20s Count = %10d",
                entry.getKey(), count ) ) ;
        }

        check( expected.equals(counts.keySet()),
            "Requests not loadbalanced across expected instances: "
            + " expected " + expected + ", actual " + counts.keySet() ) ;

        return current ;
    }

    public void doLoadBalance( Set<String> expected, Set<InitialContext> ics
        ) throws NamingException {
        // XXX add checking for approximate distribution
        // according to weights
        Map<String,Integer> counts =
            new HashMap<String,Integer>() ;

        String newLocation = "" ;
        int i = 0 ;
        for (InitialContext ctx : ics ) {
            Location locBean = lookup( ctx ) ;
            newLocation = invokeMethod( locBean );
            note( "result[" + (i++) + "]= " + newLocation);
            increment( counts, newLocation ) ;
        }

        note( "Call distribution:" ) ;
        for (Map.Entry<String,Integer> entry : counts.entrySet()) {
            int count = entry.getValue().intValue() ;
            note( String.format( "\tName = %20s Count = %10d",
                entry.getKey(), count ) ) ;
        }

        check( (counts.keySet().size() == 1)
            && expected.containsAll( counts.keySet() ),
            "Request did not failover to a single instance in expected instance "
            + expected ) ;
    }

    // Test scenario for issue 14732:
    // 1. One instance of cluster is running (all remaining are stopped)
    // 2. Create Initial Context (with endpoints having all instance host and ports)
    // 3. Do EJB Lookup
    // 4. Call Business method
    // 5. Create InitialContext
    // 6. Stop Instance (the only instance)
    // 7. Start remaining instances
    // 8. Do lookup (the expectation is it will failover to other instance)
    // 9. Call Business Method
    @Test( "14732" )
    public void test14732() throws NamingException {
        // Capture full endpoint list for later use in makeIC.
        String endpoints = getIIOPEndpointList() ;

        // Make sure only inst is running
        boolean first = true ;
        String running = "" ;
        for (String inst : gfCluster.runningInstances()) {
            if (first) {
                running = inst ;
                note( "Running instance is " + inst ) ;
                first = false ;
            } else {
                ac.stopInstance(inst);
            }
        }

        InitialContext ctx = makeIC( endpoints ) ;
        note( "got new initial context") ;

        Location locBean = lookup( ctx ) ;
        note( "located EJB" ) ;

        String current = invokeMethod( locBean );
        note( "EJB invocation returned " + current ) ;
        check( current.equals( running ),
            "Current location " + current + " is not the same as the"
                + " running location " + running ) ;

        ac.stopInstance( running ) ;
        for (String inst : clusterInfo.keySet()) {
            if (!inst.equals(running)) {
                ac.startInstance( inst ) ;
            }
        }

        locBean = lookup( ctx ) ;
        note( "Locating EJB second time") ;

        current = invokeMethod( locBean );
        note( "EJB invocation returned " + current ) ;
        check( !current.equals( running ),
            "Apparent location " + current
            + " is the same as a stoppped instance " + running ) ;
}

    // Test scenario for issue 14867:
    // 1. Deploy an application to a cluster with three instances
    // 2. Start up one client with target-server+ specifying the three instances.
    // 3. In a loop (100 iterations):
    //    1. Create a new InitialContext
    //    2. Do a lookup
    //    3. Remember which instance the lookup went to
    // 4. Create and start two new instances
    // 5. Then do 100 new IntialContext/lookup.
    // 6. Destroy new instances.
    // New instances should process some of new requests
    @Test( "14867c" )
    public void test14867createInstances() throws NamingException {
        Set<String> instances = gfCluster.runningInstances() ;
        note( "Running instances = " + instances ) ;
        check( instances.size() >= 3, "Cluster must contain at least 5 instances") ;

        final String gfInstance1 = pick( instances, true ) ;
        final String gfInstance2 = pick( instances, true ) ;
        final String gfInstance3 = pick( instances, true ) ;
        gfCluster.stopCluster() ;

        gfCluster.startInstance( gfInstance1 );
        gfCluster.startInstance( gfInstance2 );
        gfCluster.startInstance( gfInstance3 );
        final String endpoints = getIIOPEndpointList() ;

        final int COUNT = 100 ;
        instances = gfCluster.runningInstances() ;
        note( "Running instances = " + instances ) ;
        final String[] flags = { /* "subcontract", "transport", "folb" */ } ;
        Location last = null ;
        try {
            setORBDebug( flags ) ;
            /* last = */ doLoadBalance( instances, COUNT, endpoints ) ;
        } finally {
            clearORBDebug( flags ) ;
        }

        final String testBase = "testInstance" ;
        final int portBase = 20000 ;
        gfCluster.createInstances( testBase, 2, "", portBase ) ;
        gfCluster.startInstance( testBase + "0" );
        if (last != null) {
            invokeMethod(last) ;
        }
        gfCluster.startInstance( testBase + "1" );
        if (last != null) {
            invokeMethod(last) ;
        }

        gfCluster.sleep(10);

        try {
            setORBDebug( flags ) ;
            instances = gfCluster.runningInstances() ;
            note( "Running instances = " + instances ) ;
            doLoadBalance( instances, 10*COUNT, endpoints ) ;
        } finally {
            clearORBDebug( flags ) ;
            gfCluster.stopInstance( testBase + "0" ) ;
            gfCluster.destroyInstance( testBase + "0" ) ;
            gfCluster.stopInstance( testBase + "1" ) ;
            gfCluster.destroyInstance( testBase + "1" ) ;
        }
    }

    // Test scenario for issue 14867:
    // 1. Make sure cluster has at least 5 instances.  Stop 2 instances.
    // 2. Deploy an application to a cluster with three instances
    // 3. Start up one client with target-server+ specifying the three instances.
    // 4. In a loop (100 iterations):
    //    1. Create a new InitialContext
    //    2. Do a lookup
    //    3. Remember which instance the lookup went to
    // 5. Start two more instances
    // 6. Then do 100 new IntialContext/lookup.
    // New instances should process some of new requests
    @Test( "14867" )
    public void test14867() throws NamingException {
        Set<String> instances = gfCluster.runningInstances() ;
        note( "Running instances = " + instances ) ;
        check( instances.size() >= 5, "Cluster must contain at least 5 instances") ;

        final String gfInstance1 = pick( instances, true ) ;
        final String gfInstance2 = pick( instances, true ) ;
        final String gfInstance3 = pick( instances, true ) ;
        final String gfInstance4 = pick( instances, true ) ;
        final String gfInstance5 = pick( instances, true ) ;
        gfCluster.stopCluster() ;

        gfCluster.startInstance( gfInstance1 );
        gfCluster.startInstance( gfInstance2 );
        gfCluster.startInstance( gfInstance3 );
        final String endpoints = getIIOPEndpointList() ;

        final int COUNT = 1000 ;
        instances = gfCluster.runningInstances() ;
        note( "Running instances = " + instances ) ;
        final String[] flags = { /* "subcontract", "transport", "folb" */ } ;
        Location last = null ;
        try {
            setORBDebug( flags ) ;
            /* last = */ doLoadBalance( instances, COUNT, endpoints ) ;
        } finally {
            clearORBDebug( flags ) ;
        }

        gfCluster.startInstance( gfInstance4 );
        if (last != null) {
            invokeMethod(last) ;
        }
        gfCluster.startInstance( gfInstance5 );
        if (last != null) {
            invokeMethod(last) ;
        }

        gfCluster.sleep(10);

        try {
            setORBDebug( flags ) ;
            instances = gfCluster.runningInstances() ;
            note( "Running instances = " + instances ) ;
            doLoadBalance( instances, COUNT, endpoints ) ;
        } finally {
            clearORBDebug( flags ) ;
        }
    }

    // Test scenario:
    // 1. App is deployed on a 3 instance cluster
    // 2. loop for 100 times:
    // - ic = new InitialContext
    // - ejb = ic.lookup
    // - loc = ejb.getLocation
    // - if first time, kill instance loc
    // 3. see that LB happens on remaining cluster
    @Test( "lbstopinstance" )
    public void testLBStopInstance() throws Exception {
        final int CLUSTER_SIZE = 3 ;
        final int NUM_IC = 100 ;
        Set<String> clusterInstances = new HashSet<String>() ;
        Set<String> instances = gfCluster.runningInstances() ;
        for (int ctr=0; ctr<CLUSTER_SIZE; ctr++) {
            String inst = pick(instances,true ) ;
            if (inst != null) {
                clusterInstances.add( inst ) ;
            }
        }
        check( clusterInstances.size() == 3, "Not enough instances to run test" ) ;

        gfCluster.stopCluster() ;
        for (String inst : clusterInstances) {
            gfCluster.startInstance(inst);
        }

        InitialContext ic = makeIC()  ;
        Location locBean = lookup(ic) ;
        String inst = invokeMethod( locBean ) ;
        gfCluster.stopInstance( inst ) ;
        instances.remove( inst ) ;

        doLoadBalance( gfCluster.runningInstances(), NUM_IC-1, getIIOPEndpointList() ) ;
    }

    // Test scenario for issue 15637 (Gopal's email 1/27/11 4:46 PM)
    // 1. Stop the cluster.
    // 2. Start 1 instance (inst) in the cluster.
    // 3. LB to one instance
    // 4. Start cluster
    // 5. Kill inst
    // 6. LB test
    @Test( "15637" )
    public void test15637() throws NamingException {
        final Set<String> instances = gfCluster.runningInstances() ;
        final String selected = pick( instances, true ) ;
        final String second = pick( instances, true ) ;
        final Set<String> initialInstances = new HashSet<String>() ;
        initialInstances.add( selected ) ;
        initialInstances.add( second ) ;
        final String initalEndpoints = getIIOPEndpointList(initialInstances) ;
        gfCluster.stopCluster();
        gfCluster.startInstance(selected) ;
        doLoadBalance(gfCluster.runningInstances(), 10, initalEndpoints );
        gfCluster.startCluster();
        gfCluster.stopInstance(selected);
        doLoadBalance(gfCluster.runningInstances(), 100, initalEndpoints );
    }

    // Test scenario:
    // 1. Run a 3 instance cluster
    // 2. Create initialContext with running instances.
    // 3. Lookup stateful (b1) and stateless (b2) session beans.
    // 4. inst = access b1
    // 5. shutdown inst
    // 6. Create and start 2 new instances in cluster
    // 7. inst2 = access b1 (should fail over to new instance)
    // 8. shutdown inst2
    // 9. inst3 = access b2
    @Test( "15746" )
    public void test15746() throws NamingException {
        Set<String> running = gfCluster.runningInstances() ;
        if (running.size() < 3) {
            fail( "Must have cluster with at least three instances") ;
            return ;
        }

        final int numToStop = running.size() - 3 ;

        for (int ctr=0; ctr < numToStop; ctr++) {
            final String inst = pick( gfCluster.runningInstances() ) ;
            gfCluster.stopInstance(inst );
        }

        InitialContext ic = makeIC() ;
        Location slsb = lookup( ic, BeanType.SLSB ) ;
        Location sfsb = lookup( ic, BeanType.SFSB ) ;

        String inst = invokeMethod(slsb) ;
        gfCluster.stopInstance(inst);

        final String testBase = "testInstance" ;
        final int portBase = 20000 ;
        gfCluster.createInstances( testBase, 2, "", portBase ) ;
        gfCluster.startInstance( testBase + "0" );
        gfCluster.startInstance( testBase + "1" );

        final String[] flags = { /* "subcontract", "transport", */ "folb" } ;
        try {
            setORBDebug(flags);
            String inst2 = invokeMethod(slsb) ;
            gfCluster.stopInstance(inst2);

            // String inst2_1 = invokeMethod(sfsb) ;
            // gfCluster.stopInstance(inst2_1);

            String inst3 = invokeMethod(sfsb) ;
        } finally {
            clearORBDebug(flags);
        }
    }
    
    // Test scenario:
    // 1. Run a 3 instance cluster
    // 2. Create initialContext with running instances.
    // 3. Lookup stateless (b1) Session bean.
    // 4. inst1 = access b1
    // 5. shutdown inst1
    // 6. Lookup stateful (b2) Session bean.
    // 7. inst2 = access b2
    // 8. shutdown inst2
    // 9. inst3 = access b2
    @Test( "15804sfsb" )
    public void test15804sfsb() throws NamingException {
        Set<String> running = gfCluster.runningInstances() ;
        if (running.size() < 3) {
            fail( "Must have cluster with at least three instances") ;
            return ;
        } else {
            note("test15804sfsb: Cluster Size= "  +  running.size());
        }
        
        InitialContext ic = makeIC() ;
        Location slsb = lookup( ic, BeanType.SLSB ) ;
        
        String inst1 = invokeMethod(slsb) ;
        gfCluster.stopInstance(inst1);

	gfCluster.sleep(2);
        Location sfsb = lookup( ic, BeanType.SFSB ) ;
        String inst2 = invokeMethod(sfsb) ;
        gfCluster.stopInstance(inst2);
	gfCluster.sleep(2);
        String inst3 = invokeMethod(sfsb) ;
    }
   
    // same as 15804sfsb, except that we use manual kill instance
    @Test( "15804sfsb_kill" )
    public void test15804sfsb_kill() throws NamingException {
        Set<String> running = gfCluster.runningInstances() ;
        if (running.size() < 3) {
            fail( "Must have cluster with at least three instances") ;
            return ;
        } else {
            note("test15804sfsb: Cluster Size= "  +  running.size());
        }
               
        InitialContext ic = makeIC() ;
        Location slsb = lookup( ic, BeanType.SLSB ) ;
        
        String inst1 = invokeMethod(slsb) ;
        
        gfCluster.killInstance(inst1);
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
            //ignore
        }
        Location sfsb = lookup( ic, BeanType.SFSB ) ;
        String inst2 = invokeMethod(sfsb) ;    
        
        gfCluster.killInstance(inst2);
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
            //ignore
        }
        String inst3 = invokeMethod(sfsb) ;
    }
    
    // same as 15804sfsb_kill, except that we delete an instance midway
    // 1. Run a 3 instance cluster
    // 2. Create initialContext with running instances.
    // 3. Lookup stateless (b1) Session bean.
    // 4. inst1 = access b1
    // 5.1 kill inst1
    // 5.2 delete inst1
    // 6. Lookup stateful (b2) Session bean.
    // 7. inst2 = access b2
    // 8. kill inst2
    // 9. inst3 = access b2
    //Deploy MultiEJBApp.ear
//stop-cluster st-cluster
//start-instance instance110; start-instance instance109
//Kill the instance with message [SFSB1Bean.getName]
//Delete the instance  with message [SFSB1Bean.getName]
//start-cluster st-cluster
//Kill the instance with message [SFSB1Bean.getName]
//Run test with appclient: appclient ... com.sun.appserver.ee.tests.client.ClientDynamicClusterRemoveInstance MultipleFO
//Exception was throw on client side.
        
    @Test( "15804sfsb_kill_delete" )
    public void test15804sfsb_kill_delete() throws NamingException {
        Set<String> running = gfCluster.runningInstances() ;
        if (running.size() < 3) {
            fail( "Must have cluster with at least three instances") ;
            return ;
        } else {
            note("test15804sfsb: Cluster Size= "  +  running.size());
        }
               
        InitialContext ic = makeIC() ;
        Location slsb = lookup( ic, BeanType.SLSB ) ;
        
        String inst1 = invokeMethod(slsb) ;
        
        gfCluster.killInstance(inst1);
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
            //ignore
        }
        gfCluster.destroyInstance(inst1);
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
            //ignore
        }
        Location sfsb = lookup( ic, BeanType.SFSB ) ;
        String inst2 = invokeMethod(sfsb) ;    
        
        gfCluster.killInstance(inst2);
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
            //ignore
        }
        String inst3 = invokeMethod(sfsb) ;
    }
    // Test scenario:
    // 1. Run a 3 instance cluster
    // 2. Create initialContext with running instances.
    // 3. Lookup stateful (b1) Session bean.
    // 4. inst1 = access b1
    // 5. shutdown inst1
    // 6. Lookup stateless (b2) Session bean.
    // 7. inst2 = access b2
    // 8. shutdown inst2
    // 9. inst3 = access b2
    @Test( "15804slsb" )
    public void test15804slsb() throws NamingException {
        Set<String> running = gfCluster.runningInstances() ;
        if (running.size() < 3) {
            fail( "Must have cluster with at least three instances") ;
            return ;
        } else {
            note("test15804slsb: Cluster Size= "  +  running.size());
        }
        
        InitialContext ic = makeIC() ;
        Location sfsb = lookup( ic, BeanType.SFSB ) ;
        
        String inst1 = invokeMethod(sfsb) ;
        gfCluster.stopInstance(inst1);

        Location slsb = lookup( ic, BeanType.SLSB ) ;
        String inst2 = invokeMethod(slsb) ;
        gfCluster.stopInstance(inst2);
        String inst3 = invokeMethod(slsb) ;
    }

    // Test scenario:
    // 1. App is deployed on a 3 instance cluster
    // 2. Send 100 New initial Contexts, approx 30 are created on instance 1
    // 3. Bring down instance 1
    // 4. Call business methods using the IC , the 30 requests are failed
    //    over to only one instance
    // 5. The expectation is, the failoved requests should be distributed
    //    among healthy instances (15 each approx)
    @Test( "lbfail2")
    public void testLBFail2() throws NamingException {
        final int CLUSTER_SIZE = 3 ;
        final int NUM_IC = 100 ;
        Set<String> clusterInstances = new HashSet<String>() ;
        Set<String> instances = gfCluster.runningInstances() ;
        for (int ctr=0; ctr<CLUSTER_SIZE; ctr++) {
            String inst = pick(instances,true ) ;
            if (inst != null) {
                clusterInstances.add( inst ) ;
            }
        }
        check( clusterInstances.size() == 3, "Not enough instances to run test" ) ;

        gfCluster.stopCluster() ;
        for (String inst : clusterInstances) {
            gfCluster.startInstance(inst);
        }

        String shutdownInst = pick( clusterInstances ) ;
        Set<InitialContext> chosen = new HashSet<InitialContext>() ;

        for (int ctr=0; ctr<NUM_IC; ctr++ ) {
            InitialContext ic = makeIC() ;
            Location locBean = lookup(ic) ;
            String runningInstance = invokeMethod( locBean ) ;
            if (runningInstance.equals( shutdownInst )) {
                chosen.add( ic ) ;
            }
        }

        gfCluster.stopInstance( shutdownInst ) ;
        final String[] flags = { /* "subcontract", "transport", "folb" */ } ;
        try {
            // setORBDebug( flags ) ;
            instances = gfCluster.runningInstances() ;
            note( "Running instances = " + instances ) ;
            doLoadBalance( gfCluster.runningInstances(), chosen ) ;
        } finally {
            // clearORBDebug( flags ) ;
        }
    }

    @Pre
    public void ensureIC() throws NamingException {
        Set<String> runningInstances = gfCluster.runningInstances() ;
        note( "Running instances: " + runningInstances ) ;
        // Make sure an initial context is created with endpoints before any
        // test starts
        makeIC() ;
    }

    @Post
    public void ensure() {
        // make sure all instances are running
        gfCluster.startCluster();
    }
}
