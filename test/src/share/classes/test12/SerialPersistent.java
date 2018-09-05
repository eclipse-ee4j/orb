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

package test12;
import java.io.ObjectStreamField;

public class SerialPersistent implements java.io.Serializable {

    private static final ObjectStreamField[] serialPersistentFields = {
        new ObjectStreamField("member1",Object.class),
        new ObjectStreamField("member2",java.util.Hashtable.class),
        new ObjectStreamField("member3",byte.class),
        new ObjectStreamField("member4",char.class),
        new ObjectStreamField("member5",short.class),
        new ObjectStreamField("member6",int.class),
        new ObjectStreamField("member7",long[][][].class),
        //        new ObjectStreamField("member8",float.class),
        new ObjectStreamField("member9",double.class),
        new ObjectStreamField("member10",boolean[].class),
    };

    java.lang.Object member1;
    java.util.Hashtable member2;
    byte member3;
    char member4;
    short member5;
    int member6;
    long[][][] member7;
    float member8;
    double member9;
    boolean[] member10;
}
