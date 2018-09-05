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

import com.sun.corba.ee.impl.util.JDKBridge;
import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.spi.JndiConstants;
import org.omg.CORBA.ORB;
import sun.rmi.rmic.Main;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.rmi.Remote;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

public class Util {

    static private boolean debug = false ;

    static public void trace( String msg )
    {
        if (debug)
            System.out.println( msg ) ;
    }

    public static final String HANDSHAKE = "Ready.";
    
    /*
     * Create an ORB.
     */
    public static ORB createORB ( String nameServerHost,
                                  int nameServerPort, String orbDebugFlags ) {
        // Setup the name server arguments...
        String[] nameServerArgs;

        if (nameServerHost != null) {
            nameServerArgs = new String[4];
            nameServerArgs[2] = "-ORBInitialHost";
            nameServerArgs[3] = nameServerHost;
        } else {
            nameServerArgs = new String[2];
        }

        nameServerArgs[0] = "-ORBInitialPort";
        nameServerArgs[1] = Integer.toString(nameServerPort);

        // Setup the ORB properties...

        Properties props = System.getProperties();

        props.put("org.omg.CORBA.ORBClass", "com.sun.corba.ee.impl.orb.ORBImpl");
        props.put("org.omg.CORBA.ORBSingletonClass", "com.sun.corba.ee.impl.orb.ORBSingleton");

        if (orbDebugFlags != null)
            props.put( ORBConstants.DEBUG_PROPERTY, orbDebugFlags ) ;

        // Create and return the ORB...
        return ORB.init(nameServerArgs, props);
    }

    /*
     * Start up the ORB and return the name context.
     * Name server must be started prior to this call.
     */
    public static Context startORBAndGetNameContext (String nameServerHost,
                                                     int nameServerPort, String orbDebugFlags ) throws Exception  {
        // Create the ORB...
        ORB orb = createORB( nameServerHost, nameServerPort, orbDebugFlags );

        // Return the initial context...
        return getInitialContext(true,nameServerHost,nameServerPort,orb);
    }

    /*
     * Get the initial context.
     * Name server must be started prior to this call.
     */
    public static Context getInitialContext (   boolean iiop,
                                                String nameServerHost,
                                                int nameServerPort,
                                                ORB orb)
        throws Exception {
        Context result = null;

        // Create a hashtable with the correct properties...

        Hashtable env = new Hashtable();

        if (iiop) {

            env.put("java.naming.corba.orb", orb);
            env.put("java.naming.factory.initial", JndiConstants.COSNAMING_CONTEXT_FACTORY);

        } else {

            String serverUrl = "rmi://";

            if (nameServerHost != null && !nameServerHost.equals("")) {
                serverUrl += nameServerHost;
            }

            serverUrl += ":" + Integer.toString(nameServerPort);

            env.put("java.naming.factory.initial", JndiConstants.REGISTRY_CONTEXT_FACTORY);
            env.put("java.naming.provider.url",serverUrl);
        }

        // Now get and return our context...

        return new InitialContext(env);
    }


    /*
     * Start up the ORB and lookup object reference.
     * Name server must be started prior to this call.
     */
    public static Object startORBAndLookup (
                                            String servantName, String nameServerHost, int nameServerPort,
                                            String orbDebugFlags ) throws Exception {
        // Start the ORB and get a name context...
        Context context = startORBAndGetNameContext( nameServerHost,
                                                     nameServerPort, orbDebugFlags );

        // Now do the lookup and return it...

        return context.lookup(servantName);
    }

    public static int getHttpServerPort() {
        String prop = System.getProperty("http.server.port");
        if (prop != null) {
            return Integer.parseInt(prop);
        } else {
            throw new Error("Must set http.server.port property");
        }
    }

    public static int getNameServerPort() {
        String prop = System.getProperty("name.server.port");
        if (prop != null) {
            return Integer.parseInt(prop);
        } else {
            throw new Error("Must set name.server.port property");
    }
    }

    public static void setDefaultCodeBase(boolean iiop) {
        
        // If we don't have a codebase set, set it to a
        // reasonable default (iiop only)...
        
        if (iiop) {
            String codebaseKey = "java.rmi.server.codebase";
            if (System.getProperty(codebaseKey) == null) {                 
                String codebase = "http://localhost:" + getHttpServerPort() + "/";
                System.getProperties().put(codebaseKey,codebase);
                JDKBridge.setCodebaseProperties();
            }
        } else {            
            JDKBridge.setLocalCodebase(null);
        }
    }

