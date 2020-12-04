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

package com.sun.corba.ee.spi.logging ;

import com.sun.corba.ee.impl.encoding.OSFCodeSetRegistry;

import com.sun.corba.ee.impl.ior.iiop.JavaSerializationComponent;

import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;

import org.glassfish.pfl.basic.logex.Chain;
import org.glassfish.pfl.basic.logex.ExceptionWrapper;
import org.glassfish.pfl.basic.logex.Log;
import org.glassfish.pfl.basic.logex.LogLevel;
import org.glassfish.pfl.basic.logex.Message;
import org.glassfish.pfl.basic.logex.WrapperGenerator;

import com.sun.corba.ee.spi.logex.corba.CS;
import com.sun.corba.ee.spi.logex.corba.CSValue;
import com.sun.corba.ee.spi.logex.corba.ORBException ;
import com.sun.corba.ee.spi.logex.corba.CorbaExtension ;

import com.sun.corba.ee.spi.transport.Connection;
import com.sun.corba.ee.spi.transport.EventHandler;

import java.io.IOException;
import java.io.Serializable;

import java.net.MalformedURLException;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.MalformedInputException;

import java.rmi.RemoteException;

import java.util.List;
import java.util.NoSuchElementException;

import org.omg.CORBA.BAD_INV_ORDER;
import org.omg.CORBA.BAD_OPERATION;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.BAD_TYPECODE;
import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.DATA_CONVERSION;
import org.omg.CORBA.INITIALIZE;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.INV_OBJREF;
import org.omg.CORBA.MARSHAL;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CORBA.OBJ_ADAPTER;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CORBA.TIMEOUT;
import org.omg.CORBA.TRANSIENT;
import org.omg.CORBA.TypeCodePackage.BadKind;
import org.omg.CORBA.Bounds;
import org.omg.CORBA.UNKNOWN;

import org.omg.CORBA.portable.RemarshalException;

@ExceptionWrapper( idPrefix="IOP" )
@ORBException( omgException=false, group=CorbaExtension.ORBUtilGroup )
public interface ORBUtilSystemException {
    ORBUtilSystemException self = WrapperGenerator.makeWrapper(
        ORBUtilSystemException.class,  CorbaExtension.self ) ;
    
    @Log( level=LogLevel.WARNING, id=1 )
    @Message( "Adapter ID not available" )
    BAD_OPERATION adapterIdNotAvailable(  ) ;
    
    @Log( level=LogLevel.WARNING, id=2 )
    @Message( "Server ID not available" )
    BAD_OPERATION serverIdNotAvailable(  ) ;
    
    @Log( level=LogLevel.WARNING, id=3 )
    @Message( "ORB ID not available" )
    BAD_OPERATION orbIdNotAvailable(  ) ;
    
    @Log( level=LogLevel.WARNING, id=4 )
    @Message( "Object adapter ID not available" )
    BAD_OPERATION objectAdapterIdNotAvailable(  ) ;
    
    @Log( level=LogLevel.WARNING, id=5 )
    @Message( "Error connecting servant" )
    BAD_OPERATION connectingServant( @Chain RemoteException exc  ) ;
    
    @Log( level=LogLevel.FINE, id=6 )
    @Message( "Expected typecode kind {0} but got typecode kind {1}" )
    BAD_OPERATION extractWrongType( String arg0, String arg1 ) ;
    
    @Log( level=LogLevel.WARNING, id=7 )
    @Message( "Expected typecode kind to be one of {0} but got "
        + "typecode kind {1}" )
    BAD_OPERATION extractWrongTypeList( List<String> opList, String tcName ) ;
    
    @Log( level=LogLevel.WARNING, id=8 )
    @Message( "String length of {0} exceeds bounded string length of {1}" )
    BAD_OPERATION badStringBounds( int len, int boundedLen ) ;
    
    @Log( level=LogLevel.WARNING, id=10 )
    @Message( "Tried to insert an object of an incompatible type into an Any "
        + "for an object reference" )
    BAD_OPERATION insertObjectIncompatible(  ) ;
    
    @Log( level=LogLevel.WARNING, id=11 )
    @Message( "insert_Object call failed on an Any" )
    BAD_OPERATION insertObjectFailed( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=12 )
    @Message( "extract_Object call failed on an Any" )
    BAD_OPERATION extractObjectIncompatible(  ) ;
    
    @Log( level=LogLevel.WARNING, id=13 )
    @Message( "Fixed type does not match typecode" )
    BAD_OPERATION fixedNotMatch(  ) ;
    
    @Log( level=LogLevel.WARNING, id=14 )
    @Message( "Tried to insert Fixed type for non-Fixed typecode" )
    BAD_OPERATION fixedBadTypecode( @Chain BadKind bk ) ;
    
    @Log( level=LogLevel.WARNING, id=23 )
    @Message( "set_exception(Any) called with null args for DSI ServerRequest" )
    BAD_OPERATION setExceptionCalledNullArgs(  ) ;
    
    @Log( level=LogLevel.WARNING, id=24 )
    @Message( "set_exception(Any) called with a bad (non-exception) type" )
    BAD_OPERATION setExceptionCalledBadType(  ) ;
    
    @Log( level=LogLevel.WARNING, id=25 )
    @Message( "ctx() called out of order for DSI ServerRequest" )
    BAD_OPERATION contextCalledOutOfOrder(  ) ;
    
    @Log( level=LogLevel.WARNING, id=26 )
    @Message( "ORB configurator class {0} could not be instantiated" )
    BAD_OPERATION badOrbConfigurator( @Chain Exception exc, String name ) ;

    @Log( level=LogLevel.WARNING, id=26 )
    @Message( "ORB configurator class {0} could not be instantiated" )
    BAD_OPERATION badOrbConfigurator( String name ) ;
    
    @Log( level=LogLevel.WARNING, id=27 )
    @Message( "Error in running ORB configurator" )
    BAD_OPERATION orbConfiguratorError( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=28 )
    @Message( "This ORB instance has been destroyed, so no operations can be "
        + "performed on it" )
    BAD_OPERATION orbDestroyed(  ) ;
    
    @Log( level=LogLevel.WARNING, id=29 )
    @Message( "Negative bound for string TypeCode is illegal" )
    BAD_OPERATION negativeBounds(  ) ;
    
    @Log( level=LogLevel.WARNING, id=30 )
    @Message( "Called typecode extract on an uninitialized typecode" )
    BAD_OPERATION extractNotInitialized(  ) ;
    
    @Log( level=LogLevel.WARNING, id=31 )
    @Message( "extract_Object failed on an uninitialized Any" )
    BAD_OPERATION extractObjectFailed( @Chain Exception exc ) ;

    int METHOD_NOT_FOUND_IN_TIE = CorbaExtension.self.getMinorCode(
        ORBUtilSystemException.class, "methodNotFoundInTie" ) ;

    @Log( level=LogLevel.FINE, id=32 )
    @Message( "Could not find method named {0} in class {1} in reflective Tie" )
    BAD_OPERATION methodNotFoundInTie( String arg0, String arg1 ) ;
    
    @Log( level=LogLevel.FINE, id=33 )
    @Message( "ClassNotFoundException while attempting to load preferred "
        + "stub named {0}" )
    @CS( CSValue.MAYBE )
    BAD_OPERATION classNotFound1( @Chain Exception exc, String arg0 ) ;
    
    @Log( level=LogLevel.FINE, id=34 )
    @Message( "ClassNotFoundException while attempting to load alternate "
        + "stub named {0}" )
    @CS( CSValue.MAYBE )
    BAD_OPERATION classNotFound2( @Chain Exception exc, String arg0 ) ;
    
    @Log( level=LogLevel.FINE, id=35 )
    @Message( "ClassNotFoundException while attempting to load interface {0}" )
    @CS( CSValue.MAYBE )
    BAD_OPERATION classNotFound3( @Chain Exception exc, String arg0 ) ;
    
    @Log( level=LogLevel.WARNING, id=36 )
    @Message( "POA ServantNotActive exception while trying get an "
        + "org.omg.CORBA.Portable.Delegate for an org.omg.PortableServer.Servant" )
    BAD_OPERATION getDelegateServantNotActive( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=37 )
    @Message( "POA WrongPolicy exception while trying get an "
        + "org.omg.CORBA.Portable.Delegate for an org.omg.PortableServer.Servant" )
    BAD_OPERATION getDelegateWrongPolicy( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.FINE, id=38 )
    @Message( "Call to StubAdapter.setDelegate did not pass a stub" )
    BAD_OPERATION setDelegateRequiresStub(  ) ;
    
    @Log( level=LogLevel.WARNING, id=39 )
    @Message( "Call to StubAdapter.getDelegate did not pass a stub" )
    BAD_OPERATION getDelegateRequiresStub(  ) ;
    
    @Log( level=LogLevel.WARNING, id=40 )
    @Message( "Call to StubAdapter.getTypeIds did not pass a stub" )
    BAD_OPERATION getTypeIdsRequiresStub(  ) ;
    
    @Log( level=LogLevel.WARNING, id=41 )
    @Message( "Call to StubAdapter.getORB did not pass a stub" )
    BAD_OPERATION getOrbRequiresStub(  ) ;
    
    @Log( level=LogLevel.WARNING, id=42 )
    @Message( "Call to StubAdapter.connect did not pass a stub" )
    BAD_OPERATION connectRequiresStub(  ) ;
    
    @Log( level=LogLevel.WARNING, id=43 )
    @Message( "Call to StubAdapter.isLocal did not pass a stub" )
    BAD_OPERATION isLocalRequiresStub(  ) ;
    
    @Log( level=LogLevel.WARNING, id=44 )
    @Message( "Call to StubAdapter.request did not pass a stub" )
    BAD_OPERATION requestRequiresStub(  ) ;
    
    @Log( level=LogLevel.WARNING, id=45 )
    @Message( "Call to StubAdapter.activateTie did not pass a valid Tie" )
    BAD_OPERATION badActivateTieCall(  ) ;
    
    @Log( level=LogLevel.WARNING, id=46 )
    @Message( "Bad operation from _invoke: {0}" )
    BAD_OPERATION badOperationFromInvoke( @Chain Exception exc, String arg0 ) ;
    
    String couldNotAccessStubDelegate = "Could not access StubDelegateImpl" ;

    @Log( level=LogLevel.WARNING, id=47 )
    @Message( couldNotAccessStubDelegate )
    BAD_OPERATION couldNotAccessStubDelegate( @Chain Exception exc ) ;

    @Log( level=LogLevel.WARNING, id=47 )
    @Message( couldNotAccessStubDelegate )
    BAD_OPERATION couldNotAccessStubDelegate( ) ;
    
    @Log( level=LogLevel.WARNING, id=48 )
    @Message( "Could not load interface {0} for creating stub" )
    BAD_OPERATION couldNotLoadInterface( @Chain Exception exc, String arg0 ) ;
    
    @Log( level=LogLevel.WARNING, id=49 )
    @Message( "Could not activate POA from foreign ORB due to "
        + "AdapterInactive exception in StubAdapter" )
    BAD_OPERATION adapterInactiveInActivateServant( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=50 )
    @Message( "Could not instantiate stub class {0} for dynamic RMI-IIOP" )
    BAD_OPERATION couldNotInstantiateStubClass( @Chain Exception exc,
        String arg0 ) ;
    
