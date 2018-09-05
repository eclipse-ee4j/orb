/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.orb ;

import java.applet.Applet ;
import java.util.Properties ;

public class AppletDataCollector extends DataCollectorBase {
    private Applet applet ;

    AppletDataCollector( Applet app, Properties props, String localHostName,
        String configurationHostName ) 
    {
        super( props, localHostName, configurationHostName ) ;
        this.applet = app ;
    }

    public boolean isApplet() 
    {
        return true ;
    }

    protected void collect( )
    {
        checkPropertyDefaults() ;

        findPropertiesFromFile() ;

        // We do not use system properties for applets in order to 
        // avoid security exceptions.

        findPropertiesFromProperties() ;
        findPropertiesFromApplet( applet ) ;
    }
}
