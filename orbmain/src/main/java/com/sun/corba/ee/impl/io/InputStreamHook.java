/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
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

import java.io.IOException;
import java.io.StreamCorruptedException;
import java.io.NotActiveException;
import java.io.ObjectInputStream;

import org.omg.CORBA.portable.ValueInputStream;

import com.sun.corba.ee.spi.orb.ORBVersion;
import com.sun.corba.ee.spi.orb.ORBVersionFactory;
import com.sun.corba.ee.spi.logging.UtilSystemException;
import com.sun.corba.ee.spi.logging.OMGSystemException;

import com.sun.corba.ee.spi.trace.StreamFormatVersion;
import java.util.HashMap;
import java.util.Map;

@StreamFormatVersion
public abstract class InputStreamHook extends ObjectInputStream
{
    // These should be visible in all the nested classes
    static final OMGSystemException omgWrapper =
        OMGSystemException.self ;

    static final UtilSystemException utilWrapper =
        UtilSystemException.self ;


    private class HookGetFields extends ObjectInputStream.GetField {
        private Map<String,Object> fields = null;

        HookGetFields(Map<String,Object> fields){
            this.fields = fields;
        }

        /**
         * Get the ObjectStreamClass that describes the fields in the stream.
         *
         * REVISIT!  This doesn't work since we have our own ObjectStreamClass.
         */
        public java.io.ObjectStreamClass getObjectStreamClass() {
            return null;
        }
                
        /**
         * Return true if the named field is defaulted and has no value
         * in this stream.
         */
        public boolean defaulted(String name)
            throws IOException, IllegalArgumentException  {
            return (!fields.containsKey(name));
        }
                
        /**
         * Get the value of the named boolean field from the persistent field.
         */
        public boolean get(String name, boolean defvalue) 
            throws IOException, IllegalArgumentException {
            if (defaulted(name)) {
                return defvalue;
            } else {
                return ((Boolean) fields.get(name)).booleanValue();
            }
        }
                
        /**
         * Get the value of the named char field from the persistent fields.
         */
        public char get(String name, char defvalue) 
            throws IOException, IllegalArgumentException {
            if (defaulted(name)) {
                return defvalue;
            } else {
                return ((Character) fields.get(name)).charValue();
            }

        }
                
        /**
         * Get the value of the named byte field from the persistent fields.
         */
        public byte get(String name, byte defvalue) 
            throws IOException, IllegalArgumentException {
            if (defaulted(name)) {
                return defvalue;
            } else {
                return ((Byte) fields.get(name)).byteValue();
            }

        }
                
        /**
         * Get the value of the named short field from the persistent fields.
         */
        public short get(String name, short defvalue) 
            throws IOException, IllegalArgumentException {
            if (defaulted(name)) {
                return defvalue;
            } else {
                return ((Short) fields.get(name)).shortValue();
            }

        }
                
        /**
         * Get the value of the named int field from the persistent fields.
         */
        public int get(String name, int defvalue) 
            throws IOException, IllegalArgumentException {
            if (defaulted(name)) {
                return defvalue;
            } else {
                return ((Integer) fields.get(name)).intValue();
            }
        }
                
        /**
         * Get the value of the named long field from the persistent fields.
         */
        public long get(String name, long defvalue)
            throws IOException, IllegalArgumentException {
            if (defaulted(name)) {
                return defvalue;
            } else {
                return ((Long) fields.get(name)).longValue();
            }
        }
                
        /**
         * Get the value of the named float field from the persistent fields.
         */
        public float get(String name, float defvalue) 
            throws IOException, IllegalArgumentException {
            if (defaulted(name)) {
                return defvalue;
            } else {
                return ((Float) fields.get(name)).floatValue();
            }
        }
                
        /**
         * Get the value of the named double field from the persistent field.
         */
        public double get(String name, double defvalue) 
            throws IOException, IllegalArgumentException  {
            if (defaulted(name)) {
                return defvalue;
            } else {
                return ((Double) fields.get(name)).doubleValue();
            }
        }
                
