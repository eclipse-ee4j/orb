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

package corba.security;

import java.security.ProtectionDomain ;
import java.security.Permission ;
import java.security.PermissionCollection ;
import java.security.Principal ;
import java.security.CodeSource ;
import java.security.cert.Certificate ;
import java.security.Policy ;

import java.util.Properties ;
import java.util.Enumeration ;

import java.io.PrintStream ;
import java.net.URL ;

public class Client 
{
    private PrintStream out ;
    private PrintStream err ;
    // private ORB orb ;

    public static void main(String args[])
    {
        System.out.println( "Starting Permission test" ) ;
        try{
            Properties props = new Properties( System.getProperties() ) ;
            props.put( "org.omg.CORBA.ORBClass", 
                "com.sun.corba.ee.impl.orb.ORBImpl" ) ;
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
        //this.orb = (ORB)ORB.init( args, props ) ;
        this.out = System.out ;
        this.err = System.err ;

        runTests() ;
    }

// *************************************************
// ***************   Utilities   *******************
// *************************************************

    private void error( String msg )
    {
        RuntimeException exc = new RuntimeException( msg ) ;
        throw exc ;
    }
    
    private void info( String msg )
    {
        out.println( msg ) ;
    }


// *************************************************
// ***************   TESTS   ***********************
// *************************************************
    private void dumpPermissions( PermissionCollection pc ) 
    {
        Enumeration perms = pc.elements() ;
        while (perms.hasMoreElements()) {
            Permission current = (Permission)perms.nextElement() ;
            info( "\t\t" + current ) ;
        }
    }

    private void dumpProtectionDomain( String msg, ProtectionDomain pd ) 
    {
        CodeSource cs = pd.getCodeSource() ;
        Policy policy = Policy.getPolicy() ;
        PermissionCollection pc = policy.getPermissions( pd ) ;

        info( msg ) ;
        info( "\tCodeSource: " + cs ) ;
        info( "\tPermissions:" ) ;
        dumpPermissions( pc ) ;
    }

    private void dumpProtectionDomainForClass( Class cls )
    {
        dumpProtectionDomain( "ProtectionDomain for " + cls, 
            cls.getProtectionDomain() ) ;
    }


    private void dumpProtectionDomainForPath( String path )
    {
        URL url = null ;

        try {
            url = new URL( "file:" + path  + "/-" ) ;
        } catch (Exception exc) {
            exc.printStackTrace( ) ;
            System.exit(1) ;
        }

        CodeSource cs = new CodeSource( url, (Certificate[])null ) ;
        Policy policy = Policy.getPolicy() ;
        PermissionCollection pc = policy.getPermissions( cs ) ;
        info( "Permissions for code loaded from path " + path ) ;
        info( "URL: " + url ) ;
        info( "\tPermissionCollection:" ) ;
        dumpPermissions( pc ) ;
        info( "" ) ;
    }

    private Class getClass( String name ) 
    {
        try {
            return Class.forName( name ) ;
        } catch (Exception exc) {
            return null ;
        }
    }

    private void dumpProperty( String name ) 
    {
        info( "Property " + name + " has value " + 
            System.getProperty( name ) ) ;
    }

    private void runTests()
    {
        info( "System.getSecurityManager() = " + System.getSecurityManager() ) ;
        dumpProperty( "com.sun.corba.ee.POA.ORBServerId" ) ;
        dumpProperty( "com.sun.corba.ee.ORBBase" ) ;
        dumpProperty( "java.security.policy" ) ;
        dumpProperty( "java.security.debug" ) ;
        dumpProperty( "java.security.manager" ) ;
        info( "" ) ;
        
        dumpProtectionDomainForPath(
            System.getProperty( "com.sun.corba.ee.ORBBase" ) + "/build" ) ;
        dumpProtectionDomainForPath(
            System.getProperty( "com.sun.corba.ee.ORBBase" ) + "/optional/build" ) ;
        dumpProtectionDomainForPath(
            System.getProperty( "com.sun.corba.ee.ORBBase" ) + "/test/build" ) ;

        dumpProtectionDomainForClass( 
            com.sun.corba.ee.spi.orb.ORB.class ) ;
        dumpProtectionDomainForClass( 
            com.sun.corba.ee.impl.orb.ORBImpl.class ) ;
        dumpProtectionDomainForClass( 
            org.omg.CORBA.ORB.class ) ;
        dumpProtectionDomainForClass(
            corba.security.Client.class ) ;

        Class cls = getClass( 
            "com.sun.corba.ee.spi.copyobject.OptimizedCopyobjectDefaults" ) ; 
        if (cls != null)
            dumpProtectionDomainForClass( cls ) ;

        cls = getClass(
            "com.sun.corba.ee.impl.copyobject.newreflect.ClassCopier" ) ;
        if (cls != null)
            dumpProtectionDomainForClass( cls ) ;
    }
}
