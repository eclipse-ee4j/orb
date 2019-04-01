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

public class IDLPropertiesTest {

    //
    // Set of idl names corresponding to alphabetically sorted set of
    // interface methods. See TestIDLNameTranslator for sorting details.
    //
    static final String[] IDL_NAMES = { "a", "get", "_get_a__", "_get_ABc", "_get_b", "_get_CDE", "getDAB", "getDCD", "getDzzz", "getEfg", "_get_zde", "is",
            "isA", "isBCD", "_get_c", "_get_CCCCCe", "isCZ", "_get_cf", "set", "_set_a__", "_set_b", "setCDE", "setEfg", "_set_zde" };

    public static String[] getIDLNames() {
        return IDL_NAMES;
    }

    public interface IDLProperties extends java.rmi.Remote {

        // should force a __ to be added to getter attribute
        void a() throws java.rmi.RemoteException;

        // not a property since there is no <name> portion
        int get() throws java.rmi.RemoteException;

        // valid getter
        int getA() throws java.rmi.RemoteException;

        // valid getter
        int getABc() throws java.rmi.RemoteException;

        // valid getter
        int getB() throws java.rmi.RemoteException;

        // getter
        int getCDE() throws java.rmi.RemoteException;

        // not a getter. can't have void return type.
        void getDAB() throws java.rmi.RemoteException;

        // not a getter. can't have void return type.
        void getDCD(int a) throws java.rmi.RemoteException;

        // not a getter. can't have any parameters.
        int getDzzz(int a) throws java.rmi.RemoteException;

        // valid getter
        boolean getZde() throws java.rmi.RemoteException;

        // not a getter. throws at least one checked exception in addition to
        // java.rmi.RemoteException(or one of its subclasses)
        int getEfg() throws java.rmi.RemoteException, java.lang.Exception;

        // not a property since there is no <name> portion
        boolean is() throws java.rmi.RemoteException;

        // not a property since "is" only applies to boolean
        int isA() throws java.rmi.RemoteException;

        // not valid. must be boolean primitive
        Boolean isBCD() throws java.rmi.RemoteException;

        // valid boolean property
        boolean isC() throws java.rmi.RemoteException;

        // valid boolean property
        boolean isCCCCCe() throws java.rmi.RemoteException;

        // not boolean property. must have 0 args
        boolean isCZ(int a) throws java.rmi.RemoteException;

        // valid boolean property
        boolean isCf() throws java.rmi.RemoteException;

        // not a property since there is no <name> portion
        int set() throws java.rmi.RemoteException;

        void setA(int c) throws java.rmi.RemoteException;

        // valid setter
        void setB(int b) throws java.rmi.RemoteException;

        // not a setter. no corresponding getter with correct type.
        void setCDE(Integer i) throws java.rmi.RemoteException;

        // not a setter. no corresponding getter.
        void setEfg(int a) throws java.rmi.RemoteException;

        // valid setter
        void setZde(boolean a) throws java.rmi.RemoteException;

    }

}
