/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.simpledynamic;

import java.net.Socket;
import java.util.Properties;
import java.rmi.RemoteException ;

import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.spi.transport.Acceptor;
import com.sun.corba.ee.spi.transport.TransportDefault;
import org.glassfish.pfl.basic.func.UnaryVoidFunction;

import org.testng.Assert ;
import org.testng.annotations.Test ;

public class NewAcceptorClient extends Framework {
    private static final int SERVER_PORT = Integer.parseInt( PORT_NUM ) ;

    // Make sure that the ORB does not create any default acceptors.
    @Override
    protected void setServerPort( Properties props ) {
        super.setServerPort( props ) ;
        props.setProperty( ORBConstants.NO_DEFAULT_ACCEPTORS, "true" ) ;
        props.setProperty( ORBConstants.ORB_SERVER_ID_PROPERTY, "1" ) ;
    }

    // Can be overridden if necessary to allow the ORB to be further
    // configured before it is used.
    @Override
    protected void updateORB( ORB orb, boolean isServer ) {
        final Acceptor listener = TransportDefault.makeLazyCorbaAcceptor(orb,
            SERVER_PORT, "localhost", "IIOP_CLEAR_TEXT" ) ;

        UnaryVoidFunction<Socket> func = new UnaryVoidFunction<Socket>() {
            public void evaluate( Socket sock ) {
                msg( "Processing message on socket " + sock ) ;
                listener.processSocket( sock ) ;
            }
        } ;

        final Acceptor processor = TransportDefault.makeAcceptOnlyCorbaAcceptor(
            orb, SERVER_PORT, "localhost", "IIOP_CLEAR_TEXT", func ) ;

        orb.getTransportManager().registerAcceptor( listener ) ;
        orb.getTransportManager().registerAcceptor( processor ) ;
        // Called for side-effect of initializing IORTemplate and transport
        orb.getFVDCodeBaseIOR() ;
    }

    private Echo makeServant( String name ) {
        try {
            return new EchoImpl( name ) ;
        } catch (RemoteException rex) {
            Assert.fail( "Unexpected remote exception " + rex ) ;
            return null ; // never reached
        }
    }

    private void msg( String msg ) {
        System.out.println( "NewAcceptorClient: " + msg ) ;
    }

    private static final int ITERATIONS = 10 ;

    @Test
    public void testNewAcceptor() throws RemoteException {
        final Echo servant = makeServant( "acceptorTest" ) ;
        bindServant( servant, Echo.class, "AcceptorTest" ) ;
        Echo clientRef = findStub( Echo.class, "AcceptorTest" ) ;

        String data = "This is my test string" ;

        for (int ctr=0; ctr<ITERATIONS; ctr++) {
            Object result = clientRef.echo( data ) ;
            Assert.assertTrue( result instanceof String );
            String strres = (String)result ;
            Assert.assertEquals(strres, data);
        }
    }

    public static void main( String[] args ) {
        Class[] classes = { NewAcceptorClient.class } ;
        Framework.run( "gen/corba/simpledynamic/test-output", classes ) ;
    }
}
