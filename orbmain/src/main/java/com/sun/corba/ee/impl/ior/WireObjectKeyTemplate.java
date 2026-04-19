/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
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

package com.sun.corba.ee.impl.ior;


import com.sun.corba.ee.spi.ior.ObjectAdapterId ;
import com.sun.corba.ee.spi.ior.ObjectId ;
import com.sun.corba.ee.spi.ior.ObjectKeyTemplate ;
import com.sun.corba.ee.spi.logging.IORSystemException ;
import com.sun.corba.ee.spi.misc.ORBConstants ;
import com.sun.corba.ee.spi.orb.ORB ;
import com.sun.corba.ee.spi.orb.ORBVersion ;
import com.sun.corba.ee.spi.orb.ORBVersionFactory ;
import com.sun.corba.ee.spi.protocol.ServerRequestDispatcher ;

import org.omg.CORBA_2_3.portable.OutputStream ;

/**
 * @author Ken Cavanaugh
 */
public class WireObjectKeyTemplate implements ObjectKeyTemplate {

    private static final IORSystemException WRAPPER = IORSystemException.self;
    private static final ObjectAdapterId NULL_OBJECT_ADAPTER_ID = new ObjectAdapterIdArray(new String[0]);

    private ORB orb;

    public WireObjectKeyTemplate(ORB orb) {
        this.orb = orb;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        return obj instanceof WireObjectKeyTemplate;
    }

    @Override
    public int hashCode() {
        return 53; // All WireObjectKeyTemplates are the same, so they should
                   // have the same hashCode.
    }

    @Override
    public void write(ObjectId id, OutputStream os) {
        byte[] key = id.getId();
        os.write_octet_array(key, 0, key.length);
    }

    @Override
    public void write(OutputStream os) {
        // Does nothing
    }

    @Override
    public int getSubcontractId() {
        return ORBConstants.DEFAULT_SCID;
    }

    // While it might make sense to throw an exception here, this causes
    // problems since we need to check whether unusual object references
    // are local or not. It seems that the easiest way to handle this is
    // to return an invalid server id.
    @Override
    public int getServerId() {
        return -1;
    }

    @Override
    public String getORBId() {
        throw WRAPPER.orbIdNotAvailable();
    }

    @Override
    public ObjectAdapterId getObjectAdapterId() {
        return NULL_OBJECT_ADAPTER_ID;
    }


    // Adapter ID is not available, since our
    // ORB did not implement the object carrying this key.
    @Override
    public byte[] getAdapterId() {
        throw WRAPPER.adapterIdNotAvailable();
    }


    @Override
    public ORBVersion getORBVersion() {
        return ORBVersionFactory.getFOREIGN();
    }


    @Override
    public ServerRequestDispatcher getServerRequestDispatcher(ObjectId id) {
        byte[] bid = id.getId();
        String str = new String(bid);
        return orb.getRequestDispatcherRegistry().getServerRequestDispatcher(str);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
            + "[subcontractId=" + getSubcontractId()
            + " serverId=" + getServerId()
            + " objectadapterId=" + getObjectAdapterId()
            + "]";
    }
}
