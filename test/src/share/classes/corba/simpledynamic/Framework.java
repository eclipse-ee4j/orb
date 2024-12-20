/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package corba.simpledynamic;

import java.util.Properties ;
import java.util.Hashtable ;

import java.rmi.Remote ;

import javax.rmi.CORBA.Util ;
import javax.rmi.CORBA.Tie ;

import javax.naming.InitialContext ;
import javax.naming.NamingException ;

import com.sun.corba.ee.spi.JndiConstants;
import org.testng.TestNG ;
import org.testng.annotations.AfterTest ;
import org.testng.annotations.BeforeTest ;

import com.sun.corba.ee.spi.orb.ORB ;

import com.sun.corba.ee.spi.misc.ORBConstants ;

import com.sun.corba.ee.impl.naming.cosnaming.TransientNameService ;

import static corba.framework.PRO.* ;

public abstract class Framework {
    private ORB clientORB ;
    private ORB serverORB ;
    private InitialContext clientIC ;
    private InitialContext serverIC ;

    protected static final String PORT_NUM = "46132" ;

    private String BASE = "com.sun.corba.ee." ;

    private void setSystemProperties() {
        System.setProperty( "javax.rmi.CORBA.UtilClass",
            BASE + "impl.javax.rmi.CORBA.Util" ) ;
        System.setProperty( "javax.rmi.CORBA.StubClass",
            BASE + "impl.javax.rmi.CORBA.StubDelegateImpl" ) ;
        System.setProperty( "javax.rmi.CORBA.PortableRemoteObjectClass",
            BASE + "impl.javax.rmi.PortableRemoteObject" ) ;

        // We will only use dynamic RMI-IIOP for this test.
        System.out.println( "Setting property " + ORBConstants.USE_DYNAMIC_STUB_PROPERTY 
            + " to true" ) ;
        System.setProperty( ORBConstants.USE_DYNAMIC_STUB_PROPERTY, "true" ) ;

        // Use the J2SE ic provider
        System.setProperty( "java.naming.factory.initial", 
            JndiConstants.COSNAMING_CONTEXT_FACTORY ) ;
    }

    // We need to set up the client and server ORBs, and start a transient
    // name server that runs on the server ORB, with the client ORB referring
    // to the server ORB's name service.
    protected ORB makeORB( boolean isServer, Properties extra ) {
        Properties props = new Properties( extra ) ;
        props.setProperty( "org.omg.CORBA.ORBClass", BASE + "impl.orb.ORBImpl" ) ;
        props.setProperty( ORBConstants.INITIAL_HOST_PROPERTY, "localhost" ) ;
        props.setProperty( ORBConstants.INITIAL_PORT_PROPERTY, PORT_NUM ) ;
        props.setProperty( ORBConstants.ALLOW_LOCAL_OPTIMIZATION, "true" ) ;

        if (isServer) {
            props.setProperty( ORBConstants.ORB_ID_PROPERTY, "serverORB" ) ;
            props.setProperty( ORBConstants.SERVER_HOST_PROPERTY, "localhost" ) ;
            props.setProperty( ORBConstants.ORB_SERVER_ID_PROPERTY, "300" ) ;
            setServerPort( props ) ;
        } else {
            props.setProperty( ORBConstants.ORB_ID_PROPERTY, "clientORB" ) ;
        }

        ORB orb = (ORB)ORB.init( new String[0], props ) ;

        updateORB( orb, isServer ) ;

        if (isServer) {
            new TransientNameService( 
                com.sun.corba.ee.spi.orb.ORB.class.cast(orb) ) ;
        }

        return orb ;
    }

    // This is the default setup for the server ORB's listening port.
    // This can be overridden if necessary.
    protected void setServerPort( Properties props ) {
        props.setProperty( ORBConstants.PERSISTENT_SERVER_PORT_PROPERTY, PORT_NUM ) ;
    }

