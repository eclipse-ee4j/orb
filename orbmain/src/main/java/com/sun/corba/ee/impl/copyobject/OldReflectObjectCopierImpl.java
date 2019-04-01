/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

/*
 * Appserver logging has been added to this class, levels of
 * verbosity are as follows:
 *
 * INFO = no extra output in logs, standard production setting
 * FINE = (IasUtilDelegate) logs message stating if HIGHP or STDP
 *        copyObject code is executed. Can be used to determine
 *        how often the sun.reflect code falls back to std ORB copy
 * FINER = logs tracing info for arrayCopy and copyFields
 * FINEST = logs everything, including exception stack traces
 */

package com.sun.corba.ee.impl.copyobject;

import com.sun.corba.ee.impl.util.Utility;
import com.sun.corba.ee.spi.orb.ORB;
import org.glassfish.pfl.basic.reflection.Bridge;
import org.glassfish.pfl.dynamic.copyobject.spi.ObjectCopier;
import org.glassfish.pfl.dynamic.copyobject.spi.ReflectiveCopyException;

import java.io.Externalizable;
import java.io.Serializable;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Provides the functionality of copying objects using reflection. NOTE: Currently the implementation does not implement
 * this copying functionality for objects which have fields whose types are based on inner classes. If for any reason
 * copying cannot be done using reflection it uses the original ORB serialization to implement the copying
 */
public class OldReflectObjectCopierImpl implements ObjectCopier {
    private IdentityHashMap objRefs;
    private ORB orb;

    public OldReflectObjectCopierImpl(org.omg.CORBA.ORB orb) {
        objRefs = new IdentityHashMap();
        this.orb = (ORB) orb;
    }

    /**
     * reflectCache is used to cache the reflection attributes of a class
     */
    private static Map reflectCache = new HashMap();

    /**
     * Provides the functionality of a cache for storing the various reflection attributes of a class so that access to
     * these methods is not done repeatedly
     */
    class ReflectAttrs {
        public Field[] fields;
        public Constructor constr;
        public Class thisClass;
        public Class arrayClass;
        public Class superClass;
        public boolean isImmutable;
        public boolean isDate;
        public boolean isSQLDate;

        public ReflectAttrs(Class cls) {
            thisClass = cls;
            String name = cls.getName();
            char ch = name.charAt(0);

            isImmutable = false;
            isDate = false;
            isSQLDate = false;
            fields = null;
            constr = null;
            superClass = null;
            if (ch == '[') {
                arrayClass = cls.getComponentType();
            } else if (isImmutable(name)) {
                isImmutable = true;
            } else if (name.equals("java.util.Date")) {
                isDate = true;
            } else if (name.equals("java.sql.Date")) {
                isSQLDate = true;
            } else {
                if (Externalizable.class.isAssignableFrom(cls))
                    constr = getExternalizableConstructor(cls);
                else if (Serializable.class.isAssignableFrom(cls))
                    constr = getSerializableConstructor(cls);
                if (constr != null) {
                    constr.setAccessible(true);
                }
                fields = cls.getDeclaredFields();
                AccessibleObject.setAccessible(fields, true);
                superClass = cls.getSuperclass();
            }
        }
    };

    /**
     * Bridge is used to access the reflection factory for obtaining serialization constructors. This must be carefully
     * protected!
     */
    private static final Bridge bridge = (Bridge) AccessController.doPrivileged(new PrivilegedAction() {
        public Object run() {
            return Bridge.get();
        }
    });

    /**
     * Returns public no-arg constructor of given class, or null if none found. Access checks are disabled on the returned
     * constructor (if any), since the defining class may still be non-public.
     */
    private Constructor getExternalizableConstructor(Class cl) {
        try {
            Constructor cons = cl.getDeclaredConstructor(new Class[0]);
            cons.setAccessible(true);
            return ((cons.getModifiers() & Modifier.PUBLIC) != 0) ? cons : null;
        } catch (NoSuchMethodException ex) {
            // test for null on calling routine will avoid NPE just to be safe
            return null;
        }
    }

    /**
     * Returns true if classes are defined in the same package, false otherwise.
     *
     * Copied from the Merlin java.io.ObjectStreamClass.
     */
    private boolean packageEquals(Class cl1, Class cl2) {
        Package pkg1 = cl1.getPackage(), pkg2 = cl2.getPackage();
        return ((pkg1 == pkg2) || ((pkg1 != null) && (pkg1.equals(pkg2))));
    }

