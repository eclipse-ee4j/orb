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
// -capitalize and parseTypeModifier should probably be in the
//  generators package.
// -D58319<daz> Add version() method.
// -D62023<daz> Add absDelta() method to support float computations.

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Vector;

public class Util
{
  private static final String DEFAULT_MESSAGE_RESOURCE = "com/sun/tools/corba/ee/idl/idl";
  // <d58319>
  /**
   * Fetch the version number of this build of the IDL Parser Framework
   * from the appropriate properties file.
   * @return the version number contained within the appropriate properties
   *  file, which indicates the build of this IDL Parser Framework.
   **/
  public static String getVersion ()
  {
    return getVersion (DEFAULT_MESSAGE_RESOURCE);
  }

  /**
   * Fetch the version number of this build of the IDL Parser Framework.
   * This method may be called before or after the framework has been
   * initialized. If the framework is initialized, the version information
   * is extracted from the message properties object; otherwise, it is extracted
   * from the indicated messages file.
   * @return the version number.
   **/
  protected static String getVersion(String filename)
  {
    String version = "";
    if (messages == null)  // Use supplied file
    {
      Vector<String> oldMsgFiles = msgResources;
      if (filename == null || filename.equals (""))
        filename = DEFAULT_MESSAGE_RESOURCE;
      filename = filename.replace ('/', File.separatorChar);
      registerMessageResource(filename);
      version = getMessage ("Version.product", getMessage ("Version.number"));
      msgResources = oldMsgFiles;
      messages = null;
    }
    else
    {
      version = getMessage ("Version.product", getMessage ("Version.number"));
    }
    return version;
  } // getVersion

  public static boolean isAttribute (String name, Hashtable symbolTable)
  {
    com.sun.tools.corba.ee.idl.SymtabEntry entry = (com.sun.tools.corba.ee.idl.SymtabEntry)symbolTable.get (name);
    return entry == null ? false : entry instanceof AttributeEntry;
  } // isAttribute

  public static boolean isConst (String name, Hashtable symbolTable)
  {
    com.sun.tools.corba.ee.idl.SymtabEntry entry = (com.sun.tools.corba.ee.idl.SymtabEntry)symbolTable.get (name);
    return entry == null ? false : entry instanceof com.sun.tools.corba.ee.idl.ConstEntry;
  } // isConst

  public static boolean isEnum (String name, Hashtable symbolTable)
  {
    com.sun.tools.corba.ee.idl.SymtabEntry entry = (com.sun.tools.corba.ee.idl.SymtabEntry)symbolTable.get (name);
    return entry == null ? false : entry instanceof com.sun.tools.corba.ee.idl.EnumEntry;
  } // isEnum

  public static boolean isException (String name, Hashtable symbolTable)
  {
    com.sun.tools.corba.ee.idl.SymtabEntry entry = (com.sun.tools.corba.ee.idl.SymtabEntry)symbolTable.get (name);
    return entry == null ? false : entry instanceof com.sun.tools.corba.ee.idl.ExceptionEntry;
  } // isException

  public static boolean isInterface (String name, Hashtable symbolTable)
  {
    com.sun.tools.corba.ee.idl.SymtabEntry entry = (com.sun.tools.corba.ee.idl.SymtabEntry)symbolTable.get (name);
    return entry == null ? false : entry instanceof com.sun.tools.corba.ee.idl.InterfaceEntry;
  } // isInterface

  public static boolean isMethod (String name, Hashtable symbolTable)
  {
    com.sun.tools.corba.ee.idl.SymtabEntry entry = (com.sun.tools.corba.ee.idl.SymtabEntry)symbolTable.get (name);
    return entry == null ? false : entry instanceof com.sun.tools.corba.ee.idl.MethodEntry;
  } // isMethod

  public static boolean isModule (String name, Hashtable symbolTable)
  {
    com.sun.tools.corba.ee.idl.SymtabEntry entry = (com.sun.tools.corba.ee.idl.SymtabEntry)symbolTable.get (name);
    return entry == null ? false : entry instanceof com.sun.tools.corba.ee.idl.ModuleEntry;
  } // isModule

  public static boolean isParameter (String name, Hashtable symbolTable)
  {
    com.sun.tools.corba.ee.idl.SymtabEntry entry = (com.sun.tools.corba.ee.idl.SymtabEntry)symbolTable.get (name);
    return entry == null ? false : entry instanceof com.sun.tools.corba.ee.idl.ParameterEntry;
  } // isParameter

  public static boolean isPrimitive (String name, Hashtable symbolTable)
  {
    // Distinguish "string" because the name could be something like:
    // string(25 + 1)
    com.sun.tools.corba.ee.idl.SymtabEntry entry = (com.sun.tools.corba.ee.idl.SymtabEntry)symbolTable.get (name);
    if (entry == null)
    {
      // If it is null then it may be of the form string(<exp>).
      // Don't just check for string because the name "string" may
      // have been overridden.
      int parenIndex = name.indexOf ('(');
      if (parenIndex >= 0)
        entry = (com.sun.tools.corba.ee.idl.SymtabEntry)symbolTable.get (name.substring (0, parenIndex));
    }
    return entry == null ? false : entry instanceof com.sun.tools.corba.ee.idl.PrimitiveEntry;
  } // isPrimitive

  public static boolean isSequence (String name, Hashtable symbolTable)
  {
    com.sun.tools.corba.ee.idl.SymtabEntry entry = (com.sun.tools.corba.ee.idl.SymtabEntry)symbolTable.get (name);
    return entry == null ? false : entry instanceof com.sun.tools.corba.ee.idl.SequenceEntry;
  } // isSequence

