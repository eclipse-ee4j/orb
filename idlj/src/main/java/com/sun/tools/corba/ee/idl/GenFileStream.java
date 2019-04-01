/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1997-1999 IBM Corp. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.tools.corba.ee.idl;

// NOTES:

import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class GenFileStream extends PrintWriter {
    public GenFileStream(String filename) {
        // What I really want to do here is:
        // super (byteStream = new ByteArrayOutputStream ());
        // but that isn't legal. The super constructor MUST
        // be called before any instance variables are used.
        // This implementation gets around that problem.
        // <f49747.1>
        // super (tmpByteStream = new ByteArrayOutputStream ());
        // byteStream = tmpByteStream;
        super(tmpCharArrayWriter = new CharArrayWriter());
        charArrayWriter = tmpCharArrayWriter;
        name = filename;
    } // ctor

    public void close() {
        File file = new File(name);
        try {
            if (checkError())
                throw new IOException();
            // <f49747.1>
            // FileOutputStream fileStream = new FileOutputStream (file);
            // fileStream.write (byteStream.toByteArray ());
            // fileStream.close ();
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(charArrayWriter.toCharArray());
            fileWriter.close();
        } catch (IOException e) {
            String[] parameters = { name, e.toString() };
            System.err.println(Util.getMessage("GenFileStream.1", parameters));
        }
        super.close();
    } // close

    public String name() {
        return name;
    } // name

    // <f49747.1>
    // private ByteArrayOutputStream byteStream;
    // private static ByteArrayOutputStream tmpByteStream;
    private CharArrayWriter charArrayWriter;
    private static CharArrayWriter tmpCharArrayWriter;
    private String name;
} // GenFileStream
