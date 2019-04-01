/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.protocol;

import org.omg.CORBA.TRANSIENT;
import org.omg.CORBA.SystemException;

import org.omg.CORBA.portable.ServantObject;

import com.sun.corba.ee.spi.protocol.LocalClientRequestDispatcher;
import com.sun.corba.ee.spi.protocol.RequestDispatcherRegistry;
import com.sun.corba.ee.spi.protocol.ForwardException;

import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.ior.IOR;

import com.sun.corba.ee.spi.oa.ObjectAdapterFactory;
import com.sun.corba.ee.spi.oa.OADestroyed;

import com.sun.corba.ee.spi.ior.ObjectAdapterId;
import com.sun.corba.ee.spi.ior.TaggedProfile;
import com.sun.corba.ee.spi.ior.ObjectKeyTemplate;
import com.sun.corba.ee.spi.ior.ObjectId;

import com.sun.corba.ee.spi.logging.POASystemException;
import com.sun.corba.ee.spi.logging.ORBUtilSystemException;

import com.sun.corba.ee.spi.trace.IsLocal;
import com.sun.corba.ee.spi.trace.Subcontract;
import org.glassfish.pfl.tf.spi.annotation.InfoMethod;

@Subcontract
@IsLocal
public abstract class LocalClientRequestDispatcherBase implements LocalClientRequestDispatcher {
    protected static final POASystemException poaWrapper = POASystemException.self;
    protected static final ORBUtilSystemException wrapper = ORBUtilSystemException.self;

    // XXX May want to make some of this configuratble as in the remote case
    // Should this be unified? How can we better handle this retry/backoff
    // implementation with a single implementation?
    private static final int INITIAL_BACKOFF = 1; // initially start off very small
                                                  // because 1 millisecond is a long time for a local call.

    private static final int MAX_BACKOFF = 1000; // Never sleep longer than this
    private static final int MAX_WAIT_TIME = 10 * 1000; // Total time to wait for a local request.

    protected ORB orb;
    private int scid;

    // Cached information needed for local dispatch
    protected boolean servantIsLocal;
    protected ObjectAdapterFactory oaf;
    protected ObjectAdapterId oaid;
    protected byte[] objectId;

    // If isNextIsLocalValid.get() == Boolean.TRUE,
    // the next call to isLocal should be valid
    private static final ThreadLocal isNextCallValid = new ThreadLocal() {
        @Override
        protected synchronized Object initialValue() {
            return Boolean.TRUE;
        }
    };

    protected LocalClientRequestDispatcherBase(ORB orb, int scid, IOR ior) {
        this.orb = orb;

        TaggedProfile prof = ior.getProfile();
        servantIsLocal = orb.getORBData().isLocalOptimizationAllowed() && prof.isLocal();

        ObjectKeyTemplate oktemp = prof.getObjectKeyTemplate();
        this.scid = oktemp.getSubcontractId();
        RequestDispatcherRegistry sreg = orb.getRequestDispatcherRegistry();
        oaf = sreg.getObjectAdapterFactory(scid);
        oaid = oktemp.getObjectAdapterId();
        ObjectId oid = prof.getObjectId();
        objectId = oid.getId();
    }

    public byte[] getObjectId() {
        return objectId;
    }

    @IsLocal
    public boolean is_local(org.omg.CORBA.Object self) {
        return false;
    }

    /*
     * Possible paths through useLocalInvocation/servant_preinvoke/servant_postinvoke:
     *
     * A: call useLocalInvocation If useLocalInvocation returns false, servant_preinvoke is not called. If
     * useLocalInvocation returns true, call servant_preinvoke If servant_preinvoke returns null, goto A else (local
     * invocation proceeds normally) servant_postinvoke is called
     *
     */
    @IsLocal
    public boolean useLocalInvocation(org.omg.CORBA.Object self) {
        if (isNextCallValid.get() == Boolean.TRUE) {
            return servantIsLocal;
        } else {
            isNextCallValid.set(Boolean.TRUE);
        }

        return false;
    }

    @InfoMethod
    private void servantNotCompatible() {
    }

