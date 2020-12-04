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
// -F46082.51<daz> Remove -stateful feature.
// -D52042<daz> Allow floating-point constants to be initialized with
//  integral as well as floating-point literals.  No other variations allowed.
// -D58058<daz> Set index array type to long rather than array element type.
// -D56351<daz> Update computation of RepositoryIDs to CORBA 2.3 (see spec.).
// -D57110<daz> Provide method() to set/clear ability for scoped names to
//  resolve to modules.  Allows rep. ids to be assigned to modules.
// -D46094<daz> Prohibit exceptions from appearing wihtin structs, unions, exceptions.
// -D46094<daz> Prohibit attributes from appearing as operation parameter types,
//  operation return types, attribute types.
// -D59067<daz> Prohibit nested value boxes.
// -D59166<daz> Prohibit collisions between keywords and non-escaped identifiers.
// -D59809<daz> At Pigeonhole(), add map short name of CORBA types to long name
//  (e.g., CORBA/StringValue --> org/omg/CORBA/StringValue), which allows fully-
//  qualified CORBA type names to resolve successfully.
// -F60858.1<daz> Support "-corba" option, level <= 2.2: issue warning for
//  keyowrd collisions;
// -D60942<daz> Prohibit operations from appearing within parameter types.
// -D61643<daz> Repair pigeonhole() to correctly filter bad RepIDs.
// -D62023<daz> Support -noWarn option; Issue warnings when tokens are
//  deprecated keywords or keywords in greater release version.
// -D61919<daz> Emit entries for modules originally opened in #include files
//  appearing at global scope and then reopened in the main IDL file.  Only
//  types appearing in the main IDL source will be emitted.

import java.io.EOFException;
import java.io.IOException;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;
import java.util.List ;
import java.util.ArrayList ;
import java.util.Iterator ;

import java.math.BigInteger;

/**
 *
 **/
class Parser
{
  /**
   *
   **/
  Parser (com.sun.tools.corba.ee.idl.Preprocessor preprocessor, com.sun.tools.corba.ee.idl.Arguments arguments, Hashtable overrides,
      Hashtable symtab, com.sun.tools.corba.ee.idl.SymtabFactory stFac, com.sun.tools.corba.ee.idl.constExpr.ExprFactory exprFac, String [] genKeywords)
  {
    this.arguments = arguments;
    noWarn         = arguments.noWarn; // <d62023>
    corbaLevel     = arguments.corbaLevel; // <f60858.1>
    paths          = arguments.includePaths;
    symbols        = arguments.definedSymbols;
    verbose        = arguments.verbose;
    emitAll        = arguments.emitAll;
    // <f46082.46.01>
    cppModule      = arguments.cppModule;
    // <f46082.51> Remove -stateful feature.
    //parseStateful  = arguments.parseStateful;
    overrideNames  = (overrides == null) ? new Hashtable<>() : overrides;
    symbolTable    = (symtab == null) ? new Hashtable<>() : symtab;
    keywords       = (genKeywords == null) ? new String [0] : genKeywords;
    stFactory      = stFac;
    exprFactory    = exprFac;
    currentModule  = topLevelModule = new com.sun.tools.corba.ee.idl.ModuleEntry();
    prep           = preprocessor;
    repIDStack.push (new com.sun.tools.corba.ee.idl.IDLID());
    addPrimEntries ();
  } // ctor

  /**
   *
   **/
  void parse (String file) throws IOException
  {
    IncludeEntry fileEntry = stFactory.includeEntry ();
    fileEntry.name ('"' + file + '"');
    try
    {
      // Set absolute file path
      fileEntry.absFilename (com.sun.tools.corba.ee.idl.Util.getAbsolutePath(file, paths));
    }
    catch (IOException ioe)
    {}

    // <f46082.51> Remove -stateful feature.
    //scanner = new Scanner (fileEntry, keywords, verbose, parseStateful, emitAll);
    // <f60585.1> Support "-corba [level]" option.
    //scanner = new Scanner (fileEntry, keywords, verbose, emitAll);
    scanner = new com.sun.tools.corba.ee.idl.Scanner(fileEntry, keywords, verbose, emitAll, corbaLevel,
        arguments.scannerDebugFlag );
    topLevelModule.sourceFile (fileEntry);

    // Prime the pump...
    // Match handles preprocessor directives, so use match to
    // call scanner.getToken just in case the first token is
    // such a directive.  But match depends on the token
    // already having a value, so fudge something.
    token = new com.sun.tools.corba.ee.idl.Token(0);
    tokenHistory.insert (token); // Initialize look back buffer <26jul1997daz>.
    try
    {
      match (0);
      if (token.equals (com.sun.tools.corba.ee.idl.Token.EOF))
        com.sun.tools.corba.ee.idl.ParseException.nothing(file);
      else
        specification (topLevelModule);
    }
    catch (com.sun.tools.corba.ee.idl.ParseException exception)  // Match MIGHT throw this
    {
      // It has already been reported, just end.
    }
    catch (EOFException exception)  // skipToSemicolon MIGHT throw this
    {
      // It has already been reported, just end.
    }
  } // parse

  /**
   *
   **/
  private void addPrimEntries ()
  {
    symbolTable.put ("short", stFactory.primitiveEntry ("short"));
    symbolTable.put ("long", stFactory.primitiveEntry ("long"));
    symbolTable.put ("long long", stFactory.primitiveEntry ("long long"));
    symbolTable.put ("unsigned short", stFactory.primitiveEntry ("unsigned short"));
    symbolTable.put ("unsigned long", stFactory.primitiveEntry ("unsigned long"));
    symbolTable.put ("unsigned long long", stFactory.primitiveEntry ("unsigned long long"));
    symbolTable.put ("char", stFactory.primitiveEntry ("char"));
    symbolTable.put ("wchar", stFactory.primitiveEntry ("wchar"));
    symbolTable.put ("float", stFactory.primitiveEntry ("float"));
    //Support fixed type: symbolTable.put ("fixed", stFactory.primitiveEntry ("fixed"));
    symbolTable.put ("double", stFactory.primitiveEntry ("double"));
    symbolTable.put ("boolean", stFactory.primitiveEntry ("boolean"));
    symbolTable.put ("octet", stFactory.primitiveEntry ("octet"));
    symbolTable.put ("any", stFactory.primitiveEntry ("any"));

    com.sun.tools.corba.ee.idl.InterfaceEntry object = stFactory.interfaceEntry();
    object.name ("Object");
    symbolTable.put ("Object", object);

    com.sun.tools.corba.ee.idl.ValueEntry valueBase = stFactory.valueEntry();
    valueBase.name ("ValueBase");
    symbolTable.put ("ValueBase", valueBase);

    // put these same entries in the lowercase symbol table
    lcSymbolTable.put ("short", stFactory.primitiveEntry ("short"));
    lcSymbolTable.put ("long", stFactory.primitiveEntry ("long"));
    lcSymbolTable.put ("long long", stFactory.primitiveEntry ("long long"));
    lcSymbolTable.put ("unsigned short", stFactory.primitiveEntry ("unsigned short"));
    lcSymbolTable.put ("unsigned long", stFactory.primitiveEntry ("unsigned long"));
    lcSymbolTable.put ("unsigned long long", stFactory.primitiveEntry ("unsigned long long"));
    lcSymbolTable.put ("char", stFactory.primitiveEntry ("char"));
    lcSymbolTable.put ("wchar", stFactory.primitiveEntry ("wchar"));
    lcSymbolTable.put ("float", stFactory.primitiveEntry ("float"));
    // Support fixed type: lcSymbolTable.put ("fixed", stFactory.primitiveEntry ("fixed"));
    lcSymbolTable.put ("double", stFactory.primitiveEntry ("double"));
    lcSymbolTable.put ("boolean", stFactory.primitiveEntry ("boolean"));
    lcSymbolTable.put ("octet", stFactory.primitiveEntry ("octet"));
    lcSymbolTable.put ("any", stFactory.primitiveEntry ("any"));
    lcSymbolTable.put ("object", object);
    lcSymbolTable.put ("valuebase", valueBase);
  } // addPrimEntries

  /**
   *
   **/
  private void specification (com.sun.tools.corba.ee.idl.ModuleEntry entry) throws IOException
  {
    while (!token.equals (com.sun.tools.corba.ee.idl.Token.EOF))
    {
      definition (entry);
      addToEmitList (entry);
    }
  } // specification

  // ModuleEntry is the topLevelModule; add its contained types to the emit list.
  /**
   *
   **/
  private void addToEmitList (com.sun.tools.corba.ee.idl.ModuleEntry entry)
  {
    for (Enumeration e = entry.contained ().elements (); e.hasMoreElements();)
    {
      com.sun.tools.corba.ee.idl.SymtabEntry emitEntry = (com.sun.tools.corba.ee.idl.SymtabEntry)e.nextElement ();
      if (emitEntry.emit ())
      {
        emitList.addElement (emitEntry);

        // <d61919> I think the absence of the following statement was an
        // oversight.  If module X.Y.Z first appears in an include file, then is
        // reopened in the main IDL source, this statement guarantees that X.Y.Z
        // definitions within the main IDL source are emitted.
        ///---------------------------------------------------------------------
        // If any of this module's elements should be emitted, add
        // this module to the emit list.
        if (emitEntry instanceof com.sun.tools.corba.ee.idl.ModuleEntry)
          checkContained ((com.sun.tools.corba.ee.idl.ModuleEntry)emitEntry);
        if (emitEntry instanceof IncludeEntry)
        {
          includes.addElement (emitEntry.name ());
          includeEntries.addElement ((IncludeEntry) emitEntry);
        }
      }
      else
        // If any of this module's elements should be emitted, add
        // this module to the emit list.
        if (emitEntry instanceof com.sun.tools.corba.ee.idl.ModuleEntry)
          checkContained ((com.sun.tools.corba.ee.idl.ModuleEntry)emitEntry);
    }
    entry.contained ().removeAllElements ();
  } // addToEmitList

  /**
   *
   **/
  private void checkContained (com.sun.tools.corba.ee.idl.ModuleEntry entry)
  {
    // If any of this module's elements is to be emitted,
    // then add the module to the emit list.
    for (Enumeration e = entry.contained ().elements (); e.hasMoreElements ();)
    {
      com.sun.tools.corba.ee.idl.SymtabEntry contained = (com.sun.tools.corba.ee.idl.SymtabEntry)e.nextElement ();
      if (contained instanceof com.sun.tools.corba.ee.idl.ModuleEntry)
        checkContained ((com.sun.tools.corba.ee.idl.ModuleEntry)contained);
      if (contained.emit ())
      {
        if (!emitList.contains (entry)) {
          emitList.addElement (entry);
        }
        entry.emit (true);
        break;
      }
    }
  } // checkContained

  /**
   *
   **/
  private void definition (com.sun.tools.corba.ee.idl.ModuleEntry entry) throws IOException
  {
    try
    {
      switch (token.type)
      {
        case com.sun.tools.corba.ee.idl.Token.Typedef:
        case com.sun.tools.corba.ee.idl.Token.Struct:
        case com.sun.tools.corba.ee.idl.Token.Union:
        case com.sun.tools.corba.ee.idl.Token.Enum:
          typeDcl (entry);
          break;
        case com.sun.tools.corba.ee.idl.Token.Const:
          constDcl (entry);
          break;
        case com.sun.tools.corba.ee.idl.Token.Native:
          nativeDcl (entry);
          break;
        case com.sun.tools.corba.ee.idl.Token.Exception:
          exceptDcl (entry);
          break;
        case com.sun.tools.corba.ee.idl.Token.Interface:
          interfaceProd (entry, com.sun.tools.corba.ee.idl.InterfaceEntry.NORMAL);
          break;
        case com.sun.tools.corba.ee.idl.Token.Local:
          match( com.sun.tools.corba.ee.idl.Token.Local ) ;
          if (token.type ==  com.sun.tools.corba.ee.idl.Token.Interface)
              interfaceProd( entry, com.sun.tools.corba.ee.idl.InterfaceEntry.LOCAL ) ;
          else
              throw com.sun.tools.corba.ee.idl.ParseException.syntaxError(scanner, new int[]{
                      com.sun.tools.corba.ee.idl.Token.Interface}, token.type) ;
          break ;
        case com.sun.tools.corba.ee.idl.Token.Module:
          module (entry);
          break;
        case com.sun.tools.corba.ee.idl.Token.Abstract:
          match (com.sun.tools.corba.ee.idl.Token.Abstract);
          if (token.type == com.sun.tools.corba.ee.idl.Token.Interface)
            interfaceProd (entry, com.sun.tools.corba.ee.idl.InterfaceEntry.ABSTRACT);
          else if (token.type == com.sun.tools.corba.ee.idl.Token.Valuetype)
            valueProd (entry, true);
          else
            throw com.sun.tools.corba.ee.idl.ParseException.syntaxError (scanner, new int[]{
                    com.sun.tools.corba.ee.idl.Token.Interface, com.sun.tools.corba.ee.idl.Token.Valuetype}, token.type);
          break;
        case com.sun.tools.corba.ee.idl.Token.Custom:
        case com.sun.tools.corba.ee.idl.Token.Valuetype:
          valueProd (entry, false);
          break;
        default:
          throw com.sun.tools.corba.ee.idl.ParseException.syntaxError (scanner, new int[]{
                  com.sun.tools.corba.ee.idl.Token.Typedef, com.sun.tools.corba.ee.idl.Token.Struct, com.sun.tools.corba.ee.idl.Token.Union, com.sun.tools.corba.ee.idl.Token.Enum,
                  com.sun.tools.corba.ee.idl.Token.Const, com.sun.tools.corba.ee.idl.Token.Exception, com.sun.tools.corba.ee.idl.Token.Interface, com.sun.tools.corba.ee.idl.Token.Valuetype,
                  com.sun.tools.corba.ee.idl.Token.Module}, token.type);
      }
      match (com.sun.tools.corba.ee.idl.Token.Semicolon);
    }
    catch (com.sun.tools.corba.ee.idl.ParseException e)
    {
      skipToSemicolon ();
    }
  } // definition

  /**
   *
   **/
  private void module (com.sun.tools.corba.ee.idl.ModuleEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    match (com.sun.tools.corba.ee.idl.Token.Module);
    repIDStack.push (((com.sun.tools.corba.ee.idl.IDLID)repIDStack.peek ()).clone ());
    com.sun.tools.corba.ee.idl.ModuleEntry newEntry = newModule (entry);
    ((com.sun.tools.corba.ee.idl.IDLID)repIDStack.peek ()).appendToName (newEntry.name ());
    // comment must immediately precede "module" keyword
    newEntry.comment (tokenHistory.lookBack (1).comment);
    currentModule = newEntry;
    match (com.sun.tools.corba.ee.idl.Token.Identifier);
    prep.openScope (newEntry);
    match (com.sun.tools.corba.ee.idl.Token.LeftBrace);
    definition (newEntry);
    while (!token.equals (com.sun.tools.corba.ee.idl.Token.EOF) && !token.equals (com.sun.tools.corba.ee.idl.Token.RightBrace))
      definition (newEntry);
    prep.closeScope (newEntry);
    match (com.sun.tools.corba.ee.idl.Token.RightBrace);
    currentModule = entry;
    repIDStack.pop ();
  } // module

