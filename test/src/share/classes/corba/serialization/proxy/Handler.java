/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.serialization.proxy;

import java.io.Serializable;

import java.lang.reflect.*;

public final class Handler implements InvocationHandler, Serializable {

  public Handler() {
    super();
  }

  public final Object invoke(final Object proxy,
                             final Method method,
                             final Object[] args) throws Throwable {
         //System.out.println("From DynamicProxy InvokeHandler ..");
         //Doesn't matter what you return here
         return null;
    }

}

