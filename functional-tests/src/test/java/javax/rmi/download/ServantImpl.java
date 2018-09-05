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
