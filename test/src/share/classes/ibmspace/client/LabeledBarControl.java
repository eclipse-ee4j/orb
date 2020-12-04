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

// A container for BarControls that adds labels.

package ibmspace.client;

import java.awt.*;
import java.util.*;
import java.text.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.event.*;


public class LabeledBarControl extends JPanel
{
    private JLabel      fLabel;
    private BarControl  fBar;

    public LabeledBarControl(String text)
    {
        BorderLayout layout = new BorderLayout (5,5);

        fLabel = new JLabel (text);
        fLabel.setPreferredSize (new Dimension(80,20));
        fLabel.setMinimumSize (new Dimension(80,20));
        fLabel.setMaximumSize (new Dimension(80,20));

        fBar = new BarControl (Color.blue);
        fBar.setBorder (new EmptyBorder (3,2,3,2));

        setPreferredSize (new Dimension(50,20));
        setMinimumSize (new Dimension(500,20));
        setMaximumSize (new Dimension(1000,20));

        setLayout (layout);
        add (fLabel, "West");
        add (fBar, "Center");
    }

    public LabeledBarControl(String text, byte orientation)
    {
        BorderLayout layout = new BorderLayout (5,5);

        fLabel = new JLabel (text);
        //fLabel.setBorder (new EtchedBorder());

        fBar = new BarControl (Color.blue, orientation);

        setLayout (layout);

        add (fBar, "Center");

        switch ( orientation )
            {
            case BarControl.HORIZONTAL:
                fLabel.setPreferredSize (new Dimension(80,20));
                fLabel.setMinimumSize (new Dimension(80,20));
                fBar.setBorder (new EmptyBorder (3,2,3,2));
                add (fLabel, "West");
                break;
            case BarControl.VERTICAL:
                fLabel.setFont (new Font ("SansSerif", Font.PLAIN, 10));
                fLabel.setPreferredSize (new Dimension(40,20));
                fLabel.setMinimumSize (new Dimension(40,20));
                fLabel.setMaximumSize (new Dimension(40,20));
                fBar.setBorder (new EmptyBorder (2,12,2,12));
                fLabel.setHorizontalAlignment (JLabel.CENTER);
                fBar.setSize (new Dimension(20,80));
                fBar.setMaximumSize (new Dimension(20,100));
                fBar.setMinimumSize (new Dimension(20,50));
                fBar.setPreferredSize (new Dimension(20,80));
                add (fLabel, "South");
                break;
            default:
                break;
            }

    }

    public void addActionListener (ActionListener listener)
    {
        fBar.addActionListener (listener);
    }

    public void removeActionListener (ActionListener listener)
    {
        fBar.removeActionListener (listener);
    }

    public void setLabelText (String labelText)
    {
        fLabel.setText (labelText);
    }

    public String getLabelText ()
    {
        return fLabel.getText ();
    }

    public void setBarColor (Color color)
    {
        fBar.setColor (color);
    }

    public Color getBarColor ()
    {
        return fBar.getColor ();
    }

    public BarControl getBarControl ()
    {
        return fBar;
    }


    public void setPercentage (double percentage)
    {
        fBar.setPercentage (percentage);
    }

    public double getPercentage ()
    {
        return fBar.getPercentage ();
    }

}
