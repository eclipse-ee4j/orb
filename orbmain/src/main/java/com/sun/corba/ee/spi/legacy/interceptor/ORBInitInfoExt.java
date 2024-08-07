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

package com.sun.corba.ee.spi.legacy.interceptor;

import com.sun.corba.ee.spi.orb.ORB;

/**
 * The interface defines an extension to the standard ORBInitInfo that gives access to the ORB being initialized.
 * Interceptors run as the last stage of initialization of the ORB, so the ORB instance returned by getORB is fully
 * initialized. Note that this facility eventually shows up post-CORBA 3.0 as a result of the resolution of OMG core
 * issue on accessing the ORB from local objects.
 */
public interface ORBInitInfoExt {
    ORB getORB();
}
