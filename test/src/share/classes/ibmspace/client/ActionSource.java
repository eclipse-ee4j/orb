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

import java.util.*;
import java.awt.event.*;


public class ActionSource
{
    protected Vector  fActionListeners;

    public ActionSource()
    {
        fActionListeners = new Vector ();
    }

    public void addActionListener (ActionListener listener)
    {
        if (!fActionListeners.contains (listener))
            fActionListeners.addElement (listener);
    }

    public void removeActionListener (ActionListener listener)
    {
        fActionListeners.removeElement (listener);
    }

    public void notifyListeners (Object source, String command)
    {
        ActionEvent event = new ActionEvent (source, ActionEvent.ACTION_PERFORMED, command);

        for (int i=0; i<fActionListeners.size(); i++)
            {
                ActionListener listener = (ActionListener)fActionListeners.elementAt (i);
                listener.actionPerformed (event);
            }

    }

}
