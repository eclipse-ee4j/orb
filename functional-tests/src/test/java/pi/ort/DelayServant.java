/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package pi.ort;

import IORInterceptorTest.*;    // for IDL

/**
 * Servant implementation for delayPOA. This is a special servant created for
 * ORT testing, it has one method that sleeps for the given time and notifies
 * ORTStateChangeEvaluator after completion with the notificationToken.
 */
public class DelayServant extends delayPOA {
    public void forInMillis( int timeInMillis, String notificationToken ) {
        try {
            System.out.println( "DelayServant.forInMillis() called with " +
                timeInMillis );
            System.out.flush( );
            Thread.sleep( timeInMillis );
            ORTStateChangeEvaluator.getInstance( ).notificationTokenFromDelayServant( notificationToken );
        } catch( Exception e ) {
            System.err.println( "The Thread.sleep() in DelayServant crashed " +
                e );
            e.printStackTrace( );
            System.exit( 1 );
        }
    }
}
