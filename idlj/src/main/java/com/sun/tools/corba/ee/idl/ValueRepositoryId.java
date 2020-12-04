/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1997-1999 IBM Corp. All rights reserved.
 * Copyright (c) 2019 Payara Services Ltd.
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

import java.security.MessageDigest;
import java.util.Hashtable;

/**
 *
 **/
public class ValueRepositoryId
{
  private MessageDigest sha;       // Message digest used to compute SHA-1
  private int           index;     // Current index in the 'logical' sequence
  private Hashtable<SymtabEntry, Integer>     types;     // Already processed types
  private String        hashcode;  // The computed hashcode

  public ValueRepositoryId ()
  {
    try
    {
      sha = MessageDigest.getInstance ("SHA-1");
    }
    catch (Exception exception)
    {}
    index    = 0;
    types    = new Hashtable<>();
    hashcode = null;
  } // ctor

  /**Add a value to the hashcode being computed.
     @param value the value to be added to the value RepositoryID. */
  public void addValue (int value)
  {
    sha.update ((byte)((value >> 24) & 0x0F));
    sha.update ((byte)((value >> 16) & 0x0F));
    sha.update ((byte)((value >>  8) & 0x0F));
    sha.update ((byte)(value & 0x0F));
    index++;
  } // addValue

  /** Add a type to the list of types which have already been included.
      Note that the type should be added prior to its value.
      @param entry the type to be added to the value RepositoryID. */
  public void addType (SymtabEntry entry)
  {
    types.put(entry, index);
  }

  /** Check to see if a specified type has already been processed. If so,
      add the appropriate 'previously processed' code (0xFFFFFFFF) and
      sequence offset, and return false; otherwise add the symbol table entry
      and current offset to the hashtable and return false.
      @param entry the type to be checked
      @return true if the symbol table entry has not been previously added;
       and false otherwise. */
  public boolean isNewType (SymtabEntry entry)
  {
    Integer index = types.get(entry);
    if (index == null)
    {
      addType (entry);
      return true;
    }
    addValue (0xFFFFFFFF);
    addValue(index);
    return false;
  } // isNewType

  /** Get the hashcode computed for the value type. This method MUST not be
      called until all fields have been added, since it computes the hash
      code from the values entered for each field.
      @return the 64 bit hashcode for the value type represented as a
       16 character hexadecimal string. */
  public String getHashcode ()
  {
    if (hashcode == null)
    {
      byte [] digest = sha.digest ();
      hashcode = hexOf (digest[0]) + hexOf (digest[1]) +
                 hexOf (digest[2]) + hexOf (digest[3]) +
                 hexOf (digest[4]) + hexOf (digest[5]) +
                 hexOf (digest[6]) + hexOf (digest[7]);
    }
    return hashcode;
  } // getHashCode

  // Convert a byte to a two character hex string:
  private static String hexOf (byte value)
  {
    int d1 = (value >> 4) & 0x0F;
    int d2 = value & 0x0F;
    return "0123456789ABCDEF".substring (d1, d1 + 1) +
           "0123456789ABCDEF".substring (d2, d2 + 1);
  } // hexOf
} // class ValueRepositoryId
