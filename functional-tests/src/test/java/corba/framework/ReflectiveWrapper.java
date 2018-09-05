/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.framework;

import java.lang.reflect.Method;

import test.ShutdownHook;

public class ReflectiveWrapper {

    public static void main(String[] args) {
        try {
            System.out.println("** Executing ReflectiveWrapper **");
            Runtime.getRuntime().addShutdownHook(new ShutdownHook());
            String[] array = new String[args.length - 1];
            System.arraycopy(args, 1, array, 0, args.length -1);

            Class cls = Class.forName(args[0]);
            Class[] params = { args.getClass() };
            Method method = cls.getDeclaredMethod("main", params);
            method.invoke(null, new Object[] { array });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
