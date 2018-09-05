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

package ibmspace.server;

import java.util.Vector;

public class PlanetNames implements java.io.Serializable
{
    private Vector      fNames;

    public PlanetNames ()
    {
        fNames = new Vector ();
        fNames.addElement (new String ("Deneb"));
        fNames.addElement (new String ("Proxima"));
        fNames.addElement (new String ("Enif"));
        fNames.addElement (new String ("Altair"));
        fNames.addElement (new String ("Ursa"));
        fNames.addElement (new String ("Sauron"));
        fNames.addElement (new String ("Sol"));
        fNames.addElement (new String ("Propus"));
        fNames.addElement (new String ("Hobbes"));
        fNames.addElement (new String ("Spica"));
        fNames.addElement (new String ("Yavin"));
        fNames.addElement (new String ("Virgo"));
        fNames.addElement (new String ("Tiber"));
        fNames.addElement (new String ("Quark"));
        fNames.addElement (new String ("Coxa"));
        fNames.addElement (new String ("Libra"));
        fNames.addElement (new String ("Atlas"));
        fNames.addElement (new String ("Alkaid"));
        fNames.addElement (new String ("Antares"));
        fNames.addElement (new String ("Rigel"));
        fNames.addElement (new String ("Murzim"));
        fNames.addElement (new String ("Barsoon"));
        fNames.addElement (new String ("Atria"));
        fNames.addElement (new String ("Thune"));
        fNames.addElement (new String ("Regor"));
        fNames.addElement (new String ("Remulak"));
        fNames.addElement (new String ("Sooltar"));
        fNames.addElement (new String ("Klah"));
        fNames.addElement (new String ("Dabih"));
        fNames.addElement (new String ("Basil"));
        fNames.addElement (new String ("Hope"));
        fNames.addElement (new String ("Torino"));
        fNames.addElement (new String ("Scorpio"));
        fNames.addElement (new String ("Procyon"));
        fNames.addElement (new String ("Beid"));
        fNames.addElement (new String ("Denali"));
        fNames.addElement (new String ("Ain"));
    }

    public String getName ()
    {
        int index = (int)(Math.random() * (fNames.size()-1));
        String name = (String)fNames.elementAt (index);
        fNames.removeElementAt (index);
        return name;
    }
  
}
