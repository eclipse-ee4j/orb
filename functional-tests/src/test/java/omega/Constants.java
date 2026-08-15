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

package omega;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Constants extends Remote {

    public final static byte BYTE = 1;
    public final static short SHORT = 2;
    public final static int INT = 3;
    public final static long LONG = 4;
    public final static char CHAR = 5;
    public final static float FLOAT = 6;
    public final static double DOUBLE = 7;
    public final static String STRING = "abc";

    public final static byte BYTE2 = 10;
    public final static short SHORT2 = 20;
    public final static int INT2 = 30;
    public final static long LONG2 = 40L;
    public final static char CHAR2 = 50;
    public final static float FLOAT2 = 60F;
    public final static double DOUBLE2 = 70D;
    public final static String STRING2 = "def";

}
