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

package com.sun.corba.ee.internal.io;

public class IIOPInputStream {
    private static native Object allocateNewObject(Class aclass,
                                                   Class initclass)
        throws InstantiationException, IllegalAccessException;
    /* Create a pending exception.  This is needed to get around
     * the fact that the *Delegate methods do not explicitly
     * declare that they throw exceptions.
     *
     * This native methods creates an exception of the given type with
     * the given message string and posts it to the pending queue.
     */
    private static native void throwExceptionType(Class c, String message);

    /* The following native methods of the form set*Field are used
     * to set private, protected, and package private fields
     * of an Object.
     */
    private static native void setObjectField(Object o, Class c, String fieldName, String fieldSig, Object v);
    private static native void setBooleanField(Object o, Class c, String fieldName, String fieldSig, boolean v);
    private static native void setByteField(Object o, Class c, String fieldName, String fieldSig, byte v);
    private static native void setCharField(Object o, Class c, String fieldName, String fieldSig, char v);
    private static native void setShortField(Object o, Class c, String fieldName, String fieldSig, short v);
    private static native void setIntField(Object o, Class c, String fieldName, String fieldSig, int v);
    private static native void setLongField(Object o, Class c, String fieldName, String fieldSig, long v);
    private static native void setFloatField(Object o, Class c, String fieldName, String fieldSig, float v);
    private static native void setDoubleField(Object o, Class c, String fieldName, String fieldSig, double v);
    private static native void readObject(Object obj, Class asClass, Object ois);

    private static native void setObjectFieldOpt(Object o, long fieldID, Object v);
    private static native void setBooleanFieldOpt(Object o, long fieldID, boolean v);
    private static native void setByteFieldOpt(Object o, long fieldID, byte v);
    private static native void setCharFieldOpt(Object o, long fieldID, char v);
    private static native void setShortFieldOpt(Object o, long fieldID, short v);
    private static native void setIntFieldOpt(Object o, long fieldID, int v);
    private static native void setLongFieldOpt(Object o, long fieldID, long v);

    private static native void setFloatFieldOpt(Object o, long fieldID, float v);
    private static native void setDoubleFieldOpt(Object o, long fieldID, double v);
}
