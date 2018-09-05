/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

//
// Created       : 2005 Jul 29 (Fri) 07:52:10 by Harold Carr.
// Last Modified : 2005 Aug 29 (Mon) 14:09:31 by Harold Carr.
//

package com.sun.corba.ee.impl.folb;

import java.util.List;
import org.omg.IOP.TaggedComponent;
import org.omg.PortableInterceptor.IORInfo;
import com.sun.corba.ee.spi.folb.ClusterInstanceInfo;
import com.sun.corba.ee.spi.folb.ClusterInstanceInfo;
import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.transport.SocketInfo;

/**
 * An implementation of this interface <code>org.omg.CORBA.LocalObject</code>.
 * An instance of this interface is plugged into the ORB via
 * <code>ORB.register_initial_reference(ORBConstants.CSIv2SSLTaggedComponentHandler, instance)</code>.
 *
 * @author Harold Carr
 */
public interface CSIv2SSLTaggedComponentHandler
{
    /**
     * @param iorInfo - from IORInterceptor.establish_components.
     * @param clusterInstanceInfo On the server-side, the FOLB system will pass all ClusterInstanceInfo
     * to the CSIv2/SSL system.  
     * @return null or org.omg.IOP.TaggedComponent.
     * The CSIv2SSL system returns <code>null</code> if no security
     * information is to be added to IORs.  Otherwise it returns the
     * CSIv2SSL <code>org.omg.IOP.TaggedComponent</code> that will be
     * added to IORs.
     */
    public TaggedComponent insert(IORInfo iorInfo, 
                                  List<ClusterInstanceInfo> clusterInstanceInfo);

    /** Extract is called on each invocation of the IOR, so that the security code can
     * run properly.
     * If the given IOR contains CSIv2SSL host/port
     * info that should be used for this invocation then
     * extract should return a List of SocketInfo. 
     * Otherwise it should return null.
     * @param ior The target ior of the current invocation.
     * @return List of all SocketInfos found in the IOR.
     */
    public List<SocketInfo> extract(IOR ior); 
}

// End of file.


