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

package corba.stubserialization;

import java.rmi.RemoteException;

import javax.rmi.PortableRemoteObject; 
import java.io.FileInputStream ;
import java.io.ObjectInputStream ;

import com.sun.corba.ee.spi.presentation.rmi.StubAdapter ;
import com.sun.corba.ee.spi.orb.ORB ;

public class HelloServant extends PortableRemoteObject implements Hello
{
    ORB orb ;

    public HelloServant( ORB orb ) throws RemoteException 
    {
        super();
        this.orb = orb ;
    }
        
    public String sayHello( ) throws RemoteException
    {
        return Constants.HELLO;
    }

    public String sayHelloToStub( String fileName ) throws RemoteException 
    {
       FileInputStream fis = null ;
       ObjectInputStream ois = null ;

       try {
           System.out.println(
               "Deserializing the Stub from a FileStream: Start");
           fis = new FileInputStream( Client.getFile( fileName ) ) ;
           ois = new ObjectInputStream(fis);
           Object obj = ois.readObject(); 
           StubAdapter.connect( obj, orb ) ;
           System.out.println(
               "Deserializing the Stub from a FileStream: Complete");
           Echo echo = (Echo) obj;
           System.out.println( 
               "Invoking after Serialization and Deserialization" );
           String msg = echo.echo( Constants.HELLO ) ;
           System.out.println( 
               "Invoking after Serialization and Deserialization Complete" );
           return msg ; 
        } catch (Exception exc) {
            throw new RemoteException( "Error in sayHelloToStub", exc ) ;
        } finally {
            try {
                if (ois != null)
                    ois.close() ;
                if (fis != null)
                    fis.close() ;
            } catch (Exception exc) {
                // Nothing to do if close throws an IOException.
            }
        }

    }

    /*
    public TestAppReturnValue getTARV() throws RemoteException 
    {
        return new TestAppReturnValue() ;
    }
    */
}
