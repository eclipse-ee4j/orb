/*
 * Copyright (c) 2016, 2020 Oracle and/or its affiliates.
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

package com.sun.corba.ee.impl.encoding;

import java.security.AccessController;
import java.security.PrivilegedAction;

import com.sun.corba.ee.impl.protocol.giopmsgheaders.Message;
import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.protocol.MessageMediator;
import com.sun.corba.ee.spi.transport.Connection;

/**
 * Factory for creating various output streams with AccessController
 * 
 * @author jwells
 */
public class OutputStreamFactory {
    public static TypeCodeOutputStream newTypeCodeOutputStream(final ORB orb) {
        return AccessController.doPrivileged(new PrivilegedAction<TypeCodeOutputStream>() {

            @Override
            public TypeCodeOutputStream run() {
                return new TypeCodeOutputStream(orb);
            }

        });
    }

    public static EncapsOutputStream newEncapsOutputStream(final ORB orb, final GIOPVersion version) {
        return AccessController.doPrivileged(new PrivilegedAction<EncapsOutputStream>() {

            @Override
            public EncapsOutputStream run() {
                return new EncapsOutputStream(orb, version);
            }

        });
    }

    public static EncapsOutputStream newEncapsOutputStream(final ORB orb) {
        return AccessController.doPrivileged(new PrivilegedAction<EncapsOutputStream>() {

            @Override
            public EncapsOutputStream run() {
                return new EncapsOutputStream(orb);
            }

        });

    }

    public static CDROutputObject newCDROutputObject(final ORB orb, final MessageMediator mediator, final GIOPVersion giopVersion,
            final Connection connection, final Message header, final byte streamFormatVersion) {
        return AccessController.doPrivileged(new PrivilegedAction<CDROutputObject>() {

            @Override
            public CDROutputObject run() {
                return new CDROutputObject(orb, mediator, giopVersion, connection, header, streamFormatVersion);
            }
        });
    }

    public static CDROutputObject newCDROutputObject(final ORB orb, final MessageMediator messageMediator, final Message header,
            final byte streamFormatVersion) {
        return AccessController.doPrivileged(new PrivilegedAction<CDROutputObject>() {

            @Override
            public CDROutputObject run() {
                return new CDROutputObject(orb, messageMediator, header, streamFormatVersion);
            }
        });

    }

    public static CDROutputObject newCDROutputObject(final ORB orb, final MessageMediator messageMediator, final Message header,
            final byte streamFormatVersion, final int strategy) {
        return AccessController.doPrivileged(new PrivilegedAction<CDROutputObject>() {

            @Override
            public CDROutputObject run() {
                return new CDROutputObject(orb, messageMediator, header, streamFormatVersion, strategy);
            }
        });

    }
}
