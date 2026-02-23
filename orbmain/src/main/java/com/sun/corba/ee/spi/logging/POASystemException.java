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

import com.sun.corba.ee.spi.logex.corba.CS;
import com.sun.corba.ee.spi.logex.corba.CSValue;
import com.sun.corba.ee.spi.logex.corba.CorbaExtension ;
import com.sun.corba.ee.spi.logex.corba.ORBException ;

import org.glassfish.pfl.basic.logex.Chain;
import org.glassfish.pfl.basic.logex.ExceptionWrapper;
import org.glassfish.pfl.basic.logex.Log;
import org.glassfish.pfl.basic.logex.LogLevel;
import org.glassfish.pfl.basic.logex.Message;
import org.glassfish.pfl.basic.logex.WrapperGenerator;
import org.omg.CORBA.BAD_INV_ORDER;
import org.omg.CORBA.BAD_OPERATION;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.INITIALIZE;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CORBA.OBJ_ADAPTER;
import org.omg.CORBA.TRANSIENT;
import org.omg.CORBA.UNKNOWN;

@ExceptionWrapper( idPrefix="IOP" )
@ORBException( omgException=false, group=CorbaExtension.POAGroup )
public interface POASystemException {
    POASystemException self = WrapperGenerator.makeWrapper( 
        POASystemException.class, CorbaExtension.self ) ;
    
    @Log( level=LogLevel.WARNING, id=1 )
    @Message( "Servant Manager already set" )
    BAD_INV_ORDER servantManagerAlreadySet(  ) ;
    
    @Log( level=LogLevel.WARNING, id=2 )
    @Message( "Request to wait for POA destruction while servicing request "
        + "would deadlock" )
    BAD_INV_ORDER destroyDeadlock(  ) ;
    
    @Log( level=LogLevel.WARNING, id=1 )
    @Message( "Bad operation on servant ORB???" )
    BAD_OPERATION servantOrb(  ) ;
    
    @Log( level=LogLevel.WARNING, id=2 )
    @Message( "Bad Servant???" )
    BAD_OPERATION badServant(  ) ;
    
    @Log( level=LogLevel.WARNING, id=3 )
    @Message( "Illegal Forward Request???" )
    BAD_OPERATION illegalForwardRequest( @Chain Throwable thr ) ;
    
    @Log( level=LogLevel.WARNING, id=4 )
    @Message( "setDaemon() failed in creating destroy thread" )
    BAD_OPERATION couldNotSetDaemon( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=1 )
    @Message( "Bad transaction context" )
    BAD_PARAM badTransactionContext(  ) ;
    
    @Log( level=LogLevel.WARNING, id=2 )
    @Message( "Bad repository id" )
    BAD_PARAM badRepositoryId(  ) ;
    
    @Log( level=LogLevel.WARNING, id=3 )
    @Message( "Null repository id" )
    BAD_PARAM nullRepositoryId(  ) ;
    
    @Log( level=LogLevel.WARNING, id=1 )
    @Message( "invoke setup???" )
    INTERNAL invokesetup(  ) ;
    
    @Log( level=LogLevel.WARNING, id=2 )
    @Message( "bad local reply status???" )
    INTERNAL badLocalreplystatus(  ) ;
    
    @Log( level=LogLevel.WARNING, id=3 )
    @Message( "persistent serverport error???" )
    INTERNAL persistentServerportError() ;

    @Log( level=LogLevel.WARNING, id=4 )
    @Message( "servant dispatch???" )
    INTERNAL servantDispatch(  ) ;
    
    @Log( level=LogLevel.WARNING, id=5 )
    @Message( "wrong client request dispatcher???" )
    INTERNAL wrongClientsc(  ) ;
    
    @Log( level=LogLevel.WARNING, id=6 )
    @Message( "can't clone template???" )
    INTERNAL cantCloneTemplate(  ) ;
    
    @Log( level=LogLevel.WARNING, id=7 )
    @Message( "POACurrent stack is unbalanced" )
    INTERNAL poacurrentUnbalancedStack( @Chain Exception ex ) ;
    
    @Log( level=LogLevel.WARNING, id=8 )
    @Message( "Null field in POACurrent" )
    @CS( CSValue.MAYBE )
    INTERNAL poacurrentNullField(  ) ;
    
    @Log( level=LogLevel.WARNING, id=9 )
    @Message( "POA internalGetServant error" )
    INTERNAL poaInternalGetServantError(  ) ;
    
