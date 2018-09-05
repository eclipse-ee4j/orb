/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package test;

import java.lang.management.RuntimeMXBean ;
import java.lang.management.ManagementFactory ;

import java.util.List ;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Date;
import java.util.StringTokenizer;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.io.BufferedReader;
import sun.tools.java.ClassPath;
import sun.tools.java.ClassFile;
import java.net.ServerSocket;
import java.util.Properties;
import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.spi.orb.ORB ;

/**
 * The abstract Test class.  The simplest way to use this class is to inherit from it
 * and implement the run method.  Set the status to an Error exception if the test
 * did not pass.
 *
 * A typical invocation :
 *      java test.Test -test javax.rmi.CORBA.io.BaseIOTest -wait -verbose -iterate 5
 *
 * @see #UsageAndExit
 **/
public abstract class Test implements java.lang.Runnable
{
    private static final boolean DEBUG_CLASS_LOADER = false ;

    static {
        displayRuntimeInformation() ;
    } 

    public static String display( String[] args ) {
        String result = null ;
        for (String str : args) {
            if (result == null) 
                result = "[" + str ;
            else
                result += ", " + str ;
        }
        result += "]" ;
        return result ;
    }

    public static void displayRuntimeInformation() {
        if (DEBUG_CLASS_LOADER) {
            RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean() ;

            System.out.println( "Boot classpath = " ) ;
            for (String str : bean.getBootClassPath().split(":")) 
                System.out.println( "\t" + str ) ;

            System.out.println( "System classpath = " ) ;
            for (String str : bean.getClassPath().split(":")) 
                System.out.println( "\t" + str ) ;

            List<String> args = bean.getInputArguments() ;
            System.out.println( "VM arguments:" ) ;
            for (String str : args ) 
                System.out.println( "\t" + str ) ;
        }
    }

    // Set this to true to turn on the use of a security manager
    // that actually enforces security (and watch all of the tests 
    // fail due to access exceptions).
    private static final boolean USE_REAL_SECURITY_MANAGER = false ;

    // Strings MUST be lower case.  The hash table is initialized
    // with the command line arguments converted to lower case.
    public static String OUTPUT_DIRECTORY               = "-output";
    public static String TEST_CLASS_NAME_FLAG           = "-test";
    public static String TEST_FILE_NAME_FLAG            = "-file";
    public static String NUMBER_OF_ITERATIONS_FLAG      = "-iterate";
    public static String NO_ITERATE_FLAG                = "-noiterate";
    public static String VERBOSE_FLAG                   = "-verbose";
    public static String DEMURE_FLAG                    = "-demure";
    public static String SEPARATE_PROCESS_FLAG          = "-separateprocess";
    public static String WAIT_FLAG                      = "-wait";
    public static String ORB_DEBUG                      = "-orbdebug";
    public static String DURATION                       = "duration";
    public static String STATUS                         = "status";
    public static String FVD_FLAG                       = "-fvd";
    public static String DEBUG_FLAG                     = "-debug";
    public static String FORK_DEBUG_LEVEL               = "-forkdebuglevel";
    public static String IF_PRESENT_FLAG                = "-ifpresent";

    public static final String ORB_CLASS_KEY = "org.omg.CORBA.ORBClass";
    public static final String ORB_CLASS = "com.sun.corba.ee.impl.orb.ORBImpl";

    protected Hashtable args;
    protected Error status = null;
    protected String sArgs[] = null;
    protected boolean verbose = false;
    protected boolean demure = false;

    private Hashtable<String, Object> results[];
    private static File testPackageDir = null;
    private static File outputDir = null;
    private static int mainResult = 0;

    public static boolean debug = false;
    public static int forkDebugLevel = 0 ;
    
    // forkDebugLevel values 
    public static int DISPLAY = 1 ;
    public static int ATTACH = 2 ;

    public static void dprint( String msg ) 
    {
        if (debug)
            System.out.println( "TEST: " + msg ) ;
    }

    public static void UsageAndExit()
    {
        System.out.println();
        System.out.println("Usage: java test.Test <class args> | <file args>\n");
        System.out.println("      <class args>: -test <test class> [-iterate <#>] [-verbose] [<test args>]");
        System.out.println("       <file args>: -file <test file path> [-iterate <#>] [-verbose]\n");
        System.out.println("      <test class>: fully qualified test class name\n");
        System.out.println("  <test file path>: @<path relative to test.Test dir> | <absolutePath>");
        System.out.println("       <test file>: [<class args> | <file args> \\n]...[<class args> | <file args> \\n]");
        System.exit(1);
    }

