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

package ibmspace.common;

public class TechProfile implements java.io.Serializable {
    private int fRange;
    private int fSpeed;
    private int fWeapons;
    private int fShields;
    private int fMini;

    public TechProfile(int r, int sp, int w, int sh, int m) {
        fRange = r;
        fSpeed = sp;
        fWeapons = w;
        fShields = sh;
        fMini = m;
    }

    public int getRange() {
        return fRange;
    }

    public int getSpeed() {
        return fSpeed;
    }

    public int getWeapons() {
        return fWeapons;
    }

    public int getShields() {
        return fShields;
    }

    public int getMini() {
        return fMini;
    }

    public String toString() {
        String s = "(";
        s += String.valueOf(fRange) + ",";
        s += String.valueOf(fSpeed) + ",";
        s += String.valueOf(fWeapons) + ",";
        s += String.valueOf(fShields) + ",";
        s += String.valueOf(fMini) + ",";
        s += ")";
        return s;
    }

}
