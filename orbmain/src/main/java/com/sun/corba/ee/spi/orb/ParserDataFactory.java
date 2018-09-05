/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.orb ;

import com.sun.corba.ee.impl.orb.NormalParserData ;
import com.sun.corba.ee.impl.orb.PrefixParserData ;
import org.glassfish.pfl.basic.contain.Pair;

public class ParserDataFactory {
    public static ParserData make( String  propertyName,
        Operation operation, String fieldName, Object defaultValue,
        Object testValue, String testData )
    {
        return new NormalParserData( propertyName, operation, fieldName,
            defaultValue, testValue, testData ) ;
    }

    public static ParserData make( String  propertyName,
        Operation operation, String fieldName, Object defaultValue,
        Object testValue, Pair<String,String>[] testData, Class componentType )
    {
        return new PrefixParserData( propertyName, operation, fieldName,
            defaultValue, testValue, testData, componentType ) ;
    }
}
