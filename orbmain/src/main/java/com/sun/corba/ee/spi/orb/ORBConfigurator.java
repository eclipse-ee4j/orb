/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.orb ;

/** Interface used to configure an ORB instance.  The DataCollector dc has all
 * available config info available.  The configure method may constructor a 
 * parser, call dc.setParser( parser ), get the consolidated properties from dc,
 * and parse this information.  The configure method may also register ORB
 * components with the ORB and perform other parts of ORB initialization.
 * Implementations of this interface must have a public no-args constructor.
 */
public interface ORBConfigurator {
    void configure( DataCollector dc, ORB orb ) ;
}

