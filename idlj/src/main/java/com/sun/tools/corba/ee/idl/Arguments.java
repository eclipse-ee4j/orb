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
// -F46082.51<daz> Remove -stateful option. "Stateful interfaces" obsolete.
// -D58319<daz> Add -version option.  Note that this may occur as the last
//  argument on the command-line.
// -F60858.1<daz> Add -corba [level] option.  Accept IDL upto this level, and
//  behave in a "proprietary manner" otherwise.
// -D62023<daz> Add -noWarn option to supress warnings.

import com.sun.tools.corba.ee.idl.som.cff.FileLocator;

import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * This class is responsible for parsing the command line arguments to the compiler. To add new arguments to the
 * compiler, this class must be extended and the parseOtherArgs method overridden.
 **/
public class Arguments {
    /**
     * Method parseOtherArgs() is called when the framework detects arguments which are unknown to it. The default
     * implementation of this method simply throws an InvalidArgument exception. Any overriding implementation must check
     * the arguments passed to it for validity and process the arguments appropriately. If it detects an invalid argument,
     * it should throw an InvalidArgument exception. Arguments MUST be of the form `/&lt;arg&gt; [&lt;qualifiers&gt;]' or
     * `-&lt;arg&gt; [&lt;qualifiers&gt;]' where &lt;qualifiers&gt; is optional (for example, -iC:\includes, `C:\includes'
     * is the qualifier for the argument `i').
     *
     * @param args The arguments which are unknown by the framework.
     * @param properties Environment-style properties collected from the file idl.config.
     * @exception InvalidArgument if the argument is unknown.
     **/
    protected void parseOtherArgs(String[] args, Properties properties) throws InvalidArgument {
        if (args.length > 0)
            throw new InvalidArgument(args[0]);
    } // parseOtherArgs

    protected void setDebugFlags(String args) {
        StringTokenizer st = new StringTokenizer(args, ",");
        while (st.hasMoreTokens()) {
            String token = st.nextToken();

            // If there is a public boolean data member in this class
            // named token + "DebugFlag", set it to true.
            try {
                Field fld = this.getClass().getField(token + "DebugFlag");
                int mod = fld.getModifiers();
                if (Modifier.isPublic(mod) && !Modifier.isStatic(mod))
                    if (fld.getType() == boolean.class)
                        fld.setBoolean(this, true);
            } catch (Exception exc) {
                // ignore it
            }
        }
    }