    @Log( level=LogLevel.WARNING, id=51 )
    @Message( "String expected in OperationFactory.getString()" )
    BAD_OPERATION stringExpectedInOperation(  ) ;
    
    @Log( level=LogLevel.WARNING, id=52 )
    @Message( "Object[] expected" )
    BAD_OPERATION objectArrayExpected(  ) ;
    
    @Log( level=LogLevel.WARNING, id=53 )
    @Message( "Pair<String,String> expected" )
    BAD_OPERATION pairStringStringExpected(  ) ;
    
    @Log( level=LogLevel.WARNING, id=54 )
    @Message( "Error while attempting to load class {0}" )
    BAD_OPERATION classActionException( @Chain Exception exc, String arg0 ) ;
    
    @Log( level=LogLevel.WARNING, id=55 )
    @Message( "Bad URL {0} in URLAction" )
    BAD_OPERATION badUrlInAction( @Chain Exception exc, String arg0 ) ;
    
    @Log( level=LogLevel.WARNING, id=56 )
    @Message( "Property value {0} is not in the range {1} to {2}" )
    BAD_OPERATION valueNotInRange( int arg0, int arg1, int arg2 ) ;
    
    @Log( level=LogLevel.WARNING, id=57 )
    @Message( "Number of token ({0}) and number of actions ({1}) don't match" )
    BAD_OPERATION numTokensActionsDontMatch( int arg0, int arg1 ) ;
    
    @Log( level=LogLevel.WARNING, id=58 )
    @Message( "Could not find constructor <init>(String) in class {0}" )
    BAD_OPERATION exceptionInConvertActionConstructor( @Chain Exception exc,
        String arg0 ) ;
    
    @Log( level=LogLevel.WARNING, id=59 )
    @Message( "Exception in ConvertAction operation" )
    BAD_OPERATION exceptionInConvertAction( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.FINE, id=60 )
    @Message( "Useless exception on call to Closeable.close()" )
    BAD_OPERATION ioExceptionOnClose( @Chain Exception exc ) ;

    @Log( level=LogLevel.FINE, id=63 )
    @Message( "Class {0} could not be loaded by bundle {1}" )
    BAD_OPERATION bundleCouldNotLoadClass( @Chain Exception exc,
        String arg0, String arg1 ) ;

    @Log( level=LogLevel.WARNING, id=75 )
    @Message( "Exception while handling event on {0}" )
    BAD_OPERATION exceptionInSelector( @Chain Throwable t, EventHandler arg0 ) ;
    
    @Log( level=LogLevel.FINE, id=76 )
    @Message( "Ignoring cancelled SelectionKey {0}: key will be removed "
        + "from Selector" )
    BAD_OPERATION canceledSelectionKey( SelectionKey arg0 ) ;
    
    @Log( level=LogLevel.FINE, id=77 )
    @Message( "The OSGi PackageAdmin service is not available" )
    BAD_OPERATION packageAdminServiceNotAvailable(  ) ;
    
    @Log( level=LogLevel.WARNING, id=78 )
    @Message( "The ORBImpl.set_parameters method was called more than once" )
    BAD_OPERATION setParameterCalledAgain(  ) ;
    
    @Log( level=LogLevel.WARNING, id=81 )
    @Message( "Could not make an instance of Class {0}" )
    BAD_OPERATION couldNotMakeInstance( @Chain Exception ex, Class<?> arg0 ) ;
    
    @Log( level=LogLevel.WARNING, id=82 )
    @Message( "Exception in createCopy`")
    BAD_OPERATION exceptionInCreateCopy( @Chain Exception exc);

    @Message( "Exception in reset method in ClientGroupManager")
    @Log( level=LogLevel.FINE, id=83 )
    BAD_OPERATION exceptionInReset(@Chain Throwable t);

    @Message( "No IORUpdate service context present in ClientGroupManager")
    @Log( level=LogLevel.FINE, id=84 )
    BAD_OPERATION noIORUpdateServicateContext(@Chain BAD_PARAM e);

    @Message( "Exception in next method in ClientGroupManager")
    @Log( level=LogLevel.FINE, id=85 )
    BAD_OPERATION exceptionInNext(@Chain Throwable t);

    @Message( "Could not bind initial GIS to name service")
    @Log( level=LogLevel.FINE, id=86 )
    BAD_OPERATION bindNameException(@Chain Exception e);

    @Log( level=LogLevel.WARNING, id=1 )
    @Message( "Null parameter" )
    @CS( CSValue.MAYBE )
    BAD_PARAM nullParam(  ) ;

    @Log( level=LogLevel.WARNING, id=1 )
    @Message( "Null parameter" )
    @CS( CSValue.NO )
    BAD_PARAM nullParamNoComplete(  ) ;
    
    @Log( level=LogLevel.FINE, id=2 )
    @Message( "Unable to find value factory" )
    BAD_PARAM unableFindValueFactory( @Chain MARSHAL exc ) ;
    
    @Log( level=LogLevel.WARNING, id=3 )
    @Message( "Abstract interface derived from non-abstract interface" )
    BAD_PARAM abstractFromNonAbstract(  ) ;
    
    @Log( level=LogLevel.WARNING, id=4 )
    @Message( "Error in reading IIOP TaggedProfile" )
    BAD_PARAM invalidTaggedProfile(  ) ;
    
    @Log( level=LogLevel.FINE, id=5 )
    @Message( "Object reference came from foreign ORB" )
    BAD_PARAM objrefFromForeignOrb(  ) ;

    int LOCAL_OBJECT_NOT_ALLOWED = CorbaExtension.self.getMinorCode(
        ORBUtilSystemException.class, "localObjectNotAllowed" ) ;

    @Log( level=LogLevel.FINE, id=6 )
    @Message( "Local object not allowed" )
    BAD_PARAM localObjectNotAllowed(  ) ;
    
    @Log( level=LogLevel.WARNING, id=7 )
    @Message( "null object reference" )
    BAD_PARAM nullObjectReference(  ) ;
    
    @Log( level=LogLevel.WARNING, id=8 )
    @Message( "Could not load class {0}" )
    BAD_PARAM couldNotLoadClass( String arg0 ) ;
    
    @Log( level=LogLevel.WARNING, id=9 )
    @Message( "Malformed URL {0}" )
    BAD_PARAM badUrl( String arg0 ) ;
    
    String fieldNotFound = "Field {0} not found in parser data object" ;

    @Log( level=LogLevel.WARNING, id=10 )
    @Message( fieldNotFound )
    BAD_PARAM fieldNotFound( String arg0 ) ;

    @Log( level=LogLevel.WARNING, id=10 )
    @Message( fieldNotFound )
    BAD_PARAM fieldNotFound( @Chain Exception exc, String arg0 ) ;
    
    @Log( level=LogLevel.WARNING, id=11 )
    @Message( "Error in setting field {0} to value {1} in parser data object" )
    BAD_PARAM errorSettingField( @Chain Throwable exc, String arg0,
        Object arg1 ) ;
    
    @Log( level=LogLevel.WARNING, id=12 )
    @Message( "Bounds error occurred in DII request" )
    BAD_PARAM boundsErrorInDiiRequest( @Chain Bounds b ) ;
    
    @Log( level=LogLevel.WARNING, id=13 )
    @Message( "Initialization error for persistent server" )
    @CS( CSValue.MAYBE)
    BAD_PARAM persistentServerInitError( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=14 )
    @Message( "Could not create array for field {0} with "
        + "component type {1} and size {2}" )
    BAD_PARAM couldNotCreateArray( @Chain Throwable exc, String arg0,
        Class<?> arg1, int arg2 ) ;
    
    @Log( level=LogLevel.WARNING, id=15 )
    @Message( "Could not set array for field {0} at index {1} "
        + "with component type {2} and size {3} to value {4}" )
    BAD_PARAM couldNotSetArray( @Chain Throwable thr, String arg0, int arg1,
        Class<?> arg2, int arg3, Object arg4 ) ;
    
    @Log( level=LogLevel.WARNING, id=16 )
    @Message( "Illegal bootstrap operation {0}" )
    BAD_PARAM illegalBootstrapOperation( String arg0 ) ;
    
    @Log( level=LogLevel.WARNING, id=17 )
    @Message( "Runtime Exception during bootstrap operation" )
    BAD_PARAM bootstrapRuntimeException( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=18 )
    @Message( "Exception during bootstrap operation" )
    BAD_PARAM bootstrapException( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=19 )
    @Message( "Expected a string, but argument was not of String type" )
    BAD_PARAM stringExpected(  ) ;
    
    @Log( level=LogLevel.WARNING, id=20 )
    @Message( "{0} does not represent a valid kind of typecode" )
    BAD_PARAM invalidTypecodeKind( @Chain Throwable t, int kind ) ;
    
    @Log( level=LogLevel.WARNING, id=21 )
    @Message( "cannot have a SocketFactory and a ContactInfoList at the "
        + "same time" )
    BAD_PARAM socketFactoryAndContactInfoListAtSameTime(  ) ;
    
    @Log( level=LogLevel.WARNING, id=22 )
    @Message( "cannot have Acceptors and a legacy SocketFactory at the "
        + "same time" )
    BAD_PARAM acceptorsAndLegacySocketFactoryAtSameTime(  ) ;
    
    @Log( level=LogLevel.WARNING, id=23 )
    @Message( "Reflective POA Servant requires an instance of "
        + "org.omg.CORBA_2_3.ORB" )
    BAD_PARAM badOrbForServant( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=24 )
    @Message( "Request partitioning value specified, {0}, "
        + "is outside supported range, {1} - {2}" )
    BAD_PARAM invalidRequestPartitioningPolicyValue( int arg0, int arg1,
        int arg2 ) ;
    
    @Log( level=LogLevel.WARNING, id=25 )
    @Message( "Could not set request partitioning component value to {0}, "
        + "valid values are {1} - {2}" )
    BAD_PARAM invalidRequestPartitioningComponentValue( int arg0,
        int arg1, int arg2 ) ;
    
    @Log( level=LogLevel.WARNING, id=26 )
    @Message( "Invalid request partitioning id {0}, "
        + "valid values are {1} - {2}" )
    BAD_PARAM invalidRequestPartitioningId( int arg0, int arg1, int arg2 ) ;
    
    @Log( level=LogLevel.FINE, id=27 )
    @Message( "ORBDynamicStubFactoryFactoryClass property had value {0}, "
        + "which could not be loaded by the ORB ClassLoader" )
    BAD_PARAM errorInSettingDynamicStubFactoryFactory( String arg0 ) ;
    
    @Log( level=LogLevel.WARNING, id=28 )
    @Message( "An attempt was made to register a ServiceContext.Factory with an "
        + "ID that is already registered" )
    BAD_PARAM registerDuplicateServiceContext(  ) ;
    
    @Log( level=LogLevel.WARNING, id=29 )
    @Message( "CORBA object is not an ObjectImpl in ORB.getIOR" )
    BAD_PARAM notAnObjectImpl(  ) ;
    
    String badTimeoutStringData = "{0} is not a valid positive decimal "
        + "integer for {1}" ;

    @Log( level=LogLevel.WARNING, id=30 )
    @Message( badTimeoutStringData )
    BAD_PARAM badTimeoutStringData( @Chain Exception exc, String arg0,
        String arg1 ) ;