    /**
     * Takes an array of strings and produces a Hashtable wherein the keys
     * are strings prefixed with a hyphen and the values are the strings which
     * immediately follow such hyphen prefixed strings.  However, if strings
     * '-one' and '-two' are found in sequence, the key '-one' has an empty
     * string for a value.
     * <p>
     * Example :
     * <p>
     * If an array of {'-one','-two','Value2','-three','Value3'} is the input,
     * the Hashtable { key = '-one' value = ''; key = '-two' value = 'Value2';
     * key = '-three' value = 'Value3'} is the output.
     *
     * @param args An array of strings.
     * @return The hashtable created from the array
     **/
    public static Hashtable<String, String> createFromConsolePairs(String args[])
    {
        int i = 0;
        Hashtable<String, String> table = new Hashtable<String, String>();
        String key, value;

        while(i < args.length){
            key = args[i].toLowerCase();

            if (key.charAt(0) != '-')
                throw new Error(key + " is not a key");

            i++;
            if (i == args.length)
                value = "";
            else {
                value = args[i];

                if (value.charAt(0) == '-') {
                    value = "";
                } else
                    i++;
            }

            table.put(key, value);

        }

        if (table.containsKey( DEBUG_FLAG ))
            dprint( "Contents of argument table: " + table ) ;

        return table;
    }

    /**
     * Return a list of free ports.
     **/
    public static int[] getFreePorts(int count) throws IOException {
        int[] result = new int[count];
        ServerSocket[] sockets = new ServerSocket[count];
        for (int i = 0; i < count; i++) {
            sockets[i] = new ServerSocket(0);
            result[i] = sockets[i].getLocalPort();
        }
        for (int i = 0; i < count; i++) {
            sockets[i].close();
        }
        return result;
    }
    
    /**
     * Gets the main argument list as a Hashtable.
     **/
    public Hashtable getArgs()
    {
        return args;
    }

    /**
     * Sets the Hashtable argument list.
     **/
    public void setArgs(Hashtable args)
    {
        try {
            this.args = args;
            verbose = args.containsKey(VERBOSE_FLAG);
            demure = args.containsKey(DEMURE_FLAG);

            // If FVD_FLAG exists, set test boolean on IIOPInputStream.
            // This forces the runtime to always use FullValueDescription 
            // instead of ObjectStreamClass
        } catch (java.lang.Throwable thr) {
            System.out.println( "Caught exception " + thr ) ;
            thr.printStackTrace() ;
        }
    }

    /**
     * Gets the main argument list as the original String array.
     * @return String args[]
     **/
    public String[] getArgsAsArgs()
    {
        return sArgs;
    }

    public void createResultsTable( int num )
    {
        results = new Hashtable[num] ;
    }

    public static void main(String args[])
    {
        // checkSunTools() ;

        if (System.getProperty( "jcov" ) != null)
            Runtime.getRuntime().addShutdownHook(
                new ShutdownHook() ) ;

        try {
            if (USE_REAL_SECURITY_MANAGER)
                System.setSecurityManager( new SecurityManager() ) ;
            else
                // This disables all security checks. 
                System.setSecurityManager(new javax.rmi.download.SecurityManager());

            // Initialize the port based properties with free ports...

            Properties sysProps = System.getProperties();
            int[] ports = getFreePorts(2);
            
            if (sysProps.get("http.server.port") == null) {
                // System.out.println("Setting http.server.port = "+ports[0]);
                sysProps.put("http.server.port",Integer.toString(ports[0]));
            }
            
            if (sysProps.get("name.server.port") == null) {
                // System.out.println("Setting name.server.port = "+ports[1]);
                sysProps.put("name.server.port",Integer.toString(ports[1]));
            }
            
            if (sysProps.get("java.rmi.server.codebase") == null) {
                String codebase = "http://localhost:"+Integer.toString(ports[0])+"/";
                // System.out.println("Setting java.rmi.server.codebase = "+codebase);
                sysProps.put("java.rmi.server.codebase",codebase);
            }

            // Initialize the ORB class property...
            if (sysProps.get(ORB_CLASS_KEY) == null) {
                sysProps.put(ORB_CLASS_KEY, ORB_CLASS);
            }

            // Run the test...
            
            if (run(args)) {
                System.out.println("\nTests Completed\n ---- Press CTRL-C to Finish ----");
                while(true);
            }
        } catch (Throwable thr) {
            System.out.println("Unexpected throwable: " + thr ) ;
            thr.printStackTrace() ;
            mainResult = 1;
        } finally {
            // Make sure we clean up...
            ServantContext.destroyAll();

            // And ensure we exit...
            System.exit(mainResult);
        }
    }


