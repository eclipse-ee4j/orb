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

import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.portable.ServantObject;

import com.sun.corba.ee.spi.protocol.LocalClientRequestDispatcher;

/**
 * @author Harold Carr
 */

public class NotLocalLocalCRDImpl implements LocalClientRequestDispatcher {
    public boolean useLocalInvocation(org.omg.CORBA.Object self) {
        return false;
    }

    public boolean is_local(org.omg.CORBA.Object self) {
        return false;
    }

    public ServantObject servant_preinvoke(org.omg.CORBA.Object self, String operation, Class expectedType) {
        // REVISIT: Rewrite rmic.HelloTest and rmic.LocalStubTest
        // (which directly call servant_preinvoke)
        // then revert to exception again.
        return null;
        // throw new INTERNAL();
    }

    public void servant_postinvoke(org.omg.CORBA.Object self, ServantObject servant) {
        // throw new INTERNAL();
    }
}

// End of file.
