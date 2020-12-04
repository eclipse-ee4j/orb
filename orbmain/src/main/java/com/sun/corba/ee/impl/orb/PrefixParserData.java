/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
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

package com.sun.corba.ee.impl.orb ;

import java.util.Properties ;

import com.sun.corba.ee.spi.orb.Operation ;
import com.sun.corba.ee.spi.orb.PropertyParser ;
import org.glassfish.pfl.basic.contain.Pair;

public class PrefixParserData extends ParserDataBase {

    private Pair<String,String>[] testData ;
    private Class componentType ;

    public PrefixParserData( String  propertyName,
        Operation operation, String fieldName, Object defaultValue,
        Object testValue, Pair<String,String>[] testData, Class componentType )
    {
        super( propertyName, operation, fieldName, defaultValue, testValue ) ;
        this.testData = testData ;
        this.componentType = componentType ;
    }

    public void addToParser( PropertyParser parser ) 
    {
        parser.addPrefix( getPropertyName(), getOperation(), getFieldName(), 
            componentType ) ;
    }

    public void addToProperties( Properties props ) 
    {
        for (Pair<String,String> sp : testData) {
            String propName = getPropertyName() ;
            if (propName.charAt( propName.length() - 1 ) != '.')
                propName += "." ;

            props.setProperty( propName + sp.first(), sp.second() ) ;
        }
    }
}
