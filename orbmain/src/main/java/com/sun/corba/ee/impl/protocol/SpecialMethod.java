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

import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;

import com.sun.corba.ee.spi.oa.ObjectAdapter;

import com.sun.corba.ee.spi.protocol.MessageMediator;

import com.sun.corba.ee.spi.logging.ORBUtilSystemException;

import com.sun.corba.ee.spi.oa.NullServant;

public abstract class SpecialMethod {
    static final ORBUtilSystemException wrapper = ORBUtilSystemException.self;

    public abstract boolean isNonExistentMethod();

    public abstract String getName();

    public abstract MessageMediator invoke(java.lang.Object servant, MessageMediator request, byte[] objectId, ObjectAdapter objectAdapter);

    public static final SpecialMethod getSpecialMethod(String operation) {
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().equals(operation)) {
                return methods[i];
            }
        }
        return null;
    }

    static SpecialMethod[] methods = { new IsA(), new GetInterface(), new NonExistent(), new NotExistent() };
}

class NonExistent extends SpecialMethod {
    public boolean isNonExistentMethod() {
        return true;
    }

    public String getName() { // _non_existent
        return "_non_existent";
    }

    public MessageMediator invoke(java.lang.Object servant, MessageMediator request, byte[] objectId, ObjectAdapter objectAdapter) {
        boolean result = (servant == null) || (servant instanceof NullServant);
        MessageMediator response = request.getProtocolHandler().createResponse(request, null);
        ((OutputStream) response.getOutputObject()).write_boolean(result);
        return response;
    }
}

class NotExistent extends NonExistent {
    @Override
    public String getName() { // _not_existent
        return "_not_existent";
    }
}

class IsA extends SpecialMethod { // _is_a
    public boolean isNonExistentMethod() {
        return false;
    }

    public String getName() {
        return "_is_a";
    }

    public MessageMediator invoke(java.lang.Object servant, MessageMediator request, byte[] objectId, ObjectAdapter objectAdapter) {
        if ((servant == null) || (servant instanceof NullServant)) {
            return request.getProtocolHandler().createSystemExceptionResponse(request, wrapper.badSkeleton(), null);
        }

        String[] ids = objectAdapter.getInterfaces(servant, objectId);
        String clientId = ((InputStream) request.getInputObject()).read_string();
        boolean answer = false;
        for (int i = 0; i < ids.length; i++) {
            if (ids[i].equals(clientId)) {
                answer = true;
                break;
            }
        }

        MessageMediator response = request.getProtocolHandler().createResponse(request, null);
        ((OutputStream) response.getOutputObject()).write_boolean(answer);
        return response;
    }
}

class GetInterface extends SpecialMethod { // _get_interface
    public boolean isNonExistentMethod() {
        return false;
    }

    public String getName() {
        return "_interface";
    }

    public MessageMediator invoke(java.lang.Object servant, MessageMediator request, byte[] objectId, ObjectAdapter objectAdapter) {
        if ((servant == null) || (servant instanceof NullServant)) {
            return request.getProtocolHandler().createSystemExceptionResponse(request, wrapper.badSkeleton(), null);
        } else {
            return request.getProtocolHandler().createSystemExceptionResponse(request, wrapper.getinterfaceNotImplemented(), null);
        }
    }
}

// End of file.
