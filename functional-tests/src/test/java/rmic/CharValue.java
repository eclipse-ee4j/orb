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

public class CharValue implements java.io.Serializable {

    char value;

    public CharValue (char value) {
        this.value = value;
    }

    public char getValue () throws java.rmi.RemoteException {
        return value;
    }

    public int hashCode () {
        return value;
    }

    public boolean equals (Object it) {
        boolean result = false;
        if (this == it) {
            result = true;
        } else if (it != null && it instanceof CharValue) {
            CharValue other = (CharValue) it;
            if (other.value == value) {
                result = true;
            }
        }
        return result;
    }
}

