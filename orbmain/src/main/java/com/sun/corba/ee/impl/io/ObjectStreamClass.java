/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-2012 IBM Corp. All rights reserved.
 * Copyright (c) 2019 Payara Services Ltd.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.io;

import com.sun.corba.ee.impl.misc.ClassInfoCache;
import com.sun.corba.ee.impl.util.RepositoryId;
import com.sun.corba.ee.spi.trace.TraceValueHandler;
import org.glassfish.pfl.basic.concurrent.SoftCache;
import org.glassfish.pfl.basic.reflection.Bridge;
import org.omg.CORBA.ValueMember;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.security.AccessController;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A ObjectStreamClass describes a class that can be serialized to a stream
 * or a class that was serialized to a stream.  It contains the name
 * and the serialVersionUID of the class.
 * <br>
 * The ObjectStreamClass for a specific class loaded in this Java VM can
 * be found using the lookup method.
 *
 * @author  Roger Riggs
 * @version ObjectStreamClass.java   1.17 99/06/07
 * @since   JDK1.1
 */
@TraceValueHandler
public class ObjectStreamClass implements java.io.Serializable {
    private static final boolean DEBUG_SVUID = false ;

    public static final long kDefaultUID = -1;

    // True if this is an Enum type (6877056)
    private boolean isEnum ;

    private static final Bridge bridge = 
        AccessController.doPrivileged(
            new PrivilegedAction<Bridge>() {
                public Bridge run() {
                    return Bridge.get() ;
                }
            } 
        ) ;

    /** Find the descriptor for a class that can be serialized.  Null
     * is returned if the specified class does not implement
     * java.io.Serializable or java.io.Externalizable.
     */
    @TraceValueHandler
    static final ObjectStreamClass lookup(Class<?> cl) {
        ObjectStreamClass desc = lookupInternal(cl);
        if (desc.isSerializable() || desc.isExternalizable())
            return desc;
        return null;
    }

    /*
     * Find the class descriptor for the specified class.
     * Package access only so it can be called from ObjectIn/OutStream.
     */
    static ObjectStreamClass lookupInternal(Class<?> cl)
    {
        /* Synchronize on the hashtable so no two threads will do
         * this at the same time.
         */
        ObjectStreamClass desc = null;
        synchronized (descriptorFor) {
            /* Find the matching descriptor if it already known */
            desc = (ObjectStreamClass)descriptorFor.get( cl ) ;
            if (desc == null) {
                /* Check if it's serializable */
                ClassInfoCache.ClassInfo cinfo = ClassInfoCache.get( cl ) ;
                boolean serializable = Serializable.class.isAssignableFrom(cl) ;
                
                /* If the class is only Serializable,
                 * lookup the descriptor for the superclass.
                 */
                ObjectStreamClass superdesc = null;
                if (serializable) {
                    Class<?> superclass = cl.getSuperclass();
                    if (superclass != null)
                        superdesc = lookup(superclass);
                }

                /* Check if its' externalizable.
                 * If it's Externalizable, clear the serializable flag.
                 * Only one or the other may be set in the protocol.
                 */
                boolean externalizable = false;
                if (serializable) {
                    externalizable =
                        ((superdesc != null) && superdesc.isExternalizable()) ||
                        cinfo.isAExternalizable(cl);
                    if (externalizable) {
                        serializable = false;
                    }
                }

                /* Create a new version descriptor,
                 * it put itself in the known table.
                 */
                desc = new ObjectStreamClass(cl, superdesc,
                                             serializable, externalizable);
            }

            // Must always call init.  See bug 4488137.  This code was
            // incorrectly changed to return immediately on a non-null
            // cache result.  That allowed threads to gain access to
            // unintialized instances.
            //
            // All threads must sync on the member variable lock
            // and check the initialization state.
            //
            // Another possibility is to continue to synchronize on the
            // descriptorFor map, but that leads to poor performance
            // (see bug 4165204 "ObjectStreamClass can hold global lock
            // for a very long time").
            //
            // We will need to live with 4165204 until we can rewrite 
            // this to follow the improved java.io.ObjectStreamClass
            // in J2SE 1.4 and later.  Calling desc.init() outside of
            // this synchronized block can lead to deadlocks as
            // reported in 5104239.
            desc.init();
        }

        return desc;
    }

    /**
     * The name of the class described by this descriptor.
     * @return name of class
     */
    public final String getName() {
        return name;
    }

    /**
     * Return the serialVersionUID for this class.
     * The serialVersionUID defines a set of classes all with the same name
     * that have evolved from a common root class and agree to be serialized
     * and deserialized using a common format.
     * @param clazz class to get UID of
     * @return UID for the class
     */
    public static final long getSerialVersionUID( java.lang.Class<?> clazz) {
        ObjectStreamClass theosc = ObjectStreamClass.lookup( clazz );
        if (theosc != null) {
            return theosc.getSerialVersionUID( );
        }
        return 0;
    }

    /**
     * Return the serialVersionUID for this class.
     * The serialVersionUID defines a set of classes all with the same name
     * that have evolved from a common root class and agree to be serialized
     * and deserialized using a common format.
     * @return SerialVersionUID for this class.
     */
    public final long getSerialVersionUID() {
        return suid;
    }

