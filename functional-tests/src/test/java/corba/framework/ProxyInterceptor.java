/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.framework ;

import java.util.Iterator ;
import java.util.Set ;
import java.util.HashSet ;
import java.lang.reflect.Method ;
import java.lang.reflect.Proxy ;
import java.lang.reflect.InvocationHandler ;
import java.lang.reflect.InvocationTargetException ;

import corba.framework.MethodEvent ;
import corba.framework.MethodEventListener ;

/** Create an interceptor that reports method entry and exit for a 
 * certain set of methods.  Method entry and exit is reported as
 * a MethodEvent to all registered MethodEventListeners.
 * <p>
 * For example, suppose that we want to trace all calls to 
 * Resolver.list and Resolver.resolve.  This could be done as follows, 
 * inside a user defined ORB Configurator:
 * <pre>
 * // Create the proxy resolver, and plug it into the ORB.
 * Resolver resolver = orb.getResolver() ;
 * ProxyInterceptor proxy = ProxyInterceptor.make( "ResolverInterceptor",
 *     new Class[] { Resolver.class }, resolver ) ;
 * orb.setResolver( (Resolver)proxy.getActual() ) ;
 *
 * // Register the methods we want to monitor
 * Class cls = Resolver.class ;
 * Method resolveMethod = cls.getMethod( "resolve", 
 *     new Class[] { String.class } ) ;
 * Method listMethod = cls.getMethod( "list", null ) ;
 * proxy.addMethod( resolveMethod ) ;
 * proxy.addMethod( listMethod ) ;
 *
 * // Add the test listener
 * proxy.addListener( traceAccumulator ) ;
 * </pre>
 * <p>
 * Later, we just do traceAccumulator.validate( seq ), where
 * seq is the expected sequence of entry/exit events.
 */
public class ProxyInterceptor {
    private final String id ;       // The id for this interceptor
    private final Set methods ;     // Set<Method>
    private final Set listeners ;   // Set<Listener>
    private final Object target ;   // The actual target
    private final Object actual ;   // The proxy that forwards requests
                                    // to target

    private class MyHandler implements InvocationHandler
    {
        private void broadcastEnter( MethodEvent mev ) 
        {
            Iterator iter = listeners.iterator() ;
            while (iter.hasNext()) {
                MethodEventListener listener = 
                    (MethodEventListener)(iter.next()) ;
                listener.methodEntered( mev ) ;
            }
        }

        private void broadcastExit( MethodEvent mev ) 
        {
            Iterator iter = listeners.iterator() ;
            while (iter.hasNext()) {
                MethodEventListener listener = 
                    (MethodEventListener)(iter.next()) ;
                listener.methodExited( mev ) ;
            }
        }

        private Object invokeMethod( Method method, Object target, 
            Object[] args )
        {
            try {
                return method.invoke( target, args )  ;
            } catch (IllegalAccessException exc) {
                throw new RuntimeException( 
                    "Illegal access exception on method " + method, exc ) ;
            } catch (IllegalArgumentException exc) {
                throw new RuntimeException( 
                    "Illegal argument exception on method " + method, exc ) ;
            } catch (InvocationTargetException exc) {
                throw new RuntimeException( 
                    "Invocation target exception on method " + method, exc ) ;
            }
        }

        public synchronized Object invoke( Object proxy, Method method,
            Object[] args )
        {
            if (methods.contains( method )) {
                MethodEvent mev = MethodEvent.make( id, method ) ;
                try {
                    broadcastEnter( mev ) ;
                    return invokeMethod( method, target, args ) ;
                } finally {
                    broadcastExit( mev ) ;
                }
            } else
                return invokeMethod( method, target, args ) ;
        }
    }

    /** Create a ProxyInterceptor for the given classes.
     * Each Class in intf must be an interface.
     * The id string is used to identify which ProxyInterceptor this 
     * is.
     */
    public static ProxyInterceptor make( String id, Class[] intf,
        Object target ) 
    {
        return new ProxyInterceptor( id, intf, target ) ;
    }

    private ProxyInterceptor( String id, Class[] intf, Object target ) 
    {
        this.id = id ;
        methods = new HashSet() ;
        listeners = new HashSet() ;
        this.target = target ;
        InvocationHandler ih = new MyHandler() ;
        actual = Proxy.newProxyInstance( intf[0].getClassLoader(),
            intf, ih ) ;
    }

    /** Add method to the list of methods that are reported to the
     * listeners.
     */
    public synchronized void addMethod( Method method ) 
    {
        methods.add( method ) ;
    }

    /** Remote method from the list of methods that are reported to the
     * listeners.
     */
    public synchronized void removeMethod( Method method ) 
    {
        methods.remove( method ) ;
    }

    /** Return the list of methods are reported to the listeners.
     */
    public synchronized Method[] getMethods()
    {
        return (Method[])methods.toArray( new Method[0] ) ;
    }

    /** Add listener to the list of listeners for 
     * method entry and exit events.
     */
    public synchronized void addListener( MethodEventListener listener )
    {
        listeners.add( listener ) ;
    }

    /** Remove listener from the list of listeners for 
     * method entry and exit events.
     */
    public synchronized void removeListener( MethodEventListener listener ) 
    {
        listeners.remove( listener ) ;
    }

    /** Return the list of listeners for 
     * method entry and exit events.
     */
    public synchronized MethodEventListener[] getListeners()
    {
        return (MethodEventListener[])listeners.toArray( 
            new MethodEventListener[0] ) ;
    }

    /** Return the actual object to use.  This object simply
     * forwards all method invocations to the target, reporting
     * method entry and exit for registered methods to all 
     * registered listeners.
     */
    public synchronized Object getActual() 
    {
        return actual ;
    }
}
