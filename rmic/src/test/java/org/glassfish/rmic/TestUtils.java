/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.rmic;

import java.io.File;

public class TestUtils {
    @SuppressWarnings("ConstantConditions")
    public static String getClassPathString() {
        String classFileName = toPath(TestUtils.class.getName());
        String filePath = TestUtils.class.getClassLoader().getResource(classFileName).getPath();
        return filePath.substring(0, filePath.indexOf(classFileName));
    }

    private static String toPath(String className) {
        return className.replace('.', File.separatorChar) + ".class";
    }
}
