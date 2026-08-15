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

import java.io.Serializable;

public interface Hello extends java.rmi.Remote {
    String CONSTANT1 = "constant1";
    int CONSTANT2 = 3 + 3;

    public String getCodeBase() throws java.rmi.RemoteException;

    public void publishRemoteObject(String name) throws java.rmi.RemoteException;

    public String sayHello() throws java.rmi.RemoteException;

    public int sum(int value1, int value2) throws java.rmi.RemoteException;

    public String concatenate(String str1, String str2) throws java.rmi.RemoteException;

    public String checkOBV(ObjectByValue obv) throws java.rmi.RemoteException;

    public ObjectByValue getOBV() throws java.rmi.RemoteException;

    public Hello getHello() throws java.rmi.RemoteException;

    public int[] echoArray(int[] array) throws java.rmi.RemoteException;

    public long[][] echoArray(long[][] array) throws java.rmi.RemoteException;

    public short[][][] echoArray(short[][][] array) throws java.rmi.RemoteException;

    public ObjectByValue[] echoArray(ObjectByValue[] array) throws java.rmi.RemoteException;

    public ObjectByValue[][] echoArray(ObjectByValue[][] array) throws java.rmi.RemoteException;

    public AbstractObject echoAbstract(AbstractObject absObj) throws java.rmi.RemoteException;

    public AbstractObject[] getRemoteAbstract() throws java.rmi.RemoteException;

    public void shutDown() throws java.rmi.RemoteException;

    public void throwHello(int count, String message) throws java.rmi.RemoteException, HelloException;

    public void throw_NO_PERMISSION(String s, int minor) throws java.rmi.RemoteException;

    public CharValue echoCharValue(CharValue value) throws java.rmi.RemoteException;

    public Object echoObject(Object it) throws java.rmi.RemoteException;

    public Serializable echoSerializable(Serializable it) throws java.rmi.RemoteException;

    public void throwError(Error it) throws java.rmi.RemoteException;

    public void throwRemoteException(java.rmi.RemoteException it) throws java.rmi.RemoteException;

    public void throwRuntimeException(RuntimeException it) throws java.rmi.RemoteException;

    public Hello echoRemote(Hello stub) throws java.rmi.RemoteException;
}
