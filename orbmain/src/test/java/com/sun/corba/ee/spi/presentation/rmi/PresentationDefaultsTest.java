/*
 * Copyright (c) 2018, 2020 Oracle and/or its affiliates.
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

package com.sun.corba.ee.spi.presentation.rmi;

import com.meterware.simplestub.Memento;
import com.meterware.simplestub.SystemPropertySupport;
import com.sun.corba.ee.spi.misc.ORBConstants;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class PresentationDefaultsTest {

    private List<Memento> mementos = new ArrayList<>();

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {
        for (Memento memento : mementos)
            memento.revert();
    }

    @Test
    public void dynamicStubFactory_createsDynamicStubs() throws Exception {
        assertThat(PresentationDefaults.getDynamicStubFactoryFactory().createsDynamicStubs(), is(true));
    }

    @Test
    public void staticStubFactory_doesNotCreateDynamicStubs() throws Exception {
        assertThat(PresentationDefaults.getStaticStubFactoryFactory().createsDynamicStubs(), is(false));
    }

    @Test
    public void defaultOrbPresentationManager_createsDynamicStubs() throws Exception {
        assertThat(PresentationDefaults.makeOrbPresentationManager().useDynamicStubs(), is(true));
    }

    @Test
    public void whenSystemPropertyFalse_presentationManagerCreatesStaticStubs() throws Exception {
        mementos.add(SystemPropertySupport.install(ORBConstants.USE_DYNAMIC_STUB_PROPERTY, "false"));

        assertThat(PresentationDefaults.makeOrbPresentationManager().useDynamicStubs(), is(false));
    }
}
