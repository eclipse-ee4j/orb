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

package rmic;
import javax.rmi.CORBA.Stub;

public interface LocalHello extends java.rmi.Remote {
    public String sayHello(String to) throws java.rmi.RemoteException;
    public String echoString(String it) throws java.rmi.RemoteException;
    public Object echoObject(Object it) throws java.rmi.RemoteException;
    public int identityHash(Object it) throws java.rmi.RemoteException;
    public int[] identityHash(Object a, Object b, Object c) throws java.rmi.RemoteException;
    
    public test.Hello echoHello (test.Hello in) throws java.rmi.RemoteException;
    public rmic.Hello echoHello (rmic.Hello in) throws java.rmi.RemoteException;
    public void argNamesClash(int in,
                              int _in, 
                              int out,
                              int _out,
                              int so,
                              int exCopy,
                              int copies,
                              int method,
                              int reply,
                              int ex) throws java.rmi.RemoteException;
                            
    public Base newServant() throws java.rmi.RemoteException;                           
    
    public String testPrimTypes(String arg0,
                                double arg1,
                                float arg2,
                                String arg3,
                                boolean arg4,
                                Object arg5,
                                String arg6) throws java.rmi.RemoteException;

    public Object echoString(Object value1, String str, Object value2) throws java.rmi.RemoteException;

    public Object echoArg1(int arg0, Object arg1) throws java.rmi.RemoteException;
}
