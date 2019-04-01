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

/**
 * Provides the <tt>DomainManager</tt> with the means to access policies.
 * <P>
 * The <tt>DomainManager</tt> has associated with it the policy objects for a particular domain. The domain manager also
 * records the membership of the domain and provides the means to add and remove members. The domain manager is itself a
 * member of a domain, possibly the domain it manages. The domain manager provides mechanisms for establishing and
 * navigating relationships to superior and subordinate domains and creating and accessing policies.
 */

public interface DomainManagerOperations {
    /**
     * This returns the policy of the specified type for objects in this domain. The types of policies available are domain
     * specific. See the CORBA specification for a list of standard ORB policies.
     *
     * @param policy_type Type of policy to request
     */
    public org.omg.CORBA.Policy get_domain_policy(int policy_type);
}
