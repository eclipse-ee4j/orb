/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2020 Payara Services Ltd.
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

package corba.framework;

import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.spi.JndiConstants;
import test.Util;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

/** Static library class to provide access to test configuration data.
 * This thing need to be further re-written to a class that is instantiated,
 * instead of the current static approach.  It should set up the defaults from
 * system properties and arguments to the constructor in the constructor.
 * It should provide setters and getters as needed for the tests.
 * Note that all setters must update all data, especially including the 
 * properties returned from getORBDProperties, getClientProperties, and getServerProperties.
 */
public class Options {
    // Prevent normal instantiation
    private Options() { }

    public static final String defORBDHandshake = "ORBD is ready.";
    public static final String defServerHandshake = "Server is ready.";

    // Port related:
    private static Port orbInitialPort;
    private static Port activationPort;

    private static String javaIDLHome;                  
    private static String activationDbDirName;          // NO key   defActivationDbDirName
    private static String classpath;                    // key java.class.path
    private static String orbClass;                     // key org.omg.CORBA.ORBClass

    private static Vector orbdArgs ; 
    private static Vector serverArgs ;
    private static Vector clientArgs ;

    // Extra execution strategy arguments
    private static Hashtable orbdExtra ;
    private static Hashtable serverExtra ;
    private static Hashtable clientExtra ;

    // Extra arguments to compilers
    private static Vector rmicArgs ;
    private static Vector idlCompilerArgs ;
    private static Vector javacArgs ;

    // environment properties
    private static Properties defaultProperties ;
    private static Properties ORBDProps ;
    private static Properties serverProps ;
    private static Properties clientProps ;

    private static String emmaFile;

    // Note:  Directories must have the file separator already appended 
    //        to the end
    private static String testDirectory;
    private static String reportDirectory;
    private static String outputDirectory;

    // Files for compilation
    private static String[] javaFiles = null ;
    private static String[] idlFiles = null ;
    private static String[] rmicFiles = null ;

    private static void initializeDefaults( Properties props ) {
        // Initialize default properties from the system properties.
        for (String key : Util.PROCESS_PROPERTIES) {
            String value = System.getProperty( key ) ;
            if (value != null)
                props.setProperty( key, value ) ;
        }

        String prop = props.getProperty(ORBConstants.INITIAL_PORT_PROPERTY);
        if (prop == null)
            orbInitialPort = new Port();
        else
            orbInitialPort = new Port(Integer.parseInt(prop));
        props.setProperty( ORBConstants.INITIAL_PORT_PROPERTY, orbInitialPort.toString() ) ;

        prop = props.getProperty(ORBConstants.ORBD_PORT_PROPERTY);
        if (prop == null)
            activationPort = new Port();
        else
            activationPort = new Port(Integer.parseInt(prop));

        emmaFile = props.getProperty("emma.coverage.out.file", "") ;
        orbClass = props.getProperty("org.omg.CORBA.ORBClass", "com.sun.corba.ee.impl.orb.ORBImpl" );

        props.setProperty( "java.naming.factory.initial", JndiConstants.COSNAMING_CONTEXT_FACTORY ) ;
    }

    private static String getPackageAsDir( CORBATest parent ) {
        String packageName = CORBAUtil.getPackageName(parent);
        String pkg = packageName.replace('.', File.separatorChar);
        return pkg ;
    }

    private static String getTestDirectory( CORBATest parent ) {
        String testBase = (String)(parent.getArgs().get( "-testbase" )) ;
        String testRoot = "" ;
        
        if (testBase == null) {
            testRoot = "/../src/share/classes/";
            testBase = System.getProperty( "user.dir" ) ;
        } else {
            testRoot = "/src/share/classes/";
        }

        String result = testBase + testRoot.replace('/', File.separatorChar) + 
            getPackageAsDir( parent )  + File.separator;
        return result ;
    }

