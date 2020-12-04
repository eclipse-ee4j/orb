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

//
// Created       : 2005 Jun 13 (Mon) 11:04:09 by Harold Carr.
// Last Modified : 2005 Sep 26 (Mon) 22:39:12 by Harold Carr.
//

package corba.folb;

import java.net.InetAddress ;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

import com.sun.corba.ee.spi.folb.ClusterInstanceInfo;
import com.sun.corba.ee.spi.folb.GroupInfoService;
import com.sun.corba.ee.impl.folb.GroupInfoServiceBase;
import com.sun.corba.ee.spi.folb.GroupInfoServiceObserver;
import com.sun.corba.ee.spi.folb.SocketInfo;

import com.sun.corba.ee.impl.misc.ORBUtility;
import java.util.ArrayList;

/**
 * @author Harold Carr
 */
public class GroupInfoServiceImpl
    extends org.omg.CORBA.LocalObject
    implements GroupInfoService
{
    private List<String> currentInstances;
    private GIS gis;
    private boolean debug = true; // REVISIT - get from ORB

    private class GIS extends GroupInfoServiceBase
    {
        public List<ClusterInstanceInfo> internalClusterInstanceInfo(
            List<String> endpoints ) { 
            throw new RuntimeException( "Should not be called" ) ;
        }

        @Override
        public List<ClusterInstanceInfo> getClusterInstanceInfo(
            String[] adapterName, List<String> endpoints )
        {
            return getClusterInstanceInfo( adapterName ) ;
        }

        @Override
        public List<ClusterInstanceInfo> getClusterInstanceInfo(
            String[] adapterName)
        {
            String adapter_name = ORBUtility.formatStringArray(adapterName);

            try {
                if (debug) dprint(".getMemberAddresses->: " + adapter_name);
                if (debug) dprint(".getMemberAddresses: " + adapter_name 
                       + ": current members: " + currentInstances);

                List<ClusterInstanceInfo> info =
                    new LinkedList<ClusterInstanceInfo>();
                ClusterInstanceInfo instanceInfo;


                String hostName = "";
                try {
                    hostName = InetAddress.getLocalHost().getHostAddress();
                } catch (UnknownHostException e) {
                    dprint(".getMemberAddresses: " + adapter_name 
                           + ": exception: " + e);
                    e.printStackTrace(System.out);
                    System.exit(1);
                }

                for (int i=0; i<corba.folb_8_1.Common.socketTypes.length; ++i){

                    if (! currentInstances.contains(corba.folb_8_1.Common.socketTypes[i])) {
                        if (debug) dprint(".getMemberAddresses: " + adapter_name 
                               + ": NOT in current members: " + 
                               corba.folb_8_1.Common.socketTypes[i]);
                        continue;
                    }

                    if (debug) dprint(".getMemberAddresses: " + adapter_name 
                           + ":IN current members: " + 
                           corba.folb_8_1.Common.socketTypes[i]);

                    //
                    // A BAD Address.
                    //

                    SocketInfo siBad =
                        new SocketInfo("t" + i, "bad" + i, i + 1);


                    //
                    // A Good Address.
                    //

                    SocketInfo si = 
                        new SocketInfo(corba.folb_8_1.Common.socketTypes[i],
                                       hostName,
                                       corba.folb_8_1.Common.socketPorts[i]);

                    //
                    // One fake instance.
                    //
                    List<SocketInfo> socketInfos = new ArrayList<SocketInfo>() ;
                    socketInfos.add( siBad ) ;
                    socketInfos.add( si ) ;
                    instanceInfo = 
                        new ClusterInstanceInfo("instance-" + i, i + 1,
                                                socketInfos);
                    info.add(instanceInfo);

                    //
                    // REVISIT: this is not used in testing - remove
                    //
                    // Only add one good address in test ReferenceFactory.
                    //

                    if (isNoLabelName(adapterName)) {
                        if (debug) dprint(".getMemberAddresses: " + adapter_name
                               + ": no label ReferenceFactory - only added one good address");
                        break;
                    }
                }

                return info;

            } catch (RuntimeException e) {
                dprint(".getMemberAddresses: " + adapter_name 
                       + ": exception: " + e);
                e.printStackTrace(System.out);
                System.exit(1);
                throw e;
            } finally {
                if (debug) dprint(".getMemberAddresses<-: " + adapter_name);
            }
        }

        @Override
        public boolean shouldAddAddressesToNonReferenceFactory(
            String[] adapterName)
        {
            return Common.POA_WITH_ADDRESSES_WITH_LABEL.equals(
                adapterName[adapterName.length-1]);
        }

        @Override
        public boolean shouldAddMembershipLabel (String[] adapterName)
        {
            return ! isNoLabelName(adapterName);
        }

        ////////////////////////////////////////////////////
        //
        // Implementation
        //

        private boolean isNoLabelName(String[] adapterName)
        {
            return Common.RFM_WITH_ADDRESSES_WITHOUT_LABEL.equals(
                adapterName[adapterName.length-1]);
        }
    }

    public GroupInfoServiceImpl()
    {
        gis = new GIS();
        currentInstances = new LinkedList<String>();
        for (int i = 0; i < corba.folb_8_1.Common.socketTypes.length; ++i){
            currentInstances.add(corba.folb_8_1.Common.socketTypes[i]);
        }
    }

    ////////////////////////////////////////////////////
    //
    // GroupInfoService
    //

    public boolean addObserver(GroupInfoServiceObserver x) 
    {
        return gis.addObserver(x);
    }

    public void notifyObservers()
    {
        gis.notifyObservers();
    }

    @Override
    public List<ClusterInstanceInfo> getClusterInstanceInfo(
        String[] adapterName, List<String> endpoints )
    {
        return gis.getClusterInstanceInfo(adapterName,endpoints);
    }

    public List<ClusterInstanceInfo> getClusterInstanceInfo(
        String[] adapterName)
    {
        return gis.getClusterInstanceInfo(adapterName);
    }

    public boolean shouldAddAddressesToNonReferenceFactory(
        String[] adapterName)
    {
        return gis.shouldAddAddressesToNonReferenceFactory(adapterName);
    }

    public boolean shouldAddMembershipLabel (String[] adapterName)
    {
        return gis.shouldAddMembershipLabel(adapterName);
    }

    ////////////////////////////////////////////////////
    //
    // Implementation used by GroupInfoServiceTestServant
    //

    public boolean add(String x)
    {
        if (debug) dprint(".add->: " + x);
        if (debug) dprint(".add: current members before: " + currentInstances);
        boolean result = currentInstances.add(x);
        if (debug) dprint(".add: current members after : " + currentInstances);
        notifyObservers();
        if (debug) dprint(".add<-: " + x + " " + result);
        return result;
    }


    public boolean remove(String x)
    {
        if (debug) dprint(".remove->: " + x);
        if (debug) dprint(".remove: current members before: " + currentInstances);
        boolean result = currentInstances.remove(x);
        if (debug) dprint(".remove: current members after : " + currentInstances);
        notifyObservers();
        if (debug) dprint(".remove<-: " + x + " " + result);
        return result;
    }

    private static void dprint(String msg)
    {
        ORBUtility.dprint("GroupInfoServiceImpl", msg);
    }
}

// End of file.
