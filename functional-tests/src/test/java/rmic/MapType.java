/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
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

package rmic;

import sun.rmi.rmic.Names;
import sun.rmi.rmic.iiop.Type;
import sun.rmi.rmic.iiop.CompoundType;
import sun.rmi.rmic.iiop.ContextStack;
import sun.tools.java.ClassPath;
import sun.tools.java.ClassDeclaration;
import sun.tools.java.Identifier;

public class MapType extends CompoundType {

    public static boolean resetTypesForEach = false;
        
    private MapType() {
        super(null,0,null);
    }

    public String getTypeDescription () {
        return null;
    }


    public int getCount () {
        return countTypes();
    }
    
    public static Type getType (String className,
                                ContextStack stack) {
            
        if (MapType.resetTypesForEach) {
            stack.getEnv().reset();
        }
            
        Identifier classID = Identifier.lookup(className);
        classID = stack.getEnv().resolvePackageQualifiedName(classID);
        classID = Names.mangleClass(classID);
        ClassDeclaration decl = stack.getEnv().getClassDeclaration(classID);
        return makeType(decl.getType(), null, stack);
    }

    public static void main (String[] args) {

        int status = 0;
        try {
            resetTypesForEach = Boolean.valueOf(args[0]).booleanValue();
            int offset = Integer.parseInt(args[1]);
            TestEnv env = new TestEnv(new ClassPath(args[2]));
            ContextStack stack = new ContextStack(env);

            for (int i = 3; i < args.length; i++)  {
                String className = args[i];
                try {
                    env.reset();
                    int line = offset + i - 2;
                    String num = Integer.toString(line);
                    if (line < 10) num = "    " + num;
                    else if (line < 100) num = "   " + num;
                    else if (line < 1000) num = "  " + num;
                    else if (line < 10000) num = " " + num;
                    System.out.print(num + " - " + className);

                    Type result = getType(className,stack);

                    if (result != null) {
                        if (env.nerrors > 0) {
                            status = 1;
                            System.out.println("!!!Failure: result = " + result.getTypeDescription());   
                        } else {
                            System.out.println(" = " + result.getTypeDescription());
                        }
                    }
                } catch (Throwable e) {
                    if (e instanceof ThreadDeath) throw (ThreadDeath) e;
                    status = 1;
                    System.out.println("!!!Exception: " + className + " caught " + e);
                }
            }
        } catch (Throwable e) {
            System.out.println("!!!Exception: caught " + e);
            status = 1;
        }
                
        System.exit(status);
    }
}
