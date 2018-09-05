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

package ibmspace.server;

import ibmspace.common.TechProfile;


public class ResearchLab implements Investment, java.io.Serializable
{
    private Budget        fTechBudget;
    private Technology    fRange;
    private Technology    fSpeed;
    private Technology    fWeapons;
    private Technology    fShields;
    private Technology    fMini;

    public ResearchLab (TechProfile profile)
    {
        fRange = new Technology ("Range", profile.getRange());
        fSpeed = new Technology ("Speed", profile.getSpeed());
        fWeapons = new Technology ("Weapons", profile.getWeapons());
        fShields = new Technology ("Shields", profile.getShields());
        fMini = new Technology ("Mini", profile.getMini());

        fTechBudget = new Budget ("Technology");
        fTechBudget.addBudgetItem (new BudgetItem(fRange, 20));
        fTechBudget.addBudgetItem (new BudgetItem(fSpeed, 20));
        fTechBudget.addBudgetItem (new BudgetItem(fWeapons, 20));
        fTechBudget.addBudgetItem (new BudgetItem(fShields, 20));
        fTechBudget.addBudgetItem (new BudgetItem(fMini, 20));
    }

    public String getName ()
    {
        return "Technology";
    }

    public void invest (long investment)
    {
        fTechBudget.invest (investment);
    }

    public TechProfile getTechProfile ()
    {
        int range = fRange.getLevel ();
        int speed = fSpeed.getLevel ();
        int weapons = fWeapons.getLevel ();
        int shields = fShields.getLevel ();
        int mini = fMini.getLevel ();
        return new TechProfile (range, speed, weapons, shields, mini);
    }

    public Budget getTechnologyBudget ()
    {
        return fTechBudget;
    }

}
