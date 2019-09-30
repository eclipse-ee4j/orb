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

/** 
 *  The corbaname: URL definitions from the -ORBInitDef and -ORBDefaultInitDef's
 *  will be stored in this object. This object is capable of storing CorbaLoc
 *  profiles as defined in the CorbaName grammer.
 *
 *  @author Hemanth
 */
public class CorbanameURL extends INSURLBase
{
    /**
     * This constructor takes a corbaname: url with 'corbaname:' prefix stripped
     * and initializes all the variables accordingly. If there are any parsing
     * errors then BAD_PARAM exception is raised.
     */
    public CorbanameURL( String aURL ) {
        String url = aURL;
  
        // First Clean the URL Escapes if there are any
        try {
            url = Utility.cleanEscapes( url );
        } catch( Exception e ) {
            badAddress( e, aURL );
        }

        int delimiterIndex = url.indexOf( '#' );
        String corbalocString = null;
        if( delimiterIndex != -1 ) {
            corbalocString = "corbaloc:" + url.substring( 0, delimiterIndex ) ;
        } else {
            corbalocString = "corbaloc:" + url ;
        }

        try {
            // Check the corbaloc grammar and set the returned corbaloc
            // object to the CorbaName Object
            INSURL insURL = 
                INSURLHandler.getINSURLHandler().parseURL( corbalocString );
            copyINSURL( insURL );
            // String after '#' is the Stringified name used to resolve
            // the Object reference from the rootnaming context. If
            // the String is null then the Root Naming context is passed
            // back
            if((delimiterIndex > -1) &&
               (delimiterIndex < (aURL.length() - 1)))
            {
                int start = delimiterIndex + 1 ;
                String result = url.substring(start) ;
                theStringifiedName = result ;
            } 
        } catch( Exception e ) {
            badAddress( e, aURL );
        }
    }

    /**
     * A Utility method to copy all the variables from CorbalocURL object to
     * this instance. 
     */
    private void copyINSURL( INSURL url ) {
        rirFlag = url.getRIRFlag( );
        theEndpointInfo = (java.util.ArrayList) url.getEndpointInfo( );
        theKeyString = url.getKeyString( );
        theStringifiedName = url.getStringifiedName( );
    }

    public boolean isCorbanameURL( ) {
        return true;
    }

}
