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


import com.sun.corba.ee.impl.javax.rmi.CORBA.Util;
import com.sun.corba.ee.impl.misc.ClassInfoCache ;
import com.sun.corba.ee.impl.util.RepositoryId;
import com.sun.corba.ee.impl.util.Utility;
import com.sun.corba.ee.spi.logging.OMGSystemException;
import com.sun.corba.ee.spi.logging.UtilSystemException;
import com.sun.corba.ee.spi.trace.ValueHandlerRead ;
import com.sun.corba.ee.spi.trace.ValueHandlerWrite ;
import com.sun.org.omg.SendingContext.CodeBase;
import com.sun.org.omg.SendingContext.CodeBaseHelper;

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map ;
import java.util.concurrent.ConcurrentHashMap;

import org.glassfish.pfl.basic.logex.OperationTracer;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.portable.IndirectionException;

@ValueHandlerRead
@ValueHandlerWrite
public final class ValueHandlerImpl implements javax.rmi.CORBA.ValueHandlerMultiFormat {
    private static final OMGSystemException omgWrapper =
        OMGSystemException.self ;
    protected static final UtilSystemException utilWrapper =
        UtilSystemException.self ;

    // Property to override our maximum stream format version
    public static final String FORMAT_VERSION_PROPERTY
        = "com.sun.corba.ee.MaxStreamFormatVersion";

    private static final byte MAX_SUPPORTED_FORMAT_VERSION = (byte)2;
    private static final byte STREAM_FORMAT_VERSION_1 = (byte)1;

    // The ValueHandler's maximum stream format version to advertise,
    // set in a static initializer.
    private static final byte MAX_STREAM_FORMAT_VERSION;

    static {
        MAX_STREAM_FORMAT_VERSION = getMaxStreamFormatVersion();
    }

    // Looks for the FORMAT_VERSION_PROPERTY system property
    // to allow the user to override our default stream format
    // version.  Note that this still only allows them to pick
    // a supported version (1 through MAX_STREAM_FORMAT_VERSION).
    private static byte getMaxStreamFormatVersion() {

        try {
            String propValue = AccessController.doPrivileged( 
                new PrivilegedAction<String>() {
                public String run() {
                    return System.getProperty(
                        ValueHandlerImpl.FORMAT_VERSION_PROPERTY);
                }
            });

            // The property wasn't set
            if (propValue == null) {
                return MAX_SUPPORTED_FORMAT_VERSION;
            }

            byte result = Byte.parseByte(propValue);

            // REVISIT.  Just set to MAX_SUPPORTED_FORMAT_VERSION
            // or really let the system shutdown with this Error?
            if (result < 1 || result > MAX_SUPPORTED_FORMAT_VERSION) {
                throw new ExceptionInInitializerError(
                    "Invalid stream format version: " + result
                    + ".  Valid range is 1 through "
                    + MAX_SUPPORTED_FORMAT_VERSION);
            }

            return result;

        } catch (Exception ex) {
            // REVISIT.  Swallow this or really let
            // the system shutdown with this Error?

            Error err = new ExceptionInInitializerError(ex);
            err.initCause( ex ) ;
            throw err ;
        }
    }

    public static final short kRemoteType = 0;
    public static final short kAbstractType = 1;
    public static final short kValueType = 2;

    // Since the Input/OutputStream is unique to a thread, only one thread
    // can ever put a particular key into the stream pairs maps.  Multiple threads
    // will simultaneously update these maps, so we need a ConcurrentHashMap.
    // But we don't need to use putIfAbsent to store into the maps.
    private final Map<org.omg.CORBA.portable.InputStream,IIOPInputStream>
        inputStreamPairs = new ConcurrentHashMap<org.omg.CORBA.portable.InputStream,
            IIOPInputStream>();
    private final Map<org.omg.CORBA.portable.OutputStream,IIOPOutputStream>
        outputStreamPairs = new ConcurrentHashMap<org.omg.CORBA.portable.OutputStream,
           IIOPOutputStream>();

    // See javax.rmi.CORBA.ValueHandlerMultiFormat
    public byte getMaximumStreamFormatVersion() {
        return MAX_STREAM_FORMAT_VERSION;
    }

