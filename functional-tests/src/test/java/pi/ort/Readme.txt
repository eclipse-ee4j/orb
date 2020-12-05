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

To test ORT related interceptor notifications, the following infrastructure
is used.

1. ORTStateChangeEvaluator (A Singleton) recieves AdapterStateChange and 
   AdapterManagerStateChange Notifications from SampleIORInterceptor
   and provides utility methods to evaluate (compare) the expected state 
   Vs. the actual state. Server.java uses ORTStateChangeEvaluator to check 
   that the test is working by passing expected states.
2. SampleIORInterceptor simply reports the state changes to 
   ORTStateChangeEvaluator
3. Server.java drives the test

This test also verifies that ORBId and ORBServerId values are propogated
correctly to IORInterceptor.

