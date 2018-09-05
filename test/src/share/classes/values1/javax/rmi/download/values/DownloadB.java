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

package javax.rmi.download.values;
//import com.sun.corba.ee.impl.util.JDKClassLoader;

public class DownloadB implements java.io.Serializable {
    public class Inner {}
    public class Nested {}
    
    public String toString() {
        String exMsg = "";
        
        // First, make sure that using Class.forName() fails to load
        // our nested class...
        
        try {
            Class.forName("javax.rmi.download.values.DownloadB$Nested");
            
            // Succeeded, but should not have...
            
            return "DownloadB.toString(): loaded Nested when calling Class.forName()!"; 
        } catch (Exception e) {}
        
        // Now make sure that using JDKClassLoader.loadClass() succeeds to
        // load our nested class...
        
        try {
            //JDKClassLoader.loadClass(null,"javax.rmi.download.values.DownloadB$Nested",true);
            
            // Success...
            
            return "Loaded DownloadB.Nested";
            
        } catch (Exception e) {
            exMsg = e.toString();
        }

        // Failed to load...
        
        return "DownLoadB.toString() failed to load DownloadB.Nested. Caught: "+exMsg;
    }
}
