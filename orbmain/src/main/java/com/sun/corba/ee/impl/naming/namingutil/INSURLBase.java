/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.naming.namingutil;

import com.sun.corba.ee.spi.logging.OMGSystemException ;

import com.sun.corba.ee.spi.orb.ORB ;

/** The corbaloc: URL definitions from the -ORBInitDef and -ORBDefaultInitDef's
 *  will be stored in this object. This object is capable of storing multiple
 *  Host profiles as defined in the CorbaLoc grammer.
 *
 *  @author  Hemanth
 */
public abstract class INSURLBase implements INSURL {
    private static OMGSystemException wrapper =
        OMGSystemException.self ;

    // If rirFlag is set to true that means internal
    // boot strapping technique will be used. If set to
    // false then the EndpointInfo will be used to create the
    // Service Object reference.
    protected boolean rirFlag = false ;
    protected java.util.ArrayList theEndpointInfo = null ;
    protected String theKeyString = "NameService" ;
    protected String theStringifiedName = null ;

    /**
     *  A Utility method to throw BAD_PARAM exception to signal malformed
     *  INS URL.
     */
    protected void badAddress( String name )
    {
        throw wrapper.soBadAddress( name ) ;
    }

    protected void badAddress( java.lang.Throwable e, String name )
    {
        throw wrapper.soBadAddress( e, name ) ;
    }

    public boolean getRIRFlag( ) {
        return rirFlag;
    } 

    public java.util.List getEndpointInfo( ) {
        return theEndpointInfo;
    }

    public String getKeyString( ) {
        return theKeyString;
    }

    public String getStringifiedName( ) {
        return theStringifiedName;
    }

    public abstract boolean isCorbanameURL( );

    public void dPrint( ) {
        System.out.println( "URL Dump..." );
        System.out.println( "Key String = " + getKeyString( ) );
        System.out.println( "RIR Flag = " + getRIRFlag( ) );
        System.out.println( "isCorbanameURL = " + isCorbanameURL() );
        for( int i = 0; i < theEndpointInfo.size( ); i++ ) {
            ((IIOPEndpointInfo) theEndpointInfo.get( i )).dump( );
        }
        if( isCorbanameURL( ) ) {
            System.out.println( "Stringified Name = " + getStringifiedName() );
        }
    }
    
}

