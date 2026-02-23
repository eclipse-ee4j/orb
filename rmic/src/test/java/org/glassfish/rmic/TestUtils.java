/*
 * Copyright (c) 2018, 2020 Oracle and/or its affiliates. All rights reserved.
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
        return className.replace('.', '/') + ".class";
    }
}
