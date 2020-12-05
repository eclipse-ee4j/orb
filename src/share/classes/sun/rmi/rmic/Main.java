/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
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

package sun.rmi.rmic;

import java.util.Vector;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.MissingResourceException;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;

import sun.tools.java.ClassFile;
import sun.tools.java.ClassDefinition;
import sun.tools.java.ClassDeclaration;
import sun.tools.java.ClassNotFound;
import sun.tools.java.Identifier;
import sun.tools.java.ClassPath;

import sun.tools.util.CommandLine;
import java.lang.reflect.Constructor;
import java.util.Properties;

/**
 * Main "rmic" program.
 */
@SuppressWarnings({"deprecation"})
public class Main implements sun.rmi.rmic.Constants {
    String sourcePathArg;
    String sysClassPathArg;
    String extDirsArg;
    String classPathString;
    File destDir;
    int flags;
    long tm;
    Vector classes;
    boolean nowrite;
    boolean nocompile;
    boolean keepGenerated;
    boolean status;
    String[] generatorArgs;
    Vector generators;
    Class environmentClass = BatchEnvironment.class;
    boolean iiopGeneration = false;

    /**
     * Name of the program.
     */
    String program;

    /**
     * The stream where error message are printed.
     */
    OutputStream out;

    /**
     * Constructor.
     */
    public Main(OutputStream out, String program) {
        this.out = out;
        this.program = program;
    }

    /**
     * Output a message.
     */
    public void output(String msg) {
        PrintStream out =
            this.out instanceof PrintStream ? (PrintStream)this.out
            : new PrintStream(this.out, true);
        out.println(msg);
    }

    /**
     * Top level error message.  This method is called when the
     * environment could not be set up yet.
     */
    public void error(String msg, Object... args) {
        output(getText(msg, args));
    }

    /**
     * Usage
     */
    public void usage() {
        error("rmic.usage", program);
    }
    
    /**
     * Run the compiler
     */
    public synchronized boolean compile(String argv[]) {
        if (!parseArgs(argv)) {
            return false;
        }

        if (classes.size() == 0) {
            usage();
            return false;
        }

        return doCompile();
    }

    /**
     * Get the destination directory.
     */
    public File getDestinationDir() {
        return destDir;
    }

