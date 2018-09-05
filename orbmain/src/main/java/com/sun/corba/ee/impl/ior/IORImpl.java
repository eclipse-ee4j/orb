/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.ior;

import java.util.Iterator ;
import java.util.Map ;
import java.util.HashMap ;

import java.io.StringWriter;
import java.io.IOException;

import org.omg.CORBA_2_3.portable.InputStream ;
import org.omg.CORBA_2_3.portable.OutputStream ;

import org.omg.IOP.TAG_INTERNET_IOP ;

import com.sun.corba.ee.spi.ior.ObjectId ;
import com.sun.corba.ee.spi.ior.TaggedProfileTemplate ;
import com.sun.corba.ee.spi.ior.TaggedProfile ;
import com.sun.corba.ee.spi.ior.IOR ;
import com.sun.corba.ee.spi.ior.IORTemplate ;
import com.sun.corba.ee.spi.ior.IORTemplateList ;
import com.sun.corba.ee.spi.ior.IdentifiableFactoryFinder ;
import com.sun.corba.ee.spi.ior.IdentifiableContainerBase ;
import com.sun.corba.ee.spi.ior.ObjectKeyTemplate ;
import com.sun.corba.ee.spi.ior.IORFactories ;

import com.sun.corba.ee.spi.orb.ORB;

import com.sun.corba.ee.impl.encoding.MarshalOutputStream;
import com.sun.corba.ee.impl.encoding.OutputStreamFactory;
import com.sun.corba.ee.impl.encoding.EncapsOutputStream;

import com.sun.corba.ee.impl.misc.HexOutputStream;
import com.sun.corba.ee.spi.misc.ORBConstants;

import com.sun.corba.ee.spi.logging.IORSystemException ;

import com.sun.corba.ee.spi.ior.iiop.IIOPProfile ;

