/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.protocol;

import com.sun.corba.ee.spi.protocol.RequestId;
import com.sun.corba.ee.spi.logging.ORBUtilSystemException;

/**
 * Represents a protocol request id.  Currently used to ensure proper
 * sequencing of fragmented messages.
 *
 * @author Charlie Hunt
 */
public class RequestIdImpl implements RequestId {
    private static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    final private int value;
    final private boolean defined;
    final static private String UNDEFINED = "?";
    final static public 
            RequestId UNKNOWN_CORBA_REQUEST_ID = new RequestIdImpl();

    /** Creates a new instance of CorbaRequestIdImpl
     * @param requestId value of the request ID
     */
    public RequestIdImpl(int requestId) {
        this.value = requestId;
        this.defined = true;
    }

    /** Creates a new instance of CorbaRequestIdImpl */
    private RequestIdImpl() {
        this.defined = false;
        // initialize value, but note it is a meaningless value
        // and should not be used via getValue()!
        this.value = -1;
    }

    /** Return the value of this CorbaRequestId */
    public int getValue() {
        if (defined) {
            return this.value;
        } else {
            throw wrapper.undefinedCorbaRequestIdNotAllowed();
        }
    }

    /** Is there a numeric identifier for this CorbaRequestId ? */
    public boolean isDefined() {
        return defined;
    }

    /** Does this CorbaRequestId equal another CorbaRequestId ? */
    @Override
    public boolean equals(Object requestId) {

        if (requestId == null || !(requestId instanceof RequestId)) {
            return false;
        }
        
        if (this.isDefined()) {
            if (((RequestId)requestId).isDefined()) {
                return this.value == ((RequestId)requestId).getValue();
            } else { // requestId is not defined and "this" is defined
                return false;
            }
        } else {
            // "this" is not defined
            // simply return result of NOT requestId.isDefined()
            return !((RequestId)requestId).isDefined();
        }
    }

    /** Return this CorbaRequestId's hashCode */
    @Override
    public int hashCode() {
        return this.value;
    }
    
    /** String representing this CorbaRequestId */
    @Override
    public String toString() {
        if (defined) {
            return Integer.toString(this.value);
        } else {
            return UNDEFINED;
        }
    }
}