    /**
     * Get a resource as stream given a fully qualified class name.
     * @param className The fully qualified class name.
     * @param extensionName The resource extension name (e.g. ".class", ".properties", etc).
     **/
    public static InputStreamReader getResource (   String className,
                                                    String extensionName)
        throws ClassNotFoundException {

        // Get the class instance...

        Class theClass = Class.forName(className);

        // Create the resource name...

        String resourceName = className;
        int index = resourceName.lastIndexOf('.');

        if (index >= 0) {
            resourceName = resourceName.substring(index+1);
        }

        resourceName += extensionName;

        // Get the resource stream...

        InputStream resource = theClass.getResourceAsStream(resourceName);

        // Wrap it up and return it as a InputStreamReader...

        if (resource != null) {
            return new InputStreamReader(resource);
        } else {
            return null;
        }
    }

    /**
     * Return the specified resource as an array of strings. Whitespace is trimmed
     * from both ends of all strings. Blank lines are ignored.
     * @param className The fully qualified class name.
     * @param extensionName The resource extension name (e.g. ".classlist", ".array", etc).
     * @param ignoreLinesStartingWith Any lines which begin with this string (after
     * whitespace has been trimmed) will be ignored. May be null.
     */
    protected String[] getResourceAsArray ( String className,
                                            String extensionName,
                                            String ignoreLinesStartingWith)
        throws ClassNotFoundException, IOException {

        String[] result = null;
        InputStreamReader inStream = getResource(className,extensionName);

        if (inStream == null) {
            throw new Error("Could not load resource: " + className + extensionName);
        }

        try {
            Vector<String> list = new Vector<String>();
            BufferedReader in = new BufferedReader(inStream);

            while (true) {
                String line = in.readLine();
                if (line != null) {
                    line = line.trim();
                    if (line.length() > 0) {
                        if (ignoreLinesStartingWith == null || !line.startsWith(ignoreLinesStartingWith)) {
                            list.addElement(line);
                        }
                    }
                } else {
                    break;
                }
            }

            result = new String[list.size()];
            list.copyInto(result);

        } finally {
            inStream.close();
        }

        return result;
    }

    /**
     * Compare two resources for the given class, ignoring whitespace.
     * @param className The fully qualified class name.
     * @param sourceExtension The 'file' extension for the source (e.g. ".properties").
     * @param targetExtension The 'file' extension for the target (e.g. ".reference").
     * @param ignorePrefix Ignore all lines beginning with these strings. May be null.
     */
    public void compareUnorderedResources ( String className,
                                            String sourceExtension,
                                            String targetExtension,
                                            String[] ignorePrefix)
        throws ClassNotFoundException,
               IOException {

        // Get resources as string arrays and check them...

        String[] source = getResourceAsArray(className,sourceExtension,null);
        String[] target = getResourceAsArray(className,targetExtension,null);

        if (source == null) {
            throw new Error("Could not load " + className + sourceExtension + " resource.");
        }

        if (target == null) {
            throw new Error("Could not load " + className + targetExtension + " resource.");
        }

        if (source.length != target.length) {
            throw new Error("Resource comparison failed for " + className + ": resource lengths different");
        }

        // Sort them...

        sort(source);
        sort(target);

        // Compare them...

        for (int i = 0; i < source.length; i++) {

            boolean match = true;

            if (!source[i].equals(target[i])) {

                // Doesn't match. Should we ignore both lines?
                
                boolean doTokenMatch = true;
                boolean ignoreSource = false;
                boolean ignoreTarget = false;
                
                if (ignorePrefix != null) {
                    for (String anIgnorePrefix : ignorePrefix) {
                        if (source[i].startsWith(anIgnorePrefix)) {
                            ignoreSource = true;
                        }
                        if (target[i].startsWith(anIgnorePrefix)) {
                            ignoreTarget = true;
                        }
                    }
                }

                // Are we supposed to ignore BOTH lines?
                
                if (ignoreSource && ignoreTarget) {
                    
                    // Yes, so skip token match and leave match = true...
                    
                    doTokenMatch = false;
                    
                } else {
                    
                    // No, are we supposed to ignore ONE line?
                    
                    if (ignoreSource || ignoreTarget) {
                        
                        // Yes, so skip token match and set match = false...
                        
                        doTokenMatch = false;
                        match = false;
                    }
                }
                
                if (doTokenMatch) {

                    // Nope, so tokenize, sort and re-compare...

                    StringTokenizer s = new StringTokenizer(source[i],", \n\t\r");
                    StringTokenizer t = new StringTokenizer(target[i],", \n\t\r");

                    int sLen = s.countTokens();
                    int tLen = t.countTokens();

                    if (sLen == tLen) {
                        String[] sTokens = new String[sLen];
                        String[] tTokens = new String[sLen];
                        int j = 0;
                        while (s.hasMoreTokens()) {
                            sTokens[j++] = s.nextToken();
                        }
                        j = 0;
                        while (t.hasMoreTokens()) {
                            tTokens[j++] = t.nextToken();
                        }
                        sort(sTokens);
                        sort(tTokens);
                        for (j = 0; j < sLen; j++) {
                            if (!sTokens[j].equals(tTokens[j])) {
                                match = false;
                                break;
                            }
                        }
                    } else {
                        match = false;
                    }
                }
            }
            
            if (!match) {
                String error =  "found '" + source[i] + "', expected '" + target[i] + "'.";
                throw new Error("Resource comparison failed for " + className + ": " + error);
            }
        }
    }


