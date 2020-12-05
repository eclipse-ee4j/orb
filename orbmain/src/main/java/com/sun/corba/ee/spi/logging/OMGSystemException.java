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

import com.sun.corba.ee.spi.ior.ObjectAdapterId;
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

import org.omg.CORBA.BAD_CONTEXT;
import org.omg.CORBA.BAD_INV_ORDER;
import org.omg.CORBA.BAD_OPERATION;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.BAD_TYPECODE;
import org.omg.CORBA.DATA_CONVERSION;
import org.omg.CORBA.IMP_LIMIT;
import org.omg.CORBA.INITIALIZE;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.INTF_REPOS;
import org.omg.CORBA.INV_OBJREF;
import org.omg.CORBA.INV_POLICY;
import org.omg.CORBA.MARSHAL;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.NO_RESOURCES;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CORBA.OBJ_ADAPTER;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;
import org.omg.CORBA.TRANSIENT;
import org.omg.CORBA.UNKNOWN;

@ExceptionWrapper( idPrefix="IOP" )
@ORBException( omgException=true, group=0 )
public interface OMGSystemException {
    OMGSystemException self = WrapperGenerator.makeWrapper( 
        OMGSystemException.class, CorbaExtension.self ) ;

    @Log( level=LogLevel.WARNING, id=1 )
    @Message( "IDL context not found" )
    BAD_CONTEXT idlContextNotFound(  ) ;
    
    @Log( level=LogLevel.WARNING, id=2 )
    @Message( "No matching IDL context property" )
    BAD_CONTEXT noMatchingIdlContext(  ) ;
    
    @Log( level=LogLevel.WARNING, id=1 )
    @Message( "Dependency exists in IFR preventing destruction of this object" )
    BAD_INV_ORDER depPreventDestruction(  ) ;
    
    @Log( level=LogLevel.WARNING, id=2 )
    @Message( "Attempt to destroy indestructible objects in IFR" )
    BAD_INV_ORDER destroyIndestructible(  ) ;
    
    @Log( level=LogLevel.WARNING, id=3 )
    @Message( "Operation would deadlock" )
    BAD_INV_ORDER shutdownWaitForCompletionDeadlock(  ) ;
    
    @Log( level=LogLevel.WARNING, id=4 )
    @Message( "ORB has shutdown" )
    BAD_INV_ORDER badOperationAfterShutdown(  ) ;
    
    @Log( level=LogLevel.WARNING, id=5 )
    @Message( "Attempt to invoke send or invoke operation of the same "
        + "Request object more than once " )
    BAD_INV_ORDER badInvoke(  ) ;
    
    @Log( level=LogLevel.WARNING, id=6 )
    @Message( "Attempt to set a servant manager after one has already been set" )
    BAD_INV_ORDER badSetServantManager(  ) ;
    
    @Log( level=LogLevel.WARNING, id=7 )
    @Message( "ServerRequest::arguments called more than once or after a call "
        + "to ServerRequest::set_exception" )
    BAD_INV_ORDER badArgumentsCall(  ) ;
    
    @Log( level=LogLevel.WARNING, id=8 )
    @Message( "ServerRequest::ctx called more than once or before "
        + "ServerRequest::arguments or after ServerRequest::ctx, "
        + "ServerRequest::set_result or ServerRequest::set_exception" )
    BAD_INV_ORDER badCtxCall(  ) ;
    
    @Log( level=LogLevel.WARNING, id=9 )
    @Message( "ServerRequest::set_result called more than once or before "
        + "ServerRequest::arguments or after ServerRequest::set_result or "
        + "ServerRequest::set_exception" )
    BAD_INV_ORDER badResultCall(  ) ;
    
    @Log( level=LogLevel.WARNING, id=10 )
    @Message( "Attempt to send a DII request after it was sent previously" )
    BAD_INV_ORDER badSend(  ) ;
    
    @Log( level=LogLevel.WARNING, id=11 )
    @Message( "Attempt to poll a DII request or to retrieve its result "
        + "before the request was sent" )
    BAD_INV_ORDER badPollBefore(  ) ;
    
    @Log( level=LogLevel.WARNING, id=12 )
    @Message( "Attempt to poll a DII request or to retrieve its result after "
        + "the result was retrieved previously" )
    BAD_INV_ORDER badPollAfter(  ) ;
    
