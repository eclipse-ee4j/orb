/*
 * Copyright (c) 1998, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.rmic.iiop;

import org.glassfish.rmic.IndentingWriter;
import org.glassfish.rmic.Main;
import org.glassfish.rmic.tools.java.ClassDefinition;
import org.glassfish.rmic.tools.java.ClassFile;
import org.glassfish.rmic.tools.java.ClassPath;
import org.glassfish.rmic.tools.java.Identifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashSet;

/**
 * Generator provides a small framework from which IIOP-specific
 * generators can inherit.  Common logic is implemented here which uses
 * both abstract methods as well as concrete methods which subclasses may
 * want to override. The following methods must be present in any subclass:
 * <pre>
 *      Default constructor
 *              CompoundType getTopType(BatchEnvironment env, ClassDefinition cdef);
 *      int parseArgs(String argv[], int currentIndex);
 *      boolean requireNewInstance();
 *              OutputType[] getOutputTypesFor(CompoundType topType,
 *                                     HashSet alreadyChecked);
 *              String getFileNameExtensionFor(OutputType outputType);
 *              void writeOutputFor (   OutputType outputType,
 *                              HashSet alreadyChecked,
 *                                                              IndentingWriter writer) throws IOException;
 * </pre>
 * @author      Bryan Atsatt
 */
