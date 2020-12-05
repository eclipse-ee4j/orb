/*
 * Copyright (c) 2018, 2020 Oracle and/or its affiliates.
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

public class TwoLevelNested {

    public class Level1 {

        public class Level2 implements Remote, Cloneable {
            public void level2Execute() throws RemoteException {
                System.out.println("Level2.level2Execute executed");
            }
        }
    }
/*

    void tryThis() {
        new Runnable() {
            @Override
            public void run() {
                System.out.println("Called inner class");
            }
        };
    }
*/
}
