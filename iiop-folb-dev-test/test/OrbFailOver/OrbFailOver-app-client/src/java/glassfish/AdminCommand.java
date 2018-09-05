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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import testtools.Base;

/**
 *
 * @author ken
 */
public class AdminCommand {
    // Note that only one thread at a time may use an instance of AdminCommand.
    private static final String ASADMIN_CMD_PROP = "test.folb.asadmin.command" ;
    private static final String DEFAULT_ASADMIN = System.getProperty(
        ASADMIN_CMD_PROP ) ;

    private final Base base ;
    private final String asadmin ;
    private final boolean echoOnly ; // echo command only if true: do not exec
    private final List<String> commandOutput = new ArrayList<String>() ;
    private final IntervalTimer timer = new IntervalTimer() ;

    public AdminCommand( Base base ) {
        this( base, DEFAULT_ASADMIN ) ;
    }

    public AdminCommand( Base base, String asadmin ) {
        this( base, asadmin, false ) ;
    }
    public AdminCommand( Base base, String asadmin, boolean echoOnly ) {
        this.base = base ;
        this.asadmin = asadmin ;
        this.echoOnly = echoOnly ;
    }

    public static class IntervalTimer {
        long lastTime ;

        public void start() {
            lastTime = System.nanoTime() ;
        }

        /** Returns interval since last start() or interval() call in
         * microseconds.
         * @return Elapsed time in microseconds
         */
        public long interval() {
            final long current = System.nanoTime() ;
            final long diff = current - lastTime ;
            start() ;
            return diff/1000 ;
        }
    }
    private boolean adminCommand( String command ) {
        return Command(asadmin + " " + command);
    }

    private boolean Command( String cmd ) {
        commandOutput.clear() ;
        base.note( "Command " + cmd ) ;

        if (!echoOnly) {
            try {
                timer.start() ;
                final Process proc = Runtime.getRuntime().exec( cmd ) ;
                final InputStream is = proc.getInputStream() ;
                final BufferedReader reader = new BufferedReader(
                    new InputStreamReader( is ) );
                String line = reader.readLine() ;
                while (line != null) {
                    base.note( line ) ;
                    commandOutput.add( line ) ;
                    line = reader.readLine() ;
                }

                final int result = proc.waitFor();
                if (result != 0) {
                    throw new RuntimeException( "Command " + cmd
                        + " failed with result " + result ) ;
                }
            } catch (Exception ex) {
                base.note( "Exception " + ex + " in " + cmd ) ;
                // ex.printStackTrace();
                return false ;
            } finally {
                base.note( "Command " + cmd + " executed in "
                    + timer.interval() + " microseconds\n" ) ;
            }
        }

        return true ;
    }

    public List<String> commandOutput() {
        return commandOutput ;
    }

    public boolean startDomain() {
        return adminCommand( "start-domain" ) ;
    }

    public boolean stopDomain() {
        return adminCommand( "stop-domain" ) ;
    }

    public boolean enableSecureAdmin() {
        return adminCommand( "enable-secure-admin" ) ;
    }

    public boolean createCluster( String clusterName ) {
        return adminCommand( "create-cluster " + clusterName ) ;
    }

    public boolean deleteCluster( String clusterName ) {
        return adminCommand( "delete-cluster " + clusterName ) ;
    }

    public boolean startCluster( String clusterName ) {
        return adminCommand( "start-cluster " + clusterName ) ;
    }

    public boolean stopCluster( String clusterName ) {
        return adminCommand( "stop-cluster " + clusterName ) ;
    }

    public boolean createNodeSsh( String nodeHost, String installDir,
        String agentName ) {
        String str = String.format( 
            "--user admin create-node-ssh --nodehost %s --installdir %s %s", 
            nodeHost, installDir, agentName );
        return adminCommand( str ) ;
    }

    public boolean destroyNodeSsh( String agentName ) {
        return adminCommand( "delete-node-ssh " + agentName ) ;
    }