    @Log( level=LogLevel.WARNING, id=13 )
    @Message( "Attempt to poll a synchronous DII request or to retrieve results "
        + "from a synchronous DII request" )
    BAD_INV_ORDER badPollSync(  ) ;
    
    @Log( level=LogLevel.FINE, id=14 )
    @Message( "Invalid call to forward_reference() when reply status is not "
        + "LOCATION_FORWARD" )
    BAD_INV_ORDER invalidPiCall1(  ) ;
    
    @Log( level=LogLevel.FINE, id=14 )
    @Message( "Cannot access this attribute or method at this point" )
    BAD_INV_ORDER invalidPiCall2(  ) ;
    
    @Log( level=LogLevel.FINE, id=14 )
    @Message( "Cannot call set_slot from within an ORBInitializer" )
    BAD_INV_ORDER invalidPiCall3(  ) ;
    
    @Log( level=LogLevel.FINE, id=14 )
    @Message( "Cannot call get_slot from within an ORBInitializer" )
    BAD_INV_ORDER invalidPiCall4(  ) ;
    
    @Log( level=LogLevel.FINE, id=15 )
    @Message( "Service context add failed in portable interceptor because "
        + "a service context with id {0} already exists" )
    BAD_INV_ORDER serviceContextAddFailed( int id ) ;
    
    @Log( level=LogLevel.WARNING, id=16 )
    @Message( "Registration of PolicyFactory failed because a factory already "
        + "exists for the given PolicyType {0}" )
    BAD_INV_ORDER policyFactoryRegFailed( int type ) ;
    
    @Log( level=LogLevel.WARNING, id=17 )
    @Message( "POA cannot create POAs while undergoing destruction" )
    BAD_INV_ORDER createPoaDestroy(  ) ;
    
    @Log( level=LogLevel.WARNING, id=18 )
    @Message( "Attempt to reassign priority" )
    BAD_INV_ORDER priorityReassign(  ) ;
    
    @Log( level=LogLevel.WARNING, id=19 )
    @Message( "An OTS/XA integration xa_start() call returned XAER_OUTSIDE" )
    BAD_INV_ORDER xaStartOutsize(  ) ;
    
    @Log( level=LogLevel.WARNING, id=20 )
    @Message( "An OTS/XA integration xa_ call returned XAER_PROTO" )
    BAD_INV_ORDER xaStartProto(  ) ;
    
    @Log( level=LogLevel.WARNING, id=1 )
    @Message( "ServantManager returned wrong servant type" )
    BAD_OPERATION badServantManagerType(  ) ;
    
    @Log( level=LogLevel.WARNING, id=2 )
    @Message( "Operation or attribute not known to target object " )
    BAD_OPERATION operationUnknownToTarget(  ) ;
    
    @Log( level=LogLevel.WARNING, id=1 )
    @Message( "Failure to register, unregister or lookup value factory" )
    BAD_PARAM unableRegisterValueFactory(  ) ;
    
    @Log( level=LogLevel.WARNING, id=2 )
    @Message( "RID already defined in IFR" )
    BAD_PARAM ridAlreadyDefined(  ) ;
    
    @Log( level=LogLevel.WARNING, id=3 )
    @Message( "Name already used in the context in IFR " )
    BAD_PARAM nameUsedIfr(  ) ;
    
    @Log( level=LogLevel.WARNING, id=4 )
    @Message( "Target is not a valid container" )
    BAD_PARAM targetNotContainer(  ) ;
    
    @Log( level=LogLevel.WARNING, id=5 )
    @Message( "Name clash in inherited context" )
    BAD_PARAM nameClash(  ) ;

    int NOT_SERIALIZABLE = CorbaExtension.self.getMinorCode(
        OMGSystemException.class, "notSerializable" ) ;

    @Log( level=LogLevel.WARNING, id=6 )
    @Message( "Class {0} is not Serializable" )
    @CS( CSValue.MAYBE )
    BAD_PARAM notSerializable( String arg0 ) ;
    
    @Log( level=LogLevel.FINE, id=7 )
    @Message( "string_to_object conversion failed due to bad scheme name {0}" )
    BAD_PARAM soBadSchemeName( String arg0 ) ;
    