    /**
     * Returns subclass-accessible no-arg constructor of first non-serializable superclass, or null if none found. Access
     * checks are disabled on the returned constructor (if any).
     */
    private Constructor getSerializableConstructor(Class cl) {
        Class initCl = cl;
        if (initCl == null) {
            // should not be possible for initCl==null but log and return null
            // test for null on calling routine will avoid NPE just to be safe
            return null;
        }
        while (Serializable.class.isAssignableFrom(initCl)) {
            if ((initCl = initCl.getSuperclass()) == null) {
                return null;
            }
        }
        try {
            Constructor cons = initCl.getDeclaredConstructor(new Class[0]);
            int mods = cons.getModifiers();
            if ((mods & Modifier.PRIVATE) != 0 || ((mods & (Modifier.PUBLIC | Modifier.PROTECTED)) == 0 && !packageEquals(cl, initCl))) {
                // test for null on calling routine will avoid NPE just to be safe
                return null;
            }
            cons = bridge.newConstructorForSerialization(cl, cons);
            cons.setAccessible(true);
            return cons;
        } catch (NoSuchMethodException ex) {
            // test for null on calling routine will avoid NPE just to be safe
            return null;
        }
    }

    /**
     * Gets the reflection attributes for a class from the cache or if it is not in the cache yet, computes the attributes
     * and populates the cache
     *
     * @param cls the class whose attributes are needed
     * @return the attributes needed for reflection
     *
     * This method must be synchronized so that reflectCache.put can safely update the reflectCache.
     */
    private final synchronized ReflectAttrs getClassAttrs(Class cls) {
        ReflectAttrs attrs = null;

        attrs = (ReflectAttrs) reflectCache.get(cls);
        if (attrs == null) {
            attrs = new ReflectAttrs(cls);
            reflectCache.put(cls, (Object) attrs);
        }
        return attrs;
    }

    public static boolean isImmutable(String classname) {
        if (classname.startsWith("java.lang.")) {
            String typename = classname.substring(10);
            if (typename.compareTo("String") == 0 || typename.compareTo("Class") == 0 || typename.compareTo("Integer") == 0
                    || typename.compareTo("Boolean") == 0 || typename.compareTo("Long") == 0 || typename.compareTo("Double") == 0
                    || typename.compareTo("Byte") == 0 || typename.compareTo("Char") == 0 || typename.compareTo("Short") == 0
                    || typename.compareTo("Object") == 0 || typename.compareTo("Float") == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Utility to copy array of primitive types or objects. Used by local stubs to copy objects
     *
     * @param obj the object to copy or connect.
     * @return the copied object.
     * @exception RemoteException if any object could not be copied.
     */
    private final Object arrayCopy(Object obj, Class aClass) throws RemoteException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Object acopy = null;

        if (aClass.isPrimitive()) {
            if (aClass == byte.class) {
                acopy = ((byte[]) obj).clone();
            } else if (aClass == char.class) {
                acopy = ((char[]) obj).clone();
            } else if (aClass == short.class) {
                acopy = ((short[]) obj).clone();
            } else if (aClass == int.class) {
                acopy = ((int[]) obj).clone();
            } else if (aClass == long.class) {
                acopy = ((long[]) obj).clone();
            } else if (aClass == double.class) {
                acopy = ((double[]) obj).clone();
            } else if (aClass == float.class) {
                acopy = ((float[]) obj).clone();
            } else if (aClass == boolean.class) {
                acopy = ((boolean[]) obj).clone();
            }
            objRefs.put(obj, acopy);
        } else if (aClass == String.class) {
            acopy = ((String[]) obj).clone();
            objRefs.put(obj, acopy);
        } else {
            int alen = Array.getLength(obj);

            aClass = obj.getClass().getComponentType();

            acopy = Array.newInstance(aClass, alen);

            objRefs.put(obj, acopy);
            for (int idx = 0; idx < alen; idx++) {
                Object aobj = Array.get(obj, idx);
                aobj = reflectCopy(aobj);
                Array.set(acopy, idx, aobj);
            }
        }

        return acopy;
    }

    /**
     * Utility to copy fields of an object. Used by local stub to copy objects
     *
     * @param obj the object whose fields need to be copied
     * @exception RemoteException if any object could not be copied.
     */
    private final void copyFields(Class cls, Field[] fields, Object obj, Object copy)
            throws RemoteException, IllegalAccessException, InstantiationException, InvocationTargetException {
        if (fields == null || fields.length == 0) {
            return;
        }

        // regular object, so copy the fields over
        for (int idx = 0; idx < fields.length; idx++) {
            Field fld = fields[idx];
            int modifiers = fld.getModifiers();
            Object fobj = null;
            Class fieldClass = fld.getType();

            if (!Modifier.isStatic(modifiers)) {
                if (fieldClass == int.class) {
                    fld.setInt(copy, fld.getInt(obj));
                } else if (fieldClass == long.class) {
                    fld.setLong(copy, fld.getLong(obj));
                } else if (fieldClass == double.class) {
                    fld.setDouble(copy, fld.getDouble(obj));
                } else if (fieldClass == byte.class) {
                    fld.setByte(copy, fld.getByte(obj));
                } else if (fieldClass == char.class) {
                    fld.setChar(copy, fld.getChar(obj));
                } else if (fieldClass == short.class) {
                    fld.setShort(copy, fld.getShort(obj));
                } else if (fieldClass == float.class) {
                    fld.setFloat(copy, fld.getFloat(obj));
                } else if (fieldClass == boolean.class) {
                    fld.setBoolean(copy, fld.getBoolean(obj));
                } else {
                    fobj = fld.get(obj);
                    Object newfobj = reflectCopy(fobj);
                    fld.set(copy, newfobj);
                }
            }
        }
    }

    // Returns an empty instance of Class cls. Useful for
    // cloning collection types. Requires a no args constructor,
    // public for now (but could use non-public)
    private Object makeInstanceOfClass(Class cls) throws IllegalAccessException, InstantiationException {
        return cls.newInstance();
    }

    // Copy any object that is an instanceof Map.
    private Object copyMap(Object obj) throws RemoteException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Map src = (Map) obj;
        Map result = (Map) makeInstanceOfClass(src.getClass());
        // Do this early, or self-references cause stack overflow!
        objRefs.put(src, result);
        Iterator iter = src.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) (iter.next());
            Object key = entry.getKey();
            Object value = entry.getValue();
            // Checks for null are handled in reflectCopy.
            Object newKey = reflectCopy(key);
            Object newValue = reflectCopy(value);
            result.put(newKey, newValue);
        }

