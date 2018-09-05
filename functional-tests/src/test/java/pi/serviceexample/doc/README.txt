#
#
# This program and the accompanying materials are made available under the
# terms of the Eclipse Distribution License v. 1.0, which is available at
# http://www.eclipse.org/org/documents/edl-v10.php.
#
# SPDX-License-Identifier: BSD-3-Clause
#

<h3>Flowing Context Information between Clients and Servers</h3>

A typical interceptor-based service will flow context information
between clients and servers.  The AService example shows how this
information is flowed from the client thread into the client
interceptors, across the wire into the server interceptors, to the
servant thread and back.

It is important to note that the client and servant is not aware that
the service is implemented using interceptors.  Instead, they interact
with the service through local object references (the aService
reference in this example).

<AService diagram>

1. The client invokes aService.begin().
1a. aService.begin() sets the service context information in a slot
    it has reserved in PICurrent.
2. The client invokes a method on some reference (i.e., ref.method()).
   This invocation is taking place inside the context of the service since
   aService.begin() has been invoked.
3. send_request is entered for the ref.method call.
4. get_effective_component(s) may be called to determine if the
   reference being invoked needs to interact with the service.
5. get_slot is called to retrieve the context set in step 1a.
6. The service interceptor adds a service context containing the 
   appropriate context (most likely depending on the context set in
   step 1a and any components retrieved in step 4).  It uses
   add_request_service_context to add this context information to
   the request which is sent over the wire.
7. The ref.method request arrives at the server, activating the
   receive_request_service_contexts interception point.
8. get_request_service_context is used to retrieve the service context
   added at step 6.
9. ServerRequestInfo.set_slot is used to transfer the context information
   from the service context retrieved in step 8 to the logical thread
   local data.
10. receive_request is entered.
11. The "ref" Servant's "method" is entered.
12. The servant method invokes aService.verify() to interact with the
    service.
13. The service uses get_slot on PICurrent to retrieve the context
    information sent from the client side.  The service may also
    use set_slot on PICurrent to set any return context information.
14. After the Servant completes, the send_* interception point is
    entered.  If the service set any return context information then
    it would add a service context to the return.  This is not shown
    in this example.

<h3>Avoiding Infinite Recursion During Interceptor Outcalls</h3>

Some services may need to make invocations on other CORBA object
references from within interception points. When making outcalls from
within interception points steps must be taken to avoid infinite
recursion since those outcalls will flow through the inteception
points. The LoggingService example illustrates this case.

The LoggingService example is comprised of ClientRequestInterceptors
registered in a client program, and ServerRequestInterceptors
registered in a server program.  These interceptors send information
from the client and server to a LoggingService implemenation which
logs this information.

However, since the LoggingService implementation is itself a CORBA
server, we must ensure that we do not log calls to the logger.  The
following diagrams illustrate the steps taken to avoid infinite
recursion.

The following diagram shows the simplest case of avoiding recursion
when calling an external logger from within interceptors. These steps
are useful for the case where the client ORB only contains
ClientRequestInterceptors, the server ORB only contains
ServerRequestInterceptors, and the LoggingService is external to both
the client and server ORBs.

<LoggingService diagram>

1. The client invokes a method on some reference.
2. The client interceptor's send_request method is entered for the method
   invoked in step 1.
3. A slot reserved for indicating an outcall is set to true on PICurrent.
4. The same slot is checked on ClientRequestInfo via get_slot.
   This value will not be set since it represents the client's thread
   state at step #1.  Therefore a call to the logger will be made.
5. send_request is entered for the logger invocation made in step 4.
6. A slot reserved for indicating an outcall is set to true on PICurrent.
   Note, this slot is always unconditionally set.  This is necessary
   when there are 2 or more interceptors doing outcalls.  Since interceptors
   do not know about the existence of other interceptors we
   always set the PICurrent slot.
7. The slot is checked on ClientRequestInfo via get_slot.
   This time the value is set (from step 3).  Therefore we do not log
   this call (which is the call to the logger itself).  The send_request
   point entered at step 5 finishes.
8. The ORB sends the log invocation made at step 4 to the logging service.
9. The send_request point for step 2 finishes.  The ORB sends the
   initial client invocation made in step 1 to the server for the
   appropriate reference.
10. The server's receive_request_service_contexts point is entered.
    It logs the incoming request.  A server which does not contain
    client interceptors which make outcalls and which does not contain the 
    implementation of the logger itself does not need to set recursion
    avoidance slots.  That is what this diagram represents.
    Therefore all the server side interception points are entered
    with no further checking and the response from the method initially
    invoked by the client is returned to the client ORB.
14. The client's receive_* point is entered.  Steps similar to
    2-9 will occur if this point needs to make an outcall.

<h3>Avoiding recursion for colocated outcall references</h3>

The following diagram illustrates the case where the LoggingService
may be colocated in the same ORB as the reference being invoked by the
client.  In general, it is not possible to know that a particular
object reference is NOT colocated with any other objects hosted by
that ORB.  Therefore, to cover all corner cases, more steps must be
taken.

This diagram only shows the server side.  The client side steps are
identical to those in the preceeding diagram.

<LoggingServiceColocated diagram>

1. The request from the client ORB arrives, activating the
   receive_request_service_contexts interception point.
2. A slot reserved for indicating an outcall is set to true on PICurrent.
3. ServerRequestInfo.get_request_service_context is used to check for
   the presence of a service context indicating an outcall.
   The service context is not present so the logger is invoked from
   within the interception point.
4. The send_request interception point is entered for the logger request.
5. get_slot check for the outcall indicator.  
6. The outcall indicator slot is set (from step #2).
   Therefore add_request_service_context is used to a service context
   indicating an outcall. This is necessary since there is no logical
   relationship between client and server threads.
7. The logger invocation arrives at receive_request_service_contexts.
8. A slot reserved for indicating an outcall is set to true on PICurrent.
9. ServerRequestInfo.get_request_service_context is used to check for
   the presence of a service context indicating an outcall.
   The service context is present so no further action is taken.
   receive_request_service_contexts exits.
10. The logger request proceeds to receive_request.  Steps similar
    to those taken inside receive_request_service_context are necessary
    if the logger is called at the receive_request point.  That is
    not shown in the diagram (although the example code logs all points).
11. The logger request arrives at the LoggingService Servant, which
    logs the initial client request (not invocation of the logger).
12. The logger request proceeds to ServerRequestInterceptor.send_*
    (most likely send_reply).
13. The logger request proceeds to ClientRequestInterceptor.receive_*
    (most likely receive_reply).

The main point illustrated in this example is the necessity of using both
client and server interceptors in conjunction with PICurrent slots and
service contexts to indicate an outcall.

<h3>Avoid recursion by using multiple ORBs</h3>

A simpler way to avoid recursion is to ensure that the outcall references
are associated with a different ORB that does not have the logging
interceptors registered.  That way the outcall invocations never go through
interceptors.

This seems like a simple solution, but, in general, interceptors are
registered via properties passed to the VM during startup.  This means that
all ORBs created in that VM will contain all interceptors so this solution
will not work.

This solution will only work where interceptors are explicitly registered
in client code during ORB.init.  However, that is not a typical nor
a recommended way to register interceptor-based services.

<h1>Example code</h1>

The following files contain the code which the above diagrams illustrate.
Instructions for compiling and executing these examples follow the code.

<files here>

<h1>Compiling and running the application</h1>

// End of file.
