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

// Displays planet statistics.  This is a useful example of text
// scaling, which should be a whole lot easier in 1.2 with
// tranformations, but this is written to work on 1.1.6 or later.

package ibmspace.client;

import java.awt.*;
import javax.swing.*;
import ibmspace.common.Planet;
import ibmspace.common.PlanetView;

public class PlanetStatsView extends JComponent
{
    private String      fName = "Unknown";
    private String      fIncome = "Unknown";
    private String      fPopulation = "Unknown";
    private String      fTemperature = "Unknown";
    private String      fGravity = "Unknown";
    private String      fMetal = "Unknown";

    public PlanetStatsView ()
    {
    }

    public void presentPlanet (PlanetView planet)
    {

        // More info if we have it

        if ( planet != null ) {
            fName = planet.getName ();
            if ( planet.isOwned() ) {
                fIncome = String.valueOf (planet.getIncome());
                fPopulation = String.valueOf (planet.getPopulation());
                fTemperature = String.valueOf (planet.getTemp());
                fGravity = String.valueOf (planet.getGravity());
                fMetal = String.valueOf (planet.getMetal());
            }
        }

        repaint ();
    }

    public void paint (Graphics g)
    {
        update (g);
    }

    public void update (Graphics g)
    {
        Rectangle bounds = getBounds ();

        //bounds.grow (-horzInset,-vertInset);
        int bx = bounds.x;
        int by = bounds.y;
        int bw = bounds.width;
        int bh = bounds.height;

        //
        // Determine and Set Optimal Font Point Size
        //

        int maxHeight = bh / 8;
        int maxWidth  = bw / 2;
        int pointSize = 1;

        for ( int pt = 1; pt < 72; pt ++ ) {
            Font f = new Font ("SansSerif",Font.PLAIN,pt);
            g.setFont (f);
            FontMetrics fm = g.getFontMetrics ();
            int height = fm.getHeight () + fm.getLeading ();
            int width = fm.stringWidth (" Income: ");

            if ( height > maxHeight || width > maxWidth )
                break;

            pointSize = pt;
        }


        //
        // Align Text Fields
        //

        int x, y;
        FontMetrics fm = g.getFontMetrics ();
        g.setColor (Color.black);

        x = maxWidth - fm.stringWidth ("Income:");
        y = 3 * maxHeight;
        g.drawString ("Income:", x, y);
        x = maxWidth + 5;
        g.drawString (fIncome, x, y);

        x = maxWidth - fm.stringWidth ("Pop:");
        y += maxHeight;
        g.drawString ("Pop:", x, y);
        x = maxWidth + 5;
        g.drawString (fPopulation, x, y);

        x = maxWidth - fm.stringWidth ("Temp:");
        y += maxHeight;
        g.drawString ("Temp:", x, y);
        x = maxWidth + 5;
        g.drawString (fTemperature, x, y);

        x = maxWidth - fm.stringWidth ("Gravity:");
        y += maxHeight;
        g.drawString ("Gravity:", x, y);
        x = maxWidth + 5;
        g.drawString (fGravity, x, y);

        x = maxWidth - fm.stringWidth ("Metal:");
        y += maxHeight;
        g.drawString ("Metal:", x, y);
        x = maxWidth + 5;
        g.drawString (fMetal, x, y);

        int planetWidth = fm.stringWidth (fName);
        x = (bw - planetWidth)/2;
        y = (int)(1.5 * maxHeight);
        g.setFont (new Font ("SansSerif",Font.BOLD,pointSize+1));
        g.drawString (fName, x, y);

    }

}
