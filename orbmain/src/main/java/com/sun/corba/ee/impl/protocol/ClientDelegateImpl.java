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

package com.sun.corba.ee.impl.protocol;

import java.util.Iterator;

import org.omg.CORBA.Context;
import org.omg.CORBA.ContextList;
import org.omg.CORBA.ExceptionList;
import org.omg.CORBA.NamedValue;
import org.omg.CORBA.NVList;
import org.omg.CORBA.Request;

import org.omg.CORBA.portable.ApplicationException;
import org.omg.CORBA.portable.Delegate;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.RemarshalException;
import org.omg.CORBA.portable.ServantObject;

import com.sun.corba.ee.impl.encoding.CDRInputObject;
import com.sun.corba.ee.impl.encoding.CDROutputObject;
import com.sun.corba.ee.spi.protocol.ClientInvocationInfo;
import com.sun.corba.ee.spi.protocol.ClientRequestDispatcher;

import com.sun.corba.ee.spi.presentation.rmi.StubAdapter;
import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.protocol.ClientDelegate;
import com.sun.corba.ee.spi.transport.ContactInfo;
import com.sun.corba.ee.spi.transport.ContactInfoList;
import com.sun.corba.ee.spi.transport.ContactInfoListIterator;
import com.sun.corba.ee.spi.misc.ORBConstants;

import com.sun.corba.ee.impl.corba.RequestImpl;
import com.sun.corba.ee.spi.logging.ORBUtilSystemException;
import com.sun.corba.ee.impl.util.JDKBridge;

import com.sun.corba.ee.impl.misc.ORBUtility;
import com.sun.corba.ee.spi.ior.TaggedProfile;
import com.sun.corba.ee.spi.ior.TaggedProfileTemplate;
import com.sun.corba.ee.spi.ior.iiop.IIOPAddress;
import com.sun.corba.ee.spi.ior.iiop.IIOPProfileTemplate;
import com.sun.corba.ee.spi.trace.IsLocal;
import com.sun.corba.ee.spi.trace.Subcontract;
import org.glassfish.pfl.basic.logex.OperationTracer;
import org.glassfish.pfl.tf.spi.TimingPointType;
import org.glassfish.pfl.tf.spi.annotation.InfoMethod;

// implements com.sun.corba.ee.impl.core.ClientRequestDispatcher
// so RMI-IIOP Util.isLocal can call ClientRequestDispatcher.useLocalInvocation.

/**
 * @author Harold Carr
 */
@Subcontract
@IsLocal
public class ClientDelegateImpl extends ClientDelegate {
    private ORB orb;
    private static final ORBUtilSystemException wrapper = ORBUtilSystemException.self;

    private ContactInfoList contactInfoList;

    public ClientDelegateImpl(ORB orb, ContactInfoList contactInfoList) {
        this.orb = orb;
        // this.tp = orb.getTimerManager().points() ;
        this.contactInfoList = contactInfoList;
    }

    //
    // framework.subcontract.Delegate
    //

    public ORB getBroker() {
        return orb;
    }

    public ContactInfoList getContactInfoList() {
        return contactInfoList;
    }

    //
    // CORBA_2_3.portable.Delegate
    //

    @InfoMethod
    private void requestInfo(String operation, ContactInfo info) {
    }

    @InfoMethod
    private void retryingRequest(Exception exc) {
    }

    @InfoMethod(tpName = "totalInvocation", tpType = TimingPointType.ENTER)
    private void enter_totalInvocation() {
    }

    @InfoMethod(tpName = "totalInvocation", tpType = TimingPointType.EXIT)
    private void exit_totalInvocation() {
    }

    @InfoMethod(tpName = "hasNextNext", tpType = TimingPointType.ENTER)
    private void enter_hasNextNext() {
    }

    @InfoMethod(tpName = "hasNextNext", tpType = TimingPointType.EXIT)
    private void exit_hasNextNext() {
    }

    private IIOPAddress getPrimaryAddress(final IOR ior) {
        if (ior != null) {
            for (TaggedProfile tprof : ior) {
                final TaggedProfileTemplate tpt = tprof.getTaggedProfileTemplate();
                if (tpt instanceof IIOPProfileTemplate) {
                    final IIOPProfileTemplate ipt = (IIOPProfileTemplate) tpt;
                    return ipt.getPrimaryAddress();
                }
            }
        }

        return null;
    }

