/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1997-1999 IBM Corp. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.tools.corba.ee.idl;

// NOTES:

/**
 * An invalid argument for the compiler has been encountered.
 **/
public class InvalidArgument extends Exception
{
  /** @param arg the invalid argument. */
  public InvalidArgument (String arg)
  {
    message = Util.getMessage("InvalidArgument.1", arg) + "\n\n" + Util.getMessage("usage");
  } // ctor

  public InvalidArgument ()
  {
    message = Util.getMessage("InvalidArgument.2") + "\n\n" + Util.getMessage("usage");
  } // ctor

  public String getMessage ()
  {
    return message;
  } // getMessage

  private String message = null;
} // class InvalidArgument
