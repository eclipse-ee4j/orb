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

package com.sun.corba.ee.spi.protocol;

import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.transport.ContactInfoList;

public abstract class ClientDelegate extends org.omg.CORBA_2_3.portable.Delegate {
    /**
     * The ORB associated * with an invocation.
     *
     * @return ORB
     */
    public abstract ORB getBroker();

    /**
     * Get the CorbaContactInfoList which represents they encoding/protocol/transport combinations that may be used to
     * contact the service.
     *
     * @return CorbaContactInfoList
     */
    public abstract ContactInfoList getContactInfoList();
}

// End of file.