    @InfoMethod
    private void targetIOR(IOR ior) {
    }

    @InfoMethod
    private void effectiveTargetIOR(IOR ior) {
    }

    @Subcontract
    @Override
    public OutputStream request(org.omg.CORBA.Object self, String operation, boolean responseExpected) {

        targetIOR(contactInfoList.getTargetIOR());
        effectiveTargetIOR(contactInfoList.getEffectiveTargetIOR());
        enter_totalInvocation();

        try {
            OutputStream result = null;
            boolean retry;
            do {
                retry = false;

                Iterator contactInfoListIterator = null;
                ContactInfo contactInfo = null;
                ClientInvocationInfo invocationInfo = null;

                try {
                    invocationInfo = orb.createOrIncrementInvocationInfo();
                    contactInfoListIterator = invocationInfo.getContactInfoListIterator();
                    if (contactInfoListIterator == null) {
                        contactInfoListIterator = contactInfoList.iterator();
                        invocationInfo.setContactInfoListIterator(contactInfoListIterator);
                    }

                    try {
                        enter_hasNextNext();

                        if (!contactInfoListIterator.hasNext()) {
                            // REVISIT: When we unwind the retry stack
                            // these are necessary.
                            orb.getPIHandler().initiateClientPIRequest(false);
                            ORBUtility.pushEncVersionToThreadLocalState(ORBConstants.JAVA_ENC_VERSION);
                            throw ((ContactInfoListIterator) contactInfoListIterator).getFailureException();
                        }
                        contactInfo = (ContactInfo) contactInfoListIterator.next();
                        requestInfo(operation, contactInfo);
                    } finally {
                        exit_hasNextNext();
                    }

                    ClientRequestDispatcher subcontract = contactInfo.getClientRequestDispatcher();
                    // Remember chosen subcontract for invoke and releaseReply.
                    //
                    // NOTE: This is necessary since a stream is not available
                    // in releaseReply if there is a client marshaling error
                    // or an error in _invoke.
                    invocationInfo.setClientRequestDispatcher(subcontract);
                    result = subcontract.beginRequest(self, operation, !responseExpected, contactInfo);
                } catch (RuntimeException e) {
                    // REVISIT:
                    // this part similar to BufferManagerWriteStream.overflow()
                    retry = contactInfoListIterator != null
                            && ((ContactInfoListIterator) contactInfoListIterator).reportException(contactInfo, e);

                    if (retry) {
                        retryingRequest(e);
                        invocationInfo.setIsRetryInvocation(true);
                    } else {
                        throw e;
                    }
                }
            } while (retry);
            return result;
        } finally {
            // Enable operation tracing for argument marshaling
            if (orb.operationTraceDebugFlag) {
                OperationTracer.enable();
            }
            OperationTracer.begin("client argument marshaling:op=" + operation);
        }
    }

    @Subcontract
    @Override
    public InputStream invoke(org.omg.CORBA.Object self, OutputStream output) throws ApplicationException, RemarshalException {
        // Disable operation tracing for argment marshaling
        OperationTracer.disable();
        OperationTracer.finish();

        ClientRequestDispatcher subcontract = getClientRequestDispatcher();
        try {
            return (InputStream) subcontract.marshalingComplete((Object) self, (CDROutputObject) output);
        } finally {
            // Enable operation tracing for result unmarshaling
            if (orb.operationTraceDebugFlag) {
                OperationTracer.enable();
            }
            OperationTracer.begin("client result unmarshaling");
        }
    }

    @Subcontract
    @Override
    public void releaseReply(org.omg.CORBA.Object self, InputStream input) {
        try {
            // NOTE: InputStream may be null (e.g., exception request from PI).
            ClientRequestDispatcher subcontract = getClientRequestDispatcher();
            if (subcontract != null) {
                // Important: avoid an NPE.
                // ie: Certain errors may happen before a subcontract is selected.
                subcontract.endRequest(orb, self, (CDRInputObject) input);
            }
            orb.releaseOrDecrementInvocationInfo();
        } finally {
            exit_totalInvocation();

            // Disable operation tracing for result unmarshaling
            OperationTracer.disable();
            OperationTracer.finish();
        }
    }

