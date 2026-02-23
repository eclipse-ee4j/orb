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

package com.sun.corba.ee.impl.ior.iiop;

import com.sun.corba.ee.impl.orb.ORBVersionImpl;
import com.sun.corba.ee.spi.ior.ObjectId;
import com.sun.corba.ee.spi.ior.ObjectKeyTemplate;
import com.sun.corba.ee.spi.ior.iiop.IIOPProfileTemplate;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.orb.ORBVersion;

import org.junit.Test;

import static com.meterware.simplestub.Stub.createStrictStub;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class IIOPProfileImplTest {
    private ORB orb = createStrictStub(ORB.class);
    private ObjectId oid = createStrictStub(ObjectId.class);
    private IIOPProfileTemplate profileTemplate = createStrictStub(IIOPProfileTemplate.class);
    private ObjectKeyTemplateStub objectKeyTemplate = createStrictStub(ObjectKeyTemplateStub.class);

    private IIOPProfileImpl iiopProfile = new IIOPProfileImpl(orb, objectKeyTemplate, oid, profileTemplate);

    @Test
    public void whenForeignProfileOrbVersion_isLocalReturnsFalse() throws Exception {
        setOrbVersion(ORBVersionImpl.FOREIGN);

        assertThat(iiopProfile.isLocal(), is(false));
    }

    private void setOrbVersion(ORBVersion orbVersion) {
        objectKeyTemplate.setOrbVersion(orbVersion);
    }

    abstract static class ObjectKeyTemplateStub implements ObjectKeyTemplate {

        private ORBVersion orbVersion = ORBVersionImpl.NEW;

        public void setOrbVersion(ORBVersion orbVersion) {
            this.orbVersion = orbVersion;
        }

        @Override
        public ORBVersion getORBVersion() {
            return orbVersion;
        }
    }

}
