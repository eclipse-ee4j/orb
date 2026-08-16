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

package com.sun.corba.ee.spi.orb;

/**
 * Interface used to configure an ORB instance. The DataCollector dc has all available config info available. The
 * configure method may constructor a parser, call dc.setParser( parser ), get the consolidated properties from dc, and
 * parse this information. The configure method may also register ORB components with the ORB and perform other parts of
 * ORB initialization. Implementations of this interface must have a public no-args constructor.
 */
public interface ORBConfigurator {
    void configure(DataCollector dc, ORB orb);
}
