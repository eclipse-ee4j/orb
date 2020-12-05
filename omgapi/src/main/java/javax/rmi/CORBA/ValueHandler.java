/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
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
 * Defines methods which allow serialization of Java objects
 * to and from GIOP streams.
 **/
public interface ValueHandler {

    /**
     * Writes a value to the stream using Java semantics.
     * @param out the stream to write the value to.
     * @param value the value to be written to the stream.
     **/
    void writeValue(org.omg.CORBA.portable.OutputStream out,
                    java.io.Serializable value);

    /**
     * Reads a value from the stream using Java semantics.
     * @param in the stream to read the value from.
     * @param offset the current position in the input stream.
     * @param clz the type of the value to be read in.
     * @param repositoryID the RepositoryId of the value to be read in.
     * @param sender the sending context runtime codebase.
     * @return the value read from the stream.
     **/
    java.io.Serializable readValue(org.omg.CORBA.portable.InputStream in,
                                   int offset,
                                   java.lang.Class clz, 
                                   String repositoryID,
                                   org.omg.SendingContext.RunTime sender);

    /**
     * Returns the CORBA RepositoryId for the given Java class.
     * @param clz a Java class.
     * @return the CORBA RepositoryId for the class.
     **/
    java.lang.String getRMIRepositoryID(java.lang.Class clz);

    /**
     * Indicates whether the given class performs custom or
     * default marshaling.
     * @param clz the class to test for custom marshaling.
     * @return <code>true</code> if the class performs custom marshaling, <code>false</code>
     * if it does not.
     **/
    boolean isCustomMarshaled(java.lang.Class clz);

    /**
     * Returns the CodeBase for this ValueHandler.  This is used by
     * the ORB runtime.  The server sends the service context containing
     * the IOR for this CodeBase on the first GIOP reply.  The client
     * does the same on the first GIOP request.
     * @return the SendingContext.CodeBase of this ValueHandler.
     **/
    org.omg.SendingContext.RunTime getRunTimeCodeBase();

    /**
     * If the value contains a <code>writeReplace</code> method then the result
     * is returned.  Otherwise, the value itself is returned.
     * @param value the value to be marshaled.
     * @return the true value to marshal on the wire.
     **/
    java.io.Serializable writeReplace(java.io.Serializable value);

}
