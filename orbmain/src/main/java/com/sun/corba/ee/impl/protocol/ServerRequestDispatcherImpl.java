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

package com.sun.corba.ee.impl.protocol;

import org.omg.CORBA.BAD_OPERATION;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.Any;

import org.omg.CORBA.portable.InvokeHandler;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.UnknownException;

import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.orb.ORBVersion;
import com.sun.corba.ee.spi.orb.ORBVersionFactory;
import com.sun.corba.ee.spi.orb.ObjectKeyCacheEntry;
import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.ior.ObjectKey;
import com.sun.corba.ee.spi.ior.ObjectKeyTemplate;
import com.sun.corba.ee.spi.ior.ObjectAdapterId;
import com.sun.corba.ee.spi.oa.ObjectAdapterFactory;
import com.sun.corba.ee.spi.oa.ObjectAdapter;
import com.sun.corba.ee.spi.oa.OAInvocationInfo;
import com.sun.corba.ee.spi.oa.OADestroyed;
import com.sun.corba.ee.spi.oa.NullServant;
import com.sun.corba.ee.spi.protocol.MessageMediator;
import com.sun.corba.ee.spi.protocol.ServerRequestDispatcher;
import com.sun.corba.ee.spi.protocol.ForwardException;
import com.sun.corba.ee.spi.protocol.RequestDispatcherRegistry;
import com.sun.corba.ee.spi.transport.Connection;
import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;

import com.sun.corba.ee.spi.servicecontext.ServiceContextDefaults;
import com.sun.corba.ee.spi.servicecontext.ServiceContext;
import com.sun.corba.ee.spi.servicecontext.ServiceContexts;
import com.sun.corba.ee.spi.servicecontext.UEInfoServiceContext;
import com.sun.corba.ee.spi.servicecontext.CodeSetServiceContext;
import com.sun.corba.ee.spi.servicecontext.SendingContextServiceContext;
import com.sun.corba.ee.spi.servicecontext.ORBVersionServiceContext;

import com.sun.corba.ee.impl.corba.ServerRequestImpl;
import com.sun.corba.ee.impl.encoding.CDROutputObject;
import com.sun.corba.ee.impl.encoding.MarshalInputStream;
import com.sun.corba.ee.impl.encoding.CodeSetComponentInfo;
import com.sun.corba.ee.impl.encoding.OSFCodeSetRegistry;
import com.sun.corba.ee.impl.misc.ORBUtility;
import com.sun.corba.ee.spi.logging.ORBUtilSystemException;
import com.sun.corba.ee.spi.logging.POASystemException;
import com.sun.corba.ee.spi.trace.Subcontract;
import org.glassfish.pfl.basic.logex.OperationTracer;
import org.glassfish.pfl.tf.spi.annotation.InfoMethod;

@Subcontract
public class ServerRequestDispatcherImpl implements ServerRequestDispatcher {
    private static final ORBUtilSystemException wrapper = ORBUtilSystemException.self;
    private static final POASystemException poaWrapper = POASystemException.self;

    protected ORB orb; // my ORB instance

    public ServerRequestDispatcherImpl(ORB orb) {
        this.orb = orb;
    }

    /**
     * Called from ORB.locate when a LocateRequest arrives. Result is not always absolutely correct: may indicate
     * OBJECT_HERE for non-existent objects, which is resolved on invocation. This "bug" is unavoidable, since in general
     * the object may be destroyed between a locate and a request. Note that this only checks that the appropriate
     * ObjectAdapter is available, not that the servant actually exists. Need to signal one of OBJECT_HERE, OBJECT_FORWARD,
     * OBJECT_NOT_EXIST.
     * 
     * @return Result is null if object is (possibly) implemented here, otherwise an IOR indicating objref to forward the
     * request to.
     * @exception org.omg.CORBA.OBJECT_NOT_EXIST is thrown if we know the object does not exist here, and we are not
     * forwarding.
     */
    @Subcontract
    @Override
    public IOR locate(ObjectKey okey) {
        ObjectKeyTemplate oktemp = okey.getTemplate();

        try {
            checkServerId(okey);
        } catch (ForwardException fex) {
            return fex.getIOR();
        }

        // Called only for its side-effect of throwing appropriate exceptions
        findObjectAdapter(oktemp);

        return null;
    }

    @InfoMethod
    private void generalMessage(String msg) {
    }

    @InfoMethod
    private void exceptionMessage(String msg, Throwable thr) {
    }

