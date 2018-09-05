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

/**
 * @author Harold Carr
 */
public interface Selector
{
    public void setTimeout(long timeout);
    public long getTimeout();
    public void registerInterestOps(EventHandler eventHandler);
    public void registerForEvent(EventHandler eventHander);
    public void unregisterForEvent(EventHandler eventHandler);
    public void close();
}

// End of file.