    String soBadAddress = "string_to_object conversion failed due to "
        + "bad address in name {0}" ;

    @Log( level=LogLevel.FINE, id=8 )
    @Message( soBadAddress )
    BAD_PARAM soBadAddress( @Chain Throwable exc, String arg0 ) ;

    @Log( level=LogLevel.FINE, id=8 )
    @Message( soBadAddress )
    BAD_PARAM soBadAddress( String arg0 ) ;
    
    @Log( level=LogLevel.FINE, id=9 )
    @Message( "string_to_object conversion failed due to bad schema specific "
        + "part in name {0}" )
    BAD_PARAM soBadSchemaSpecific( @Chain Exception exc, String arg0 ) ;
    
    @Log( level=LogLevel.WARNING, id=10 )
    @Message( "string_to_object conversion failed due to non specific reason" )
    BAD_PARAM soNonSpecific(  ) ;
    
    @Log( level=LogLevel.WARNING, id=11 )
    @Message( "Attempt to derive abstract interface from non-abstract base "
        + "interface in the Interface Repository" )
    BAD_PARAM irDeriveAbsIntBase(  ) ;
    
    @Log( level=LogLevel.WARNING, id=12 )
    @Message( "Attempt to let a ValueDef support more than one non-abstract "
        + "interface in the Interface Repository" )
    BAD_PARAM irValueSupport(  ) ;
    
    @Log( level=LogLevel.WARNING, id=13 )
    @Message( "Attempt to use an incomplete TypeCode as a parameter" )
    BAD_PARAM incompleteTypecode(  ) ;
    
    @Log( level=LogLevel.WARNING, id=14 )
    @Message( "Invalid object id passed to POA::create_reference_by_id " )
    BAD_PARAM invalidObjectId(  ) ;
    
    @Log( level=LogLevel.WARNING, id=15 )
    @Message( "Bad name argument in TypeCode operation" )
    BAD_PARAM typecodeBadName(  ) ;
    
    @Log( level=LogLevel.WARNING, id=16 )
    @Message( "Bad RepositoryId argument in TypeCode operation" )
    BAD_PARAM typecodeBadRepid(  ) ;
    
    @Log( level=LogLevel.WARNING, id=17 )
    @Message( "Invalid member name in TypeCode operation " )
    BAD_PARAM typecodeInvMember(  ) ;
    
    @Log( level=LogLevel.WARNING, id=18 )
    @Message( "Duplicate label value in create_union_tc " )
    BAD_PARAM tcUnionDupLabel(  ) ;
    
    @Log( level=LogLevel.WARNING, id=19 )
    @Message( "Incompatible TypeCode of label and discriminator in "
        + "create_union_tc " )
    BAD_PARAM tcUnionIncompatible(  ) ;
    
    @Log( level=LogLevel.WARNING, id=20 )
    @Message( "Supplied discriminator type illegitimate in create_union_tc " )
    BAD_PARAM tcUnionBadDisc(  ) ;
    
    @Log( level=LogLevel.WARNING, id=21 )
    @Message( "Any passed to ServerRequest::set_exception does not contain "
        + "an exception " )
    BAD_PARAM setExceptionBadAny(  ) ;
    
    @Log( level=LogLevel.WARNING, id=22 )
    @Message( "Unlisted user exception passed to ServerRequest::set_exception " )
    BAD_PARAM setExceptionUnlisted(  ) ;
    
    @Log( level=LogLevel.WARNING, id=23 )
    @Message( "wchar transmission code set not in service context" )
    BAD_PARAM noClientWcharCodesetCtx(  ) ;
    
    @Log( level=LogLevel.WARNING, id=24 )
    @Message( "Service context is not in OMG-defined range" )
    BAD_PARAM illegalServiceContext(  ) ;
    
    @Log( level=LogLevel.WARNING, id=25 )
    @Message( "Enum value out of range" )
    BAD_PARAM enumOutOfRange(  ) ;
    
    @Log( level=LogLevel.FINE, id=26 )
    @Message( "Invalid service context Id in portable interceptor" )
    BAD_PARAM invalidServiceContextId(  ) ;
    
    @Log( level=LogLevel.WARNING, id=27 )
    @Message( "Attempt to call register_initial_reference with a null Object" )
    BAD_PARAM rirWithNullObject(  ) ;
    
