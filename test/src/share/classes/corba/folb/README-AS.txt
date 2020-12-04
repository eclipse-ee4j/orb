#
# Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
#
# This program and the accompanying materials are made available under the
# terms of the Eclipse Public License v. 2.0 which is available at
# http://www.eclipse.org/legal/epl-2.0, or the Eclipse Distribution License
# v. 1.0 which is available at
# http://www.eclipse.org/org/documents/edl-v10.php.
#
# This Source Code may also be made available under the following Secondary
# Licenses when the conditions for such availability set forth in the Eclipse
# Public License v. 2.0 are satisfied: GNU General Public License v2.0
# w/Classpath exception which is available at
# https://www.gnu.org/software/classpath/license.html.
#
# SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause OR GPL-2.0 WITH
# Classpath-exception-2.0
#

This file shows how IIOP FOLB is configured in AS.
It is slightly complicated by the late requirement of support for
static clusters (ala 8.1 EE) is GMS is disabled.

------------------------------------------------------------------------------
DYNAMIC and STATIC:

    IIOPSSLSocketFactory (via ORBData)
    ReferenceFactoryManager (via RIR)
    CSIv2SSLTaggedComponentHandler (via RIR)
    ClientGroupManager (via ORBConfigurator)
        ClientRequestInterceptor, (via ORBInitializer)
        IIOPPrimaryToContactInfo, (via ORBData)
        IORToSocketInfo (via ORBData)
                        (uses CSIv2SSLTaggedComponentHandler.extract)
        Offers FOLB_CLIENT_GROUP_INFO_SERVICE (via RIR)
    S1ASSerialContextFactory (via Property)
        Registers with FOLB_CLIENT_GROUP_INFO_SERVICE (via RIR)

------------------------------------------------------------------------------
DYNAMIC:

    com.sun.enterprise.ee.ejb.iiop.IiopFolbGmsClient (via RIR)
        Registers with GMS
        Offers FOLB_SERVER_GROUP_INFO_SERVICE (via RIR)
    ServerGroupManager (via ORBConfigurator)
        IORInterceptor (via ORBInitializer)
                       (uses CSIv2SSLTaggedComponentHandler.insert)
        ServerRequestInterceptor (via ORBInitializer)
        Registers with FOLB_SERVER_GROUP_INFO_SERVICE (via RIR)
    TxSecIORInterceptor - disable adding CSIv2 addresses.
                        (via ORBInitializer)

------------------------------------------------------------------------------
STATIC:

    FailoverIORInterceptor (via Property)-adds failover addresses (from ADMIN).
    TxSecIORInterceptor (via ORBInitializer) - adds CSIv2 addresses.

;;; End of file.
