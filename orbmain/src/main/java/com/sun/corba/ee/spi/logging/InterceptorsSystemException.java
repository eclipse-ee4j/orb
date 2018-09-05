/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.logging ;

import com.sun.corba.ee.spi.oa.ObjectAdapter;
import org.glassfish.pfl.basic.logex.Chain;
import org.glassfish.pfl.basic.logex.ExceptionWrapper;
import org.glassfish.pfl.basic.logex.Log;
import org.glassfish.pfl.basic.logex.LogLevel;
import org.glassfish.pfl.basic.logex.Message;
import org.glassfish.pfl.basic.logex.WrapperGenerator;
import com.sun.corba.ee.spi.logex.corba.CS;
import com.sun.corba.ee.spi.logex.corba.CSValue;

import com.sun.corba.ee.spi.logex.corba.ORBException ;
import com.sun.corba.ee.spi.logex.corba.CorbaExtension ;
import java.util.List;

import org.omg.CORBA.BAD_INV_ORDER;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CORBA.UNKNOWN;
import org.omg.PortableInterceptor.ObjectReferenceTemplate;

@ExceptionWrapper( idPrefix="IOP" )
@ORBException( omgException=false, group=CorbaExtension.InterceptorsGroup )
public interface InterceptorsSystemException {
    InterceptorsSystemException self = WrapperGenerator.makeWrapper( 
        InterceptorsSystemException.class, CorbaExtension.self ) ;
    
    @Log( level=LogLevel.WARNING, id=1 )
    @Message( "Interceptor type {0} is out of range" )
    BAD_PARAM typeOutOfRange( int type ) ;
    
    @Log( level=LogLevel.WARNING, id=2 )
    @Message( "Interceptor's name is null: use empty string for "
        + "anonymous interceptors" )
    BAD_PARAM nameNull(  ) ;
    
    @Log( level=LogLevel.WARNING, id=1 )
    @Message( "resolve_initial_reference is invalid during pre_init" )
    BAD_INV_ORDER rirInvalidPreInit(  ) ;
    
    @Log( level=LogLevel.WARNING, id=2 )
    @Message( "Expected state {0}, but current state is {1}" )
    BAD_INV_ORDER badState1( int arg0, int arg1 ) ;
    
    @Log( level=LogLevel.WARNING, id=3 )
    @Message( "Expected state {0} or {1}, but current state is {2}" )
    BAD_INV_ORDER badState2( int arg0, int arg1, int arg2 ) ;
    
    @Log( level=LogLevel.WARNING, id=1 )
    @Message( "IOException during cancel request" )
    @CS( CSValue.MAYBE )
    COMM_FAILURE ioexceptionDuringCancelRequest( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=1 )
    @Message( "Exception was null" )
    INTERNAL exceptionWasNull(  ) ;
    
    @Log( level=LogLevel.WARNING, id=2 )
    @Message( "Object has no delegate" )
    INTERNAL objectHasNoDelegate(  ) ;
    
    @Log( level=LogLevel.WARNING, id=3 )
    @Message( "Delegate was not a ClientRequestDispatcher" )
    INTERNAL delegateNotClientsub(  ) ;
    
    @Log( level=LogLevel.WARNING, id=4 )
    @Message( "Object is not an ObjectImpl" )
    INTERNAL objectNotObjectimpl(  ) ;
    
    @Log( level=LogLevel.WARNING, id=5 )
    @Message( "Assertion failed: Interceptor set exception to UserException or "
        + "ApplicationException" )
    INTERNAL exceptionInvalid(  ) ;
    
    @Log( level=LogLevel.WARNING, id=6 )
    @Message( "Assertion failed: Reply status is initialized but not "
        + "SYSTEM_EXCEPTION or LOCATION_FORWARD" )
    INTERNAL replyStatusNotInit(  ) ;
    
    @Log( level=LogLevel.WARNING, id=7 )
    @Message( "Exception in arguments" )
    INTERNAL exceptionInArguments( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=8 )
    @Message( "Exception in exceptions" )
    INTERNAL exceptionInExceptions( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=9 )
    @Message( "Exception in contexts" )
    INTERNAL exceptionInContexts( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=10 )
    @Message( "Another exception was null" )
    INTERNAL exceptionWasNull2(  ) ;
    
