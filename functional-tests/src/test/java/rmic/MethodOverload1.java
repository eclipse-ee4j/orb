/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
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
