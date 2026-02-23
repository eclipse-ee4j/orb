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

package com.sun.corba.ee.impl.transport;

import com.sun.corba.ee.impl.encoding.CDRInputObject;
import com.sun.corba.ee.impl.misc.ORBUtility;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.LocateReplyOrReplyMessage;
import com.sun.corba.ee.spi.logging.ORBUtilSystemException;
import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.protocol.MessageMediator;
import com.sun.corba.ee.spi.trace.Transport;
import com.sun.corba.ee.spi.transport.Connection;
import com.sun.corba.ee.spi.transport.ResponseWaitingRoom;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.glassfish.pfl.tf.spi.annotation.InfoMethod;
import org.omg.CORBA.SystemException;

/**
 * @author Harold Carr
 */
@Transport
public class ResponseWaitingRoomImpl
    implements
        ResponseWaitingRoom
{
    final private static ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    final static class OutCallDesc
    {
        MessageMediator messageMediator;
        SystemException exception;
        CDRInputObject inputObject;
        ReentrantLock lock = new ReentrantLock();
        Condition condition = lock.newCondition();
    }

    // Maps requestId to an OutCallDesc.
    final private Map<Integer, OutCallDesc> out_calls;
    final private ORB orb;
    final private Connection connection;


    public ResponseWaitingRoomImpl(ORB orb, Connection connection)
    {
        this.orb = orb;
        this.connection = connection;
        this.out_calls = 
               Collections.synchronizedMap(new HashMap<Integer, OutCallDesc>());
    }

    @Transport
    public void registerWaiter(MessageMediator messageMediator)
    {
        display( "messageMediator request ID",
            messageMediator.getRequestId() ) ;
        display( "messageMediator operation name",
            messageMediator.getOperationName() ) ;

        Integer requestId = messageMediator.getRequestId();
        
        OutCallDesc call = new OutCallDesc();
        call.messageMediator = messageMediator;
        OutCallDesc exists = out_calls.put(requestId, call);
        if (exists != null) {
            wrapper.duplicateRequestIdsInResponseWaitingRoom(
                       ORBUtility.operationNameAndRequestId(
                           (MessageMediator)exists.messageMediator),
                       ORBUtility.operationNameAndRequestId(messageMediator));
        }
    }

    @Transport
    public void unregisterWaiter(MessageMediator mediator)
    {
        MessageMediator messageMediator = mediator;
        display( "messageMediator request ID",
            messageMediator.getRequestId() ) ;
        display( "messageMediator operation name",
            messageMediator.getOperationName() ) ;

        Integer requestId = messageMediator.getRequestId();

        out_calls.remove(requestId);
    }

    @Transport
    public CDRInputObject waitForResponse(MessageMediator messageMediator) {
        CDRInputObject returnStream = null;
        
        display( "messageMediator request ID",
            messageMediator.getRequestId() ) ;
        display( "messageMediator operation name",
            messageMediator.getOperationName() ) ;
        
        Integer requestId = messageMediator.getRequestId();
        
        if (messageMediator.isOneWay()) {
            // The waiter is removed in releaseReply in the same
            // way as a normal request.
            display( "Oneway request: not waiting") ;
            return null;
        }
        
        OutCallDesc call = out_calls.get(requestId);
        if (call == null) {
            throw wrapper.nullOutCall() ;
        }

        // Value from ORBData is in milliseconds, will convert it nanoseconds
        // to use it with Condition.awaitNanos()
        long waitForResponseTimeout =
                orb.getORBData().getWaitForResponseTimeout() * 1000 * 1000;
        
        try {
            call.lock.lock();
            while (call.inputObject == null && call.exception == null) {
                // Wait for the reply from the server.
                // The ReaderThread reads in the reply IIOP message
                // and signals us.
                try {
                    display( "Waiting for response..." ) ;
                    
                    waitForResponseTimeout =
                            call.condition.awaitNanos(waitForResponseTimeout);
                    if (call.inputObject == null && call.exception == null) {
                        if (waitForResponseTimeout > 0) {
                            // it's a "spurious wait wakeup", need to
                            // continue to wait for a response
                            display( "Spurious wakeup, continuing to wait for ",
                                waitForResponseTimeout/1000000 );
                        } else {
                            // timed out waiting for data
                            call.exception =
                                wrapper.communicationsTimeoutWaitingForResponse(
                                orb.getORBData().getWaitForResponseTimeout());
                            // REVISIT:
                            // Normally the inputObject or exception is
                            // created from the response stream.
                            // Need to fake encoding version since
                            // it is expected to be popped in endRequest.
                            ORBUtility.pushEncVersionToThreadLocalState(
                                    ORBConstants.JAVA_ENC_VERSION);
                        }
                    }
                } catch (InterruptedException ie) {};
            }
            if (call.exception != null) {
                display( "Exception from call", call.exception ) ;
                throw call.exception;
            }
            
            returnStream = call.inputObject;
        } finally {
            call.lock.unlock();
        }
        
        // REVISIT -- exceptions from unmarshaling code will
        // go up through this client thread!
        
        if (returnStream != null) {
            // On fragmented streams the header MUST be unmarshaled here
            // (in the client thread) in case it blocks.
            // If the header was already unmarshaled, this won't
            // do anything
            // REVISIT: cast - need interface method.
            ((CDRInputObject)returnStream).unmarshalHeader();
        }
        
        return returnStream;
    }

    @InfoMethod
    private void display( String msg ) { }

    @InfoMethod
    private void display( String msg, int value ) { }

    @InfoMethod
    private void display( String msg, Object value ) { }

    @Transport
    public void responseReceived(CDRInputObject is)
    {
        CDRInputObject inputObject = (CDRInputObject) is;
        LocateReplyOrReplyMessage header = (LocateReplyOrReplyMessage)
            inputObject.getMessageHeader();
        display( "requestId", header.getRequestId()) ;
        display( "header", header ) ;

        OutCallDesc call = out_calls.get(header.getRequestId());

        // This is an interesting case.  It could mean that someone sent us a
        // reply message, but we don't know what request it was for.  That
        // would probably call for an error.  However, there's another case
        // that's normal and we should think about --
        //
        // If the unmarshaling thread does all of its work inbetween the time
        // the ReaderThread gives it the last fragment and gets to the
        // out_calls.get line, then it will also be null, so just return;
        if (call == null) {
            display( "No waiter" ) ;
            return;
        }

        // Set the reply InputObject and signal the client thread
        // that the reply has been received.
        // The thread signalled will remove outcall descriptor if appropriate.
        // Otherwise, it'll be removed when last fragment for it has been put on
        // BufferManagerRead's queue.
        
        try {
            call.lock.lock();
            MessageMediator messageMediator =
                           (MessageMediator)call.messageMediator;

            display( "Notifying waiters") ;
            display( "messageMediator request ID",
                messageMediator.getRequestId() ) ;
            display( "messageMediator operation name",
                messageMediator.getOperationName() ) ;

            messageMediator.setReplyHeader(header);
            messageMediator.setInputObject(is);
            inputObject.setMessageMediator(messageMediator);
            call.inputObject = is;
            call.condition.signal();
        } finally {
            call.lock.unlock();
        }
    }

    public int numberRegistered()
    {
        return out_calls.size();
    }

    //////////////////////////////////////////////////
    //
    // CorbaResponseWaitingRoom
    //

    @Transport
    public void signalExceptionToAllWaiters(SystemException systemException) {
        OutCallDesc call;
        synchronized (out_calls) {
            Iterator<OutCallDesc> itr = out_calls.values().iterator();
            while (itr.hasNext()) {
                call = itr.next();
                try {
                    call.lock.lock();
                    ((MessageMediator)call.messageMediator).cancelRequest();
                    call.inputObject = null;
                    call.exception = systemException;
                    call.condition.signal();
                } finally {
                    call.lock.unlock();
                }
            }
        }
    }

    public MessageMediator getMessageMediator(int requestId)
    {
        OutCallDesc call = out_calls.get(requestId);
        if (call == null) {
            // This can happen when getting early reply fragments for a
            // request which has completed (e.g., client marshaling error).
            return null;
        }
        return call.messageMediator;
    }
}

// End of file.