    /**
     * Compare two resources for the given class, ignoring whitespace.
     * @param className The fully qualified class name.
     * @param sourceExtension The 'file' extension for the source (e.g. ".properties").
     * @param targetExtension The 'file' extension for the target (e.g. ".reference").
     */
    public void compareResources (  String className,
                                    String sourceExtension,
                                    String targetExtension)
        throws ClassNotFoundException,
               IOException {
        boolean done = false;
        InputStreamReader inStream = null;
        InputStreamReader refStream = null;

        try {
            // First, get our resource streams and make sure they are valid...
            inStream = getResource(className,sourceExtension);
            refStream = getResource(className,targetExtension);

            if (inStream == null) {
                throw new Error("Could not load " + className + sourceExtension + " resource.");
            }

            if (refStream == null) {
                throw new Error("Could not load " + className + targetExtension + " resource.");
            }

            StreamTokenizer in = new StreamTokenizer(inStream);
            StreamTokenizer ref = new StreamTokenizer(refStream);

            // Turn off 'number' parsing...
            in.ordinaryChars('0','9');
            in.wordChars('0','9');
            in.ordinaryChars('-','-');
            in.wordChars('-','-');
            in.ordinaryChars('.','.');
            in.wordChars('.','.');

            ref.ordinaryChars('0','9');
            ref.wordChars('0','9');
            ref.ordinaryChars('-','-');
            ref.wordChars('-','-');
            ref.ordinaryChars('.','.');
            ref.wordChars('.','.');

            while (!done) {
                String expected = null;
                String found = null;

                if (in.nextToken() == ref.nextToken()) {
                    if (in.ttype == StreamTokenizer.TT_EOF) {
                        done = true;
                    } else {
                        if (in.sval == null && ref.sval == null) {
                        } else {
                            if (in.sval == null || ref.sval == null || !in.sval.equals(ref.sval)) {
                                found = in.sval;
                                expected = ref.sval;
                            }
                        }
                    }
                } else {
                    found = String.valueOf((char)in.ttype);
                    expected = String.valueOf((char)ref.ttype);
                }

                if (expected != null) {
                    String error =  "found '" + found+ "', expected '" + expected + "' (line " + in.lineno() + ").";
                    throw new Error("Resource comparison failed for " + className + ": " + error);
                }
            }
        } finally {
            if (inStream != null) 
                inStream.close();
            if (refStream != null) 
                refStream.close();
        }
    }

        
    // Returns a platform specific build classes directory (either windows or unix ONLY). 
    // Returns null if tests are in test jar or build directory does not exist.
    //
    // NOTE : This is meant strictly for the original RMI-IIOP workspace structure.
    public static String getClassesDirectory(String childWorkspace) {
                
        String className = "com.sun.corba.ee.impl.orb.ORBImpl";
        ClassPath classPath = new ClassPath(System.getProperty("java.class.path"));
        ClassFile cls = classPath.getFile(className.replace('.',File.separatorChar) + ".class");
        try {
            classPath.close();
        } catch (IOException e) {
            // ignore
        }
                
        if (cls != null) {
                        
            File pathDir;
                        
            // Ok, we have a ClassFile for com.sun.corba.ee.impl.orb.ORBImpl.class. Is it in 
            // a zip file?
                        
            if (cls.isZipped()) {
                                
                return null;

            } else {
                String platform = "win32";

                                // No, so walk back to root...
                                
                String path = cls.getPath();
                if (File.separatorChar == '\\') {
                    path = path.replace('/',File.separatorChar);
                } else {
                    platform = "solaris";
                    path = path.replace('\\',File.separatorChar);
                }
                File dir = new File(path);
                                

                // the hardcoded number 10 has to be changed depending on
                // the hierarchy presented by the className, if the hierarchy
                // changes then this number needs to be changed

                for (int i = 0; i < 10; i++) {
                    dir = new File(dir.getParent());
                                        
                }
                                
                pathDir = new File(dir,
                                   childWorkspace+File.separatorChar+
                                   "build"+File.separatorChar+
                                   platform+File.separatorChar+
                                   "classes");
            }
                        
            // If we have a directory, update the Hashtable...
                        
            if (pathDir.exists() && pathDir.isDirectory()) {
                return pathDir.getPath();
            }
            else return null;
        }
        else return null;
    }
        
