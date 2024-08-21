/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

module org.glassfish.corba.orb {

    requires java.desktop;
    requires java.logging;
    requires java.naming;
    requires java.rmi;
    requires java.sql;

    requires org.glassfish.corba.internal;
    requires org.glassfish.corba.omgapi;

    requires org.glassfish.gmbal.api;

    requires org.glassfish.pfl.basic;
    requires org.glassfish.pfl.dynamic;
    requires org.glassfish.pfl.tf;

    requires osgi.core;

    exports com.sun.corba.ee.impl.javax.rmi;
    exports com.sun.corba.ee.impl.javax.rmi.CORBA;
    exports com.sun.corba.ee.impl.legacy.connection;
    exports com.sun.corba.ee.impl.orb;
    exports com.sun.corba.ee.impl.util;
    exports com.sun.corba.ee.spi.transport;

    opens com.sun.corba.ee.impl.oa.poa;
    opens com.sun.corba.ee.impl.oa.toa;
    opens com.sun.corba.ee.spi.ior;
    opens com.sun.corba.ee.spi.orb;

}
