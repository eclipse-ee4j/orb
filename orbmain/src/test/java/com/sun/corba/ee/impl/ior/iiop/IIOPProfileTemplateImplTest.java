/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.corba.ee.impl.ior.iiop;

import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;
import com.sun.corba.ee.spi.ior.iiop.IIOPAddress;
import com.sun.corba.ee.spi.ior.iiop.IIOPProfileTemplate;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.transport.SocketInfo;
import org.junit.Test;

import static com.meterware.simplestub.Stub.createStrictStub;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class IIOPProfileTemplateImplTest {
    private static final String HOST = "testhost";
    private static final int PORT = 1234;

    private ORB orb = createStrictStub(ORB.class);
    private GIOPVersion version = new GIOPVersion(1, 2);
    private IIOPAddress primary = new IIOPAddressImpl(HOST, PORT);
    private IIOPProfileTemplate impl = new IIOPProfileTemplateImpl(orb, version, primary);

    @Test
    public void socketHostAndAddress_matchPrimaryAddress() {
        assertThat(impl.getPrimarySocketInfo().getHost(), equalTo(HOST));
        assertThat(impl.getPrimarySocketInfo().getPort(), equalTo(PORT));
    }

    @Test
    public void whenNoTaggedComponents_socketTypeIsPlainText() {
        assertThat(impl.getPrimarySocketInfo().getType(), equalTo(SocketInfo.IIOP_CLEAR_TEXT));
    }

    @Test
    public void whenContainsHttpJavaCodebaseComponent_socketTypeIsPlainText() {
        addJavaCodebase("http://localhost:1401/base");

        assertThat(impl.getPrimarySocketInfo().getType(), equalTo(SocketInfo.IIOP_CLEAR_TEXT));
    }

    @Test
    public void whenContainsHttpsJavaCodebaseComponent_socketTypeIsSsl() {
        addJavaCodebase("https://localhost:1402/base");

        assertThat(impl.getPrimarySocketInfo().getType(), equalTo(SocketInfo.SSL_PREFIX));
    }

    private void addJavaCodebase(String urls) {
        impl.add(new JavaCodebaseComponentImpl(urls));
    }
}