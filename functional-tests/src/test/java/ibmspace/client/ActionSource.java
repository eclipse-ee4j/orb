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

import java.util.*;
import java.awt.event.*;

public class ActionSource {
    protected Vector fActionListeners;

    public ActionSource() {
        fActionListeners = new Vector();
    }

    public void addActionListener(ActionListener listener) {
        if (!fActionListeners.contains(listener)) {
            fActionListeners.addElement(listener);
        }
    }

    public void removeActionListener(ActionListener listener) {
        fActionListeners.removeElement(listener);
    }

    public void notifyListeners(Object source, String command) {
        ActionEvent event = new ActionEvent(source, ActionEvent.ACTION_PERFORMED, command);

        for (int i = 0; i < fActionListeners.size(); i++) {
            ActionListener listener = (ActionListener) fActionListeners.elementAt(i);
            listener.actionPerformed(event);
        }

    }

}