    private static void setDefaultOutputDir(Hashtable<String, String> flags) {
        String outputDir = flags.get(OUTPUT_DIRECTORY);
        if (outputDir == null) {
        
            // No output dir, so set to gen if present...
 
            String className = "com.sun.corba.ee.spi.orb.ORB";
            ClassPath classPath = new ClassPath(System.getProperty("java.class.path"));
            ClassFile cls = classPath.getFile(className.replace('.',File.separatorChar) + ".class");
            try {
                classPath.close();
            } catch (IOException e) {
                // discard any exception
            }

            if (cls != null) {

                File genDir = null;
                
                // Ok, we have a ClassFile for com.sun.corba.ee.spi.orb.ORB.class. Is it in 
                // a zip file?
                
                if (cls.isZipped()) {
                    
                    // Yes. _REVISIT_
                    
                    System.out.println("test.Test found com.sun.corba.ee.spi.orb.ORB.class in: " + cls.getPath());
                    System.out.println("The output directory did not default to 'gen'.");
                
                } else {
                    
                    // No, so walk back to root...
                    
                    String path = cls.getPath();
                    if (File.separatorChar == '\\') {
                        path = path.replace('/',File.separatorChar);
                    } else {
                        path = path.replace('\\',File.separatorChar);
                    }
                    File dir = new File(path);

                    for (int i = 0; i < 7; i++) {
                        dir = new File(dir.getParent());

                    }

                    genDir = new File(dir,"gen");
                }

                // If we have a directory, update the Hashtable...
                
                if (genDir != null && genDir.exists() && genDir.isDirectory()) {
                    flags.put(OUTPUT_DIRECTORY,genDir.getPath());
                }
            }
        }
    }
    
    private static boolean expandMagicDirectoryFor(String key, Hashtable<String, String> flags) {

        boolean result = false;
        String value = flags.get(key);

        // Do we need to expand our magic directory?

        if (value != null && (value.startsWith("%") || value.startsWith("@"))) {

            // Yes, do we already have our test directory?

            if (testPackageDir == null) {

                // Nope, so look it up...

                testPackageDir = ClassUtils.packageDirectory("test.Test",true);

                if (testPackageDir == null || !testPackageDir.isDirectory()) {
                    System.out.println("Cannot find directory for 'test'.");
                    System.exit(1);
                }

                // Make an output dir also...

                outputDir = new File(testPackageDir.getParent());
            }

            // Make a new value...

            String magicValue;

            if (key.equals(OUTPUT_DIRECTORY)) {
                magicValue = outputDir.getPath();
            } else {

                if (value.length() > 1) {
                    File theFile = new File(testPackageDir,value.substring(1));
                    magicValue = theFile.getPath();
                } else {
                    magicValue = testPackageDir.getPath();
                }
            }

            // Change the value in the hashtable...

            flags.put(key,magicValue);
            result = true;
        }

        return result;
    }

    public String getPassed() {
        return "Passed";
    }

    public String getFailed(Error e) {
        return "Failed, caught " + e;
    }

    public String getName () {
        String className = getClass().getName();
        int index = className.lastIndexOf('.');
        if (index >= 0) {
            return className.substring(index+1);
        } else {
            return className;
        }
    }

