/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.presentation.rmi;

import com.sun.corba.ee.spi.logex.stdcorba.StandardLogger;
import javax.naming.Context;
import org.glassfish.pfl.basic.logex.Chain;
import org.glassfish.pfl.basic.logex.ExceptionWrapper;
import org.glassfish.pfl.basic.logex.Log;
import org.glassfish.pfl.basic.logex.LogLevel;
import org.glassfish.pfl.basic.logex.Message;
import org.glassfish.pfl.basic.logex.WrapperGenerator;

/**
 *
 * @author ken
 */
@ExceptionWrapper(idPrefix = "ORBPRES")
public interface Exceptions {

    Exceptions self = WrapperGenerator.makeWrapper(Exceptions.class, StandardLogger.self);

    int EXCEPTIONS_PER_CLASS = 100;

// JNDISateFactoryImpl
    int JSFI_START = 0;

    @Message("No stub could be created")
    @Log(level = LogLevel.FINE, id = JSFI_START + 1)
    void noStub(@Chain Exception exc);

    @Message("Could not connect stub")
    @Log(level = LogLevel.FINE, id = JSFI_START + 2)
    void couldNotConnect(@Chain Exception exc);

    @Message("Could not get ORB from naming context")
    @Log(level = LogLevel.FINE, id = JSFI_START + 2)
    void couldNotGetORB(@Chain Exception exc, Context nc);

// DynamicStubImpl
    int DSI_START = JSFI_START + EXCEPTIONS_PER_CLASS;

    @Message("ClassNotFound exception in readResolve on class {0}")
    @Log(level = LogLevel.FINE, id = DSI_START + 1)
    void readResolveClassNotFound(@Chain ClassNotFoundException exc, String cname);
}