    /**
     * Return the serialVersionUID string for this class.
     * The serialVersionUID defines a set of classes all with the same name
     * that have evolved from a common root class and agree to be serialized
     * and deserialized using a common format.
     * @return UID for this class
     */
    public final String getSerialVersionUIDStr() {
        if (suidStr == null)
            suidStr = Long.toHexString(suid).toUpperCase();
        return suidStr;
    }

    /**
     * Return the actual (computed) serialVersionUID for this class.
     * @param clazz Class to get UID of
     * @return The class UID
     */
    public static final long getActualSerialVersionUID( java.lang.Class<?> clazz )
    {
        ObjectStreamClass theosc = ObjectStreamClass.lookup( clazz );
        if( theosc != null )
        {
                return theosc.getActualSerialVersionUID( );
        }
        return 0;
    }

    /**
     * Return the actual (computed) serialVersionUID for this class.
     * @return UID for this class
     */
    public final long getActualSerialVersionUID() {
        return actualSuid;
    }

    /**
     * Return the actual (computed) serialVersionUID for this class.
     * @return UID for this class
     */
    public final String getActualSerialVersionUIDStr() {
        if (actualSuidStr == null)
            actualSuidStr = Long.toHexString(actualSuid).toUpperCase();
        return actualSuidStr;
    }

    /**
     * Return the class in the local VM that this version is mapped to.
     * Null is returned if there is no corresponding local class.
     * @return Class this is mapped to
     */
    public final Class<?> forClass() {
        return ofClass;
    }

    /**
     * Return an array of the fields of this serializable class.
     * @return an array containing an element for each persistent
     * field of this class. Returns an array of length zero if
     * there are no fields.
     * @since JDK1.2
     */
    public ObjectStreamField[] getFields() {
        // Return a copy so the caller can't change the fields.
        if (fields.length > 0) {
            ObjectStreamField[] dup = new ObjectStreamField[fields.length];
            System.arraycopy(fields, 0, dup, 0, fields.length);
            return dup;
        } else {
            return fields;
        }
    }

    public boolean hasField(ValueMember field)
    {
        try {
            for (int i = 0; i < fields.length; i++) {
                if (fields[i].getName().equals(field.name)) {
                    if (fields[i].getSignature().equals(
                        ValueUtility.getSignature(field)))
                        return true;
                }
            }
        } catch (Exception exc) {
            // Ignore this; all we want to do is return false
            // Note that ValueUtility.getSignature can throw checked exceptions.
        }

        return false;
    }

    /* Avoid unnecessary allocations. */
    final ObjectStreamField[] getFieldsNoCopy() {
        return fields;
    }

    /**
     * Get the field of this class by name.
     * @param name name of the field to get
     * @return The ObjectStreamField object of the named field or null if there
     * is no such named field.
     */
    public final ObjectStreamField getField(String name) {
        /* Binary search of fields by name.
         */
        for (int i = fields.length-1; i >= 0; i--) {
            if (name.equals(fields[i].getName())) {
                return fields[i];
            }
        }
        return null;
    }

    public Serializable writeReplace(Serializable value) {
        if (writeReplaceObjectMethod != null) {
            try {
                return (Serializable) writeReplaceObjectMethod.invoke(value);
            } catch(Throwable t) {
                throw new RuntimeException(t);
            }
        }
        else return value;
    }

    public Object readResolve(Object value) {
        if (readResolveObjectMethod != null) {
            try {
                return readResolveObjectMethod.invoke(value);
            } catch(Throwable t) {
                throw new RuntimeException(t);
            }
        }
        else return value;
    }

