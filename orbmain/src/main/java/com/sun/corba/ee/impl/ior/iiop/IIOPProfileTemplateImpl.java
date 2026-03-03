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

package com.sun.corba.ee.impl.ior.iiop;

import com.sun.corba.ee.impl.encoding.OutputStreamFactory;
import com.sun.corba.ee.impl.ior.EncapsulationUtility ;
import com.sun.corba.ee.spi.ior.ObjectId ;
import com.sun.corba.ee.spi.ior.ObjectKeyTemplate ;
import com.sun.corba.ee.spi.ior.TaggedComponent ;
import com.sun.corba.ee.spi.ior.TaggedProfile ;
import com.sun.corba.ee.spi.ior.TaggedProfileTemplate ;
import com.sun.corba.ee.spi.ior.TaggedProfileTemplateBase ;
import com.sun.corba.ee.spi.ior.iiop.GIOPVersion ;
import com.sun.corba.ee.spi.ior.iiop.IIOPAddress ;
import com.sun.corba.ee.spi.ior.iiop.IIOPFactories ;
import com.sun.corba.ee.spi.ior.iiop.IIOPProfileTemplate ;
import com.sun.corba.ee.spi.ior.iiop.JavaCodebaseComponent;
import com.sun.corba.ee.spi.orb.ORB ;
import com.sun.corba.ee.spi.transport.SocketInfo;

import java.util.Iterator ;

import org.omg.CORBA_2_3.portable.InputStream ;
import org.omg.CORBA_2_3.portable.OutputStream ;
import org.omg.IOP.TAG_INTERNET_IOP ;

/**
 * @author 
 * If getMinorVersion==0, this does not contain any tagged components
 */
public class IIOPProfileTemplateImpl extends TaggedProfileTemplateBase 
    implements IIOPProfileTemplate, SocketInfo
{
    private ORB orb ;
    private GIOPVersion giopVersion ;
    private IIOPAddress primary ;
   
    public Iterator<TaggedComponent> getTaggedComponents() {
        return iterator() ;
    }

    @Override
    public String toString() {
        return String.format("IIOPProfileTemplateImpl[giopVersion=%d.%d primary=%s:%d]",
              giopVersion.getMajor(), giopVersion.getMinor(), primary.getHost(), primary.getPort());
    }

    public boolean equals( Object obj )
    {
        if (!(obj instanceof IIOPProfileTemplateImpl))
            return false ;

        IIOPProfileTemplateImpl other = (IIOPProfileTemplateImpl)obj ;

        return super.equals( obj ) && giopVersion.equals( other.giopVersion ) &&
            primary.equals( other.primary ) ;
    }

    public int hashCode()
    {
        return super.hashCode() ^ giopVersion.hashCode() ^ primary.hashCode() ;
    }

    public TaggedProfile create( ObjectKeyTemplate oktemp, ObjectId id ) 
    {
        return IIOPFactories.makeIIOPProfile( orb, oktemp, id, this ) ;
    }

    public GIOPVersion getGIOPVersion()
    {
        return giopVersion ;
    }

    public IIOPAddress getPrimaryAddress() 
    {
        return primary ;
    }

    @Override
    public SocketInfo getPrimarySocketInfo()
    {
        return this;
    }

    @Override
    public String getType()
    {
        return stream().anyMatch(this::isSslTaggedComponent) ? SocketInfo.SSL_PREFIX : SocketInfo.IIOP_CLEAR_TEXT;
    }

    private boolean isSslTaggedComponent(TaggedComponent component) {
        return component instanceof JavaCodebaseComponent && ((JavaCodebaseComponent) component).getURLs().contains("https:");
    }

    @Override
    public String getHost()
    {
        return primary.getHost();
    }

    @Override
    public int getPort()
    {
        return primary.getPort();
    }

    public IIOPProfileTemplateImpl( ORB orb, GIOPVersion version, IIOPAddress primary )
    {
        this.orb = orb ;
        this.giopVersion = version ;
        this.primary = primary ;
        if (giopVersion.getMinor() == 0)
            // Adding tagged components is not allowed for IIOP 1.0,
            // so this template is complete and should be made immutable.
            makeImmutable() ;
    }

    public IIOPProfileTemplateImpl( InputStream istr )
    {
        byte major = istr.read_octet() ;
        byte minor = istr.read_octet() ;
        giopVersion = GIOPVersion.getInstance( major, minor ) ;
        primary = new IIOPAddressImpl( istr ) ;
        orb = (ORB)(istr.orb()) ;
        // Handle any tagged components (if applicable)
        if (minor > 0) 
            EncapsulationUtility.readIdentifiableSequence(      
                this, orb.getTaggedComponentFactoryFinder(), istr ) ;

        makeImmutable() ;
    }
    
    public void write( ObjectKeyTemplate okeyTemplate, ObjectId id, OutputStream os) 
    {
        giopVersion.write( os ) ;
        primary.write( os ) ;

        // Note that this is NOT an encapsulation: do not marshal
        // the endianness flag.  However, the length is required.
        // Note that this cannot be accomplished with a codec!

        // Use the byte order of the given stream
        OutputStream encapsulatedOS = OutputStreamFactory.newEncapsOutputStream( (ORB)os.orb()
        ) ;

        okeyTemplate.write( id, encapsulatedOS ) ;
        EncapsulationUtility.writeOutputStream( encapsulatedOS, os ) ;

        if (giopVersion.getMinor() > 0) 
            EncapsulationUtility.writeIdentifiableSequence( this, os ) ;
    }
    
    /** Write out this IIOPProfileTemplateImpl only.
    */
    public void writeContents( OutputStream os) 
    {
        giopVersion.write( os ) ;
        primary.write( os ) ;

        if (giopVersion.getMinor() > 0) 
            EncapsulationUtility.writeIdentifiableSequence( this, os ) ;
    }
    
    public int getId() 
    {
        return TAG_INTERNET_IOP.value ;
    }

    public boolean isEquivalent( TaggedProfileTemplate temp )
    {
        if (!(temp instanceof IIOPProfileTemplateImpl))
            return false ;

        IIOPProfileTemplateImpl tempimp = (IIOPProfileTemplateImpl)temp ;

        return primary.equals( tempimp.primary )  ;
    }

}
