/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
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
