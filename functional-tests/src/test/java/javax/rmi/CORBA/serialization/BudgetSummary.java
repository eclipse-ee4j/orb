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

package javax.rmi.CORBA.serialization;

import java.lang.reflect.Array;
import java.io.Serializable;

public class BudgetSummary implements Serializable
{
    //private String[][]  test = {{"One","Two"}};
    private String[]    fNames;
    private double[]    fPercentages;


    public BudgetSummary (int numItems, String[] names, double[] percentages)
    {
        fNames = names;
        fPercentages = percentages;
    }


}
