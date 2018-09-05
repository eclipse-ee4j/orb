/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.ior.iiop ;

import org.omg.CORBA_2_3.portable.InputStream ;

import com.sun.corba.ee.spi.ior.Identifiable ;
import com.sun.corba.ee.spi.ior.IdentifiableFactory ;
import com.sun.corba.ee.spi.ior.EncapsulationFactoryBase ;
import com.sun.corba.ee.spi.ior.ObjectId ;
import com.sun.corba.ee.spi.ior.ObjectKeyTemplate ;

import com.sun.corba.ee.spi.ior.iiop.IIOPAddress ;
import com.sun.corba.ee.spi.ior.iiop.IIOPProfileTemplate ;
import com.sun.corba.ee.spi.ior.iiop.GIOPVersion ;

import com.sun.corba.ee.spi.orb.ORB ;

import com.sun.corba.ee.spi.folb.ClusterInstanceInfo ;

import com.sun.corba.ee.impl.encoding.MarshalInputStream ;

import com.sun.corba.ee.impl.ior.iiop.IIOPAddressImpl ;
import com.sun.corba.ee.impl.ior.iiop.CodeSetsComponentImpl ;
import com.sun.corba.ee.impl.ior.iiop.AlternateIIOPAddressComponentImpl ;
import com.sun.corba.ee.impl.ior.iiop.JavaCodebaseComponentImpl ;
import com.sun.corba.ee.impl.ior.iiop.MaxStreamFormatVersionComponentImpl ;
import com.sun.corba.ee.impl.ior.iiop.JavaSerializationComponent;
import com.sun.corba.ee.impl.ior.iiop.ORBTypeComponentImpl ;
import com.sun.corba.ee.impl.ior.iiop.IIOPProfileImpl ;
import com.sun.corba.ee.impl.ior.iiop.IIOPProfileTemplateImpl ;
import com.sun.corba.ee.impl.ior.iiop.RequestPartitioningComponentImpl ;
import com.sun.corba.ee.impl.ior.iiop.LoadBalancingComponentImpl ;
import com.sun.corba.ee.impl.ior.iiop.ClusterInstanceInfoComponentImpl ;
import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.spi.misc.ORBConstants;

import org.omg.IOP.TAG_ALTERNATE_IIOP_ADDRESS ;
import org.omg.IOP.TAG_CODE_SETS ;
import org.omg.IOP.TAG_JAVA_CODEBASE ;
import org.omg.IOP.TAG_RMI_CUSTOM_MAX_STREAM_FORMAT ;
import org.omg.IOP.TAG_ORB_TYPE ;
import org.omg.IOP.TAG_INTERNET_IOP ;

/** This class provides all of the factories for the IIOP profiles and
 * components.  This includes direct construction of profiles and templates,
 * as well as constructing factories that can be registered with an
 * IdentifiableFactoryFinder.
 */
public abstract class IIOPFactories {
    private IIOPFactories() {}

    public static IdentifiableFactory makeRequestPartitioningComponentFactory()
    {
        return new EncapsulationFactoryBase(ORBConstants.TAG_REQUEST_PARTITIONING_ID) {
            public Identifiable readContents(InputStream in)
            {
                int threadPoolToUse = in.read_ulong();
                Identifiable comp = 
                    new RequestPartitioningComponentImpl(threadPoolToUse);
                return comp;
            }
        };
    } 

    public static RequestPartitioningComponent makeRequestPartitioningComponent(
            int threadPoolToUse)
    {
        return new RequestPartitioningComponentImpl(threadPoolToUse);
    }

    public static IdentifiableFactory makeLoadBalancingComponentFactory()
    {
        return new EncapsulationFactoryBase(ORBConstants.TAG_LOAD_BALANCING_ID) {
            public Identifiable readContents(InputStream in)
            {
                int loadBalancingValue = in.read_ulong();
                Identifiable comp = 
                    new LoadBalancingComponentImpl(loadBalancingValue);
                return comp;
            }
        };
    } 

    public static LoadBalancingComponent makeLoadBalancingComponent(
            int loadBalancingValue)
    {
        return new LoadBalancingComponentImpl(loadBalancingValue);
    }

    public static IdentifiableFactory makeClusterInstanceInfoComponentFactory()
    {
        return new EncapsulationFactoryBase(
            ORBConstants.FOLB_MEMBER_ADDRESSES_TAGGED_COMPONENT_ID) {

            public Identifiable readContents(InputStream in)
            {
                final ClusterInstanceInfo cinfo = new ClusterInstanceInfo( in ) ;
                Identifiable comp = 
                    new ClusterInstanceInfoComponentImpl(cinfo);
                return comp;
            }
        };
    } 

    public static ClusterInstanceInfoComponent makeClusterInstanceInfoComponent(
        ClusterInstanceInfo cinfo)
    {
        return new ClusterInstanceInfoComponentImpl(cinfo);
    }

