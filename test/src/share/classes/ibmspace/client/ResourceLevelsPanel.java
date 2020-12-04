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

// Like PlanetStatsView, this view displays text and scales it
// to fit the given space.

package ibmspace.client;

import java.awt.*;
import javax.swing.*;

public class ResourceLevelsPanel extends JComponent
{
    private String      fSavings = "";
    private String      fMetal = "";
    private String      fIncome = "";
    private String      fIIOP = "";

    public ResourceLevelsPanel ()
    {
    }

    public String valueOf (long value)
    {
        String units = "";

        if ( value > 1000000 ) {
            value = value / 1000;
            if ( value > 1000000 ) {
                value = value / 1000;
                units = "M";
            } else {
                units = "K";
            }
        }
        return String.valueOf(value) + units;
    }


    public void setShipSavings (long savings)
    {
        fSavings = valueOf(savings);
    }

    public void setShipMetal (long metal)
    {
        fMetal = valueOf(metal);
    }

    public void setIncome (long income)
    {
        fIncome = valueOf(income);
    }

    public void setIIOPCalls (long calls)
    {
        fIIOP = valueOf(calls);
    }


    public void paint (Graphics g)
    {
        update (g);
    }

    public void update (Graphics g)
    {
        Rectangle bounds = getBounds ();

        int bx = bounds.x;
        int by = bounds.y;
        int bw = bounds.width;
        int bh = bounds.height;

        //
        // Determine and Set Optimal Font Point Size
        //

        int maxHeight = bh / 3;
        int maxWidth  = bw / 2;
        int pointSize = 1;
        int padding = 0;

        for ( int pt = 1; pt < 72; pt ++ ) {
            Font f = new Font ("SansSerif",Font.PLAIN,pt);
            g.setFont (f);
            FontMetrics fm = g.getFontMetrics ();
            int height = fm.getHeight () + fm.getLeading ();
            int width = fm.stringWidth ("Savings: " + fSavings);
            width = Math.max(width,fm.stringWidth ("Metal: " + fMetal));
            width = Math.max(width,fm.stringWidth ("Income: " + fIncome));
            width = Math.max(width,fm.stringWidth ("IIOP: " + fIIOP));

            if ( height > maxHeight || width > maxWidth )
                break;

            padding = (maxWidth - width) / 4;
            pointSize = pt;
        }

        //
        // Align Text Fields
        //

        int x, y;
        FontMetrics fm = g.getFontMetrics ();
        g.setColor (Color.black);

        int indent = fm.stringWidth ("Savings: ");

        x = padding;
        y = (int)(1.5 * (double)maxHeight);
        g.drawString ("Savings: ", x, y);
        x += indent;
        g.drawString (fSavings, x, y);

        x = maxWidth + padding;
        g.drawString ("Income: ", x, y);
        x += indent;
        g.drawString (fIncome, x, y);

        x = padding;
        y += maxHeight;
        g.drawString ("Metal: ", x, y);
        x += indent;
        g.drawString (fMetal, x, y);

        x = maxWidth + padding;
        g.setColor (Color.red);
        g.drawString ("IIOP: ", x, y);
        x += indent;
        g.drawString (fIIOP, x, y);

    }



}
