#
# Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
#
# This program and the accompanying materials are made available under the
# terms of the Eclipse Distribution License v. 1.0, which is available at
# http://www.eclipse.org/org/documents/edl-v10.php.
#
# SPDX-License-Identifier: BSD-3-Clause
#

Summary and suggested reading order:

==============================================================================
Connection interceptor (called an ORBSocketFactory):

Summary:

The server side part of the ORBSocketFactory is told the type to
create as well as a port number.

The client side part of the ORBSocketFactory is called on every client
request.  An ORB first asks the factory for type/host/port information
(given an IOR).  If the ORB already has a connection of the
type/host/port it will use the existing connection.  Otherwise it will
then ask the factory to create a client socket, giving it that
type/host/port.  Finally, the createSocket method may throw an
exception to tell the ORB to ask it for type/host/port info again.
The information passed back and forth between the ORB and factory can
act as a cookie for the factory if desired.

Interfaces:

        com.sun.corba.ee.spi.legacy.connection.ORBSocketFactory
        com.sun.corba.ee.spi.legacy.connection.EndPointInfo
        com.sun.corba.ee.spi.legacy.connection.GetEndPointInfoAgainException

==============================================================================
Access to a request's socket:

Summary:

The request's socket is available via ClientRequestInfo and
ServerRequestInfo.  We enable this by having them implement the
RequestInfoExt interface.

Interfaces:

        com.sun.corba.ee.spi.legacy.interceptor.RequestInfoExt
        com.sun.corba.ee.spi.legacy.connection.Connection

==============================================================================
Extending IORInfo to support the multiple server port API:

Summary:

We support the multiple server port API in PortableInterceptors by
having IORInfo implement the IORInfoExt interface.  The description on
how to use the multiple server port APIs is found in
ORBSocketFactory.java.

Interfaces:

       com.sun.corba.ee.spi.legacy.interceptor.IORInfoExt
       com.sun.corba.ee.spi.legacy.interceptor.UnknownType

;; End.