    @Log( level=LogLevel.WARNING, id=10 )
    @Message( "First Object Adapter name is {0}, should be RootPOA" )
    INTERNAL makeFactoryNotPoa( String arg0 ) ;
    
    @Log( level=LogLevel.WARNING, id=11 )
    @Message( "Duplicate ORB version service context" )
    INTERNAL duplicateOrbVersionSc(  ) ;
    
    @Log( level=LogLevel.WARNING, id=12 )
    @Message( "preinvoke clone error" )
    INTERNAL preinvokeCloneError(  ) ;
    
    @Log( level=LogLevel.WARNING, id=13 )
    @Message( "preinvoke POA destroyed" )
    INTERNAL preinvokePoaDestroyed(  ) ;
    
    @Log( level=LogLevel.WARNING, id=14 )
    @Message( "Bad dispatch policy for RETAIN policy in "
        + "POAPolicyMediatorFactory" )
    INTERNAL pmfCreateRetain(  ) ;
    
    @Log( level=LogLevel.WARNING, id=15 )
    @Message( "Bad dispatch policy for NON_RETAIN policy in "
        + "POAPolicyMediatorFactory" )
    INTERNAL pmfCreateNonRetain(  ) ;
    
    @Log( level=LogLevel.WARNING, id=16 )
    @Message( "Inconsistent policy in PolicyMediator" )
    INTERNAL policyMediatorBadPolicyInFactory(  ) ;
    
    @Log( level=LogLevel.WARNING, id=17 )
    @Message( "ObjectAlreadyActive in servantToId" )
    INTERNAL servantToIdOaa( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=18 )
    @Message( "ServantAlreadyActive in servantToId" )
    INTERNAL servantToIdSaa(@Chain Exception exc  ) ;
    
    @Log( level=LogLevel.WARNING, id=19 )
    @Message( "WrongPolicy in servantToId" )
    INTERNAL servantToIdWp( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=20 )
    @Message( "Can't resolve root POA" )
    INTERNAL cantResolveRootPoa( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=21 )
    @Message( "Call made to local client request dispatcher with non-local "
        + "servant" )
    INTERNAL servantMustBeLocal(  ) ;
    
    @Log( level=LogLevel.WARNING, id=22 )
    @Message( "IOR does not have any profiles" )
    INTERNAL noProfilesInIor(  ) ;
    
    @Log( level=LogLevel.WARNING, id=23 )
    @Message( "Tried to decrement AOMEntry counter that is already 0" )
    INTERNAL aomEntryDecZero(  ) ;
    
    @Log( level=LogLevel.WARNING, id=24 )
    @Message( "Tried to add a POA to an inactive POAManager" )
    @CS( CSValue.MAYBE )
    INTERNAL addPoaInactive(  ) ;
    
    @Log( level=LogLevel.WARNING, id=25 )
    @Message( "POA tried to make an illegal state transition" )
    INTERNAL illegalPoaStateTrans(  ) ;
    
    @Log( level=LogLevel.WARNING, id=26 )
    @Message( "Unexpected exception in POA {0}" )
    INTERNAL unexpectedException( @Chain Throwable thr, String arg0 ) ;
    
    @Log( level=LogLevel.WARNING, id=27 )
    @Message( "Exception occurred in RMORBInitializer.post_init" )
    INTERNAL rfmPostInitException( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=28 )
    @Message( "Exception occurred in ReferenceManagerConfigurator.configure" )
    INTERNAL rfmConfigureException( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=29 )
    @Message( "RFM was inactive on state change" )
    INTERNAL rfmManagerInactive( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.FINE, id=30 )
    @Message( "Suspend condition wait was unexpectedly interrupted" )
    INTERNAL rfmSuspendConditionWaitInterrupted(  ) ;

    @Log( level=LogLevel.SEVERE, id=31)
    @Message( "Some locks not released in find_POA: "
        + "readLocked={0}, writeLocked={1}, childReadLocked={2}")
    INTERNAL findPOALocksNotReleased(boolean readLocked, boolean writeLocked,
         boolean childReadLocked);
    
    @Log( level=LogLevel.WARNING, id=1 )
    @Message( "Single thread policy is not supported" )
    NO_IMPLEMENT singleThreadNotSupported(  ) ;
    
    @Log( level=LogLevel.WARNING, id=2 )
    @Message( "This method is not implemented" )
    NO_IMPLEMENT methodNotImplemented(  ) ;
    
    String poaLookupError = "Error in find_POA" ;

    @Log( level=LogLevel.WARNING, id=1 )
    @Message( poaLookupError )
    OBJ_ADAPTER poaLookupError( @Chain Exception exc  ) ;

