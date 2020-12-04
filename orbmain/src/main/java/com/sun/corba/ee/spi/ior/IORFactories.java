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

package com.sun.corba.ee.spi.ior ;

import java.io.Serializable ;

import org.omg.CORBA_2_3.portable.InputStream ;

import org.omg.CORBA.BAD_PARAM ;
import org.omg.CORBA.portable.ValueFactory ;

import org.omg.PortableInterceptor.ObjectReferenceTemplate ;
import org.omg.PortableInterceptor.ObjectReferenceFactory ;

import com.sun.corba.ee.impl.ior.ObjectIdImpl ;
import com.sun.corba.ee.impl.ior.ObjectKeyImpl ;
import com.sun.corba.ee.impl.ior.IORImpl ;
import com.sun.corba.ee.impl.ior.IORTemplateImpl ;
import com.sun.corba.ee.impl.ior.IORTemplateListImpl ;
import com.sun.corba.ee.impl.ior.ObjectReferenceProducerBase ;
import com.sun.corba.ee.impl.ior.ObjectReferenceFactoryImpl ;
import com.sun.corba.ee.impl.ior.ObjectReferenceTemplateImpl ;
import com.sun.corba.ee.impl.ior.ObjectKeyFactoryImpl ;

import com.sun.corba.ee.impl.misc.ORBUtility ;

import com.sun.corba.ee.spi.orb.ORB ;

/** This class provides a number of factory methods for creating
 * various IOR SPI classes which are not subclassed for specific protocols.
 * The following types must be created using this class:
 * <ul>
 * <li>ObjectId</li>
 * <li>ObjectKey</li>
 * <li>IOR</li>
 * <li>IORTemplate</li>
 * </ul>
 */
public class IORFactories {
    private IORFactories() {} 

    /** Create an ObjectId for the given byte sequence.
     * @param id bytes to set as object id
     * @return created ObjectId
     */
    public static ObjectId makeObjectId( byte[] id )
    {
        return new ObjectIdImpl( id ) ;
    }

    /** Create an ObjectKey for the given ObjectKeyTemplate and
     * ObjectId.
     * @param oktemp Template of object
     * @param oid id of object
     * @return created ObjectKey
     */
    public static ObjectKey makeObjectKey( ObjectKeyTemplate oktemp, ObjectId oid )
    {
        return new ObjectKeyImpl( oktemp, oid ) ;
    }

    /** Create an empty IOR for the given orb and typeid.  The result is mutable.
     * @param orb orb to create IOR from
     * @param typeid id of type for IOR
     * @return created IOR
     */
    public static IOR makeIOR( ORB orb, String typeid ) 
    {
        return new IORImpl( orb, typeid ) ;
    }

    /** Create an empty IOR for the given orb with a null typeid.  The result is mutable.
     * @param orb ORB to create IOR for
     * @return an empty IOR
     */
    public static IOR makeIOR( ORB orb ) 
    {
        return new IORImpl( orb ) ;
    }

    /** Read an IOR from an InputStream.  ObjectKeys are not shared.
     * @param orb ORB to use as factory
     * @param is stream to read from
     * @return IOR read from stream
     */
    public static IOR makeIOR( ORB orb, InputStream is )
    {
        return new IORImpl( orb, is ) ;
    }

    /** Create an IORTemplate with the given ObjectKeyTemplate.  The result
     * is mutable.
     * @param oktemp template to use
     * @return created IORTemplate
     */
    public static IORTemplate makeIORTemplate( ObjectKeyTemplate oktemp )
    {
        return new IORTemplateImpl( oktemp ) ;
    }

    /** Read an IORTemplate from an InputStream.
     * @param is stream to read from
     * @return template read from stream
     */
    public static IORTemplate makeIORTemplate( InputStream is )
    {
        return new IORTemplateImpl( is ) ;
    }

    public static IORTemplateList makeIORTemplateList() 
    {
        return new IORTemplateListImpl() ;
    }

    public static IORTemplateList makeIORTemplateList( InputStream is ) 
    {
        return new IORTemplateListImpl( is ) ;
    }

    public static IORFactory getIORFactory( ObjectReferenceTemplate ort ) 
    {
        if (ort instanceof ObjectReferenceTemplateImpl) {
            ObjectReferenceTemplateImpl orti = 
                (ObjectReferenceTemplateImpl)ort ;
            return orti.getIORFactory() ;
        }

        throw new BAD_PARAM() ;
    }

    public static IORTemplateList getIORTemplateList( ObjectReferenceFactory orf ) 
    {
        if (orf instanceof ObjectReferenceProducerBase) {
            ObjectReferenceProducerBase base =
                (ObjectReferenceProducerBase)orf ;
            return base.getIORTemplateList() ;
        }

        throw new BAD_PARAM() ;
    }

    public static ObjectReferenceTemplate makeObjectReferenceTemplate( ORB orb, 
        IORTemplate iortemp ) 
    {
        return new ObjectReferenceTemplateImpl( orb, iortemp ) ;
    }

    public static ObjectReferenceFactory makeObjectReferenceFactory( ORB orb, 
        IORTemplateList iortemps )
    {
        return new ObjectReferenceFactoryImpl( orb, iortemps ) ;
    }

    public static ObjectKeyFactory makeObjectKeyFactory( ORB orb ) 
    {
        return new ObjectKeyFactoryImpl( orb ) ;
    }

    public static org.omg.CORBA.Object makeObjectReference( IOR ior ) 
    {
        return ORBUtility.makeObjectReference( ior ) ;
    }

    /** This method must be called in order to register the value
     * factories for the ObjectReferenceTemplate and ObjectReferenceFactory
     * value types.
     * @param orb ORB to register value factory against
     */
    public static void registerValueFactories( ORB orb ) 
    {
        // Create and register the factory for the Object Reference Template
        // implementation.
        ValueFactory vf = new ValueFactory() {
            public Serializable read_value( InputStream is ) 
            {
                return new ObjectReferenceTemplateImpl( is ) ;
            }
        } ;

        orb.register_value_factory( ObjectReferenceTemplateImpl.repositoryId, vf ) ;

        // Create and register the factory for the Object Reference Factory
        // implementation.
        vf = new ValueFactory() {
            public Serializable read_value( InputStream is ) 
            {
                return new ObjectReferenceFactoryImpl( is ) ;
            }
        } ;

        orb.register_value_factory( ObjectReferenceFactoryImpl.repositoryId, vf ) ;
    }
}