    @Log( level=LogLevel.FINE, id=28 )
    @Message( "Invalid component Id {0} in portable interceptor" )
    BAD_PARAM invalidComponentId( int arg0 ) ;

    int INVALID_PROFILE_ID = CorbaExtension.self.getMinorCode(
        OMGSystemException.class, "invalidProfileId" ) ;

    @Log( level=LogLevel.WARNING, id=29 )
    @Message( "Profile ID does not define a known profile or it is impossible "
        + "to add components to that profile" )
    BAD_PARAM invalidProfileId(  ) ;
    
    @Log( level=LogLevel.WARNING, id=30 )
    @Message( "Two or more Policy objects with the same PolicyType value "
        + "supplied to Object::set_policy_overrides or "
        + "PolicyManager::set_policy_overrides" )
    BAD_PARAM policyTypeDuplicate(  ) ;
    
    @Log( level=LogLevel.WARNING, id=31 )
    @Message( "Attempt to define a oneway operation with non-void result, "
        + "out or inout parameters or user exceptions" )
    BAD_PARAM badOnewayDefinition(  ) ;
    
    @Log( level=LogLevel.WARNING, id=32 )
    @Message( "DII asked to create request for an implicit operation" )
    BAD_PARAM diiForImplicitOperation(  ) ;
    
    @Log( level=LogLevel.WARNING, id=33 )
    @Message( "An OTS/XA integration xa_ call returned XAER_INVAL" )
    BAD_PARAM xaCallInval(  ) ;
    
    @Log( level=LogLevel.WARNING, id=34 )
    @Message( "Union branch modifier method called with "
        + "bad case label discriminator" )
    BAD_PARAM unionBadDiscriminator(  ) ;
    
    @Log( level=LogLevel.WARNING, id=35 )
    @Message( "Illegal IDL context property name" )
    BAD_PARAM ctxIllegalPropertyName(  ) ;
    
    @Log( level=LogLevel.WARNING, id=36 )
    @Message( "Illegal IDL property search string" )
    BAD_PARAM ctxIllegalSearchString(  ) ;
    
    @Log( level=LogLevel.WARNING, id=37 )
    @Message( "Illegal IDL context name" )
    BAD_PARAM ctxIllegalName(  ) ;
    
    @Log( level=LogLevel.WARNING, id=38 )
    @Message( "Non-empty IDL context" )
    BAD_PARAM ctxNonEmpty(  ) ;
    
    @Log( level=LogLevel.WARNING, id=39 )
    @Message( "Unsupported RMI/IDL custom value type stream format {0}" )
    BAD_PARAM invalidStreamFormatVersion( int arg0 ) ;
    
    @Log( level=LogLevel.WARNING, id=40 )
    @Message( "ORB output stream does not support ValueOutputStream interface" )
    @CS( CSValue.MAYBE )
    BAD_PARAM notAValueoutputstream(  ) ;
    
    @Log( level=LogLevel.WARNING, id=41 )
    @Message( "ORB input stream does not support ValueInputStream interface" )
    BAD_PARAM notAValueinputstream(  ) ;
    
    @Log( level=LogLevel.WARNING, id=1 )
    @Message( "Attempt to marshal incomplete TypeCode" )
    BAD_TYPECODE marshallIncompleteTypecode(  ) ;
    
    @Log( level=LogLevel.WARNING, id=2 )
    @Message( "Member type code illegitimate in TypeCode operation" )
    BAD_TYPECODE badMemberTypecode(  ) ;
    
    @Log( level=LogLevel.WARNING, id=3 )
    @Message( "Illegal parameter type" )
    BAD_TYPECODE illegalParameter(  ) ;
    
    @Log( level=LogLevel.WARNING, id=1 )
    @Message( "Character does not map to negotiated transmission code set" )
    DATA_CONVERSION charNotInCodeset( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=2 )
    @Message( "Failure of PriorityMapping object" )
    DATA_CONVERSION priorityMapFailre(  ) ;
    
    @Log( level=LogLevel.WARNING, id=1 )
    @Message( "Unable to use any profile in IOR" )
    IMP_LIMIT noUsableProfile(  ) ;
    
    @Log( level=LogLevel.WARNING, id=1 )
    @Message( "Priority range too restricted for ORB" )
    INITIALIZE priorityRangeRestrict(  ) ;
    
