/*
 * Copyright (c) 1997, 2020, Oracle and/or its affiliates.
 * Copyright (c) 1997-1999 IBM Corp. All rights reserved.
 * Copyright (c) 2019 Payara Services Ltd.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.corba.idl;

// NOTES:
// -D56351<daz> Update computation of RepositoryIDs to CORBA 2.3 (see spec.).
// -D58319<daz> Display version info. for -version option.

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;

import org.glassfish.corba.idl.constExpr.DefaultExprFactory;
import org.glassfish.corba.idl.constExpr.ExprFactory;

/**
 * Compiler usage:
 * <br><br>
 *
 * java com.sun.tools.corba.ee.idl.toJava.compile [options] &lt;idl file&gt;
 * <br><br>
 *
 * where &lt;idl file&gt; is the name of a file containing IDL definitions,
 * and [options] is any combination of the options listed below.  The options
 * and the idl file name can appear in any order.
 * <br><br>
 *
 * Options:
 * <dl>
 * <dt>-i &lt;include path&gt;
 * <dd>By default, the current directory is scanned for included files.
 *     This option adds another directory.  See also Note 1 below.
 *
 * <dt>-d &lt;symbol&gt;
 * <dd>This is equivalent to the following line in an IDL file: #define &lt;symbol&gt;
 *
 * <dt>-emitAll
 * <dd>Emit all types, including those found in #included files.
 *
 * <dt>-v
 * <dd>Verbose mode.
 * </dl>
 *
 * Note 1:  If you have an include path or paths that you will always be using,
 * it can get tedious putting these on the command with the -i option all the
 * time.  Instead, these can be placed into a config file called idl.config.
 * This file must be in the CLASSPATH.  The format of the includes line is:
 *
 * <pre>
 * includes=&lt;path1&gt;&lt;path2&gt;;...;&lt;pathN&gt;
 * </pre>
 *
 * Note that the path separator character, here shown as a semicolon, is
 * machine dependent.  For instance, on Windows 95 this character is a
 * semicolon, on UNIX it is a colon.
 *
 * <p>
 * Note 2:  If you are directly invoking the main method on this class (not
 * a subclass), then it will only check that the IDL file is syntactically
 * correct.  It does not generate any files.  Only extensions to this
 * framework generate files, therefore an extension must be invoked if you
 * want files to be generated.
 * <br><br>
 *
 * To Extend the compiler:
 * <br><br>
 *
 * You only need to extend the compiler if you want it to generate something
 * other than what it currently generates.
 * <br><br>
 *
 * Step 1 - Implement the generator interfaces:
 * <br><br>
 *
 * Each generator interface defines one method: generate (Hashtable, XXXEntry, PrintWriter);
 * <br>
 * - The Hashtable is the symbol table; each element is a SymtabEntry (or a
 *   subclass of SymtabEntry) and is keyed by its fully qualified name;
 *   <br>
 * - XXXEntry is the appropriate entry for the type to be generated.  For
 *   example: AttributeGen defines generate (Hashtable, AttributeEntry, PrintWriter);
 *   ConstGen defines generate (Hashtable, ConstEntry, PrintWriter); etc.
 *   <br>
 * - The PrintWriter is a stream to the file being generated.  For the
 *   generators called by the compiler framework, this will be null.  The
 *   generator is responsible for creating and opening files.  But for
 *   generators that are called by other generators - for instance,
 *   MethodGen.generate will most likely be called by InterfaceGen.generate -
 *   this parameter is provided so that the proper file can be written to.
 *   <br><br>
 *
 * Step 2 - Implement the GenFactory interface:
 * <br><br>
 *
 * All of the generators implemented in Step 1 must be created somehow.  There
 * is an interface for a factory, GenFactory, which must be implemented.  The
 * name of this factory must be set in the extension to the Compile class (see
 * Step 3, below).
 * <br><br>
 *
 * Step 3 - Extend Factories:
 * <br><br>
 *
 * Extend Factories and override the method genFactory.  This
 * method must return an instance of the factory which you implemented in
 * step 2.  Your extension of this class may also do more, this is only the
 * minimum.  See Factories for more information.
 * <br><br>
 *
 * Step 4 - Extend Compile:
 * <br><br>
 *
 * Your extension of Compile should contain a minimum of
 * two methods:
 * <dl>
 * <dt>protected Factories ()
 * <dd>This method overrides Compile.factories and returns your
 *     extension from Step 3.
 *
 * <dt>public static void main (String[] args)
 * <dd>This main method must instantiate this class and call its start method.
 * </dl>
 *
 * Given that the extension of Factories is MyFactories, the extension of
 * Compile could be:
 *
 * <pre>
 * public class MyCompile extends Compile
 * {
 *   protected Factories factories ()
 *   {
 *     return new MyFactories ();
 *   }
 *   public static void main (String[] args)
 *   {
 *     MyCompile compile = new MyCompile ();
 *     compile.start (args);
 *   }
 * }
 * </pre>
 *
 * If you would like a bit more control over the processing of the framework,
 * you can replace compile.start with what it calls.  But then you also have
 * to handle the exceptions which start handles for you:
 * <pre>
 * public class MyCompile extends Compile
 * {
 *   ...
 *
 *   public static void main (String[] args)
 *   {
 *     MyCompile compile = new MyCompile ();
 *     try
 *     {
 *       compile.init (args);
 *       java.util.Enumeration emitList = compile.parse ();
 *       compile.generate ();
 *     }
 *     catch (InvalidArgument e)
 *     {
 *       System.err.println (e);
 *     }
 *     catch (java.io.IOException e)
 *     {
 *       System.err.println (e);
 *     }
 *   }
 * }
 * </pre>
 *
 * Note that compile.parse returns an enumeration.  This enumerates the
 * SymtabEntry's which should be generated.  If the parse method detects
 * errors, it returns null.  Note that you do not have to check that
 * `emitList' is valid before calling generate (that's done internally), but
 * if you do any processing between parse and generate, emitList should be
 * checked before executing that code.
 * <br><br>
 **/
