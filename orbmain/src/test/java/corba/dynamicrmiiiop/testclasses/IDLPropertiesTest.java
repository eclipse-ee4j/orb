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

public class IDLPropertiesTest {

    //
    // Set of idl names corresponding to alphabetically sorted set of
    // interface methods.  See TestIDLNameTranslator for sorting details.    
    //
    static final String[] IDL_NAMES = {   
        "a",
        "get", 
        "_get_a__",
        "_get_ABc",
        "_get_b",
        "_get_CDE",
        "getDAB",
        "getDCD",
        "getDzzz",
        "getEfg",
        "_get_zde",
        "is",
        "isA",
        "isBCD",
        "_get_c",
        "_get_CCCCCe",
        "isCZ",
        "_get_cf",
        "set",
        "_set_a__",
        "_set_b",
        "setCDE",
        "setEfg",
        "_set_zde"
    };
    
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

        // not valid.  must be boolean primitive
        Boolean isBCD() throws java.rmi.RemoteException;
        
        // valid boolean property
        boolean isC() throws java.rmi.RemoteException;

        // valid boolean property
        boolean isCCCCCe() throws java.rmi.RemoteException;

        // not boolean property.  must have 0 args
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

        // not a setter.  no corresponding getter.
        void setEfg(int a) throws java.rmi.RemoteException;

        // valid setter
        void setZde(boolean a) throws java.rmi.RemoteException;
                
    }

}
