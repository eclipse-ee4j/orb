/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.txpoa;

import org.omg.CORBA.TSIdentification;

public class TSIdentificationImpl extends org.omg.CORBA.LocalObject implements TSIdentification {

    private org.omg.CosTSPortability.Sender sender = null;
    private org.omg.CosTSPortability.Receiver receiver = null;

    /**
     * identify_sender is called by the OTS during initialization to register its Sender callback interface with the ORB.
     * identify_sender may throw a AlreadyIdentified exception if the registration has already been done previously.
     */
    public void identify_sender(org.omg.CosTSPortability.Sender senderOTS)
            throws org.omg.CORBA.TSIdentificationPackage.NotAvailable, org.omg.CORBA.TSIdentificationPackage.AlreadyIdentified {
        if (sender == null)
            sender = senderOTS;
        else
            throw new org.omg.CORBA.TSIdentificationPackage.AlreadyIdentified();
    }

    /**
     * identify_receiver is called by the OTS during initialization to register its Receiver callback interface with the
     * ORB. identify_receiver may throw a AlreadyIdentified exception if the registration has already been done previously.
     */
    public void identify_receiver(org.omg.CosTSPortability.Receiver receiverOTS)
            throws org.omg.CORBA.TSIdentificationPackage.NotAvailable, org.omg.CORBA.TSIdentificationPackage.AlreadyIdentified {
        if (receiver == null)
            receiver = receiverOTS;
        else
            throw new org.omg.CORBA.TSIdentificationPackage.AlreadyIdentified();
    }

    /**
     * getSender is not defined in the OTS spec. It is just a convenience method to allow the ORB to access the Sender
     * subsequent to initialization.
     */
    public org.omg.CosTSPortability.Sender getSender() {
        return sender;
    }

    /**
     * getReceiver is not defined in the OTS spec. It is just a convenience method to allow the ORB to access the Receiver
     * subsequent to initialization.
     */
    public org.omg.CosTSPortability.Receiver getReceiver() {
        return receiver;
    }
}
