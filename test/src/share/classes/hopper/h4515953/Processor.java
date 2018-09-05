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
import org.omg.CORBA.portable.IDLEntity;

public interface Processor extends Remote {
    
    // Fails since the import statement specifies
    // the original portable InputStream
    public IDLEntity testIDLEntity(IDLEntity input) throws RemoteException;

    // Works since a local 2.3 InputStream is defined
    public TestInterface testInterface(TestInterface input) throws RemoteException;
}
