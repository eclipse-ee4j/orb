/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1993-1997 IBM Corp. All rights reserved.
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

package javax.rmi.CORBA;

import java.io.File;
import java.io.FileInputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Properties;

class GetORBPropertiesFileAction implements PrivilegedAction {
    private boolean debug = false ;

    public GetORBPropertiesFileAction () {
    }

    private String getSystemProperty(final String name) {
        // This will not throw a SecurityException because this
        // class was loaded from rt.jar using the bootstrap classloader.
        String propValue = (String) AccessController.doPrivileged(
            new PrivilegedAction() {
                public java.lang.Object run() {
                    return System.getProperty(name);
                }
            }
        );

        return propValue;
    }

    private void getPropertiesFromFile( Properties props, String fileName )
    {
        try {
            File file = new File( fileName ) ;
            if (!file.exists())
                return ;

            FileInputStream in = new FileInputStream( file ) ;
            
            try {
                props.load( in ) ;
            } finally {
                in.close() ;
            }
        } catch (Exception exc) {
            if (debug)
                System.out.println( "ORB properties file " + fileName + 
                    " not found: " + exc) ;
        }
    }

    public Object run() 
    {
        Properties defaults = new Properties() ;

        String javaHome = getSystemProperty( "java.home" ) ;
        String fileName = javaHome + File.separator + "lib" + File.separator +
            "orb.properties" ;

        getPropertiesFromFile( defaults, fileName ) ;

        Properties results = new Properties( defaults ) ;

        String userHome = getSystemProperty( "user.home" ) ;
        fileName = userHome + File.separator + "orb.properties" ;

        getPropertiesFromFile( results, fileName ) ;
        return results ;
    }
}
