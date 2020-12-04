/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
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
