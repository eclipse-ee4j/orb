/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.encoding;

/**
 * Defines the methods on an input stream which provide a way to get and restore its internal state without violating
 * encapsulation.
 */
interface RestorableInputStream {
    Object createStreamMemento();

    void restoreInternalState(Object streamMemento);
}