    public static boolean run( String args[])
    {
        Hashtable<String, String> flags = createFromConsolePairs(args);

        // Expand our 'magic' directories, if need be...
        expandMagicDirectoryFor(TEST_FILE_NAME_FLAG,flags);
        expandMagicDirectoryFor(OUTPUT_DIRECTORY,flags);
        setDefaultOutputDir(flags);

        String testClassName = flags.get(TEST_CLASS_NAME_FLAG);

        // Are we running a single test class?


        if (testClassName != null) {
            runTestClass(testClassName,flags,args);
        } else {
            // No, are we running a test file?
            String testFileName = flags.get(TEST_FILE_NAME_FLAG);

            if (testFileName != null) {
                // Yes, so do it...
                runTestFile(testFileName,args);
            } else {
                // Nope, so error...
                UsageAndExit();
            }
        }

        return (flags.containsKey(WAIT_FLAG)) ;
    }
    
    private static boolean useDynamicStubs()
    {
        // return Boolean.getBoolean( 
            // ORBConstants.USE_DYNAMIC_STUB_PROPERTY ) ;
        return ORB.getPresentationManager().useDynamicStubs() ;
    }

    private static boolean useCodegenReflectiveCopyobject()
    {
        return Boolean.getBoolean( 
            ORBConstants.USE_CODEGEN_REFLECTIVE_COPYOBJECT ) ;
    }

    // This is needed in several tests.
    public static boolean useJavaSerialization()
    {
        return Boolean.getBoolean( 
            ORBConstants.ENABLE_JAVA_SERIALIZATION_PROPERTY ) ;
    }

    public static boolean debugORBInit()
    {
        return Boolean.getBoolean( 
            ORBConstants.INIT_DEBUG_PROPERTY ) ;
    }

    public static void identifyEnvironment() 
    {
        System.out.println( "Running on " + new Date() ) ;
        System.out.println( "Running with " + (useCodegenReflectiveCopyobject() ?
            "codegen" : "standard") + " reflective object copier" ) ;
        System.out.println( "Running with " + (useDynamicStubs() ? 
            "dynamic" : "static") + " stubs" ) ;
        System.out.println("Running with serialization encoding: " +
                           (useJavaSerialization() ? "JSG" : "CDR"));
        System.out.println( "Dynamic StubFactoryFactory: " +
            ORB.getPresentationManager().getDynamicStubFactoryFactory() ) ;
        System.out.println("Encoding is " +
            (useJavaSerialization() ? "JSG" : "CDR"));
        System.out.println("ORB initialization debugging is " +
            (debugORBInit() ? "on" : "off"));
        System.out.println( "Environment:" ) ;
        System.out.println( "    java.version               = " + 
            System.getProperty( "java.version" ) ) ;
        System.out.println( "    java.vm.name               = " + 
            System.getProperty( "java.vm.name" ) ) ;
        System.out.println( "    os.name                    = " + 
            System.getProperty( "os.name" ) ) ;
        System.out.println( "    os.arch                    = " + 
            System.getProperty( "os.arch" ) ) ;
        System.out.println( "    os.version                 = " + 
            System.getProperty( "os.version" ) ) ;
    }

