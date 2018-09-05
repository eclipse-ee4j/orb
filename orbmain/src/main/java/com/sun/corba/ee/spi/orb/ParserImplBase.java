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

import java.util.Map ;
import java.util.Set ;
import java.util.Iterator ;
import java.util.Properties ;

import java.security.PrivilegedExceptionAction ;
import java.security.PrivilegedActionException ;
import java.security.AccessController ;

import java.lang.reflect.Field ;

import com.sun.corba.ee.spi.logging.ORBUtilSystemException ;

public abstract class ParserImplBase {
    private static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    protected abstract PropertyParser makeParser() ;

    /** Override this method if there is some needed initialization
    * that takes place after argument parsing.
    */
    protected void complete() 
    {
    }

    public ParserImplBase()
    {
    }

    public void init( DataCollector coll )
    {
        PropertyParser parser = makeParser() ;
        coll.setParser( parser ) ;
        Properties props = coll.getProperties() ;
        Map map = parser.parse( props ) ;
        setFields( map ) ;

        // Make sure that any extra initialization takes place after all the
        // fields are set from the map.
        complete() ;
    }

    private Field getAnyField( String name )
    {
        Field result = null ;

        try {
            Class cls = this.getClass() ;
            result = cls.getDeclaredField( name ) ;
            while (result == null) {
                cls = cls.getSuperclass() ;
                if (cls == null) {
                    break;
                }

                result = cls.getDeclaredField( name ) ;
            }
        } catch (Exception exc) {
            throw wrapper.fieldNotFound( exc, name ) ;
        }

        if (result == null) {
            throw wrapper.fieldNotFound(name);
        }

        return result ;
    }

    protected void setFields( Map map )
    {
        Set entries = map.entrySet() ;
        Iterator iter = entries.iterator() ;
        while (iter.hasNext()) {
            java.util.Map.Entry entry = (java.util.Map.Entry)(iter.next()) ;
            final String name = (String)(entry.getKey()) ;
            final Object value = entry.getValue() ;

            try {
                AccessController.doPrivileged( 
                    new PrivilegedExceptionAction() {
                        public Object run() throws IllegalAccessException, 
                            IllegalArgumentException
                        {
                            Field field = getAnyField( name ) ;
                            field.setAccessible( true ) ;
                            field.set( ParserImplBase.this, value ) ;
                            return null ;
                        }
                    } 
                ) ;
            } catch (PrivilegedActionException exc) {
                // Since exc wraps the actual exception, use exc.getCause()
                // instead of exc.
                throw wrapper.errorSettingField( exc.getCause(), name, value ) ;
            }
        }
    }
}