    @Subcontract
    @Override
    public void dispatch(MessageMediator request) {
        // to set the codebase information, if any transmitted; and also
        // appropriate ORB Version.
        consumeServiceContexts(request);

        // Now that we have the service contexts processed and the
        // correct ORBVersion set, we must finish initializing the
        // stream.
        ((MarshalInputStream) request.getInputObject()).performORBVersionSpecificInit();

        ObjectKeyCacheEntry entry = request.getObjectKeyCacheEntry();
        ObjectKey okey = entry.getObjectKey();

        // Check that this server is the right server
        try {
            checkServerId(okey);
        } catch (ForwardException fex) {
            operationAndId(request.getOperationName(), request.getRequestId());

            request.getProtocolHandler().createLocationForward(request, fex.getIOR(), null);
            return;
        }

        String operation = request.getOperationName();
        ObjectAdapter objectAdapter = entry.getObjectAdapter();

        try {
            byte[] objectId = okey.getId().getId();
            ObjectKeyTemplate oktemp = okey.getTemplate();

            if (objectAdapter == null) {
                // This call is expensive enough to cache, hence the creation
                // of the ObjectKeyCacheEntry mechanism.
                objectAdapter = findObjectAdapter(oktemp);
                entry.setObjectAdapter(objectAdapter);
            }

            java.lang.Object servant = getServantWithPI(request, objectAdapter, objectId, oktemp, operation);

            dispatchToServant(servant, request, objectId, objectAdapter);
        } catch (ForwardException ex) {
            generalMessage("Caught ForwardException");
            // Thrown by Portable Interceptors from InterceptorInvoker,
            // through Response constructor.
            request.getProtocolHandler().createLocationForward(request, ex.getIOR(), null);
        } catch (OADestroyed ex) {
            generalMessage("Caught OADestroyed");

            // Clear any cached ObjectAdapter in the ObjectKeyCacheEntry, because
            // it is no longer valid.
            entry.clearObjectAdapter();

            // DO NOT CALL releaseServant here!
            // The problem is that OADestroyed is only thrown by oa.enter, in
            // which case oa.exit should NOT be called, and neither should
            // the invocationInfo stack be popped.

            // Destroyed POAs can be recreated by normal adapter activation.
            // So just restart the dispatch.
            dispatch(request);
        } catch (RequestCanceledException ex) {
            generalMessage("Caught RequestCanceledException");

            // IDLJ generated non-tie based skeletons do not catch the
            // RequestCanceledException. Rethrow the exception, which will
            // cause the worker thread to unwind the dispatch and wait for
            // other requests.
            throw ex;
        } catch (UnknownException ex) {
            generalMessage("Caught UnknownException");

            // RMIC generated tie skeletons convert all Throwable exception
            // types (including RequestCanceledException, ThreadDeath)
            // thrown during reading fragments into UnknownException.
            // If RequestCanceledException was indeed raised,
            // then rethrow it, which will eventually cause the worker
            // thread to unstack the dispatch and wait for other requests.
            if (ex.originalEx instanceof RequestCanceledException) {
                throw (RequestCanceledException) ex.originalEx;
            }

            ServiceContexts contexts = ServiceContextDefaults.makeServiceContexts(orb);
            UEInfoServiceContext usc = ServiceContextDefaults.makeUEInfoServiceContext(ex.originalEx);

            contexts.put(usc);

            SystemException sysex = wrapper.unknownExceptionInDispatch(ex);
            request.getProtocolHandler().createSystemExceptionResponse(request, sysex, contexts);
        } catch (Throwable ex) {
            generalMessage("Caught other exception");
            request.getProtocolHandler().handleThrowableDuringServerDispatch(request, ex, CompletionStatus.COMPLETED_MAYBE);
        }
    }

    @Subcontract
    private void releaseServant(ObjectAdapter objectAdapter) {
        if (objectAdapter == null) {
            generalMessage("Null object adapter");
            return;
        }

        try {
            objectAdapter.returnServant();
        } finally {
            objectAdapter.exit();
            orb.popInvocationInfo();
        }
    }

    // Note that objectAdapter.enter() must be called before getServant.
    @Subcontract
    private java.lang.Object getServant(ObjectAdapter objectAdapter, byte[] objectId, String operation) throws OADestroyed {

        OAInvocationInfo info = objectAdapter.makeInvocationInfo(objectId);
        info.setOperation(operation);
        orb.pushInvocationInfo(info);
        objectAdapter.getInvocationServant(info);
        return info.getServantContainer();
    }