    public static void runTestFile(String testFile, String[] args) {
        if (testFile == null) 
            UsageAndExit();

        // Open up the file...
        Vector<String> lines = new Vector<String>();
        DataInputStream stream = null;

        try {
            // Make sure the file exists...

            File theFile = new File(testFile);

            if (!theFile.exists()) {
                System.out.println("Test file does not exist: " + 
                                   theFile.getPath());
                System.exit(1);
            }

            // Read all the lines into the 'lines' vector...
            stream = new DataInputStream(new FileInputStream(theFile));

            String line = stream.readLine();

            while (line != null) {
                line = line.trim();

                if (!line.startsWith("//") && !line.equals("")) {
                    lines.addElement(line);
                }

                line = stream.readLine();
            }

            stream.close();

            System.out.println( "======================================================================" ) ;
            System.out.println( "CORBA test suite " + testFile ) ;
            identifyEnvironment() ;
            System.out.println( "======================================================================" ) ;

            // Run all tests...
            int testCount = lines.size();
            for (int i = 0; i < testCount; i++) {
                line = lines.elementAt(i);

                // Parse the line -- format is same as for main...
                StringTokenizer parser = new StringTokenizer(line,
                                                             ", \t\n\r", false);

                int count = parser.countTokens();
                String[] testArgs = new String[count];
                for (int j = 0; j < count; j++) {
                    testArgs[j] = parser.nextToken();
                }

                // Concatenate the two sets of args...
                String[] theArgs = new String[args.length + testArgs.length];
                System.arraycopy(args,0,theArgs,0,args.length);
                System.arraycopy(testArgs,0,theArgs,args.length,testArgs.length);

                // See if we have -separateprocess flag...
                boolean ownProcess = false;
                for (String testArg : testArgs) {
                    if (testArg.equalsIgnoreCase(SEPARATE_PROCESS_FLAG)) {
                        ownProcess = true;
                        break;
                    }
                }

                // Now run it...
                if (ownProcess) {
                    Vector<String> command = new Vector<String>();
                    command.addElement(System.getProperty("java.home") + "/bin/java");
                    command.addElement("-classpath");
                    command.addElement(System.getProperty("java.class.path"));

                    command.addElement(
                        "-Djavax.rmi.CORBA.UtilClass=" + 
                        "com.sun.corba.ee.impl.javax.rmi.CORBA.Util");
                    command.addElement(
                        "-Djavax.rmi.CORBA.StubClass=" +
                        "com.sun.corba.ee.impl.javax.rmi.CORBA.StubDelegateImpl");
                    command.addElement(
                        "-Djavax.rmi.CORBA.PortableRemoteObjectClass=" +
                        "com.sun.corba.ee.impl.javax.rmi.PortableRemoteObject");
                
                    Util.inheritProperties( command ) ;

                    command.addElement("test.Test");

                    for (String testArg : testArgs) {
                        command.addElement(testArg);
                    }
       
                    for (int j = 0; j < args.length; j++) {
                        if (args[j].equals(TEST_FILE_NAME_FLAG)) {
                            j++;   
                        } else {
                            command.addElement(args[j]);
                        }
                    }
                    
                    String[] theCommand = new String[command.size()];
                    command.copyInto(theCommand);
                    
                    if (forkDebugLevel >= DISPLAY) {
                        System.out.println( "Forking Test:" ) ;
                        for (String str : theCommand) 
                            System.out.println( "\t" + str ) ;
                    }

                    int result = Util.execAndWaitFor(theCommand,System.out,
                                                     System.err);

                    if (result != 0) {
                        mainResult = result;   
                    }
                } else {
                    run(theArgs);
                }
            }
        } catch (IOException e1) {
            try {
                if (stream != null) stream.close();
            } catch (IOException e2) {
                // ignore exception
            }
        }
    }

    private static int getInt( String str ) {
        dprint( "getInt called with " + str ) ;
        if (str == null)
            return 0 ;

        try {
            return Integer.parseInt( str ) ;
        } catch (NumberFormatException exc) {
            return 0 ;
        }
    }

    public static void runTestClass(String testClassName, Hashtable<String, String> flags, String args[]) {
        dprint( "Running test " + testClassName ) ;

        if (testClassName == null) 
            UsageAndExit();

        Test testObj = null;

        try{
            testObj = (Test)Class.forName(testClassName).newInstance();
        } catch(ClassNotFoundException cnfe) {
            System.out.print( "    " + testClassName + ": not found - " ) ;
            if (flags.containsKey( IF_PRESENT_FLAG ) ) {
                System.out.println( "skipping execution" ) ;
                return ;
            } else {
                System.out.println( cnfe ) ;
                cnfe.printStackTrace() ;
                UsageAndExit() ;
            }
        } catch(Throwable e) {
            e.printStackTrace();
            UsageAndExit();
        }

        testObj.sArgs = args;
        testObj.setArgs(flags);

        debug = flags.containsKey (DEBUG_FLAG);
        forkDebugLevel = getInt( (String)flags.get( FORK_DEBUG_LEVEL ) ) ;
        dprint( "forkDebugLevel = " + forkDebugLevel ) ;

        int iterations = 1;
        long sum = 0;
        boolean noIterate = flags.containsKey(NO_ITERATE_FLAG);
        if (!noIterate && flags.containsKey(NUMBER_OF_ITERATIONS_FLAG))
            iterations = Integer.parseInt(flags.get( NUMBER_OF_ITERATIONS_FLAG));
        testObj.createResultsTable( iterations ) ;

        try {
            boolean verbose = flags.containsKey(VERBOSE_FLAG);
            boolean demure = flags.containsKey(DEMURE_FLAG);
            String info = "";
            if (flags.containsKey(FVD_FLAG))
                info = " (Using FVD) ";

            if (verbose) {
                System.out.print("    " + testClassName + info + ": ");
            } else if (demure) {
                System.out.print("    " + testObj.getName() + " ... ");
            }

            dprint( "setup called on test" ) ;
            testObj.setup();

            for (int i = 0; i < iterations; i++) {
                dprint( "Iteration " + i + " begins" ) ;
                if (verbose) {
                    System.out.print("Run " + (i+1) + " : ");
                    System.out.flush();
                }

                dprint( "beginAnIteration called" ) ;
                testObj.beginAnIteration();

                long start = System.currentTimeMillis();

                dprint( "run called" ) ;
                try {
                    testObj.run();
                } catch (Error e) {
                    dprint( "run returned error " + e ) ;
                    testObj.status = e;
                }

                long duration = System.currentTimeMillis() - start;
                sum = sum + duration;
                dprint( "finishAnIteration called" ) ;
                Error status = testObj.finishAnIteration();
                testObj.results[i] = new Hashtable<String, Object>();
                testObj.results[i].put(DURATION, duration);
                if (status != null) {
                    testObj.results[i].put(STATUS, status);
                    mainResult = 1;
                }
                
                if (verbose) {
                    System.out.print("[" + duration + "ms] ");
                    if (status == null) System.out.println("PASSED");
                    else {
                        System.out.println("FAILED");
                        System.out.println(status.getMessage());
                    }
                    System.out.flush();
                } else if (demure) {
                    if (status == null) {
                        System.out.println(testObj.getPassed());
                    } else {
                        System.out.println(testObj.getFailed(status));
                    }
                    System.out.flush();
                }
            }

            if (verbose && iterations > 1) {
                System.out.println("   AVERAGE TIME (MS) = " + (sum/iterations));
            }

            testObj.finish();

        } catch (Throwable e) {
            if (e instanceof ThreadDeath) {
                throw (ThreadDeath) e;
            } else {
                mainResult = 1;
                System.out.print( "    " + testClassName + 
                                  " failed, caught: " + e);
                e.printStackTrace();
            }
        }
    }