    private ClientRequestDispatcher getClientRequestDispatcher() {
        return ((InvocationInfo) orb.getInvocationInfo()).getClientRequestDispatcher();
    }

    public org.omg.CORBA.Object get_interface_def(org.omg.CORBA.Object obj) {
        InputStream is = null;
        // instantiate the stub
        org.omg.CORBA.Object stub = null;

        try {
            OutputStream os = request(null, "_interface", true);
            is = invoke((org.omg.CORBA.Object) null, os);

            org.omg.CORBA.Object objimpl = is.read_Object();

            // check if returned object is of correct type
            if (!objimpl._is_a("IDL:omg.org/CORBA/InterfaceDef:1.0")) {
                throw wrapper.wrongInterfaceDef();
            }

            try {
                stub = (org.omg.CORBA.Object) JDKBridge.loadClass("org.omg.CORBA._InterfaceDefStub").newInstance();
            } catch (Exception ex) {
                throw wrapper.noInterfaceDefStub(ex);
            }

            org.omg.CORBA.portable.Delegate del = StubAdapter.getDelegate(objimpl);
            StubAdapter.setDelegate(stub, del);
        } catch (ApplicationException e) {
            // This cannot happen.
            throw wrapper.applicationExceptionInSpecialMethod(e);
        } catch (RemarshalException e) {
            return get_interface_def(obj);
        } finally {
            releaseReply((org.omg.CORBA.Object) null, is);
        }

        return stub;
    }

    @InfoMethod
    private void foundMyId() {
    }

    @InfoMethod
    private void foundIdInRepostioryId() {
    }

    @InfoMethod
    private void callingServer() {
    }

    @InfoMethod
    private void serverReturned() {
    }

    @InfoMethod
    private void retryingRequest() {
    }

    @Subcontract
    public boolean is_a(org.omg.CORBA.Object obj, String dest) {
        while (true) {
            // dest is the typeId of the interface to compare against.
            // repositoryIds is the list of typeIds that the stub knows about.

            // First we look for an answer using local information.
            String[] repositoryIds = StubAdapter.getTypeIds(obj);
            String myid = contactInfoList.getTargetIOR().getTypeId();
            if (dest.equals(myid)) {
                foundMyId();
                return true;
            }

            for (int i = 0; i < repositoryIds.length; i++) {
                if (dest.equals(repositoryIds[i])) {
                    foundIdInRepostioryId();
                    return true;
                }
            }

            // But repositoryIds may not be complete, so it may be necessary to
            // go to server.
            InputStream is = null;
            try {
                callingServer();

                OutputStream os = request(null, "_is_a", true);
                os.write_string(dest);
                is = invoke((org.omg.CORBA.Object) null, os);
                boolean result = is.read_boolean();

                serverReturned();

                return result;
            } catch (ApplicationException e) {
                // This cannot happen.
                throw wrapper.applicationExceptionInSpecialMethod(e);
            } catch (RemarshalException e) {
                // Fall through and retry the operation after a short
                // pause
                retryingRequest();
                try {
                    Thread.sleep(5);
                } catch (Exception exc) {
                    // ignore the exception
                }
            } finally {
                releaseReply((org.omg.CORBA.Object) null, is);
            }
        }
    }

    public boolean non_existent(org.omg.CORBA.Object obj) {
        InputStream is = null;
        try {
            OutputStream os = request(null, "_non_existent", true);
            is = invoke((org.omg.CORBA.Object) null, os);

            return is.read_boolean();

        } catch (ApplicationException e) {
            // This cannot happen.
            throw wrapper.applicationExceptionInSpecialMethod(e);
        } catch (RemarshalException e) {
            return non_existent(obj);
        } finally {
            releaseReply((org.omg.CORBA.Object) null, is);
        }
    }

    public org.omg.CORBA.Object duplicate(org.omg.CORBA.Object obj) {
        return obj;
    }

    public void release(org.omg.CORBA.Object obj) {
        // DO NOT clear out internal variables to release memory
        // This delegate may be pointed-to by other objrefs.
    }

