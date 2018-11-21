/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

//
// Created       : 2005 Jun 08 (Tue) 14:04:09 by Harold Carr.
// Last Modified : 2005 Sep 28 (Wed) 09:40:45 by Harold Carr.
//

package com.sun.corba.ee.impl.folb;

import com.sun.corba.ee.spi.logging.ORBUtilSystemException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.server.UID;
import java.util.List;
import java.util.LinkedList;
import javax.rmi.PortableRemoteObject;

import org.omg.CORBA.Any;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.IOP.Codec;
import org.omg.IOP.CodecPackage.InvalidTypeForEncoding;
import org.omg.IOP.CodecFactory;
import org.omg.IOP.CodecFactoryHelper;
import org.omg.IOP.CodecFactoryPackage.UnknownEncoding;
import org.omg.IOP.Encoding;
import org.omg.IOP.ServiceContext;
import org.omg.IOP.TaggedComponent;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.ForwardRequestHelper;
import org.omg.PortableInterceptor.IORInfo;
import org.omg.PortableInterceptor.IORInterceptor;
import org.omg.PortableInterceptor.ObjectReferenceTemplate;
import org.omg.PortableInterceptor.ORBInitializer;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ServerRequestInterceptor;
import org.omg.PortableInterceptor.ServerRequestInfo;

import com.sun.corba.ee.spi.folb.GroupInfoService;
import com.sun.corba.ee.spi.folb.GroupInfoServiceObserver;
import com.sun.corba.ee.spi.folb.ClusterInstanceInfo;
import com.sun.corba.ee.spi.folb.SocketInfo;

import com.sun.corba.ee.spi.ior.iiop.ClusterInstanceInfoComponent ;
import com.sun.corba.ee.spi.ior.iiop.IIOPFactories ;
import com.sun.corba.ee.spi.legacy.interceptor.ServerRequestInfoExt;

import com.sun.corba.ee.spi.oa.rfm.ReferenceFactory;
import com.sun.corba.ee.spi.oa.rfm.ReferenceFactoryManager;
import com.sun.corba.ee.spi.orb.ORBConfigurator ;
import com.sun.corba.ee.spi.orb.DataCollector ;
import com.sun.corba.ee.spi.orb.ORB;

import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.spi.trace.Folb;
import org.glassfish.pfl.tf.spi.annotation.InfoMethod;

/**
 * @author Harold Carr
 */
