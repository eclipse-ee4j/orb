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

import java.util.List ;
import java.util.LinkedList ;
import java.util.Map ;
import java.util.HashMap ;
import java.util.Iterator ;
import java.util.Properties ;

import com.sun.corba.ee.impl.orb.ParserAction ;
import com.sun.corba.ee.impl.orb.ParserActionFactory ;

public class PropertyParser {
    private List<ParserAction> actions ;

    public PropertyParser( ) 
    {
        actions = new LinkedList<ParserAction>() ;
    }

    public PropertyParser add( String propName, 
        Operation action, String fieldName )
    {
        actions.add( ParserActionFactory.makeNormalAction( propName, 
            action, fieldName ) ) ;
        return this ;
    }

    public PropertyParser addPrefix( String propName, 
        Operation action, String fieldName, Class<?> componentType )
    {
        actions.add( ParserActionFactory.makePrefixAction( propName, 
            action, fieldName, componentType ) ) ;
        return this ;
    }

    /** Return a map from field name to value.
     * @param props properties to convert
     * @return unsynchonized Map
    */
    public Map<String,Object> parse( Properties props )
    {
        Map<String,Object> map = new HashMap<String,Object>() ;
        Iterator<ParserAction> iter = actions.iterator() ;
        while (iter.hasNext()) {
            ParserAction act = iter.next() ;
            Object result = act.apply( props ) ; 
                
            // A null result means that the property was not set for
            // this action, so do not override the default value in this case.
            if (result != null) {
                map.put(act.getFieldName(), result);
            }
        }

        return map ;
    }

    public Iterator<ParserAction> iterator()
    {
        return actions.iterator() ;
    }
}
