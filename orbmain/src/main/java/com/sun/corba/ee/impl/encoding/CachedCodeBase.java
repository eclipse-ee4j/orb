/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.encoding;

import com.sun.org.omg.CORBA.ValueDefPackage.FullValueDescription;

import com.sun.org.omg.SendingContext.CodeBase;
import com.sun.org.omg.SendingContext.CodeBaseHelper;
import com.sun.org.omg.SendingContext._CodeBaseImplBase;

import com.sun.corba.ee.spi.logging.ORBUtilSystemException;

import com.sun.corba.ee.spi.transport.Connection;

import com.sun.corba.ee.spi.ior.IOR ;

import com.sun.corba.ee.spi.orb.ORB ;
import java.util.Hashtable;

/**
 * Provides the reading side with a per connection cache of
 * info obtained via calls to the remote CodeBase.
 *
 * Previously, most of this was in IIOPConnection.
 *
 * Features:
 *    Delays cache creation unless used
 *    Postpones remote calls until necessary
 *    Handles creating obj ref from IOR
 *    Maintains caches for the following maps:
 *         CodeBase IOR to obj ref (global)
 *         RepId to implementation URL(s)
 *         RepId to remote FVD
 *         RepId to superclass type list
 *
 * Needs cache management.
 */
public class CachedCodeBase extends _CodeBaseImplBase
{
    private static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    private Hashtable<String,String> implementations ;
    private Hashtable<String,FullValueDescription> fvds ;
    private Hashtable<String,String[]> bases ;

    private volatile CodeBase delegate;
    private Connection conn;

    private static Object iorMapLock = new Object() ; 
    private static Hashtable<IOR,CodeBase> iorMap = 
        new Hashtable<IOR,CodeBase>();

    public static synchronized void cleanCache( ORB orb ) {
        synchronized (iorMapLock) {
            for (IOR ior : iorMap.keySet()) {
                if (ior.getORB() == orb) {
                    iorMap.remove( ior ) ;
                }
            }
        }
    }

    public CachedCodeBase(Connection connection) {
        conn = connection;
    }

    public com.sun.org.omg.CORBA.Repository get_ir () {
        return null;
    }
        
    public synchronized String implementation (String repId) {
        String urlResult = null;

        if (implementations == null)
            implementations = new Hashtable<String,String>();
        else
            urlResult = implementations.get(repId);

        if (urlResult == null && connectedCodeBase()) {
            urlResult = delegate.implementation(repId);

            if (urlResult != null)
                implementations.put(repId, urlResult);
        }

        return urlResult;
    }

    public synchronized String[] implementations (String[] repIds) {
        String[] urlResults = new String[repIds.length];

        for (int i = 0; i < urlResults.length; i++)
            urlResults[i] = implementation(repIds[i]);

        return urlResults;
    }

    public synchronized FullValueDescription meta (String repId) {
        FullValueDescription result = null;

        if (fvds == null)
            fvds = new Hashtable<String,FullValueDescription>();
        else
            result = fvds.get(repId);

        if (result == null && connectedCodeBase()) {
            result = delegate.meta(repId);

            if (result != null)
                fvds.put(repId, result);
        }

        return result;
    }

    public synchronized FullValueDescription[] metas (String[] repIds) {
        FullValueDescription[] results 
            = new FullValueDescription[repIds.length];

        for (int i = 0; i < results.length; i++)
            results[i] = meta(repIds[i]);

        return results;
    }

    public synchronized String[] bases (String repId) {

        String[] results = null;

        if (bases == null)
            bases = new Hashtable<String,String[]>();
        else
            results = bases.get(repId);

        if (results == null && connectedCodeBase()) {
            results = delegate.bases(repId);

            if (results != null)
                bases.put(repId, results);
        }

        return results;
    }

    // Ensures that we've used the connection's IOR to create
    // a valid CodeBase delegate.  If this returns false, then
    // it is not valid to access the delegate.
    private synchronized boolean connectedCodeBase() {
        if (delegate != null)
            return true;

        if (conn.getCodeBaseIOR() == null) {
            // The delegate was null, so see if the connection's
            // IOR was set.  If so, then we just need to connect
            // it.  Otherwise, there is no hope of checking the
            // remote code base.  That could be a bug if the
            // service context processing didn't occur, or it
            // could be that we're talking to a foreign ORB which
            // doesn't include this optional service context.

            wrapper.codeBaseUnavailable( conn ) ;

            return false;
        }

        synchronized(iorMapLock) {
            // Do we have a reference initialized by another connection?
            delegate = iorMap.get( conn.getCodeBaseIOR() );
            if (delegate != null)
                return true;
            
            // Connect the delegate and update the cache
            delegate = CodeBaseHelper.narrow( getObjectFromIOR() );
            
            // Save it for the benefit of other connections
            iorMap.put( conn.getCodeBaseIOR(), delegate );
        }

        // It's now safe to use the delegate
        return true;
    }

    private org.omg.CORBA.Object getObjectFromIOR() {
        return CDRInputStream_1_0.internalIORToObject(
            conn.getCodeBaseIOR(), null /*stubFactory*/, conn.getBroker());
    }
}

// End of file.