@Folb
public class ServerGroupManager
    extends
        org.omg.CORBA.LocalObject
    implements 
        GroupInfoServiceObserver,
        IORInterceptor,
        ORBConfigurator,
        ORBInitializer,
        ServerRequestInterceptor
{
    private static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    private static final String baseMsg = ServerGroupManager.class.getName();
    private static final long serialVersionUID = -3197578705750630503L;

    private transient ORB orb;
    private transient GroupInfoService gis;
    private transient CSIv2SSLTaggedComponentHandler
        csiv2SSLTaggedComponentHandler;
    private String membershipLabel;

    private enum MembershipChangeState { IDLE, DOING_WORK, RETRY_REQUIRED };
    private MembershipChangeState membershipChangeState =
        MembershipChangeState.IDLE;

    private ReferenceFactoryManager referenceFactoryManager;
    private Codec codec;
    private boolean initialized = false;

    // REVISIT - the app server identifies socket "types" with
    // these strings.  Should be an official API.
    private static final String SSL = com.sun.corba.ee.spi.transport.SocketInfo.SSL_PREFIX ;
    private static final String CLEAR = com.sun.corba.ee.spi.transport.SocketInfo.IIOP_CLEAR_TEXT ;

    @InfoMethod
    private void alreadyInitialized() { }

    @Folb
    private void initialize() {

        if (initialized) {
            alreadyInitialized();
            return;
        }

        try {
            initialized = true;

            updateMembershipLabel();

            CodecFactory codecFactory =
                CodecFactoryHelper.narrow(
                  orb.resolve_initial_references(
                      ORBConstants.CODEC_FACTORY_NAME));

            codec = codecFactory.create_codec(
                new Encoding((short)0, (byte)1, (byte)2));

            referenceFactoryManager = (ReferenceFactoryManager)
                orb.resolve_initial_references(
                    ORBConstants.REFERENCE_FACTORY_MANAGER);

            gis = (GroupInfoService) PortableRemoteObject.narrow(
                orb.resolve_initial_references(
                    ORBConstants.FOLB_SERVER_GROUP_INFO_SERVICE),
                GroupInfoService.class);

            gis.addObserver(this);

            try {
                csiv2SSLTaggedComponentHandler =
                    (CSIv2SSLTaggedComponentHandler)
                    orb.resolve_initial_references(
                        ORBConstants.CSI_V2_SSL_TAGGED_COMPONENT_HANDLER);
            } catch (InvalidName e) {
                csiv2SSLTaggedComponentHandler = null;
                wrapper.noCSIV2Handler( e ) ;
            }
        } catch (InvalidName e) {
            wrapper.serverGroupManagerException( e ) ;
        } catch (UnknownEncoding e) {
            wrapper.serverGroupManagerException( e ) ;
        }
    }

    ////////////////////////////////////////////////////
    //
    // Interceptor operations
    //

    public String name() {
        return baseMsg; 
    }

    public void destroy() {
    }

    ////////////////////////////////////////////////////
    //
    // IORInterceptor
    //

    @InfoMethod
    private void adapterName( String[] arr ) { }

    @InfoMethod
    private void addingAddresses() { }

    @InfoMethod
    private void notAddingAddress() { }

    @InfoMethod
    private void addingMembershipLabel( String ml ) { }

    @InfoMethod
    private void notAddingMembershipLabel( ) { }

    @InfoMethod
    private void skippingEndpoint( SocketInfo si ) {}

    @InfoMethod
    private void includingEndpoint( SocketInfo si ) {}

    @InfoMethod
    private void addingInstanceInfoFor( String name, int weight ) {}

    @Folb
    public void establish_components(IORInfo iorInfo) {
        try {
            initialize();

            // Only handle ReferenceFactory adapters.
            String[] adapterName = 
                ((com.sun.corba.ee.impl.interceptors.IORInfoImpl)iorInfo)
                    .getObjectAdapter().getAdapterTemplate().adapter_name();

            adapterName( adapterName ) ;

            ReferenceFactory rf = referenceFactoryManager.find(adapterName);
            if (rf == null) {
                if (gis.shouldAddAddressesToNonReferenceFactory(adapterName)) {
                    addingAddresses() ;
                } else {
                    notAddingAddress();
                    return;
                }
            }

            // Get all addressing information.

            // both CLEAR and SSL
            List<ClusterInstanceInfo> info = 
                gis.getClusterInstanceInfo(adapterName);

            // Let security handle SSL infomation.
            if (csiv2SSLTaggedComponentHandler != null) {
                TaggedComponent csiv2 = 
                    csiv2SSLTaggedComponentHandler.insert(iorInfo, info);
                if (csiv2 != null) {
                    iorInfo.add_ior_component(csiv2);
                }
            }

            // Handle CLEAR_TEXT addresses.
            for (ClusterInstanceInfo clusterInstanceInfo : info) {
                addingInstanceInfoFor( clusterInstanceInfo.name(),
                    clusterInstanceInfo.weight() ) ;

                List<SocketInfo> listOfSocketInfo = 
                    new LinkedList<SocketInfo>();

                for (SocketInfo sinfo : clusterInstanceInfo.endpoints()) {
                    if (sinfo.type().startsWith( SSL )) {
                        skippingEndpoint(sinfo);
                    } else {
                        includingEndpoint(sinfo);
                        // Don't want identifier like orb-listener-1 from GlassFish here
                        final SocketInfo si = new SocketInfo( CLEAR, sinfo.host(), sinfo.port() ) ;
                        listOfSocketInfo.add( si ) ;
                    }
                }

                final ClusterInstanceInfo ninfo = new ClusterInstanceInfo(
                    clusterInstanceInfo.name(),
                    clusterInstanceInfo.weight(),
                    listOfSocketInfo ) ;

                ClusterInstanceInfoComponent comp = 
                    IIOPFactories.makeClusterInstanceInfoComponent( 
                        ninfo ) ;

                iorInfo.add_ior_component( comp.getIOPComponent(orb) ) ;
            }

            // Handle membership label.
            if (gis.shouldAddMembershipLabel(adapterName)) {
                TaggedComponent tc = new TaggedComponent(
                    ORBConstants.FOLB_MEMBERSHIP_LABEL_TAGGED_COMPONENT_ID,
                    membershipLabel.getBytes());

                addingMembershipLabel( membershipLabel );
                iorInfo.add_ior_component(tc);
            } else {
                notAddingMembershipLabel();
            }
        } catch (RuntimeException e) {
            wrapper.serverGroupManagerException(e);
        }
    }

    public void components_established( IORInfo iorInfo ) {
    }

    public void adapter_manager_state_changed( int managerId, short state ) {
    }

    public void adapter_state_changed( ObjectReferenceTemplate[] templates,
        short state ) {
    }

    ////////////////////////////////////////////////////
    //
    // GroupInfoServiceObserver
    //

    @InfoMethod
    private void alreadyChangingMembership() { }

    @InfoMethod
    private void loopingForMembershipChange() { }

    @InfoMethod
    private void unexpectedStateForMembershipChange() { }

    @Folb
    public void membershipChange() {
        try {
            synchronized (this) {
                if (membershipChangeState == MembershipChangeState.IDLE) {
                    membershipChangeState = MembershipChangeState.DOING_WORK;
                } else {
                    // State is DOING_WORK or RETRY_REQUIRED.
                    membershipChangeState = MembershipChangeState.RETRY_REQUIRED;
                    alreadyChangingMembership();
                    return;
                }
            }

            boolean loop;

            do {
                loop = false;

                restartFactories();

                synchronized (this) {
                    if (membershipChangeState == MembershipChangeState.RETRY_REQUIRED) {
                        membershipChangeState = MembershipChangeState.DOING_WORK;
                        // One or more notifies arrived while processing
                        // this notify.  Therefore do the restart again.
                        loop = true;
                        loopingForMembershipChange();
                    } else if (membershipChangeState == MembershipChangeState.DOING_WORK) {
                        membershipChangeState = MembershipChangeState.IDLE;
                    } else if (membershipChangeState == MembershipChangeState.IDLE) {
                        unexpectedStateForMembershipChange();
                    }
                }
            } while (loop);
            
        } catch (RuntimeException e) {
            wrapper.serverGroupManagerException(e);

            // If we get an exception we need to ensure that we do not
            // lock out further changes.
            synchronized (this) {
                membershipChangeState = MembershipChangeState.IDLE;
            }
        }
    }


    @Folb
    public class WorkerThread extends Thread {
        @InfoMethod
        private void suspendRFM() { }

        @InfoMethod
        private void updateMembershipLabelInfo() { }

        @InfoMethod
        private void restartFactories() { }

        @InfoMethod
        private void resumeRFM() { }

        @Folb
        @Override
        public void run() {
            try {
                suspendRFM() ;
                referenceFactoryManager.suspend();

                // Requests have drained so update label.
                // IMPORTANT: do not update label until requests
                // have drained.  Otherwise responses will compare
                // against wrong label.
                updateMembershipLabelInfo();
                updateMembershipLabel();

                restartFactories();
                referenceFactoryManager.restartFactories();
            } finally {
                resumeRFM();
                referenceFactoryManager.resume();
            }
        }
    }

    @InfoMethod
    private void waitingForWorkerTermination() { }

    @Folb
    private void restartFactories() {
        //
        // REVISIT
        //
        // restart gets exception since a remote call is coming
        // in on a non-ReferenceFactory POA.  The ORB does not
        // discriminate the granularity of restart.
        // See ORBImpl.isDuringDispatch
        //
        // Workaround by using a different thread.
        //
        // Note: this is only a problem in the test because
        // the test client sends an "add" message that
        // is serviced by a server worker thread that calls
        // membershipChange.  This method calls restartFactories
        // that calls destory POA that calls isDuringDispatch.
        // isDuringDispatch uses a thread local to determine
        // it is a  dispatch.  Using another thread fools 
        // isDuringDispatch into letting this chain proceed.
        //
        
        final ReferenceFactoryManager rfm = referenceFactoryManager;

        Thread worker = new WorkerThread() ;
        
        worker.start();
        
        // Make sure the worker terminates before we continue
        waitingForWorkerTermination();
        boolean tryAgain;
        do {
            tryAgain = false;

            try { 
                worker.join(); 
            } catch (InterruptedException e) { 
                Thread.interrupted() ; 
                tryAgain = true; 
            }
        } while (tryAgain);
    }

    @InfoMethod
    private void newMembershipLabel( String ml ) { }

    @Folb
    private void updateMembershipLabel() {
        UID uid = new UID();
        String hostAddress = null;
        try {
            // REVISIT 
            // name could match GroupInfoService's idea of instance id/name.
            // Not necessary but easier to debug.
            hostAddress = InetAddress.getLocalHost().getHostAddress();
            membershipLabel = hostAddress + ":::" + uid;
            newMembershipLabel( membershipLabel );
        } catch (UnknownHostException e) {
            wrapper.serverGroupManagerException(e);
        }
    }

    ////////////////////////////////////////////////////
    //
    // ServerRequestInterceptor
    //

    @Folb
    public void receive_request_service_contexts(ServerRequestInfo ri)
    {
        initialize();
    }

    @Folb
    public void receive_request(ServerRequestInfo ri)
    {
        initialize();
    }

    public void send_reply(ServerRequestInfo ri)
    {
        send_star(".send_reply", ri);
    }

    public void send_exception(ServerRequestInfo ri)
    {
        send_star(".send_exception", ri);
    }

    public void send_other(ServerRequestInfo ri)
    {
        send_star(".send_other", ri);
    }

    @InfoMethod
    private void rfmIsHolding() { }

    @InfoMethod
    private void notManagedByReferenceFactory( String[] adapterName ) { }

    @InfoMethod 
    private void membershipLabelsEqual() { }
    
    @InfoMethod 
    private void membershipLabelsNotEqual() { }
    
    @InfoMethod 
    private void membershipLabelsNotPresent() { }
   
    @InfoMethod
    private void sendingUpdatedIOR( String[] adapterName ) { }

    /**
     * If the request membership label is out-of-date or missing
     * then return an updated IOR.
     */
    @Folb
    private void send_star(String point, ServerRequestInfo ri)
    {
        String[] adapterName = null;
        try {
            adapterName = ri.adapter_name();

            if (referenceFactoryManager.getState() ==
                ReferenceFactoryManager.RFMState.SUSPENDED) {

                rfmIsHolding();
                return;
            }

            ReferenceFactory referenceFactory = 
                referenceFactoryManager.find(adapterName);

            // Only handle RefenceFactory adapters.
            if (referenceFactory == null && 
                    !((ServerRequestInfoExt)ri).isNameService()) {
                notManagedByReferenceFactory( adapterName ) ;
                return;
            }

            // Handle membership label from request.
            String requestMembershipLabel = null;
            try {
                ServiceContext sc = ri.get_request_service_context(
                    ORBConstants.FOLB_MEMBERSHIP_LABEL_SERVICE_CONTEXT_ID);
                // REVISIT - internationalization
                if (sc != null) {
                    byte[] data = sc.context_data;
                    requestMembershipLabel = new String(data);

                    if (membershipLabel.equals(requestMembershipLabel)) {
                        membershipLabelsEqual();
                        return;
                    }
                    membershipLabelsNotEqual();
                }
            } catch (BAD_PARAM e) {
                membershipLabelsNotPresent();
                // REVISIT: CHECK: if not our ORB then return.  --
            }

            // Send IOR UPDATE
            //
            // At this point either the labels do not match
            // or our ORB has sent a request without a label (e.g., bootstrap).
            // Therefore send an updated IOR.
            sendingUpdatedIOR( adapterName ) ;
            
            byte[] objectId = ri.object_id();
            org.omg.CORBA.Object ref = 
                referenceFactory.createReference(objectId);
            Any any = orb.create_any();
            // ForwardRequest is used for convenience.
            // This code has nothing to do with PortableInterceptor.
            ForwardRequest fr = new ForwardRequest(ref);
            ForwardRequestHelper.insert(any, fr);
            byte[] data = null;
            try {
                data = codec.encode_value(any);
            } catch (InvalidTypeForEncoding e) {
                wrapper.serverGroupManagerException(e);
            }
            ServiceContext sc = new ServiceContext(
                ORBConstants.FOLB_IOR_UPDATE_SERVICE_CONTEXT_ID, data);
            ri.add_reply_service_context(sc, false);
        } catch (RuntimeException e) {
            wrapper.serverGroupManagerException(e);
        }
    }

    ////////////////////////////////////////////////////
    //
    // ORBInitializer
    //

    public void pre_init(ORBInitInfo info) 
    {
    }

    @Folb
    public void post_init(ORBInitInfo info) {
        try {
            info.add_ior_interceptor(this);
            info.add_server_request_interceptor(this);
        } catch (Exception e) {
            wrapper.serverGroupManagerException(e);
        }
    }

    ////////////////////////////////////////////////////
    //
    // ORBConfigurator
    //

    @Folb
    public void configure(DataCollector collector, ORB orb) 
    {
        this.orb = orb;

        // Setup for IOR and ServerRequest Interceptors
        orb.getORBData().addORBInitializer(this);
    }
}

// End of file.