    @Log( level=LogLevel.WARNING, id=1 )
    @Message( poaLookupError )
    OBJ_ADAPTER poaLookupError() ;
    
    @Log( level=LogLevel.FINE, id=2 )
    @Message( "POA is inactive" )
    OBJ_ADAPTER poaInactive(  ) ;
    
    @Log( level=LogLevel.WARNING, id=3 )
    @Message( "POA has no servant manager" )
    OBJ_ADAPTER poaNoServantManager(  ) ;
    
    @Log( level=LogLevel.WARNING, id=4 )
    @Message( "POA has no default servant" )
    OBJ_ADAPTER poaNoDefaultServant(  ) ;
    
    @Log( level=LogLevel.WARNING, id=5 )
    @Message( "POA servant is not unique" )
    OBJ_ADAPTER poaServantNotUnique(  ) ;
    
    @Log( level=LogLevel.WARNING, id=6 )
    @Message( "Bad policy in POA" )
    OBJ_ADAPTER poaWrongPolicy(  ) ;
    
    @Log( level=LogLevel.WARNING, id=7 )
    @Message( "Another error in find_POA" )
    OBJ_ADAPTER findpoaError(  ) ;
    
    @Log( level=LogLevel.WARNING, id=9 )
    @Message( "POA ServantActivator lookup failed" )
    OBJ_ADAPTER poaServantActivatorLookupFailed( @Chain Throwable exc ) ;
    
    @Log( level=LogLevel.WARNING, id=10 )
    @Message( "POA has bad servant manager" )
    OBJ_ADAPTER poaBadServantManager(  ) ;
    
    @Log( level=LogLevel.WARNING, id=11 )
    @Message( "POA ServantLocator lookup failed" )
    OBJ_ADAPTER poaServantLocatorLookupFailed(  ) ;
    
    @Log( level=LogLevel.WARNING, id=12 )
    @Message( "Unknown policy passed to POA" )
    OBJ_ADAPTER poaUnknownPolicy(  ) ;
    
    @Log( level=LogLevel.WARNING, id=13 )
    @Message( "POA not found" )
    OBJ_ADAPTER poaNotFound(  ) ;
    
    @Log( level=LogLevel.WARNING, id=14 )
    @Message( "Error in servant lookup" )
    OBJ_ADAPTER servantLookup(  ) ;
    
    @Log( level=LogLevel.WARNING, id=15 )
    @Message( "Error in local servant lookup" )
    OBJ_ADAPTER localServantLookup( @Chain Throwable thr ) ;
    
    @Log( level=LogLevel.WARNING, id=16 )
    @Message( "Bad type for servant manager" )
    OBJ_ADAPTER servantManagerBadType(  ) ;
    
    @Log( level=LogLevel.WARNING, id=17 )
    @Message( "Servant's _default_POA must be an instance of POAImpl" )
    OBJ_ADAPTER defaultPoaNotPoaimpl( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=18 )
    @Message( "Wrong POA policies for _this_object called outside of an "
        + "invocation context" )
    OBJ_ADAPTER wrongPoliciesForThisObject(  ) ;
    
    @Log( level=LogLevel.WARNING, id=19 )
    @Message( "ServantNotActive exception in _this_object" )
    OBJ_ADAPTER thisObjectServantNotActive( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=20 )
    @Message( "WrongPolicy exception in _this_object" )
    OBJ_ADAPTER thisObjectWrongPolicy( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.FINE, id=21 )
    @Message( "Operation called outside of invocation context" )
    OBJ_ADAPTER noContext( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=22 )
    @Message( "ServantActivator.incarnate() returned a null Servant" )
    OBJ_ADAPTER incarnateReturnedNull(  ) ;
    
    @Log( level=LogLevel.WARNING, id=23 )
    @Message( "ReferenceFactoryManager caught exception in "
        + "AdapterActivator.unknown_adaptor" )
    OBJ_ADAPTER rfmAdapterActivatorFailed( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=24 )
    @Message( "ReferenceFactoryManager is not active" )
    OBJ_ADAPTER rfmNotActive(  ) ;
    
    @Log( level=LogLevel.WARNING, id=25 )
    @Message( "ReferenceFactoryManager is already active" )
    OBJ_ADAPTER rfmAlreadyActive(  ) ;
    
    @Log( level=LogLevel.WARNING, id=26 )
    @Message( "ReferenceFactoryManager activate method failed" )
    OBJ_ADAPTER rfmActivateFailed( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=27 )
    @Message( "ReferenceFactoryManager restart called with a null argument" )
    OBJ_ADAPTER rfmNullArgRestart(  ) ;
    