    @Subcontract
    protected java.lang.Object getServantWithPI(MessageMediator request, ObjectAdapter objectAdapter, byte[] objectId,
            ObjectKeyTemplate oktemp, String operation) throws OADestroyed {

        // Prepare Portable Interceptors for a new server request
        // and invoke receive_request_service_contexts. The starting
        // point may throw a SystemException or ForwardException.
        orb.getPIHandler().initializeServerPIInfo(request, objectAdapter, objectId, oktemp);
        orb.getPIHandler().invokeServerPIStartingPoint();

        objectAdapter.enter();

        // This must be set just after the enter so that exceptions thrown by
        // enter do not cause
        // the exception reply to pop the thread stack and do an extra oa.exit.
        if (request != null) {
            request.setExecuteReturnServantInResponseConstructor(true);
        }

        java.lang.Object servant = getServant(objectAdapter, objectId, operation);

        // Note: we do not know the MDI on a null servant.
        // We only end up in that situation if _non_existent called,
        // so that the following handleNullServant call does not throw an
        // exception.
        String mdi = "unknown";

        if (servant instanceof NullServant) {
            handleNullServant(operation, (NullServant) servant);
        } else {
            mdi = objectAdapter.getInterfaces(servant, objectId)[0];
        }

        orb.getPIHandler().setServerPIInfo(servant, mdi);

        if (((servant != null) && !(servant instanceof org.omg.CORBA.DynamicImplementation)
                && !(servant instanceof org.omg.PortableServer.DynamicImplementation))
                || (SpecialMethod.getSpecialMethod(operation) != null)) {
            orb.getPIHandler().invokeServerPIIntermediatePoint();
        }

        return servant;
    }

    @Subcontract
    protected void checkServerId(ObjectKey okey) {
        ObjectKeyTemplate oktemp = okey.getTemplate();
        int sId = oktemp.getServerId();
        int scid = oktemp.getSubcontractId();

        if (!orb.isLocalServerId(scid, sId)) {
            generalMessage("bad server ID");
            orb.handleBadServerId(okey);
        }
    }

    @Subcontract
    private ObjectAdapter findObjectAdapter(ObjectKeyTemplate oktemp) {
        RequestDispatcherRegistry scr = orb.getRequestDispatcherRegistry();
        int scid = oktemp.getSubcontractId();
        ObjectAdapterFactory oaf = scr.getObjectAdapterFactory(scid);
        if (oaf == null) {
            throw wrapper.noObjectAdapterFactory();
        }

        ObjectAdapterId oaid = oktemp.getObjectAdapterId();
        ObjectAdapter oa = oaf.find(oaid);

        if (oa == null) {
            throw wrapper.badAdapterId();
        }

        return oa;
    }

    /**
     * Always throws OBJECT_NOT_EXIST if operation is not a special method. If operation is _non_existent or _not_existent,
     * this will just return without performing any action, so that _non_existent can return false. Always throws
     * OBJECT_NOT_EXIST for any other special method. Update for issue 4385.
     * 
     * @param operation Name of method to get
     * @param nserv Servant throw associated {@link Exception} if not such method exists.
     */
    @Subcontract
    protected void handleNullServant(String operation, NullServant nserv) {
        SpecialMethod specialMethod = SpecialMethod.getSpecialMethod(operation);

        if ((specialMethod == null) || !specialMethod.isNonExistentMethod()) {
            throw nserv.getException();
        }
    }

    @InfoMethod
    private void objectInfo(String msg, Object obj) {
    }

