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

package corba.copyobjectpolicy;

import java.lang.reflect.Method ;

import com.sun.corba.ee.spi.orb.ORB ;
import com.sun.corba.ee.spi.orb.ORBConfigurator ;
import com.sun.corba.ee.spi.orb.DataCollector ;

import com.sun.corba.ee.spi.copyobject.CopierManager ;
import com.sun.corba.ee.spi.copyobject.CopyobjectDefaults ;

import corba.framework.TraceAccumulator ;
import corba.framework.ProxyInterceptor ;
import org.glassfish.pfl.dynamic.copyobject.spi.ObjectCopierFactory;

public class UserConfigurator implements ORBConfigurator 
{
    // All of these statics are needed in the main test
    public static final int VALUE_INDEX = 1 ;
    public static final int REFERENCE_INDEX = 2 ;

    public static final String VALUE_NAME = "ValueInterceptor" ;
    public static final String REFERENCE_NAME = "ReferenceInterceptor" ;

    // The TraceAccumular connected to the ProxyInterceptors 
    // used in this test.
    public static final TraceAccumulator traceAccum =
        new TraceAccumulator() ;

    public static final Method makeMethod ;
    
    static {
        try {
            makeMethod = ObjectCopierFactory.class.
                getDeclaredMethod( "make" ) ;
        } catch (Exception exc) {
            throw new RuntimeException( 
                "Cannot find ObjectCopierFactory.make() method", exc ) ;
        }
    } 

    private ProxyInterceptor makePI( String name, ObjectCopierFactory factory ) 
    {
        ProxyInterceptor result = ProxyInterceptor.make(
            name,  new Class[] { ObjectCopierFactory.class }, factory ) ;
        result.addListener( traceAccum ) ;
        result.addMethod( makeMethod ) ;

        return result ;
    }

    /** Set up two copiers: the value copier, and the reference
     * copier.  Make the value copier the default .
     */
    public void configure( DataCollector dc, ORB orb ) 
    {
        CopierManager cm = orb.getCopierManager() ;
        cm.setDefaultId( VALUE_INDEX ) ;

        ObjectCopierFactory value =
            CopyobjectDefaults.makeORBStreamObjectCopierFactory( orb ) ;
        ProxyInterceptor valuePI = makePI( VALUE_NAME,
            value ) ;
        cm.registerObjectCopierFactory( 
            (ObjectCopierFactory)valuePI.getActual(), 
            VALUE_INDEX ) ;

        ObjectCopierFactory reference =
            CopyobjectDefaults.getReferenceObjectCopierFactory( ) ;
        ProxyInterceptor referencePI = makePI( REFERENCE_NAME,
            reference ) ;
        cm.registerObjectCopierFactory( 
            (ObjectCopierFactory)referencePI.getActual(), 
            REFERENCE_INDEX ) ;
    }
}
