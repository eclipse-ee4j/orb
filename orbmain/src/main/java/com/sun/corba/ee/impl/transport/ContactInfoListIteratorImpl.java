/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.transport;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.TRANSIENT;

import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.transport.TcpTimeouts;
import com.sun.corba.ee.spi.transport.ContactInfo;
import com.sun.corba.ee.spi.transport.ContactInfoList;
import com.sun.corba.ee.spi.transport.ContactInfoListIterator;
import com.sun.corba.ee.spi.transport.IIOPPrimaryToContactInfo;

import com.sun.corba.ee.spi.logging.ORBUtilSystemException;
import com.sun.corba.ee.impl.protocol.InvocationInfo;

import com.sun.corba.ee.spi.trace.Transport;
import org.glassfish.pfl.tf.spi.annotation.InfoMethod;

// REVISIT: create a unit test for this class.

@Transport
public class ContactInfoListIteratorImpl implements ContactInfoListIterator {
    protected static final ORBUtilSystemException wrapper = ORBUtilSystemException.self;

    protected ORB orb;
    protected ContactInfoList contactInfoList;
    protected RuntimeException failureException;
    private boolean usePRLB;
    protected TcpTimeouts tcpTimeouts;

    // ITERATOR state
    protected Iterator<ContactInfo> effectiveTargetIORIterator;
    protected ContactInfo previousContactInfo;
    protected boolean isAddrDispositionRetry;
    protected boolean retryWithPreviousContactInfo;
    protected IIOPPrimaryToContactInfo primaryToContactInfo;
    protected ContactInfo primaryContactInfo;
    protected List<ContactInfo> listOfContactInfos;
    protected TcpTimeouts.Waiter waiter;
    // Set of endpoints that have failed since the last successful communication
    // with the IOR.
    protected Set<ContactInfo> failedEndpoints;
    // End ITERATOR state

    public ContactInfoListIteratorImpl(ORB orb, ContactInfoList corbaContactInfoList, ContactInfo primaryContactInfo, List listOfContactInfos,
            boolean usePerRequestLoadBalancing) {
        this.orb = orb;
        this.tcpTimeouts = orb.getORBData().getTransportTcpConnectTimeouts();
        this.contactInfoList = corbaContactInfoList;
        this.primaryContactInfo = primaryContactInfo;
        if (listOfContactInfos != null) {
            // listOfContactInfos is null when used by the legacy
            // socket factory. In that case this iterator is NOT used.

            this.effectiveTargetIORIterator = listOfContactInfos.iterator();
        }
        // List is immutable so no need to synchronize access.
        this.listOfContactInfos = listOfContactInfos;

        this.previousContactInfo = null;
        this.isAddrDispositionRetry = false;
        this.retryWithPreviousContactInfo = false;

        this.failureException = null;

        this.waiter = tcpTimeouts.waiter();
        this.failedEndpoints = new HashSet<ContactInfo>();

        this.usePRLB = usePerRequestLoadBalancing;

        if (usePerRequestLoadBalancing) {
            // We certainly DON'T want sticky behavior if we are using PRLB.
            primaryToContactInfo = null;
        } else {
            primaryToContactInfo = orb.getORBData().getIIOPPrimaryToContactInfo();
        }
    }

    @InfoMethod
    private void display(String msg) {
    }

    @InfoMethod
    private void display(String msg, Object value) {
    }

    @InfoMethod
    private void display(String msg, long value) {
    }

    ////////////////////////////////////////////////////
    //
    // java.util.Iterator
    //

    @Transport
    public boolean hasNext() {
        boolean result = false;
        if (retryWithPreviousContactInfo) {
            display("backoff before retry previous");

            if (waiter.isExpired()) {
                display("time to wait for connection exceeded ", tcpTimeouts.get_max_time_to_wait());

                // NOTE: Need to indicate the timeout.
                // And it needs to break the loop in the delegate.
                failureException = wrapper.communicationsRetryTimeout(failureException, tcpTimeouts.get_max_time_to_wait());
                return false;
            }

            waiter.sleepTime();
            waiter.advance();
            return true;
        }

        if (isAddrDispositionRetry) {
            return true;
        }

        if (primaryToContactInfo != null) {
            result = primaryToContactInfo.hasNext(primaryContactInfo, previousContactInfo, listOfContactInfos);
        } else {
            result = effectiveTargetIORIterator.hasNext();
        }

        if (!result && !waiter.isExpired()) {
            display("Reached end of ContactInfoList list. Starting at beginning");

            previousContactInfo = null;
            if (primaryToContactInfo != null) {
                primaryToContactInfo.reset(primaryContactInfo);
            } else {
                // Argela:
                effectiveTargetIORIterator = listOfContactInfos.iterator();
            }

            result = hasNext();
            return result;
        }

        return result;
    }

