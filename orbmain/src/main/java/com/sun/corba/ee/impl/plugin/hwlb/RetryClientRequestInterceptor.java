/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

//
// Created       : 2005 Oct 05 (Wed) 14:28:37 by Harold Carr.
// Last Modified : 2005 Oct 19 (Wed) 14:01:31 by Harold Carr.
//

package com.sun.corba.ee.impl.plugin.hwlb ;

import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.ORBInitializer;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName;
import org.omg.CORBA.SystemException;
import com.sun.corba.ee.spi.logging.ORBUtilSystemException;
import com.sun.corba.ee.impl.misc.ORBUtility;

public class RetryClientRequestInterceptor
    extends org.omg.CORBA.LocalObject
    implements ORBInitializer, ClientRequestInterceptor
{
    private static final String baseMsg = 
        RetryClientRequestInterceptor.class.getName();

    private static final String TRANSIENT_REPOSITORY_ID =
        "IDL:omg.org/CORBA/TRANSIENT:1.0";

    private static final String OBJECT_NOT_EXIST_REPOSITORY_ID =
        "IDL:omg.org/CORBA/OBJECT_NOT_EXIST:1.0";

    // The logic causes the initial value to be doubled each time
    // it is used, including the first time.  So the first sleep
    // will be 2 * initialBackoff.
    private static final long INITIAL_BACKOFF_DEFAULT = 500; // 1/2 second
    private static long initialBackoff = INITIAL_BACKOFF_DEFAULT;

    private static final long TRANSIENT_RETRY_TIMEOUT_DEFAULT =
        1000 * 60 * 5; // 5 minutes
    private static long transientRetryTimeout =
        TRANSIENT_RETRY_TIMEOUT_DEFAULT;

    private static boolean debug = true;

    private static class BackoffAndStartTime {
        public long startTime;
        public long backoff;
        BackoffAndStartTime() {
            backoff = initialBackoff;
        }
    }

    // NOTE: Cannot use slots since they are reset on retry.
    private ThreadLocal backoffAndStartTime =
        new ThreadLocal() {
            protected Object initialValue() {
                return new BackoffAndStartTime();
            }
        };

    private long getStartTime() {
        return ((BackoffAndStartTime)backoffAndStartTime.get()).startTime;
    }

    private void setStartTime(long x) {
        ((BackoffAndStartTime)backoffAndStartTime.get()).startTime = x;
    }

    private long getBackoff() {
        return ((BackoffAndStartTime)backoffAndStartTime.get()).backoff;
    }

    private void setBackoff(long x) {
        ((BackoffAndStartTime)backoffAndStartTime.get()).backoff = x;
    }

    private void doubleBackoff() {
        setBackoff(getBackoff() * 2);
    }

    ////////////////////////////////////////////////////
    //
    // Application specific
    //

    public static void setInitialBackoff(long x) {
        initialBackoff = x;
    }

    public static long getInitialBackoff() {
        return initialBackoff;
    }

    public static void setTransientRetryTimeout(long x) {
        transientRetryTimeout = x;
    }

    public static long getTransientRetryTimeout() {
        return transientRetryTimeout;
    }

    public static void setDebug(boolean x) {
        debug = x;
    }

    ////////////////////////////////////////////////////
    //
    // Interceptor operations
    //

    public String name() 
    {
        return baseMsg; 
    }

    public void destroy() 
    {
    }

    ////////////////////////////////////////////////////
    //
    // ClientRequestInterceptor
    //

    public void send_request(ClientRequestInfo ri)
    {
    }

    public void send_poll(ClientRequestInfo ri)
    {
    }

    public void receive_reply(ClientRequestInfo ri)
    {
        setBackoff(initialBackoff);
    }

    public void receive_exception(ClientRequestInfo ri)
        throws ForwardRequest
    {
        if (! (isTransientException(ri) || isBadServerIdException(ri))) {
            setBackoff(initialBackoff);
            return;
        }

        String msg = 
            ".receive_exception:" 
            + " " + ri.received_exception_id()
            + " " + ri.operation()
            + ": ";

        if (getBackoff() == initialBackoff) {

            if (debug) {
                System.out.println(msg + "initializing timer");
            }

            setStartTime(System.currentTimeMillis());
            
        } else if (System.currentTimeMillis() - getStartTime() 
                   >= transientRetryTimeout) {

            if (debug) {
                System.out.println(msg
                                   + "exceeded transientRetryTimeout: "
                                   + transientRetryTimeout
                                   + " - not retrying");
            }

            return;
        }

        doubleBackoff();

        if (debug) {
            System.out.println(msg + "sleep: " + getBackoff());
        }
        try {
            Thread.sleep(getBackoff());
        } catch (InterruptedException e) {
            // Ignore
        }
        if (debug) {
            System.out.println(msg + "done sleeping");
        }
        if (isTransientException(ri)) {
            throw new ForwardRequest(ri.effective_target());
        } else if (isBadServerIdException(ri)) {
            throw new ForwardRequest(ri.target());
        } else {
            if (debug) {
                System.out.println(msg + "unexpected: " 
                                   + ri.received_exception_id());
            }
        }
    }

    public void receive_other(ClientRequestInfo ri)
    {
        setBackoff(initialBackoff);
    }

    ////////////////////////////////////////////////////
    //
    // ORBInitializer
    //

    public void pre_init(ORBInitInfo info) 
    {
    }

    public void post_init(ORBInitInfo info) 
    {
        try {
            if (debug) {
                System.out.println(".post_init: registering: " + this);
            }
            info.add_client_request_interceptor(this);
        } catch (DuplicateName e) {
            // REVISIT - LOG AND EXIT
            if (debug) {
                System.out.println(".post_init: exception: " + e);
            }
        }
    }

    //////////////////////////////////////////////////
    //
    // Implementation
    //

    private boolean isTransientException(ClientRequestInfo ri)
    {
        return ri.received_exception_id().equals(TRANSIENT_REPOSITORY_ID);
    }

    private boolean isBadServerIdException(ClientRequestInfo ri)
    {
        if (! ri.received_exception_id().equals(OBJECT_NOT_EXIST_REPOSITORY_ID)) {
            return false;
        }

        SystemException se = 
            ORBUtility.extractSystemException(ri.received_exception());

        return 
            se instanceof org.omg.CORBA.OBJECT_NOT_EXIST
            && se.minor == ORBUtilSystemException.BAD_SERVER_ID;
    }
}

// End of file.

