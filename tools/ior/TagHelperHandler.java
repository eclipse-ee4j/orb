/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package tools.ior;

import java.lang.reflect.*;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.Any;
import org.omg.IOP.Codec;

/**
 * If you have a Helper class which will read the type of
 * thing you want to map from a tag, you don't have to
 * write an EncapsHandler, just specify the helper
 * in the appropriate setup file.
 */
public class TagHelperHandler implements EncapsHandler
{
    private TypeCode typeCode;
    private Method extractMethod;
    private Codec codec;

    private static final Class[] EXTRACT_ARG_TYPES
        = new Class[] { org.omg.CORBA.Any.class };

    // Surely these are already defined somewhere
    private static final Class[] NO_ARG_TYPES = new Class[] {};
    private static final Object[] NO_ARGS = new Object[] {};

    public TagHelperHandler(String helperClassName, Codec codec)
        throws ClassNotFoundException, 
               IllegalAccessException,
               IllegalArgumentException,
               InvocationTargetException,
               NoSuchMethodException,
               SecurityException {

        // This codec was indicated in the setup file, or
        // defaulted to the GIOP 1.0 CDR Encapsulation Codec.
        this.codec = codec;

        // Find the indicated helper class so we can get the
        // desired type's TypeCode as well as the helper's
        // extract method.
        Class helper = Class.forName(helperClassName);

        typeCode
            = (TypeCode)helper.getDeclaredMethod("type", 
                                                 NO_ARG_TYPES).invoke(null, 
                                                                      NO_ARGS);

        extractMethod
            = helper.getDeclaredMethod("extract", EXTRACT_ARG_TYPES);
    }

    public void display(byte[] data,
                        TextOutputHandler out,
                        Utility util)
        throws DecodingException {

        try {

            out.output("type: " + typeCode.id());

            // Decode using the TypeCode from the helper.
            Any any = codec.decode_value(data, typeCode);

            // Have the helper extract the desired type from
            // the any.
            java.lang.Object value = extractMethod.invoke(null, 
                                                          new Object[] { any });

            // Recursively display the type via reflection.
            util.recursiveDisplay("data", value, out);

        } catch (Exception ex) {
            throw new DecodingException(ex.getMessage());
        }
    }
}
