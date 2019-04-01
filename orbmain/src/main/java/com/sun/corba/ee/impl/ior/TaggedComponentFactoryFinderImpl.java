/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.ior;

import com.sun.corba.ee.spi.ior.TaggedComponent;
import com.sun.corba.ee.spi.ior.TaggedComponentFactoryFinder;

import com.sun.corba.ee.impl.encoding.EncapsOutputStream;
import com.sun.corba.ee.impl.encoding.OutputStreamFactory;
import com.sun.corba.ee.spi.orb.ORB;

import org.omg.CORBA_2_3.portable.InputStream;

/**
 * @author Ken Cavanaugh
 */
public class TaggedComponentFactoryFinderImpl extends IdentifiableFactoryFinderBase<TaggedComponent> implements TaggedComponentFactoryFinder {
    public TaggedComponentFactoryFinderImpl(ORB orb) {
        super(orb);
    }

    public TaggedComponent handleMissingFactory(int id, InputStream is) {
        return new GenericTaggedComponent(id, is);
    }

    public TaggedComponent create(org.omg.CORBA.ORB orb, org.omg.IOP.TaggedComponent comp) {
        EncapsOutputStream os = OutputStreamFactory.newEncapsOutputStream((ORB) orb);
        org.omg.IOP.TaggedComponentHelper.write(os, comp);
        InputStream is = (InputStream) (os.create_input_stream());
        // Skip the component ID: we just wrote it out above
        is.read_ulong();

        return (TaggedComponent) create(comp.tag, is);
    }
}