    /**
     * Check that the servant in info (which must not be null) is an instance of the expectedType. If not, set the thread
     * local flag and return false.
     */
    @IsLocal
    protected boolean checkForCompatibleServant(ServantObject so, Class expectedType) {
        if (so == null) {
            return false;
        }

        // Normally, this test will never fail. However, if the servant
        // and the stub were loaded in different class loaders, this test
        // will fail.
        if (!expectedType.isInstance(so.servant)) {
            servantNotCompatible();
            isNextCallValid.set(Boolean.FALSE);

            // When servant_preinvoke returns null, the stub will
            // recursively re-invoke itself. Thus, the next call made from
            // the stub is another useLocalInvocation call.
            return false;
        }

        return true;
    }

    // The actual servant_preinvoke implementation, which must be
    // overridden. This method may throw exceptions
    // which are handled by servant_preinvoke.
    protected ServantObject internalPreinvoke(org.omg.CORBA.Object self, String operation, Class expectedType) throws OADestroyed {
        return null;
    }

    // This method is called when OADestroyed is caught. This allows
    // subclasses to provide cleanup code if necessary.
    protected void cleanupAfterOADestroyed() {
        // Default is NO-OP
    }

    @InfoMethod
    private void display(String msg) {
    }

    @InfoMethod
    private void display(String msg, int value) {
    }

    @InfoMethod
    private void display(String msg, Object value) {
    }

    // servant_preinvoke is here to contain the exception handling
    // logic that is common to all POA based servant_preinvoke implementations.
    @Subcontract
    public ServantObject servant_preinvoke(org.omg.CORBA.Object self, String operation, Class expectedType) {

        long startTime = -1;
        long backoff = INITIAL_BACKOFF;
        long maxWait = MAX_WAIT_TIME;

        while (true) {
            try {
                display("Calling internalPreinvoke");
                return internalPreinvoke(self, operation, expectedType);
            } catch (OADestroyed pdes) {
                display("Caught OADestroyed: will retry");
                cleanupAfterOADestroyed();
            } catch (TRANSIENT exc) {
                display("Caught transient");
                // Local calls are very fast, so don't waste time setting
                // up to track the time UNLESS the first attempt fails
                // with a TRANSIENT exception.
                long currentTime = System.currentTimeMillis();

                // Delay if not too much time passed, otherwise re-throw
                // the TRANSIENT exception.
                if (startTime == -1) {
                    display("backoff (first retry)", backoff);
                    startTime = currentTime;
                } else if ((currentTime - startTime) > MAX_WAIT_TIME) {
                    display("Total time exceeded", MAX_WAIT_TIME);
                    throw exc;
                } else {
                    backoff *= 2;
                    if (backoff > MAX_BACKOFF) {
                        backoff = MAX_BACKOFF;
                    }

                    display("increasing backoff (will retry)", backoff);
                }

                try {
                    Thread.sleep(backoff);
                } catch (InterruptedException iexc) {
                    // As usual, ignore the possible InterruptedException
                }

                display("retry");
            } catch (ForwardException ex) {
                /*
                 * REVISIT ClientRequestDispatcher csub = (ClientRequestDispatcher) StubAdapter.getDelegate( ex.forward_reference ) ;
                 * IOR ior = csub.getIOR() ; setLocatedIOR( ior ) ;
                 */
                display("Unsupported ForwardException");
                throw new RuntimeException("deal with this.", ex);
            } catch (ThreadDeath ex) {
                // ThreadDeath on the server side should not cause a client
                // side thread death in the local case. We want to preserve
                // this behavior for location transparency, so that a ThreadDeath
                // has the same affect in either the local or remote case.
                // The non-colocated case is handled in iiop.ORB.process, which
                // throws the same exception.
                display("Caught ThreadDeath");
                throw wrapper.runtimeexception(ex, ex.getClass().getName(), ex.getMessage());
            } catch (Throwable t) {
                display("Caught Throwable");

                if (t instanceof SystemException)
                    throw (SystemException) t;

                throw poaWrapper.localServantLookup(t);
            }
        }
    }
}

// End of file.
