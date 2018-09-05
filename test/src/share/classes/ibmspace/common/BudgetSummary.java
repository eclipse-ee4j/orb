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

import java.lang.reflect.Array;


public class BudgetSummary implements java.io.Serializable
{
    private String[]    fNames;
    private double[]    fPercentages;


    public BudgetSummary (int numItems, String[] names, double[] percentages)
    {
        fNames = names;
        fPercentages = percentages;
    }

    public String[] getNames ()
    {
        return fNames;
    }

    public double getPercentage (String name)
    {
        for (int i=0; i<Array.getLength(fNames); i++) {
            if ( fNames[i].equals (name) ) {
                return fPercentages[i];
            }
        }
        return 0;
    }

    public void setPercentage (String name, double percentage)
    {
        for (int i=0; i<Array.getLength(fNames); i++) {
            if ( fNames[i].equals (name) ) {
                fPercentages[i] = percentage;
            }
        }
    }

    public String toString ()
    {
        String s = "(";
        int num = Array.getLength (fNames);
        for (int i=0; i<num; i++) {
            s += "(" + fNames[i] + "," + String.valueOf(fPercentages[i]) + ")" + ",";
        }
        s += ")";
        return s;
    }

}



