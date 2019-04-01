/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
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
 * An extension of <code>ValueBase</code> that is implemented by custom value types.
 */
public interface CustomValue extends ValueBase, CustomMarshal {

}
