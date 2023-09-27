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
