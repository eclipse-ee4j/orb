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