    @Log( level=LogLevel.WARNING, id=30 )
    @Message( badTimeoutStringData )
    BAD_PARAM badTimeoutStringData( String arg0, String arg1 ) ;
    
    @Log( level=LogLevel.WARNING, id=31 )
    @Message( "Timeout data must be 3 or 4 positive decimal "
        + "integers separated by :" )
    BAD_PARAM badTimeoutDataLength(  ) ;
    
    @Log( level=LogLevel.WARNING, id=32 )
    @Message( "Load balancing value specified, {0}, is outside "
        + "supported range, {1} - {2}" )
    BAD_PARAM invalidLoadBalancingPolicyValue( int arg0, int arg1, int arg2 ) ;
    
    @Log( level=LogLevel.WARNING, id=33 )
    @Message( "Could not set load balancing component value to {0}, "
        + "valid values are {1} - {2}" )
    BAD_PARAM invalidLoadBalancingComponentValue( int arg0, int arg1, int arg2 ) ;
    
    @Log( level=LogLevel.WARNING, id=34 )
    @Message( "Invalid request partitioning id {0}, valid values are {1} - {2}" )
    BAD_PARAM invalidLoadBalancingId( String arg0, String arg1, String arg2 ) ;
    
    @Log( level=LogLevel.FINE, id=35 )
    @Message( "CodeBase unavailable on connection {0}" )
    BAD_PARAM codeBaseUnavailable( Connection conn ) ;
    
    @Log( level=LogLevel.WARNING, id=1 )
    @Message( "DSI method not called" )
    @CS( CSValue.MAYBE )
    BAD_INV_ORDER dsimethodNotcalled(  ) ;
    
    @Log( level=LogLevel.WARNING, id=2 )
    @Message( "arguments(NVList) called more than once for DSI ServerRequest" )
    BAD_INV_ORDER argumentsCalledMultiple(  ) ;
    
    @Log( level=LogLevel.WARNING, id=3 )
    @Message( "arguments(NVList) called after exceptions set for "
        + "DSI ServerRequest" )
    BAD_INV_ORDER argumentsCalledAfterException(  ) ;
    
    @Log( level=LogLevel.WARNING, id=4 )
    @Message( "arguments(NVList) called with null args for DSI ServerRequest" )
    BAD_INV_ORDER argumentsCalledNullArgs(  ) ;
    
    @Log( level=LogLevel.FINE, id=5 )
    @Message( "arguments(NVList) not called for DSI ServerRequest" )
    BAD_INV_ORDER argumentsNotCalled(  ) ;
    
    @Log( level=LogLevel.WARNING, id=6 )
    @Message( "set_result(Any) called more than once for DSI ServerRequest" )
    BAD_INV_ORDER setResultCalledMultiple(  ) ;
    
    @Log( level=LogLevel.FINE, id=7 )
    @Message( "set_result(Any) called exception was set for DSI ServerRequest" )
    BAD_INV_ORDER setResultAfterException(  ) ;
    
    @Log( level=LogLevel.WARNING, id=8 )
    @Message( "set_result(Any) called with null args for DSI ServerRequest" )
    BAD_INV_ORDER setResultCalledNullArgs(  ) ;
    
    @Log( level=LogLevel.WARNING, id=1 )
    @Message( "Foreign to native typecode conversion constructor should not be "
        + "called with native typecode" )
    BAD_TYPECODE badRemoteTypecode(  ) ;
    
    @Log( level=LogLevel.WARNING, id=2 )
    @Message( "Invoked operation on unresolved recursive TypeCode" )
    BAD_TYPECODE unresolvedRecursiveTypecode(  ) ;
    
    @Log( level=LogLevel.FINE, id=1 )
    @Message( "Connection failure: socketType: {0}; hostname: {1}; port: {2}" )
    COMM_FAILURE connectFailure( @Chain Throwable t, String arg0,
        String arg1, String arg2 ) ;

    String writeErrorSend = "Write error sent" ;

    @Log( level=LogLevel.FINE, id=3 )
    @Message( writeErrorSend )
    @CS( CSValue.MAYBE )
    COMM_FAILURE writeErrorSend( @Chain Exception exc ) ;

    @Log( level=LogLevel.FINE, id=3 )
    @Message( writeErrorSend )
    COMM_FAILURE writeErrorSend() ;
    
    @Log( level=LogLevel.WARNING, id=4 )
    @Message( "Get properties error" )
    COMM_FAILURE getPropertiesError(  ) ;
    
    @Log( level=LogLevel.WARNING, id=5 )
    @Message( "Bootstrap server is not available" )
    COMM_FAILURE bootstrapServerNotAvail(  ) ;
    
    @Log( level=LogLevel.WARNING, id=6 )
    @Message( "Invocation error" )
    COMM_FAILURE invokeError(  ) ;
    
    @Log( level=LogLevel.WARNING, id=7 )
    @Message( "DefaultSocketFactory.createServerSocket only handles "
        + "IIOP_CLEAR_TEXT, given {0}" )
    COMM_FAILURE defaultCreateServerSocketGivenNonIiopClearText( String arg0 ) ;
    
    @Log( level=LogLevel.FINE, id=8 )
    @Message( "Connection abort" )
    COMM_FAILURE connectionAbort( @Chain Throwable thr  ) ;

    int CONNECTION_REBIND = CorbaExtension.self.getMinorCode(
        ORBUtilSystemException.class, "connectionRebind" ) ;

    String connectionRebind = "Connection rebind" ;

    @Log( level=LogLevel.FINE, id=9 )
    @Message( "Connection rebind" )
    COMM_FAILURE connectionRebind( @Chain Throwable thr  ) ;

    @Log( level=LogLevel.FINE, id=9 )
    @Message( "Connection rebind" )
    @CS( CSValue.MAYBE )
    COMM_FAILURE connectionRebindMaybe( @Chain Throwable thr  ) ;

    @Log( level=LogLevel.FINE, id=9 )
    @Message( "Connection rebind" )
    COMM_FAILURE connectionRebind( ) ;
    
    @Log( level=LogLevel.WARNING, id=10 )
    @Message( "Received a GIOP MessageError, indicating header corruption or "
        + "version mismatch" )
    COMM_FAILURE recvMsgError(  ) ;
    
    @Log( level=LogLevel.FINE, id=11 )
    @Message( "IOException received when reading from connection {0}" )
    COMM_FAILURE ioexceptionWhenReadingConnection( @Chain Exception exc,
        Connection arg0 ) ;
    
    @Log( level=LogLevel.FINE, id=12 )
    @Message( "SelectionKey invalid on channel, {0}" )
    COMM_FAILURE selectionKeyInvalid( String arg0 ) ;
    
    @Log( level=LogLevel.FINE, id=13 )
    @Message( "Unexpected {0} in accept" )
    COMM_FAILURE exceptionInAccept( @Chain Exception exc, String arg0 ) ;
    
    @Log( level=LogLevel.FINE, id=14 )
    @Message( "Unexpected exception, has permissions {0}" )
    COMM_FAILURE securityExceptionInAccept( @Chain Exception exc, String arg0 ) ;
    
    @Log( level=LogLevel.WARNING, id=15 )
    @Message( "Read of full message failed : bytes requested = {0} "
        + "bytes read = {1} max wait time = {2} total time spent waiting = {3}" )
    COMM_FAILURE transportReadTimeoutExceeded( int arg0, int arg1,
        int arg2, int arg3 ) ;
    
    @Log( level=LogLevel.SEVERE, id=16 )
    @Message( "Unable to create IIOP listener on the specified "
        + "host {0} and port {1}" )
    COMM_FAILURE createListenerFailed( @Chain Throwable thr, String host, int port ) ;
    
    @Log( level=LogLevel.FINE, id=17 )
    @Message( "Throwable received in ReadBits" )
    COMM_FAILURE throwableInReadBits( @Chain Throwable exc ) ;
    
    @Log( level=LogLevel.WARNING, id=18 )
    @Message( "IOException in accept" )
    COMM_FAILURE oexceptionInAccept(  ) ;
    
    @Log( level=LogLevel.WARNING, id=19 )
    @Message( "Communications timeout waiting for response.  "
        + "Exceeded {0} milliseconds" )
    @CS( CSValue.MAYBE )
    COMM_FAILURE communicationsTimeoutWaitingForResponse( long arg0 ) ;
    
    @Log( level=LogLevel.WARNING, id=20 )
    @Message( "Communications retry timeout.  Exceeded {0} milliseconds" )
    COMM_FAILURE communicationsRetryTimeout( @Chain Exception exc, long arg0 ) ;
    
    @Log( level=LogLevel.FINE, id=21 )
    @Message( "Ignoring exception while waiting for retry" )
    COMM_FAILURE ignoringExceptionWhileWaitingForRetry(  ) ;
    
    @Log( level=LogLevel.SEVERE, id=22 )
    @Message( "Invalid request for a temporary write selector object for use "
        + "on a blocking connection: {0}." )
    COMM_FAILURE temporaryWriteSelectorWithBlockingConnection(
        Connection arg0 ) ;
    
    @Log( level=LogLevel.SEVERE, id=23 )
    @Message( "Invalid request for a temporary read selector object for use "
        + "on a blocking connection: {0}." )
    COMM_FAILURE temporaryReadSelectorWithBlockingConnection(
        Connection arg0 ) ;
    
    @Log( level=LogLevel.SEVERE, id=24 )
    @Message( "TemporarySelector's Selector, {0} .select(timeout) must called "
        + "with timeout value greater than 0, "
        + "called with a timeout value of, {1}." )
    COMM_FAILURE temporarySelectorSelectTimeoutLessThanOne( Selector arg0,
        long arg1 ) ;
    
    @Log( level=LogLevel.WARNING, id=25 )
    @Message( "Write of message exceeded TCP timeout : max wait time = {0} ms, "
        + "total time spent blocked, waiting to write = {1} ms." )
    COMM_FAILURE transportWriteTimeoutExceeded( int arg0, int arg1 ) ;
    
    @Log( level=LogLevel.SEVERE, id=26 )
    @Message( "Unexpected exception when reading with a temporary selector: "
        + "bytes read = {0}, bytes requested = {1}, "
        + "time spent waiting = {2} ms, max time to wait = {3}." )
    COMM_FAILURE exceptionWhenReadingWithTemporarySelector( @Chain Exception exc,
        int arg0, int arg1, int arg2, int arg3 ) ;
    
    @Log( level=LogLevel.SEVERE, id=27 )
    @Message( "Unexpected exception when writing with a temporary selector:  "
        + "bytes written = {0}, total bytes requested to write = {1}, "
        + "time spent waiting = {2} ms, max time to wait = {3}." )
    COMM_FAILURE exceptionWhenWritingWithTemporarySelector( @Chain Exception exc,
        int arg0, int arg1, int arg2, int arg3 ) ;
    
    @Log( level=LogLevel.FINE, id=28 )
    @Message( "Throwable received in doOptimizedReadStrategy" )
    COMM_FAILURE throwableInDoOptimizedReadStrategy( @Chain Throwable thr ) ;
    
    @Log( level=LogLevel.WARNING, id=29 )
    @Message( "Blocking read failed, expected to read additional bytes:  "
        + "max wait time = {0}ms total time spent waiting = {1}ms" )
    COMM_FAILURE blockingReadTimeout( long arg0, long arg1 ) ;
    
