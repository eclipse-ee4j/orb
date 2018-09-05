/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.resolver;

import java.util.List ;
import java.util.Map ;
import java.util.Comparator ;
import java.util.Iterator ;
import java.util.HashMap ;
import java.util.ArrayList ;
import java.util.Collections ;

import org.omg.CosNaming.NamingContextExt ;
import org.omg.CosNaming.NamingContextExtHelper ;
import org.omg.CORBA.ORBPackage.InvalidName ;

import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.ior.IORTemplate;
import com.sun.corba.ee.spi.ior.ObjectKey;
import com.sun.corba.ee.spi.ior.IORFactories;
import com.sun.corba.ee.spi.ior.iiop.IIOPAddress;
import com.sun.corba.ee.spi.ior.iiop.IIOPProfileTemplate ;
import com.sun.corba.ee.spi.ior.iiop.IIOPFactories ;
import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;
import com.sun.corba.ee.spi.ior.iiop.AlternateIIOPAddressComponent;
import com.sun.corba.ee.spi.orb.Operation;
import com.sun.corba.ee.spi.orb.ORB;

import com.sun.corba.ee.impl.encoding.EncapsInputStream;
import com.sun.corba.ee.impl.encoding.EncapsInputStreamFactory;
import com.sun.corba.ee.spi.logging.ORBUtilSystemException ;
import com.sun.corba.ee.spi.logging.OMGSystemException ;
import com.sun.corba.ee.impl.naming.namingutil.INSURLHandler;
import com.sun.corba.ee.impl.naming.namingutil.IIOPEndpointInfo;
import com.sun.corba.ee.impl.naming.namingutil.INSURL;
import com.sun.corba.ee.impl.naming.namingutil.CorbalocURL;
import com.sun.corba.ee.impl.naming.namingutil.CorbanameURL;
import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.impl.misc.ORBUtility;

/** 
 * This class provides an Operation that converts from CORBA INS URL strings into
 * CORBA object references.  It will eventually become extensible, but for now it
 * simply encapsulates the existing implementation.  Once the full extensibility
 * is in place, we want this operation to convert string to INSURL, which has mainly
 * a public resolver method that returns an object reference.
 * 
 * @author  Hemanth
 * @author  Ken
 */
public class INSURLOperationImpl implements Operation
{
    private ORB orb;
    private static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;
    private static final OMGSystemException omgWrapper =
        OMGSystemException.self ;

    // Root Naming Context for default resolution of names.
    private NamingContextExt rootNamingContextExt;
    private Object rootContextCacheLock = new Object() ;

    // The URLHandler to parse INS URL's
    private INSURLHandler insURLHandler = INSURLHandler.getINSURLHandler() ;

    public INSURLOperationImpl( ORB orb )
    {
        this.orb = orb ;
    }

    private static final int NIBBLES_PER_BYTE = 2 ;
    private static final int UN_SHIFT = 4 ; // "UPPER NIBBLE" shift factor for <<
    
    /** This static method takes a Stringified IOR and converts it into IOR object.
      * It is the caller's responsibility to only pass strings that start with "IOR:".
      */
    private org.omg.CORBA.Object getIORFromString( String str )
    {
        // Length must be even for str to be valid
        if ( (str.length() & 1) == 1 ) {
            throw wrapper.badStringifiedIorLen();
        }

        byte[] buf = new byte[(str.length() - ORBConstants.STRINGIFY_PREFIX.length()) / NIBBLES_PER_BYTE];
        for (int i=ORBConstants.STRINGIFY_PREFIX.length(), j=0; i < str.length(); i +=NIBBLES_PER_BYTE, j++) {
             buf[j] = (byte)((ORBUtility.hexOf(str.charAt(i)) << UN_SHIFT) & 0xF0);
             buf[j] |= (byte)(ORBUtility.hexOf(str.charAt(i+1)) & 0x0F);
        }
        EncapsInputStream s = EncapsInputStreamFactory.newEncapsInputStream(orb, buf, buf.length, 
            orb.getORBData().getGIOPVersion());
        s.consumeEndian();
        return s.read_Object() ;
    }

