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

import org.glassfish.rmic.tools.java.ClassDefinition;
import org.glassfish.rmic.tools.java.ClassDefinitionFactory;
import org.glassfish.rmic.tools.java.Environment;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.objectweb.asm.Opcodes;

public class BinaryClassFactory implements ClassDefinitionFactory {
    @Override
    public ClassDefinition loadDefinition(InputStream is, Environment env) throws IOException {
        DataInputStream dis = new DataInputStream(new BufferedInputStream(is));
        return BinaryClass.load(env, dis, 0);
    }

    @Override
    public int getMaxClassVersion() {
        return Opcodes.V9;
    }
}
