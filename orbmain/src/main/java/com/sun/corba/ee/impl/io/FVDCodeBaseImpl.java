/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
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

package com.sun.corba.ee.impl.io;

import org.omg.CORBA.ORB;

import javax.rmi.CORBA.ValueHandler;

import java.util.Map;
import java.util.HashMap;
import java.util.Stack;

import com.sun.org.omg.CORBA.ValueDefPackage.FullValueDescription;

import com.sun.org.omg.SendingContext._CodeBaseImplBase;

import com.sun.corba.ee.spi.logging.OMGSystemException;

import com.sun.corba.ee.impl.javax.rmi.CORBA.Util;

/**
 * This class acts as the remote interface to receivers wishing to retrieve the information of a remote Class.
 */
public class FVDCodeBaseImpl extends _CodeBaseImplBase {
    // Contains rep. ids as keys to FullValueDescriptions
    private static Map<String, FullValueDescription> fvds = new HashMap<String, FullValueDescription>();

    // Private ORBSingleton used when we need an ORB while not
    // having a delegate set.
    private transient ORB orb = null;

    private static final OMGSystemException wrapper = OMGSystemException.self;

    // backward compatability so that appropriate rep-id calculations
    // can take place
    // this needs to be transient to prevent serialization during
    // marshalling/unmarshalling
    private transient ValueHandlerImpl vhandler = null;

    public FVDCodeBaseImpl(ValueHandler vh) {
        // vhandler will never be null
        this.vhandler = (com.sun.corba.ee.impl.io.ValueHandlerImpl) vh;
    }

    // Operation to obtain the IR from the sending context
    public com.sun.org.omg.CORBA.Repository get_ir() {
        return null;
    }

    // Operations to obtain a URL to the implementation code
    public String implementation(String x) {
        try {
            // Util.getCodebase may return null which would
            // cause a BAD_PARAM exception.
            String result = Util.getInstance().getCodebase(vhandler.getClassFromType(x));
            if (result == null) {
                return "";
            } else {
                return result;
            }
        } catch (ClassNotFoundException cnfe) {
            throw wrapper.missingLocalValueImpl(cnfe);
        }
    }

    public String[] implementations(String[] x) {
        String result[] = new String[x.length];

        for (int i = 0; i < x.length; i++) {
            result[i] = implementation(x[i]);
        }

        return result;
    }

    // the same information
    public FullValueDescription meta(String x) {
        try {
            FullValueDescription result = fvds.get(x);

            if (result == null) {
                try {
                    result = ValueUtility.translate(_orb(), ObjectStreamClass.lookup(vhandler.getAnyClassFromType(x)), vhandler);
                } catch (Throwable t) {
                    if (orb == null) {
                        orb = ORB.init();
                    }

                    result = ValueUtility.translate(orb, ObjectStreamClass.lookup(vhandler.getAnyClassFromType(x)), vhandler);
                }

                if (result != null) {
                    fvds.put(x, result);
                } else {
                    throw wrapper.missingLocalValueImpl();
                }
            }

            return result;
        } catch (Throwable t) {
            throw wrapper.incompatibleValueImpl(t);
        }
    }

    public FullValueDescription[] metas(String[] x) {
        FullValueDescription descriptions[] = new FullValueDescription[x.length];

        for (int i = 0; i < x.length; i++) {
            descriptions[i] = meta(x[i]);
        }

        return descriptions;
    }

    // information
    public String[] bases(String x) {
        try {
            Stack<String> repIds = new Stack<String>();
            Class parent = ObjectStreamClass.lookup(vhandler.getClassFromType(x)).forClass().getSuperclass();

            while (!parent.equals(java.lang.Object.class)) {
                repIds.push(vhandler.createForAnyType(parent));
                parent = parent.getSuperclass();
            }

            String result[] = new String[repIds.size()];
            for (int i = result.length - 1; i >= 0; i++) {
                result[i] = repIds.pop();
            }

            return result;
        } catch (Throwable t) {
            throw wrapper.missingLocalValueImpl(t);
        }
    }
}
