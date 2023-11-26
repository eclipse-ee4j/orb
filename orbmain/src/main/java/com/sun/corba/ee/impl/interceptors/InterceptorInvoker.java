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

package com.sun.corba.ee.impl.interceptors;

import org.omg.CORBA.SystemException;
import org.omg.PortableInterceptor.LOCATION_FORWARD;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.IORInterceptor;
import org.omg.PortableInterceptor.IORInterceptor_3_0;
import org.omg.PortableInterceptor.ServerRequestInterceptor;
import org.omg.PortableInterceptor.ObjectReferenceTemplate;

import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.oa.ObjectAdapter;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.trace.TraceInterceptor;

import com.sun.corba.ee.spi.logging.InterceptorsSystemException;
import java.util.Arrays;
import org.glassfish.pfl.tf.spi.annotation.InfoMethod;

/**
 * Handles invocation of interceptors. Has specific knowledge of how to invoke IOR, ClientRequest, and ServerRequest
 * interceptors. Makes use of the InterceptorList to retrieve the list of interceptors to be invoked. Most methods in
 * this class are package scope so that they may only be called from the PIHandlerImpl.
 */
@TraceInterceptor
public class InterceptorInvoker {
    private ORB orb;

    private static final InterceptorsSystemException wrapper = InterceptorsSystemException.self;

    // The list of interceptors to be invoked
    private InterceptorList interceptorList;

    // True if interceptors are to be invoked, or false if not
    // Note: This is a global enable/disable flag, whereas the enable flag
    // in the RequestInfoStack in PIHandlerImpl is only for a particular Thread.
    private boolean enabled = false;

    // PICurrent variable.
    private PICurrent current;

    // NOTE: Be careful about adding additional attributes to this class.
    // Multiple threads may be calling methods on this invoker at the same
    // time.

    /**
     * Creates a new Interceptor Invoker. Constructor is package scope so only the ORB can create it. The invoker is
     * initially disabled, and must be explicitly enabled using setEnabled().
     */
    InterceptorInvoker(ORB orb, InterceptorList interceptorList, PICurrent piCurrent) {
        this.orb = orb;
        this.interceptorList = interceptorList;
        this.enabled = false;
        this.current = piCurrent;
    }

    /**
     * Enables or disables the interceptor invoker
     */
    synchronized void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    synchronized boolean getEnabled() {
        return this.enabled;
    }

    /*
     **********************************************************************
     * IOR Interceptor invocation
     **********************************************************************/

    /**
     * Called when a new POA is created.
     *
     * @param oa The Object Adapter associated with the IOR interceptor.
     */
    @TraceInterceptor
    void objectAdapterCreated(ObjectAdapter oa) {
        // If invocation is not yet enabled, don't do anything.
        if (getEnabled()) {
            // Create IORInfo object to pass to IORInterceptors:
            IORInfoImpl info = new IORInfoImpl(oa);

            // Call each IORInterceptor:
            IORInterceptor[] iorInterceptors = (IORInterceptor[]) interceptorList.getInterceptors(InterceptorList.INTERCEPTOR_TYPE_IOR);
            int size = iorInterceptors.length;

            // Implementation note:
            // This loop counts backwards for greater efficiency.
            // Benchmarks have shown that counting down is more efficient
            // than counting up in Java for loops, as a compare to zero is
            // faster than a subtract and compare to zero. In this case,
            // it doesn't really matter much, but it's simply a force of habit.

            for (int i = (size - 1); i >= 0; i--) {
                IORInterceptor interceptor = iorInterceptors[i];
                try {
                    interceptor.establish_components(info);
                } catch (Exception e) {
                    // as per PI spec (orbos/99-12-02 sec 7.2.1), if
                    // establish_components throws an exception, ignore it.
                    // But do log something for debugging.
                    wrapper.ignoredExceptionInEstablishComponents(e, oa);
                }
            }

            // Change the state so that only template operations are valid
            info.makeStateEstablished();

            for (int i = (size - 1); i >= 0; i--) {
                IORInterceptor interceptor = iorInterceptors[i];
                if (interceptor instanceof IORInterceptor_3_0) {
                    IORInterceptor_3_0 interceptor30 = (IORInterceptor_3_0) interceptor;
                    // Note that exceptions here are NOT ignored, as per the
                    // ORT spec (orbos/01-01-04)
                    try {
                        interceptor30.components_established(info);
                    } catch (Exception exc) {
                        wrapper.exceptionInComponentsEstablished(exc, oa);
                    }
                }
            }

            // Change the state so that no operations are valid,
            // in case a reference to info escapes this scope.
            // This also completes the actions associated with the
            // template interceptors on this POA.
            info.makeStateDone();
        }
    }

