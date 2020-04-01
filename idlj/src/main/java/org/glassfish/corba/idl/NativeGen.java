/*
 * Copyright (c) 1997, 2020, Oracle and/or its affiliates.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.corba.idl;

// NOTES:

import java.io.PrintWriter;
import java.util.Hashtable;

public interface NativeGen extends Generator
{
  void generate (Hashtable symbolTable, NativeEntry entry, PrintWriter stream);
} // interface NativeGen

