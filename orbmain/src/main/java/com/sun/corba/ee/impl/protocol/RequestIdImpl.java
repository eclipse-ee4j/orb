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

package com.sun.corba.ee.impl.protocol;

import com.sun.corba.ee.spi.logging.ORBUtilSystemException;
import com.sun.corba.ee.spi.protocol.RequestId;

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
