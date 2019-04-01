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
// Created       : 2005 Jun 07 (Tue) 13:04:09 by Harold Carr.
// Last Modified : 2005 Oct 03 (Mon) 15:09:46 by Harold Carr.
//

package com.sun.corba.ee.impl.folb;

import java.util.LinkedList;

import org.omg.CORBA.Any;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.IOP.Codec;
import org.omg.IOP.CodecPackage.FormatMismatch;
import org.omg.IOP.CodecPackage.TypeMismatch;
import org.omg.IOP.CodecFactory;
import org.omg.IOP.CodecFactoryHelper;
import org.omg.IOP.CodecFactoryPackage.UnknownEncoding;
import org.omg.IOP.Encoding;
import org.omg.IOP.ServiceContext;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.ForwardRequestHelper;
import org.omg.PortableInterceptor.ORBInitializer;
import org.omg.PortableInterceptor.ORBInitInfo;

import com.sun.corba.ee.spi.folb.ClusterInstanceInfo;
import com.sun.corba.ee.spi.folb.GroupInfoService;
import com.sun.corba.ee.spi.folb.GroupInfoServiceObserver;

import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.ior.iiop.IIOPProfileTemplate;
import com.sun.corba.ee.spi.orb.DataCollector;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.orb.ORBConfigurator;
import com.sun.corba.ee.spi.transport.IIOPPrimaryToContactInfo;
import com.sun.corba.ee.spi.transport.IORToSocketInfo;
import com.sun.corba.ee.spi.transport.SocketInfo;
import com.sun.corba.ee.spi.transport.ContactInfo;

import com.sun.corba.ee.impl.interceptors.ClientRequestInfoImpl;
import com.sun.corba.ee.spi.logging.ORBUtilSystemException;
import com.sun.corba.ee.spi.misc.ORBConstants;

// BEGIN imports for IIOPPrimaryToContactInfo
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
// END imports for IIOPPrimaryToContactInfo

// BEGIN import for IORToSocketInfo
import java.util.ArrayList;
import com.sun.corba.ee.spi.ior.iiop.AlternateIIOPAddressComponent;
import com.sun.corba.ee.spi.ior.iiop.ClusterInstanceInfoComponent;
import com.sun.corba.ee.spi.ior.iiop.IIOPAddress;
import com.sun.corba.ee.spi.trace.Folb;

import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextHelper;
import org.omg.CosNaming.NameComponent;

import javax.rmi.PortableRemoteObject;
import org.glassfish.pfl.tf.spi.annotation.InfoMethod;

// END import for IORToSocketInfo

// REVISIT - log messages must be internationalized.

/**
 * @author Harold Carr
 */
