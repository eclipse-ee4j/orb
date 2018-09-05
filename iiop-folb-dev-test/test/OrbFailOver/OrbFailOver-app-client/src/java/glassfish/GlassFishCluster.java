/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package glassfish;

import argparser.Pair;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import testtools.Base;

/** Class used to create and manage a GlassFish cluster across multiple nodes.
 *
 * @author ken
 */
public class GlassFishCluster {
    // This is also the definition used in the implementation of the
    // EJB, but I don't want to import that here.  Actually, this doesn't
    // really belong here either, but it's too important to the IIOP FOLB
    // tests to not include, and putting it into Main doesn't work either
    // when createInstances is used.
    public static final String INSTANCE_NAME_PROPERTY = "instance_name" ;

    private final GlassFishInstallation gfInst ;
    private final String clusterName ;
    private final Map<String,InstanceInfo> instanceInfoMap ;
    private final Set<String> runningInstances ;

    public GlassFishCluster( GlassFishInstallation gfInst, String clusterName,
        boolean skipSetup ) {
        this.clusterName = clusterName ;
        this.gfInst = gfInst ;
        this.instanceInfoMap = new HashMap<String,InstanceInfo>() ;
        this.runningInstances = new HashSet<String>() ;

        if (skipSetup) {
            final AdminCommand ac = gfInst.ac() ;
            ac.listInstances( clusterName ) ;
            final Map<String,String> instances = ac.getOutputTable() ;
            for (Map.Entry<String,String> entry : instances.entrySet()) {
                final String inst = entry.getKey() ;
                final String state = entry.getValue() ;
                if (state.equals( "running" )) {
                    runningInstances.add( inst ) ;
                }
                final String instDNPrefix = "servers.server." + inst ;
                final String naDN = instDNPrefix + ".node-ref" ;
                ac.get( naDN ) ;
                final Map<String,String> naRef = ac.getOutputProperties() ;
                final String na = naRef.get( naDN ) ;

                final String nodeDN = "nodes.node." + na + ".node-host" ;
                ac.get(nodeDN) ;
                final Map<String,String> nodeRef = ac.getOutputProperties() ;
                final String node = nodeRef.get( nodeDN ) ;

                final InstanceInfo ii = new InstanceInfo( inst, node ) ;
                final String propsDN =  instDNPrefix + ".system-property.*.value" ;
                ac.get( propsDN ) ;
                final Map<String,String> ports = ac.getOutputProperties() ;
                for (Map.Entry<String,String> entry2 : ports.entrySet()) {
                    final String prop = entry2.getKey() ;
                    final String[] tokens = prop.split( "\\." ) ;
                    if (tokens.length == 6) {
                        final String key = tokens[4] ;
                        if (key.contains( "PORT" )) {
                            final String value = entry2.getValue() ;
                            final StandardPorts port = StandardPorts.valueOf( key ) ;
                            final int pnum = Integer.parseInt(value) ;
                            ii.addPort(port, pnum);
                        }
                    }
                }
                instanceInfoMap.put( inst, ii ) ;
            }
        } else {
            gfInst.ac().createCluster( clusterName ) ;
            for (Pair<String,Integer> pair : gfInst.availableNodes()) {
                String node = pair.first() ;
                gfInst.ac().createNodeSsh( node, gfInst.installDir(),
                    gfInst.getNAName(node) );
            }
        }
    }

    public InstanceInfo createInstance( String instanceName, String nodeName,
        int portBase ) {
        return createInstance( instanceName, nodeName, portBase, "" ) ;
    }

    public InstanceInfo createInstance( String instanceName, String nodeName,
        int portBase, String orbDebug ) {

        gfInst.ac().createInstance( gfInst.getNAName( nodeName ), clusterName,
            portBase, instanceName ) ;
        InstanceInfo info = new InstanceInfo(instanceName, nodeName) ;
        for (String str : gfInst.ac().commandOutput() ) {
            int index = str.indexOf( '=' ) ;
            if (index > 0) {
                final String pnameString = str.substring( 0, index ) ;
                final String pnumString = str.substring( index + 1 ) ;
                StandardPorts pname = StandardPorts.valueOf(pnameString) ;
                int pnum = Integer.parseInt(pnumString) ;
                info.addPort( pname, pnum) ;
            }
        }

        instanceInfoMap.put( instanceName, info) ;

        String debugDottedName =
            "configs.config." + clusterName + "-config.java-config.debug-enabled" ;
        gfInst.ac().set( debugDottedName, "true" ) ;

        // Note: setting properties on GF 3.1 does not work properly on
        // a create-instance commands: see issue 15683.
        // So we use a separate command for this.
        Properties props = new Properties() ;
        if ((orbDebug != null) && !orbDebug.isEmpty()) {
            props.setProperty( "com.sun.corba.ee.ORBDebug", orbDebug ) ;
            gfInst.ac().createSystemProperties(instanceName, props);
        }


        return info ;
    }

