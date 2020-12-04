/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
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

package org.omg.CORBA.portable;
/**
 * The generated Java classes corresponding to valuetype IDL types
 * implement this interface. In other words, the Java mapping of
 * valuetype objects implement the ValueBase interface. The generated 
 * Java class for valuetype's shall provide an implementation of the 
 * ValueBase interface for the corresponding value type. 
 * For value types that are streamable (i.e. non-custom), 
 * the generated Java class shall also provide an implementation 
 * for the org.omg.CORBA.portable.Streamable interface. 
 * (CORBA::ValueBase is mapped to java.io.Serializable.)
 */
public interface ValueBase extends IDLEntity {
    /**
     * Provides truncatable repository ids.
     * @return a String array--list of truncatable repository ids.
     */
    String[] _truncatable_ids();
}

