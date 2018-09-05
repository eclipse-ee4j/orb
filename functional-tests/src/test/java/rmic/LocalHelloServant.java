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

package rmic;
import com.sun.corba.ee.impl.util.JDKBridge;
import javax.rmi.CORBA.Stub;
import org.omg.CORBA.BAD_OPERATION;
import java.rmi.RemoteException;
import java.rmi.MarshalException;

public class LocalHelloServant extends javax.rmi.PortableRemoteObject implements LocalHello {

    public LocalHelloServant() throws java.rmi.RemoteException {
        JDKBridge.setLocalCodebase(null);
    }
        
    public String sayHello (String to) throws java.rmi.RemoteException {
        return "Hello " + to;
    }
    
    public String echoString(String it) throws java.rmi.RemoteException {
        return it;
    }
    
    public Object echoObject(Object it) throws java.rmi.RemoteException {
        return it;
    }

    public int identityHash(Object it) throws java.rmi.RemoteException {
        return System.identityHashCode(it);
    }
    
    public int[] identityHash(Object a, Object b, Object c) throws java.rmi.RemoteException {
        int[] result = new int[3];
        result[0] = System.identityHashCode(a);
        result[1] = System.identityHashCode(b);
        result[2] = System.identityHashCode(c);
        return result;
    }

    public test.Hello echoHello (test.Hello in) throws java.rmi.RemoteException {
        return in;   
    }
    
    public rmic.Hello echoHello (rmic.Hello in) throws java.rmi.RemoteException {
        return in;
    }

    public void argNamesClash(int in,
                              int _in, 
                              int out,
                              int _out,
                              int so,
                              int exCopy,
                              int copies,
                              int method,
                              int reply,
                              int ex) throws java.rmi.RemoteException {
                                
    }

    public Base newServant() throws java.rmi.RemoteException {
        String codebase = JDKBridge.getLocalCodebase();
        if (codebase != null) {
            throw new java.rmi.RemoteException("localCodebase = "+codebase);
        }
        
        return new BaseImpl();
    }

    public String testPrimTypes(String arg0,
                                double arg1,
                                float arg2,
                                String arg3,
                                boolean arg4,
                                Object arg5,
                                String arg6) throws java.rmi.RemoteException {
        return "help";
    }

    public Object echoString(Object value1, String str, Object value2) throws java.rmi.RemoteException {
        if (!(value1 instanceof RemoteException)) {
            throw new RemoteException("value1 not RemoteException. Is "+value1.getClass());   
        }
        if (!(value2 instanceof MarshalException)) {
            throw new RemoteException("value2 not MarshalException. Is "+value2.getClass());   
        }
        
        return str;   
    }
    
    public Object echoArg1(int arg0, Object arg1) throws java.rmi.RemoteException {
        return arg1;
    }

}
