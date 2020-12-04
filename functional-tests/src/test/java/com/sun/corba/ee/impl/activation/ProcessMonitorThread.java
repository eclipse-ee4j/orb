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
 