    private static final int CREATE_INSTANCES_PORT_BASE = 9000 ;
    private static final int CREATE_INSTANCES_PORT_INCREMENT = 1000 ;

    // Worst case: run out of ports because of how this is constructed
    // and how ports are allocated on a single node.  This is 56, which
    // should be more instances than I ever node for a unit test.
    private static final int MAX_INSTANCES =
        (Short.MAX_VALUE - CREATE_INSTANCES_PORT_BASE) /
            CREATE_INSTANCES_PORT_INCREMENT ;

    /** Create a number of instances spread across the available nodes in the
     * cluster.  The name of each instance is instanceBaseName + number, for
     * a number from 0 to numInstances - 1.  numInstances must not exceed
     * the total available capacity in the GF installation as indicated by
     * the elements of gfInst.availableNodes.
     *
     * @param instanceBaseName The base name to use for all instance names.
     * @param numInstances The number of instances to create.
     * @param orbDebug comma separated list of ORBDebug flags to turn on in
     * the created instances.
     */
    public Map<String,InstanceInfo> createInstances( String instanceBaseName,
        int numInstances, String orbDebug ) {
        return createInstances( instanceBaseName, numInstances, orbDebug,
            CREATE_INSTANCES_PORT_BASE ) ;
    }

    /** Create a number of instances spread across the available nodes in the
     * cluster.  The name of each instance is instanceBaseName + number, for
     * a number from 0 to numInstances - 1.  numInstances must not exceed
     * the total available capacity in the GF installation as indicated by
     * the elements of gfInst.availableNodes.
     *
     * @param instanceBaseName The base name to use for all instance names.
     * @param numInstances The number of instances to create.
     * @param orbDebug comma separated list of ORBDebug flags to turn on in
     * the created instances.
     */
    public Map<String,InstanceInfo> createInstances( String instanceBaseName,
        int numInstances, String orbDebug, int portBase ) {

        int numAvailable = 0 ;
        for (Pair<String,Integer> pair : gfInst.availableNodes() ) {
            numAvailable += pair.second() ;
        }

        if (numInstances > MAX_INSTANCES) {
            throw new RuntimeException( "Request number of instances "
                + numInstances + " is greater than maximum instances supported"
                + MAX_INSTANCES ) ;
        }

        if (numInstances > numAvailable) {
            throw new RuntimeException( "Request number of instances "
                + numInstances + " is greater than available instances "
                + numAvailable ) ;
        }

        WeightedCircularIterator<String> iter =
            new WeightedCircularIterator<String>() ;
        for (Pair<String,Integer> pair : gfInst.availableNodes() ) {
            iter.add( pair.first(), pair.second() ) ;
        }

        final Map<String,InstanceInfo> result =
            new HashMap<String,InstanceInfo>() ;
        for (int index=0; index<numInstances; index++) {
            final String node = iter.next() ;
            final int currentPortBase = portBase
                + index * CREATE_INSTANCES_PORT_INCREMENT ;
            final String instanceName = instanceBaseName + index ;
            InstanceInfo info = createInstance( instanceName, node, 
                currentPortBase, orbDebug) ;
            result.put( instanceName, info ) ;
        }

        return result ;
    }

    public void destroyInstance( String instanceName ) {
        gfInst.ac().destroyInstance(instanceName);
        instanceInfoMap.remove( instanceName ) ;
    }

    public void startInstance( String instanceName ) {
        if (instanceInfoMap.keySet().contains(instanceName)) {
            if (gfInst.ac().startInstance( instanceName )) {
                runningInstances.add( instanceName ) ;
            }
        }
    }

    public void stopInstance( String instanceName ) {
        if (instanceInfoMap.keySet().contains(instanceName)) {
            if (gfInst.ac().stopInstance( instanceName )) {
                runningInstances.remove( instanceName ) ;
            }
        }
    }
    
    public void killInstance( String instanceName ) {
        if (instanceInfoMap.keySet().contains(instanceName)) {
            if (gfInst.ac().killInstance( instanceName )) {
                runningInstances.remove( instanceName ) ;
            }
        }
    }
    
