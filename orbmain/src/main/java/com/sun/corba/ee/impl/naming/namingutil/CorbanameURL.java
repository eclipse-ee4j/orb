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
 * The corbaname: URL definitions from the -ORBInitDef and -ORBDefaultInitDef's will be stored in this object. This
 * object is capable of storing CorbaLoc profiles as defined in the CorbaName grammer.
 *
 * @author Hemanth
 */
public class CorbanameURL extends INSURLBase {
    /**
     * This constructor takes a corbaname: url with 'corbaname:' prefix stripped and initializes all the variables
     * accordingly. If there are any parsing errors then BAD_PARAM exception is raised.
     * 
     * @param aURL corbaname URL as a String
     */
    public CorbanameURL(String aURL) {
        String url = aURL;

        // First Clean the URL Escapes if there are any
        try {
            url = Utility.cleanEscapes(url);
        } catch (Exception e) {
            badAddress(e, aURL);
        }

        int delimiterIndex = url.indexOf('#');
        String corbalocString = null;
        if (delimiterIndex != -1) {
            corbalocString = "corbaloc:" + url.substring(0, delimiterIndex);
        } else {
            corbalocString = "corbaloc:" + url;
        }

        try {
            // Check the corbaloc grammar and set the returned corbaloc
            // object to the CorbaName Object
            INSURL insURL = INSURLHandler.getINSURLHandler().parseURL(corbalocString);
            copyINSURL(insURL);
            // String after '#' is the Stringified name used to resolve
            // the Object reference from the rootnaming context. If
            // the String is null then the Root Naming context is passed
            // back
            if ((delimiterIndex > -1) && (delimiterIndex < (aURL.length() - 1))) {
                int start = delimiterIndex + 1;
                String result = url.substring(start);
                theStringifiedName = result;
            }
        } catch (Exception e) {
            badAddress(e, aURL);
        }
    }

    /**
     * A Utility method to copy all the variables from CorbalocURL object to this instance.
     */
    private void copyINSURL(INSURL url) {
        rirFlag = url.getRIRFlag();
        theEndpointInfo = (java.util.ArrayList) url.getEndpointInfo();
        theKeyString = url.getKeyString();
        theStringifiedName = url.getStringifiedName();
    }

    public boolean isCorbanameURL() {
        return true;
    }

}
