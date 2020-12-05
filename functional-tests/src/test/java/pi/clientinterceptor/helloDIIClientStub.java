/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Eclipse Distribution License
 * v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License v. 2.0 are satisfied: GNU General Public License v2.0
 * w/Classpath exception which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause OR GPL-2.0 WITH
 * Classpath-exception-2.0
 */

package pi.clientinterceptor;

import org.omg.CORBA.*;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Utility class to encapsulate the logic to make DII calls on the 
 * hello interface.
 */
public class helloDIIClientStub {

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

  void sayOneway() {
      // Create parameter list:
      NVList argList = orb.create_list( 0 );

      // No result parameter:

      // Invoke method as a oneway:
      Request thisReq = object._create_request( null, "sayOneway", 
          argList, null );
      thisReq.send_oneway();
  }

  boolean _is_a( String repository_id ) {
      // Create parameter list:
      NVList argList = orb.create_list( 0 );

      Any a1 = orb.create_any();
      a1.insert_string( repository_id );
      argList.add_value( "repository_id", a1, ARG_IN.value );

      // Create result parameter:
      Any result = orb.create_any();
      result.insert_boolean( true );
      NamedValue resultVal = orb.create_named_value( "result", result, 
          org.omg.CORBA.ARG_OUT.value );

      // Invoke method:
      Request thisReq = object._create_request( null, "_is_a", 
          argList, resultVal );
      thisReq.invoke();

      // Return result:
      result = thisReq.result().value();
      return result.extract_boolean();
  }

  boolean _non_existent() {
      // Create parameter list:
      NVList argList = orb.create_list( 0 );

      // Create result parameter:
      Any result = orb.create_any();
      result.insert_boolean( false );
      NamedValue resultVal = orb.create_named_value( "result", result, 
          org.omg.CORBA.ARG_OUT.value );

      // Invoke method:
      Request thisReq = object._create_request( null, "_non_existent", 
          argList, resultVal );
      thisReq.invoke();

      // Return result:
      result = thisReq.result().value();
      return result.extract_boolean();
  }

  org.omg.CORBA.Object _get_interface_def() {
      // Create parameter list:
      NVList argList = orb.create_list( 0 );

      // Create result parameter:
      Any result = orb.create_any();
      result.insert_Object( (org.omg.CORBA.Object)null );
      NamedValue resultVal = orb.create_named_value( "result", result, 
          org.omg.CORBA.ARG_OUT.value );

      // Invoke method:
      Request thisReq = object._create_request( null, "_get_interface_def", 
          argList, resultVal );

      try {
          thisReq.invoke();
      }
      catch( BAD_OPERATION e ) {
          // expected, since we do not implement _get_interface_def in our ORB.
      }

      // Return result:
      result = thisReq.result().value();
      return result.extract_Object();
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

