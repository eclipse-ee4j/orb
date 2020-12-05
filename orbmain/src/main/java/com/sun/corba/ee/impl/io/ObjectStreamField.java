/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.corba.ee.impl.io;

import com.sun.corba.ee.impl.misc.ClassInfoCache;
import org.glassfish.pfl.basic.reflection.Bridge;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * A description of a field in a serializable class.
 * A array of these is used to declare the persistent fields of
 * a class.
 *
 */
public class ObjectStreamField implements Comparable 
{
    private static final Bridge bridge = 
        AccessController.doPrivileged(
            new PrivilegedAction<Bridge>() {
                public Bridge run() {
                    return Bridge.get() ;
                }
            } 
        ) ;

    // Create a named field with the specified type.
    public ObjectStreamField(String n, Class clazz) {
        name = n;
        this.clazz = clazz;
        cinfo = ClassInfoCache.get( clazz ) ;

        // Compute the typecode for easy switching
        if (clazz.isPrimitive()) {
            if (clazz == Integer.TYPE) {
                type = 'I';
            } else if (clazz == Byte.TYPE) {
                type = 'B';
            } else if (clazz == Long.TYPE) {
                type = 'J';
            } else if (clazz == Float.TYPE) {
                type = 'F';
            } else if (clazz == Double.TYPE) {
                type = 'D';
            } else if (clazz == Short.TYPE) {
                type = 'S';
            } else if (clazz == Character.TYPE) {
                type = 'C';
            } else if (clazz == Boolean.TYPE) {
                type = 'Z';
            }
        } else if (cinfo.isArray()) {
            type = '[';
            typeString = ObjectStreamClass.getSignature(clazz);
        } else {
            type = 'L';
            typeString = ObjectStreamClass.getSignature(clazz);
        }

        if (typeString != null)
            signature = typeString;
        else
            signature = String.valueOf(type);

    }

    public ObjectStreamField(Field field) {
        this(field.getName(), field.getType());
        setField( field ) ;
    }

    /**
     * Get the name of this field.
     * @return The field name
     */
    public String getName() {
        return name;
    }

    public ClassInfoCache.ClassInfo getClassInfo() {
        return cinfo ;
    }

    /**
     * Get the type of the field.
     * @return The type of the field
     */
    public Class getType() {
        if (clazz != null)
            return clazz;
        switch (type) {
        case 'B': clazz = Byte.TYPE;
            break;
        case 'C': clazz = Character.TYPE;
            break;
        case 'S': clazz = Short.TYPE;
            break;
        case 'I': clazz = Integer.TYPE;
            break;
        case 'J': clazz = Long.TYPE;
            break;
        case 'F': clazz = Float.TYPE;
            break;
        case 'D': clazz = Double.TYPE;
            break;
        case 'Z': clazz = Boolean.TYPE;
            break;
        case '[':
        case 'L':
            clazz = Object.class;
            break;
        }

        return clazz;
    }

    public char getTypeCode() {
        return type;
    }

    public String getTypeString() {
        return typeString;
    }

    Field getField() {
        return field;
    }

    void setField(Field field) {
        this.field = field;
        this.fieldID = bridge.objectFieldOffset( field ) ;
    }


    /**
     * test if this field is a primitive or not.
     * @return if this field is primitive.
     */
    public boolean isPrimitive() {
        return (type != '[' && type != 'L');
    }

    /**
     * Compare this with another ObjectStreamField.
     * return -1 if this is smaller, 0 if equal, 1 if greater
     * types that are primitives are "smaller" than objects.
     * if equal, the names are compared.
     */
    @Override
    public int compareTo(Object o) {
        ObjectStreamField f2 = (ObjectStreamField)o;
        boolean thisprim = (this.typeString == null);
        boolean otherprim = (f2.typeString == null);

        if (thisprim != otherprim) {
            return (thisprim ? -1 : 1);
        }
        return this.name.compareTo(f2.name);
    }

    /**
     * Compare the types of two class descriptors.
     * The match if they have the same primitive types.
     * or if they are both objects and the object types match.
     * @param other type to compare with
     * @return if the two types are equivalent
     */
    public boolean typeEquals(ObjectStreamField other) {
        if (other == null || type != other.type)
            return false;

        /* Return true if the primitive types matched */
        if (typeString == null && other.typeString == null)
            return true;

        return ObjectStreamClass.compareClassNames(typeString,
                                                   other.typeString,
                                                   '/');
    }

    /* Returns the signature of the Field.
     *
     */
    public String getSignature() {

        return signature;

    }

    /**
     * Return a string describing this field.
     */
    public String toString() {
        if (typeString != null)
            return typeString + " " + name;
        else
            return type + " " + name;
    }

    public Class getClazz() {
        return clazz;
    }

    /* Returns the Field ID
     *
     */
    public long getFieldID() {
        return fieldID ;
    }

    private String name;                // the name of the field
    private char type;                  // type first byte of the type signature
    private Field field;                // Reflected field
    private String typeString;          // iff object, typename
    private Class clazz;                // the type of this field, if has been resolved
    private ClassInfoCache.ClassInfo cinfo ;

    // the next 2 things are RMI-IIOP specific, it can be easily
    // removed, if we can figure out all place where there are dependencies
    // to this.  Signature is esentially equal to typestring. Then
    // essentially we can use the java.io.ObjectStreamField as such.

    private String signature;   // the signature of the field
    private long fieldID = Bridge.INVALID_FIELD_OFFSET ; 
}