    /**
     * Parse the arguments for compile.
     */
    public boolean parseArgs(String argv[]) {
        sourcePathArg = null;
        sysClassPathArg = null;
        extDirsArg = null;

        classPathString = null;
        destDir = null;
        flags = F_WARNINGS;
        tm = System.currentTimeMillis();
        classes = new Vector();
        nowrite = false;
        nocompile = false;
        keepGenerated = false;
        generatorArgs = getArray("generator.args",true);
        if (generatorArgs == null) {
            return false;
        }
        generators = new Vector();

        // Pre-process command line for @file arguments
        try {
            argv = CommandLine.parse(argv);
        } catch (FileNotFoundException e) {
            error("rmic.cant.read", e.getMessage());
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        // Parse arguments
        for (int i = 0 ; i < argv.length ; i++) {
            if (argv[i] != null) {
                if (argv[i].equals("-g")) {
                    flags &= ~F_OPT;
                    flags |= F_DEBUG_LINES | F_DEBUG_VARS;
                    argv[i] = null;
                } else if (argv[i].equals("-O")) {
                    flags &= ~F_DEBUG_LINES;
                    flags &= ~F_DEBUG_VARS;
                    flags |= F_OPT | F_DEPENDENCIES;
                    argv[i] = null;
                } else if (argv[i].equals("-nowarn")) {
                    flags &= ~F_WARNINGS;
                    argv[i] = null;
                } else if (argv[i].equals("-debug")) {
                    flags |= F_DUMP;
                    argv[i] = null;
                } else if (argv[i].equals("-depend")) {
                    flags |= F_DEPENDENCIES;
                    argv[i] = null;
                } else if (argv[i].equals("-verbose")) {
                    flags |= F_VERBOSE;
                    argv[i] = null;
                } else if (argv[i].equals("-nowrite")) {
                    nowrite = true;
                    argv[i] = null;
                } else if (argv[i].equals("-Xnocompile")) {
                    nocompile = true;
                    keepGenerated = true;
                    argv[i] = null;
                } else if (argv[i].equals("-keep") ||
                           argv[i].equals("-keepgenerated")) {
                    keepGenerated = true;
                    argv[i] = null;
                } else if (argv[i].equals("-show")) {
                    error("rmic.option.unsupported", "-show");
                    usage();
                    return false;
                } else if (argv[i].equals("-classpath")) {
                    if ((i + 1) < argv.length) {
                        if (classPathString != null) {
                            error("rmic.option.already.seen", "-classpath");
                            usage();
                            return false;
                        }
                        argv[i] = null;
                        classPathString = argv[++i];
                        argv[i] = null;
                    } else {
                        error("rmic.option.requires.argument", "-classpath");
                        usage();
                        return false;
                    }
                } else if (argv[i].equals("-sourcepath")) {
                    if ((i + 1) < argv.length) {
                        if (sourcePathArg != null) {
                            error("rmic.option.already.seen", "-sourcepath");
                            usage();
                            return false;
                        }
                        argv[i] = null;
                        sourcePathArg = argv[++i];
                        argv[i] = null;
                    } else {
                        error("rmic.option.requires.argument", "-sourcepath");
                        usage();
                        return false;
                    }
                } else if (argv[i].equals("-bootclasspath")) {
                    if ((i + 1) < argv.length) {
                        if (sysClassPathArg != null) {
                            error("rmic.option.already.seen", "-bootclasspath");
                            usage();
                            return false;
                        }
                        argv[i] = null;
                        sysClassPathArg = argv[++i];
                        argv[i] = null;
                    } else {
                        error("rmic.option.requires.argument", "-bootclasspath");
                        usage();
                        return false;
                    }
                } else if (argv[i].equals("-extdirs")) {
                    if ((i + 1) < argv.length) {
                        if (extDirsArg != null) {
                            error("rmic.option.already.seen", "-extdirs");
                            usage();
                            return false;
                        }
                        argv[i] = null;
                        extDirsArg = argv[++i];
                        argv[i] = null;
                    } else {
                        error("rmic.option.requires.argument", "-extdirs");
                        usage();
                        return false;
                    }
                } else if (argv[i].equals("-d")) {
                    if ((i + 1) < argv.length) {
                        if (destDir != null) {
                            error("rmic.option.already.seen", "-d");
                            usage();
                            return false;
                        }
                        argv[i] = null;
                        destDir = new File(argv[++i]);
                        argv[i] = null;
                        if (!destDir.exists()) {
                            error("rmic.no.such.directory", destDir.getPath());
                            usage();
                            return false;
                        }
                    } else {
                        error("rmic.option.requires.argument", "-d");
                        usage();
                        return false;
                    }
                } else {
                    if (!checkGeneratorArg(argv,i)) {
                        usage();
                        return false;
                    }
                }
            }
        }


        // Now that all generators have had a chance at the args,
        // scan what's left for classes and illegal args...

        for (int i = 0; i < argv.length; i++) {
            if (argv[i] != null) {
                if (argv[i].startsWith("-")) {
                    error("rmic.no.such.option", argv[i]);
                    usage();
                    return false;
                } else {
                    classes.addElement(argv[i]);
                }
            }
        }


        // If the generators vector is empty, add the default generator...

        if (generators.size() == 0) {
            addGenerator("default");
        }

        return true;
    }

    /**
     * If this argument is for a generator, instantiate it, call
     * parseArgs(...) and add generator to generators vector.
     * Returns false on error.
     */
    protected boolean checkGeneratorArg(String[] argv, int currentIndex) {
        boolean result = true;
        if (argv[currentIndex].startsWith("-")) {
            String arg = argv[currentIndex].substring(1).toLowerCase(); // Remove '-'
            for (int i = 0; i < generatorArgs.length; i++) {
                if (arg.equalsIgnoreCase(generatorArgs[i])) {
                    // Got a match, add Generator and call parseArgs...
                    Generator gen = addGenerator(arg);
                    if (gen == null) {
                        return false;    
                    }
                    result = gen.parseArgs(argv,this);
                    break;
                }
            }
        }
        return result;
    }
        
    /**
     * Instantiate and add a generator to the generators array.
     */
    protected Generator addGenerator(String arg) {
                
        Generator gen;
                
        // Create an instance of the generator and add it to
        // the array...
                
        String className = getString("generator.class." + arg);
        if (className == null) {
            error("rmic.missing.property",arg);
            return null;
        }
                
        try {
            gen = (Generator) Class.forName(className).newInstance();
        } catch (Exception e) {
            error("rmic.cannot.instantiate",className);
            return null;
        }

        generators.addElement(gen);
                
        // Get the environment required by this generator...
                
        Class envClass = BatchEnvironment.class;
        String env = getString("generator.env." + arg);         
        if (env != null) {
            try {
                envClass = Class.forName(env);
                 
                // Is the new class a subclass of the current one?

                if (environmentClass.isAssignableFrom(envClass)) {
                             
                    // Yes, so switch to the new one...
                                
                    environmentClass = envClass;
                            
                } else {
                                
                    // No. Is the current class a subclass of the
                    // new one?
                                
                    if (!envClass.isAssignableFrom(environmentClass)) {
                                    
                        // No, so it's a conflict...

                        error("rmic.cannot.use.both",environmentClass.getName(),envClass.getName());
                        return null;
                    }
                }
            } catch (ClassNotFoundException e) {
                error("rmic.class.not.found",env);
                return null;
            }
        }
                
        // If this is the iiop stub generator, cache
        // that fact for the jrmp generator...
                
        if (arg.equals("iiop")) {
            iiopGeneration = true;
        }
        return gen;
    }
    
    /**
     * Grab a resource string and parse it into an array of strings. Assumes
     * comma separated list.
     * @param name The resource name.
     * @param mustExist If true, throws error if resource does not exist. If
     * false and resource does not exist, returns zero element array.
     */
    protected String[] getArray(String name, boolean mustExist) {
        String[] result = null;
        String value = getString(name);
        if (value == null) {
            if (mustExist) {
                error("rmic.resource.not.found",name);
                return null;
            } else {
                return new String[0];
            }
        }
                
        StringTokenizer parser = new StringTokenizer(value,", \t\n\r", false);
        int count = parser.countTokens();
        result = new String[count];
        for (int i = 0; i < count; i++) {
            result[i] = parser.nextToken();
        }

        return result;
    }
        
    /**
     * Get the correct type of BatchEnvironment
     */
    public BatchEnvironment getEnv() {

        ClassPath classPath =
            BatchEnvironment.createClassPath(classPathString,
                                             sysClassPathArg,
                                             extDirsArg);
        BatchEnvironment result = null;
        try {
            Class[] ctorArgTypes = {OutputStream.class,ClassPath.class,Main.class};
            Object[] ctorArgs = {out,classPath,this};
            Constructor constructor = environmentClass.getConstructor(ctorArgTypes);
            result = (BatchEnvironment) constructor.newInstance(ctorArgs);
            result.reset();
        }
        catch (Exception e) {
            error("rmic.cannot.instantiate",environmentClass.getName());
        }
        return result;
    }
        

    /**
     * Do the compile with the switches and files already supplied
     */
    public boolean doCompile() {
        // Create batch environment
        BatchEnvironment env = getEnv();
        env.flags |= flags;

        // Set the classfile version numbers
        // Compat and 1.1 stubs must retain the old version number.
        env.majorVersion = 45;
        env.minorVersion = 3;
        
        // Preload the "out of memory" error string just in case we run
        // out of memory during the compile.
        String noMemoryErrorString = getText("rmic.no.memory");
        String stackOverflowErrorString = getText("rmic.stack.overflow");

        try {
            /** Load the classes on the command line
             * Replace the entries in classes with the ClassDefinition for the class
             */
            for (int i = classes.size()-1; i >= 0; i-- ) {
                Identifier implClassName =
                    Identifier.lookup((String)classes.elementAt(i));

                /*
                 * Fix bugid 4049354: support using '.' as an inner class
                 * qualifier on the command line (previously, only mangled
                 * inner class names were understood, like "pkg.Outer$Inner").
                 *
                 * The following method, also used by "javap", resolves the
                 * given unmangled inner class name to the appropriate
                 * internal identifier.  For example, it translates
                 * "pkg.Outer.Inner" to "pkg.Outer. Inner".
                 */
                implClassName = env.resolvePackageQualifiedName(implClassName);
                /*
                 * But if we use such an internal inner class name identifier
                 * to load the class definition, the Java compiler will notice
                 * if the impl class is a "private" inner class and then deny
                 * skeletons (needed unless "-v1.2" is used) the ability to
                 * cast to it.  To work around this problem, we mangle inner
                 * class name identifiers to their binary "outer" class name:
                 * "pkg.Outer. Inner" becomes "pkg.Outer$Inner".
                 */
                implClassName = Names.mangleClass(implClassName);

                ClassDeclaration decl = env.getClassDeclaration(implClassName);
                try {
                    ClassDefinition def = decl.getClassDefinition(env);
                    for (int j = 0; j < generators.size(); j++) {
                        Generator gen = (Generator)generators.elementAt(j);
                        gen.generate(env, def, destDir);
                    }
                } catch (ClassNotFound ex) {
                    env.error(0, "rmic.class.not.found", implClassName);
                }

            }

            // compile all classes that need compilation
            if (!nocompile) {
                compileAllClasses(env);
            }
        } catch (OutOfMemoryError ee) {
            // The compiler has run out of memory.  Use the error string
            // which we preloaded.
            env.output(noMemoryErrorString);
            return false;
        } catch (StackOverflowError ee) {
            env.output(stackOverflowErrorString);
            return false;
        } catch (Error ee) {
            // We allow the compiler to take an exception silently if a program
            // error has previously been detected.  Presumably, this makes the
            // compiler more robust in the face of bad error recovery.
            if (env.nerrors == 0 || env.dump()) {
                ee.printStackTrace();
                env.error(0, "fatal.error");
            }
        } catch (Exception ee) {
            if (env.nerrors == 0 || env.dump()) {
                ee.printStackTrace();
                env.error(0, "fatal.exception");
            }
        }

        env.flushErrors();

        boolean status = true;
        if (env.nerrors > 0) {
            String msg = "";
            if (env.nerrors > 1) {
                msg = getText("rmic.errors", env.nerrors);
            } else {
                msg = getText("rmic.1error");
            }
            if (env.nwarnings > 0) {
                if (env.nwarnings > 1) {
                    msg += ", " + getText("rmic.warnings", env.nwarnings);
                } else {
                    msg += ", " + getText("rmic.1warning");
                }
            }
            output(msg);
            status = false;
        } else {
            if (env.nwarnings > 0) {
                if (env.nwarnings > 1) {
                    output(getText("rmic.warnings", env.nwarnings));
                } else {
                    output(getText("rmic.1warning"));
                }
            }
        }

        // last step is to delete generated source files
        if (!keepGenerated) {
            env.deleteGeneratedFiles();
        }

        // We're done
        if (env.verbose()) {
            tm = System.currentTimeMillis() - tm;
            output(getText("rmic.done_in", Long.toString(tm)));
        }

        // Shutdown the environment object and release our resources.
        // Note that while this is unneccessary when rmic is invoked
        // the command line, there are environments in which rmic
        // from is invoked within a server process, so resource
        // reclamation is important...

        env.shutdown();

        sourcePathArg = null;
        sysClassPathArg = null;
        extDirsArg = null;
        classPathString = null;
        destDir = null;
        classes = null;
        generatorArgs = null;
        generators = null;
        environmentClass = null;
        program = null;
        out = null;

        return status;
    }

    /*
     * Compile all classes that need to be compiled.
     */
    public void compileAllClasses (BatchEnvironment env)
        throws ClassNotFound,
               IOException,
               InterruptedException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream(4096);
        boolean done;

        do {
            done = true;
            for (Enumeration e = env.getClasses() ; e.hasMoreElements() ; ) {
                ClassDeclaration c = (ClassDeclaration)e.nextElement();
                done = compileClass(c,buf,env);
            }
        } while (!done);
    }

