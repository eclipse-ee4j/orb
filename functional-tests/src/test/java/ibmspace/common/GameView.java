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
