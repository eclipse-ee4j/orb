/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.ior.iiop;

import com.sun.corba.ee.impl.encoding.EncapsInputStream;
import com.sun.corba.ee.impl.encoding.EncapsInputStreamFactory;
import com.sun.corba.ee.impl.encoding.EncapsOutputStream;
import com.sun.corba.ee.impl.encoding.OutputStreamFactory;
import com.sun.corba.ee.impl.ior.EncapsulationUtility;
import com.sun.corba.ee.impl.orb.ORBVersionImpl;
import com.sun.corba.ee.impl.util.JDKBridge;
import com.sun.corba.ee.spi.ior.IORFactories;
import com.sun.corba.ee.spi.ior.IdentifiableBase;
import com.sun.corba.ee.spi.ior.ObjectAdapterId;
import com.sun.corba.ee.spi.ior.ObjectId;
import com.sun.corba.ee.spi.ior.ObjectKey;
import com.sun.corba.ee.spi.ior.ObjectKeyTemplate;
import com.sun.corba.ee.spi.ior.TaggedComponent;
import com.sun.corba.ee.spi.ior.TaggedProfile;
import com.sun.corba.ee.spi.ior.TaggedProfileTemplate;
import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;
import com.sun.corba.ee.spi.ior.iiop.IIOPAddress;
import com.sun.corba.ee.spi.ior.iiop.IIOPFactories;
import com.sun.corba.ee.spi.ior.iiop.IIOPProfile;
import com.sun.corba.ee.spi.ior.iiop.IIOPProfileTemplate;
import com.sun.corba.ee.spi.ior.iiop.JavaCodebaseComponent;
import com.sun.corba.ee.spi.logging.IORSystemException;
import com.sun.corba.ee.spi.oa.ObjectAdapter;
import com.sun.corba.ee.spi.oa.ObjectAdapterFactory;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.orb.ORBVersion;
import com.sun.corba.ee.spi.protocol.RequestDispatcherRegistry;
import com.sun.corba.ee.spi.trace.IsLocal;
import org.glassfish.pfl.tf.spi.annotation.InfoMethod;
import org.omg.CORBA.SystemException;
import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.CORBA_2_3.portable.OutputStream;
import org.omg.IOP.TAG_INTERNET_IOP;
import org.omg.IOP.TAG_JAVA_CODEBASE;

import java.util.Iterator;

/**
 * @author
 */
@IsLocal
public class IIOPProfileImpl extends IdentifiableBase implements IIOPProfile
{
    private static final IORSystemException wrapper =
        IORSystemException.self ;

    private ORB orb ;
    private ObjectId oid;
    private IIOPProfileTemplate proftemp;
    private ObjectKeyTemplate oktemp ;
    private ObjectKey objectKey;

    // Cached lookups
    protected String codebase = null ;
    protected boolean cachedCodebase = false;

    private boolean checkedIsLocal = false ;
    private boolean cachedIsLocal = false ;

    // initialize-on-demand holder
    private static class LocalCodeBaseSingletonHolder {
        public static JavaCodebaseComponent comp ;

        static {
            String localCodebase = JDKBridge.getLocalCodebase() ;
            if (localCodebase == null) {
                comp = null;
            } else {
                comp = IIOPFactories.makeJavaCodebaseComponent(localCodebase);
            }
        }
    }

    private GIOPVersion giopVersion = null;

    public String toString() {
        StringBuilder sb = new StringBuilder() ;
        sb.append( "IIOPProfileImpl[proftemp=") ;
        sb.append( proftemp.toString() ) ;
        sb.append( " oktemp=" ) ;
        sb.append( oktemp.toString() ) ;
        sb.append( " oid=" ) ;
        sb.append( oid.toString() ) ;
        return sb.toString() ;
    }

    @Override
    public boolean equals( Object obj )
    {
        if (!(obj instanceof IIOPProfileImpl)) {
            return false;
        }

        IIOPProfileImpl other = (IIOPProfileImpl)obj ;

        return oid.equals( other.oid ) && proftemp.equals( other.proftemp ) &&
            oktemp.equals( other.oktemp ) ;
    }

