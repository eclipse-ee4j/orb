#
# Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
#
# This program and the accompanying materials are made available under the
# terms of the Eclipse Distribution License v. 1.0, which is available at
# http://www.eclipse.org/org/documents/edl-v10.php.
#
# SPDX-License-Identifier: BSD-3-Clause
#

------------------------------------------------------------------------------
General Interaction:

client                 server



A general call to see IORUpdates and failover:

                                             (RFM with and without components)
--- echo --+   +-------------------------------------------+    +---> Test
           |   |                                           |    |
           |   |                                           |    |
           v   |                                           |    v
 ClientGroupManager                                        | ServerRequestInt
  IORToSocketInfo                                          |  send_reply
   (get addresses from IOR)                                |   check mem label
  IIOPrimaryToContactInfo                                  |   maybe IORUpdate
   (.hasNext/.next - FailoverCache)                        |    |
  ClientRequestInterceptor                                 |    |
   (send membership label                                  |    |
     and handle IORUpdate)
                                                       ServerGroupManager

                                                         |     ^      |
                                                         |     |      |
                                                    register  notify  getInfo
                                                         |     |      |
                                                         |     |      |
                                                         |     |      |
                                                         |     |      |
Adding and removing "instances" to test IORUpdates:      v     |      v

--- add/remove ------> GISTest/(POA) --- add/remove -->     GISImpl



Adding and removing acceptors/connections to cause failover:

--- add/remove ------> Test/(POA)

------------------------------------------------------------------------------
Tests:

Bootstrap:
        Missing membership label
        Therefore IORUpdate

Normal operation:
        Send label
        No IORUpdate

IORUpdate only:
        Setup: kill "instance"
        Execute:
                Invoke
                IORUpdate

Failover without update:
        Setup: kill listener/connections.
        Execute:
                Invoke
                Cannot connect to address
                Therefore try next address
                No IORUpdate since GIS not aware of "failure"

Failover with update:
        Setup: kill "instance"
        Then setup/execute above: Failover with update.
        IORUpdate

Independent POAs:
        Ensure no address tag or membership labels added to POAs.
        Ensure no restarted by RFM.

"Circular" failover success:
        Setup: kill listener/connections and invoke to get in middle of list.
               kill listener/connections in remainder of list.
               restart listener/connections before middle of list.
        Execute:
                Invoke
                Ensure .hasNext/.next "mod" to beginning of list
                       on a single request

"Circular" failover fail:
        Same as above but do not restart.
        Ensure failure returned to client when get back to 
        middle starting point in list.

------------------------------------------------------------------------------
Server-side configuration:

Server.setProperties:
        Sets persistent server port.
        Sets user-defined listen ports. W, X, Y, Z.
        Registers ORBConfigurator for the ServerGroupManager.
                 addORBInitializer
                        add_ior_interceptor
                        add_server_request_interceptor
        Registers ORBConfigurator for test.
                 register_initial_reference of fake GIS for test.
                 register_initial_reference of real ReferenceFactoryManager

Server.main:
        ORB.init executes configurators.

        Create a ReferenceFactory for the test.
        Create (using above RF) and bind reference for the test.

        Create a "special" ReferenceFactory that does not add components.
        Create (using special RF) and bind a reference for the test.

        Create and bind an object managed by an independent POA.
        (This object also controls fake GIS.)

ServerGroupManager.initialize
        updateMembershipLabel - first time.
        Get RFM and GIS from initial references
        Register with GIS for change notifications.
        

// End of file.
