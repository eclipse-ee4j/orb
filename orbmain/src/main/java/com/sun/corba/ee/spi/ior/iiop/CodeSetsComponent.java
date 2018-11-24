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

import com.sun.corba.ee.impl.encoding.CodeSetComponentInfo;

import org.glassfish.gmbal.ManagedData;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.Description;

/**
 * @author Ken Cavanaugh
 */
@ManagedData
@Description("The character codesets to be used for encoding " + "strings sent to the object reference represented by " + "this IOR")
public interface CodeSetsComponent extends TaggedComponent {
    @ManagedAttribute
    @Description("The codeset component info")
    // we'll just use toString() to represent this
    public CodeSetComponentInfo getCodeSetComponentInfo();
}
