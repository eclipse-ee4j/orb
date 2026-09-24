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
 * This class is the entry point to parse different types of INS URL's.
 *
 * @author Hemanth
 */
public class INSURLHandler {

    private static INSURLHandler insURLHandler = null;

    // Length of corbaloc:
    private static final int CORBALOC_PREFIX_LENGTH = 9;

    // Length of corbaname:
    private static final int CORBANAME_PREFIX_LENGTH = 10;

    private INSURLHandler() {
    }

    public synchronized static INSURLHandler getINSURLHandler() {
        if (insURLHandler == null) {
            insURLHandler = new INSURLHandler();
        }
        return insURLHandler;
    }

    public INSURL parseURL(String aUrl) {
        String url = aUrl;
        if (url.startsWith("corbaloc:") == true) {
            return new CorbalocURL(url.substring(CORBALOC_PREFIX_LENGTH));
        } else if (url.startsWith("corbaname:") == true) {
            return new CorbanameURL(url.substring(CORBANAME_PREFIX_LENGTH));
        }
        return null;
    }
}