    @Subcontract
    protected void consumeServiceContexts(MessageMediator request) {
        operationAndId(request.getOperationName(), request.getRequestId());

        ServiceContexts ctxts = request.getRequestServiceContexts();
        ServiceContext sc;
        GIOPVersion giopVersion = request.getGIOPVersion();

        // we cannot depend on this since for our local case, we do not send
        // in this service context. Can we rely on just the CodeSetServiceContext?
        // boolean rtSC = false; // Runtime ServiceContext

        boolean hasCodeSetContext = processCodeSetContext(request, ctxts);

        objectInfo("GIOP version", giopVersion);
        objectInfo("Has code set context?", hasCodeSetContext);

        sc = ctxts.get(SendingContextServiceContext.SERVICE_CONTEXT_ID);

        if (sc != null) {
            SendingContextServiceContext scsc = (SendingContextServiceContext) sc;
            IOR ior = scsc.getIOR();

            try {
                request.getConnection().setCodeBaseIOR(ior);
            } catch (ThreadDeath td) {
                throw td;
            } catch (Throwable t) {
                throw wrapper.badStringifiedIor(t);
            }
        }

        // the RTSC is sent only once during session establishment. We
        // need to find out if the CodeBaseRef is already set. If yes,
        // then also the rtSC flag needs to be set to true
        // this is not possible for the LocalCase since there is no
        // IIOPConnection for the LocalCase

        // used for a case where we have JDK 1.3 supporting 1.0 protocol,
        // but sending 2 service contexts, that is not normal as per
        // GIOP rules, based on above information, we figure out that we
        // are talking to the legacy ORB and set the ORB Version Accordingly.

        // this special case tell us that it is legacy SUN orb
        // and not a foreign one
        // rtSC is not available for localcase due to which this generic
        // path would fail if relying on rtSC
        // if (giopVersion.equals(GIOPVersion.V1_0) && hasCodeSetContext && rtSC)
        boolean isForeignORB = false;

        if (giopVersion.equals(GIOPVersion.V1_0) && hasCodeSetContext) {
            generalMessage("Old Sun ORB");
            orb.setORBVersion(ORBVersionFactory.getOLD());
            // System.out.println("setting legacy ORB version");
        } else {
            // If it didn't include our ORB version service context (below),
            // then it must be a foreign ORB.
            isForeignORB = true;
        }

        // try to get the ORBVersion sent as part of the ServiceContext
        // if any
        sc = ctxts.get(ORBVersionServiceContext.SERVICE_CONTEXT_ID);
        if (sc != null) {
            ORBVersionServiceContext ovsc = (ORBVersionServiceContext) sc;

            ORBVersion version = ovsc.getVersion();
            orb.setORBVersion(version);

            isForeignORB = false;
        }

        if (isForeignORB) {
            generalMessage("Foreign ORB");
            orb.setORBVersion(ORBVersionFactory.getFOREIGN());
        }
    }

    @Subcontract
    protected MessageMediator dispatchToServant(java.lang.Object servant, MessageMediator req, byte[] objectId,
            ObjectAdapter objectAdapter) {
        try {
            if (orb.operationTraceDebugFlag) {
                OperationTracer.enable();
            }

            OperationTracer.begin("Dispatch to servant");

            operationAndId(req.getOperationName(), req.getRequestId());
            objectInfo("Servant info", servant);

            MessageMediator response = null;

            String operation = req.getOperationName();

            SpecialMethod method = SpecialMethod.getSpecialMethod(operation);
            if (method != null) {
                objectInfo("Handling special method", method.getName());

                response = method.invoke(servant, req, objectId, objectAdapter);
                return response;
            }

            // Invoke on the servant using the portable DSI skeleton
            if (servant instanceof org.omg.CORBA.DynamicImplementation) {
                generalMessage("Handling old style DSI type servant");

                org.omg.CORBA.DynamicImplementation dynimpl = (org.omg.CORBA.DynamicImplementation) servant;
                ServerRequestImpl sreq = new ServerRequestImpl(req, orb);

                // Note: When/if dynimpl.invoke calls arguments() or
                // set_exception() then intermediate points are run.
                dynimpl.invoke(sreq);

                response = handleDynamicResult(sreq, req);
            } else if (servant instanceof org.omg.PortableServer.DynamicImplementation) {
                generalMessage("Handling POA DSI type servant");
                org.omg.PortableServer.DynamicImplementation dynimpl = (org.omg.PortableServer.DynamicImplementation) servant;
                ServerRequestImpl sreq = new ServerRequestImpl(req, orb);

                // Note: When/if dynimpl.invoke calls arguments() or
                // set_exception() then intermediate points are run.
                dynimpl.invoke(sreq);

                response = handleDynamicResult(sreq, req);
            } else {
                generalMessage("Handling invoke handler type servant");
                InvokeHandler invhandle = (InvokeHandler) servant;

                OutputStream stream = null;
                try {
                    stream = invhandle._invoke(operation, (org.omg.CORBA.portable.InputStream) req.getInputObject(), req);
                } catch (BAD_OPERATION e) {
                    wrapper.badOperationFromInvoke(e, operation);
                    throw e;
                }
                response = ((CDROutputObject) stream).getMessageMediator();
            }

            return response;
        } finally {
            OperationTracer.disable();
            OperationTracer.finish();
        }
    }

    @Subcontract
    protected MessageMediator handleDynamicResult(ServerRequestImpl sreq, MessageMediator req) {

        MessageMediator response = null;

        // Check if ServerRequestImpl.result() has been called
        Any excany = sreq.checkResultCalled();

        if (excany == null) { // normal return
            generalMessage("Handling normal result");

            // Marshal out/inout/return parameters into the ReplyMessage
            response = sendingReply(req);
            OutputStream os = (OutputStream) response.getOutputObject();
            sreq.marshalReplyParams(os);
        } else {
            generalMessage("Handling error");

            response = sendingReply(req, excany);
        }

        return response;
    }

