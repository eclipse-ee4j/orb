/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package javax.rmi.CORBA.serialization;

public class StockDefaultFactory implements StockValueFactory {

  public Stock create (String arg0)
  {
    return new StockImpl (arg0);
  }

  public java.io.Serializable read_value (org.omg.CORBA_2_3.portable.InputStream is)
  {                 
    return is.read_value(new StockImpl ());
  }
}
