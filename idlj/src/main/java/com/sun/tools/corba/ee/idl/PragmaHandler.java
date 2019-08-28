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
// - Add openScope and closeScope.

import java.io.IOException;

public abstract class PragmaHandler
{
  public abstract boolean process (String pragma, String currentToken) throws IOException;

  void init (com.sun.tools.corba.ee.idl.Preprocessor p)
  {
    preprocessor = p;
  } // init

  // Utility methods.

  /** Get the current token.
   * @return the current token
   */
  protected String currentToken ()
  {
    return preprocessor.currentToken ();
  } // currentToken

  /** This method, given an entry name, returns the entry with that name.
      It can take fully or partially qualified names and returns the
      appropriate entry defined within the current scope.  If no entry
      exists, null is returned.
    * @param string entry name to look for
    * @return entry with that name or {@code null} if no entry
    */
  protected SymtabEntry getEntryForName (String string)
  {
    return preprocessor.getEntryForName (string);
  } // getEntryForName

  /** This method returns a string of all of the characters from the input
      file from the current position up to, but not including, the end-of-line
      character(s).
    * @return String from current position
    */
  protected String getStringToEOL () throws IOException
  {
    return preprocessor.getStringToEOL ();
  } // getStringToEOL

  /** This method returns a string of all of the characters from the input
      file from the current position up to, but not including, the given
      character.  It encapsulates parenthesis and quoted strings, meaning
      it does not stop if the given character is found within parentheses
      or quotes.  For instance, given the input of `start(inside)end',
      getUntil ('n') will return "start(inside)e"
    * @param c token to read up to. The token itself will not be read
    */
  protected String getUntil (char c) throws IOException
  {
    return preprocessor.getUntil (c);
  } // getUntil

  /** 
   * This method returns the next token String from the input file.
   * @return the next token String
   */
  protected String nextToken () throws IOException
  {
    return preprocessor.nextToken ();
  } // nextToken

  /** This method assumes that the current token marks the beginning
      of a scoped name.  It then parses the subsequent identifier and
      double colon tokens, builds the scoped name, and finds the symbol
      table entry with that name.
    * @return a SymtabEntry of the name
    */
  protected SymtabEntry scopedName () throws IOException
  {
    return preprocessor.scopedName ();
  } // scopedName

  /** Skip to the end of the line. */
  protected void skipToEOL () throws IOException
  {
    preprocessor.skipToEOL ();
  } // skipToEOL

  /** This method skips the data in the input file until the specified
    * character is encountered, then it returns the next token.
    * @param c token to indicate end of skipping
    */
  protected String skipUntil (char c) throws IOException
  {
    return preprocessor.skipUntil (c);
  } // skipUntil

  /** This method displays a Parser Exception complete with line number
      and position information with the given message string.
    * @param message message to display as part of the Exception
    * @see Exception#getMessage()
    */
  protected void parseException (String message)
  {
    preprocessor.parseException (message);
  } // parseException

  /** This method is called when the parser encounters a left curly brace.
      An extender of PragmaHandler may find scope information useful.
      For example, the prefix pragma takes effect as soon as it is
      encountered and stays in effect until the current scope is closed.
      If a similar pragma extension is desired, then the openScope and
      closeScope methods are available for overriding.
      @param entry the symbol table entry whose scope has just been opened.
       Be aware that, since the scope has just been entered, this entry is
       incomplete at this point.  */
  protected void openScope (SymtabEntry entry)
  {
  } // openScope

  /** This method is called when the parser encounters a right curly brace.
      An extender of PragmaHandler may find scope information useful.
      For example, the prefix pragma takes effect as soon as it is
      encountered and stays in effect until the current scope is closed.
      If a similar pragma extension is desired, then the openScope and
      closeScope methods are available for overriding.
      @param entry the symbol table entry whose scope has just been closed. */
  protected void closeScope (SymtabEntry entry)
  {
  } // closeScope

  private com.sun.tools.corba.ee.idl.Preprocessor preprocessor = null;
} // class PragmaHandler
