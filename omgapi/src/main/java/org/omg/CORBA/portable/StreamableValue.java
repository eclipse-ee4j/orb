/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.omg.CORBA.portable;

/**
 * Defines the base type for all non-boxed IDL valuetypes that are not custom marshaled.
 *
 * All value types implement ValueBase either directly or indirectly by implementing either the StreamableValue or
 * CustomValue interface.
 *
 * @author OMG
 * @version 1.15 07/27/07
 */
public interface StreamableValue extends Streamable, ValueBase {

}
