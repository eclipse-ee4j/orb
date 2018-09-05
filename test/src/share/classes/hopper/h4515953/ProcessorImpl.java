/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package hopper.h4515953;

import java.rmi.*;
import javax.rmi.PortableRemoteObject;
import org.omg.CORBA.portable.IDLEntity;

public class ProcessorImpl extends PortableRemoteObject implements Processor {

    public ProcessorImpl() throws RemoteException {}

    // Fails since the import statement specifies
    // the original portable InputStream
    public IDLEntity testIDLEntity(IDLEntity input) { return input; }

    // Works since a local 2.3 InputStream is defined
    public TestInterface testInterface(TestInterface input) { return input; }
}
