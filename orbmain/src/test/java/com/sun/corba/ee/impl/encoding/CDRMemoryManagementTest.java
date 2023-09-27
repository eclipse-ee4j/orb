/*
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
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

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class CDRMemoryManagementTest extends EncodingTestBase {

    @Test
    public void whenFinishReadingFragment_dontReleaseIt() {
        setMessageBody(0, 0, 0, 1);
        addFragment(0, 0, 0, 2);

        getInputObject().read_long();

        assertEquals(0, getNumBuffersReleased());
    }

    @Test
    public void whenStartReadingNextFragment_releasePreviousFragment() {
        setMessageBody(0, 0, 0, 1);
        addFragment(0, 0, 0, 2);

        getInputObject().read_long();
        getInputObject().read_long();

        assertEquals(1, getNumBuffersReleased());
    }

    @Test
    public void whenStartReadingNextFragmentWhileMarkActive_dontReleasePreviousFragment() {
        setMessageBody(0, 0, 0, 1);
        addFragment(0, 0, 0, 2);

        getInputObject().read_long();
        getInputObject().mark(0);
        getInputObject().read_long();

        assertEquals(0, getNumBuffersReleased());
    }

    @Test
    public void whenStartReadingNextFragmentWhileMarkActive_releasePreviousFragmentOnResetAndNewRead() {
        setMessageBody(0, 0, 0, 1);
        addFragment(0, 0, 0, 2);

        getInputObject().read_long();
        getInputObject().mark(0);
        getInputObject().read_long();
        getInputObject().reset();
        getInputObject().read_long();

        assertEquals(1, getNumBuffersReleased());
    }

    @Test
    public void whenFragmentAddedAfterMarkActive_releaseSubsequentFragmentOnClose() throws IOException {
        setMessageBody(0, 0, 0, 1);
        addFragment(0, 0, 0, 2);

        getInputObject().read_long();
        getInputObject().mark(0);
        getInputObject().read_long();
        getInputObject().close();

        assertEquals(2, getNumBuffersReleased());
    }

    @Test
    public void whenInputObjectClosed_releaseAllFragments() throws IOException {
        setMessageBody(0, 0, 0, 1);
        addFragment(0, 0, 0, 2);
        addFragment(0, 0, 0, 3);

        getInputObject().read_short();
        getInputObject().close();

        assertEquals(3, getNumBuffersReleased());
    }

    @Test
    public void whenInputObjectClosedWhileMarkActive_releaseAllFragments() throws IOException {
        setMessageBody(0, 0, 0, 1);
        addFragment(0, 0, 0, 2);

        getInputObject().read_short();
        getInputObject().mark(0);
        getInputObject().close();

        assertEquals(2, getNumBuffersReleased());
    }
}
