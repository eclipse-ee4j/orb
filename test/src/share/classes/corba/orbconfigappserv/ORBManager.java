/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

//
// Created       : 2003 Apr 15 (Tue) 15:11:52 by Harold Carr.
// Last Modified : 2003 Apr 15 (Tue) 17:22:07 by Harold Carr.
//

package corba.orbconfigappserv;

import org.omg.CORBA.ORB;

import java.util.*;
import java.io.*;

import javax.rmi.CORBA.Tie;
import javax.rmi.PortableRemoteObject;

import javax.naming.InitialContext;
import javax.naming.Context;

import com.sun.corba.ee.spi.misc.ORBConstants;


public class ORBManager 
{
    private static final String poaOrbClass = 
        "com.sun.corba.ee.impl.orb.ORBImpl";
    private static final String poaOrbSingletonClass =
        "com.sun.corba.ee.impl.orb.ORBSingleton";
    private static final String peorbConfigClass = 
        "corba.orbconfigappserv.UserORBConfiguratorImpl";

    public static final String OMG_ORB_CLASS_PROPERTY =
            "org.omg.CORBA.ORBClass";
    public static final String OMG_ORB_SINGLETON_CLASS_PROPERTY =
            "org.omg.CORBA.ORBSingletonClass";
    public static final String SUN_PEORB_CONFIGURATOR_CLASS_PROPERTY =
            "com.sun.corba.ee.ORBUserConfigurators";
    public static final String ORB_UTIL_CLASS_PROPERTY =
            "javax.rmi.CORBA.UtilClass";
    public static final String JNDI_PROVIDER_URL_PROPERTY = 
            "java.naming.provider.url";
    public static final String JNDI_CORBA_ORB_PROPERTY = 
            "java.naming.corba.orb";
       
    public static final String OMG_ORB_INIT_HOST_PROPERTY = 
            "org.omg.CORBA.ORBInitialHost";
    public static final String OMG_ORB_INIT_PORT_PROPERTY = 
            "org.omg.CORBA.ORBInitialPort";
    public static final String SUN_ORB_SERVER_HOST_PROPERTY =
            "com.sun.corba.ee.ORBServerHost";
    public static final String SUN_ORB_SERVER_PORT_PROPERTY =
            "com.sun.corba.ee.ORBServerPort";

    public static final String RMIIIOP_STUB_DELEGATE_CLASS_PROPERTY =
            "javax.rmi.CORBA.StubClass";
    public static final String RMIIIOP_PRO_DELEGATE_CLASS_PROPERTY =
            "javax.rmi.CORBA.PortableRemoteObjectClass";
    
    public static final String SUN_ORB_SOCKET_FACTORY_CLASS_PROPERTY =
            "com.sun.corba.ee.connection.ORBSocketFactoryClass";
    
    private static final String J2EE_INITIALIZER = 
            "corba.orbconfigappserv.ORBInitializerImpl";
    private static final String PI_ORB_INITIALIZER_CLASS_PREFIX =
            "org.omg.PortableInterceptor.ORBInitializerClass.";

    public static final String ORB_LISTEN_SOCKET_PROPERTY =
            "com.sun.corba.ee.connection.ORBListenSocket";
    private static final String IIOP_CLEAR_TEXT_CONNECTION =
            "IIOP_CLEAR_TEXT";

    private static final String DEFAULT_ORB_INIT_HOST = "Testing";
    private static final String DEFAULT_ORB_INIT_PORT = "4500";

    private static org.omg.CORBA.ORB orb = null;  // the singleton ORB
    private static int orbInitialPort = -1;

    public static void main(String[] av)
    {
        testInit("First init:", av, null);

        Properties props = new Properties();
        props.put(UserORBConfiguratorImpl.propertyName,
                  "corba.orbconfigappserver.ORBManager");
        testInit("Second init:", av, props);

        System.out.println();
        System.out.println("DONE");
    }

    public static void testInit(String msg, String[] av, Properties props)
    {
        System.out.println();
        System.out.println(msg);
        System.out.println();
        init(av, null);
        System.out.println(
         ((com.sun.corba.ee.spi.orb.ORB)orb).getORBData().getORBInitialHost());
    }

    /**
     * Initializes the single ORB in this process. 
     * This must be called before calling other methods within this class.
     */
    public static synchronized void init(String[] args, Properties props)  {
        /*
        if (orb != null) // There should be only ONE ORB.
            return;
        */

        orb = createORB(args, props);
    }

    public static org.omg.CORBA.ORB getORB() {
        if ( orb == null ) 
            init(null, null);
        return orb;
    }

    /**
     * This is called by the PEORBConfigurator only to get past 
     * JTS initialization. No one else should have to call this
     */
    public static synchronized void setORB(org.omg.CORBA.ORB theORB) {
        if ( orb == null ) 
            orb = theORB;
    }


    public static int getORBInitialPort() {
        if ( orbInitialPort == -1 )
            checkORBInitialPort(new Properties());
        return orbInitialPort;
    }

