/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.activation;

/**
 *
 * @version     1.37
 * @author      Anita Jindal
 * @since       JDK1.2
 */

 
import com.sun.corba.ee.spi.activation.Server;
import com.sun.corba.ee.spi.activation.EndPointInfo;
import com.sun.corba.ee.spi.activation.ORBAlreadyRegistered;
import com.sun.corba.ee.spi.activation.ORBPortInfo;
import com.sun.corba.ee.spi.activation.InvalidORBid;
import com.sun.corba.ee.spi.activation.ServerHeldDown;
import com.sun.corba.ee.spi.activation.RepositoryPackage.ServerDef;

import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.spi.logging.ActivationSystemException ;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.NoSuchElementException;

public class ServerTableEntry 
{

    private final static int DE_ACTIVATED = 0;
    private final static int ACTIVATING   = 1;
    private final static int ACTIVATED    = 2;
    private final static int RUNNING      = 3;
    private final static int HELD_DOWN    = 4;


    private String printState()
    {
        String str = "UNKNOWN";

        switch (state) {
        case (DE_ACTIVATED) : str = "DE_ACTIVATED"; break;
        case (ACTIVATING  ) : str = "ACTIVATING  "; break;
        case (ACTIVATED   ) : str = "ACTIVATED   "; break;
        case (RUNNING     ) : str = "RUNNING     "; break;
        case (HELD_DOWN   ) : str = "HELD_DOWN   "; break;
        default: break;
        }

        return str;
    }

    private final static long waitTime    = 2000;
    private static final int ActivationRetryMax = 5;

    // state of each entry
    private int state;
    private int serverId;
    private Map<String,List<EndPointInfo>> orbAndPortInfo ;
    private Server serverObj;
    private ServerDef serverDef;
    private Process process;
    private int activateRetryCount=0;
    private String activationCmd;
    private ActivationSystemException wrapper ;

    @Override
    public String toString()
    {
        return "ServerTableEntry[" + "state=" + printState() +
            " serverId=" + serverId +
            " activateRetryCount=" + activateRetryCount + "]" ;
    }

    // get the string needed to make the activation command
    private static String javaHome, classPath, fileSep, pathSep;

    static {
        javaHome  = System.getProperty("java.home");
        classPath = System.getProperty("java.class.path");
        fileSep   = System.getProperty("file.separator");
        pathSep   = System.getProperty("path.separator");
    }

    ServerTableEntry( ActivationSystemException wrapper, 
        int serverId, ServerDef serverDef, int initialPort,
        String dbDirName, boolean verify, boolean debug )
    {
        this.wrapper = wrapper ;
        this.serverId = serverId;
        this.serverDef = serverDef;
        this.debug = debug ;

        orbAndPortInfo = new HashMap<String,List<EndPointInfo>>();

        activateRetryCount = 0;
        state = ACTIVATING;

        // compute the activation command
        activationCmd = 
        
            // add path to the java vm
            javaHome + fileSep + "bin" + fileSep + "java " +

            // add any arguments to the server Java VM
            serverDef.serverVmArgs + " " + 

            // add ORB properties
            "-Dioser=" + System.getProperty( "ioser" ) + " " +
            "-D" + ORBConstants.INITIAL_PORT_PROPERTY   + "=" + initialPort + " " +
            "-D" + ORBConstants.DB_DIR_PROPERTY         + "=" + dbDirName + " " +
            "-D" + ORBConstants.ACTIVATED_PROPERTY      + "=true " + 
            "-D" + ORBConstants.ORB_SERVER_ID_PROPERTY  + "=" + serverId + " " +
            "-D" + ORBConstants.SERVER_NAME_PROPERTY    + "=" + serverDef.serverName + " " + 
            // we need to pass in the verify flag, so that the server is not
            // launched, when we try to validate its definition during registration
            // into the RepositoryImpl

            (verify ? "-D" + ORBConstants.SERVER_DEF_VERIFY_PROPERTY + "=true ": "") + 

            // add classpath to the server
            "-classpath " + classPath + 
            (serverDef.serverClassPath.equals("") == true ? "" : pathSep) + 
            serverDef.serverClassPath + 

            // add server class name and arguments
            " com.sun.corba.ee.impl.activation.ServerMain " + serverDef.serverArgs
            
            // Add the debug flag, if any
            + (debug ? " -debug" : "") ;

        if (debug) System.out.println( 
                                      "ServerTableEntry constructed with activation command " + 
                                      activationCmd);
    }

