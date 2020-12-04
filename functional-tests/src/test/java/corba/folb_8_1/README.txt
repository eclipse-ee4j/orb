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

Server:

  - Opens five acceptors:
    The standard IIOP_CLEAR_TEXT port.
    A  "W" type at port 3333.
    An "X" type at port 4444.
    A  "Y" type at port 5555.
    A  "Z" type EPHEMERAL port.

  - unregister, given a port type, will find and kill the acceptor of
    that type and kill all connections accepted by that acceptor.

  - register, given a port type, will start up that acceptor at the
    ports show above.

Client: Without sticky:
        
  - Looks up a reference to I.
  - Tells the server to unregister (kill) its IIOP_CLEAR_TEST server
    port followed by the W, X, and Y ports.  It leaves the Z port open.
  - A ClientRequestInterceptor remembers the socket type on which each
    request is made (or if it is colocated).
  - Checks that the connection type is the expected one.
    The order expected ir IIOP_CLEAR_TEXT, W, X, Y.
  - Tells the server (via connection Z) to register (start up) W.
  - Tells the server to unregister W.
  - Checks that the unregister request goes on W (instead of Z) since
    no failover cache (sticky manager) is present.

Client: With sticky:

  - Same as above except that the request on the last step should
    go on Z even though W (earlier in the list) is present since
    the sticky manager remembers Z as the last good connection.

ColocatedCS: WithoutSticky and WithSticky:

  - Same as above but all requests do not use connetions
    regardless of the state of the server ports.

  - Since we use a local subcontract none of the failover code is invoked.

ClientTwoRefs and ColocatedClientTwoRefs: 

  - Test to ensure that we go to the correct object on the server.

  - If we used the entire ContactInfo as the return from the cache
    then we would use the wrong IOR.  Only use the SocketInfo and
    then find a match in the current ContactInfoList.

  - Colocated test for completeness.

ZeroPort:

  - REVISIT
    Need to be made into a separate test.

    When references contain a 0 port in their primary we hash
    on the entire IOR.  Otherwise we map IORs referring to two
    different servers to only one of the servers.

ClientForTiming:

  - When REALLY doing timing tests:
    + Set NUMBER_OF_WARMUP_LOOPS and NUMBER_OF_TIMING_LOOPS appropriately.
    + Set debug to false (so nothing is written to logs).
    + Either set logging to FINE or set ORBUtil.mc
    COMM_FAILURE/CONNECT_FAILURE to fine (to avoid writing logs).

  - This tests run and puts its results in the GEN directory
    but the results are NOT examined.

  - A white box test to measure:
    + NoFsNoFNoC: 
      Requests without failover support plugged in.
    + FsNoFNoc:
      Requests with failover support plugged in but no failover and no cache.
    + FSNoFC:
      Requests with failover support plugged in but no failover and cache.
    + FsFNoC:
      Requests with failover support plugged in, failover, no cache.
    + FsFC:
      Requests with failover support plugged in, failover, cache.

  - Relies ORB support and timing plugged into various parts of ORB. 

  - REVISIT:
    Note: there is a problem in FsFC mode:
    In setupFailover it does NOT find the map entry for the primary
    key - even though it is there.  NEEDS TO BE FIXED.

    This means that, when doing revertCache after a failover request
    that we put null into the map instead of the original
    IIOP_CLEAR_TEXT entry.  This means that on the next invocation
    we initialize the map rather than find the IIOP_CLEAR_TEXT entry.
    So the timing test is slightly not accurate.  Regardless, the
    IIOP_CLEAR_TEXT entry fails and we failover to W, which is what we
    wanted to time.
    
;;; End of file.


