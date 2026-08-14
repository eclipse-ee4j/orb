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
