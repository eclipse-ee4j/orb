/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.naming.namingutil;

/**
 * INS URL is a generic interface for two different types of URL's specified in INS spec.
 *
 * @Author Hemanth
 */
public interface INSURL {
    public boolean getRIRFlag();

    // There can be one or more Endpoint's in the URL, so the return value is
    // a List
    public java.util.List getEndpointInfo();

    public String getKeyString();

    public String getStringifiedName();

    // This method will return true only in CorbanameURL, It is provided because
    // corbaname: URL needs special handling.
    public boolean isCorbanameURL();

    // A debug method, which is not required for normal operation
    public void dPrint();
}
