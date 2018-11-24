/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.encoding;

import com.sun.corba.ee.spi.logging.ORBUtilSystemException;
import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;
import com.sun.corba.ee.spi.orb.ORB;

import org.omg.CORBA.INTERNAL;

/**
 * Creates read/write buffer managers to handle over/under flow in CDR*putStream.
 */

public class BufferManagerFactory {
    private static final ORBUtilSystemException wrapper = ORBUtilSystemException.self;

    public static final int GROW = 0;
    public static final int STREAM = 2;

    // The next two methods allow creation of BufferManagers based on GIOP version.
    // We may want more criteria to be involved in this decision.
    // These are only used for sending messages (so could be fragmenting)
    public static BufferManagerRead newBufferManagerRead(GIOPVersion version, byte encodingVersion, ORB orb) {

        // REVISIT - On the reading side, shouldn't we monitor the incoming
        // fragments on a given connection to determine what fragment size
        // they're using, then use that ourselves?

        if (encodingVersion != ORBConstants.CDR_ENC_VERSION) {
            return new BufferManagerReadGrow();
        }

        switch (version.intValue()) {
        case GIOPVersion.VERSION_1_0:
            return new BufferManagerReadGrow();
        case GIOPVersion.VERSION_1_1:
        case GIOPVersion.VERSION_1_2:
            // The stream reader can handle fragmented and non-fragmented messages
            return new BufferManagerReadStream(orb);
        default:
            // REVISIT - what is appropriate?
            throw new INTERNAL("Unknown GIOP version: " + version);
        }
    }

    static BufferManagerRead newReadEncapsulationBufferManager() {
        return new BufferManagerReadGrow();
    }

    static BufferManagerWrite newWriteEncapsulationBufferManager(ORB orb) {
        return new BufferManagerWriteGrow(orb);
    }

    static BufferManagerWrite newBufferManagerWrite(int strategy, byte encodingVersion, ORB orb) {
        if (encodingVersion != ORBConstants.CDR_ENC_VERSION) {
            if (strategy != BufferManagerFactory.GROW) {
                throw wrapper.invalidBuffMgrStrategy("newBufferManagerWrite");
            }
            return new BufferManagerWriteGrow(orb);
        }
        switch (strategy) {
        case BufferManagerFactory.GROW:
            return new BufferManagerWriteGrow(orb);
        case BufferManagerFactory.STREAM:
            return new BufferManagerWriteStream(orb);
        default:
            throw new INTERNAL("Unknown buffer manager write strategy: " + strategy);
        }
    }

    public static BufferManagerWrite newBufferManagerWrite(GIOPVersion version, byte encodingVersion, ORB orb) {
        if (encodingVersion != ORBConstants.CDR_ENC_VERSION) {
            return new BufferManagerWriteGrow(orb);
        }
        return newBufferManagerWrite(orb.getORBData().getGIOPBuffMgrStrategy(version), encodingVersion, orb);
    }
}
