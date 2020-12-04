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
import java.util.Hashtable;
import java.rmi.RemoteException;
import org.omg.CORBA.ORB;
import javax.naming.Context;
import javax.rmi.CORBA.Tie;
import javax.rmi.PortableRemoteObject;
import java.util.Enumeration;

public class ServantManagerImpl implements ServantManager {

    private Hashtable table = new Hashtable();
    private ORB orb = null;
    private Context context = null;

    public ServantManagerImpl() {
    }

    public ServantManagerImpl(ORB orb, Context context) {
        this.orb = orb;
        this.context = context;
    }
    
    /**
     * Start a servant in the remote process.
     * @param servantClass The class of the servant object. Must have a default constructor.
     * @param servantName The name by which this servant should be known.
     * @param publishName True if the name should be published in the name server.
     * @param nameServerHost The name server host. May be null if local host.
     * @param nameServerPort The name server port.
     * @param iiop True if iiop.
     */
    public synchronized Remote startServant(
                                            String servantClass, String servantName, boolean publishName, 
                                            String nameServerHost, int nameServerPort, 
                                            boolean iiop ) throws java.rmi.RemoteException {
        try {
            if (nameServerHost != null && nameServerHost.equals("")) {
                nameServerHost = null;
            }
            
            // Do we already have this guy?
            
            Remote result = null;
            State state = (State) table.get(servantName);
            
            if (state == null) {
      
                // No, make sure we have an orb if we need it...
                
                if (iiop) {
                    if (orb == null) {
                        Tie tie = javax.rmi.CORBA.Util.getTie(this);
                        
                        if (tie != null) {
                            orb = tie.orb();
                        } else {
                            orb = Util.createORB(nameServerHost,nameServerPort,
                                                 null );
                        }
                    }
                }
                
                // Make sure we have our name context...
                
                if (context == null) {
                    context = Util.getInitialContext(iiop,nameServerHost,nameServerPort,orb);
                }
                
                // Ok, create an instance and export it (if we need to)...
                
                Class theClass = Class.forName(servantClass);
                Remote servant = (Remote) theClass.newInstance();

                if (!(servant instanceof PortableRemoteObject)) {
                    PortableRemoteObject.exportObject(servant);
                }
                                
                // Are we supposed to publish it?
                
                if (publishName) {
                    
                    // Yes, so do it...
                    
                    context.rebind(servantName, servant);
                    
                    // Set the result with the stub
                    // returned by the lookup. This is needed
                    // to workaround a bug in the CNCtx class
                    // which causes it to ALWAYS connect the tie
                    // even if it has already been connected. By
                    // doing this lookup, we ensure that the IOR
                    // we know this guy by is correct.
                    
                    result = (Remote)context.lookup(servantName);
                    
                } else {
                
                    // No, so get it's tie and connect it to the orb...
                    
                    if (iiop) {
                        Tie tie = javax.rmi.CORBA.Util.getTie(servant);
                        tie.orb(orb);
                        result = PortableRemoteObject.toStub(servant);
                    } else {
                        result = servant;
                    }
                }
                    
                // Stash it away in our table...

                state = new State();
                state.remote = result;
                state.published = publishName;
                table.put(servantName,state);

            } else {
                
                // Just return the original one...
                    
                result = state.remote;
            }
                
            return result;
                
        } catch (Exception e) {
            RemoteException rexc = new RemoteException (e.toString(), e );
            throw rexc ;
        }
    }

    /**
     * Unexport the specified servant. If the servant was published, will be unpublised.
     */
    public synchronized void stopServant(String servantName) throws java.rmi.RemoteException {
        
        try {
            State state = (State) table.get(servantName);
            
            if (state != null) {
                
                // Unexport it...
                
                PortableRemoteObject.unexportObject(state.remote);
                
                // Was it published?
                
                if (state.published) {
                    
                    // Yes, so unpublish it...
                    
                    context.unbind(servantName);
                }
            }
        } catch (Exception e) {
            RemoteException rexc = new RemoteException (e.toString());
            rexc.initCause(e) ;
            throw rexc ;
        }
    }

    /**
     * Stop all servants in this context.
     */
    public synchronized void stopAllServants() throws java.rmi.RemoteException {
        for (Enumeration e = table.keys() ; e.hasMoreElements() ;) {
            try {
                stopServant((String) e.nextElement());
            } catch (Exception e1) {}
        }
    }

    /**
     * @Return String the String "Pong"
     */
    public String ping() throws java.rmi.RemoteException {
        return "Pong";
    }
 
    public static void main(String[] args) {
        
        String host = null;
        int port = Util.getNameServerPort();
        
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("-ORBInitialHost")) {
                host = args[++i];
            } else if (arg.equals("-ORBInitialPort")) {
                String portString = args[++i];
                port = Integer.parseInt(portString);
            }
        }
        
        if (Util.startSingleServant(ServantContext.SERVANT_MANAGER_CLASS,
                                    ServantContext.SERVANT_MANAGER_NAME,
                                    host,
                                    port,
                                    true, null ) == false) {
            System.exit(1);
        }
    }
}

class State {
    public Remote remote;
    public boolean published;
}
