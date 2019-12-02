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
// -D57110<daz> Allow ID pragma directive to be applied to modules and update
//  feature in accordance to CORBA 2.3.
// -D59165<daz> Enable escaped identifiers when processing pragmas.
// -f60858.1<daz> Support -corba option, level = 2.2: Accept identifiers that
//  collide with keywords, in letter but not case, and issue a warning.
// -d62023 <daz> support -noWarn option; suppress inappropriate warnings when
//  parsing IBM-specific pragmas (#meta <interface_name> abstract).

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;

/**
 * This class should be extended if new pragmas are desired.  If the
 * preprocessor encounters a pragma name which it doesn't recognize
 * (anything other than ID, prefix, or version), it calls the method
 * otherPragmas.  This is the only method which need be overridden.
 * The Preprocessor base class has a number of utility-like methods
 * which can be used by the overridden otherPragmas method.
 **/
public class Preprocessor
{
  /**
   * Public zero-argument constructor.
   **/
  Preprocessor ()
  {
  } // ctor

  /**
   *
   **/
  void init (com.sun.tools.corba.ee.idl.Parser p)
  {
    parser  = p;
    symbols = p.symbols;
    macros  = p.macros;
  } // init

  @Override
  protected Object clone ()
  {
    return new Preprocessor ();
  } // clone