    @Log( level=LogLevel.FINE, id=30 )
    @Message( "Exception in a blocking read on connection {0} with a "
        + "temporary selector" )
    COMM_FAILURE exceptionBlockingReadWithTemporarySelector( @Chain Exception exc,
        Connection arg0 ) ;
    
    @Log( level=LogLevel.SEVERE, id=31 )
    @Message( "Invalid operation, attempting a non-blocking read on blocking "
        + "connection, {0}" )
    COMM_FAILURE nonBlockingReadOnBlockingSocketChannel( Connection arg0 ) ;
    
    @Log( level=LogLevel.FINE, id=32 )
    @Message( "Unexpected exception when canceling SelectionKey and "
        + "flushing temporary Selector" )
    COMM_FAILURE unexpectedExceptionCancelAndFlushTempSelector(
        @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=33 )
    @Message( "Ignoring request to read a message which exceeds read size "
        + "threshold of {0} bytes, requested size was {1}. "
        + "Use ORB property -D{2}=<# of bytes> to set threshold higher." )
    COMM_FAILURE maximumReadByteBufferSizeExceeded( int arg0,
        int arg1, String arg2 ) ;
    
    @Log( level=LogLevel.FINE, id=34 )
    @Message( "Received {0}, in a blocking read on connection, {1}, "
        + "because an 'end of stream' was detected" )
    COMM_FAILURE blockingReadEndOfStream( @Chain Exception exc, String arg0,
        String arg1 ) ;
    
    @Log( level=LogLevel.FINE, id=35 )
    @Message( "Received {0}, in a non-blocking read on connection, {1}, "
        + "because an 'end of stream' was detected" )
    COMM_FAILURE nonblockingReadEndOfStream( String arg0, String arg1 ) ;
    
    @Log( level=LogLevel.WARNING, id=36 )
    @Message( "IOException in accept" )
    COMM_FAILURE ioexceptionInAccept( @Chain Exception exc ) ;

    @Log( level=LogLevel.FINE, id=36 )
    @Message( "IOException in accept" )
    COMM_FAILURE ioexceptionInAcceptFine( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=37 )
    @Message( "Timeout while reading data in buffer manager" )
    COMM_FAILURE bufferReadManagerTimeout(  ) ;
    
    @Log( level=LogLevel.WARNING, id=1 )
    @Message( "A character did not map to the transmission code set" )
    DATA_CONVERSION badStringifiedIorLen(  ) ;
    
    @Log( level=LogLevel.WARNING, id=2 )
    @Message( "Bad stringified IOR" )
    DATA_CONVERSION badStringifiedIor( @Chain Throwable thr ) ;
    
    @Log( level=LogLevel.WARNING, id=3 )
    @Message( "Unable to perform resolve_initial_references due to bad host or "
        + "port configuration" )
    DATA_CONVERSION badModifier(  ) ;
    
    @Log( level=LogLevel.WARNING, id=4 )
    @Message( "Codesets incompatible" )
    DATA_CONVERSION codesetIncompatible(  ) ;
    
    @Log( level=LogLevel.WARNING, id=5 )
    @Message( "Illegal hexadecimal digit" )
    DATA_CONVERSION badHexDigit(  ) ;
    
    @Log( level=LogLevel.WARNING, id=6 )
    @Message( "Invalid unicode pair detected during code set conversion" )
    DATA_CONVERSION badUnicodePair( @Chain MalformedInputException exc  ) ;
    
    @Log( level=LogLevel.WARNING, id=7 )
    @Message( "Tried to convert bytes to a single java char, "
        + "but conversion yielded more than one Java char (Surrogate pair?)" )
    DATA_CONVERSION btcResultMoreThanOneChar(  ) ;
    
    @Log( level=LogLevel.WARNING, id=8 )
    @Message( "Client sent code set service context that we do not support" )
    DATA_CONVERSION badCodesetsFromClient(  ) ;
    
    @Log( level=LogLevel.WARNING, id=9 )
    @Message( "Char to byte conversion for a CORBA char resulted in more than "
        + "one byte" )
    @CS( CSValue.MAYBE )
    DATA_CONVERSION invalidSingleCharCtb(  ) ;
    
    @Log( level=LogLevel.WARNING, id=10 )
    @Message( "Character to byte conversion did not exactly double number of "
        + "chars (GIOP 1.1 only)" )
    @CS( CSValue.MAYBE )
    DATA_CONVERSION badGiop11Ctb(  ) ;
    
    @Log( level=LogLevel.WARNING, id=12 )
    @Message( "Tried to insert a sequence of length {0} into a "
        + "bounded sequence of maximum length {1} in an Any" )
    DATA_CONVERSION badSequenceBounds( int len, int maxLen ) ;
    
    @Log( level=LogLevel.WARNING, id=13 )
    @Message( "Class {0} is not a subtype of ORBSocketFactory" )
    DATA_CONVERSION illegalSocketFactoryType( String arg0 ) ;
    
    @Log( level=LogLevel.WARNING, id=14 )
    @Message( "{0} is not a valid custom socket factory" )
    DATA_CONVERSION badCustomSocketFactory( @Chain Exception exc, String arg0 ) ;
    
    @Log( level=LogLevel.WARNING, id=15 )
    @Message( "Fragment size {0} is too small: it must be at least {1}" )
    DATA_CONVERSION fragmentSizeMinimum( int arg0, int arg1 ) ;
    
    @Log( level=LogLevel.WARNING, id=16 )
    @Message( "Illegal value for fragment size ({0}): must be divisible by {1}" )
    DATA_CONVERSION fragmentSizeDiv( int arg0, int arg1 ) ;
    
    @Log( level=LogLevel.WARNING, id=17 )
    @Message( "Could not instantiate ORBInitializer {0}" )
    DATA_CONVERSION orbInitializerFailure( @Chain Exception exc, String arg0 ) ;
    
    @Log( level=LogLevel.WARNING, id=18 )
    @Message( "orb initializer class {0} is not a subtype of ORBInitializer" )
    DATA_CONVERSION orbInitializerType( String arg0 ) ;
    
    @Log( level=LogLevel.WARNING, id=19 )
    @Message( "Bad syntax for ORBInitialReference" )
    DATA_CONVERSION orbInitialreferenceSyntax(  ) ;
    
    @Log( level=LogLevel.WARNING, id=20 )
    @Message( "Could not instantiate Acceptor {0}" )
    DATA_CONVERSION acceptorInstantiationFailure( @Chain Exception exc,
        String arg0 ) ;
    
    @Log( level=LogLevel.WARNING, id=21 )
    @Message( "Acceptor class {0} is not a subtype of Acceptor" )
    DATA_CONVERSION acceptorInstantiationTypeFailure( String arg0 ) ;
    
    @Log( level=LogLevel.WARNING, id=22 )
    @Message( "Class {0} is not a subtype of CorbaContactInfoListFactory" )
    DATA_CONVERSION illegalContactInfoListFactoryType( String arg0 ) ;
    
    @Log( level=LogLevel.WARNING, id=23 )
    @Message( "{0} is not a valid CorbaContactInfoListFactory" )
    DATA_CONVERSION badContactInfoListFactory( @Chain Exception exc,
        String arg0 ) ;
    
    @Log( level=LogLevel.WARNING, id=24 )
    @Message( "Class {0} is not a subtype of IORToSocketInfo" )
    DATA_CONVERSION illegalIorToSocketInfoType( String arg0 ) ;
    
    @Log( level=LogLevel.WARNING, id=25 )
    @Message( "{0} is not a valid custom IORToSocketInfo" )
    DATA_CONVERSION badCustomIorToSocketInfo( @Chain Exception exc,
        String arg0 ) ;
    
    @Log( level=LogLevel.WARNING, id=26 )
    @Message( "Class {0} is not a subtype of IIOPPrimaryToContactInfo" )
    DATA_CONVERSION illegalIiopPrimaryToContactInfoType( String arg0 ) ;
    
    @Log( level=LogLevel.WARNING, id=27 )
    @Message( "{0} is not a valid custom IIOPPrimaryToContactInfo" )
    DATA_CONVERSION badCustomIiopPrimaryToContactInfo( @Chain Exception exc,
        String arg0 ) ;
    
    @Log( level=LogLevel.WARNING, id=1 )
    @Message( "Bad corbaloc: URL" )
    INV_OBJREF badCorbalocString(  ) ;
    
    @Log( level=LogLevel.WARNING, id=2 )
    @Message( "No profile in IOR" )
    INV_OBJREF noProfilePresent(  ) ;
    
    @Log( level=LogLevel.WARNING, id=1 )
    @Message( "Cannot create ORB ID datastore" )
    INITIALIZE cannotCreateOrbidDb(  ) ;
    
    @Log( level=LogLevel.WARNING, id=2 )
    @Message( "Cannot read ORB ID datastore" )
    INITIALIZE cannotReadOrbidDb(  ) ;
    
    @Log( level=LogLevel.WARNING, id=3 )
    @Message( "Cannot write ORB ID datastore" )
    INITIALIZE cannotWriteOrbidDb(  ) ;
    
    @Log( level=LogLevel.WARNING, id=4 )
    @Message( "legacyGetServerPort called before endpoints initialized" )
    INITIALIZE getServerPortCalledBeforeEndpointsInitialized(  ) ;
    
    @Log( level=LogLevel.FINE, id=5 )
    @Message( "Persistent server port is not set" )
    @CS( CSValue.MAYBE )
    INITIALIZE persistentServerportNotSet(  ) ;
    
    @Log( level=LogLevel.FINE, id=6 )
    @Message( "Persistent server ID is not set" )
    @CS( CSValue.MAYBE )
    INITIALIZE persistentServeridNotSet(  ) ;
    
    @Log( level=LogLevel.WARNING, id=7 )
    @Message( "Exception occurred while running a user configurator" )
    INITIALIZE userConfiguratorException( @Chain Exception exc  ) ;
    
    @Log( level=LogLevel.WARNING, id=1 )
    @Message( "Non-existent ORB ID" )
    INTERNAL nonExistentOrbid(  ) ;
    
    @Log( level=LogLevel.WARNING, id=2 )
    @Message( "No server request dispatcher" )
    INTERNAL noServerSubcontract(  ) ;
    
    @Log( level=LogLevel.WARNING, id=3 )
    @Message( "server request dispatcher template size error" )
    INTERNAL serverScTempSize(  ) ;
    
    @Log( level=LogLevel.WARNING, id=4 )
    @Message( "No client request dispatcher class" )
    INTERNAL noClientScClass(  ) ;
    
    @Log( level=LogLevel.WARNING, id=5 )
    @Message( "No IIOP profile in server request dispatcher" )
    INTERNAL serverScNoIiopProfile(  ) ;
    
    @Log( level=LogLevel.WARNING, id=6 )
    @Message( "getSystemException returned null" )
    INTERNAL getSystemExReturnedNull(  ) ;
    
    @Log( level=LogLevel.WARNING, id=7 )
    @Message( "The repository ID of a user exception had a bad length" )
    INTERNAL peekstringFailed(  ) ;
    
    @Log( level=LogLevel.WARNING, id=8 )
    @Message( "Unable to determine local hostname from "
        + "InetAddress.getLocalHost().getHostName()" )
    INTERNAL getLocalHostFailed( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=10 )
    @Message( "Bad locate request status in IIOP locate reply" )
    INTERNAL badLocateRequestStatus(  ) ;
    
