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

import com.sun.corba.ee.org.omg.CORBA.SUNVMCID;

/**
 * The root class for all CORBA standard exceptions. These exceptions
 * may be thrown as a result of any CORBA operation invocation and may
 * also be returned by many standard CORBA API methods. The standard
 * exceptions contain a minor code, allowing more detailed specification, and a
 * completion status. This class is subclassed to
 * generate each one of the set of standard ORB exceptions.
 * <code>SystemException</code> extends
 * <code>java.lang.RuntimeException</code>; thus none of the
 * <code>SystemException</code> exceptions need to be
 * declared in signatures of the Java methods mapped from operations in
 * IDL interfaces.
 *
 * @see <A href="../../../../guide/idl/jidlExceptions.html">documentation on
 * Java&nbsp;IDL exceptions</A>
 */

public abstract class SystemException extends java.lang.RuntimeException {

    /**
     * The CORBA Exception minor code.
     * @serial
     */
    public int minor;

    /**
     * The status of the operation that threw this exception.
     * @serial
     */
    public CompletionStatus completed;

    /**
     * Constructs a <code>SystemException</code> exception with the specified detail
     * message, minor code, and completion status.
     * A detail message is a String that describes this particular exception.
     * @param reason the String containing a detail message
     * @param minor the minor code
     * @param completed the completion status
     */
    protected SystemException(String reason, int minor, CompletionStatus completed) {
        super(reason);
        this.minor = minor;
        this.completed = completed;
    }

    /**
     * Converts this exception to a representative string.
     */
    public String toString() {
        // The fully qualified exception class name
        String result = super.toString();

        // The vmcid part
        int vmcid = minor & 0xFFFFF000;
        switch (vmcid) {
            case OMGVMCID.value:
                result += "  vmcid: OMG";
                break;
            case SUNVMCID.value:
                result += "  vmcid: SUN";
                break;
            default:
                result += "  vmcid: 0x" + Integer.toHexString(vmcid);
                break;
        }

        // The minor code part
        int mc = minor & 0x00000FFF;
        result += "  minor code: " + mc;

        // The completion status part
        switch (completed.value()) {
            case CompletionStatus._COMPLETED_YES:
                result += "  completed: Yes";
                break;
            case CompletionStatus._COMPLETED_NO:
                result += "  completed: No";
                break;
            case CompletionStatus._COMPLETED_MAYBE:
            default:
                result += " completed: Maybe";
                break;
        }
        return result;
    }
}