    /**
     * Called before the iteration testing for the test class begins.  General setup
     * can be done here.
     **/
    public void setup()
    {
        // no op
    }

    /**
     * Prepare for a single interation just before run() is called.
     **/
    public void beginAnIteration()
    {
        status = null;
    }

    /**
     * Your test method.  Set the Test.status Error code to a java.lang.Error instance
     * if the test did not pass.  Otherwise, leave the status field as null.
     **/
    public abstract void run();

    /**
     * This is called after the run() method finishes to collect status.
     *
     * @return The status(An Error upon failure or null if success)
     **/
    public Error finishAnIteration()
    {
        return status;
    }

    /**
     * Called when all iterations over a test class are completed.  General cleanup
     * can be done here.
     **/
    public void finish()
    {
        // no op
    }


    public static void sort(String a[])
    {
        quickSort(a, 0, a.length - 1);
    }


    /** This is a generic version of C.A.R Hoare's Quick Sort
     * algorithm.  This will handle arrays that are already
     * sorted, and arrays with duplicate keys.<BR>
     *
     * If you think of a one dimensional array as going from
     * the lowest index on the left to the highest index on the right
     * then the parameters to this function are lowest index or
     * left and highest index or right.  The first time you call
     * this function it will be with the parameters 0, a.length - 1.
     *
     * @param a       a String array
     * @param lo0     left boundary of array partition
     * @param hi0     right boundary of array partition
     */
    private static void quickSort(String a[], int lo0, int hi0) {
        int lo = lo0;
        int hi = hi0;
        String mid;

        if ( hi0 > lo0) {

            /* Arbitrarily establishing partition element as the midpoint of
             * the array.
             */
            mid = a[ ( lo0 + hi0 ) / 2 ];

            // loop through the array until indices cross
            while( lo <= hi ) {
                /* find the first element that is greater than or equal to
                 * the partition element starting from the left Index.
                 */
                while( ( lo < hi0 ) && ( a[lo].compareTo(mid) < 0 ))
                    ++lo;

                /* find an element that is smaller than or equal to
                 * the partition element starting from the right Index.
                 */
                while( ( hi > lo0 ) && ( a[hi].compareTo(mid) > 0 ))
                    --hi;

                // if the indexes have not crossed, swap
                if( lo <= hi )
                    {
                        swap(a, lo, hi);
                        ++lo;
                        --hi;
                    }
            }

            /* If the right index has not reached the left side of array
             * must now sort the left partition.
             */
            if( lo0 < hi )
                quickSort( a, lo0, hi );

            /* If the left index has not reached the right side of array
             * must now sort the right partition.
             */
            if( lo < hi0 )
                quickSort( a, lo, hi0 );
        }
    }

    private static void swap(String a[], int i, int j)
    {
        String T;
        T = a[i];
        a[i] = a[j];
        a[j] = T;

    }
}

