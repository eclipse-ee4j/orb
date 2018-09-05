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

public class NormalParserAction extends ParserActionBase {
    public NormalParserAction( String propertyName, 
        Operation operation, String fieldName )
    {
        super( propertyName, false, operation, fieldName ) ;
    }

    /** Create a String[] of all suffixes of property names that
     * match the propertyName prefix, pass this to op, and return the
     * result.
     */
    public Object apply( Properties props ) 
    {
        Object value = props.getProperty( getPropertyName() ) ;
        if (value != null)
            return getOperation().operate( value ) ;
        else 
            return null ;
    }
}

