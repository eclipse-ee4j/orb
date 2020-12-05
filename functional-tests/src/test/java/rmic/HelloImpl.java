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

import org.omg.CORBA.ORB;
import javax.rmi.CORBA.Tie;
import javax.rmi.PortableRemoteObject;
import javax.rmi.CORBA.Util;
import org.omg.CORBA.NO_PERMISSION;
import org.omg.CORBA.CompletionStatus;
import java.io.Serializable;
import java.rmi.server.RMIClassLoader;

public class HelloImpl //extends javax.rmi.PortableRemoteObject
    implements Hello {

    ObjectByValue obv = null;
    AbstractObject[] remotes = null;

    public HelloImpl() throws java.rmi.RemoteException {
    }

    public void initRemotes() throws java.rmi.RemoteException {
        if (remotes == null) {
            remotes = new AbstractObject[3];
            remotes[0] = new RemoteObjectServer(0);
            remotes[1] = new RemoteObjectServer(1);
            remotes[2] = new RemoteObjectServer(2);
        }
    }

    public String getCodeBase() throws java.rmi.RemoteException {
        return RMIClassLoader.getClassAnnotation(getClass());  
    }
    
    public void publishRemoteObject(String name) throws java.rmi.RemoteException {
        try {
            test.Util.singleServantContext.rebind(name,new RemoteObjectServer(192));
        } catch (javax.naming.NamingException e) {
            throw new java.rmi.RemoteException("publishRemoteException caught: "+e); 
        }
    }
        
    public String sayHello() throws java.rmi.RemoteException {
        return "hello";
    }

    public int sum(int value1, int value2) throws java.rmi.RemoteException {
        return value1 + value2;
    }

    public String concatenate(String str1, String str2) throws java.rmi.RemoteException {
        return str1+str2;
    }

    public String checkOBV(ObjectByValue obv) throws java.rmi.RemoteException {
        this.obv = obv;

        return "The Results are: "  +
            (obv.getValue1() + obv.getValue2()) +
            obv.getString1()    +
            obv.getString2();
    }

    public ObjectByValue getOBV() throws java.rmi.RemoteException {
        return obv;
    }

    public Hello getHello () throws java.rmi.RemoteException {
        return (Hello) PortableRemoteObject.toStub(this);
    }

    public int[] echoArray (int[] array) throws java.rmi.RemoteException {
        return array;
    }

    public long[][] echoArray (long[][] array) throws java.rmi.RemoteException {
        return array;
    }

    public short[][][] echoArray (short[][][] array) throws java.rmi.RemoteException {
        return array;
    }

    public ObjectByValue[] echoArray (ObjectByValue[] array) throws java.rmi.RemoteException {
        return array;
    }

    public ObjectByValue[][] echoArray (ObjectByValue[][] array) throws java.rmi.RemoteException {
        return array;
    }

    public AbstractObject echoAbstract (AbstractObject absObj) throws java.rmi.RemoteException {
        return absObj;
    }

    public AbstractObject[] getRemoteAbstract() throws java.rmi.RemoteException {
        initRemotes();
        return remotes;
    }


    public void shutDown () throws java.rmi.RemoteException {
        System.exit(0);
    }

    public void throwHello (int count, String message) throws java.rmi.RemoteException, HelloException {
        throw new HelloException(count,message);
    }

    public void throw_NO_PERMISSION (String s, int minor) throws java.rmi.RemoteException {
        throw new NO_PERMISSION(s,minor,CompletionStatus.COMPLETED_YES);
    }

    public CharValue echoCharValue (CharValue value) throws java.rmi.RemoteException {
        return value;
    }

    public Object echoObject (Object it) throws java.rmi.RemoteException {
        return it;
    }

    public Serializable echoSerializable (Serializable it) throws java.rmi.RemoteException {
        return it;
    }

    public void throwError(Error it) throws java.rmi.RemoteException {
        throw it;
    }
    
    public void throwRemoteException(java.rmi.RemoteException it) throws java.rmi.RemoteException {
        throw it;
    }
    
    public void throwRuntimeException(RuntimeException it) throws java.rmi.RemoteException {
        throw it;
    }

    public Hello echoRemote (Hello stub) throws java.rmi.RemoteException {
        return stub;
    }
}
