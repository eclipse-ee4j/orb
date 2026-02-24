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

package com.sun.corba.ee.impl.io;

import com.sun.corba.ee.spi.logex.stdcorba.StandardLogger;

import java.io.IOException;
import java.io.InvalidClassException;
import java.io.NotSerializableException;
import java.io.StreamCorruptedException;

import org.glassfish.pfl.basic.logex.Chain;
import org.glassfish.pfl.basic.logex.ExceptionWrapper;
import org.glassfish.pfl.basic.logex.Log;
import org.glassfish.pfl.basic.logex.LogLevel;
import org.glassfish.pfl.basic.logex.Message;
import org.glassfish.pfl.basic.logex.WrapperGenerator;

/** Logging and Exception handling for the io package.
 *
 * @author ken
 */
@ExceptionWrapper( idPrefix="ORBIO" )
public interface Exceptions {
    public Exceptions self = WrapperGenerator.makeWrapper( Exceptions.class,
        StandardLogger.self );

    // Allow 100 exceptions per class
    static final int EXCEPTIONS_PER_CLASS = 100 ;

// IIOPInputStream
    static final int IIS_START = 1 ;

    @Message( "No optional data exception constructor available" )
    @Log( id = IIS_START + 0 )
    ExceptionInInitializerError noOptionalDataExceptionConstructor(
        @Chain Exception exc );

    @Message( "Can't create optional data exception")
    @Log( id = IIS_START + 1 )
    Error cantCreateOptionalDataException(@Chain Exception ex);

    @Message( "readLine method not supported")
    @Log( id = IIS_START + 2 )
    IOException readLineMethodNotSupported();

    @Message( "registerValidation method not supported")
    @Log( id = IIS_START + 3 )
    Error registerValidationNotSupport();

    @Message( "resolveClass method not supported")
    @Log( id = IIS_START + 4 )
    IOException resolveClassNotSupported();

    @Message( "resolveObject method not supported")
    @Log( id = IIS_START + 5 )
    IOException resolveObjectNotSupported();

    @Message( "IllegalAccessException when invoking readObject")
    @Log( id = IIS_START + 6 )
    void illegalAccessInvokingObjectRead(@Chain IllegalAccessException e);

    @Message( "Bad type {0} for primitive field")
    @Log( id = IIS_START + 7 )
    InvalidClassException invalidClassForPrimitive(String name);

    @Message( "Unknown call type {0} while reading object field: "
        + "possible stream corruption")
    @Log( id = IIS_START + 8 )
    StreamCorruptedException unknownCallType(int callType);

    @Message( "Unknown typecode kind {0} while reading object field: "
        + "possible stream corruption")
    @Log( id = IIS_START + 9 )
    StreamCorruptedException unknownTypecodeKind(int value);

    @Message( "Assigning instance of class {0} to field {1}" )
    @Log( id = IIS_START + 10 )
    ClassCastException couldNotAssignObjectToField(
        @Chain IllegalArgumentException exc, String className,
        String fieldName );

    @Message( "Not setting field {0} on class {1}: "
        + "likely that class has evolved")
    @Log( level=LogLevel.FINE, id = IIS_START + 11 )
    void notSettingField( String fieldName, String className );

    @Message( "Stream corrupted" )
    @Log( id = IIS_START + 12 )
    StreamCorruptedException streamCorrupted(Throwable t);

    @Log( id= IIS_START + 13 ) 
    @Message( "Could not unmarshal enum with cls {0}, value {1} using EnumDesc" )
    IOException couldNotUnmarshalEnum( String cls, String value ) ;

// IIOPOutputStream
    int IOS_START = IIS_START + EXCEPTIONS_PER_CLASS ;

    @Message( "method annotateClass not supported" )
    @Log( id = IOS_START + 1 )
    IOException annotateClassNotSupported();

    @Message( "method replaceObject not supported" )
    @Log( id = IOS_START + 2 )
    IOException replaceObjectNotSupported();

    @Message( "serialization of ObjectStreamClass not supported" )
    @Log( id = IOS_START + 3 )
    IOException serializationObjectStreamClassNotSupported();

    @Message( "serialization of ObjectStreamClass not supported" )
    @Log( id = IOS_START + 4 )
    NotSerializableException notSerializable(String name);

    @Message( "Invalid class {0} for writing field" )
    @Log( id = IOS_START + 5 )
    InvalidClassException invalidClassForWrite(String name);

// InputStreamHook
    int ISH_START = IOS_START + EXCEPTIONS_PER_CLASS ;

    @Message( "Default data already read" )
    @Log( id = ISH_START + 1 )
    StreamCorruptedException defaultDataAlreadyRead();

    @Message( "Default data must be read first" )
    @Log( id = ISH_START + 2 )
    StreamCorruptedException defaultDataMustBeReadFirst();

    @Message( "Default data not sent or already read" )
    @Log( id = ISH_START + 3 )
    StreamCorruptedException defaultDataNotPresent();

// ObjectStreamClass
    int OSC_START = ISH_START + EXCEPTIONS_PER_CLASS ;

    @Message( "Default data not sent or already read" )
    @Log( level=LogLevel.FINE, id = OSC_START + 1 )
    void couldNotAccessSerialPersistentFields( @Chain Exception e,
        String name);

    @Message( "Field type mismatch in Class {0} for field (name {1}, type {2})"
        + "and reflected field (name {3}, type {4})")
    @Log( level=LogLevel.FINE, id = OSC_START + 2 )
    void fieldTypeMismatch( String cname, String fldName,
        Class<?> fldType, String rfldName, Class<?> rfldType ) ;

    @Message( "Could not find field {1} in class {0}" )
    @Log( level=LogLevel.FINE, id = OSC_START + 3 )
    void noSuchField( @Chain NoSuchFieldException e, String className,
        String fieldName );

    @Message( "Could not hasStaticInitializer method in class {0}" )
    @Log( id = OSC_START + 4 )
    InternalError cantFindHasStaticInitializer(String cname);

    @Message( "Could not invoke hasStaticInitializer method" )
    @Log( id = OSC_START + 5 )
    InternalError errorInvokingHasStaticInitializer(@Chain Exception ex);

// OutputStreamHook
    int OSH_START = OSC_START + EXCEPTIONS_PER_CLASS ;

    @Message( "Call writeObject twice" )
    @Log( id = OSH_START + 1 )
    IOException calledWriteObjectTwice();

    @Message( "Call defaultWriteObject or writeFields twice" )
    @Log( id = OSH_START + 2 )
    IOException calledDefaultWriteObjectTwice();

    @Message( "Cannot call defaultWriteObject or writeFields after "
        + "writing custom data")
    @Log( id = OSH_START + 3 )
    IOException defaultWriteObjectAfterCustomData();

// ValueHandleImpl
    int VHI_START = OSH_START + EXCEPTIONS_PER_CLASS ;

    @Message( "Invalid primitive type {0}")
    @Log( id = VHI_START + 1 )
    Error invalidPrimitiveType(String name);

    @Message( "Invalid primitive component type {0}")
    @Log( id = VHI_START + 2 )
    Error invalidPrimitiveComponentType(String name);
}
