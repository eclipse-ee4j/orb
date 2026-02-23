/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
 * Copyright (c) 2021 Payara Services Ltd.
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

package com.sun.corba.ee.impl.copyobject ;

import com.sun.corba.ee.impl.util.Utility;

import java.rmi.Remote;

import org.glassfish.pfl.dynamic.copyobject.impl.JavaStreamObjectCopierImpl ;
import org.omg.CORBA.ORB ;


public class JavaStreamORBObjectCopierImpl extends JavaStreamObjectCopierImpl {
    private final ORB orb ;

    public JavaStreamORBObjectCopierImpl( ORB orb ) {
        this.orb = orb ;
    }

    public Object copy(Object obj, boolean debug ) {
        return copy( obj ) ;
    }

    @Override
    public Object copy(Object obj) {
        if (obj instanceof Remote) {
            // Yes, so make sure it is connected and converted
            // to a stub (if needed)...
            return Utility.autoConnect(obj,orb,true);
        }

        return super.copy( obj ) ;
    }
}
