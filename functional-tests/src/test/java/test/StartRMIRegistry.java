/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package test;

import sun.rmi.registry.RegistryImpl;
import java.rmi.registry.Registry;
import java.rmi.RMISecurityManager;

public class StartRMIRegistry {

    private static Registry registry = null;
    
    /**
     * Main program to start a registry. <br>
     * The port number can be specified on the command line.
     */
    public static void main(String args[]) {
        // Create and install the security manager
        System.setSecurityManager(new RMISecurityManager());

        try {
            int port = Registry.REGISTRY_PORT;
            if (args.length >= 1) {
                port = Integer.parseInt(args[0]);
            }
            registry = new RegistryImpl(port);
            System.out.println(Util.HANDSHAKE);


            // prevent registry from exiting
            while (true) {
                try {
                    // The following timeout is used because a bug in the
                    // native C code for Thread.sleep() cause it to return
                    // immediately for any higher value.
                    Thread.sleep(Integer.MAX_VALUE - 1);
                } catch (InterruptedException e) {
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Port is not a number.");
        } catch (Exception e) {
            System.out.println("RegistryImpl.main: an exception occurred: " +
                               e.getMessage());
            e.printStackTrace();
        }
        System.exit(1);
    }
}