    /** 
     * Verify whether the server definition is valid.
     */
    public int verify()
    {
        try {

            if (debug)
                System.out.println("Server being verified w/" + activationCmd);
        
            process = Runtime.getRuntime().exec(activationCmd);
            int result = process.waitFor();
            if (debug)
                printDebug( "verify", "returns " + ServerMain.printResult( result ) ) ;
            return result ;
        } catch (Exception e) {
            if (debug)
                printDebug( "verify", "returns unknown error because of exception " + 
                            e ) ;
            return ServerMain.UNKNOWN_ERROR;
        }
    }

    private void printDebug(String method, String msg)
    {
        System.out.println("ServerTableEntry: method  =" + method);
        System.out.println("ServerTableEntry: server  =" + serverId);
        System.out.println("ServerTableEntry: state   =" + printState());
        System.out.println("ServerTableEntry: message =" + msg);
        System.out.println();
    }
        
    synchronized void activate() throws org.omg.CORBA.SystemException
    {
        state = ACTIVATED;

        try {
            if (debug)
                printDebug("activate", "activating server");
            process = Runtime.getRuntime().exec(activationCmd);
        } catch (Exception e) { 
            deActivate();
            if (debug)
                printDebug("activate", "throwing premature process exit");
            throw wrapper.unableToStartProcess() ;
        }
    }

    synchronized void register(Server server)
    {
        if (state == ACTIVATED) {

            serverObj = server;

            //state = RUNNING;
            //notifyAll();

            if (debug) 
                printDebug("register", "process registered back");

        } else {

            if (debug) 
                printDebug("register", "throwing premature process exit");
            throw wrapper.serverNotExpectedToRegister() ; 
        }
    }

    synchronized void registerPorts( String orbId, EndPointInfo [] endpointList)
        throws ORBAlreadyRegistered
    {

        // find if the ORB is already registered, then throw an exception
        if (orbAndPortInfo.containsKey(orbId)) {
            throw new ORBAlreadyRegistered(orbId);
        }

        // store all listener ports and their types 
        List<EndPointInfo> serverListenerPorts = new ArrayList<EndPointInfo>() ;
        for (int i = 0; i < endpointList.length; i++) {
            serverListenerPorts.add( new EndPointInfo( 
                endpointList[i].endpointType, endpointList[i].port ) ) ;

            if (debug)
                System.out.println("registering type: " + endpointList[i].endpointType  +  
                    "  port  " + endpointList[i].port);
        }

        // put this set of listener ports in the HashMap associated
        // with the orbId
        orbAndPortInfo.put(orbId, serverListenerPorts);
        if (state == ACTIVATED) {
            state = RUNNING;
            notifyAll();
        }
        // _REVISIT_, If the state is not equal to ACTIVATED then it is a bug
        // need to log that error, once the Logging framework is in place
        // for rip-int.
        if (debug) 
            printDebug("registerPorts", "process registered Ports");
    }
    
    synchronized void install()
    {
        if (state == RUNNING)
            serverObj.install() ;
        else
            throw wrapper.serverNotRunning() ;
    }

    synchronized void uninstall()
    {
        if (state == RUNNING) {

            deActivate();

            try {
                if (serverObj != null) {
                    serverObj.shutdown(); // shutdown the server
                    serverObj.uninstall() ; // call the uninstall
                }

                if (process != null) {
                    process.destroy();
                }
            } catch (Exception ex) {
                // what kind of exception should be thrown
            }
        } else {
            throw wrapper.serverNotRunning() ;
        }
    }

    synchronized void holdDown() 
    {
        state = HELD_DOWN;

        if (debug) 
            printDebug( "holdDown", "server held down" ) ;

        notifyAll();
    }

    synchronized void deActivate() 
    {
        state = DE_ACTIVATED;

        if (debug)
            printDebug( "deActivate", "server deactivated" ) ;

        notifyAll();
    }

    synchronized void checkProcessHealth( ) {
        // If the State in the ServerTableEntry is RUNNING and the
        // Process was shut down abnormally, The method will change the
        // server state as De-Activated.
        if( state == RUNNING ) {
            try {
                int exitVal = process.exitValue();
            } catch (IllegalThreadStateException e1) {
                return;
            }
            synchronized ( this ) {
                // Clear the PortInformation as it is old 
                orbAndPortInfo.clear(); 
                // Move the state to De-Activated, So that the next
                // call to this server will re-activate.
                deActivate();
            }
        }
    }

