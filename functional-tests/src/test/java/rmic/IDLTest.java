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

package rmic;

/*
 * @test
 */
public abstract class IDLTest extends RMICTest {

    /**
     * Return an array of fully qualified class names for which generation
     * should occur. Return empty array if none.
     */
    protected abstract String[] getGenerationClasses () throws Throwable;

    /**
     * Return the primary generator argument (e.g. "-iiop" or "-idl").
     */
    protected String getGeneratorArg () {
        return "-idl";
    }

    /**
     * Perform the test.
     */
    protected abstract void doTest () throws Throwable;
}
