/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

//
// Created       : 2005 Sep 23 (Fri) 15:17:47 by Harold Carr.
// Last Modified : 2005 Oct 03 (Mon) 10:28:47 by Harold Carr.
//

package corba.folb;

import java.util.Properties;

import org.omg.CORBA.SystemException;

import com.sun.corba.ee.spi.misc.ORBConstants;

import org.testng.annotations.BeforeSuite ;
import org.testng.annotations.Test ;

import org.testng.Assert ;

/**
 * @author Harold Carr
 */
public class ClientCircular extends ClientBase {

    @BeforeSuite
    public void clientSetup() throws Exception {
        Properties props = getDefaultProperties() ;
            
        // Set retry timeout to 5 seconds.
        props.setProperty(ORBConstants.TRANSPORT_TCP_CONNECT_TIMEOUTS_PROPERTY, 
            "250:5000:100");
        // props.setProperty(ORBConstants.DEBUG_PROPERTY,
                          // "transport,subcontract");

        setup( props ) ;
        circularSetup() ;
    }

    @Test
    public void test() throws Exception {
        dprint("--------------------------------------------------");
        dprint("Circular failover without update (send label, no IORUpdate)");
        dprint("--------------------------------------------------");

        makeCall(testRfmWithAddressesWithLabel,
                        Common.TEST_RFM_WITH_ADDRESSES_WITH_LABEL,
                        "Circular failover without update (send label, no IORUpdate)",
                        corba.folb_8_1.Common.X,
                        SEND_MEMBERSHIP_LABEL, NO_IOR_UPDATE);

        dprint("--------------------------------------------------");
        dprint("Remove last Acceptor");
        dprint("--------------------------------------------------");
        gisPoaWithAddressesWithLabels.removeAcceptorAndConnections(
            corba.folb_8_1.Common.X);
        Thread.sleep(5000);

        dprint("--------------------------------------------------");
        dprint("Circular timeout reached.");
        dprint("--------------------------------------------------");
        try {
            makeCall(testRfmWithAddressesWithLabel,
                            Common.TEST_RFM_WITH_ADDRESSES_WITH_LABEL,
                            "Circular timeout reached.",
                            "DUMMY",
                            SEND_MEMBERSHIP_LABEL, NO_IOR_UPDATE);

            Assert.fail( "Circular timeout failed: call incorrectly succeeded" ) ;
        } catch (Exception e) {
            SystemException cf = wrapper.connectFailure( new RuntimeException(),
                "dummy", "dummy", "dummy");
            checkMarshalException("Circular timeout", e, cf);
        }
    }

    public static void main(String[] av) {
        doMain( ClientCircular.class ) ;
    }
}

// End of file.