    /*
     * Start up the ORB, export and publish servant in the
     * name server. Expects:
     *
     *  args[0] == Implementation class name
     *  args[1] == Name to publish in the name server
     *  args[2] == Name server host (may be null for local)
     *  args[3] == Name server port
     *  args[4] == "-iiop" or "-jrmp"
     *  args[5] == ORB debug flags
     *
     * Name server must be started prior to this call.
     */
    public static void main(String[] args) {
                
        boolean error = false;

        if (args.length != 5) {
            error = true;
        } else {
            boolean iiop = true;
            if (args[4].equalsIgnoreCase("-iiop")) {
            } else if (args[4].equalsIgnoreCase("-jrmp")) {
                iiop = false;
                // Dynamic RMI-IIOP prevents the use of JRMP, so 
                // make sure it is disabled.
                System.setProperty( "com.sun.corba.ee.ORBUseDynamicStub", "false" ) ;
            } else {
                error = true;
            }
                     
            if (!error) {

                if (System.getSecurityManager() == null) {
                    System.setSecurityManager(new javax.rmi.download.SecurityManager());
                }

                setDefaultCodeBase(iiop);

                String host = args[2];
                if (host.equals("-nullHost")) host = null;

                if (!startSingleServant( args[0], args[1], host, Integer.parseInt(args[3]), iiop, null )) {
                    System.exit(1);
                }
            }
        }

        System.out.println("Usage: test.Util <impl class name> <publish name> <name server host> <name server port> <-iiop | -jrmp>");
    }

    /*
     * Start up the ORB, export and publish servant in the
     * name server. Meant to be used as
     * a convenient way to write a main which wants to run
     * a single servant:
     *
     *   public static void main(String[] args) {
     *      Util.startSingleServant("MyImpl","MyServer",1050,true);
     *   }
     *
     * Name server must be started prior to this call.
     */
    public static boolean startSingleServant(  
                                             String servantClassName, String servantName, String nameServerHost, 
                                             int nameServerPort, boolean iiop, String orbDebugFlags ) {
        boolean result = true;
        trace( "Util.startSingleServant called." ) ;

        try {

            Class servantClass = Class.forName(servantClassName);
            Remote servant = (Remote) servantClass.newInstance();
            result = startSingleServant( servant, servantName, nameServerHost,
                                         nameServerPort, iiop, orbDebugFlags );

        } catch (Throwable e) {

            e.printStackTrace(System.out);
            System.out.println();
            System.out.flush();
            result = false;

            if (e instanceof ThreadDeath) {
                throw new ThreadDeath();
            }
        } finally {
            trace( "Util.startSingleServant exiting." ) ;
        }

        return result;
    }

    public static Context singleServantContext = null;

    /*
     * Start up the ORB, export and publish servant in the
     * name server. Meant to be used as
     * a convenient way to write a main which wants to run
     * a single servant.
     *
     * Name server must be started prior to this call.
     */
    public static boolean startSingleServant(  Remote servant, String servantName, 
        String nameServerHost, int nameServerPort, boolean iiop, String orbDebugFlags ) {

        boolean result = true;
        
        trace( "Util.startSingleServant called" ) ;

        try {
            ORB orb = null;

            if (iiop) {
                orb = createORB( null, nameServerPort, orbDebugFlags );
            }

            if (!(servant instanceof PortableRemoteObject)) {
                PortableRemoteObject.exportObject(servant);
            }

            singleServantContext = getInitialContext(iiop,nameServerHost,nameServerPort,orb);

            singleServantContext.rebind(servantName, servant);

            Object stub = singleServantContext.lookup(servantName);

            if (stub != null) {
                System.out.println(HANDSHAKE);
            } else {
                System.out.println("Self test FAILED.");
            }

            System.out.flush();
        } catch (Throwable e) {
            e.printStackTrace(System.out);
            System.out.println();
            System.out.flush();
            result = false;

            trace( "Util.startSingleServant exiting" ) ;
            if (e instanceof ThreadDeath) {
                throw new ThreadDeath();
            }
        }

        return result;
    }

