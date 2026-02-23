/*
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.corba.ee.spi.ior.iiop;

import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.orb.ORBData;

import org.junit.Test;
import org.omg.IOP.TAG_ALTERNATE_IIOP_ADDRESS;

import static com.meterware.simplestub.Stub.createStrictStub;
import static com.meterware.simplestub.Stub.createStub;
import static org.junit.Assert.assertEquals;

public class IIOPFactoriesTest {

    @Test
    public void canCreateAlternateIIOPAddressComponent() {
        IIOPAddress addr = IIOPFactories.makeIIOPAddress("localhost", 2345);
        AlternateIIOPAddressComponent comp = IIOPFactories.makeAlternateIIOPAddressComponent(addr);
        org.omg.IOP.TaggedComponent tcomp = comp.getIOPComponent(createStub(ORBFake.class));
        assertEquals(tcomp.tag, TAG_ALTERNATE_IIOP_ADDRESS.value);
    }

    abstract static public class ORBDataFake implements ORBData {
        @Override
        public int getGIOPBufferSize() {
            return 100;
        }
    }

    abstract static public class ORBFake extends ORB {

        private ORBData data = createStrictStub(ORBDataFake.class);

        @Override
        public ORBData getORBData() {
            return data;
        }
    }

}
