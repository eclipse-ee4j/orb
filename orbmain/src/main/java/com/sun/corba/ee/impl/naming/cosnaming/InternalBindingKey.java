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

package com.sun.corba.ee.impl.naming.cosnaming;

import org.omg.CosNaming.NameComponent;

/**
 * Class InternalBindingKey implements the necessary wrapper code around the org.omg.CosNaming::NameComponent class to
 * implement the proper equals() method and the hashCode() method for use in a hash table. It computes the hashCode once
 * and stores it, and also precomputes the lengths of the id and kind strings for faster comparison.
 */
public class InternalBindingKey {
    // A key contains a name
    public NameComponent name;
    private int idLen;
    private int kindLen;
    private int hashVal;

    // Default Constructor
    public InternalBindingKey() {
    }

    // Normal constructor
    public InternalBindingKey(NameComponent n) {
        idLen = 0;
        kindLen = 0;
        setup(n);
    }

    // Setup the object
    protected void setup(NameComponent n) {
        this.name = n;
        // Precompute lengths and values since they will not change
        if (this.name.id != null) {
            idLen = this.name.id.length();
        }
        if (this.name.kind != null) {
            kindLen = this.name.kind.length();
        }
        hashVal = 0;
        if (idLen > 0)
            hashVal += this.name.id.hashCode();
        if (kindLen > 0)
            hashVal += this.name.kind.hashCode();
    }

    // Compare the keys by comparing name's id and kind
    public boolean equals(java.lang.Object o) {
        if (o == null)
            return false;
        if (o instanceof InternalBindingKey) {
            InternalBindingKey that = (InternalBindingKey) o;
            // Both lengths must match
            if (this.idLen != that.idLen || this.kindLen != that.kindLen) {
                return false;
            }
            // If id is set is must be equal
            if (this.idLen > 0 && this.name.id.equals(that.name.id) == false) {
                return false;
            }
            // If kind is set it must be equal
            if (this.kindLen > 0 && this.name.kind.equals(that.name.kind) == false) {
                return false;
            }
            // Must be the same
            return true;
        } else {
            return false;
        }
    }

    // Return precomputed value
    public int hashCode() {
        return this.hashVal;
    }
}
