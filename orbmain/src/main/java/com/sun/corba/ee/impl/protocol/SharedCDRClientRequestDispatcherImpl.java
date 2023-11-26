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

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.omg.CORBA.portable.ApplicationException;

import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.protocol.MessageMediator;

import com.sun.corba.ee.impl.encoding.CDRInputObject;
import com.sun.corba.ee.impl.encoding.CDROutputObject;
import com.sun.corba.ee.spi.trace.Subcontract;
import org.glassfish.pfl.tf.spi.annotation.InfoMethod;

/**
 * ClientDelegate is the RMI client-side subcontract or representation It implements RMI delegate as well as our
 * internal ClientRequestDispatcher interface.
 */
@Subcontract
public class SharedCDRClientRequestDispatcherImpl extends ClientRequestDispatcherImpl {

    @InfoMethod
    private void operationAndId(String msg, int rid) {
    }

    // REVISIT:
    // Rather than have separate CDR subcontract,
    // use same CorbaClientRequestDispatcherImpl but have
    // different MessageMediator finishSendingRequest and waitForResponse
    // handle what is done below.
    // Benefit: then in ContactInfo no need to do a direct new
    // of subcontract - does not complicate subcontract registry.

    @Override
    @Subcontract
    public CDRInputObject marshalingComplete(java.lang.Object self, CDROutputObject outputObject)
            throws ApplicationException, org.omg.CORBA.portable.RemarshalException {
        MessageMediator messageMediator = null;
        messageMediator = (MessageMediator) outputObject.getMessageMediator();
        operationAndId(messageMediator.getOperationName(), messageMediator.getRequestId());
        final ORB orb = (ORB) messageMediator.getBroker();
        operationAndId(messageMediator.getOperationName(), messageMediator.getRequestId());

        CDROutputObject cdrOutputObject = outputObject;
        final CDROutputObject fCDROutputObject = cdrOutputObject;

        //
        // Create server-side input object.
        //

        CDRInputObject cdrInputObject = AccessController.doPrivileged(new PrivilegedAction<CDRInputObject>() {
            @Override
            public CDRInputObject run() {
                return fCDROutputObject.createInputObject(orb);
            }
        });

        messageMediator.setInputObject(cdrInputObject);
        cdrInputObject.setMessageMediator(messageMediator);

        //
        // Dispatch
        //

        // REVISIT: Impl cast.
        ((MessageMediatorImpl) messageMediator).handleRequestRequest(messageMediator);

        // InputStream must be closed on the InputObject so that its
        // ByteBuffer can be released to the ByteBufferPool. We must do
        // this before we re-assign the cdrInputObject reference below.
        try {
            cdrInputObject.close();
        } catch (IOException ex) {
            // No need to do anything since we're done with the input stream
            // and cdrInputObject will be re-assigned a new client-side input
            // object, (i.e. won't result in a corba error).
            // XXX log this
        }

        //
        // Create client-side input object
        //

        cdrOutputObject = messageMediator.getOutputObject();
        final CDROutputObject fCDROutputObject2 = cdrOutputObject;
        cdrInputObject = AccessController.doPrivileged(new PrivilegedAction<CDRInputObject>() {

            @Override
            public CDRInputObject run() {
                // TODO Auto-generated method stub
                return fCDROutputObject2.createInputObject(orb);
            }

        });
        messageMediator.setInputObject(cdrInputObject);
        cdrInputObject.setMessageMediator(messageMediator);

        cdrInputObject.unmarshalHeader();

        CDRInputObject inputObject = cdrInputObject;

        return processResponse(orb, messageMediator, inputObject);
    }

}

// End of file.
