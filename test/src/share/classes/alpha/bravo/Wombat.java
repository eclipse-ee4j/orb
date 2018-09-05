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

/*--Notes. See section 5.4-----------------------------------------------------
  5.4.1   passRemote uses java.rmi.Remote as a parameter and return value
  5.4.2   Wombat extends omega.Wallaby which is itself a Remote interface
  5.4.3.1 foo is an int read-write property
  5.4.3.2 URL is a String read-only property
  5.4.3.3 boo is a boolean read-write property
  5.4.3.4 attribute name pre-mangling. See 5.4.3.1-3
          ambiguous attribute name chirp, BLINT_CONSTANT;
  5.4.4   methods chirp and buzz
  5.4.5   constants BLEAT_CONSTANT, BLINT_CONSTANT
 ----------------------------------------------------------------------------*/

package alpha.bravo;

public interface Wombat extends java.rmi.Remote,
                                omega.Wallaby {
    String  BLEAT_CONSTANT = "bleat";
    int     BLINT_CONSTANT = 1;
    void    chirp(int x)       throws java.rmi.RemoteException;
    void    buzz()             throws java.rmi.RemoteException,
    omega.MammalOverload;
    int     getFoo()           throws java.rmi.RemoteException;
    void    setFoo(int x)      throws java.rmi.RemoteException;
    String  getURL()           throws java.rmi.RemoteException;
    boolean isBoo()            throws java.rmi.RemoteException;
    void    setBoo(boolean b)  throws java.rmi.RemoteException;
    void    passRemote()       throws java.rmi.RemoteException;
    java.rmi.Remote
        passRemote( java.rmi.Remote r )
        throws java.rmi.RemoteException;
    int     getChirp()         throws java.rmi.RemoteException;
    boolean isBLINT_CONSTANT() throws java.rmi.RemoteException;
}
