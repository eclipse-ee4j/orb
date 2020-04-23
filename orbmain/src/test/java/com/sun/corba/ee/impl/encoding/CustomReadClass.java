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
import java.io.ObjectInputStream;
import java.io.Serializable;

/**
 * A serializable class that implements readObject for custom marshalling but uses the standard write object.
 */
public class CustomReadClass implements Serializable {
  int aPositiveValue;

  private void readObject(ObjectInputStream anInputStream) throws IOException, ClassNotFoundException {
    anInputStream.defaultReadObject();
    aPositiveValue = Math.max(aPositiveValue, 1);
  }

}
