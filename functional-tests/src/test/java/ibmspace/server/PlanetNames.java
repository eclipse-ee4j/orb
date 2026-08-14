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

package ibmspace.server;

import java.util.Vector;

public class PlanetNames implements java.io.Serializable {
    private Vector fNames;

    public PlanetNames() {
        fNames = new Vector();
        fNames.addElement(new String("Deneb"));
        fNames.addElement(new String("Proxima"));
        fNames.addElement(new String("Enif"));
        fNames.addElement(new String("Altair"));
        fNames.addElement(new String("Ursa"));
        fNames.addElement(new String("Sauron"));
        fNames.addElement(new String("Sol"));
        fNames.addElement(new String("Propus"));
        fNames.addElement(new String("Hobbes"));
        fNames.addElement(new String("Spica"));
        fNames.addElement(new String("Yavin"));
        fNames.addElement(new String("Virgo"));
        fNames.addElement(new String("Tiber"));
        fNames.addElement(new String("Quark"));
        fNames.addElement(new String("Coxa"));
        fNames.addElement(new String("Libra"));
        fNames.addElement(new String("Atlas"));
        fNames.addElement(new String("Alkaid"));
        fNames.addElement(new String("Antares"));
        fNames.addElement(new String("Rigel"));
        fNames.addElement(new String("Murzim"));
        fNames.addElement(new String("Barsoon"));
        fNames.addElement(new String("Atria"));
        fNames.addElement(new String("Thune"));
        fNames.addElement(new String("Regor"));
        fNames.addElement(new String("Remulak"));
        fNames.addElement(new String("Sooltar"));
        fNames.addElement(new String("Klah"));
        fNames.addElement(new String("Dabih"));
        fNames.addElement(new String("Basil"));
        fNames.addElement(new String("Hope"));
        fNames.addElement(new String("Torino"));
        fNames.addElement(new String("Scorpio"));
        fNames.addElement(new String("Procyon"));
        fNames.addElement(new String("Beid"));
        fNames.addElement(new String("Denali"));
        fNames.addElement(new String("Ain"));
    }

    public String getName() {
        int index = (int) (Math.random() * (fNames.size() - 1));
        String name = (String) fNames.elementAt(index);
        fNames.removeElementAt(index);
        return name;
    }

}
