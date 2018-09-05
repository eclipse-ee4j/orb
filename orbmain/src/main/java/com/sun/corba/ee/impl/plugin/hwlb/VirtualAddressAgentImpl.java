/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.plugin.hwlb ;

import java.lang.reflect.Field ;

import java.security.AccessController ;
import java.security.PrivilegedAction ;

import java.util.Iterator ;

import org.omg.CORBA.LocalObject ;

import org.omg.CORBA_2_3.portable.InputStream ;

import org.omg.IOP.TAG_INTERNET_IOP ;

import org.omg.PortableInterceptor.ORBInitializer ;
import org.omg.PortableInterceptor.IORInterceptor_3_0 ;
import org.omg.PortableInterceptor.IORInfo ;
import org.omg.PortableInterceptor.ORBInitInfo ;
import org.omg.PortableInterceptor.ObjectReferenceTemplate ;

import com.sun.corba.ee.spi.orb.ORB ;
import com.sun.corba.ee.spi.orb.ORBData ;
import com.sun.corba.ee.spi.orb.ORBConfigurator ;
import com.sun.corba.ee.spi.orb.DataCollector ;
import com.sun.corba.ee.spi.orb.ParserImplBase ;
import com.sun.corba.ee.spi.orb.PropertyParser ;
import com.sun.corba.ee.spi.orb.OperationFactory ;

import com.sun.corba.ee.spi.ior.Identifiable ;
import com.sun.corba.ee.spi.ior.IdentifiableFactoryFinder ;
import com.sun.corba.ee.spi.ior.EncapsulationFactoryBase ;
import com.sun.corba.ee.spi.ior.IORTemplate ;
import com.sun.corba.ee.spi.ior.IORFactories ;
import com.sun.corba.ee.spi.ior.TaggedProfileTemplate ;
import com.sun.corba.ee.spi.ior.TaggedComponent ;
import com.sun.corba.ee.spi.ior.TaggedProfile ;
import com.sun.corba.ee.spi.ior.ObjectKey ;
import com.sun.corba.ee.spi.ior.ObjectId ;
import com.sun.corba.ee.spi.ior.ObjectKeyTemplate ;

import com.sun.corba.ee.spi.ior.iiop.IIOPAddress ;
import com.sun.corba.ee.spi.ior.iiop.IIOPProfileTemplate ;
import com.sun.corba.ee.spi.ior.iiop.IIOPFactories ;
import com.sun.corba.ee.spi.ior.iiop.AlternateIIOPAddressComponent ;
import com.sun.corba.ee.spi.ior.iiop.GIOPVersion ;



import com.sun.corba.ee.impl.ior.iiop.IIOPProfileImpl ;
import com.sun.corba.ee.impl.ior.iiop.IIOPProfileTemplateImpl ;

import com.sun.corba.ee.spi.misc.ORBConstants ;

import com.sun.corba.ee.impl.orb.ORBDataParserImpl ;



import com.sun.corba.ee.impl.oa.poa.BadServerIdHandler ;

import com.sun.corba.ee.impl.interceptors.IORInfoImpl ;
import com.sun.corba.ee.spi.logging.ORBUtilSystemException;
import com.sun.corba.ee.spi.trace.Subcontract;
import org.glassfish.pfl.tf.spi.annotation.InfoMethod;

