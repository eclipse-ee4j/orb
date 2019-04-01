/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.protocol;

import com.sun.corba.ee.impl.encoding.CDRInputObject;
import com.sun.corba.ee.impl.encoding.CDROutputObject;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.transport.ContactInfo;

/**
 * <code>ClientRequestDispatcher</code> coordinates the request (and possible response) processing for a specific
 * <em>protocol</em>.
 *
 * @author Harold Carr
 */
public interface ClientRequestDispatcher {
    /**
     * At the beginning of a request the presentation block uses this to obtain an OutputObject to set data to be sent on a
     * message.
     *
     * @param self -
     * @param methodName - the remote method name
     * @param isOneWay - <code>true</code> if the message is asynchronous
     * @param contactInfo - the CorbaContactInfo which which created/chose this <code>ClientRequestDispatcher</code>
     *
     * @return OutputObject
     */
    public CDROutputObject beginRequest(Object self, String methodName, boolean isOneWay, ContactInfo contactInfo);

    /**
     * After the presentation block has set data on the CDROutputObject it signals the PEPt runtime to send the encoded data
     * by calling this method.
     *
     * @param self -
     * @param outputObject
     *
     * @return CDRInputObject if the message is synchronous.
     *
     * @throws {
     * @link org.omg.CORBA.portable.ApplicationException ApplicationException} if the remote side raises an exception
     * declared in the remote interface.
     *
     * @throws {
     * @link org.omg.CORBA.portable.RemarshalException RemarshalException} if the PEPt runtime would like the presentation
     * block to start over.
     */
    public CDRInputObject marshalingComplete(java.lang.Object self, CDROutputObject outputObject)
            // REVISIT EXCEPTIONS
            throws org.omg.CORBA.portable.ApplicationException, org.omg.CORBA.portable.RemarshalException;

    /**
     * After the presentation block completes a request it signals the PEPt runtime by calling this method.
     *
     * This method may release resources. In some cases it may cause control or error messages to be sent.
     *
     * @param broker -
     * @param inputObject -
     */
    public void endRequest(ORB broker, java.lang.Object self, CDRInputObject inputObject);
}

// End of file.
