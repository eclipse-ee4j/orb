/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.framework ;

import junit.extensions.RepeatedTest ;
import junit.framework.TestResult ;
import junit.framework.Test ;

public class TimedTest extends RepeatedTest 
{
    // Duration in nanoseconds
    private long duration ;

    public TimedTest( Test test, int reps )
    {
        super( test, reps ) ;
    }

    public void run( TestResult result )
    {
        long startTime = System.nanoTime() ;
        long stopTime = 0 ;
        try {
            super.run( result ) ;
        } finally {
            stopTime = System.nanoTime() ;
        }
        duration = stopTime - startTime ;
    }

    public long getDuration() 
    {
        return duration ;
    }
}


