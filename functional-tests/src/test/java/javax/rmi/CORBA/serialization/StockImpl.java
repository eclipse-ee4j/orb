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

//nk
import java.util.Random;
//nk

public class StockImpl extends Stock
{
    //nk
    private static Random random = new Random();
    private final static float MAX_VALUE = 67;
    //nk

    public StockImpl (String arg0)
    {
        //nk
        symbol = arg0;
        if (symbol.equals("Sun")) {
            current = 30.0f;
        } else {
            // generate random stock price between 20 and 60
            current = (float)(random.nextInt(40) + 20);
        }
        //nk
    }

    StockImpl() {}

    public float update()
    {
        //nk
        float change = ((float)(random.nextGaussian() * 1.0));
        if (symbol.equals("Sun") && current < MAX_VALUE - 5)
            change = Math.abs(change);      // what did you expect?

        float newCurrent = current + change;

        // don't allow stock price to step outside range
        if (newCurrent < 0 || newCurrent > MAX_VALUE)
            change = 0;

        current += change;

        return change;
        //nk
    }

}
