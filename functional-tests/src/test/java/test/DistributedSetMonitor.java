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
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.NameClassPair;
import javax.rmi.CORBA.Tie;
import javax.rmi.PortableRemoteObject;
import org.omg.CORBA.ORB;
import java.io.DataInputStream;
import javax.rmi.PortableRemoteObject;

/**
 * DistributedSetMonitor implements the DistributedSet interface and
 * supports the addition of notifiers which enable monitoring events
 * in the set.
 *
 * @version     1.0, 5/13/98
 * @author      Bryan Atsatt
 */
public class DistributedSetMonitor  extends PortableRemoteObject
    implements DistributedSet, AlarmHandler {

    private Hashtable sets = new Hashtable();
    private Vector notifiers = new Vector();
    private String name = null;
    private String type = null;
    private String publishName = null;
    private Context context = null;
    private long autoRefreshDelay = 0;

    //_____________________________________________________________________
    // DistributedSet Methods
    //_____________________________________________________________________
 
    /*
     * See if this set is still active. Returns PING_RESPONSE.
     */
    public synchronized String ping (String fromSetName) throws RemoteException {
        int count = notifiers.size();
        for (int i = 0; i < count; i++) {
            DistributedSetNotifier notifier = 
                (DistributedSetNotifier) notifiers.elementAt(i); 
            notifier.pinged(fromSetName);
        }
        return PING_RESPONSE; 
    }
    
    /*
     * Get this set's name.
     */
    public String getName () throws RemoteException {
        return name;
    }
    
    /* 
     * Notify this set that the specified set is joining. If the set
     * already is 'known' by this instance, this call performs no
     * action.
     */
    public synchronized void join (String setName, DistributedSet set) throws RemoteException {
        
        // Do an add, and do _not_ call set.join()...
        
        add(setName,set,false);
    }


    /*
     * Notify this set that the specified set is leaving.
     */
    public synchronized void leave (String setName) throws RemoteException {
        doLeave(setName,false);
    }

    /*
     * Broadcast a message to all sets.
     */
    public synchronized void broadcastMessage (String message) throws RemoteException {
        
        // Send the message to all sets...
        
        Enumeration e = sets.elements();
        while (e.hasMoreElements()) {
            DistributedSet set = (DistributedSet) e.nextElement();
            set.receiveMessage(message,name);
        }
        
        // Now send it to ourself...
        
        //receiveMessage(message,name);
    }

    /*
     * Send a message to specified set.
     */
    public synchronized void sendMessage (DistributedSet toSet, String message) throws RemoteException {
        toSet.receiveMessage(message,name);
    }

    /*
     * Receive a message from another set. Messages are forwarded to all
     * registered notifiers.
     */
    public synchronized void receiveMessage (String message, String fromSetName) throws RemoteException {
        doReceiveMessage(message,fromSetName);
    }

    /*
     * Return the number of currently active sets, _excluding_ 
     * this instance.
     */
    public synchronized int countSets () throws RemoteException {
        return sets.size();
    }
    
    /*
     * List the names of all the active sets, _excluding_ this
     * instance.
     */
    public String[] listSetNames () throws RemoteException {
        return doListSetNames();
    }
  
    /*
     * Get a set instance by name. Returns null if not found.
     */
    public synchronized DistributedSet getSet (String setName) throws RemoteException {
        return doGetSet(setName);
    }

    //_____________________________________________________________________
    // AlarmHandler Methods
    //_____________________________________________________________________

    public void wakeup (Alarm theAlarm, long nextAlarmWakeupTime) {
        
        // Do the refresh...
        
        try {
            refresh();
        } catch (ThreadDeath death) {
            System.out.println("wakeup caught ThreadDeath!");   
            throw death;
        } catch (Error e) {}
        
        // Reschedule alarm if we need to...

        if (autoRefreshDelay > 0) {
            Alarm.scheduleWakeupFromNow(theAlarm,autoRefreshDelay);
        }
    }
        
    //_____________________________________________________________________
    // Local Methods
    //_____________________________________________________________________

    
    /*
     * Constructor.
     * @param orb The ORB to connect to.
     * @param context The naming context to use.
     * @param name The name for this instance.
     * @param type The type for all instances that should be part of this
     * set. May not contain '$' character.
     * @param autoRefreshDelay The number of seconds between refresh calls.
     * If zero, no auto-refresh will be performed.
     */
    public DistributedSetMonitor (  ORB orb,
                                    Context context,
                                    String name,
                                    String type,
                                    int autoRefreshDelay)
        throws RemoteException, NamingException {
        this.context = context;
        this.name = name;
        this.type = type;
        
        if (autoRefreshDelay > 0) {
            this.autoRefreshDelay = autoRefreshDelay * 1000;
        } else {
            this.autoRefreshDelay = 0;
        }
        
        // Set publishName...
    
        if (type.indexOf('$') >= 0) {
            throw new NamingException("type may not contain '$': " + type);
        }

        publishName = type + "$" + name;
 
        // Connect to the ORB...
        
        Tie tie = javax.rmi.CORBA.Util.getTie(this);
        tie.orb(orb);

        // Publish self...

        try {
            context.bind(publishName, tie);
        } catch (Exception e) {

            // There is already such a name registered.
            // Can we reuse it?
                
            boolean okToReuse = false;
                
            try {
                DistributedSet other = lookup(publishName);  
                if (other != null) {
                    try {
                        other.ping(this.name);
                    } catch (RemoteException ex) {
                        
                        // Nope, so we can reuse the name...
                        
                        okToReuse = true;
                    }
                } else {
                    okToReuse = true;
                }
            } catch (Exception ne) {
                okToReuse = true;
            }
                
            if (okToReuse) {
                    
                // We can reuse the name...
                    
                context.rebind(publishName, tie);
            } else {
                    
                // We cannot reuse the name...
                    
                PortableRemoteObject.unexportObject(this);
                throw new NamingException("Name is already in use: " + name);
            }
        }
        
        // Initialize our list...
        
        checkNameServer();
            
        // Are we supposed to auto-refresh?
            
        if (autoRefreshDelay > 0) {
                
            // Yep, so set an alarm...
                
            Alarm.scheduleWakeupFromNow(this,autoRefreshDelay);
        }
    }
    
    /*
     * List the names of all the active sets, _excluding_ this
     * instance.
     */
    public synchronized String[] doListSetNames () {
        int count = sets.size();
        String[] result = new String[count];
        int index = 0;
        Enumeration e = sets.keys();
        while (e.hasMoreElements()) {
            result[index++] = (String) e.nextElement();
        }
        return result;
    }
    
    /*
     * Get a set instance by name. Returns null if not found.
     */
    public synchronized DistributedSet doGetSet (String setName) {
        
        // Do we have it?
        
        DistributedSet result = (DistributedSet) sets.get(setName); 
        
        if (result == null) {
            
            // No, so see if it exists in the name server...
            
            result = lookup(type + '$' + setName);  

            if (result != null) {
                
                // It does, so add it...
                
                try {
                    add(setName,result,true);
                } catch (RemoteException re) {}
            }
        }
        
        return result;
    }

    
    /*
     * Destroy this instance. Must be called!
     */
    public void destroy () {
        
        // Turn off alarms...
        
        autoRefreshDelay = 0;
        
        // Unpublish...
        
        try {
            context.unbind(publishName);
        } catch (Exception e) {}
        
        // Tell all sets that we are leaving...
            
        Enumeration e = sets.elements();
        while (e.hasMoreElements()) {
            DistributedSet set = (DistributedSet) e.nextElement();
            
            try {
                set.leave(name);
            } catch (RemoteException e1){}
        }
    }
    
    /*
     * Refresh the list of active sets. Just checks to ensure that
     * each registered set is still alive, and removes any that are
     * not. Unfortunately, this method must perform some copying
     * and therefore creates garbage.  
     */
    public void refresh () {
        
        String setName = null;
        
        // Are all the sets we currently know about alive?
        // Since we have to call isAlive with the lock _released_, we
        // need to copy all the keys and values first(!)...
        
        String[] allNames = null;
        DistributedSet[] allSets = null;
        int count;
        
        synchronized (this) {
            count =  sets.size();
            if (count > 0) {
                allNames = new String[count];
                allSets = new DistributedSet[count];
                count = 0;
                Enumeration e = sets.keys();
                while (e.hasMoreElements()) {
                    allNames[count] = (String) e.nextElement();
                    allSets[count] = (DistributedSet) sets.get(allNames[count]);
                    count++;
                }
            }
        }

        // Now, with the lock released, iterate and call isAlive for
        // each...
        
        for (int i = 0; i < count; i++) {
            isAlive(allNames[i],null,allSets[i]);
        }
    }
        
    public void checkNameServer () {
        
        String setName = null;
        
        try {
            // Get the currently registered sets from the name server...
            
            // REMIND:  Is there a way to limit this to names of the correct
            //          type, so we don't have to see if the name starts with
            //          a known string?? Surely there must be! Looks like
            //          using InitialDirContext instead of InitialContext allows
            //          for associating attributes with objects and doing searches
            //          with them, but I don't think the CosNaming provider supports
            //          this.
            
            NamingEnumeration names = context.list("");
            
            // Are there any we don't know about?
            
            while(names.hasMore()) {
                
                NameClassPair pair = (NameClassPair) names.next();
                String publishName = pair.getName();

                // Is this our type?
                
                int index = publishName.indexOf('$');
                if (index >= 0 && publishName.startsWith(type)) {
                    
                    // Yes, extract the name...
                    
                    setName = publishName.substring(index+1);

                    // Is this one that we don't know about?
                    
                    if (!setName.equals(this.name) &&
                        !sets.containsKey(setName)) {

                        // Yes, so we must do a join...
                        
                        DistributedSet set = lookup(publishName);

                        if (set == null) {
                            try {
                                context.unbind(publishName);
                            } catch (Exception ex3) {}
                        }

                        if (set != null && isAlive(setName,publishName,set)) {
                            
                            try {
                                add(setName,set,true);
                            } catch (RemoteException re) {}
                        }
                    }
                }
            }
        } catch (NamingException e) {
            doReceiveMessage("checkNameServer caught " + e.toString(),setName);
        }
    }
    
    /*
     * Add a notifier.
     */
    public synchronized void addNotifier (DistributedSetNotifier notifier) {
        notifiers.addElement(notifier);   
    }
 
    //_____________________________________________________________________
    // This main is intended only as an test/example of using a this class.
    //_____________________________________________________________________

    /*
     * Start up a set. arg[0] == nameServerHost, arg[1] == nameServerPort
     */
    public static void main(String[] args) {
        
        boolean iiop = true;
        
        try {
            
            // Get the args...

            String name = null;
            String host = null;
            int port = 0;
      
            if (args.length == 3) {
                name = args[0];
                port = Integer.parseInt(args[1]);
                host = args[2];
                if (host.equals("null")) {
                    host = null;
                }
            } else if (args.length == 2) {
                name = args[0];
                port = Integer.parseInt(args[1]);
            } else {
                System.out.println("Usage: DistributedSetMonitor <name> <nameServerPort> [<nameServerHost>]");
                System.exit(1);
            }
            
            // Create the orb and initial context...
            
            ORB orb = Util.createORB(host,port,null);
            Context context = null;
            
            try {
                context = Util.getInitialContext(iiop,host,port,orb);
            } catch (Exception e) {
                
                if (host == null) {
                    System.out.println("Starting name server. Don't forget to kill it later!");
                        
                    try {
                        Util.startNameServer(port,iiop);
                    } catch (Exception e1) {
                        System.out.println("Failed. Caught " + e1.toString());
                        System.exit(1);
                    }
                        
                    // REMIND: We have to recreate the orb at this point! Why?
                        
                    orb = Util.createORB(host,port,null);
                    context = Util.getInitialContext(iiop,host,port,orb);
                        
                } else {
                
                    System.out.println("Could not connect to the name server. Did you forget to start it?");
                    System.exit(1);
                }
            }
            
            // Create a monitor...
            
            DistributedSetMonitor monitor = new DistributedSetMonitor(orb,context,name,"main",5);
            
            // Add our notifier...
            
            Notifier notifier = new Notifier(monitor);
            monitor.addNotifier(notifier);
            
            // Print out the 'commands'...
            
            System.out.println("\n" + name + " Ready"); 
            System.out.println("Type 'quit<rtn>' to exit or 'message<rtn>' to broadcast a message.\n"); 

            // Dump the current sets...
            
            notifier.dumpCurrent(null);
            
            // Now loop till we're done...
           
            boolean run = true;
            DataInputStream inStream = new DataInputStream(System.in);
            
            while (run) {
                
                String input = inStream.readLine();
               
                if (input.indexOf("quit") >= 0) {
                    run = false;
                    monitor.destroy();
                } else {
                    if (input.length() > 0) {
                        monitor.broadcastMessage(input);
                    }
                }
            }
            
        } catch (ThreadDeath death) {
            throw death;
        } catch (Throwable e) {
            if (e instanceof NamingException) {
                String message = e.getMessage();
                if (message.indexOf("already in use") >= 0) {
                    System.out.println(message);
                }
            } else {
                e.printStackTrace(System.out);
                System.out.println();
            }
            System.out.flush();
        }
        
        System.exit(0);
    }
        
    //_____________________________________________________________________
    // Internal Methods
    //_____________________________________________________________________
    
    private DistributedSet lookup (String publishName) {
        
        DistributedSet result = null;
        
        try {
            Object it = context.lookup(publishName);
            
            if (it != null) {
                result = (DistributedSet) PortableRemoteObject.narrow(it,DistributedSet.class);  
            }
        } catch (Exception e) {}
        
        return result;
    }
    
    
    /*
     * Must be called with lock RELEASED!
     */
    private boolean isAlive (String name, String publishName, DistributedSet set) {
        
        boolean result = true;
        
        try {
            set.ping(this.name);
        } catch (RemoteException ex) {
            
            result = false;
            
            synchronized (this) {
                
                // Dead set. First remove it from our list.
                
                doLeave(name,true);
            }
            
            // Now make sure it is removed from the name server...
            
            if (publishName == null) {
                publishName = type + '$' + name;
            }
            
            try {
                context.unbind(publishName);
            } catch (NamingException ex3) {}
        }
        
        return result;
    }

    
    private void add ( String setName,
                       DistributedSet set,
                       boolean doJoin) throws RemoteException {
        
        boolean added = false;
        
        synchronized (this) {
        
            // Do we already know about this set?
            
            if (!sets.containsKey(setName)) {
                
                // No, so add it...
            
                sets.put(setName,set);
                
                // Notify about the change...
            
                notifyChanged(true,setName,false);
                
                added = true;
            }
        }
        
        // Do a join, with the lock released, if
        // we need to...
        
        if (added && doJoin) {
            set.join(name,this);
        }
    }

    private void doLeave (String setName, boolean died) {
        sets.remove(setName);
        notifyChanged(false,setName,died);
    }

    private void doReceiveMessage (String message, String fromSetName) {
        int count = notifiers.size();
        for (int i = 0; i < count; i++) {
            DistributedSetNotifier notifier = 
                (DistributedSetNotifier) notifiers.elementAt(i); 
            notifier.messageReceived(message,fromSetName);
        }
    }

    private void notifyChanged (boolean added, String setName, boolean died) {
        int count = notifiers.size();
        for (int i = 0; i < count; i++) {
            DistributedSetNotifier notifier = 
                (DistributedSetNotifier) notifiers.elementAt(i); 
            
            if (added) {
                notifier.setAdded(setName);
            } else {
                notifier.setRemoved(setName,died);
            }
        }
    }
}

