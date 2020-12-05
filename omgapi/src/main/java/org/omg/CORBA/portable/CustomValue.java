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

/**
 * Defines the base interface for all custom value types 
 * generated from IDL.
 *
 * All value types implement ValueBase either directly 
 * or indirectly by implementing either the StreamableValue 
 * or CustomValue interface.
 * @author OMG
 * @version 1.16 07/27/07
 */

package org.omg.CORBA.portable;

import org.omg.CORBA.CustomMarshal;
/**
 * An extension of <code>ValueBase</code> that is implemented by custom value 
 * types.
 */
public interface CustomValue extends ValueBase, CustomMarshal {

}