    private String getPropertiesString( Properties props ) {
        final StringBuilder sb = new StringBuilder() ;
        final Set<String> propNames = props.stringPropertyNames() ;

        boolean first = true ;

        for (String pname : propNames) {
             if (first) {
                 first = false ;
             } else {
                 sb.append( ':' ) ;
             }

             sb.append( pname ) ;
             sb.append( '=' ) ;
             sb.append( props.getProperty(pname) ) ;
        }

        return sb.toString() ;
    }

    public boolean createSystemProperties( String instanceName, Properties props ) {
        if (props == null) {
            return false ;
        }

        String command = String.format(
            "create-system-properties --target %s %s", instanceName,
            getPropertiesString(props)) ;

        boolean result = adminCommand( command ) ;
        return result ;
    }

    public boolean createInstance( String agentName, String clusterName, 
        int portBase, String instanceName ) {
        String command = String.format( "create-instance --node %s "
                + "--cluster %s --portbase %d --checkports=true %s",
                agentName, clusterName, portBase, instanceName )  ;

        boolean result = adminCommand( command ) ;

        if (echoOnly) {
            // for testing only
            int current = portBase ;
            for (StandardPorts sport : StandardPorts.values()) {
                String msg = sport + "=" + current++ ;
                base.note( msg );
                commandOutput.add( msg ) ;
            }
        }

        return result ;
    }

    public boolean destroyInstance( String instanceName ) {
        return adminCommand( "delete-instance " + instanceName ) ;
    }


    public void sleep( int seconds ) {
        try {
            // delay to make sure change has actually propagated
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException ex) {
            // ignore this
        }
    }

    public boolean startInstance( String name ) {
        boolean result = adminCommand( "start-instance " + name) ;
        // sleep(10) ;
        return result ;
    }

    public boolean stopInstance( String name ) {
        boolean result = adminCommand( "stop-instance --force true " + name) ;
        // sleep(10) ;
        return result ;
    }

    public boolean killInstance( String name ) {
        // uniquely identify the process id
        String processCommandContains = name + "/config" ;
        if (!Command("scripts/kill " + processCommandContains)) {
            System.out.println("SEVERE ERROR: Failed to kill instance "  + name + ", trying stop instance!");
            return stopInstance(name);
        }   
        return true ;
    }
    
    public boolean destroyCluster(String clusterName) {
        return adminCommand( "delete-cluster " + clusterName ) ;
    }

    public boolean deploy( String clusterName, String componentName,
        String jarFile ) {

        String cmd = String.format( "deploy --target %s --name %s --force %s",
            clusterName, componentName, jarFile )  ;

        return adminCommand( cmd ) ;
    }

    public boolean deploy( String clusterName, String componentName,
        String jarFile, boolean availabilityEnabled ) {

        String cmd = String.format( "deploy --target %s --name %s --availabilityenabled=%b --force %s",
            clusterName, componentName, availabilityEnabled, jarFile)  ;

        return adminCommand( cmd ) ;
    }

    public boolean undeploy( String clusterName, String componentName ) {

        String cmd = String.format( "undeploy --target %s --name %s --force",
            clusterName, componentName )  ;

        return adminCommand( cmd ) ;
    }

    public boolean set( String dottedName, String value ) {
        String cmd = String.format( "set %s=%s", dottedName, value ) ;
        return adminCommand( cmd ) ;
    }

    public boolean get( String dottedName ) {
        return adminCommand( "get " + dottedName ) ;
    }

    public Map<String,String> getOutputProperties() {
        Map<String,String> result = new HashMap<String,String>() ;
        for (String str : commandOutput() ) {
            int index = str.indexOf( '=' ) ;
            if (index > 0) {
                final String key = str.substring( 0, index ) ;
                final String value = str.substring( index + 1 ) ;
                result.put( key, value ) ;
            }
        }
        return result ;
    }

    public boolean listClusters() {
        return adminCommand( "list-clusters" ) ;
    }

    public boolean listInstances( String clusterName ) {
        return adminCommand( "list-instances " + clusterName ) ;
    }

    public Map<String,String> getOutputTable() {
        Map<String,String> result = new HashMap<String,String>() ;
        for (String str : commandOutput() ) {
            String[] token = str.split( "\\s+" ) ;
            if (token.length == 2) {
                result.put( token[0], token[1] ) ;
            }
        }
        return result ;
    }
}
