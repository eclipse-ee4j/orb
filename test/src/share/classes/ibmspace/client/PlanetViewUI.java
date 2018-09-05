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

// PlanetView is really a wrapper for planet gifs that is used
// by GalaxyView. 


package ibmspace.client;

import java.awt.*;
import ibmspace.common.Planet;
import ibmspace.common.PlanetView;

public class PlanetViewUI
{
    private static Image    fgUnknownPlanetImage = null;
    private static Image    fgGoodPlanetImage = null;
    private static Image    fgBadPlanetImage = null;
    private static Image    fgSelectionImage = null;

    private PlanetView      fPlanetView;
    private Image           fIcon;
    private Image           fSelection;

    private boolean         fHasSatelites = false;


  
    public static void setUnknownPlanetImage (Image image)
    {
        fgUnknownPlanetImage = image;
    }

    public static void setGoodPlanetImage (Image image)
    {
        fgGoodPlanetImage = image;
    }

    public static void setBadPlanetImage (Image image)
    {
        fgBadPlanetImage = image;
    }

    public static void setSelectionImage (Image image)
    {
        fgSelectionImage = image;
    }

    public PlanetViewUI (PlanetView planet)
    {
        fPlanetView = planet;
        Button c = new Button ();
        fHasSatelites = planet.hasSatelites ();
        double suitability = planet.getSuitability ();

        //System.out.println ("Suitability: " + suitability);

        if ( suitability == -1 ) {
            fIcon = fgUnknownPlanetImage;
        } else if ( suitability > 0.5 ) {
            fIcon = fgGoodPlanetImage;
        } else {
            fIcon = fgBadPlanetImage;
        }
    
        fSelection = fgSelectionImage;
    }

    public String getName ()
    {
        return fPlanetView.getName ();
    }

    public Rectangle getBounds ()
    {
        Point location = fPlanetView.getCoordinates();
        return new Rectangle (location.x-19, location.y-19, 38, 38);
    }

    public void draw (Graphics g, boolean drawPlanet, boolean drawName, boolean drawSelection)
    {
        Point location = fPlanetView.getCoordinates ();

        if ( drawPlanet ) {
            if ( drawSelection ) {
                g.drawImage (fSelection, location.x-30, location.y-30, 60, 60, null);
            }
            g.drawImage (fIcon, location.x-19, location.y-19, 38, 38, null);

            if ( fHasSatelites ) {
                g.setColor (Color.white);
                g.drawOval (location.x-22, location.y-21, 43, 43);
            }
        }

        if ( drawName ) {
            g.setColor (Color.white);
            g.setFont (new Font ("SansSerif",Font.PLAIN,10));
            FontMetrics fm = g.getFontMetrics ();
            int width = fm.stringWidth (getName());
            int height = fm.getHeight () + fm.getLeading ();
            int x = location.x - (width/2);
            int y = location.y + 20 + height;
            g.drawString (getName(), x, y);
        }

    }

}
