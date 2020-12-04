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

