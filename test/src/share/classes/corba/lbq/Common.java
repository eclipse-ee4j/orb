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
// Created       : 2005 Oct 05 (Wed) 14:11:24 by Harold Carr.
// Last Modified : 2005 Oct 05 (Wed) 15:07:07 by Harold Carr.
//

package corba.lbq;

import java.util.Vector;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Properties ;

import java.rmi.Remote ;

import java.lang.reflect.Field ;

import java.security.AccessController ;
import java.security.PrivilegedAction ;

import javax.rmi.PortableRemoteObject ;

import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;

import org.omg.PortableServer.POA ;
import org.omg.PortableServer.Servant ;

import org.omg.PortableInterceptor.ORBInitializer ;

import com.sun.corba.ee.spi.orb.ORB ;
import com.sun.corba.ee.spi.orb.ORBData ;

import com.sun.corba.ee.impl.orb.ORBDataParserImpl ;
import com.sun.corba.ee.impl.orb.ORBImpl ;

import com.sun.corba.ee.spi.misc.ORBConstants ;

public class Common
{
    public static final String ReferenceName = "Test";
    private static final String NameService = "NameService";

    public static org.omg.CORBA.Object resolve(String name, ORB orb)
        throws 
            Exception
    {
        return getNameService(orb).resolve(makeNameComponent(name));
    }

    public static org.omg.CORBA.Object rebind(String name,
                                              org.omg.CORBA.Object ref,
                                              ORB orb)
        throws 
            Exception
    {
        NamingContext nc = getNameService(orb);
        nc.rebind(makeNameComponent(name), ref);
        return ref;
    }
  
    public static NameComponent[] makeNameComponent(String name)
    {
        Vector result = new Vector();
        StringTokenizer tokens = new StringTokenizer(name, "/");
        while (tokens.hasMoreTokens()) {
            result.addElement(tokens.nextToken());
        }
        NameComponent path[] = new NameComponent[result.size()];
        for (int i = 0; i < result.size(); ++i) {
            path[i] = new NameComponent((String)result.elementAt(i), "");
        }
        return path;
    }

    public static NamingContext getNameService(ORB orb) throws Exception
    {
        return NamingContextHelper.narrow(
            orb.resolve_initial_references(NameService));
    }

    public static void addORBInitializer( ORB orb, ORBInitializer init ) {

        final ORBData odata = orb.getORBData() ;

        // Add init to the end of a copy of the ORBInitializers
        // from the ORBData.
        final ORBInitializer[] oldOrbInits = odata.getORBInitializers() ;
        final int newIndex = oldOrbInits.length ;
        final ORBInitializer[] newOrbInits = new ORBInitializer[newIndex+1] ;
        for (int ctr=0; ctr<newIndex; ctr++)
            newOrbInits[ctr] = oldOrbInits[ctr] ;
        newOrbInits[newIndex] = init ;

        // Nasty hack: Use reflection to set the private field!
        // REVISIT: AS 9 ORB has an ORB API for setting ORBInitializers.
        AccessController.doPrivileged(
            new PrivilegedAction() {
                public Object run() {
                    try {
                        final Field fld = 
                            ORBDataParserImpl.class.getDeclaredField( 
                                "orbInitializers" ) ;
                        fld.setAccessible( true ) ;
                        fld.set( odata, newOrbInits ) ;
                        return null ;
                    } catch (Exception exc) {
                      exc.printStackTrace();
                        throw new RuntimeException( 
                            "Could not set ORBData.orbInitializers", exc ) ;
                    }
                }
            }
        )  ;
    }

    public static ORB makeControlPlaneORB( String initialHost, int initialPort ) {
        try {
            Properties props = new Properties() ;
            props.setProperty( ORBConstants.INITIAL_HOST_PROPERTY, initialHost ) ;
            props.setProperty( ORBConstants.INITIAL_PORT_PROPERTY, "" + initialPort ) ;
            ORB result = new ORBImpl() ;
            result.set_parameters( props ) ;
            POA rootPOA = (POA) result.resolve_initial_references("RootPOA") ;
            rootPOA.the_POAManager().activate() ;
            return result ;
        } catch (Exception exc) {
            throw new RuntimeException( exc ) ;
        }
    }

    /** Create an object reference for the given servant and register it
     * with the given name.
     * @param orb The ORB to use for this object reference.
     * @param impl The object implementation to use.
     * @param remoteClass The class type of the servant.
     * @param name The name (in comp/comp form) to use for binding the reference
     * to the name service.
     */
    public static void makeObject(
        ORB orb, Remote impl, String name ) {

        try {
            Servant servant = (Servant)javax.rmi.CORBA.Util.getTie( impl ) ;
            byte[] id = name.getBytes() ;
            POA rootPOA = (POA) orb.resolve_initial_references("RootPOA") ;
            rootPOA.activate_object_with_id( id, servant ) ;
            org.omg.CORBA.Object ref = rootPOA.id_to_reference( id ) ;
            rebind( name, ref, orb ) ;  
        } catch (Exception exc) {
            throw new RuntimeException( exc ) ;
        }
    }

    public static <T> T getObject( ORB orb, String name, Class<T> type ) {
        try {
            org.omg.CORBA.Object obj = resolve( name, orb ) ;
            return type.cast( PortableRemoteObject.narrow( obj, type ) ) ;
        } catch (Exception exc) {
            throw new RuntimeException( exc ) ;
        }
    }

    /** Return all objrefs of type T in the naming context identified by name.
     * Limited to at most 1000 objrefs (to avoid writing more complex code).
     */
    public static <T> List<T> getObjects( ORB orb, String name, Class<T> type ) {
        try {
            org.omg.CORBA.Object obj = resolve( name, orb ) ;
            NamingContext nc = NamingContextHelper.narrow( obj ) ;
            BindingListHolder blh = new BindingListHolder() ;
            BindingIteratorHolder bih = new BindingIteratorHolder() ;
            nc.list( 1000, blh, bih ) ;
            List<T> result = new ArrayList<T>() ;
            for (Binding bind : blh.value) {
                try {
                    if (bind.binding_type.value() == BindingType._nobject) {
                        org.omg.CORBA.Object elem = nc.resolve( bind.binding_name ) ;
                        result.add( type.cast( PortableRemoteObject.narrow( elem,
                            type ) ));
                    }
                } catch (Exception exc) {
                    // elem was not an object of type type: ignore it.
                }
            }

            return result ;
        } catch (Exception exc) {
            throw new RuntimeException( exc ) ;
        }
    }
}

// End of file.
