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

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * A serializable class that implements writeObject for custom marshalling but uses the standard read object.
 */
public class CustomWriteClass implements Serializable {
  int aPositiveValue;


  private void writeObject(ObjectOutputStream aOutputStream) throws IOException {
    aPositiveValue = Math.min(aPositiveValue, 1);
    aOutputStream.defaultWriteObject();
  }

}
