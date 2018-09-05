#
# Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
#
# This program and the accompanying materials are made available under the
# terms of the Eclipse Distribution License v. 1.0, which is available at
# http://www.eclipse.org/org/documents/edl-v10.php.
#
# SPDX-License-Identifier: BSD-3-Clause
#

This tests connection interception.

Be sure to read the JavaDoc in:
   com/sun/corba/ee/spi/legacy/connection/*
   com/sun/corba/ee/spi/legacy/interceptor/*

Also tests:

This tests proprietary interceptor ordering.
If ordering fails it will raise INTERNAL/-45.

This ensures that interceptor calls are balanced when the ORB is shutdown.
If it fails it raises a RuntimeException.

==============================================================================
Transient

ORBD:

orbd started with no VM properties nor command line args.

SERVER:

ServerTransient started with no VM properties nor command line args.

ServerTransient starts ServerCommon with Transient arg.

ServerCommon sets the socket factory and port properties.

During ORB.init three ports are opened for listening (besides the
default clear text port).

During ORB.init establish_components adds one "Listen Ports"
tagged component containing the data on the 3 "extra" ports.
It gets that data from IORInfoExt (proprietary).

CLIENT:

Client started with no VM properties nor command line args.

When using the default socket factory the client always makes
requests to the server by connecting to the default listen port.

When using custom socket factory the client makes requests to the
server by connecting to ports default, 1, 2, 3 (in a cycle).
Note that once it makes a connection it uses the ephemeral port
allocated by accept (normal TCP/IP operation).

Besides cycling through the listen ports, the client, on the first
invocation on each reference, get bad info back from the socket
factory to test the try again loop.


==============================================================================
Persistent

ORBD:

Start up with VM args:

   -Dcom.sun.corba.ee.POA.ORBBadServerIdHandlerClass=corba.connectintercept_1_4.ORBDBadServerIdHandler
   -Dcom.sun.corba.ee.connection.ORBSocketFactoryClass=corba.connectintercept_1_4.MySocketFactory
   -Dcom.sun.corba.ee.connection.ORBListenSocket=MyType1:2000,MyType2:2001,MyType3:0

SERVER:

Use ServerTool to register ServerPersistent no VM properties nor
command line args). 

ripServerTool -cmd register -applicationName "corba.connectintercept_1_4.ServerPersistent" \
        -server corba.connectintercept_1_4.ServerPersistent -classpath ...

CLIENT:

Client started with no VM properties nor command line args.

//