/** An IOR is represented as a list of profiles.
* Only objects that extend TaggedProfile should be added to an IOR.
* However, enforcing this restriction requires overriding all
* of the addYYY methods inherited from List, so no check
* is included here.
* @author Ken Cavanaugh
*/
public class IORImpl extends IdentifiableContainerBase<TaggedProfile> 
    implements IOR
{
    private String typeId;
    private ORB factory = null ;
    static final IORSystemException wrapper =
        IORSystemException.self ;
    private boolean isCachedHashValue = false;
    private int cachedHashValue;

    public Iterator<TaggedProfile> getTaggedProfiles() {
        return iterator() ;
    }

    public ORB getORB()
    {
        return factory ;
    }

    /* This variable is set directly from the constructors that take
     * an IORTemplate or IORTemplateList as arguments; otherwise it
     * is derived from the list of TaggedProfile instances on the first
     * call to getIORTemplates.  Note that we assume that an IOR with
     * mutiple TaggedProfile instances has the same ObjectId in each
     * TaggedProfile, as otherwise the IOR could never be created through
     * an ObjectReferenceFactory.
     */
    private IORTemplateList iortemps = null ;

    @Override
    public boolean equals( Object obj )
    {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof IOR)) {
            return false;
        }

        IOR other = (IOR)obj ;

        return super.equals( obj ) && typeId.equals( other.getTypeId() ) ;
    }

    @Override
    public int hashCode() 
    {
        if (!isCachedHashValue) { 
            cachedHashValue = (super.hashCode() ^ typeId.hashCode()); 
            isCachedHashValue = true; 
        }
        return cachedHashValue;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder() ;
        sb.append( "IORImpl[type=") ;
        sb.append( typeId ) ;
        sb.append( " iorTemplates=" ) ;

        IORTemplateList list = getIORTemplates() ;
        sb.append( list.toString() ) ;
        return sb.toString() ;
    }

    /** Construct an empty IOR.  This is needed for null object references.
    */
    public IORImpl( ORB orb )
    {
        this( orb, "" ) ;
    }

    public IORImpl( ORB orb, String typeid )
    {
        factory = orb ;
        this.typeId = typeid ;
    }

    /** Construct an IOR from an IORTemplate by applying the same
    * object id to each TaggedProfileTemplate in the IORTemplate.
    */
    public IORImpl( ORB orb, String typeId, IORTemplate iortemp, ObjectId id) 
    {
        this( orb, typeId ) ;

        this.iortemps = IORFactories.makeIORTemplateList() ;
        this.iortemps.add( iortemp ) ;
    
        addTaggedProfiles( iortemp, id ) ;
        
        makeImmutable() ;
    }
    
    private void addTaggedProfiles( IORTemplate iortemp, ObjectId id ) 
    {
        ObjectKeyTemplate oktemp = iortemp.getObjectKeyTemplate() ;
        for( TaggedProfileTemplate temp : iortemp) {
            TaggedProfile profile = temp.create( oktemp, id ) ;
            add( profile ) ;
        }
    }

    /** Construct an IOR from an IORTemplate by applying the same
    * object id to each TaggedProfileTemplate in the IORTemplate.
    */
    public IORImpl( ORB orb, String typeId, IORTemplateList iortemps, ObjectId id) 
    {
        this( orb, typeId ) ;

        this.iortemps = iortemps ;

        Iterator<IORTemplate> iter = iortemps.iterator() ;
        while (iter.hasNext()) {
            IORTemplate iortemp = iter.next() ;
            addTaggedProfiles( iortemp, id ) ;
        }
        
        makeImmutable() ;
    }
    
    // Note that orb is not always the ORB of is!
    public IORImpl(ORB orb, InputStream is) 
    {
        this( orb, is.read_string() ) ;

        IdentifiableFactoryFinder<TaggedProfile> finder = 
            factory.getTaggedProfileFactoryFinder() ;

        EncapsulationUtility.readIdentifiableSequence( this, finder, is ) ;

        makeImmutable() ;
    }
    
    public String getTypeId() 
    {
        return typeId ;
    }
    
    public void write(OutputStream os) 
    {
        os.write_string( typeId ) ;
        EncapsulationUtility.writeIdentifiableSequence( this, os ) ;
    }

    public String stringify()
    {
        StringWriter bs;

        MarshalOutputStream s = OutputStreamFactory.newEncapsOutputStream(factory);
        s.putEndian();
        write( (OutputStream)s );
        bs = new StringWriter();
        try {
            s.writeTo(new HexOutputStream(bs));
        } catch (IOException ex) {
            throw wrapper.stringifyWriteError( ex ) ;
        }

        return ORBConstants.STRINGIFY_PREFIX + bs;
    }

    @Override
    public synchronized void makeImmutable()
    {
        makeElementsImmutable() ;

        if (iortemps != null) {
            iortemps.makeImmutable();
        }

        super.makeImmutable() ;
    }
    
    public org.omg.IOP.IOR getIOPIOR() {    
        EncapsOutputStream os = OutputStreamFactory.newEncapsOutputStream(factory);
        write(os);
        InputStream is = (InputStream) (os.create_input_stream());
        return org.omg.IOP.IORHelper.read(is);
    }

    public boolean isNil()
    {
        //
        // The check for typeId length of 0 below is commented out
        // as a workaround for a bug in ORBs which send a
        // null objref with a non-empty typeId string.
        //
        return ((size() == 0) /* && (typeId.length() == 0) */);
    }

    public boolean isEquivalent(IOR ior)
    {
        Iterator<TaggedProfile> myIterator = iterator() ;
        Iterator<TaggedProfile> otherIterator = ior.iterator() ;
        while (myIterator.hasNext() && otherIterator.hasNext()) {
            TaggedProfile myProfile = myIterator.next() ;
            TaggedProfile otherProfile = otherIterator.next() ;
            if (!myProfile.isEquivalent( otherProfile )) {
                return false;
            }
        }

        return myIterator.hasNext() == otherIterator.hasNext() ; 
    }

    private void initializeIORTemplateList() 
    {
        // Maps ObjectKeyTemplate to IORTemplate
        Map<ObjectKeyTemplate,IORTemplate> oktempToIORTemplate = 
            new HashMap<ObjectKeyTemplate,IORTemplate>() ;

        iortemps = IORFactories.makeIORTemplateList() ;
        Iterator<TaggedProfile> iter = iterator() ;
        ObjectId oid = null ; // used to check that all profiles have the same oid.
        while (iter.hasNext()) {
            TaggedProfile prof = iter.next() ;
            TaggedProfileTemplate ptemp = prof.getTaggedProfileTemplate() ;
            ObjectKeyTemplate oktemp = prof.getObjectKeyTemplate() ;

            // Check that all oids for all profiles are the same: if they are not,
            // throw exception.
            if (oid == null) {
                oid = prof.getObjectId();
            } else if (!oid.equals( prof.getObjectId() )) {
                throw wrapper.badOidInIorTemplateList();
            }

            // Find or create the IORTemplate for oktemp.
            IORTemplate iortemp = oktempToIORTemplate.get( oktemp ) ;
            if (iortemp == null) {
                iortemp = IORFactories.makeIORTemplate( oktemp ) ;
                oktempToIORTemplate.put( oktemp, iortemp ) ;
                iortemps.add( iortemp ) ;
            }

            iortemp.add( ptemp ) ;
        }

        iortemps.makeImmutable() ;
    }

    /** Return the IORTemplateList for this IOR.  Will throw
     * exception if it is not possible to generate an IOR
     * from the IORTemplateList that is equal to this IOR, 
     * which can only happen if not every TaggedProfile in the
     * IOR has the same ObjectId.
     */
    public synchronized IORTemplateList getIORTemplates() 
    {
        if (iortemps == null) {
            initializeIORTemplateList();
        }

        return iortemps ;
    }

    /** Return the first IIOPProfile in this IOR.
     * Originally we planned to remove this, because we planned to use
     * multiple IIOP profiles.  However, we really have no need for 
     * multiple profiles in the ORB, so we will probably never remove
     * this API.
     */
    public IIOPProfile getProfile() 
    {
        IIOPProfile iop = null ;
        Iterator<TaggedProfile> iter = 
            iteratorById( TAG_INTERNET_IOP.value ) ;
        if (iter.hasNext()) {
            iop =
                IIOPProfile.class.cast(iter.next());
        }
 
        if (iop != null) {
            return iop;
        }
 
        // if we come to this point then no IIOP Profile
        // is present.  Therefore, throw an exception.
        throw wrapper.iorMustHaveIiopProfile() ;
    }
}
