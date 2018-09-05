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
   * @return true iff supplied string has form '&lt;format&gt;:&lt;string&gt;', where
   * &lt;format&gt; is any non-empty string not containing ':'.
   **/
  public static boolean hasValidForm (String string)
  {
    return string != null && string.indexOf (':') > 0;
  } // hasValidForm

  private String _id;
} // class RepositoryID