    public Object operate( Object arg ) 
    {
        if (arg instanceof String) {
            String str = (String)arg ;

            if (str.startsWith( ORBConstants.STRINGIFY_PREFIX )) {
                return getIORFromString(str);
            } else {
                INSURL insURL = insURLHandler.parseURL( str ) ;
                if (insURL == null) {
                    throw omgWrapper.soBadSchemeName(str);
                }
                return resolveINSURL( insURL ) ;
            }
        }

        throw wrapper.stringExpected() ;
    }

    private org.omg.CORBA.Object resolveINSURL( INSURL theURLObject ) {
        if( theURLObject.isCorbanameURL() ) {
            return resolveCorbaname( (CorbanameURL)theURLObject );
        } else {
            return resolveCorbaloc( (CorbalocURL)theURLObject );
        }
    }
      
    /**
     *  resolves a corbaloc: url that is encapsulated in a CorbalocURL object.
     * 
     *  @return the CORBA.Object if resolution is successful
     */
    private org.omg.CORBA.Object resolveCorbaloc(
        CorbalocURL theCorbaLocObject ) 
    {
        org.omg.CORBA.Object result = null;
        // If RIR flag is true use the Bootstrap protocol
        // Bug 6678177 noticed that this is incorrect: rir means use 
        // resolve_initial_references on the local ORB!
        if( theCorbaLocObject.getRIRFlag( ) )  {
            String keyString = theCorbaLocObject.getKeyString() ;
            if (keyString.equals( "" )) {
                keyString = "NameService";
            }

            try {
                result = orb.resolve_initial_references( keyString ) ;
            } catch (InvalidName exc) {
                throw omgWrapper.soBadSchemaSpecific( exc, keyString ) ;
            }
        } else {
            result = getIORUsingCorbaloc( theCorbaLocObject );
        }

        return result;
    }

    /**
     *  resolves a corbaname: url that is encapsulated in a CorbanameURL object.
     * 
     *  @return the CORBA.Object if resolution is successful
     */
    private org.omg.CORBA.Object resolveCorbaname( CorbanameURL theCorbaName ) {
        org.omg.CORBA.Object result = null;

        try {
            NamingContextExt theNamingContext = null;

            if( theCorbaName.getRIRFlag( ) ) {
                // Case 1 of corbaname: rir#
                theNamingContext = getDefaultRootNamingContext( );
            } else {
                // Case 2 of corbaname: ::hostname#
                org.omg.CORBA.Object corbalocResult = 
                    getIORUsingCorbaloc( theCorbaName );
                if( corbalocResult == null ) {
                    return null;
                }

                theNamingContext = 
                    NamingContextExtHelper.narrow( corbalocResult );
            }

            String StringifiedName = theCorbaName.getStringifiedName( );

            if( StringifiedName == null ) {
                // This means return the Root Naming context
                return theNamingContext;
            } else {
                return theNamingContext.resolve_str( StringifiedName );
            }
        } catch( Exception e ) {
            clearRootNamingContextCache( );
            // Bug 6475580
            throw omgWrapper.soBadSchemaSpecific( e,
                theCorbaName.getStringifiedName() ) ;
        }
     }