        /**
         * Get the value of the named Object field from the persistent field.
         */
        public Object get(String name, Object defvalue) 
            throws IOException, IllegalArgumentException {
            if (defaulted(name)) {
                return defvalue;
            } else {
                return fields.get(name);
            }

        }
                
        @Override
        public String toString(){
            return fields.toString();
        }
    }

    public InputStreamHook()
        throws IOException {
        super();
    }

    @Override
    @StreamFormatVersion
    public void defaultReadObject()
        throws IOException, ClassNotFoundException, NotActiveException
    {
        readObjectState.beginDefaultReadObject(this);

        defaultReadObjectDelegate();

        readObjectState.endDefaultReadObject(this);
    }

    abstract void defaultReadObjectDelegate();

    abstract void readFields(java.util.Map<String,Object> fieldToValueMap)
        throws java.io.InvalidClassException, java.io.StreamCorruptedException,
               ClassNotFoundException, java.io.IOException;


    // See java.io.ObjectInputStream.GetField
    // Remember that this is equivalent to defaultReadObject
    // in RMI-IIOP
    @Override
    public ObjectInputStream.GetField readFields()
        throws IOException, ClassNotFoundException, NotActiveException {

        Map<String,Object> fieldValueMap = new HashMap<String,Object>();

        // We were treating readFields same as defaultReadObject. It is
        // incorrect if the state is readOptionalData. If this line
        // is uncommented, it will throw a stream corrupted exception.
        // _REVISIT_: The ideal fix would be to add a new state. In 
        // writeObject user may do one of the following
        // 1. Call defaultWriteObject() 
        // 2. Put out optional fields 
        // 3. Call writeFields 
        // We have the state defined for (1) and (2) but not for (3), so
        // we should ideally introduce a new state for 3 and have the
        // beginDefaultReadObject do nothing.
        //readObjectState.beginDefaultReadObject(this);

        readFields(fieldValueMap);

        readObjectState.endDefaultReadObject(this);

        return new HookGetFields(fieldValueMap);
    }

    // The following is a State pattern implementation of what
    // should be done when the sender's Serializable has a
    // writeObject method.  This was especially necessary for
    // RMI-IIOP stream format version 2.  Please see the
    // state diagrams in the docs directory of the workspace.    
    //
    // On the reader's side, the main factors are whether or not
    // we have a readObject method and whether or not the
    // sender wrote default data

    @StreamFormatVersion
    protected void setState(ReadObjectState newState) {
        readObjectState = newState;
    }

    protected abstract byte getStreamFormatVersion();
    abstract org.omg.CORBA_2_3.portable.InputStream getOrbStream();

    // Description of possible actions
    @StreamFormatVersion
    protected static class ReadObjectState {
        private final String name ;

        public ReadObjectState() {
            String className = this.getClass().getName() ;
            int index = className.indexOf( '$' ) ;
            name = className.substring( index + 1 ) ;
        }

        @StreamFormatVersion
        public final void beginUnmarshalCustomValue(InputStreamHook stream, boolean calledDefaultWriteObject, 
            boolean hasReadObject) throws IOException {
            beginUnmarshalCustomValueOverride( stream, calledDefaultWriteObject, hasReadObject ) ;
        }

        @StreamFormatVersion
        public final void endUnmarshalCustomValue(InputStreamHook stream) throws IOException {
            endUnmarshalCustomValueOverride( stream ) ;
        }

        @StreamFormatVersion
        public final void beginDefaultReadObject(InputStreamHook stream) throws IOException {
            beginDefaultReadObjectOverride( stream ) ;
        }

        @StreamFormatVersion
        public final void endDefaultReadObject(InputStreamHook stream) throws IOException {
            endDefaultReadObjectOverride( stream ) ;
        }

        @StreamFormatVersion
        public final void readData(InputStreamHook stream) throws IOException {
            readDataOverride( stream ) ;
        }

        public void beginUnmarshalCustomValueOverride(InputStreamHook stream, 
            boolean calledDefaultWriteObject, boolean hasReadObject) throws IOException {}
        public void endUnmarshalCustomValueOverride(InputStreamHook stream) throws IOException {}
        public void beginDefaultReadObjectOverride(InputStreamHook stream) throws IOException {}
        public void endDefaultReadObjectOverride(InputStreamHook stream) throws IOException {}
        public void readDataOverride(InputStreamHook stream) throws IOException {}

