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

package javax.rmi.CORBA;

/**
 * Java to IDL ptc 02-01-12 1.5.1.5
 */
public interface ValueHandlerMultiFormat extends ValueHandler {

    /**
     * Returns the maximum stream format version for
     * RMI/IDL custom value types that is supported
     * by this ValueHandler object. The ValueHandler
     * object must support the returned stream format version and
     * all lower versions.
     *
     * An ORB may use this value to include in a standard
     * IOR tagged component or service context to indicate to other
     * ORBs the maximum RMI-IIOP stream format that it
     * supports.  If not included, the default for GIOP 1.2
     * is stream format version 1, and stream format version
     * 2 for GIOP 1.3 and higher.
     * @return the maximum version supported
     */
    byte getMaximumStreamFormatVersion();

    /**
     * Allows the ORB to pass the stream format
     * version for RMI/IDL custom value types. If the ORB
     * calls this method, it must pass a stream format version
     * between 1 and the value returned by the
     * getMaximumStreamFormatVersion method inclusive,
     * or else a BAD_PARAM exception with standard minor code
     * will be thrown.
     * <p>
     * If the ORB calls the older ValueHandler.writeValue(OutputStream,
     * Serializable) method, stream format version 1 is implied.
     * </p>
     * The ORB output stream passed to the ValueHandlerMultiFormat.writeValue
     * method must implement the ValueOutputStream interface, and the
     * ORB input stream passed to the ValueHandler.readValue method must
     * implement the ValueInputStream interface.
     * @param out stream to write the value out to
     * @param value a {@link java.io.Serializable} value to write
     * @param streamFormatVersion stream format version
     */
    void writeValue(org.omg.CORBA.portable.OutputStream out,
                    java.io.Serializable value,
                    byte streamFormatVersion);
}
