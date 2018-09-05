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

// Arrow represents an arrow to be drawn between two points.  It
// also has some behavior specific to this game.  Namely, (1) you
// can shorten the head or tail so that the the end points can be
// the centers of planet gifs while the arrows are drawn between
// the planet perimeters, and (2) you can also shorten the tail to
// show progress of a journey (the tail shortens until the fleet
// reaches the destination planet.  Even with these, this is probably
// a generally useful class fo UI work.  The arrow head drawing code
// is not but it's fast.

package ibmspace.client;

import java.awt.*;

public class JourneyArrow extends Arrow
{
    private int       fAvailableRange = 0;
    private Color     fAcceptedColor = Color.cyan;
    private Color     fRejectedColor = Color.red;
    private boolean   fShowDistance = false;
    private int       fDistance;
    private String    fDistanceString;

    public JourneyArrow ()
    {
    }

    public void setAvailableRange (int range)
    {
        fAvailableRange = range;
    }

    public void setAcceptedColor (Color color)
    {
        fAcceptedColor = color;
    }

    public void setRejectedColor (Color color)
    {
        fRejectedColor = color;
    }

    public void setShowDistance (boolean show)
    {
        fShowDistance = show;
    }

    public int getDistance ()
    {
        if ( fCacheBlown ) {
            fDistance = (int)(getLength() / 50 + 0.5);
        }

        return fDistance;
    }

    public void draw (Graphics g)
    {
        int distance = getDistance ();

        if ( fShowDistance ) {
            fDistanceString = String.valueOf (distance);
            setLabel (fDistanceString);
        } else {
            setLabel (null);
        }

        if ( distance <= fAvailableRange ) {
            g.setColor (fAcceptedColor);
        } else {
            g.setColor (fRejectedColor);
        }

        super.draw (g);
    }
  



}