    // List of properties that must be inherited by any process
    // the test framework starts by Runtime.exec.  Used both in
    // the IBM tests (based on test.Test) and the CORBA test framework
    // tests (based on corba.framework.CORBATest).
    public static String[] PROCESS_PROPERTIES =    {
        // RMI delegates
        "javax.rmi.CORBA.UtilClass",
        "javax.rmi.CORBA.StubClass",
        "javax.rmi.CORBA.PortableRemoteObjectClass",

        // Standard ORB impl classes
        "org.omg.CORBA.ORBClass",
        "org.omg.CORBA.ORBSingletonClass",

        // Security related
        "com.sun.corba.ee.ORBBase",
        "java.security.policy",
        "java.security.debug",
        "java.security.manager",

        // Test setup
        "corba.test.orb.classpath",
        "corba.test.controller.name",
        "http.server.port",
        "name.server.port",
        "java.rmi.server.codebase",
        "java.compiler",
        "java.rmi.server.codebase",
        "http.server.root.directory",
        "com.sun.corba.ee.JavaIDLHome",

        // For testing tools
        "emma.coverage.out.file",
        "emma.coverage.out.merge",
        "emma.rt.control",
        "junit.report.dir",
        "net.sourceforge.cobertura.datafile", 

        // Test configuration properties
        ORBConstants.ORB_SERVER_ID_PROPERTY,
        ORBConstants.GIOP_VERSION,
        ORBConstants.GIOP_FRAGMENT_SIZE,
        ORBConstants.GIOP_BUFFER_SIZE,
        ORBConstants.GIOP_11_BUFFMGR,
        ORBConstants.GIOP_12_BUFFMGR,
        ORBConstants.USE_DYNAMIC_STUB_PROPERTY,
        ORBConstants.ENABLE_JAVA_SERIALIZATION_PROPERTY,
        ORBConstants.USE_CODEGEN_REFLECTIVE_COPYOBJECT,
        ORBConstants.INIT_DEBUG_PROPERTY,
        ORBConstants.DEBUG_DYNAMIC_STUB,
        ORBConstants.INITIAL_PORT_PROPERTY,
        ORBConstants.ORBD_PORT_PROPERTY
    };

    public static void inheritProperties( Vector command )
    {
        for (int j = 0; j < PROCESS_PROPERTIES.length; j++) {
            String key = PROCESS_PROPERTIES[j];
            String value = System.getProperty(key);
            if (value != null) {
                command.insertElementAt("-D" + key + "=" + value, 1 );   
            }
        }
    }

    public static Process startProcess( Vector command,
        String handShake ) throws IOException {
        return startProcess( command, handShake, Test.forkDebugLevel ) ;
    }

    public static Process startProcess( Vector command, 
        String handShake, int debugLevel ) throws IOException 
    {
        inheritProperties( command ) ;

        if (debugLevel >= Test.ATTACH) {
            command.insertElementAt( "-Xrunjdwp:transport=dt_socket,server=y,suspend=y", 1 ) ;
            command.insertElementAt( "-Xnoagent", 1 ) ;
            command.insertElementAt( "-Xdebug", 1 ) ;
        }

        String[] arg = new String[command.size()];
        command.copyInto(arg);

        if (debugLevel >= Test.DISPLAY) {
            StringBuffer buff = new StringBuffer() ;
            buff.append( "startProcess: about to exec:" ) ;
            for (int ctr=0; ctr<command.size(); ctr++ ) {
                buff.append( " " ) ;
                buff.append( (String)command.elementAt(ctr) ) ;
            }
            trace( buff.toString() ) ;
            trace( "handShake = \"" + handShake + "\"" ) ;      
        }

        return startProcess(arg,handShake,debugLevel);
    }

    private static void displayCommand( String[] command ) 
    {
        System.out.println( 
            "-----------------------------------------------------------------" ) ;
        System.out.println( "Current working directory: " +
            System.getProperty( "user.dir" ) ) ;
        System.out.println( "Command to exec:" ) ;
        for (String str : command)
            System.out.println( "\t" + str ) ;
        System.out.println( 
            "-----------------------------------------------------------------" ) ;
    }

