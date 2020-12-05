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

package javax.rmi.download;

public class ServantImpl implements Servant {

    public String EchoSingleRemoteInterface() throws java.rmi.RemoteException{
        return "EchoSingleRemoteInterface";
    }

    public TheValue getValue() throws java.rmi.RemoteException{
        try{
            String url = System.getProperty("java.rmi.server.codebase");
            return (TheValue)java.rmi.server.RMIClassLoader.loadClass(new java.net.URL(url),"javax.rmi.download.values.TheValueImpl").newInstance();
        }
        catch(Throwable t){
            java.io.CharArrayWriter cw = new java.io.CharArrayWriter();
            t.printStackTrace(new java.io.PrintWriter(cw));

            return new TheErrorValue(cw.toString());
        }
    }

    public void forceNotSerializableException(java.io.Serializable val) throws java.rmi.RemoteException {
        // no op
    }

    public java.io.Serializable forceNotSerializableException() throws java.rmi.RemoteException {
        return new ContainerOfBadVal();
    }

    public void throwRemoteExceptionWithNonSerializableValue() throws java.rmi.RemoteException {
        throw new TheBadException();
    }

    public String passClass(Class clz) throws java.rmi.RemoteException {
        return "Name:"+clz.getName();
    }   

    public java.lang.Object testWriteReadObject(java.lang.Object o) 
        throws java.rmi.RemoteException {
        return o;
    }

}
