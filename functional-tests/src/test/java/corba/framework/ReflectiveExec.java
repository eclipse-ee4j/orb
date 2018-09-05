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

import java.util.Enumeration;

import test.Test;

public class ReflectiveExec extends ExternalExec {
    protected String[] buildCommand()
    {
        // Command line array:

        // [java executable]
        // [arguments to the java executable]
        // [-D environment variables]
        // [debug arguments to the java executable]
        // corba.framwork.ReflectiveWrapper
        // [class name]
        // [arguments to the program]

        String[] debugArgs = getDebugVMArgs() ;

        int size = 2 + debugArgs.length + VMArgs.length +
            environment.size() + programArgs.length + 1;

        String cmd [] = new String [size];

        int idx = 0;
        // Java executable
        cmd[idx++] = Options.getJavaExec();

        // Arguments to the java executable
        for(int i = 0; i < VMArgs.length; i++)
            cmd[idx++] = VMArgs[i];

        // -D environment variables
        Enumeration names = environment.propertyNames();
        while(names.hasMoreElements()) {
            String name =(String) names.nextElement();
            cmd[idx++] = "-D" + name + "="
                + environment.getProperty(name);
        }

        // Debugging arguments, if any
        for(int i = 0; i < debugArgs.length; i++ )
            cmd[idx++] = debugArgs[i];


        cmd[idx++] = "corba.framework.ReflectiveWrapper";

        // Class name
        cmd[idx++] = className;

        // Arguments to the program
        for(int i = 0; i < programArgs.length; i++)
            cmd[idx++] = programArgs[i];

        Test.dprint("--------");
        for(int i = 0; i < cmd.length; i++)
            Test.dprint("" + i + ": " + cmd[i]);
        Test.dprint("--------");

        return cmd;
    }

}