    // See javax.rmi.CORBA.ValueHandlerMultiFormat
    @ValueHandlerWrite
    public void writeValue(org.omg.CORBA.portable.OutputStream out,
                           java.io.Serializable value,
                           byte streamFormatVersion) {

        if (streamFormatVersion == 2) {
            if (!(out instanceof org.omg.CORBA.portable.ValueOutputStream)) {
                throw omgWrapper.notAValueoutputstream() ;
            }
        } else if (streamFormatVersion != 1) {
            throw omgWrapper.invalidStreamFormatVersion( 
                streamFormatVersion ) ;
        }

        writeValueWithVersion( out, value, streamFormatVersion);
    }

    private ValueHandlerImpl(){}
    
    static ValueHandlerImpl getInstance() {
    	return new ValueHandlerImpl();
    }

    /**
     * Writes the value to the stream using java semantics.
     * @param _out The stream to write the value to
     * @param value The value to be written to the stream
     **/
    @ValueHandlerWrite
    public void writeValue(org.omg.CORBA.portable.OutputStream _out, 
                           java.io.Serializable value) {

        writeValueWithVersion( _out, value, STREAM_FORMAT_VERSION_1);
    }

    @ValueHandlerWrite
    private void writeValueWithVersion( org.omg.CORBA.portable.OutputStream _out, 
        java.io.Serializable value, byte streamFormatVersion) {

        org.omg.CORBA_2_3.portable.OutputStream out =
            (org.omg.CORBA_2_3.portable.OutputStream) _out;

        IIOPOutputStream jdkToOrbOutputStreamBridge = null;

        jdkToOrbOutputStreamBridge = outputStreamPairs.get(_out);

        if (jdkToOrbOutputStreamBridge == null) {
            jdkToOrbOutputStreamBridge = createOutputStream();
            jdkToOrbOutputStreamBridge.setOrbStream(out);
            outputStreamPairs.put(_out, jdkToOrbOutputStreamBridge);
        }

        try {
            jdkToOrbOutputStreamBridge.increaseRecursionDepth();
            writeValueInternal(jdkToOrbOutputStreamBridge, out,
                value, streamFormatVersion);
        } finally {
            if (jdkToOrbOutputStreamBridge.decreaseRecursionDepth() == 0) {
                outputStreamPairs.remove(_out);
            }
        }
    }

    @ValueHandlerWrite
    private void writeValueInternal( IIOPOutputStream bridge, 
        org.omg.CORBA_2_3.portable.OutputStream out, 
        java.io.Serializable value, byte streamFormatVersion) {

        Class<?> clazz = value.getClass();
        ClassInfoCache.ClassInfo cinfo = ClassInfoCache.get( clazz ) ;
        if (cinfo.isArray()) {
            write_Array(out, value, clazz.getComponentType());
        } else {
            bridge.simpleWriteObject(value, streamFormatVersion);
        }
    }

    /**
     * Reads a value from the stream using java semantics.
     * @param _in The stream to read the value from
     * @param offset offset position in the stream
     * @param clazz The type of the value to be read in
     * @param repositoryID repo ID for the value to read
     * @param _sender The sending context runtime
     * @return The serializable value read from the stream
     **/
    @ValueHandlerRead
    public java.io.Serializable readValue(org.omg.CORBA.portable.InputStream _in, 
        int offset, java.lang.Class clazz, String repositoryID,
        org.omg.SendingContext.RunTime _sender) {

        java.io.Serializable result = null;

        // Must use narrow rather than a direct cast to a com.sun
        // class.  Fix for bug 4379539.
        CodeBase sender = CodeBaseHelper.narrow(_sender);

        org.omg.CORBA_2_3.portable.InputStream in =
            (org.omg.CORBA_2_3.portable.InputStream) _in;

        IIOPInputStream jdkToOrbInputStreamBridge = inputStreamPairs.get(_in);
        if (jdkToOrbInputStreamBridge == null) {
            jdkToOrbInputStreamBridge = createInputStream();
            jdkToOrbInputStreamBridge.setOrbStream(in);
            jdkToOrbInputStreamBridge.setSender(sender);
            // backward compatability 4365188
            jdkToOrbInputStreamBridge.setValueHandler(this);
            inputStreamPairs.put(_in, jdkToOrbInputStreamBridge);
        }

        try {
            jdkToOrbInputStreamBridge.increaseRecursionDepth();
            result = readValueInternal(jdkToOrbInputStreamBridge, in, offset,
                clazz, repositoryID, sender);
        } finally {
            if (jdkToOrbInputStreamBridge.decreaseRecursionDepth() == 0) {
                inputStreamPairs.remove(_in);
            }
        }

        return result;
    }

