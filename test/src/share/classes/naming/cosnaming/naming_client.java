/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

// 
// naming_client.java - test for name service
// 
// 
package naming.cosnaming;

import java.lang.Thread;

import java.io.OutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.Random;

import org.omg.CORBA.*;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;


public class naming_client
{
    public static void main(String args[])
    {
        try {
            int numberOfClients = 1;
            int numberOfObjects = 5;
            int totalErrors = 0;

            String nsIOR = null;
            String nsKey = "NameService";

            NamingContext initial = null;
      
            // Get an orb object
            java.util.Properties props = System.getProperties();
      
            // Parse arguments
            for (int i=0;i<args.length;i++) {
                if (args[i].equals("-t") || args[i].equals("-threads")
                    && i<args.length-1) {
                    try {
                        numberOfClients = java.lang.Integer.parseInt(args[i+1]);
                        message("setting number of simultaneous clients to " + numberOfClients);
                    } catch (java.lang.NumberFormatException e) {
                        error_message("illegal argument " + args[i+1] + " to option: " + args[i]);
                        throw e;
                    }
                } else if (args[i].equals("-o") || args[i].equals("-objects")
                           && i<args.length-1) {
                    try {
                        numberOfObjects = java.lang.Integer.parseInt(args[i+1]);
                        message("setting number of objects per client to " + numberOfObjects);
                    } catch (java.lang.NumberFormatException e) {
                        error_message("illegal argument " + args[i+1] + " to option: " + args[i]);
                        throw e;
                    }
                } else if (args[i].equals("-i") || args[i].equals("-ior")
                           && i<args.length-1) {
                    // Use this IOR
                    nsIOR = args[i+1];
                    message("setting initial IOR to " + nsIOR);
                } else if (args[i].equals("-k") || args[i].equals("-key")
                           && i<args.length-1) {
                    // Use this Key
                    nsKey = args[i+1];
                    message("using " + nsKey + " as key for resolve_initial_references");
                } else if (args[i].equals("-ORBInitialPort") && i<args.length-1) {
                    // Stuff it into the properties
                    props.put("org.omg.CORBA.ORBInitialPort",args[i+1]);
                } else if (args[i].equals("-ORBInitialHost") && i<args.length-1) {
                    // Stuff it into the properties
                    props.put("org.omg.CORBA.ORBInitialHost",args[i+1]);
                }

            }
      
            org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args,props);
      
            // Get the initial list of services
            String[] list = orb.list_initial_services();

            // Print list out
            if (list != null) {
                message("orb.list_initial_services() returned:");
                String s = new String();
                for (int i=0;i<list.length;i++) {
                    s = s + list[i] + ", ";
                }
                message(s);
            } else {
                error_message("orb.list_initial_services() returned nothing!");
                totalErrors++;
            }

            // Resolve each entry in the list
            for (int i=0;i<list.length;i++) {
                try {
                    if (list[i].equals("NamingService")) continue;

                    org.omg.CORBA.Object obj = orb.resolve_initial_references(list[i]);
                    if (obj != null) {
                        message("resolve(" + list[i] + ") returns non-null");
                    } else {
                        error_message("resolve(" + list[i] + ") returns nothing!");
                        totalErrors++;
                    }
                } catch (org.omg.CORBA.ORBPackage.InvalidName e) {
                    error_message("resolve(" + list[i] + ") throws: " + e);
                    throw e;
                } catch (org.omg.CORBA.SystemException e) {
                    error_message("resolve(" + list[i] + ") throws: " + e);
                    throw e;
                }
            }

            // Get the name service initial object reference
            message("getting initial naming context");
        
            // Pick the initial naming context to start with
            if (nsIOR != null && nsIOR.length() > 0) {
                // Specified IOR
                message("trying to destringify and narrow from " + nsIOR);
                org.omg.CORBA.Object obj = orb.string_to_object(nsIOR);
                try {
                    initial = NamingContextHelper.narrow(obj);
                } catch (org.omg.CORBA.BAD_PARAM e) {
                    error_message("Could not destringify initial IOR and narrow to CosNaming::NamingContext!");
                    totalErrors++;
                    throw e;
                }
            } else if (nsKey != null && nsKey.length() > 0) {
                // Specified key
                message("trying to resolve " + nsKey);
                org.omg.CORBA.Object obj = null;
                try {
                    obj = orb.resolve_initial_references(nsKey);
                } catch (org.omg.CORBA.ORBPackage.InvalidName e) {
                    error_message("Could not resolve_initial_references on " + nsKey +
                                  ": got InvalidName exception");
                    e.printStackTrace();
                    throw e;
                }
                message("narrowing " + nsKey);
                try {
                    initial = NamingContextHelper.narrow(obj);
                } catch (org.omg.CORBA.BAD_PARAM e) {
                    error_message("Could not narrow to CosNaming::NamingContext from key " + nsKey + "!");
                    totalErrors++;
                    throw e;
                }
            } else {
                error_message("Cannot obtain initial naming context, no IOR or key available!");
                totalErrors++;
                initial = null;
                throw new Exception("Cannot obtain initial naming context, no IOR or key available!");
            }
      
            message("got initial naming context: " + initial);
          
            // Start clients
            if (numberOfClients < 2) {
                message("running single client synchronously with " + numberOfObjects + " objects");
                // create test
                NamingTester test = new NamingTester(orb,0,numberOfObjects,initial);
                // run it
                test.run();
                // collect errors
                totalErrors += test.getErrors();
            } else {
                // Create a number of threads
                message("creating " + numberOfClients + " individual test-threads with " + numberOfObjects + "objects");
                Thread[] test_threads = new Thread[numberOfClients];
                NamingTester[] tests = new NamingTester[numberOfClients];
                for (int i=0;i<numberOfClients;i++) {
                    tests[i] = new NamingTester(orb,i,numberOfObjects,initial);
                    test_threads[i] = new Thread(tests[i]);
                }
                // Start them
                message("starting " + numberOfClients + " test-threads");
                for (int i=0;i<numberOfClients;i++)
                    test_threads[i].start();
                // Wait for them to finish
                message("joining " + numberOfClients + " test-threads");
                for (int i=0;i<numberOfClients;i++) {
                    try {
                        test_threads[i].join();
                    } catch (InterruptedException ex) {
                        error_message("was interrupted while joining test thread " + i);
                        throw ex;
                    }
                }
        
                // Collect errors
                for (int i=0;i<numberOfClients;i++)
                    totalErrors += tests[i].getErrors();
            }

            message("total errors = " + totalErrors);

            if (totalErrors > 0)
                System.exit (1);

        } catch (Throwable ex) { 
            System.err.println("Caught exception: " + ex);
            ex.printStackTrace();
            System.exit (1);
        }
    }
  
    public static void message(String msg) {
        System.out.println("naming_client: " + msg);
    }
    public static void error_message(String msg) {
        System.err.println("naming_client: error: " + msg);
    }
  
}

