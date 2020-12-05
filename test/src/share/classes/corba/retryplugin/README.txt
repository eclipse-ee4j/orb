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

This test shows how to stop an ORB from receiving further requests by
throwing TRANSIENT to the client.

REVISIT:  This test should be refactored to use callbacks from the
server to the client to indicate test phase changes (i.e., rejecting
on/off; always rejecting/retry timeout; shutdown) rather than Thread.sleep.

------------------------------------------------------------------------------
        ***** IMPORTANT NOTICE *****

Should only use RetryClientRequestInterceptor if the client *ONLY* talks
to stateless beans.

If client even talks to one single stateful bean then it should *NOT*
install the RetryClientRequestInterceptor.  Instead the client should
be changed to catch TRANSIENT and retry.

------------------------------------------------------------------------------

Operation:

The client will keep retrying the requests that get TRANSIENT until
success or until the server is shutdown.

If the server is shutdown the client will try another server address,
if the reference has multiple addresses.  If there is only one address
then a MarshalException/COMM_FAILURE is thrown to the client
application.

Configuration:

The server-side installs the RetryServerRequestInterceptor.
Call RetryServerRequestInterceptor.setRejectingRequests(true)
to stop receiving accepting further requests.  After an appropriate
period of time shutdown the server.

The client-side install the RetryClientRequestInterceptor.
No changes are necessary in client code.

Note:

If the timeout is reached then the TRANSIENT exception is thrown to the client.
However, a bug in the ORB may mask that exception and return an
exception indicating the client info stack is null.  That exception
can be ignored and treated as TRANSIENT timing out.

Note:

If testing this in an ORB after Oct 4, 2005, the
RetryClientRequestInterceptor will not even see the TRANSIENT since
that is handled by the ContactInfoListIterator.

// End of file.