    /**
     * Collect the command-line parameters.
     **/
    void parseArgs(String[] args) throws InvalidArgument {
        Vector unknownArgs = new Vector();
        int i = 0;

        try {
            // Process command line parameters
            for (i = 0; i < args.length - 1; ++i) {
                if (args[i].charAt(0) != '-' && args[i].charAt(0) != '/')
                    throw new InvalidArgument(args[i]);
                String lcArg = args[i].substring(1).toLowerCase();

                // Include path
                if (lcArg.equals("i")) {
                    includePaths.addElement(args[++i]);
                } else if (lcArg.startsWith("i")) {
                    includePaths.addElement(args[i].substring(2));
                } else if (lcArg.equals("v") || lcArg.equals("verbose")) {
                    // Verbose mode
                    verbose = true;
                } else if (lcArg.equals("d")) {
                    // Define symbol
                    definedSymbols.put(args[++i], "");
                } else if (lcArg.equals("debug")) {
                    // Turn on debug flags
                    setDebugFlags(args[++i]);
                } else if (lcArg.startsWith("d")) {
                    definedSymbols.put(args[i].substring(2), "");
                } else if (lcArg.equals("emitall")) {
                    // Emit bindings for included sources
                    emitAll = true;
                } else if (lcArg.equals("keep")) {
                    // Keep old files
                    keepOldFiles = true;
                } else if (lcArg.equals("nowarn")) {
                    // <d62023> Suppress warnings
                    noWarn = true;
                } else if (lcArg.equals("trace")) {
                    // Allow tracing.
                    Runtime.getRuntime().traceMethodCalls(true);
                }
                // <f46082.51> Remove -stateful feature.
                // else if (lcArg.equals ("stateful"))
                // {
                // Emit stateful bindings.
                // parseStateful = true;
                // }
                // CPPModule
                else if (lcArg.equals("cppmodule")) {
                    cppModule = true;
                } else if (lcArg.equals("version")) {
                    // Version
                    versionRequest = true;
                } else if (lcArg.equals("corba")) {
                    // CORBA level
                    if (i + 1 >= args.length)
                        throw new InvalidArgument(args[i]);
                    String level = args[++i];
                    if (level.charAt(0) == '-')
                        throw new InvalidArgument(args[i - 1]);
                    try {
                        corbaLevel = new Float(level).floatValue();
                    } catch (NumberFormatException e) {
                        throw new InvalidArgument(args[i]);
                    }
                } else {
                    unknownArgs.addElement(args[i]);
                    ++i;
                    while (i < (args.length - 1) && args[i].charAt(0) != '-' && args[i].charAt(0) != '/') {
                        unknownArgs.addElement(args[i++]);
                    }
                    --i;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            // If there is any array indexing problem, it is probably
            // because the qualifier on the last argument is missing.
            // Report that this last argument is invalid.
            throw new InvalidArgument(args[args.length - 1]);
        }

        // <d57319>
        // The last argument is the file argument or "-version", which may
        // be specified without a file argument.
        if (i == args.length - 1) {
            if (args[i].toLowerCase().equals("-version"))
                versionRequest = true;
            else
                file = args[i];
        } else
            throw new InvalidArgument();

        // Get and process the idl.config file.
        Properties props = new Properties();
        try {
            DataInputStream stream = FileLocator.locateFileInClassPath("idl.config");
            props.load(stream);
            addIncludePaths(props);
        } catch (IOException e) {
        }

        // Call parseOtherArgs. By default, if there are unknown args,
        // InvalidArgument is called. A call to parseOtherArgs is useful
        // only when this framework has been extended.
        String[] otherArgs;
        if (unknownArgs.size() > 0) {
            otherArgs = new String[unknownArgs.size()];
            unknownArgs.copyInto(otherArgs);
        } else
            otherArgs = new String[0];

        parseOtherArgs(otherArgs, props);
    } // parseArgs

    /**
     *
     **/
    private void addIncludePaths(Properties props) {
        String paths = props.getProperty("includes");
        if (paths != null) {
            String separator = System.getProperty("path.separator");
            int end = -separator.length(); // so the first pass paths == original paths
            do {
                paths = paths.substring(end + separator.length());
                end = paths.indexOf(separator);
                if (end < 0)
                    end = paths.length();
                includePaths.addElement(paths.substring(0, end));
            } while (end != paths.length());
        }
    } // addIncludePaths

    /**
     * The name of the IDL file.
     **/
    public String file = null;

    /**
     * True if the user wishes to see processing remarks.
     **/
    public boolean verbose = false;

    /**
     * If this is true, then existing files should not be overwritten by the compiler.
     **/
    public boolean keepOldFiles = false;

    /**
     * If this is true, then the types in all included files are also emitted.
     **/
    public boolean emitAll = false;

    // <f46082.51> Remove -stateful feature.
    /// **
    // * If this is true, then stateful interfaces (for the Objects-by-Value
    // * proposal) are allowed. This is not yet a standard, so it must
    // * explicitly be called for by setting the -stateful argument to the
    // * compiler. If -stateful does not appear on the command line, then
    // * the IDL will be parsed according to the standards.
    // **/
    // public boolean parseStateful = false;
    /**
     * A list of strings, each of which is a path from which included files are found.
     **/
    public Vector includePaths = new Vector();

    /**
     * A table of defined symbols. The key is the symbol name; the value (if any) is the replacement value for the symbol.
     **/
    public Hashtable definedSymbols = new Hashtable();

    /**
     * &lt;f46082.46.01&gt; True if new module entries are created for each re-opened module.
     **/
    public boolean cppModule = false;

    /**
     * -version option.
     **/
    public boolean versionRequest = false; // <D58319>

    // <f60858.1> Specify the maximal level of the CORBA spec. the parser
    // will support.
    //
    // NOTE: For BOSS 3.0, specify at 2.2. Raise to greater value in future
    // releases.
    /**
     * -corba [level] option, where [level] is a floating-point number indicating the maximal level of CORBA IDL the parser
     * framework can accept.
     **/
    public float corbaLevel = 2.2f;
    // <d62023>
    /**
     * -noWarn option. Suppress warnings when true.
     **/
    public boolean noWarn = false; // Issue warnings by default.

    // Currently defined debug flags. Any additions must be called xxxDebugFlag.
    // All debug flags must be public boolean types.
    // These are set by passing the flag -ORBDebug x,y,z in the ORB init args.
    // Note that x,y,z must not contain spaces.
    public boolean scannerDebugFlag = false;
    public boolean tokenDebugFlag = false;

} // class Arguments
