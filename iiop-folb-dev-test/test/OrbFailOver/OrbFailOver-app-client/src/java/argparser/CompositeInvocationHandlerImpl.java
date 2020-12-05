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

package argparser ;


import java.util.Map ;
import java.util.LinkedHashMap ;
  
import java.lang.reflect.Method ;
import java.lang.reflect.InvocationHandler ;

public class CompositeInvocationHandlerImpl implements 
    CompositeInvocationHandler
{
    private Map<Class<?>,InvocationHandler> classToInvocationHandler =
        new LinkedHashMap<Class<?>,InvocationHandler>() ;
    private InvocationHandler defaultHandler = null ;

    public void addInvocationHandler( Class<?> interf,
	InvocationHandler handler ) 
    {
	classToInvocationHandler.put( interf, handler ) ;
    }

    public void setDefaultHandler( InvocationHandler handler ) 
    {
	defaultHandler = handler ;
    }

    public Object invoke( Object proxy, Method method, Object[] args )
	throws Throwable
    {
	// Note that the declaring class in method is the interface
	// in which the method was defined, not the proxy class.
	Class<?> cls = method.getDeclaringClass() ;

        // Handle Object methods here.  This allows overridding of
        // toString, equals, and hashCode in a class that extends
        // CompositeInvocationHandlerImpl.
        if (cls.equals( Object.class )) {
            try {
                return method.invoke( this, args ) ;
            } catch (Exception exc) {
                throw new RuntimeException( "Invocation error on Object method",
                    exc ) ;
            }
        }

	InvocationHandler handler = classToInvocationHandler.get(cls) ;

	if (handler == null) {
	    if (defaultHandler != null) {
                handler = defaultHandler;
            } else {
		throw new RuntimeException( "No invocation handler for method " 
		    + "\"" + method.toString() + "\"" ) ;
	    }
	}

	// handler should never be null here.

	return handler.invoke( proxy, method, args ) ;
    }
}
