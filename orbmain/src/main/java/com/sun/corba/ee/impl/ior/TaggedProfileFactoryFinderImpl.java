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

import com.sun.corba.ee.spi.ior.TaggedProfile;

import com.sun.corba.ee.spi.orb.ORB;

import org.omg.CORBA_2_3.portable.InputStream;

/**
 * @author
 */
public class TaggedProfileFactoryFinderImpl extends IdentifiableFactoryFinderBase<TaggedProfile> {
    public TaggedProfileFactoryFinderImpl(ORB orb) {
        super(orb);
    }

    public TaggedProfile handleMissingFactory(int id, InputStream is) {
        return new GenericTaggedProfile(id, is);
    }
}