        @Override
        public String toString() {
            return name ;
        }
    }

    protected ReadObjectState readObjectState = DEFAULT_STATE;
    
    protected static final ReadObjectState DEFAULT_STATE = new DefaultState();
    protected static final ReadObjectState IN_READ_OBJECT_OPT_DATA 
        = new InReadObjectOptionalDataState();
    protected static final ReadObjectState IN_READ_OBJECT_NO_MORE_OPT_DATA
        = new InReadObjectNoMoreOptionalDataState();
    protected static final ReadObjectState IN_READ_OBJECT_DEFAULTS_SENT
        = new InReadObjectDefaultsSentState();
    protected static final ReadObjectState NO_READ_OBJECT_DEFAULTS_SENT
        = new NoReadObjectDefaultsSentState();

    protected static final ReadObjectState IN_READ_OBJECT_REMOTE_NOT_CUSTOM_MARSHALED
        = new InReadObjectRemoteDidNotUseWriteObjectState();
    protected static final ReadObjectState IN_READ_OBJECT_PAST_DEFAULTS_REMOTE_NOT_CUSTOM
        = new InReadObjectPastDefaultsRemoteDidNotUseWOState();

    protected static class DefaultState extends ReadObjectState {

        @Override
        public void beginUnmarshalCustomValueOverride(InputStreamHook stream,
                                              boolean calledDefaultWriteObject,
                                              boolean hasReadObject)
            throws IOException {

            if (hasReadObject) {
                if (calledDefaultWriteObject) {
                    stream.setState(IN_READ_OBJECT_DEFAULTS_SENT);
                } else {
                    try {
                        if (stream.getStreamFormatVersion() == 2) {
                            ((ValueInputStream) stream.getOrbStream())
                                .start_value();
                        }
                    } catch( Exception e ) {
                        // This will happen for Big Integer which uses 
                        // writeFields in it's writeObject. We should be past
                        // start_value by now.
                        // NOTE: If we don't log any exception here we should
                        // be fine. If there is an error, it will be caught 
                        // while reading the optional data.
                 
                    }
                    stream.setState(IN_READ_OBJECT_OPT_DATA);
                }
            } else {
                if (calledDefaultWriteObject) {
                    stream.setState(NO_READ_OBJECT_DEFAULTS_SENT);
                } else {
                    throw new StreamCorruptedException("No default data sent");
                }
            }
        }
    }

    // REVISIT.  If a readObject exits here without reading
    // default data, we won't skip it.  This could be done automatically
    // as in line 1492 in IIOPInputStream.
    protected static class InReadObjectRemoteDidNotUseWriteObjectState extends ReadObjectState {

        @Override
        public void beginUnmarshalCustomValueOverride(InputStreamHook stream,
                                              boolean calledDefaultWriteObject,
                                              boolean hasReadObject) 
        {
            throw utilWrapper.badBeginUnmarshalCustomValue() ;
        }

        @Override
        public void endDefaultReadObjectOverride(InputStreamHook stream) {
            stream.setState(IN_READ_OBJECT_PAST_DEFAULTS_REMOTE_NOT_CUSTOM);
        }

        @Override
        public void readDataOverride(InputStreamHook stream) {
            stream.throwOptionalDataIncompatibleException();
        }
    }

    protected static class InReadObjectPastDefaultsRemoteDidNotUseWOState extends ReadObjectState {

        @Override
        public void beginUnmarshalCustomValueOverride(InputStreamHook stream,
                                              boolean calledDefaultWriteObject,
                                              boolean hasReadObject)
        {
            throw utilWrapper.badBeginUnmarshalCustomValue() ;
        }

        @Override
        public void beginDefaultReadObjectOverride(InputStreamHook stream) throws IOException 
        {
            throw Exceptions.self.defaultDataAlreadyRead() ;
        }