final class NamingTester implements Runnable
{
    private NamingContext theRoot;
    private NamingContext playground;
    private int errors;
    private int me;
    private int numberOfObjects;
    private org.omg.CORBA.ORB theORB;
  
    NamingTester(org.omg.CORBA.ORB orb,int id,int numObjs,NamingContext initNC)
    {
        me = id;
        numberOfObjects = numObjs;
        theORB = orb;
        theRoot = initNC;
    }
  
    public void run()
    {
        Random random = new Random();
    
        try {
            // Create a playground name
            message("creating a playground");
            NameComponent[] playgroundName = new NameComponent[1];
            playgroundName[0] = new NameComponent();
            playgroundName[0].id = "(" + Thread.currentThread().getName() + ")";
            playgroundName[0].kind = "playground";

            boolean Unique = false;
            boolean done = false;
            while (!(Unique || done)) {
                try {
                    if (me % 2 == 0) {
                        // Create and name it directly
                        message("bind_new_context(): " + nameToString(playgroundName));
                        playground = theRoot.bind_new_context(playgroundName);
                    } else {
                        // Create first, then name
                        message("new_context(); bind_context(): " +
                                nameToString(playgroundName));
                        playground = theRoot.new_context();
                        theRoot.bind_context(playgroundName,playground);
                    }
                    // If we get here we got it bound
                    Unique = true;
                } catch (org.omg.CosNaming.NamingContextPackage.InvalidName ex) {
                    error_message("caught invalid name exception: should not happen!");
                    errors++;
                    done = true;
                } catch (org.omg.CosNaming.NamingContextPackage.AlreadyBound ex) {
                    error_message("caught already bound exception for: " +
                                  nameToString(playgroundName) + ": should not happen!");

                    errors++;
                    theRoot.unbind(playgroundName);
                    message("Playground " + nameToString(playgroundName) +
                            "seems to be occupied, attempting to create a different name");
                    // Pick a different name: append a random int to the name
                    int ri = -1737356518; // random.nextInt();
                    playgroundName[0].id = playgroundName[0].id + "[" + ri + "]";
                }
            }
      
            if (playground == null) {
                errors++;
                error_message("haven't got a playground, exiting!");
                return;
            }

            // Verify that it is there
            {
                message("resolving playground");
                org.omg.CORBA.Object obj = theRoot.resolve(playgroundName);
                if (obj == null) {
                    errors++;
                    error_message("cannot resolve " + nameToString(playgroundName) +
                                  " to playground.");
                } else {
                    try {
                        NamingContext nc = NamingContextHelper.narrow(obj);
                        message("resolve(" + nameToString(playgroundName) +
                                ") -> " + nc);
                    } catch (org.omg.CORBA.SystemException ex) {
                        errors++;
                        error_message("cannot narrow resolved playground to a naming context!");
                    }
                }
            }

            // Bind again
            try {
                theRoot.bind_context(playgroundName,playground);
                errors++;
                error_message("bind_context() second time should throw alreadybound exception but doesn't!");
            } catch (org.omg.CosNaming.NamingContextPackage.AlreadyBound ex) {
                message("bind_context() second time throws exception, which is fine.");
            }

            // Rebind
            try {
                theRoot.rebind_context(playgroundName,playground);
                message("rebind_context() second time works.");
            } catch (org.omg.CosNaming.NamingContextPackage.NotFound ex) {
                errors++;
                error_message("rebind_context() second time throws exception, but it shouldn't!");
            }

            // Now we are ready to create objects
            {
                // Create a holder for the object references
                org.omg.CORBA.Object[] objects = new org.omg.CORBA.Object[numberOfObjects];
                // Create an array of names, i.e. 2D array of namecomponents
                NameComponent[][] names = new NameComponent[numberOfObjects][1];

                message("creating names and object references for " + numberOfObjects +
                        " objects");
        
                // Fill out objectrefs and their names
                for (int i=0;i<numberOfObjects;i++) {
                    // Set object to playground object ref
                    objects[i] = playground;
                    // Create a name for each object
                    names[i][0] = new NameComponent();
                    names[i][0].id = "(" + me + ":" + i + ")";
                    names[i][0].kind = "(" + Thread.currentThread().getName() + ")";
                }
                // Create an name guaranteed to be invalid
                NameComponent[] invalid_name = new NameComponent[1];
                invalid_name[0] = new NameComponent();
                invalid_name[0].id = "(" + me + ":" + (numberOfObjects+1) + ")";
                invalid_name[0].kind = "(" + Thread.currentThread().getName() + ")";    

                // Bind all objects under the names
                message("bind() " + numberOfObjects + " objects");
                for (int i=0;i<numberOfObjects;i++) {
                    try {
                        playground.bind(names[i],objects[i]);
                    } catch (org.omg.CosNaming.NamingContextPackage.AlreadyBound ex) {
                        error_message("bind() caught already bound exception for " +
                                      nameToString(names[i]) + "!");
                    }
                }
        
                // Bind all objects under the names a second time (throws ex)
                message("bind() " + numberOfObjects + " objects a second time");
                for (int i=0;i<numberOfObjects;i++) {
                    try {
                        playground.bind(names[i],objects[i]);
                        errors++;
                        error_message("2nd bind() didn't throw already bound exception for " +
                                      nameToString(names[i]) + "!");
                    } catch (org.omg.CosNaming.NamingContextPackage.AlreadyBound ex) {
                        //message("2nd bind() throws already bound exception, that's fine");
                    }
                }
        
                // Rebind all objects under the names
                message("rebind() " + numberOfObjects + " objects");
                for (int i=0;i<numberOfObjects;i++) {
                    try {
                        playground.rebind(names[i],objects[i]);
                    } catch (org.omg.CosNaming.NamingContextPackage.NotFound ex) {
                        errors++;
                        error_message("rebind() caught not found exception for " +
                                      nameToString(names[i]));
                    }
                }

                // Now resolve a lot of objects
                org.omg.CORBA.Object obj = null;
                int index = 0;
                NameComponent[] fullObjectPath = new NameComponent[2];
                fullObjectPath[0] = playgroundName[0];
                fullObjectPath[1] = null;

                message("resolve() " + numberOfObjects + " objects 5 times each, from playground and initial");
                for (int i=0;i<numberOfObjects*5;i++) {
                    index = random.nextInt();
                    index %= numberOfObjects;
                    if (index < 0)
                        index += numberOfObjects;
                    try {
                        // Try resolve
                        obj = playground.resolve(names[index]);

                        if (obj == null) {
                            errors++;
                            error_message("playground.resolve() #" + i + " for " +
                                          nameToString(names[index]) +
                                          " returns null objref!");
                        }
                    } catch (org.omg.CosNaming.NamingContextPackage.NotFound ex) {
                        errors++;
                        error_message("playground.resolve() #" + i + " for " +
                                      nameToString(names[index]) +
                                      " throw exception!");
                    }
                    try {
                        // Try resolve from initial
                        fullObjectPath[1] = names[index][0];
                        obj = theRoot.resolve(fullObjectPath);
                        if (obj == null) {
                            errors++;
                            error_message("theRoot.resolve() #" + i + " for " +
                                          nameToString(fullObjectPath) +
                                          " returns null objref!");
                        }
                    } catch (org.omg.CosNaming.NamingContextPackage.NotFound ex) {
                        errors++;
                        error_message("theRoot.resolve() #" + i + " for " +
                                      nameToString(fullObjectPath) +
                                      " throw exception!");
                    }
                }

                message("resolving() an illegal name: " + nameToString(invalid_name));
                try {
                    playground.resolve(invalid_name);
                    errors++;
                    error_message("playground.resolve(" + nameToString(invalid_name) +
                                  ") succeded, which it shouldn't!");
                } catch (org.omg.CosNaming.NamingContextPackage.NotFound ex) {
                    message("playground.resolve(" + nameToString(invalid_name) +
                            ") throws NotFound, which it should.");
                }
        
                // List the names
                boolean[] seen = new boolean[numberOfObjects];
                boolean more = false;
                int askFor = 5;
                int totalSeen = 0;
                int idx1,idx2;

                message("list() in playground to find " + numberOfObjects + " objects");
                BindingListHolder blh = new BindingListHolder();
                BindingIteratorHolder bih = new BindingIteratorHolder();
                playground.list(askFor,blh,bih);
                do {
                    for (int i=0;i<blh.value.length;i++) {
                        totalSeen++;
                        // name id format: { int:int } (second int is our index)
                        String s = blh.value[i].binding_name[0].id;
                        idx1 = s.lastIndexOf(':') + 1;
                        idx2 = s.lastIndexOf(')');
                        index = java.lang.Integer.parseInt(s.substring(idx1,idx2));
                        seen[index] = true;
                    }
                    if (bih.value != null)
                        more = bih.value.next_n(askFor,blh);
                } while (more);
                // Verify that all were seen
                if (totalSeen < numberOfObjects) {
                    errors++;
                    error_message("list() only reports " + totalSeen +
                                  ", not " + numberOfObjects + "!");
                }
                for (int i=0;i<numberOfObjects;i++) {
                    if (!seen[i]) {
                        errors++;
                        error_message("list() did not find object #" + i +"!");
                    }
                }
                // Remove iterator
                if (bih.value != null)
                    bih.value.destroy();
        
                // Unbind all names
                message("unbind() " + numberOfObjects + " objects");
                for (int i=0;i<numberOfObjects;i++) {
                    try {
                        playground.unbind(names[i]);
                    } catch (org.omg.CosNaming.NamingContextPackage.NotFound ex) {
                        errors++;
                        error_message("unbind() caught not found exception " +
                                      nameToString(names[i]));
                    }
                }
            }

            // Clean up
            try {
                message("cleaning up by unbinding playground: " +
                        nameToString(playgroundName));
                theRoot.unbind(playgroundName);
            } catch (org.omg.CosNaming.NamingContextPackage.NotFound ex) {
                errors++;
                error_message("playground not found while cleaning up: " + ex);
            }
      
            try {
                message("destroying playground");
                playground.destroy();
            } catch (org.omg.CosNaming.NamingContextPackage.NotEmpty ex) {
                errors++;
                error_message("playground was not empty while attempting to destroy!");
            }

            message("cleaning and removing playground complete.");

        } catch (org.omg.CORBA.UserException ex) {
            errors++;
            error_message("caught user exception:");
            ex.printStackTrace();
        } catch (org.omg.CORBA.SystemException ex) {
            errors++;
            error_message("caught system exception:");
            ex.printStackTrace();
        } catch (Throwable t) {
            errors++;
            t.printStackTrace();
        }
    }

    public static String nameToString(NameComponent[] name)
    {
        String s = new String("{");
        if (name != null || name.length > 0) {
            for (int i=0;i<name.length;i++) {
                if (i>0)
                    s = s + ",";
                s = s +  "[" + name[i].id + "," + name[i].kind + "]";
            }
        }
        s = s + "}";
        return s;
    }
  
    public int getErrors() {
        return errors;
    }

    public void message(String msg) {
        System.out.println("NamingTester[" + Thread.currentThread().getName() + "]: " + msg);
    }
    public void error_message(String msg) {
        System.err.println("NamingTester[" + Thread.currentThread().getName() + "]: Error: " + msg);
        errors++;
    }
}