    private static synchronized ORB createORB(String[] args, Properties props) 
    {
        ORB neworb = null;
        Properties orbInitProperties = new Properties();
          
        System.setProperty(OMG_ORB_CLASS_PROPERTY, poaOrbClass);
        System.setProperty(OMG_ORB_SINGLETON_CLASS_PROPERTY,
                           poaOrbSingletonClass);
        System.setProperty(SUN_PEORB_CONFIGURATOR_CLASS_PROPERTY 
                           + "." + peorbConfigClass,"");
    
        System.setProperty(SUN_ORB_SOCKET_FACTORY_CLASS_PROPERTY,
                           "corba.orbconfigappserv.SocketFactoryImpl");

        if (System.getProperty(ORB_UTIL_CLASS_PROPERTY) == null) {
            System.setProperty(ORB_UTIL_CLASS_PROPERTY,
                          "com.sun.corba.ee.impl.javax.rmi.CORBA.Util");
        }

        try {
            if (props != null) {
                orbInitProperties.putAll(props);
            }

            orbInitProperties.put(
                PI_ORB_INITIALIZER_CLASS_PREFIX + J2EE_INITIALIZER, "");

            orbInitProperties.put("com.sun.corba.ee.ORBAllowLocalOptimization", 
                                  "true" ) ;

            orbInitProperties.put(OMG_ORB_CLASS_PROPERTY, poaOrbClass);
            orbInitProperties.put(OMG_ORB_SINGLETON_CLASS_PROPERTY, 
                                  poaOrbSingletonClass);
            
            String initialPort = checkORBInitialPort(orbInitProperties);
            String orbInitialHost = checkORBInitialHost(orbInitProperties);
            // Add -ORBInitRef for INS to work
            args = addORBInitRef(args, orbInitialHost, initialPort); 
            // START OF IASRI 4627397, 4658320, 4669565, 4676091, 4738349
            checkAdditionalORBListeners(orbInitProperties);

            
            neworb = ORB.init(args, orbInitProperties);
        }
        catch ( Exception ex ) {
            System.out.println(ex);
        }

        return neworb;
    }
    
    private static String checkORBInitialHost(Properties props) 
    {
        // Host setting in system properties always takes precedence.
        String orbInitialHost = System.getProperty(OMG_ORB_INIT_HOST_PROPERTY);
        if ( orbInitialHost == null )
            orbInitialHost = props.getProperty(OMG_ORB_INIT_HOST_PROPERTY);
        System.out.println("Found orb initial host: " + orbInitialHost);
        orbInitialHost = DEFAULT_ORB_INIT_HOST;
        System.setProperty(OMG_ORB_INIT_HOST_PROPERTY, orbInitialHost);
        System.out.println("Overwriting orb initial host to " + orbInitialHost);
        return orbInitialHost;
    }

    private static String checkORBInitialPort(Properties props) 
    {
        // Port setting in system properties always takes precedence.
        String initialPort = System.getProperty(OMG_ORB_INIT_PORT_PROPERTY);
        if ( initialPort == null )
            initialPort = props.getProperty(OMG_ORB_INIT_PORT_PROPERTY);
        if( initialPort == null )
            initialPort = DEFAULT_ORB_INIT_PORT;
        System.setProperty(OMG_ORB_INIT_PORT_PROPERTY, initialPort);
        orbInitialPort = new Integer(initialPort).intValue();
        System.out.println("Setting orb initial port to " + initialPort);
        return initialPort;
    }

    private static void checkAdditionalORBListeners(Properties props) {
        StringBuffer listenSockets = new StringBuffer("");
        listenSockets.append(
            (listenSockets.length()>0 ? "," : "")
            + "FOO" + ":" + 2000
            );
        // Both ways work.
        //props.setProperty(ORB_LISTEN_SOCKET_PROPERTY, listenSockets.toString());
        System.setProperty(ORB_LISTEN_SOCKET_PROPERTY, listenSockets.toString());
    }

    private static String[] addORBInitRef(String[] args, String orbInitialHost, 
                           String initialPort)
    {
        // Add -ORBInitRef NameService=....
        // This ensures that INS will be used to talk with the NameService.
        String[] newArgs;
        int i=0;
        if ( args == null ) {
            newArgs = new String[2];
        }
        else {
            newArgs = new String[args.length + 2];
            for ( ; i<args.length; i++ )
                newArgs[i] = args[i];
        }
        newArgs[i++] = "-ORBInitRef";
        newArgs[i++] = "NameService=corbaloc:iiop:1.2@" 
                        + orbInitialHost + ":" + initialPort
                        + "/NameService";
        return newArgs;
    }

    public static Tie exportObject(java.rmi.Remote remote) 
        throws java.rmi.RemoteException
    {
        PortableRemoteObject.exportObject(remote);
        Tie servantsTie = javax.rmi.CORBA.Util.getTie(remote);
        
        // Note: at this point the Tie doesnt have a delegate inside it,
        // so it is not really "exported".
        // The following call does orb.connect() which is the real exporting
        servantsTie.orb(getORB());
        return servantsTie;
    }
        
    private static void checkDelegateProps()
    {
        java.security.AccessController.doPrivileged(new java.security.PrivilegedAction() {
            public java.lang.Object run() {
        
                String utilDelegate = System.getProperty(ORB_UTIL_CLASS_PROPERTY);
                if ( utilDelegate == null || utilDelegate.equals("") ) {
                    // Set up system properties for RMI-IIOP delegates
                    System.setProperty(RMIIIOP_STUB_DELEGATE_CLASS_PROPERTY, 
                            "com.sun.corba.ee.impl.javax.rmi.CORBA.StubDelegateImpl");
                    System.setProperty(RMIIIOP_PRO_DELEGATE_CLASS_PROPERTY, 
                            "com.sun.corba.ee.impl.javax.rmi.PortableRemoteObject");
                }

                return null;
            }
        });
    }
}

// End of file.