  /**
   *
   **/
  com.sun.tools.corba.ee.idl.Token process (com.sun.tools.corba.ee.idl.Token t) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    token   = t;
    scanner = parser.scanner;
    // <f46082.40> Deactivate escaped identifier processing in Scanner while
    // preprocessing.
    //scanner.underscoreOK = true;
    scanner.escapedOK = false;
    try
    {
      switch (token.type)
      {
        case com.sun.tools.corba.ee.idl.Token.Include:
          include ();
          break;
        case com.sun.tools.corba.ee.idl.Token.If:
          ifClause ();
          break;
        case com.sun.tools.corba.ee.idl.Token.Ifdef:
          ifdef (false);
          break;
        case com.sun.tools.corba.ee.idl.Token.Ifndef:
          ifdef (true);
          break;
        case com.sun.tools.corba.ee.idl.Token.Else:
          if (alreadyProcessedABranch.empty ())
            throw com.sun.tools.corba.ee.idl.ParseException.elseNoIf(scanner);
          else if ((alreadyProcessedABranch.peek ()).booleanValue ())
            skipToEndif ();
          else
          {
            alreadyProcessedABranch.pop ();
            alreadyProcessedABranch.push (true);
            token = scanner.getToken ();
          }
          break;
        case com.sun.tools.corba.ee.idl.Token.Elif:
          elif ();
          break;
        case com.sun.tools.corba.ee.idl.Token.Endif:
          if (alreadyProcessedABranch.empty ())
            throw com.sun.tools.corba.ee.idl.ParseException.endNoIf(scanner);
          else
          {
            alreadyProcessedABranch.pop ();
            token = scanner.getToken ();
            break;
          }
        case com.sun.tools.corba.ee.idl.Token.Define:
          define ();
          break;
        case com.sun.tools.corba.ee.idl.Token.Undef:
          undefine ();
          break;
        case com.sun.tools.corba.ee.idl.Token.Pragma:
          pragma ();
          break;
        case com.sun.tools.corba.ee.idl.Token.Unknown:
          if (!parser.noWarn)
            com.sun.tools.corba.ee.idl.ParseException.warning (scanner, com.sun.tools.corba.ee.idl.Util.getMessage("Preprocessor.unknown", token.name));
        case com.sun.tools.corba.ee.idl.Token.Error:
        case com.sun.tools.corba.ee.idl.Token.Line:
        case com.sun.tools.corba.ee.idl.Token.Null:
          // ignore
        default:
          scanner.skipLineComment ();
          token = scanner.getToken ();
      }
    }
    catch (IOException e)
    {
      // <f46082.40> Underscore may now precede any identifier, so underscoreOK
      // is vestigal.  The Preprocessor must reset escapedOK so that Scanner
      // will process escaped identifiers according to specification.
      //scanner.underscoreOK = false;
      scanner.escapedOK = true;
      throw e;
    }
    catch (com.sun.tools.corba.ee.idl.ParseException e)
    {
      // <f46082.40> See above.
      //scanner.underscoreOK = false;
      scanner.escapedOK = true;
      throw e;
    }
    // <f46082.40> See above.
    //scanner.underscoreOK = false;
    scanner.escapedOK = true;
    return token;
  } // process

  /**
   *
   **/
  private void include () throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    match (com.sun.tools.corba.ee.idl.Token.Include);
    IncludeEntry include = parser.stFactory.includeEntry (parser.currentModule);
    include.sourceFile (scanner.fileEntry ());
    scanner.fileEntry ().addInclude (include);
    if (token.type == com.sun.tools.corba.ee.idl.Token.StringLiteral)
      include2 (include);
    else if (token.type == com.sun.tools.corba.ee.idl.Token.LessThan)
      include3 (include);
    else
    {
      int[] expected = {com.sun.tools.corba.ee.idl.Token.StringLiteral, com.sun.tools.corba.ee.idl.Token.LessThan};
      throw com.sun.tools.corba.ee.idl.ParseException.syntaxError(scanner, expected, token.type);
    }
    if (parser.currentModule instanceof com.sun.tools.corba.ee.idl.ModuleEntry)
      ((com.sun.tools.corba.ee.idl.ModuleEntry)parser.currentModule).addContained (include);
    else if (parser.currentModule instanceof com.sun.tools.corba.ee.idl.InterfaceEntry)
      ((com.sun.tools.corba.ee.idl.InterfaceEntry)parser.currentModule).addContained (include);
  } // include

  /**
   *
   **/
  private void include2 (IncludeEntry include) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    include.name ('"' + token.name + '"');
    include4 (include, token.name);
    match (com.sun.tools.corba.ee.idl.Token.StringLiteral);
  } // include2

  /**
   *
   **/
  private void include3 (IncludeEntry include) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    if (token.type != com.sun.tools.corba.ee.idl.Token.LessThan)
      // match will throw an exception
      match (com.sun.tools.corba.ee.idl.Token.LessThan);
    else
    {
      try
      {
        String includeFile = getUntil ('>');
        token = scanner.getToken ();
        include.name ('<' + includeFile + '>');
        include4 (include, includeFile);
        match (com.sun.tools.corba.ee.idl.Token.GreaterThan);
      }
      catch (IOException e)
      {
        throw com.sun.tools.corba.ee.idl.ParseException.syntaxError(scanner, ">", "EOF");
      }
    }
  } // include3

  /**
   *
   **/
  private void include4 (IncludeEntry include, String filename) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    try
    {
      // If the #include is at the global scope, it is treated as
      // an import statement.  If it is within some other scope, it
      // is treated as a normal #include.
      boolean includeIsImport = parser.currentModule == parser.topLevelModule;
      //daz
      include.absFilename (com.sun.tools.corba.ee.idl.Util.getAbsolutePath(filename, parser.paths));
      scanner.scanIncludedFile (include, getFilename (filename), includeIsImport);
    }
    catch (IOException e)
    {
      com.sun.tools.corba.ee.idl.ParseException.generic(scanner, e.toString());
    }
  } // include4

  /**
   *
   **/
  private void define () throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    match (com.sun.tools.corba.ee.idl.Token.Define);
    if (token.equals (com.sun.tools.corba.ee.idl.Token.Identifier))
    {
      String symbol = scanner.getStringToEOL ();
      symbols.put (token.name, symbol.trim ());
      match (com.sun.tools.corba.ee.idl.Token.Identifier);
    }
    else if (token.equals (com.sun.tools.corba.ee.idl.Token.MacroIdentifier))
    {
      symbols.put (token.name, '(' + scanner.getStringToEOL () . trim ());
      macros.addElement (token.name);
      match (com.sun.tools.corba.ee.idl.Token.MacroIdentifier);
    }
    else
      throw com.sun.tools.corba.ee.idl.ParseException.syntaxError (scanner, com.sun.tools.corba.ee.idl.Token.Identifier, token.type);
  } // define

  /**
   *
   **/
  private void undefine () throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    match (com.sun.tools.corba.ee.idl.Token.Undef);
    if (token.equals (com.sun.tools.corba.ee.idl.Token.Identifier))
    {
      symbols.remove (token.name);
      macros.removeElement (token.name);
      match (com.sun.tools.corba.ee.idl.Token.Identifier);
    }
    else
      throw com.sun.tools.corba.ee.idl.ParseException.syntaxError (scanner, com.sun.tools.corba.ee.idl.Token.Identifier, token.type);
  } // undefine

  /**
   *
   **/
  private void ifClause () throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    match (com.sun.tools.corba.ee.idl.Token.If);
    constExpr ();
  } // ifClause

  /**
   *
   **/
  private void constExpr () throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    com.sun.tools.corba.ee.idl.SymtabEntry dummyEntry = new com.sun.tools.corba.ee.idl.SymtabEntry(parser.currentModule);
    dummyEntry.container (parser.currentModule);
    parser.parsingConditionalExpr = true;
    com.sun.tools.corba.ee.idl.constExpr.Expression boolExpr = booleanConstExpr (dummyEntry);
    parser.parsingConditionalExpr = false;
    boolean expr;
    if (boolExpr.value () instanceof Boolean)
      expr = ((Boolean)boolExpr.value ());
    else
      expr = ((Number)boolExpr.value ()).longValue () != 0;
    alreadyProcessedABranch.push (expr);
    if (!expr)
      skipToEndiforElse ();
  } // constExpr

  /**
   *
   **/
  com.sun.tools.corba.ee.idl.constExpr.Expression booleanConstExpr (com.sun.tools.corba.ee.idl.SymtabEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    com.sun.tools.corba.ee.idl.constExpr.Expression expr = orExpr (null, entry);
    try
    {
      expr.evaluate ();
    }
    catch (com.sun.tools.corba.ee.idl.constExpr.EvaluationException e)
    {
      com.sun.tools.corba.ee.idl.ParseException.evaluationError(scanner, e.toString());
    }
    return expr;
  } // booleanConstExpr

  /**
   *
   **/
  private com.sun.tools.corba.ee.idl.constExpr.Expression orExpr (com.sun.tools.corba.ee.idl.constExpr.Expression e, com.sun.tools.corba.ee.idl.SymtabEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    if (e == null)
      e = andExpr (null, entry);
    else
    {
      com.sun.tools.corba.ee.idl.constExpr.BinaryExpr b = (com.sun.tools.corba.ee.idl.constExpr.BinaryExpr)e;
      b.right (andExpr (null, entry));
      e.rep (e.rep () + b.right ().rep ());
    }
    if (token.equals (com.sun.tools.corba.ee.idl.Token.DoubleBar))
    {
      match (token.type);
      com.sun.tools.corba.ee.idl.constExpr.BooleanOr or = parser.exprFactory.booleanOr (e, null);
      or.rep (e.rep () + " || ");
      return orExpr (or, entry);
    }
    else
      return e;
  } // orExpr

  /**
   *
   **/
  private com.sun.tools.corba.ee.idl.constExpr.Expression andExpr (com.sun.tools.corba.ee.idl.constExpr.Expression e, com.sun.tools.corba.ee.idl.SymtabEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    if (e == null)
      e = notExpr (entry);
    else
    {
      com.sun.tools.corba.ee.idl.constExpr.BinaryExpr b = (com.sun.tools.corba.ee.idl.constExpr.BinaryExpr)e;
      b.right (notExpr (entry));
      e.rep (e.rep () + b.right ().rep ());
    }
    if (token.equals (com.sun.tools.corba.ee.idl.Token.DoubleAmpersand))
    {
      match (token.type);
      com.sun.tools.corba.ee.idl.constExpr.BooleanAnd and = parser.exprFactory.booleanAnd (e, null);
      and.rep (e.rep () + " && ");
      return andExpr (and, entry);
    }
    else
      return e;
  } // andExpr

  /**
   *
   **/
  private com.sun.tools.corba.ee.idl.constExpr.Expression notExpr (/*boolean alreadySawExclamation, */com.sun.tools.corba.ee.idl.SymtabEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    com.sun.tools.corba.ee.idl.constExpr.Expression e;
    if (token.equals (com.sun.tools.corba.ee.idl.Token.Exclamation))
    {
      match (com.sun.tools.corba.ee.idl.Token.Exclamation);
      e = parser.exprFactory.booleanNot (definedExpr (entry));
      e.rep ("!" + ((com.sun.tools.corba.ee.idl.constExpr.BooleanNot)e).operand ().rep ());
    }
    else
      e = definedExpr (entry);
    return e;
  } // notExpr

  /**
   *
   **/
  private com.sun.tools.corba.ee.idl.constExpr.Expression definedExpr (com.sun.tools.corba.ee.idl.SymtabEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    if (token.equals (com.sun.tools.corba.ee.idl.Token.Identifier) && token.name.equals ("defined"))
      match (com.sun.tools.corba.ee.idl.Token.Identifier);
    return equalityExpr (null, entry);
  } // definedExpr

  /**
   *
   **/
  private com.sun.tools.corba.ee.idl.constExpr.Expression equalityExpr (com.sun.tools.corba.ee.idl.constExpr.Expression e, com.sun.tools.corba.ee.idl.SymtabEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    if (e == null)
    {
      parser.token = token; // Since parser to parse, give it this token
      e = parser.constExp (entry);
      token = parser.token; // Since parser last parsed, get its token
    }
    else
    {
      com.sun.tools.corba.ee.idl.constExpr.BinaryExpr b = (com.sun.tools.corba.ee.idl.constExpr.BinaryExpr)e;
      parser.token = token; // Since parser to parse, give it this token
      com.sun.tools.corba.ee.idl.constExpr.Expression constExpr = parser.constExp (entry);
      token = parser.token; // Since parser last parsed, get its token
      b.right (constExpr);
      e.rep (e.rep () + b.right ().rep ());
    }
    if (token.equals (com.sun.tools.corba.ee.idl.Token.DoubleEqual))
    {
      match (token.type);
      com.sun.tools.corba.ee.idl.constExpr.Equal eq = parser.exprFactory.equal (e, null);
      eq.rep (e.rep () + " == ");
      return equalityExpr (eq, entry);
    }
    else if (token.equals (com.sun.tools.corba.ee.idl.Token.NotEqual))
    {
      match (token.type);
      com.sun.tools.corba.ee.idl.constExpr.NotEqual n = parser.exprFactory.notEqual (e, null);
      n.rep (e.rep () + " != ");
      return equalityExpr (n, entry);
    }
    else if (token.equals (com.sun.tools.corba.ee.idl.Token.GreaterThan))
    {
      match (token.type);
      com.sun.tools.corba.ee.idl.constExpr.GreaterThan g = parser.exprFactory.greaterThan (e, null);
      g.rep (e.rep () + " > ");
      return equalityExpr (g, entry);
    }
    else if (token.equals (com.sun.tools.corba.ee.idl.Token.GreaterEqual))
    {
      match (token.type);
      com.sun.tools.corba.ee.idl.constExpr.GreaterEqual g = parser.exprFactory.greaterEqual (e, null);
      g.rep (e.rep () + " >= ");
      return equalityExpr (g, entry);
    }
    else if (token.equals (com.sun.tools.corba.ee.idl.Token.LessThan))
    {
      match (token.type);
      com.sun.tools.corba.ee.idl.constExpr.LessThan l = parser.exprFactory.lessThan (e, null);
      l.rep (e.rep () + " < ");
      return equalityExpr (l, entry);
    }
    else if (token.equals (com.sun.tools.corba.ee.idl.Token.LessEqual))
    {
      match (token.type);
      com.sun.tools.corba.ee.idl.constExpr.LessEqual l = parser.exprFactory.lessEqual (e, null);
      l.rep (e.rep () + " <= ");
      return equalityExpr (l, entry);
    }
    else
      return e;
  } // equalityExpr

  /**
   *
   **/
  com.sun.tools.corba.ee.idl.constExpr.Expression primaryExpr (com.sun.tools.corba.ee.idl.SymtabEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    com.sun.tools.corba.ee.idl.constExpr.Expression primary = null;
    switch (token.type)
    {
      case com.sun.tools.corba.ee.idl.Token.Identifier:
        // If an identifier gets this far, it means that no
        // preprocessor variable was defined with that name.
        // Generate a FALSE boolean expr.
        //daz        primary = parser.exprFactory.terminal ("0", new Long (0));
        primary = parser.exprFactory.terminal ("0", BigInteger.valueOf (0));
        token = scanner.getToken ();
        break;
      case com.sun.tools.corba.ee.idl.Token.BooleanLiteral:
      case com.sun.tools.corba.ee.idl.Token.CharacterLiteral:
      case com.sun.tools.corba.ee.idl.Token.IntegerLiteral:
      case com.sun.tools.corba.ee.idl.Token.FloatingPointLiteral:
      case com.sun.tools.corba.ee.idl.Token.StringLiteral:
        //daz        primary = parser.literal ();
        primary = parser.literal (entry);
        token = parser.token;
        break;
      case com.sun.tools.corba.ee.idl.Token.LeftParen:
        match (com.sun.tools.corba.ee.idl.Token.LeftParen);
        primary = booleanConstExpr (entry);
        match (com.sun.tools.corba.ee.idl.Token.RightParen);
        primary.rep ('(' + primary.rep () + ')');
        break;
      default:
        int[] expected = {com.sun.tools.corba.ee.idl.Token.Literal, com.sun.tools.corba.ee.idl.Token.LeftParen};
        throw com.sun.tools.corba.ee.idl.ParseException.syntaxError(scanner, expected, token.type);
    }
    return primary;
  } // primaryExpr

  /**
   *
   **/
  private void ifDefine (boolean inParens, boolean not) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    if (token.equals (com.sun.tools.corba.ee.idl.Token.Identifier))
      if ((not && symbols.containsKey (token.name)) || (!not && !symbols.containsKey (token.name)))
      {
        alreadyProcessedABranch.push (false);
        skipToEndiforElse ();
      }
      else
      {
        alreadyProcessedABranch.push (true);
        match (com.sun.tools.corba.ee.idl.Token.Identifier);
        if (inParens)
          match (com.sun.tools.corba.ee.idl.Token.RightParen);
      }
    else
      throw com.sun.tools.corba.ee.idl.ParseException.syntaxError (scanner, com.sun.tools.corba.ee.idl.Token.Identifier, token.type);
  } // ifDefine

  /**
   *
   **/
  private void ifdef (boolean not) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    if (not)
      match (com.sun.tools.corba.ee.idl.Token.Ifndef);
    else
      match (com.sun.tools.corba.ee.idl.Token.Ifdef);
    if (token.equals (com.sun.tools.corba.ee.idl.Token.Identifier))
      if ((not && symbols.containsKey (token.name)) || (!not && !symbols.containsKey (token.name)))
      {
        alreadyProcessedABranch.push (false);
        skipToEndiforElse ();
      }
      else
      {
        alreadyProcessedABranch.push (true);
        match (com.sun.tools.corba.ee.idl.Token.Identifier);
      }
    else
      throw com.sun.tools.corba.ee.idl.ParseException.syntaxError (scanner, com.sun.tools.corba.ee.idl.Token.Identifier, token.type);
  } // ifdef

  /**
   *
   **/
  private void elif () throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    if (alreadyProcessedABranch.empty ()) {
      throw com.sun.tools.corba.ee.idl.ParseException.elseNoIf(scanner);
  } else if ((alreadyProcessedABranch.peek ()).booleanValue ()) {
      skipToEndif ();
    } else
    {
      match (com.sun.tools.corba.ee.idl.Token.Elif);
      constExpr ();
    }
  } // elif

  /**
   *
   **/
  private void skipToEndiforElse () throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    while (!token.equals (com.sun.tools.corba.ee.idl.Token.Endif) && !token.equals (com.sun.tools.corba.ee.idl.Token.Else) && !token.equals (com.sun.tools.corba.ee.idl.Token.Elif))
    {
      if (token.equals (com.sun.tools.corba.ee.idl.Token.Ifdef) || token.equals (com.sun.tools.corba.ee.idl.Token.Ifndef))
      {
        alreadyProcessedABranch.push (true);
        skipToEndif ();
      }
      else
        token = scanner.skipUntil ('#');
    }
    process (token);
  } // skipToEndiforElse

  /**
   *
   **/
  private void skipToEndif () throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    while (!token.equals (com.sun.tools.corba.ee.idl.Token.Endif))
    {
      token = scanner.skipUntil ('#');
      if (token.equals (com.sun.tools.corba.ee.idl.Token.Ifdef) || token.equals (com.sun.tools.corba.ee.idl.Token.Ifndef))
      {
        alreadyProcessedABranch.push (true);
        skipToEndif ();
      }
    }
    alreadyProcessedABranch.pop ();
    match (com.sun.tools.corba.ee.idl.Token.Endif);
  } // skipToEndif

  ///////////////
  // For Pragma

  /**
   *
   **/
  private void pragma () throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    match (com.sun.tools.corba.ee.idl.Token.Pragma);
    String pragmaType = token.name;

    // <d59165> Enable escaped identifiers while processing pragma internals.
    // Don't enable until scanning pragma name!
    scanner.escapedOK = true;
    match (com.sun.tools.corba.ee.idl.Token.Identifier);

    // Add pragma entry to container
    com.sun.tools.corba.ee.idl.PragmaEntry pragmaEntry = parser.stFactory.pragmaEntry (parser.currentModule);
    pragmaEntry.name (pragmaType);
    pragmaEntry.sourceFile (scanner.fileEntry ());
    pragmaEntry.data (scanner.currentLine ());
    if (parser.currentModule instanceof com.sun.tools.corba.ee.idl.ModuleEntry)
      ((com.sun.tools.corba.ee.idl.ModuleEntry)parser.currentModule).addContained (pragmaEntry);
    else if (parser.currentModule instanceof com.sun.tools.corba.ee.idl.InterfaceEntry)
      ((com.sun.tools.corba.ee.idl.InterfaceEntry)parser.currentModule).addContained (pragmaEntry);

    // If the token was an identifier, then pragmaType WILL be non-null.
    if (pragmaType.equals ("ID"))
      idPragma ();
    else if (pragmaType.equals ("prefix"))
      prefixPragma ();
    else if (pragmaType.equals ("version"))
      versionPragma ();
    
    // we are adding extensions to the Sun's idlj compiler to
    // handle correct code generation for local Objects, where
    // the OMG is taking a long time to formalize stuff.  Good
    // example of this is poa.idl.  Two proprietory pragmas
    // sun_local and sun_localservant are defined.  sun_local
    // generates only Holder and Helper classes, where read
    // and write methods throw marshal exceptions.  sun_localservant
    // is to generate Helper, Holder, and only Skel with _invoke
    // throwing an exception, since it does not make sense for
    // local objects.

    else if (pragmaType.equals ("sun_local"))
      localPragma();
    else if (pragmaType.equals ("sun_localservant"))
      localServantPragma();
    else
    {
      otherPragmas (pragmaType, tokenToString ());
      token = scanner.getToken ();
    }

    scanner.escapedOK = false; // <d59165> Disable escaped identifiers.
  } // pragma

  // <d57110> Pragma ID can be appiled to modules and it is an error to
  // name a type in more than one ID pragma directive.

  private final Vector<SymtabEntry> PragmaIDs = new Vector<>();

  private void localPragma () throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    // Before I can use a parser method, I must make sure it has the current token.
    parser.token = token;
    // this makes sense only for interfaces, if specified for modules,
    // parser should throw an error
    com.sun.tools.corba.ee.idl.SymtabEntry anErrorOccurred = new com.sun.tools.corba.ee.idl.SymtabEntry();
    com.sun.tools.corba.ee.idl.SymtabEntry entry = parser.scopedName (parser.currentModule, anErrorOccurred);
    // Was the indicated type found in the symbol table?
    if (entry == anErrorOccurred)
    {
        System.out.println("Error occured ");
      // Don't have to generate an error, scopedName already has.
      scanner.skipLineComment ();
      token = scanner.getToken ();
    }
    else
    {
      // by this time we have already parsed the ModuleName and the
      // pragma type, therefore setInterfaceType
      if (entry instanceof com.sun.tools.corba.ee.idl.InterfaceEntry) {
          com.sun.tools.corba.ee.idl.InterfaceEntry ent = (com.sun.tools.corba.ee.idl.InterfaceEntry) entry;
          ent.setInterfaceType (com.sun.tools.corba.ee.idl.InterfaceEntry.LOCAL_SIGNATURE_ONLY);
      }
      token = parser.token;
      String string = token.name;
      match (com.sun.tools.corba.ee.idl.Token.StringLiteral);
      // for non-interfaces it doesn't make sense, so just ignore it
    }
  } // localPragma

  private void localServantPragma () throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    // Before I can use a parser method, I must make sure it has the current token.
    parser.token = token;
    // this makes sense only for interfaces, if specified for modules,
    // parser should throw an error
    com.sun.tools.corba.ee.idl.SymtabEntry anErrorOccurred = new com.sun.tools.corba.ee.idl.SymtabEntry();
    com.sun.tools.corba.ee.idl.SymtabEntry entry = parser.scopedName (parser.currentModule, anErrorOccurred);

    // Was the indicated type found in the symbol table?
    if (entry == anErrorOccurred)
    {
      // Don't have to generate an error, scopedName already has.
      scanner.skipLineComment ();
      token = scanner.getToken ();
        System.out.println("Error occured ");
    }
    else
    {
      // by this time we have already parsed the ModuleName and the
      // pragma type, therefore setInterfaceType
      if (entry instanceof com.sun.tools.corba.ee.idl.InterfaceEntry) {
          com.sun.tools.corba.ee.idl.InterfaceEntry ent = (com.sun.tools.corba.ee.idl.InterfaceEntry) entry;
          ent.setInterfaceType (com.sun.tools.corba.ee.idl.InterfaceEntry.LOCALSERVANT);
      }
      token = parser.token;
      String string = token.name;
      match (com.sun.tools.corba.ee.idl.Token.StringLiteral);
      // for non-interfaces it doesn't make sense, so just ignore it
    }
  } // localServantPragma


  /**
   *
   **/
  private void idPragma () throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    // Before I can use a parser method, I must make sure it has the current token.
    parser.token = token;

    // <d57110> This flag will relax the restriction that the scopedNamed
    // in this ID pragma directive cannot resolve to a module.
    parser.isModuleLegalType (true);
    com.sun.tools.corba.ee.idl.SymtabEntry anErrorOccurred = new com.sun.tools.corba.ee.idl.SymtabEntry();
    com.sun.tools.corba.ee.idl.SymtabEntry entry = parser.scopedName (parser.currentModule, anErrorOccurred);
    parser.isModuleLegalType (false);  // <57110>

    // Was the indicated type found in the symbol table?
    if (entry == anErrorOccurred)
    {
      // Don't have to generate an error, scopedName already has.
      scanner.skipLineComment ();
      token = scanner.getToken ();
    }
    // <d57110>
    //else if (PragmaIDs.contains (entry))
    //{
    //  ParseException.badRepIDAlreadyAssigned (scanner, entry.name ());
    //  scanner.skipLineComment ();
    //  token = scanner.getToken ();
    //}
    else
    {
      token = parser.token;
      String string = token.name;
      // Do not match token until after raise exceptions, otherwise
      // incorrect messages will be emitted!
      if (PragmaIDs.contains (entry)) // <d57110>
      {
        com.sun.tools.corba.ee.idl.ParseException.badRepIDAlreadyAssigned(scanner, entry.name());
      }
      else if (!com.sun.tools.corba.ee.idl.RepositoryID.hasValidForm(string)) // <d57110>
      {
        com.sun.tools.corba.ee.idl.ParseException.badRepIDForm(scanner, string);
      }
      else
      {
        entry.repositoryID (new com.sun.tools.corba.ee.idl.RepositoryID(string));
        PragmaIDs.addElement (entry); // <d57110>
      }
      match (com.sun.tools.corba.ee.idl.Token.StringLiteral);
    }
  } // idPragma

  /**
   *
   **/
  private void prefixPragma () throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    String string = token.name;
    match (com.sun.tools.corba.ee.idl.Token.StringLiteral);
    ((com.sun.tools.corba.ee.idl.IDLID)parser.repIDStack.peek ()).prefix (string);
    ((com.sun.tools.corba.ee.idl.IDLID)parser.repIDStack.peek ()).name ("");
  } // prefixPragma

  /**
   *
   **/
  private void versionPragma () throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    // Before I can use a parser method, I must make sure it has the current token.
    parser.token = token;
    // This flag will relax the restriction that the scopedNamed
    // in this Version pragma directive cannot resolve to a module.
    parser.isModuleLegalType (true);
    com.sun.tools.corba.ee.idl.SymtabEntry anErrorOccurred = new com.sun.tools.corba.ee.idl.SymtabEntry();
    com.sun.tools.corba.ee.idl.SymtabEntry entry = parser.scopedName (parser.currentModule, anErrorOccurred);
    // reset the flag to original value
    parser.isModuleLegalType (false);
    if (entry == anErrorOccurred)
    {
      // Don't have to generate an error, scopedName already has.
      scanner.skipLineComment ();
      token = scanner.getToken ();
    }
    else
    {
      token = parser.token;
      String string = token.name;
      match (com.sun.tools.corba.ee.idl.Token.FloatingPointLiteral);
      if (entry.repositoryID () instanceof com.sun.tools.corba.ee.idl.IDLID)
        ((com.sun.tools.corba.ee.idl.IDLID)entry.repositoryID ()).version (string);
    }
  } // versionPragma

  private final Vector<PragmaHandler> pragmaHandlers = new Vector ();

  /**
   *
   **/
  void registerPragma (com.sun.tools.corba.ee.idl.PragmaHandler handler)
  {
    pragmaHandlers.addElement (handler);
  } // registerPragma

  /**
   *
   **/
  private void otherPragmas (String pragmaType, String currentToken) throws IOException
  {
    for (int i = pragmaHandlers.size () - 1; i >= 0; --i)
    {
      com.sun.tools.corba.ee.idl.PragmaHandler handler = pragmaHandlers.elementAt (i);
      if (handler.process (pragmaType, currentToken))
                break;
    }
  } // otherPragmas

  /*
   * These protected methods are used by extenders, by the code
   * which implements otherPragma.
   */

  /**
   * Get the current token.
   * @return the current token
   **/
  String currentToken ()
  {
    return tokenToString ();
  } // currentToken

  /**
   * This method, given an entry name, returns the entry with that name.
   * It can take fully or partially qualified names and returns the
   * appropriate entry defined within the current scope.  If no entry
   * exists, null is returned.
   * @param string entry name to look for
   * @return entry with that name or {@code null} if no entry
   **/
  com.sun.tools.corba.ee.idl.SymtabEntry getEntryForName (String string)
  {
    boolean partialScope = false;
    boolean globalScope  = false;

    // Change all ::'s to /'s
    if (string.startsWith ("::"))
    {
      globalScope = true;
      string = string.substring (2);
    }
    int index = string.indexOf ("::");
    while (index >= 0)
    {
      partialScope = true;
      string = string.substring (0, index) + '/' + string.substring (index + 2);
      index = string.indexOf ("::");
    }

    // Get the entry for that string
    com.sun.tools.corba.ee.idl.SymtabEntry entry = null;
    if (globalScope)
      entry = parser.recursiveQualifiedEntry (string);
    else if (partialScope)
      entry = parser.recursivePQEntry (string, parser.currentModule);
    else
      entry = parser.unqualifiedEntryWMod (string, parser.currentModule);
    return entry;
  } // getEntryForName

  /**
   * This method returns a string of all of the characters from the
   * input file from the current position up to, but not including,
   * the end-of-line character(s).
   * @return String from current position
   **/
  String getStringToEOL () throws IOException
  {
    return scanner.getStringToEOL ();
  } // getStringToEOL

  /**
   * This method returns a string of all of the characters from the
   * input file from the current position up to, but not including,
   * the given character.  It encapsulates parenthesis and quoted strings,
   * meaning it does not stop if the given character is found within
   * parentheses or quotes.  For instance, given the input of
   * `start(inside)end', getUntil ('n') will return "start(inside)e"
   * @param c token to read up to. The token itself will not be read.
   **/
  String getUntil (char c) throws IOException
  {
    return scanner.getUntil (c);
  } // getUntil

  private boolean lastWasMacroID = false;

  /**
   *
   **/
  private String tokenToString ()
  {
    if (token.equals (com.sun.tools.corba.ee.idl.Token.MacroIdentifier))
    {
      lastWasMacroID = true;
      return token.name;
    }
    else if (token.equals (com.sun.tools.corba.ee.idl.Token.Identifier))
      return token.name;
    else
      return token.toString ();
  } // tokenToString

  /**
   * This method returns the next token String from the input file.
   * @return the next token String
   **/
  String nextToken () throws IOException
  {
    if (lastWasMacroID)
    {
      lastWasMacroID = false;
      return "(";
    }
    else
    {
      token = scanner.getToken ();
      return tokenToString ();
    }
  } // nextToken

  /**
   * This method assumes that the current token marks the beginning
   * of a scoped name.  It then parses the subsequent identifier and
   * double colon tokens, builds the scoped name, and finds the symbol
   * table entry with that name.
   * @return a SymtabEntry of the name
   **/
  com.sun.tools.corba.ee.idl.SymtabEntry scopedName () throws IOException
  {
    boolean     globalScope  = false;
    boolean     partialScope = false;
    String      name         = null;
    com.sun.tools.corba.ee.idl.SymtabEntry entry        = null;
    try
    {
      if (token.equals (com.sun.tools.corba.ee.idl.Token.DoubleColon))
        globalScope = true;
      else
      {
        if (token.equals (com.sun.tools.corba.ee.idl.Token.Object))
        {
          name = "Object";
          match (com.sun.tools.corba.ee.idl.Token.Object);
        }
        else if (token.type == com.sun.tools.corba.ee.idl.Token.ValueBase)
        {
          name = "ValueBase";
          match (com.sun.tools.corba.ee.idl.Token.ValueBase);
        }
        else
        {
          name = token.name;
          match (com.sun.tools.corba.ee.idl.Token.Identifier);
        }
      }
      while (token.equals (com.sun.tools.corba.ee.idl.Token.DoubleColon))
      {
        match (com.sun.tools.corba.ee.idl.Token.DoubleColon);
        partialScope = true;
        if (name != null)
          name = name + '/' + token.name;
        else
          name = token.name;
        match (com.sun.tools.corba.ee.idl.Token.Identifier);
      }
      if (globalScope)
        entry = parser.recursiveQualifiedEntry (name);
      else if (partialScope)
        entry = parser.recursivePQEntry (name, parser.currentModule);
      else
        entry = parser.unqualifiedEntryWMod (name, parser.currentModule);
    }
    catch (com.sun.tools.corba.ee.idl.ParseException e)
    {
      entry = null;
    }
    return entry;
  } // scopedName

  /**
   * Skip to the end of the line.
   **/
  void skipToEOL () throws IOException
  {
    scanner.skipLineComment ();
  } // skipToEOL

  /**
   * This method skips the data in the input file until the specified
   * character is encountered, then it returns the next token.
   * @param c token to indicate end of skipping
   **/
  String skipUntil (char c) throws IOException
  {
    if (!(lastWasMacroID && c == '('))
      token = scanner.skipUntil (c);
    return tokenToString ();
  } // skipUntil

  /**
   * This method displays a Parser Exception complete with line number
   * and position information with the given message string.
   * @param message message to display as part of the Exception
   * @see Exception#getMessage()
   **/
  void parseException (String message)
  {
    // <d62023> Suppress warnings
    if (!parser.noWarn)
      com.sun.tools.corba.ee.idl.ParseException.warning(scanner, message);
  } // parseException

  // For Pragma
  ///////////////
  // For macro expansion

  /**
   *
   **/
  String expandMacro (String macroDef, com.sun.tools.corba.ee.idl.Token t) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    token = t;
    // Get the parameter values from the macro 'call'
    Vector<String> parmValues = getParmValues ();

    // Get the parameter names from the macro definition
    // NOTE:  a newline character is appended here so that when
    // getStringToEOL is called, it stops scanning at the end
    // of this string.
    scanner.scanString (macroDef + '\n');
    Vector<String> parmNames = new Vector<>();
    macro (parmNames);

    if (parmValues.size () < parmNames.size ())
      throw com.sun.tools.corba.ee.idl.ParseException.syntaxError (scanner, com.sun.tools.corba.ee.idl.Token.Comma, com.sun.tools.corba.ee.idl.Token.RightParen);
    else if (parmValues.size () > parmNames.size ())
      throw com.sun.tools.corba.ee.idl.ParseException.syntaxError (scanner, com.sun.tools.corba.ee.idl.Token.RightParen, com.sun.tools.corba.ee.idl.Token.Comma);

    macroDef = scanner.getStringToEOL ();
    for (int i = 0; i < parmNames.size (); ++i)
      macroDef = replaceAll (macroDef, (String)parmNames.elementAt (i), (String)parmValues.elementAt (i));
    return removeDoublePound (macroDef);
  } // expandMacro

  // This method is only used by the macro expansion methods.
  /**
   *
   **/
  private void miniMatch (int type) throws com.sun.tools.corba.ee.idl.ParseException
  {
    // A normal production would now execute:
    // match (type);
    // But match reads the next token.  I don't want to do that now.
    // Just make sure the current token is a 'type'.
    if (!token.equals (type))
      throw com.sun.tools.corba.ee.idl.ParseException.syntaxError(scanner, type, token.type);
  } // miniMatch

  /**
   *
   **/
  private Vector<String> getParmValues () throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    Vector<String> values = new Vector<>();
    if (token.equals (com.sun.tools.corba.ee.idl.Token.Identifier))
    {
      match (com.sun.tools.corba.ee.idl.Token.Identifier);
      miniMatch (com.sun.tools.corba.ee.idl.Token.LeftParen);
    }
    else if (!token.equals (com.sun.tools.corba.ee.idl.Token.MacroIdentifier))
      throw com.sun.tools.corba.ee.idl.ParseException.syntaxError (scanner, com.sun.tools.corba.ee.idl.Token.Identifier, token.type);

    if (!token.equals (com.sun.tools.corba.ee.idl.Token.RightParen))
    {
      values.addElement (scanner.getUntil (',', ')').trim ());
      token = scanner.getToken ();
      macroParmValues (values);
    }
    return values;
  } // getParmValues

  private void macroParmValues(Vector<String> values) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    while (!token.equals (com.sun.tools.corba.ee.idl.Token.RightParen))
    {
      miniMatch (com.sun.tools.corba.ee.idl.Token.Comma);
      values.addElement (scanner.getUntil (',', ')').trim ());
      token = scanner.getToken ();
    }
  } // macroParmValues

  private void macro (Vector<String> parmNames) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    match (token.type);
    match (com.sun.tools.corba.ee.idl.Token.LeftParen);
    macroParms (parmNames);
    miniMatch (com.sun.tools.corba.ee.idl.Token.RightParen);
  } // macro

  private void macroParms (Vector<String> parmNames) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    if (!token.equals (com.sun.tools.corba.ee.idl.Token.RightParen))
    {
      parmNames.addElement (token.name);
      match (com.sun.tools.corba.ee.idl.Token.Identifier);
      macroParms2 (parmNames);
    }
  } // macroParms

  private void macroParms2 (Vector<String> parmNames) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    while (!token.equals (com.sun.tools.corba.ee.idl.Token.RightParen))
    {
      match (com.sun.tools.corba.ee.idl.Token.Comma);
      parmNames.addElement (token.name);
      match (com.sun.tools.corba.ee.idl.Token.Identifier);
    }
  } // macroParms2

  private String replaceAll (String string, String from, String to)
  {
    int index = 0;
    while (index != -1)
    {
      index = string.indexOf (from, index);
      if (index != -1)
      {
        if (!embedded (string, index, index + from.length ()))
          if (index > 0 && string.charAt(index) == '#')
            string = string.substring (0, index) + '"' + to + '"' + string.substring (index + from.length ());
          else
            string = string.substring (0, index) + to + string.substring (index + from.length ());
        index += to.length ();
      }
    }
    return string;
  } // replaceAll

  /**
   *
   **/
  private boolean embedded (String string, int index, int endIndex)
  {
    // Don't replace if found substring is not an independent id.
    // For example, don't replace "thither".indexOf ("it", 0)
    boolean ret    = false;
    char    preCh  = index == 0 ? ' ' : string.charAt (index - 1);
    char    postCh = endIndex >= string.length () - 1 ? ' ' : string.charAt (endIndex);
    if ((preCh >= 'a' && preCh <= 'z') || (preCh >= 'A' && preCh <= 'Z'))
      ret = true;
    else if ((postCh >= 'a' && postCh <= 'z') || (postCh >= 'A' && postCh <= 'Z') || (postCh >= '0' && postCh <= '9') || postCh == '_')
      ret = true;
    else
      ret = inQuotes (string, index);
    return ret;
  } // embedded

  /**
   *
   **/
  private boolean inQuotes (String string, int index)
  {
    int quoteCount = 0;
    for (int i = 0; i < index; ++i)
      if (string.charAt (i) == '"') ++quoteCount;
    // If there are an odd number of quotes before this region,
    // then this region is within quotes
    return quoteCount % 2 != 0;
  } // inQuotes

  /**
   * Remove any occurrences of ##.
   **/
  private String removeDoublePound (String string)
  {
    int index = 0;
    while (index != -1)
    {
      index = string.indexOf ("##", index);
      if (index != -1)
      {
        int startSkip = index - 1;
        int stopSkip  = index + 2;
        if (startSkip < 0)
          startSkip = 0;
        if (stopSkip >= string.length ())
          stopSkip = string.length () - 1;
        while (startSkip > 0 &&
               (string.charAt (startSkip) == ' ' ||
               string.charAt (startSkip) == '\t'))
          --startSkip;
        while (stopSkip < string.length () - 1 &&
               (string.charAt (stopSkip) == ' ' ||
               string.charAt (stopSkip) == '\t'))
          ++stopSkip;
        string = string.substring (0, startSkip + 1) + string.substring (stopSkip);
      }
    }
    return string;
  } // removeDoublePound

  // For macro expansion
  ///////////////

  /**
   *
   **/
  private String getFilename (String name) throws FileNotFoundException
  {
    String fullName = null;
    File file = new File (name);
    if (file.canRead ())
      fullName = name;
    else
    {
      Enumeration<String> pathList = parser.paths.elements ();
      while (!file.canRead () && pathList.hasMoreElements ())
      {
        fullName = pathList.nextElement () + File.separatorChar + name;
        file = new File (fullName);
      }
      if (!file.canRead ())
        throw new FileNotFoundException (name);
    }
    return fullName;
  } // getFilename

  /**
   *
   **/
  private void match (int type) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    if (!token.equals (type))
      throw com.sun.tools.corba.ee.idl.ParseException.syntaxError(scanner, type, token.type);
    token = scanner.getToken ();

    // <d62023> Added for convenience, but commented-out because there is
    // no reason to issue warnings for tokens scanned during preprocessing.
    // See issueTokenWarnings().
    //issueTokenWarnings ();

    //System.out.println ("Preprocessor.match token = " + token.type);
    //if (token.equals (Token.Identifier) || token.equals (Token.MacroIdentifier))
    //  System.out.println ("Preprocessor.match token name = " + token.name);

    // If the token is a defined thingy, scan the defined string
    // instead of the input stream for a while.
    if (token.equals (com.sun.tools.corba.ee.idl.Token.Identifier) || token.equals (com.sun.tools.corba.ee.idl.Token.MacroIdentifier))
    {
      String string = symbols.get (token.name);
      if (string != null && !string.equals (""))
        // If this is a macro, parse the macro
        if (macros.contains (token.name))
        {
          scanner.scanString (expandMacro (string, token));
          token = scanner.getToken ();
        }
        // else this is just a normal define
        else
        {
          scanner.scanString (string);
          token = scanner.getToken ();
        }
    }
  } // match

  /**
   * This method is called when the parser encounters a left curly brace.
   * An extender of PragmaHandler may find scope information useful.
   * For example, the prefix pragma takes effect as soon as it is
   * encountered and stays in effect until the current scope is closed.
   * If a similar pragma extension is desired, then the openScope and
   * closeScope methods are available for overriding.
   * @param entry the symbol table entry whose scope has just been opened.
   *  Be aware that, since the scope has just been entered, this entry is
   *  incomplete at this point.
   **/
  void openScope (com.sun.tools.corba.ee.idl.SymtabEntry entry)
  {
    for (int i = pragmaHandlers.size () - 1; i >= 0; --i)
    {
      com.sun.tools.corba.ee.idl.PragmaHandler handler = pragmaHandlers.elementAt (i);
      handler.openScope (entry);
    }
  } // openScope

  /**
   * This method is called when the parser encounters a right curly brace.
   * An extender of PragmaHandler may find scope information useful.
   * For example, the prefix pragma takes effect as soon as it is
   * encountered and stays in effect until the current scope is closed.
   * If a similar pragma extension is desired, then the openScope and
   * closeScope methods are available for overriding.
   * @param entry the symbol table entry whose scope has just been closed.
   **/
  void closeScope (com.sun.tools.corba.ee.idl.SymtabEntry entry)
  {
    for (int i = pragmaHandlers.size () - 1; i >= 0; --i)
    {
      com.sun.tools.corba.ee.idl.PragmaHandler handler = pragmaHandlers.elementAt (i);
      handler.closeScope (entry);
    }
  } // closeScope

  private com.sun.tools.corba.ee.idl.Parser parser;
  private com.sun.tools.corba.ee.idl.Scanner scanner;
  private Hashtable<String, String> symbols;
  private Vector<String>    macros;

  // The logic associated with this stack is scattered above.
  // A concise map of the logic is:
  // case #if false, #ifdef false, #ifndef true
  //   push (false);
  //   skipToEndifOrElse ();
  // case #if true, #ifdef true, #ifndef false
  //   push (true);
  // case #elif <conditional>
  //   if (top == true)
  //     skipToEndif ();
  //   else if (conditional == true)
  //     pop ();
  //     push (true);
  //   else if (conditional == false)
  //     skipToEndifOrElse ();
  // case #else
  //   if (top == true)
  //     skipToEndif ();
  //   else
  //     pop ();
  //     push (true);
  // case #endif
  //   pop ();
  private final  Stack<Boolean>  alreadyProcessedABranch = new Stack<Boolean> ();
                 com.sun.tools.corba.ee.idl.Token token;

  private static String indent = "";
}
