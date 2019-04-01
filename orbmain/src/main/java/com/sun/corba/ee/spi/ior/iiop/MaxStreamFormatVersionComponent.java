/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.ior.iiop;

import com.sun.corba.ee.spi.ior.TaggedComponent;

import org.glassfish.gmbal.ManagedData;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.Description;

// Java to IDL ptc 02-01-12 1.4.11
// TAG_RMI_CUSTOM_MAX_STREAM_FORMAT
@ManagedData
@Description("Component representing the maximum RMI-IIOP stream format " + "version to be used with this IOR")
public interface MaxStreamFormatVersionComponent extends TaggedComponent {
    @ManagedAttribute
    @Description("The maximum RMI-IIOP stream format version " + "(usually 2)")
    public byte getMaxStreamFormatVersion();
}