    @Override
    public int hashCode()
    {
        return oid.hashCode() ^ proftemp.hashCode() ^ oktemp.hashCode() ;
    }

    public ObjectId getObjectId()
    {
        return oid ;
    }

    public TaggedProfileTemplate getTaggedProfileTemplate()
    {
        return proftemp ;
    }

    public ObjectKeyTemplate getObjectKeyTemplate() 
    {
        return oktemp ;
    }

    private IIOPProfileImpl( ORB orb ) 
    {
        this.orb = orb ;
    }

    public IIOPProfileImpl( ORB orb, ObjectKeyTemplate oktemp, ObjectId oid, 
        IIOPProfileTemplate proftemp )
    {
        this( orb ) ;
        this.oktemp = oktemp ;
        this.oid = oid ;
        this.proftemp = proftemp ;
    }

    public IIOPProfileImpl( InputStream is )
    {
        this( (ORB)(is.orb()) ) ;
        init( is ) ;
    }

    public IIOPProfileImpl( ORB orb, org.omg.IOP.TaggedProfile profile) 
    {
        this( orb ) ;

        if (profile == null || profile.tag != TAG_INTERNET_IOP.value ||
            profile.profile_data == null) {
            throw wrapper.invalidTaggedProfile() ;
        }

        EncapsInputStream istr = EncapsInputStreamFactory.newEncapsInputStream(orb, profile.profile_data, 
            profile.profile_data.length);
        istr.consumeEndian();
        init( istr ) ;
    }

    private void init( InputStream istr )
    {
        // First, read all of the IIOP IOR data
        GIOPVersion version = new GIOPVersion() ;
        version.read( istr ) ;
        IIOPAddress primary = new IIOPAddressImpl( istr ) ;
        byte[] key = EncapsulationUtility.readOctets( istr ) ;

        ObjectKey okey = orb.getObjectKeyFactory().create( key ) ;
        oktemp = okey.getTemplate() ;
        oid = okey.getId() ;

        proftemp = IIOPFactories.makeIIOPProfileTemplate( orb, 
            version, primary ) ;

        // Handle any tagged components (if applicable)
        if (version.getMinor() > 0) {
            EncapsulationUtility.readIdentifiableSequence(proftemp,
                orb.getTaggedComponentFactoryFinder(), istr);
        }

        // If there is no codebase in this IOR and there IS a
        // java.rmi.server.codebase property set, we need to
        // update the IOR with the local codebase.  Note that
        // there is only one instance of the local codebase, but it
        // can be safely shared in multiple IORs since it is immutable.
        if (uncachedGetCodeBase() == null) {
            JavaCodebaseComponent jcc = LocalCodeBaseSingletonHolder.comp ;

            if (jcc != null) {
                if (version.getMinor() > 0) {
                    proftemp.add(jcc);
                }

                codebase = jcc.getURLs() ;
            }

            // Whether codebase is null or not, we have it,
            // and so getCodebase ned never call uncachedGetCodebase.
            cachedCodebase = true;
        }
    }   

    public void writeContents(OutputStream os)
    {
        proftemp.write( oktemp, oid, os ) ;
    }

    public int getId()
    {
        return proftemp.getId() ;
    }

    public boolean isEquivalent( TaggedProfile prof )
    {
        if (!(prof instanceof IIOPProfile)) {
            return false;
        }

        IIOPProfile other = (IIOPProfile)prof ;

        return oid.equals( other.getObjectId() ) && 
               proftemp.isEquivalent( other.getTaggedProfileTemplate() ) &&
               oktemp.equals( other.getObjectKeyTemplate() ) ;
    }

    public synchronized ObjectKey getObjectKey()
    {
        if (objectKey == null) {
            objectKey = IORFactories.makeObjectKey( oktemp, oid ) ;
        }
        return objectKey ;
    }

