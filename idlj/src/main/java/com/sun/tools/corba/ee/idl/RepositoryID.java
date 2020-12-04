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
// -D57110<daz> Add method to verify format (CORBA 2.3).

public class RepositoryID
{
  public RepositoryID ()
  {
    _id = "";
  } // ctor

  public RepositoryID (String id)
  {
    _id = id;
  } // ctor

  public String ID ()
  {
    return _id;
  } // ID

  public Object clone ()
  {
    return new RepositoryID (_id);
  } // clone

  public String toString ()
  {
    return ID ();
  } // toString

  /**
   * Determine is a supplied string meets the minimal format requirement
   * for a Repository ID.
   * @param string String  to check for validity
   * @return true iff supplied string has form '&lt;format&gt;:&lt;string&gt;', where
   * &lt;format&gt; is any non-empty string not containing ':'.
   **/
  public static boolean hasValidForm (String string)
  {
    return string != null && string.indexOf (':') > 0;
  } // hasValidForm

  private String _id;
} // class RepositoryID
