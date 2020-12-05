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

package com.sun.corba.ee.spi.protocol;

import com.sun.corba.ee.impl.encoding.CDRInputObject;
import com.sun.corba.ee.impl.encoding.CDROutputObject;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.transport.ContactInfo;

/**
 * <code>ClientRequestDispatcher</code> coordinates the request (and possible
 * response) processing for a specific <em>protocol</em>.
 *
 * @author Harold Carr
 */
public interface ClientRequestDispatcher
{
    /**
     * At the beginning of a request the presentation block uses this
     * to obtain an OutputObject to set data to be sent on a message.
     *
     * @param self -
     * @param methodName - the remote method name
     * @param isOneWay - <code>true</code> if the message is asynchronous
     * @param contactInfo - the CorbaContactInfo
     * which which created/chose this <code>ClientRequestDispatcher</code>
     *
     * @return OutputObject
     */
    public CDROutputObject beginRequest(Object self,
                                     String methodName,
                                     boolean isOneWay,
                                     ContactInfo contactInfo);

    /**
     * After the presentation block has set data on the CDROutputObject
     * it signals the PEPt runtime to send the encoded data by calling this
     * method.
     *
     * @param self -
     * @param outputObject object to mark as complete
     *
     * @return CDRInputObject if the message is synchronous.
     *
     * @throws org.omg.CORBA.portable.ApplicationException 
     * if the remote side raises an exception declared in the remote interface.
     * 
     * @throws org.omg.CORBA.portable.RemarshalException RemarshalException
     * if the PEPt runtime would like the presentation block to start over.
     */
    public CDRInputObject marshalingComplete(java.lang.Object self,
                                          CDROutputObject outputObject)
    // REVISIT EXCEPTIONS
        throws
            org.omg.CORBA.portable.ApplicationException, 
            org.omg.CORBA.portable.RemarshalException;

    /**
     * After the presentation block completes a request it signals
     * the PEPt runtime by calling this method.
     *
     * This method may release resources.  In some cases it may cause
     * control or error messages to be sent.
     *
     * @param broker ORB used
     * @param self unused
     * @param inputObject unused
     */
    public void endRequest(ORB broker,
                           java.lang.Object self, 
                           CDRInputObject inputObject);
}

// End of file.
