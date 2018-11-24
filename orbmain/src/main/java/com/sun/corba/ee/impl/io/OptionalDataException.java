/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.io;

/**
 * Unexpected data appeared in an ObjectInputStream trying to read an Object. This exception occurs when the stream
 * contains primitive data instead of the object expected by readObject. The eof flag in the exception is true to
 * indicate that no more primitive data is available. The count field contains the number of bytes available to read.
 *
 * @author unascribed
 * @version 1.7, 11/02/98
 * @since JDK1.1
 */
public class OptionalDataException extends java.io.IOException {
    /*
     * Create an <code>OptionalDataException</code> with a length.
     */
    OptionalDataException(int len) {
        eof = false;
        length = len;
    }

    /*
     * Create an <code>OptionalDataException</code> signifing no more primitive data is available.
     */
    OptionalDataException(boolean end) {
        length = 0;
        eof = end;
    }

    /**
     * The number of bytes of primitive data available to be read in the current buffer.
     */
    public int length;

    /**
     * True if there is no more data in the buffered part of the stream.
     */
    public boolean eof;
}
