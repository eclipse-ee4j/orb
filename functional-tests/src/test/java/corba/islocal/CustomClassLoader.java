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

//
// Created       : 2003 May 18 (Sun) 15:17:31 by Harold Carr.
// Last Modified : 2003 May 19 (Mon) 13:33:33 by Harold Carr.
//

package corba.islocal;

import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import corba.framework.Loader;

public class CustomClassLoader
    extends Loader
{
    private int indent = 0;
    private Hashtable classes = new Hashtable();
    private boolean debug = false;

    public CustomClassLoader()
    {
        addPaths("java.class.path");
    }

    private void addPaths(String propertyName)
    {
        StringTokenizer tokens =
            new StringTokenizer(System.getProperty(propertyName),
                                System.getProperty("path.separator"));
        while (tokens.hasMoreTokens()) {
            addPath(tokens.nextToken());
        }
    }

    protected synchronized Class loadClass(String name, boolean resolve)
        throws ClassNotFoundException
    {
        boolean errorFound = false;
        Class c = null;
        try {

            printIndent(indent++, ">> " + name);

            c = (Class) classes.get(name);
            if (c != null) {
                return c;
            }

            try {
                c = findClass(name);
            } catch (ClassNotFoundException e) {
                c = super.findSystemClass(name);
                return c;
            }
            if (resolve) {
                resolveClass(c);
            }
        } catch (Throwable t) {
            errorFound = true;
            printIndent(--indent, "<E " + name + " " + t.toString());
            if (t instanceof ClassNotFoundException) {
                throw (ClassNotFoundException) t;
            }
        } finally {
            if (! errorFound) {
                printIndent(--indent, "<< " + name);
            }
        }
        classes.put(name, c);
        return c;
    }

    private void printIndent(int amount, String msg)
    {
        if (debug) {
            for (int i = 0; i < amount; i++) {
                System.out.print(" ");
            }
            System.out.println(msg);
        }
    }
}

// End of file.
