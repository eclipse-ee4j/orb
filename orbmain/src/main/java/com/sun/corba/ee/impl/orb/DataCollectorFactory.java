/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.orb;

import java.applet.Applet;
import java.util.Properties;
import java.net.URL;

import com.sun.corba.ee.spi.orb.DataCollector;

public abstract class DataCollectorFactory {
    private DataCollectorFactory() {
    }

    public static DataCollector create(Applet app, Properties props, String localHostName) {
        String appletHost = localHostName;

        if (app != null) {
            URL appletCodeBase = app.getCodeBase();

            if (appletCodeBase != null)
                appletHost = appletCodeBase.getHost();
        }

        return new AppletDataCollector(app, props, localHostName, appletHost);
    }

    public static DataCollector create(String[] args, Properties props, String localHostName) {
        return new NormalDataCollector(args, props, localHostName, localHostName);
    }

    public static DataCollector create(Properties props, String localHostName) {
        return new PropertyOnlyDataCollector(props, localHostName, localHostName);
    }
}