    @ValueHandlerRead
    private java.io.Serializable readValueInternal(IIOPInputStream bridge, 
        org.omg.CORBA_2_3.portable.InputStream in, int offset, 
        java.lang.Class<?> clazz, String repositoryID,
        com.sun.org.omg.SendingContext.CodeBase sender) {

        java.io.Serializable result = null;

        if (clazz == null) {
            // clazz == null indicates an FVD situation for a nonexistant class
            if (isArray(repositoryID)){
                read_Array( bridge, in, null, sender, offset);
            } else {
                bridge.simpleSkipObject( repositoryID, sender);
            }
            return result;
        }

        ClassInfoCache.ClassInfo cinfo = ClassInfoCache.get( clazz ) ;
        if (cinfo.isArray()) {
            result = (java.io.Serializable)read_Array(
                bridge, in, clazz, sender, offset);
        } else {
            result = (java.io.Serializable)bridge.simpleReadObject(
                clazz, cinfo, repositoryID, sender, offset);
        }

        return result;
    }

    /**
     * Returns the repository ID for the given RMI value Class.
     * @param clz The class to return a repository ID for.
     * @return the repository ID of the Class.
     **/
    public java.lang.String getRMIRepositoryID(java.lang.Class clz) {
        return RepositoryId.createForJavaType(clz);
    }

    /**
     * Indicates whether the given Class performs custom or
     * default marshaling.
     * @param clz The class to test for custom marshaling.
     * @return True if the class performs custom marshaling, false
     * if it does not.
     **/
    public boolean isCustomMarshaled(java.lang.Class clz) {
        return ObjectStreamClass.lookup(clz).isCustomMarshaled();
    }

    /**
     * Returns the CodeBase for this ValueHandler.  This is used by
     * the ORB runtime.  The server sends the service context containing
     * the IOR for this CodeBase on the first GIOP reply.  The clients
     * do the same on the first GIOP request.
     * @return the SendingContext.CodeBase of this ValueHandler.
     **/
    public org.omg.SendingContext.RunTime getRunTimeCodeBase() {
        CodeBase codeBase = new FVDCodeBaseImpl(this);
        return codeBase;
    }


    // methods supported for backward compatability so that the appropriate
    // Rep-id calculations take place based on the ORB version

    /**
     *  Returns a boolean of whether or not RepositoryId indicates
     *  FullValueDescriptor.
     *  used for backward compatability
     * @param clazz The type to get description for
     * @param repositoryID The repository ID
     * @return If full description should be retrieved
     * @throws IOException If suids do not match or if the repositoryID
     * is not an RMIValueType.
     */
     public boolean useFullValueDescription(Class clazz, String repositoryID) throws IOException
     {
        return RepositoryId.useFullValueDescription(clazz, repositoryID);
     }

     public String getClassName(String id)
     {
        RepositoryId repID = RepositoryId.cache.getId(id);
        return repID.getClassName();
     }

     public Class getClassFromType(String id)
        throws ClassNotFoundException
     {
        RepositoryId repId = RepositoryId.cache.getId(id);
        return repId.getClassFromType();
     }

     public Class getAnyClassFromType(String id)
        throws ClassNotFoundException
     {
        RepositoryId repId = RepositoryId.cache.getId(id);
        return repId.getAnyClassFromType();
     }

     public String createForAnyType(Class cl)
     {
        return RepositoryId.createForAnyType(cl);
     }

     public String getDefinedInId(String id)
     {
        RepositoryId repId = RepositoryId.cache.getId(id);
        return repId.getDefinedInId();
     }

     public String getUnqualifiedName(String id)
     {
        RepositoryId repId = RepositoryId.cache.getId(id);
        return repId.getUnqualifiedName();
     }

     public String getSerialVersionUID(String id)
     {
        RepositoryId repId = RepositoryId.cache.getId(id);
        return repId.getSerialVersionUID();
     }


     public boolean isAbstractBase(Class clazz)
     {
        return RepositoryId.isAbstractBase(clazz);
     }

     public boolean isSequence(String id)
     {
        RepositoryId repId = RepositoryId.cache.getId(id);
        return repId.isSequence();
     }

    /**
     * If the value contains a writeReplace method then the result
     * is returned.  Otherwise, the value itself is returned.
     * @return the true value to marshal on the wire.
     **/
    @ValueHandlerWrite
    public java.io.Serializable writeReplace(java.io.Serializable value) {
        return ObjectStreamClass.lookup(value.getClass()).writeReplace(value);
    }

