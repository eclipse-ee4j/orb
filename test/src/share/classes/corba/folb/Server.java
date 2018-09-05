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
// Created       : 2005 Jun 09 (Thu) 14:44:09 by Harold Carr.
// Last Modified : 2005 Sep 23 (Fri) 15:03:10 by Harold Carr.
//

package corba.folb;

import java.rmi.RemoteException;
import java.util.Properties;

import org.omg.CORBA.LocalObject ;
import org.omg.CORBA.ORBPackage.InvalidName;

import org.omg.PortableServer.ForwardRequest ;
import org.omg.PortableServer.POA ;
import org.omg.PortableServer.Servant ;
import org.omg.PortableServer.ServantLocator ;
import org.omg.PortableServer.ServantLocatorPackage.CookieHolder ;

import com.sun.corba.ee.spi.oa.rfm.ReferenceFactoryManager;
import com.sun.corba.ee.spi.oa.rfm.ReferenceFactory;
import com.sun.corba.ee.spi.orb.ORBConfigurator ;
import com.sun.corba.ee.spi.orb.DataCollector ;
import com.sun.corba.ee.spi.orb.ORB;

import com.sun.corba.ee.impl.folb.ServerGroupManager;
import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.impl.misc.ORBUtility;

import corba.framework.Options;
import corba.hcks.U;

/**
 * @author Harold Carr
 */
