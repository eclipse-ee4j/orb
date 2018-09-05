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

import ibmspace.common.*;
import java.util.Vector;
import ibmspace.common.Identifiable;
import ibmspace.common.ID;


public class Player implements Identifiable, java.io.Serializable
{
    private ID          fID;
    private String      fName;
    private double      fIdealTemp;
    private double      fIdealGravity;
    private PlanetImpl  fHome = null;

    private ShipSavings fSavings = null;
    private ResearchLab fResearchLab = null;
    private Budget      fBudget = null;
    private long        fShipMetal;

    private Vector      fMessages;

    public Player(String name, double idealTemp, double idealGravity)
    {
        fName = name;
        fIdealTemp = idealTemp;
        fIdealGravity = idealGravity;
        fHome = null;

        fSavings = new ShipSavings (40000);
        fShipMetal = 40000;

        fResearchLab = new ResearchLab (new TechProfile(6,2,2,2,1));
        fBudget = new Budget ("Main Budget");
        fBudget.addBudgetItem (new BudgetItem (fSavings,50));
        fBudget.addBudgetItem (new BudgetItem (fResearchLab,50));

        fMessages = new Vector ();
    }

    public void rememberPlanetOwner (Planet planet, String owner)
    {
    }

    public ID getID ()
    {
        return fID;
    }

    public String getName ()
    {
        return fName;
    }

    public void setHome (PlanetImpl home)
    {
        fHome = home;
    }

    public PlanetImpl getHome ()
    {
        return fHome;
    }

    public Budget getBudget ()
    {
        return fBudget;
    }

    public Budget getTechBudget ()
    {
        return fResearchLab.getTechnologyBudget ();
    }

    public Budget getPlanetBudget (PlanetImpl planet)
    {
        BudgetItem item = fBudget.findBudgetItem (planet.getName());
        if ( item != null ) {
            return (Budget)item.getInvestment ();
        } else {
            return null;
        }
    }

    public ShipSavings getShipSavings ()
    {
        return fSavings;
    }

    public long getShipMetal ()
    {
        return fShipMetal;
    }

    public ResearchLab getResearchLab ()
    {
        return fResearchLab;
    }

    public void setShipMetal (int metal)
    {
        fShipMetal = metal;
    }

    public void addShipMetal (long metal)
    {
        fShipMetal += metal;
    }

    public void removeShipMetal (long metal)
    {
        fShipMetal -= metal;
    }

    public double getIdealTemp ()
    {
        return fIdealTemp;
    }

    public double getIdealGravity ()
    {
        return fIdealGravity;
    }

    public double getRelativeTempFor (double temp)
    {
        return temp/fIdealTemp * 72.0;
    }

    public double getRelativeGravityFor (double gravity)
    {
        return gravity/fIdealGravity;
    }


    public void clearMessages ()
    {
        fMessages.removeAllElements ();
    }

    public void addMessage (String message)
    {
        fMessages.addElement (message);
    }

    public Vector getMessages ()
    {
        Vector m = fMessages;
        fMessages = new Vector ();
        return m;
    }


}