@Subcontract
public class VirtualAddressAgentImpl 
    extends LocalObject 
    implements ORBConfigurator, ORBInitializer, IORInterceptor_3_0
{
    private static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    public static final String VAA_HOST_PROPERTY = ORBConstants.SUN_PREFIX + 
        "ORBVAAHost" ;
    public static final String VAA_PORT_PROPERTY = ORBConstants.SUN_PREFIX + 
        "ORBVAAPort" ;
    private static final long serialVersionUID = 5670615031510472636L;

    private String host = null ;
    private int port = 0 ;
    private ORB orb = null ;
    private IIOPAddress addr = null ;
    private ORBInitializer[] newOrbInits = null ;

    @Subcontract
    private class AddressParser extends ParserImplBase {
        private String _host = null ;
        private int _port = 0 ;

        public PropertyParser makeParser() {
            PropertyParser parser = new PropertyParser() ;
            parser.add( VAA_HOST_PROPERTY, OperationFactory.stringAction(),
                "_host" ) ;
            parser.add( VAA_PORT_PROPERTY, OperationFactory.integerAction(),
                "_port" ) ;
            return parser ;
        }

        @Subcontract
        @Override
        protected void complete() {
            host = _host ;
            port = _port ;
        }
    }

    @InfoMethod 
    private void agentAddress( IIOPAddress addr ) { }

    @Subcontract
    public void configure( DataCollector dc, final ORB orb ) {
        this.orb = orb ;

        orb.setBadServerIdHandler( 
            new BadServerIdHandler() {
                public void handle( ObjectKey objectkey ) {
                    // NO-OP
                }
            }
        ) ;

        // Create a new parser to extract the virtual address
        // host/port information from the data collector
        final AddressParser parser = new AddressParser() ;
        parser.init( dc ) ;
        addr = IIOPFactories.makeIIOPAddress( host, port ) ;    
        agentAddress(addr);

        // Register the special IIOPProfile in the TaggedProfileFactoryFinder.
        // This means that the isLocal check will be handled properly even
        // when an objref leaves the server that created it and then comes
        // back and gets unmarshalled.
        IdentifiableFactoryFinder finder = 
            orb.getTaggedProfileFactoryFinder() ;
        finder.registerFactory( 
            new EncapsulationFactoryBase( TAG_INTERNET_IOP.value ) {
                public Identifiable readContents( InputStream in ) {
                    Identifiable result = new SpecialIIOPProfileImpl( in ) ;
                    return result ;
                }
            }
        ) ;

        // Add this object to the initializer list
        // by using a PropertyParser in a rather unusual fashion

        final ORBData odata = orb.getORBData() ;

        // Add this object to the end of a copy of the ORBInitializers
        // from the ORBData.
        final ORBInitializer[] oldOrbInits = odata.getORBInitializers() ;
        final int newIndex = oldOrbInits.length ;
        newOrbInits = new ORBInitializer[newIndex+1] ;
        for (int ctr=0; ctr<newIndex; ctr++)
            newOrbInits[ctr] = oldOrbInits[ctr] ;
        newOrbInits[newIndex] = this ;

        // Nasty hack: Use reflection to set the private field!
        // REVISIT: AS 9 has an ORB API for setting ORBInitializers.
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
                        throw wrapper.couldNotSetOrbInitializer( exc ) ;
                    }
                }
            }
        )  ;
    }

    @Subcontract
    public void pre_init( ORBInitInfo info ) {
        // NO-OP
    }

    @Subcontract
    public void post_init( ORBInitInfo info ) {
        // register this object as an IORInterceptor.
        try {
            info.add_ior_interceptor( this ) ;
        } catch (Exception exc) {
            wrapper.vaaErrorInPostInit( exc ) ;
        }
    }

    @Subcontract
    public void establish_components( IORInfo info ) {
        // NO-OP
    }

    // This is exactly like IIOPProfileImpl, except for isLocal.
    // Here isLocal is true iff the profile's primary address
    // is the address of the LB.
    @Subcontract
    private class SpecialIIOPProfileImpl extends
        IIOPProfileImpl {

        private boolean isLocalChecked = false ;
        private boolean isLocalCachedValue = false ;

        public SpecialIIOPProfileImpl( InputStream in ) {
            super( in ) ;
        }

        public SpecialIIOPProfileImpl( ORB orb, ObjectKeyTemplate oktemp,
            ObjectId id, IIOPProfileTemplate ptemp ) {
            super( orb, oktemp, id, ptemp ) ;
        }

        @InfoMethod
        private void iiopProfileTemplate( IIOPProfileTemplate temp ) { }

        @InfoMethod
        private void templateAddress( IIOPAddress addr ) { }

        @Subcontract
        @Override
        public boolean isLocal() {
            if (!isLocalChecked) {
                isLocalChecked = true ;

                IIOPProfileTemplate ptemp = 
                    (IIOPProfileTemplate)getTaggedProfileTemplate() ;

                iiopProfileTemplate(ptemp);
                templateAddress(addr);

                isLocalCachedValue = addr.equals( ptemp.getPrimaryAddress() ) ;
            }

            return isLocalCachedValue ;
        }
    }

    // This is exactly like IIOPProfileTemplateImpl, except that
    // create creates SpecialIIOPProfileImpl instead of IIOPProfileImpl.
    private class SpecialIIOPProfileTemplateImpl extends
        IIOPProfileTemplateImpl {
        
        private ORB orb ; 

        public SpecialIIOPProfileTemplateImpl( ORB orb, GIOPVersion version,
            IIOPAddress primary ) {
            super( orb, version, primary ) ;
            this.orb = orb ;
        }

        @Override
        public TaggedProfile create( ObjectKeyTemplate oktemp, ObjectId id ) {
            return new SpecialIIOPProfileImpl( orb, oktemp, id, this ) ;
        }
    }

    @Subcontract
    private TaggedProfileTemplate makeCopy( TaggedProfileTemplate temp ) {
        if (temp instanceof IIOPProfileTemplate) {
            final IIOPProfileTemplate oldTemplate = (IIOPProfileTemplate)temp ;

            // FINALLY, here is where we actualy replace the
            // default address (from the ORB configuration) that 
            // is normally used for IOR creation with the 
            // virtual adress of the external agent.
            //
            // However, we also want to change the behavior of 
            // the TaggedProfile.isLocal method, so that objrefs
            // created by this template are recognized as being local.
            // To do this, we need to subclass IIOPProfileImpl, overriding
            // the definition of isLocal, and then subclass 
            // IIOPProfileTemplateImpl, overriding the create method
            // to use the subclass of IIOPProfileImpl.
            final IIOPProfileTemplate result = 
                new SpecialIIOPProfileTemplateImpl(
                    orb, oldTemplate.getGIOPVersion(), addr ) ;

            final Iterator iter = oldTemplate.iterator() ;
            while (iter.hasNext()) {
                TaggedComponent comp = (TaggedComponent)iter.next() ;
                if (!(comp instanceof AlternateIIOPAddressComponent)) 
                    result.add( comp ) ;
            }
        
            return result ;
        } else {
            return temp ;
        }
    }

    @Subcontract
    public void components_established( IORInfo info ) {
        // Cast this to the implementation class in case we are building
        // this class on JDK 1.4, which has the pre-CORBA 3.0 version of
        // IOFInfo that does not have the adapter_template or current_factory
        // methods.
        IORInfoImpl myInfo = (IORInfoImpl)info ;

        // Get the object adapter's adapter_template as an IORTemplate
        final IORTemplate iort = 
            (IORTemplate)IORFactories.getIORFactory( 
                myInfo.adapter_template() ) ;

        // Make a copy of the original IORTempalte
        final IORTemplate result = IORFactories.makeIORTemplate( 
            iort.getObjectKeyTemplate() ) ;

        // Clone iort, but remove all TAG_ALTERNATE_ADDRESS components,
        // and change the primary address/port to be the host/port
        // values in this class.
        final Iterator iter = iort.iterator() ;
        while (iter.hasNext()) {
            TaggedProfileTemplate tpt = (TaggedProfileTemplate)iter.next() ;
            result.add( makeCopy( tpt ) ) ;
        }

        final ObjectReferenceTemplate newOrt = 
            IORFactories.makeObjectReferenceTemplate( orb, result ) ;

        // Install the modified copy as the current_factory (instead of the
        // default behavior, which is simply to use adapter_template)
        myInfo.current_factory( newOrt );
    }

    public void adapter_manager_state_changed( int id,
        short state ) {
        // NO-OP
    }

    public void adapter_state_changed( ObjectReferenceTemplate[] templates,
        short state ) {
        // NO-OP
    }

    public String name() {
        return this.getClass().getName() ;
    }

    public void destroy() {
        // NO-OP
    }
}
