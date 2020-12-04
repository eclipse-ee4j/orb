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

package corba.cdrext;

import org.omg.CORBA.*;
import org.omg.CORBA.portable.*;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.PortableServer.*;

import java.rmi.*;
import javax.rmi.PortableRemoteObject;
import javax.naming.*;
import java.io.*;
import java.util.*;

public class Server extends PortableRemoteObject implements Tester
{
    public Server() throws java.rmi.RemoteException {}

    public MarshalTester verify(byte[] predata, 
                                MarshalTester input, 
                                byte[] postdata)
        throws DataCorruptedException
    {
        if (predata == null) {
            System.out.println("predata is null");
            throw new DataCorruptedException("predata is null");
        }
        if (postdata == null) {
            System.out.println("postdata is null");
            throw new DataCorruptedException("postdata is null");
        }
        if (!Arrays.equals(predata, postdata)) {
            System.out.println("byte arrays not equal");
            throw new DataCorruptedException("Byte arrays not equal");
        }

        return input;
    }

    public java.lang.Object verify(java.lang.Object obj) {
        return obj;
    }

    public Map verify(Map map) {
        return map;
    }

    public List verify(List list) {
        return list;
    }

    public java.sql.Date verify(java.sql.Date date) {
        return date;
    }

    public Properties verify(Properties props) {
        return props;
    }

    public Hashtable verify(Hashtable table) {
        return table;
    }

    public void throwCheckedException() throws CheckedException {
        throw new CheckedException("CheckedException");
    }

    public void throwRuntimeException() {
        throw new UncheckedException("Runtime Exception");
    }

    public void throwRemoteException() throws RemoteException {
        throw new RemoteException("This is a remote exception");
    }

    public AbsTester getAbsTester() {
        return this;
    }

    public void ping() {}

    public static void main(String[] args) {
        try {

            ORB orb = ORB.init(args, System.getProperties());
      
            // Get rootPOA
            POA rootPOA = (POA)orb.resolve_initial_references("RootPOA");
            rootPOA.the_POAManager().activate();

            Server impl = new Server();
            javax.rmi.CORBA.Tie tie = javax.rmi.CORBA.Util.getTie( impl ) ; 

            byte[] id = rootPOA.activate_object( 
                                                 (org.omg.PortableServer.Servant)tie ) ;
            org.omg.CORBA.Object obj = rootPOA.id_to_reference( id ) ;

            // get the root naming context
            org.omg.CORBA.Object objRef = 
                orb.resolve_initial_references("NameService");
            NamingContext ncRef = NamingContextHelper.narrow(objRef);
      
            // bind the Object Reference in Naming
            NameComponent nc = new NameComponent("Tester", "");
            NameComponent path[] = {nc};
            
            ncRef.rebind(path, obj);
            
            // Emit the handshake the test framework expects
            // (can be changed in Options by the running test)
            System.out.println ("Server is ready.");

            // Wait for clients
            orb.run();

//             Context rootContext = new InitialContext();
//             Server p = new Server();
//             rootContext.rebind("Tester", p);
//             System.out.println("Server is ready.");
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

}