    @Transport
    public ContactInfo next() {
        if (retryWithPreviousContactInfo) {
            retryWithPreviousContactInfo = false;
            return previousContactInfo;
        }

        if (isAddrDispositionRetry) {
            isAddrDispositionRetry = false;
            return previousContactInfo;
        }

        // We hold onto the last in case we get an addressing
        // disposition retry. Then we use it again.

        // We also hold onto it for the sticky manager.

        if (primaryToContactInfo != null) {
            previousContactInfo = (ContactInfo) primaryToContactInfo.next(primaryContactInfo, previousContactInfo, listOfContactInfos);
        } else {
            previousContactInfo = effectiveTargetIORIterator.next();
        }

        // We must use waiter here regardless of whether or not
        // there is a IIOPPrimaryToContactInfo or not.
        // Failure to do this resulted in bug 6568174.
        if (failedEndpoints.contains(previousContactInfo)) {
            failedEndpoints.clear();
            waiter.sleepTime();
            waiter.advance();
        }

        return previousContactInfo;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public ContactInfoList getContactInfoList() {
        return contactInfoList;
    }

    @Transport
    public void reportSuccess(ContactInfo contactInfo) {
        display("contactInfo", contactInfo);
        failedEndpoints.clear();
        waiter.reset(); // not strictly necessary
    }

    @Transport
    public boolean reportException(ContactInfo contactInfo, RuntimeException ex) {
        boolean result = false;
        display("contactInfo", contactInfo);

        failedEndpoints.add(contactInfo);
        this.failureException = ex;
        if (ex instanceof COMM_FAILURE) {
            SystemException se = (SystemException) ex;
            if (se.minor == ORBUtilSystemException.CONNECTION_REBIND) {
                display("COMM_FAILURE(connection rebind): " + "retry with previous contact info", ex);

                retryWithPreviousContactInfo = true;
                result = true;
                return result;
            } else {
                if (se.completed == CompletionStatus.COMPLETED_NO) {
                    if (hasNext()) {
                        display("COMM_FAILURE(COMPLETED_NO, hasNext true): " + "retry with next contact info", ex);
                        result = true;
                        return result;
                    }
                    if (contactInfoList.getEffectiveTargetIOR() != contactInfoList.getTargetIOR()) {
                        display("COMM_FAILURE(COMPLETED_NO, hasNext false, " + "effective != target): " + "retry with target", ex);

                        // retry from root ior
                        updateEffectiveTargetIOR(contactInfoList.getTargetIOR());
                        result = true;
                        return result;
                    }
                }
            }
        } else if (ex instanceof TRANSIENT) {
            display("TRANSIENT: retry with previous contact info", ex);
            retryWithPreviousContactInfo = true;
            result = true;
            return result;
        }
        result = false;
        waiter.reset(); // not strictly necessary.
        return result;
    }

    public RuntimeException getFailureException() {
        if (failureException == null) {
            return wrapper.invalidContactInfoListIteratorFailureException();
        } else {
            return failureException;
        }
    }

    ////////////////////////////////////////////////////
    //
    // spi.CorbaContactInfoListIterator
    //

    @Transport
    public void reportAddrDispositionRetry(ContactInfo contactInfo, short disposition) {
        previousContactInfo.setAddressingDisposition(disposition);
        isAddrDispositionRetry = true;
        waiter.reset(); // necessary
    }

    @Transport
    public void reportRedirect(ContactInfo contactInfo, IOR forwardedIOR) {
        updateEffectiveTargetIOR(forwardedIOR);
        waiter.reset(); // Necessary
    }

    ////////////////////////////////////////////////////
    //
    // Implementation.
    //

    //
    // REVISIT:
    //
    // The normal operation for a standard iterator is to throw
    // ConcurrentModificationException whenever the underlying collection
    // changes. This is implemented by keeping a modification counter (the
    // timestamp may fail because the granularity is too coarse).
    // Essentially what you need to do is whenever the iterator fails this
    // way, go back to ContactInfoList and get a new iterator.
    //
    // Need to update CorbaClientRequestDispatchImpl to catch and use
    // that exception.
    //

    public void updateEffectiveTargetIOR(IOR newIOR) {
        contactInfoList.setEffectiveTargetIOR(newIOR);
        // If we report the exception in _request (i.e., beginRequest
        // we cannot throw RemarshalException to the stub because _request
        // does not declare that exception.
        // To keep the two-level dispatching (first level chooses ContactInfo,
        // second level is specific to that ContactInfo/EPT) we need to
        // ensure that the request dispatchers get their iterator from the
        // InvocationStack (i.e., ThreadLocal). That way if the list iterator
        // needs a complete update it happens right here.

        // Ugly hack for Argela: avoid rotating the iterator in this case,
        // or we rotate the iterator twice on every request.
        //
        // Remove this hack once we figure out why we get all of the
        // membership changes when the cluster shape does not change.
        ContactInfoListImpl.setSkipRotate();

        ((InvocationInfo) orb.getInvocationInfo()).setContactInfoListIterator(contactInfoList.iterator());
    }
}

// End of file.