    /*
     * Compile a single class.
     */
    public boolean compileClass (ClassDeclaration c,
                                 ByteArrayOutputStream buf,
                                 BatchEnvironment env)
        throws ClassNotFound,
               IOException,
               InterruptedException {
        boolean done = true;
        env.flushErrors();
        sun.tools.javac.SourceClass src;

        switch (c.getStatus()) {
        case CS_UNDEFINED:
            {
                if (!env.dependencies()) {
                    break;
                }
                // fall through
            }

        case CS_SOURCE:
            {
                done = false;
                env.loadDefinition(c);
                if (c.getStatus() != CS_PARSED) {
                    break;
                }
                // fall through
            }
                        
        case CS_PARSED:
            {
                if (c.getClassDefinition().isInsideLocal()) {
                    break;
                }
                // If we get to here, then compilation is going
                // to occur. If the -Xnocompile switch is set
                // then fail. Note that this check is required
                // here because this method is called from
                // generators, not just from within this class...

                if (nocompile) {
                    throw new IOException("Compilation required, but -Xnocompile option in effect");
                }

                done = false;

                src = (sun.tools.javac.SourceClass)c.getClassDefinition(env);
                src.check(env);
                c.setDefinition(src, CS_CHECKED);
                // fall through
            }

        case CS_CHECKED:
            {
                src = (sun.tools.javac.SourceClass)c.getClassDefinition(env);
                // bail out if there were any errors
                if (src.getError()) {
                    c.setDefinition(src, CS_COMPILED);
                    break;
                }
                done = false;
                buf.reset();
                src.compile(buf);
                c.setDefinition(src, CS_COMPILED);
                src.cleanup(env);

                if (src.getError() || nowrite) {
                    break;
                }

                String pkgName = c.getName().getQualifier().toString().replace('.', File.separatorChar);
                String className = c.getName().getFlatName().toString().replace('.', SIGC_INNERCLASS) + ".class";

                File file;
                if (destDir != null) {
                    if (pkgName.length() > 0) {
                        file = new File(destDir, pkgName);
                        if (!file.exists()) {
                            file.mkdirs();
                        }
                        file = new File(file, className);
                    } else {
                        file = new File(destDir, className);
                    }
                } else {
                    ClassFile classfile = (ClassFile)src.getSource();
                    if (classfile.isZipped()) {
                        env.error(0, "cant.write", classfile.getPath());
                        break;
                    }
                    file = new File(classfile.getPath());
                    file = new File(file.getParent(), className);
                }

                // Create the file
                try {
                    FileOutputStream out = new FileOutputStream(file.getPath());
                    buf.writeTo(out);
                    out.close();
                    if (env.verbose()) {
                        output(getText("rmic.wrote", file.getPath()));
                    }
                } catch (IOException ee) {
                    env.error(0, "cant.write", file.getPath());
                }
            }
        }
        return done;
    }