        return result;
    }

    // Pass in attrs just to avoid looking them up again.
    private Object copyAnyClass(ReflectAttrs attrs, Object obj)
            throws RemoteException, InstantiationException, IllegalAccessException, InvocationTargetException {
        // regular object, so copy the fields over
        Constructor cons = attrs.constr;
        if (cons == null)
            throw new IllegalArgumentException("Class " + attrs.thisClass + " is not Serializable");

        Object copy = cons.newInstance();

        // Do this before copyFields, or self-references cause stack overflow!
        objRefs.put(obj, copy);
        copyFields(attrs.thisClass, attrs.fields, obj, copy);
        Class cls = attrs.superClass;
        while (cls != null && cls != Object.class) {
            attrs = getClassAttrs(cls);
            copyFields(cls, attrs.fields, obj, copy);
            cls = attrs.superClass;
        }

        return copy;
    }

    /**
     * Utility to copy objects using Java reflection. Used by the local stub to copy objects
     *
     * @param obj the object to copy or connect.
     * @return the copied object.
     */
    private final Object reflectCopy(Object obj) throws RemoteException, InstantiationException, IllegalAccessException, InvocationTargetException {
        // Always check for nulls here, so we don't need to check in other places.
        if (obj == null)
            return null;

        Class cls = obj.getClass();
        ReflectAttrs attrs = getClassAttrs(cls);

        Object copy = null;

        if (attrs.isImmutable || (obj instanceof org.omg.CORBA.Object)) {
            return obj;
        }

        if (obj instanceof Remote) {
            return Utility.autoConnect(obj, orb, true);
        }

        copy = objRefs.get(obj);
        if (copy == null) {
            // Handle instance of HashMap specially because Map.Entry contains
            // non-static finals. HashTable is likewise handled here.
            if ((cls.getName().equals("java.util.HashMap")) || (cls.getName().equals("java.util.HashTable"))) {
                copy = copyMap(obj);
            } else {
                Class aClass = attrs.arrayClass;

                if (aClass != null) {
                    // object is an array, so do the array copy
                    copy = arrayCopy(obj, aClass);
                } else {
                    if (attrs.isDate) {
                        copy = new java.util.Date(((java.util.Date) obj).getTime());
                        objRefs.put(obj, copy);
                    } else if (attrs.isSQLDate) {
                        copy = new java.sql.Date(((java.sql.Date) obj).getTime());
                        objRefs.put(obj, copy);
                    } else {
                        copy = copyAnyClass(attrs, obj);
                    }
                }
            }
        }

        return copy;
    }

    // This is the public interface. It must never be called from
    // inside this class. It is the single point at which all exceptions
    // are caught, wrapper, and rethrown as ReflectiveCopyExceptions.
    // This can trigger fallback behavior in IasUtilDelegate.
    public Object copy(final Object obj, boolean debug) throws ReflectiveCopyException {
        return copy(obj);
    }

    public Object copy(final Object obj) throws ReflectiveCopyException {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction() {
                public Object run() throws RemoteException, InstantiationException, IllegalAccessException, InvocationTargetException {
                    return reflectCopy(obj);
                }
            });
        } catch (ThreadDeath td) {
            throw td;
        } catch (Throwable thr) {
            throw new ReflectiveCopyException("Could not copy object of class " + obj.getClass().getName(), thr);
        }
    }
}