    private static Process startProcess( String[] command, 
        String handShake, int debugLevel ) throws IOException 
    {
        Process theProcess = null;
        if (debugLevel >= Test.DISPLAY)
            displayCommand( command ) ;

        theProcess = Runtime.getRuntime().exec(command);

        ProcessMonitor monitor ;

        if (debugLevel >= Test.DISPLAY)
            monitor = new ProcessMonitor( theProcess, 
                System.out, System.err, handShake);
        else
            monitor = new ProcessMonitor(theProcess, 
                StreamReader.NULL_OUTPUT_STREAM, StreamReader.NULL_OUTPUT_STREAM, handShake);

        monitor.start();

        try {
            monitor.waitForHandshake(120000);
        } catch (Exception e) {
            theProcess.destroy();
            theProcess = null;
            try {
                monitor.finishWriting();
            } catch (InterruptedException iex) {
                System.err.println( iex );
                iex.printStackTrace( );
            }

            Error err = new Error(e.getMessage());
            err.initCause( e ) ;
            throw err ;
        }

        return theProcess;
    }

    /**
     * Run the rmic compiler.
     * @param generatorArg The generator argument (e.g. "-iiop" or "-idl").
     * May be null.
     * @param additionalArgs Additional arguments. May be null.
     * @param classes Classes to compile. If null or empty, this method
     * does nothing.
     * @param externalProcess If true, rmic will be run in an external process;
     * if false, rmic will be run in the current process.
     * @throws Exception if compile fails.
     */
    public static void rmic (   String generatorArg,
                                String[] additionalArgs,
                                String[] classes,
                                boolean externalProcess) throws Exception {
        rmic(generatorArg,additionalArgs,classes,externalProcess,System.out);
    }

    /**
     * Run the rmic compiler.
     * @param generatorArg The generator argument (e.g. "-iiop" or "-idl").
     * May be null.
     * @param additionalArgs Additional arguments. May be null.
     * @param classes Classes to compile. If null or empty, this method
     * does nothing.
     * @param externalProcess If true, rmic will be run in an external process;
     * if false, rmic will be run in the current process.
     * @param out Where to write output.
     * @throws Exception if compile fails.
     */
    public static void rmic (   String generatorArg,
                                String[] additionalArgs,
                                String[] classes,
                                boolean externalProcess,
                                OutputStream out) throws Exception {

        if (externalProcess) {
            rmicExternal(generatorArg,additionalArgs,classes,out);
        } else {
            rmicInternal(generatorArg,additionalArgs,classes,out);
        }
    }

    /**
     * Runs the rmic compiler in the current process.
     * @param generatorArg The generator argument (e.g. "-iiop" or "-idl").
     * May be null.
     * @param additionalArgs Additional arguments. May be null.
     * @param classes Classes to compile. If null or empty, this method
     * does nothing.
     * @param out Where to write output.
     * @throws Exception if compile fails.
     */
    private static void rmicInternal (String generatorArg,
                                      String[] additionalArgs,
                                      String[] classes,
                                      OutputStream out)
        throws Exception {

        try {
            trace( "Util.rmicInternal called" ) ;
            trace( "\tgeneratorArg = " + generatorArg ) ;
            trace( "\tadditionalArgs = " + Test.display(additionalArgs)) ;
            trace( "\tclasses = " + Test.display(classes)) ;

            if (classes != null && classes.length > 0) {
                int commandCount = classes.length+2;
                if (debug) 
                    commandCount++;
                if (generatorArg != null) 
                    commandCount++;
                if (additionalArgs != null) 
                    commandCount+=additionalArgs.length;
                String[] args = new String[commandCount];
                int index = 0;
                if (generatorArg != null) 
                    args[index++] = generatorArg;
                if (additionalArgs != null) {
                    int count = additionalArgs.length;
                    System.arraycopy(additionalArgs,0,args,index,count);
                    index += count;
                }

                args[index++] = "-classpath";
                args[index++] = System.getProperty("java.class.path");
                if (debug)
                    args[index++] = "-verbose" ;
            
                int count = classes.length;
                System.arraycopy(classes,0,args,index,count);

                // Test.checkSunTools() ;
                Main compiler = new Main(out, "rmic");
                if (!compiler.compile(args)) {
                    throw new Exception ("compile failed");
                }
            }

        } catch (Throwable e) {
            if (e instanceof ThreadDeath) {
                throw (ThreadDeath) e;
            }
            Exception exc = new Exception(e.toString());
            exc.initCause( e ) ;
            throw exc ;
        } finally {
            trace( "Util.rmicInternal exiting" ) ;
        }
    }

