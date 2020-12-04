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

package com.sun.corba.ee.impl.transport;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantReadWriteLock ;
import java.util.concurrent.locks.ReadWriteLock ;

import com.sun.corba.ee.spi.ior.IOR ;
import com.sun.corba.ee.spi.ior.iiop.IIOPAddress ;
import com.sun.corba.ee.spi.ior.iiop.IIOPProfile ;
import com.sun.corba.ee.spi.ior.iiop.IIOPProfileTemplate ;
import com.sun.corba.ee.spi.ior.iiop.LoadBalancingComponent ;
import com.sun.corba.ee.spi.ior.TaggedProfile ;
import com.sun.corba.ee.spi.ior.TaggedProfileTemplate ;
import com.sun.corba.ee.spi.ior.TaggedComponent ;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.protocol.LocalClientRequestDispatcher;
import com.sun.corba.ee.spi.protocol.LocalClientRequestDispatcherFactory;
import com.sun.corba.ee.spi.transport.ContactInfoList ;
import com.sun.corba.ee.spi.transport.SocketInfo;
import com.sun.corba.ee.spi.transport.ContactInfo;

import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.impl.protocol.NotLocalLocalCRDImpl;
import com.sun.corba.ee.spi.trace.IsLocal;
import com.sun.corba.ee.spi.trace.Transport;

import com.sun.corba.ee.spi.logging.ORBUtilSystemException ;
import org.glassfish.pfl.basic.func.UnaryPredicate;
import org.glassfish.pfl.tf.spi.annotation.InfoMethod;

/**
 * @author Harold Carr
 */
@Transport
@IsLocal
public class ContactInfoListImpl implements ContactInfoList {
    private static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    protected ORB orb;
    private ReadWriteLock lcrdLock = new ReentrantReadWriteLock() ;
    protected LocalClientRequestDispatcher localClientRequestDispatcher;
    protected IOR targetIOR;
    protected IOR effectiveTargetIOR;
    protected List<ContactInfo> effectiveTargetIORContactInfoList;
    protected ContactInfo primaryContactInfo;
    private boolean usePerRequestLoadBalancing = false ;

    private int startCount = 0 ;

    private UnaryPredicate<ContactInfo> testPred =
        new UnaryPredicate<ContactInfo>() {
            public boolean evaluate( ContactInfo arg ) {
                return !arg.getType().equals( SocketInfo.IIOP_CLEAR_TEXT ) ;
            }
        } ;

    private <T> List<T> filter( List<T> arg, UnaryPredicate<T> pred ) {
        List<T> result = new ArrayList<T>() ;
        for (T elem : arg ) {
            if (pred.evaluate( elem )) {
                result.add( elem ) ;
            }
        }

        return result ;
    }

