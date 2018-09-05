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

import java.net.InetAddress ;

import java.util.Properties ;

import org.omg.CORBA.INTERNAL ;
import org.omg.CORBA.CompletionStatus ;

public class PropertyOnlyDataCollector extends DataCollectorBase 
{
    public PropertyOnlyDataCollector( Properties props, 
        String localHostName, String configurationHostName ) 
    {
        super( props, localHostName, configurationHostName ) ;
    }

    public boolean isApplet() 
    {
        return false ;
    }

    protected void collect()
    {
        checkPropertyDefaults() ;

        findPropertiesFromProperties() ;
    }
}
