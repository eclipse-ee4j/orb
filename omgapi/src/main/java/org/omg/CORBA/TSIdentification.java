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

package org.omg.CORBA;


/** 
 * An interface defined in the OMG Transactions Service Specification 
 * that provides methods to allow the JTS to register
 * its Sender and Receiver interfaces with the ORB. <code>TSIdentification</code> 
 * methods are always called from the same address space (i.e. it is 
 * a pseudo-object), hence it is not necessary to define any stubs/skeletons. 
 * During initialization, an instance of <code>TSIdentification</code> is provided 
 * to the JTS by the ORB using the method 
 * <code>com.sun.corba.ee.spi.costransactions.TransactionService.identify_ORB</code>.
*/

public interface TSIdentification { 

    /** 
     * Called by the OTS 
     * during initialization in order to register its Sender 
     * callback interface with the ORB. This method
     * may throw an <code>AlreadyIdentified</code> exception if
     * the registration has already been done previously.
         * @param senderOTS the <code>Sender</code> object to be
         *                  registered
         * @throws org.omg.CORBA.TSIdentificationPackage.NotAvailable
         *         if the ORB is unavailable to register the given <code>Sender</code>
         *         object
         * @throws org.omg.CORBA.TSIdentificationPackage.AlreadyIdentified
         *         if the given <code>Sender</code> object has already been registered
         *         with the ORB
         *         
    */
    public void 
        identify_sender(org.omg.CosTSPortability.Sender senderOTS)
        throws org.omg.CORBA.TSIdentificationPackage.NotAvailable, 
               org.omg.CORBA.TSIdentificationPackage.AlreadyIdentified ;


    /** 
     * Called by the OTS during 
     * initialization to register its <code>Receiver</code> callback interface 
     * with the ORB.  This operation may throw an <code> AlreadyIdentified</code> 
     * exception if the registration has already been done previously.
         * @param receiverOTS the <code>Receiver</code> object to register with the ORB
         * @throws org.omg.CORBA.TSIdentificationPackage.NotAvailable
         *         if the ORB is unavailable to register the given <code>Receiver</code>
         *         object
         * @throws org.omg.CORBA.TSIdentificationPackage.AlreadyIdentified
         *         if the given <code>Receiver</code> object has already been registered
         *         with the ORB
    */
    public void 
        identify_receiver(org.omg.CosTSPortability.Receiver receiverOTS)
        throws org.omg.CORBA.TSIdentificationPackage.NotAvailable, 
               org.omg.CORBA.TSIdentificationPackage.AlreadyIdentified ;
}