    /**
     *  This is an internal method to get the IOR from the CorbalocURL object.
     * 
     *  @return the CORBA.Object if resolution is successful
     */
    private org.omg.CORBA.Object getIORUsingCorbaloc( INSURL corbalocObject ) 
    {
        Map     profileMap = new HashMap();
        List    profileList1_0 = new ArrayList();

        // corbalocObject cannot be null, because it's validated during
        // parsing. So no null check is required.
        java.util.List theEndpointInfo = corbalocObject.getEndpointInfo();
        String theKeyString = corbalocObject.getKeyString();
        // If there is no KeyString then it's invalid
        if( theKeyString == null ) {
            return null;
        }

        ObjectKey key = orb.getObjectKeyFactory().create( 
            theKeyString.getBytes() );
        IORTemplate iortemp = IORFactories.makeIORTemplate( key.getTemplate() );

        java.util.Iterator iterator = theEndpointInfo.iterator( );
        while( iterator.hasNext( ) ) {
            IIOPEndpointInfo element = 
                (IIOPEndpointInfo) iterator.next( );
            IIOPAddress addr = IIOPFactories.makeIIOPAddress( element.getHost(), 
                element.getPort() );
            GIOPVersion giopVersion = GIOPVersion.getInstance( (byte)element.getMajor(), 
                                             (byte)element.getMinor());
            IIOPProfileTemplate profileTemplate = null;
            if (giopVersion.equals(GIOPVersion.V1_0)) {
                profileTemplate = IIOPFactories.makeIIOPProfileTemplate(
                    orb, giopVersion, addr);
                profileList1_0.add(profileTemplate);
            } else {
                if (profileMap.get(giopVersion) == null) {
                    profileTemplate = IIOPFactories.makeIIOPProfileTemplate(
                        orb, giopVersion, addr);
                    profileMap.put(giopVersion, profileTemplate);
                } else {
                    profileTemplate = (IIOPProfileTemplate)profileMap.get(giopVersion);
                    AlternateIIOPAddressComponent iiopAddressComponent =
                                IIOPFactories.makeAlternateIIOPAddressComponent(addr);
                    profileTemplate.add(iiopAddressComponent);
                }
            }
        }

        GIOPVersion giopVersion = orb.getORBData().getGIOPVersion();
        IIOPProfileTemplate pTemplate = (IIOPProfileTemplate)profileMap.get(giopVersion);
        if (pTemplate != null) {
            iortemp.add(pTemplate); // Add profile for GIOP version used by this ORB
            profileMap.remove(giopVersion); // Now remove this value from the map
        }

        // Create a comparator that can sort in decending order (1.2, 1.1, ...)
        Comparator comp = new Comparator() {
            public int compare(Object o1, Object o2) {
                GIOPVersion gv1 = (GIOPVersion)o1;
                GIOPVersion gv2 = (GIOPVersion)o2;
                return (gv1.lessThan(gv2) ? 1 : (gv1.equals(gv2) ? 0 : -1));
            };
        };

        // Now sort using the above comparator
        List list = new ArrayList(profileMap.keySet());
        Collections.sort(list, comp);

        // Add the profiles in the sorted order
        Iterator iter = list.iterator();
        while (iter.hasNext()) {
            IIOPProfileTemplate pt = (IIOPProfileTemplate)profileMap.get(iter.next());
            iortemp.add(pt);
        }

        // Finally add the 1.0 profiles
        iortemp.addAll(profileList1_0);

        IOR ior = iortemp.makeIOR( orb, "", key.getId() ) ;
        return ORBUtility.makeObjectReference( ior ) ;
    }

    /**
     *  This is required for corbaname: resolution. Currently we
     *  are not caching RootNamingContext as the reference to rootNamingContext
     *  may not be Persistent in all the implementations. 
     *  _REVISIT_ to clear the rootNamingContext in case of COMM_FAILURE.
     * 
     *  @return the org.omg.COSNaming.NamingContextExt if resolution is 
     *   successful
     *  
     */
    private NamingContextExt getDefaultRootNamingContext( ) {
        synchronized( rootContextCacheLock ) {
            if( rootNamingContextExt == null ) {
                try {
                    rootNamingContextExt =
                        NamingContextExtHelper.narrow(
                        orb.getLocalResolver().resolve( "NameService" ) );
                } catch( Exception e ) {
                    rootNamingContextExt = null;
                }
            }
        }
        return rootNamingContextExt;
    }

    /**
     *  A utility method to clear the RootNamingContext, if there is an
     *  exception in resolving CosNaming:Name from the RootNamingContext,
     */
    private void clearRootNamingContextCache( ) {
        synchronized( rootContextCacheLock ) {
            rootNamingContextExt = null;
        }
    }
}
