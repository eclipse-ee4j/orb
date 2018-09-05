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

import java.util.Vector;
import java.lang.reflect.Array;
import ibmspace.common.BudgetSummary;
import java.io.Serializable;

public class Budget implements Investment, Serializable
{
    String fName;
    Vector fBudgetItems;




    public Budget (String name)
    {
        fName = name;
        fBudgetItems = new Vector ();
    }

    public String getName ()
    {
        return fName;
    }

    public void invest (long dollars)
    {
        balance ();

        for (int i=0; i<fBudgetItems.size(); i++) {
            BudgetItem item = (BudgetItem)fBudgetItems.elementAt (i);
            Investment investment = item.getInvestment ();
            long investmentDollars = dollars * item.getPercentage() / 100;
            if ( investmentDollars > dollars ) {
                investmentDollars = dollars;
            }
            investment.invest (investmentDollars);
            dollars -= investmentDollars;
        }
    }

    public int totalAllocations ()
    {
        int total = 0;

        for (int i=0; i<fBudgetItems.size(); i++) {
            BudgetItem item = (BudgetItem)fBudgetItems.elementAt(i);
            total += item.getPercentage ();
        }

        return total;
    }

    public int numberOfBudgetItems ()
    {
        return fBudgetItems.size ();
    }

    public void balance ()
    {
        int difference = totalAllocations() - 100;
        int change = 0;

        if ( difference != 0 ) {
            if ( difference > 0 ) {
                change = -1;
            } else {
                change = 1;
            }

            int i = 0;
            while ( difference != 0 ) {
                BudgetItem item = (BudgetItem)fBudgetItems.elementAt(i);
                item.setPercentage (item.getPercentage()+change);
                difference += change;

                i++;
                if ( i == numberOfBudgetItems() ) i = 0;
            }
        }

    }

    public void addBudgetItem (BudgetItem item)
    {
        fBudgetItems.addElement (item);
    }

    public void removeBudgetItem (BudgetItem item)
    {
        fBudgetItems.removeElement (item);
    }

    public BudgetItem findBudgetItem (Investment investment)
    {
        for (int i=0; i<fBudgetItems.size(); i++) {
            BudgetItem item = (BudgetItem)fBudgetItems.elementAt (i);
            if ( item.getInvestment() == investment ) {
                return item;
            }
        }
        return null;
    }

    public BudgetItem findBudgetItem (String name)
    {
        for (int i=0; i<fBudgetItems.size(); i++) {
            BudgetItem item = (BudgetItem)fBudgetItems.elementAt (i);
            if ( item.getName().equals(name) ) {
                return item;
            }
        }
        return null;
    }

    public BudgetSummary createSummary ()
    {
        int numItems = fBudgetItems.size ();
        String[] names = new String [numItems];
        double[] percentages = new double [numItems];

        for (int i=0; i<numItems; i++) {
            BudgetItem item = (BudgetItem)fBudgetItems.elementAt(i);
            names[i] = item.getName ();
            percentages[i] = ((double)item.getPercentage())/100.0;
        }

        return new BudgetSummary (numItems, names, percentages);
    }


    public void update (BudgetSummary summary)
    {
        String[] names = summary.getNames ();

        for (int i=0; i<Array.getLength(names); i++) {
            BudgetItem item = findBudgetItem (names[i]);
            if ( item != null ) {
                item.setPercentage ( (int)(summary.getPercentage(names[i])*100));
            }
        }

    }

    public String toString ()
    {
        String s = getName() + " (";
        for (int i=0; i<fBudgetItems.size(); i++) {
            BudgetItem item = (BudgetItem)fBudgetItems.elementAt (i);
            s += item.toString () + ",";
        }
        s += ")";
        return s;
    }

}
