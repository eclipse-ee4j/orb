/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.ior;

import org.omg.CORBA_2_3.portable.InputStream;

import com.sun.corba.ee.impl.encoding.EncapsOutputStream;
import com.sun.corba.ee.impl.encoding.OutputStreamFactory;
import com.sun.corba.ee.spi.orb.ORB;

/**
 * Base class to use for implementing TaggedComponents. It implements the getIOPComponent method using the
 * TaggedComponent.write() method.
 *
 * @author Ken Cavanaugh
 */
public abstract class TaggedComponentBase extends IdentifiableBase implements TaggedComponent {
    public org.omg.IOP.TaggedComponent getIOPComponent(org.omg.CORBA.ORB orb) {
        EncapsOutputStream os = OutputStreamFactory.newEncapsOutputStream((ORB) orb);
        os.write_ulong(getId()); // Fix for 6158378
        write(os);
        InputStream is = (InputStream) (os.create_input_stream());
        return org.omg.IOP.TaggedComponentHelper.read(is);
    }
}