    @Log( level=LogLevel.WARNING, id=11 )
    @Message( "Error while stringifying an object reference" )
    INTERNAL stringifyWriteError(  ) ;
    
    @Log( level=LogLevel.WARNING, id=12 )
    @Message( "IIOP message with bad GIOP 1.0 message type" )
    INTERNAL badGiopRequestType(  ) ;
    
    @Log( level=LogLevel.WARNING, id=13 )
    @Message( "Error in unmarshalling user exception" )
    INTERNAL errorUnmarshalingUserexc(  ) ;
    
    @Log( level=LogLevel.WARNING, id=14 )
    @Message( "Overflow in RequestDispatcherRegistry" )
    INTERNAL requestdispatcherregistryError(  ) ;
    
    @Log( level=LogLevel.WARNING, id=15 )
    @Message( "Error in processing a LocationForward" )
    INTERNAL locationforwardError(  ) ;
    
    @Log( level=LogLevel.WARNING, id=16 )
    @Message( "Wrong client request dispatcher" )
    INTERNAL wrongClientsc(  ) ;
    
    @Log( level=LogLevel.WARNING, id=17 )
    @Message( "Bad servant in read_Object" )
    INTERNAL badServantReadObject(  ) ;
    
    @Log( level=LogLevel.WARNING, id=18 )
    @Message( "multiple IIOP profiles not supported" )
    INTERNAL multIiopProfNotSupported(  ) ;
    
    @Log( level=LogLevel.WARNING, id=20 )
    @Message( "Error in GIOP magic" )
    @CS( CSValue.MAYBE )
    INTERNAL giopMagicError(  ) ;
    
    @Log( level=LogLevel.WARNING, id=21 )
    @Message( "Error in GIOP version" )
    @CS( CSValue.MAYBE )
    INTERNAL giopVersionError(  ) ;
    
    @Log( level=LogLevel.WARNING, id=22 )
    @Message( "Illegal reply status in GIOP reply message" )
    @CS( CSValue.MAYBE )
    INTERNAL illegalReplyStatus(  ) ;
    
    @Log( level=LogLevel.WARNING, id=23 )
    @Message( "Illegal GIOP message type" )
    INTERNAL illegalGiopMsgType(  ) ;
    
    @Log( level=LogLevel.WARNING, id=24 )
    @Message( "Fragmentation not allowed for this message type" )
    @CS( CSValue.MAYBE )
    INTERNAL fragmentationDisallowed(  ) ;
    
    @Log( level=LogLevel.WARNING, id=25 )
    @Message( "Bad status in the IIOP reply message" )
    INTERNAL badReplystatus(  ) ;
    
    @Log( level=LogLevel.WARNING, id=26 )
    @Message( "character to byte converter failure" )
    INTERNAL ctbConverterFailure( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=27 )
    @Message( "byte to character converter failure" )
    INTERNAL btcConverterFailure( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=28 )
    @Message( "Unsupported wchar encoding: ORB only supports fixed width "
        + "UTF-16 encoding" )
    INTERNAL wcharArrayUnsupportedEncoding(  ) ;
    
    @Log( level=LogLevel.WARNING, id=29 )
    @Message( "Illegal target address disposition value" )
    @CS( CSValue.MAYBE )
    INTERNAL illegalTargetAddressDisposition(  ) ;
    
    @Log( level=LogLevel.WARNING, id=30 )
    @Message( "No reply while attempting to get addressing disposition" )
    INTERNAL nullReplyInGetAddrDisposition(  ) ;
    
    @Log( level=LogLevel.WARNING, id=31 )
    @Message( "Invalid GIOP target addressing preference" )
    INTERNAL orbTargetAddrPreferenceInExtractObjectkeyInvalid(  ) ;
    
    @Log( level=LogLevel.WARNING, id=32 )
    @Message( "Invalid isStreamed TCKind {0}" )
    @CS( CSValue.MAYBE )
    INTERNAL invalidIsstreamedTckind( int kind ) ;
    
    @Log( level=LogLevel.WARNING, id=33 )
    @Message( "Found a JDK 1.3.1 patch level indicator with value less than "
        + "JDK 1.3.1_01 value of 1" )
    INTERNAL invalidJdk131PatchLevel(  ) ;
    
    @Log( level=LogLevel.WARNING, id=34 )
    @Message( "Error unmarshalling service context data" )
    @CS( CSValue.MAYBE )
    INTERNAL svcctxUnmarshalError(  ) ;
    
    @Log( level=LogLevel.WARNING, id=35 )
    @Message( "null IOR" )
    INTERNAL nullIor(  ) ;
    
    @Log( level=LogLevel.WARNING, id=36 )
    @Message( "Unsupported GIOP version {0}" )
    INTERNAL unsupportedGiopVersion( GIOPVersion arg0 ) ;
    
    @Log( level=LogLevel.WARNING, id=37 )
    @Message( "Application exception in special method: should not happen" )
    INTERNAL applicationExceptionInSpecialMethod( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=38 )
    @Message( "Assertion failed: statement not reachable (1)" )
    INTERNAL statementNotReachable1(  ) ;
    
    @Log( level=LogLevel.WARNING, id=39 )
    @Message( "Assertion failed: statement not reachable (2)" )
    INTERNAL statementNotReachable2(  ) ;
    
    @Log( level=LogLevel.WARNING, id=40 )
    @Message( "Assertion failed: statement not reachable (3)" )
    INTERNAL statementNotReachable3(  ) ;
    
    @Log( level=LogLevel.FINE, id=41 )
    @Message( "Assertion failed: statement not reachable (4)" )
    INTERNAL statementNotReachable4(  ) ;
    
    @Log( level=LogLevel.WARNING, id=42 )
    @Message( "Assertion failed: statement not reachable (5)" )
    INTERNAL statementNotReachable5(  ) ;
    
    @Log( level=LogLevel.WARNING, id=43 )
    @Message( "Assertion failed: statement not reachable (6)" )
    INTERNAL statementNotReachable6(  ) ;
    
    @Log( level=LogLevel.WARNING, id=44 )
    @Message( "Unexpected exception while unmarshalling DII user exception" )
    INTERNAL unexpectedDiiException( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=45 )
    @Message( "This method should never be called" )
    INTERNAL methodShouldNotBeCalled(  ) ;
    
    @Log( level=LogLevel.WARNING, id=46 )
    @Message( "We do not support cancel request for GIOP 1.1" )
    INTERNAL cancelNotSupported(  ) ;
    
    @Log( level=LogLevel.WARNING, id=47 )
    @Message( "Empty stack exception while calling runServantPostInvoke" )
    INTERNAL emptyStackRunServantPostInvoke( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=48 )
    @Message( "Bad exception typecode" )
    INTERNAL problemWithExceptionTypecode( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=49 )
    @Message( "Illegal Subcontract id {0}" )
    INTERNAL illegalSubcontractId( String arg0 ) ;
    
    @Log( level=LogLevel.WARNING, id=50 )
    @Message( "Bad system exception in locate reply" )
    INTERNAL badSystemExceptionInLocateReply(  ) ;
    
    @Log( level=LogLevel.WARNING, id=51 )
    @Message( "Bad system exception in reply" )
    @CS( CSValue.MAYBE )
    INTERNAL badSystemExceptionInReply( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=52 )
    @Message( "Bad CompletionStatus {0} in locate reply" )
    @CS( CSValue.MAYBE )
    INTERNAL badCompletionStatusInLocateReply( int arg0 ) ;
    
    @Log( level=LogLevel.WARNING, id=53 )
    @Message( "Bad CompletionStatus {0} in reply" )
    @CS( CSValue.MAYBE )
    INTERNAL badCompletionStatusInReply( int arg0 ) ;
    
    @Log( level=LogLevel.WARNING, id=54 )
    @Message( "The BadKind exception should never occur here" )
    INTERNAL badkindCannotOccur( @Chain BadKind bk ) ;

    @Log( level=LogLevel.WARNING, id=54 )
    @Message( "The BadKind exception should never occur here" )
    INTERNAL badkindCannotOccur( ) ;
    
    @Log( level=LogLevel.WARNING, id=55 )
    @Message( "Could not resolve alias typecode" )
    INTERNAL errorResolvingAlias(  ) ;
    
    @Log( level=LogLevel.WARNING, id=56 )
    @Message( "The long double type is not supported in Java" )
    INTERNAL tkLongDoubleNotSupported(  ) ;
    
    @Log( level=LogLevel.WARNING, id=57 )
    @Message( "Illegal typecode kind" )
    INTERNAL typecodeNotSupported(  ) ;
    
    String boundsCannotOccur =
        "Bounds exception cannot occur in this context" ;

    @Log( level=LogLevel.WARNING, id=59 )
    @Message( boundsCannotOccur )
    INTERNAL boundsCannotOccur( @Chain Bounds bd ) ;

    @Log( level=LogLevel.WARNING, id=59 )
    @Message( boundsCannotOccur )
    INTERNAL boundsCannotOccur( @Chain org.omg.CORBA.TypeCodePackage.Bounds bd ) ;
    
    @Log( level=LogLevel.WARNING, id=61 )
    @Message( "Number of invocations is already zero, but another invocation "
        + "has completed" )
    @CS( CSValue.YES )
    INTERNAL numInvocationsAlreadyZero(  ) ;
    
    @Log( level=LogLevel.WARNING, id=62 )
    @Message( "Error in constructing instance of bad server ID handler" )
    INTERNAL errorInitBadserveridhandler( @Chain Exception exc  ) ;
    
    @Log( level=LogLevel.WARNING, id=63 )
    @Message( "No TOAFactory is available" )
    INTERNAL noToa(  ) ;
    
    @Log( level=LogLevel.WARNING, id=64 )
    @Message( "No POAFactory is available" )
    INTERNAL noPoa(  ) ;
    
    @Log( level=LogLevel.WARNING, id=65 )
    @Message( "Invocation info stack is unexpectedly empty" )
    INTERNAL invocationInfoStackEmpty(  ) ;
    
    @Log( level=LogLevel.WARNING, id=66 )
    @Message( "Empty or null code set string" )
    INTERNAL badCodeSetString(  ) ;
    
    @Log( level=LogLevel.WARNING, id=67 )
    @Message( "Unknown native codeset: {0}" )
    INTERNAL unknownNativeCodeset( int codeset ) ;
    
    @Log( level=LogLevel.WARNING, id=68 )
    @Message( "Unknown conversion codeset: {0}" )
    INTERNAL unknownConversionCodeSet( int codeset ) ;
    
    @Log( level=LogLevel.WARNING, id=69 )
    @Message( "Invalid codeset number" )
    INTERNAL invalidCodeSetNumber( @Chain NumberFormatException exc  ) ;
    
    @Log( level=LogLevel.WARNING, id=70 )
    @Message( "Invalid codeset string {0}" )
    INTERNAL invalidCodeSetString( @Chain NoSuchElementException exc,
        String arg0 ) ;
    
    @Log( level=LogLevel.WARNING, id=71 )
    @Message( "Invalid CTB converter {0}" )
    INTERNAL invalidCtbConverterName( Exception exc, String arg0 ) ;
    
    @Log( level=LogLevel.WARNING, id=72 )
    @Message( "Invalid BTC converter {0}" )
    INTERNAL invalidBtcConverterName( @Chain Exception exc, String arg0 ) ;
    