@Folb
public class ClientGroupManager extends org.omg.CORBA.LocalObject
        implements ClientRequestInterceptor, GroupInfoService, IIOPPrimaryToContactInfo, IORToSocketInfo, ORBConfigurator, ORBInitializer {
    private static final ORBUtilSystemException wrapper = ORBUtilSystemException.self;

    private static final long serialVersionUID = 7849660203226017842L;
    public final String baseMsg = ClientGroupManager.class.getName();

    public static boolean sentMemberShipLabel = false; // For test.
    public static boolean receivedIORUpdate = false; // For test.

    private ORB orb;
    private Codec codec;

    private boolean initialized = false;

    private IOR lastIOR; // Initially null, thus the separate lock object.
    private final Object lastIORLock = new Object();
    private CSIv2SSLTaggedComponentHandler csiv2SSLTaggedComponentHandler;
    private transient GIS gis = new GIS();

    public ClientGroupManager() {
    }

    @InfoMethod
    private void reportException(Exception exc) {
    }

    @InfoMethod
    private void notFound(String name) {
    }

    @Folb
    private void initialize() {
        if (initialized) {
            return;
        }

        try {
            initialized = true;

            try {
                csiv2SSLTaggedComponentHandler = (CSIv2SSLTaggedComponentHandler) orb
                        .resolve_initial_references(ORBConstants.CSI_V2_SSL_TAGGED_COMPONENT_HANDLER);
            } catch (InvalidName e) {
                csiv2SSLTaggedComponentHandler = null;
                notFound(ORBConstants.CSI_V2_SSL_TAGGED_COMPONENT_HANDLER);
            }
            CodecFactory codecFactory = CodecFactoryHelper.narrow(orb.resolve_initial_references(ORBConstants.CODEC_FACTORY_NAME));

            codec = codecFactory.create_codec(new Encoding((short) 0, (byte) 1, (byte) 2));
        } catch (InvalidName e) {
            reportException(e);
        } catch (UnknownEncoding e) {
            reportException(e);
        }
    }

    ////////////////////////////////////////////////////
    //
    // IORToSocketInfo
    //

    @InfoMethod
    private void nonSSLSocketInfo() {
    }

    @InfoMethod
    private void returningPreviousSocketInfo(List lst) {
    }

    @Folb
    public List getSocketInfo(IOR ior, List previous) {
        initialize();

        try {
            if (csiv2SSLTaggedComponentHandler != null) {
                List<SocketInfo> csiv2 = csiv2SSLTaggedComponentHandler.extract(ior);
                if (csiv2 != null) {
                    /*
                     * The contract with CSIv2 says if SSL is to be used then ONLY try SSL addresssses.
                     */
                    return csiv2;
                }
            }

            nonSSLSocketInfo();

            if (!previous.isEmpty()) {
                returningPreviousSocketInfo(previous);
                return previous;
            }

            List result = new ArrayList();

            //
            // IIOPProfile Primary address
            //

            IIOPProfileTemplate iiopProfileTemplate = (IIOPProfileTemplate) ior.getProfile().getTaggedProfileTemplate();
            IIOPAddress primary = iiopProfileTemplate.getPrimaryAddress();
            String host = primary.getHost().toLowerCase();
            int port = primary.getPort();

            SocketInfo primarySocketInfo = createSocketInfo("primary", SocketInfo.IIOP_CLEAR_TEXT, host, port);
            result.add(primarySocketInfo);

            //
            // List alternate cluster addresses
            //

            final Iterator<ClusterInstanceInfoComponent> iterator = iiopProfileTemplate.iteratorById(ORBConstants.FOLB_MEMBER_ADDRESSES_TAGGED_COMPONENT_ID,
                    ClusterInstanceInfoComponent.class);

            while (iterator.hasNext()) {
                ClusterInstanceInfo clusterInstanceInfo = iterator.next().getClusterInstanceInfo();
                List<com.sun.corba.ee.spi.folb.SocketInfo> endpoints = clusterInstanceInfo.endpoints();
                for (com.sun.corba.ee.spi.folb.SocketInfo socketInfo : endpoints) {
                    result.add(createSocketInfo("ClusterInstanceInfo.endpoint", socketInfo.type(), socketInfo.host(), socketInfo.port()));
                }
            }

            //
            // List alternate TAG_ALTERNATE_IIOP_ADDRESS (for corbaloc)
            //

            final Iterator<AlternateIIOPAddressComponent> aiterator = iiopProfileTemplate.iteratorById(org.omg.IOP.TAG_ALTERNATE_IIOP_ADDRESS.value,
                    AlternateIIOPAddressComponent.class);

            while (aiterator.hasNext()) {
                AlternateIIOPAddressComponent alternate = aiterator.next();

                host = alternate.getAddress().getHost().toLowerCase();
                port = alternate.getAddress().getPort();

                result.add(createSocketInfo("AlternateIIOPAddressComponent", SocketInfo.IIOP_CLEAR_TEXT, host, port));
            }

            return result;

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            RuntimeException rte = new RuntimeException(e.getMessage());
            rte.initCause(e);
            throw rte;
        }
    }

    @Folb
    private SocketInfo createSocketInfo(final String msg, final String type, final String host, final int port) {
        return new SocketInfo() {
            public String getType() {
                return type;
            }

            public String getHost() {
                return host;
            }

            public int getPort() {
                return port;
            }

            @Override
            public boolean equals(Object o) {
                if (o == null) {
                    return false;
                }
                if (!(o instanceof SocketInfo)) {
                    return false;
                }
                SocketInfo other = (SocketInfo) o;
                if (other.getPort() != port) {
                    return false;
                }
                if (!other.getHost().equals(host)) {
                    return false;
                }
                if (!other.getType().equals(type)) {
                    return false;
                }
                return true;
            }

            @Override
            public String toString() {
                return "SocketInfo[" + type + " " + host + " " + port + "]";
            }

            @Override
            public int hashCode() {
                return port ^ host.hashCode() ^ type.hashCode();
            }
        };
    }

    ////////////////////////////////////////////////////
    //
    // IIOPPrimaryToContactInfo
    //

    private Map map = new HashMap();

    @Folb
    public synchronized void reset(ContactInfo primary) {
        initialize();
        try {
            map.remove(getKey(primary));
        } catch (Throwable t) {
            throw wrapper.exceptionInReset(t);
        }
    }

    @InfoMethod
    private void hasNextInfo(int previousIndex, int contactInfoSize) {
    }

    @Folb
    public synchronized boolean hasNext(ContactInfo primary, ContactInfo previous, List contactInfos) {
        initialize();
        try {
            boolean result;
            if (previous == null) {
                result = true;
            } else {
                int previousIndex = contactInfos.indexOf(previous);
                int contactInfosSize = contactInfos.size();
                hasNextInfo(previousIndex, contactInfosSize);
                if (previousIndex < 0) {
                    // This SHOULD not happen.
                    // It would only happen if the previous is NOT
                    // found in the current list of contactInfos.
                    RuntimeException rte = new RuntimeException(

                            "Problem in " + baseMsg + ".hasNext: previousIndex: " + previousIndex);
                    // REVISIT - error message
                    throw rte;
                } else {
                    // Since this is a retry, ensure that there is a following
                    // ContactInfo for .next
                    result = (contactInfosSize - 1) > previousIndex;
                }
            }
            return result;
        } catch (Throwable t) {
            // REVISIT - error msg
            RuntimeException rte = new RuntimeException(baseMsg + ".hasNext error");
            rte.initCause(t);
            throw rte;
        }
    }

    @InfoMethod
    private void initializeMap() {
    }

    @InfoMethod
    private void primaryMappedTo(Object obj) {
    }

    @InfoMethod
    private void cannotFindMappedEntry() {
    }

    @InfoMethod
    private void iiopFailoverTo(Object obj) {
    }

    @InfoMethod
    private void mappedResult(Object obj) {
    }

    @InfoMethod
    private void mappedResultWithUpdate(Object obj, int prevIndex, int size) {
    }

    @Folb
    public synchronized ContactInfo next(ContactInfo primary, ContactInfo previous, List contactInfos) {
        initialize();
        try {
            Object result = null;

            if (previous == null) {
                // This is NOT a retry.
                result = map.get(getKey(primary));
                if (result == null) {
                    initializeMap();
                    // NOTE: do not map primary to primary.
                    // In case of local transport we NEVER use primary.
                    result = contactInfos.get(0);
                    map.put(getKey(primary), result);
                } else {
                    primaryMappedTo(result);
                    int position = contactInfos.indexOf(result);
                    if (position == -1) {
                        // It is possible that communication to the key
                        // took place on SharedCDR, then a corbaloc to
                        // same location uses a SocketOrChannelContactInfo
                        // and vice versa.
                        cannotFindMappedEntry();
                        reset(primary);
                        return next(primary, previous, contactInfos);
                    }
                    // NOTE: This step is critical. You do NOT want to
                    // return contact info from the map. You want to find
                    // it, as a SocketInfo, in the current list, and then
                    // return that ContactInfo. Otherwise you will potentially
                    // return a ContactInfo pointing to an incorrect IOR.
                    result = contactInfos.get(position);
                    mappedResult(result);
                }
            } else {
                // This is a retry.
                // If previous is last element then .next is not called
                // because hasNext will return false.
                result = contactInfos.get(contactInfos.indexOf(previous) + 1);
                map.put(getKey(primary), result);

                iiopFailoverTo(result);

                if (orb.folbDebugFlag) {
                    // Only compute if debugging here.
                    mappedResultWithUpdate(result, contactInfos.indexOf(previous), contactInfos.size());
                }
            }
            return (ContactInfo) result;
        } catch (Throwable t) {
            throw wrapper.exceptionInNext(t);
        }
    }

    @Folb
    private Object getKey(ContactInfo contactInfo) {
        if (((SocketInfo) contactInfo).getPort() == 0) {
            // When CSIv2 is used the primary will have a zero port.
            // Therefore type/host/port will NOT be unique.
            // So use the entire IOR for the key in that case.
            return contactInfo.getContactInfoList().getEffectiveTargetIOR();
        } else {
            return contactInfo;
        }
    }

    ////////////////////////////////////////////////////
    //
    // GroupInfoService
    //

    @Folb
    public List<ClusterInstanceInfo> getInitialClusterInstanceInfo(ORB orb, List<String> endpoints) {
        try {
            org.omg.CORBA.Object ref;
            if (endpoints.isEmpty()) {
                ref = orb.resolve_initial_references("NameService");
            } else {
                final StringBuilder sb = new StringBuilder();
                sb.append("corbaloc:");
                boolean first = true;
                for (String str : endpoints) {
                    if (first) {
                        first = false;
                    } else {
                        sb.append(',');
                    }

                    sb.append("iiop:1.2@").append(str);
                }

                sb.append("/NameService");
                ref = orb.string_to_object(sb.toString());
            }

            NamingContext nctx = NamingContextHelper.narrow(ref);
            NameComponent[] path = { new NameComponent(ORBConstants.INITIAL_GROUP_INFO_SERVICE, "") };
            InitialGroupInfoService.InitialGIS initGIS = (InitialGroupInfoService.InitialGIS) PortableRemoteObject.narrow(nctx.resolve(path),
                    InitialGroupInfoService.InitialGIS.class);
            return initGIS.getClusterInstanceInfo();
        } catch (Exception e) {
            reportException(e);
            return null;
        }
    }

    private class GIS extends GroupInfoServiceBase {
        public List<ClusterInstanceInfo> internalClusterInstanceInfo(List<String> endpoints) {
            if (lastIOR == null) {
                return getInitialClusterInstanceInfo(orb, endpoints);
            }

            IIOPProfileTemplate iiopProfileTemplate;
            synchronized (lastIORLock) {
                iiopProfileTemplate = (IIOPProfileTemplate) lastIOR.getProfile().getTaggedProfileTemplate();
            }
            Iterator<ClusterInstanceInfoComponent> iterator = iiopProfileTemplate.iteratorById(ORBConstants.FOLB_MEMBER_ADDRESSES_TAGGED_COMPONENT_ID,
                    ClusterInstanceInfoComponent.class);

            LinkedList<ClusterInstanceInfo> results = new LinkedList<ClusterInstanceInfo>();

            while (iterator.hasNext()) {
                ClusterInstanceInfo clusterInstanceInfo = iterator.next().getClusterInstanceInfo();
                results.add(clusterInstanceInfo);
            }

            return results;
        }

        @Override
        public boolean shouldAddAddressesToNonReferenceFactory(String[] x) {
            throw new RuntimeException("Should not be called in this context");
        }

        @Override
        public boolean shouldAddMembershipLabel(String[] adapterName) {
            throw new RuntimeException("Should not be called in this context");
        }
    }

    public boolean addObserver(GroupInfoServiceObserver x) {
        return gis.addObserver(x);
    }

    public void notifyObservers() {
        gis.notifyObservers();
    }

    public List<ClusterInstanceInfo> getClusterInstanceInfo(String[] adapterName) {
        return gis.getClusterInstanceInfo(adapterName);
    }

    public List<ClusterInstanceInfo> getClusterInstanceInfo(String[] adapterName, List<String> endpoints) {
        return gis.getClusterInstanceInfo(adapterName, endpoints);
    }

    public boolean shouldAddAddressesToNonReferenceFactory(String[] x) {
        return gis.shouldAddAddressesToNonReferenceFactory(x);
    }

    public boolean shouldAddMembershipLabel(String[] adapterName) {
        return gis.shouldAddMembershipLabel(adapterName);
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
    // ClientRequestInterceptor
    //

    @InfoMethod
    private void sendRequestMembershipLabel(String label) {
    }

    @InfoMethod
    private void sendRequestNoMembershipLabel() {
    }

    @Folb
    public void send_request(ClientRequestInfo ri) {
        try {
            operation(ri.operation());
            initialize(); // REVISIT - remove this one later?

            org.omg.CORBA.Object ref = ri.effective_target();
            IOR ior = orb.getIOR(ref, false);
            IIOPProfileTemplate iiopProfileTemplate = (IIOPProfileTemplate) ior.getProfile().getTaggedProfileTemplate();
            Iterator iterator = iiopProfileTemplate.iteratorById(ORBConstants.FOLB_MEMBERSHIP_LABEL_TAGGED_COMPONENT_ID);
            if (iterator.hasNext()) {
                org.omg.IOP.TaggedComponent membershipLabelTaggedComponent = ((com.sun.corba.ee.spi.ior.TaggedComponent) iterator.next()).getIOPComponent(orb);
                byte[] data = membershipLabelTaggedComponent.component_data;
                sentMemberShipLabel = true; // For test
                sendRequestMembershipLabel(new String(data));
                ServiceContext sc = new ServiceContext(ORBConstants.FOLB_MEMBERSHIP_LABEL_SERVICE_CONTEXT_ID, data);
                ri.add_request_service_context(sc, false);
            } else {
                sentMemberShipLabel = false; // For test
                sendRequestNoMembershipLabel();
            }
        } catch (RuntimeException e) {
            throw e;
        }
    }

    public void send_poll(ClientRequestInfo ri) {
    }

    public void receive_reply(ClientRequestInfo ri) {
        receive_star(".receive_reply", ri);
    }

    public void receive_exception(ClientRequestInfo ri) {
        receive_star(".receive_exception", ri);
    }

    public void receive_other(ClientRequestInfo ri) {
        receive_star(".receive_other", ri);
    }

    @InfoMethod
    private void operation(String op) {
    }

    @InfoMethod
    private void noIORUpdate() {
    }

    @InfoMethod
    private void receivedIORUpdateInfo() {
    }

    @Folb
    private void receive_star(String point, ClientRequestInfo ri) {
        operation(ri.operation());
        ServiceContext iorServiceContext = null;
        try {
            iorServiceContext = ri.get_reply_service_context(ORBConstants.FOLB_IOR_UPDATE_SERVICE_CONTEXT_ID);
        } catch (BAD_PARAM e) {
            wrapper.noIORUpdateServicateContext(e);
        }

        if (iorServiceContext == null) {
            noIORUpdate();
            receivedIORUpdate = false; // For testing.
            return;
        }

        receivedIORUpdateInfo();
        receivedIORUpdate = true;

        IOR ior = extractIOR(iorServiceContext.context_data);
        synchronized (lastIORLock) {
            lastIOR = ior; // Used by LB.
            gis.notifyObservers();
        }
        reportLocatedIOR(ri, ior);

    }

    protected void reportLocatedIOR(ClientRequestInfo ri, IOR ior) {
        // REVISIT - interface;
        ((ClientRequestInfoImpl) ri).setLocatedIOR(ior);
    }

    protected IOR extractIOR(byte[] data) {
        Any any = null;
        try {
            any = codec.decode_value(data, ForwardRequestHelper.type());
        } catch (FormatMismatch e) {
            reportException(e);
        } catch (TypeMismatch e) {
            reportException(e);
        }

        // ForwardRequest is used for convenience.
        // This code has nothing to do with PortableInterceptor.
        ForwardRequest fr = ForwardRequestHelper.extract(any);
        org.omg.CORBA.Object ref = fr.forward;
        return orb.getIOR(ref, false);
    }

    ////////////////////////////////////////////////////
    //
    // ORBInitializer
    //

    public void pre_init(ORBInitInfo info) {
    }

    @Folb
    public void post_init(ORBInitInfo info) {
        try {
            info.add_client_request_interceptor(this);
        } catch (Exception e) {
            reportException(e);
        }
    }

    ////////////////////////////////////////////////////
    //
    // ORBConfigurator
    //

    @Folb
    public void configure(DataCollector collector, ORB orb) {
        this.orb = orb;
        orb.getORBData().addORBInitializer(this);
        orb.getORBData().setIIOPPrimaryToContactInfo(this);
        orb.getORBData().setIORToSocketInfo(this);
        // So the load-balancer register to get get updates.
        try {
            orb.register_initial_reference(ORBConstants.FOLB_CLIENT_GROUP_INFO_SERVICE, this);
        } catch (InvalidName e) {
            reportException(e);
        }
    }
}

// End of file.
