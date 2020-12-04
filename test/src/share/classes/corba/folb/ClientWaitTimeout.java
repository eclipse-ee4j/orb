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

//
// Created       : 2005 Sep 23 (Fri) 15:17:47 by Harold Carr.
// Last Modified : 2005 Oct 03 (Mon) 10:28:16 by Harold Carr.
//

package corba.folb;

import java.util.Hashtable;
import java.util.Properties;
import javax.naming.InitialContext;

import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.SystemException;

import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.impl.misc.ORBUtility;

import corba.framework.Controller;
import corba.hcks.U;

import org.testng.annotations.BeforeSuite ;
import org.testng.annotations.Test ;

import org.testng.Assert ;

/**
 * @author Harold Carr
 */
public class ClientWaitTimeout extends ClientBase {

    @BeforeSuite
    public void clientSetup() throws Exception {
            Properties props = getDefaultProperties();
            
            // Set retry timeout to 5 seconds.
            props.setProperty(ORBConstants.WAIT_FOR_RESPONSE_TIMEOUT, "5000");
            props.setProperty(ORBConstants.DEBUG_PROPERTY,
                              "transport,subcontract");

            setup(props);
            circularSetup();
    }

    @Test
    public void test() throws Exception {
        dprint("--------------------------------------------------");
        dprint("neverReturns - so should timeout in wait");
        dprint("--------------------------------------------------");

        try {
            testRfmWithAddressesWithLabel.neverReturns();
            Assert.fail( "should not return, but did return" ) ;
        } catch (java.rmi.MarshalException e) {
            SystemException cf = 
                wrapper.communicationsTimeoutWaitingForResponse( -1);
            checkMarshalException("neverReturns", e, cf);
        }
    }

    public static void main(String[] av) {
        doMain( ClientWaitTimeout.class ) ;
    }
}

// End of file.