@SuppressWarnings("unchecked")
public class Compile
{
  public Compile ()
  {
    NoPragma noPragma = new NoPragma();
    noPragma.init (preprocessor);
    preprocessor.registerPragma (noPragma);

    // <d41197> Reset static variables to allow parsing multiple sources.
    // DO NOT reset SymtabEntry.maxKey because it crashes IDLC.
    ParseException.detected  = false;
    SymtabEntry.includeStack = (Stack<Boolean>) new java.util.Stack ();
    SymtabEntry.setEmit      = true;
    //SymtabEntry.maxKey     = -1;
    Parser.repIDStack        = new java.util.Stack (); // <d56351>
  } // ctor

  public static void main (String[] args)
  {
    (new Compile ()).start (args);
  } // main

  protected Factories factories ()
  {
    return new Factories();
  } // genFactoryName

  /**
   * Initialize the framework.
   * @param args command line arguments
   **/
  protected void init (String[] args) throws InvalidArgument
  {
    initFactories ();
    arguments.parseArgs (args);
    initGenerators ();
    parser = new Parser(preprocessor, arguments, overrideNames, symbolTable, symtabFactory, exprFactory, keywords);
    preprocessor.init (parser);
    parser.includes = includes;
    parser.includeEntries = includeEntries;
  } // init

  /**
   * Parse the IDL file and return an enumeration of the symbols to be
   * generated.  All elements of the Enumeration will be extensions of
   * SymtabEntry.  If any errors were encountered during parsing, null
   * will be returned.
   * @return {@link Enumeration}&lt;{@link SymtabEntry}&gt; 
   *    or null if a non-IO error occured
   * @throws IOException if there was an error reading the file
   **/
  protected Enumeration parse () throws IOException
  {
    if (arguments.verbose)
      System.out.println (Util.getMessage("Compile.parsing", arguments.file));
    parser.parse (arguments.file);
    if ( !ParseException.detected )
    {
      parser.forwardEntryCheck();

      // <46082.03> Revert to "IDL:"-style (i.e., regular) repository ID.
      //parser.updateRepositoryIds();
    }
    if (arguments.verbose)
      System.out.println (Util.getMessage("Compile.parseDone", arguments.file));
    if (ParseException.detected)
    {
      symbolTable = null;
      emitList    = null;
    }
    else
    {
      symbolTable = Parser.symbolTable;
      emitList    = parser.emitList.elements ();
    }
    return emitList;
  } // parse

