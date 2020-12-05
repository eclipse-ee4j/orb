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
