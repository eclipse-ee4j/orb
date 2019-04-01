/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.orb;

import java.util.Properties;

import com.sun.corba.ee.spi.orb.Operation;

public abstract class ParserActionBase implements ParserAction {
    private String propertyName;
    private boolean prefix;
    private Operation operation;
    private String fieldName;

    public int hashCode() {
        return propertyName.hashCode() ^ operation.hashCode() ^ fieldName.hashCode() ^ (prefix ? 0 : 1);
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (!(obj instanceof ParserActionBase))
            return false;

        ParserActionBase other = (ParserActionBase) obj;

        return propertyName.equals(other.propertyName) && prefix == other.prefix && operation.equals(other.operation) && fieldName.equals(other.fieldName);
    }

    public ParserActionBase(String propertyName, boolean prefix, Operation operation, String fieldName) {
        this.propertyName = propertyName;
        this.prefix = prefix;
        this.operation = operation;
        this.fieldName = fieldName;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public boolean isPrefix() {
        return prefix;
    }

    public String getFieldName() {
        return fieldName;
    }

    public abstract Object apply(Properties props);

    protected Operation getOperation() {
        return operation;
    }
}
