/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.threadpool;

import com.sun.corba.ee.impl.threadpool.Exceptions;
import java.util.ArrayList;
import java.util.List;

/**
 * Interface to support thread state validation. The basic idea is that one or more validators can be registered with an
 * implementation of the TSV. The validators are executed whenever a thread is returned to the threadpool, For example,
 * a validator may check for unreleased locks or uncleared threadlocals. This is intended as a last-ditch backstop for
 * leaking resource problems.
 *
 * @author ken
 */
public class ThreadStateValidator {
    private static final Exceptions wrapper = Exceptions.self;

    private static final List<Runnable> validators = new ArrayList<Runnable>();

    private ThreadStateValidator() {
    }

    /**
     * Register a thread validator (represented as a Runnable). A validator may check for locks that should not be held,
     * check for threadlocals that should be cleared, or take any other action to check for resources that should not be
     * held once the thread is no longer needed, as signaled by the thread being returned to the threadpool.
     * <p>
     * A validator typically may take the following actions:
     * <ol>
     * <li>Check whether or not a resource has been released.
     * <li>Log any detected problems.
     * <li>Reclaim the resource.
     * </ol>
     * A validator should NOT throw an exception, as all exceptions thrown from a validator will be ignored.
     *
     * @param validator
     */
    public static void registerValidator(Runnable validator) {
        validators.add(validator);
    }

    /**
     * Execute all of the validators. Should only be called from the threadpool implementation.
     */
    public static void checkValidators() {
        for (Runnable run : validators) {
            try {
                run.run();
            } catch (Throwable thr) {
                wrapper.threadStateValidatorException(run, thr);
            }
        }
    }
}
