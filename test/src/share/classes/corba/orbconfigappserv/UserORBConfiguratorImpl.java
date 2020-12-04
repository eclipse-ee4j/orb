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