    @TraceInterceptor
    void adapterManagerStateChanged(int managerId, short newState) {
        if (getEnabled()) {
            IORInterceptor[] interceptors = (IORInterceptor[]) interceptorList.getInterceptors(InterceptorList.INTERCEPTOR_TYPE_IOR);
            int size = interceptors.length;

            for (int i = (size - 1); i >= 0; i--) {
                try {
                    IORInterceptor interceptor = interceptors[i];
                    if (interceptor instanceof IORInterceptor_3_0) {
                        IORInterceptor_3_0 interceptor30 = (IORInterceptor_3_0) interceptor;
                        interceptor30.adapter_manager_state_changed(managerId, newState);
                    }
                } catch (Exception exc) {
                    // No-op: ignore exception in this case
                    // But do log something for debugging.
                    wrapper.ignoredExceptionInAdapterManagerStateChanged(exc, managerId, newState);
                }
            }
        }
    }

    @TraceInterceptor
    void adapterStateChanged(ObjectReferenceTemplate[] templates, short newState) {

        if (getEnabled()) {
            IORInterceptor[] interceptors = (IORInterceptor[]) interceptorList.getInterceptors(InterceptorList.INTERCEPTOR_TYPE_IOR);
            int size = interceptors.length;

            for (int i = (size - 1); i >= 0; i--) {
                try {
                    IORInterceptor interceptor = interceptors[i];
                    if (interceptor instanceof IORInterceptor_3_0) {
                        IORInterceptor_3_0 interceptor30 = (IORInterceptor_3_0) interceptor;
                        interceptor30.adapter_state_changed(templates, newState);
                    }
                } catch (Exception exc) {
                    // No-op: ignore exception in this case
                    // But do log something for debugging.
                    wrapper.ignoredExceptionInAdapterStateChanged(exc, Arrays.asList(templates), newState);
                }
            }
        }
    }

    /*
     **********************************************************************
     * Client Interceptor invocation
     **********************************************************************/

    @InfoMethod
    private void invokeClientStartingCall(String name) {
    }

    @InfoMethod
    private void invokeClientStartingForwardRequest(String name) {
    }

    @InfoMethod
    private void invokeClientStartingSystemException(String name, SystemException exc) {
    }

    /**
     * Invokes either send_request, or send_poll, depending on the value of info.getStartingPointCall()
     */
    @TraceInterceptor
    void invokeClientInterceptorStartingPoint(ClientRequestInfoImpl info) {
        info.interceptorsEnabledForThisRequest = getEnabled();

        // If invocation is not yet enabled, don't do anything.
        if (info.interceptorsEnabledForThisRequest) {
            try {
                // Make a a fresh slot table available to TSC in case
                // interceptors need to make out calls.
                // Client's TSC is now RSC via RequestInfo.
                current.pushSlotTable();
                info.setPICurrentPushed(true);
                info.setCurrentExecutionPoint(ClientRequestInfoImpl.EXECUTION_POINT_STARTING);

                // Get all ClientRequestInterceptors:
                ClientRequestInterceptor[] clientInterceptors = (ClientRequestInterceptor[]) interceptorList
                        .getInterceptors(InterceptorList.INTERCEPTOR_TYPE_CLIENT);
                int size = clientInterceptors.length;

                // We will assume that all interceptors returned successfully,
                // and adjust the flowStackIndex to the appropriate value if
                // we later discover otherwise.
                int flowStackIndex = size;
                boolean continueProcessing = true;

                // This ORB only supports send_request. It does not implement
                // the CORBA messaging specification, so send_poll is not
                // needed.
                for (int i = 0; continueProcessing && (i < size); i++) {
                    ClientRequestInterceptor cri = clientInterceptors[i];

                    try {
                        invokeClientStartingCall(cri.name());
                        cri.send_request(info);
                    } catch (ForwardRequest e) {
                        invokeClientStartingForwardRequest(cri.name());

                        // as per PI spec (orbos/99-12-02 sec 5.2.1.), if
                        // interception point throws a ForwardRequest,
                        // no other Interceptors' send_request operations are
                        // called.
                        flowStackIndex = i;
                        info.setForwardRequest(e);
                        info.setEndingPointCall(ClientRequestInfoImpl.CALL_RECEIVE_OTHER);
                        info.setReplyStatus(LOCATION_FORWARD.value);

                        updateClientRequestDispatcherForward(info);

                        // For some reason, using break here causes the VM on
                        // NT to lose track of the value of flowStackIndex
                        // after exiting the for loop. I changed this to
                        // check a boolean value instead and it seems to work
                        // fine.
                        continueProcessing = false;
                    } catch (SystemException e) {
                        invokeClientStartingSystemException(cri.name(), e);

                        // as per PI spec (orbos/99-12-02 sec 5.2.1.), if
                        // interception point throws a SystemException,
                        // no other Interceptors' send_request operations are
                        // called.
                        flowStackIndex = i;
                        info.setEndingPointCall(ClientRequestInfoImpl.CALL_RECEIVE_EXCEPTION);
                        info.setReplyStatus(SYSTEM_EXCEPTION.value);
                        info.setException(e);

                        // For some reason, using break here causes the VM on
                        // NT to lose track of the value of flowStackIndex
                        // after exiting the for loop. I changed this to
                        // check a boolean value instead and it seems to
                        // work fine.
                        continueProcessing = false;
                    }
                }

                // Remember where we left off in the flow stack:
                info.setFlowStackIndex(flowStackIndex);
            } finally {
                // Make the SlotTable fresh for the next interception point.
                current.resetSlotTable();
            }
        } // end enabled check
    }

