/*
 * Copyright (c) 2018, 2020 Oracle and/or its affiliates.
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

package com.sun.corba.ee.impl.folb;

import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.protocol.ClientDelegate;
import com.sun.corba.ee.spi.transport.ContactInfoList;

import org.omg.CORBA.portable.ObjectImpl;

import static com.meterware.simplestub.Stub.createStrictStub;

public class StubObject extends ObjectImpl {
    static org.omg.CORBA.Object createObjectWithIOR(IOR ior) {
        StubObject result = new StubObject();
        result._set_delegate(createDelegateWithIOR(ior));
        return result;
    }

    private static TestClientDelegate createDelegateWithIOR(IOR ior) {
        TestClientDelegate delegate = createStrictStub(TestClientDelegate.class);
        delegate.setContactInfoList(createInfoListWithIOR(ior));
        return delegate;
    }

    private static TestContactInfoList createInfoListWithIOR(IOR ior) {
        TestContactInfoList infoList = createStrictStub(TestContactInfoList.class);
        infoList.setTargetIOR(ior);
        return infoList;
    }

    public String[] _ids() {
        return new String[0];
    }

    abstract static class TestClientDelegate extends ClientDelegate {
        private ContactInfoList contactInfoList;

        public void setContactInfoList(ContactInfoList contactInfoList) {
            this.contactInfoList = contactInfoList;
        }

        public ContactInfoList getContactInfoList() {
            return contactInfoList;
        }
    }

    abstract static class TestContactInfoList implements ContactInfoList {
        private IOR ior;

        public void setTargetIOR(IOR ior) {
            this.ior = ior;
        }

        public IOR getTargetIOR() {
            return ior;
        }
    }
}
