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

package pi.serverrequestinfo;

import org.omg.CORBA.*;
import org.omg.PortableInterceptor.*;
import org.omg.PortableServer.*;

import java.util.*;
import java.io.*;

import ServerRequestInfo.*;

/**
 * Servant implementation, shared by ServerRequestDispatcher and POA versions of
 * the servant.  
 */
class DSIImpl {
    // The object to delegate all calls to:
    helloDelegate delegate;

    // The orb to use to make DSI-related calls on
    private ORB orb;

    public DSIImpl( ORB orb, PrintStream out, String symbol,
        helloDelegate.ClientCallback clientCallback ) 
    {
        super();
        this.orb = orb;
        this.delegate = new helloDelegate( out, symbol, clientCallback );
    }

    public void invoke( ServerRequest r ) {
        String opName = r.op_name();
        java.lang.Object result = null;

        if( opName.equals( "sayHello" ) ) {
            sayHello( r );
        }
        else if( opName.equals( "sayOneway" ) ) {
            sayOneway( r );
        }
        else if( opName.equals( "saySystemException" ) ) {
            saySystemException( r );
        }
        else if( opName.equals( "sayUserException" ) ) {
            sayUserException( r );
        }
        else if( opName.equals( "syncWithServer" ) ) {
            syncWithServer( r );
        }
        else if( opName.equals( "sayInvokeAgain" ) ) {
            sayInvokeAgain( r );
        }
    }

    private void sayHello( ServerRequest r ) {
        NVList list = orb.create_list( 0 );
        r.arguments( list );

        String answer = delegate.sayHello();

        // Return result:
        Any result = orb.create_any();
        result.insert_string( answer );
        r.result( result );
    }

    private void sayOneway( ServerRequest r ) {
        NVList list = orb.create_list( 0 );
        r.arguments( list );

        delegate.sayOneway();

        // Return void result:
        Any ret = orb.create_any();
        ret.type( orb.get_primitive_tc( TCKind.tk_void ) );
        r.set_result( ret );
    }
    
    private void saySystemException( ServerRequest r ) {
        // Must call arguments first.  Bug?
        NVList list = orb.create_list( 0 );
        r.arguments( list );

        delegate.saySystemException();
    }

    private void sayUserException( ServerRequest r ) {
        try {
            delegate.sayUserException();
        }
        catch( ExampleException e ) {
            Any any = orb.create_any();
            ExampleExceptionHelper.insert( any, e );
            r.set_exception( any );
        }
    }
    
    private void syncWithServer( ServerRequest r ) {
        // Decode exceptionRaised parameter
        NVList nvlist = orb.create_list( 0 );

        Any a1 = orb.create_any();
        a1.type( orb.get_primitive_tc( TCKind.tk_boolean ) );
        nvlist.add_value( "exceptionRaised", a1, ARG_IN.value );
        r.arguments( nvlist );

        boolean exceptionRaised = a1.extract_boolean();

        // Make call to delegate:
        String answer = delegate.syncWithServer( exceptionRaised );

        // Return result:
        Any result = orb.create_any();
        result.insert_string( answer );
        r.result( result );
    }

    private void sayInvokeAgain( ServerRequest r ) {
        NVList list = orb.create_list( 0 );
        Any a1 = orb.create_any();
        a1.type( orb.get_primitive_tc( TCKind.tk_long ) );
        list.add_value( "n", a1, ARG_IN.value );

        r.arguments( list );

        int n = a1.extract_long();
        delegate.sayInvokeAgain( n );

        // Return void result:
        Any ret = orb.create_any();
        ret.type( orb.get_primitive_tc( TCKind.tk_void ) );
        r.set_result( ret );
    }
}