    /**
     * Initialize the options.  This should be called by the 
     * test framework, not individual tests.  It should be called
     * before each new test runs to reset everything.
     *
     *@param  parent   The current test
     */
    public static void init(CORBATest parent) throws IOException
    {
        serverArgs = new Vector(10);
        clientArgs = new Vector(10);

        orbdExtra = new Hashtable(10);
        serverExtra = new Hashtable(10);
        clientExtra = new Hashtable(10);

        orbdExtra.put(ExternalExec.HANDSHAKE_KEY, defORBDHandshake);
        serverExtra.put(ExternalExec.HANDSHAKE_KEY, defServerHandshake);
        
        rmicArgs = new Vector(10);
        idlCompilerArgs = new Vector(10);
        javacArgs = new Vector(10);
        
        javaFiles = null ;
        idlFiles = null ;
        rmicFiles = null ;

        defaultProperties = new Properties() ;
        initializeDefaults( defaultProperties ) ;

        setORBDArgs( "-ORBDebug orbd" ) ;

        testDirectory = getTestDirectory( parent ) ;

        String defaultOutputDirectory = parent.getArgs().get(test.Test.OUTPUT_DIRECTORY)
            + File.separator + getPackageAsDir(parent) + File.separator;

        setDirectories( defaultOutputDirectory, defaultProperties ) ;

        // Set up props based on default properties. The props may be further modified in the tests.
        ORBDProps = new Properties( defaultProperties ) ;
        ORBDProps.setProperty( ORBConstants.PERSISTENT_SERVER_PORT_PROPERTY, getActivationPort() ) ;

        String persistentServerId = defaultProperties.getProperty(ORBConstants.ORB_SERVER_ID_PROPERTY, "1");
        ORBDProps.setProperty( ORBConstants.ORB_SERVER_ID_PROPERTY, persistentServerId ) ;

        serverProps = new Properties( defaultProperties ) ;
        clientProps = new Properties( defaultProperties ) ;
    }

    // Set directories and classpath parts that depend on outputdir.
    private static void setDirectories( String outdir, Properties props ) {
        outputDirectory = outdir ;
        reportDirectory = outdir ;
        props.setProperty( "output.dir", outdir ) ;

        javaIDLHome = outdir + "JavaIDLHome" ;
        props.setProperty( "com.sun.corba.ee.JavaIDLHome", javaIDLHome ) ;

        activationDbDirName = javaIDLHome + File.separator + "db.dir" ;
        props.setProperty( ORBConstants.DB_DIR_PROPERTY, activationDbDirName ) ;

        StringBuilder newPath = new StringBuilder(outputDirectory + File.pathSeparator);
        newPath.append(System.getProperty("java.class.path"));
        classpath = newPath.toString();
    }

    public static long getMaximumTimeout() {
        return 120000 ;
    }

    /**
     * Returns the JavaIDLHome directory path (defaults to
     * output directory/JavaIDLHome).
     */
    public static String getJavaIDLHome() {
        return javaIDLHome;
    }

    /**
     * Get the ORB initial port value (defaults to 1050 unless an
     * environment variable called "org.omg.CORBA.ORBInitialPort"
     * was found).
     */
    public static String getORBInitialPort() {
        return orbInitialPort.toString();
    }

    /**
     * Get the activation port value (defaults to 1049 unless an
     * environment variable called "ActivationPort" was found).
     */
    public static String getActivationPort() {
        return activationPort.toString();
    }

    /**
     * Get argument vector passed to ORBD (initially -ORBDebug orbd).
     */
    public static Vector getORBDArgs() {
        return orbdArgs;
    }

    /**
     * Add a number of command line arguments for the
     * ORBD (space separated).
     */
    public static void setORBDArgs(String values) {
        orbdArgs = new Vector() ;
        Options.addArgsFromString(values, orbdArgs);
    }

    /**
     * Get argument vector passed to a server (default is
     * no arguments).
     */
    public static Vector getServerArgs() {
        return serverArgs;
    }

    /**
     * Add one command line argument for the server.
     */
    public static void addServerArg(String value) {
        serverArgs.add(value);
    }

    /**
     * Add a number of command line arguments for the
     * server (space separated).
     */
    public static void addServerArgs(String values) {
        Options.addArgsFromString(values, serverArgs);
    }

    /**
     * Get argument vector passed to a client (default is
     * no arguments).
     */
    public static Vector getClientArgs() {
        return clientArgs;
    }

    /**
     * Add one command line argument for the client.
     */
    public static void addClientArg(String value) {
        clientArgs.add(value);
    }

    /**
     * Add a number of command line arguments for the
     * client (space separated).
     */
    public static void addClientArgs(String values) {
        Options.addArgsFromString(values, clientArgs);
    }

    /**
     * Return the test's home directory (where it's source
     * files are located.  Defaults to
     * ../src/share/classes/{package}/ since the current
     * directory is assumed to be ../test/make).
     */
    public static String getTestDirectory() {
        return testDirectory;
    }

    /**
     * Return the test's report directory (defaults to
     * the output directory).  This is where output files such
     * as javac.err.txt and javac.out.txt will go.
     */
    public static String getReportDirectory() {
        return reportDirectory;
    }

    /**
     * Return the test's output directory (defaults to
     * {output dir from command line}/{package}/).
     */
    public static String getOutputDirectory() {
        return outputDirectory;
    }

    /**
     * Set the output directory -- disallowed.
     */
    public static void setOutputDirectory(String value) {
        setDirectories( value, defaultProperties ) ;
    }

    /**
     * Return the class path to pass to subprocesses 
     * (defaults to the system property java.class.path
     * plus the output directory).
     */
    public static String getClasspath() {
        return classpath;
    }

