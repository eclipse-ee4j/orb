/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.transport;

import java.util.List;

import com.sun.corba.ee.spi.ior.IOR;

public interface IORToSocketInfo
{
    /** Used to extract socket address information from an IOR.
     * @param ior The ior from which the socket info is extracted.
     * @param previous The previous list, which may be reused if not null.
     * @return a list of SocketInfo.
     */
    public List<? extends SocketInfo> getSocketInfo(IOR ior, 
        List<? extends SocketInfo> previous);
}

// End of file.