    /**
     * Main program
     */
    public static void main(String argv[]) {
        Main compiler = new Main(System.out, "rmic");
        System.exit(compiler.compile(argv) ? 0 : 1);
    }

    /**
     * Return the string value of a named resource in the rmic.properties
     * resource bundle.  If the resource is not found, null is returned.
     */
    public static String getString(String key) {
        if (!resourcesInitialized) {
            initResources();
        }

        // To enable extensions, search the 'resourcesExt'
        // bundle first, followed by the 'resources' bundle...

        if (resourcesExt != null) {
            try {
                return resourcesExt.getString(key);
            } catch (MissingResourceException e) {}
        }

        try {
            return resources.getString(key);
        } catch (MissingResourceException ignore) {
        }
        return null;
    }

    private static boolean resourcesInitialized = false;
    private static ResourceBundle resources;
    private static ResourceBundle resourcesExt = null;

    private static void initResources() {
        try {
            resources =
                ResourceBundle.getBundle("sun.rmi.rmic.resources.rmic");
            resourcesInitialized = true;
            try {
                resourcesExt =
                    ResourceBundle.getBundle("sun.rmi.rmic.resources.rmicext");
            } catch (MissingResourceException e) {}
        } catch (MissingResourceException e) {
            throw new Error("fatal: missing resource bundle: " +
                            e.getClassName());
        }
    }

    public static String getText(String key) {
        String message = getString(key);
        if (message == null) {
            message = "no text found: \"" + key + "\"";
        }
        return message;
    }

    public static String getText(String key, Object... args )
    {
        String format = getString(key);
        if (format == null) {
            format = "no text found: key = \"" + key + "\", " +
                "arguments = " ;
            
            for (int ctr=0; ctr<args.length; ctr++) {
                if (ctr != 0)
                    format += ", " ;
                format += "\"{" + ctr + "}\"" ;
            }
        }

        return java.text.MessageFormat.format(format, args);
    }
}
