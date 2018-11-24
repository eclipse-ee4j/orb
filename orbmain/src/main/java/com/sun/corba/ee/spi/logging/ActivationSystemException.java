/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.logging;

import com.sun.corba.ee.spi.logex.corba.ORBException;
import com.sun.corba.ee.spi.logex.corba.CorbaExtension;

import org.glassfish.pfl.basic.logex.Chain;
import org.glassfish.pfl.basic.logex.ExceptionWrapper;
import org.glassfish.pfl.basic.logex.Log;
import org.glassfish.pfl.basic.logex.LogLevel;
import org.glassfish.pfl.basic.logex.Message;
import org.glassfish.pfl.basic.logex.WrapperGenerator;

import org.omg.CORBA.INITIALIZE;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.OBJECT_NOT_EXIST;

@ExceptionWrapper(idPrefix = "IOP")
@ORBException(omgException = false, group = CorbaExtension.ActivationGroup)
public interface ActivationSystemException {
    ActivationSystemException self = WrapperGenerator.makeWrapper(ActivationSystemException.class, CorbaExtension.self);

    @Log(level = LogLevel.WARNING, id = 1)
    @Message("Cannot read repository datastore")
    INITIALIZE cannotReadRepositoryDb(@Chain Exception exc);

    @Log(level = LogLevel.WARNING, id = 2)
    @Message("Cannot add initial naming")
    INITIALIZE cannotAddInitialNaming();

    @Log(level = LogLevel.WARNING, id = 1)
    @Message("Cannot write repository datastore")
    INTERNAL cannotWriteRepositoryDb(@Chain Exception exc);

    @Log(level = LogLevel.WARNING, id = 3)
    @Message("Server not expected to register")
    INTERNAL serverNotExpectedToRegister();

    @Log(level = LogLevel.WARNING, id = 4)
    @Message("Unable to start server process")
    INTERNAL unableToStartProcess();

    @Log(level = LogLevel.WARNING, id = 6)
    @Message("Server is not running")
    INTERNAL serverNotRunning();

    @Log(level = LogLevel.WARNING, id = 1)
    @Message("Error in BadServerIdHandler")
    OBJECT_NOT_EXIST errorInBadServerIdHandler(@Chain Exception exc);
}