    private String getClientEndMethodName(int endingPointCall) {
        switch (endingPointCall) {
        case ClientRequestInfoImpl.CALL_RECEIVE_REPLY:
            return "receive_reply";
        case ClientRequestInfoImpl.CALL_RECEIVE_EXCEPTION:
            return "receive_exception";
        case ClientRequestInfoImpl.CALL_RECEIVE_OTHER:
            return "receive_other";
        }
        return "";
    }

    @InfoMethod
    private void invokeClientEndingCall(String name, String call) {
    }

    @InfoMethod
    private void invokeClientEndingForwardRequest(String name) {
    }

    @InfoMethod
    private void invokeClientEndingSystemException(String name, SystemException exc) {
    }

    /**
     * Invokes either receive_reply, receive_exception, or receive_other, depending on the value of
     * info.getEndingPointCall()
     */
    @TraceInterceptor
    void invokeClientInterceptorEndingPoint(ClientRequestInfoImpl info) {
        // If invocation is not yet enabled, don't do anything.
        if (info.interceptorsEnabledForThisRequest) {
            try {
                // NOTE: It is assumed someplace else prepared a
                // fresh TSC slot table.

                info.setCurrentExecutionPoint(ClientRequestInfoImpl.EXECUTION_POINT_ENDING);

                // Get all ClientRequestInterceptors:
                ClientRequestInterceptor[] clientInterceptors = (ClientRequestInterceptor[]) interceptorList
                        .getInterceptors(InterceptorList.INTERCEPTOR_TYPE_CLIENT);
                int flowStackIndex = info.getFlowStackIndex();

                // Determine whether we are calling receive_reply,
                // receive_exception, or receive_other:
                int endingPointCall = info.getEndingPointCall();

                // If we would be calling RECEIVE_REPLY, but this is a
                // one-way call, override this and call receive_other:
                if ((endingPointCall == ClientRequestInfoImpl.CALL_RECEIVE_REPLY) && info.getIsOneWay()) {
                    endingPointCall = ClientRequestInfoImpl.CALL_RECEIVE_OTHER;
                    info.setEndingPointCall(endingPointCall);
                }

                // Only step through the interceptors whose starting points
                // have successfully returned.
                // Unlike the previous loop, this one counts backwards for a
                // reason - we must execute these in the reverse order of the
                // starting points.
                for (int i = (flowStackIndex - 1); i >= 0; i--) {
                    ClientRequestInterceptor cri = clientInterceptors[i];

                    try {
                        invokeClientEndingCall(cri.name(), getClientEndMethodName(endingPointCall));

                        switch (endingPointCall) {
                        case ClientRequestInfoImpl.CALL_RECEIVE_REPLY:
                            cri.receive_reply(info);
                            break;
                        case ClientRequestInfoImpl.CALL_RECEIVE_EXCEPTION:
                            cri.receive_exception(info);
                            break;
                        case ClientRequestInfoImpl.CALL_RECEIVE_OTHER:
                            cri.receive_other(info);
                            break;
                        }
                    } catch (ForwardRequest e) {
                        invokeClientEndingForwardRequest(cri.name());

                        // as per PI spec (orbos/99-12-02 sec 5.2.1.), if
                        // interception point throws a ForwardException,
                        // ending point call changes to receive_other.
                        endingPointCall = ClientRequestInfoImpl.CALL_RECEIVE_OTHER;
                        info.setEndingPointCall(endingPointCall);
                        info.setReplyStatus(LOCATION_FORWARD.value);
                        info.setForwardRequest(e);
                        updateClientRequestDispatcherForward(info);
                    } catch (SystemException e) {
                        invokeClientEndingSystemException(cri.name(), e);

                        // as per PI spec (orbos/99-12-02 sec 5.2.1.), if
                        // interception point throws a SystemException,
                        // ending point call changes to receive_exception.
                        endingPointCall = ClientRequestInfoImpl.CALL_RECEIVE_EXCEPTION;
                        info.setEndingPointCall(endingPointCall);
                        info.setReplyStatus(SYSTEM_EXCEPTION.value);
                        info.setException(e);
                    }
                }
            } finally {
                // See doc for setPICurrentPushed as to why this is necessary.
                // Check info for null in case errors happen before initiate.
                if (info.isPICurrentPushed()) {
                    current.popSlotTable();
                    // After the pop, original client's TSC slot table
                    // remains avaiable via PICurrent.
                }
            }
        } // end enabled check
    }

