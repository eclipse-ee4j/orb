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
