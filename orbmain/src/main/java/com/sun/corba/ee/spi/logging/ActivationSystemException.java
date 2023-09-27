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