    /**
     * Encapsulates writing of Java char arrays so that the 1.3 subclass
     * can override it without exposing internals across packages.  This
     * is a fix for bug 4367783.
     */
    @ValueHandlerWrite
    private void writeCharArray(org.omg.CORBA_2_3.portable.OutputStream out,
                                char[] array,
                                int offset,
                                int length)
    {
        out.write_wchar_array(array, offset, length);
    }

    @ValueHandlerWrite
    private void write_Array(org.omg.CORBA_2_3.portable.OutputStream out, 
        java.io.Serializable obj, Class type) {

        int i, length;
        ClassInfoCache.ClassInfo cinfo = ClassInfoCache.get( type ) ;

        if (type.isPrimitive()) {
            if (type == Integer.TYPE) {
                int[] array = (int[])((Object)obj);
                length = array.length;
                out.write_ulong(length);
                out.write_long_array(array, 0, length);
            } else if (type == Byte.TYPE) {
                byte[] array = (byte[])((Object)obj);
                length = array.length;
                out.write_ulong(length);
                out.write_octet_array(array, 0, length);
            } else if (type == Long.TYPE) {
                long[] array = (long[])((Object)obj);
                length = array.length;
                out.write_ulong(length);
                out.write_longlong_array(array, 0, length);
            } else if (type == Float.TYPE) {
                float[] array = (float[])((Object)obj);
                length = array.length;
                out.write_ulong(length);
                out.write_float_array(array, 0, length);
            } else if (type == Double.TYPE) {
                double[] array = (double[])((Object)obj);
                length = array.length;
                out.write_ulong(length);
                out.write_double_array(array, 0, length);
            } else if (type == Short.TYPE) {
                short[] array = (short[])((Object)obj);
                length = array.length;
                out.write_ulong(length);
                out.write_short_array(array, 0, length);
            } else if (type == Character.TYPE) {
                char[] array = (char[])((Object)obj);
                length = array.length;
                out.write_ulong(length);
                writeCharArray(out, array, 0, length);
            } else if (type == Boolean.TYPE) {
                boolean[] array = (boolean[])((Object)obj);
                length = array.length;
                out.write_ulong(length);
                out.write_boolean_array(array, 0, length);
            } else {
                throw Exceptions.self.invalidPrimitiveType(
                    obj.getClass().getName() ) ;
            }
        } else if (type == java.lang.Object.class) {
            Object[] array = (Object[])((Object)obj);
            length = array.length;
            out.write_ulong(length);
            for (i = 0; i < length; i++) {               
                Util.getInstance().writeAny(out, array[i]);
            }
        } else {
            Object[] array = (Object[])((Object)obj);
            length = array.length;
            out.write_ulong(length);
            int callType = kValueType;
                        
            if (cinfo.isInterface()) { 
                String className = type.getName();
                                
                if (cinfo.isARemote(type)) {
                    // RMI Object reference...
                    callType = kRemoteType;
                } else if (cinfo.isACORBAObject(type)) {
                    // IDL Object reference...
                    callType = kRemoteType;
                } else if (RepositoryId.isAbstractBase(type)) {
                    // IDL Abstract Object reference...
                    callType = kAbstractType;
                } else if (ObjectStreamClassCorbaExt.isAbstractInterface(type)) {
                    callType = kAbstractType;
                }
            }
                        
            for (i = 0; i < length; i++) {
                switch (callType) {
                case kRemoteType: 
                    Util.getInstance().writeRemoteObject(out, array[i]);
                    break;
                case kAbstractType: 
                    Util.getInstance().writeAbstractObject(out,array[i]);
                    break;
                case kValueType:
                    try{
                        out.write_value((java.io.Serializable)array[i]);
                    } catch(ClassCastException cce){
                        if (array[i] instanceof java.io.Serializable) {
                            throw cce;
                        } else {
                            Utility.throwNotSerializableForCorba(
                                array[i].getClass().getName());
                        }
                    }
                    break;
                }
            }
        }
    }

    /**
     * Encapsulates reading of Java char arrays so that the 1.3 subclass
     * can override it without exposing internals across packages.  This
     * is a fix for bug 4367783.
     */
    @ValueHandlerRead
    private void readCharArray(org.omg.CORBA_2_3.portable.InputStream in,
                                 char[] array,
                                 int offset,
                                 int length)
    {  
        in.read_wchar_array(array, offset, length);
    }