        @Override
        public void readDataOverride(InputStreamHook stream) {
            stream.throwOptionalDataIncompatibleException();
        }
    }

    protected void throwOptionalDataIncompatibleException() 
    {
        throw omgWrapper.rmiiiopOptionalDataIncompatible2() ;
    }


    protected static class InReadObjectDefaultsSentState extends ReadObjectState {
        
        @Override
        public void beginUnmarshalCustomValueOverride(InputStreamHook stream,
                                              boolean calledDefaultWriteObject,
                                              boolean hasReadObject) {
            // This should never happen.
            throw utilWrapper.badBeginUnmarshalCustomValue() ;
        }

        @Override
        public void endUnmarshalCustomValueOverride(InputStreamHook stream) {

            // In stream format version 2, we can skip over
            // the optional data this way.  In stream format version 1,
            // we will probably wind up with an error if we're
            // unmarshaling a superclass.
            if (stream.getStreamFormatVersion() == 2) {
                ((ValueInputStream)stream.getOrbStream()).start_value();
                ((ValueInputStream)stream.getOrbStream()).end_value();
            }

            stream.setState(DEFAULT_STATE);
        }

        @Override
        public void endDefaultReadObjectOverride(InputStreamHook stream) throws IOException {

            // Read the fake valuetype header in stream format version 2
            if (stream.getStreamFormatVersion() == 2) {
                ((ValueInputStream) stream.getOrbStream()).start_value();
            }

            stream.setState(IN_READ_OBJECT_OPT_DATA);
        }

        @Override
        public void readDataOverride(InputStreamHook stream) throws IOException {
            org.omg.CORBA.ORB orb = stream.getOrbStream().orb();
            if ((orb == null) ||
                    !(orb instanceof com.sun.corba.ee.spi.orb.ORB)) {
                throw new StreamCorruptedException(
                                     "Default data must be read first");
            }
            ORBVersion clientOrbVersion = 
                ((com.sun.corba.ee.spi.orb.ORB)orb).getORBVersion();

            // Fix Date interop bug. For older versions of the ORB don't do
            // anything for readData(). Before this used to throw 
            // StreamCorruptedException for older versions of the ORB where
            // calledDefaultWriteObject always returns true.
            if ((ORBVersionFactory.getPEORB().compareTo(clientOrbVersion) <= 0) || 
                    (clientOrbVersion.equals(ORBVersionFactory.getFOREIGN()))) {
                throw Exceptions.self.defaultDataMustBeReadFirst() ;
            }
        }
    }

    protected static class InReadObjectOptionalDataState extends ReadObjectState {

        @Override
        public void beginUnmarshalCustomValueOverride(InputStreamHook stream,
                                              boolean calledDefaultWriteObject,
                                              boolean hasReadObject) 
        {
            // This should never happen.
            throw utilWrapper.badBeginUnmarshalCustomValue() ;
        }

        @Override
        public void endUnmarshalCustomValueOverride(InputStreamHook stream) throws IOException 
        {
            if (stream.getStreamFormatVersion() == 2) {
                ((ValueInputStream)stream.getOrbStream()).end_value();
            }
            stream.setState(DEFAULT_STATE);
        }
        
        @Override
        public void beginDefaultReadObjectOverride(InputStreamHook stream) throws IOException 
        {
            throw Exceptions.self.defaultDataNotPresent() ;
        }

        
    }

    protected static class InReadObjectNoMoreOptionalDataState 
        extends InReadObjectOptionalDataState {

        @Override
        public void readDataOverride(InputStreamHook stream) throws IOException {
            stream.throwOptionalDataIncompatibleException();
        }
    }

    protected static class NoReadObjectDefaultsSentState extends ReadObjectState {
        @Override
        public void endUnmarshalCustomValueOverride(InputStreamHook stream) throws IOException {
            // Code should read default fields before calling this

            if (stream.getStreamFormatVersion() == 2) {
                ((ValueInputStream)stream.getOrbStream()).start_value();
                ((ValueInputStream)stream.getOrbStream()).end_value();
            }

            stream.setState(DEFAULT_STATE);
        }
    }
}
