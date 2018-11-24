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

import org.glassfish.external.statistics.CountStatistic;

/**
 * @author Harold Carr
 */
public interface ConnectionCache {
    public String getMonitoringName();

    public String getCacheType();

    public void stampTime(Connection connection);

    public long numberOfConnections();

    public long numberOfIdleConnections();

    public long numberOfBusyConnections();

    public boolean reclaim();

    /**
     * Close all connections in the connection cache. This is used as a final cleanup, and will result in abrupt termination
     * of any pending communications.
     */
    public void close();
}

// End of file.