    @ValueHandlerRead
    private java.lang.Object read_Array( IIOPInputStream bridge, 
        org.omg.CORBA_2_3.portable.InputStream in, 
        Class sequence, com.sun.org.omg.SendingContext.CodeBase sender, 
        int offset) 
    {
        try {
            // Read length of coming array
            int length = in.read_ulong();
            int i;

            if (sequence == null) {
                for (i = 0; i < length; i++) {
                    in.read_value();
                }

                return null;
            }
                        
            OperationTracer.startReadArray( sequence.getName(), length ) ;

            Class<?> componentType = sequence.getComponentType();
            Class<?> actualType = componentType;
            ClassInfoCache.ClassInfo cinfo = ClassInfoCache.get( 
                componentType ) ;

            if (componentType.isPrimitive()) {
                if (componentType == Integer.TYPE) {
                    int[] array = new int[length];
                    in.read_long_array(array, 0, length);
                    return ((java.io.Serializable)((Object)array));
                } else if (componentType == Byte.TYPE) {
                    byte[] array = new byte[length];
                    in.read_octet_array(array, 0, length);
                    return ((java.io.Serializable)((Object)array));
                } else if (componentType == Long.TYPE) {
                    long[] array = new long[length];
                    in.read_longlong_array(array, 0, length);
                    return ((java.io.Serializable)((Object)array));
                } else if (componentType == Float.TYPE) {
                    float[] array = new float[length];
                    in.read_float_array(array, 0, length);
                    return ((java.io.Serializable)((Object)array));
                } else if (componentType == Double.TYPE) {
                    double[] array = new double[length];
                    in.read_double_array(array, 0, length);
                    return ((java.io.Serializable)((Object)array));
                } else if (componentType == Short.TYPE) {
                    short[] array = new short[length];
                    in.read_short_array(array, 0, length);
                    return ((java.io.Serializable)((Object)array));
                } else if (componentType == Character.TYPE) {
                    char[] array = new char[length];
                    readCharArray(in, array, 0, length);
                    return ((java.io.Serializable)((Object)array));
                } else if (componentType == Boolean.TYPE) {
                    boolean[] array = new boolean[length];
                    in.read_boolean_array(array, 0, length);
                    return ((java.io.Serializable)((Object)array));
                } else {
                    throw Exceptions.self.invalidPrimitiveComponentType(
                        sequence.getName());
                }
            } else if (componentType == java.lang.Object.class) {
                Object[] array = (Object[])java.lang.reflect.Array.newInstance(
                    componentType, length);

                // Store this object and its beginning position
                // since there might be indirections to it while
                // it's been unmarshalled.
                bridge.activeRecursionMgr.addObject(offset, array);

                for (i = 0; i < length; i++) {
                    Object objectValue = null;
                    try {
                        OperationTracer.readingIndex( i ) ;
                        objectValue = Util.getInstance().readAny(in);
                    } catch(IndirectionException cdrie) {
                        try {
                            // The CDR stream had never seen the given offset 
                            // before, so check the recursion manager (it will 
                            // throw an IOException if it doesn't have a 
                            // reference, either).
                            objectValue = bridge.activeRecursionMgr.getObject(
                                cdrie.offset);
                        } catch (IOException ie) {
                            // Translate to a MARSHAL exception since
                            // ValueHandlers aren't allowed to throw
                            // IOExceptions
                            throw utilWrapper.invalidIndirection( ie,
                                                            cdrie.offset ) ;
                        }
                    }
                    
                    array[i] = objectValue;
                }
                return ((java.io.Serializable)((Object)array));
            } else {
                Object[] array = (Object[])java.lang.reflect.Array.newInstance(
                    componentType, length);
                // Store this object and its beginning position
                // since there might be indirections to it while
                // it's been unmarshalled.                              
                bridge.activeRecursionMgr.addObject(offset, array);

                // Decide what method call to make based on the componentType.
                // If it is a componentType for which we need to load a stub,
                // convert the componentType to the correct stub type.
                                
                int callType = kValueType;
                boolean narrow = false;
                                
                if (cinfo.isInterface()) {
                    boolean loadStubClass = false;
                    // String className = componentType.getName();
                        
                    if (cinfo.isARemote(componentType)) {
                        // RMI Object reference...
                        callType = kRemoteType;
                        
                        // for better performance, load the stub class once
                        // instead of for each element of the array
                        loadStubClass = true;
                    } else if (cinfo.isACORBAObject(componentType)) {
                        // IDL Object reference...
                        callType = kRemoteType;
                        loadStubClass = true;
                    } else if (RepositoryId.isAbstractBase(componentType)) {
                        // IDL Abstract Object reference...
                        callType = kAbstractType;
                        loadStubClass = true;
                    } else if (ObjectStreamClassCorbaExt.isAbstractInterface(
                        componentType)) {
                        // RMI Abstract Object reference...
                        callType = kAbstractType;
                    }

                    if (loadStubClass) {
                        try {
                            String codebase = Util.getInstance().getCodebase(
                                componentType);
                            String repID = RepositoryId.createForAnyType(
                                componentType);
                            Class<?> stubType =
                                Utility.loadStubClass(repID, codebase, 
                                    componentType); 
                            actualType = stubType;
                        } catch (ClassNotFoundException e) {
                            narrow = true;
                        }
                    } else {
                        narrow = true;
                    }
                }

                for (i = 0; i < length; i++) {
                    
                    try {
                        OperationTracer.readingIndex( i ) ;

                        switch (callType) {
                        case kRemoteType: 
                            if (!narrow) {
                                array[i] = (Object)in.read_Object(actualType); 
                            } else {
                                array[i] = Utility.readObjectAndNarrow(in, 
                                    actualType);
                            }
                            break;
                        case kAbstractType: 
                            if (!narrow) {
                                array[i] = in.read_abstract_interface(
                                    actualType);
                            } else {
                                array[i] = Utility.readAbstractAndNarrow(in, 
                                    actualType);
                            }
                            break;
                        case kValueType:
                            array[i] = (Object)in.read_value(actualType);
                            break;
                        }
                    } catch(IndirectionException cdrie) {
                        // The CDR stream had never seen the given offset before,
                        // so check the recursion manager (it will throw an
                        // IOException if it doesn't have a reference, either).
                        try {
                            array[i] = bridge.activeRecursionMgr.getObject(
                                cdrie.offset);
                        } catch (IOException ioe) {
                            // Translate to a MARSHAL exception since
                            // ValueHandlers aren't allowed to throw
                            // IOExceptions
                            throw utilWrapper.invalidIndirection( ioe,
                                                               cdrie.offset ) ;
                        }
                    }
                    
                }
                
                return ((java.io.Serializable)((Object)array));
            }
        } finally {
            // We've completed deserializing this object.  Any
            // future indirections will be handled correctly at the
            // CDR level.  The ActiveRecursionManager only deals with
            // objects currently being deserialized.
            bridge.activeRecursionMgr.removeObject(offset);

            if (sequence != null) {
                OperationTracer.endReadArray() ;
            }
        }
    }

