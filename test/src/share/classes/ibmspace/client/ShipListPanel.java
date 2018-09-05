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