    /**
     * Set the class path which will be given to
     * subprocesses.  (Note:  This does not affect the
     * classpath of the current process)
     */
    public static void setClasspath(String value) {
        classpath = value;
    }

    /**
     * Get the ORB class name (defaults to the IIOP ORB).
     */
    public static String getORBClass() {
        return orbClass;
    }

    /**
     * Get a hashtable of extra flags to give to the ORBD
     * execution strategy.
     */
    public static Hashtable getORBDExtra() {
        return orbdExtra;
    }

    /**
     * Get a hashtable of extra flags to give to server
     * execution strategies.
     */
    public static Hashtable getServerExtra() {
        return serverExtra;
    }

    /**
     * Get a hashtable of extra flags to give to client
     * execution strategies.
     */
    public static Hashtable getClientExtra() {
        return clientExtra;
    }

    /**
     * The framework already provides many properties to
     * ORBD, but this allows the test author to override
     * or augment them.
     */
    public static Properties getORBDProperties() {
        return ORBDProps;
    }

    /**
     * The framework already provides many properties to
     * a server, but this allows the test author to override
     * or augment them.
     */
    public static Properties getServerProperties() {
        return serverProps;
    }

    /**
     * The framework already provides many properties to
     * a client, but this allows the test author to override
     * or augment them.
     */
    public static Properties getClientProperties() {
        return clientProps;
    }

    /**
     * Get the string for the java executable to use
     * when creating subprocesses. Set to
     * {java.home}/bin/java).
     */
    public static String getJavaExec() { return System.getProperty("java.home")
            + File.separator + "bin" + File.separator + "java";
    }

    /**
     * Returns a vector of arguments to the Java compiler.
     */
    public static Vector getJavacArgs() {
        return javacArgs;
    }

    /**
     * Add one argument to the vector of Java compiler args.
     */
    public static void addJavacArg(String value) {
        javacArgs.add(value);
    }

    /**
     * Add a number of arguments to the vector of Java compiler
     * args (space separated).
     */
    public static void addJavacArgs(String values) {
        Options.addArgsFromString(values, javacArgs);
    }

    /**
     * Returns a vector of arguments to RMIC.
     */
    public static Vector getRMICArgs() {
        return rmicArgs;
    }

    /**
     * Add one argument to the vector of RMIC args.
     */
    public static void addRMICArg(String value) {
        rmicArgs.add(value);
    }

    /**
     * Add a number of arguments to the vector of RMIC args
     * (space separated).
     */
    public static void addRMICArgs(String values) {
        Options.addArgsFromString(values, rmicArgs);
    }

    /**
     * Returns a vector of arguments to the IDL compiler.
     */
    public static Vector getIDLCompilerArgs() {
        return idlCompilerArgs;
    }

    /**
     * Add one argument to the vector of IDL compiler args.
     */
    public static void addIDLCompilerArg(String value) {
        idlCompilerArgs.add(value);
    }

    /**
     * Add a number of arguments to the vector of IDL compiler
     * args(space separated).
     */
    public static void addIDLCompilerArgs(String values) {
        Options.addArgsFromString(values, idlCompilerArgs);
    }

    /**
     * Utility method for adding space separated arguments to 
     * a given vector.
     */
    private static void addArgsFromString(String args, Vector container)
    {
        StringTokenizer st = new StringTokenizer(args);

        while (st.hasMoreTokens()) {
            String arg = st.nextToken();
            container.add(arg);
        }
    }

    /**
     * Set the array of fully qualified class names to
     * give to RMIC.
     */
    public static void setRMICClasses(String[] value) {
        rmicFiles = value;
    }

    /**
     * Get the array of fully qualified class names that
     * will be given to RMIC.
     */
    public static String[] getRMICClasses() {
        return rmicFiles;
    }

    /**
     * Set the array of .java file names to give to javac
     * (note: any one of these can be either a full path
     * or a file name in the test directory, but must
     * end in .java).
     */
    public static void setJavaFiles(String[] value) {
        javaFiles = value;
    }

    /**
     * Get the array of .java file names that will be
     * given to javac.
     */
    public static String[] getJavaFiles() {
        return javaFiles;
    }

    /**
     * Set the array of .idl file names to give to idlj
     * (note: any one of these can be either a full path
     * or a file name in the test directory).
     */
    public static void setIDLFiles(String[] value) {
        idlFiles = value;
    }

    /**
     * Get the array of IDL file names that will be
     * given to idlj.
     */
    public static String[] getIDLFiles() {
        return idlFiles;
    }

    public static String getEmmaFile() { 
        return emmaFile ;
    }

    /**
     * Create and return an abstraction for an unused port.
     */
    public static Port getUnusedPort() {
        return new Port();
    }
}
