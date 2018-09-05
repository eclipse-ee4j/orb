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

// Like PlanetStatsView, this view displays text and scales
// it to fit the given view size.


package ibmspace.client;

import java.awt.*;
import javax.swing.*;

public class TechLevelsPanel extends JComponent
{
    private String fRange = "Range = UNKNOWN";
    private String fSpeed = "Speed = UNKNOWN";
    private String fWeapons = "Weapons = UNKNOWN";
    private String fShields = "Shields = UNKNOWN";
    private String fMini = "Mini = UNKNOWN";

    public TechLevelsPanel ()
    {
    }

    public void setRange (int range)
    {
        fRange = "Range = " + String.valueOf(range);
    }

    public void setSpeed (int speed)
    {
        fSpeed = "Speed = " + String.valueOf(speed);
    }

    public void setWeapons (int weapons)
    {
        fWeapons = "Weapons = " + String.valueOf(weapons);
    }

    public void setShields (int shields)
    {
        fShields = "Shields = " + String.valueOf(shields);
    }

    public void setMini (int mini)
    {
        fMini = "Mini = " + String.valueOf(mini);
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

        int maxHeight = bh / 7;
        int maxWidth  = bw - 15;
        int pointSize = 1;
        int padding = 0;

        for ( int pt = 1; pt < 72; pt ++ ) {
            Font f = new Font ("SansSerif",Font.PLAIN,pt);
            g.setFont (f);
            FontMetrics fm = g.getFontMetrics ();
            int height = fm.getHeight () + fm.getLeading ();
            int width = fm.stringWidth (fRange);
            width = Math.max(width,fm.stringWidth (fSpeed));
            width = Math.max(width,fm.stringWidth (fWeapons));
            width = Math.max(width,fm.stringWidth (fShields));
            width = Math.max(width,fm.stringWidth (fMini));

            if ( height > maxHeight || width > maxWidth )
                break;

            padding = (maxWidth - width) / 2;
            pointSize = pt;
        }



        //
        // Align Text Fields
        //

        int x, y;
        FontMetrics fm = g.getFontMetrics ();
        g.setColor (Color.black);

        x = padding;
        y = 2 * maxHeight;
        g.drawString (fRange, x, y);
        y += maxHeight;
        g.drawString (fSpeed, x, y);
        y += maxHeight;
        g.drawString (fWeapons, x, y);
        y += maxHeight;
        g.drawString (fShields, x, y);
        y += maxHeight;
        g.drawString (fMini, x, y);
    }

}