    /*
     **********************************************************************
     * Server Interceptor invocation
     **********************************************************************/

    @InfoMethod
    private void invokeServerStartingCall(String name) {
    }

    @InfoMethod
    private void invokeServerStartingForwardRequest(String name) {
    }

    @InfoMethod
    private void invokeServerStartingSystemException(String name, SystemException exc) {
    }

    /**
     * Invokes receive_request_service_context interception points.
     */
    @TraceInterceptor
    void invokeServerInterceptorStartingPoint(ServerRequestInfoImpl info) {
        info.interceptorsEnabledForThisRequest = getEnabled();

        // If invocation is not yet enabled, don't do anything.
        if (info.interceptorsEnabledForThisRequest) {
            try {
                // Make a fresh slot table for RSC.
                current.pushSlotTable();
                info.setSlotTable(current.getSlotTable());

                // Make a fresh slot table for TSC in case
                // interceptors need to make out calls.
                current.pushSlotTable();

                info.setCurrentExecutionPoint(ServerRequestInfoImpl.EXECUTION_POINT_STARTING);

                // Get all ServerRequestInterceptors:
                ServerRequestInterceptor[] serverInterceptors = (ServerRequestInterceptor[]) interceptorList
                        .getInterceptors(InterceptorList.INTERCEPTOR_TYPE_SERVER);
                int size = serverInterceptors.length;

                // We will assume that all interceptors returned successfully,
                // and adjust the flowStackIndex to the appropriate value if
                // we later discover otherwise.
                int flowStackIndex = size;
                boolean continueProcessing = true;

                // Currently, there is only one server-side starting point
                // interceptor called receive_request_service_contexts.
                for (int i = 0; continueProcessing && (i < size); i++) {
                    ServerRequestInterceptor sri = serverInterceptors[i];
                    try {
                        invokeServerStartingCall(sri.name());
                        sri.receive_request_service_contexts(info);
                    } catch (ForwardRequest e) {
                        invokeServerStartingForwardRequest(sri.name());

                        // as per PI spec (orbos/99-12-02 sec 5.3.1.), if
                        // interception point throws a ForwardRequest,
                        // no other Interceptors' starting points are
                        // called and send_other is called.
                        flowStackIndex = i;
                        info.setForwardRequest(e);
                        info.setIntermediatePointCall(ServerRequestInfoImpl.CALL_INTERMEDIATE_NONE);
                        info.setEndingPointCall(ServerRequestInfoImpl.CALL_SEND_OTHER);
                        info.setReplyStatus(LOCATION_FORWARD.value);

                        // For some reason, using break here causes the VM on
                        // NT to lose track of the value of flowStackIndex
                        // after exiting the for loop. I changed this to
                        // check a boolean value instead and it seems to work
                        // fine.
                        continueProcessing = false;
                    } catch (SystemException e) {
                        invokeServerStartingSystemException(sri.name(), e);

                        // as per PI spec (orbos/99-12-02 sec 5.3.1.), if
                        // interception point throws a SystemException,
                        // no other Interceptors' starting points are
                        // called.
                        flowStackIndex = i;
                        info.setException(e);
                        info.setIntermediatePointCall(ServerRequestInfoImpl.CALL_INTERMEDIATE_NONE);
                        info.setEndingPointCall(ServerRequestInfoImpl.CALL_SEND_EXCEPTION);
                        info.setReplyStatus(SYSTEM_EXCEPTION.value);

                        // For some reason, using break here causes the VM on
                        // NT to lose track of the value of flowStackIndex
                        // after exiting the for loop. I changed this to
                        // check a boolean value instead and it seems to
                        // work fine.
                        continueProcessing = false;
                    }
                }

                // Remember where we left off in the flow stack:
                info.setFlowStackIndex(flowStackIndex);
            } finally {
                // The remaining points, ServantManager and Servant
                // all run in the same logical thread.
                current.popSlotTable();
                // Now TSC and RSC are equivalent.
            }
        } // end enabled check
    }

