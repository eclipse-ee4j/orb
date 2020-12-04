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

package ibmspace.client;

import java.util.Vector;
import java.lang.reflect.Array;
import javax.swing.*;
import javax.swing.event.*;
import ibmspace.common.*;

public class ShipListPanel extends JScrollPane
{
    private JList       fList = null;
    private Vector      fFleets = null;
    private Vector      fLabels = null;

    public ShipListPanel()
    {
        fList = new JList();
        fList.setSelectionMode (0);
        fFleets = new Vector ();
        fLabels = new Vector ();
        getViewport().setView (fList);
    }

    private void updateList ()
    {
        String[] label = new String [fLabels.size()];
        for (int i=0; i<fLabels.size(); i++) {
            label[i] = (String)fLabels.elementAt(i);
        }
        fList.setListData (label);
        revalidate ();
    }

    public void addItem (Fleet fleet)
    {
        String label = fleet.toString ();
        fLabels.addElement (label);
        fFleets.addElement (fleet);
        updateList ();
    }

    public void removeItem (String item)
    {
    }

    public void removeAll ()
    {
        fLabels = new Vector ();
        fFleets = new Vector ();
        updateList ();
    }

    public Fleet[] getSelection ()
    {
        Object[] sel = fList.getSelectedValues ();

        if ( sel == null && Array.getLength(sel) == 0 ) return null;
    
        Fleet[] fleets = new Fleet [Array.getLength(sel)];
        int fleet = 0;

        for (int s=0; s<Array.getLength(sel); s++) {
            for (int f=0; f<fLabels.size(); f++) {
                String label = (String)fLabels.elementAt (f);
                if ( label == sel[s] ) {
                    fleets[fleet++] = (Fleet)fFleets.elementAt (f);
                    break;
                }
            }
        }
        return fleets;
    }

}
