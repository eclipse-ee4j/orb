/*
 * Copyright (c) 2018, 2019, Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.rmic.tools.java;

import java.io.IOException;
import java.io.InputStream;

public interface ClassDefinitionFactory {
    ClassDefinition loadDefinition(InputStream is, Environment env) throws IOException;

    int getMaxClassVersion();
}
