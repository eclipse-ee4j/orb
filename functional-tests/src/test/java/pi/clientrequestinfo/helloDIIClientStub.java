/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package pi.clientrequestinfo;

import org.omg.CORBA.*;
import java.rmi.Remote;
import java.rmi.RemoteException;

import ClientRequestInfo.*;

/**
 * Utility class to encapsulate the logic to make DII calls on the 
 * hello interface.
 */
public class helloDIIClientStub 
{

  /** The CORBA object to make DII calls on */
  private org.omg.CORBA.Object object;

  /** The ORB to use to create objects */
  private ORB orb;

  /**
   * Creates a new helloDIIClientStub which will make DII calls on the given
   * corba object, and will create objects using the given ORB.
   */
  public helloDIIClientStub( ORB orb, org.omg.CORBA.Object object ) {
      this.object = object;
      this.orb = orb;
  }

  public org.omg.CORBA.Object getObject() {
      return object;
  }

  String sayHello() {
      // Create parameter list:
      NVList argList = orb.create_list( 0 );

      // Create result parameter:
      Any result = orb.create_any();
      result.insert_string( "dummy" );
      NamedValue resultVal = orb.create_named_value( "result", result, 
          org.omg.CORBA.ARG_OUT.value );

      // Invoke method:
      Request thisReq = object._create_request( null, "sayHello", 
          argList, resultVal );
      thisReq.invoke();

      // Return result:
      result = thisReq.result().value();
      return result.extract_string();
  }

  String saySystemException() {
      // Create parameter list:
      NVList argList = orb.create_list( 0 );

      // Create result parameter:
      Any result = orb.create_any();
      result.insert_string( "dummy" );
      NamedValue resultVal = orb.create_named_value( "result", result, 
          org.omg.CORBA.ARG_OUT.value );

      // Invoke method:
      Request thisReq = object._create_request( null, "saySystemException", 
          argList, resultVal );
      thisReq.invoke();

      // Return result:
      result = thisReq.result().value();
      return result.extract_string();
  }

  String sayUserException() 
      throws UnknownUserException, ExampleException 
  {
      // Create parameter list:
      NVList argList = orb.create_list( 0 );

      // Create result parameter:
      Any result = orb.create_any();
      result.insert_string( "dummy" );
      NamedValue resultVal = orb.create_named_value( "result", result, 
          org.omg.CORBA.ARG_OUT.value );

      // Exception list:
      ExceptionList excList = orb.create_exception_list();
      excList.add( ExampleExceptionHelper.type() );

      // Invoke method:
      Request thisReq = object._create_request( null, "sayUserException", 
          argList, resultVal, excList, null );
      thisReq.invoke();

      // Analyze environment to see if UserException was thrown.
      Environment env = thisReq.env();
      Exception exc = env.exception();

      // _REVISIT_ For now, we will accept either UnknownUserException or
      // ExampleException since the DII code in our ORB does not provide us
      // access to the actual exception that was thrown (see 
      // RequestImpl.doInvocation)

      if( exc instanceof UnknownUserException ) {
          throw (UnknownUserException)exc;
      }
      else if( exc instanceof ExampleException ) {
          throw (ExampleException)exc;
      }
      else {
          throw new RuntimeException( 
              "sayUserException: Unexpected exception: " + exc );
      }
  }

  void sayOneway() {
      // Create parameter list:
      NVList argList = orb.create_list( 0 );

      // No result parameter:

      // Invoke method as a oneway:
      Request thisReq = object._create_request( null, "sayOneway", 
          argList, null );
      thisReq.send_oneway();
  }

  String sayArguments( String arg1, int arg2, boolean arg3 ) {
      // Create parameter list:
      NVList argList = orb.create_list( 3 );
      Any par1Value = orb.create_any();
      par1Value.insert_string( arg1 );
      Any par2Value = orb.create_any();
      par2Value.insert_long( arg2 );
      Any par3Value = orb.create_any();
      par3Value.insert_boolean( arg3 );
      argList.add_value( "arg1", par1Value, ARG_IN.value );
      argList.add_value( "arg2", par2Value, ARG_IN.value );
      argList.add_value( "arg3", par3Value, ARG_IN.value );

      // Add two contexts so we can test contexts():
      ContextList ctxList = orb.create_context_list();
      ctxList.add( "context1" );
      ctxList.add( "context2" );

      // Create result parameter:
      Any result = orb.create_any();
      result.insert_string( "dummy" );
      NamedValue resultVal = orb.create_named_value( "result", result, 
          org.omg.CORBA.ARG_OUT.value );

      // Invoke method:
      Request thisReq = object._create_request( null, "sayArguments", 
          argList, resultVal, null, ctxList );
      thisReq.invoke();

      // Return result:
      result = thisReq.result().value();
      return result.extract_string();
  }

  void clearInvoked() {
      // Create parameter list:
      NVList argList = orb.create_list( 0 );

      // No result parameter:

      // Invoke method:
      Request thisReq = object._create_request( null, "clearInvoked", 
          argList, null );
      thisReq.invoke();
  }

  boolean wasInvoked() {
      // Create parameter list:
      NVList argList = orb.create_list( 0 );

      // Create result parameter:
      Any result = orb.create_any();
      result.insert_boolean( false );
      NamedValue resultVal = orb.create_named_value( "result", result, 
          org.omg.CORBA.ARG_OUT.value );

      // Invoke method:
      Request thisReq = object._create_request( null, "wasInvoked", 
          argList, resultVal );
      thisReq.invoke();

      // Return result:
      result = thisReq.result().value();
      return result.extract_boolean();
  }

  void resetServant() {
      // Create parameter list:
      NVList argList = orb.create_list( 0 );

      // No result parameter:

      // Invoke method:
      Request thisReq = object._create_request( null, "resetServant", 
          argList, null );
      thisReq.invoke();
  }

}

