/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.orb ;

import java.util.Properties ;

import com.sun.corba.ee.spi.orb.Operation ;
import com.sun.corba.ee.spi.orb.PropertyParser ;

public class NormalParserData extends ParserDataBase {
    private String testData ;

    public NormalParserData( String  propertyName,
        Operation operation, String fieldName, Object defaultValue,
        Object testValue, String testData )
    {
        super( propertyName, operation, fieldName, defaultValue, testValue ) ;
        this.testData = testData ;
    }
    public void addToParser( PropertyParser parser ) 
    {
        parser.add( getPropertyName(), getOperation(), getFieldName() ) ;
    }

    public void addToProperties( Properties props ) 
    {
        props.setProperty( getPropertyName(), testData ) ;
    }
}

