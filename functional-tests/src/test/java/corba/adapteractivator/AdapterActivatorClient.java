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

package corba.adapteractivator ;

import org.omg.CORBA.ORB;

import corba.framework.statusU;
import corba.framework.RTMConstants;
import corba.framework.GetID;
import org.glassfish.pfl.test.JUnitReportHelper;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextHelper;

/**
* Purpose                       : To verify that AdapterActivator implementation registered, recreates
*                                         the POA when ORB receives a request for an object reference that
*                                         identifies the target POA does not exist
*
* Expected Result       : Client gets a valid response
*
* Purpose                       : To verify that client gets a OBJECT_NOT_EXIST exception when
*                                         ORB receives a request for an object reference that identifies
*                                         the target POA does not exist and the AdapterActivator
*                                         implementation registered with rootPoa does not create the POA
*
* Expected Result       : Client gets OBJECT_NOT_EXIST exception
*
*/

public class AdapterActivatorClient {

    private Hello helloRef1,helloRef2;
    private Close closeRef;
    private NamingContext rootContext;
    private ORB orb;
    private statusU status = new statusU();
    private JUnitReportHelper helper = new JUnitReportHelper( AdapterActivatorClient.class.getName() ) ;

    final String testDesc = "To verify that the AdapterActivator registers and"+
             "\n             recreates the POA when the ORB received a request"+
             "\n             for an object reference which identifies the" +
             "\n             target POA that doesn't exist";

    public AdapterActivatorClient(String[] args) {

        try {
            orb = ORB.init(args,System.getProperties());

            // Get root Naming Context
            org.omg.CORBA.Object obj = orb.resolve_initial_references("NameService");
            rootContext = NamingContextHelper.narrow(obj);
            adapterActivatorTest1();
            adapterActivatorTest2();
            System.out.println("\nAdapterActivatorClient : Calling shutdown()");

            //Resolve Object Reference for CloseServer
            NameComponent nc = new NameComponent("CloseServer","");
            NameComponent name[] =      {nc};
            closeRef = CloseHelper.narrow(rootContext.resolve(name));
            closeRef.shutdown();
        } catch(Exception Ex) {
            status.addStatus(GetID.generateID(this, ""), RTMConstants.FAIL,
                             "Got exception: "+ Ex.toString());
        } finally {
            helper.done() ;
            status.printSummary(GetID.generateID(this, ""), testDesc);
            if (status.totalFail() > 0)
                System.exit(1) ;
        }
    }

    void adapterActivatorTest1() {

        String subTestName = "Test01";
        helper.start( subTestName ) ;
        try {

            // Resolve Object Reference
            NameComponent nc = new NameComponent("HelloServer1","");
            NameComponent name[] =      {nc};
            helloRef1 = HelloHelper.narrow(rootContext.resolve(name));

            System.out.println("\nAdapterActivator-1 Started\n");
            System.out.println("Calling Operation on HelloServant of Poa1. Poa1 is "+
                               "child of RootPoa, RootPoa is registered with AdapterActivator"+
                               " implementation which creates Poa1 otherwise returns false");
            System.out.println(helloRef1.sayHello());
            status.addStatus(subTestName, RTMConstants.PASS, "Calling "+
                             "operation on HelloServant of Poa1 ok");
            helper.pass() ;
        } catch(Exception ex) {
            helper.fail( ex ) ;
            ex.printStackTrace();
            status.addStatus(subTestName, RTMConstants.FAIL,
                             "Got exception: "+ ex.toString());
        }
    }

    void adapterActivatorTest2() {

        String subTestName = "Test02";
        helper.start( subTestName ) ;
        try {

            //Resolve Object Reference
            NameComponent nc = new NameComponent("HelloServer2","");
            NameComponent name[] =      {nc};
            helloRef2 = HelloHelper.narrow(rootContext.resolve(name));

            System.out.println("\nAdapterActivator-2 Started\n");
            System.out.println("Calling Operation on HelloServant of Poa2. Poa2 is "+
                               "child of RootPoa, RootPoa is registered with AdapterActivator "+
                               "implementation which creates Poa1 otherwise returns false");
            System.out.println(helloRef2.sayHello());
            helper.fail( "Unexpected success: should see OBJECT_NOT_EXIST exception" ) ;
        } catch (org.omg.CORBA.OBJECT_NOT_EXIST ex) {
            status.addStatus(subTestName, RTMConstants.PASS, "Operation on "+
                             "HelloServant threw the expected OBJECT_NOT_EXIST"+
                             " exception");
            helper.pass() ;
        } catch(Exception ex) {
            ex.printStackTrace();
            status.addStatus(subTestName, RTMConstants.FAIL,
                             "Got exception: "+ ex.toString());
            helper.fail( ex ) ;
        }
    }

    public static void main( String [] args ) {
        AdapterActivatorClient client = new AdapterActivatorClient(args);
    }
}
