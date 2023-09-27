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

package com.sun.corba.ee.impl.naming.namingutil;

/**
 * INS URL is a generic interface for two different types of URL's specified in INS spec.
 *
 * @author Hemanth
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
