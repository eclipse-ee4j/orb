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

package corba.mixedorb;

import java.util.Properties ;
import java.util.Hashtable ;

import java.rmi.RemoteException ;

import javax.rmi.CORBA.Util ;
import javax.rmi.CORBA.Tie ;

import javax.naming.InitialContext ;

import com.sun.corba.ee.spi.JndiConstants;
import org.omg.CORBA.ORB ;

import org.testng.TestNG ;
import org.testng.Assert ;
import org.testng.annotations.Test ;
import org.testng.annotations.AfterTest ;
import org.testng.annotations.BeforeTest ;

import com.sun.corba.ee.spi.misc.ORBConstants ;

import com.sun.corba.ee.impl.naming.cosnaming.TransientNameService ;

import static corba.framework.PRO.* ;

public class Client {
    private ORB clientORB ;
    private ORB serverORB ;

    private static final String TEST_REF_NAME = "testref" ;
    private static final String SERVER_NAME = "fromServer" ;
    private static final String CLIENT_NAME = "fromClient" ;
    private static final String PORT_NUM = "3874" ;

    // Make sure this is unaffected by the rename.
    // The whole point of this test is to work with both the
    // se and ee ORBs.
    private String SE_BASE = "com.sun.corba." + "se." ;

    // This will be renamed, if we are renaming.
    private String RENAMED_BASE = "com.sun.corba.ee." ;

    private void setSystemProperties( String base ) {
        // Set the RMI-IIOP delegates and the singleton according to base.      
        System.setProperty( "javax.rmi.CORBA.UtilClass",
            base + "impl.javax.rmi.CORBA.Util" ) ;
        System.setProperty( "javax.rmi.CORBA.StubClass",
            base + "impl.javax.rmi.CORBA.StubDelegateImpl" ) ;
        System.setProperty( "javax.rmi.CORBA.PortableRemoteObjectClass",
            base + "impl.javax.rmi.PortableRemoteObject" ) ;

        // We will only use dynamic RMI-IIOP for this test.
        // And only for the EE ORB: the SE ORB uses a slightly different
        // property name.
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
    private ORB makeORB( String base, boolean isServer) {
        Properties props = new Properties() ;
        props.setProperty( "org.omg.CORBA.ORBClass", base + "impl.orb.ORBImpl" ) ;
        props.setProperty( ORBConstants.INITIAL_HOST_PROPERTY, "localhost" ) ;
        props.setProperty( ORBConstants.INITIAL_PORT_PROPERTY, PORT_NUM ) ;
        props.setProperty( ORBConstants.ALLOW_LOCAL_OPTIMIZATION, "true" ) ;

        if (isServer) {
            props.setProperty( ORBConstants.ORB_ID_PROPERTY, "serverORB" ) ;
            props.setProperty( ORBConstants.PERSISTENT_SERVER_PORT_PROPERTY, PORT_NUM ) ;
            props.setProperty( ORBConstants.SERVER_HOST_PROPERTY, "localhost" ) ;
            props.setProperty( ORBConstants.ORB_SERVER_ID_PROPERTY, "300" ) ;
        } else {
            props.setProperty( ORBConstants.ORB_ID_PROPERTY, "clientORB" ) ;
        }

        ORB orb = ORB.init( new String[0], props ) ;

        if (isServer) {
            new TransientNameService( 
                com.sun.corba.ee.spi.orb.ORB.class.cast(orb) ) ;
        }

        return orb ;
    }

    private Echo makeServant( String name ) {
        try {
            return new EchoImpl( name ) ;
        } catch (RemoteException rex) {
            Assert.fail( "Unexpected remote exception " + rex ) ;
            return null ; // never reached
        }
    }

    private void doServer( ORB orb ) {
        try {
            Hashtable env = new Hashtable() ;
            env.put( "java.naming.corba.orb", orb ) ;
            InitialContext ic = new InitialContext( env ) ;

            Echo servant = makeServant( SERVER_NAME ) ;
            Tie tie = Util.getTie( servant ) ;
            tie.orb( orb ) ;

            Echo ref = toStub( servant, Echo.class ) ;
            ic.bind( TEST_REF_NAME, ref ) ;
        } catch (Exception exc) {
            System.out.println( "Caught exception " + exc ) ;
            exc.printStackTrace() ;
            System.exit( 1 ) ;
        }
    }

    private void doClient( ORB orb ) {
        try {
            Hashtable env = new Hashtable() ;
            env.put( "java.naming.corba.orb", orb ) ;
            InitialContext ic = new InitialContext( env ) ;

            Echo servant = makeServant( CLIENT_NAME ) ;
            Tie tie = Util.getTie( servant ) ;
            tie.orb( orb ) ;

            Echo ref = toStub( servant, Echo.class ) ;

            Echo sref = narrow( ic.lookup( TEST_REF_NAME ), Echo.class ) ;
            Assert.assertEquals( sref.name(), SERVER_NAME ) ;

            Echo rref = sref.say( ref ) ;
            Assert.assertEquals( rref.name(), CLIENT_NAME ) ;

            Echo r2ref = rref.say( sref ) ;
            Assert.assertEquals( r2ref.name(), SERVER_NAME ) ;

            Echo ref2 = ref.say( ref ) ;
            Assert.assertEquals( ref2.name(), ref.name() ) ;
        } catch (Exception exc) {
            System.out.println( "Caught exception " + exc ) ;
            exc.printStackTrace() ;
            System.exit( 1 ) ;
        }
    }

    @BeforeTest
    public void setUp() {
        setSystemProperties( RENAMED_BASE ) ;
        serverORB = makeORB( RENAMED_BASE, true ) ;
        // This should be SE_BASE 
        clientORB = makeORB( SE_BASE, false ) ;

        try {
            serverORB.resolve_initial_references( "NameService" ) ;

            // Make sure that the FVD codebase IOR is not shared between
            // multiple ORBs in the value handler, because that causes
            // errors in the JDK ORB.
            com.sun.corba.ee.spi.orb.ORB orb = (com.sun.corba.ee.spi.orb.ORB)serverORB ;
            orb.getFVDCodeBaseIOR() ;

            clientORB.resolve_initial_references( "NameService" ) ;
        } catch (Exception exc) {
            throw new RuntimeException( exc ) ;
        }
    }

    @Test()
    public void run() {
        doServer( serverORB ) ;
        doClient( clientORB ) ;
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
        com.sun.corba.ee.impl.javax.rmi.CORBA.Util.getInstance().
            unregisterTargetsForORB( clientORB ) ;
        clientORB.destroy() ;

        // The Server ORB does clean up correctly.
        serverORB.destroy() ;
    }

    public static void main( String[] args ) {
        TestNG tng = new TestNG() ;

        Class[] tngClasses = new Class[] {
            Client.class 
        } ;

        tng.setTestClasses( tngClasses ) ;

        tng.run() ;

        // Make sure we report success/failure to the wrapper.
        System.exit( tng.hasFailure() ? 1 : 0 ) ;
    }
}
