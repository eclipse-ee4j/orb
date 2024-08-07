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
import com.sun.corba.ee.spi.ior.TaggedComponent;
import com.sun.corba.ee.spi.ior.TaggedProfileTemplate;
import com.sun.corba.ee.spi.ior.iiop.IIOPProfile;
import com.sun.corba.ee.spi.ior.iiop.IIOPProfileTemplate;
import org.omg.CORBA.ORB;
import org.omg.CORBA_2_3.portable.OutputStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.meterware.simplestub.Stub.createStrictStub;

abstract class TestIOR implements IOR {

    static TestIOR createIORWithTaggedComponents(int id, org.omg.IOP.TaggedComponent... components) {
        TestIOR ior = createStrictStub(TestIOR.class);
        ior.setProfile(createIIOPProfileWithTaggedComponents(id, components));
        return ior;
    }

    private IIOPProfile profile;

    public IIOPProfile getProfile() {
        return profile;
    }

    public void setProfile(IIOPProfile profile) {
        this.profile = profile;
    }

    static TestIIOPProfile createIIOPProfileWithTaggedComponents(int id, org.omg.IOP.TaggedComponent... components) {
        TestIIOPProfile profile = createStrictStub(TestIIOPProfile.class);
        profile.setProfileTemplate(createIIOPProfileTemplateWithTaggedComponents(id, components));
        return profile;
    }

    abstract static class TestIIOPProfile implements IIOPProfile {

        private TaggedProfileTemplate profileTemplate;

        public TaggedProfileTemplate getTaggedProfileTemplate() {
            return profileTemplate;
        }

        public void setProfileTemplate(TaggedProfileTemplate profileTemplate) {
            this.profileTemplate = profileTemplate;
        }

    }

    static TestIIOPProfileTemplate createIIOPProfileTemplateWithTaggedComponents(int id, org.omg.IOP.TaggedComponent... components) {
        TestIIOPProfileTemplate template = createStrictStub(TestIIOPProfileTemplate.class);
        for (org.omg.IOP.TaggedComponent component : components)
            template.addTaggedComponent(id, component);
        return template;
    }

    abstract static class TestIIOPProfileTemplate implements IIOPProfileTemplate {
        private Map<Integer, List<TaggedComponent>> taggedComponents = new HashMap<Integer, List<TaggedComponent>>();

        public void addTaggedComponent(int id, org.omg.IOP.TaggedComponent component) {
            getTaggedComponentList(id).add(new TestTaggedComponent(id, component));
        }

        public Iterator<TaggedComponent> iteratorById(int id) {
            return getTaggedComponentList(id).iterator();
        }

        private List<TaggedComponent> getTaggedComponentList(int id) {
            List<TaggedComponent> componentList = taggedComponents.get(id);
            if (componentList == null) {
                componentList = new ArrayList<TaggedComponent>();
                taggedComponents.put(id, componentList);
            }
            return componentList;
        }
    }

    static class TestTaggedComponent implements TaggedComponent {

        private int id;
        private org.omg.IOP.TaggedComponent iopComponent;

        TestTaggedComponent(int id, org.omg.IOP.TaggedComponent iopComponent) {
            this.id = id;
            this.iopComponent = iopComponent;
        }

        public org.omg.IOP.TaggedComponent getIOPComponent(ORB orb) {
            return iopComponent;
        }

        public int getId() {
            return id;
        }

        public void write(OutputStream outputStream) {
        }
    }
}
