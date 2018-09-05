/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.io;

import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedAction;

import java.lang.reflect.Modifier;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import com.sun.corba.ee.impl.misc.ClassInfoCache ;

import com.sun.corba.ee.impl.misc.ORBUtility ;

// This file contains some utility methods that
// originally were in the OSC in the RMI-IIOP
// code delivered by IBM.  They don't make
// sense there, and hence have been put
// here so that they can be factored out in
// an attempt to eliminate redundant code from
// ObjectStreamClass.  Eventually the goal is
// to move to java.io.ObjectStreamClass, and
// java.io.ObjectStreamField.

// class is package private for security reasons

class ObjectStreamClassCorbaExt {

    /**
     * Return true, iff,
     *
     * 1. 'cl' is an interface, and
     * 2. 'cl' and all its ancestors do not implement java.rmi.Remote, and
     * 3. if 'cl' has no methods (including those of its ancestors), or,
     *    if all the methods (including those of its ancestors) throw an
     *    exception that is atleast java.rmi.RemoteException or one of
     *    java.rmi.RemoteException's super classes.
     */
    static final boolean isAbstractInterface(Class cl) {
        ClassInfoCache.ClassInfo cinfo = ClassInfoCache.get( cl ) ;
        if (!cinfo.isInterface() || cinfo.isARemote(cl)) {
            return false;
        }

        Method[] methods = cl.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Class exceptions[] = methods[i].getExceptionTypes();
            boolean exceptionMatch = false;
            for (int j = 0; (j < exceptions.length) && !exceptionMatch; j++) {
                if ((java.rmi.RemoteException.class == exceptions[j]) ||
                    (java.lang.Throwable.class == exceptions[j]) ||
                    (java.lang.Exception.class == exceptions[j]) ||
                    (java.io.IOException.class == exceptions[j])) {
                    exceptionMatch = true;
                }
            }
            if (!exceptionMatch) {
                return false;
            }
        }

        return true;
    }

    // Common collisions (same length):
    // java.lang.String
    // java.math.BigDecimal
    // java.math.BigInteger
    private static final String objectString         = "Ljava/lang/Object;" ;
    private static final String serializableString   = "Ljava/io/Serializable;" ;
    private static final String externalizableString = "Ljava/io/Externalizable;" ;

    // Note that these 3 lengths are different!
    private static final int objectLength = objectString.length() ;
    private static final int serializableLength = serializableString.length() ;
    private static final int externalizableLength = externalizableString.length() ;

    private static final boolean debugIsAny = false ;

    /*
     *  Returns TRUE if type is 'any'.
     *  This is in the marshaling path, so we want it to run as
     *  fast as possible.
     */
    static final boolean isAny(String typeString) {
        if (debugIsAny) {
            ORBUtility.dprint( 
                ObjectStreamClassCorbaExt.class.getName(), 
                "IsAny: typeString = " + typeString ) ;
        }

        int length = typeString.length() ;

        if (length == objectLength) {
            // Note that java.lang.String occurs a lot, and has the
            // same length as java.lang.Object!
            if (typeString.charAt(length-2) == 't')
                return objectString.equals( typeString ) ;
            else
                return false ;
        }

        if (length == serializableLength) {
            // java.math.BigInteger and java.math.BigDecimal have the same
            // length as java.io.Serializable
            if (typeString.charAt(length-2) == 'e')
                return serializableString.equals( typeString ) ;
            else 
                return false ;
        }

        if (length == externalizableLength)
            return externalizableString.equals( typeString ) ;

        return false ;
    }

    private static final Method[] getDeclaredMethods(final Class clz) {
        return AccessController.doPrivileged(
            new PrivilegedAction<Method[]>() {
                public Method[] run() {
                    return clz.getDeclaredMethods();
                }
            }
        );
    }

}