  /**
   *
   **/
  private void interfaceProd (com.sun.tools.corba.ee.idl.ModuleEntry entry, int interfaceType)
      throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    match (com.sun.tools.corba.ee.idl.Token.Interface);
    String name = token.name;
    match (com.sun.tools.corba.ee.idl.Token.Identifier);
    interface2 (entry, name, interfaceType);
  } // interfaceProd

  /**
   *
   **/
  private void interface2 (com.sun.tools.corba.ee.idl.ModuleEntry module, String name, int interfaceType)
      throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    if (token.type == com.sun.tools.corba.ee.idl.Token.Colon || token.type == com.sun.tools.corba.ee.idl.Token.LeftBrace) {
        repIDStack.push (((com.sun.tools.corba.ee.idl.IDLID)repIDStack.peek ()).clone ());
        com.sun.tools.corba.ee.idl.InterfaceEntry entry = stFactory.interfaceEntry (module,
            (com.sun.tools.corba.ee.idl.IDLID)repIDStack.peek ());
        entry.sourceFile (scanner.fileEntry ());
        entry.name (name);
        entry.setInterfaceType(interfaceType);
        // Comment must immediately precede "[local | abstract] interface" keyword
        entry.comment (tokenHistory.lookBack (
            entry.getInterfaceType() == com.sun.tools.corba.ee.idl.InterfaceEntry.NORMAL ? 2 : 3).comment);

        if (!com.sun.tools.corba.ee.idl.ForwardEntry.replaceForwardDecl(entry))
            com.sun.tools.corba.ee.idl.ParseException.badAbstract(scanner, entry.fullName());
        pigeonhole (module, entry);
        ((com.sun.tools.corba.ee.idl.IDLID)repIDStack.peek ()).appendToName (name);
        currentModule = entry;
        interfaceDcl (entry);
        currentModule = module;
        repIDStack.pop ();
    } else  { // This is a forward declaration
        com.sun.tools.corba.ee.idl.ForwardEntry entry = stFactory.forwardEntry (module, (com.sun.tools.corba.ee.idl.IDLID)repIDStack.peek ());
        entry.sourceFile (scanner.fileEntry ());
        entry.name (name);
        entry.setInterfaceType(interfaceType);
        // comment must immediately precede "interface" keyword.
        entry.comment (tokenHistory.lookBack (
            entry.getInterfaceType() == com.sun.tools.corba.ee.idl.InterfaceEntry.NORMAL ? 2 : 3).comment);
        pigeonhole (module, entry);
    }
  } // interface2

  /**
   *
   **/
  private void interfaceDcl (com.sun.tools.corba.ee.idl.InterfaceEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    if (token.type != com.sun.tools.corba.ee.idl.Token.LeftBrace)
        inheritanceSpec (entry);
    else if (!entry.isAbstract ()) {
        com.sun.tools.corba.ee.idl.SymtabEntry objectEntry = qualifiedEntry ("Object");
        com.sun.tools.corba.ee.idl.SymtabEntry realOEntry  = typeOf (objectEntry);
        if (objectEntry == null)
            ;  // qualifiedEntry already generated an error message
        else if (!isInterface(realOEntry))
            com.sun.tools.corba.ee.idl.ParseException.wrongType(scanner, overrideName("Object"),
                    "interface", objectEntry.typeName());
        else
            entry.derivedFromAddElement (realOEntry, scanner);
    }

    prep.openScope (entry);
    match (com.sun.tools.corba.ee.idl.Token.LeftBrace);
    while (token.type != com.sun.tools.corba.ee.idl.Token.RightBrace)
        export (entry);
    prep.closeScope (entry);
    match (com.sun.tools.corba.ee.idl.Token.RightBrace);
  } // interfaceDcl

  /**
   *
   **/
  private void export (com.sun.tools.corba.ee.idl.InterfaceEntry entry) throws IOException
  {
    try
    {
      switch (token.type)
      {
        case com.sun.tools.corba.ee.idl.Token.Typedef:
        case com.sun.tools.corba.ee.idl.Token.Struct:
        case com.sun.tools.corba.ee.idl.Token.Union:
        case com.sun.tools.corba.ee.idl.Token.Enum:
          typeDcl (entry);
          break;
        case com.sun.tools.corba.ee.idl.Token.Const:
          constDcl (entry);
          break;
        case com.sun.tools.corba.ee.idl.Token.Native:
          nativeDcl (entry);
          break;
        case com.sun.tools.corba.ee.idl.Token.Exception:
          exceptDcl (entry);
          break;
        case com.sun.tools.corba.ee.idl.Token.Readonly:
        case com.sun.tools.corba.ee.idl.Token.Attribute:
          attrDcl (entry);
          break;
        case com.sun.tools.corba.ee.idl.Token.Oneway:
        case com.sun.tools.corba.ee.idl.Token.Float:
        case com.sun.tools.corba.ee.idl.Token.Double:
        case com.sun.tools.corba.ee.idl.Token.Long:
        case com.sun.tools.corba.ee.idl.Token.Short:
        case com.sun.tools.corba.ee.idl.Token.Unsigned:
        case com.sun.tools.corba.ee.idl.Token.Char:
        case com.sun.tools.corba.ee.idl.Token.Wchar:
        case com.sun.tools.corba.ee.idl.Token.Boolean:
        case com.sun.tools.corba.ee.idl.Token.Octet:
        case com.sun.tools.corba.ee.idl.Token.Any:
        case com.sun.tools.corba.ee.idl.Token.String:
        case com.sun.tools.corba.ee.idl.Token.Wstring:
        case com.sun.tools.corba.ee.idl.Token.Identifier:
        case com.sun.tools.corba.ee.idl.Token.Object:
        // <f46082.40> Value base type.
        case com.sun.tools.corba.ee.idl.Token.ValueBase:
        case com.sun.tools.corba.ee.idl.Token.DoubleColon:
        case com.sun.tools.corba.ee.idl.Token.Void:
          opDcl (entry);
          break;
        // <f46082.51> Remove -stateful feature.
        //case Token.State:       if (parseStateful) {
        //                          stateDef (entry);
        //                          break; }
        default:
          throw com.sun.tools.corba.ee.idl.ParseException.syntaxError(scanner, new int[]{
                  com.sun.tools.corba.ee.idl.Token.Typedef, com.sun.tools.corba.ee.idl.Token.Struct, com.sun.tools.corba.ee.idl.Token.Union, com.sun.tools.corba.ee.idl.Token.Enum,
                  com.sun.tools.corba.ee.idl.Token.Const, com.sun.tools.corba.ee.idl.Token.Exception, com.sun.tools.corba.ee.idl.Token.Readonly, com.sun.tools.corba.ee.idl.Token.Attribute,
                  com.sun.tools.corba.ee.idl.Token.Oneway, com.sun.tools.corba.ee.idl.Token.Float, com.sun.tools.corba.ee.idl.Token.Double, com.sun.tools.corba.ee.idl.Token.Long,
                  com.sun.tools.corba.ee.idl.Token.Short, com.sun.tools.corba.ee.idl.Token.Unsigned, com.sun.tools.corba.ee.idl.Token.Char, com.sun.tools.corba.ee.idl.Token.Wchar,
                  com.sun.tools.corba.ee.idl.Token.Boolean, com.sun.tools.corba.ee.idl.Token.Octet, com.sun.tools.corba.ee.idl.Token.Any, com.sun.tools.corba.ee.idl.Token.String,
                  com.sun.tools.corba.ee.idl.Token.Wstring, com.sun.tools.corba.ee.idl.Token.Identifier, com.sun.tools.corba.ee.idl.Token.DoubleColon, com.sun.tools.corba.ee.idl.Token.Void,
                  com.sun.tools.corba.ee.idl.Token.ValueBase}, token.type);
      }
      match (com.sun.tools.corba.ee.idl.Token.Semicolon);
    }
    catch (com.sun.tools.corba.ee.idl.ParseException exception)
    {
      skipToSemicolon ();
    }
  } // export

  private void inheritanceSpec (com.sun.tools.corba.ee.idl.InterfaceEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    for (match (com.sun.tools.corba.ee.idl.Token.Colon); ; match (com.sun.tools.corba.ee.idl.Token.Comma)) {
        com.sun.tools.corba.ee.idl.SymtabEntry parent = scopedName (entry.container (),
            stFactory.interfaceEntry ());
        com.sun.tools.corba.ee.idl.SymtabEntry realParent = typeOf (parent);

        if (isInterfaceOnly (realParent)) {
            boolean isInterface = (realParent instanceof com.sun.tools.corba.ee.idl.InterfaceEntry);
            if (entry.derivedFrom ().contains (realParent))
                com.sun.tools.corba.ee.idl.ParseException.alreadyDerived(scanner, realParent.fullName(), entry.fullName());
            else if (!entry.isAbstract () || 
                (((com.sun.tools.corba.ee.idl.InterfaceType)realParent).getInterfaceType() == com.sun.tools.corba.ee.idl.InterfaceType.ABSTRACT))
                entry.derivedFromAddElement (realParent, scanner);
            else
                com.sun.tools.corba.ee.idl.ParseException.nonAbstractParent(scanner, entry.fullName(), parent.fullName());
        } else if (isForward( realParent )) {
            com.sun.tools.corba.ee.idl.ParseException.illegalForwardInheritance(scanner,
                    entry.fullName(), parent.fullName()) ;
        } else
            com.sun.tools.corba.ee.idl.ParseException.wrongType(scanner, parent.fullName(), "interface", entryName(parent));

        if ((parent instanceof com.sun.tools.corba.ee.idl.InterfaceEntry) && (((com.sun.tools.corba.ee.idl.InterfaceEntry)parent).state () != null))
            if (entry.state () == null)
                entry.initState ();
            else
                throw com.sun.tools.corba.ee.idl.ParseException.badState(scanner, entry.fullName());

        if (token.type != com.sun.tools.corba.ee.idl.Token.Comma)
            break;
    }
  } // inheritanceSpec

  // <57110> Member _moduleIsLegalType may be set by any feature to allow
  // method scopedName() and any of its helper methods -- qualifiedName(),
  // partlyQualifiedName(), and unqualifiedName() -- to return a ModuleEntry
  // rather than a parse error in the event a name resolves to a module.  The
  // flag must be cleared (set to false) to resume normal parsing behavior.
  //
  // Currently, this is used only when preprocessing the ID pragma directive.

  private boolean _isModuleLegalType = false;

  /**
   *
   **/
  public boolean isModuleLegalType ()
  {
    return _isModuleLegalType;
  }; // moduleIsLegaType

  /**
   *
   **/
  public void isModuleLegalType (boolean b)
  {
    _isModuleLegalType = b;
  }; // moduleIsLegalType

  /**
   *
   **/
  com.sun.tools.corba.ee.idl.SymtabEntry scopedName (com.sun.tools.corba.ee.idl.SymtabEntry container,
    com.sun.tools.corba.ee.idl.SymtabEntry expected) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    return scopedName( container, expected, true ) ;
  }

  com.sun.tools.corba.ee.idl.SymtabEntry scopedName (com.sun.tools.corba.ee.idl.SymtabEntry container, com.sun.tools.corba.ee.idl.SymtabEntry expected,
    boolean mustBeReferencable ) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    boolean globalScope  = false;
    boolean partialScope = false;
    String  name         = null;
    if (token.type == com.sun.tools.corba.ee.idl.Token.DoubleColon)
      globalScope = true;
    else
    {
      if (token.type == com.sun.tools.corba.ee.idl.Token.Object)
      {
        name = "Object";
        match (com.sun.tools.corba.ee.idl.Token.Object);
      }
      else if (token.type == com.sun.tools.corba.ee.idl.Token.ValueBase) // <f46082.40>
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
    while (token.type == com.sun.tools.corba.ee.idl.Token.DoubleColon)
    {
      match (com.sun.tools.corba.ee.idl.Token.DoubleColon);
      partialScope = true;
      if (name != null)
        name += '/' + token.name;
      else name = token.name;
        match (com.sun.tools.corba.ee.idl.Token.Identifier);
    }
    com.sun.tools.corba.ee.idl.SymtabEntry entry = null;
    if (globalScope)
      entry = qualifiedEntry (name);
    else if (partialScope)
      entry = partlyQualifiedEntry (name, container);
    else
      entry = unqualifiedEntry (name, container);

    if (entry == null)
      // Make the entry the expected entry.  The generators will
      // not be called now, since a semantic exception ocurred, but
      // the parse has to finish and something valid has to be
      // returned.
      (entry = expected).name (name);
    else if (!entry.isReferencable() && mustBeReferencable)
      throw com.sun.tools.corba.ee.idl.ParseException.illegalIncompleteTypeReference(scanner, name) ;

    return entry;
  } // scopedName

  private void valueProd (com.sun.tools.corba.ee.idl.ModuleEntry entry, boolean isAbstract) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    boolean isCustom = (token.type == com.sun.tools.corba.ee.idl.Token.Custom);
    if (isCustom)
      match (com.sun.tools.corba.ee.idl.Token.Custom);
    match (com.sun.tools.corba.ee.idl.Token.Valuetype);
    String name = token.name;
    match (com.sun.tools.corba.ee.idl.Token.Identifier);
  
    switch (token.type)
    {
      case com.sun.tools.corba.ee.idl.Token.LeftBrace:
      case com.sun.tools.corba.ee.idl.Token.Colon:
      case com.sun.tools.corba.ee.idl.Token.Supports:
        value2 (entry, name, isAbstract, isCustom);
        return;
      case com.sun.tools.corba.ee.idl.Token.Semicolon:
        if (isCustom)
          break;
        valueForwardDcl (entry, name, isAbstract);
        return;
    }
    if (isCustom)
      throw com.sun.tools.corba.ee.idl.ParseException.badCustom(scanner);
    if (isAbstract)
      throw com.sun.tools.corba.ee.idl.ParseException.abstractValueBox(scanner);
    valueBox (entry, name);
  }  // valueProd

  /**
   *
   **/
  private void value2 (com.sun.tools.corba.ee.idl.ModuleEntry module, String name, boolean isAbstract,
      boolean isCustom) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    repIDStack.push (((com.sun.tools.corba.ee.idl.IDLID)repIDStack.peek ()).clone ());
    // The 'actual' repository ID will be calculated at the end of the
    // parsing phase, since it is based on the entire contents of the
    // declaration, and needs to have all forward references resolved:
    com.sun.tools.corba.ee.idl.ValueEntry entry = stFactory.valueEntry (module, (com.sun.tools.corba.ee.idl.IDLID)repIDStack.peek ());
    entry.sourceFile (scanner.fileEntry ());
    entry.name (name);
    entry.setInterfaceType (isAbstract ? com.sun.tools.corba.ee.idl.InterfaceType.ABSTRACT : com.sun.tools.corba.ee.idl.InterfaceType.NORMAL);
    entry.setCustom (isCustom);
    // Comment must immediately precede "[abstract | custom] value" keyword
    entry.comment (tokenHistory.lookBack ((isAbstract || isCustom) ? 3 : 2).comment);
    // If this value has been forward declared, there are probably
    // other values which derive from a ForwardValueEntry.  Replace
    // those ForwardValueEntry's with this ValueEntry:
    if (!com.sun.tools.corba.ee.idl.ForwardEntry.replaceForwardDecl(entry))
      com.sun.tools.corba.ee.idl.ParseException.badAbstract(scanner, entry.fullName());
    pigeonhole (module, entry);
    ((com.sun.tools.corba.ee.idl.IDLID)repIDStack.peek ()).appendToName (name);
    currentModule = entry;
    valueDcl (entry);
    entry.tagMethods ();
    currentModule = module;
    repIDStack.pop ();
  } // value2

  /**
   *
   **/
  private void valueDcl (com.sun.tools.corba.ee.idl.ValueEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    if (token.type == com.sun.tools.corba.ee.idl.Token.Colon)
      valueInheritanceSpec (entry);
    else if (!entry.isAbstract ())
    {
      com.sun.tools.corba.ee.idl.SymtabEntry objectEntry = qualifiedEntry ("ValueBase");
      com.sun.tools.corba.ee.idl.SymtabEntry realOEntry  = typeOf (objectEntry);
      if (objectEntry == null)
        ; // qualifiedEntry already generated an error message
      else if (!isValue (realOEntry))
        com.sun.tools.corba.ee.idl.ParseException.wrongType(scanner, overrideName("ValueBase"), "value", objectEntry.typeName());
      else
        entry.derivedFromAddElement (realOEntry, false, scanner);
    }
    if (token.type == com.sun.tools.corba.ee.idl.Token.Supports)
      valueSupportsSpec (entry);
    prep.openScope (entry);
    match (com.sun.tools.corba.ee.idl.Token.LeftBrace);
    while (token.type != com.sun.tools.corba.ee.idl.Token.RightBrace)
    {
      valueElement (entry);  
    }
    prep.closeScope (entry);
    match (com.sun.tools.corba.ee.idl.Token.RightBrace);
  } // valueDcl

  /**
   *
   **/
  private void valueInheritanceSpec (com.sun.tools.corba.ee.idl.ValueEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    match (com.sun.tools.corba.ee.idl.Token.Colon);
    boolean isTruncatable = (token.type == com.sun.tools.corba.ee.idl.Token.Truncatable);
    if (isTruncatable)
        match (com.sun.tools.corba.ee.idl.Token.Truncatable);
    for (; ; match (com.sun.tools.corba.ee.idl.Token.Comma), isTruncatable = false) {
        com.sun.tools.corba.ee.idl.SymtabEntry parent = scopedName (entry.container (),
            stFactory.valueEntry ());
        com.sun.tools.corba.ee.idl.SymtabEntry realParent = typeOf (parent);
        if (isValue (realParent) && !(realParent instanceof com.sun.tools.corba.ee.idl.ValueBoxEntry))
            entry.derivedFromAddElement (realParent, isTruncatable, 
                scanner);
        else if (isForward(realParent))
            com.sun.tools.corba.ee.idl.ParseException.illegalForwardInheritance(scanner,
                    entry.fullName(), parent.fullName()) ;
        else
            com.sun.tools.corba.ee.idl.ParseException.wrongType(scanner,
                    parent.fullName(), "value", entryName(parent));
        if (token.type != com.sun.tools.corba.ee.idl.Token.Comma)
            break;
    }
  } // valueInheritanceSpec

  /**
   *
   **/
  private void valueSupportsSpec (com.sun.tools.corba.ee.idl.ValueEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    match (com.sun.tools.corba.ee.idl.Token.Supports) ;
    for (; ; match( com.sun.tools.corba.ee.idl.Token.Comma ) ) {
        com.sun.tools.corba.ee.idl.SymtabEntry parent = scopedName (entry.container (), stFactory.interfaceEntry ());
        com.sun.tools.corba.ee.idl.SymtabEntry realParent = typeOf (parent);
        if (isInterface(realParent))
            entry.derivedFromAddElement (realParent, scanner);
        else
            com.sun.tools.corba.ee.idl.ParseException.wrongType(scanner, parent.fullName(), "interface",
                    entryName(parent));

        if (token.type != com.sun.tools.corba.ee.idl.Token.Comma)
            break;
    }
  }  // valueSupportsSpec

  private void valueElement (com.sun.tools.corba.ee.idl.ValueEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    if (entry.isAbstract ())
      export (entry);
    else
      switch (token.type)
      {
        case com.sun.tools.corba.ee.idl.Token.Private:
        case com.sun.tools.corba.ee.idl.Token.Public:
          valueStateMember (entry);
          break;
        case com.sun.tools.corba.ee.idl.Token.Init:
        case com.sun.tools.corba.ee.idl.Token.Factory:  // <d62023> "factory" supplants "init" in 2.4RTF
          initDcl (entry);
          break;
        case com.sun.tools.corba.ee.idl.Token.Typedef:
        case com.sun.tools.corba.ee.idl.Token.Struct:
        case com.sun.tools.corba.ee.idl.Token.Union:
        case com.sun.tools.corba.ee.idl.Token.Enum:
        case com.sun.tools.corba.ee.idl.Token.Const:
        case com.sun.tools.corba.ee.idl.Token.Native:
        case com.sun.tools.corba.ee.idl.Token.Exception:
        case com.sun.tools.corba.ee.idl.Token.Readonly:
        case com.sun.tools.corba.ee.idl.Token.Attribute:
        case com.sun.tools.corba.ee.idl.Token.Oneway:
        case com.sun.tools.corba.ee.idl.Token.Float:
        case com.sun.tools.corba.ee.idl.Token.Double:
        case com.sun.tools.corba.ee.idl.Token.Long:
        case com.sun.tools.corba.ee.idl.Token.Short:
        case com.sun.tools.corba.ee.idl.Token.Unsigned:
        case com.sun.tools.corba.ee.idl.Token.Char:
        case com.sun.tools.corba.ee.idl.Token.Wchar:
        case com.sun.tools.corba.ee.idl.Token.Boolean:
        case com.sun.tools.corba.ee.idl.Token.Octet:
        case com.sun.tools.corba.ee.idl.Token.Any:
        case com.sun.tools.corba.ee.idl.Token.String:
        case com.sun.tools.corba.ee.idl.Token.Wstring:
        case com.sun.tools.corba.ee.idl.Token.Identifier:
        case com.sun.tools.corba.ee.idl.Token.Object:
        case com.sun.tools.corba.ee.idl.Token.ValueBase:
        case com.sun.tools.corba.ee.idl.Token.DoubleColon:
        case com.sun.tools.corba.ee.idl.Token.Void:
          export (entry);
          break;
        default:
          throw com.sun.tools.corba.ee.idl.ParseException.syntaxError(scanner, new int[]{
                          com.sun.tools.corba.ee.idl.Token.Private, com.sun.tools.corba.ee.idl.Token.Public, com.sun.tools.corba.ee.idl.Token.Init, com.sun.tools.corba.ee.idl.Token.ValueBase,
                          com.sun.tools.corba.ee.idl.Token.Typedef, com.sun.tools.corba.ee.idl.Token.Struct, com.sun.tools.corba.ee.idl.Token.Union, com.sun.tools.corba.ee.idl.Token.Enum,
                          com.sun.tools.corba.ee.idl.Token.Const, com.sun.tools.corba.ee.idl.Token.Exception, com.sun.tools.corba.ee.idl.Token.Readonly, com.sun.tools.corba.ee.idl.Token.Attribute,
                          com.sun.tools.corba.ee.idl.Token.Oneway, com.sun.tools.corba.ee.idl.Token.Float, com.sun.tools.corba.ee.idl.Token.Double, com.sun.tools.corba.ee.idl.Token.Long,
                          com.sun.tools.corba.ee.idl.Token.Short, com.sun.tools.corba.ee.idl.Token.Unsigned, com.sun.tools.corba.ee.idl.Token.Char, com.sun.tools.corba.ee.idl.Token.Wchar,
                          com.sun.tools.corba.ee.idl.Token.Boolean, com.sun.tools.corba.ee.idl.Token.Octet, com.sun.tools.corba.ee.idl.Token.Any, com.sun.tools.corba.ee.idl.Token.String,
                          com.sun.tools.corba.ee.idl.Token.Wstring, com.sun.tools.corba.ee.idl.Token.Identifier, com.sun.tools.corba.ee.idl.Token.DoubleColon, com.sun.tools.corba.ee.idl.Token.Void},
                  token.type);
      }  // switch
  }  // valueElement

  // <f46082.40>
  /**
   *
   **/
  private void valueStateMember (com.sun.tools.corba.ee.idl.ValueEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    com.sun.tools.corba.ee.idl.TypedefEntry typedefEntry =
        stFactory.typedefEntry (entry, (com.sun.tools.corba.ee.idl.IDLID)repIDStack.peek ());
    typedefEntry.sourceFile (scanner.fileEntry ());
    // comment must immediately precede "public", "private" keywords
    typedefEntry.comment (token.comment);
    boolean isPublic = (token.type == com.sun.tools.corba.ee.idl.Token.Public);
    if (isPublic)
      match (com.sun.tools.corba.ee.idl.Token.Public);
    else
      match (com.sun.tools.corba.ee.idl.Token.Private);
    // <f46082.40> Add constructed types declared "inline" to the contained
    // vector of this value entry.
    boolean isConstTypeSpec =
        (token.type == com.sun.tools.corba.ee.idl.Token.Struct || token.type == com.sun.tools.corba.ee.idl.Token.Union || token.type == com.sun.tools.corba.ee.idl.Token.Enum);
    // <f46082.40> Make typedefEntry anonymous.  If this line is removed,
    // the entry will be named incorrectly.  See <d50618>.
    typedefEntry.name ("");
    typedefEntry.type (typeSpec (typedefEntry));
    addDeclarators (entry, typedefEntry, isPublic);
    // <f46082.40>
    if (isConstTypeSpec)
      entry.addContained (typedefEntry);
    match (com.sun.tools.corba.ee.idl.Token.Semicolon);
  }  // valueStateMember


  private void addDeclarators (com.sun.tools.corba.ee.idl.ValueEntry entry, com.sun.tools.corba.ee.idl.TypedefEntry typedefEntry,
      boolean isPublic) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    int modifier = isPublic ? com.sun.tools.corba.ee.idl.InterfaceState.Public : com.sun.tools.corba.ee.idl.InterfaceState.Private;
    try
    {
      Vector typedefList = new Vector ();
      declarators (typedefEntry, typedefList);
      for (Enumeration e = typedefList.elements (); e.hasMoreElements ();)
        entry.addStateElement (
            new com.sun.tools.corba.ee.idl.InterfaceState(modifier, (com.sun.tools.corba.ee.idl.TypedefEntry)e.nextElement ()), scanner);
    }
    catch (com.sun.tools.corba.ee.idl.ParseException exception)
    {
      skipToSemicolon ();
    }
  } // addDeclarators

  /**
   *
   **/
  private void initDcl (com.sun.tools.corba.ee.idl.ValueEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    com.sun.tools.corba.ee.idl.MethodEntry method = stFactory.methodEntry (entry, (com.sun.tools.corba.ee.idl.IDLID)repIDStack.peek ());
    method.sourceFile (scanner.fileEntry ());
    // Comment must immediately precede "init" keyword:
    method.comment (token.comment);
    repIDStack.push (((com.sun.tools.corba.ee.idl.IDLID)repIDStack.peek ()).clone ());
    ((com.sun.tools.corba.ee.idl.IDLID)repIDStack.peek ()).appendToName (token.name);

    // <d62023> In 2.3 prelim, <init_dcl> ::= "init" "(" ...
    if (token.type == com.sun.tools.corba.ee.idl.Token.Init)
    {
      method.name ("init");
      match (com.sun.tools.corba.ee.idl.Token.Init);
      match (com.sun.tools.corba.ee.idl.Token.LeftParen);
    }
    else // <d62023> In 2.4rtf, <init_dcl> ::= "factory" <Indentifier> "(" ...
    {
      match (com.sun.tools.corba.ee.idl.Token.Factory);
      method.name (token.name);
      if (token.type == com.sun.tools.corba.ee.idl.Token.MacroIdentifier)
        match (com.sun.tools.corba.ee.idl.Token.MacroIdentifier);  // "(" already consumed.
      else
      {
        match (com.sun.tools.corba.ee.idl.Token.Identifier);
        match (com.sun.tools.corba.ee.idl.Token.LeftParen);
      }
    }

    if (token.type != com.sun.tools.corba.ee.idl.Token.RightParen)
      for (;;)
      {
        initParamDcl (method);
        if (token.type == com.sun.tools.corba.ee.idl.Token.RightParen)
          break;
        match (com.sun.tools.corba.ee.idl.Token.Comma);
      }
    entry.initializersAddElement (method, scanner);
    match (com.sun.tools.corba.ee.idl.Token.RightParen);
    match (com.sun.tools.corba.ee.idl.Token.Semicolon);
    repIDStack.pop ();
  } // initDcl

  /**
   *
   **/
  private void initParamDcl (com.sun.tools.corba.ee.idl.MethodEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    com.sun.tools.corba.ee.idl.ParameterEntry parmEntry = stFactory.parameterEntry (entry, (com.sun.tools.corba.ee.idl.IDLID)repIDStack.peek ());
    parmEntry.sourceFile (scanner.fileEntry());
    // Comment must immediately precede parameter attribute
    parmEntry.comment (token.comment);
    match (com.sun.tools.corba.ee.idl.Token.In);
    parmEntry.passType (com.sun.tools.corba.ee.idl.ParameterEntry.In);
    parmEntry.type (paramTypeSpec (entry));
    parmEntry.name (token.name);
    match (com.sun.tools.corba.ee.idl.Token.Identifier);
    if (isntInList (entry.parameters (), parmEntry.name ()))
      entry.addParameter (parmEntry);
  } // initParamDcl

  /**
   *
   **/
  private void valueBox (com.sun.tools.corba.ee.idl.ModuleEntry module, String name) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    repIDStack.push (((com.sun.tools.corba.ee.idl.IDLID)repIDStack.peek ()).clone ());
    // Note: The 'actual' repository ID will be calculated at the end of
    // the parsing phase, since it is based on the entire contents of the
    // declaration, and needs to have all forward references resolved:
    com.sun.tools.corba.ee.idl.ValueEntry entry = stFactory.valueBoxEntry (module, (com.sun.tools.corba.ee.idl.IDLID)repIDStack.peek ());
    entry.sourceFile (scanner.fileEntry ());
    entry.name (name);
    // comment must immediately precede "value" keyword
    entry.comment (tokenHistory.lookBack (2).comment);
    // <f46082.40> Value boxes may not be forwarded.
    // If this value has been forward declared, there are probably
    // other values which derive from a ForwardValueEntry.
    // Replace those ForwardValueEntry's with this ValueEntry:
    //if (!ForwardValueEntry.replaceForwardDecl (entry))
    //   ParseException.badAbstract (scanner, entry.fullName());
    com.sun.tools.corba.ee.idl.SymtabEntry valueForward = Parser.symbolTable.get(entry.fullName());
    if (valueForward != null && valueForward instanceof com.sun.tools.corba.ee.idl.ForwardEntry) {
      com.sun.tools.corba.ee.idl.ParseException.forwardedValueBox(scanner, entry.fullName());
    }
    pigeonhole (module, entry);
    ((com.sun.tools.corba.ee.idl.IDLID)repIDStack.peek ()).appendToName (name);
    currentModule = entry;
    com.sun.tools.corba.ee.idl.TypedefEntry typedefEntry = stFactory.typedefEntry (entry, (com.sun.tools.corba.ee.idl.IDLID)repIDStack.peek ());
    typedefEntry.sourceFile (scanner.fileEntry ());
    typedefEntry.comment (token.comment);
    // <d50237> Workaround to place typedefEntry in the _contained vector of
    // this value box entry ONLY when <type_spec> is a constructed type declared
    // at this point (i.e., not an identifier that resolves to a constructed
    // type), so that emitters may generate bindings for it. <daz>
    boolean isConstTypeSpec =
        token.type == com.sun.tools.corba.ee.idl.Token.Struct || token.type == com.sun.tools.corba.ee.idl.Token.Union || token.type == com.sun.tools.corba.ee.idl.Token.Enum;
    // <d50618> Make typedefEntry anonymous.  If this line is removed, the
    // entry will be named incorrectly.
    typedefEntry.name ("");
    typedefEntry.type (typeSpec (typedefEntry));
    // <d59067> Value boxes cannot be nested.
    if (typedefEntry.type () instanceof com.sun.tools.corba.ee.idl.ValueBoxEntry)
      com.sun.tools.corba.ee.idl.ParseException.nestedValueBox(scanner);
    //typedefEntry.name ("");
    entry.addStateElement (new com.sun.tools.corba.ee.idl.InterfaceState(com.sun.tools.corba.ee.idl.InterfaceState.Public, typedefEntry), scanner);
    if (isConstTypeSpec)
      entry.addContained (typedefEntry);
    currentModule = module;
    repIDStack.pop ();
  } // valueBox

  /**
   *
   **/
  private void valueForwardDcl (com.sun.tools.corba.ee.idl.ModuleEntry module, String name, boolean isAbstract)
      throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    com.sun.tools.corba.ee.idl.ForwardValueEntry entry = stFactory.forwardValueEntry (module, (com.sun.tools.corba.ee.idl.IDLID)repIDStack.peek ());
    entry.sourceFile (scanner.fileEntry ());
    entry.name (name);
    entry.setInterfaceType(isAbstract ? com.sun.tools.corba.ee.idl.InterfaceType.ABSTRACT : com.sun.tools.corba.ee.idl.InterfaceType.NORMAL );
    // Comment must immediately precede "[abstract] value" keyword[s]
    entry.comment (tokenHistory.lookBack (isAbstract? 3 : 2).comment);
    pigeonhole (module, entry);
  } // valueForwardDcl

  private void nativeDcl (com.sun.tools.corba.ee.idl.SymtabEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    match (com.sun.tools.corba.ee.idl.Token.Native);
    com.sun.tools.corba.ee.idl.NativeEntry nativeEntry = stFactory.nativeEntry (entry, (com.sun.tools.corba.ee.idl.IDLID)repIDStack.peek ());
    nativeEntry.sourceFile (scanner.fileEntry ());
    // Comment must immediately precede "native" keyword
    nativeEntry.comment (tokenHistory.lookBack (1).comment);
    nativeEntry.name (token.name);
    match (com.sun.tools.corba.ee.idl.Token.Identifier);
    pigeonhole (entry, nativeEntry);
  } // nativeDcl
  /**
   *
   **/
  private void constDcl (com.sun.tools.corba.ee.idl.SymtabEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    match (com.sun.tools.corba.ee.idl.Token.Const);
    com.sun.tools.corba.ee.idl.ConstEntry constEntry = stFactory.constEntry (entry, (com.sun.tools.corba.ee.idl.IDLID)repIDStack.peek ());
    constEntry.sourceFile (scanner.fileEntry ());
    // Comment must immediately precede "const" keyword
    constEntry.comment (tokenHistory.lookBack (1).comment);
    constType (constEntry);
    constEntry.name (token.name);
    match (com.sun.tools.corba.ee.idl.Token.Identifier);
    match (com.sun.tools.corba.ee.idl.Token.Equal);
    constEntry.value (constExp (constEntry));
    verifyConstType (constEntry.value (), typeOf (constEntry.type ()));
    pigeonhole (entry, constEntry);
  } // constDcl

  /**
   *
   **/
  private void constType (com.sun.tools.corba.ee.idl.SymtabEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    switch (token.type)
    {
      case com.sun.tools.corba.ee.idl.Token.Octet:
        entry.type( octetType()) ;
        break ;
      case com.sun.tools.corba.ee.idl.Token.Long:
      case com.sun.tools.corba.ee.idl.Token.Short:
      case com.sun.tools.corba.ee.idl.Token.Unsigned:
        entry.type (integerType (entry));
        break;
      case com.sun.tools.corba.ee.idl.Token.Char:
      case com.sun.tools.corba.ee.idl.Token.Wchar:
        entry.type (charType ());
        break;
      case com.sun.tools.corba.ee.idl.Token.Boolean:
        entry.type (booleanType ());
        break;
      case com.sun.tools.corba.ee.idl.Token.Float:
      case com.sun.tools.corba.ee.idl.Token.Double:
        entry.type (floatingPtType ());
        break;
      case com.sun.tools.corba.ee.idl.Token.String:
      case com.sun.tools.corba.ee.idl.Token.Wstring:
        entry.type (stringType (entry));
        break;
      case com.sun.tools.corba.ee.idl.Token.Identifier:
      case com.sun.tools.corba.ee.idl.Token.DoubleColon:
        entry.type (scopedName (entry.container (), stFactory.primitiveEntry ()));
        if (hasArrayInfo (entry.type ()))
          com.sun.tools.corba.ee.idl.ParseException.illegalArray(scanner, "const");
        com.sun.tools.corba.ee.idl.SymtabEntry entryType = typeOf (entry.type ());
        if (!((entryType instanceof com.sun.tools.corba.ee.idl.PrimitiveEntry) || (entryType instanceof com.sun.tools.corba.ee.idl.StringEntry)))
        {
          com.sun.tools.corba.ee.idl.ParseException.wrongType(scanner, entry.fullName(), "primitive or string", entryName(entry.type()));
          entry.type (qualifiedEntry ("long"));
        }
        else if (entryType instanceof com.sun.tools.corba.ee.idl.PrimitiveEntry)
        {
          String any = overrideName ("any");
          if (entryType.name().equals (any))
          {
            com.sun.tools.corba.ee.idl.ParseException.wrongType(scanner, entry.fullName(), "primitive or string (except " + any + ')', any);
            entry.type (qualifiedEntry ("long"));
          }
        }
        break;
      default:
        throw com.sun.tools.corba.ee.idl.ParseException.syntaxError (scanner, new int[]{
                com.sun.tools.corba.ee.idl.Token.Long, com.sun.tools.corba.ee.idl.Token.Short, com.sun.tools.corba.ee.idl.Token.Unsigned, com.sun.tools.corba.ee.idl.Token.Char,
                com.sun.tools.corba.ee.idl.Token.Wchar, com.sun.tools.corba.ee.idl.Token.Boolean, com.sun.tools.corba.ee.idl.Token.Float, com.sun.tools.corba.ee.idl.Token.Double,
                com.sun.tools.corba.ee.idl.Token.String, com.sun.tools.corba.ee.idl.Token.Wstring, com.sun.tools.corba.ee.idl.Token.Identifier,
                com.sun.tools.corba.ee.idl.Token.DoubleColon}, token.type);
    }
  } // constType

  /**
   *
   **/
  private boolean hasArrayInfo (com.sun.tools.corba.ee.idl.SymtabEntry entry)
  {
    while (entry instanceof com.sun.tools.corba.ee.idl.TypedefEntry)
    {
      if (!((com.sun.tools.corba.ee.idl.TypedefEntry)entry).arrayInfo ().isEmpty())
        return true;
      entry = entry.type ();
    }
  return false;
  } // hasArrayInfo

  /**
   *
   **/
  public static String overrideName (String string)
  {
    String name = overrideNames.get(string);
    return (name == null) ? string : name;
  } // overrideName

  // If entry is boolean, expression value must be boolean
  // If entry is float/double, expression value must be float/double
  // If entry is integral, expression value must be integral
  // If entry is string, expression value must be string

  /**
   *
   **/
  private void verifyConstType (com.sun.tools.corba.ee.idl.constExpr.Expression e, com.sun.tools.corba.ee.idl.SymtabEntry t)
  {
    Object value = e.value ();
    if (value instanceof BigInteger)
      verifyIntegral ((Number)value, t);
    else if (value instanceof String)
      verifyString (e, t);
    else if (value instanceof Boolean)
      verifyBoolean (t);
    else if (value instanceof Character)
      verifyCharacter (e, t);
    else if (value instanceof Float || value instanceof Double)
      verifyFloat((Number)value, t);
    else if (value instanceof com.sun.tools.corba.ee.idl.ConstEntry)
      verifyConstType (((com.sun.tools.corba.ee.idl.ConstEntry)value).value (), t);
    else
      com.sun.tools.corba.ee.idl.ParseException.wrongExprType(scanner, t.fullName(),
              (value == null) ? "" : value.toString());
  } // verifyConstType

  private static final int MAX_SHORT  = 32767;
  private static final int MIN_SHORT  = -32768;
  private static final int MAX_USHORT = 65535;

  /**
   *
   **/
  private void verifyIntegral (Number n, com.sun.tools.corba.ee.idl.SymtabEntry t)
  {
    boolean outOfRange = false;
    //KEEP: Useful for debugging com.sun.tools.corba.ee.idl.constExpr package
    //System.out.println ("verifyIntegral, n = " + n.toString ());

    if (t == qualifiedEntry( "octet" )) {
        if ((n.longValue() > 255) || (n.longValue() < 0))
            outOfRange = true ;
    } else if (t == qualifiedEntry ("long")) {
        if (n.longValue () > Integer.MAX_VALUE || n.longValue() < Integer.MIN_VALUE)
            outOfRange = true;
    } else if (t == qualifiedEntry ("short")) {
        if (n.intValue () > Short.MAX_VALUE || n.intValue () < Short.MIN_VALUE)
            outOfRange = true;
    } else if (t == qualifiedEntry ("unsigned long")) {
        if (n.longValue () > (long)Integer.MAX_VALUE*2+1 || n.longValue() < 0)
            outOfRange = true;
    } else if (t == qualifiedEntry ("unsigned short")) {
        if (n.intValue () > (int) Short.MAX_VALUE*2+1 || n.intValue () < 0)
            outOfRange = true;
    } else if (t == qualifiedEntry ("long long")) {
        // BigInteger required because value being compared may exceed
        // java.lang.Long.MAX_VALUE/MIN_VALUE:
        BigInteger llMax = BigInteger.valueOf (Long.MAX_VALUE);
        BigInteger llMin = BigInteger.valueOf (Long.MIN_VALUE);
        if (((BigInteger)n).compareTo (llMax) > 0 || 
            ((BigInteger)n).compareTo (llMin) < 0)
            outOfRange = true;
    } else if (t == qualifiedEntry ("unsigned long long")) {
        BigInteger ullMax = BigInteger.valueOf (Long.MAX_VALUE).
            multiply (BigInteger.valueOf (2)).
            add (BigInteger.valueOf (1));
        BigInteger ullMin = BigInteger.valueOf (0);
        if (((BigInteger)n).compareTo (ullMax) > 0 || 
            ((BigInteger)n).compareTo (ullMin) < 0)
            outOfRange = true;
    } else {
        String got = null;
        // THIS MUST BE CHANGED; BIGINTEGER IS ALWAYS THE CONTAINER
        /*
        if (n instanceof Short)
          got = "short";
        else if (n instanceof Integer)
          got = "long";
        else
          got = "long long";
        */
        got = "long";
        com.sun.tools.corba.ee.idl.ParseException.wrongExprType(scanner, t.fullName(), got);
    }

    if (outOfRange)
        com.sun.tools.corba.ee.idl.ParseException.outOfRange(scanner, n.toString(), t.fullName());
  } // verifyIntegral

  /**
   *
   **/
  private void verifyString (com.sun.tools.corba.ee.idl.constExpr.Expression e, com.sun.tools.corba.ee.idl.SymtabEntry t)
  {
    String string = (String)(e.value()) ;
    if (!(t instanceof com.sun.tools.corba.ee.idl.StringEntry)) {
        com.sun.tools.corba.ee.idl.ParseException.wrongExprType(scanner, t.fullName(), e.type());
    } else if (((com.sun.tools.corba.ee.idl.StringEntry)t).maxSize () != null) {
        com.sun.tools.corba.ee.idl.constExpr.Expression maxExp = ((com.sun.tools.corba.ee.idl.StringEntry)t).maxSize ();
        try {
            Number max = (Number)maxExp.value ();
            if (string.length () > max.intValue ())
                com.sun.tools.corba.ee.idl.ParseException.stringTooLong(scanner, string, max.toString());
        } catch (Exception exception) {
            // If the above statement is not valid and throws an
            // exception, then an error occurred and was reported
            // earlier.  Move on.
        }
    } 
    
    if (!e.type().equals( t.name())) {
        // cannot mix strings and wide strings
        com.sun.tools.corba.ee.idl.ParseException.wrongExprType(scanner, t.name(), e.type()) ;
    }
  } // verifyString

  /**
   *
   **/
  private void verifyBoolean (com.sun.tools.corba.ee.idl.SymtabEntry t)
  {
    if (!t.name ().equals (overrideName ("boolean")))
      com.sun.tools.corba.ee.idl.ParseException.wrongExprType(scanner, t.name(), "boolean");
  } // verifyBoolean

  /**
   *
   **/
  private void verifyCharacter (com.sun.tools.corba.ee.idl.constExpr.Expression e, com.sun.tools.corba.ee.idl.SymtabEntry t)
  {
    // Bug fix 4382578:  Can't compile a wchar literal.
    // Allow a Character to be either a char or a wchar.
    if (!t.name ().equals (overrideName ("char")) &&
        !t.name ().equals (overrideName ("wchar")) ||
        !t.name().equals(e.type()) )
        com.sun.tools.corba.ee.idl.ParseException.wrongExprType(scanner, t.fullName(), e.type()) ;
  } // verifyCharacter

  /**
   *
   **/
  private void verifyFloat (Number f, com.sun.tools.corba.ee.idl.SymtabEntry t)
  {
    // <d52042> Added range checking for floats.
    //if (!(t.name ().equals (overrideName ("float")) ||
    //    t.name ().equals (overrideName ("double"))))
    //  ParseException.wrongExprType (scanner, 
    //      t.fullName (), (f instanceof Float) ? "float" : "double");
    //KEEP: Useful for debugging com.sun.tools.corba.ee.idl.constExpr package
    //System.out.println ("verifyFloat, f = " + f.toString ());
    boolean outOfRange = false;
    if (t.name ().equals (overrideName ("float")))
    {
      double absVal = (f.doubleValue () < 0.0) ?
          f.doubleValue () * -1.0 : f.doubleValue ();
      if ((absVal != 0.0) &&
          (absVal > Float.MAX_VALUE || absVal < Float.MIN_VALUE))
        outOfRange = true;
    }
    else if (t.name ().equals (overrideName ("double")))
    {
      // Cannot check range of double until BigDecimal is the basis
      // of all floating-point types.  Currently, it is Double.  The
      // parser will fail when instantiating a Double with an exception.
    }
    else
    {
      com.sun.tools.corba.ee.idl.ParseException.wrongExprType(scanner, t.fullName(),
              (f instanceof Float) ? "float" : "double");
    }
    if (outOfRange)
      com.sun.tools.corba.ee.idl.ParseException.outOfRange(scanner, f.toString(), t.fullName());
  } // verifyFloat

  /**
   *
   **/
  com.sun.tools.corba.ee.idl.constExpr.Expression constExp (com.sun.tools.corba.ee.idl.SymtabEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    // Parse the expression.
    com.sun.tools.corba.ee.idl.constExpr.Expression expr = orExpr (null, entry);

    // Set its target type.
    if (expr.type() == null)
      expr.type (entry.typeName ());
    // Compute its value and <d53042> coerce it to the target type.
    try
    {
      expr.evaluate ();

      // <d54042> Coerces integral value to Double if an integer literal
      // was used to initialize a floating-point constant expression.
      if (expr instanceof com.sun.tools.corba.ee.idl.constExpr.Terminal &&
          expr.value () instanceof BigInteger &&
          (overrideName (expr.type ()).equals ("float") ||
              overrideName (expr.type ()).contains("double")))
      {
        expr.value (((BigInteger)expr.value ()).doubleValue ());
      }
    }
    catch (com.sun.tools.corba.ee.idl.constExpr.EvaluationException exception)
    {
      com.sun.tools.corba.ee.idl.ParseException.evaluationError(scanner, exception.toString());
    }
    return expr;
  } // constExp

  /**
   *
   **/
  private com.sun.tools.corba.ee.idl.constExpr.Expression orExpr (com.sun.tools.corba.ee.idl.constExpr.Expression e, com.sun.tools.corba.ee.idl.SymtabEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    if (e == null)
      e = xorExpr (null, entry);
    else
    {
      com.sun.tools.corba.ee.idl.constExpr.BinaryExpr b = (com.sun.tools.corba.ee.idl.constExpr.BinaryExpr)e;
      b.right (xorExpr (null, entry));
      e.rep (e.rep () + b.right ().rep ());
    }
    if (token.equals (com.sun.tools.corba.ee.idl.Token.Bar))
    {
      match (token.type);
      com.sun.tools.corba.ee.idl.constExpr.Or or = exprFactory.or (e, null);
      or.type (entry.typeName ());
      or.rep (e.rep () + " | ");
      return orExpr (or, entry);
    }
    return e;
  } // orExpr

  /**
   *
   **/
  private com.sun.tools.corba.ee.idl.constExpr.Expression xorExpr (com.sun.tools.corba.ee.idl.constExpr.Expression e, com.sun.tools.corba.ee.idl.SymtabEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    if (e == null)
      e = andExpr (null, entry);
    else
    {
      com.sun.tools.corba.ee.idl.constExpr.BinaryExpr b = (com.sun.tools.corba.ee.idl.constExpr.BinaryExpr)e;
      b.right (andExpr (null, entry));
      e.rep (e.rep () + b.right ().rep ());
    }
    if (token.equals (com.sun.tools.corba.ee.idl.Token.Carat))
    {
      match (token.type);
      com.sun.tools.corba.ee.idl.constExpr.Xor xor = exprFactory.xor (e, null);
      xor.rep (e.rep () + " ^ ");
      xor.type (entry.typeName ());
      return xorExpr (xor, entry);
    }
    return e;
  } // xorExpr

  /**
   *
   **/
  private com.sun.tools.corba.ee.idl.constExpr.Expression andExpr (com.sun.tools.corba.ee.idl.constExpr.Expression e, com.sun.tools.corba.ee.idl.SymtabEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    if (e == null)
      e = shiftExpr (null, entry);
    else
    {
      com.sun.tools.corba.ee.idl.constExpr.BinaryExpr b = (com.sun.tools.corba.ee.idl.constExpr.BinaryExpr)e;
      b.right (shiftExpr (null, entry));
      e.rep (e.rep () + b.right ().rep ());
    }
    if (token.equals (com.sun.tools.corba.ee.idl.Token.Ampersand))
    {
      match (token.type);
      com.sun.tools.corba.ee.idl.constExpr.And and = exprFactory.and (e, null);
      and.rep(e.rep () + " & ");
      and.type (entry.typeName ());
      return andExpr (and, entry);
    }
    return e;
  } // andExpr

  /**
   *
   **/
  private com.sun.tools.corba.ee.idl.constExpr.Expression shiftExpr (com.sun.tools.corba.ee.idl.constExpr.Expression e, com.sun.tools.corba.ee.idl.SymtabEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    if (e == null)
      e = addExpr (null, entry);
    else
    {
      com.sun.tools.corba.ee.idl.constExpr.BinaryExpr b = (com.sun.tools.corba.ee.idl.constExpr.BinaryExpr)e;
      b.right (addExpr (null, entry));
      e.rep (e.rep () + b.right ().rep ());
    }
    if (token.equals (com.sun.tools.corba.ee.idl.Token.ShiftLeft))
    {
      match (token.type);
      com.sun.tools.corba.ee.idl.constExpr.ShiftLeft sl = exprFactory.shiftLeft (e, null);
      sl.type (entry.typeName ());
      sl.rep (e.rep () + " << ");
      return shiftExpr (sl, entry);
    }
    if (token.equals (com.sun.tools.corba.ee.idl.Token.ShiftRight))
    {
      match (token.type);
      com.sun.tools.corba.ee.idl.constExpr.ShiftRight sr = exprFactory.shiftRight (e, null);
      sr.type (entry.typeName ());
      sr.rep (e.rep () + " >> ");
      return shiftExpr (sr, entry);
    }
    return e;
  } // shiftExpr

  /**
   *
   **/
  private com.sun.tools.corba.ee.idl.constExpr.Expression addExpr (com.sun.tools.corba.ee.idl.constExpr.Expression e, com.sun.tools.corba.ee.idl.SymtabEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    if (e == null)
      e = multExpr (null, entry);
    else
    {
      com.sun.tools.corba.ee.idl.constExpr.BinaryExpr b = (com.sun.tools.corba.ee.idl.constExpr.BinaryExpr)e;
      b.right (multExpr (null, entry));
      e.rep (e.rep () + b.right ().rep ());
    }
    if (token.equals (com.sun.tools.corba.ee.idl.Token.Plus))
    {
      match (token.type);
      com.sun.tools.corba.ee.idl.constExpr.Plus p = exprFactory.plus (e, null);
      p.type (entry.typeName ());
      p.rep (e.rep () + " + ");
      return addExpr (p, entry);
    }
    if (token.equals (com.sun.tools.corba.ee.idl.Token.Minus))
    {
      match (token.type);
      com.sun.tools.corba.ee.idl.constExpr.Minus m = exprFactory.minus (e, null);
      m.type (entry.typeName ());
      m.rep (e.rep () + " - ");
      return addExpr (m, entry);
    }
    return e;
  } // addExpr

  /**
   *
   **/
  private com.sun.tools.corba.ee.idl.constExpr.Expression multExpr (com.sun.tools.corba.ee.idl.constExpr.Expression e, com.sun.tools.corba.ee.idl.SymtabEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    if (e == null)
    e = unaryExpr (entry);
    else
    {
      com.sun.tools.corba.ee.idl.constExpr.BinaryExpr b = (com.sun.tools.corba.ee.idl.constExpr.BinaryExpr)e;
      b.right (unaryExpr (entry));
      e.rep (e.rep () + b.right ().rep ());
    }
    if (token.equals (com.sun.tools.corba.ee.idl.Token.Star))
    {
      match (token.type);
      com.sun.tools.corba.ee.idl.constExpr.Times t = exprFactory.times (e, null);
      t.type (entry.typeName ());
      t.rep (e.rep () + " * ");
      return multExpr (t, entry);
    }
    if (token.equals (com.sun.tools.corba.ee.idl.Token.Slash))
    {
      match (token.type);
      com.sun.tools.corba.ee.idl.constExpr.Divide d = exprFactory.divide (e, null);
      d.type (entry.typeName ());
      d.rep (e.rep () + " / ");
      return multExpr (d, entry);
    }
    if (token.equals (com.sun.tools.corba.ee.idl.Token.Percent))
    {
      match (token.type);
      com.sun.tools.corba.ee.idl.constExpr.Modulo m = exprFactory.modulo (e, null);
      m.type (entry.typeName ());
      m.rep (e.rep () + " % ");
      return multExpr (m, entry);
    }
    return e;
  } // multExpr

  /**
   *
   **/
  private com.sun.tools.corba.ee.idl.constExpr.Expression unaryExpr (com.sun.tools.corba.ee.idl.SymtabEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    if (token.equals (com.sun.tools.corba.ee.idl.Token.Plus))
    {
      match (token.type);
      com.sun.tools.corba.ee.idl.constExpr.Expression e   = primaryExpr (entry);
      com.sun.tools.corba.ee.idl.constExpr.Positive pos = exprFactory.positive (e);
      pos.type (entry.typeName());
      pos.rep ('+' + e.rep());
      return pos;
    }
    if (token.equals (com.sun.tools.corba.ee.idl.Token.Minus))
    {
      match (token.type);
      com.sun.tools.corba.ee.idl.constExpr.Expression e   = primaryExpr (entry);
      com.sun.tools.corba.ee.idl.constExpr.Negative neg = exprFactory.negative (e);
      neg.type (entry.typeName());
      neg.rep ('-' + e.rep());
      return neg;
    }
    if (token.equals (com.sun.tools.corba.ee.idl.Token.Tilde))
    {
      match (token.type);
      com.sun.tools.corba.ee.idl.constExpr.Expression e   = primaryExpr (entry);
      com.sun.tools.corba.ee.idl.constExpr.Not not = exprFactory.not (e);
      not.type (entry.typeName());
      not.rep ('~' + e.rep());
      return not;
    }
    return primaryExpr (entry);
  } // unaryExpr

  /**
   *
   **/
  private com.sun.tools.corba.ee.idl.constExpr.Expression primaryExpr (com.sun.tools.corba.ee.idl.SymtabEntry entry)
      throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    com.sun.tools.corba.ee.idl.constExpr.Expression primary = null;
    if (parsingConditionalExpr)
    {
      prep.token = token; // Give current token to preprocessor
      primary    = prep.primaryExpr (entry);
      token      = prep.token; // Get the current token from preprocessor
    }
    else
      switch (token.type)
      {
        case com.sun.tools.corba.ee.idl.Token.Identifier:
        case com.sun.tools.corba.ee.idl.Token.DoubleColon:
          com.sun.tools.corba.ee.idl.ConstEntry expectedC = stFactory.constEntry ();
          expectedC.value (exprFactory.terminal ("1", BigInteger.valueOf (1)));
          com.sun.tools.corba.ee.idl.SymtabEntry ref = scopedName (entry.container (), expectedC);
          if (!(ref instanceof com.sun.tools.corba.ee.idl.ConstEntry))
          {
            com.sun.tools.corba.ee.idl.ParseException.invalidConst(scanner, ref.fullName());
            // An error occurred.  Just give it some bogus value. <daz>
            //primary = exprFactory.terminal ("1", new Long (1));
            primary = exprFactory.terminal ("1", BigInteger.valueOf (1));
          }
          else
            primary = exprFactory.terminal ((com.sun.tools.corba.ee.idl.ConstEntry)ref);
          break;
        case com.sun.tools.corba.ee.idl.Token.BooleanLiteral:
        case com.sun.tools.corba.ee.idl.Token.CharacterLiteral:
        case com.sun.tools.corba.ee.idl.Token.IntegerLiteral:
        case com.sun.tools.corba.ee.idl.Token.FloatingPointLiteral:
        case com.sun.tools.corba.ee.idl.Token.StringLiteral:
          primary = literal (entry);
          break;
        case com.sun.tools.corba.ee.idl.Token.LeftParen:
          match (com.sun.tools.corba.ee.idl.Token.LeftParen);
          primary = constExp (entry);
          match (com.sun.tools.corba.ee.idl.Token.RightParen);
          primary.rep ('(' + primary.rep () + ')');
          break;
        default:
          throw com.sun.tools.corba.ee.idl.ParseException.syntaxError (scanner, new int[]{
                          com.sun.tools.corba.ee.idl.Token.Identifier, com.sun.tools.corba.ee.idl.Token.DoubleColon, com.sun.tools.corba.ee.idl.Token.Literal, com.sun.tools.corba.ee.idl.Token.LeftParen},
                  token.type);
      }
    return primary;
  } // primaryExpr

  /**
   *
   **/
  @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
  com.sun.tools.corba.ee.idl.constExpr.Expression literal (com.sun.tools.corba.ee.idl.SymtabEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    String     string  = token.name;
    com.sun.tools.corba.ee.idl.constExpr.Expression literal = null;
    switch (token.type)
    {
      case com.sun.tools.corba.ee.idl.Token.IntegerLiteral:
        match (com.sun.tools.corba.ee.idl.Token.IntegerLiteral);
        try
        {
          literal = exprFactory.terminal (string, parseString (string));
          literal.type (entry.typeName ());
        }
        catch (NumberFormatException exception)
        {
          com.sun.tools.corba.ee.idl.ParseException.notANumber(scanner, string);
          literal = exprFactory.terminal ("0", BigInteger.valueOf (0));
        }
        break;
      case com.sun.tools.corba.ee.idl.Token.CharacterLiteral:
        boolean isWide = token.isWide();
        match (com.sun.tools.corba.ee.idl.Token.CharacterLiteral);
        literal = exprFactory.terminal ("'" + string.substring (1) + "'", string.charAt( 0 ), isWide );
        break;
      case com.sun.tools.corba.ee.idl.Token.FloatingPointLiteral:
        match (com.sun.tools.corba.ee.idl.Token.FloatingPointLiteral);
        try
        {
          literal = exprFactory.terminal (string, new Double (string));
          literal.type (entry.typeName ());
        }
        catch (NumberFormatException e)
        {
          com.sun.tools.corba.ee.idl.ParseException.notANumber(scanner, string);
        }
        break;
      case com.sun.tools.corba.ee.idl.Token.BooleanLiteral:
        literal = booleanLiteral ();
        break;
      case com.sun.tools.corba.ee.idl.Token.StringLiteral:
        literal = stringLiteral ();
        break;
      default:
        throw com.sun.tools.corba.ee.idl.ParseException.syntaxError (scanner, com.sun.tools.corba.ee.idl.Token.Literal, token.type);
    }
    return literal;
  } // literal

  /**
   *
   **/
  private BigInteger parseString (String string) throws NumberFormatException
  {
    int radix = 10;
    if (string.length() > 1)
      if (string.charAt (0) == '0')
        if (string.charAt (1) == 'x' || string.charAt (1) == 'X')
        {
          string = string.substring (2);
          radix = 16;
        }
        else
          radix = 8;
    return new BigInteger (string, radix);
  } // parseString

  @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
  private com.sun.tools.corba.ee.idl.constExpr.Terminal booleanLiteral() throws IOException, com.sun.tools.corba.ee.idl.ParseException {
    Boolean bool;
    switch ( token.name ) {
      case "TRUE":
        bool = true;
        break;
      case "FALSE":
        bool = false;
        break;
      default:
        ParseException.invalidConst( scanner, token.name );
        bool = false;
        break;
    }
    String name = token.name;
    match (com.sun.tools.corba.ee.idl.Token.BooleanLiteral);
    return exprFactory.terminal (name, bool);
  }

  /**
   *
   **/
  private com.sun.tools.corba.ee.idl.constExpr.Expression stringLiteral () throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    // If string literals appear together, concatenate them.  Ie:
    // "Twas " "brillig " "and " "the " "slithy " "toves"
    // becomes
    // "Twas brillig and the slithy toves"
    boolean isWide = token.isWide() ;
    String literal = "";
    do
    {
      literal += token.name;
      match (com.sun.tools.corba.ee.idl.Token.StringLiteral);
    } while (token.equals (com.sun.tools.corba.ee.idl.Token.StringLiteral));
    com.sun.tools.corba.ee.idl.constExpr.Expression stringExpr = exprFactory.terminal (literal, isWide );
    stringExpr.rep ('"' + literal + '"');
    return stringExpr;
  } // stringLiteral

  /**
   *
   **/
  private com.sun.tools.corba.ee.idl.constExpr.Expression positiveIntConst (com.sun.tools.corba.ee.idl.SymtabEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    com.sun.tools.corba.ee.idl.constExpr.Expression e     = constExp (entry);
    Object     value = e.value ();
    while (value instanceof com.sun.tools.corba.ee.idl.ConstEntry)
      value = ((com.sun.tools.corba.ee.idl.ConstEntry)value).value ().value ();
    if (!(value instanceof Number) || value instanceof Float || value instanceof Double)
    {
      com.sun.tools.corba.ee.idl.ParseException.notPositiveInt(scanner, e.rep());
      //e = exprFactory.terminal ("1", new Long (1));
      e = exprFactory.terminal ("1", BigInteger.valueOf (1));
    }
    //else if (((Number)value).longValue () <= 0) {
    //   ParseException.notPositiveInt (scanner, value.toString ());
    //   e = exprFactory.terminal ("1", new Long (1)); }
    else if (((BigInteger)value).compareTo (BigInteger.valueOf (0)) <= 0)
    {
      com.sun.tools.corba.ee.idl.ParseException.notPositiveInt(scanner, value.toString());
      //e = exprFactory.terminal ("1", new Long (1)); <daz>
      e = exprFactory.terminal ("1", BigInteger.valueOf (1));
    }
    return e;
  } // positiveIntConst

  /**
   *
   **/
  private com.sun.tools.corba.ee.idl.SymtabEntry typeDcl (com.sun.tools.corba.ee.idl.SymtabEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    switch (token.type)
    {
      case com.sun.tools.corba.ee.idl.Token.Typedef:
        match (com.sun.tools.corba.ee.idl.Token.Typedef);
        return typeDeclarator (entry);
      case com.sun.tools.corba.ee.idl.Token.Struct:
        return structType (entry);
      case com.sun.tools.corba.ee.idl.Token.Union:
        return unionType (entry);
      case com.sun.tools.corba.ee.idl.Token.Enum:
        return enumType (entry);
      default:
        throw com.sun.tools.corba.ee.idl.ParseException.syntaxError (scanner, new int[]{
                com.sun.tools.corba.ee.idl.Token.Typedef, com.sun.tools.corba.ee.idl.Token.Struct, com.sun.tools.corba.ee.idl.Token.Union, com.sun.tools.corba.ee.idl.Token.Enum}, token.type);
    }
  } // typeDcl

  /**
   *
   **/
  private com.sun.tools.corba.ee.idl.TypedefEntry typeDeclarator (com.sun.tools.corba.ee.idl.SymtabEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    com.sun.tools.corba.ee.idl.TypedefEntry typedefEntry = stFactory.typedefEntry (entry, (com.sun.tools.corba.ee.idl.IDLID)repIDStack.peek ());
    typedefEntry.sourceFile (scanner.fileEntry ());
    // Comment must immediately precede "typedef" keyword
    typedefEntry.comment (tokenHistory.lookBack (1).comment);
    typedefEntry.type (typeSpec (entry));
    Vector typedefList = new Vector ();
    declarators (typedefEntry, typedefList);
    for (Enumeration e = typedefList.elements(); e.hasMoreElements();)
      pigeonhole (entry, (com.sun.tools.corba.ee.idl.SymtabEntry)e.nextElement ());
    return typedefEntry;
  } // typeDeclarator

  /**
   *
   **/
  private com.sun.tools.corba.ee.idl.SymtabEntry typeSpec (com.sun.tools.corba.ee.idl.SymtabEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    return ((token.type == com.sun.tools.corba.ee.idl.Token.Struct) ||
            (token.type == com.sun.tools.corba.ee.idl.Token.Union)  ||
            (token.type == com.sun.tools.corba.ee.idl.Token.Enum))
        ? constrTypeSpec (entry)
        : simpleTypeSpec (entry, true);
  } // typeSpec

  /**
   *
   **/
  private com.sun.tools.corba.ee.idl.SymtabEntry simpleTypeSpec (com.sun.tools.corba.ee.idl.SymtabEntry entry,
    boolean mustBeReferencable ) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    // <f46082.40>
    //if ((token.type == Token.Identifier)  ||
    //    (token.type == Token.DoubleColon) ||
    //    (token.type == Token.Object)) {
    if ((token.type == com.sun.tools.corba.ee.idl.Token.Identifier)  ||
        (token.type == com.sun.tools.corba.ee.idl.Token.DoubleColon) ||
        (token.type == com.sun.tools.corba.ee.idl.Token.Object)      ||
        (token.type == com.sun.tools.corba.ee.idl.Token.ValueBase))
    {
      com.sun.tools.corba.ee.idl.SymtabEntry container = ((entry instanceof com.sun.tools.corba.ee.idl.InterfaceEntry) ||
                               (entry instanceof com.sun.tools.corba.ee.idl.ModuleEntry)    ||
                               (entry instanceof com.sun.tools.corba.ee.idl.StructEntry)    ||
                               (entry instanceof com.sun.tools.corba.ee.idl.UnionEntry))
          ? entry
          : entry.container ();
      return scopedName (container, stFactory.primitiveEntry (),
        mustBeReferencable);
    }
    return ((token.type == com.sun.tools.corba.ee.idl.Token.Sequence) ||
            (token.type == com.sun.tools.corba.ee.idl.Token.String)   ||
            (token.type == com.sun.tools.corba.ee.idl.Token.Wstring))
        ? templateTypeSpec (entry)
        : baseTypeSpec (entry);
  } // simpleTypeSpec

  /**
   *
   **/
  private com.sun.tools.corba.ee.idl.SymtabEntry baseTypeSpec (com.sun.tools.corba.ee.idl.SymtabEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    switch (token.type)
    {
      case com.sun.tools.corba.ee.idl.Token.Float:
      case com.sun.tools.corba.ee.idl.Token.Double:
        return floatingPtType ();
      case com.sun.tools.corba.ee.idl.Token.Long:
      case com.sun.tools.corba.ee.idl.Token.Short:
     case com.sun.tools.corba.ee.idl.Token.Unsigned:
        return integerType (entry);
      case com.sun.tools.corba.ee.idl.Token.Char:
      case com.sun.tools.corba.ee.idl.Token.Wchar:
        return charType ();
      case com.sun.tools.corba.ee.idl.Token.Boolean:
        return booleanType ();
     case com.sun.tools.corba.ee.idl.Token.Octet:
        return octetType ();
      case com.sun.tools.corba.ee.idl.Token.Any:
        return anyType ();
      // NOTE: Object and ValueBase are <base_type_spec>s, but both
      // are processed at simpleTypeSpec(), not here.  parmTypeSpec()
      // directly checks for these types.  Could make baseTypeSpec() do
      // the same
      default:
        throw com.sun.tools.corba.ee.idl.ParseException.syntaxError (scanner, new int[]{
                com.sun.tools.corba.ee.idl.Token.Float, com.sun.tools.corba.ee.idl.Token.Double, com.sun.tools.corba.ee.idl.Token.Long, com.sun.tools.corba.ee.idl.Token.Short,
                com.sun.tools.corba.ee.idl.Token.Unsigned, com.sun.tools.corba.ee.idl.Token.Char, com.sun.tools.corba.ee.idl.Token.Wchar, com.sun.tools.corba.ee.idl.Token.Boolean,
                com.sun.tools.corba.ee.idl.Token.Octet, com.sun.tools.corba.ee.idl.Token.Any}, token.type);
    }
  } // baseTypeSpec

  /**
   *
   **/
  private com.sun.tools.corba.ee.idl.SymtabEntry templateTypeSpec (com.sun.tools.corba.ee.idl.SymtabEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    switch (token.type)
    {
      case com.sun.tools.corba.ee.idl.Token.Sequence:
        return sequenceType (entry);
      case com.sun.tools.corba.ee.idl.Token.String:
      case com.sun.tools.corba.ee.idl.Token.Wstring:
        return stringType (entry);
    }
    throw com.sun.tools.corba.ee.idl.ParseException.syntaxError (scanner, new int[]{com.sun.tools.corba.ee.idl.Token.Sequence, com.sun.tools.corba.ee.idl.Token.String, com.sun.tools.corba.ee.idl.Token.Wstring}, token.type);
  } // templateTypeSpec

  /**
   *
   **/
  private com.sun.tools.corba.ee.idl.SymtabEntry constrTypeSpec (com.sun.tools.corba.ee.idl.SymtabEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    switch (token.type)
    {
      case com.sun.tools.corba.ee.idl.Token.Struct:
        return structType (entry);
      case com.sun.tools.corba.ee.idl.Token.Union:
        return unionType (entry);
      case com.sun.tools.corba.ee.idl.Token.Enum:
        return enumType (entry);
    }
    throw com.sun.tools.corba.ee.idl.ParseException.syntaxError (scanner, new int[]{com.sun.tools.corba.ee.idl.Token.Struct, com.sun.tools.corba.ee.idl.Token.Union, com.sun.tools.corba.ee.idl.Token.Enum}, token.type);
  } // constrTypeSpec

  /**
   *
   **/
  private void declarators (com.sun.tools.corba.ee.idl.TypedefEntry entry, Vector list) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    for (; ; match (com.sun.tools.corba.ee.idl.Token.Comma))
    {
      com.sun.tools.corba.ee.idl.TypedefEntry newEntry = (com.sun.tools.corba.ee.idl.TypedefEntry)entry.clone ();
      declarator (newEntry);
      if (isntInList (list, newEntry.name ()))
        list.addElement (newEntry);
      if (token.type != com.sun.tools.corba.ee.idl.Token.Comma)
        break;
    }
  } // declarators

  /**
   *
   **/
  private void declarator (com.sun.tools.corba.ee.idl.TypedefEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    entry.name (token.name);
    // If the declarator is commented then override the comment cloned from the parent
    // entry. <08aug1997daz>
    if (!token.comment.text ().equals (""))
      entry.comment (token.comment);
    match (com.sun.tools.corba.ee.idl.Token.Identifier);
    while (token.type == com.sun.tools.corba.ee.idl.Token.LeftBracket)
      fixedArraySize (entry);
  } // declarator

  /**
   *
   **/
  private com.sun.tools.corba.ee.idl.PrimitiveEntry floatingPtType () throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    String name = "double";
    if (token.type == com.sun.tools.corba.ee.idl.Token.Float)
    {
      match (com.sun.tools.corba.ee.idl.Token.Float);
      name = "float";
    }
    else if (token.type == com.sun.tools.corba.ee.idl.Token.Double)
      match (com.sun.tools.corba.ee.idl.Token.Double);
    else
    {
      int [] expected = {com.sun.tools.corba.ee.idl.Token.Float, com.sun.tools.corba.ee.idl.Token.Double};
      com.sun.tools.corba.ee.idl.ParseException.syntaxError (scanner, new int[]{com.sun.tools.corba.ee.idl.Token.Float, com.sun.tools.corba.ee.idl.Token.Double}, token.type);
    }
    com.sun.tools.corba.ee.idl.PrimitiveEntry ret = null;
    try
    {
      ret = (com.sun.tools.corba.ee.idl.PrimitiveEntry)qualifiedEntry (name);
    }
    catch (ClassCastException exception)
    {
      com.sun.tools.corba.ee.idl.ParseException.undeclaredType(scanner, name);
    }
    return ret;
  } // floatingPtType

  /**
   *
   **/
  private com.sun.tools.corba.ee.idl.PrimitiveEntry integerType (com.sun.tools.corba.ee.idl.SymtabEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    String name = "";
    if (token.type == com.sun.tools.corba.ee.idl.Token.Unsigned)
    {
      match (com.sun.tools.corba.ee.idl.Token.Unsigned);
      name = "unsigned ";
    }
    name += signedInt();
    com.sun.tools.corba.ee.idl.PrimitiveEntry ret = null;
    try
    {
      ret = (com.sun.tools.corba.ee.idl.PrimitiveEntry) qualifiedEntry (name);
    }
    catch (ClassCastException exception)
    {
      com.sun.tools.corba.ee.idl.ParseException.undeclaredType(scanner, name);
    }
    return ret;
  } // integerType

  /**
   *
   **/
  private String signedInt () throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    String ret = "long";
    if (token.type == com.sun.tools.corba.ee.idl.Token.Long)
    {
      match (com.sun.tools.corba.ee.idl.Token.Long);
      // <signedInt'> ::= "long" | e
      if (token.type == com.sun.tools.corba.ee.idl.Token.Long)
      {
        ret = "long long";
        match (com.sun.tools.corba.ee.idl.Token.Long);
      }
    }
    else if (token.type == com.sun.tools.corba.ee.idl.Token.Short)
    {
      ret = "short";
      match (com.sun.tools.corba.ee.idl.Token.Short);
    }
    else
      com.sun.tools.corba.ee.idl.ParseException.syntaxError (scanner, new int[]{com.sun.tools.corba.ee.idl.Token.Long, com.sun.tools.corba.ee.idl.Token.Short}, token.type);
    return ret;
  } // signedInt

  /**
   *
   **/
  private com.sun.tools.corba.ee.idl.PrimitiveEntry charType () throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    String tokenName;
    if (token.type == com.sun.tools.corba.ee.idl.Token.Char)
    {
      match (com.sun.tools.corba.ee.idl.Token.Char);
      tokenName = "char";
    }
    else
    {
      match (com.sun.tools.corba.ee.idl.Token.Wchar);
      tokenName = "wchar";
    }
    com.sun.tools.corba.ee.idl.PrimitiveEntry ret = null;
    try
    {
      ret = (com.sun.tools.corba.ee.idl.PrimitiveEntry) qualifiedEntry (tokenName);
    }
    catch (ClassCastException exception)
    {
      com.sun.tools.corba.ee.idl.ParseException.undeclaredType(scanner, overrideName(tokenName));
    }
    return ret;
  } // charType

  /**
   *
   **/
  private com.sun.tools.corba.ee.idl.PrimitiveEntry booleanType () throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    com.sun.tools.corba.ee.idl.PrimitiveEntry ret = null;
    match (com.sun.tools.corba.ee.idl.Token.Boolean);
    try
    {
      ret = (com.sun.tools.corba.ee.idl.PrimitiveEntry) qualifiedEntry ("boolean");
    }
    catch (ClassCastException exception)
    {
      com.sun.tools.corba.ee.idl.ParseException.undeclaredType(scanner, overrideName("boolean"));
    }
    return ret;
  } // booleanType

  /**
   *
   **/
  private com.sun.tools.corba.ee.idl.PrimitiveEntry octetType () throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    com.sun.tools.corba.ee.idl.PrimitiveEntry ret = null;
    match (com.sun.tools.corba.ee.idl.Token.Octet);
    try
    {
      ret = (com.sun.tools.corba.ee.idl.PrimitiveEntry) qualifiedEntry ("octet");
    }
    catch (ClassCastException exception)
    {
      com.sun.tools.corba.ee.idl.ParseException.undeclaredType(scanner, overrideName("octet"));
    }
    return ret;
  } // octetType

  /**
   *
   **/
  private com.sun.tools.corba.ee.idl.SymtabEntry anyType () throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    match (com.sun.tools.corba.ee.idl.Token.Any);
    try
    {
      return qualifiedEntry ("any");
    }
    catch (ClassCastException exception)
    {
      com.sun.tools.corba.ee.idl.ParseException.undeclaredType(scanner, overrideName("any"));
      return null;
    }
  } // anyType

  /**
   *
   **/
  private com.sun.tools.corba.ee.idl.StructEntry structType (com.sun.tools.corba.ee.idl.SymtabEntry entry) throws IOException,
          com.sun.tools.corba.ee.idl.ParseException
  {
    match (com.sun.tools.corba.ee.idl.Token.Struct);
    String name = token.name;
    match (com.sun.tools.corba.ee.idl.Token.Identifier);
    com.sun.tools.corba.ee.idl.StructEntry structEntry = null ;

    if (token.type == com.sun.tools.corba.ee.idl.Token.LeftBrace) {
      repIDStack.push(((com.sun.tools.corba.ee.idl.IDLID)repIDStack.peek ()).clone ()) ;
//!!!      ((com.sun.tools.corba.ee.idl.IDLID)repIDStack.peek ()).appendToName (name);
      structEntry = makeStructEntry( name, entry, false ) ;
      prep.openScope (structEntry);
      match (com.sun.tools.corba.ee.idl.Token.LeftBrace) ;
      member (structEntry) ;
      memberList2 (structEntry) ;
      prep.closeScope (structEntry);
      match (com.sun.tools.corba.ee.idl.Token.RightBrace) ;
      repIDStack.pop() ;
    } else if (token.equals( com.sun.tools.corba.ee.idl.Token.Semicolon )) {
      structEntry = makeStructEntry( name, entry, true ) ; 
    } else {
      throw com.sun.tools.corba.ee.idl.ParseException.syntaxError (scanner,
              new int[]{com.sun.tools.corba.ee.idl.Token.Semicolon, com.sun.tools.corba.ee.idl.Token.LeftBrace}, token.type);
    }
    return structEntry;
  } // structType

  private com.sun.tools.corba.ee.idl.StructEntry makeStructEntry( String name, com.sun.tools.corba.ee.idl.SymtabEntry entry,
    boolean isForward )  
  {
    com.sun.tools.corba.ee.idl.StructEntry structEntry = stFactory.structEntry (entry,
      (com.sun.tools.corba.ee.idl.IDLID)repIDStack.peek () );
    structEntry.isReferencable( !isForward ) ;
    structEntry.sourceFile (scanner.fileEntry ());
    structEntry.name (name);
    // Comment must immediately preceed "struct" keyword
    structEntry.comment (tokenHistory.lookBack (1).comment);
    pigeonhole( entry, structEntry ) ; 
    return structEntry ;
  }

  /**
   *
   **/
  private void memberList2 (com.sun.tools.corba.ee.idl.StructEntry entry) throws IOException
  {
    while (token.type != com.sun.tools.corba.ee.idl.Token.RightBrace)
      member (entry);
  } // memberList2

  /**
   *
   **/
  private void member (com.sun.tools.corba.ee.idl.StructEntry entry) throws IOException
  {
    com.sun.tools.corba.ee.idl.TypedefEntry newEntry = stFactory.typedefEntry(entry, (com.sun.tools.corba.ee.idl.IDLID)repIDStack.peek());
    newEntry.sourceFile (scanner.fileEntry ());
    // comment must immediately precede <type_spec> lexeme
    newEntry.comment (token.comment);
    try
    {
      newEntry.type (typeSpec (entry));
      if (newEntry.type () == entry)
        throw com.sun.tools.corba.ee.idl.ParseException.recursive(scanner, entry.fullName(),
                (token.name == null) ? "" : token.name);
      // <d46094> Exception cannot appear within a struct, union, or exception
      if (typeOf (newEntry) instanceof com.sun.tools.corba.ee.idl.ExceptionEntry)
        throw com.sun.tools.corba.ee.idl.ParseException.illegalException(scanner, entryName(entry));
      declarators (newEntry, entry.members ());
      match (com.sun.tools.corba.ee.idl.Token.Semicolon);
    }
    catch (com.sun.tools.corba.ee.idl.ParseException exception)
    {
      skipToSemicolon ();
    }
  } // member

  /**
   *
   **/
  private final boolean isConstTypeSpec (com.sun.tools.corba.ee.idl.Token t)
  {
    return (t.type == com.sun.tools.corba.ee.idl.Token.Struct || t.type == com.sun.tools.corba.ee.idl.Token.Union || t.type == com.sun.tools.corba.ee.idl.Token.Enum);
  } // isConstTypeSpec

  /**
   *
   **/
  private com.sun.tools.corba.ee.idl.UnionEntry unionType (com.sun.tools.corba.ee.idl.SymtabEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    match (com.sun.tools.corba.ee.idl.Token.Union) ;
    String name = token.name ;
    match (com.sun.tools.corba.ee.idl.Token.Identifier) ;
    com.sun.tools.corba.ee.idl.UnionEntry unionEntry = null ;

    if (token.type == com.sun.tools.corba.ee.idl.Token.Switch) {
      repIDStack.push (((com.sun.tools.corba.ee.idl.IDLID)repIDStack.peek ()).clone ());
//!!!      ((com.sun.tools.corba.ee.idl.IDLID)repIDStack.peek ()).appendToName (name);
      unionEntry = makeUnionEntry( name, entry, false ) ;
      match (com.sun.tools.corba.ee.idl.Token.Switch);
      match (com.sun.tools.corba.ee.idl.Token.LeftParen);
      unionEntry.type (switchTypeSpec (unionEntry));
      match (com.sun.tools.corba.ee.idl.Token.RightParen);
      prep.openScope (unionEntry);
      match (com.sun.tools.corba.ee.idl.Token.LeftBrace);
      switchBody (unionEntry);
      verifyUnion (unionEntry);
      prep.closeScope (unionEntry);
      match (com.sun.tools.corba.ee.idl.Token.RightBrace);
      repIDStack.pop ();
    } else if (token.equals( com.sun.tools.corba.ee.idl.Token.Semicolon )) {
      unionEntry = makeUnionEntry( name, entry, true ) ;
    } else {
      throw com.sun.tools.corba.ee.idl.ParseException.syntaxError (scanner,
              new int[]{com.sun.tools.corba.ee.idl.Token.Semicolon, com.sun.tools.corba.ee.idl.Token.Switch}, token.type);
    }
     
    return unionEntry ;
  } // unionType

  private com.sun.tools.corba.ee.idl.UnionEntry makeUnionEntry( String name, com.sun.tools.corba.ee.idl.SymtabEntry entry,
    boolean isForward )  
  {
    com.sun.tools.corba.ee.idl.UnionEntry unionEntry = stFactory.unionEntry (entry,
      (com.sun.tools.corba.ee.idl.IDLID)repIDStack.peek () );
    unionEntry.isReferencable( !isForward ) ;
    unionEntry.sourceFile (scanner.fileEntry ());
    unionEntry.name (name);
    // Comment must immediately preceed "union" keyword
    unionEntry.comment (tokenHistory.lookBack (1).comment);
    pigeonhole( entry, unionEntry ) ; 
    return unionEntry ;
  }

  /**
   *
   **/
  private void verifyUnion (com.sun.tools.corba.ee.idl.UnionEntry u)
  {
    if (u.typeName ().equals (overrideName ("boolean")))
    {
      if (caseCount (u) > 2)
        com.sun.tools.corba.ee.idl.ParseException.noDefault(scanner);
    }
    else if (u.type () instanceof com.sun.tools.corba.ee.idl.EnumEntry)
    {
      if (caseCount (u) > ((com.sun.tools.corba.ee.idl.EnumEntry)u.type ()).elements ().size ())
        com.sun.tools.corba.ee.idl.ParseException.noDefault(scanner);
    }
  } // verifyUnion

  /**
   *
   **/
  private long caseCount (com.sun.tools.corba.ee.idl.UnionEntry u)
  {
    long cases = 0;
    Enumeration branches = u.branches ().elements ();
    while (branches.hasMoreElements ())
    {
      com.sun.tools.corba.ee.idl.UnionBranch branch = (com.sun.tools.corba.ee.idl.UnionBranch)branches.nextElement ();
      cases += branch.labels.size ();
      if (branch.isDefault)
        ++cases;
    }
    return cases;
  } // caseCount

  /**
   *
   **/
  private com.sun.tools.corba.ee.idl.SymtabEntry switchTypeSpec (com.sun.tools.corba.ee.idl.UnionEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    com.sun.tools.corba.ee.idl.SymtabEntry ret = null;
    switch (token.type)
    {
       case com.sun.tools.corba.ee.idl.Token.Long:
       case com.sun.tools.corba.ee.idl.Token.Short:
       case com.sun.tools.corba.ee.idl.Token.Unsigned:
         return integerType (entry);
       case com.sun.tools.corba.ee.idl.Token.Char:
       case com.sun.tools.corba.ee.idl.Token.Wchar:
         return charType();
       case com.sun.tools.corba.ee.idl.Token.Boolean:
         return booleanType();
       case com.sun.tools.corba.ee.idl.Token.Enum:
         return enumType (entry);
       case com.sun.tools.corba.ee.idl.Token.Identifier:
       case com.sun.tools.corba.ee.idl.Token.DoubleColon:
         ret = scopedName (entry, stFactory.primitiveEntry ());
         if (hasArrayInfo (entry.type ()))
           com.sun.tools.corba.ee.idl.ParseException.illegalArray(scanner, "switch");
         com.sun.tools.corba.ee.idl.SymtabEntry retType = typeOf (ret);
         if (!(retType instanceof com.sun.tools.corba.ee.idl.EnumEntry || retType instanceof com.sun.tools.corba.ee.idl.PrimitiveEntry))
           com.sun.tools.corba.ee.idl.ParseException.wrongType(scanner, ret.fullName(),
                   "long, unsigned long, short, unsigned short, char, boolean, enum",
                   entryName(ret.type()));
         else if (ret instanceof com.sun.tools.corba.ee.idl.PrimitiveEntry)
         {
           com.sun.tools.corba.ee.idl.SymtabEntry octet = qualifiedEntry ("octet");
           com.sun.tools.corba.ee.idl.SymtabEntry flt   = qualifiedEntry ("float");
           com.sun.tools.corba.ee.idl.SymtabEntry dbl   = qualifiedEntry ("double");
           if (retType == octet || retType == flt || retType == dbl)
             com.sun.tools.corba.ee.idl.ParseException.wrongType(scanner, ret.fullName(),
                     "long, unsigned long, short, unsigned short, char, boolean, enum",
                     entryName(ret.type()));
         }
         break;
       default:
         throw com.sun.tools.corba.ee.idl.ParseException.syntaxError (scanner, new int[]{
                 com.sun.tools.corba.ee.idl.Token.Long, com.sun.tools.corba.ee.idl.Token.Short, com.sun.tools.corba.ee.idl.Token.Unsigned, com.sun.tools.corba.ee.idl.Token.Char,
                 com.sun.tools.corba.ee.idl.Token.Boolean, com.sun.tools.corba.ee.idl.Token.Enum, com.sun.tools.corba.ee.idl.Token.Identifier,
                 com.sun.tools.corba.ee.idl.Token.DoubleColon}, token.type);
    }
    return ret;
  } // switchTypeSpec

  // This is only used by the union methods
  com.sun.tools.corba.ee.idl.UnionBranch defaultBranch = null;

  /**
   *
   **/
  private void switchBody (com.sun.tools.corba.ee.idl.UnionEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    caseProd (entry);
    while (!token.equals (com.sun.tools.corba.ee.idl.Token.RightBrace))
      caseProd (entry);
    entry.defaultBranch ((defaultBranch == null) ? null : defaultBranch.typedef);
    defaultBranch = null;
  } // switchBody

  /**
   *
   **/
  private void caseProd (com.sun.tools.corba.ee.idl.UnionEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    com.sun.tools.corba.ee.idl.UnionBranch branch = new com.sun.tools.corba.ee.idl.UnionBranch();
    entry.addBranch (branch);
    caseLabel (entry, branch);
    while (token.equals (com.sun.tools.corba.ee.idl.Token.Case) || token.equals (com.sun.tools.corba.ee.idl.Token.Default))
      caseLabel (entry, branch);
    elementSpec (entry, branch);
    match (com.sun.tools.corba.ee.idl.Token.Semicolon);
  } // caseProd

  /**
   *
   **/
  private void caseLabel (com.sun.tools.corba.ee.idl.UnionEntry entry, com.sun.tools.corba.ee.idl.UnionBranch branch) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    if (token.type == com.sun.tools.corba.ee.idl.Token.Case)
    {
      match (com.sun.tools.corba.ee.idl.Token.Case);
      com.sun.tools.corba.ee.idl.ConstEntry tmpEntry = stFactory.constEntry (entry, (com.sun.tools.corba.ee.idl.IDLID)repIDStack.peek ());
      tmpEntry.sourceFile (scanner.fileEntry ());
      tmpEntry.type (entry);

      com.sun.tools.corba.ee.idl.constExpr.Expression label;
      com.sun.tools.corba.ee.idl.SymtabEntry type = typeOf (entry.type ());
      if (type instanceof com.sun.tools.corba.ee.idl.EnumEntry)
        label = matchEnum ((com.sun.tools.corba.ee.idl.EnumEntry)type);
      else
      {
        label = constExp (tmpEntry);
        verifyConstType (label, type);
      }
      if (entry.has (label))
        com.sun.tools.corba.ee.idl.ParseException.branchLabel(scanner, label.rep());
      branch.labels.addElement (label);
      match (com.sun.tools.corba.ee.idl.Token.Colon);
    }
    else if (token.type == com.sun.tools.corba.ee.idl.Token.Default)
    {
      match (com.sun.tools.corba.ee.idl.Token.Default);
      match (com.sun.tools.corba.ee.idl.Token.Colon);
      if (entry.defaultBranch () != null)
        com.sun.tools.corba.ee.idl.ParseException.alreadyDefaulted(scanner);
      branch.isDefault = true;
      defaultBranch    = branch;
    }
    else
      throw com.sun.tools.corba.ee.idl.ParseException.syntaxError (scanner, new int[]{com.sun.tools.corba.ee.idl.Token.Case, com.sun.tools.corba.ee.idl.Token.Default}, token.type);
  } // caselabel

  /**
   *
   **/
  private com.sun.tools.corba.ee.idl.constExpr.Expression matchEnum (com.sun.tools.corba.ee.idl.EnumEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    // Get the symbol table entry for the case label based on the
    // scope of the EnumEntry, NOT the UnionEntry (the union could be
    // in a different scope than the enum).  Given
    // module M { enum E {A, B, C, D}; };
    // a case label for A could be one of the following:
    // case A:
    // case M::A:
    // case ::M::A:
    com.sun.tools.corba.ee.idl.SymtabEntry label = scopedName (entry.container(), new com.sun.tools.corba.ee.idl.SymtabEntry());
    return exprFactory.terminal (label.name (), false);
  } // matchEnum

  /**
   *
   **/
  private void elementSpec (com.sun.tools.corba.ee.idl.UnionEntry entry, com.sun.tools.corba.ee.idl.UnionBranch branch) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    com.sun.tools.corba.ee.idl.TypedefEntry typedef = stFactory.typedefEntry (entry, (com.sun.tools.corba.ee.idl.IDLID)repIDStack.peek ());
    typedef.sourceFile (scanner.fileEntry ());
    // Comment must immediately precede <type_spec> lexeme
    typedef.comment (token.comment);
    typedef.type (typeSpec (entry));
    if (typedef.type () == entry)
      throw com.sun.tools.corba.ee.idl.ParseException.recursive(scanner, entry.fullName(), (token.name == null) ? "" : token.name);
    // <d46094> Exception cannot appear within a struct, union, or exception
    if (typeOf (typedef) instanceof com.sun.tools.corba.ee.idl.ExceptionEntry)
      throw com.sun.tools.corba.ee.idl.ParseException.illegalException(scanner, entryName(entry));
    declarator (typedef);
    branch.typedef = typedef;
    // Ensure a branch with the same name doesn't already exist.
    if (entry.has (typedef))
      com.sun.tools.corba.ee.idl.ParseException.branchName(scanner, typedef.name());
  } // elementSpec

  /**
   *
   **/
  private com.sun.tools.corba.ee.idl.EnumEntry enumType (com.sun.tools.corba.ee.idl.SymtabEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    match (com.sun.tools.corba.ee.idl.Token.Enum);
    com.sun.tools.corba.ee.idl.EnumEntry enumEntry = newEnumEntry (entry);
    // comment must immediately precede "enum" keyword
    enumEntry.comment (tokenHistory.lookBack (1).comment);
    enumEntry.name (token.name);
    match (com.sun.tools.corba.ee.idl.Token.Identifier);
    prep.openScope (enumEntry);
    match (com.sun.tools.corba.ee.idl.Token.LeftBrace);
    if (isntInStringList (enumEntry.elements (), token.name))
    {
      enumEntry.addElement (token.name);
      com.sun.tools.corba.ee.idl.SymtabEntry element = new com.sun.tools.corba.ee.idl.SymtabEntry(entry, (com.sun.tools.corba.ee.idl.IDLID)repIDStack.peek ());
      // if block taken from EnumEntry ctor
      if (element.module ().equals (""))
        element.module (element.name ());
      else if (!element.name ().equals (""))
        element.module (element.module () + "/" + element.name ());
      element.name (token.name);
      // <d50237> Place the SymtabEntry representing this enumeration
      // contant into the SymtabEntry defining its scope (e.g., InterfaceEntry,
      // ValueEntry, etc.) rather than the SymtabEntry passed in, which
      // may not define the contant's scope (e.g., TypedefEntry).
      //pigeonhole (entry, element); } <daz>
      pigeonhole (enumEntry.container (), element);
    }
    match (com.sun.tools.corba.ee.idl.Token.Identifier);
    enumType2 (enumEntry);
    prep.closeScope (enumEntry);
    match (com.sun.tools.corba.ee.idl.Token.RightBrace);
    return enumEntry;
  } // enumType

  /**
   *
   **/
  private void enumType2 (com.sun.tools.corba.ee.idl.EnumEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    while (token.type == com.sun.tools.corba.ee.idl.Token.Comma)
    {
      match (com.sun.tools.corba.ee.idl.Token.Comma);
      String name = token.name;
      match (com.sun.tools.corba.ee.idl.Token.Identifier);
      if (isntInStringList (entry.elements (), name))
      {
        entry.addElement (name);
        com.sun.tools.corba.ee.idl.SymtabEntry element = new com.sun.tools.corba.ee.idl.SymtabEntry(entry.container (), (com.sun.tools.corba.ee.idl.IDLID)repIDStack.peek ());
        // if block taken from EnumEntry ctor:
        if (element.module ().equals (""))
          element.module (element.name ());
        else if (!element.name().equals (""))
          element.module (element.module () + "/" + element.name ());
        element.name (name);
        pigeonhole (entry.container  (), element);
      }
    }
  } // enumType2

  /**
   *
   **/
  private com.sun.tools.corba.ee.idl.SequenceEntry sequenceType (com.sun.tools.corba.ee.idl.SymtabEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    match (com.sun.tools.corba.ee.idl.Token.Sequence);
    match (com.sun.tools.corba.ee.idl.Token.LessThan);

    com.sun.tools.corba.ee.idl.SequenceEntry newEntry = newSequenceEntry (entry);
    com.sun.tools.corba.ee.idl.SymtabEntry tsentry = simpleTypeSpec (newEntry, false );
    newEntry.type (tsentry);
    if (!tsentry.isReferencable()) {
        // This is a sequence type that is referencing an
        // incomplete forward declaration of a struct or
        // union.  Save the sequence in a list for later
        // backpatching.
        try {
            List fwdTypes = (List)tsentry.dynamicVariable( ftlKey ) ;
            if (fwdTypes == null) {
                fwdTypes = new ArrayList() ;
                tsentry.dynamicVariable( ftlKey, fwdTypes ) ;
            }
            fwdTypes.add( newEntry ) ;
        } catch (NoSuchFieldException exc) {
            throw new IllegalStateException() ; 
        }
    }

    if (token.type == com.sun.tools.corba.ee.idl.Token.Comma)
    {
      match (com.sun.tools.corba.ee.idl.Token.Comma);
      com.sun.tools.corba.ee.idl.ConstEntry tmpEntry = stFactory.constEntry (newEntry, (com.sun.tools.corba.ee.idl.IDLID)repIDStack.peek ());
      tmpEntry.sourceFile (scanner.fileEntry ());
      tmpEntry.type (qualifiedEntry ("long"));
      newEntry.maxSize (positiveIntConst (tmpEntry));
      verifyConstType (newEntry.maxSize(), qualifiedEntry ("long"));
    }
    match (com.sun.tools.corba.ee.idl.Token.GreaterThan);
    return newEntry;
  } // sequenceType

  /**
   *
   **/
  private com.sun.tools.corba.ee.idl.StringEntry stringType (com.sun.tools.corba.ee.idl.SymtabEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    com.sun.tools.corba.ee.idl.StringEntry string = stFactory.stringEntry ();
    if (token.type == com.sun.tools.corba.ee.idl.Token.String)
    {
      string.name (overrideName ("string"));
      match (com.sun.tools.corba.ee.idl.Token.String);
    }
    else
    {
      string.name (overrideName ("wstring"));
      match (com.sun.tools.corba.ee.idl.Token.Wstring);
    }
    string.maxSize (stringType2 (entry));
    return string;
  } // stringType

  /**
   *
   **/
  private com.sun.tools.corba.ee.idl.constExpr.Expression stringType2 (com.sun.tools.corba.ee.idl.SymtabEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    if (token.type == com.sun.tools.corba.ee.idl.Token.LessThan)
    {
      match (com.sun.tools.corba.ee.idl.Token.LessThan);

      // START IBM.11417 failure in the IDL compiler
      //Expression maxSize = positiveIntConst (entry);   IBM.11417

      com.sun.tools.corba.ee.idl.ConstEntry tmpEntry = stFactory.constEntry (entry, (com.sun.tools.corba.ee.idl.IDLID)repIDStack.peek
());
      tmpEntry.sourceFile (scanner.fileEntry ());
      tmpEntry.type (qualifiedEntry ("long"));
      com.sun.tools.corba.ee.idl.constExpr.Expression maxSize = positiveIntConst (tmpEntry);

      // END IBM.11417

      verifyConstType (maxSize, qualifiedEntry ("long"));
      match (com.sun.tools.corba.ee.idl.Token.GreaterThan);
      return maxSize;
    }
    return null;
  } // stringType2

  /**
   *
   **/
  private void fixedArraySize (com.sun.tools.corba.ee.idl.TypedefEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    match (com.sun.tools.corba.ee.idl.Token.LeftBracket);
    com.sun.tools.corba.ee.idl.ConstEntry tmpEntry = stFactory.constEntry (entry, (com.sun.tools.corba.ee.idl.IDLID)repIDStack.peek ());
    tmpEntry.sourceFile (scanner.fileEntry ());
    // <d58058> Set type of tmpExpr to "long", which is the array index type.
    // Previously, this type was erroneously set to the array element type.
    //tmpEntry.type (entry.type ());
    tmpEntry.type (qualifiedEntry ("long"));
    com.sun.tools.corba.ee.idl.constExpr.Expression expr = positiveIntConst (tmpEntry);
    entry.addArrayInfo (expr);
    verifyConstType (expr, qualifiedEntry ("long"));
    match (com.sun.tools.corba.ee.idl.Token.RightBracket);
  } // fixedArraySize

  /**
   *
   **/
  private void attrDcl (com.sun.tools.corba.ee.idl.InterfaceEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    com.sun.tools.corba.ee.idl.AttributeEntry attribute = stFactory.attributeEntry (entry, (com.sun.tools.corba.ee.idl.IDLID)repIDStack.peek ());
    attribute.sourceFile (scanner.fileEntry ());
    // Comment must immediately precede the "attribute" keyword.  Save the
    // comment preceding the declaration for use below.
    attribute.comment (token.comment);
    com.sun.tools.corba.ee.idl.Comment dclComment = attribute.comment ();

    if (token.type == com.sun.tools.corba.ee.idl.Token.Readonly)
    {
      match (com.sun.tools.corba.ee.idl.Token.Readonly);
      attribute.readOnly (true);
    }
    match (com.sun.tools.corba.ee.idl.Token.Attribute);
    attribute.type (paramTypeSpec (attribute));
    attribute.name (token.name);
    // Override declaration comment if attribute identifier is commented
    if (!token.comment.text ().equals (""))
      attribute.comment (token.comment);
    entry.methodsAddElement (attribute, scanner);
    pigeonholeMethod (entry, attribute);
    // Declaration comment was overriden:
    if (!token.comment.text ().equals (""))
    {
      // Create a temporary attribute with declaration comment so cloning in
      // attrdcl2() can use declaration comment as default.
      com.sun.tools.corba.ee.idl.AttributeEntry attributeClone = (com.sun.tools.corba.ee.idl.AttributeEntry) attribute.clone ();
      attributeClone.comment (dclComment);

      match (com.sun.tools.corba.ee.idl.Token.Identifier);
      attrDcl2 (entry, attributeClone);
    }
    else
    {
      match (com.sun.tools.corba.ee.idl.Token.Identifier);
      attrDcl2 (entry, attribute);
    }
    //match (Token.Identifier);
    //attrDcl2 (entry, attribute);
  } // attrDcl

  /**
   *
   **/
  private void attrDcl2 (com.sun.tools.corba.ee.idl.InterfaceEntry entry, com.sun.tools.corba.ee.idl.AttributeEntry clone)
          throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    while (token.type == com.sun.tools.corba.ee.idl.Token.Comma)
    {
      match (com.sun.tools.corba.ee.idl.Token.Comma);
      com.sun.tools.corba.ee.idl.AttributeEntry attribute = (com.sun.tools.corba.ee.idl.AttributeEntry)clone.clone ();
      attribute.name (token.name);
      // Override the declaration comment (i.e., that preceding the
      // "attribute" keyword) if the attribute identifier is commented.
      if (!token.comment.text ().equals (""))
        attribute.comment (token.comment);
      entry.methodsAddElement (attribute, scanner);
      pigeonholeMethod (entry, attribute);
      match (com.sun.tools.corba.ee.idl.Token.Identifier);
    }
  } // attrDcl2

  /**
   *
   **/
  private void exceptDcl (com.sun.tools.corba.ee.idl.SymtabEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    match (com.sun.tools.corba.ee.idl.Token.Exception);
    com.sun.tools.corba.ee.idl.ExceptionEntry exceptEntry = stFactory.exceptionEntry (entry, (com.sun.tools.corba.ee.idl.IDLID)repIDStack.peek ());
    repIDStack.push (((com.sun.tools.corba.ee.idl.IDLID)repIDStack.peek ()).clone ());
    ((com.sun.tools.corba.ee.idl.IDLID)repIDStack.peek ()).appendToName (token.name);
    exceptEntry.sourceFile (scanner.fileEntry ());
    // Comment must immediately precede "exception" keyword
    exceptEntry.comment (tokenHistory.lookBack (1).comment);
    exceptEntry.name (token.name);
    match (com.sun.tools.corba.ee.idl.Token.Identifier);
    pigeonhole (entry, exceptEntry);
    if (token.equals (com.sun.tools.corba.ee.idl.Token.LeftBrace))
    {
      prep.openScope (exceptEntry);
      match (com.sun.tools.corba.ee.idl.Token.LeftBrace);
      memberList2 (exceptEntry);
      prep.closeScope (exceptEntry);
      match (com.sun.tools.corba.ee.idl.Token.RightBrace);
      repIDStack.pop ();
    }
    else
      throw com.sun.tools.corba.ee.idl.ParseException.syntaxError (scanner, com.sun.tools.corba.ee.idl.Token.LeftBrace, token.type);
  } // exceptDcl

  /**
   *
   **/
  private void opDcl (com.sun.tools.corba.ee.idl.InterfaceEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    com.sun.tools.corba.ee.idl.MethodEntry method = stFactory.methodEntry (entry, (com.sun.tools.corba.ee.idl.IDLID)repIDStack.peek ());
    method.sourceFile (scanner.fileEntry ());
    // Comment must immediately precede "oneway" keyword or <return_type_spec>
    method.comment (token.comment);
    if (token.type == com.sun.tools.corba.ee.idl.Token.Oneway)
    {
      match (com.sun.tools.corba.ee.idl.Token.Oneway);
      method.oneway (true);
    }
    method.type (opTypeSpec (method));
    repIDStack.push (((com.sun.tools.corba.ee.idl.IDLID)repIDStack.peek ()).clone ());
    ((com.sun.tools.corba.ee.idl.IDLID)repIDStack.peek ()).appendToName (token.name);
    method.name (token.name);
    entry.methodsAddElement (method, scanner);
    pigeonholeMethod (entry, method);
    opDcl2 (method);
    if (method.oneway ())
      checkIfOpLegalForOneway (method);
    repIDStack.pop ();
  } // opDcl

  /**
   *
   **/
  private void checkIfOpLegalForOneway (com.sun.tools.corba.ee.idl.MethodEntry method)
  {
    boolean notLegal = false;
    if ((method.type() != null) ||
         (method.exceptions().size() != 0)) notLegal = true;
    else
    {
      for (Enumeration e = method.parameters().elements(); e.hasMoreElements();)
      {
        if (((com.sun.tools.corba.ee.idl.ParameterEntry)e.nextElement ()).passType () != com.sun.tools.corba.ee.idl.ParameterEntry.In)
        {
          notLegal = true;
          break;
        }
      }
    }
    if (notLegal)
      com.sun.tools.corba.ee.idl.ParseException.oneway(scanner, method.name());
  } // checkifOpLegalForOneway

  /**
   *
   **/
  private void opDcl2 (com.sun.tools.corba.ee.idl.MethodEntry method) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    if (token.equals (com.sun.tools.corba.ee.idl.Token.MacroIdentifier))
    {
      match (com.sun.tools.corba.ee.idl.Token.MacroIdentifier);
      parameterDcls2 (method);
    }
    else
    {
      match (com.sun.tools.corba.ee.idl.Token.Identifier);
      parameterDcls (method);
     }
    opDcl3 (method);
  } // opDcl2

  /**
   *
   **/
  private void opDcl3 (com.sun.tools.corba.ee.idl.MethodEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    if (token.type != com.sun.tools.corba.ee.idl.Token.Semicolon)
    {
      if (!token.equals (com.sun.tools.corba.ee.idl.Token.Raises) && !token.equals (com.sun.tools.corba.ee.idl.Token.Context))
        throw com.sun.tools.corba.ee.idl.ParseException.syntaxError (scanner, new int[]{
                com.sun.tools.corba.ee.idl.Token.Raises, com.sun.tools.corba.ee.idl.Token.Context, com.sun.tools.corba.ee.idl.Token.Semicolon}, token.type);
      if (token.type == com.sun.tools.corba.ee.idl.Token.Raises)
        raisesExpr (entry);
      if (token.type == com.sun.tools.corba.ee.idl.Token.Context)
        contextExpr (entry);
    }
  } // opDcl3

  /**
   *
   **/
  private com.sun.tools.corba.ee.idl.SymtabEntry opTypeSpec (com.sun.tools.corba.ee.idl.SymtabEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    com.sun.tools.corba.ee.idl.SymtabEntry ret = null;
    if (token.type == com.sun.tools.corba.ee.idl.Token.Void)
      match (com.sun.tools.corba.ee.idl.Token.Void);
    else
      ret = paramTypeSpec (entry);
    return ret;
  } // opTypeSpec

  /**
   *
   **/
  private void parameterDcls (com.sun.tools.corba.ee.idl.MethodEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    match (com.sun.tools.corba.ee.idl.Token.LeftParen);
    parameterDcls2 (entry);
  } // parameterDcls

  /**
   *
   **/
  private void parameterDcls2 (com.sun.tools.corba.ee.idl.MethodEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    if (token.type == com.sun.tools.corba.ee.idl.Token.RightParen)
      match (com.sun.tools.corba.ee.idl.Token.RightParen);
    else
    {
      paramDcl (entry);
      while (token.type == com.sun.tools.corba.ee.idl.Token.Comma)
      {
        match (com.sun.tools.corba.ee.idl.Token.Comma);
        paramDcl (entry);
      }
      match (com.sun.tools.corba.ee.idl.Token.RightParen);
    }
  } // paraneterDcls2

  /**
   *
   **/
  private void paramDcl (com.sun.tools.corba.ee.idl.MethodEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    com.sun.tools.corba.ee.idl.ParameterEntry parmEntry = stFactory.parameterEntry (entry, (com.sun.tools.corba.ee.idl.IDLID)repIDStack.peek ());
    parmEntry.sourceFile (scanner.fileEntry ());
    // Comment must immeiately precede the parameter attribute
    parmEntry.comment (token.comment);
    paramAttribute (parmEntry);
    parmEntry.type (paramTypeSpec (entry));
    parmEntry.name (token.name);
    match (com.sun.tools.corba.ee.idl.Token.Identifier);
    if (isntInList (entry.parameters (), parmEntry.name ()))
      entry.addParameter (parmEntry);
  } // paramDcl

  /**
   *
   **/
  private void paramAttribute (com.sun.tools.corba.ee.idl.ParameterEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    if (token.type == com.sun.tools.corba.ee.idl.Token.In)
    {
      entry.passType (com.sun.tools.corba.ee.idl.ParameterEntry.In);
      match (com.sun.tools.corba.ee.idl.Token.In);
    }
    else if (token.type == com.sun.tools.corba.ee.idl.Token.Out)
    {
      entry.passType (com.sun.tools.corba.ee.idl.ParameterEntry.Out);
      match (com.sun.tools.corba.ee.idl.Token.Out);
    }
    else if (token.type == com.sun.tools.corba.ee.idl.Token.Inout)
    {
      entry.passType (com.sun.tools.corba.ee.idl.ParameterEntry.Inout);
      match (com.sun.tools.corba.ee.idl.Token.Inout);
    }
    else
      throw com.sun.tools.corba.ee.idl.ParseException.syntaxError (scanner, new int[]{
              com.sun.tools.corba.ee.idl.Token.In, com.sun.tools.corba.ee.idl.Token.Out, com.sun.tools.corba.ee.idl.Token.Inout}, token.type);
  } // paramAttribute

  /**
   *
   **/
  private void raisesExpr (com.sun.tools.corba.ee.idl.MethodEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    match (com.sun.tools.corba.ee.idl.Token.Raises);
    match (com.sun.tools.corba.ee.idl.Token.LeftParen);
    // Comment must immediately precede <scoped_name> for exception
    com.sun.tools.corba.ee.idl.Comment tempComment = token.comment;
    com.sun.tools.corba.ee.idl.SymtabEntry exception = scopedName(entry.container (), stFactory.exceptionEntry ());
    if (typeOf (exception) instanceof com.sun.tools.corba.ee.idl.ExceptionEntry)
    {
      // Comment must immediately precede <scoped_name> for exception
      exception.comment (tempComment);
      if (isntInList (entry.exceptions (), exception))
        entry.exceptionsAddElement ((com.sun.tools.corba.ee.idl.ExceptionEntry) exception);
    }
    else
      com.sun.tools.corba.ee.idl.ParseException.wrongType(scanner, exception.fullName(),
              "exception", entryName(exception.type()));
    raisesExpr2 (entry);
    match (com.sun.tools.corba.ee.idl.Token.RightParen);
  } // raisesExpr

  /**
   *
   **/
  private void raisesExpr2 (com.sun.tools.corba.ee.idl.MethodEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    while (token.type == com.sun.tools.corba.ee.idl.Token.Comma)
    {
      match (com.sun.tools.corba.ee.idl.Token.Comma);
      // Comment must immediately precede <scoped_name> of exception
      com.sun.tools.corba.ee.idl.Comment tempComment = token.comment;
      com.sun.tools.corba.ee.idl.SymtabEntry exception = scopedName (entry.container (), stFactory.exceptionEntry ());
      if (typeOf (exception) instanceof com.sun.tools.corba.ee.idl.ExceptionEntry)
      {
        // Comment must immediately precede <scoped_name> of exception
        exception.comment (tempComment);
        if (isntInList (entry.exceptions (), exception))
          entry.addException ((com.sun.tools.corba.ee.idl.ExceptionEntry)exception);
      }
      else
        com.sun.tools.corba.ee.idl.ParseException.wrongType(scanner, exception.fullName(),
                "exception", entryName(exception.type()));
    }
  } // raisesExpr2

  /**
   *
   **/
  private void contextExpr (com.sun.tools.corba.ee.idl.MethodEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    match (com.sun.tools.corba.ee.idl.Token.Context);
    match (com.sun.tools.corba.ee.idl.Token.LeftParen);
    String stringLit = (String)stringLiteral ().value ();
    if (isntInStringList (entry.contexts (), stringLit))
      entry.addContext (stringLit);
    contextExpr2 (entry);
    match (com.sun.tools.corba.ee.idl.Token.RightParen);
  } // contextExpr


  private void contextExpr2 (com.sun.tools.corba.ee.idl.MethodEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    while (token.type == com.sun.tools.corba.ee.idl.Token.Comma)
    {
      match (com.sun.tools.corba.ee.idl.Token.Comma);
      String stringLit = (String)stringLiteral ().value ();
      if (isntInStringList (entry.contexts (), stringLit))
        entry.addContext (stringLit);
    }
  } // contextExpr2

  /**
   *
   **/
  private com.sun.tools.corba.ee.idl.SymtabEntry paramTypeSpec (com.sun.tools.corba.ee.idl.SymtabEntry entry) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    com.sun.tools.corba.ee.idl.SymtabEntry ret = null;
    switch (token.type)
    {
      case com.sun.tools.corba.ee.idl.Token.Float:
      case com.sun.tools.corba.ee.idl.Token.Double:
      case com.sun.tools.corba.ee.idl.Token.Long:
      case com.sun.tools.corba.ee.idl.Token.Short:
      case com.sun.tools.corba.ee.idl.Token.Unsigned:
      case com.sun.tools.corba.ee.idl.Token.Char:
      case com.sun.tools.corba.ee.idl.Token.Wchar:
      case com.sun.tools.corba.ee.idl.Token.Boolean:
      case com.sun.tools.corba.ee.idl.Token.Octet:
      case com.sun.tools.corba.ee.idl.Token.Any:
        return baseTypeSpec (entry);
      case com.sun.tools.corba.ee.idl.Token.String:
      case com.sun.tools.corba.ee.idl.Token.Wstring:
        return stringType (entry);
      case com.sun.tools.corba.ee.idl.Token.Identifier:
      case com.sun.tools.corba.ee.idl.Token.Object:
      // <f46082.40>
      case com.sun.tools.corba.ee.idl.Token.ValueBase:
      case com.sun.tools.corba.ee.idl.Token.DoubleColon:
        ret = scopedName (entry.container (), stFactory.primitiveEntry ());
        if (typeOf (ret) instanceof com.sun.tools.corba.ee.idl.AttributeEntry)
          //ParseException.attributeParamType (scanner);
          com.sun.tools.corba.ee.idl.ParseException.attributeNotType(scanner, ret.name());
        else // <d60942>
          if (typeOf (ret) instanceof com.sun.tools.corba.ee.idl.MethodEntry)
            com.sun.tools.corba.ee.idl.ParseException.operationNotType(scanner, ret.name());

        //if (!(returnType instanceof PrimitiveEntry ||
        //     returnType instanceof StringEntry))
        //ParseException.wrongType (scanner, ret.fullName(),
        //                          "primitive or string", entryName (ret.type()));
        break;
      default:
        throw com.sun.tools.corba.ee.idl.ParseException.syntaxError (scanner, new int[]{
                com.sun.tools.corba.ee.idl.Token.Float, com.sun.tools.corba.ee.idl.Token.Double, com.sun.tools.corba.ee.idl.Token.Long, com.sun.tools.corba.ee.idl.Token.Short,
                com.sun.tools.corba.ee.idl.Token.Unsigned, com.sun.tools.corba.ee.idl.Token.Char, com.sun.tools.corba.ee.idl.Token.Wchar, com.sun.tools.corba.ee.idl.Token.Boolean,
                com.sun.tools.corba.ee.idl.Token.Octet, com.sun.tools.corba.ee.idl.Token.Any, com.sun.tools.corba.ee.idl.Token.String, com.sun.tools.corba.ee.idl.Token.Wstring,
                com.sun.tools.corba.ee.idl.Token.Identifier, com.sun.tools.corba.ee.idl.Token.DoubleColon, com.sun.tools.corba.ee.idl.Token.ValueBase}, token.type);
    }
    return ret;
  } // paramTypeSpec

  /**
   *
   **/
  private void match (int type) throws IOException, com.sun.tools.corba.ee.idl.ParseException
  {
    com.sun.tools.corba.ee.idl.ParseException exception = null;
    if (!token.equals (type))
    {
      exception = com.sun.tools.corba.ee.idl.ParseException.syntaxError(scanner, type, token.type);
      // Missing a semicolon is a common error.  If a semicolon was expected,
      // assume it exists and keep the current token (don't get the next one).
      // BEWARE!!! THIS HAS THE POTENTIAL FOR AN INFINITE LOOP!
      if (type == com.sun.tools.corba.ee.idl.Token.Semicolon)
        return;
    }
    // <f46082.40> Unecessary due to new valueElement() algorithm.
    //if (!tokenStack.empty())
    //{
    //  token = (Token)tokenStack.pop ();
    //  return;
    //}

    // Fetch the next token.
    token = scanner.getToken ();

    // <d62023> Issue warnings about tokens.
    issueTokenWarnings ();

    // Maintain history of most recent tokens.
    tokenHistory.insert (token);

    // <d59166> Identifiers that collide with keywords are illegal.  Note
    // that escaped identifers never collide!
    /*
    if (token.collidesWithKeyword ())
    {
      // <f60858.1> Issue a warning only
      if (corbaLevel <= 2.2f)
        ParseException.keywordCollisionWarning (scanner, token.name);
      else
        exception = ParseException.keywordCollision (scanner, token.name);
    }
    */

    while (token.isDirective ())
      token = prep.process (token);

    // If the token is a defined thingy, scan the defined string
    // instead of the input stream for a while.
    if (token.equals (com.sun.tools.corba.ee.idl.Token.Identifier) || token.equals (com.sun.tools.corba.ee.idl.Token.MacroIdentifier))
    {
      String string = symbols.get (token.name);
      if (string != null && !string.equals (""))
      {
        // If this is a macro, parse the macro
        if (macros.contains (token.name))
        {
          scanner.scanString (prep.expandMacro (string, token));
          match (token.type);
        }
        else // This is just a normal define.
        {
          scanner.scanString (string);
          match (token.type);
        }
      }
    }
    if (exception != null)
      throw exception;
  } // match

  // <d62023>
  /**
   * Issue warnings according to attributes of current Token.
   **/
  private void issueTokenWarnings ()
  {
    if (noWarn)
      return;
      
    if ((token.equals (com.sun.tools.corba.ee.idl.Token.Identifier) || token.equals (com.sun.tools.corba.ee.idl.Token.MacroIdentifier))
        && !token.isEscaped ())
    {
      // Identifier collision with keyword in another release.
      // Identifier collision with keyword in letter, but not in case.
      if (token.collidesWithKeyword ())
        com.sun.tools.corba.ee.idl.ParseException.warning (scanner, com.sun.tools.corba.ee.idl.Util.getMessage("Migration.keywordCollision", token.name));
    }
    // Deprecated keyword.
    if (token.isKeyword () && token.isDeprecated ())
      com.sun.tools.corba.ee.idl.ParseException.warning (scanner, com.sun.tools.corba.ee.idl.Util.getMessage("Deprecated.keyword", token.toString()));
  } // issueTokenWarnings

  /**
   *
   **/
  private com.sun.tools.corba.ee.idl.ModuleEntry newModule (com.sun.tools.corba.ee.idl.ModuleEntry oldEntry)
  {
    com.sun.tools.corba.ee.idl.ModuleEntry entry = stFactory.moduleEntry (oldEntry, (com.sun.tools.corba.ee.idl.IDLID)repIDStack.peek ());
    entry.sourceFile (scanner.fileEntry ());
    entry.name (token.name);
    // If this named module already exists, just reopen it.
    /* <46082.46.01> if cppModule, always create new module entry */
    com.sun.tools.corba.ee.idl.SymtabEntry prevEntry = (com.sun.tools.corba.ee.idl.SymtabEntry) symbolTable.get (entry.fullName ());
    if (!cppModule && prevEntry != null && prevEntry instanceof com.sun.tools.corba.ee.idl.ModuleEntry)
    {
      // A module has been reopened, return that ModuleEntry.
      entry = (com.sun.tools.corba.ee.idl.ModuleEntry) prevEntry;
      if (oldEntry == topLevelModule)
      {
        // Do a little checking:
        if (!entry.emit ())
          // The entry module is being reopened to put new stuff into it.
          // The module itself is not marked as "emit", but the new stuff
          // may be, so put the module on the emitList (add it to topLevelModule).
          addToContainer (oldEntry, entry);
        else if (!oldEntry.contained().contains (entry))
          // <d50767> The entry module being reopened is to be emitted, but
          // will not be placed on the emitList! I.E., it was not added to
          // topLevelModule.  Occurs when a generator manually inserts
          // ModuleEntrys into the symbol table (e.g., org; see preParse()
          // in ...idl.toJava.Compile). <daz>
          addToContainer (oldEntry, entry);
      }
    }
    else
      pigeonhole (oldEntry, entry);
    return entry;
  } // newModule

  /**
   *
   **/
  private com.sun.tools.corba.ee.idl.EnumEntry newEnumEntry (com.sun.tools.corba.ee.idl.SymtabEntry oldEntry)
  {
    com.sun.tools.corba.ee.idl.EnumEntry entry = stFactory.enumEntry (oldEntry, (com.sun.tools.corba.ee.idl.IDLID)repIDStack.peek ());
    entry.sourceFile (scanner.fileEntry ());
    entry.name (token.name);
    pigeonhole (oldEntry, entry);
    return entry;
  } // newEnumEntry

  /**
   *
   **/
  private com.sun.tools.corba.ee.idl.SequenceEntry newSequenceEntry (com.sun.tools.corba.ee.idl.SymtabEntry oldEntry)
  {
    com.sun.tools.corba.ee.idl.SequenceEntry entry = stFactory.sequenceEntry (oldEntry, (com.sun.tools.corba.ee.idl.IDLID)repIDStack.peek ());
     entry.sourceFile (scanner.fileEntry ());
     entry.name ("");
     pigeonhole (oldEntry, entry);
     return entry;
  } // newSequenceEntry

    private void updateSymbolTable( String fullName, com.sun.tools.corba.ee.idl.SymtabEntry entry, boolean lcCheck )
    {
        // Check for case-insensitive collision (IDL error).
        String lcFullName = fullName.toLowerCase();
        if (lcCheck)
            if (lcSymbolTable.get (lcFullName) != null) {
                com.sun.tools.corba.ee.idl.ParseException.alreadyDeclared(scanner, fullName);
            }
        symbolTable.put (fullName, entry);
        lcSymbolTable.put (lcFullName, entry);
        // <d59809> Allow fully-qualified CORBA types to be resolved by mapping
        // short name (e.g., CORBA/StringValue) to long name, actual name.
        String omgPrefix = "org/omg/CORBA" ;
        if (fullName.startsWith (omgPrefix)) {
            overrideNames.put (
                "CORBA" + fullName.substring (omgPrefix.length()), fullName);
        }
    }

    private void pigeonhole (com.sun.tools.corba.ee.idl.SymtabEntry container, com.sun.tools.corba.ee.idl.SymtabEntry entry)
    {
        if (entry.name().equals (""))
            entry.name (unknownNamePrefix + ++sequence);

        // If this object is not in the overrides list, then it is
        // ok to put it in the table (if it IS in the overrides list,
        // it is already in the table under a different name).
        String fullName = entry.fullName();
        if (overrideNames.get (fullName) == null) {
            addToContainer (container, entry);

            // It is an error is this name already exists in the symbol
            // table, unless this is a redefinition of a forward decl.
            // Re-opening a module is also legal, but not handled here.
            com.sun.tools.corba.ee.idl.SymtabEntry oldEntry = (com.sun.tools.corba.ee.idl.SymtabEntry) symbolTable.get (fullName);

            if (oldEntry == null) {
                updateSymbolTable( fullName, entry, true ) ;
            } else if (oldEntry instanceof com.sun.tools.corba.ee.idl.ForwardEntry &&
                entry instanceof com.sun.tools.corba.ee.idl.InterfaceEntry) {

                String repIDPrefix = ((com.sun.tools.corba.ee.idl.IDLID)entry.repositoryID ()).prefix ();
                String oldRepIDPrefix = ((com.sun.tools.corba.ee.idl.IDLID)oldEntry.repositoryID ()).prefix ();
                if (repIDPrefix.equals (oldRepIDPrefix)) {
                    updateSymbolTable( fullName, entry, false ) ;
                } else {
                    com.sun.tools.corba.ee.idl.ParseException.badRepIDPrefix(scanner, fullName,
                            oldRepIDPrefix, repIDPrefix);
                }
            } else if (entry instanceof com.sun.tools.corba.ee.idl.ForwardEntry &&
                       (oldEntry instanceof com.sun.tools.corba.ee.idl.InterfaceEntry ||
                        oldEntry instanceof com.sun.tools.corba.ee.idl.ForwardEntry)) {
                if (oldEntry instanceof com.sun.tools.corba.ee.idl.ForwardEntry &&
                    entry.repositoryID () instanceof com.sun.tools.corba.ee.idl.IDLID &&
                    oldEntry.repositoryID () instanceof com.sun.tools.corba.ee.idl.IDLID) {

                    String repIDPrefix = 
                        ((com.sun.tools.corba.ee.idl.IDLID)entry.repositoryID ()).prefix ();
                    String oldRepIDPrefix = 
                        ((com.sun.tools.corba.ee.idl.IDLID)oldEntry.repositoryID ()).prefix ();

                    if (!(repIDPrefix.equals (oldRepIDPrefix))) {
                        // Disallow multiple ForwardEntry's having same Repository
                        // ID prefixes (CORBA 2.3).
                        com.sun.tools.corba.ee.idl.ParseException.badRepIDPrefix(scanner, fullName,
                                oldRepIDPrefix, repIDPrefix);
                    }
                }
            } else if (cppModule && entry instanceof com.sun.tools.corba.ee.idl.ModuleEntry &&
                oldEntry instanceof com.sun.tools.corba.ee.idl.ModuleEntry) {
                // Allow multiple ModuleEntrys when user submits
                // the -cppModule flag.
            } else if (fullName.startsWith ("org/omg/CORBA") || 
                fullName.startsWith ("CORBA")) {
                // Ignore CORBA PIDL types entered at preParse() by generator.
            } else if (isForwardable( oldEntry, entry )) {
                // Both oldEntry and entry are structs or unions.
                // Legality depends on isReferencable on the two entries:
                // oldEntry     Entry
                //      T           T       ERROR alreadyDeclared
                //      T           F       legal fwd decl
                //      F           T       if defined in same file legal,
                //                          otherwise ERROR
                //      F           F       legal fwd decl
                if (oldEntry.isReferencable() && entry.isReferencable())
                    com.sun.tools.corba.ee.idl.ParseException.alreadyDeclared(scanner, fullName);

                if (entry.isReferencable()) {
                    String firstFile = 
                        oldEntry.sourceFile().absFilename() ;
                    String defFile = 
                        entry.sourceFile().absFilename() ;
                    if (!firstFile.equals( defFile ))
                        com.sun.tools.corba.ee.idl.ParseException.declNotInSameFile(scanner,
                                fullName, firstFile) ;
                    else {
                        updateSymbolTable( fullName, entry, false ) ;

                        List oldRefList ;

                        try {
                            oldRefList = (List)oldEntry.dynamicVariable( 
                                ftlKey ) ;
                        } catch (NoSuchFieldException exc) {
                            throw new IllegalStateException() ;
                        }

                        if (oldRefList != null) {
                            // Update entries in backpatch list
                            Iterator iter = oldRefList.iterator() ;
                            while (iter.hasNext()) {
                                com.sun.tools.corba.ee.idl.SymtabEntry elem = (com.sun.tools.corba.ee.idl.SymtabEntry)iter.next() ;
                                elem.type( entry ) ;
                            }
                        }
                    }
                }
            } else {
                com.sun.tools.corba.ee.idl.ParseException.alreadyDeclared(scanner, fullName);
            }
        }
    } // pigeonhole

    private boolean isForwardable( com.sun.tools.corba.ee.idl.SymtabEntry oldEntry,
        com.sun.tools.corba.ee.idl.SymtabEntry entry )
    {
        return ((oldEntry instanceof com.sun.tools.corba.ee.idl.StructEntry) &&
            (entry instanceof com.sun.tools.corba.ee.idl.StructEntry)) ||
           ((oldEntry instanceof com.sun.tools.corba.ee.idl.UnionEntry) &&
            (entry instanceof com.sun.tools.corba.ee.idl.UnionEntry)) ;
    }

  // pigeonhole checks to see if this entry is already in the symbol
  // table and generates an error if it is.  Methods must be checked
  // not only against the symbol table but also against their
  // interface's parent's methods.  This is done in InterfaceEntry.
  // verifyMethod, so no checking need be done here.

  /**
   *
   **/
  private void pigeonholeMethod (com.sun.tools.corba.ee.idl.InterfaceEntry container, com.sun.tools.corba.ee.idl.MethodEntry entry)
  {
    if (entry.name ().equals (""))
       entry.name (unknownNamePrefix + ++sequence);

    // If this object is not in the overrides list, then it is
    // ok to put it in the table (if it IS in the overrides list,
    // it is already in the table under a different name).
    String fullName = entry.fullName ();
    if (overrideNames.get (fullName) == null)
    {
      addToContainer (container, entry);
      String lcFullName = fullName.toLowerCase ();
      symbolTable.put (fullName, entry);
      lcSymbolTable.put (lcFullName, entry);
      // <d59809> Allow fully-qualified CORBA types to be resolved by mapping
      // short name (e.g., CORBA/StringValue) to long name, actual name.
      if (fullName.startsWith ("org/omg/CORBA"))
        overrideNames.put ("CORBA" + fullName.substring (13), fullName);
    }
  } // pigeonholeMethod

  /**
   *
   **/
  private void addToContainer (com.sun.tools.corba.ee.idl.SymtabEntry container, com.sun.tools.corba.ee.idl.SymtabEntry contained)
  {
    if (container instanceof com.sun.tools.corba.ee.idl.ModuleEntry)
      ((com.sun.tools.corba.ee.idl.ModuleEntry)container).addContained (contained);
    else if (container instanceof com.sun.tools.corba.ee.idl.InterfaceEntry)
      ((com.sun.tools.corba.ee.idl.InterfaceEntry)container).addContained (contained);
    else if (container instanceof com.sun.tools.corba.ee.idl.StructEntry)
      ((com.sun.tools.corba.ee.idl.StructEntry)container).addContained (contained);
    else if (container instanceof com.sun.tools.corba.ee.idl.UnionEntry)
      ((com.sun.tools.corba.ee.idl.UnionEntry)container).addContained (contained);
    else if (container instanceof com.sun.tools.corba.ee.idl.SequenceEntry)
      ((com.sun.tools.corba.ee.idl.SequenceEntry)container).addContained (contained);
  } // addToContainer

  // NOTE: qualifiedEntry/partlyQualifiedEntry/unqualifiedEntry and
  // their court could probably use some performance improvements,
  // but I'm scared to touch anything.  It's the most complex bit of
  // code in this parser.

  // The qualified named type is searched for in the following order:
  // 1.  OverrideNames
  // 2.  Global scope
  // 3.  Inheritance scope (if container is an interface)
  // A qualified name is one which begins with :: or is assumed to be
  // in the global scope (like long, short, etc).

  /**
   *
   **/
  com.sun.tools.corba.ee.idl.SymtabEntry qualifiedEntry (String typeName)
  {
    com.sun.tools.corba.ee.idl.SymtabEntry type = recursiveQualifiedEntry (typeName);
    if (type == null)
      // Then it's not anywhere, report the error.
      com.sun.tools.corba.ee.idl.ParseException.undeclaredType(scanner, typeName);

    // <d57110> Relax this retriction when parsing ID pragma directive, e.g..
    //else if (type instanceof ModuleEntry) {
    else if (type instanceof com.sun.tools.corba.ee.idl.ModuleEntry && !_isModuleLegalType)
    {
      // Module's are not valid types.
      com.sun.tools.corba.ee.idl.ParseException.moduleNotType(scanner, typeName);
      type = null;
    }
    return type;
  } // qualifiedEntry

  /**
   *
   **/
  com.sun.tools.corba.ee.idl.SymtabEntry recursiveQualifiedEntry (String typeName)
  {
    com.sun.tools.corba.ee.idl.SymtabEntry type = null;
    if (typeName != null && !typeName.equals ("void"))
    {
      int index = typeName.lastIndexOf ('/');
      if (index >= 0)
      {
        // Figure out if the container of this thing exists, converting any typedefs to interfaces if necessary.
        type = recursiveQualifiedEntry (typeName.substring (0, index));
        if (type == null)
          return null;
        else if (type instanceof com.sun.tools.corba.ee.idl.TypedefEntry)
          typeName = typeOf (type).fullName () + typeName.substring (index);
      }

      // If we got this far, a container exists, start over looking
      // for the thing itself (this is the meat of the method):
      type = searchOverrideNames (typeName);
      if (type == null)
        type = (com.sun.tools.corba.ee.idl.SymtabEntry) symbolTable.get (typeName); // search global scope:
      if (type == null)
        type = searchGlobalInheritanceScope (typeName);
    }
    return type;
  } // recursiveQualifiedEntry

  // A partially qualified name is of the form <scope>::<name>.
  // First the scope is defined (meaning it is fully qualified);
  // Then the name is searched for in the scope.

  /**
   *
   **/
  com.sun.tools.corba.ee.idl.SymtabEntry partlyQualifiedEntry (String typeName, com.sun.tools.corba.ee.idl.SymtabEntry container)
  {
    // This is the simple logic of this method:
    // type = searchModuleScope (typeName.substring (0, typeName.lastIndexOf ('/')), container);
    // type = qualifiedEntry (type.fullName () + typeName.substring (typeName.lastIndexOf ('/')));
    // But searchModuleScope only finds the first module that fits.
    // The name might not be in that module but in one further out
    // in the module scope.  Should others be searched?
    com.sun.tools.corba.ee.idl.SymtabEntry type = null;
    if (typeName != null)
    {
      int index = typeName.lastIndexOf ('/');

      // Figure out if the container of this thing exists, converting any
      // typedefs to interfaces if necessary:
      type = recursivePQEntry (typeName.substring (0, index), container);
      if (type instanceof com.sun.tools.corba.ee.idl.TypedefEntry)
        typeName = typeOf (type).fullName () + typeName.substring (index);

      // If we got this far, a container exists, start over looking
      // for the thing itself.

      if (container != null)
        type = searchModuleScope (typeName.substring (0, typeName.lastIndexOf ('/')), container);
      if (type == null)
        type = qualifiedEntry (typeName);
      else
        type = qualifiedEntry (type.fullName () + typeName.substring (typeName.lastIndexOf ('/')));
    }
    return type;
  } // partlyQualifiedEntry

  // partlyQualifiedEntry and recursivePQEntry are almost identical.
  // They are different because when the recursive one is looking for
  // the existence of containers, the error check for a module type
  // must not occur (this check is done in qualifiedEntry).  Only
  // when the full partly qualified name is being processed must this
  // check be performed.

  /**
   *
   **/
  com.sun.tools.corba.ee.idl.SymtabEntry recursivePQEntry (String typeName, com.sun.tools.corba.ee.idl.SymtabEntry container)
  {
    com.sun.tools.corba.ee.idl.SymtabEntry type = null;
    if (typeName != null)
    {
      int index = typeName.lastIndexOf ('/');
      if (index < 0)
        type = searchModuleScope (typeName, container);
      else
      {
        // Figure out if the container of this thing exists, converting any
        // typedefs to interfaces if necessary:
        type = recursivePQEntry (typeName.substring (0, index), container);
        if (type == null)
          return null;
        else if (type instanceof com.sun.tools.corba.ee.idl.TypedefEntry)
          typeName = typeOf (type).fullName () + typeName.substring (index);

        // If we got this far, a container exists, start over, looking
        // for the thing itself (This is the meat of the method):
        if (container != null)
          type = searchModuleScope (typeName.substring (0, typeName.lastIndexOf ('/')), container);
          if (type == null)
            recursiveQualifiedEntry (typeName);
          else
            type = recursiveQualifiedEntry (type.fullName () + typeName.substring (typeName.lastIndexOf ('/')));
      }
    }
    return type;
  } // recursivePQEntry

  // The named type is searched for in the following order:
  // 1.  Local scope
  // 2.  Inheritance scope
  // 3.  OverrideNames
  // 4.  Module scope

  /**
   *
   **/
  com.sun.tools.corba.ee.idl.SymtabEntry unqualifiedEntry (String typeName, com.sun.tools.corba.ee.idl.SymtabEntry container)
  {
    com.sun.tools.corba.ee.idl.SymtabEntry type = unqualifiedEntryWMod (typeName, container);

    // <d57110> Relax this retriction in special cases, e.g., when
    // parsing a ID pragma directive.
    //if (type instanceof ModuleEntry) {
    if (type instanceof com.sun.tools.corba.ee.idl.ModuleEntry && !_isModuleLegalType)
    {
      // Module's are not valid types:
      com.sun.tools.corba.ee.idl.ParseException.moduleNotType(scanner, typeName);
      type = null;
    }
    return type;
  } // unqualifiedEntry

  /**
   *
   **/
  com.sun.tools.corba.ee.idl.SymtabEntry unqualifiedEntryWMod (String typeName, com.sun.tools.corba.ee.idl.SymtabEntry container)
  {
    com.sun.tools.corba.ee.idl.SymtabEntry type = null;
    if ((typeName != null) && !typeName.equals ("void"))
    {
      // Search local scope:
      type = (com.sun.tools.corba.ee.idl.SymtabEntry)symbolTable.get (container.fullName () + '/' + typeName);
      if (type == null)
        type = searchLocalInheritanceScope (typeName, container);
      if (type == null)
        type = searchOverrideNames (typeName);
      if ((type == null) && (container != null))
        type = searchModuleScope (typeName, container);
      if (type == null)
        type = searchParentInheritanceScope (typeName, container);
    }
    if (type == null)
      // Then it's not anywhere, report the error:
      com.sun.tools.corba.ee.idl.ParseException.undeclaredType(scanner, typeName);
    return type;
  } // unqualifiedEntryWMod

  /**
   * Walks up the enclosing scopes until it finds an interface type. Then,
   * searches up that interface inheritance tree for the type definition.
   *
   * @param name type name to be searched for.
   * @param ptype parent type entry.
   **/
  com.sun.tools.corba.ee.idl.SymtabEntry searchParentInheritanceScope(String name, com.sun.tools.corba.ee.idl.SymtabEntry ptype) {

    String cname = ptype.fullName();

    while ((ptype != null) && !(cname.equals ("")) &&
           !(ptype instanceof com.sun.tools.corba.ee.idl.InterfaceEntry)) {
        int index = cname.lastIndexOf ('/');
        if (index < 0) {
            cname = "";
        } else {
            cname = cname.substring (0, index);
            ptype = (com.sun.tools.corba.ee.idl.SymtabEntry) symbolTable.get(cname);
        }
    }

    if ((ptype == null) || !(ptype instanceof com.sun.tools.corba.ee.idl.InterfaceEntry)) {
        return null; // could not find an enclosing interface type - give up.
    }

    // check if the enclosing interface supports the type definition.
    String fullName = ptype.fullName () + '/' + name;
    com.sun.tools.corba.ee.idl.SymtabEntry type = (com.sun.tools.corba.ee.idl.SymtabEntry) symbolTable.get (fullName);
    if (type != null) {
        return type; // found type definition.
    }
    
    // search up the interface inheritance tree.
    return searchLocalInheritanceScope(name, ptype);
  }

  /**
   *
   **/
  com.sun.tools.corba.ee.idl.SymtabEntry searchGlobalInheritanceScope (String name)
  {
    // See if the container of this named object is an interface:
    int         index = name.lastIndexOf ('/');
    com.sun.tools.corba.ee.idl.SymtabEntry entry = null;
    if (index >= 0)
    {
      String containerName = name.substring (0, index);
      entry = (com.sun.tools.corba.ee.idl.SymtabEntry)symbolTable.get (containerName);
      entry = (entry instanceof com.sun.tools.corba.ee.idl.InterfaceEntry)
          // It's an interface, now look in its inheritance scope:
          ? searchLocalInheritanceScope (name.substring (index + 1), entry)
          : null;
    }
    return entry;
  } // searchGlobalInheritanceScope

  /**
   *
   **/
  com.sun.tools.corba.ee.idl.SymtabEntry searchLocalInheritanceScope (String name, com.sun.tools.corba.ee.idl.SymtabEntry container)
  {
    return (container instanceof com.sun.tools.corba.ee.idl.InterfaceEntry)
        ? searchDerivedFrom (name, (com.sun.tools.corba.ee.idl.InterfaceEntry) container)
        : null;
  } // searchLocalInheritanceScope

  /**
   *
   **/
  com.sun.tools.corba.ee.idl.SymtabEntry searchOverrideNames (String name)
  {
    String overrideName = overrideNames.get(name);
    return (overrideName != null)
        ? (com.sun.tools.corba.ee.idl.SymtabEntry)symbolTable.get (overrideName)
        : null;
  } // searchOverrideNames

  /**
   *
   **/
  com.sun.tools.corba.ee.idl.SymtabEntry searchModuleScope (String name, com.sun.tools.corba.ee.idl.SymtabEntry container)
  {
    String      module   = container.fullName ();
    String      fullName = module + '/' + name;
    com.sun.tools.corba.ee.idl.SymtabEntry type     = (com.sun.tools.corba.ee.idl.SymtabEntry)symbolTable.get (fullName);
    while ((type == null) && !module.equals (""))
    {
      int index = module.lastIndexOf ('/');
      if (index < 0)
        module = "";
      else
      {
        module   = module.substring (0, index);
        fullName = module + '/' + name;
        type     = (com.sun.tools.corba.ee.idl.SymtabEntry)symbolTable.get (fullName);
      }
    }
    return (type == null) ? (com.sun.tools.corba.ee.idl.SymtabEntry)symbolTable.get (name) : type;
  } // searchModuleScope

  /**
   *
   **/
  com.sun.tools.corba.ee.idl.SymtabEntry searchDerivedFrom (String name, com.sun.tools.corba.ee.idl.InterfaceEntry i)
  {
    for (Enumeration e = i.derivedFrom ().elements (); e.hasMoreElements ();)
    {
      com.sun.tools.corba.ee.idl.SymtabEntry tmp = (com.sun.tools.corba.ee.idl.SymtabEntry)e.nextElement ();
      if (tmp instanceof com.sun.tools.corba.ee.idl.InterfaceEntry)
      {
        com.sun.tools.corba.ee.idl.InterfaceEntry parent = (com.sun.tools.corba.ee.idl.InterfaceEntry)tmp;
        String fullName = parent.fullName () + '/' + name;
        com.sun.tools.corba.ee.idl.SymtabEntry type = (com.sun.tools.corba.ee.idl.SymtabEntry)symbolTable.get (fullName);
        if (type != null)
          return type;
        type = searchDerivedFrom (name, parent);
        if (type != null)
          return type;
      }
      // else it is a ForwardEntry and nothing can be done at this point.
    }
    return null;
  } // searchDerivedFrom

  /**
   *
   **/
  String entryName (com.sun.tools.corba.ee.idl.SymtabEntry entry)
  {
    if (entry instanceof com.sun.tools.corba.ee.idl.AttributeEntry)
      return "attribute";
    if (entry instanceof com.sun.tools.corba.ee.idl.ConstEntry)
      return "constant";
    if (entry instanceof com.sun.tools.corba.ee.idl.EnumEntry)
      return "enumeration";
    if (entry instanceof com.sun.tools.corba.ee.idl.ExceptionEntry)
      return "exception";
    if (entry instanceof com.sun.tools.corba.ee.idl.ValueBoxEntry)
      return "value box";
    if (entry instanceof com.sun.tools.corba.ee.idl.ForwardValueEntry || entry instanceof com.sun.tools.corba.ee.idl.ValueEntry)
      return "value";
    if (entry instanceof com.sun.tools.corba.ee.idl.ForwardEntry || entry instanceof com.sun.tools.corba.ee.idl.InterfaceEntry)
      return "interface";
    if (entry instanceof com.sun.tools.corba.ee.idl.MethodEntry)
      return "method";
    if (entry instanceof com.sun.tools.corba.ee.idl.ModuleEntry)
      return "module";
    if (entry instanceof com.sun.tools.corba.ee.idl.ParameterEntry)
      return "parameter";
    if (entry instanceof com.sun.tools.corba.ee.idl.PrimitiveEntry)
      return "primitive";
    if (entry instanceof com.sun.tools.corba.ee.idl.SequenceEntry)
      return "sequence";
    if (entry instanceof com.sun.tools.corba.ee.idl.StringEntry)
      return "string";
    if (entry instanceof com.sun.tools.corba.ee.idl.StructEntry)
      return "struct";
    if (entry instanceof com.sun.tools.corba.ee.idl.TypedefEntry)
      return "typedef";
    if (entry instanceof com.sun.tools.corba.ee.idl.UnionEntry)
      return "union";
    return "void";
  } // entryName

  /**
   *
   **/
  private boolean isInterface (com.sun.tools.corba.ee.idl.SymtabEntry entry)
  {
    return entry instanceof com.sun.tools.corba.ee.idl.InterfaceEntry || (entry instanceof com.sun.tools.corba.ee.idl.ForwardEntry
        && !(entry instanceof com.sun.tools.corba.ee.idl.ForwardValueEntry)) ;
  }

  private boolean isValue (com.sun.tools.corba.ee.idl.SymtabEntry entry)
  {
    return entry instanceof com.sun.tools.corba.ee.idl.ValueEntry; // || entry instanceof ForwardValueEntry;
  }

  private boolean isInterfaceOnly (com.sun.tools.corba.ee.idl.SymtabEntry entry)
  {
    return entry instanceof com.sun.tools.corba.ee.idl.InterfaceEntry;
  }

  private boolean isForward(com.sun.tools.corba.ee.idl.SymtabEntry entry)
  {
      return entry instanceof com.sun.tools.corba.ee.idl.ForwardEntry;
  }

  // list must be a vector of Strings.
  /**
   *
   **/
  private boolean isntInStringList (Vector<String> list, String name)
  {
    boolean isnt = true;
    Enumeration<String> e = list.elements ();
    while (e.hasMoreElements ())
      if (name.equals (e.nextElement()))
      {
        com.sun.tools.corba.ee.idl.ParseException.alreadyDeclared(scanner, name);
        isnt = false;
        break;
      }
    return isnt;
  } // isntInStringList

  // list must be a vector of SymtabEntry's.
  /**
   *
   **/
  private boolean isntInList (Vector list, String name)
  {
    boolean isnt = true;
    for (Enumeration e = list.elements (); e.hasMoreElements ();)
      if (name.equals (((com.sun.tools.corba.ee.idl.SymtabEntry)e.nextElement ()).name ()))
      {
        com.sun.tools.corba.ee.idl.ParseException.alreadyDeclared(scanner, name);
        isnt = false;
        break;
      }
    return isnt;
  } // isntInList

  // list must be a vector of SymtabEntry's.
  /**
   *
   **/
  private boolean isntInList (Vector list, com.sun.tools.corba.ee.idl.SymtabEntry entry)
  {
    boolean isnt = true;
    for (Enumeration e = list.elements (); e.hasMoreElements ();)
    {
      com.sun.tools.corba.ee.idl.SymtabEntry eEntry = (com.sun.tools.corba.ee.idl.SymtabEntry)e.nextElement ();
      if (entry == eEntry)  // && entry.fullName().equals (eEntry.fullName()))
      {
        com.sun.tools.corba.ee.idl.ParseException.alreadyDeclared(scanner, entry.fullName());
        isnt = false;
        break;
      }
     }
     return isnt;
  } // isntInList

  /**
   *
   **/
  public static com.sun.tools.corba.ee.idl.SymtabEntry typeOf (com.sun.tools.corba.ee.idl.SymtabEntry entry)
  {
    while (entry instanceof com.sun.tools.corba.ee.idl.TypedefEntry)
      entry = entry.type ();
    return entry;
  } // typeOf

  /**
   *
   **/
  void forwardEntryCheck ()
  {
    for (Enumeration<SymtabEntry> e = symbolTable.elements (); e.hasMoreElements ();)
    {
      com.sun.tools.corba.ee.idl.SymtabEntry entry = e.nextElement ();
      if (entry instanceof com.sun.tools.corba.ee.idl.ForwardEntry)
        com.sun.tools.corba.ee.idl.ParseException.forwardEntry(scanner, entry.fullName());
    }
  } // forwardEntryCheck

  // <46082.03> Revert to "IDL:"-style (i.e., regular) repository ID.
  /*
  void updateRepositoryIds () {
     for (Enumeration e = symbolTable.elements(); e.hasMoreElements();) {
         SymtabEntry entry = (SymtabEntry) e.nextElement();
         if (entry instanceof ValueEntry)
           ((ValueEntry) entry).calcRepId();
     }
  } // updateRepositoryIds
  */

  ////////////////////
  // Error Handling Methods

  // A syntax error occurred.  Skip until a semicolon is encountered.
  // Ignore semicolons within {...} blocks
  /**
   *
   **/
  private void skipToSemicolon () throws IOException
  {
    while (!token.equals (com.sun.tools.corba.ee.idl.Token.EOF) && !token.equals (com.sun.tools.corba.ee.idl.Token.Semicolon))
    {
      if (token.equals (com.sun.tools.corba.ee.idl.Token.LeftBrace))
        skipToRightBrace();
      try
      {
        match (token.type);
      }
      catch (com.sun.tools.corba.ee.idl.ParseException exception)
      {
        // The error has already been reported...
      }
    }
    if (token.equals (com.sun.tools.corba.ee.idl.Token.EOF))
      throw new EOFException ();
    try
    {
      match (com.sun.tools.corba.ee.idl.Token.Semicolon);
    }
    catch (Exception exception)
    {
    }
  } // skipToSemicolon

  /**
   *
   **/
  private void skipToRightBrace () throws IOException
  {
    boolean firstTime = true;
    while (!token.equals (com.sun.tools.corba.ee.idl.Token.EOF) && !token.equals (com.sun.tools.corba.ee.idl.Token.RightBrace))
    {
      if (firstTime)
        firstTime = false;
      else if (token.equals (com.sun.tools.corba.ee.idl.Token.LeftBrace))
        skipToRightBrace ();
      try
      {
        match (token.type);
      }
      catch (com.sun.tools.corba.ee.idl.ParseException exception)
      {
        // The error has already been reported...
      }
    }
    if (token.equals (com.sun.tools.corba.ee.idl.Token.EOF))
      throw new EOFException();
  } // skipToRightBrace

  // Error Handling Methods
  ////////////////////

  // <d56351> In CORBA 2.3, an IDL file provides a new scope for Repository IDs.
  // The following methods provide a means for other classes in the framework
  // to manage this scoping (see Preprocessor and Scanner).

  // public static int nPush = 0;
  // public static int nPop = 0;

  /**
   *
   **/
  public static void enteringInclude ()
  {
    repIDStack.push (new com.sun.tools.corba.ee.idl.IDLID());
  } // enteringInclude

  /**
   *
   **/
  public static void exitingInclude ()
  {
    repIDStack.pop ();
  } // exitingInclude


  public static final String unknownNamePrefix = "uN__";

       static Hashtable<String, SymtabEntry>   symbolTable;
              Hashtable<String, SymtabEntry>   lcSymbolTable  = new Hashtable ();
       static Hashtable<String, String>   overrideNames;
              Vector<SymtabEntry>      emitList       = new Vector ();
              boolean     emitAll;
  // <f46082.46.01>
              boolean     cppModule;
  // <d62023>
              boolean     noWarn;
              com.sun.tools.corba.ee.idl.Scanner scanner;
  // <f46082.40> No longer necessary due to new valueElement() algorithm.
  //          Stack       tokenStack     = new Stack();
              Hashtable<String, String>   symbols;
              Vector<String>      macros         = new Vector ();
              Vector<String>      paths;

  // Only needed for the pragma directive
              com.sun.tools.corba.ee.idl.SymtabEntry currentModule  = null;

  // <d56351> Static field necessary to allow Scanner access to enterind/exiting
  // Include() methods. Must reset in Compile class, too!
  //          Stack       repIDStack     = new Stack ();
       static Stack       repIDStack     = new Stack ();

  // Dynamic variable key used for forward type lists.
  // A struct or union X entry may have this attached,
  // which always contains a List<SymtabEntry>.
  // The elements are entries E such that E.type() == X.
  // This list must be resolved in pigeonhole when the
  // type is finally defined.  This is similar to
  // ForwardEntry.replaceForwardDecl.
  private static int ftlKey = com.sun.tools.corba.ee.idl.SymtabEntry.getVariableKey() ;

              int         sequence       = 0;
              Vector<String>      includes;
              Vector<IncludeEntry>      includeEntries;

  // Only needed in primaryExpr.  Set in Preprocessor.booleanConstExpr.
              boolean     parsingConditionalExpr = false;

              com.sun.tools.corba.ee.idl.Token token;
              com.sun.tools.corba.ee.idl.ModuleEntry topLevelModule;
  private com.sun.tools.corba.ee.idl.Preprocessor prep;
  private     boolean       verbose;
              com.sun.tools.corba.ee.idl.SymtabFactory stFactory;
              com.sun.tools.corba.ee.idl.constExpr.ExprFactory exprFactory;
  private     String[]      keywords;
  // <f46082.51> Remove -stateful feature.
  //private     boolean       parseStateful = false;

  // Circular buffer containing most recent tokens, including the current token.
  private com.sun.tools.corba.ee.idl.TokenBuffer tokenHistory = new com.sun.tools.corba.ee.idl.TokenBuffer();
  protected   float       corbaLevel; // <f60858.1>
  private com.sun.tools.corba.ee.idl.Arguments arguments;

} // class Parser