    @Log( level=LogLevel.WARNING, id=73 )
    @Message( "Could not duplicate CDRInputStream" )
    INTERNAL couldNotDuplicateCdrInputStream( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=74 )
    @Message( "BootstrapResolver caught an unexpected ApplicationException" )
    INTERNAL bootstrapApplicationException( @Chain Exception exc  ) ;
    
    @Log( level=LogLevel.FINE, id=75 )
    @Message( "Old entry in serialization indirection table has a "
        + "different value than the value being added with the same key" )
    INTERNAL duplicateIndirectionOffset(  ) ;
    
    @Log( level=LogLevel.WARNING, id=76 )
    @Message( "GIOP Cancel request contained a bad request ID: the request "
        + "ID did not match the request that was to be cancelled" )
    INTERNAL badMessageTypeForCancel(  ) ;
    
    @Log( level=LogLevel.WARNING, id=77 )
    @Message( "Duplicate ExceptionDetailMessage" )
    INTERNAL duplicateExceptionDetailMessage(  ) ;
    
    @Log( level=LogLevel.WARNING, id=78 )
    @Message( "Bad ExceptionDetailMessage ServiceContext type" )
    INTERNAL badExceptionDetailMessageServiceContextType(  ) ;
    
    @Log( level=LogLevel.WARNING, id=79 )
    @Message( "unexpected direct ByteBuffer with non-channel socket" )
    INTERNAL unexpectedDirectByteBufferWithNonChannelSocket(  ) ;
    
    @Log( level=LogLevel.WARNING, id=80 )
    @Message( "unexpected non-direct ByteBuffer with channel socket" )
    INTERNAL unexpectedNonDirectByteBufferWithChannelSocket(  ) ;
    
    @Log( level=LogLevel.WARNING, id=82 )
    @Message( "There should be at least one CorbaContactInfo to try (and fail) "
        + "so this error should not be seen." )
    INTERNAL invalidContactInfoListIteratorFailureException(  ) ;
    
    @Log( level=LogLevel.WARNING, id=83 )
    @Message( "Remarshal with nowhere to go" )
    INTERNAL remarshalWithNowhereToGo(  ) ;
    
    @Log( level=LogLevel.WARNING, id=84 )
    @Message( "Exception when sending close connection" )
    INTERNAL exceptionWhenSendingCloseConnection( @Chain Throwable thr ) ;
    
    @Log( level=LogLevel.WARNING, id=85 )
    @Message( "A reflective tie got an error while invoking method {0} on "
        + "class {1}" )
    INTERNAL invocationErrorInReflectiveTie( @Chain Exception exc,
        String arg0, String arg1 ) ;
    
    @Log( level=LogLevel.WARNING, id=86 )
    @Message( "Could not find or invoke write method on exception "
        + "Helper class {0}" )
    INTERNAL badHelperWriteMethod( @Chain Exception exc, String arg0 ) ;
    
    @Log( level=LogLevel.WARNING, id=87 )
    @Message( "Could not find or invoke read method on exception "
        + "Helper class {0}" )
    INTERNAL badHelperReadMethod( @Chain Exception exc, String arg0 ) ;
    
    @Log( level=LogLevel.WARNING, id=88 )
    @Message( "Could not find or invoke id method on exception "
        + "Helper class {0}" )
    INTERNAL badHelperIdMethod( @Chain Exception exc, String arg0 ) ;
    
    @Log( level=LogLevel.WARNING, id=89 )
    @Message( "Tried to write exception of type {0} that was not declared on "
        + "method" )
    INTERNAL writeUndeclaredException( @Chain Exception exc, String arg0 ) ;
    
    @Log( level=LogLevel.WARNING, id=90 )
    @Message( "Tried to read undeclared exception with ID {0}" )
    INTERNAL readUndeclaredException( String arg0 ) ;
    
    @Log( level=LogLevel.WARNING, id=91 )
    @Message( "Unable to setSocketFactoryORB" )
    INTERNAL unableToSetSocketFactoryOrb( @Chain Throwable thr ) ;
    
    @Log( level=LogLevel.WARNING, id=92 )
    @Message( "Unexpected exception occurred where no exception should occur" )
    INTERNAL unexpectedException( @Chain Throwable exc ) ;
    
    @Log( level=LogLevel.WARNING, id=93 )
    @Message( "No invocation handler available for {0}" )
    INTERNAL noInvocationHandler( String arg0 ) ;
    
    @Log( level=LogLevel.WARNING, id=94 )
    @Message( "{0}: invalid buffer manager strategy for Java serialization" )
    INTERNAL invalidBuffMgrStrategy( String arg0 ) ;
    
    @Log( level=LogLevel.WARNING, id=95 )
    @Message( "Java stream initialization failed" )
    INTERNAL javaStreamInitFailed(  ) ;
    
    @Log( level=LogLevel.WARNING, id=96 )
    @Message( "An ORBVersionServiceContext was already in the "
        + "service context list" )
    INTERNAL duplicateOrbVersionServiceContext(  ) ;
    
    @Log( level=LogLevel.WARNING, id=97 )
    @Message( "A SendingContextServiceContext was already in the "
        + "service context list" )
    INTERNAL duplicateSendingContextServiceContext(  ) ;
    
    @Log( level=LogLevel.WARNING, id=98 )
    @Message( "No such threadpool or queue {0}" )
    INTERNAL noSuchThreadpoolOrQueue( @Chain Throwable thr, int arg0 ) ;
    
    @Log( level=LogLevel.FINE, id=99 )
    @Message( "Successfully created IIOP listener on the specified host/port: "
        + "{0}/{1}" )
    INTERNAL infoCreateListenerSucceeded( String arg0, String arg1 ) ;
    
    @Log( level=LogLevel.WARNING, id=100 )
    @Message( "Exception occurred while closing an IO stream object" )
    INTERNAL ioexceptionDuringStreamClose( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.SEVERE, id=101 )
    @Message( "Invalid Java serialization version {0}" )
    INTERNAL invalidJavaSerializationVersion( JavaSerializationComponent arg0 ) ;
    
    @Log( level=LogLevel.WARNING, id=102 )
    @Message( "Object in ServiceContext map was not of the correct type" )
    INTERNAL errorInServiceContextMap(  ) ;
    
    @Log( level=LogLevel.WARNING, id=103 )
    @Message( "The ContactInfoList in a CorbaClientDelegate is NOT a "
        + "CorbaContactInfoList" )
    INTERNAL badTypeInDelegate(  ) ;
    
    @Log( level=LogLevel.WARNING, id=117 )
    @Message( "Ignoring parsed fragment message because there is no "
        + "fragment queue found for request id {0}." )
    INTERNAL noFragmentQueueForRequestId( String arg0 ) ;
    
    @Log( level=LogLevel.WARNING, id=118 )
    @Message( "Ignoring unexpected InterruptedException while waiting for "
        + "next fragment in "
        + "CorbaMessageMediatorImpl.resumeOptimizedReadProcessing." )
    INTERNAL resumeOptimizedReadThreadInterrupted( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.SEVERE, id=119 )
    @Message( "Not allowed to get the integer value for an undefined "
        + "CorbaRequestId." )
    INTERNAL undefinedCorbaRequestIdNotAllowed(  ) ;
    
    @Log( level=LogLevel.WARNING, id=120 )
    @Message( "Illegal call to getKey in CacheTable: this instance has no "
        + "reverse map" )
    INTERNAL getKeyInvalidInCacheTable(  ) ;
    
    @Log( level=LogLevel.WARNING, id=121 )
    @Message( "TimerManager not initialized: error in constructing TypeCodeImpl" )
    INTERNAL timerManagerNotInitialized(  ) ;
    
    @Log( level=LogLevel.WARNING, id=122 )
    @Message( "TimingPoints instance is null in TypeCodeImpl constructor" )
    INTERNAL timingPointsAreNull(  ) ;
    
    @Log( level=LogLevel.SEVERE, id=123 )
    @Message( "Error in connection event handler caused event loss: may result "
        + "in client-side deadlock" )
    INTERNAL lostConnectionEvent(  ) ;
    
    @Log( level=LogLevel.WARNING, id=124 )
    @Message( "SharedCDRContactInfoImpl does not support SocketInfo calls" )
    INTERNAL undefinedSocketinfoOperation(  ) ;
    
    @Log( level=LogLevel.WARNING, id=125 )
    @Message( "Duplicate request ids in response waiting room: "
        + "over wrote old one: {0},  with new one: {1}" )
    INTERNAL duplicateRequestIdsInResponseWaitingRoom( String arg0, String arg1 ) ;
    
    @Log( level=LogLevel.FINE, id=132 )
    @Message( "Exception occurred in reader thread" )
    INTERNAL exceptionInReaderThread( @Chain Throwable thr ) ;
    
    @Log( level=LogLevel.FINE, id=133 )
    @Message( "Exception occurred in listener thread" )
    INTERNAL exceptionInListenerThread( @Chain Throwable thr ) ;
    
    @Log( level=LogLevel.WARNING, id=134 )
    @Message( "Exception occurred in handleRequest for a Request message" )
    INTERNAL exceptionInHandleRequestForRequest( @Chain Throwable thr  ) ;
    
    @Log( level=LogLevel.WARNING, id=135 )
    @Message( "Exception occurred in handleRequest for a LocateRequest message" )
    INTERNAL exceptionInHandleRequestForLocateRequest( @Chain Throwable thr ) ;
    
    @Log( level=LogLevel.WARNING, id=136 )
    @Message( "Could not set ORBData.orbInitializers")
    INTERNAL couldNotSetOrbInitializer(@Chain Exception exc);

    @Log( id=137 )
    @Message( "Connection {0} not null in createMessageMediator" )
    INTERNAL connectionNotNullInCreateMessageMediator(
        Connection connection);

    @Log( level=LogLevel.FINE, id=138 )
    @Message( "Old typeId {0} is not the same as the new typeId {1} in "
        + "setEffectiveTargetIOR" )
    INTERNAL changedTypeIdOnSetEffectiveTargetIOR( String oldTypeId,
        String newTypeId );

    @Log( level=LogLevel.WARNING, id=1 )
    @Message( "Data read past end of chunk without closing the chunk" )
    MARSHAL chunkOverflow(  ) ;
    
    @Log( level=LogLevel.WARNING, id=2 )
    @Message( "Grow buffer strategy called underflow handler" )
    MARSHAL unexpectedEof(  ) ;
    
    @Log( level=LogLevel.WARNING, id=3 )
    @Message( "Error in reading marshalled object" )
    MARSHAL readObjectException(  ) ;
    
    @Log( level=LogLevel.WARNING, id=4 )
    @Message( "Character not IOS Latin-1 compliant in marshalling" )
    MARSHAL characterOutofrange(  ) ;
    
    @Log( level=LogLevel.WARNING, id=5 )
    @Message( "Exception thrown during result() on ServerRequest" )
    @CS( CSValue.MAYBE )
    MARSHAL dsiResultException( Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=6 )
    @Message( "grow() called on IIOPInputStream" )
    MARSHAL iiopinputstreamGrow(  ) ;
    
    @Log( level=LogLevel.FINE, id=7 )
    @Message( "Underflow in BufferManagerReadStream after last fragment in "
        + "message" )
    MARSHAL endOfStream(  ) ;
    
