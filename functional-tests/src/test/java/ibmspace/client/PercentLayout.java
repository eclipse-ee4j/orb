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

// A layout manager that lays out components in either a vertical
// or horizontal arrangement giving each component a percentage
// of the arrangement.  I did this quickly at the end and it can
// certainly be improved.  For one, it doesn't check to ensure that
// the percentages total 100!  It's still pretty useful.

package ibmspace.client;

import java.lang.*;
import java.awt.*;
import java.util.*;

public class PercentLayout implements LayoutManager2 {
    static int VERT = 0;
    static int HORZ = 1;

    private int fAlign;

    private Vector fComponents = null;

    public PercentLayout(int align) {
        fAlign = align;
        fComponents = new Vector();
    }

    public void addLayoutComponent(Component c, Object constraints) {
        fComponents.addElement(new ComponentInfo(c, (Float) constraints));
    }

    public void addLayoutComponent(String name, Component c) {
        // Not supported
    }

    public void removeLayoutComponent(Component c) {
        for (int i = 0; i < fComponents.size(); i++) {
            ComponentInfo ci = (ComponentInfo) fComponents.elementAt(i);
            if (ci.fComponent == c) {
                fComponents.removeElement(ci);
            }
        }
    }

    public float getLayoutAlignmentX(Container target) {
        return target.getAlignmentX();
    }

    public float getLayoutAlignmentY(Container target) {
        return target.getAlignmentY();
    }

    public void invalidateLayout(Container target) {
    }

    public Dimension preferredLayoutSize(Container target) {
        Dimension size = new Dimension(0, 0);

        if (fAlign == VERT) {
            for (int i = 0; i < fComponents.size(); i++) {
                ComponentInfo ci = (ComponentInfo) fComponents.elementAt(i);
                Component c = ci.fComponent;
                Dimension cSize = c.getPreferredSize();
                size.setSize(Math.max(size.width, cSize.width), size.height + cSize.height);
            }
        } else {
            for (int i = 0; i < fComponents.size(); i++) {
                ComponentInfo ci = (ComponentInfo) fComponents.elementAt(i);
                Component c = ci.fComponent;
                Dimension cSize = c.getPreferredSize();
                size.setSize(size.width + cSize.width, Math.max(size.height, cSize.height));
            }
        }

        return size;
    }

    public Dimension minimumLayoutSize(Container target) {
        Dimension size = new Dimension(0, 0);

        if (fAlign == VERT) {
            for (int i = 0; i < fComponents.size(); i++) {
                ComponentInfo ci = (ComponentInfo) fComponents.elementAt(i);
                Component c = ci.fComponent;
                Dimension cSize = c.getMinimumSize();
                size.setSize(Math.max(size.width, cSize.width), size.height + cSize.height);
            }
        } else {
            for (int i = 0; i < fComponents.size(); i++) {
                ComponentInfo ci = (ComponentInfo) fComponents.elementAt(i);
                Component c = ci.fComponent;
                Dimension cSize = c.getMinimumSize();
                size.setSize(size.width + cSize.width, Math.max(size.height, cSize.height));
            }
        }

        return size;
    }

    public Dimension maximumLayoutSize(Container target) {
        Dimension size = new Dimension(0, 0);

        if (fAlign == VERT) {
            for (int i = 0; i < fComponents.size(); i++) {
                ComponentInfo ci = (ComponentInfo) fComponents.elementAt(i);
                Component c = ci.fComponent;
                Dimension cSize = c.getMaximumSize();
                size.setSize(Math.max(size.width, cSize.width), size.height + cSize.height);
            }
        } else {
            for (int i = 0; i < fComponents.size(); i++) {
                ComponentInfo ci = (ComponentInfo) fComponents.elementAt(i);
                Component c = ci.fComponent;
                Dimension cSize = c.getMaximumSize();
                size.setSize(size.width + cSize.width, Math.max(size.height, cSize.height));
            }
        }

        return size;
    }

    public void layoutContainer(Container target) {
        Insets insets = target.getInsets();
        int x = insets.left;
        int y = insets.top;
        int width = target.getSize().width - (insets.left + insets.right);
        int height = target.getSize().height - (insets.top + insets.bottom);

        if (fAlign == VERT) {
            for (int i = 0; i < fComponents.size(); i++) {
                ComponentInfo ci = (ComponentInfo) fComponents.elementAt(i);
                Component c = ci.fComponent;
                float p = ci.fPercentage;
                int ch = (int) (height * p);
                c.setLocation(x, y);
                c.setSize(width, ch);
                y += ch;
            }
        } else {
            for (int i = 0; i < fComponents.size(); i++) {
                ComponentInfo ci = (ComponentInfo) fComponents.elementAt(i);
                Component c = ci.fComponent;
                float p = ci.fPercentage;
                int cw = (int) (width * p);
                c.setLocation(x, y);
                c.setSize(cw, height);
                x += cw;
            }
        }

    }

}

class ComponentInfo {
    public Component fComponent = null;
    public float fPercentage;

    public ComponentInfo(Component c, Float p) {
        fComponent = c;
        fPercentage = p.floatValue();
    }
}
