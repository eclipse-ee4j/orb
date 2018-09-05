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

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.MarshalException;
import java.util.Hashtable;
import java.io.IOException;

public interface MangleMethods extends Remote {

    // Methods that should be attributes...
    
    int getFoo () throws RemoteException;
    int getAB () throws RemoteException;
    boolean isX () throws RemoteException;
    char getChar() throws RemoteException;
    void setChar(char c) throws RemoteException, RuntimeException;
    long getLong() throws RemoteException, ClassCastException;      // RuntimeException subclass.
    int getSomething() throws RemoteException, MarshalException;    // RemoteException subclass.
    
    // Methods that look like they should be attributes
    // but aren't...
    
    byte getByte () throws Exception;           // Invalid exception.
  
    boolean is () throws RemoteException;          // No property name
    
    void setZ(int z) throws RemoteException;       // No getter
    
    boolean isLong() throws RemoteException;       // Same name as getLong, different type.
    
    boolean isShort() throws RemoteException;      // getter...
    void setShort(short s) throws RemoteException; // ... setter different types...
    
    int getOther(char i) throws RemoteException;   // Argument.
 
    void getIt() throws RemoteException;            // void return.
 
    int getY() throws RemoteException;             // getter...
    void setY() throws RemoteException;            // ... setter with void arg
    
    // Miscellaneous...
 
    boolean isFloat() throws RemoteException;      // getter...
    void setFloat(float f) throws RemoteException; // ... setter different types ...
    float getFloat() throws RemoteException;       // ... set/get are attrs, 'is' isn't.
    
    boolean isEmpty() throws RemoteException;      // Looks like a case-collision...
    boolean IsEmpty() throws RemoteException;      // ... but isn't.
    
    int doAJob() throws RemoteException;           // Case collision...
    int doAjob() throws RemoteException;           // ... but not attributes.

    boolean isAJob() throws RemoteException;       // Case collision...
    boolean getAjob() throws RemoteException;      // ... and are (different) attributes.
    
    byte getfred() throws RemoteException;          // Not case collision (5.4.3.4 mangling)...
    void setFred(byte b) throws RemoteException;    // ... and are attribute pair.
    
    int _do\u01c3It$() throws RemoteException;         // Methods with illegal chars...
    int _do\u01c3It$(int i) throws RemoteException;    // ... and overloaded versions...
    int _do\u01c3It$(char c) throws RemoteException;   // ...
    
    int getFooBar() throws RemoteException;         // Attribute...
    int fooBar() throws RemoteException;            // ... and colliding method.
    
    // IDL Keyword collisions...
    
    int typeDef () throws RemoteException;          // Method name is keyword
    int getDefault () throws RemoteException;       // Attribute name is keyword.
    int getObject () throws RemoteException;        // Attribute name is keyword.
    int getException () throws RemoteException;     // Attribute name is keyword.     
    
    
    // Assertion mechanism...
    
    class Asserts {
        
        private static Hashtable map = null;
        
        public static String[] getAsserts (String methodSig) {
            
            if (map == null) {
                map = new Hashtable();
                for (int i = 0; i < ASSERTS.length; i++) {
                    map.put(ASSERTS[i][0],new Integer(i));
                }
            }
            
            Integer theIndex = (Integer)map.get(methodSig);
            
            if (theIndex == null) {
                throw new Error("Assert not found for " + methodSig);
            }

            return ASSERTS[theIndex.intValue()];
        }
        
        private static String[][] ASSERTS = {
                                    
            //   Method Signature           Kind        Attribute Name      Wire Name
            //   ----------------------     ----------- ------------------- -------------------------
            {"int getFoo()",            "GET",      "foo",              "_get_foo"},
            {"int getAB()",             "GET",      "AB",               "_get_AB"},
            {"boolean isX()",           "IS",       "x",                "_get_x"},
            {"char getChar()",          "GET_RW",   "_char",            "_get_char"},
            {"void setChar(char)",      "SET",      "_char",            "_set_char"},
            {"byte getByte()",          "NONE",     null,               "getByte"},
            {"boolean is()",            "NONE",     null,               "is"},
            {"void setZ(int)",          "NONE",     null,               "setZ"},
            {"boolean isLong()",        "NONE",     null,               "isLong"},
            {"long getLong()",          "GET",      "_long",            "_get_long"},
            {"boolean isShort()",       "NONE",     null,               "isShort"},
            {"void setShort(short)",    "NONE",     null,               "setShort"},
            {"int getSomething()",      "GET",      "something",        "_get_something"},
            {"int getOther(char)",      "NONE",     null,               "getOther"},
            {"void getIt()",            "NONE",     null,               "getIt"},
            {"int getY()",              "GET",      "y",                "_get_y"},
            {"void setY()",             "NONE",     null,               "setY"},
            {"boolean isFloat()",       "NONE",     null,               "isFloat"},
            {"float getFloat()",        "GET_RW",   "_float",           "_get_float"},
            {"void setFloat(float)",    "SET",      "_float",           "_set_float"},
            {"boolean isEmpty()",       "IS",       "empty",            "_get_empty"},
            {"boolean IsEmpty()",       "NONE",     null,               "IsEmpty"},
            {"int doAJob()",            "NONE",     null,               "doAJob_2_3"},
            {"int doAjob()",            "NONE",     null,               "doAjob_2"},
            {"boolean isAJob()",        "IS",       "AJob_0_1",         "_get_AJob_0_1"},
            {"boolean getAjob()",       "GET",      "ajob_",            "_get_ajob_"},
            {"byte getfred()",          "GET",      "fred",             "_get_fred"},
            {"void setFred(byte)",      "NONE",     null,               "setFred"},
            {"int _do\u01c3It$()",      "NONE",     null,               "J_doU01C3ItU0024__"},
            {"int _do\u01c3It$(int)",   "NONE",     null,               "J_doU01C3ItU0024__long"},
            {"int _do\u01c3It$(char)",  "NONE",     null,               "J_doU01C3ItU0024__wchar"},
            {"int getFooBar()",         "GET",      "fooBar__",         "_get_fooBar__"},
            {"int fooBar()",            "NONE",     null,               "fooBar"},
            {"int typeDef()",           "NONE",     null,               "_typeDef"},
            {"int getDefault()",        "GET",      "_default",         "_get_default"},
            {"int getObject()",         "GET",      "_object",          "_get_object"},
            {"int getException()",      "GET",      "_exception",       "_get_exception"},
        };
    }
}
