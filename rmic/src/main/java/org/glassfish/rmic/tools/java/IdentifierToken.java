/*
 * Copyright (c) 1996, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.rmic.tools.java;

/**
 * Information about the occurrence of an identifier.
 * The parser produces these to represent name which cannot yet be
 * bound to field definitions.
 *
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 *
 * @see
 */

public
class IdentifierToken {
    long where;
    int modifiers;
    Identifier id;

    public IdentifierToken(long where, Identifier id) {
        this.where = where;
        this.id = id;
    }

    /** Use this constructor when the identifier is synthesized.
     * The location will be 0.
     */
    public IdentifierToken(Identifier id) {
        this.where = 0;
        this.id = id;
    }

    public IdentifierToken(long where, Identifier id, int modifiers) {
        this.where = where;
        this.id = id;
        this.modifiers = modifiers;
    }

    /** The source location of this identifier occurrence. */
    public long getWhere() {
        return where;
    }

    /** The identifier itself (possibly qualified). */
    public Identifier getName() {
        return id;
    }

    /** The modifiers associated with the occurrence, if any. */
    public int getModifiers() {
        return modifiers;
    }

    public String toString() {
        return id.toString();
    }

    /**
     * Return defaultWhere if id is null or id.where is missing (0).
     * Otherwise, return id.where.
     */
    public static long getWhere(IdentifierToken id, long defaultWhere) {
        return (id != null && id.where != 0) ? id.where : defaultWhere;
    }
}
