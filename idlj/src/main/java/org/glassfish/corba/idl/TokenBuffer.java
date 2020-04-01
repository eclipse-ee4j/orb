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

class TokenBuffer
{
  private final int DEFAULT_SIZE = 10;

  private int   _size      = 0;
  private Token _buffer [] = null;
  private int   _currPos   = -1;

  TokenBuffer ()
  {
    _size    = DEFAULT_SIZE;
    _buffer  = new Token[_size];
    _currPos = -1;
  } // ctor

  TokenBuffer (int size) throws Exception
  {
    _size    = size;   // _size == 0 is legal, but useless and problematic
    _buffer  = new Token[_size];
    _currPos = -1;
  } // ctor

  /** Inserts a token at the head of the buffer. */
  void insert (Token token)
  {
    // _size == 0 ==> ArithmeticException: divide by zero
    _currPos = ++_currPos % _size;
    _buffer [_currPos] = token;
  }

  /** Returns the token residing "i" elements from the head of the buffer. */
  Token lookBack (int i)
  {
    // Beware: i > _size ==> idx < 0 ==> ArrayOutOfBoundsException
    return _buffer [(_currPos - i) >= 0 ? _currPos - i : _currPos - i + _size];
  }

  /** Return the token most recently inserted into the buffer (i.e., the head of the buffer.) */
  Token current ()
  {
    // Beware: _buffer empty || _size == 0 ==> ArrayOutOfBoundsException
    return _buffer [_currPos];
  }
}   // class TokenBuffer


/*==================================================================================
  DATE<AUTHOR>   ACTION
  ----------------------------------------------------------------------------------
  11aug1997<daz> Initial version completed.  Buffer used to maintain history of
                 comments extracted from source file during parse.
  ==================================================================================*/