    @InfoMethod
    private void invokeServerIntermediateCall(String name) {
    }

    @InfoMethod
    private void invokeServerIntermediateForwardRequest(String name) {
    }

    @InfoMethod
    private void invokeServerIntermediateSystemException(String name, SystemException exc) {
    }

    /**
     * Invokes receive_request interception points
     */
    @TraceInterceptor
    void invokeServerInterceptorIntermediatePoint(ServerRequestInfoImpl info) {

        int intermediatePointCall = info.getIntermediatePointCall();
        // If invocation is not yet enabled, don't do anything.
        if (info.interceptorsEnabledForThisRequest && (intermediatePointCall != ServerRequestInfoImpl.CALL_INTERMEDIATE_NONE)) {

            // NOTE: do not touch the slotStack. The RSC and TSC are
            // equivalent at this point.

            info.setCurrentExecutionPoint(ServerRequestInfoImpl.EXECUTION_POINT_INTERMEDIATE);

            // Get all ServerRequestInterceptors:
            ServerRequestInterceptor[] serverInterceptors = (ServerRequestInterceptor[]) interceptorList
                    .getInterceptors(InterceptorList.INTERCEPTOR_TYPE_SERVER);
            int size = serverInterceptors.length;

            // Currently, there is only one server-side intermediate point
            // interceptor called receive_request.
            for (int i = 0; i < size; i++) {
                ServerRequestInterceptor sri = serverInterceptors[i];
                try {
                    invokeServerIntermediateCall(sri.name());
                    sri.receive_request(info);
                } catch (ForwardRequest e) {
                    invokeServerIntermediateForwardRequest(sri.name());

                    // as per PI spec (orbos/99-12-02 sec 5.3.1.), if
                    // interception point throws a ForwardRequest,
                    // no other Interceptors' intermediate points are
                    // called and send_other is called.
                    info.setForwardRequest(e);
                    info.setEndingPointCall(ServerRequestInfoImpl.CALL_SEND_OTHER);
                    info.setReplyStatus(LOCATION_FORWARD.value);
                    break;
                } catch (SystemException e) {
                    invokeServerIntermediateSystemException(sri.name(), e);

                    // as per PI spec (orbos/99-12-02 sec 5.3.1.), if
                    // interception point throws a SystemException,
                    // no other Interceptors' starting points are
                    // called.
                    info.setException(e);
                    info.setEndingPointCall(ServerRequestInfoImpl.CALL_SEND_EXCEPTION);
                    info.setReplyStatus(SYSTEM_EXCEPTION.value);
                    break;
                }
            }
        } // end enabled check
    }

    private String getServerEndMethodName(int endingPointCall) {
        switch (endingPointCall) {
        case ServerRequestInfoImpl.CALL_SEND_REPLY:
            return "send_reply";
        case ServerRequestInfoImpl.CALL_SEND_EXCEPTION:
            return "send_exception";
        case ServerRequestInfoImpl.CALL_SEND_OTHER:
            return "send_other";
        }
        return "";
    }

