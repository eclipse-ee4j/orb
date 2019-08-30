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

import org.omg.CORBA.CompletionStatus;
import java.util.StringTokenizer;

/**
 *  This class is the entry point to parse different types of INS URL's.
 * 
 *  @author Hemanth
 */

public class INSURLHandler {

    private static INSURLHandler insURLHandler = null;

    // Length of corbaloc:
    private static final int CORBALOC_PREFIX_LENGTH = 9;

    // Length of corbaname:
    private static final int CORBANAME_PREFIX_LENGTH = 10;

    private INSURLHandler( ) {
    }

    public synchronized static INSURLHandler getINSURLHandler( ) {
        if( insURLHandler == null ) {
            insURLHandler = new INSURLHandler( );
        }
        return insURLHandler;
    }

    public INSURL parseURL( String aUrl ) {
        String url = aUrl;
        if ( url.startsWith( "corbaloc:" ) == true ) {
            return new CorbalocURL( url.substring( CORBALOC_PREFIX_LENGTH ) ); 
        } else if ( url.startsWith ( "corbaname:" ) == true ) {
            return new CorbanameURL( url.substring( CORBANAME_PREFIX_LENGTH ) );
        } 
        return null;
    }
}
