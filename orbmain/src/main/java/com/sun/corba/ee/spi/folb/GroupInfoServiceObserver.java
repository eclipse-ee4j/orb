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
// Created       : 2005 Jun 13 (Mon) 11:04:09 by Harold Carr.
// Last Modified : 2005 Aug 08 (Mon) 17:59:49 by Harold Carr.
//

package com.sun.corba.ee.spi.folb;

/**
 * @author Harold Carr
 */
public interface GroupInfoServiceObserver
{
    /**
     * Called when the GroupInfoService that you register with
     * has a change.  You should call the GroupInfoService
     * <code>getClusterInstanceInfo</code> method to get
     * updated info.
     */
    public void membershipChange();
}

// End of file.
