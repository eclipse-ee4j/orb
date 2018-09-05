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

public class TheErrorValue extends java.rmi.RemoteException implements TheValue {
    String mssg = null;

    public TheErrorValue(String mssg){
        this.mssg = mssg;
    }

    public String sayHello(){
        return "Error : Could not load TheValueImpl so this was loaded instead :"+mssg;
    }
}
