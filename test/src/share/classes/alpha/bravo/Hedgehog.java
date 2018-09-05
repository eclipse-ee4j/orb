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

public class Hedgehog extends Warthog
    implements java.io.Serializable, Porcupine {
    public final static short   MAX_WARTS = 12;
    private    int      length;
    protected  boolean  foobah;
    public     Wallaby  wally;
    public              Hedgehog(String name) {}
    int      height;
    public     int      size;
    public              Hedgehog(int length) {}
    public     Kangaroo roo;
    public     int      getLength() { int i = 0; return i; }
    public     int      getSize() { int i = 1; return i; }       //from Porcupine

    public transient int trans;
}

