/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
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

package com.sun.corba.ee.impl.protocol;

import com.sun.corba.ee.impl.encoding.CDRInputObject;
import com.sun.corba.ee.impl.encoding.CDROutputObject;
import com.sun.corba.ee.impl.encoding.CodeSetComponentInfo;
import com.sun.corba.ee.impl.encoding.CodeSetConversion;
import com.sun.corba.ee.impl.encoding.EncapsInputStream;
import com.sun.corba.ee.impl.misc.ORBUtility;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.ReplyMessage;
import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.ior.iiop.CodeSetsComponent;
import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;
import com.sun.corba.ee.spi.ior.iiop.IIOPProfileTemplate;
import com.sun.corba.ee.spi.logging.ORBUtilSystemException;
import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.orb.ORBVersion;
import com.sun.corba.ee.spi.protocol.ClientRequestDispatcher;
import com.sun.corba.ee.spi.protocol.MessageMediator;
import com.sun.corba.ee.spi.servicecontext.*;
import com.sun.corba.ee.spi.trace.Subcontract;
import com.sun.corba.ee.spi.transport.Connection;
import com.sun.corba.ee.spi.transport.ContactInfo;
import com.sun.corba.ee.spi.transport.ContactInfoListIterator;
import com.sun.corba.ee.spi.transport.OutboundConnectionCache;
import org.glassfish.pfl.tf.spi.TimingPointType;
import org.glassfish.pfl.tf.spi.annotation.InfoMethod;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.portable.ApplicationException;
import org.omg.CORBA.portable.RemarshalException;
import org.omg.CORBA.portable.UnknownException;
import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.IOP.ExceptionDetailMessage;
import org.omg.IOP.TAG_CODE_SETS;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * ClientDelegate is the RMI client-side subcontract or representation
 * It implements RMI delegate as well as our internal ClientRequestDispatcher
 * interface.
 */