    public org.omg.IOP.TaggedProfile getIOPProfile()
    {
        EncapsOutputStream os = OutputStreamFactory.newEncapsOutputStream( orb ) ;
        os.write_long( getId() ) ;
        write( os ) ;
        InputStream is = (InputStream)(os.create_input_stream()) ;
        return org.omg.IOP.TaggedProfileHelper.read( is ) ;
    }

    private String uncachedGetCodeBase() {
        Iterator<TaggedComponent> iter = 
            proftemp.iteratorById( TAG_JAVA_CODEBASE.value ) ;

        if (iter.hasNext()) {
            JavaCodebaseComponent jcbc = 
                JavaCodebaseComponent.class.cast( iter.next() ) ;
            return jcbc.getURLs() ;
        }

        return null ;
    }

    public synchronized String getCodebase() {
        if (!cachedCodebase) {
            cachedCodebase = true ;
            codebase = uncachedGetCodeBase() ;
        }

        return codebase ;
    }

    /**
     * @return the ORBVersion associated with the object key in the IOR.
     */
    public ORBVersion getORBVersion() {
        return oktemp.getORBVersion();
    }

    @InfoMethod
    private void computingIsLocal( String host, int scid, int sid, int port ) {}

    @InfoMethod
    private void isLocalResults( boolean isLocalHost, boolean isLocalServerId,
         boolean isLocalPort ) {}

    @IsLocal
    public synchronized boolean isLocal()
    {
        if (!checkedIsLocal) {
            checkedIsLocal = true ;
            if (isForeignObject()) return false;

            final int port = proftemp.getPrimaryAddress().getPort();
            final String host = proftemp.getPrimaryAddress().getHost() ;
            final int scid = oktemp.getSubcontractId() ;
            final int sid = oktemp.getServerId() ;
            computingIsLocal( host, scid, sid, port ) ;

            final boolean isLocalHost = orb.isLocalHost( host ) ;
            final boolean isLocalServerId = (sid == -1) || orb.isLocalServerId( scid, sid ) ;
            final boolean isLocalServerPort = orb.getLegacyServerSocketManager().legacyIsLocalServerPort( port ) ;
            isLocalResults( isLocalHost, isLocalServerId, isLocalServerPort ) ;

            cachedIsLocal = isLocalHost && isLocalServerId && isLocalServerPort ;
        }

        return cachedIsLocal ;
    }

    private boolean isForeignObject() {
        return oktemp.getORBVersion() == ORBVersionImpl.FOREIGN;
    }

    /** Return the servant for this IOR, if it is local AND if the OA that
     * implements this objref supports direct access to servants outside of an
     * invocation.
     */
    @IsLocal
    public java.lang.Object getServant()
    {
        if (!isLocal()) {
            return null;
        }

        RequestDispatcherRegistry scr = orb.getRequestDispatcherRegistry() ;
        ObjectAdapterFactory oaf = scr.getObjectAdapterFactory( 
            oktemp.getSubcontractId() ) ;

        ObjectAdapterId oaid = oktemp.getObjectAdapterId() ;
        ObjectAdapter oa = null ;

        try {
            oa = oaf.find( oaid ) ;
        } catch (SystemException exc) {
            // Could not find the OA, so just return null.
            // This usually happens when POAs are being deleted,
            // and the POA always return null for getLocalServant anyway.
            wrapper.getLocalServantFailure( exc, oaid ) ;
            return null ;
        }

        byte[] boid = oid.getId() ;
        java.lang.Object servant = oa.getLocalServant( boid ) ;
        return servant ;
    }

    /**
     * Return GIOPVersion for this IOR.
     * Requests created against this IOR will be of the
     * return Version.
     */
    public synchronized GIOPVersion getGIOPVersion()
    {
        return proftemp.getGIOPVersion() ;
    }

    public void makeImmutable()
    {
        proftemp.makeImmutable() ;
    }
}
