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
