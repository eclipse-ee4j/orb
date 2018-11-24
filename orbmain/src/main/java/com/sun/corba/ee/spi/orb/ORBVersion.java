/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.orb;

import org.omg.CORBA.portable.OutputStream;

import org.glassfish.gmbal.ManagedData;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;

@ManagedData
@Description("The version of the ORB")
public interface ORBVersion extends Comparable<ORBVersion> {
    byte FOREIGN = 0; // ORB from another vendor
    byte OLD = 1; // JDK 1.3.0 or earlier
    byte NEW = 2; // JDK 1.3.1 FCS
    byte JDK1_3_1_01 = 3; // JDK1_3_1_01 patch
    byte NEWER = 10; // JDK 1.4.x
    byte PEORB = 20; // PEORB in JDK 1.5, S1AS 8, J2EE 1.4

    @ManagedAttribute
    @Description("ORB version (0=FOREIGN,1=OLD,2=NEW,3=JDK1_3_1_01,10=NEWER,20=PEORB)")
    byte getORBType();

    void write(OutputStream os);

    public boolean lessThan(ORBVersion version);
}