    @Log( level=LogLevel.WARNING, id=1 )
    @Message( "wchar Code Set support not specified" )
    INV_OBJREF noServerWcharCodesetCmp(  ) ;
    
    @Log( level=LogLevel.WARNING, id=2 )
    @Message( "Codeset component required for type using wchar or wstring data" )
    INV_OBJREF codesetComponentRequired(  ) ;
    
    @Log( level=LogLevel.WARNING, id=1 )
    @Message( "Unable to reconcile IOR specified policy with "
        + "effective policy override" )
    INV_POLICY iorPolicyReconcileError(  ) ;
    
    @Log( level=LogLevel.WARNING, id=2 )
    @Message( "Invalid PolicyType" )
    INV_POLICY policyUnknown(  ) ;
    
    @Log( level=LogLevel.WARNING, id=3 )
    @Message( "No PolicyFactory has been registered for the given PolicyType" )
    INV_POLICY noPolicyFactory(  ) ;
    
    @Log( level=LogLevel.WARNING, id=1 )
    @Message( "An OTS/XA integration xa_ call returned XAER_RMERR" )
    INTERNAL xaRmerr(  ) ;
    
    @Log( level=LogLevel.WARNING, id=2 )
    @Message( "An OTS/XA integration xa_ call returned XAER_RMFAIL" )
    INTERNAL xaRmfail(  ) ;
    
    @Log( level=LogLevel.WARNING, id=1 )
    @Message( "Interface Repository not available" )
    INTF_REPOS noIr(  ) ;
    
    @Log( level=LogLevel.WARNING, id=2 )
    @Message( "No entry for requested interface in Interface Repository" )
    INTF_REPOS noInterfaceInIr(  ) ;
    
    String unableLocateValueFactory = "Unable to locate value factory" ;

    @Log( level=LogLevel.FINE, id=1 )
    @Message( unableLocateValueFactory )
    @CS( CSValue.MAYBE )
    MARSHAL unableLocateValueFactory(  ) ;

    @Log( level=LogLevel.FINE, id=1 )
    @Message( unableLocateValueFactory )
    @CS( CSValue.MAYBE )
    MARSHAL unableLocateValueFactory( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=2 )
    @Message( "ServerRequest::set_result called before ServerRequest::ctx "
        + "when the operation IDL contains a context clause " )
    MARSHAL setResultBeforeCtx(  ) ;
    
    @Log( level=LogLevel.WARNING, id=3 )
    @Message( "NVList passed to ServerRequest::arguments does not describe "
        + "all parameters passed by client" )
    MARSHAL badNvlist(  ) ;
    
    @Log( level=LogLevel.WARNING, id=4 )
    @Message( "Attempt to marshal Local object" )
    MARSHAL notAnObjectImpl( @Chain BAD_PARAM exception ) ;
    
    @Log( level=LogLevel.WARNING, id=5 )
    @Message( "wchar or wstring data erroneously sent by client over "
        + "GIOP 1.0 connection " )
    MARSHAL wcharBadGiopVersionSent(  ) ;
    
    @Log( level=LogLevel.WARNING, id=6 )
    @Message( "wchar or wstring data erroneously returned by server over "
        + "GIOP 1.0 connection " )
    MARSHAL wcharBadGiopVersionReturned(  ) ;
    
    @Log( level=LogLevel.WARNING, id=7 )
    @Message( "Unsupported RMI/IDL custom value type stream format" )
    @CS( CSValue.MAYBE )
    MARSHAL unsupportedFormatVersion(  ) ;

    int RMIIIOP_OPTIONAL_DATA_INCOMPATIBLE =
        CorbaExtension.self.getMinorCode( OMGSystemException.class,
        "rmiiiopOptionalDataIncompatible1" ) ;

    @Log( level=LogLevel.FINE, id=8 )
    @Message( "No optional data available" )
    MARSHAL rmiiiopOptionalDataIncompatible1(  ) ;
    
    @Log( level=LogLevel.FINE, id=8 )
    @Message( "Not enough space left in current chunk" )
    MARSHAL rmiiiopOptionalDataIncompatible2(  ) ;
    
    @Log( level=LogLevel.FINE, id=8 )
    @Message( "Not enough optional data available" )
    MARSHAL rmiiiopOptionalDataIncompatible3(  ) ;

