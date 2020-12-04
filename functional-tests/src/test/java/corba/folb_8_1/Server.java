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
// Created       : 2002 Jul 19 (Fri) 14:48:59 by Harold Carr.
// Last Modified : 2005 Jul 19 (Tue) 12:24:38 by Harold Carr.
//

package corba.folb_8_1;


import java.util.Properties ;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;

import com.sun.corba.ee.spi.misc.ORBConstants;

import corba.hcks.U;

/**
 * @author Harold Carr
 */
public class Server
{
    public static final String baseMsg = Common.class.getName();

    public static ORB orb;

    // So it can be accessed later.
    public static org.omg.CORBA.Object ref;

    public static void setProperties(Properties props, int[] socketPorts)
    {
        //
        // Debugging flags.  Generally commented out.
        //
        /*
        props.setProperty(ORBConstants.DEBUG_PROPERTY,
                          "giop,transport,subcontract");
        */

        //
        // Tell the ORB to listen on user-defined port types.
        //

        String listenPorts = formatListenPorts();
        props.setProperty(ORBConstants.LISTEN_SOCKET_PROPERTY, listenPorts);
        System.out.println(listenPorts);

        //
        // Register the socket factory that knows how to create
        // Sockets of type X Y and Z.
        //

        props.setProperty(ORBConstants.SOCKET_FACTORY_CLASS_PROPERTY,
                          SocketFactoryImpl.class.getName());

        //
        // Register and IORInterceptor that will put port 
        // type/address info into IORs.
        // E.G.: X/<hostanme>:*, Y/<hostname>:4444, Z/<hostname>:5555
        //

        props.setProperty("org.omg.PortableInterceptor.ORBInitializerClass." + IORInterceptorImpl.class.getName(),
                          "dummy");
    }

    public static String formatListenPorts()
    {
        String result = "";
        for (int i = 0; i < Common.socketTypes.length; i++) {
            result += Common.socketTypes[i] 
                + ":" 
                + Integer.toString(Common.socketPorts[i]);
            if (i + 1 < Common.socketTypes.length) {
                result += ",";
            }
        }
        return result;
    }
  
    public static void main(String av[])
    {
        try {
            if (! ColocatedCS.isColocated) {
                Properties props = System.getProperties();
                setProperties(props, Common.socketPorts);
                orb = ORB.init(av, props);
            }

            POA poa = Common.createPOA("child", false, orb);
            ref = Common.createAndBind(Common.serverName1, orb, poa);
            Common.createAndBind(Common.serverName2, orb, poa);
      
            System.out.println ("Server is ready.");

            synchronized (ColocatedCS.signal) {
                ColocatedCS.signal.notifyAll();
            }
            
            orb.run();
            
        } catch (Throwable t) {
            System.out.println(baseMsg + t);
            t.printStackTrace(System.out);
            System.exit(1);
        }
    }

}

// This class is to ensure that we do NOT store a contact info to
// a different object and try to send an invocation from a client to
// an incorrect Tie.
class I2Servant extends I2POA
{
    private com.sun.corba.ee.spi.orb.ORB orb;

    public I2Servant(ORB orb)
    {
        this.orb = (com.sun.corba.ee.spi.orb.ORB) orb;
    }

    public int m(String x)
    {
        int result = new Integer(x).intValue();
        System.out.println("I2Servant.m result: " + result);
        System.out.flush();
        return result;
    }
  
    public org.omg.CORBA.Object n(String x)
    {
        return Server.ref;
    }

    public int foo(int x)
    {
        return x;
    }
}

class IServant extends IPOA
{
    private com.sun.corba.ee.spi.orb.ORB orb;

    public IServant(ORB orb)
    {
        this.orb = (com.sun.corba.ee.spi.orb.ORB) orb;
    }

    public String m(String x)
    {
        return "IServant echoes: " + x;
    }

    public int n(String x)
    {
        return 101;
    }

    public int throwRuntimeException(int x)
    {
        return 1/x;
    }

    public boolean unregister(String socketType)
    {
        return U.unregisterAcceptorAndCloseConnections(socketType, orb);
    }

    public boolean register(String socketType)
    {
        return U.registerAcceptor(socketType, 
                          ((Integer) Common.socketTypeToPort.get(socketType))
                              .intValue(),
                          orb);
    }
}

// End of file.
