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

package com.sun.corba.ee.tool;

import org.glassfish.pfl.basic.tools.argparser.ArgParser;
import org.glassfish.pfl.basic.tools.argparser.DefaultValue;
import org.glassfish.pfl.basic.tools.argparser.Help;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextHelper;

/** This tool checks to see if an ORB is listening at the given host and port.
 * It can print out the round trip time, and do a series of pings, or a single ping.
 * <p>
 * A ping consists of constructing a corbaname URL for the NameService, and
 * narrowing the corresponding object reference to the name service.
 *
 * @author ken
 */
public class OrbPing {
    public static class IntervalTimer {
        long lastTime ;

        public void start() {
            lastTime = System.nanoTime() ;
        }

        /** Returns interval since last start() or interval() call in
         * microseconds.
         * @return Elapsed time in microseconds
         */
        public long interval() {
            final long current = System.nanoTime() ;
            final long diff = current - lastTime ;
            start() ;
            return diff/1000 ;
        }
    }

    private interface Args {
        @DefaultValue( "1" )
        @Help( "The number of times to repeat the ORB ping")
        int count() ;

        @DefaultValue( "localhost" )
        @Help( "The host running the ORB")
        String host() ;

        @DefaultValue( "3037")
        @Help( "The port on which the ORB listens for clear text requests")
        int port() ;

        @DefaultValue( "false" )
        @Help( "Display extra information, including timing information" )
        boolean verbose() ;
    }

    private static Args args ;
    private static ORB orb ;
    private static IntervalTimer timer = new IntervalTimer() ;

    private static void ping( String host, int port ) {
        final String url = String.format( "corbaname:iiop:1.2@%s:%d/NameService",
            host, port ) ;

        org.omg.CORBA.Object cobject = null ;
        try {
            timer.start() ;
            cobject = orb.string_to_object( url ) ;
        } catch (Exception exc) {
            msg( "Exception in string_to_object call: %s\n", exc ) ;
        } finally {
            if (args.verbose()) {
                msg( "string_to_object call took %d microseconds\n",
                    timer.interval() ) ;
            }
        }

        NamingContext nctx ;

        try {
            timer.start() ;
            nctx = NamingContextHelper.narrow(cobject);
        } catch (Exception exc) {
            msg( "Exception in naming narrow call: %s\n", exc ) ;
        } finally {
            if (args.verbose()) {
                msg( "naming narrow call took %d microseconds\n",
                    timer.interval() ) ;
            }
        }
    }

    private static void msg( String str, Object... args ) {
        System.out.printf( str, args ) ;
    }

    public static void main( String[] params ) {
        args = (new ArgParser( Args.class )).parse( params, Args.class ) ;

        try {
            timer.start() ;
            orb = ORB.init( params, null ) ;
        } catch (Exception exc) {
            msg( "Exception in ORB.init: %s\n", exc ) ;
        } finally {
            if (args.verbose()) {
                msg( "ORB.init call took %d microseconds\n", timer.interval() ) ;
            }
        }

        for (int ctr=0; ctr<args.count(); ctr++ ) {
            ping( args.host(), args.port() ) ;
        }
    }
}
