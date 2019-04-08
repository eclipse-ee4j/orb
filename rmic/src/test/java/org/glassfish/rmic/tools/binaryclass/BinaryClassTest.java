/*
 * Copyright (c) 2018, 2019, Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.rmic.tools.binaryclass;

import static org.junit.Assume.assumeTrue;

import org.glassfish.rmic.tools.java.ClassDefinitionFactoryTest;
import org.glassfish.rmic.tools.javac.BatchEnvironment;
import org.junit.BeforeClass;

public class BinaryClassTest extends ClassDefinitionFactoryTest {

    public BinaryClassTest() {
        super(new BinaryClassFactory());
    }

    @BeforeClass
    public static void testIfBinaryClassSupported() {
        assumeTrue(BatchEnvironment.mayUseBinaryClassFactory());
    }
}
