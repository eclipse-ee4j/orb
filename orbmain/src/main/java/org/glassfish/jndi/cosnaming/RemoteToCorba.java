/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.jndi.cosnaming;

import java.rmi.Remote;
import java.util.Hashtable;

import javax.naming.ConfigurationException;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.spi.StateFactory;

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