    /**
     * Runs the rmic compiler in a separate process.
     * @param generatorArg The generator argument (e.g. "-iiop" or "-idl").
     * May be null.
     * @param additionalArgs Additional arguments. May be null.
     * @throws Exception if compile fails.
     */
    private static void rmicExternal (String generatorArg,
                                      String[] additionalArgs,
                                      String[] classes,
                                      OutputStream out)
        throws Exception {

        trace( "Util.rmicExternal called" ) ;
        trace( "\tgeneratorArg = " + generatorArg ) ;
        trace( "\tadditionalArgs = " + Test.display(additionalArgs)) ;
        trace( "\tclasses = " + Test.display(classes)) ;

        if (classes != null && classes.length > 0) {
            Vector temp = new Vector(10);
            temp.addElement(System.getProperty("java.home") + "/bin/java");    // _REVISIT_ Should be rmic!
            String classPath = System.getProperty( "java.class.path" ) ;
            temp.addElement("-classpath");
            temp.addElement(classPath);
            temp.addElement("sun.rmi.rmic.Main");
            temp.addElement("-classpath");
            temp.addElement(classPath);
            if (debug)
                temp.addElement( "-verbose" ) ;
            if (generatorArg != null) {
                temp.addElement(generatorArg);
            }

            int commandCount = temp.size();
            int additionalCount = additionalArgs != null ? additionalArgs.length : 0;
            int classCount = classes.length;
            String command[] = new String[commandCount + additionalCount + classCount];
            temp.copyInto(command);
            if (additionalCount > 0) {
                System.arraycopy(additionalArgs,0,command,commandCount,additionalCount);
            }
            System.arraycopy(classes,0,command,commandCount + additionalCount,classCount);

            // Do the compile...

            try {
                int result = execAndWaitFor(command,out,System.err);
                if (result != 0) {
                    throw new Exception("compile failed: " + result);
                }
            } catch (Throwable e) {
                if (e instanceof ThreadDeath) {
                    throw (ThreadDeath) e;
                }
                Exception exc = new Exception(e.toString());
                exc.initCause( e ) ;
                throw exc ;
            } finally {
                trace( "Util.rmicExternal exiting" ) ;
            }
        }
    }

    public static Process startServer ( String serverClass,
                                        String serverName,
                                        String nameServerHost,
                                        int nameServerPort,
                                        boolean iiop) throws IOException {
        // Fill out the command...

        Vector temp = new Vector(15);

        temp.addElement(System.getProperty("java.home") + "/bin/java" );
        temp.addElement("-Dorg.omg.CORBA.ORBClass=com.sun.corba.ee.impl.orb.ORBImpl");
        temp.addElement("-Dorg.omg.CORBA.ORBSingletonClass=com.sun.corba.ee.impl.orb.ORBSingleton");

        // add the Util delegates property; this is required so that
        // appropriate delegates are picked up when running with the
        // serialization code that has 5 bug fixes to handle
        // interoperability and the correct RepID generation

        temp.addElement("-Djavax.rmi.CORBA.UtilClass=com.sun.corba.ee.impl.javax.rmi.CORBA.Util");
        temp.addElement("-Djavax.rmi.CORBA.StubClass=com.sun.corba.ee.impl.javax.rmi.CORBA.StubDelegateImpl");
        temp.addElement("-Djavax.rmi.CORBA.PortableRemoteObjectClass=com.sun.corba.ee.impl.javax.rmi.PortableRemoteObject");
        String codebase = System.getProperty("java.rmi.server.codebase");
        if (codebase != null) {
            temp.addElement("-Djava.rmi.server.codebase=" + codebase);
        }

        boolean is12VM = true;
        if (is12VM) {
            String policy = System.getProperty("java.security.policy");
            if (policy != null) {
                temp.addElement("-Djava.security.policy=" + policy);
            }
        }

        temp.addElement("-classpath");
        temp.addElement(System.getProperty("java.class.path"));
        temp.addElement("test.Util");
        temp.addElement(serverClass);
        temp.addElement(serverName);
        temp.addElement(nameServerHost != null ? nameServerHost : "-nullHost");
        temp.addElement(Integer.toString(nameServerPort));

        if (iiop) {
            temp.addElement("-iiop");
        } else {
            temp.addElement("-jrmp");
        }

        // Start her up...

        return startProcess(temp,Util.HANDSHAKE,Test.forkDebugLevel);
    }