    @InfoMethod
    private void serverInvokeEndingPoint(String name, String call) {
    }

    @InfoMethod
    private void caughtForwardRequest(String name) {
    }

    @InfoMethod
    private void caughtSystemException(String name, SystemException ex) {
    }

    /**
     * Invokes either send_reply, send_exception, or send_other, depending on the value of info.getEndingPointCall()
     */
    @TraceInterceptor
    void invokeServerInterceptorEndingPoint(ServerRequestInfoImpl info) {
        // If invocation is not yet enabled, don't do anything.
        if (info.interceptorsEnabledForThisRequest) {
            try {
                // NOTE: do not touch the slotStack. The RSC and TSC are
                // equivalent at this point.

                // REVISIT: This is moved out to PIHandlerImpl until dispatch
                // path is rearchitected. It must be there so that
                // it always gets executed so if an interceptor raises
                // an exception any service contexts added in earlier points
                // this point get put in the exception reply (via the SC Q).

                // Get all ServerRequestInterceptors:
                ServerRequestInterceptor[] serverInterceptors = (ServerRequestInterceptor[]) interceptorList
                        .getInterceptors(InterceptorList.INTERCEPTOR_TYPE_SERVER);
                int flowStackIndex = info.getFlowStackIndex();

                // Determine whether we are calling
                // send_exception, or send_other:
                int endingPointCall = info.getEndingPointCall();

                // Only step through the interceptors whose starting points
                // have successfully returned.
                for (int i = (flowStackIndex - 1); i >= 0; i--) {
                    ServerRequestInterceptor sri = serverInterceptors[i];

                    try {
                        serverInvokeEndingPoint(sri.name(), getServerEndMethodName(endingPointCall));

                        switch (endingPointCall) {
                        case ServerRequestInfoImpl.CALL_SEND_REPLY:
                            sri.send_reply(info);
                            break;
                        case ServerRequestInfoImpl.CALL_SEND_EXCEPTION:
                            sri.send_exception(info);
                            break;
                        case ServerRequestInfoImpl.CALL_SEND_OTHER:
                            sri.send_other(info);
                            break;
                        }
                    } catch (ForwardRequest e) {
                        caughtForwardRequest(sri.name());

                        // as per PI spec (orbos/99-12-02 sec 5.3.1.), if
                        // interception point throws a ForwardException,
                        // ending point call changes to receive_other.
                        endingPointCall = ServerRequestInfoImpl.CALL_SEND_OTHER;
                        info.setEndingPointCall(endingPointCall);
                        info.setForwardRequest(e);
                        info.setReplyStatus(LOCATION_FORWARD.value);
                        info.setForwardRequestRaisedInEnding();
                    } catch (SystemException e) {
                        caughtSystemException(sri.name(), e);

                        // as per PI spec (orbos/99-12-02 sec 5.3.1.), if
                        // interception point throws a SystemException,
                        // ending point call changes to send_exception.
                        endingPointCall = ServerRequestInfoImpl.CALL_SEND_EXCEPTION;
                        info.setEndingPointCall(endingPointCall);
                        info.setException(e);
                        info.setReplyStatus(SYSTEM_EXCEPTION.value);
                    }
                }

                // Remember that all interceptors' starting and ending points
                // have already been executed so we need not do anything.
                info.setAlreadyExecuted(true);
            } finally {
                // Get rid of the Server side RSC.
                current.popSlotTable();
            }
        } // end enabled check
    }

    /*
     **********************************************************************
     * Private utility methods
     **********************************************************************/

    /**
     * Update the client delegate in the event of a ForwardRequest, given the information in the passed-in info object.
     */
    @TraceInterceptor
    private void updateClientRequestDispatcherForward(ClientRequestInfoImpl info) {

        ForwardRequest forwardRequest = info.getForwardRequestException();

        // ForwardRequest may be null if the forwarded IOR is set internal
        // to the ClientRequestDispatcher rather than explicitly through Portable
        // Interceptors. In this case, we need not update the client
        // delegate ForwardRequest object.
        if (forwardRequest != null) {
            org.omg.CORBA.Object object = forwardRequest.forward;

            // Convert the forward object into an IOR:
            IOR ior = orb.getIOR(object, false);
            info.setLocatedIOR(ior);
        }
    }

}