    private boolean isArray(String repId){
        return RepositoryId.cache.getId(repId).isSequence();
    }

    private IIOPOutputStream createOutputStream() {
        if (System.getSecurityManager() != null) {
            return AccessController.doPrivileged( 
                new PrivilegedAction<IIOPOutputStream>() {
                    public IIOPOutputStream run() {     
                        try {
                            return new IIOPOutputStream() ;     
                        } catch (IOException exc) {
                            throw utilWrapper.exceptionInCreateIiopOutputStream( 
                                exc ) ;
                        }
                    }
                }
            ); 
        } else {
            try {
                return new IIOPOutputStream() ;     
            } catch (IOException exc) {
                throw utilWrapper.exceptionInCreateIiopOutputStream( exc ) ;
            }
        }
    }

    private IIOPInputStream createInputStream() {
        if (System.getSecurityManager() != null) {
            return AccessController.doPrivileged( 
                new PrivilegedAction<IIOPInputStream>() { 
                    public IIOPInputStream run() {       
                        try {
                            return new IIOPInputStream() ;          
                        } catch (IOException exc) {
                            throw utilWrapper.exceptionInCreateIiopInputStream( 
                                exc ) ;
                        }               
                    }
                }
            );
        } else {
            try {
                return new IIOPInputStream() ;      
            } catch (IOException exc) {
                throw utilWrapper.exceptionInCreateIiopInputStream( exc ) ;
            }
        }
    }

    /**
     * Our JDK 1.3 and JDK 1.3.1 behavior subclasses override this.
     * The correct behavior is for a Java char to map to a CORBA wchar,
     * but our older code mapped it to a CORBA char.
     */
    TCKind getJavaCharTCKind() {
        return TCKind.tk_wchar;
    }
}

