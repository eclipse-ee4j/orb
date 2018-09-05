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

import com.sun.corba.ee.spi.orb.Operation ;

public class ParserActionFactory{
    private ParserActionFactory() {}

    public static ParserAction makeNormalAction( String propertyName,
        Operation operation, String fieldName )
    {
        return new NormalParserAction( propertyName, operation, fieldName ) ;
    }

    public static ParserAction makePrefixAction( String propertyName,
        Operation operation, String fieldName, Class componentType )
    {
        return new PrefixParserAction( propertyName, operation, fieldName, componentType ) ;
    }
}
