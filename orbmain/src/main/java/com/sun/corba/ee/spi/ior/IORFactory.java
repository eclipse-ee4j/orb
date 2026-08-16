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

import com.sun.corba.ee.spi.orb.ORB;

/**
 * An IORFactory provides the capability of creating IORs. It contains some collection of TaggedProfileTemplates, which
 * can be iterated over for portable interceptors.
 */
public interface IORFactory extends Writeable, MakeImmutable {
    /**
     * Construct an IOR containing the given ORB, typeid, and ObjectId. The same ObjectId will be used for all
     * TaggedProfileTemplates in the IORFactory.
     * 
     * @param orb ORB to use
     * @param typeid typeid of IOR
     * @param oid objectId IOR
     * @return created IOR
     */
    IOR makeIOR(ORB orb, String typeid, ObjectId oid);

    /**
     * Return true iff this.makeIOR(orb,typeid,oid).isEquivalent( other.makeIOR(orb,typeid,oid) for all orb, typeid, and
     * oid.
     * 
     * @param other factory to compare with
     * @return true if they are equivalent
     */
    boolean isEquivalent(IORFactory other);
}
