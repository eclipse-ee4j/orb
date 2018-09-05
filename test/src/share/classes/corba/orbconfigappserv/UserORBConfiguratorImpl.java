/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

//
// Created       : 2003 Apr 15 (Tue) 15:36:45 by Harold Carr.
// Last Modified : 2003 Apr 15 (Tue) 16:36:40 by Harold Carr.
//

package corba.orbconfigappserv;

import org.omg.CORBA.INITIALIZE;
import com.sun.corba.ee.spi.orb.ORB ;
import com.sun.corba.ee.spi.orb.ORBConfigurator;
import com.sun.corba.ee.spi.orb.ParserImplBase;
import com.sun.corba.ee.spi.orb.PropertyParser;
import com.sun.corba.ee.spi.orb.DataCollector;
import com.sun.corba.ee.spi.orb.OperationFactory;

public class UserORBConfiguratorImpl 
    implements 
        ORBConfigurator 
{
    public static String propertyName = "userConfigProperty";

    private static class ConfigParser extends ParserImplBase {
        private ORB orb ;

        public ConfigParser( ORB orb ) {
            this.orb = orb ;
        }

        public Class testclass = 
            corba.orbconfigappserv.UserORBConfiguratorImpl.class;

        public PropertyParser makeParser()
        {
            PropertyParser parser = new PropertyParser() ;
            parser.add( propertyName,
                        OperationFactory.classAction(orb.classNameResolver()), 
                        "testclass" ) ;
            return parser ;
        }
    }

    public void configure( DataCollector dc, ORB orb ) {
        ConfigParser parser = new ConfigParser( orb );
        parser.init( dc );
        Class theTestclass = parser.testclass;

        if (theTestclass != null) {
            try {
                Object o = theTestclass.newInstance();
                System.out.println("UserORBConfiguratorImpl.configure: " + o);
            } catch (Exception ex) {
                throw new org.omg.CORBA.INITIALIZE(ex.toString());
            }
        }
    }   
}
