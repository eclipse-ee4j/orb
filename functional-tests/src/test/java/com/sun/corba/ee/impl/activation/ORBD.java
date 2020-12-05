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

import com.sun.corba.ee.impl.legacy.connection.SocketFactoryAcceptorImpl;
import com.sun.corba.ee.impl.misc.CorbaResourceUtil;
import com.sun.corba.ee.impl.naming.cosnaming.TransientNameService;
import com.sun.corba.ee.impl.transport.AcceptorImpl;
import com.sun.corba.ee.spi.activation.Activator;
import com.sun.corba.ee.spi.activation.ActivatorHelper;
import com.sun.corba.ee.spi.activation.Locator;
import com.sun.corba.ee.spi.activation.LocatorHelper;
import com.sun.corba.ee.spi.activation.RepositoryPackage.ServerDef;
import com.sun.corba.ee.spi.legacy.connection.LegacyServerSocketEndPointInfo;
import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.transport.Acceptor;
import com.sun.corba.ee.spi.transport.SocketInfo;

import java.io.File;
import java.util.Properties;

/**
 * 
 * @version     1.10, 97/12/06
 * @author      Rohit Garg
 * @since       JDK1.2
 */
public class ORBD
{

    protected void initializeBootNaming(ORB orb)
    {
        // create a bootstrap server
        int initSvcPort = orb.getORBData().getORBInitialPort();

        Acceptor acceptor;
        // REVISIT: see ORBConfigurator. use factory in TransportDefault.
        if (orb.getORBData().getLegacySocketFactory() == null) {
            acceptor = 
                new AcceptorImpl(
                    orb,
                        initSvcPort,
                    LegacyServerSocketEndPointInfo.BOOT_NAMING,
                    SocketInfo.IIOP_CLEAR_TEXT);
        } else {
            acceptor = 
                new SocketFactoryAcceptorImpl(
                    orb,
                        initSvcPort,
                    LegacyServerSocketEndPointInfo.BOOT_NAMING,
                    SocketInfo.IIOP_CLEAR_TEXT);
        }
        orb.getCorbaTransportManager().registerAcceptor(acceptor);
    }

    protected ORB createORB(String[] args)
    {
        Properties props = System.getProperties();

        // For debugging.
        //props.put( ORBConstants.DEBUG_PROPERTY, "naming" ) ;
        //props.put( ORBConstants.DEBUG_PROPERTY, "transport,giop,naming" ) ;

        props.put( ORBConstants.ORB_SERVER_ID_PROPERTY, "1000" ) ;
        props.put( ORBConstants.PERSISTENT_SERVER_PORT_PROPERTY, 
            props.getProperty( ORBConstants.ORBD_PORT_PROPERTY,
                Integer.toString( 
                    ORBConstants.DEFAULT_ACTIVATION_PORT ) ) ) ;

        // See Bug 4396928 for more information about why we are initializing
        // the ORBClass to PIORB (now ORBImpl, but should check the bugid).
        props.put("org.omg.CORBA.ORBClass", 
            "com.sun.corba.ee.impl.orb.ORBImpl");

        return (ORB) ORB.init(args, props);
    }

    private void run(String[] args) 
    {
        try {
            // parse the args and try setting the values for these
            // properties
            processArgs(args);

            ORB orb = createORB(args);

            if (orb.orbdDebugFlag) 
                System.out.println( "ORBD begins initialization." ) ;

            boolean firstRun = createSystemDirs( ORBConstants.DEFAULT_DB_DIR );

            startActivationObjects(orb);

            if (firstRun) // orbd is being run the first time
                installOrbServers(getRepository(), getActivator());

            if (orb.orbdDebugFlag) {
                System.out.println( "ORBD is ready." ) ;
                System.out.println("ORBD serverid: " +
                        System.getProperty(ORBConstants.ORB_SERVER_ID_PROPERTY));
                System.out.println("activation dbdir: " +
                        System.getProperty(ORBConstants.DB_DIR_PROPERTY));
                System.out.println("activation port: " +
                        System.getProperty(ORBConstants.ORBD_PORT_PROPERTY));

                String pollingTime = System.getProperty(
                    ORBConstants.SERVER_POLLING_TIME);
                if( pollingTime == null ) {
                    pollingTime = Integer.toString( 
                        ORBConstants.DEFAULT_SERVER_POLLING_TIME );
                }
                System.out.println("activation Server Polling Time: " +
                        pollingTime + " milli-seconds ");

                String startupDelay = System.getProperty(
                    ORBConstants.SERVER_STARTUP_DELAY);
                if( startupDelay == null ) {
                    startupDelay = Integer.toString( 
                        ORBConstants.DEFAULT_SERVER_STARTUP_DELAY );
                }
                System.out.println("activation Server Startup Delay: " +
                        startupDelay + " milli-seconds " );
            }

            // The following two lines start the Persistent NameService
            NameServiceStartThread theThread =
                new NameServiceStartThread( orb, dbDir );
            theThread.start( );

            orb.run();
        } catch( org.omg.CORBA.COMM_FAILURE cex ) {
            System.out.println( CorbaResourceUtil.getText("orbd.commfailure"));
            System.out.println( cex );
            cex.printStackTrace();
        } catch( org.omg.CORBA.INTERNAL iex ) {
            System.out.println( CorbaResourceUtil.getText(
                "orbd.internalexception"));
            System.out.println( iex );
            iex.printStackTrace();
        } catch (Exception ex) {
            System.out.println(CorbaResourceUtil.getText(
                "orbd.usage", "orbd"));
            System.out.println( ex );
            ex.printStackTrace();
        }
    }

