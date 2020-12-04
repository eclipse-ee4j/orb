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

package com.sun.corba.ee.impl.presentation.rmi ;

import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.presentation.rmi.StubAdapter;

import javax.naming.ConfigurationException;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.spi.StateFactory;
import javax.rmi.PortableRemoteObject;
import java.lang.reflect.Field;
import java.rmi.Remote;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

// This creates a dependendcy on the implementation
// of the CosNaming service provider.

/**
  * StateFactory that turns java.rmi.Remote objects to org.omg.CORBA.Object.
  * This version works either with standard RMI-IIOP or Dynamic RMI-IIOP.
  *
  * @author Ken Cavanaugh 
  */

public class JNDIStateFactoryImpl implements StateFactory {

    @SuppressWarnings("WeakerAccess")
    public JNDIStateFactoryImpl() { }

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
     * @return The CORBA object for <tt>orig</tt> or null.
     * @exception ConfigurationException If the CORBA object cannot be obtained
     *    due to configuration problems
     * @exception NamingException If some other problem prevented a CORBA
     *    object from being obtained from the Remote object.
     */
    @Override
    public Object getStateToBind(Object orig, Name name, Context ctx, Hashtable<?,?> env) throws NamingException {
        if (orig instanceof org.omg.CORBA.Object) {
            return orig;
        }

        if (!(orig instanceof Remote)) {
            return null;
        }

        ORB orb = getORB( ctx ) ; 
        if (orb == null) {
            // Wrong kind of context, so just give up and let another StateFactory
            // try to satisfy getStateToBind.
            return null ;
        }

        Remote stub;

        try {
            stub = PortableRemoteObject.toStub( (Remote)orig ) ;
        } catch (Exception exc) {
            Exceptions.self.noStub( exc ) ;
            // Wrong sort of object: just return null to allow another StateFactory
            // to handle this.  This can happen easily because this StateFactory
            // is specified for the application, not the service context provider.
            return null ;
        }

        if (StubAdapter.isStub( stub )) {
            try {
                StubAdapter.connect( stub, orb ) ; 
            } catch (Exception exc) {
                Exceptions.self.couldNotConnect( exc ) ;

                if (!(exc instanceof java.rmi.RemoteException)) {
                    // Wrong sort of object: just return null to allow another StateFactory
                    // to handle this call.
                    return null ;
                }

                // ignore RemoteException because stub might have already
                // been connected
            }
        }

        return stub ;
    }

    // This is necessary because the _orb field is package private in 
    // com.sun.jndi.cosnaming.CNCtx.  This is not an ideal solution.
    // The best solution for our ORB is to change the CosNaming provider
    // to use the StubAdapter.  But this has problems as well, because
    // other vendors may use the CosNaming provider with a different ORB
    // entirely.
    private ORB getORB( Context ctx ) {
        try {
            return (ORB) getOrbField(ctx).get( ctx ) ;
        } catch (Exception exc) {
            Exceptions.self.couldNotGetORB( exc, ctx );
            return null;
        }
    }

    private ConcurrentMap<Class<?>, Field> orbFields = new ConcurrentHashMap<>();

    private Field getOrbField(Context ctx) {
        Field orbField = orbFields.get(ctx.getClass());
        if (orbField != null) return orbField;

        orbField = AccessController.doPrivileged((PrivilegedAction<Field>) () -> getField(ctx.getClass(), "_orb"));

        orbFields.put(ctx.getClass(), orbField);
        return orbField;
    }

    private Field getField(Class<?> aClass, String fieldName) {
        try {
            Field field = aClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            if (aClass.getSuperclass() == null)
                return null;
            else
                return getField(aClass.getSuperclass(), fieldName);
        }
    }
}
