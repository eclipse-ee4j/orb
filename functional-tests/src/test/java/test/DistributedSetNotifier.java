/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package test;

/**
 * DistributedSetNotifier provides a simple interface for DistributedSetMonitor
 * instances to provide event notification. 
 *
 * @version     1.0, 5/13/98
 * @author      Bryan Atsatt
 */
public interface DistributedSetNotifier {
    public void pinged (String fromSetName);
    public void setAdded (String setName);
    public void setRemoved (String setName, boolean died);
    public void messageReceived (String message, String fromSetName);
}

