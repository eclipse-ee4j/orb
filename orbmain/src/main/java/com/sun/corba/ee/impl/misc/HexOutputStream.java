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

package com.sun.corba.ee.impl.misc;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;

/**
 * Writes each input byte as a 2 byte hexidecimal output pair making it
 * possible to turn arbitrary binary data into an ASCII format.
 * The high 4 bits of the byte is translated into the first byte.
 *
 * @author      Jeff Nisewanger
 */
public class HexOutputStream extends OutputStream
{
    static private final char hex[] = {
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };

    private StringWriter writer; 

    /**
     * Creates a new HexOutputStream.
     * @param w The underlying StringWriter.
     */
    public
        HexOutputStream(StringWriter w) {
        writer = w;
    }


    /**
     * Writes a byte. Will block until the byte is actually
     * written.
     * param b The byte to write out.
     * @exception java.io.IOException I/O error occurred.
     */
    public synchronized void write(int b) throws IOException {
        writer.write(hex[((b >> 4) & 0xF)]);
        writer.write(hex[((b >> 0) & 0xF)]);
    }

    public synchronized void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    public synchronized void write(byte[] b, int off, int len)
        throws IOException
    {
        for(int i=0; i < len; i++) {
            write(b[off + i]);
        }
    }
}

