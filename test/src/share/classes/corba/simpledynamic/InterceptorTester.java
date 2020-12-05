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

package corba.simpledynamic;

import org.omg.PortableInterceptor.ORBInitializer ;
import org.omg.PortableInterceptor.ClientRequestInterceptor ;
import org.omg.PortableInterceptor.ClientRequestInfo ;
import org.omg.PortableInterceptor.ORBInitInfo ;
import org.omg.PortableInterceptor.ForwardRequest ;

import org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName ;

import org.omg.CORBA.LocalObject ;
import org.omg.CORBA.SystemException ;
import org.omg.CORBA.INTERNAL ;
import org.omg.CORBA.COMM_FAILURE ;
import org.omg.CORBA.ORB ;
import org.omg.CORBA.Any ;

import com.sun.corba.ee.impl.misc.ORBUtility ;

public class InterceptorTester extends LocalObject implements
    ORBInitializer, ClientRequestInterceptor {

    public static InterceptorTester theTester = null ;
    public static boolean verbose = false ;

    private int errors = 0 ;
    private boolean exceptionExpected = false ;

    public InterceptorTester() {
        theTester = this ;
    }

    public void clear() {
        errors = 0 ;
        exceptionExpected = false ;
    }
    
    public int getErrors() {
        return errors ;
    }

    public void setExceptionExpected() {
        exceptionExpected = true ;
    }
    
    private void msg( String msg ) {
        if (verbose) {
            System.out.println( "+++InterceptorTester: " + msg ) ;
        }
    }

    private void error( String msg ) {
        msg( "ERROR: " + msg ) ;
        errors++ ;
    }

    public void pre_init( ORBInitInfo info ) {
    }

    public void post_init( ORBInitInfo info ) {
        try {
            info.add_client_request_interceptor( this ) ;
        } catch (DuplicateName exc) {
            INTERNAL internal = new INTERNAL() ;
            internal.initCause( exc ) ;
            throw internal ;
        }
    }

    public String name() {
        return "ClientInterceptor" ;
    }

    public void destroy() {
    }

    public void send_request( ClientRequestInfo ri ) throws ForwardRequest {
        msg( "send_request called" ) ;
    }

    public void send_poll( ClientRequestInfo ri ) {
        error( "send_poll should not be called" ) ;
    }

    public void receive_reply( ClientRequestInfo ri ) {
        if (exceptionExpected) {
            error( "normal completion when exception expected!" ) ;
        } else {
            msg( "normal completion" ) ;
        }
    }

    public void receive_exception( ClientRequestInfo ri ) throws ForwardRequest {
        if (!exceptionExpected) {
            error( "exception when normal completion expected!" ) ;
        } else {
            msg( "expected exception" ) ;
        }

        Any exception = ri.received_exception() ;
        SystemException sysex = ORBUtility.extractSystemException( exception ) ;

        if (!(sysex instanceof COMM_FAILURE)) {
            error( "Expected COMM_FAILURE, got " + sysex ) ;
        } else {
            msg( "expected COMM_FAILURE" ) ;
        }

        sysex.printStackTrace() ;
    }

    public void receive_other( ClientRequestInfo ri ) throws ForwardRequest {
        error( "receive_other should not be called" ) ;
    }
}
