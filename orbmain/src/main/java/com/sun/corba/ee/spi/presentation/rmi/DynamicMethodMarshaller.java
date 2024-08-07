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

package com.sun.corba.ee.spi.presentation.rmi;

import org.omg.CORBA.ORB;
import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.CORBA_2_3.portable.OutputStream;
import org.omg.CORBA.portable.ApplicationException;

import java.lang.reflect.Method;

import java.rmi.RemoteException;

/**
 * Used to read and write arguments and results for a particular method.
 *
 */
public interface DynamicMethodMarshaller {
    /**
     * Returns the method used to create this DynamicMethodMarshaller.
     * 
     * @return the method used
     */
    Method getMethod();

    /**
     * Copy the arguments as needed for this particular method. Can be optimized so that as little copying as possible is
     * performed.
     * 
     * @param args arguments to copy
     * @param orb ORB to use
     * @throws RemoteException if there is a remote error
     * @return a copy of the arguments
     */
    Object[] copyArguments(Object[] args, ORB orb) throws RemoteException;

    /**
     * Read the arguments for this method from the InputStream. Returns null if there are no arguments.
     * 
     * @param is stream to read arguments from
     * @return array of arguments
     */
    Object[] readArguments(InputStream is);

    /**
     * Write arguments for this method to the OutputStream. Does nothing if there are no arguments.
     * 
     * @param os stream to write to
     * @param args arguments to write
     */
    void writeArguments(OutputStream os, Object[] args);

    /**
     * Copy the result as needed for this particular method. Can be optimized so that as little copying as possible is
     * performed.
     * 
     * @param result Object to copy
     * @param orb ORB to use for copying
     * @throws RemoteException if there is an error with the ORB
     * @return copied object
     */
    Object copyResult(Object result, ORB orb) throws RemoteException;

    /**
     * Read the result from the InputStream. Returns null if the result type is null.
     * 
     * @param is stream to read from
     * @return result
     */
    Object readResult(InputStream is);

    /**
     * Write the result to the OutputStream. Does nothing if the result type is null.
     * 
     * @param os stream to write to
     * @param result result to write to stream
     */
    void writeResult(OutputStream os, Object result);

    /**
     * Returns true iff thr's class is a declared exception (or a subclass of a declared exception) for this
     * DynamicMethodMarshaller's method.
     * 
     * @param thr throwable to checl
     * @return if it is a declared (non-runtime) exception
     */
    boolean isDeclaredException(Throwable thr);

    /**
     * Write the repository ID of the exception and the value of the exception to the OutputStream. ex should be a declared
     * exception for this DynamicMethodMarshaller's method.
     * 
     * @param os stream to write to
     * @param ex exception to write
     */
    void writeException(OutputStream os, Exception ex);

    /**
     * Reads an exception ID and the corresponding exception from the input stream. This should be an exception declared in
     * this method.
     * 
     * @param ae id of exception to read
     * @return read exception
     */
    Exception readException(ApplicationException ae);
}