    // obj._get_delegate() == this due to the argument passing conventions in
    // portable.ObjectImpl, so we just ignore obj here.
    public boolean is_equivalent(org.omg.CORBA.Object obj, org.omg.CORBA.Object ref) {
        if (ref == null) {
            return false;
        }

        // If ref is a local object, it is not a Stub!
        if (!StubAdapter.isStub(ref)) {
            return false;
        }

        Delegate del = StubAdapter.getDelegate(ref);
        if (del == null) {
            return false;
        }

        // Optimize the x.is_equivalent( x ) case
        if (del == this) {
            return true;
        }

        // If delegate was created by a different ORB, return false
        if (!(del instanceof ClientDelegateImpl)) {
            return false;
        }

        ClientDelegateImpl corbaDelegate = (ClientDelegateImpl) del;
        ContactInfoList ccil = corbaDelegate.getContactInfoList();
        return this.contactInfoList.getTargetIOR().isEquivalent(ccil.getTargetIOR());
    }

    /**
     * This method overrides the org.omg.CORBA.portable.Delegate.equals method, and does the equality check based on IOR
     * equality.
     */
    @Override
    public boolean equals(org.omg.CORBA.Object self, java.lang.Object other) {
        if (other == null) {
            return false;
        }

        if (!StubAdapter.isStub(other)) {
            return false;
        }

        Delegate delegate = StubAdapter.getDelegate(other);
        if (delegate == null) {
            return false;
        }

        if (delegate instanceof ClientDelegateImpl) {
            ClientDelegateImpl otherDel = (ClientDelegateImpl) delegate;
            IOR otherIor = otherDel.contactInfoList.getTargetIOR();
            return this.contactInfoList.getTargetIOR().equals(otherIor);
        }

        // Come here if other is not implemented by our ORB.
        return false;
    }

    @Override
    public int hashCode(org.omg.CORBA.Object obj) {
        return this.hashCode();
    }

    public int hash(org.omg.CORBA.Object obj, int maximum) {
        int h = this.hashCode();
        if (h > maximum) {
            return 0;
        }
        return h;
    }

    public Request request(org.omg.CORBA.Object obj, String operation) {
        return new RequestImpl(orb, obj, null, operation, null, null, null, null);
    }

    public Request create_request(org.omg.CORBA.Object obj, Context ctx, String operation, NVList arg_list, NamedValue result) {
        return new RequestImpl(orb, obj, ctx, operation, arg_list, result, null, null);
    }

    public Request create_request(org.omg.CORBA.Object obj, Context ctx, String operation, NVList arg_list, NamedValue result,
            ExceptionList exclist, ContextList ctxlist) {
        return new RequestImpl(orb, obj, ctx, operation, arg_list, result, exclist, ctxlist);
    }

    @Override
    public org.omg.CORBA.ORB orb(org.omg.CORBA.Object obj) {
        return this.orb;
    }

    /**
     * Returns true if this object is implemented by a local servant.
     *
     * REVISIT: locatedIOR should be replaced with a method call that returns the current IOR for this request (e.g.
     * ContactInfoChooser).
     *
     * @param self The object reference which delegated to this delegate.
     * @return true only if the servant incarnating this object is located in this ORB.
     */
    @Override
    @IsLocal
    public boolean is_local(org.omg.CORBA.Object self) {
        return contactInfoList.getEffectiveTargetIOR().getProfile().isLocal();
    }

    @Override
    public ServantObject servant_preinvoke(org.omg.CORBA.Object self, String operation, Class expectedType) {
        return contactInfoList.getLocalClientRequestDispatcher().servant_preinvoke(self, operation, expectedType);
    }

    @Override
    public void servant_postinvoke(org.omg.CORBA.Object self, ServantObject servant) {
        contactInfoList.getLocalClientRequestDispatcher().servant_postinvoke(self, servant);
    }

    /*
     * Returns the codebase for object reference provided.
     * 
     * @param self the object reference whose codebase needs to be returned.
     * 
     * @return the codebase as a space delimited list of url strings or null if none.
     */
    @Override
    public String get_codebase(org.omg.CORBA.Object self) {
        if (contactInfoList.getTargetIOR() != null) {
            return contactInfoList.getTargetIOR().getProfile().getCodebase();
        }
        return null;
    }

    @Override
    public String toString(org.omg.CORBA.Object self) {
        return contactInfoList.getTargetIOR().toString();
    }

    @Override
    public int hashCode() {
        return this.contactInfoList.hashCode();
    }
}

// End of file.
