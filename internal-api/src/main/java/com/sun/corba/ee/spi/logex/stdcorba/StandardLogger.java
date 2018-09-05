/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.logex.stdcorba;

import org.glassfish.pfl.basic.logex.ExceptionWrapper;
import org.glassfish.pfl.basic.logex.WrapperGenerator;

/**
 *
 * @author ken_admin
 */
public class StandardLogger extends WrapperGenerator.ExtensionBase {
    public static final StandardLogger self = new StandardLogger() ;

    private static final String SPI_PREFIX = "com.sun.corba.ee.spi." ;
    private static final String IMPL_PREFIX = "com.sun.corba.ee.impl." ;
    public static final String CORBA_LOGGER_PREFIX =
        "javax.enterprise.resource.corba" ;

    @Override
    public String getLoggerName( Class<?> cls ) {
        final ExceptionWrapper ew = cls.getAnnotation( ExceptionWrapper.class ) ;
        if (!ew.loggerName().equals( "" ))  {
            return ew.loggerName() ;
        }

        final String name = cls.getPackage().getName() ;
        String shortName ;
        if (name.startsWith( SPI_PREFIX )) {
            shortName = name.substring( SPI_PREFIX.length() ) ;
        } else if (name.startsWith( IMPL_PREFIX )) {
            shortName = name.substring( IMPL_PREFIX.length() ) ;
        } else {
            shortName = name ;
        }

        return CORBA_LOGGER_PREFIX + "." + shortName ;
    }
}
