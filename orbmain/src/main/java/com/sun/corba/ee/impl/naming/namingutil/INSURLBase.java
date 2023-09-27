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

import com.sun.corba.ee.spi.logging.OMGSystemException;

/**
 * The corbaloc: URL definitions from the -ORBInitDef and -ORBDefaultInitDef's will be stored in this object. This
 * object is capable of storing multiple Host profiles as defined in the CorbaLoc grammer.
 *
 * @author Hemanth
 */
public abstract class INSURLBase implements INSURL {
    private static OMGSystemException wrapper = OMGSystemException.self;

    // If rirFlag is set to true that means internal
    // boot strapping technique will be used. If set to
    // false then the EndpointInfo will be used to create the
    // Service Object reference.
    protected boolean rirFlag = false;
    protected java.util.ArrayList theEndpointInfo = null;
    protected String theKeyString = "NameService";
    protected String theStringifiedName = null;

    /**
     * A Utility method to throw BAD_PARAM exception to signal malformed INS URL.
     * 
     * @param name Invalid name of the URL
     */
    protected void badAddress(String name) {
        throw wrapper.soBadAddress(name);
    }

    protected void badAddress(java.lang.Throwable e, String name) {
        throw wrapper.soBadAddress(e, name);
    }

    public boolean getRIRFlag() {
        return rirFlag;
    }

    public java.util.List getEndpointInfo() {
        return theEndpointInfo;
    }

    public String getKeyString() {
        return theKeyString;
    }

    public String getStringifiedName() {
        return theStringifiedName;
    }

    public abstract boolean isCorbanameURL();

    public void dPrint() {
        System.out.println("URL Dump...");
        System.out.println("Key String = " + getKeyString());
        System.out.println("RIR Flag = " + getRIRFlag());
        System.out.println("isCorbanameURL = " + isCorbanameURL());
        for (int i = 0; i < theEndpointInfo.size(); i++) {
            ((IIOPEndpointInfo) theEndpointInfo.get(i)).dump();
        }
        if (isCorbanameURL()) {
            System.out.println("Stringified Name = " + getStringifiedName());
        }
    }

}
