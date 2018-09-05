/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.connectioncache ;

import java.util.Random;

public class RandomDelay{
    Random random = new Random() ;

    private final int minDelay ;
    private final int maxDelay ;

    public RandomDelay( int minDelay, int maxDelay ) {
        if (minDelay < 0)
            throw new RuntimeException( "minDelay must be >= 0") ;
        if (maxDelay < minDelay)
            throw new RuntimeException( "maxDelay must be >= minDelay" ) ;

        this.minDelay = minDelay ;
        this.maxDelay = maxDelay ;
    }

    void randomWait() {
        int delay = minDelay ;
        if (maxDelay > minDelay) {
            delay = minDelay + random.nextInt( maxDelay-minDelay ) ;
        }

        if (delay > 0) {
            try {
                wait(delay);
            } catch (InterruptedException ex) {
                // ignore this
            }
        }
    }
}
