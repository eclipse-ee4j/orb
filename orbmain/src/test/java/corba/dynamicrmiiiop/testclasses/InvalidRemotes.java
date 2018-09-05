/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.dynamicrmiiiop.testclasses;

/**
 * Invalid RMI/IDL Remote Interface Types
 */ 
public class InvalidRemotes {

    // should extend java.rmi.Remote, either directly or indirectly
    public interface InvalidRemote1 {}
    
    // should extend java.rmi.Remote, either directly or indirectly 
    public interface InvalidRemote2 extends InvalidRemote1 {}

    // RMI/IDL Exceptions should not extend java.rmi.Remote, either directly
    // or indirectly
    public class InvalidException1 extends java.lang.Exception
        implements java.rmi.Remote {}

    // RMI/IDL Exceptions should not extend java.rmi.Remote, either directly
    // or indirectly
    public class InvalidException2 extends InvalidException1 {}

    // contains method with invalid exception type
    public interface InvalidRemote3 extends java.rmi.Remote {
        public void foo1() throws java.rmi.RemoteException, InvalidException1;
    }

    // contains method with invalid exception type
    public interface InvalidRemote4 extends java.rmi.Remote {
        public void foo1() throws java.rmi.RemoteException, InvalidException2;
    }

    // Each remote method should throw java.rmi.RemoteException or one of its
    // super-class exception types.
    public interface InvalidRemote5 extends java.rmi.Remote {
        public void foo1();
    }    

    // contains method with invalid exception type
    public interface InvalidRemote6 extends java.rmi.Remote {
        public void foo1() throws java.rmi.RemoteException, java.lang.Error;
    } 

    // contains method with invalid exception type
    public interface InvalidRemote7 extends java.rmi.Remote {
        public void foo1() throws java.rmi.RemoteException, 
            java.lang.RuntimeException;
    } 

    private class InvalidException3 extends java.lang.RuntimeException {}
    // contains method with invalid exception type
    public interface InvalidRemote8 extends java.rmi.Remote {
        public void foo1() throws java.rmi.RemoteException, 
            InvalidException3;
    } 
    
    // has a field other than primitive or String
    public interface InvalidRemote9 extends java.rmi.Remote {
        Object o = null;
    }

    private interface A {
        void foo() throws java.rmi.RemoteException;
    }

    private interface B {
        void foo() throws java.rmi.RemoteException;
    }

    // can't directly inherit from multiple base interfaces which define a
    // method with the same name
    public interface InvalidRemote10 extends java.rmi.Remote, A, B {}

    private interface C extends A {}
    private interface D extends B {}

    // can't directly inherit from multiple base interfaces which define a
    // method with the same name.  
    public interface InvalidRemote11 extends java.rmi.Remote, C, D {}
    
    private interface E {
        void foo() throws java.rmi.RemoteException;
    }

    private interface F {
        void foo(int a) throws java.rmi.RemoteException;
    }

    // can't directly inherit from multiple base interfaces which define a
    // method with the same name
    public interface InvalidRemote12 extends java.rmi.Remote, E, F {}

    private interface G extends E {}
    private interface H extends F {}

    // can't directly inherit from multiple base interfaces which define a
    // method with the same name
    public interface InvalidRemote13 extends java.rmi.Remote, G, H {}

    // can't directly inherit from multiple base interfaces which define a
    // method with the same name
    public interface InvalidRemote14 extends G, java.rmi.Remote, H {}

    
    // can't directly inherit from multiple base interfaces which define a
    // method with the same name.  doesn't matter if a method with the same
    // name is defined in the most derived interface
    public interface InvalidRemote15 extends G, java.rmi.Remote, H {
        void foo() throws java.rmi.RemoteException;
    }

    // must be an interface
    public class InvalidRemote16 {}

    // illegal constant type. must be primitive or String
    public interface InvalidRemote17 extends java.rmi.Remote {
        int[] FOO = { 1, 2, 3 };
    }
    
    // applying mangling rules results in clash
    public interface InvalidRemote18 extends java.rmi.Remote {
        void J_foo() throws java.rmi.RemoteException;
        void _foo() throws java.rmi.RemoteException;
    }

    // applying mangling rules results in clash
    public interface InvalidRemote19 extends java.rmi.Remote {
        void foo() throws java.rmi.RemoteException;
        void foo(int a) throws java.rmi.RemoteException;
        void foo__long() throws java.rmi.RemoteException;
    }
    

}