    public static Process startServer (String serverClass) throws IOException {

        // Fill out the command...
        Vector cmd = new Vector();

        cmd.add(System.getProperty("java.home") + "/bin/java");

        String policy = System.getProperty("java.security.policy");
        if (policy != null)
            cmd.addElement("-Djava.security.policy=" + policy);

        cmd.add("-Dorg.omg.CORBA.ORBClass=com.sun.corba.ee.impl.orb.ORBImpl");
        cmd.add("-Dorg.omg.CORBA.ORBSingletonClass=com.sun.corba.ee.impl.orb.ORBSingleton");
        cmd.add("-Djavax.rmi.CORBA.UtilClass=com.sun.corba.ee.impl.javax.rmi.CORBA.Util");
        cmd.add("-Djavax.rmi.CORBA.StubClass=com.sun.corba.ee.impl.javax.rmi.CORBA.StubDelegateImpl");
        cmd.add("-Djavax.rmi.CORBA.PortableRemoteObjectClass=com.sun.corba.ee.impl.javax.rmi.PortableRemoteObject");

        cmd.add("-classpath");
        cmd.add(System.getProperty("java.class.path"));
        cmd.add(serverClass);

        // Start her up...
        return startProcess(cmd,Util.HANDSHAKE,Test.forkDebugLevel);
    }

    public static Process startServer (String serverClass, Vector properties) throws IOException {

        // Fill out the command...

        Vector cmd = new Vector() ;
        cmd.add( System.getProperty("java.home") + "/bin/java" );

        cmd.add(  "-Dorg.omg.CORBA.ORBClass=com.sun.corba.ee.impl.orb.ORBImpl" );
        cmd.add(  "-Dorg.omg.CORBA.ORBSingletonClass=com.sun.corba.ee.impl.orb.ORBSingleton" );
        cmd.add(  "-Djavax.rmi.CORBA.UtilClass=com.sun.corba.ee.impl.javax.rmi.CORBA.Util" );
        cmd.add(  "-Djavax.rmi.CORBA.StubClass=com.sun.corba.ee.impl.javax.rmi.CORBA.StubDelegateImpl" );
        cmd.add(  "-Djavax.rmi.CORBA.PortableRemoteObjectClass=com.sun.corba.ee.impl.javax.rmi.PortableRemoteObject" );

        // Add properties
        int index;
        for (index = 0; index < properties.size(); index++) 
            cmd.add(  (String)properties.elementAt(index) );

        // Add classpath and server class
        cmd.add(  "-classpath" );
        cmd.add(  System.getProperty("java.class.path") );
        cmd.add(  serverClass );

        // Start her up...

        return startProcess(cmd,Util.HANDSHAKE,Test.forkDebugLevel);
    }

    // This version of startServer allows added paths to be appended to the classpath
    public static Process startServer (String serverClass, Vector properties, 
                                       String classpath) 
        throws IOException {

        // Fill out the command... 

        Vector cmd = new Vector() ;
        cmd.add(  System.getProperty("java.home") + "/bin/java" );
        cmd.add(  "-Dorg.omg.CORBA.ORBClass=com.sun.corba.ee.impl.orb.ORBImpl" );
        cmd.add(  "-Dorg.omg.CORBA.ORBSingletonClass=com.sun.corba.ee.impl.orb.ORBSingleton" );
        cmd.add(  "-Djavax.rmi.CORBA.UtilClass=com.sun.corba.ee.impl.javax.rmi.CORBA.Util" );
        cmd.add(  "-Djavax.rmi.CORBA.StubClass=com.sun.corba.ee.impl.javax.rmi.CORBA.StubDelegateImpl" );
        cmd.add(  "-Djavax.rmi.CORBA.PortableRemoteObjectClass=com.sun.corba.ee.impl.javax.rmi.PortableRemoteObject" );

        // Add properties
        int index;
        for (index = 0; index < properties.size(); index++) 
            cmd.add(  (String)properties.elementAt(index) );

        // Add classpath and server class
        cmd.add(  "-classpath" );
        cmd.add(  System.getProperty("java.class.path")+File.pathSeparator+classpath );
        cmd.add(  serverClass );
                
        // Start her up...
        return startProcess(cmd,Util.HANDSHAKE,Test.forkDebugLevel);
    }