    String missingLocalValueImpl = "Missing local value implementation" ;

    @Log( level=LogLevel.WARNING, id=1 )
    @Message( missingLocalValueImpl )
    @CS( CSValue.MAYBE )
    NO_IMPLEMENT missingLocalValueImpl( @Chain Throwable exc ) ;

    @Log( level=LogLevel.WARNING, id=1 )
    @Message( missingLocalValueImpl )
    @CS( CSValue.MAYBE )
    NO_IMPLEMENT missingLocalValueImpl() ;
    
    @Log( level=LogLevel.WARNING, id=2 )
    @Message( "Incompatible value implementation version" )
    @CS( CSValue.MAYBE )
    NO_IMPLEMENT incompatibleValueImpl( @Chain Throwable thr ) ;
    
    @Log( level=LogLevel.WARNING, id=3 )
    @Message( "Unable to use any profile in IOR" )
    NO_IMPLEMENT noUsableProfile2(  ) ;
    
    @Log( level=LogLevel.WARNING, id=4 )
    @Message( "Attempt to use DII on Local object" )
    NO_IMPLEMENT diiLocalObject(  ) ;
    
    @Log( level=LogLevel.WARNING, id=5 )
    @Message( "Biomolecular Sequence Analysis iterator cannot be reset" )
    NO_IMPLEMENT bioReset(  ) ;
    
    @Log( level=LogLevel.WARNING, id=6 )
    @Message( "Biomolecular Sequence Analysis metadata is not available as XML" )
    NO_IMPLEMENT bioMetaNotAvailable(  ) ;
    
    @Log( level=LogLevel.WARNING, id=7 )
    @Message( "Genomic Maps iterator cannot be reset" )
    NO_IMPLEMENT bioGenomicNoIterator(  ) ;
    
    @Log( level=LogLevel.FINE, id=1 )
    @Message( "The portable Java bindings do not support arguments()" )
    NO_RESOURCES piOperationNotSupported1(  ) ;
    
    @Log( level=LogLevel.FINE, id=1 )
    @Message( "The portable Java bindings do not support exceptions()" )
    NO_RESOURCES piOperationNotSupported2(  ) ;
    
    @Log( level=LogLevel.FINE, id=1 )
    @Message( "The portable Java bindings do not support contexts()" )
    NO_RESOURCES piOperationNotSupported3(  ) ;
    
    @Log( level=LogLevel.FINE, id=1 )
    @Message( "The portable Java bindings do not support operation_context()" )
    NO_RESOURCES piOperationNotSupported4(  ) ;
    
    @Log( level=LogLevel.FINE, id=1 )
    @Message( "The portable Java bindings do not support result()" )
    NO_RESOURCES piOperationNotSupported5(  ) ;
    
    @Log( level=LogLevel.FINE, id=1 )
    @Message( "The object ID was never set" )
    NO_RESOURCES piOperationNotSupported6(  ) ;
    
    @Log( level=LogLevel.FINE, id=1 )
    @Message( "The ObjectKeyTemplate was never set" )
    NO_RESOURCES piOperationNotSupported7(  ) ;
    
    @Log( level=LogLevel.FINE, id=1 )
    @Message( "ServerRequest::arguments() was never called" )
    NO_RESOURCES piOperationNotSupported8(  ) ;
    
    @Log( level=LogLevel.WARNING, id=2 )
    @Message( "No connection for request's priority" )
    NO_RESOURCES noConnectionPriority(  ) ;
    
    @Log( level=LogLevel.WARNING, id=1 )
    @Message( "An OTS/XA integration xa_ call returned XAER_RB" )
    TRANSACTION_ROLLEDBACK xaRb(  ) ;
    
    @Log( level=LogLevel.WARNING, id=2 )
    @Message( "An OTS/XA integration xa_ call returned XAER_NOTA" )
    TRANSACTION_ROLLEDBACK xaNota(  ) ;
    
    @Log( level=LogLevel.WARNING, id=3 )
    @Message( "OTS/XA integration end() was called with success set to TRUE "
        + "while transaction rollback was deferred" )
    TRANSACTION_ROLLEDBACK xaEndTrueRollbackDeferred(  ) ;
    
