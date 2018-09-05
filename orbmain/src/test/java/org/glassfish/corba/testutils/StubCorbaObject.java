/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.corba.testutils;

import org.omg.CORBA.*;
import org.omg.CORBA.Object;

public class StubCorbaObject implements org.omg.CORBA.Object {
    public boolean _is_a(String s) {
        return false;
    }

    public boolean _is_equivalent(org.omg.CORBA.Object object) {
        return false;
    }

    public boolean _non_existent() {
        return false;
    }

    public int _hash(int i) {
        return 0;
    }

    public Object _duplicate() {
        return null;
    }

    public void _release() {
    }

    public Object _get_interface_def() {
        return null;
    }

    public Request _request(String s) {
        return null;
    }

    public Request _create_request(Context context, String s, NVList nvList, NamedValue namedValue) {
        return null;
    }

    public Request _create_request(Context context, String s, NVList nvList, NamedValue namedValue, ExceptionList exceptionList, ContextList contextList) {
        return null;
    }

    public Policy _get_policy(int i) {
        return null;
    }

    public DomainManager[] _get_domain_managers() {
        return new DomainManager[0];
    }

    public Object _set_policy_override(Policy[] policies, SetOverrideType setOverrideType) {
        return null;
    }
}
