/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
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
     */
    Method getMethod();

    /**
     * Copy the arguments as needed for this particular method. Can be optimized so that as little copying as possible is
     * performed.
     */
    Object[] copyArguments(Object[] args, ORB orb) throws RemoteException;

    /**
     * Read the arguments for this method from the InputStream. Returns null if there are no arguments.
     */
    Object[] readArguments(InputStream is);

    /**
     * Write arguments for this method to the OutputStream. Does nothing if there are no arguments.
     */
    void writeArguments(OutputStream os, Object[] args);

    /**
     * Copy the result as needed for this particular method. Can be optimized so that as little copying as possible is
     * performed.
     */
    Object copyResult(Object result, ORB orb) throws RemoteException;

    /**
     * Read the result from the InputStream. Returns null if the result type is null.
     */
    Object readResult(InputStream is);

    /**
     * Write the result to the OutputStream. Does nothing if the result type is null.
     */
    void writeResult(OutputStream os, Object result);

    /**
     * Returns true iff thr's class is a declared exception (or a subclass of a declared exception) for this
     * DynamicMethodMarshaller's method.
     */
    boolean isDeclaredException(Throwable thr);

    /**
     * Write the repository ID of the exception and the value of the exception to the OutputStream. ex should be a declared
     * exception for this DynamicMethodMarshaller's method.
     */
    void writeException(OutputStream os, Exception ex);

    /**
     * Reads an exception ID and the corresponding exception from the input stream. This should be an exception declared in
     * this method.
     */
    Exception readException(ApplicationException ae);
}
