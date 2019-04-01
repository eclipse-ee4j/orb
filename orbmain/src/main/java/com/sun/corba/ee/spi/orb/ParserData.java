/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.orb;

import java.util.Properties;

public interface ParserData {
    public String getPropertyName();

    public Operation getOperation();

    public String getFieldName();

    public Object getDefaultValue();

    public Object getTestValue();

    public void addToParser(PropertyParser parser);

    public void addToProperties(Properties props);
}