    @Log( level=LogLevel.WARNING, id=1 )
    @Message( "Request discarded because of resource exhaustion in POA or "
        + "because POA is in DISCARDING state" )
    TRANSIENT poaRequestDiscard(  ) ;
    
    @Log( level=LogLevel.WARNING, id=2 )
    @Message( "No usable profile in IOR" )
    TRANSIENT noUsableProfile3(  ) ;
    
    @Log( level=LogLevel.WARNING, id=3 )
    @Message( "Request cancelled" )
    TRANSIENT requestCancelled(  ) ;
    
    @Log( level=LogLevel.WARNING, id=4 )
    @Message( "POA destroyed" )
    TRANSIENT poaDestroyed(  ) ;
    
    @Log( level=LogLevel.WARNING, id=1 )
    @Message( "Attempt to pass an unactivated (unregistered) value as an "
        + "object reference" )
    OBJECT_NOT_EXIST unregisteredValueAsObjref(  ) ;
    
    String noObjectAdaptor = "Failed to create or locate Object Adaptor" ;

    @Log( level=LogLevel.FINE, id=2 )
    @Message( noObjectAdaptor )
    OBJECT_NOT_EXIST noObjectAdaptor( @Chain Exception exc ) ;

    @Log( level=LogLevel.FINE, id=2 )
    @Message( noObjectAdaptor )
    OBJECT_NOT_EXIST noObjectAdaptor() ;
    
    @Log( level=LogLevel.WARNING, id=3 )
    @Message( "Biomolecular Sequence Analysis Service is no longer available" )
    OBJECT_NOT_EXIST bioNotAvailable(  ) ;
    
    @Log( level=LogLevel.WARNING, id=4 )
    @Message( "Object Adapter Inactive" )
    OBJECT_NOT_EXIST objectAdapterInactive(  ) ;
    
    String adapterActivatorException = "System exception in "
        + "POA::unknown_adapter for POA {0} with parent POA {1}" ;

    @Log( level=LogLevel.WARNING, id=1 )
    @Message( adapterActivatorException )
    OBJ_ADAPTER adapterActivatorException( @Chain Exception exc, String arg0,
        ObjectAdapterId arg1 ) ;

    @Log( level=LogLevel.WARNING, id=1 )
    @Message( adapterActivatorException )
    OBJ_ADAPTER adapterActivatorException( String arg0, ObjectAdapterId arg1 ) ;
    
    @Log( level=LogLevel.WARNING, id=2 )
    @Message( "Incorrect servant type returned by servant manager " )
    OBJ_ADAPTER badServantType(  ) ;
    
    @Log( level=LogLevel.WARNING, id=3 )
    @Message( "No default servant available [POA policy]" )
    OBJ_ADAPTER noDefaultServant(  ) ;
    
    @Log( level=LogLevel.WARNING, id=4 )
    @Message( "No servant manager available [POA Policy]" )
    OBJ_ADAPTER noServantManager(  ) ;
    
    @Log( level=LogLevel.WARNING, id=5 )
    @Message( "Violation of POA policy by ServantActivator::incarnate" )
    OBJ_ADAPTER badPolicyIncarnate(  ) ;
    
    @Log( level=LogLevel.WARNING, id=6 )
    @Message( "Exception in "
        + "PortableInterceptor::IORInterceptor.components_established" )
    OBJ_ADAPTER piExcCompEstablished(  ) ;
    
    @Log( level=LogLevel.FINE, id=7 )
    @Message( "Null servant returned by servant manager" )
    OBJ_ADAPTER nullServantReturned(  ) ;
    
    String unknownUserException =
        "Unlisted user exception received by client " ;

    @Log( level=LogLevel.FINE, id=1 )
    @Message( unknownUserException )
    @CS( CSValue.MAYBE )
    UNKNOWN unknownUserException(  ) ;

    @Log( level=LogLevel.FINE, id=1 )
    @Message( unknownUserException )
    @CS( CSValue.MAYBE )
    UNKNOWN unknownUserException( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=2 )
    @Message( "Non-standard System Exception not supported" )
    UNKNOWN unsupportedSystemException(  ) ;
    
    @Log( level=LogLevel.WARNING, id=3 )
    @Message( "An unknown user exception received by a portable interceptor" )
    UNKNOWN piUnknownUserException(  ) ;
}