    @Log( level=LogLevel.WARNING, id=11 )
    @Message( "Servant invalid" )
    INTERNAL servantInvalid(  ) ;
    
    @Log( level=LogLevel.WARNING, id=12 )
    @Message( "Can't pop only PICurrent" )
    INTERNAL cantPopOnlyPicurrent(  ) ;
    
    @Log( level=LogLevel.WARNING, id=13 )
    @Message( "Can't pop another PICurrent" )
    INTERNAL cantPopOnlyCurrent2(  ) ;
    
    @Log( level=LogLevel.WARNING, id=14 )
    @Message( "DSI result is null" )
    INTERNAL piDsiResultIsNull(  ) ;
    
    @Log( level=LogLevel.WARNING, id=15 )
    @Message( "DII result is null" )
    INTERNAL piDiiResultIsNull(  ) ;
    
    @Log( level=LogLevel.WARNING, id=16 )
    @Message( "Exception is unavailable" )
    INTERNAL exceptionUnavailable(  ) ;
    
    @Log( level=LogLevel.WARNING, id=17 )
    @Message( "Assertion failed: client request info stack is null" )
    INTERNAL clientInfoStackNull(  ) ;
    
    @Log( level=LogLevel.WARNING, id=18 )
    @Message( "Assertion failed: Server request info stack is null" )
    INTERNAL serverInfoStackNull(  ) ;
    
    @Log( level=LogLevel.WARNING, id=19 )
    @Message( "Mark and reset failed" )
    INTERNAL markAndResetFailed( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=20 )
    @Message( "currentIndex > tableContainer.size(): {0} > {1}" )
    INTERNAL slotTableInvariant( int arg0, int arg1 ) ;
    
    @Log( level=LogLevel.WARNING, id=21 )
    @Message( "InterceptorList is locked" )
    INTERNAL interceptorListLocked(  ) ;
    
    @Log( level=LogLevel.WARNING, id=22 )
    @Message( "Invariant: sorted size + unsorted size == total size was violated" )
    INTERNAL sortSizeMismatch(  ) ;
    
    @Log( level=LogLevel.FINE, id=23 )
    @Message( "Ignored exception in establish_components method for "
        + "ObjectAdapter {0} (as per specification)" )
    INTERNAL ignoredExceptionInEstablishComponents( @Chain Exception exc,
        ObjectAdapter oa ) ;
    
    @Log( level=LogLevel.FINE, id=24 )
    @Message( "Exception in components_established method for ObjectAdapter {0}" )
    INTERNAL exceptionInComponentsEstablished( @Chain Exception exc, 
        ObjectAdapter oa ) ;
    
    @Log( level=LogLevel.FINE, id=25 )
    @Message( "Ignored exception in adapter_manager_state_changed method for "
        + "managerId {0} and newState {1} (as per specification)" )
    INTERNAL ignoredExceptionInAdapterManagerStateChanged( @Chain Exception exc,
        int managerId, short newState ) ;
    
    @Log( level=LogLevel.FINE, id=26 )
    @Message( "Ignored exception in adapter_state_changed method for " +
        "templates {0} and newState {1} (as per specification)" )
    INTERNAL ignoredExceptionInAdapterStateChanged( @Chain Exception exc,
        List<ObjectReferenceTemplate> templates, short newState ) ;
    
    @Log( level=LogLevel.WARNING, id=1 )
    @Message( "Policies not implemented" )
    NO_IMPLEMENT piOrbNotPolicyBased(  ) ;
    
    @Log( level=LogLevel.FINE, id=1 )
    @Message( "ORBInitInfo object is only valid during ORB_init" )
    OBJECT_NOT_EXIST orbinitinfoInvalid(  ) ;
    
    @Log( level=LogLevel.FINE, id=1 )
    @Message( "Unknown request invocation error" )
    @CS( CSValue.MAYBE )
    UNKNOWN unknownRequestInvoke(  ) ;
}