    public static IdentifiableFactory makeAlternateIIOPAddressComponentFactory()
    {
        return new EncapsulationFactoryBase(TAG_ALTERNATE_IIOP_ADDRESS.value) {
            public Identifiable readContents( InputStream in ) 
            {
                IIOPAddress addr = new IIOPAddressImpl( in ) ;
                Identifiable comp = 
                    new AlternateIIOPAddressComponentImpl( addr ) ;
                return comp ;
            }
        } ;
    } 

    public static AlternateIIOPAddressComponent makeAlternateIIOPAddressComponent(
        IIOPAddress addr )
    {
        return new AlternateIIOPAddressComponentImpl( addr ) ;
    }

    public static IdentifiableFactory makeCodeSetsComponentFactory()
    {
        return new EncapsulationFactoryBase(TAG_CODE_SETS.value) {
            public Identifiable readContents( InputStream in ) 
            {
                return new CodeSetsComponentImpl( in ) ;
            }
        } ;
    }
    
    public static CodeSetsComponent makeCodeSetsComponent( ORB orb )
    {
        return new CodeSetsComponentImpl( orb ) ;
    }
        
    public static IdentifiableFactory makeJavaCodebaseComponentFactory()
    {
        return new EncapsulationFactoryBase(TAG_JAVA_CODEBASE.value) {
            public Identifiable readContents( InputStream in ) 
            {
                String url = in.read_string() ;
                Identifiable comp = new JavaCodebaseComponentImpl( url ) ;
                return comp ;
            }
        } ;
    }

    public static JavaCodebaseComponent makeJavaCodebaseComponent( 
        String codebase ) 
    {
        return new JavaCodebaseComponentImpl( codebase ) ;
    }

    public static IdentifiableFactory makeORBTypeComponentFactory()
    {
        return new EncapsulationFactoryBase(TAG_ORB_TYPE.value) {
            public Identifiable readContents( InputStream in ) 
            {
                int type = in.read_ulong() ;
                Identifiable comp = new ORBTypeComponentImpl( type ) ;
                return comp ;
            }
        } ;
    }

    public static ORBTypeComponent makeORBTypeComponent( int type ) 
    {
        return new ORBTypeComponentImpl( type ) ;
    }

    public static IdentifiableFactory makeMaxStreamFormatVersionComponentFactory()
    {
        return new EncapsulationFactoryBase(TAG_RMI_CUSTOM_MAX_STREAM_FORMAT.value) {
            public Identifiable readContents(InputStream in)
            {
                byte version = in.read_octet() ;
                Identifiable comp = new MaxStreamFormatVersionComponentImpl(version);
                return comp ;
            }
        };
    } 

    public static MaxStreamFormatVersionComponent makeMaxStreamFormatVersionComponent()
    {
        return new MaxStreamFormatVersionComponentImpl() ;
    }

    public static IdentifiableFactory makeJavaSerializationComponentFactory() {
        return new EncapsulationFactoryBase(
                                ORBConstants.TAG_JAVA_SERIALIZATION_ID) {
            public Identifiable readContents(InputStream in) {
                byte version = in.read_octet();
                Identifiable cmp = new JavaSerializationComponent(version);
                return cmp;
            }
        };
    }

    public static JavaSerializationComponent makeJavaSerializationComponent() {
        return JavaSerializationComponent.singleton();
    }

    public static IdentifiableFactory makeIIOPProfileFactory()
    {
        return new EncapsulationFactoryBase(TAG_INTERNET_IOP.value) {
            public Identifiable readContents( InputStream in ) 
            {
                Identifiable result = new IIOPProfileImpl( in ) ;
                return result ;
            }
        } ;
    }

    public static IIOPProfile makeIIOPProfile( ORB orb, ObjectKeyTemplate oktemp,
        ObjectId oid, IIOPProfileTemplate ptemp )
    {
        return new IIOPProfileImpl( orb, oktemp, oid, ptemp ) ;
    }

    public static IIOPProfile makeIIOPProfile( ORB orb, 
        org.omg.IOP.TaggedProfile profile )
    {
        return new IIOPProfileImpl( orb, profile ) ;
    }

    public static IdentifiableFactory makeIIOPProfileTemplateFactory()
    {
        return new EncapsulationFactoryBase(TAG_INTERNET_IOP.value) {
            public Identifiable readContents( InputStream in ) 
            {
                Identifiable result = new IIOPProfileTemplateImpl( in ) ;
                return result ;
            }
        } ;
    }

    public static IIOPProfileTemplate makeIIOPProfileTemplate( ORB orb, 
        GIOPVersion version, IIOPAddress primary ) 
    {
        return new IIOPProfileTemplateImpl( orb, version, primary ) ;
    }

    public static IIOPAddress makeIIOPAddress( String host, int port ) 
    {
        return new IIOPAddressImpl( host, port ) ;
    }

    public static IIOPAddress makeIIOPAddress( InputStream is ) 
    {
        return new IIOPAddressImpl( is ) ;
    }
}