    @Log( level=LogLevel.WARNING, id=8 )
    @Message( "Invalid ObjectKey in request header" )
    MARSHAL invalidObjectKey() ;

    @Log( level=LogLevel.WARNING, id=8 )
    @Message( "Invalid ObjectKey in request header" )
    MARSHAL invalidObjectKey( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=9 )
    @Message( "Unable to locate value class for repository ID {0} "
        + "because codebase URL {1} is malformed" )
    @CS( CSValue.MAYBE )
    MARSHAL malformedUrl( @Chain MalformedURLException exc,
        String arg0, String arg1 ) ;
    
    @Log( level=LogLevel.WARNING, id=10 )
    @Message( "Error from readValue on ValueHandler in CDRInputStream" )
    @CS( CSValue.MAYBE )
    MARSHAL valuehandlerReadError( @Chain Error err ) ;
    
    @Log( level=LogLevel.WARNING, id=11 )
    @Message( "Exception from readValue on ValueHandler in CDRInputStream" )
    @CS( CSValue.MAYBE )
    MARSHAL valuehandlerReadException( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=12 )
    @Message( "Bad kind in isCustomType in CDRInputStream" )
    MARSHAL badKind( BadKind bk ) ;
    
    @Log( level=LogLevel.WARNING, id=13 )
    @Message( "Could not find class {0} in CDRInputStream.readClass" )
    @CS( CSValue.MAYBE )
    MARSHAL cnfeReadClass( @Chain ClassNotFoundException exc, String arg0 ) ;
    
    @Log( level=LogLevel.WARNING, id=14 )
    @Message( "Bad repository ID indirection at index {0}" )
    @CS( CSValue.MAYBE )
    MARSHAL badRepIdIndirection( int index ) ;
    
    @Log( level=LogLevel.WARNING, id=15 )
    @Message( "Bad codebase string indirection at index {0}" )
    @CS( CSValue.MAYBE )
    MARSHAL badCodebaseIndirection( int index ) ;
    
    String unknownCodeSet = "Unknown code set {0} specified by client ORB as a "
        + "negotiated code set" ;

    @Log( level=LogLevel.WARNING, id=16 )
    @Message( unknownCodeSet )
    MARSHAL unknownCodeset( int arg0 ) ;

    @Log( level=LogLevel.WARNING, id=16 )
    @Message( unknownCodeSet )
    MARSHAL unknownCodeset( OSFCodeSetRegistry.Entry arg0 ) ;
    
    @Log( level=LogLevel.WARNING, id=17 )
    @Message( "Attempt to marshal wide character or string data in GIOP 1.0" )
    @CS( CSValue.MAYBE )
    MARSHAL wcharDataInGiop10(  ) ;
    
    @Log( level=LogLevel.WARNING, id=18 )
    @Message( "String or wstring with a negative length {0}" )
    @CS( CSValue.MAYBE )
    MARSHAL negativeStringLength( int arg0 ) ;
    
    @Log( level=LogLevel.WARNING, id=19 )
    @Message( "CDRInputStream.read_value(null) called, but no repository ID "
        + "information on the wire" )
        @CS( CSValue.MAYBE )
    MARSHAL expectedTypeNullAndNoRepId(  ) ;
    
    @Log( level=LogLevel.WARNING, id=20 )
    @Message( "CDRInputStream.read_value() called, but no repository ID "
        + "information on the wire" )
    MARSHAL readValueAndNoRepId(  ) ;
    
    @Log( level=LogLevel.WARNING, id=22 )
    @Message( "Received end tag {0}, which is less than the expected value {1}" )
    @CS( CSValue.MAYBE )
    MARSHAL unexpectedEnclosingValuetype( int endTag, int expected ) ;
    
    @Log( level=LogLevel.WARNING, id=23 )
    @Message( "Read non-negative end tag {0} at offset {1} (end tags should "
        + "always be negative)" )
    @CS( CSValue.MAYBE )
    MARSHAL positiveEndTag( int endTag, int offset ) ;
    
    @Log( level=LogLevel.WARNING, id=24 )
    @Message( "Out call descriptor is missing" )
    @CS( CSValue.MAYBE )
    MARSHAL nullOutCall(  ) ;
    
    @Log( level=LogLevel.WARNING, id=25 )
    @Message( "write_Object called with a local object" )
    @CS( CSValue.MAYBE )
    MARSHAL writeLocalObject(  ) ;
    
    @Log( level=LogLevel.WARNING, id=26 )
    @Message( "Tried to insert non-ObjectImpl {0} into an Any via insert_Object" )
    @CS( CSValue.MAYBE )
    MARSHAL badInsertobjParam( String name ) ;
    
    @Log( level=LogLevel.WARNING, id=27 )
    @Message( "Codebase present in RMI-IIOP stream format version 1 optional "
        + "data valuetype header" )
    @CS( CSValue.MAYBE )
    MARSHAL customWrapperWithCodebase(  ) ;
    
    @Log( level=LogLevel.WARNING, id=28 )
    @Message( "Indirection present in RMI-IIOP stream format version 2 optional "
        + "data valuetype header" )
    @CS( CSValue.MAYBE )
    MARSHAL customWrapperIndirection(  ) ;
    
    @Log( level=LogLevel.WARNING, id=29 )
    @Message( "0 or more than one repository ID found reading the optional data "
        + "valuetype header" )
    @CS( CSValue.MAYBE )
    MARSHAL customWrapperNotSingleRepid(  ) ;
    
    @Log( level=LogLevel.WARNING, id=30 )
    @Message( "Bad valuetag {0} found while reading repository IDs" )
    @CS( CSValue.MAYBE )
    MARSHAL badValueTag( String arg0 ) ;
    
    @Log( level=LogLevel.WARNING, id=31 )
    @Message( "Bad typecode found for custom valuetype" )
    @CS( CSValue.MAYBE )
    MARSHAL badTypecodeForCustomValue( @Chain BadKind bk ) ;
    
    @Log( level=LogLevel.WARNING, id=32 )
    @Message( "An error occurred using reflection to invoke IDL Helper "
        + "write method" )
    @CS( CSValue.MAYBE )
    MARSHAL errorInvokingHelperWrite( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=33 )
    @Message( "A bad digit was found while marshalling an IDL fixed type" )
    @CS( CSValue.MAYBE )
    MARSHAL badDigitInFixed(  ) ;
    
    @Log( level=LogLevel.WARNING, id=34 )
    @Message( "Referenced type of indirect type not marshaled" )
    @CS( CSValue.MAYBE )
    MARSHAL refTypeIndirType(  ) ;
    
    @Log( level=LogLevel.WARNING, id=35 )
    @Message( "Request message reserved bytes has invalid length" )
    @CS(CSValue.MAYBE)
    MARSHAL badReservedLength(  ) ;
    
    @Log( level=LogLevel.WARNING, id=36 )
    @Message( "A null object is not allowed here" )
    MARSHAL nullNotAllowed(  ) ;
    
    @Log( level=LogLevel.WARNING, id=38 )
    @Message( "Error in typecode union discriminator" )
    MARSHAL unionDiscriminatorError(  ) ;
    
    @Log( level=LogLevel.WARNING, id=39 )
    @Message( "Cannot marshal a native TypeCode" )
    MARSHAL cannotMarshalNative(  ) ;
    
    @Log( level=LogLevel.WARNING, id=40 )
    @Message( "Cannot marshal an invalid TypeCode kind" )
    MARSHAL cannotMarshalBadTckind(  ) ;
    
    @Log( level=LogLevel.WARNING, id=41 )
    @Message( "Invalid indirection value {0} (>-4): probable stream corruption" )
    MARSHAL invalidIndirection( int arg0 ) ;
    
    @Log( level=LogLevel.FINE, id=42 )
    @Message( "No type found at indirection {0}: probably stream corruption" )
    MARSHAL indirectionNotFound( int arg0 ) ;
    
    @Log( level=LogLevel.WARNING, id=43 )
    @Message( "Recursive TypeCode not supported by InputStream subtype" )
    MARSHAL recursiveTypecodeError(  ) ;
    
    @Log( level=LogLevel.WARNING, id=44 )
    @Message( "TypeCode is of wrong kind to be simple" )
    MARSHAL invalidSimpleTypecode(  ) ;
    
    @Log( level=LogLevel.WARNING, id=45 )
    @Message( "TypeCode is of wrong kind to be complex" )
    MARSHAL invalidComplexTypecode(  ) ;
    
    @Log( level=LogLevel.WARNING, id=46 )
    @Message( "Cannot marshal typecode of invalid kind" )
    MARSHAL invalidTypecodeKindMarshal(  ) ;
    
    @Log( level=LogLevel.WARNING, id=47 )
    @Message( "Default union branch not expected" )
    MARSHAL unexpectedUnionDefault(  ) ;
    
    @Log( level=LogLevel.WARNING, id=48 )
    @Message( "Illegal discriminator type in union" )
    MARSHAL illegalUnionDiscriminatorType(  ) ;
    
    @Log( level=LogLevel.WARNING, id=49 )
    @Message( "Could not skip over {0} bytes at offset {1}" )
    @CS( CSValue.MAYBE )
    MARSHAL couldNotSkipBytes( int len, int offset ) ;
    
    @Log( level=LogLevel.WARNING, id=50 )
    @Message( "Incorrect chunk length {0} at offset {1}" )
    MARSHAL badChunkLength( int len, int offset ) ;
    
    @Log( level=LogLevel.WARNING, id=51 )
    @Message( "Unable to locate array of repository IDs from indirection {0}" )
    MARSHAL unableToLocateRepIdArray( int indir ) ;
    
    @Log( level=LogLevel.WARNING, id=52 )
    @Message( "Fixed of length {0} in buffer of length {1}" )
    MARSHAL badFixed( short flen, int blen ) ;
    
    @Log( level=LogLevel.WARNING, id=53 )
    @Message( "Failed to load stub for {0} with class {1}" )
    MARSHAL readObjectLoadClassFailure( String arg0, String arg1 ) ;
    
    @Log( level=LogLevel.WARNING, id=54 )
    @Message( "Could not instantiate Helper class {0}" )
    MARSHAL couldNotInstantiateHelper( @Chain InstantiationException exc,
        Class<?> arg0 ) ;
    
    @Log( level=LogLevel.WARNING, id=55 )
    @Message( "Bad ObjectAdapterId for TOA" )
    MARSHAL badToaOaid(  ) ;
    
    @Log( level=LogLevel.WARNING, id=56 )
    @Message( "Could not invoke helper read method for helper {0}" )
    MARSHAL couldNotInvokeHelperReadMethod( @Chain Exception exc,
         Class<?> arg0 ) ;
    
    @Log( level=LogLevel.WARNING, id=57 )
    @Message( "Could not load class {0}" )
    @CS( CSValue.MAYBE )
    MARSHAL couldNotFindClass( String clasName ) ;
    
    @Log( level=LogLevel.FINE, id=58 )
    @Message( "Error in arguments(NVList) for DSI ServerRequest" )
    MARSHAL badArgumentsNvlist( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.FINE, id=59 )
    @Message( "Could not create stub" )
    MARSHAL stubCreateError( @Chain Throwable thr ) ;
    
