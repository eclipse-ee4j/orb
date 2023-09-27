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

package com.sun.corba.ee.impl.orb;

import java.util.Properties;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;

import java.lang.reflect.Array;

import com.sun.corba.ee.spi.orb.ORB;

import com.sun.corba.ee.spi.orb.Operation;

import com.sun.corba.ee.spi.logging.ORBUtilSystemException;
import org.glassfish.pfl.basic.contain.Pair;

public class PrefixParserAction extends ParserActionBase {
    private static final ORBUtilSystemException wrapper = ORBUtilSystemException.self;

    private Class componentType;

    public PrefixParserAction(String propertyName, Operation operation, String fieldName, Class componentType) {
        super(propertyName, true, operation, fieldName);
        this.componentType = componentType;
    }

    /**
     * For each String s that matches the prefix given by getPropertyName(), apply getOperation() to { suffix( s ), value }
     * and add the result to an Object[] which forms the result of apply. Returns null if there are no matches.
     */
    public Object apply(Properties props) {
        String prefix = getPropertyName();
        int prefixLength = prefix.length();
        if (prefix.charAt(prefixLength - 1) != '.') {
            prefix += '.';
            prefixLength++;
        }

        List matches = new LinkedList();

        // Find all keys in props that start with propertyName
        Iterator iter = props.keySet().iterator();
        while (iter.hasNext()) {
            String key = (String) (iter.next());
            if (key.startsWith(prefix)) {
                String suffix = key.substring(prefixLength);
                String value = props.getProperty(key);
                Pair<String, String> data = new Pair<String, String>(suffix, value);
                Object result = getOperation().operate(data);
                matches.add(result);
            }
        }

        int size = matches.size();
        if (size > 0) {
            // Convert the list into an array of the proper type.
            // An Object[] as a result does NOT work. Also report
            // any errors carefully, as errors here or in parsers that
            // use this Operation often show up at ORB.init().
            Object result = null;
            try {
                result = Array.newInstance(componentType, size);
            } catch (Throwable thr) {
                throw wrapper.couldNotCreateArray(thr, getPropertyName(), componentType, size);
            }

            Iterator iter2 = matches.iterator();
            int ctr = 0;
            while (iter2.hasNext()) {
                Object obj = iter2.next();

                try {
                    Array.set(result, ctr, obj);
                } catch (Throwable thr) {
                    throw wrapper.couldNotSetArray(thr, getPropertyName(), ctr, componentType, size, obj);
                }
                ctr++;
            }

            return result;
        } else {
            return null;
        }
    }
}