public class Server
    implements
        ORBConfigurator
{
    static {
        // This is needed to guarantee that this test will ALWAYS use dynamic
        // RMI-IIOP.  Currently the default is dynamic when renamed to "ee",
        // but static in the default "se" packaging, and this test will
        // fail without dynamic RMI-IIOP.
        System.setProperty( ORBConstants.USE_DYNAMIC_STUB_PROPERTY, "true" ) ;
    }

    private static final String baseMsg = Server.class.getName();

    public static void setProperties(Properties props)
    {
        //
        // Debugging flags.
        //

        props.setProperty(ORBConstants.DEBUG_PROPERTY,
                          //"giop,transport,subcontract,poa"
                          "transport"
                          );


        //
        // Must set server id and persistent port for 
        // persistent POAs (e.g., ReferenceFactory)
        //
        
        // 300 is arbitrary;
        props.setProperty(ORBConstants.ORB_SERVER_ID_PROPERTY, "300");
        // 4567 is arbitrary;
        props.setProperty(ORBConstants.PERSISTENT_SERVER_PORT_PROPERTY,
                          new Integer(4567).toString());
        
        //
        // Tell the ORB to listen on user-define ports
        //
        
        String listenPorts = corba.folb_8_1.Server.formatListenPorts();
        props.setProperty(ORBConstants.LISTEN_SOCKET_PROPERTY, listenPorts);
        U.sop("Listen ports: " + listenPorts);


        //
        // Register the socket factory that knows how to create
        // Sockets of types used by test.
        //

        props.setProperty(ORBConstants.SOCKET_FACTORY_CLASS_PROPERTY,
                          corba.folb_8_1.SocketFactoryImpl.class.getName());


        //
        // This registers the IIOPGroupAgent into the system
        // which then registers itself as an ORBInitializer
        // to then register itself as IOR and ServerRequest Interceptors.
        //
        
        props.setProperty(ORBConstants.USER_CONFIGURATOR_PREFIX
                          + ServerGroupManager.class.getName(),
                          "dummy");
        
        //
        // This configurator registers the "fake" GIS
        //

        props.setProperty(ORBConstants.USER_CONFIGURATOR_PREFIX
                          + Server.class.getName(),
                          "dummy");


        //
        // Make ReferenceFactoryManager available
        //

        props.setProperty(ORBConstants.RFM_PROPERTY,"dummy");


        //
        // This configurator registers the CSIv2SSLTaggedComponentHandler
        //

        props.setProperty(ORBConstants.USER_CONFIGURATOR_PREFIX
                          + CSIv2SSLTaggedComponentHandlerImpl.class.getName(),
                          "dummy");
    }

    public static void main(String[] av)
    {
        try {

            Properties props = new Properties();
            setProperties(props);

            dprint("--------------------------------------------------");
            dprint("ORB.init");
            dprint("--------------------------------------------------");
            ORB orb = (ORB) ORB.init(av, props);


            dprint("--------------------------------------------------");
            dprint("resolve ReferenceFactoryManager");
            dprint("--------------------------------------------------");
            ReferenceFactoryManager rfm = (ReferenceFactoryManager)
                orb.resolve_initial_references(
                   ORBConstants.REFERENCE_FACTORY_MANAGER);

            dprint("--------------------------------------------------");
            dprint("activate ReferenceFactoryManager");
            dprint("--------------------------------------------------");
            rfm.activate();

            //
            // repo id, object id  and locator managed by ReferenceFactory.
            //

            Servant s = (Servant) javax.rmi.CORBA.Util.getTie(new EchoTestServant(orb));
            String repositoryId = s._all_interfaces(null, null)[0];
            // objectId is used to make a reference but it is
            // never used in the dispatch.
            byte[] objectId = "DUMMY".getBytes();
            ServantLocator servantLocator = new TestServantLocator(orb);

            //
            // The ReferenceFactory and Object reference for the test.
            //

            ReferenceFactory rf;
            org.omg.CORBA.Object ref;

            dprint("--------------------------------------------------");
            dprint("create ReferenceFactory: " + Common.RFM_WITH_ADDRESSES_WITH_LABEL);
            dprint("--------------------------------------------------");
            rf = rfm.create(Common.RFM_WITH_ADDRESSES_WITH_LABEL,
                            repositoryId, null, servantLocator);

            dprint("--------------------------------------------------");
            dprint("createReference: " + rf);
            dprint("--------------------------------------------------");
            ref = rf.createReference(objectId);

            dprint("--------------------------------------------------");
            dprint("bind reference: " 
                   + Common.TEST_RFM_WITH_ADDRESSES_WITH_LABEL);
            dprint("--------------------------------------------------");
            U.rebind(Common.RFM_WITH_ADDRESSES_WITH_LABEL, ref, orb);

            //
            // A ReferenceFactory with a name that causes
            // establish_components to not add label.
            // This is so we can create a reference missing the
            // membership label.  When we see the call come in with the
            // missing label we send an IOR UPDATE.
            //

            dprint("--------------------------------------------------");
            dprint("create ReferenceFactory: " 
                   + Common.RFM_WITH_ADDRESSES_WITHOUT_LABEL);
            dprint("--------------------------------------------------");
            rf = rfm.create(Common.RFM_WITH_ADDRESSES_WITHOUT_LABEL,
                            repositoryId, null, servantLocator);

            dprint("--------------------------------------------------");
            dprint("createReference: " + rf);
            dprint("--------------------------------------------------");
            ref = rf.createReference(objectId);

            dprint("--------------------------------------------------");
            dprint("bind reference: " 
                   + Common.TEST_RFM_WITH_ADDRESSES_WITHOUT_LABEL);
            U.rebind(Common.TEST_RFM_WITH_ADDRESSES_WITHOUT_LABEL, ref, orb);
            dprint("--------------------------------------------------");

            //
            // An object managed by an independent POA.
            // This object is also used to control GIS.
            //

            GroupInfoServiceImpl gis = (GroupInfoServiceImpl) 
                orb.resolve_initial_references(
                    ORBConstants.FOLB_SERVER_GROUP_INFO_SERVICE);

            dprint("--------------------------------------------------");
            dprint("getRootPOA");
            dprint("--------------------------------------------------");
            POA rootPOA = U.getRootPOA(orb);
            rootPOA.the_POAManager().activate();

            Servant servant = (Servant)
                javax.rmi.CORBA.Util.getTie(
                    new GroupInfoServiceTestServant(orb, gis));

            dprint("--------------------------------------------------");
            dprint("createWithServantAndBind: "
                   + Common.GIS_POA_WITHOUT_ADDRESSES_WITHOUT_LABEL);
            dprint("--------------------------------------------------");
            U.createWithServantAndBind(Common.GIS_POA_WITHOUT_ADDRESSES_WITHOUT_LABEL,
                                       servant, rootPOA, orb);


            dprint("--------------------------------------------------");
            dprint("createPOA: " 
                   + Common.GIS_POA_WITH_ADDRESSES_WITH_LABEL);
            dprint("--------------------------------------------------");

            POA poaWithTags = 
                rootPOA.create_POA(Common.POA_WITH_ADDRESSES_WITH_LABEL,
                                   null, null);
            poaWithTags.the_POAManager().activate();


            dprint("--------------------------------------------------");
            dprint("createWithServantAndBind: "
                   + Common.GIS_POA_WITH_ADDRESSES_WITH_LABEL);
            dprint("--------------------------------------------------");
            U.createWithServantAndBind(Common.GIS_POA_WITH_ADDRESSES_WITH_LABEL,
                                       servant, poaWithTags, orb);

            //
            // Server ready.
            //

            dprint("--------------------------------------------------");
            System.out.println(Options.defServerHandshake);
            dprint("--------------------------------------------------");

            orb.run();

            // REVISIT - move ServerGroupManager configurator here from test.

        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    ////////////////////////////////////////////////////
    //
    // ORBConfigurator
    //

    public void configure(DataCollector collector, ORB orb) 
    {
        dprint(".configure->:");

        try {

            //
            // Make Test GroupInfoService available
            //

            orb.register_initial_reference(
                ORBConstants.FOLB_SERVER_GROUP_INFO_SERVICE,
                new GroupInfoServiceImpl());

        } catch (InvalidName e) {
            // REVISIT
            e.printStackTrace(System.out);
        }
        dprint(".configure<-:");
    }

    ////////////////////////////////////////////////////
    //
    // Servantlocator
    //

    private static class TestServantLocator
        extends LocalObject
        implements ServantLocator 
    {
        ORB orb;

        public TestServantLocator(ORB orb) 
        {
            this.orb = orb;
        }

        public synchronized void deactivate()
        {
        }

        public synchronized Servant preinvoke(
            byte[] oid, POA adapter, String operation, CookieHolder the_cookie)
            throws ForwardRequest
        {
            try {
                return (Servant) javax.rmi.CORBA.Util.getTie(new EchoTestServant(orb));
            } catch (RemoteException e) {
                e.printStackTrace(System.out);
            }
            return null;
        }

        public void postinvoke(
            byte[] oid, POA adapter, String operation, Object the_cookie,
            Servant the_servant)
        {
        }
    }

    ////////////////////////////////////////////////////
    //
    // Implementation
    //

    private static void dprint(String msg)
    {
        ORBUtility.dprint("Server", msg);
    }
}

// End of file.
