/*
 * Copyright (c) 2020, Oracle and/or its affiliates.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.encoding;

/**
 * A derived class that can be serialized.
 */
class DerivedValue extends Value1 {
  boolean ready;
  byte aByte;
  short aShort;
}
