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

import java.rmi.Remote;

import javax.rmi.CORBA.Tie;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import org.omg.CORBA.SystemException;
import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.CORBA_2_3.portable.OutputStream;
import org.omg.CORBA.portable.ResponseHandler;
import org.omg.CORBA.portable.UnknownException;
import org.omg.PortableServer.Servant;

import com.sun.corba.ee.spi.presentation.rmi.PresentationManager;
import com.sun.corba.ee.spi.presentation.rmi.PresentationDefaults;
import com.sun.corba.ee.spi.presentation.rmi.DynamicMethodMarshaller;

import com.sun.corba.ee.spi.logging.ORBUtilSystemException;

import org.glassfish.pfl.basic.proxy.DynamicAccessPermission;

public final class ReflectiveTie extends Servant implements Tie {
    private static final ORBUtilSystemException wrapper = ORBUtilSystemException.self;

    private Remote target = null;
    private PresentationManager pm;
    private PresentationManager.ClassData classData = null;

    public ReflectiveTie(PresentationManager pm) {
        if (!PresentationDefaults.inAppServer()) {
            SecurityManager s = System.getSecurityManager();
            if (s != null) {
                s.checkPermission(new DynamicAccessPermission("access"));
            }
        }

        this.pm = pm;
    }

    public String[] _all_interfaces(org.omg.PortableServer.POA poa, byte[] objectId) {
        return classData.getTypeIds();
    }

    public void setTarget(Remote target) {
        this.target = target;

        if (target == null) {
            classData = null;
        } else {
            Class targetClass = target.getClass();
            classData = pm.getClassData(targetClass);
        }
    }

    public Remote getTarget() {
        return target;
    }

    public org.omg.CORBA.Object thisObject() {
        return _this_object();
    }

    public void deactivate() {
        try {
            _poa().deactivate_object(_poa().servant_to_id(this));
        } catch (org.omg.PortableServer.POAPackage.WrongPolicy exception) {
            // ignore
        } catch (org.omg.PortableServer.POAPackage.ObjectNotActive exception) {
            // ignore
        } catch (org.omg.PortableServer.POAPackage.ServantNotActive exception) {
            // ignore
        }
    }

    public org.omg.CORBA.ORB orb() {
        return _orb();
    }

    public void orb(org.omg.CORBA.ORB orb) {
        try {
            ((org.omg.CORBA_2_3.ORB) orb).set_delegate(this);
        } catch (ClassCastException e) {
            throw wrapper.badOrbForServant(e);
        }
    }

    public Object dispatchToMethod(Method javaMethod, Remote target, Object[] args) throws InvocationTargetException {

        try {
            return javaMethod.invoke(target, args);
        } catch (IllegalAccessException ex) {
            throw wrapper.invocationErrorInReflectiveTie(ex, javaMethod.getName(), javaMethod.getDeclaringClass().getName());
        } catch (IllegalArgumentException ex) {
            throw wrapper.invocationErrorInReflectiveTie(ex, javaMethod.getName(), javaMethod.getDeclaringClass().getName());
        }
    }

    public org.omg.CORBA.portable.OutputStream _invoke(String method, org.omg.CORBA.portable.InputStream _in, ResponseHandler reply) {
        Method javaMethod = null;
        DynamicMethodMarshaller dmm = null;

        try {
            InputStream in = (InputStream) _in;

            javaMethod = classData.getIDLNameTranslator().getMethod(method);
            if (javaMethod == null)
                throw wrapper.methodNotFoundInTie(method, target.getClass().getName());

            dmm = pm.getDynamicMethodMarshaller(javaMethod);

            Object[] args = dmm.readArguments(in);

            Object result = dispatchToMethod(javaMethod, target, args);

            OutputStream os = (OutputStream) reply.createReply();

            dmm.writeResult(os, result);

            return os;
        } catch (InvocationTargetException ex) {
            // Unwrap the actual exception so that it can be wrapped by an
            // UnknownException or thrown if it is a system exception.
            // This is expected in the server dispatcher code.
            Throwable thr = ex.getCause();
            if (thr instanceof SystemException)
                throw (SystemException) thr;
            else if ((thr instanceof Exception) && dmm.isDeclaredException(thr)) {
                OutputStream os = (OutputStream) reply.createExceptionReply();
                dmm.writeException(os, (Exception) thr);
                return os;
            } else
                throw new UnknownException(thr);
        }
    }
}
