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