    synchronized boolean isValid() 
    {
        if ((state == ACTIVATING) || (state == HELD_DOWN)) {
            if (debug)
                printDebug( "isValid", "returns true" ) ;

            return true;
        }       

        try {
            int exitVal = process.exitValue();
        } catch (IllegalThreadStateException e1) {
            return true;
        }  
     
        if (state == ACTIVATED) {
            if (activateRetryCount < ActivationRetryMax) {
                if (debug)
                    printDebug("isValid", "reactivating server");
                activateRetryCount++;
                activate(); 
                return true;
            }
         
            if (debug)
                printDebug("isValid", "holding server down");

            holdDown();
            return true;
        }
 
        deActivate();
        return false;
    }

    synchronized ORBPortInfo[] lookup(String endpointType) throws ServerHeldDown 
    {
        while ((state == ACTIVATING) || (state == ACTIVATED)) {
            try {
                wait(waitTime);
                if (!isValid()) break;
            } catch(Exception e) {}
        }

        ORBPortInfo[] orbAndPortList = null;
     
        if (state == RUNNING) {
            orbAndPortList = new ORBPortInfo[orbAndPortInfo.size()];

            try {
                int numElements = 0;
                int i;
                int port;
                for ( String orbId : orbAndPortInfo.keySet() ) {
                    // get an entry corresponding to orbId
                    List<EndPointInfo> serverListenerPorts = orbAndPortInfo.get(orbId);
                    port = -1;
                    // return the port corresponding to the endpointType
                    for (EndPointInfo ep : serverListenerPorts) {
                        if (debug)
                            System.out.println("lookup num-ports " + 
                                serverListenerPorts.size() + "   " + 
                                ep.endpointType + "   " + ep.port );
                        if (ep.endpointType.equals(endpointType)) {
                            port = ep.port;
                            break;
                        }
                    }
                    orbAndPortList[numElements] = new ORBPortInfo(orbId, port);
                    numElements++;
                }
            } catch (NoSuchElementException e) {
                // have everything in the table
            }
            return orbAndPortList;
        }
     
        if (debug) 
            printDebug("lookup", "throwing server held down error");
 
        throw new ServerHeldDown( serverId ) ;
    }

    synchronized EndPointInfo[] lookupForORB(String orbId) 
        throws ServerHeldDown, InvalidORBid
    {
        while ((state == ACTIVATING) || (state == ACTIVATED)) {
            try {
                wait(waitTime);
                if (!isValid()) break;
            } catch(Exception e) {}
        }
        EndPointInfo[] portList = null;
     
        if (state == RUNNING) {
            try {
                // get an entry corresponding to orbId
                List<EndPointInfo> serverListenerPorts = orbAndPortInfo.get(orbId);

                portList = new EndPointInfo[serverListenerPorts.size()];
                // return the port corresponding to the endpointType
                int i = 0 ;
                for (EndPointInfo ep : serverListenerPorts) {
                    if (debug)
                        System.out.println("lookup num-ports " + 
                            serverListenerPorts.size() + "   " + 
                            ep.endpointType + "   " + ep.port );
                    portList[i] = new EndPointInfo(ep.endpointType, ep.port);
                }
            } catch (NoSuchElementException e) {
                // no element in HashMap corresponding to ORBid found
                throw new InvalidORBid();
            }

            return portList;
        }
     
        if (debug) 
            printDebug("lookup", "throwing server held down error");
 
        throw new ServerHeldDown( serverId ) ;
    }

    synchronized String[] getORBList()
    {
        String [] orbList = new String[orbAndPortInfo.size()];

        try {
            int numElements = 0;
            for ( String orbId : orbAndPortInfo.keySet() ) {
                orbList[numElements++] = orbId ;
            }
        } catch (NoSuchElementException e) {
            // have everything in the table
        }
        return orbList;
    }
 
    int getServerId()
    {
        return serverId;
    }
    
    boolean isActive()
    {
        return (state == RUNNING) || (state == ACTIVATED);
    }

    synchronized void destroy()
    {

        deActivate();

        try {
            if (serverObj != null) 
                serverObj.shutdown();

            if (debug)
                printDebug( "destroy", "server shutdown successfully" ) ;
        } catch (Exception ex) {
            if (debug)
                printDebug( "destroy", 
                            "server shutdown threw exception" + ex ) ;
            // ex.printStackTrace();
        }

        try {
            if (process != null) 
                process.destroy();

            if (debug)
                printDebug( "destroy", "process destroyed successfully" ) ;
        } catch (Exception ex) {
            if (debug)
                printDebug( "destroy", 
                            "process destroy threw exception" + ex ) ;

            // ex.printStackTrace();
        }
    }

    private boolean debug = false;
}
