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

/**
 * @author Ken Cavanaugh
 */
@ManagedData
@Description("Component representing Codebase URLs for downloading code")
public interface JavaCodebaseComponent extends TaggedComponent {
    @ManagedAttribute
    @Description("List of URLs in the codebase")
    public String getURLs();
}
