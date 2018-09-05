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

import org.junit.Test;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;

public class BatchEnvironmentTest {

    @Test
    public void createdClassPathString_usesPathSeparator() throws Exception {
        String systemPath = "./jdk/jre/lib/rt.jar";
        String classPath = "./user.jar" + File.pathSeparator + "./user2.jar" + File.pathSeparator + "./user3.jar";

        assertThat(BatchEnvironment.createClassPath(classPath, systemPath).toString().split(File.pathSeparator), arrayWithSize(4));
    }
 }
