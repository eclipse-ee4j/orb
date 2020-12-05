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
public class MethodOverload1 {
    public void foo(){}
    public void foo(int i){}
    public void foo(java.rmi.RemoteException e){}

    public static final String[] IDL_NAMES = {
        "create",
        "foo__",
        "foo__long",
        "foo__java_rmi_RemoteException",
    };
    
    public static final boolean[] CONSTRUCTOR = {
        true,
        false,
        false,
        false,
    };
}

class MethodOverload2 {
    public void create(){}

    public static final String[] IDL_NAMES = {
        "create__",
        "create",
    };
    public static final boolean[] CONSTRUCTOR = {
        true,
        false,
    };
}

class MethodOverload3 {
    public void create(){}
    public void create(boolean e){}

    public static final String[] IDL_NAMES = {
        "create",
        "create__",
        "create__boolean",
    };
    public static final boolean[] CONSTRUCTOR = {
        true,
        false,
        false,
    };
}

class MethodOverload4 {

    public MethodOverload4() {}
    public MethodOverload4(char i) {}
    
    public static final String[] IDL_NAMES = {
        "create__",
        "create__wchar",
    };
    
    public static final boolean[] CONSTRUCTOR = {
        true,
        true,
    };
}

class MethodOverload5 {

    public MethodOverload5() {}
    public MethodOverload5(char i) {}
    public void create(){}
    
    public static final String[] IDL_NAMES = {
        "create__",
        "create__wchar",
        "create",
    };
    
    public static final boolean[] CONSTRUCTOR = {
        true,
        true,
        false,
    };
}

class MethodOverload6 {

    public MethodOverload6() {}
    public MethodOverload6(char i) {}
    public void create(){}
    public void create(char i){}
    
    public static final String[] IDL_NAMES = {
        "create____",
        "create__wchar__",
        "create__",
        "create__wchar",
    };
    
    public static final boolean[] CONSTRUCTOR = {
        true,
        true,
        false,
        false,
    };
}
