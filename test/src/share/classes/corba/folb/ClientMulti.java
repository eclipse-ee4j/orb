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

//
// Created       : 2005 Sep 21 (Wed) 09:14:00 by Harold Carr.
// Last Modified : 2005 Sep 30 (Fri) 16:27:36 by Harold Carr.
//

package corba.folb;

import java.util.Hashtable;
import java.util.Properties;
import javax.naming.InitialContext;

import org.omg.CORBA.ORB;

import com.sun.corba.ee.impl.misc.ORBUtility;

import corba.framework.Controller;
import corba.hcks.U;

import org.testng.annotations.BeforeSuite ;
import org.testng.annotations.Test ;

import org.testng.Assert ;

/**
 * @author Harold Carr
 */
public class ClientMulti extends ClientBase {

    @BeforeSuite 
    public void clientSetup() {
        setup( getDefaultProperties() ) ;
    }

    @Test
    public void test() throws Exception
    {
        CallThread a =
            new CallThread(1000, testRfmWithAddressesWithLabel);
        CallThread b =
            new CallThread(1000, testRfmWithAddressesWithLabel);
        CallThread c =
            new CallThread(1000, testRfmWithAddressesWithLabel);
        a.start();
        b.start();
        c.start();

        do {
            gisPoaWithAddressesWithLabels.removeInstance(
                corba.folb_8_1.Common.Z);
            Thread.sleep(1000);
            gisPoaWithAddressesWithLabels.addInstance(
                corba.folb_8_1.Common.Z);
            Thread.sleep(1000);
        } while (!a.done || !b.done || !c.done);

        Assert.assertTrue( (a.failures + b.failures + c.failures) == 0 ) ;
    }

    public static void main(String[] av) {
        doMain( ClientCircular.class ) ;
    }
}

class CallThread extends Thread
{
    int iterations;
    int failures;
    EchoTest ref;
    boolean done;

    CallThread(int iterations, EchoTest ref)
    { 
        this.failures = 0 ;
        this.iterations = iterations;
        this.ref = ref;
        done = false;
    }

    public void run()
    {
        for (int i = 0; i < iterations; ++i) {
            try {
                ref.echo("FOO");
            } catch (java.rmi.RemoteException e) {
                failures++ ;
                System.out.println("CallThread.run FAILURE !!!!!");
                e.printStackTrace(System.out);
            }
        }
        done = true;
    }
}

// End of file.
