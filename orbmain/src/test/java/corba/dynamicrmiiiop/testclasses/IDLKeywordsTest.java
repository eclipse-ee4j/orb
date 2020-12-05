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

package corba.dynamicrmiiiop.testclasses;

public class IDLKeywordsTest {

    //
    // Set of idl names corresponding to alphabetically sorted set of
    // interface methods.  See TestIDLNameTranslator for sorting details.    
    //    
    // List doesn't contain the following
    // IDL Keywords : boolean, case, char, const, default, double, float,
    //                interface, long, Object, short, switch, void, num
    //                since they are also Java keywords
    //
    static final String[] IDL_NAMES = {   
        "_Any", "_EXCEPTION", "_RaiseS", "_TRUE",    "_attribute", "_context", 
        "_falsE", "_in", "_inout", "_module", "_octet",
        "_oneway", "_out",  "_readonly", "_sequence",
        "_string", "_struct", "_typedef", "_union", "_unsigNED"
    };
    
    public static String[] getIDLNames() {
        return IDL_NAMES;
    }

    public interface IDLKeywords extends java.rmi.Remote {

        // Comparison to IDL keywords is case-insensitive.
        
        void Any() throws java.rmi.RemoteException; 
        void EXCEPTION() throws java.rmi.RemoteException; 
        void RaiseS() throws java.rmi.RemoteException; 
        void TRUE() throws java.rmi.RemoteException;

        void attribute() throws java.rmi.RemoteException; 
        void context() throws java.rmi.RemoteException; 
        void falsE() throws java.rmi.RemoteException; 

        void in() throws java.rmi.RemoteException; 
        void inout() throws java.rmi.RemoteException;
        void module() throws java.rmi.RemoteException; 
        void octet() throws java.rmi.RemoteException;
        void oneway() throws java.rmi.RemoteException; 
        void out() throws java.rmi.RemoteException; 

        void readonly() throws java.rmi.RemoteException; 
        void sequence() throws java.rmi.RemoteException;
        void string() throws java.rmi.RemoteException; 
        void struct() throws java.rmi.RemoteException; 
        void typedef() throws java.rmi.RemoteException; 
        void union() throws java.rmi.RemoteException; 
        void unsigNED() throws java.rmi.RemoteException; 
       
    }

}
