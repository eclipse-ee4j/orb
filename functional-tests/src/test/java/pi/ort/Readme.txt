#
# Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
#
# This program and the accompanying materials are made available under the
# terms of the Eclipse Distribution License v. 1.0, which is available at
# http://www.eclipse.org/org/documents/edl-v10.php.
#
# SPDX-License-Identifier: BSD-3-Clause
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