@Subcontract
public class ClientRequestDispatcherImpl
    implements
        ClientRequestDispatcher
{
    private static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    // Used for locking
    private final Object lock = new Object();

    private ORBVersionServiceContext ovsc = 
                   ServiceContextDefaults.makeORBVersionServiceContext();


    private MaxStreamFormatVersionServiceContext msfvc = 
                            ServiceContextDefaults.getMaxStreamFormatVersionServiceContext();

    private ConcurrentMap<ContactInfo,Object> locks =
        new ConcurrentHashMap<ContactInfo,Object>() ;

    @InfoMethod
    private void usingCachedConnection( Connection conn ) { }

    @InfoMethod
    private void usingCreatedConnection( Connection conn ) { }

    @InfoMethod
    private void connectionCached( Connection conn ) { }

    @InfoMethod
    private void connectionRegistered( Connection conn ) { }

    @InfoMethod
    private void createdMessageMediator( MessageMediator med ) { }

    @InfoMethod
    private void createOutputObject( CDROutputObject out ) { }

    @InfoMethod
    private void generalMessage ( String msg ) { }

    @InfoMethod
    private void remarshalWithHasNextTrue( ContactInfo info ) { }

    @InfoMethod( tpName="totalRequest", tpType=TimingPointType.ENTER )
    private void enter_totalRequest() { }

    @InfoMethod( tpName="totalRequest", tpType=TimingPointType.EXIT )
    private void exit_totalRequest() { }

    @InfoMethod( tpName="connectionSetup", tpType=TimingPointType.ENTER )
    private void enter_connectionSetup() { }

    @InfoMethod( tpName="connectionSetup", tpType=TimingPointType.EXIT )
    private void exit_connectionSetup() { }

    @InfoMethod( tpName="clientDecoding", tpType=TimingPointType.ENTER )
    private void enter_clientDecoding() { }

    @InfoMethod( tpName="clientDecoding", tpType=TimingPointType.EXIT )
    private void exit_clientDecoding() { }

    @InfoMethod( tpName="clientEncoding", tpType=TimingPointType.ENTER )
    private void enter_clientEncoding() { }

    @InfoMethod( tpName="clientEncoding", tpType=TimingPointType.EXIT )
    private void exit_clientEncoding() { }

    @InfoMethod( tpName="clientTransportAndWait", tpType=TimingPointType.ENTER )
    private void enter_clientTransportAndWait() { }

    @InfoMethod( tpName="clientTransportAndWait", tpType=TimingPointType.EXIT )
    private void exit_clientTransportAndWait() { }

    @InfoMethod( tpName="processResponse", tpType=TimingPointType.ENTER )
    private void enter_processResponse() { }

    @InfoMethod( tpName="processResponse", tpType=TimingPointType.EXIT )
    private void exit_processResponse() { }

    @InfoMethod( tpName="requestAddServiceContexts", tpType=TimingPointType.ENTER )
    private void enter_requestAddServiceContexts() { }

    @InfoMethod( tpName="requestAddServiceContexts", tpType=TimingPointType.EXIT )
    private void exit_requestAddServiceContexts() { }

    @Subcontract
    public CDROutputObject beginRequest(Object self, String opName,
        boolean isOneWay, ContactInfo contactInfo) {

        final ORB orb = contactInfo.getBroker();

        enter_totalRequest() ;

        // Portable Interceptor initialization.
        orb.getPIHandler().initiateClientPIRequest( false );

        Connection connection = null;

        Object lock = locks.get( contactInfo ) ;

        if (lock == null) {
            Object newLock = new Object() ;
            lock = locks.putIfAbsent( contactInfo, newLock ) ;
            if (lock == null) {
                lock = newLock ;
            }
        }

        // This locking is done so that multiple connections are not created
        // for the same endpoint.  Note that we are locking here on the
        // same lock for different contactInfos that are equal, which is really
        // what's required.  This is a fix for 7016182.
        //
        // TODO NEW CACHE: all of this is replaced with the get() call, except
        // the code needed to add the connection to the selector.
        //
        // Is the fix for 7016182 still needed with the new caches?
        synchronized (lock) {
            if (contactInfo.isConnectionBased()) {
                try {
                    enter_connectionSetup();

                    if (contactInfo.shouldCacheConnection()) {
                        connection = orb.getTransportManager()
                            .getOutboundConnectionCache(contactInfo)
                            .get(contactInfo);
                    }

                    if (connection != null) {
                        usingCachedConnection( connection ) ;
                    } else {
                        connection =
                            contactInfo.createConnection();
                        usingCreatedConnection( connection ) ;

                        if (connection.shouldRegisterReadEvent()) {
                            orb.getTransportManager().getSelector(0)
                                .registerForEvent(connection.getEventHandler());
                            connection.setState("ESTABLISHED");
                            connectionRegistered( connection ) ;
                        }

                        // Do not do connection reclaim here since the 
                        // connections are marked in use by registerWaiter()
                        // call and since this call happens later do it after
                        // that.
                        if (contactInfo.shouldCacheConnection()) {
                            OutboundConnectionCache connectionCache =
                                orb.getTransportManager()
                                        .getOutboundConnectionCache(contactInfo);
                            connectionCache.stampTime(connection);
                            connectionCache.put(contactInfo, connection);
                            connectionCached( connection ) ;
                        }
                    }
                } finally {
                    exit_connectionSetup();
                }
            }
        }

        MessageMediator messageMediator =
            contactInfo.createMessageMediator(orb, contactInfo, connection,
                opName, isOneWay);
        createdMessageMediator(messageMediator);

        // NOTE: Thread data so we can get the mediator in release reply
        // in order to remove the waiter in CorbaConnection.
        // We cannot depend on obtaining information in releaseReply
        // via its InputStream argument since, on certain errors
        // (e.g., client marshaling errors), the stream may be null.
        // Likewise for releaseReply "self".
        // NOTE: This must be done before initializing the message since
        // that may start sending fragments which may end up in "early"
        // replies or client marshaling exceptions.
        orb.getInvocationInfo().setMessageMediator(messageMediator);

        performCodeSetNegotiation(messageMediator);

        enter_requestAddServiceContexts() ;
        try {
            addServiceContexts(messageMediator);
        } finally {
            exit_requestAddServiceContexts() ;
        }

        CDROutputObject outputObject = contactInfo.createOutputObject(messageMediator);

        createOutputObject(outputObject);

        // NOTE: Not necessary for oneways, but useful for debugging.
        // This must be done BEFORE message initialization since fragments
        // may be sent at that time.
        registerWaiter(messageMediator);

        // Do connection reclaim now
        synchronized (lock) {
            if (contactInfo.isConnectionBased()) {
                if (contactInfo.shouldCacheConnection()) {
                    generalMessage( "reclaiming connections" );
                    OutboundConnectionCache connectionCache = orb.getTransportManager()
                        .getOutboundConnectionCache(contactInfo);
                    connectionCache.reclaim();
                }
            }
        }

        orb.getPIHandler().setClientPIInfo(messageMediator);
        try {
            // This MUST come before message is initialized so
            // service contexts may be added by PI because
            // initial fragments may be sent during message initialization.
            orb.getPIHandler().invokeClientPIStartingPoint();
        } catch( RemarshalException e ) {
            generalMessage( "Remarshal" ) ;

            // NOTE: We get here because an interceptor raised ForwardRequest
            // and updated the IOR/Iterator.  Since we have a fresh iterator
            // hasNext should succeed.

            // REVISIT: We should feed ALL interceptor exceptions to
            // iterator.reportException so it can determine if it wants
            // to retry.  Right now, SystemExceptions will flow to the
            // client code.

            // REVISIT:
            // This assumes that interceptors update
            // ContactInfoList outside of subcontract.
            // Want to move that update to here.
            if (getContactInfoListIterator(orb).hasNext()) {
                contactInfo = getContactInfoListIterator(orb).next();
                remarshalWithHasNextTrue(contactInfo);

                // Fix for 6763340: Complete the first attempt before starting another.
                orb.getPIHandler().makeCompletedClientRequest(
                    ReplyMessage.LOCATION_FORWARD, null ) ;
                unregisterWaiter( orb ) ;
                orb.getPIHandler().cleanupClientPIRequest() ;

                return beginRequest(self, opName, isOneWay, contactInfo);
            } else {
                retryMessage( "RemarshalException: hasNext false" ) ;
                throw wrapper.remarshalWithNowhereToGo();
            }
        }

        messageMediator.initializeMessage();
        generalMessage( "initialized message");

        enter_clientEncoding();

        return outputObject;
    }

    @InfoMethod
    private void operationAndId( String op, int rid ) { }

    @Subcontract
    public CDRInputObject marshalingComplete(java.lang.Object self,
                                          CDROutputObject outputObject)
        throws 
            ApplicationException, 
            org.omg.CORBA.portable.RemarshalException
    {
        MessageMediator messageMediator = outputObject.getMessageMediator();
        ORB orb = messageMediator.getBroker();
        operationAndId(messageMediator.getOperationName(), 
            messageMediator.getRequestId() );

        try {
            exit_clientEncoding();

            enter_clientTransportAndWait();

            CDRInputObject inputObject = null ;
            try {
                inputObject = marshalingComplete1(orb, messageMediator);
            } finally {
                exit_clientTransportAndWait();
            }

            return processResponse(orb, messageMediator, inputObject);
        } finally {
            // We must ALWAYS call enter_ClientDecoding, so that the
            // corresponding exit_clientDecoding gets called in endRequest().
            enter_clientDecoding() ;
        }
    }

    @InfoMethod
    private void retryMessage( String msg ) { }

    @InfoMethod
    private void reportException( Throwable exc ) { }

    @InfoMethod
    private void reportException( String msg, Throwable exc ) { }

    @Subcontract
    public CDRInputObject marshalingComplete1(
            ORB orb, MessageMediator messageMediator)
        throws
            ApplicationException,
            org.omg.CORBA.portable.RemarshalException
    {
        operationAndId(messageMediator.getOperationName(),
            messageMediator.getRequestId() );

        try {
            messageMediator.finishSendingRequest();

            // TODO NEW CACHE: call release( conn, numResponse ) here
            // Get connection from MessageMediator
            // Get numResponse from messageMediator.isOneWay: 0 or 1 expected.

            return messageMediator.waitForResponse();
        } catch (RuntimeException e) {
            reportException( e ) ;

            boolean retry  =
                getContactInfoListIterator(orb)
                    .reportException(messageMediator.getContactInfo(), e);

            // Bug 6382377: must not lose exception in PI
            // Must run interceptor end point before retrying.
            Exception newException =
                orb.getPIHandler().invokeClientPIEndingPoint(
                    ReplyMessage.SYSTEM_EXCEPTION, e);

            if (retry) {
                if (newException == e) {
                    retryMessage( "Retry true; same exception" ) ;
                    continueOrThrowSystemOrRemarshal(messageMediator,
                                                     new RemarshalException());
                } else {
                    retryMessage( "Retry true; new exception" ) ;
                    continueOrThrowSystemOrRemarshal(messageMediator,
                                                     newException);
                }
            } else {
                // NOTE: Interceptor ending point will run in releaseReply.
                if (newException instanceof RuntimeException) {
                    retryMessage( "Retry false; RuntimeException" ) ;
                    throw (RuntimeException)newException ;
                } else if (newException instanceof RemarshalException) {
                     throw (RemarshalException) newException;
                } else {
                    retryMessage( "Retry false; other exception" ) ;
                    throw e ;
                }
            }

            return null; // for compiler
        }
    }

    @InfoMethod
    private void receivedUserException( String repoid ) { }

    @InfoMethod
    private void receivedUserExceptionDII( Throwable exc, Throwable newExc ) { }

    @InfoMethod
    private void receivedUserExceptionNotDII( Throwable exc, Throwable newExc ) { }

    @Subcontract
    protected CDRInputObject processResponse(ORB orb, 
        MessageMediator messageMediator, CDRInputObject inputObject)
        throws ApplicationException, org.omg.CORBA.portable.RemarshalException {

        operationAndId(messageMediator.getOperationName(),
            messageMediator.getRequestId() );

        enter_processResponse() ;
        try {
            // We know for sure now that we've sent a message.
            // So OK to not send initial again.
            if (messageMediator.getConnection() != null) {
                generalMessage( "Non-null connection" ) ;
                messageMediator.getConnection().setPostInitialContexts();
            }

            // NOTE: not necessary to set MessageMediator for PI.
            // It already has it.

            // Process the response.

            Exception exception = null;

            if (messageMediator.isOneWay()) {
                generalMessage( "One way request" ) ;
                getContactInfoListIterator(orb)
                    .reportSuccess(messageMediator.getContactInfo());
                // Invoke Portable Interceptors with receive_other
                exception = orb.getPIHandler().invokeClientPIEndingPoint(
                    ReplyMessage.NO_EXCEPTION, exception );
                reportException(exception);
                continueOrThrowSystemOrRemarshal(messageMediator, exception);
                return null;
            }

            consumeServiceContexts(orb, messageMediator);

            // Now that we have the service contexts processed and the
            // correct ORBVersion set, we must finish initializing the stream.
            // REVISIT - need interface for this operation.
            inputObject.performORBVersionSpecificInit();

            if (messageMediator.isSystemExceptionReply()) {
                SystemException se = messageMediator.getSystemExceptionReply();
                reportException( "received system exception", se);

                boolean doRemarshal =
                    getContactInfoListIterator(orb)
                        .reportException(messageMediator.getContactInfo(), se);

                if (doRemarshal) {
                    reportException( "Do remarshal", se);
                        
                    // Invoke Portable Interceptors with receive_exception:
                    exception = orb.getPIHandler().invokeClientPIEndingPoint(
                        ReplyMessage.SYSTEM_EXCEPTION, se );

                    // If PI did not change the exception, throw a
                    // Remarshal.
                    if( se == exception ) {
                        generalMessage( "Do remarshal: same exception");
                        // exception = null is to maintain symmetry with
                        // GenericPOAClientSC.
                        exception = null;
                        continueOrThrowSystemOrRemarshal(messageMediator,
                                                         new RemarshalException());
                        throw wrapper.statementNotReachable1() ;
                    } else {
                        reportException( "Do remarshal: new exception", exception );
                        //  Otherwise, throw the exception PI wants thrown.
                        continueOrThrowSystemOrRemarshal(messageMediator,
                                                         exception);
                        throw wrapper.statementNotReachable2() ;
                    }
                }

                // No retry, so see if was unknown.
                reportException( "NO remarshal", se);

                ServiceContexts contexts = 
                    messageMediator.getReplyServiceContexts();
                if (contexts != null) {
                    UEInfoServiceContext usc =
                        (UEInfoServiceContext)
                        contexts.get(UEInfoServiceContext.SERVICE_CONTEXT_ID);

                    if (usc != null) {
                        Throwable unknown = usc.getUE() ;
                        UnknownException ue = new UnknownException(unknown);

                        reportException( "NO remarshal: UserException available",
                            unknown );

                        // Invoke Portable Interceptors with receive_exception:
                        exception = orb.getPIHandler().invokeClientPIEndingPoint(
                            ReplyMessage.SYSTEM_EXCEPTION, ue );

                        reportException( "NO remarshal: UserException available: PI exception ",
                            exception );

                        continueOrThrowSystemOrRemarshal(messageMediator, exception);
                        throw wrapper.statementNotReachable3() ;
                    }
                }

                // It was not a comm failure nor unknown.
                // This is the general case.
                reportException( "general exception", se);

                // Invoke Portable Interceptors with receive_exception:
                exception = orb.getPIHandler().invokeClientPIEndingPoint(
                    ReplyMessage.SYSTEM_EXCEPTION, se );

                reportException( "general exception: PI exception", exception );

                continueOrThrowSystemOrRemarshal(messageMediator, exception);

                // Note: We should never need to execute this line, but
                // we should assert in case exception is null somehow.
                throw wrapper.statementNotReachable4() ;
            } else if (messageMediator.isUserExceptionReply()) {
                getContactInfoListIterator(orb)
                    .reportSuccess(messageMediator.getContactInfo());

                String exceptionRepoId = peekUserExceptionId(inputObject);
                receivedUserException(exceptionRepoId);

                Exception newException = null;

                if (messageMediator.isDIIRequest()) {
                    exception = messageMediator.unmarshalDIIUserException(
                                    exceptionRepoId, (InputStream)inputObject);
                    newException = orb.getPIHandler().invokeClientPIEndingPoint(
                                       ReplyMessage.USER_EXCEPTION, exception );
                    messageMediator.setDIIException(newException);

                    receivedUserExceptionDII(exception, newException);
                } else {
                    ApplicationException appException = new ApplicationException(
                        exceptionRepoId, (org.omg.CORBA.portable.InputStream)inputObject);

                    exception = appException;

                    newException = orb.getPIHandler().invokeClientPIEndingPoint(
                                       ReplyMessage.USER_EXCEPTION, appException );

                    receivedUserExceptionNotDII(exception, newException);
                }

                if (newException != exception) {
                    continueOrThrowSystemOrRemarshal(messageMediator,newException);
                }

                if (newException instanceof ApplicationException) {
                    throw (ApplicationException)newException;
                }
                // For DII: 
                // This return will be ignored - already unmarshaled above.
                return inputObject;

            } else if (messageMediator.isLocationForwardReply()) {
                generalMessage( "received location forward");
                
                // NOTE: Expects iterator to update target IOR
                getContactInfoListIterator(orb).reportRedirect(
                    messageMediator.getContactInfo(),
                    messageMediator.getForwardedIOR());

                // Invoke Portable Interceptors with receive_other:
                Exception newException = orb.getPIHandler().invokeClientPIEndingPoint(
                    ReplyMessage.LOCATION_FORWARD, null );

                if( !(newException instanceof RemarshalException) ) {
                    exception = newException;
                }

                // If PI did not change exception, throw Remarshal, else
                // throw the exception PI wants thrown.
                // KMC: GenericPOAClientSC did not check exception != null
                if( exception != null ) {
                    continueOrThrowSystemOrRemarshal(messageMediator, exception);
                }
                continueOrThrowSystemOrRemarshal(messageMediator,
                                                 new RemarshalException());
                throw wrapper.statementNotReachable5() ;

            } else if (messageMediator.isDifferentAddrDispositionRequestedReply()){
                generalMessage( "received different addressing dispostion request");

                // Set the desired target addressing disposition.
                getContactInfoListIterator(orb).reportAddrDispositionRetry(
                    messageMediator.getContactInfo(),
                    messageMediator.getAddrDispositionReply());

                // Invoke Portable Interceptors with receive_other:
                Exception newException = orb.getPIHandler().invokeClientPIEndingPoint(
                    ReplyMessage.NEEDS_ADDRESSING_MODE, null);

                // For consistency with corresponding code in GenericPOAClientSC:
                if( !(newException instanceof RemarshalException) ) {
                    exception = newException;
                }

                // If PI did not change exception, throw Remarshal, else
                // throw the exception PI wants thrown.
                // KMC: GenericPOAClientSC did not include exception != null check
                if( exception != null ) {
                    continueOrThrowSystemOrRemarshal(messageMediator, exception);
                }
                continueOrThrowSystemOrRemarshal(messageMediator,
                                                 new RemarshalException());
                throw wrapper.statementNotReachable6() ;
            } else /* normal response */ {
                generalMessage( "received normal response");

                getContactInfoListIterator(orb)
                    .reportSuccess(messageMediator.getContactInfo());

                messageMediator.handleDIIReply((InputStream)inputObject);

                // Invoke Portable Interceptors with receive_reply:
                exception = orb.getPIHandler().invokeClientPIEndingPoint(
                    ReplyMessage.NO_EXCEPTION, null );

                // Remember: not thrown if exception is null.
                continueOrThrowSystemOrRemarshal(messageMediator, exception);

                return inputObject;
            }
        } finally {
            exit_processResponse() ;
        }
    }

    // Filters the given exception into a SystemException or a
    // RemarshalException and throws it.  Assumes the given exception is
    // of one of these two types.  This is a utility method for
    // the above invoke code which must do this numerous times.
    // If the exception is null, no exception is thrown.
    // Note that this code is duplicated in GenericPOAClientSC.java
    @Subcontract
    protected void continueOrThrowSystemOrRemarshal(
        MessageMediator messageMediator, Exception exception)
        throws 
            SystemException, RemarshalException
    {
        final ORB orb = messageMediator.getBroker();

        if ( exception == null ) {
            // do nothing.
        } else if( exception instanceof RemarshalException ) {
            // REVISIT - unify with PI handling
            orb.getInvocationInfo().setIsRetryInvocation(true);

            // NOTE - We must unregister the waiter NOW for this request
            // since the retry will result in a new request id.  Therefore
            // the old request id would be lost and we would have a memory
            // leak in the responseWaitingRoom.
            unregisterWaiter(orb);
            throw (RemarshalException)exception;
        } else {
            throw (SystemException)exception;
        }
    }

    protected ContactInfoListIterator  getContactInfoListIterator(ORB orb) {
        return (ContactInfoListIterator) orb.getInvocationInfo().getContactInfoListIterator();
    }

    @Subcontract
    protected void registerWaiter(MessageMediator messageMediator) {
        if (messageMediator.getConnection() != null) {
            messageMediator.getConnection().registerWaiter(messageMediator);
        }
    }

    @Subcontract
    protected void unregisterWaiter(ORB orb) {
        MessageMediator messageMediator =
            orb.getInvocationInfo().getMessageMediator();
        if (messageMediator!=null && messageMediator.getConnection() != null) {
            // REVISIT:
            // The messageMediator may be null if COMM_FAILURE before
            // it is created.
            messageMediator.getConnection().unregisterWaiter(messageMediator);
        }
    }

    @Subcontract
    protected void addServiceContexts(MessageMediator messageMediator) {
        ORB orb = messageMediator.getBroker();
        Connection c = messageMediator.getConnection();
        GIOPVersion giopVersion = messageMediator.getGIOPVersion();

        ServiceContexts contexts = null;

        // If Java serialization is used.
        if (ORBUtility.getEncodingVersion() != ORBConstants.CDR_ENC_VERSION) {    
            contexts = messageMediator.getRequestServiceContexts();
            ORBVersionServiceContext lsc =
                ServiceContextDefaults.getORBVersionServiceContext() ;
            contexts.put(lsc);
            return;
        }

        if (c != null &&            
            giopVersion.equals(GIOPVersion.V1_2) && 
            c.getBroker().getORBData().alwaysSendCodeSetServiceContext()) {
            if (!c.isPostInitialContexts()) {          
                contexts = (messageMediator.getBroker()).
                                                getServiceContextsCache().get(
                                                ServiceContextsCache.CASE.CLIENT_INITIAL);
            } else {
                contexts = messageMediator.getBroker()
                    .getServiceContextsCache().get(
                        ServiceContextsCache.CASE.CLIENT_SUBSEQUENT);
            }

            addCodeSetServiceContext(c, contexts, giopVersion);

            messageMediator.setRequestServiceContexts(contexts);

        } else {
            contexts = messageMediator.getRequestServiceContexts();
        
            addCodeSetServiceContext(c, contexts, giopVersion);

            // Add the RMI-IIOP max stream format version
            // service context to every request.  Once we have GIOP 1.3,
            // we could skip it since we now support version 2, but
            // probably safer to always send it.
            
            contexts.put( msfvc );

            // ORBVersion servicecontext needs to be sent

            contexts.put( ovsc ) ;

            // NOTE : We only want to send the runtime context the first time
            if ((c != null) && !c.isPostInitialContexts()) {
                // Do not do c.setPostInitialContexts() here.
                // If a client interceptor send_request does a ForwardRequest
                // which ends up using the same connection then the service
                // context would not be sent.
                SendingContextServiceContext scsc =
                  ServiceContextDefaults.makeSendingContextServiceContext(
                                                    orb.getFVDCodeBaseIOR() ) ; 
                contexts.put( scsc ) ;
            }
        }    
    }

    @Subcontract
    protected void consumeServiceContexts(ORB orb, 
                                        MessageMediator messageMediator)
    {
        ServiceContexts ctxts = messageMediator.getReplyServiceContexts();
        ServiceContext sc ;

        if (ctxts == null) {
            return; // no service context available, return gracefully.
        }

        sc = ctxts.get( SendingContextServiceContext.SERVICE_CONTEXT_ID ) ;

        if (sc != null) {
            SendingContextServiceContext scsc =
                (SendingContextServiceContext)sc ;
            IOR ior = scsc.getIOR() ;

            try {
                // set the codebase returned by the server
                if (messageMediator.getConnection() != null) {
                    messageMediator.getConnection().setCodeBaseIOR(ior);
                }
            } catch (ThreadDeath td) {
                throw td ;
            } catch (Throwable t) {
                throw wrapper.badStringifiedIor( t ) ;
            }
        } 

        // see if the version subcontract is present, if yes, then set
        // the ORBversion
        sc = ctxts.get( ORBVersionServiceContext.SERVICE_CONTEXT_ID ) ;

        if (sc != null) {
            ORBVersionServiceContext lsc =
               (ORBVersionServiceContext) sc;

            ORBVersion version = lsc.getVersion();
            orb.setORBVersion( version ) ;
        }

        getExceptionDetailMessage(messageMediator, wrapper);
    }

    @Subcontract
    protected void getExceptionDetailMessage(
        MessageMediator  messageMediator,
        ORBUtilSystemException wrapper)
    {
        ServiceContext sc = messageMediator.getReplyServiceContexts()
            .get(ExceptionDetailMessage.value);
        if (sc == null) {
            return;
        }

        if (! (sc instanceof UnknownServiceContext)) {
            throw wrapper.badExceptionDetailMessageServiceContextType();
        }
        byte[] data = ((UnknownServiceContext)sc).getData();
        EncapsInputStream in = new EncapsInputStream(messageMediator.getBroker(), data, data.length);
        in.consumeEndian();

        String msg =
              "----------BEGIN server-side stack trace----------\n"
            + in.read_wstring() + "\n"
            + "----------END server-side stack trace----------";

        messageMediator.setReplyExceptionDetailMessage(msg);
    }

    @Subcontract
    public void endRequest(ORB orb, Object self, CDRInputObject inputObject)
    {
        try {
            exit_clientDecoding();

            // Note: the inputObject may be null if an error occurs
            //       in request or before _invoke returns.
            // Note: self may be null also (e.g., compiler generates null in stub).

            MessageMediator messageMediator =
                orb.getInvocationInfo().getMessageMediator();
            if (messageMediator != null) {
                ORBUtility.popEncVersionFromThreadLocalState();

                if (messageMediator.getConnection() != null) {
                    messageMediator.sendCancelRequestIfFinalFragmentNotSent();
                }

                // Release any outstanding NIO ByteBuffers to the ByteBufferPool

                CDRInputObject inputObj = messageMediator.getInputObject();
                if (inputObj != null) {
                    inputObj.close();
                }

                CDROutputObject outputObj = messageMediator.getOutputObject();
                if (outputObj != null) {
                    outputObj.close();
                }

                // TODO NEW CACHE: release( conn ) : conn is in MessageMediator
            }

            // XREVISIT NOTE - Assumes unregistering the waiter for
            // location forwards has already happened somewhere else.
            // The code below is only going to unregister the final successful
            // request. 

            // NOTE: In the case of a recursive stack of endRequests in a
            // finally block (because of Remarshal) only the first call to
            // unregisterWaiter will remove the waiter.  The rest will be
            // noops.
            unregisterWaiter(orb);

            // Invoke Portable Interceptors cleanup.  This is done to handle
            // exceptions during stream marshaling.  More generally, exceptions
            // that occur in the ORB after send_request (which includes
            // after returning from _request) before _invoke:
            orb.getPIHandler().cleanupClientPIRequest();
            
            // REVISIT: Early replies?
        } catch (IOException ex) { 
            // See CDRInput/OutputObject.close() for more info.
            // This won't result in a Corba error if an IOException happens.
            reportException("ignoring IOException", ex );
        } finally {
            exit_totalRequest() ;
        }
    }

    @Subcontract
    protected void performCodeSetNegotiation(MessageMediator messageMediator) {
        Connection conn = messageMediator.getConnection();
        if (conn == null) {
            return;
        }

        GIOPVersion giopVersion = messageMediator.getGIOPVersion();
        if (giopVersion.equals(GIOPVersion.V1_0)) {
            return;
        }

        IOR ior = (messageMediator.getContactInfo()).getEffectiveTargetIOR();

        synchronized( conn ) {
            if (conn.getCodeSetContext() != null) {
                return;
            }
            
            // This only looks at the first code set component.  If
            // there can be multiple locations with multiple code sets,
            // this requires more work.
            IIOPProfileTemplate temp = (IIOPProfileTemplate)ior.getProfile().
                getTaggedProfileTemplate();
            Iterator iter = temp.iteratorById(TAG_CODE_SETS.value);
            if (!iter.hasNext()) {
                // Didn't have a code set component.  The default will
                // be to use ISO8859-1 for char data and throw an
                // exception if wchar data is used.
                return;
            }

            // Get the native and conversion code sets the
            // server specified in its IOR
            CodeSetComponentInfo serverCodeSets
                = ((CodeSetsComponent)iter.next()).getCodeSetComponentInfo();

            // Perform the negotiation between this ORB's code sets and
            // the ones from the IOR
            CodeSetComponentInfo.CodeSetContext result
                = CodeSetConversion.impl().negotiate(
                      conn.getBroker().getORBData().getCodeSetComponentInfo(),
                      serverCodeSets);
            
            conn.setCodeSetContext(result);
        }
    }

    @Subcontract
    protected void addCodeSetServiceContext(Connection conn,
        ServiceContexts ctxs, GIOPVersion giopVersion) {

        // REVISIT.  OMG issue 3318 concerning sending the code set
        // service context more than once was deemed too much for the
        // RTF.  Here's our strategy for the moment:
        //
        // Send it on every request (necessary in cases of fragmentation
        // with multithreaded clients or when the first thing on a
        // connection is a LocateRequest).  Provide an ORB property
        // to disable multiple sends.
        //
        // Note that the connection is null in the local case and no
        // service context is included.  We use the ORB provided
        // encapsulation streams.
        //
        // Also, there will be no negotiation or service context
        // in GIOP 1.0.  ISO8859-1 is used for char/string, and
        // wchar/wstring are illegal.
        //
        if (giopVersion.equals(GIOPVersion.V1_0) || conn == null) {
            return;
        }
        
        CodeSetComponentInfo.CodeSetContext codeSetCtx = null;

        if (conn.getBroker().getORBData().alwaysSendCodeSetServiceContext() ||
            !conn.isPostInitialContexts()) {

            // Get the negotiated code sets (if any) out of the connection
            codeSetCtx = conn.getCodeSetContext();
        }
        
        // Either we shouldn't send the code set service context, or
        // for some reason, the connection doesn't have its code sets.
        // Perhaps the server didn't include them in the IOR.  Uses
        // ISO8859-1 for char and makes wchar/wstring illegal.
        if (codeSetCtx == null) {
            return;
        }

        CodeSetServiceContext cssc = 
            ServiceContextDefaults.makeCodeSetServiceContext(codeSetCtx);
        ctxs.put(cssc);
    }    

    @Subcontract
    protected String peekUserExceptionId(CDRInputObject inputObject) {
        CDRInputObject cdrInputObject = inputObject;
        // REVISIT - need interface for mark/reset
        cdrInputObject.mark(Integer.MAX_VALUE);
        String result = cdrInputObject.read_string();
        cdrInputObject.reset();
        return result;
    }                     
}

// End of file.
