/*
 * Copyright (c) 1997, 2020, Oracle and/or its affiliates.
 * Copyright (c) 1997-1999 IBM Corp. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.corba.idl;

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




