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

package com.sun.corba.ee.impl.io;

import com.sun.corba.ee.impl.javax.rmi.CORBA.Util;
import com.sun.corba.ee.impl.misc.ClassInfoCache;
import com.sun.corba.ee.impl.util.RepositoryId;
import com.sun.corba.ee.impl.util.Utility;
import com.sun.corba.ee.spi.trace.ValueHandlerWrite;
import org.glassfish.pfl.basic.reflection.Bridge;
import org.glassfish.pfl.tf.spi.annotation.InfoMethod;
import org.omg.CORBA.portable.OutputStream;

import java.io.Externalizable;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.NotActiveException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Stack;


/**
 * IIOPOutputStream is ...
 *
 * @author  Stephen Lewallen
 * @version 0.01, 4/6/98
 * @since   JDK1.1.6
 */

@ValueHandlerWrite
public class IIOPOutputStream
    extends com.sun.corba.ee.impl.io.OutputStreamHook
{
    private static Bridge bridge = 
        AccessController.doPrivileged(
            new PrivilegedAction<Bridge>() {
                public Bridge run() {
                    return Bridge.get() ;
                }
            } 
        ) ;

    private org.omg.CORBA_2_3.portable.OutputStream orbStream;

    private Object currentObject = null;

    private ObjectStreamClass currentClassDesc = null;

    private int recursionDepth = 0;

    private int simpleWriteDepth = 0;

    private IOException abortIOException = null;

    private Stack<ObjectStreamClass> classDescStack = 
        new Stack<ObjectStreamClass>();

    public IIOPOutputStream()
        throws java.io.IOException
   {
        super();
    }

    // If using RMI-IIOP stream format version 2, this tells
    // the ORB stream (which must be a ValueOutputStream) to
    // begin a new valuetype to contain the optional data
    // of the writeObject method.
    @ValueHandlerWrite
    protected void beginOptionalCustomData() {
        if (streamFormatVersion == 2) {

            org.omg.CORBA.portable.ValueOutputStream vout
                = (org.omg.CORBA.portable.ValueOutputStream)orbStream;

            vout.start_value(currentClassDesc.getRMIIIOPOptionalDataRepId());
        }
    }

    final void setOrbStream(org.omg.CORBA_2_3.portable.OutputStream os) {
        orbStream = os;
    }

    final org.omg.CORBA_2_3.portable.OutputStream getOrbStream() {
        return orbStream;
    }

    @InfoMethod
    private void recursionDepthInfo( int rd ) {}

    @ValueHandlerWrite
    final void increaseRecursionDepth(){
        recursionDepth++;
        recursionDepthInfo( recursionDepth ) ;
    }

    @ValueHandlerWrite
    final int decreaseRecursionDepth(){
        --recursionDepth;
        recursionDepthInfo(recursionDepth);
        return recursionDepth ;
    }

    @ValueHandlerWrite
    private void writeFormatVersion() {
        orbStream.write_octet(streamFormatVersion);
    }

    /**
     * Override the actions of the final method "writeObject()"
     * in ObjectOutputStream.
     * @since     JDK1.1.6
     */
    @ValueHandlerWrite
    @Override
    public final void writeObjectOverride(Object obj)
        throws IOException {

        writeObjectState.writeData(this);

        Util.getInstance().writeAbstractObject((OutputStream)orbStream, obj);
    }

    /**
     * Override the actions of the final method "writeObject()"
     * in ObjectOutputStream.
     * @since     JDK1.1.6
     */
    @ValueHandlerWrite
    public final void simpleWriteObject(Object obj, byte formatVersion) {
        byte oldStreamFormatVersion = streamFormatVersion;

        streamFormatVersion = formatVersion;

        Object prevObject = currentObject;
        ObjectStreamClass prevClassDesc = currentClassDesc;
        simpleWriteDepth++;

        try {
            // if (!checkSpecialClasses(obj) && !checkSubstitutableSpecialClasses(obj))
            outputObject(obj);

        } catch (IOException ee) {
            if (abortIOException == null) {
                abortIOException = ee;
            }
        } finally {
            /* Restore state of previous call incase this is a nested call */
            streamFormatVersion = oldStreamFormatVersion;
            simpleWriteDepth--;
            currentObject = prevObject;
            currentClassDesc = prevClassDesc;
        }

        /* If the recursion depth is 0, test for and clear the pending exception.
         * If there is a pending exception throw it.
         */
        IOException pending = abortIOException;
        if (simpleWriteDepth == 0) {
            abortIOException = null;
        }

        if (pending != null) {
            bridge.throwException( pending ) ;
        }
    }

    // Required by the superclass.
    ObjectStreamField[] getFieldsNoCopy() {
        return currentClassDesc.getFieldsNoCopy();
    }

    /**
     * Override the actions of the final method "defaultWriteObject()"
     * in ObjectOutputStream.
     * @since     JDK1.1.6
     */
    @ValueHandlerWrite
    public final void defaultWriteObjectDelegate()
    /* throws IOException */
    {
        try {
            if (currentObject == null || currentClassDesc == null) {
                throw new NotActiveException("defaultWriteObjectDelegate");
            }

            ObjectStreamField[] fields =
                currentClassDesc.getFieldsNoCopy();
            if (fields.length > 0) {
                outputClassFields(currentObject, currentClassDesc.forClass(),
                                  fields);
            }
        } catch(IOException ioe) {
            bridge.throwException(ioe);
        }
    }

    /**
     * Override the actions of the final method "enableReplaceObject()"
     * in ObjectOutputStream.
     * @since     JDK1.1.6
     */
    public final boolean enableReplaceObjectDelegate(boolean enable)
    /* throws SecurityException */
    {
        return false;
                
    }


    @Override
    protected final void annotateClass(Class<?> cl) throws IOException{
        throw Exceptions.self.annotateClassNotSupported() ;
    }

    @Override
    public final void close() throws IOException{
        // no op
    }

    @Override
    protected final void drain() throws IOException{
        // no op
    }

    @ValueHandlerWrite
    @Override
    public final void flush() throws IOException{
        try{
            orbStream.flush();
        } catch(Error e) {
            throw new IOException(e) ;
        }
    }

    @ValueHandlerWrite
    @Override
    protected final Object replaceObject(Object obj) throws IOException{
        throw Exceptions.self.replaceObjectNotSupported() ;
    }

    /**
     * Reset will disregard the state of any objects already written
     * to the stream.  The state is reset to be the same as a new
     * ObjectOutputStream.  The current point in the stream is marked
     * as reset so the corresponding ObjectInputStream will be reset
     * at the same point.  Objects previously written to the stream
     * will not be refered to as already being in the stream.  They
     * will be written to the stream again.
     * @since     JDK1.1
     */
    @ValueHandlerWrite
    @Override
    public final void reset() throws IOException{
        try{
            //orbStream.reset();

            if (currentObject != null || currentClassDesc != null) {
                throw new IOException("Illegal call to reset");
            }

            abortIOException = null;

            if (classDescStack == null) {
                classDescStack =
                    new Stack<ObjectStreamClass>();
            } else {
                classDescStack.setSize(0);
            }

        } catch(Error e) {
            throw new IOException(e) ;
        }
    }

    @ValueHandlerWrite
    @Override
    public final void write(byte b[]) throws IOException{
        try{
            writeObjectState.writeData(this);

            orbStream.write_octet_array(b, 0, b.length);
        } catch(Error e) {
            throw new IOException(e) ;
        }
    }

    @ValueHandlerWrite
    @Override
    public final void write(byte b[], int off, int len) throws IOException{
        try{
            writeObjectState.writeData(this);

            orbStream.write_octet_array(b, off, len);
        } catch(Error e) {
            throw new IOException(e) ;
        }
    }

    @ValueHandlerWrite
    @Override
    public final void write(int data) throws IOException{
        try{
            writeObjectState.writeData(this);

            orbStream.write_octet((byte)(data & 0xFF));
        } catch(Error e) {
            throw new IOException(e) ;
        }
    }

    @ValueHandlerWrite
    @Override
    public final void writeBoolean(boolean data) throws IOException{
        try{
            writeObjectState.writeData(this);

            orbStream.write_boolean(data);
        } catch(Error e) {
            throw new IOException(e) ;
        }
    }

    @ValueHandlerWrite
    @Override
    public final void writeByte(int data) throws IOException{
        try{
            writeObjectState.writeData(this);

            orbStream.write_octet((byte)data);
        } catch(Error e) {
            throw new IOException(e) ;
        }
    }

    @ValueHandlerWrite
    @Override
    public final void writeBytes(String data) throws IOException{
        try{
            writeObjectState.writeData(this);

            byte buf[] = data.getBytes();
            orbStream.write_octet_array(buf, 0, buf.length);
        } catch(Error e) {
            throw new IOException(e) ;
        }
    }

    @ValueHandlerWrite
    @Override
    public final void writeChar(int data) throws IOException{
        try{
            writeObjectState.writeData(this);

            orbStream.write_wchar((char)data);
        } catch(Error e) {
            throw new IOException(e) ;
        }
    }

    @ValueHandlerWrite
    @Override
    public final void writeChars(String data) throws IOException{
        try{
            writeObjectState.writeData(this);

            char buf[] = data.toCharArray();
            orbStream.write_wchar_array(buf, 0, buf.length);
        } catch(Error e) {
            throw new IOException(e) ;
        }
    }

    @ValueHandlerWrite
    @Override
    public final void writeDouble(double data) throws IOException{
        try{
            writeObjectState.writeData(this);

            orbStream.write_double(data);
        } catch(Error e) {
            throw new IOException(e) ;
        }
    }

    @ValueHandlerWrite
    @Override
    public final void writeFloat(float data) throws IOException{
        try{
            writeObjectState.writeData(this);

            orbStream.write_float(data);
        } catch(Error e) {
            throw new IOException(e) ;
        }
    }

    @ValueHandlerWrite
    @Override
    public final void writeInt(int data) throws IOException{
        try{
            writeObjectState.writeData(this);

            orbStream.write_long(data);
        } catch(Error e) {
            throw new IOException(e) ;
        }
    }

    @ValueHandlerWrite
    @Override
    public final void writeLong(long data) throws IOException{
        try{
            writeObjectState.writeData(this);

            orbStream.write_longlong(data);
        } catch(Error e) {
            throw new IOException(e) ;
        }
    }

    @ValueHandlerWrite
    @Override
    public final void writeShort(int data) throws IOException{
        try{
            writeObjectState.writeData(this);

            orbStream.write_short((short)data);
        } catch(Error e) {
            throw new IOException(e) ;
        }
    }

    @Override
    protected final void writeStreamHeader() throws IOException{
        // no op
    }

    /**
     * Helper method for correcting the Kestrel bug 4367783 (dealing
     * with larger than 8-bit chars).  The old behavior was preserved
     * in orbutil.IIOPInputStream_1_3 in order to interoperate with
     * our legacy ORBs.
     */
    protected void internalWriteUTF(org.omg.CORBA.portable.OutputStream stream,
                                    String data) 
    {
        stream.write_wstring(data);
    }

    @ValueHandlerWrite
    @Override
    public final void writeUTF(String data) throws IOException{
        try{
            writeObjectState.writeData(this);

            internalWriteUTF(orbStream, data);
        } catch(Error e) {
            throw new IOException(e) ;
        }
    }

    // INTERNAL UTILITY METHODS
    /*
     * Check for special cases of serializing objects.
     * These objects are not subject to replacement.
     */
    private boolean checkSpecialClasses(Object obj) throws IOException {

        /*
         * If this is a class, don't allow substitution
         */
        //if (obj instanceof Class) {
        //    throw new IOException("Serialization of Class not supported");
        //}

        if (obj instanceof ObjectStreamClass) {
            throw Exceptions.self.serializationObjectStreamClassNotSupported() ;
        }

        return false;
    }

    /*
     * Check for special cases of substitutable serializing objects.
     * These classes are replaceable.
     */
    private boolean checkSubstitutableSpecialClasses(Object obj)
        throws IOException
    {
        if (obj instanceof String) {
            orbStream.write_value((java.io.Serializable)obj);
            return true;
        }

        return false;
    }

    /*
     * Write out the object
     */
    @ValueHandlerWrite
    private void outputObject(final Object obj) throws IOException{
        currentObject = obj;
        Class<?> currclass = obj.getClass();

        /* Get the Class descriptor for this class,
         * Throw a NotSerializableException if there is none.
         */
        currentClassDesc = ObjectStreamClass.lookup(currclass);
        if (currentClassDesc == null) {
            throw Exceptions.self.notSerializable( currclass.getName() ) ;
        }

        /* If the object is externalizable,
         * call writeExternal.
         * else do Serializable processing.
         */
        if (currentClassDesc.isExternalizable()) {
            // Write format version
            writeFormatVersion() ;

            // KMC issue 5161: need to save state for Externalizable also!
            // Obviously an Externalizable may also call writeObject, which
            // calls writeObjectOverride, which sends the writeData input to
            // the state machine.  So we need a new state machine here!
            WriteObjectState oldState = writeObjectState ;
            setState( NOT_IN_WRITE_OBJECT ) ;

            try {
                Externalizable ext = (Externalizable)obj;
                ext.writeExternal(this);
            } finally {
                setState(oldState) ;
            }
        } else {
            /* The object's classes should be processed from supertype to
             * subtype.  Push all the clases of the current object onto a stack.
             * Remember the stack pointer where this set of classes is being
             * pushed.
             */
            if (currentClassDesc.forClass().getName().equals("java.lang.String")) {
                this.writeUTF((String)obj);
                return;
            }
            int stackMark = classDescStack.size();
            try {
                ObjectStreamClass next;
                while ((next = currentClassDesc.getSuperclass()) != null) {
                    classDescStack.push(currentClassDesc);
                    currentClassDesc = next;
                }

                do {
                    WriteObjectState oldState = writeObjectState;

                    try {
                        setState(NOT_IN_WRITE_OBJECT);

                        if (currentClassDesc.hasWriteObject()) {
                            invokeObjectWriter(currentClassDesc, obj );
                        } else {
                            defaultWriteObjectDelegate();
                        }
                    } finally {
                        setState(oldState);
                    }
                } while (classDescStack.size() > stackMark &&
                    (currentClassDesc = classDescStack.pop()) != null);
            } finally {
                classDescStack.setSize(stackMark);
            }
        }
    }

    /*
     * Invoke writer.
     * _REVISIT_ invokeObjectWriter and invokeObjectReader behave inconsistently with each other since
     * the reader returns a boolean...fix later
     */
    @ValueHandlerWrite
    private void invokeObjectWriter(ObjectStreamClass osc, Object obj)
        throws IOException {

        Class<?> c = osc.forClass() ;

        try {
            // Write format version
            writeFormatVersion() ;

            writeObjectState.enterWriteObject(this);

            try {
                osc.getWriteObjectMethod().invoke( obj, this ) ;
            } finally {
                writeObjectState.exitWriteObject(this);
            }
        } catch (Throwable t) {
            if (t instanceof IOException) {
                throw (IOException) t;
            } else if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else if (t instanceof Error) {
                throw (Error) t;
            } else {
                throw new Error("invokeObjectWriter internal error", t);
            }
        }
    }

    // This is needed for the OutputStreamHook interface.
    @ValueHandlerWrite
    void writeField(ObjectStreamField field, Object value) throws IOException {
        switch (field.getTypeCode()) {
            case 'B':
                if (value == null) {
                    orbStream.write_octet((byte) 0);
                } else {
                    orbStream.write_octet(((Byte) value).byteValue());
                }
                break;
            case 'C':
                if (value == null) {
                    orbStream.write_wchar((char) 0);
                } else {
                    orbStream.write_wchar(((Character) value).charValue());
                }
                break;
            case 'F':
                if (value == null) {
                    orbStream.write_float((float) 0);
                } else {
                    orbStream.write_float(((Float) value).floatValue());
                }
                break;
            case 'D':
                if (value == null) {
                    orbStream.write_double((double) 0);
                } else {
                    orbStream.write_double(((Double) value).doubleValue());
                }
                break;
            case 'I':
                if (value == null) {
                    orbStream.write_long(0);
                } else {
                    orbStream.write_long(((Integer) value).intValue());
                }
                break;
            case 'J':
                if (value == null) {
                    orbStream.write_longlong((long) 0);
                } else {
                    orbStream.write_longlong(((Long) value).longValue());
                }
                break;
            case 'S':
                if (value == null) {
                    orbStream.write_short((short) 0);
                } else {
                    orbStream.write_short(((Short) value).shortValue());
                }
                break;
            case 'Z':
                if (value == null) {
                    orbStream.write_boolean(false);
                } else {
                    orbStream.write_boolean(((Boolean) value).booleanValue());
                }
                break;
            case '[':
            case 'L':
                // What to do if it's null?
                writeObjectField(field, value);
                break;
            default:
                throw Exceptions.self.invalidClassForWrite(
                    currentClassDesc.getName());
            }
    }

    @ValueHandlerWrite
    private void writeObjectField(ObjectStreamField field,
                                  Object objectValue) throws IOException {

        if (ObjectStreamClassCorbaExt.isAny(field.getTypeString())) {
            Util.getInstance().writeAny(orbStream, objectValue);
        }
        else {
            Class<?> type = field.getType();
            int callType = ValueHandlerImpl.kValueType;
            ClassInfoCache.ClassInfo cinfo = field.getClassInfo() ;

            if (cinfo.isInterface()) { 
                String className = type.getName();
                
                if (cinfo.isARemote(type)) {
                    // RMI Object reference...
                    callType = ValueHandlerImpl.kRemoteType;
                } else if (cinfo.isACORBAObject(type)) {
                    // IDL Object reference...
                    callType = ValueHandlerImpl.kRemoteType;
                } else if (RepositoryId.isAbstractBase(type)) {
                    // IDL Abstract Object reference...
                    callType = ValueHandlerImpl.kAbstractType;
                } else if (ObjectStreamClassCorbaExt.isAbstractInterface(type)) {
                    callType = ValueHandlerImpl.kAbstractType;
                }
            }
                                        
            switch (callType) {
            case ValueHandlerImpl.kRemoteType: 
                Util.getInstance().writeRemoteObject(orbStream, objectValue);
                break;
            case ValueHandlerImpl.kAbstractType: 
                Util.getInstance().writeAbstractObject(orbStream, objectValue);
                break;
            case ValueHandlerImpl.kValueType:
                try{
                    orbStream.write_value((java.io.Serializable)objectValue, 
                        type);
                } catch(ClassCastException cce){
                    if (objectValue instanceof java.io.Serializable) {
                        throw cce;
                    } else {
                        Utility.throwNotSerializableForCorba(objectValue.getClass().
                            getName());
                    }
                }
            }
        }
    }

    /* Write the fields of the specified class by invoking the appropriate
     * write* method on this class.
     */
    @ValueHandlerWrite
    private void outputClassFields(Object o, Class cl,
                                   ObjectStreamField[] fields)
        throws IOException, InvalidClassException {

        // replace this all with
        // for (int i = 0; i < fields.length; i++) {
        //     fields[i].write( o, orbStream ) ;
        // Should we just put this into ObjectStreamClass?
        // Could also unroll and codegen this.

        for (int i = 0; i < fields.length; i++) {
            ObjectStreamField field = fields[i] ;
            final long offset = field.getFieldID() ;
            if (offset == Bridge.INVALID_FIELD_OFFSET) {
                throw new InvalidClassException(cl.getName(),
                    "Nonexistent field " + fields[i].getName());
            }
            switch (field.getTypeCode()) {
                case 'B':
                    byte byteValue = bridge.getByte( o, offset ) ;
                    orbStream.write_octet(byteValue);
                    break;
                case 'C':
                    char charValue = bridge.getChar( o, offset ) ;
                    orbStream.write_wchar(charValue);
                    break;
                case 'F':
                    float floatValue = bridge.getFloat( o, offset ) ;
                    orbStream.write_float(floatValue);
                    break;
                case 'D' :
                    double doubleValue = bridge.getDouble( o, offset ) ;
                    orbStream.write_double(doubleValue);
                    break;
                case 'I':
                    int intValue = bridge.getInt( o, offset ) ;
                    orbStream.write_long(intValue);
                    break;
                case 'J':
                    long longValue = bridge.getLong( o, offset ) ;
                    orbStream.write_longlong(longValue);
                    break;
                case 'S':
                    short shortValue = bridge.getShort( o, offset ) ;
                    orbStream.write_short(shortValue);
                    break;
                case 'Z':
                    boolean booleanValue = bridge.getBoolean( o, offset ) ;
                    orbStream.write_boolean(booleanValue);
                    break;
                case '[':
                case 'L':
                    Object objectValue = bridge.getObject( o, offset ) ;
                    writeObjectField(fields[i], objectValue);
                    break;
                default:
                    throw Exceptions.self.invalidClassForWrite(
                        cl.getName() ) ;
            }
        }
    }
}

