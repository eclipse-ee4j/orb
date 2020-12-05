/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
 * Copyright (c) 1997-1999 IBM Corp. All rights reserved.
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
