/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.protocol ;

// Introduce more information about WHY we are re-trying a request
// so we can properly handle the two cases:
// - BEFORE_RESPONSE means that the retry is caused by 
//   something that happened BEFORE the message was sent: either 
//   an exception from the SocketFactory, or one from the 
//   Client side send_request interceptor point.
// - AFTER_RESPONSE means that the retry is a result either of the
//   request sent to the server (from the response), or from the
//   Client side receive_xxx interceptor point.
public enum RetryType { 
    NONE( false ),
    BEFORE_RESPONSE( true ),
    AFTER_RESPONSE( true ) ;

    private final boolean isRetry ;

    RetryType( boolean isRetry ) {
        this.isRetry = isRetry ;
    }

    public boolean isRetry() {
        return this.isRetry ;
    }
} ;

