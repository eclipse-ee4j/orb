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


public class ShipSavings implements Investment, java.io.Serializable
{
    private long    fSavings;
    private double  fInterestRate;

    public ShipSavings (long initial)
    {
        fSavings = initial;
        fInterestRate = 0.07;
    }

    public ShipSavings ()
    {
        fSavings = 0;
        fInterestRate = 0.07;
    }

    public String getName ()
    {
        return "Ship Savings";
    }

    public void setInterestRate (double rate)
    {
        fInterestRate = rate;
    }

    public double getInterestRate ()
    {
        return fInterestRate;
    }

    public long computeInterest ()
    {
        return (long)(fSavings * fInterestRate);
    }

    public void invest (long investment)
    {
        fSavings += computeInterest ();
        fSavings += investment;
    }

    public long getSavings ()
    {
        return fSavings;
    }

    public void withdraw (long amount)
    {
        fSavings -= amount;
    }



}
