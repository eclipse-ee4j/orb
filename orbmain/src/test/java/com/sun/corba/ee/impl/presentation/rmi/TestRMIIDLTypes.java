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

package com.sun.corba.ee.impl.presentation.rmi;

import corba.dynamicrmiiiop.testclasses.InvalidEntities;
import corba.dynamicrmiiiop.testclasses.InvalidExceptions;
import corba.dynamicrmiiiop.testclasses.InvalidObjRefs;
import corba.dynamicrmiiiop.testclasses.InvalidValues;
import corba.dynamicrmiiiop.testclasses.ValidEntities;
import corba.dynamicrmiiiop.testclasses.ValidExceptions;
import corba.dynamicrmiiiop.testclasses.ValidObjRefs;
import corba.dynamicrmiiiop.testclasses.ValidRemotes;
import corba.dynamicrmiiiop.testclasses.ValidValues;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestRMIIDLTypes extends TestCase {

    private IDLTypesUtil idlTypesUtil;

    public static Test suite() {
        return new TestSuite(TestRMIIDLTypes.class);
    }

    @Override
    protected void setUp() {
        idlTypesUtil = new IDLTypesUtil();
    }

    @Override
    protected void tearDown() {
    }

    public void testPrimitiveTypes() {
        Class[] primitives = { Void.TYPE, Boolean.TYPE, Byte.TYPE, Character.TYPE, Short.TYPE, Integer.TYPE, Long.TYPE, Float.TYPE,
                Double.TYPE };

        for (Class primitive : primitives) {
            String msg = primitive.getName();
            assertTrue(msg, idlTypesUtil.isPrimitive(primitive));
            assertFalse(msg, idlTypesUtil.isRemoteInterface(primitive));
            assertFalse(msg, idlTypesUtil.isValue(primitive));
            assertFalse(msg, idlTypesUtil.isArray(primitive));
            assertFalse(msg, idlTypesUtil.isException(primitive));
            assertFalse(msg, idlTypesUtil.isObjectReference(primitive));
            assertFalse(msg, idlTypesUtil.isEntity(primitive));
        }

        Class[] nonPrimitives = { Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, String.class,
                java.util.Date.class, Object.class };

        for (Class nonPrimitive : nonPrimitives) {
            String msg = nonPrimitive.getName();
            assertFalse(msg, idlTypesUtil.isPrimitive(nonPrimitive));
        }

    }

    public void testRemoteInterfaceTypes() {

        Class[] remoteInterfaces = ValidRemotes.CLASSES;

        for (Class remoteIntf : remoteInterfaces) {
            String msg = remoteIntf.getName();

            assertTrue(msg, idlTypesUtil.isRemoteInterface(remoteIntf));

            assertFalse(msg, idlTypesUtil.isPrimitive(remoteIntf));
            assertFalse(msg, idlTypesUtil.isValue(remoteIntf));
            assertFalse(msg, idlTypesUtil.isArray(remoteIntf));
            assertFalse(msg, idlTypesUtil.isException(remoteIntf));
            assertFalse(msg, idlTypesUtil.isObjectReference(remoteIntf));
            assertFalse(msg, idlTypesUtil.isEntity(remoteIntf));
        }

        // NOTE invalid remote interfaces are tested in TestIDLNameTranslator
    }

    public void testValueTypes() {

        Class[] values = ValidValues.CLASSES;

        for (Class value : values) {
            String msg = value.getName();

            assertTrue(msg, idlTypesUtil.isValue(value));

            assertFalse(msg, idlTypesUtil.isPrimitive(value));
            assertFalse(msg, idlTypesUtil.isRemoteInterface(value));
            assertFalse(msg, idlTypesUtil.isArray(value));
            assertFalse(msg, idlTypesUtil.isException(value));
            assertFalse(msg, idlTypesUtil.isObjectReference(value));
            assertFalse(msg, idlTypesUtil.isEntity(value));
        }

        Class[] nonValues = InvalidValues.CLASSES;

        for (Class nonValue : nonValues) {
            String msg = nonValue.getName();
            assertFalse(msg, idlTypesUtil.isValue(nonValue));
        }

    }

    public void testExceptionTypes() {

        Class[] exceptions = ValidExceptions.CLASSES;

        for (Class excep : exceptions) {
            String msg = excep.getName();

            assertTrue(msg, idlTypesUtil.isException(excep));
            // a valid exception is always a valid value type !
            assertTrue(msg, idlTypesUtil.isValue(excep));

            assertFalse(msg, idlTypesUtil.isPrimitive(excep));
            assertFalse(msg, idlTypesUtil.isRemoteInterface(excep));
            assertFalse(msg, idlTypesUtil.isArray(excep));
            assertFalse(msg, idlTypesUtil.isObjectReference(excep));
            assertFalse(msg, idlTypesUtil.isEntity(excep));
        }

        Class[] nonExceptions = InvalidExceptions.CLASSES;

        for (Class nonException : nonExceptions) {
            String msg = nonException.getName();
            assertFalse(msg, idlTypesUtil.isException(nonException));
        }
    }

    public void testObjRefs() {

        Class[] objRefs = ValidObjRefs.CLASSES;

        for (Class objRef : objRefs) {
            String msg = objRef.getName();

            assertTrue(msg, idlTypesUtil.isObjectReference(objRef));

            assertFalse(msg, idlTypesUtil.isPrimitive(objRef));
            assertFalse(msg, idlTypesUtil.isRemoteInterface(objRef));
            assertFalse(msg, idlTypesUtil.isValue(objRef));
            assertFalse(msg, idlTypesUtil.isArray(objRef));
            assertFalse(msg, idlTypesUtil.isException(objRef));
            assertFalse(msg, idlTypesUtil.isEntity(objRef));
        }

        Class[] nonObjRefs = InvalidObjRefs.CLASSES;

        for (Class nonObjRef : nonObjRefs) {
            String msg = nonObjRef.getName();
            assertFalse(msg, idlTypesUtil.isObjectReference(nonObjRef));
        }

    }

    public void testEntities() {

        Class[] entities = ValidEntities.CLASSES;

        for (Class entity : entities) {
            String msg = entity.getName();

            assertTrue(msg, idlTypesUtil.isEntity(entity));
            // An entity type is always a value type
            assertTrue(msg, idlTypesUtil.isValue(entity));

            assertFalse(msg, idlTypesUtil.isPrimitive(entity));
            assertFalse(msg, idlTypesUtil.isRemoteInterface(entity));
            assertFalse(msg, idlTypesUtil.isArray(entity));
            assertFalse(msg, idlTypesUtil.isException(entity));
            assertFalse(msg, idlTypesUtil.isObjectReference(entity));

        }

        Class[] nonEntities = InvalidEntities.CLASSES;

        for (Class nonEntity : nonEntities) {
            String msg = nonEntity.getName();
            assertFalse(msg, idlTypesUtil.isEntity(nonEntity));
        }

    }

}