  public static boolean isStruct (String name, Hashtable symbolTable)
  {
    com.sun.tools.corba.ee.idl.SymtabEntry entry = (com.sun.tools.corba.ee.idl.SymtabEntry)symbolTable.get (name);
    return entry == null ? false : entry instanceof com.sun.tools.corba.ee.idl.StructEntry;
  } // isStruct

  public static boolean isString (String name, Hashtable symbolTable)
  {
    com.sun.tools.corba.ee.idl.SymtabEntry entry = (com.sun.tools.corba.ee.idl.SymtabEntry)symbolTable.get (name);
    return entry == null ? false : entry instanceof com.sun.tools.corba.ee.idl.StringEntry;
  } // isString

  public static boolean isTypedef (String name, Hashtable symbolTable)
  {
    com.sun.tools.corba.ee.idl.SymtabEntry entry = (com.sun.tools.corba.ee.idl.SymtabEntry)symbolTable.get (name);
    return entry == null ? false : entry instanceof com.sun.tools.corba.ee.idl.TypedefEntry;
  } // isTypedef

  public static boolean isUnion (String name, Hashtable symbolTable)
  {
    com.sun.tools.corba.ee.idl.SymtabEntry entry = (com.sun.tools.corba.ee.idl.SymtabEntry)symbolTable.get (name);
    return entry == null ? false : entry instanceof com.sun.tools.corba.ee.idl.UnionEntry;
  } // isUnion

  //////////////
  // Message-related methods

  public static String getMessage (String key)
  {
    if (messages == null)
      readMessages ();
    String message = messages.getProperty (key);
    if (message == null)
      message = getDefaultMessage (key);
    return message;
  } // getMessage

  public static String getMessage (String key, String fill)
  {
    if (messages == null)
      readMessages ();
    String message = messages.getProperty (key);
    if (message == null)
      message = getDefaultMessage (key);
    else
    {
      int index = message.indexOf ("%0");
      if (index >= 0)
        message = message.substring (0, index) + fill + message.substring (index + 2);
    }
    return message;
  } // getMessage

  public static String getMessage (String key, String[] fill)
  {
    if (messages == null)
      readMessages ();
    String message = messages.getProperty (key);
    if (message == null)
      message = getDefaultMessage (key);
    else
      for (int i = 0; i < fill.length; ++i)
      {
        int index = message.indexOf ("%" + i);
        if (index >= 0)
          message = message.substring (0, index) + fill[i] + message.substring (index + 2);
      }
    return message;
  } // getMessage

  private static String getDefaultMessage (String keyNotFound)
  {
    String message = messages.getProperty (defaultKey);
    int index = message.indexOf ("%0");
    if (index > 0)
      message = message.substring (0, index) + keyNotFound;
    return message;
  } // getDefaultMessage


  private static void readMessages() {
    messages = new Properties();
    for (String msgResource : msgResources)
      loadMessages(msgResource);

    if (messages.size() == 0)
      messages.put(defaultKey, "Error reading Messages File.");
  }

  private static void loadMessages(String msgResource) {
    try {
      ResourceBundle newMessages = ResourceBundle.getBundle(msgResource);
      for (String key : newMessages.keySet())
        messages.setProperty(key, newMessages.getString(key));
    } catch (MissingResourceException ignore) {
    }
  }

  /** Register a message resource.  This resource will be searched for in the CLASSPATH. */
  public static void registerMessageResource(String resourceName) {
    if (resourceName != null)
      if (messages == null)
        msgResources.addElement(resourceName);
      else
        loadMessages(resourceName);
  }

  private static Properties messages   = null;
  private static String     defaultKey = "default";
  private static Vector<String> msgResources = new Vector<>();
  static
  {
    msgResources.addElement (DEFAULT_MESSAGE_RESOURCE);
  }

  // Message-related methods
  ///////////////

  public static String capitalize (String lc) {
    String first = lc.substring(0, 1);
    first = first.toUpperCase();
    return first + lc.substring (1);
  }

  ///////////////
  // General file methods

  /** Searches the current user directory and a list of directories for
      a given short file name and returns its absolute file specification.
      @return Absolute file name of a given short filename
      @throws FileNotFoundException The file does not exist in the
       current user or specified directories.
      @see java.io.File#getAbsolutePath */
  public static String getAbsolutePath (String filename, Vector includePaths) throws FileNotFoundException
  {
    String filepath = null;
    File file = new File (filename);
    if (file.canRead ())
      filepath = file.getAbsolutePath ();
    else
    {
      String fullname = null;
      Enumeration pathList = includePaths.elements ();
      while (!file.canRead () && pathList.hasMoreElements ())
      {
        fullname = (String)pathList.nextElement () + File.separatorChar + filename;
        file = new File (fullname);
      }
      if (file.canRead ())
        filepath = file.getPath ();
      else
        throw new FileNotFoundException (filename);
    }
    return filepath;
  } // getAbsolutePath

  // General file methods
  ///////////////

  ///////////////
  // Numeric computations

  // <d62023>
  /**
   * Compute the absolute value of the difference between two floating-point
   * numbers having single precision.
   * @return the absolute value of the difference between two floats.
   **/
  public static float absDelta (float f1, float f2)
  {
    double delta = f1 - f2;
    return (float)((delta < 0) ? delta * -1.0 : delta);
  } // absDelta

  // Numeric computations
  ///////////////

  static com.sun.tools.corba.ee.idl.RepositoryID emptyID = new com.sun.tools.corba.ee.idl.RepositoryID();
} // class Util
