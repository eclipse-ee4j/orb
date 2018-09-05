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

import com.sun.corba.ee.spi.orb.ParserData ;
import com.sun.corba.ee.spi.orb.Operation ;

public abstract class ParserDataBase implements ParserData {
    private String  propertyName ;
    private Operation operation ;       
    private String fieldName ;
    private Object defaultValue ;
    private Object testValue ;

    protected ParserDataBase( String  propertyName,
        Operation operation, String fieldName, Object defaultValue,
        Object testValue )
    {
        this.propertyName = propertyName  ;
        this.operation = operation  ;   
        this.fieldName = fieldName  ;
        this.defaultValue = defaultValue  ;
        this.testValue = testValue  ;
    }

    public String  getPropertyName() { return propertyName ; }
    public Operation getOperation() { return operation ; }
    public String getFieldName() { return fieldName ; }
    public Object getDefaultValue() { return defaultValue ; }
    public Object getTestValue() { return testValue ; }
}
