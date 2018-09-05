/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.omg.CORBA;

/** Provides mechanisms for establishing and navigating relationships to
 *  superior and subordinate domains, as well as for creating and accessing
 *  policies. The <tt>DomainManager</tt> has associated with it the policy
 *  objects for a
 *  particular domain. The domain manager also records the membership of
 *  the domain and provides the means to add and remove members. The domain
 *  manager is itself a member of a domain, possibly the domain it manages.
 *  The domain manager provides mechanisms for establishing and navigating
 *  relationships to superior and subordinate domains and
 *  creating and accessing policies.
 */

public interface DomainManager extends DomainManagerOperations, 
    org.omg.CORBA.Object, org.omg.CORBA.portable.IDLEntity
{
}

