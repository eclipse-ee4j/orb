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

package ibmspace.server;

import java.util.Vector;
import java.io.Serializable;

public class Battle implements Serializable {
    private Vector fGroups;
    private Player fWinner = null;
    private Player fLoser = null;
    private long fScrapMetal = 0;

    public Battle() {
        fGroups = new Vector();
    }

    public Player getWinner() {
        return fWinner;
    }

    public Player getLoser() {
        return fLoser;
    }

    public long getScrapMetal() {
        return fScrapMetal;
    }

    public void addFleet(FleetImpl fleet) {
        int i = findGroupFor(fleet);
        Vector group = getGroup(i);
        group.addElement(fleet);
    }

    public void addFleets(Vector fleets) {
        for (int i = 0; i < fleets.size(); i++) {
            FleetImpl fleet = (FleetImpl) fleets.elementAt(i);
            addFleet(fleet);
        }
    }

    public void runBattleSimulation(int offender, int defender) {
        int os = getStrengthOfGroup(offender);
        int or = getResistanceOfGroup(offender);
        int ds = getStrengthOfGroup(defender);
        int dr = getResistanceOfGroup(defender);

        if (getStrengthOfGroup(offender) > getResistanceOfGroup(defender)) {
            fWinner = getOwnerOfGroup(offender);
            fLoser = getOwnerOfGroup(defender);
            eliminateGroup(defender);
        } else if (getStrengthOfGroup(defender) > getResistanceOfGroup(offender)) {
            fWinner = getOwnerOfGroup(defender);
            fLoser = getOwnerOfGroup(offender);
            eliminateGroup(offender);
        } else {
            System.out.println("random call");
            int coin = (int) (Math.random() * 1.0);
            if (coin == 0) {
                fWinner = getOwnerOfGroup(defender);
                fLoser = getOwnerOfGroup(offender);
                eliminateGroup(offender);
            } else {
                fWinner = getOwnerOfGroup(offender);
                fLoser = getOwnerOfGroup(defender);
                eliminateGroup(defender);
            }
        }
    }

    public int getNumberOfGroups() {
        return fGroups.size();
    }

    public int getStrengthOfGroup(int index) {
        int strength = 0;
        Vector group = getGroup(index);
        for (int i = 0; i < group.size(); i++) {
            FleetImpl fleet = (FleetImpl) group.elementAt(i);
            strength += fleet.getStrenth();
        }
        return strength;
    }

    public int getResistanceOfGroup(int index) {
        int resistance = 0;
        Vector group = getGroup(index);
        for (int i = 0; i < group.size(); i++) {
            FleetImpl fleet = (FleetImpl) group.elementAt(i);
            resistance += fleet.getResistance();
        }
        return resistance;
    }

    public Player getOwnerOfGroup(int i) {
        Vector group = (Vector) fGroups.elementAt(i);
        FleetImpl fleet = (FleetImpl) group.elementAt(0);
        return fleet.getOwner();
    }

    public Vector getGroup(int i) {
        Vector group = (Vector) fGroups.elementAt(i);
        return group;
    }

    public int findGroupFor(FleetImpl fleet) {
        // Look for existing group

        for (int i = 0; i < getNumberOfGroups(); i++) {
            Player owner = getOwnerOfGroup(i);
            if (owner == fleet.getOwner()) {
                return i;
            }
        }

        // Not found so create new group

        Vector group = new Vector();
        fGroups.addElement(group);
        return getNumberOfGroups() - 1;

    }

    public void eliminateGroup(int index) {
        Vector group = (Vector) fGroups.elementAt(index);
        for (int i = 0; i < group.size(); i++) {
            FleetImpl fleet = (FleetImpl) group.elementAt(i);
            fScrapMetal += fleet.getScrapMetal();
        }
        fGroups.removeElement(group);
    }

}
