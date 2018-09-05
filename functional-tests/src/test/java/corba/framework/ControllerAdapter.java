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

import java.io.*;
import java.util.*;
import test.*;

/**
 * Adapter class providing some convenient default implementations for
 * the Controller interface.
 */
public abstract class ControllerAdapter implements Controller
{
    protected String className;
    protected String processName;
    protected Properties environment;
    protected String VMArgs[];
    protected String programArgs[];
    protected OutputStream out;
    protected OutputStream err;
    protected Hashtable extra;
    
    public void initialize(String className,
                           String processName,
                           Properties environment,
                           String VMArgs[],
                           String programArgs[],
                           OutputStream out,
                           OutputStream err,
                           Hashtable extra) throws Exception
    {
        this.className = className;
        this.processName = processName;
        this.environment = environment;
        this.VMArgs = VMArgs;
        this.programArgs = programArgs;
        this.out = out;
        this.err = err;
        this.extra = extra;
      
        // Make life a little easier
        if (this.environment == null)
            this.environment = new Properties();
        if (this.VMArgs == null)
            this.VMArgs = new String[0];
        if (this.programArgs == null)
            this.programArgs = new String[0];
    }
   
    public OutputStream getOutputStream()
    {
        return out;
    }
    
    public OutputStream getErrorStream()
    {
        return err;
    }

    public String getProcessName()
    {
        return processName;
    }

    public String getClassName()
    {
        return className;
    }
}