    @Log( level=LogLevel.WARNING, id=60 )
    @Message( "Java serialization exception during {0} operation" )
    MARSHAL javaSerializationException( String arg0 ) ;
    
    @Log( level=LogLevel.WARNING, id=61 )
    @Message( "Could not read exception from UEInfoServiceContext" )
    @CS( CSValue.MAYBE )
    MARSHAL couldNotReadInfo( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=62 )
    @Message( "Could not find enum class {0} while reading an enum" )
    MARSHAL enumClassNotFound( @Chain ClassNotFoundException ex,
        String arg0 ) ;
    
    String proxyClassNotFound = "Could not find Proxy class for "
        + "interfaces {0} while reading a proxy" ;

    @Log( level=LogLevel.WARNING, id=63 )
    @Message( proxyClassNotFound )
    MARSHAL proxyClassNotFound( @Chain ClassNotFoundException exc,
        List<String> interfaceNames ) ;

    @Log( level=LogLevel.WARNING, id=63 )
    @Message( proxyClassNotFound )
    MARSHAL proxyClassNotFound( @Chain ClassNotFoundException exc, 
        String interfaceNames ) ;
    
    @Log( level=LogLevel.WARNING, id=64 )
    @Message( "Unable to load proxy class for interfaces {0} because "
        + "codebase URL {1} is malformed" )
    MARSHAL malformedProxyUrl( @Chain MalformedURLException exc,
        List<String> interfaceNames, String url ) ;
    
    @Log( level=LogLevel.WARNING, id=65 )
    @Message( "Unable to create proxy instance because the interface list "
        + "specified is empty" )
    MARSHAL emptyProxyInterfaceList( @Chain NullPointerException exc ) ;
    
    @Log( level=LogLevel.WARNING, id=66 )
    @Message( "Unable to create proxy instance because "
        + "Proxy.getProxyClass(..) called with violated restrictions." )
    MARSHAL proxyWithIllegalArgs( @Chain IllegalArgumentException exc ) ;
    
    @Log( level=LogLevel.WARNING, id=67 )
    @Message( "An instance of class {0} could not be marshalled: the class is "
        + "not an instance of java.io.Serializable" )
    MARSHAL objectNotSerializable( String arg0 ) ;

    @Log( level=LogLevel.WARNING, id=68 ) 
    @Message( "Could not unmarshal enum with cls {0}, value {1} using EnumDesc" )
    MARSHAL couldNotUnmarshalEnum( String cls, Serializable value ) ;
    
    @Log( level=LogLevel.WARNING, id=69 ) 
    @Message( "Expected String value for enum class {0}, but got value {1}" )
    MARSHAL enumValueNotString( Class cls, Serializable value ) ;
    
    @Log( level=LogLevel.FINE, id=1 )
    @Message( "feature not implemented" )
    NO_IMPLEMENT genericNoImpl(  ) ;
    
    @Log( level=LogLevel.FINE, id=2 )
    @Message( "IDL request context is not implemented" )
    NO_IMPLEMENT contextNotImplemented(  ) ;
    
    @Log( level=LogLevel.FINE, id=3 )
    @Message( "getInterface() is not implemented" )
    NO_IMPLEMENT getinterfaceNotImplemented(  ) ;
    
    @Log( level=LogLevel.FINE, id=4 )
    @Message( "send deferred is not implemented" )
    NO_IMPLEMENT sendDeferredNotimplemented(  ) ;
    
    @Log( level=LogLevel.FINE, id=5 )
    @Message( "IDL type long double is not supported in Java" )
    @CS( CSValue.MAYBE )
    NO_IMPLEMENT longDoubleNotImplemented(  ) ;
    
    @Log( level=LogLevel.WARNING, id=6 )
    @Message( "getAcceptedSocket is not supported for a CorbaAcceptorLazyImpl" )
    NO_IMPLEMENT notSupportedOnLazyAcceptor(  ) ;
    
    @Log( level=LogLevel.WARNING, id=1 )
    @Message( "No server request dispatcher found when dispatching request "
        + "to object adapter" )
    OBJ_ADAPTER noServerScInDispatch(  ) ;
    
    @Log( level=LogLevel.WARNING, id=2 )
    @Message( "Error in connecting servant to ORB" )
    OBJ_ADAPTER orbConnectError( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.FINE, id=3 )
    @Message( "StubAdapter.getDelegate failed to activate a Servant" )
    OBJ_ADAPTER adapterInactiveInActivation(  ) ;
    
    @Log( level=LogLevel.WARNING, id=1 )
    @Message( "Locate response indicated that the object was unknown" )
    OBJECT_NOT_EXIST locateUnknownObject(  ) ;

    int BAD_SERVER_ID = CorbaExtension.self.getMinorCode(
        ORBUtilSystemException.class, "badServerId" ) ;

    @Log( level=LogLevel.FINE, id=2 )
    @Message( "The server ID in the target object key does not match the "
        + "server key expected by the server" )
    OBJECT_NOT_EXIST badServerId(  ) ;
    
    @Log( level=LogLevel.WARNING, id=3 )
    @Message( "No skeleton found in the server that matches the target "
        + "object key" )
    OBJECT_NOT_EXIST badSkeleton(  ) ;
    
    @Log( level=LogLevel.WARNING, id=4 )
    @Message( "Servant not found" )
    OBJECT_NOT_EXIST servantNotFound(  ) ;
    
    @Log( level=LogLevel.WARNING, id=5 )
    @Message( "No object adapter factory" )
    OBJECT_NOT_EXIST noObjectAdapterFactory(  ) ;
    
    @Log( level=LogLevel.WARNING, id=6 )
    @Message( "Bad adapter ID" )
    OBJECT_NOT_EXIST badAdapterId(  ) ;
    
    @Log( level=LogLevel.WARNING, id=7 )
    @Message( "Dynamic Any was destroyed: all operations are invalid" )
    OBJECT_NOT_EXIST dynAnyDestroyed(  ) ;
    
    @Log( level=LogLevel.FINE, id=1 )
    @Message( "Sleep was interrupted in TCP timeouts" )
    TIMEOUT interruptedExceptionInTimeout(  ) ;
    
    @Log( level=LogLevel.FINE, id=1 )
    @Message( "Request cancelled by exception" )
    TRANSIENT requestCanceled( @Chain Throwable thr ) ;
    
    @Log( level=LogLevel.WARNING, id=1 )
    @Message( "Unknown user exception while unmarshalling" )
    @CS( CSValue.MAYBE )
    UNKNOWN unknownCorbaExc(  ) ;
    
    @Log( level=LogLevel.WARNING, id=2 )
    @Message( "Unknown user exception thrown by the server - "
        + "exception: {0}; message: {1}" )
    @CS( CSValue.MAYBE )
    UNKNOWN runtimeexception( @Chain Throwable exc, String arg0, String arg1 ) ;
    
    @Log( level=LogLevel.WARNING, id=3 )
    @Message( "Error while marshalling SystemException after DSI-based "
        + "invocation" )
    @CS(CSValue.MAYBE)
    UNKNOWN unknownDsiSysex(  ) ;
    
    @Log( level=LogLevel.WARNING, id=4 )
    @Message( "Error while unmarshalling SystemException" )
    @CS(CSValue.MAYBE)
    UNKNOWN unknownSysex( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=5 )
    @Message( "InterfaceDef object of wrong type returned by server" )
    @CS(CSValue.MAYBE)
    UNKNOWN wrongInterfaceDef(  ) ;
    
    @Log( level=LogLevel.WARNING, id=6 )
    @Message( "org.omg.CORBA._InterfaceDefStub class not available" )
    UNKNOWN noInterfaceDefStub( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.FINE, id=7 )
    @Message( "UnknownException in dispatch" )
    @CS( CSValue.MAYBE )
    UNKNOWN unknownExceptionInDispatch( @Chain Exception exc ) ;

    @Log( level=LogLevel.FINE, id=8 )
    @Message( "MARSHAL exception while trying to get value factory")
    UNKNOWN marshalErrorInReadIDLValue(@Chain MARSHAL marshal);

    @Message( "Exception in post_init in VirtualAddressAgentImpl")
    @Log( level=LogLevel.FINE, id=9 )
    UNKNOWN vaaErrorInPostInit( @Chain Exception exc);

    @Message( "Could not set tcpNoDelay on socket")
    @Log( level=LogLevel.FINE, id=10 )
    UNKNOWN couldNotSetTcpNoDelay(@Chain Exception e);

    @Message( "Exception in purgeCalls")
    @Log( level=LogLevel.FINE, id=11 )
    UNKNOWN exceptionInPurgeCalls(@Chain Exception ex);

    @Message( "Exception while closing socket")
    @Log( level=LogLevel.FINE, id=12 )
    UNKNOWN exceptionOnClose(IOException e);

    @Message( "Interrupted while waiting in writeLock on OPENING state")
    @Log( level=LogLevel.FINE, id=13 )
    UNKNOWN openingWaitInterrupted(InterruptedException ie);

    @Message( "Interrupted while waiting in writeLock on ESTABLISHED state")
    @Log( level=LogLevel.FINE, id=14 )
    UNKNOWN establishedWaitInterrupted(InterruptedException ie);

    @Message( "Exception while creating Typecode from native representation")
    @Log( level=LogLevel.FINE, id=15 )
    UNKNOWN exceptionOnCreatingTypecode(@Chain Exception e);

    @Message( "Exception in Typecode equals")
    @Log( level=LogLevel.FINE, id=16 )
    UNKNOWN exceptionInTypecodeEquals(@Chain Exception e);

    @Message( "Remarshal exception in bootstrap resolver")
    @Log( level=LogLevel.FINE, id=17 )
    UNKNOWN bootstrapRemarshalException(@Chain RemarshalException e);

    @Message( "Could not initialize initial GIS")
    @Log( level=LogLevel.FINE, id=18 )
    UNKNOWN couldNotInitializeInitialGIS(@Chain Exception exc);

    @Log( level=LogLevel.FINE, id=19 )
    @Message( "No CSIv2TaggedComponentHandler available from initial references")
    UNKNOWN noCSIV2Handler(@Chain InvalidName e);

    @Log( level=LogLevel.FINE, id=20 )
    @Message( "Error in ServerGroupManager")
    UNKNOWN serverGroupManagerException(@Chain Exception e);

    @Log( level=LogLevel.FINE, id=21 )
    @Message( "ThreadStateValidator {0} threw an exception")
    UNKNOWN threadStateValidatorException(Runnable run, @Chain Throwable thr);

    @Log( level=LogLevel.FINE, id=22 )
    @Message( "Bad GIOP 1.1 cancel request received")
    UNKNOWN bad1_1CancelRequestReceived();

    @Log( level=LogLevel.FINE, id=23 )
    @Message( "Cancel request with id 0 received")
    UNKNOWN cancelRequestWithId0();

    @Log( level=LogLevel.FINE, id=24 )
    @Message( "Bad cancel request received")
    UNKNOWN badCancelRequest();

    @Log( level=LogLevel.FINE, id=25 )
    @Message( "Class {0} not found in remote codebase {1}")
    UNKNOWN classNotFoundInCodebase(String className, String remoteCodebase);

    @Log( level=LogLevel.FINE, id=26 )
    @Message( "Class {0} not found in JDKClassLoader")
    UNKNOWN classNotFoundInJDKClassLoader(String className,
        @Chain ClassNotFoundException e);
}
