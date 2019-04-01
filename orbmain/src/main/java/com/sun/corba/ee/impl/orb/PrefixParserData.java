/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.orb;

import java.util.Properties;

import com.sun.corba.ee.spi.orb.Operation;
import com.sun.corba.ee.spi.orb.PropertyParser;
import org.glassfish.pfl.basic.contain.Pair;

public class PrefixParserData extends ParserDataBase {

    private Pair<String, String>[] testData;
    private Class componentType;

    public PrefixParserData(String propertyName, Operation operation, String fieldName, Object defaultValue, Object testValue, Pair<String, String>[] testData,
            Class componentType) {
        super(propertyName, operation, fieldName, defaultValue, testValue);
        this.testData = testData;
        this.componentType = componentType;
    }

    public void addToParser(PropertyParser parser) {
        parser.addPrefix(getPropertyName(), getOperation(), getFieldName(), componentType);
    }

    public void addToProperties(Properties props) {
        for (Pair<String, String> sp : testData) {
            String propName = getPropertyName();
            if (propName.charAt(propName.length() - 1) != '.')
                propName += ".";

            props.setProperty(propName + sp.first(), sp.second());
        }
    }
}
