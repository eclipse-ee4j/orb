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

package com.sun.corba.ee.impl.resolver ;

import com.sun.corba.ee.spi.resolver.Resolver ;

import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;
import java.util.HashSet;

import java.io.File;
import java.io.FileInputStream;

import com.sun.corba.ee.spi.orb.ORB ;

import com.sun.corba.ee.impl.misc.CorbaResourceUtil ;

public class FileResolverImpl implements Resolver
{
    private ORB orb ;
    private File file ;
    private Properties savedProps ;
    private long fileModified = 0 ;

    public FileResolverImpl( ORB orb, File file )
    {
        this.orb = orb ;
        this.file = file ;
        savedProps = new Properties() ;
    }

    public org.omg.CORBA.Object resolve( String name ) 
    {
        check() ;
        String stringifiedObject = savedProps.getProperty( name ) ;
        if (stringifiedObject == null) {
            return null;
        }
        return orb.string_to_object( stringifiedObject ) ;
    }

    public Set<String> list()
    {
        check() ;

        Set result = new HashSet() ;

        // Obtain all the keys from the property object
        Enumeration theKeys = savedProps.propertyNames();
        while (theKeys.hasMoreElements()) {
            result.add( theKeys.nextElement() ) ;
        }

        return result ;
    }

    /**
    * Checks the lastModified() timestamp of the file and optionally
    * re-reads the Properties object from the file if newer.
    */
    private void check() 
    {
        if (file == null) {
            return;
        }

        long lastMod = file.lastModified();
        if (lastMod > fileModified) {
            try {
                FileInputStream fileIS = new FileInputStream(file);
                savedProps.clear();
                savedProps.load(fileIS);
                fileIS.close();
                fileModified = lastMod;
            } catch (java.io.FileNotFoundException e) {
                System.err.println( CorbaResourceUtil.getText(
                    "bootstrap.filenotfound", file.getAbsolutePath()));
            } catch (java.io.IOException e) {
                System.err.println( CorbaResourceUtil.getText(
                    "bootstrap.exception",
                    file.getAbsolutePath(), e.toString()));
            }
        }
    }
}
