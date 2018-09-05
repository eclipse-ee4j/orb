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

// An interactive pie control.


package ibmspace.client;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public  class PieControl extends JComponent implements MouseMotionListener

{
    //
    // Data
    //

    private double  fFirstPercentage = 1.0;        // between 0.0 and 1.0
    private double  fSecondPercentage = 0;         // between 0.0 and 1.0
    private Color   fFirstColor = Color.green;
    private Color   fSecondColor = Color.blue;

    //
    // Constructors
    //

    public PieControl()
    {
        addMouseMotionListener (this);
    }

    //
    // Getters
    //

    public Color getFirstColor ()
    {
        return fFirstColor;
    }

    public Color getSecondColor ()
    {
        return fSecondColor;
    }

    public double getFirstPercentage ()
    {
        return fFirstPercentage;
    }

    public double getSecondPercentage ()
    {
        return fSecondPercentage;
    }

    //
    // Setters
    //

    public void setFirstColor (Color firstColor)
    {
        fFirstColor = firstColor;
        repaint ();
    }

    public void setSecondColor (Color secondColor)
    {
        fSecondColor = secondColor;
        repaint ();
    }

    public void setFirstPercentage (double firstPercentage)
    {
        firstPercentage = Math.max (firstPercentage, 0.0);
        firstPercentage = Math.min (firstPercentage, 1.0);

        fFirstPercentage = firstPercentage;
        fSecondPercentage = 1.0 - fFirstPercentage;
    }

    public void setSecondPercentage (double secondPercentage)
    {
        secondPercentage = Math.max (secondPercentage, 0.0);
        secondPercentage = Math.min (secondPercentage, 1.0);

        fSecondPercentage = secondPercentage;
        fFirstPercentage = 1.0 - fSecondPercentage;
    }

    //
    // Painting
    //

    public void paint (Graphics g)
    {
        update (g);
    }

    public void update (Graphics g)
    {
        Insets insets = getInsets ();
        int x = insets.left; //bounds.x;
        int y = insets.top; //bounds.y;
        int width = getSize().width - insets.left - insets.right;
        int height = getSize().height - insets.top - insets.bottom;

        int tDegrees = (int)(fFirstPercentage * 360);
        int mDegrees = (int)(fSecondPercentage * 360);

        // First Piece of Pie
        g.setColor (fFirstColor);
        g.fillArc (x,y,width,height, 89-mDegrees, -tDegrees );

        // Second Piece of Pie
        g.setColor (fSecondColor);
        g.fillArc (x,y,width,height, 89, -mDegrees);

        // Pie Outline
        g.setColor (Color.black);
        g.drawOval (x,y,width,height);
    }

    //
    // Mouse Input Handling
    //

    public void mouseDragged (MouseEvent e)
    {
        fSecondPercentage = computePercentage (e.getPoint());
        fFirstPercentage = 1 - fSecondPercentage;
        repaint();
    }

    public void mouseMoved (MouseEvent e)
    {
    }


    public double computePercentage (Point p)
    {
        Rectangle bounds = getBounds ();
        int vertInset = bounds.height / 20;
        int horzInset = bounds.width / 20;

        bounds.grow (-horzInset,-vertInset);
        int x = bounds.x;
        int y = bounds.y;
        int width = bounds.width;
        int height = bounds.height;

        Point center = new Point(x+width/2,y+height/2);

        if ( p.x > center.x ) {
            if ( p.y < center.y ) {
                // first quadrant 0 - 98

                double o = (double)(center.y - p.y);
                double a = (double)(p.x - center.x);
                double h = Math.sqrt(o*o + a*a);
                double sine = o/h;

                return (0.25 - (Math.asin(sine) * 0.5) / Math.PI);
        
            } else {
                // second quadrant 90 - 179

                double o = (double)(p.y - center.y);
                double a = (double)(p.x - center.x);
                double h = Math.sqrt(o*o + a*a);
                double sine = o/h;

                return ((Math.asin(sine) * 0.5) / Math.PI) + 0.25;
            }
        } else {
            if ( p.y > center.y ) {
                // third quadrant 180 - 269

                double o = (double)(p.y - center.y);
                double a = (double)(center.x - p.x);
                double h = Math.sqrt(o*o + a*a);
                double sine = o/h;

                return (0.25 - (Math.asin(sine) * 0.5) / Math.PI) + 0.50;
            } else {
                // fourth quadrant 270 - 359

                double o = (double)(center.y - p.y);
                double a = (double)(center.x - p.x);
                double h = Math.sqrt(o*o + a*a);
                double sine = o/h;

                return ((Math.asin(sine) * 0.5) / Math.PI) + 0.75;
            }
        }
    }


}
