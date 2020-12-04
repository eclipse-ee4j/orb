/*
 * Copyright (c) 2017, 2020 Oracle and/or its affiliates.
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

package com.sun.corba.ee.spi;

/**
 * Public constants for interaction with the Glassfish JNDI implementation.
 */
public interface JndiConstants {

    /** The name of the class used to create context factories for COSNaming. */
    String COSNAMING_CONTEXT_FACTORY = "com.sun.jndi.cosnaming.CNCtxFactory";

    /** The JDK's registry context factory. */
    String REGISTRY_CONTEXT_FACTORY = "com.sun.jndi.rmi.registry.RegistryContextFactory";

    /** A System Property which may be defined as equal to a trusted classpath URL. */
    String TRUST_URLCODEBASE = "com.sun.jndi.cosnaming.object.trustURLCodebase";

    /** A system property which can be set to true to enable JNDI federation */
    String ENABLE_FEDERATION = "com.sun.jndi.cosnaming.federation";
}
