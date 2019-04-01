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

package org.omg.CORBA.portable;

import org.omg.CORBA.SystemException;

/**
 * The Indirection exception is a Java specific system exception. It is thrown when the ORB's input stream is called to
 * demarshal a value that is encoded as an indirection that is in the process of being demarshaled. This can occur when
 * the ORB input stream calls the ValueHandler to demarshal an RMI value whose state contains a recursive reference to
 * itself. Because the top-level ValueHandler.read_value() call has not yet returned a value, the ORB input stream's
 * indirection table does not contain an entry for an object with the stream offset specified by the indirection tag.
 * The stream offset is returned in the exception's offset field.
 *
 * @see org.omg.CORBA_2_3.portable.InputStream
 * @see org.omg.CORBA_2_3.portable.OuputStream
 */
public class IndirectionException extends SystemException {

    /**
     * Points to the stream's offset.
     */
    public int offset;

    /**
     * Creates an IndirectionException with the right offset value. The stream offset is returned in the exception's offset
     * field. This exception is constructed and thrown during reading recursively defined values off of a stream.
     *
     * @param offset the stream offset where recursion is detected.
     */
    public IndirectionException(int offset) {
        super("", 0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
        this.offset = offset;
    }
}
