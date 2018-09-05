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

public class Kangaroo extends Wallaby
    implements java.io.Serializable,
               java.lang.Cloneable {
    private int  length;
    public java.util.Hashtable aHashtable;
    public       Kangaroo( int length ) { }
    private void writeObject( java.io.ObjectOutputStream s ) { }
    public int   getLength() { int i = 0; return i; }
}
