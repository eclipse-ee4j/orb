/*
 * Copyright (c) 2018, 2019, Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.rmic;

import java.io.File;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.rmi.PortableRemoteObject;

public class TestUtils {
    public static String getClassPathString() {
        return Stream.of(TestUtils.class, PortableRemoteObject.class)
              .map(TestUtils::toClassPathElement)
              .filter(Objects::nonNull)
              .collect(Collectors.joining(File.pathSeparator));
    }

    private static String toClassPathElement(Class<?> aClass) {
        ClassLoader classLoader = aClass.getClassLoader();
        if (classLoader == null) return null;

        String classFileName = toPath(aClass.getName());
        String filePath = withoutPrefix(classLoader.getResource(classFileName).getPath());
        return toClassPathElement(filePath, classFileName);
    }

    private static String withoutPrefix(String path) {
        if (path.startsWith("file:"))
            return path.substring("file:".length());
        else
            return path;
    }

    private static String toClassPathElement(String filePath, String classFileName) {
        if (filePath.contains("!"))
            return filePath.substring(0, filePath.indexOf("!"));
        else
            return filePath.substring(0, filePath.indexOf(classFileName));
    }

    private static String toPath(String className) {
        return className.replace('.', File.separatorChar) + ".class";
    }
}
