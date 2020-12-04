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
