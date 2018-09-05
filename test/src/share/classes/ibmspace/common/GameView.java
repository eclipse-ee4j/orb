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

package ibmspace.common;

import java.rmi.RemoteException;
import java.util.Vector;

// Note:  All methods in this interface are declared to throw RemoteException
//        because some subclasses (remote servers) will do this.  Declaring
//        to throw an exception does not mean that a subclass WILL throw it
//        and Java subclasses can remove exceptions but cannot add them.

public interface GameView
{
    void              test () throws RemoteException;

    //
    // Working with budgets
    //

    BudgetSummary     getMainBudget () throws RemoteException;
    BudgetSummary     getTechBudget () throws RemoteException;
    BudgetSummary     getPlanetBudget (ID planet) throws RemoteException;
    void              setMainBudget (BudgetSummary bs) throws RemoteException;
    void              setTechBudget (BudgetSummary bs) throws RemoteException;
    void              setPlanetBudget (ID planet, BudgetSummary bs) throws RemoteException;

    //
    // Working with assets
    //

    long              getShipSavings () throws RemoteException;
    long              getIncome () throws RemoteException;
    long              getShipMetal () throws RemoteException;
    TechProfile       getTechProfile () throws RemoteException;

    //
    // Working with ships
    //

    ShipDesign        designShip (String name, int type, TechProfile tech) throws RemoteException;
    ID /*fleet*/      buildFleet (ShipDesign design, int num, ID station) throws RemoteException;
    void              scrapFleet (ID fleet) throws RemoteException;
    ID /*journey*/    sendFleet (ID fleet, ID planet) throws RemoteException;
    Fleet             getFleet (ID fleet) throws RemoteException;
    ID[]              getFleetsAt (ID planet) throws RemoteException;
    Journey           getJourney (ID journeyOrShip) throws RemoteException;
    ID[]              getAllJournies () throws RemoteException;

    //
    // Working with planets
    //

    ID                getHome () throws RemoteException;
    PlanetView        getPlanet (ID planet) throws RemoteException;
    void              abandonPlanet (ID planet) throws RemoteException;

    //
    // Turn taking
    //

    Vector            takeTurn () throws RemoteException;
    void              quit () throws RemoteException;

    long              getCalls () throws RemoteException;

}