  /**
   * Invoke the generators.
   **/
    @SuppressWarnings("StatementWithEmptyBody")
    protected void generate() {
        if (ParseException.detected) {
            emitList = null;
        } else {
            emitList = parser.emitList.elements();
        }
        if (emitList != null) {
            // Emit the output files for all of the types in the IDL file
            if (arguments.verbose) {
                System.out.println();
            }
            while (emitList.hasMoreElements()) {
                SymtabEntry entry = emitList.nextElement();
                if (arguments.verbose) {
                    if (entry.generator() instanceof Noop) {
                        ; // Nothing will be generated, so don't say so.
                    } else if (entry.module().equals("")) {
                        System.out.println(Util.getMessage("Compile.generating", entry.name()));
                    } else {
                        System.out.println(Util.getMessage("Compile.generating", entry.module() + '/' + entry.name()));
                    }
                }
                entry.generate(symbolTable, null);
                if (arguments.verbose) {
                    if (entry.generator() instanceof Noop) {
                        ; // Nothing will be generated, so don't say so.
                    } else if (entry.module().equals("")) {
                        System.out.println(Util.getMessage("Compile.genDone", entry.name()));
                    } else {
                        System.out.println(Util.getMessage("Compile.genDone", entry.module() + '/' + entry.name()));
                    }
                }
            }
        }
    }

  /**
   * Start the parse/code generation process.  This method calls init,
   * parse, generate.  If more control is desired, rather than call start,
   * those three methods could be called explicitly.
   * @param args command-line arguments
   **/
  public void start (String[] args)
  {
    try
    {
      init (args);
      if (arguments.versionRequest) // <d59319>
        displayVersion ();
      else
      {
        parse ();
        generate ();
      }
    }
    catch (InvalidArgument | IOException e)
    {
      System.err.println (e);
    }
  } // start

  private void initFactories ()
  {
    // Get the factories.
    Factories factories = factories ();
    if (factories == null) factories = new Factories();

    // Get the argument processor from the factories.
    Arguments tmpArgs = factories.arguments ();
    if (tmpArgs == null)
      arguments = new Arguments();
    else
      arguments = tmpArgs;

    // Get the symbol table entry factory from the factories.
    SymtabFactory tmpSTF = factories.symtabFactory ();
    if (tmpSTF == null)
      symtabFactory = new DefaultSymtabFactory();
    else
      symtabFactory = tmpSTF;

    // Get the expression factory from the factories.
    ExprFactory tmpExpF = factories.exprFactory ();
    if (tmpExpF == null)
      exprFactory = new DefaultExprFactory ();
    else
      exprFactory = tmpExpF;

    // Get the generator factory from the factories.
    GenFactory tmpGenF = factories.genFactory ();
    if (tmpGenF == null)
      genFactory = noop;
    else
      genFactory = tmpGenF;

    // Get the language keywords.
    keywords = factories.languageKeywords ();
    if (keywords == null)
      keywords = new String[0];
  } // initFactories

