/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.legacy.interceptor;

import com.sun.corba.ee.spi.orb.ORB ;

/** The interface defines an extension to the standard ORBInitInfo 
 * that gives access to the ORB being initialized.  Interceptors run
 * as the last stage of initialization of the ORB, so the ORB
 * instance returned by getORB is fully initialized.  Note that
 * this facility eventually shows up post-CORBA 3.0 as a result 
 * of the resolution of OMG core issue on accessing the ORB from
 * local objects.
 */
public interface ORBInitInfoExt 
{
    ORB getORB() ;
}
