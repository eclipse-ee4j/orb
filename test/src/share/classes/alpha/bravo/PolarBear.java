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

public class PolarBear /*extends Warthog*/ {

    private int length;
    public int weight;

    private PolarBear() {}
    public PolarBear( int arg ) {}
    public PolarBear(int length, int weight) {}
    public int getSize() { return 0; }
    private void test( int arg ) {}
    public  void test( long arg ) {}
    public int getWeight() { return 0; }

    int a = 2;
    public final static int b = 3;

}