    @Subcontract
    protected MessageMediator sendingReply(MessageMediator req) {
        ServiceContexts scs = ServiceContextDefaults.makeServiceContexts(orb);
        return req.getProtocolHandler().createResponse(req, scs);
    }

    /**
     * Must always be called, just after the servant's method returns. Creates the ReplyMessage header and puts in the
     * transaction context if necessary.
     * 
     * @param req original request
     * @param excany {@link Any} that contains an exception
     * @return Response that contains the exception
     */
    @Subcontract
    protected MessageMediator sendingReply(MessageMediator req, Any excany) {

        ServiceContexts scs = ServiceContextDefaults.makeServiceContexts(orb);
        operationAndId(req.getOperationName(), req.getRequestId());

        // Check if the servant set a SystemException or
        // UserException
        MessageMediator resp;
        String repId = null;
        try {
            repId = excany.type().id();
        } catch (org.omg.CORBA.TypeCodePackage.BadKind e) {
            throw wrapper.problemWithExceptionTypecode(e);
        }

        if (ORBUtility.isSystemException(repId)) {
            generalMessage("Handling system exception");

            // Get the exception object from the Any
            InputStream in = excany.create_input_stream();
            SystemException ex = ORBUtility.readSystemException(in);
            // Marshal the exception back
            resp = req.getProtocolHandler().createSystemExceptionResponse(req, ex, scs);
        } else {
            generalMessage("Handling user exception");

            resp = req.getProtocolHandler().createUserExceptionResponse(req, scs);
            OutputStream os = (OutputStream) resp.getOutputObject();
            excany.write_value(os);
        }

        return resp;
    }

    @InfoMethod
    private void codeSetServiceContextInfo(CodeSetServiceContext csctx) {
    }

    /**
     * Handles setting the connection's code sets if required. Returns true if the CodeSetContext was in the request, false
     * otherwise.
     * 
     * @param request request to process
     * @param contexts context to check
     * @return if the CodeSetContext was in the request
     */
    @Subcontract
    protected boolean processCodeSetContext(MessageMediator request, ServiceContexts contexts) {

        ServiceContext sc = contexts.get(CodeSetServiceContext.SERVICE_CONTEXT_ID);

        if (sc != null) {
            // Somehow a code set service context showed up in the local case.
            if (request.getConnection() == null) {
                return true;
            }

            // If it's GIOP 1.0, it shouldn't have this context at all. Our legacy
            // ORBs sent it and we need to know if it's here to make ORB versioning
            // decisions, but we don't use the contents.
            if (request.getGIOPVersion().equals(GIOPVersion.V1_0)) {
                return true;
            }

            CodeSetServiceContext cssc = (CodeSetServiceContext) sc;
            CodeSetComponentInfo.CodeSetContext csctx = cssc.getCodeSetContext();

            // The connection's codeSetContext is null until we've received a
            // request with a code set context with the negotiated code sets.
            Connection connection = request.getConnection();

            synchronized (connection) {
                if (connection.getCodeSetContext() == null) {
                    operationAndId(request.getOperationName(), request.getRequestId());
                    codeSetServiceContextInfo(cssc);

                    connection.setCodeSetContext(csctx);

                    // We had to read the method name using ISO 8859-1
                    // (which is the default in the CDRInputStream for
                    // char data), but now we may have a new char
                    // code set. If it isn't ISO8859-1, we must tell
                    // the CDR stream to null any converter references
                    // it has created so that it will reacquire
                    // the code sets again using the new info.
                    //
                    // This should probably compare with the stream's
                    // char code set rather than assuming it's ISO8859-1.
                    // (However, the operation name is almost certainly
                    // ISO8859-1 or ASCII.)
                    if (csctx.getCharCodeSet() != OSFCodeSetRegistry.ISO_8859_1.getNumber()) {
                        ((MarshalInputStream) request.getInputObject()).resetCodeSetConverters();
                    }
                }
            }
        }

        // If no code set information is ever sent from the client,
        // the server will use ISO8859-1 for char and throw an
        // exception for any wchar transmissions.
        //
        // In the local case, we use ORB provided streams for
        // marshaling and unmarshaling. Currently, they use
        // ISO8859-1 for char/string and UTF16 for wchar/wstring.
        return sc != null;
    }

    @InfoMethod
    private void operationAndId(String operation, int rid) {
    }
}

// End of file.
