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

package com.sun.corba.ee.spi.orb ;

import com.sun.corba.ee.spi.logging.ORBUtilSystemException ;

import java.lang.reflect.Field ;
import java.security.AccessController ;
import java.security.PrivilegedActionException ;
import java.security.PrivilegedExceptionAction ;
import java.util.Iterator ;
import java.util.Map ;
import java.util.Properties ;
import java.util.Set ;

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
