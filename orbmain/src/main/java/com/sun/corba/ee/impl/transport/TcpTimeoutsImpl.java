/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.transport;

import com.sun.corba.ee.spi.transport.TcpTimeouts;

import com.sun.corba.ee.spi.logging.ORBUtilSystemException ;

/**
 * @author Charlie Hunt
 * @author Ken Cavanaugh
 */
public class TcpTimeoutsImpl implements TcpTimeouts
{
    private static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    private final int initial_time_to_wait;
    private final int max_time_to_wait;
    private int backoff_factor;
    private final int max_single_wait_time;

    public TcpTimeoutsImpl( String args ) {
        String[] data = args.split( ":" ) ;
        if ((data.length < 3) || (data.length > 4)) {
            throw wrapper.badTimeoutDataLength();
        }

        initial_time_to_wait = parseArg( "initial_time_to_wait", data[0] ) ;
        max_time_to_wait     = parseArg( "max_time_to_wait", data[1] ) ;
        setBackoffFactor( parseArg( "backoff_factor", data[2] ) ) ;
        if (data.length == 4) {
            max_single_wait_time = parseArg("max_single_wait_time", data[3]);
        } else {
            max_single_wait_time = Integer.MAX_VALUE;
        }
    }

    public TcpTimeoutsImpl( int initial_time, int max_time, 
        int backoff_percent) {
        this( initial_time, max_time, backoff_percent, Integer.MAX_VALUE ) ;
    }

    public TcpTimeoutsImpl( int initial_time, int max_time, 
        int backoff_percent, int max_single_wait_time ) {
        this.initial_time_to_wait = initial_time;
        this.max_time_to_wait = max_time;
        setBackoffFactor( backoff_percent ) ;
        this.max_single_wait_time = max_single_wait_time ;
    }

    private void setBackoffFactor( int backoff_percent ) {
        // Avoiding floating point number. Timeout is obtained by
        // dividing by 100 after multiplying by backoff_factor.
        this.backoff_factor = 100 + backoff_percent;
    }

    private int parseArg( String name, String value ) {
        try {
            int result = Integer.parseInt( value ) ;
            if (result <= 0) {
                throw wrapper.badTimeoutStringData(value, name);
            }
            return result ;
        } catch (NumberFormatException exc) {
            throw wrapper.badTimeoutStringData( exc, value, name ) ;
        }
    }

    public int get_initial_time_to_wait() { return initial_time_to_wait; }

    public int get_max_time_to_wait() { return max_time_to_wait; }

    public int get_backoff_factor() { return backoff_factor; }

    public int get_max_single_wait_time() { return max_single_wait_time; }

    public Waiter waiter() {
        return new Waiter() {
            // Use long so that arithmetic works correctly if
            // max_single_wait_time is set to Integer.MAX_VALUE.
            private long current_wait = initial_time_to_wait ;
            private long total_time = 0 ;

            public void advance() {
                if (current_wait != max_single_wait_time) {
                    current_wait = (current_wait * backoff_factor) / 100 ;
                    if (current_wait > max_single_wait_time) {
                        current_wait = max_single_wait_time;
                    }
                }
            }

            public void reset() {
                current_wait = initial_time_to_wait ;
            }

            public int getTime() {
                return (int)current_wait ;
            }

            public int getTimeForSleep() {
                int result = (int)current_wait ;
                if (total_time < max_time_to_wait) {
                    total_time += current_wait;
                }
                return result ;
            }

            public int timeWaiting() {
                return (int)total_time ;
            }

            public boolean sleepTime() {
                if (isExpired()) {
                    return false;
                }

                try {
                    Thread.sleep( getTimeForSleep() ) ;
                    return true ;
                } catch (InterruptedException exc) {
                    // this happens so rarely that we will
                    // ignore it.  Just log at FINE level.
                    wrapper.interruptedExceptionInTimeout() ;
                }

                // actually unreachable, but the compiler doesn't know that
                return false ;
            }

            public boolean isExpired() {
                return total_time >= max_time_to_wait ;
            }
        } ;
    }

    @Override
    public String toString() {
        return "TcpTimeoutsImpl[" 
            + initial_time_to_wait + ":"
            + max_time_to_wait + ":" 
            + backoff_factor + ":"
            + max_single_wait_time + "]" ;
    }

    @Override
    public boolean equals( Object obj ) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof TcpTimeouts)) {
            return false;
        }

        TcpTimeouts other = (TcpTimeouts)obj ;

        return (initial_time_to_wait == other.get_initial_time_to_wait()) &&
            (max_time_to_wait == other.get_max_time_to_wait()) &&
            (backoff_factor == other.get_backoff_factor()) &&
            (max_single_wait_time == other.get_max_single_wait_time()) ;
    }

    @Override
    public int hashCode() {
        return initial_time_to_wait ^ max_time_to_wait ^
            backoff_factor ^ max_single_wait_time ;
    }
}

// End of file.
