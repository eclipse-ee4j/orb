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

package com.sun.corba.ee.impl.interceptors;

             
import org.omg.CORBA.Any;
import org.omg.CORBA.NVList;


import org.omg.CORBA.portable.RemarshalException;

import org.omg.PortableInterceptor.ObjectReferenceTemplate ;
import org.omg.PortableInterceptor.Interceptor;
import org.omg.PortableInterceptor.PolicyFactory;
import org.omg.PortableInterceptor.Current;

import org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName ;


import com.sun.corba.ee.spi.ior.ObjectKeyTemplate;

import com.sun.corba.ee.spi.oa.ObjectAdapter;


import com.sun.corba.ee.spi.protocol.PIHandler;
import com.sun.corba.ee.spi.protocol.MessageMediator;

import com.sun.corba.ee.impl.corba.RequestImpl;

import com.sun.corba.ee.impl.protocol.giopmsgheaders.ReplyMessage;

/** 
 * This is No-Op implementation of PIHandler. It is used in ORBConfigurator
 * to initialize a piHandler before the Persistent Server Activation. This 
 * PIHandler implementation will be replaced by the real PIHandler in 
 * ORB.postInit( ) call.
 */
public class PINoOpHandlerImpl implements PIHandler 
{
    public PINoOpHandlerImpl( ) {
    }

    public void close() {
    }

    public void initialize() {
    }

    public void destroyInterceptors() {
    }

    public void objectAdapterCreated( ObjectAdapter oa ) 
    {
    }

    public void adapterManagerStateChanged( int managerId,
        short newState )
    {
    }

    public void adapterStateChanged( ObjectReferenceTemplate[] 
        templates, short newState )
    {
    }


    public void disableInterceptorsThisThread() {
    }
    
    public void enableInterceptorsThisThread() {
    }
    
    public void invokeClientPIStartingPoint() 
        throws RemarshalException
    {
    }
    
    public Exception invokeClientPIEndingPoint(
        int replyStatus, Exception exception )
    {
        return null;
    }

    public Exception makeCompletedClientRequest(
        int replyStatus, Exception exception )
    {
        return null;
    }
    
    public void initiateClientPIRequest( boolean diiRequest ) {
    }
    
    public void cleanupClientPIRequest() {
    }

    public void setClientPIInfo(MessageMediator messageMediator)
    {
    }

    public void setClientPIInfo( RequestImpl requestImpl ) 
    {
    }
    
    final public void sendCancelRequestIfFinalFragmentNotSent()
    {
    }
    
    
    public void invokeServerPIStartingPoint() 
    {
    }

    public void invokeServerPIIntermediatePoint() 
    {
    }
    
    public void invokeServerPIEndingPoint( ReplyMessage replyMessage ) 
    {
    }
    
    public void setServerPIInfo( Exception exception ) {
    }

    public void setServerPIInfo( NVList arguments )
    {
    }

    public void setServerPIExceptionInfo( Any exception )
    {
    }

    public void setServerPIInfo( Any result )
    {
    }

    public void initializeServerPIInfo( MessageMediator request,
        ObjectAdapter oa, byte[] objectId, ObjectKeyTemplate oktemp ) 
    {
    }
    
    public void setServerPIInfo( java.lang.Object servant, 
                                          String targetMostDerivedInterface ) 
    {
    }

    public void cleanupServerPIRequest() {
    }
    
    public void register_interceptor( Interceptor interceptor, int type ) 
        throws DuplicateName
    {
    }

    public Current getPICurrent( ) {
        return null;
    }

    public org.omg.CORBA.Policy create_policy(int type, org.omg.CORBA.Any val)
        throws org.omg.CORBA.PolicyError
    {
        return null;
    }

    public void registerPolicyFactory( int type, PolicyFactory factory ) {
    }
    
    public int allocateServerRequestId ()
    {
        return 0;
    }
}
