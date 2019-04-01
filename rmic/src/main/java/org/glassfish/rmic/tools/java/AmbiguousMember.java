/*
 * Copyright (c) 1994, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.rmic.tools.java;

/**
 * This exception is thrown when a field reference is ambiguous.
 *
 * WARNING: The contents of this source file are not part of any supported API. Code that depends on them does so at its
 * own risk: they are subject to change or removal without notice.
 */
@SuppressWarnings("serial") // JDK implementation class
public class AmbiguousMember extends Exception {
    /**
     * The field that was not found
     */
    public MemberDefinition field1;
    public MemberDefinition field2;

    /**
     * Constructor
     */
    public AmbiguousMember(MemberDefinition field1, MemberDefinition field2) {
        super(field1.getName() + " + " + field2.getName());
        this.field1 = field1;
        this.field2 = field2;
    }
}
