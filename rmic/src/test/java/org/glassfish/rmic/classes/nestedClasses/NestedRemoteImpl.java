/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package org.glassfish.rmic.classes.nestedClasses;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * A remote implementation that is a nested class, which is the shape that makes IIOP stub generation fail. Unlike
 * {@link TwoLevelNested}, the nested class here implements a real remote interface, so rmic accepts it as a remote
 * implementation and gets as far as generating - and then compiling - the tie.
 */
public class NestedRemoteImpl {

    public interface Svc extends Remote {
        String ping() throws RemoteException;
    }

    public static class Impl implements Svc {
        @Override
        public String ping() throws RemoteException {
            return "pong";
        }
    }
}
