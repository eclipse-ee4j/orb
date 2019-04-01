/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998 IBM Corp. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.rmic;

import org.glassfish.rmic.tools.java.ClassDefinition;

import java.io.File;

/**
 * A placeholder to handle the no-longer supported JRMP flags. Any attempt to use them will now result in an error.
 *
 * @author Peter Jones, Bryan Atsatt
 */
public class RMIGenerator implements RMIConstants, Generator {

    /**
     * Examine and consume command line arguments.
     *
     * @param argv The command line arguments. Ignore null and unknown arguments. Set each consumed argument to null.
     * @param main Report any errors using the main.error() methods.
     * @return true if no errors, false otherwise.
     */
    public boolean parseArgs(String argv[], Main main) {
        main.error("rmic.jrmp.not.supported", main.program);
        return false;
    }

    /**
     * Generate the source files for the stub and/or skeleton classes needed by RMI for the given remote implementation
     * class.
     *
     * @param env compiler environment
     * @param destDir directory for the root of the package hierarchy
     * @param cdef definition of remote implementation class to generate stubs and/or skeletons for
     */
    public void generate(BatchEnvironment env, File destDir, ClassDefinition cdef) {
    }

}