    public static Process startNameServer(int port, boolean iiop) throws IOException {
        return startNameServer(Integer.toString(port),iiop);
    }

    public static Process startNameServer(String port, boolean iiop) throws IOException {

        try {
            Thread.sleep( 3*1000 ) ; // sleep for 3 seconds
        } catch (InterruptedException exc) {
            // ignore this: very rare case
        }

        String handshake = null;
        Vector args = new Vector();

        // Fill out the command...

        args.addElement(System.getProperty("java.home") + "/bin/java" );
        args.addElement("-Dorg.omg.CORBA.ORBClass=com.sun.corba.ee.impl.orb.ORBImpl");
        args.addElement("-Dorg.omg.CORBA.ORBSingletonClass=com.sun.corba.ee.impl.orb.ORBSingleton");
        args.addElement("-Djavax.rmi.CORBA.UtilClass=com.sun.corba.ee.impl.javax.rmi.CORBA.Util");
        args.addElement("-Djavax.rmi.CORBA.StubClass=com.sun.corba.ee.impl.javax.rmi.CORBA.StubDelegateImpl");
        args.addElement("-Djavax.rmi.CORBA.PortableRemoteObjectClass=com.sun.corba.ee.impl.javax.rmi.PortableRemoteObject");
        args.addElement("-classpath");
        args.addElement(System.getProperty("java.class.path"));

        boolean is12VM = true;
        if (is12VM) {
            String policy = System.getProperty("java.security.policy");
            args.addElement("-Djava.security.policy=" + policy);
        }

        if (iiop) {

            args.addElement("com.sun.corba.ee.impl.naming.cosnaming.TransientNameServer");
            args.addElement("-ORBInitialPort");
            args.addElement(port);
            handshake = "Initial Naming Context:";

        } else {

            args.addElement("test.StartRMIRegistry");
            args.addElement(port);
            handshake = HANDSHAKE;
        }

        // Start her up...
        Test.dprint( "name server being started with command " + args ) ;
        Test.dprint( "Expecting handshake: " + handshake ) ;

        return startProcess(args,handshake,Test.forkDebugLevel);
    }

    /**
     * execAndWaitFor will create a new Process and wait for the
     * process to complete before returning
     * @param command command line arguments to pass to exec.
     * @return int the result of Process.exitValue() or -1;
     * @throws Error if an unexpected exception occurs an Error is
     * thrown with the message string from the original exception.
     */
    public static int execAndWaitFor(String[] command) {
        return execAndWaitFor(command,System.out,System.err);
    }

    /**
     * execAndWaitFor will create a new Process and wait for the
     * process to complete before returning
     * @param command command line arguments to pass to exec.
     * @param out Where to copy output.
     * @param err Where to copy error output.
     * @return int the result of Process.exitValue() or -1;
     * @throws Error if an unexpected exception occurs an Error is
     * thrown with the message string from the original exception.
     */
    public static int execAndWaitFor(String[] command,
                                     OutputStream out,
                                     OutputStream err) {

        try {
            if (Test.forkDebugLevel >= Test.DISPLAY)
                displayCommand( command ) ;

            Process theProcess  = Runtime.getRuntime().exec(command);

            ProcessMonitor monitor = new ProcessMonitor(theProcess,out,err);
            monitor.start();
            int result = waitForCompletion(theProcess,2000);
            monitor.finishWriting();
            return result;

        } catch (Throwable error) {
            Error exc = new Error(error.getMessage());
            exc.initCause( error ) ;
            throw exc ;
        }
    }


    public static int waitForCompletion( Process theProcess ,int sleepTime)
        throws java.lang.InterruptedException
    {
        int result = -1;

        try {
            theProcess.waitFor();
            result = theProcess.exitValue();
        }
        catch (java.lang.IllegalThreadStateException notDone) {
            // We assume that waitFor() does not work and exitValue()
            // failed because the Process is not done. Lets Sleep
            // for a while then check for completion again.
            Thread.sleep(sleepTime,0);
            waitForCompletion(theProcess,sleepTime+1500);
        }

        return result;
    }
}

