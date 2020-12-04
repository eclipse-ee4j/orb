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
// -D62023<daz> Cast char to byte for JDK 1.2 compatability.

import java.io.IOException;

public class InvalidCharacter extends IOException
{
  public InvalidCharacter (String filename, String line, int lineNumber, int pos, char ch)
  {
    String pointer = "^";
    if (pos > 1)
    {
      byte[] bytes = new byte [pos - 1];
      for (int i = 0; i < pos - 1; ++i)
        bytes[i] = (byte)' ';  // <d62023>
      pointer = new String (bytes) + pointer;
    }
    String[] parameters = {filename, Integer.toString (lineNumber), "" + ch, Integer.toString ((int)ch), line, pointer};
    message = Util.getMessage("InvalidCharacter.1", parameters);
  }

  public String getMessage ()
  {
    return message;
  } // getMessage

  private String message = null;
} // class InvalidCharacter




