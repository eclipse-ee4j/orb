/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package org.glassfish.jndi.cosnaming;

import javax.naming.*;
import javax.naming.spi.StateFactory;
import java.util.Hashtable;


import java.rmi.Remote;

import org.glassfish.jndi.toolkit.corba.CorbaUtils;  // for RMI-IIOP

/**
  * StateFactory that turns java.rmi.Remote objects to org.omg.CORBA.Object.
  *
  */

public class RemoteToCorba implements StateFactory {
    public RemoteToCorba() {
    }

    /**
     * Returns the CORBA object for a Remote object.
     * If input is not a Remote object, or if Remote object uses JRMP, return null.
     * If the RMI-IIOP library is not available, throw ConfigurationException.
     *
     * @param orig The object to turn into a CORBA object. If not Remote,
     *             or if is a JRMP stub or impl, return null.
     * @param name Ignored
     * @param ctx The non-null CNCtx whose ORB to use.
     * @param env Ignored
     * @return The CORBA object for {@code orig} or null.
     * @exception ConfigurationException If the CORBA object cannot be obtained
     *    due to configuration problems, for instance, if RMI-IIOP not available.
     * @exception NamingException If some other problem prevented a CORBA
     *    object from being obtained from the Remote object.
     */
    public Object getStateToBind(Object orig, Name name, Context ctx,
        Hashtable<?,?> env) throws NamingException {
    	try {
	        if (orig instanceof org.omg.CORBA.Object) {
	            // Already a CORBA object, just use it
	            return null;
	        }

	        if (orig instanceof Remote) {
	            // Turn remote object into org.omg.CORBA.Object
	            // Returns null if JRMP; let next factory try
	            // CNCtx will eventually throw IllegalArgumentException if
	            // no CORBA object gotten
	            return CorbaUtils.remoteToCorba((Remote)orig, ((CNCtx)ctx)._orb);
	        }
	    }catch(Exception e){
	    	e.printStackTrace();
	    };
        return null; // pass and let next state factory try
    }
}
