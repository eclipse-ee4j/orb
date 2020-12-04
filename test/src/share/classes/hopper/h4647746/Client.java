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

package hopper.h4647746;

import java.io.PrintStream ;

import java.util.Properties ;
import java.util.LinkedList ;
import java.util.Iterator ;
import java.util.StringTokenizer ;
import java.util.Arrays ;

import org.omg.PortableServer.POA ;
import org.omg.PortableServer.Servant ;
import org.omg.PortableServer.RequestProcessingPolicyValue ;

import org.omg.PortableServer.POAPackage.WrongPolicy ;
import org.omg.PortableServer.POAPackage.InvalidPolicy ;
import org.omg.PortableServer.POAPackage.AdapterAlreadyExists ;

import org.omg.PortableServer.portable.Delegate ;

import org.omg.CORBA.portable.ObjectImpl ;

import org.omg.CORBA.ORB ;
import org.omg.CORBA.Policy ;
import org.omg.CORBA.BAD_PARAM ;
import org.omg.CORBA.INTERNAL ;
import org.omg.CORBA.OctetSeqHolder ;

import org.omg.CORBA.ORBPackage.InvalidName ;

import org.omg.CORBA_2_3.portable.OutputStream ;
import org.omg.CORBA_2_3.portable.InputStream ;

public class Client 
{
    private PrintStream out ;
    private PrintStream err ;
    private ORB orb ;

    public static void main(String args[])
    {
        System.out.println( "Starting POA Applet initialization test" ) ;
        try{
            Properties props = System.getProperties() ;
            new Client( props, args, System.out, System.err ) ;
        } catch (Exception e) {
            System.out.println("ERROR : " + e) ;
            e.printStackTrace(System.out);
            System.exit (1);
        }
    }

    public Client( Properties props, String args[], PrintStream out,
        PrintStream err )
    {
        this.orb = ORB.init( args, props ) ;
        this.out = System.out ;
        this.err = System.err ;

        runTests() ;
    }

// *************************************************
// ***************   Utilities   *******************
// *************************************************

    private void error( String msg )
    {
        throw new RuntimeException( msg ) ;
    }
    
    private void info( String msg )
    {
        out.println( msg ) ;
    }

 // *************************************************
// ***************   TESTS   ***********************
// *************************************************

    private void runTests()
    {
        java.applet.Applet applet = new java.applet.Applet() ;
        java.applet.AppletStub dummy = new java.applet.AppletStub() {
            public void appletResize( int width, int height ) 
            {
            }

            public java.applet.AppletContext getAppletContext() 
            {
                return null ;
            }

            public java.net.URL getCodeBase()
            { 
                return null ;
            }

            public java.net.URL getDocumentBase()
            {
                return null ;
            }

            public String getParameter( String name )
            {
                return null ;
            }

            public boolean isActive()
            {
                return false ;
            }
        } ;
        applet.setStub( dummy ) ;

        ORB orb = ORB.init( applet, null ) ;    
        POA rpoa = null ;
        try {
            rpoa = (POA)(orb.resolve_initial_references( "RootPOA" )) ;
        } catch (InvalidName err) {
            error( err.toString() ) ;
        }

        Policy[] policies = { rpoa.create_request_processing_policy(
            RequestProcessingPolicyValue.USE_DEFAULT_SERVANT ) } ;

        POA cpoa = null ;

        try {
            cpoa = rpoa.create_POA( "Child", rpoa.the_POAManager(), 
                policies ) ;    
        } catch (InvalidPolicy err) {
            error( err.toString() ) ;
        } catch (AdapterAlreadyExists err) {
            error( err.toString() ) ;
        }

        Servant servant = new Servant() {
            public String[] _all_interfaces( POA poa, byte[] objectId )
            {
                return null ;
            }
        } ;

        // This should set the delegate on servant
        try {
            cpoa.set_servant( servant ) ;
        } catch (WrongPolicy err) {
            error( err.toString() ) ;
        }

        // Without the fix for bug 4647746, this fails because 
        // the delegate was not initialized in ORB.init in Applet
        // mode.
        Delegate delegate = servant._get_delegate() ;
    }
}
