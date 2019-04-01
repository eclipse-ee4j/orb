/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.transport;

import com.sun.corba.ee.impl.transport.TcpTimeoutsImpl;

/**
 * This interface defines the ability to wait for a configurable time, applying an exponential backoff to increase the
 * time. The maximum single wait time can be bounded, as well as the maximum total wait time.
 */
public interface TcpTimeouts {
    /**
     * Return the initial time to wait on the first getTime or sleepTime call on a new Waiter instance.
     */
    int get_initial_time_to_wait();

    /**
     * Get the maximum total time a Waiter can exist before isExpired returns true. -1 if not used for this TcpTimeouts
     * instances.
     */
    int get_max_time_to_wait();

    /**
     * Get the maximum time a single sleepTime or getTime can taoke or return in an instance of Waiter. -1 if not used.
     */
    int get_max_single_wait_time();

    /**
     * Return the backoff factor, which is the percentage multiplier used to compute the next timeout in the Waiter.advance
     * method.
     */
    int get_backoff_factor();

    /**
     * Interface used to represent a series of timeout values using exponential backoff. Supports both a maximum total wait
     * time and a maximum single wait time.
     * <p>
     * The total wait time starts at 0 and is incremented by each call to getTimeForSleep or sleepTime. Once the total wait
     * time exceeds the maximum total wait time, isExpired returns true.
     * <p>
     * The timer also has a current wait time, which is returned by getTime and is the interval for which sleep waits. The
     * initial value of the current wait time is get_initial_time_to_wait(). Each subsequent call to advance increases the
     * current wait time by a factor of (previous*get_backoff_factor())/100, unless get_max_single_wait_time is configured
     * and the current wait time exceeds get_max_single_wait_time(). If get_max_single_wait_time() is not used, the current
     * time increases without bound (until it overflows). Once get_max_single_wait_time() is reached, every subsequent call
     * to next() returnes get_max_single_wait_time(), and advance has no effect.
     */
    public interface Waiter {
        /**
         * Advance to the next timeout value.
         */
        void advance();

        /**
         * Set the current timeout back to the initial value. Accumulated time is not affected.
         */
        void reset();

        /**
         * Return the current timeout value. Also increments total time.
         */
        int getTimeForSleep();

        /**
         * Return the current timeout value, but do not increment total wait time.
         */
        int getTime();

        /**
         * Return the accumulated wait time.
         */
        int timeWaiting();

        /**
         * Sleep for the current timeout value. Returns true if sleep happened, otherwise false, in the case where the Waiter
         * has expired.
         */
        boolean sleepTime();

        /**
         * Returns true if the waiter has expired. It expires once the total wait time exceeds get_max_wait_time.
         */
        boolean isExpired();
    }

    /**
     * Return a Waiter that can be used for computing a series of timeouts.
     */
    Waiter waiter();

    /**
     * Factory used to create TcpTimeouts instances.
     */
    public interface Factory {
        /**
         * Create TcpTimeouts assuming that max_single_wait is unbounded.
         */
        TcpTimeouts create(int initial_time_to_wait, int max_time_to_wait, int backoff_value);

        /**
         * Create TcpTimeouts using all configuration parameters, including a bound on the maximum single wait time.
         */
        TcpTimeouts create(int initial_time_to_wait, int max_time_to_wait, int backoff_value, int max_single_wait);

        /**
         * Create TcpTimeouts from a configuration string. args must be a : separated string, with 3 or 4 args, all of which are
         * positive decimal integers. The integers are in the same order as the arguments to the other create methods.
         */
        TcpTimeouts create(String args);
    }

    Factory factory = new Factory() {
        public TcpTimeouts create(int initial_time_to_wait, int max_time_to_wait, int backoff_value) {

            return new TcpTimeoutsImpl(initial_time_to_wait, max_time_to_wait, backoff_value);
        }

        public TcpTimeouts create(int initial_time_to_wait, int max_time_to_wait, int backoff_value, int max_single_wait) {

            return new TcpTimeoutsImpl(initial_time_to_wait, max_time_to_wait, backoff_value, max_single_wait);
        }

        public TcpTimeouts create(String args) {
            return new TcpTimeoutsImpl(args);
        }
    };
}

// End of file.
