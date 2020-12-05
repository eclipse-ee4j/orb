/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
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

package test;

import java.rmi.Remote;
import java.rmi.RemoteException;
import test.ServantContext;
import test.RemoteTest;
import javax.rmi.PortableRemoteObject;

// NOTE: This is a template for subclasses of RemoteTest. Copy it, then:
//
//  1. Change the class name/package.
//  2. Change the static Strings as needed for your class.
//  3. Replace 'test.Hello' in the 2nd to last line with the name of your remote interface.
//  4. Replace last line with your test code.

/*
 * @test
 */
public class RemoteTestExample extends RemoteTest {

    private static final String publishName     = "HelloServer";
    private static final String servantClass    = "test.HelloServant";
    private static final String[] compileEm     = {servantClass};
   
    /**
     * Return an array of fully qualified remote servant class
     * names for which ties/skels need to be generated. Return
     * empty array if none.
     */
    protected String[] getRemoteServantClasses () {
        return compileEm;  
    }

    /**
     * Append additional (i.e. after -iiop and before classes) rmic arguments
     * to 'currentArgs'. This implementation will set the output directory if
     * the OUTPUT_DIRECTORY flag was passed on the command line.
     */
    protected String[] getAdditionalRMICArgs (String[] currentArgs) {
        if (iiop) {
            String[] ourArgs = {"-alwaysGenerate"};
            return super.getAdditionalRMICArgs(ourArgs);
        } else {
            return super.getAdditionalRMICArgs(currentArgs);
        }
    }

    /**
     * Perform the test.
     * @param context The context returned by getServantContext().
     */
    public void doTest (ServantContext context) throws Throwable {

        // Start up our servant. (The 'iiop' flag is set to true by RemoteTest
        // unless the -jrmp flag was used).

        Remote remote = context.startServant(servantClass,publishName,true,iiop);

        if (remote == null) {
            throw new Exception ("Could not start servant: " + servantClass);
        }

        // Narrow to our expected interface...

        test.Hello objref = (test.Hello) PortableRemoteObject.narrow(remote,test.Hello.class);

        // TEST CODE HERE...

        System.out.println(objref.sayHello("RemoteTestExample"));
    }
}
