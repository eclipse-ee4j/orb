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

package com.sun.corba.ee.impl.orb ;

import java.applet.Applet ;
import java.util.Properties ;
import java.net.URL ;

import com.sun.corba.ee.spi.orb.DataCollector ;

public abstract class DataCollectorFactory {
    private DataCollectorFactory() {}

    public static DataCollector create( Applet app, Properties props, 
        String localHostName )
    {
        String appletHost = localHostName ;

        if (app != null) {
            URL appletCodeBase = app.getCodeBase() ;
            
            if (appletCodeBase != null)
                appletHost = appletCodeBase.getHost() ;
        }

        return new AppletDataCollector( app, props, localHostName, 
            appletHost ) ;
    }

    public static DataCollector create( String[] args, Properties props, 
        String localHostName )
    {
        return new NormalDataCollector( args, props, localHostName, 
            localHostName ) ;
    }

    public static DataCollector create( Properties props, 
        String localHostName ) 
    {
        return new PropertyOnlyDataCollector( props, localHostName,
            localHostName ) ;
    }
}
