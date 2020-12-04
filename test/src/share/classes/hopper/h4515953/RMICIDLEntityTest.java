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

import test.Test;
import corba.framework.*;
import java.util.*;
import com.sun.corba.ee.spi.misc.ORBConstants;
import org.omg.CORBA.*;

/**
 * A very simple test to make sure that RMIC compilation works for Interfaces
 * which has methods with IDLEntity as parameters or return values.
 */
public class RMICIDLEntityTest extends CORBATest
{
    public static final String[] javaFiles = { "Processor.java",
                                               "ProcessorImpl.java" };

    public static final String[] rmiClasses = { "hopper/h4515953/Processor.class" };


    protected void doTest() throws Throwable
    {
        Options.setJavaFiles(javaFiles);
        compileJavaFiles();

        Options.setRMICClasses( rmiClasses );
        compileRMICFiles();

    }
}
    
