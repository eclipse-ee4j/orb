/*
 * Copyright (c) 2018, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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

package org.glassfish.rmic.asm;

import org.glassfish.rmic.tools.java.ClassDefinitionFactoryTest;
import org.junit.Test;
import org.objectweb.asm.Opcodes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

public class AsmClassFactoryTest extends ClassDefinitionFactoryTest {

    public AsmClassFactoryTest() {
        super(new AsmClassFactory());
    }

    @Test
    public void canRetrieveLatestAsmVersion() {
        assertThat(AsmClassFactory.getLatestVersion(), equalTo(Opcodes.ASM9));
    }

    @Test
    public void canRetrieveLatestSupportedClassVersion() {
        assertThat(AsmClassFactory.getLatestClassVersion(), greaterThan(Opcodes.V25));
    }
}
