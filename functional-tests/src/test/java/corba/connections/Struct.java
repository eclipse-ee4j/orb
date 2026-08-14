/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
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

//
// Created       : 2003 Sep 27 (Sat) 15:39:01 by Harold Carr.
// Last Modified : 2003 Sep 27 (Sat) 15:39:39 by Harold Carr.
//

package corba.connections;

import java.io.Serializable;

public class Struct implements Serializable {
    private static int INSTANCE_SIZE = 20;

    public static Struct[] getSampleInstance() {
        Struct[] instance = new Struct[INSTANCE_SIZE];
        for (int i = 0; i < instance.length; i++) {
            instance[i] = new Struct("This is a string", 12345678, true);
        }

        return instance;
    }

    private String _stringT;
    private int _intT;
    private boolean _booleanT;

    Struct(String s, int i, boolean b) {
        _stringT = s;
        _intT = i;
        _booleanT = b;
    }

    public void setStringT(String s) {
        _stringT = s;
    }

    public String getStringT() {
        return _stringT;
    }

    public void setIntT(int i) {
        _intT = i;
    }

    public int getIntT() {
        return _intT;
    }

    public void setBooleanT(boolean b) {
        _booleanT = b;
    }

    public boolean getBooleanT() {
        return _booleanT;
    }
}

// End of file.
