/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.corba;

import com.sun.corba.ee.spi.orb.ORB ;

///////////////////////////////////////////////////////////////////////////
// helper class for deferred invocations

/*
 * The AsynchInvoke class allows for the support of asynchronous
 * invocations. Instances of these are created with a request object,
 * and when run, perform an invocation. The instance is also
 * responsible for waking up a client that might be waiting on the
 * 'get_response' method.
 */

public class AsynchInvoke implements Runnable {

    private final RequestImpl _req;
    private final ORB         _orb;
    private final boolean     _notifyORB;

    public AsynchInvoke (ORB o, RequestImpl reqToInvokeOn, boolean n)
    {
        _orb = o;
        _req = reqToInvokeOn;
        _notifyORB = n;
    };


    /*
     * The run operation calls the invocation on the request object,
     * updates the RequestImpl state to indicate that the asynchronous
     * invocation is complete, and wakes up any client that might be
     * waiting on a 'get_response' call.
     *
     */

    public void run() 
    {
        synchronized (_req) {
            // do the actual invocation
            _req.doInvocation();
        }
    
        // for the asynchronous case, note that the response has been
        // received.
        synchronized (_req) {
            // update local boolean indicator
            _req.gotResponse = true;

            // notify any client waiting on a 'get_response'
            _req.notify();
        }
      
        if (_notifyORB == true) {
            _orb.notifyORB() ;
        }
    }

};

///////////////////////////////////////////////////////////////////////////