    // Can be overridden if necessary to allow the ORB to be further
    // configured before it is used.
    protected void updateORB( ORB orb, boolean isServer ) {
    }

    private InitialContext makeIC( ORB orb ) throws NamingException {
        Hashtable env = new Hashtable() ;
        env.put( "java.naming.corba.orb", orb ) ;
        InitialContext ic = new InitialContext( env ) ;
        return ic ;
    }

    protected ORB getClientORB() {
        return clientORB ;
    }

    protected ORB getServerORB() {
        return serverORB ;
    }

    protected InitialContext getClientIC() {
        return clientIC ;
    }

    protected InitialContext getServerIC() {
        return serverIC ;
    }

    protected Properties extraServerProperties() {
        return new Properties() ;
    }

    protected Properties extraClientProperties() {
        return new Properties() ;
    }
    
    /** Connect a servant of type cls to the orb.  
    */
    protected <T extends Remote> void connectServant( T servant, ORB orb ) {

        try {
            Tie tie = Util.getTie( servant ) ;
            tie.orb( getServerORB() ) ;
        } catch (Exception exc) {
            throw new RuntimeException( exc ) ;
        }
    }

    /** Connect a servant to the server ORB, and register it with the
     * server InitialContext under name.
     */
    protected <T extends Remote> void bindServant( T servant, Class<T> cls, 
        String name ) {

        connectServant( servant, getServerORB() ) ;

        try {
            T stub = toStub( servant, cls ) ;
            getServerIC().bind( name, stub ) ;
        } catch (Exception exc) {
            throw new RuntimeException( exc ) ;
        }
    }

    protected <T extends Remote> T findStub( Class<T> cls, String name ) {
        try {
            return narrow( getClientIC().lookup( name ), cls ) ;
        } catch (Exception exc) {
            throw new RuntimeException( exc ) ;
        }
    }

    @BeforeTest
    public void setUp() {
        setSystemProperties() ;
        serverORB = makeORB( true, extraServerProperties() ) ;
        clientORB = makeORB( false, extraClientProperties() ) ;

        try {
            serverORB.resolve_initial_references( "NameService" ) ;

            // Make sure that the FVD codebase IOR is not shared between
            // multiple ORBs in the value handler, because that causes
            // errors in the JDK ORB.
            // com.sun.corba.ee.spi.orb.ORB orb = (com.sun.corba.ee.spi.orb.ORB)serverORB ;
            // orb.getFVDCodeBaseIOR() ;

            clientORB.resolve_initial_references( "NameService" ) ;

            serverIC = makeIC( serverORB ) ;
            clientIC = makeIC( clientORB ) ;
        } catch (Exception exc) {
            throw new RuntimeException( exc ) ;
        }
    }

    @AfterTest
    public void tearDown() {
        // The Client ORB does not correctly clean up its
        // exported targets: it tries to go to the SE
        // RMI-IIOP implementation, which is not even
        // instantiated here.  So clean up manually.
        //
        // Fixing this requires changes in the ORB:
        // basically it should be the TOA's job to keep
        // track of connected objrefs and clean up the
        // information in RMI-IIOP.  This would affect
        // both the se and ee ORBs, and require a patch
        // to JSE 5.
        clientORB.shutdown( true ) ;
        // com.sun.corba.ee.impl.javax.rmi.CORBA.Util.getInstance().
        //    unregisterTargetsForORB( clientORB ) ;
        clientORB.destroy() ;

        // The Server ORB does clean up correctly.
        serverORB.destroy() ;
    }

    public static void run( String outputDirectory, Class[] tngClasses ) {
        TestNG tng = new TestNG() ;
        tng.setOutputDirectory( outputDirectory ) ;
        tng.setTestClasses( tngClasses ) ;
        tng.run() ;

        // Make sure we report success/failure to the wrapper.
        System.exit( tng.hasFailure() ? 1 : 0 ) ;
    }
}