    public Set<String> runningInstances() {
        return new HashSet<String>( runningInstances ) ;
    }

    public void startCluster() {
        if (gfInst.ac().startCluster(clusterName)) {
            runningInstances.clear() ;
            runningInstances.addAll( instanceInfoMap.keySet() ) ;
        }
    }

    public void stopCluster() {
        if (gfInst.ac().stopCluster(clusterName)) {
            runningInstances.clear() ;
        }
    }

    public void destroyCluster() {
        stopCluster() ;

        Set<String> instances = new HashSet<String>( instanceInfoMap.keySet() ) ;
        for (String instName : instances) {
            destroyInstance(instName) ;
        }

        for (Pair<String,Integer> pair : gfInst.availableNodes()) {
            String node = pair.first() ;
            gfInst.ac().destroyNodeSsh( gfInst.getNAName(node) );
        }

        gfInst.ac().destroyCluster( clusterName ) ;
    }

    public void sleep(int seconds ) {
        gfInst.ac().sleep( seconds ) ;
    }

    public static class InstanceInfo {
        private final String name ;
        private final String node ;
        private final Map<StandardPorts,Integer> portMap ;

        public InstanceInfo( String name, String node ) {
            this.name = name ;
            this.node = node ;
            this.portMap = new EnumMap<StandardPorts,Integer>(
                StandardPorts.class) ;
        }

        public String name() {
            return name ;
        }

        public String node() {
            return node ;
        }

        public Map<StandardPorts,Integer> ports() {
            return portMap ;
        }

        void addPort( StandardPorts pname, int pnum ) {
            portMap.put( pname, pnum ) ;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder() ;
            sb.append( "InstanceInfo[" ) ;
            sb.append( name ).append( ',' ) ;
            sb.append( node ).append( ',' ) ;
            sb.append( portMap.toString() ) ;
            sb.append( ']' ) ;
            return sb.toString() ;
        }
    }

    public Map<String,InstanceInfo> instanceInfo() {
        return instanceInfoMap ;
    }

    public static class Test extends Base {
        private static final String installDir =
            "/volumes/work/GlassFish/v3/glassfishv3/glassfish" ;
        private static final String dasNodeName = "minas" ;
        private static final List<Pair<String,Integer>> availableNodes =
            new ArrayList<Pair<String,Integer>>() ;

        static {
            availableNodes.add( new Pair<String,Integer>( "minas", 3 ) ) ;
            availableNodes.add( new Pair<String,Integer>( "hermes", 2 ) ) ;
            availableNodes.add( new Pair<String,Integer>( "apollo", 4 ) ) ;
        }

        private GlassFishInstallation gfInst =
            new GlassFishInstallation( this, installDir, dasNodeName,
                availableNodes, true, false ) ;

        private static final String clusterName = "c1" ;

        private GlassFishCluster gfCluster =
            new GlassFishCluster( gfInst, clusterName, false ) ;

        @testtools.Test
        public void testCreateInstance() {
            InstanceInfo info = gfCluster.createInstance( "in1", "minas", 2000 ) ;
            note( "createInstance returned " + info ) ;
            gfCluster.destroyInstance( "in1" ) ;
        }

        @testtools.Test
        public void testCreateInstances() {
            Map<String,InstanceInfo> infos = gfCluster.createInstances(
                 "in", 7, "" ) ;
            note( "createInstances returned " + infos ) ;
            gfCluster.destroyCluster();
        }

        @testtools.Test
        public void testStartStop() {
            Map<String,InstanceInfo> infos = gfCluster.createInstances(
                 "in", 7, "" ) ;
            Set<String> instances = new HashSet<String>(
                infos.keySet() )  ;
            gfCluster.startCluster() ;
            check( instances.equals( gfCluster.runningInstances() ),
                "Expected running instances does not match instances") ;

            gfCluster.stopInstance( "in1" ) ;
            instances.remove( "in1" ) ;
            check( instances.equals( gfCluster.runningInstances() ),
                "Expected running instances does not match instances") ;

            gfCluster.startInstance( "in1" ) ;
            instances.add( "in1" ) ;
            check( instances.equals( gfCluster.runningInstances() ),
                "Expected running instances does not match instances") ;

            gfCluster.stopCluster() ;
            instances.clear() ;
            check( instances.equals( gfCluster.runningInstances() ),
                "Expected running instances does not match instances") ;
        }

        public Test( String[] args ) {
            super( args ) ;
        }
    }

    public static void main( String[] args ) {
        Test test = new Test( args ) ;
        test.run() ;
    }
}
