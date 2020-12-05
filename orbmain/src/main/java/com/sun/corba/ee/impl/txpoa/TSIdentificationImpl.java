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

package com.sun.corba.ee.impl.txpoa;

import org.omg.CORBA.TSIdentification;

public class TSIdentificationImpl extends org.omg.CORBA.LocalObject
        implements TSIdentification {

    private org.omg.CosTSPortability.Sender sender=null;
    private org.omg.CosTSPortability.Receiver receiver=null;

    /** identify_sender is called by the OTS during initialization
        to register its Sender callback interface with the ORB.
        identify_sender may throw a AlreadyIdentified exception if
        the registration has already been done previously.
    */
    public void
        identify_sender(org.omg.CosTSPortability.Sender senderOTS)
        throws org.omg.CORBA.TSIdentificationPackage.NotAvailable,
               org.omg.CORBA.TSIdentificationPackage.AlreadyIdentified
    {
        if ( sender == null )
            sender = senderOTS;
        else
            throw new org.omg.CORBA.TSIdentificationPackage.AlreadyIdentified();
    }


    /** identify_receiver is called by the OTS during initialization
        to register its Receiver callback interface with the ORB.
        identify_receiver may throw a AlreadyIdentified exception if
        the registration has already been done previously.
    */
    public void
        identify_receiver(org.omg.CosTSPortability.Receiver receiverOTS)
        throws org.omg.CORBA.TSIdentificationPackage.NotAvailable,
               org.omg.CORBA.TSIdentificationPackage.AlreadyIdentified
    {
        if ( receiver == null )
            receiver = receiverOTS;
        else
            throw new org.omg.CORBA.TSIdentificationPackage.AlreadyIdentified();
    }


    /** getSender is not defined in the OTS spec. It is just a convenience
        method to allow the ORB to access the Sender subsequent to
        initialization.
    * @return the ORB's Sender
    */
    public org.omg.CosTSPortability.Sender getSender() {
        return sender;
    }

    /** getReceiver is not defined in the OTS spec. It is just a convenience
        method to allow the ORB to access the Receiver subsequent to
        initialization.
     * @return The receiver
    */
    public org.omg.CosTSPortability.Receiver
        getReceiver()
    {
        return receiver;
    }
}
