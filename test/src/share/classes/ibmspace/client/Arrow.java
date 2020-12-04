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

public class Arrow
{
    private Point     fTail = null;         // point where the arrow begins
    private Point     fHead = null;         // point where the arrow ends
    private int       fTailPadding = 0;     // distance of drawn tail to fTail
    private int       fHeadPadding = 0;     // distance of drawn head to fHead
    private String    fLabel = null;        // text label drawn at arrow midpoint
    private int       fHeadLength = 20;     // length of the head
    private int       fHeadSeparation = 7;  // controls angle of head
    private Color     fColor;               // color of arrow

    protected boolean fCacheBlown = true;
    private double    fDX, fDY, fLen, fUX, fUY;
    private Point     fFrom, fTo;
    private int[]     fTipX = new int[4];
    private int[]     fTipY = new int[4];
    private Point     fLabelLoc;

    public Arrow ()
    {
    }

    public void setHead (Point head)
    {
        fHead = head;
        fCacheBlown = true;
    }

    public void setTail (Point tail)
    {
        fTail = tail;
        fCacheBlown = true;
    }

    public void setHeadPadding (int padding)
    {
        fHeadPadding = padding;
        fCacheBlown = true;
    }

    public int getHeadPadding ()
    {
        return fHeadPadding;
    }

    public void setTailPadding (int padding)
    {
        fTailPadding = padding;
        fCacheBlown = true;
    }

    public int getTailPadding ()
    {
        return fTailPadding;
    }

    public void setHeadLength (int length)
    {
        fHeadLength = length;
        fCacheBlown = true;
    }

    public int getHeadLength ()
    {
        return fHeadLength;
    }

    public void setHeadSeparation (int sep)
    {
        fHeadSeparation = sep;
        fCacheBlown = true;
    }

    public int getHeadSeparation ()
    {
        return fHeadSeparation;
    }

    public void setColor (Color color)
    {
        fColor = color;
    }

    public Color getColor ()
    {
        return fColor;
    }

    public void setLabel (String label)
    {
        fLabel = label;
    }

    public String getLabel ()
    {
        return fLabel;
    }

    public int getLength ()
    {
        if ( fCacheBlown ) updateCache ();
        return (int)fLen;
    }

    public void draw (Graphics g)
    {
        if ( fCacheBlown ) updateCache ();

        if ( !fCacheBlown ) {
            g.setColor (fColor);
            g.drawLine (fFrom.x, fFrom.y, fTo.x, fTo.y);
            g.fillPolygon (fTipX, fTipY, 4);
            if ( fLabel != null ) {
                g.drawString (fLabel, fLabelLoc.x, fLabelLoc.y);
            }

        }
    }

    private void updateCache ()
    {
        if ( fTail != null && fHead != null ) {

            // Compute lenth and unit lengths in x and y directions
      
            fDX = (double)fHead.x - (double)fTail.x;
            fDY = (double)fHead.y - (double)fTail.y;
            fLen = Math.sqrt (fDX*fDX + fDY*fDY);
            if ( fLen > 0 ) {
                fUX = fDX/fLen;
                fUY = fDY/fLen;
            } else {
                fUX = 0;
                fUY = 0;
            }

            // Compute shortened to and from points

            int tx = fTail.x + (int)(fUX*fTailPadding);
            int ty = fTail.y + (int)(fUY*fTailPadding);
            int hx = fHead.x - (int)(fUX*fHeadPadding);
            int hy = fHead.y - (int)(fUY*fHeadPadding);
            fFrom = new Point (tx,ty);
            fTo = new Point (hx,hy);

            // Compute arrow tip points

            int bx = fTo.x - (int)(fUX*fHeadLength);
            int by = fTo.y - (int)(fUY*fHeadLength);
            fTipX[0] = fTo.x;
            fTipY[0] = fTo.y;
            fTipX[1] = bx - (int)(fUY*fHeadSeparation);
            fTipY[1] = by + (int)(fUX*fHeadSeparation);
            fTipX[2] = bx;
            fTipY[2] = by;
            fTipX[3] = bx + (int)(fUY*fHeadSeparation);
            fTipY[3] = by - (int)(fUX*fHeadSeparation);

            fLabelLoc = new Point ((fFrom.x+fTo.x)/2, (fFrom.y+fTo.y)/2);

            fCacheBlown = false;
        }
    }




}
