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

import java.util.*;
import com.sun.corba.ee.spi.misc.ORBConstants;

/** ProcessMonitorThread is started when ServerManager is instantiated. The 
  * thread wakes up every minute (This can be changed by setting sleepTime) and
  * makes sure that all the processes (Servers) registered with the ServerTool
  * are healthy. If not the state in ServerTableEntry will be changed to
  * De-Activated.
  * Note: This thread can be killed from the main thread by calling 
  *       interrupThread()
  */
public class ProcessMonitorThread extends java.lang.Thread {
    private Map<Integer,ServerTableEntry> serverTable;
    private int sleepTime; 
    private static ProcessMonitorThread instance = null;

    private ProcessMonitorThread( Map<Integer,ServerTableEntry> ServerTable, int SleepTime ) {
        serverTable = ServerTable;
        sleepTime = SleepTime;
    }

    public void run( ) {
        while( true ) {
            try {
                // Sleep's for a specified time, before checking
                // the Servers health. This will repeat as long as
                // the ServerManager (ORBD) is up and running.
                Thread.sleep( sleepTime );
            } catch( java.lang.InterruptedException e ) {
                break;
            }
            synchronized ( serverTable ) {
                // Check each ServerTableEntry to make sure that they
                // are in the right state.
                Iterator serverList = serverTable.values().iterator();
                checkServerHealth( serverList );
            }
        }
    }

    private void checkServerHealth( Iterator serverList ) {
        while (serverList.hasNext( ) ) {
            ServerTableEntry entry = (ServerTableEntry) serverList.next();
            entry.checkProcessHealth( );
        }
    }

    static void start( Map<Integer,ServerTableEntry> serverTable ) { 
        int sleepTime = ORBConstants.DEFAULT_SERVER_POLLING_TIME;

        String pollingTime = System.getProperties().getProperty( 
            ORBConstants.SERVER_POLLING_TIME ); 

        if ( pollingTime != null ) {
            try {
                sleepTime = Integer.parseInt( pollingTime ); 
            } catch (Exception e ) {
                // Too late to complain, Just use the default 
                // sleepTime
            }
        }

        instance = new ProcessMonitorThread( serverTable, 
            sleepTime );
        instance.setDaemon( true );
        instance.start();
    }

    static void interruptThread( ) {
        instance.interrupt();
    }
}
 