    private void processArgs(String[] args)
    {
        Properties props = System.getProperties();
        for (int i=0; i < args.length; i++) {
            if (args[i].equals("-port")) {
                if ((i+1) < args.length) {
                    props.put(ORBConstants.ORBD_PORT_PROPERTY, args[++i]);
                } else {
                    System.out.println(CorbaResourceUtil.getText(
                        "orbd.usage", "orbd"));
                }
            } else if (args[i].equals("-defaultdb")) {
                if ((i+1) < args.length) {
                    props.put(ORBConstants.DB_DIR_PROPERTY, args[++i]);
                } else {
                    System.out.println(CorbaResourceUtil.getText(
                        "orbd.usage", "orbd"));
                }
            } else if (args[i].equals("-serverid")) {
                if ((i+1) < args.length) {
                    props.put(ORBConstants.ORB_SERVER_ID_PROPERTY, args[++i]);
                } else {
                    System.out.println(CorbaResourceUtil.getText(
                        "orbd.usage", "orbd"));
                }
            } else if (args[i].equals("-serverPollingTime")) {
                if ((i+1) < args.length) {
                    props.put(ORBConstants.SERVER_POLLING_TIME, args[++i]);
                } else {
                    System.out.println(CorbaResourceUtil.getText(
                        "orbd.usage", "orbd"));
                }
            } else if (args[i].equals("-serverStartupDelay")) {
                if ((i+1) < args.length) {
                    props.put(ORBConstants.SERVER_STARTUP_DELAY, args[++i]);
                } else {
                    System.out.println(CorbaResourceUtil.getText(
                        "orbd.usage", "orbd"));
                }
            }
        }
    }

    /**
     * Ensure that the Db directory exists. If not, create the Db
     * and the log directory and return true. Otherwise return false.
     */
    protected boolean createSystemDirs(String defaultDbDir)
    {
        boolean dirCreated = false;
        Properties props = System.getProperties();
        String fileSep = props.getProperty("file.separator");

        // determine the ORB db directory
        dbDir = new File (props.getProperty( ORBConstants.DB_DIR_PROPERTY,
            props.getProperty("user.dir") + fileSep + defaultDbDir));

        // create the db and the logs directories
        dbDirName = dbDir.getAbsolutePath();
        props.put(ORBConstants.DB_DIR_PROPERTY, dbDirName);
        if (!dbDir.exists()) {
            dirCreated = dbDir.mkdir();
        }

        File logDir = new File (dbDir, ORBConstants.SERVER_LOG_DIR ) ;
        if (!logDir.exists()) logDir.mkdir();

        return dirCreated;
    }

    protected File dbDir;

    private String dbDirName;
    protected String getDbDirName()
    {
        return dbDirName;
    }

    protected void startActivationObjects(ORB orb) throws Exception
    {
        // create Initial Name Service object
        initializeBootNaming(orb);

        // create Repository object
        repository = new RepositoryImpl(orb, dbDir, orb.orbdDebugFlag );
        orb.register_initial_reference( ORBConstants.SERVER_REPOSITORY_NAME, repository );

        // create Locator and Activator objects
        ServerManagerImpl serverMgr =
            new ServerManagerImpl( orb, 
                                   orb.getCorbaTransportManager(),
                                   repository, 
                                   getDbDirName(), 
                                   orb.orbdDebugFlag );

        locator = LocatorHelper.narrow(serverMgr);
        orb.register_initial_reference( ORBConstants.SERVER_LOCATOR_NAME, locator );

        activator = ActivatorHelper.narrow(serverMgr);
        orb.register_initial_reference( ORBConstants.SERVER_ACTIVATOR_NAME, activator );

        // start Name Service
        new TransientNameService(orb, ORBConstants.TRANSIENT_NAME_SERVICE_NAME);
    }

    protected Locator locator;

    protected Activator activator;
    protected Activator getActivator()
    {
        return activator;
    }

    protected RepositoryImpl repository;
    protected RepositoryImpl getRepository()
    {
        return repository;
    }

    /** 
     * Go through the list of ORB Servers and initialize and start
     * them up.
     */
    protected void installOrbServers(RepositoryImpl repository, 
                                     Activator activator)
    {
        int serverId;
        String[] server;
        ServerDef serverDef;

        for (String[] orbServer : orbServers) {
            try {
                server = orbServer;
                serverDef = new ServerDef(server[1], server[2], server[3], server[4], server[5]);

                serverId = Integer.valueOf(orbServer[0]);

                repository.registerServer(serverDef, serverId);

                activator.activate(serverId);

            } catch (Exception ex) {  // ignore errors
            }
        }
    }

    public static void main(String[] args) {
        ORBD orbd = new ORBD();
        orbd.run(args);
    }

    /**
     * List of servers to be auto registered and started by the ORBd.
     * 
     * Each server entry is of the form {id, name, path, args, vmargs}.
     */
    private static String[][] orbServers = {
        {""}
    };
}