public abstract class Generator implements      org.glassfish.rmic.Generator,
                                                org.glassfish.rmic.iiop.Constants {

    private boolean alwaysGenerate = false;
    private BatchEnvironment env = null;
    private boolean trace = false;

    /**
     * Examine and consume command line arguments.
     * @param argv The command line arguments. Ignore null
     * and unknown arguments. Set each consumed argument to null.
     * @param main Report any errors using the main.error() methods.
     * @return true if no errors, false otherwise.
     */
    public boolean parseArgs(String argv[], Main main) {
        for (int i = 0; i < argv.length; i++) {
            if (argv[i] != null) {
                if (argv[i].equalsIgnoreCase("-always") ||
                    argv[i].equalsIgnoreCase("-alwaysGenerate")) {
                    alwaysGenerate = true;
                    argv[i] = null;
                } else if (argv[i].equalsIgnoreCase("-xtrace")) {
                    trace = true;
                    argv[i] = null;
                }
            }
        }
        return true;
    }

    /**
     * Return true if non-conforming types should be parsed.
     * @param stack The context stack.
     */
    protected abstract boolean parseNonConforming(ContextStack stack);

    /**
     * Create and return a top-level type.
     * @param cdef The top-level class definition.
     * @param stack The context stack.
     * @return The compound type or null if is non-conforming.
     */
    protected abstract CompoundType getTopType(ClassDefinition cdef, ContextStack stack);

    /**
     * Return an array containing all the file names and types that need to be
     * generated for the given top-level type.  The file names must NOT have an
     * extension (e.g. ".java").
     * @param topType The type returned by getTopType().
     * @param alreadyChecked A set of Types which have already been checked.
     *  Intended to be passed to Type.collectMatching(filter,alreadyChecked).
     */
    protected abstract OutputType[] getOutputTypesFor(CompoundType topType,
                                                      HashSet alreadyChecked);

    /**
     * Return the file name extension for the given file name (e.g. ".java").
     * All files generated with the ".java" extension will be compiled. To
     * change this behavior for ".java" files, override the compileJavaSourceFile
     * method to return false.
     * @param outputType One of the items returned by getOutputTypesFor(...)
     */
    protected abstract String getFileNameExtensionFor(OutputType outputType);

    /**
     * Write the output for the given OutputFileName into the output stream.
     * @param outputType One of the items returned by getOutputTypesFor(...)
     * @param alreadyChecked A set of Types which have already been checked.
     *  Intended to be passed to Type.collectMatching(filter,alreadyChecked).
     * @param writer The output stream.
     */
    protected abstract void writeOutputFor(OutputType outputType,
                                                HashSet alreadyChecked,
                                                IndentingWriter writer) throws IOException;

    /**
     * Return true if a new instance should be created for each
     * class on the command line. Subclasses which return true
     * should override newInstance() to return an appropriately
     * constructed instance.
     */
    protected abstract boolean requireNewInstance();

    /**
     * Return true if the specified file needs generation.
     */
    private boolean requiresGeneration(File target, Type theType) {

        boolean result = alwaysGenerate;

        if (!result) {

            // Get a ClassFile instance for base source or class
            // file.  We use ClassFile so that if the base is in
            // a zip file, we can still get at it's mod time...

            ClassFile baseFile;
            ClassPath path = env.getClassPath();
            String className = theType.getQualifiedName().replace('.',File.separatorChar);

            // First try the source file...

            baseFile = path.getFile(className + ".source");

            if (baseFile == null) {

                // Then try class file...

                baseFile = path.getFile(className + ".class");
            }

            // Do we have a baseFile?

            if (baseFile != null) {

                // Yes, grab baseFile's mod time...

                long baseFileMod = baseFile.lastModified();

                // Get a File instance for the target. If it is a source
                // file, create a class file instead since the source file
                // will frequently be deleted...

                String targetName = IDLNames.replace(target.getName(),".java",".class");
                String parentPath = target.getParent();
                File targetFile = new File(parentPath,targetName);

                // Does the target file exist?

                if (targetFile.exists()) {

                    // Yes, so grab it's mod time...

                    long targetFileMod = targetFile.lastModified();

                    // Set result...

                    result = targetFileMod < baseFileMod;

                } else {

                    // No, so we must generate...

                    result = true;
                }
            } else {

                // No, so we must generate...

                result = true;
            }
        }

        return result;
    }

    /**
     * Create and return a new instance of self. Subclasses
     * which need to do something other than default construction
     * must override this method.
     */
    private Generator newInstance() {
        Generator result = null;
        try {
            result = getClass().newInstance();
        }
        catch (Exception ignored){} // Should ALWAYS work!

        return result;
    }

    /**
     * Default constructor for subclasses to use.
     */
    Generator() {
    }

    /**
     * Generate output. Any source files created which need compilation should
     * be added to the compiler environment using the addGeneratedFile(File)
     * method.
     *  @param env       The compiler environment
     * @param destDir   The directory for the root of the package hierarchy
     * @param cdef      The definition for the implementation class or interface from
 *              which to generate output
     */
    public void generate(org.glassfish.rmic.BatchEnvironment env, File destDir, ClassDefinition cdef) {

        this.env = (BatchEnvironment) env;
        ContextStack contextStack = new ContextStack(this.env);
        contextStack.setTrace(trace);

        // Make sure the environment knows whether or not to parse
        // non-conforming types. This will clear out any previously
        // parsed types if necessary...

        this.env.setParseNonConforming(parseNonConforming(contextStack));

        // Get our top level type...

        CompoundType topType = getTopType(cdef, contextStack);
        if (topType != null) {

            Generator generator = this;

            // Do we need to make a new instance?

            if (requireNewInstance()) {

                                // Yes, so make one.  'this' instance is the one instantiated by Main
                                // and which knows any needed command line args...

                generator = newInstance();
            }

            // Now generate all output files...

            generator.generateOutputFiles(topType, this.env, destDir);
        }
    }

    /**
     * Create and return a new instance of self. Subclasses
     * which need to do something other than default construction
     * must override this method.
     */
    private void generateOutputFiles(CompoundType topType,
                                     BatchEnvironment env,
                                     File destDir) {

        // Grab the 'alreadyChecked' HashSet from the environment...

        HashSet alreadyChecked = env.alreadyChecked;

        // Ask subclass for a list of output types...

        OutputType[] types = getOutputTypesFor(topType,alreadyChecked);

        // Process each file...

        for (OutputType type : types) {
            File file = getFileFor(type, destDir);

            if (!requiresGeneration(file, type.getType())) {
                if (env.verbose()) env.output(Main.getText("rmic.previously.generated", file.getPath()));
            } else {
                // Now create an output stream and ask subclass to fill it up...

                try {
                    IndentingWriter out = new IndentingWriter(
                            new OutputStreamWriter(new FileOutputStream(file)), INDENT_STEP, TAB_SIZE);

                    long startTime = !env.verbose() ? 0 : System.currentTimeMillis();
                    writeOutputFor(type, alreadyChecked, out);
                    out.close();

                    if (env.verbose()) {
                        long duration = System.currentTimeMillis() - startTime;
                        env.output(Main.getText("rmic.generated", file.getPath(), Long.toString(duration)));
                    }
                    postProcessFile(env, file);
                } catch (IOException e) {
                    env.error(0, "cant.write", file.toString());
                    return;
                }
            }
        }
    }

    protected void postProcessFile(BatchEnvironment env, File file) throws FileNotFoundException {}

    /**
     * Return the File object that should be used as the output file
     * for the given OutputType.
     * @param outputType The type to create a file for.
     * @param destinationDir The directory to use as the root of the
     * package heirarchy.  May be null, in which case the current
     * classpath is searched to find the directory in which to create
     * the output file.  If that search fails (most likely because the
     * package directory lives in a zip or jar file rather than the
     * file system), the current user directory is used.
     */
    private File getFileFor(OutputType outputType, File destinationDir) {
        // Calling this method does some crucial initialization
        // in a subclass implementation. Don't skip it.
        Identifier id = getOutputId(outputType);
        File packageDir = getOutputDirectory(destinationDir, id, env);
        String classFileName = outputType.getName() + getFileNameExtensionFor(outputType);
        return new File(packageDir, classFileName);
    }

    protected abstract File getOutputDirectory(File destinationDir, Identifier id, BatchEnvironment environment);


    /**
     * Return an identifier to use for output.
     * @param outputType the type for which output is to be generated.
     * @return the new identifier. This implementation returns the input parameter.
     */
    protected Identifier getOutputId (OutputType outputType) {
        return outputType.getType().getIdentifier();
    }

    //_____________________________________________________________________
    // OutputType is a simple wrapper for a name and a Type
    //_____________________________________________________________________

    public class OutputType {
        private String name;
        private Type type;

        OutputType(String name, Type type) {
            this.name = name;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public Type getType() {
            return type;
        }
    }
}