    @Log( level=LogLevel.WARNING, id=28 )
    @Message( "ReferenceFactoryManager restart failed" )
    OBJ_ADAPTER rfmRestartFailed( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=29 )
    @Message( "ReferenceFactoryManager createReference failed" )
    OBJ_ADAPTER rfmCreateReferenceFailed( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=30 )
    @Message( "ReferenceFactoryManager destroy failed" )
    OBJ_ADAPTER rfmDestroyFailed( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=31 )
    @Message( "Illegal use of ReferenceFactoryManager parent POA detected" )
    OBJ_ADAPTER rfmIllegalParentPoaUsage(  ) ;
    
    @Log( level=LogLevel.WARNING, id=32 )
    @Message( "Illegal use of ReferenceFactoryManager parent POA detected" )
    OBJ_ADAPTER rfmIllegalPoaManagerUsage(  ) ;
    
    @Log( level=LogLevel.WARNING, id=33 )
    @Message( "Method {0} can only be called when RFM is suspended" )
    OBJ_ADAPTER rfmMethodRequiresSuspendedState( String arg0 ) ;

    int JTS_INIT_ERROR = CorbaExtension.self.getMinorCode(
        POASystemException.class, "jtsInitError" ) ;

    @Log( level=LogLevel.WARNING, id=1 )
    @Message( "JTS initialization error" )
    INITIALIZE jtsInitError(  ) ;
    
    @Log( level=LogLevel.WARNING, id=2 )
    @Message( "Persistent server ID is not set" )
    INITIALIZE persistentServeridNotSet(  ) ;
    
    @Log( level=LogLevel.WARNING, id=3 )
    @Message( "Persistent server port is not set" )
    INITIALIZE persistentServerportNotSet(  ) ;
    
    @Log( level=LogLevel.WARNING, id=4 )
    @Message( "Error in ORBD" )
    INITIALIZE orbdError(  ) ;
    
    @Log( level=LogLevel.WARNING, id=5 )
    @Message( "Error in bootstrap" )
    INITIALIZE bootstrapError(  ) ;
    
    @Log( level=LogLevel.FINE, id=1 )
    @Message( "POAManager is in discarding state" )
    TRANSIENT poaDiscarding(  ) ;
    
    @Log( level=LogLevel.FINE, id=2 )
    @Message( "Thread is active in another POAManager: holding on a different "
        + "POAManager might cause a deadlock" )
    TRANSIENT poaManagerMightDeadlock(  ) ;
    
    @Log( level=LogLevel.FINE, id=3 )
    @Message( "Thread is active in a POAManager: cannot perform operation in "
        + "RFM without risking a deadlock" )
    TRANSIENT rfmMightDeadlock(  ) ;
    
    @Log( level=LogLevel.WARNING, id=1 )
    @Message( "Error in OTS hook" )
    UNKNOWN otshookexception(  ) ;
    
    @Log( level=LogLevel.WARNING, id=2 )
    @Message( "Unknown server exception" )
    UNKNOWN unknownServerException(  ) ;
    
    @Log( level=LogLevel.WARNING, id=3 )
    @Message( "Unknown server application exception" )
    UNKNOWN unknownServerappException(  ) ;
    
    @Log( level=LogLevel.WARNING, id=4 )
    @Message( "Unknown local invocation error" )
    UNKNOWN unknownLocalinvocationError(  ) ;
    
    @Log( level=LogLevel.WARNING, id=1 )
    @Message( "AdapterActivator does not exist" )
    OBJECT_NOT_EXIST adapterActivatorNonexistent(  ) ;
    
    @Log( level=LogLevel.WARNING, id=2 )
    @Message( "AdapterActivator failed" )
    OBJECT_NOT_EXIST adapterActivatorFailed(  ) ;
    
    @Log( level=LogLevel.WARNING, id=3 )
    @Message( "Bad skeleton" )
    OBJECT_NOT_EXIST badSkeleton(  ) ;
    
    @Log( level=LogLevel.FINE, id=4 )
    @Message( "Null servant" )
    OBJECT_NOT_EXIST nullServant(  ) ;
    
    @Log( level=LogLevel.WARNING, id=5 )
    @Message( "POA has been destroyed" )
    OBJECT_NOT_EXIST adapterDestroyed(  ) ;

    @Log( level=LogLevel.FINE, id=1 )
    @Message( "waitGuard was interrupted" )
    UNKNOWN waitGuardInterrupted();
}
