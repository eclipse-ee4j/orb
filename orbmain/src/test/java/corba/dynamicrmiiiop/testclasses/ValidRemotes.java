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

package corba.dynamicrmiiiop.testclasses;

/**
 * Valid RMI/IDL Remote Interface Types
 */
public class ValidRemotes {

    public static final Class[] CLASSES = { java.rmi.Remote.class, ValidRemote0.class, ValidRemote1.class, ValidRemote2.class,
            ValidRemote3.class, ValidRemote4.class, ValidRemote5.class, ValidRemote6.class, ValidRemote7.class, ValidRemote8.class,
            ValidRemote9.class, ValidRemote10.class };

    public interface ValidRemote0 extends java.rmi.Remote {
    }

    public interface ValidRemote1 extends ValidRemote0 {
    }

    public interface ValidRemote2 extends java.rmi.Remote {
        public void foo1() throws java.rmi.RemoteException, java.io.IOException, java.lang.Exception, java.lang.Throwable;
    }

    public interface ValidRemote3 extends java.rmi.Remote {
        public void foo1() throws java.rmi.RemoteException;

        public void foo2() throws java.io.IOException;

        public void foo3() throws java.lang.Exception;

        public void foo4() throws java.lang.Throwable;
    }

    public interface ValidRemote4 extends ValidRemote3 {
    }

    public interface ValidRemote5 extends java.rmi.Remote {
        boolean a = true;
        boolean b = false;
        byte c = 0;
        char d = 'd';
        short e = 2;
        int f = 3;
        long g = 4;
        float h = 5.0f;
        double i = 6.0;
        String j = "foo";
    }

    public interface ValidRemote6 extends java.rmi.Remote {
        public void foo1() throws java.rmi.RemoteException, java.lang.Exception;

        public void foo2() throws java.rmi.RemoteException, Exception1;

    }

    public static class Exception1 extends java.lang.Exception {
    }

    public interface ValidRemote7 extends java.rmi.Remote {
        void foo() throws java.rmi.RemoteException;

        void foo(int a) throws java.rmi.RemoteException;

        void foo(String[] b) throws java.rmi.RemoteException;
    }

    public interface ValidRemote8 extends ValidRemote2, ValidRemote5, ValidRemote7 {
    }

    public interface ValidRemote9 extends ValidRemote8 {
        void foo(int a) throws java.rmi.RemoteException;
    }

    public interface ValidRemote10 extends java.rmi.Remote, ValidRemote9 {
    }
}
