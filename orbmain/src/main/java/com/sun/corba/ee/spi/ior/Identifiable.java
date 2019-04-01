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

import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.Description;

/**
 * This interface represents an entity that can be written to an OutputStream and has an identity that is represented by
 * an integer. This identity is essentially the type of the entity, and is used in order to know how to read the entity
 * back from an InputStream.
 *
 * @author Ken Cavanaugh
 */
public interface Identifiable extends Writeable {
    /**
     * Return the (type) identity of this entity.
     *
     * @return int
     */
    @ManagedAttribute
    @Description("Id of tagged component or profile")
    public int getId();
}