    /**
     * Return a string describing this ObjectStreamClass.
     */
    @Override
    public final String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append(name);
        sb.append(": static final long serialVersionUID = ");
        sb.append(Long.toString(suid));
        sb.append("L;");
        return sb.toString();
    }

    /*
     * Create a new ObjectStreamClass from a loaded class.
     * Don't call this directly, call lookup instead.
     */
    private ObjectStreamClass(java.lang.Class<?> cl, ObjectStreamClass superdesc,
                              boolean serial, boolean extern)
    {
        ofClass = cl;           /* created from this class */

        if (Proxy.isProxyClass(cl)) {
            forProxyClass = true;
        }

        name = cl.getName();
        // 6877056
        final ClassInfoCache.ClassInfo cinfo = ClassInfoCache.get( cl ) ;
        isEnum = cinfo.isEnum() ;
        superclass = superdesc;
        serializable = serial;
        if (!forProxyClass) {
            // proxy classes are never externalizable
            externalizable = extern;
        }

        /*
         * Enter this class in the table of known descriptors.
         * Otherwise, when the fields are read it may recurse
         * trying to find the descriptor for itself.
         */
        descriptorFor.put( cl, this ) ;
    }

    MethodHandle getWriteObjectMethod() {
        return writeObjectMethod;
    }

    MethodHandle getReadObjectMethod() {
        return readObjectMethod;
    }

    private static final class PersistentFieldsValue {
        private final ConcurrentMap<Class<?>, Object> map = new ConcurrentHashMap<Class<?>, Object>();
        private static final Object NULL_VALUE =
                (PersistentFieldsValue.class.getName() + ".NULL_VALUE");

        PersistentFieldsValue() { }

        ObjectStreamField[] get(Class<?> type) {
            Object value = map.get(type);
            if (value == null) {
                value = computeValue(type);
                Object oldValue = map.putIfAbsent(type, value);
                if (oldValue != null) {
                    value = oldValue;
                }
            }
            return ((value == NULL_VALUE) ? null : (ObjectStreamField[])value);
        }

        private static Object computeValue(Class<?> type) {
            try {
                bridge.ensureClassInitialized(type);
                Field pf = type.getDeclaredField("serialPersistentFields");
                int mods = pf.getModifiers();
                if (Modifier.isPrivate(mods) && Modifier.isStatic(mods) && Modifier.isFinal(mods)) {
                    java.io.ObjectStreamField[] fields = bridge.getObject(type, bridge.staticFieldOffset(pf));
                    return translateFields(fields);
                }
            } catch (NoSuchFieldException | IllegalArgumentException | ClassCastException ignored) {
            }
            return NULL_VALUE;
        }

        private static ObjectStreamField[] translateFields(java.io.ObjectStreamField[] fields) {
            ObjectStreamField[] translation = new ObjectStreamField[fields.length];
            for (int i = 0; i < fields.length; i++)
                translation[i] = new ObjectStreamField(fields[i].getName(), fields[i].getType());
            return translation;
        }
    }

    private static final PersistentFieldsValue persistentFieldsValue =
            new PersistentFieldsValue();
    /*
     * Initialize class descriptor.  This method is only invoked on class
     * descriptors created via calls to lookupInternal().  This method was kept
     * separate from the ObjectStreamClass constructor so that lookupInternal
     * does not have to hold onto a global class descriptor table lock while the
     * class descriptor is being initialized (see bug 4165204).  However,
     * this change leads to a (rare) deadlock.  This code needs to be
     * significantly re-written as in later (1.4) JDK releases.
     */
    private void init() {
        synchronized (lock) {
            // See description at definition of initialized.
            if (initialized)
                return;

            final Class<?> cl = ofClass;

            // 6877056
            if (!serializable || externalizable || forProxyClass || isEnum ||
                name.equals("java.lang.String")) {
                fields = NO_FIELDS;
            } else if (serializable) {
                fields = null ;

                /* Ask for permission to override field access checks.
                 */
                AccessController.doPrivileged(new PrivilegedAction<Object>() {
                    public Object run() {
                        /* Fill in the list of persistent fields.
                         * If it is declared, use the declared serialPersistentFields.
                         * Otherwise, extract the fields from the class itself.
                         */
                        fields = persistentFieldsValue.get(cl);

                        if (fields == null) {
                            /* Get all of the declared fields for this
                             * Class. setAccessible on all fields so they
                             * can be accessed later.  Create a temporary
                             * ObjectStreamField array to hold each
                             * non-static, non-transient field. Then copy the
                             * temporary array into an array of the correct
                             * size once the number of fields is known.
                             */
                            Field[] actualfields = cl.getDeclaredFields();

                            int numFields = 0;
                            ObjectStreamField[] tempFields =
                                new ObjectStreamField[actualfields.length];
                            for (int i = 0; i < actualfields.length; i++) {
                                Field fld = actualfields[i] ;
                                int modifiers = fld.getModifiers();
                                if (!Modifier.isStatic(modifiers) &&
                                    !Modifier.isTransient(modifiers)) {
                //                    fld.setAccessible(true) ;
                                    tempFields[numFields++] = new ObjectStreamField(fld);
                                }
                            }

                            fields = new ObjectStreamField[numFields];
                            System.arraycopy(tempFields, 0, fields, 0, numFields);
                        } else {
                            // For each declared persistent field, look for an actual
                            // reflected Field. If there is one, make sure it's the correct
                            // type and cache it in the ObjectStreamClass for that field.
                            for (int j = fields.length-1; j >= 0; j--) {
                                try {
                                    Field reflField = cl.getDeclaredField(fields[j].getName());
                                    if (fields[j].getType() == reflField.getType()) {
                                        reflField.setAccessible(true);
                                        fields[j].setField(reflField);
                                    } else {
                                        Exceptions.self.fieldTypeMismatch(
                                            cl.getName(),
                                            fields[j].getName(),
                                            fields[j].getType(),
                                            reflField.getName(),
                                            reflField.getType() ) ;
                                    }
                                } catch (NoSuchFieldException e) {
                                    Exceptions.self.noSuchField( e, cl.getName(),
                                        fields[j].getName() ) ;
                                }
                            }
                        }

                        return null;
                    }
                });

                if (fields.length > 1)
                    Arrays.sort(fields);

                /* Set up field data for use while writing using the API api. */
                computeFieldInfo();
            }

            /* Get the serialVersionUID from the class.
             * It uses the access override mechanism so make sure
             * the field objects is only used here.
             *
             * NonSerializable classes have a serialVerisonUID of 0L.
             */
             // 6877056
             if (isNonSerializable() || isEnum) {
                 suid = 0L;
             } else {
                 // Lookup special Serializable members using reflection.
                 AccessController.doPrivileged(new PrivilegedAction<Object>() {
                    public Object run() {
                        if (forProxyClass) {
                            // proxy classes always have serialVersionUID of 0L
                            suid = 0L;
                        } else {
                            try {
                                final Field f = cl.getDeclaredField("serialVersionUID");
                                int mods = f.getModifiers();
                                // SerialBug 5:  static final SUID should be read
                                if (Modifier.isStatic(mods) && Modifier.isFinal(mods) ) {
                                    long offset = bridge.staticFieldOffset(f);
                                    suid = bridge.getLong(cl, offset);
                                    // SerialBug 2: should be computed after writeObject
                                    // actualSuid = computeStructuralUID(cl);
                                } else {
                                    suid = _computeSerialVersionUID(cl);
                                    // SerialBug 2: should be computed after writeObject
                                    // actualSuid = computeStructuralUID(cl);
                                }
                            } catch (NoSuchFieldException ex) {
                                suid = _computeSerialVersionUID(cl);
                                // SerialBug 2: should be computed after writeObject
                                // actualSuid = computeStructuralUID(cl);
                            }
                        }

                        writeReplaceObjectMethod = bridge.writeReplaceForSerialization(cl);
                        readResolveObjectMethod = bridge.readResolveForSerialization(cl);

                        if (externalizable) 
                            cons = bridge.newConstructorForExternalization(cl) ;
                        else
                            cons = bridge.newConstructorForSerialization(cl) ;

                        if (serializable && !forProxyClass) {
                            /* Look for the readObject and writeObject methods. ObjectOutputStream will call them as necessary. */
                            writeObjectMethod = bridge.writeObjectForSerialization(cl);
                            readObjectMethod = bridge.readObjectForSerialization(cl);
                        }
                        return null;
                    }
                });
            }

            // This call depends on a lot of information computed above!
            actualSuid = ObjectStreamClass.computeStructuralUID(this, cl);

            // Part of GlassFish issue 5161: also need the repo id if the
            // class is Externalizable.
            // If we have a write object method, precompute the
            // RMI-IIOP stream format version 2 optional data
            // repository ID.  
            if (hasWriteObject() || isExternalizable())
                rmiiiopOptionalDataRepId = computeRMIIIOPOptionalDataRepId();

            // This must be done last.
            initialized = true;
        }
    }

    /**
     * Java to IDL ptc-02-01-12 1.5.1
     *
     * "The rep_id string passed to the start_value method must be
     * 'RMI:org.omg.custom.class:hashcode:suid' where class is the
     * fully-qualified name of the class whose writeObject method
     * is being invoked and hashcode and suid are the class's hashcode
     * and SUID."
     */
    private String computeRMIIIOPOptionalDataRepId() {

        StringBuffer sbuf = new StringBuffer("RMI:org.omg.custom.");
        sbuf.append(RepositoryId.convertToISOLatin1(this.getName()));
        sbuf.append(':');
        sbuf.append(this.getActualSerialVersionUIDStr());
        sbuf.append(':');
        sbuf.append(this.getSerialVersionUIDStr());

        return sbuf.toString();
    }

    /**
     * This will return null if there is no writeObject method.
     * @return A Serializable's optional custom data fake repository ID.
     */
    public final String getRMIIIOPOptionalDataRepId() {
        return rmiiiopOptionalDataRepId;
    }

    /*
     * Create an empty ObjectStreamClass for a class about to be read.
     * This is separate from read so ObjectInputStream can assign the
     * wire handle early, before any nested ObjectStreamClass might
     * be read.
     */
    ObjectStreamClass(String n, long s) {
        name = n;
        suid = s;
        superclass = null;
    }

    public static final synchronized ObjectStreamField[] translateFields(
            java.io.ObjectStreamField fields[]) {
        return PersistentFieldsValue.translateFields(fields);
    }

    /* Compare the base class names of streamName and localName.
     *
     * @return  Return true iff the base class name compare.
     * @parameter streamName    Fully qualified class name.
     * @parameter localName     Fully qualified class name.
     * @parameter pkgSeparator  class names use either '.' or '/'.
     *
     * Only compare base class name to allow package renaming.
     */
    static boolean compareClassNames(String streamName,
                                     String localName,
                                     char pkgSeparator) {
        /* compare the class names, stripping off package names. */
        int streamNameIndex = streamName.lastIndexOf(pkgSeparator);
        if (streamNameIndex < 0)
            streamNameIndex = 0;

        int localNameIndex = localName.lastIndexOf(pkgSeparator);
        if (localNameIndex < 0)
            localNameIndex = 0;

        return streamName.regionMatches(false, streamNameIndex,
                                        localName, localNameIndex,
                                        streamName.length() - streamNameIndex);
    }

    /*
     * Compare the types of two class descriptors.
     * They match if they have the same class name and suid
     */
    final boolean typeEquals(ObjectStreamClass other) {
        return (suid == other.suid) &&
            compareClassNames(name, other.name, '.');
    }

    /*
     * Return the superclass descriptor of this descriptor.
     */
    final void setSuperclass(ObjectStreamClass s) {
        superclass = s;
    }

    /*
     * Return the superclass descriptor of this descriptor.
     */
    final ObjectStreamClass getSuperclass() {
        return superclass;
    }

    /**
     * Return whether the class has a readObject method
     */
    final boolean hasReadObject() {
        return readObjectMethod != null;
    }

    /*
     * Return whether the class has a writeObject method
     */
    final boolean hasWriteObject() {
        return writeObjectMethod != null ;
    }

    /**
     * Returns when or not this class should be custom
     * marshaled (use chunking).  This should happen if
     * it is Externalizable OR if it or
     * any of its superclasses has a writeObject method,
     */
    final boolean isCustomMarshaled() {
        return (hasWriteObject() || isExternalizable())
            || (superclass != null && superclass.isCustomMarshaled());
    }

    /*
     * Return true if all instances of 'this' Externalizable class
     * are written in block-data mode from the stream that 'this' was read
     * from. <p>
     *
     * In JDK 1.1, all Externalizable instances are not written
     * in block-data mode.
     * In JDK 1.2, all Externalizable instances, by default, are written
     * in block-data mode and the Externalizable instance is terminated with
     * tag TC_ENDBLOCKDATA. Change enabled the ability to skip Externalizable
     * instances.
     *
     * IMPLEMENTATION NOTE:
     *   This should have been a mode maintained per stream; however,
     *   for compatibility reasons, it was only possible to record
     *   this change per class. All Externalizable classes within
     *   a given stream should either have this mode enabled or
     *   disabled. This is enforced by not allowing the PROTOCOL_VERSION
     *   of a stream to he changed after any objects have been written.
     *
     * @see ObjectOuputStream#useProtocolVersion
     * @see ObjectStreamConstants#PROTOCOL_VERSION_1
     * @see ObjectStreamConstants#PROTOCOL_VERSION_2
     *
     * @since JDK 1.2
     */
    boolean hasExternalizableBlockDataMode() {
        return hasExternalizableBlockData;
    }

    /**
     * Creates a new instance of the represented class.  If the class is
     * externalizable, invokes its public no-arg constructor; otherwise, if the
     * class is serializable, invokes the no-arg constructor of the first
     * non-serializable superclass.  Throws UnsupportedOperationException if
     * this class descriptor is not associated with a class, if the associated
     * class is non-serializable or if the appropriate no-arg constructor is
     * inaccessible/unavailable.
     */
    Object newInstance()
        throws InstantiationException, InvocationTargetException,
               UnsupportedOperationException
    {
        if (cons != null) {
            try {
                return cons.newInstance(new Object[0]);
            } catch (IllegalAccessException ex) {
                // should not occur, as access checks have been suppressed
                InternalError ie = new InternalError();
                ie.initCause( ex ) ;
                throw ie ;
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /*
     * Get the Serializability of the class.
     */
    boolean isSerializable() {
        return serializable;
    }

    /*
     * Get the externalizability of the class.
     */
    boolean isExternalizable() {
        return externalizable;
    }

    boolean isNonSerializable() {
        return ! (externalizable || serializable);
    }

    /*
     * Calculate the size of the array needed to store primitive data and the
     * number of object references to read when reading from the input
     * stream.
     */
    private void computeFieldInfo() {
        primBytes = 0;
        objFields = 0;

        for (int i = 0; i < fields.length; i++ ) {
            switch (fields[i].getTypeCode()) {
            case 'B':
            case 'Z':
                primBytes += 1;
                break;
            case 'C':
            case 'S':
                primBytes += 2;
                break;

            case 'I':
            case 'F':
                primBytes += 4;
                break;
            case 'J':
            case 'D' :
                primBytes += 8;
                break;

            case 'L':
            case '[':
                objFields += 1;
                break;
            }
        }
    }

    private static void msg( String str )
    {
        System.out.println( str ) ;
    }

    /* JDK 1.5 has introduced some new modifier bits (such as SYNTHETIC)
     * that can affect the SVUID computation (see bug 4897937).  These bits
     * must be ignored, as otherwise interoperability with ORBs in earlier
     * JDK versions can be compromised.  I am adding these masks for this
     * purpose as discussed in the CCC for this bug (see http://ccc.sfbay/4897937).
     */

    public static final int CLASS_MASK = Modifier.PUBLIC | Modifier.FINAL |
        Modifier.INTERFACE | Modifier.ABSTRACT ;
    public static final int FIELD_MASK = Modifier.PUBLIC | Modifier.PRIVATE |
        Modifier.PROTECTED | Modifier.STATIC | Modifier.FINAL | 
        Modifier.TRANSIENT | Modifier.VOLATILE ;
    public static final int METHOD_MASK = Modifier.PUBLIC | Modifier.PRIVATE |
        Modifier.PROTECTED | Modifier.STATIC | Modifier.FINAL | 
        Modifier.SYNCHRONIZED | Modifier.NATIVE | Modifier.ABSTRACT |
        Modifier.STRICT ;
    
    /*
     * Compute a hash for the specified class.  Incrementally add
     * items to the hash accumulating in the digest stream.
     * Fold the hash into a long.  Use the SHA secure hash function.
     */
    private static long _computeSerialVersionUID(Class<?> cl) {
        if (DEBUG_SVUID)
            msg( "Computing SerialVersionUID for " + cl ) ; 
        ByteArrayOutputStream devnull = new ByteArrayOutputStream(512);
        ClassInfoCache.ClassInfo cinfo = ClassInfoCache.get( cl ) ;

        long h = 0;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA");
            DigestOutputStream mdo = new DigestOutputStream(devnull, md);
            DataOutputStream data = new DataOutputStream(mdo);

            if (DEBUG_SVUID)
                msg( "\twriteUTF( \"" + cl.getName() + "\" )" ) ;
            data.writeUTF(cl.getName());

            int classaccess = cl.getModifiers();
            classaccess &= (Modifier.PUBLIC | Modifier.FINAL |
                            Modifier.INTERFACE | Modifier.ABSTRACT);

            /* Workaround for javac bug that only set ABSTRACT for
             * interfaces if the interface had some methods.
             * The ABSTRACT bit reflects that the number of methods > 0.
             * This is required so correct hashes can be computed
             * for existing class files.
             * Previously this hack was previously present in the VM.
             */
            Method[] method = cl.getDeclaredMethods();
            if ((classaccess & Modifier.INTERFACE) != 0) {
                classaccess &= (~Modifier.ABSTRACT);
                if (method.length > 0) {
                    classaccess |= Modifier.ABSTRACT;
                }
            }

            // Mask out any post-1.4 attributes
            classaccess &= CLASS_MASK ;

            if (DEBUG_SVUID)
                msg( "\twriteInt( " + classaccess + " ) " ) ;
            data.writeInt(classaccess);

            /*
             * Get the list of interfaces supported,
             * Accumulate their names their names in Lexical order
             * and add them to the hash
             */
            if (!cinfo.isArray()) {
                /* In 1.2fcs, getInterfaces() was modified to return
                 * {java.lang.Cloneable, java.io.Serializable} when
                 * called on array classes.  These values would upset
                 * the computation of the hash, so we explicitly omit
                 * them from its computation.
                 */

                Class<?> interfaces[] = cl.getInterfaces();
                Arrays.sort(interfaces, compareClassByName);

                for (int i = 0; i < interfaces.length; i++) {
                    if (DEBUG_SVUID)
                        msg( "\twriteUTF( \"" + interfaces[i].getName() + "\" ) " ) ;
                    data.writeUTF(interfaces[i].getName());
                }
            }

            /* Sort the field names to get a deterministic order */
            Field[] field = cl.getDeclaredFields();
            Arrays.sort(field, compareMemberByName);

            for (int i = 0; i < field.length; i++) {
                Field f = field[i];

                /* Include in the hash all fields except those that are
                 * private transient and private static.
                 */
                int m = f.getModifiers();
                if (Modifier.isPrivate(m) &&
                    (Modifier.isTransient(m) || Modifier.isStatic(m)))
                    continue;

                if (DEBUG_SVUID)
                    msg( "\twriteUTF( \"" + f.getName() + "\" ) " ) ;
                data.writeUTF(f.getName());

                // Mask out any post-1.4 bits
                m &= FIELD_MASK ;

                if (DEBUG_SVUID)
                    msg( "\twriteInt( " + m + " ) " ) ;
                data.writeInt(m);

                if (DEBUG_SVUID)
                    msg( "\twriteUTF( \"" + getSignature(f.getType()) + "\" ) " ) ;
                data.writeUTF(getSignature(f.getType()));
            }

            if (bridge.hasStaticInitializerForSerialization(cl)) {
                if (DEBUG_SVUID)
                    msg( "\twriteUTF( \"<clinit>\" ) " ) ;
                data.writeUTF("<clinit>");

                if (DEBUG_SVUID)
                    msg( "\twriteInt( " + Modifier.STATIC + " )" ) ;
                data.writeInt(Modifier.STATIC); // TBD: what modifiers does it have

                if (DEBUG_SVUID)
                    msg( "\twriteUTF( \"()V\" )" ) ;
                data.writeUTF("()V");
            }

            /*
             * Get the list of constructors including name and signature
             * Sort lexically, add all except the private constructors
             * to the hash with their access flags
             */

            MethodSignature[] constructors =
                MethodSignature.removePrivateAndSort(cl.getDeclaredConstructors());
            for (int i = 0; i < constructors.length; i++) {
                MethodSignature c = constructors[i];
                String mname = "<init>";
                String desc = c.signature;
                desc = desc.replace('/', '.');
                if (DEBUG_SVUID)
                    msg( "\twriteUTF( \"" + mname + "\" )" ) ;
                data.writeUTF(mname);

                // mask out post-1.4 modifiers
                int modifier = c.member.getModifiers() & METHOD_MASK ;

                if (DEBUG_SVUID)
                    msg( "\twriteInt( " + modifier + " ) " ) ;
                data.writeInt( modifier ) ;

                if (DEBUG_SVUID)
                    msg( "\twriteUTF( \"" + desc+ "\" )" ) ;
                data.writeUTF(desc);
            }

            /* Include in the hash all methods except those that are
             * private transient and private static.
             */
            MethodSignature[] methods =
                MethodSignature.removePrivateAndSort(method);
            for (int i = 0; i < methods.length; i++ ) {
                MethodSignature m = methods[i];
                String desc = m.signature;
                desc = desc.replace('/', '.');

                if (DEBUG_SVUID)
                    msg( "\twriteUTF( \"" + m.member.getName()+ "\" )" ) ;
                data.writeUTF(m.member.getName());

                // mask out post-1.4 modifiers
                int modifier = m.member.getModifiers() & METHOD_MASK ;

                if (DEBUG_SVUID)
                    msg( "\twriteInt( " + modifier + " ) " ) ;
                data.writeInt( modifier ) ;

                if (DEBUG_SVUID)
                    msg( "\twriteUTF( \"" + desc + "\" )" ) ;
                data.writeUTF(desc);
            }

            /* Compute the hash value for this class.
             * Use only the first 64 bits of the hash.
             */
            data.flush();
            byte hasharray[] = md.digest();
            for (int i = 0; i < Math.min(8, hasharray.length); i++) {
                h += (long)(hasharray[i] & 255) << (i * 8);
            }
        } catch (IOException ignore) {
            /* can't happen, but be deterministic anyway. */
            h = -1;
        } catch (NoSuchAlgorithmException complain) {
            SecurityException se = new SecurityException() ;
            se.initCause( complain ) ;
            throw se ;
        }

        return h;
    }

    private static long computeStructuralUID(
        com.sun.corba.ee.impl.io.ObjectStreamClass osc, Class<?> cl) {

        ByteArrayOutputStream devnull = new ByteArrayOutputStream(512);
        ClassInfoCache.ClassInfo cinfo = ClassInfoCache.get( cl ) ;
                
        long h = 0;
        try {
            if (!cinfo.isASerializable(cl) || cinfo.isInterface()) {
                return 0;
            }

            if (cinfo.isAExternalizable(cl)) {
                return 1;
            }

            MessageDigest md = MessageDigest.getInstance("SHA");
            DigestOutputStream mdo = new DigestOutputStream(devnull, md);
            DataOutputStream data = new DataOutputStream(mdo);

            // Get SUID of parent
            Class<?> parent = cl.getSuperclass();
            if ((parent != null))  
            // SerialBug 1; acc. to spec the one for 
            // java.lang.object
            // should be computed and put
            //     && (parent != java.lang.Object.class)) 
            {
                data.writeLong(computeStructuralUID(lookup(parent), parent));
            }

            if (osc.hasWriteObject())
                data.writeInt(2);
            else
                data.writeInt(1);

            // CORBA formal 00-11-03 10.6.2:  For each field of the
            // class that is mapped to IDL, sorted lexicographically
            // by Java field name, in increasing order...
            ObjectStreamField[] field = osc.getFields();
            if (field.length > 1) {
                Arrays.sort(field, compareObjStrFieldsByName);
            }

            // ...Java field name in UTF encoding, field
            // descriptor, as defined by the JVM spec...
            for (int i = 0; i < field.length; i++) {
                data.writeUTF(field[i].getName());
                data.writeUTF(field[i].getSignature());
            }
                        
            /* Compute the hash value for this class.
             * Use only the first 64 bits of the hash.
             */
            data.flush();
            byte hasharray[] = md.digest();
            // int minimum = Math.min(8, hasharray.length);
            // SerialBug 3: SHA computation is wrong; for loop reversed
            //for (int i = minimum; i > 0; i--) 
            for (int i = 0; i < Math.min(8, hasharray.length); i++) {
                h += (long)(hasharray[i] & 255) << (i * 8);
            }
        } catch (IOException ignore) {
            /* can't happen, but be deterministic anyway. */
            h = -1;
        } catch (NoSuchAlgorithmException complain) {
            SecurityException se = new SecurityException();
            se.initCause( complain ) ;
            throw se ;
        }
        return h;
    }

    /**
     * Compute the JVM signature for the class.
     */
    static String getSignature(Class<?> clazz) {
        String type = null;
        if (ClassInfoCache.get( clazz ).isArray()) {
            Class<?> cl = clazz;
            int dimensions = 0;
            while (ClassInfoCache.get( cl ).isArray()) {
                dimensions++;
                cl = cl.getComponentType();
            }
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < dimensions; i++) {
                sb.append("[");
            }
            sb.append(getSignature(cl));
            type = sb.toString();
        } else if (clazz.isPrimitive()) {
            if (clazz == Integer.TYPE) {
                type = "I";
            } else if (clazz == Byte.TYPE) {
                type = "B";
            } else if (clazz == Long.TYPE) {
                type = "J";
            } else if (clazz == Float.TYPE) {
                type = "F";
            } else if (clazz == Double.TYPE) {
                type = "D";
            } else if (clazz == Short.TYPE) {
                type = "S";
            } else if (clazz == Character.TYPE) {
                type = "C";
            } else if (clazz == Boolean.TYPE) {
                type = "Z";
            } else if (clazz == Void.TYPE) {
                type = "V";
            }
        } else {
            type = "L" + clazz.getName().replace('.', '/') + ";";
        }
        return type;
    }

    /*
     * Compute the JVM method descriptor for the method.
     */
    static String getSignature(Method meth) {
        StringBuffer sb = new StringBuffer();

        sb.append("(");

        Class<?>[] params = meth.getParameterTypes(); // avoid clone
        for (int j = 0; j < params.length; j++) {
            sb.append(getSignature(params[j]));
        }
        sb.append(")");
        sb.append(getSignature(meth.getReturnType()));
        return sb.toString();
    }

    /*
     * Compute the JVM constructor descriptor for the constructor.
     */
    static String getSignature(Constructor<?> cons) {
        StringBuffer sb = new StringBuffer();

        sb.append("(");

        Class<?>[] params = cons.getParameterTypes(); // avoid clone
        for (int j = 0; j < params.length; j++) {
            sb.append(getSignature(params[j]));
        }
        sb.append(")V");
        return sb.toString();
    }

    /*
     * Cache of Class -> ClassDescriptor Mappings.
     */
    static private final SoftCache<Class<?>,ObjectStreamClass> descriptorFor =
        new SoftCache<Class<?>,ObjectStreamClass>() ;

    /*
     * The name of this descriptor
     */
    private String name;

    /*
     * The descriptor of the supertype.
     */
    private ObjectStreamClass superclass;

    /*
     * Flags for Serializable and Externalizable.
     */
    private boolean serializable;
    private boolean externalizable;

    /*
     * Array of persistent fields of this class, sorted by
     * type and name.
     */
    private ObjectStreamField[] fields;

    /*
     * Class that is a descriptor for in this virtual machine.
     */
    private Class<?> ofClass;

    /*
     * True if descriptor for a proxy class.
     */
    boolean forProxyClass;


    /*
     * SerialVersionUID for this class.
     */
    private long suid = kDefaultUID;
    private String suidStr = null;

    /*
     * Actual (computed) SerialVersionUID for this class.
     */
    private long actualSuid = kDefaultUID;
    private String actualSuidStr = null;

    /*
     * The total number of bytes of primitive fields.
     * The total number of object fields.
     */
    int primBytes;
    int objFields;

    /**
     * Flag indicating whether or not this instance has 
     * successfully completed initialization.  This is to
     * try to fix bug 4373844.  Working to move to
     * reusing java.io.ObjectStreamClass for JDK 1.5.
     */
    private boolean initialized = false;

    /* Internal lock object. */
    private final Object lock = new Object();

    /* In JDK 1.1, external data was not written in block mode.
     * As of JDK 1.2, external data is written in block data mode. This
     * flag enables JDK 1.2 to be able to read JDK 1.1 written external data.
     *
     * @since JDK 1.2
     */
    private boolean hasExternalizableBlockData;

    private MethodHandle writeObjectMethod;
    private MethodHandle readObjectMethod;
    private transient MethodHandle writeReplaceObjectMethod;
    private transient MethodHandle readResolveObjectMethod;
    private Constructor<?> cons ;

    /**
     * Beginning in Java to IDL ptc/02-01-12, RMI-IIOP has a
     * stream format version 2 which puts a fake valuetype around
     * a Serializable's optional custom data.  This valuetype has
     * a special repository ID made from the Serializable's
     * information which we are pre-computing and
     * storing here.
     */
    private String rmiiiopOptionalDataRepId = null;

    /** use serialVersionUID from JDK 1.1. for interoperability */
    private static final long serialVersionUID = -6120832682080437368L;

    /**
     * Set serialPersistentFields of a Serializable class to this value to
     * denote that the class has no Serializable fields.
     */
    public static final ObjectStreamField[] NO_FIELDS =
        new ObjectStreamField[0];

    /*
     * Comparator object for Classes and Interfaces
     */
    private static Comparator<Class<?>> compareClassByName =
        new CompareClassByName();

    private static class CompareClassByName 
        implements Comparator<Class<?>> {

        public int compare(Class<?> c1, Class<?> c2) {
            return c1.getName().compareTo(c2.getName());
        }
    }

    /**
     * Comparator for ObjectStreamFields by name
     */
    private final static Comparator<ObjectStreamField> compareObjStrFieldsByName
        = new CompareObjStrFieldsByName();

    private static class CompareObjStrFieldsByName 
        implements Comparator<ObjectStreamField> {

        public int compare(ObjectStreamField o1, ObjectStreamField o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }

    /*
     * Comparator object for Members, Fields, and Methods
     */
    private static Comparator<Member> compareMemberByName =
        new CompareMemberByName();

    private static class CompareMemberByName 
        implements Comparator<Member> {

        public int compare(Member o1, Member o2) {
            String s1 = o1.getName();
            String s2 = o2.getName();

            if ((o1 instanceof Method) && (o2 instanceof Method)) {
                s1 += getSignature((Method)o1);
                s2 += getSignature((Method)o2);
            } else if ((o1 instanceof Constructor) && (o2 instanceof Constructor)) {
                s1 += getSignature((Constructor<?>)o1);
                s2 += getSignature((Constructor<?>)o2);
            }
            return s1.compareTo(s2);
        }
    }

    /* It is expensive to recompute a method or constructor signature
       many times, so compute it only once using this data structure. */
    private static class MethodSignature 
        implements Comparator<MethodSignature> {

        Member member;
        String signature;      // cached parameter signature

        /* Given an array of Method or Constructor members,
           return a sorted array of the non-private members.*/
        /* A better implementation would be to implement the returned data
           structure as an insertion sorted link list.*/
        static MethodSignature[] removePrivateAndSort(Member[] m) {
            int numNonPrivate = 0;
            for (int i = 0; i < m.length; i++) {
                if (! Modifier.isPrivate(m[i].getModifiers())) {
                    numNonPrivate++;
                }
            }
            MethodSignature[] cm = new MethodSignature[numNonPrivate];
            int cmi = 0;
            for (int i = 0; i < m.length; i++) {
                if (! Modifier.isPrivate(m[i].getModifiers())) {
                    cm[cmi] = new MethodSignature(m[i]);
                    cmi++;
                }
            }
            if (cmi > 0)
                Arrays.sort(cm, cm[0]);
            return cm;
        }

        /* Assumes that o1 and o2 are either both methods
           or both constructors.*/
        public int compare(MethodSignature c1, MethodSignature c2) {
            /* Arrays.sort calls compare when o1 and o2 are equal.*/
            if (c1 == c2)
                return 0;

            int result;
            if (isConstructor()) {
                result = c1.signature.compareTo(c2.signature);
            } else { // is a Method.
                result = c1.member.getName().compareTo(c2.member.getName());
                if (result == 0)
                    result = c1.signature.compareTo(c2.signature);
            }
            return result;
        }

        private boolean isConstructor() {
            return member instanceof Constructor;
        }

        private MethodSignature(Member m) {
            member = m;
            if (isConstructor()) {
                signature = ObjectStreamClass.getSignature((Constructor<?>)m);
            } else {
                signature = ObjectStreamClass.getSignature((Method)m);
            }
        }
    }

}