  private void initGenerators ()
  {
    AttributeGen agen = genFactory.createAttributeGen ();
    AttributeEntry.attributeGen = agen == null ? noop : agen;

    ConstGen cgen = genFactory.createConstGen ();
    ConstEntry.constGen = cgen == null ? noop : cgen;

    EnumGen egen = genFactory.createEnumGen ();
    EnumEntry.enumGen = egen == null ? noop : egen;

    ExceptionGen exgen = genFactory.createExceptionGen ();
    ExceptionEntry.exceptionGen = exgen == null ? noop : exgen;

    ForwardGen fgen = genFactory.createForwardGen ();
    ForwardEntry.forwardGen = fgen == null ? noop : fgen;

    ForwardValueGen fvgen = genFactory.createForwardValueGen ();
    ForwardValueEntry.forwardValueGen = fvgen == null ? noop : fvgen;

    IncludeGen ingen = genFactory.createIncludeGen ();
    IncludeEntry.includeGen = ingen == null ? noop : ingen;

    InterfaceGen igen = genFactory.createInterfaceGen ();
    InterfaceEntry.interfaceGen = igen == null ? noop : igen;

    ValueGen vgen = genFactory.createValueGen ();
    ValueEntry.valueGen = vgen == null ? noop : vgen;

    ValueBoxGen vbgen = genFactory.createValueBoxGen ();
    ValueBoxEntry.valueBoxGen = vbgen == null ? noop : vbgen;

    MethodGen mgen = genFactory.createMethodGen ();
    MethodEntry.methodGen = mgen == null ? noop : mgen;

    ModuleGen modgen = genFactory.createModuleGen ();
    ModuleEntry.moduleGen = modgen == null ? noop : modgen;

    NativeGen ngen = genFactory.createNativeGen ();
    NativeEntry.nativeGen = ngen == null ? noop : ngen;

    ParameterGen pgen = genFactory.createParameterGen ();
    ParameterEntry.parameterGen = pgen == null ? noop : pgen;

    PragmaGen prgen = genFactory.createPragmaGen ();
    PragmaEntry.pragmaGen = prgen == null ? noop : prgen;

    PrimitiveGen primgen = genFactory.createPrimitiveGen ();
    PrimitiveEntry.primitiveGen = primgen == null ? noop : primgen;
    
    SequenceGen seqgen = genFactory.createSequenceGen ();
    SequenceEntry.sequenceGen = seqgen == null ? noop : seqgen;

    StringGen strgen = genFactory.createStringGen ();
    StringEntry.stringGen = strgen == null ? noop : strgen;

    StructGen sgen = genFactory.createStructGen ();
    StructEntry.structGen = sgen == null ? noop : sgen;

    TypedefGen tgen = genFactory.createTypedefGen ();
    TypedefEntry.typedefGen = tgen == null ? noop : tgen;

    UnionGen ugen = genFactory.createUnionGen ();
    UnionEntry.unionGen = ugen == null ? noop : ugen;
  } // initGenerators

  /**
   * Write the version number of this compiler to standard out.
   **/
  protected void displayVersion ()
  {
    String message = Util.getMessage ("Version.product", Util.getMessage("Version.number"));
    System.out.println (message);
  }

  /**
   * This is the repository of emitter arguments.
   **/
  public Arguments arguments           = null;
  /**
   * This hashtable contains &lt;real name, alias&gt; pairs.  It is filled in by
   * extenders in cases where they wish to override an IDL type name with
   * some other name.  For instance, when mapping to Java, there could be
   * an overrideNames entry of &lt;"TRUE", "true"&gt;.  NOTE:  Do NOT change this
   * variable to a new Hash table.  Just add elements to it.
   **/
  protected Hashtable<String, String> overrideNames    = new Hashtable<>();
  /**
   * This is the symbol table.  It will be empty until the parse method
   * executes.  If errors are encountered, the state of the symbol table
   * is undefined.
   **/
  protected Hashtable<String,SymtabEntry> symbolTable      = new Hashtable<>();
  /**
   * This is a vector of strings of the form "IDLfile" or &lt;IDLfile&gt;.  It is
   * a list of the files included in the given IDL file.  It will be empty
   * until the parse method executes.  If errors are encountered, the state
   * of this vector is undefined.
   **/
  protected Vector<String> includes            = new Vector ();
  /**
   * This is a vector of IncludeEntry's.  It is a list of the files included
   * in the given IDL file.  It mirrors the includes vector.  It will be empty
   * until the parse method executes.  If errors are encountered, the state of
   * this vector is undefined.
   **/
  private Vector<IncludeEntry> includeEntries      = new Vector<>();
  private static Noop noop           = new Noop();
  private GenFactory genFactory     = null;
  private SymtabFactory symtabFactory  = null;
  private ExprFactory   exprFactory    = null;
  private Parser parser         = null;
  private Preprocessor preprocessor   = new Preprocessor();
  private Enumeration<SymtabEntry>   emitList       = null;
  private String[]      keywords       = null;
} // class Compile


