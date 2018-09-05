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
Operation:

The server side code looks up all the host's address and puts them
into TAG_ALTERNATE_IIOP_ADDRESS components in IORs.  It does that
using proprietary APIs (it is possible to use standard APIs to create
and insert the TAG_ALTERNATE_IIOP_ADDRESS component) within standard OMG
PortableInterceptor IORInteceptors.

When the client side makes the first request on an IOR it looks for
TAG_ALTERNATE_IIOP_ADDRESS components.  If any are found it uses the
address in the first one found.  It does this by overriding the
getEndPointInfo method of the DefaultSocketFactory provided with the
ORB.  This is a proprietary API.

------------------------------------------------------------------------------
Prepare:

Note: IorIntSockFactTest.java is part of the CORBA unit test framework.
Delete that file before compile with ExampleMakefile standalone.

rm IorIntSockFactTest.java

------------------------------------------------------------------------------
Compile:

export ALT_BOOTDIR=<jdk home>
gnumake -f ExampleMakefile b

------------------------------------------------------------------------------
Run:

# Start ORBD
gnumake -f ExampleMakefile o &

# Start server (note it prints "Server is read.")
gnumake -f ExampleMakefile s &

# Run client (note it prints "Server echoes: Hello" and exits).
gnumake -f ExampleMakefile c

kill <s and o process ids>

;;; End of file.
