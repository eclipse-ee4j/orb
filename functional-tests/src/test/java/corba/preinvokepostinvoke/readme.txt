#
# Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
#
# This program and the accompanying materials are made available under the
# terms of the Eclipse Distribution License v. 1.0, which is available at
# http://www.eclipse.org/org/documents/edl-v10.php.
#
# SPDX-License-Identifier: BSD-3-Clause
#

Author: Hemanth Puttaswamy

This test is to validate that the calls to ServantLocator.preinvoke() and 
ServantLocator.postinvoke() is balanced if we use ServantCachingPolicy.

The test contains 1 POA and 1 ServantLocator, 2 different Servants are bound 
in NameService with the names "Instance1" and "Instance2". The Interface
for the Servant contains 2 methods o1 and o2. When client invoke Instance1.o1,
it will invoke Instance2.o2. This should result in a call trace of
------
MyServantLocator.preinvoke called 
Interface1.o1 called with Invoking from Client...
MyServantLocator.preinvoke called 
Interface.o2 called with Invoking from Interface.o1...
MyServantLocator.postinvoke called 
MyServantLocator.postinvoke called 
-----