//_____________________________________________________________________
// Simple notifier for use by main.
//_____________________________________________________________________

class Notifier implements DistributedSetNotifier {
    
    DistributedSetMonitor set;
    
    Notifier(DistributedSetMonitor set) {
        this.set = set;
    }
    
    public void pinged (String fromSetName) {
        // System.out.println("\nPinged by " + fromSetName);
    }
    
    public void setAdded (String setName) {
        dumpCurrent(setName + " joined. ");
    }
    
    public void setRemoved (String setName,boolean died) {
        String reason = died ? " died! " : " left. ";
        dumpCurrent(setName + reason);
    }
    
    public void messageReceived (String message, String fromSetName) {
        System.out.println("<" + fromSetName + " said> " + message);
        System.out.flush();
    }
    
    public void dumpCurrent(String prefix) {
        
        String[] current = set.doListSetNames();
        int count = current.length;
        
        if (prefix != null) {
            System.out.print(prefix);   
        }
        
        if (count == 0) {
            System.out.print("No current members.");
        } else {
            if (count == 1) {
                System.out.print("1 current member: ");
            } else {
                System.out.print(count + " current members: ");
            }
            for (int i = 0; i < count; i++) {
                if (i > 0) {
                    System.out.print(", ");
                }
                System.out.print(current[i]);
            }
        }
        System.out.println();
        System.out.flush();
    }
}
