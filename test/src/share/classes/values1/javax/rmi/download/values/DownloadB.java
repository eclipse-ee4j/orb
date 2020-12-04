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
