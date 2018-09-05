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

package alpha.bravo;

public class Platypus implements java.io.Serializable {

    private static final long serialVersionUID = -917480103144118490L;

    public int typedef;
    public int _fred;
    public int a$b;                                                 //$ is \U0024
    public int x\u03bCy;                                    // \u03bc is Greek mu

    public java.lang.Object anObject;
    public java.lang.Class aClass;
    public java.lang.String aString;
    public java.io.Serializable aSerializable;
    public java.io.Externalizable anExternalizable;
    public java.rmi.Remote aRemote;
    public java.util.Hashtable aHashtable;

    class Marsupial /* implements java.io.Serializable */ {
        public int pouch;
    }

    class M$em implements java.io.Serializable {
        public int p$rice;
    }

    public int jack;
    public int Jack;
    public int JAck;
    public int J_ack;

}
