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

package org.omg.CORBA;

/**
 * A request or reply from the network is structurally invalid. 
 * This error typically indicates a bug in either the client-side 
 * or server-side run time. For example, if a reply from the server 
 * indicates that the message contains 1000 bytes, but the actual 
 * message is shorter or longer than 1000 bytes, the ORB raises 
 * this exception. <tt>MARSHAL</tt> can also be caused by using 
 * the DII or DSI incorrectly, for example, if the type of the 
 * actual parameters sent does not agree with IDL signature of an 
 * operation.<P>
 * It contains a minor code, which gives more detailed information about
 * what caused the exception, and a completion status. It may also contain
 * a string describing the exception.
 * <P>
 * See the section <A href="../../../../guide/idl/jidlExceptions.html#minorcodemeanings">Minor
 * Code Meanings</A> to see the minor codes for this exception.
 *
 * @version     1.18, 09/09/97
 * @since       JDK1.2
 */

// @SuppressWarnings({"serial"})
public final class MARSHAL extends SystemException {
    /**
     * Constructs a <code>MARSHAL</code> exception with a default minor code
     * of 0, a completion state of CompletionStatus.COMPLETED_NO,
     * and a null description.
     */
    public MARSHAL() {
        this("");
    }

    /**
     * Constructs a <code>MARSHAL</code> exception with the specified description message,
     * a minor code of 0, and a completion state of COMPLETED_NO.
     * @param s the String containing a description of the exception
     */
    public MARSHAL(String s) {
        this(s, 0, CompletionStatus.COMPLETED_NO);
    }

    /**
     * Constructs a <code>MARSHAL</code> exception with the specified
     * minor code and completion status.
     * @param minor the minor code
     * @param completed the completion status
     */
    public MARSHAL(int minor, CompletionStatus completed) {
        this("", minor, completed);
    }

    /**
     * Constructs a <code>MARSHAL</code> exception with the specified description
     * message, minor code, and completion status.
     * @param s the String containing a description message
     * @param minor the minor code
     * @param completed the completion status
     */
    public MARSHAL(String s, int minor, CompletionStatus completed) {
        super(s, minor, completed);
    }
}
