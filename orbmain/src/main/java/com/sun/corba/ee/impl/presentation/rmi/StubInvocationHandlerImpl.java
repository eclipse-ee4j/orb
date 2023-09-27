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

package com.sun.corba.ee.impl.presentation.rmi;

import com.sun.corba.ee.impl.javax.rmi.CORBA.Util;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.presentation.rmi.DynamicMethodMarshaller;
import com.sun.corba.ee.spi.presentation.rmi.InvocationInterceptor;
import com.sun.corba.ee.spi.presentation.rmi.PresentationDefaults;
import com.sun.corba.ee.spi.presentation.rmi.PresentationManager;
import com.sun.corba.ee.spi.presentation.rmi.StubAdapter;
import com.sun.corba.ee.spi.protocol.ClientDelegate;
import com.sun.corba.ee.spi.protocol.LocalClientRequestDispatcher;
import com.sun.corba.ee.spi.trace.IsLocal;
import com.sun.corba.ee.spi.transport.ContactInfoList;
import org.glassfish.pfl.basic.proxy.DynamicAccessPermission;
import org.glassfish.pfl.basic.proxy.LinkedInvocationHandler;
import org.glassfish.pfl.tf.spi.annotation.InfoMethod;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.portable.ApplicationException;
import org.omg.CORBA.portable.Delegate;
import org.omg.CORBA.portable.RemarshalException;
import org.omg.CORBA.portable.ServantObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.AccessController;
import java.security.PrivilegedAction;

@IsLocal
public final class StubInvocationHandlerImpl implements LinkedInvocationHandler {
    private transient PresentationManager.ClassData classData;
    private transient PresentationManager pm;
    private transient org.omg.CORBA.Object stub;
    private transient Proxy self;

    public void setProxy(Proxy self) {
        this.self = self;
    }

    public Proxy getProxy() {
        return self;
    }

    public StubInvocationHandlerImpl(PresentationManager pm, PresentationManager.ClassData classData, org.omg.CORBA.Object stub) {
        if (!PresentationDefaults.inAppServer()) {
            SecurityManager s = System.getSecurityManager();
            if (s != null) {
                s.checkPermission(new DynamicAccessPermission("access"));
            }
        }

        this.classData = classData;
        this.pm = pm;
        this.stub = stub;
    }

    @IsLocal
    private boolean isLocal(Delegate delegate) {
        boolean result = false;
        if (delegate instanceof ClientDelegate) {
            ClientDelegate cdel = (ClientDelegate) delegate;
            ContactInfoList cil = cdel.getContactInfoList();
            LocalClientRequestDispatcher lcrd = cil.getLocalClientRequestDispatcher();
            result = lcrd.useLocalInvocation(null);
        }

        return result;
    }

    public Object invoke(Object proxy, final Method method, Object[] args) throws Throwable {

        Delegate delegate = null;
        try {
            delegate = StubAdapter.getDelegate(stub);
        } catch (SystemException ex) {
            throw Util.getInstance().mapSystemException(ex);
        }

        org.omg.CORBA.ORB delORB = delegate.orb(stub);
        if (delORB instanceof ORB) {
            ORB orb = (ORB) delORB;

            InvocationInterceptor interceptor = orb.getInvocationInterceptor();

            try {
                interceptor.preInvoke();
            } catch (Exception exc) {
                // XXX Should we log this?
            }

            try {
                return privateInvoke(delegate, proxy, method, args);
            } finally {
                try {
                    interceptor.postInvoke();
                } catch (Exception exc) {
                    // XXX Should we log this?
                }
            }
        } else {
            // Not our ORB: so handle without invocation interceptor.
            return privateInvoke(delegate, proxy, method, args);
        }
    }

    @InfoMethod
    private void takingRemoteBranch() {
    }

    @InfoMethod
    private void takingLocalBranch() {
    }

    /**
     * Invoke the given method with the args and return the result. This may result in a remote invocation.
     * 
     * @param proxy The proxy used for this class (null if not using java.lang.reflect.Proxy)
     */
    @IsLocal
    private Object privateInvoke(Delegate delegate, Object proxy, final Method method, Object[] args) throws Throwable {
        boolean retry;
        do {
            retry = false;
            String giopMethodName = classData.getIDLNameTranslator().getIDLName(method);
            DynamicMethodMarshaller dmm = pm.getDynamicMethodMarshaller(method);

            if (!isLocal(delegate)) {
                try {
                    takingRemoteBranch();
                    org.omg.CORBA_2_3.portable.InputStream in = null;
                    try {
                        // create request
                        org.omg.CORBA_2_3.portable.OutputStream out = (org.omg.CORBA_2_3.portable.OutputStream) delegate.request(stub,
                                giopMethodName, true);
                        // marshal arguments
                        dmm.writeArguments(out, args);
                        // finish invocation
                        in = (org.omg.CORBA_2_3.portable.InputStream) delegate.invoke(stub, out);
                        // unmarshal result
                        return dmm.readResult(in);
                    } catch (ApplicationException ex) {
                        throw dmm.readException(ex);
                    } catch (RemarshalException ex) {
                        // return privateInvoke( delegate, proxy, method, args ) ;
                        retry = true;
                    } finally {
                        delegate.releaseReply(stub, in);
                    }
                } catch (SystemException ex) {
                    throw Util.getInstance().mapSystemException(ex);
                }
            } else {
                takingLocalBranch();
                org.omg.CORBA.ORB orb = delegate.orb(stub);
                ServantObject so = delegate.servant_preinvoke(stub, giopMethodName, method.getDeclaringClass());
                if (so == null) {
                    // return privateInvoke( delegate, proxy, method, args ) ;
                    retry = true;
                    continue;
                }

                try {
                    Object[] copies = dmm.copyArguments(args, orb);

                    if (!method.isAccessible()) {
                        // Make sure that we can invoke a method from a normally
                        // inaccessible package, as this reflective class must always
                        // be able to invoke a non-public method.
                        AccessController.doPrivileged(new PrivilegedAction() {
                            public Object run() {
                                method.setAccessible(true);
                                return null;
                            }
                        });
                    }

                    Object result = method.invoke(so.servant, copies);

                    return dmm.copyResult(result, orb);
                } catch (InvocationTargetException ex) {
                    Throwable mex = ex.getCause();
                    // mex should never be null, as null cannot be thrown
                    if (dmm.isDeclaredException(mex))
                        throw mex;
                    else
                        throw Util.getInstance().wrapException(mex);
                } catch (Throwable thr) {
                    if (thr instanceof ThreadDeath)
                        throw thr;

                    // This is not a user thrown exception from the
                    // method call, so don't copy it. This is either
                    // an error or a reflective invoke exception.
                    throw Util.getInstance().wrapException(thr);
                } finally {
                    delegate.servant_postinvoke(stub, so);
                }
            }
        } while (retry);
        return null;
    }
}
