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

package com.sun.corba.ee.impl.folb;


import java.rmi.Remote ;
import java.rmi.RemoteException ;

import java.util.List ;

import javax.rmi.PortableRemoteObject ;

import org.omg.CORBA.LocalObject ;

import org.omg.CosNaming.NamingContext ;
import org.omg.CosNaming.NamingContextHelper ;
import org.omg.CosNaming.NameComponent ;

//import com.sun.corba.ee.spi.orb.ORB ;

import org.omg.CORBA.ORB;

import com.sun.corba.ee.spi.misc.ORBConstants ;
import com.sun.corba.ee.spi.folb.GroupInfoService;

import org.omg.PortableServer.ForwardRequest ;
import org.omg.PortableServer.ServantLocator ;

import org.omg.PortableServer.ServantLocatorPackage.CookieHolder ;
import com.sun.corba.ee.spi.folb.ClusterInstanceInfo;
import com.sun.corba.ee.spi.logging.ORBUtilSystemException;

import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.CORBA.Policy;
import javax.rmi.CORBA.Tie;

import org.omg.PortableServer.RequestProcessingPolicyValue ;
import org.omg.PortableServer.ServantRetentionPolicyValue ;
import com.sun.corba.ee.spi.trace.Folb;
import org.glassfish.pfl.tf.spi.annotation.InfoMethod;


/**
 * This class consists of :
 * - Remote interface
 * - Impl of the Remote interface
 * - ServantLocator
 *
 * This is needed for getting the information about all the cluster endpoints
 * when an appclient/standalone client enables FOLB.
 * Without this feature, the client always ends up talking to only the endpoints specified 
 * as part of the endpoints property. But in reality, the the endpoints property only contains  
 * around 2 endpoints for bootstrapping purposes.
 *
 * In this design, we register the following remote object with CosNaming 
 * and then look it up in CosNaming to get hold of the cluster instances
 * This is done only once (during the first call to new InitialContext()).
 *
 * @author Sheetal Vartak
 */
@Folb
public class InitialGroupInfoService {
    private static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    public interface InitialGIS extends Remote {
        public List<ClusterInstanceInfo> getClusterInstanceInfo()
            throws RemoteException ;
    }

    @Folb
    public static class InitialGISImpl extends PortableRemoteObject
        implements InitialGIS {
       
        private ORB orb;

        public InitialGISImpl(ORB orb) throws RemoteException {   
            super() ;      
            this.orb = orb;     
        }
        
        @InfoMethod
        private void exceptionReport( Exception exc ) { }

        @Folb
        public List<ClusterInstanceInfo> getClusterInstanceInfo()
            throws RemoteException {

            try {
                GroupInfoService gis =
                    (GroupInfoService)PortableRemoteObject.narrow(
                    orb.resolve_initial_references(
                        ORBConstants.FOLB_SERVER_GROUP_INFO_SERVICE),
                        GroupInfoService.class);
                return gis.getClusterInstanceInfo(null);
            } catch (org.omg.CORBA.ORBPackage.InvalidName inv) {
                exceptionReport( inv ) ;
                return null;
            }
        }
    }

    public static class InitialGISServantLocator extends LocalObject
        implements ServantLocator {
        private Servant servant ;
        private InitialGISImpl impl = null; 

        public InitialGISServantLocator(ORB orb) {
            try {
                impl = new InitialGISImpl(orb) ;
            } catch (Exception exc) {
                wrapper.couldNotInitializeInitialGIS( exc ) ;
            }

            Tie tie = com.sun.corba.ee.spi.orb.ORB.class.cast( orb )
                .getPresentationManager().getTie() ;
            tie.setTarget( impl ) ;
            servant = Servant.class.cast( tie ) ;
        }

        public String getType() {
            return servant._all_interfaces(null, null)[0];
        }

        public synchronized Servant preinvoke( byte[] oid, POA adapter,
            String operation, CookieHolder the_cookie 
        ) throws ForwardRequest {
            return servant ;
        }

        public void postinvoke( byte[] oid, POA adapter,
            String operation, Object the_cookie, Servant the_servant ) {
        }
    }

    public InitialGroupInfoService(ORB orb) {             
        bindName(orb);
    }


    public void bindName (ORB orb) {
      try {
        POA rootPOA = (POA)orb.resolve_initial_references(
            ORBConstants.ROOT_POA_NAME ) ;

        Policy[] arr = new Policy[] {                                   
            rootPOA.create_servant_retention_policy(
                ServantRetentionPolicyValue.NON_RETAIN ),
            rootPOA.create_request_processing_policy(
                RequestProcessingPolicyValue.USE_SERVANT_MANAGER ),
            rootPOA.create_lifespan_policy(
                LifespanPolicyValue.TRANSIENT ) } ;

        POA poa = rootPOA.create_POA( ORBConstants.INITIAL_GROUP_INFO_SERVICE,
            null, arr ) ;

        InitialGISServantLocator servantLocator =
            new InitialGISServantLocator(orb);
        poa.set_servant_manager(servantLocator) ; 
        poa.the_POAManager().activate();

        byte[] id = new byte[]{ 1, 2, 3 } ;
        org.omg.CORBA.Object provider = 
          poa.create_reference_with_id(id, servantLocator.getType());
            
        // put object in NameService
        org.omg.CORBA.Object objRef =
          orb.resolve_initial_references("NameService");
        NamingContext ncRef = NamingContextHelper.narrow(objRef);
        NameComponent nc = 
          new NameComponent(ORBConstants.INITIAL_GROUP_INFO_SERVICE, "");
        NameComponent path[] = {nc};
        ncRef.rebind(path, provider);   
      } catch (Exception e) {
          throw wrapper.bindNameException( e ) ;
      }
    }
}