    private static ThreadLocal<Boolean> skipRotate = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return false ;
        }
    } ;

    // This is an ugly hack to avoid rotating the iterator when the
    // ClientGroupManager decides to update the iterator, which is only
    // supposed to happen when the cluster shape changes.  Unfortunately it
    // is happening on EVERY REQUEST, at least in the Argela test.
    public static void setSkipRotate() {
        skipRotate.set( true ) ;
    }

    @InfoMethod
    private void display( String msg, int value ) { }

    @InfoMethod
    private void display( String msg, Object value ) { }

    // Move the first startCount elements of the list to the end, so that
    // the list starts at the startCount'th element and continues
    // through all elements.  Each time we rotate, increment
    // startCount for load balancing.
    @Transport
    private synchronized List<ContactInfo> rotate( List<ContactInfo> arg ) {
        if (skipRotate.get()) {
            skipRotate.set( false ) ;
            return arg ;
        }

        if (usePerRequestLoadBalancing) {
            display( "startCount", startCount ) ;
            LinkedList<ContactInfo> tempList = null ;

            // This may be the best way to support PRLB for now.
            // The GIS will return types like "iiop-listener-1", but we also get
            // IIOP_CLEAR_TEXT for some, for both SSL and non-SSL ports.  Invoking
            // clear on an SSL port leads to bad failures that are not retryable.
            tempList = new LinkedList<ContactInfo>( filter( arg, testPred ) ) ;

            // Really should just be this:
            // tempList = new LinkedList<CorbaContactInfo>( arg ) ;

            if (startCount >= tempList.size()) {
                startCount = 0 ;
            }

            for (int ctr=0; ctr<startCount; ctr++) {
                ContactInfo element = tempList.removeLast() ;
                tempList.addFirst( element ) ;
            }

            startCount++ ;

            return tempList ;
        } else {
            return arg ;
        }
    }

    // XREVISIT - is this used?
    public ContactInfoListImpl(ORB orb)
    {
        this.orb = orb;
    }

    public ContactInfoListImpl(ORB orb, IOR targetIOR)
    {
        this(orb);
        setTargetIOR(targetIOR);
    }
    
    public synchronized Iterator<ContactInfo> iterator()
    {
        createContactInfoList();
        Iterator<ContactInfo> result = new ContactInfoListIteratorImpl(
            orb, this, primaryContactInfo, 
            rotate( effectiveTargetIORContactInfoList ),
            usePerRequestLoadBalancing );

        /* This doesn't work due to some strange behavior in FOLB: we are getting far
         * too many IOR updates.  Updates are received even when the cluster shape has not changed.
         */
        /*
        if (usePerRequestLoadBalancing) {
            // Copy the list, otherwise we will get a ConcurrentModificationException as
            // soon as next() is called on the iterator.
            List<CorbaContactInfo> newList = new ArrayList( effectiveTargetIORContactInfoList ) ;
            CorbaContactInfo head = newList.remove(0) ;
            newList.add( head ) ;
            effectiveTargetIORContactInfoList = newList ;
        }
        */

        return result ;
    }

    ////////////////////////////////////////////////////
    //
    // spi.transport.CorbaContactInfoList
    //

    public synchronized void setTargetIOR(IOR targetIOR)
    {
        this.targetIOR = targetIOR;
        setEffectiveTargetIOR(targetIOR);
    }

    public synchronized IOR getTargetIOR()
    {
        return targetIOR;
    }
    
    private IIOPAddress getPrimaryAddress( IOR ior ) {
        if (ior != null) {
            for (TaggedProfile tprof : ior) {
                TaggedProfileTemplate tpt = tprof.getTaggedProfileTemplate() ;
                if (tpt instanceof IIOPProfileTemplate) {
                    IIOPProfileTemplate ipt = (IIOPProfileTemplate)tpt ;
                    return ipt.getPrimaryAddress() ;
                }
            }
        }

        return null ;
    }

    @InfoMethod
    private void changingEffectiveAddress( IIOPAddress oldAddr, IIOPAddress newAddr ) { }

    @Transport
    public synchronized void setEffectiveTargetIOR(IOR newIOR)
    {
        if (targetIOR != null) {
            final String oldTypeId = targetIOR.getTypeId() ;
            final String newTypeId = newIOR.getTypeId() ;
            if (!oldTypeId.isEmpty() && !oldTypeId.equals( newTypeId )) {
                // Good place for a breakpoint.  This is probably always an
                // error, but not necessarily in the ORB.
                wrapper.changedTypeIdOnSetEffectiveTargetIOR( oldTypeId,
                    newTypeId ) ;
                // temporary?  It looks like this happens due to some error
                // in IIOP FOLB.
                return ;
            }
        }

        final IIOPAddress oldAddress = getPrimaryAddress( this.effectiveTargetIOR ) ;
        final IIOPAddress newAddress = getPrimaryAddress( newIOR ) ;
        if ((oldAddress != null) && !oldAddress.equals( newAddress )) {
            changingEffectiveAddress( oldAddress, newAddress ) ;
        }

        this.effectiveTargetIOR = newIOR;

        effectiveTargetIORContactInfoList = null;
        if (primaryContactInfo != null &&
            orb.getORBData().getIIOPPrimaryToContactInfo() != null)
        {
            orb.getORBData().getIIOPPrimaryToContactInfo()
                .reset(primaryContactInfo);
        }
        primaryContactInfo = null;
        setLocalSubcontract();

        // Set the per request load balancing flag.
        IIOPProfile prof = newIOR.getProfile() ;
        TaggedProfileTemplate temp = prof.getTaggedProfileTemplate() ;
        Iterator<TaggedComponent> lbcomps = 
            temp.iteratorById( ORBConstants.TAG_LOAD_BALANCING_ID ) ;
        if (lbcomps.hasNext()) {
            LoadBalancingComponent lbcomp = null ;
            lbcomp = (LoadBalancingComponent)(lbcomps.next()) ;
            usePerRequestLoadBalancing = 
                lbcomp.getLoadBalancingValue() == ORBConstants.PER_REQUEST_LOAD_BALANCING ; 
        }
    }

    public synchronized IOR getEffectiveTargetIOR()
    {
        return effectiveTargetIOR;
    }

    public synchronized LocalClientRequestDispatcher getLocalClientRequestDispatcher()
    {
        lcrdLock.readLock().lock() ;
        try {
            return localClientRequestDispatcher;
        } finally {
            lcrdLock.readLock().unlock() ;
        }
    }

    ////////////////////////////////////////////////////
    //
    // org.omg.CORBA.portable.Delegate
    //

    // REVISIT - hashCode(org.omg.CORBA.Object self)

    ////////////////////////////////////////////////////
    //
    // java.lang.Object
    //

    @Override
    public synchronized int hashCode()
    {
        return targetIOR.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ContactInfoListImpl other = (ContactInfoListImpl) obj;
        if (this.targetIOR != other.targetIOR &&
            (this.targetIOR == null || !this.targetIOR.equals(other.targetIOR))) {
            return false;
        }
        return true;
    }

    ////////////////////////////////////////////////////
    //
    // Implementation
    //

    @Transport
    private void createContactInfoList() {
        IIOPProfile iiopProfile = effectiveTargetIOR.getProfile();
        final boolean isLocal = iiopProfile.isLocal() ;

        if (effectiveTargetIORContactInfoList == null) {
            effectiveTargetIORContactInfoList = new ArrayList<>();

            IIOPProfileTemplate taggedProfileTemplate = (IIOPProfileTemplate) iiopProfile.getTaggedProfileTemplate();
            SocketInfo socketInfo = taggedProfileTemplate.getPrimarySocketInfo();
            primaryContactInfo = createContactInfo(socketInfo.getType(), socketInfo.getHost(), socketInfo.getPort());

            if (isLocal) {
                // NOTE: IMPORTANT:
                // Only do local.  The APP Server interceptors check
                // effectiveTarget.isLocal - which is determined via
                // the IOR - so if we added other addresses then
                // transactions and interceptors would not execute.
                ContactInfo contactInfo = new SharedCDRContactInfoImpl(
                    orb, this, effectiveTargetIOR, 
                    orb.getORBData().getGIOPAddressDisposition());
                effectiveTargetIORContactInfoList.add(contactInfo);
            } else {
                addRemoteContactInfos(effectiveTargetIOR,
                                      effectiveTargetIORContactInfoList);
            }
            display( "First time for iiopProfile", iiopProfile ) ;
        } else {
            if (!isLocal) {
                display( "Subsequent time for iiopProfile", iiopProfile ) ;
                // 6152681 - this is so SSL can change its selection on each
                // invocation
                addRemoteContactInfos(effectiveTargetIOR,
                                      effectiveTargetIORContactInfoList);
            } else {
                display( "Subsequent time for (colocated) iiopProfile",
                    iiopProfile ) ;
            }
        }

        display( "effective list", effectiveTargetIORContactInfoList ) ;
    }

    @Transport
    private void addRemoteContactInfos( IOR  effectiveTargetIOR,
        List<ContactInfo> effectiveTargetIORContactInfoList) {

        ContactInfo contactInfo;
        List<? extends SocketInfo> socketInfos = orb.getORBData()
            .getIORToSocketInfo().getSocketInfo(
                effectiveTargetIOR,
                // 6152681
                effectiveTargetIORContactInfoList);

        if (socketInfos == effectiveTargetIORContactInfoList) {
            display( "socketInfos", socketInfos ) ;
            return;
        }

        for (SocketInfo socketInfo : socketInfos) {
            String type = socketInfo.getType();
            String host = socketInfo.getHost().toLowerCase();
            int    port = socketInfo.getPort();
            contactInfo = createContactInfo(type, host, port);
            effectiveTargetIORContactInfoList.add(contactInfo);
        }
    }

    protected ContactInfo createContactInfo(String type, String hostname,
        int port) {

        return new ContactInfoImpl(
            orb, this, 
            // XREVISIT - See Base Line 62
            effectiveTargetIOR,
            orb.getORBData().getGIOPAddressDisposition(),
            type, hostname, port);
    }

    /**
     * setLocalSubcontract sets cached information that is set whenever
     * the effectiveTargetIOR changes.
     * 
     * Note: this must be maintained accurately whether or not the ORB
     * allows local optimization, because ServantManagers in the POA
     * ALWAYS use local optimization ONLY (they do not have a remote case).
     */
    @IsLocal
    protected void setLocalSubcontract() {
        lcrdLock.writeLock().lock() ;
        try {
            if (!effectiveTargetIOR.getProfile().isLocal()) {
                localClientRequestDispatcher = new NotLocalLocalCRDImpl();
                return;
            }

            // Note that we have no plan to support multi-profile IORs.
            int scid = effectiveTargetIOR.getProfile().getObjectKeyTemplate().
                getSubcontractId() ;
            LocalClientRequestDispatcherFactory lcsf = 
                orb.getRequestDispatcherRegistry().
                    getLocalClientRequestDispatcherFactory( scid ) ;
            if (lcsf != null) {
                localClientRequestDispatcher = lcsf.create( scid, effectiveTargetIOR ) ;
            }
        } finally {
            lcrdLock.writeLock().unlock() ;
        }
    }

    // For timing test.
    public ContactInfo getPrimaryContactInfo() {
        return primaryContactInfo;
    }
}

// End of file.
