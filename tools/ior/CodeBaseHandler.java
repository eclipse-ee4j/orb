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

import org.omg.IOP.Codec;
import org.omg.CORBA.Any;
import org.omg.CORBA.TypeCode;

/**
 * I can't find a Helper for the Java Codebase
 * tagged component.
 */
public class CodeBaseHandler implements EncapsHandler
{
    public void display(byte[] data,
                        TextOutputHandler out,
                        Utility util)
        throws DecodingException {

        try {

            out.output("type: Java Codebase Component");

            Codec codec = util.getCDREncapsCodec(Utility.GIOP_1_0);

            TypeCode strType = util.getORB().create_string_tc(0);

            Any any = codec.decode_value(data, strType);

            out.output("codebase: " + any.extract_string());

        } catch (Exception ex) {
            throw new DecodingException(ex.getMessage());
        }
    }
}

