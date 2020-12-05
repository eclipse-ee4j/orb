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

package javax.rmi;

import com.sun.corba.ee.impl.util.Utility;
import com.sun.corba.ee.spi.JndiConstants;
import org.omg.CORBA.ORB;
import sun.rmi.registry.RegistryImpl;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.CORBA.Tie;
import javax.rmi.CORBA.Util;
import java.io.IOException;
import java.rmi.RMISecurityManager;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

/**
 * PortableContext is a convenience class for rmi-iiop programs which simplifies
 * both server and client initialization. Server-side features include:
 * <ul>
 * <li> A {@link #main(java.lang.String[]) main} which provides:
 *    <ul>
 *    <li> Command-line initialization of servant(s).
 *    <li> Automatic selection of runtime (RMI or IIOP).
 *    <li> Automatic invocation of the appropriate name server (local host
 *         only).
 *    <li> Automatic publication of servants to the name server.
 *    </ul>
 * <li> {@link #startServant(java.lang.String, boolean) startServant methods} (used by main) are
 * available for direct use from within Applications.
 * <li> Context information for all {@link PortableContext.Servant servant} instances.
 * </ul>
 * <a name="_mainexample_"></a>
 * For example, to startup and publish 'HelloServant' under the name "myHello" with
 * the name server running on the local host and port:
 * <pre>
 *    java javax.rmi.PortableContext myHello=HelloServant
 * </pre>
 * To launch the name server if it is not running, add the -startNameServer option:
 * <pre>
 *    java javax.rmi.PortableContext myHello=HelloServant -startNameServer
 * </pre>
 * Client-side features include:
 * <ul>
 * <li> {@link #lookup(java.lang.String,java.lang.Class) lookup methods} to retrieve published objects.
 * <li> Context information for all {@link PortableContext.Client client} instances.
 * </ul>
 * <a name="_clientexample_"></a>
 * Client code can get access to published servants using the {@link #lookup(java.lang.String,java.lang.Class) lookup} or
 * {@link #lookupClient(java.lang.String,java.lang.Class) lookupClient} methods. For example, to lookup a servant which
 * implements the 'Hello' interface, is running on IIOP, and has been published as 'myHello' with
 * default host and port:
 * <pre>
 *    Hello ref = (Hello) PortableContext.lookup("iiop://myHello",Hello.class);
 * </pre>
 * <p><table border=0>
 * <tr><td valign="top"><i><b>  Note </b></i></td><td>
 * This class does not (yet) support Applets. There are two problems:
 * <ol>
 * <li> Resource Consumption.  Currently, there is a static cache of PortableContext
 * instances (to minimize resource use), which works well for applications. For applets,
 * the same process is re-used for all applets, and therefore the cache does not get cleaned
 * up.
 * <li> Security.  Applets could get a context from a previous applet, and therefore
 * have access to the ORB/InitialContext, etc.
 * </ol>
 * One solution (that doesn't throw out the entire design) is to require that the
 * cache (a Hashtable) be passed in by the applet/application, rather than be a static.
 * It's not as convenient, but does solve both problems.
 * </td></tr>
 * </table>
 * @version     1.0, 6/23/98
 * @author      Bryan Atsatt
 */
public class PortableContext {

    public static final int RMI_RUNTIME = 0;
    public static final int IIOP_RUNTIME = 1;


    private ORB orb = null;
    private InitialContext nameContext = null;
    private String nameServerHost = null;
    private int nameServerPort = 0;
    private int runtime;
    private String key = null;
    private boolean startNameServer = false;
    private static boolean is12VM = true;

    private static Hashtable cache = new Hashtable();
    private static final String HANDSHAKE = "Ready.";

    //____________________________________________________________________________________________
    // API Methods
    //____________________________________________________________________________________________

    /**
     * Lookup a remote object. The clientURL structure is:
     * <pre>
     *   rmi|iiop://[host][:port]/publishedName
     * </pre>
     * So, for example, here are various ways to specify 'bob':
     * <pre>
     *   rmi://bob             (RMIRegistry, this host, default port)
     *   rmi://myHost/bob      (RMIRegistry, myHost, default port)
     *   iiop://myHost:197/bob (TransientNameServer, myHost, port 197)
     *   iiop://:1099/bob      (TransientNameServer, this host, port 1099)
     * </pre>
     * Assuming a servant implements the 'Hello' interface, is running on IIOP, and has
     * been published as 'bob' with default host and port:
     * <pre>
     *    Hello ref = (Hello) PortableContext.lookup("iiop://bob",Hello.class)
     * </pre>
     * @param clientURL A url naming the client.
     * @param narrowTo The Class of the interface to which to narrow the remote object.
     * @exception NamingException Thrown if lookup fails.
     * @see #lookupClient(java.lang.String, java.lang.Class)
     */
    public static Object lookup (String clientURL, Class narrowTo) throws NamingException {
        PortableContext.Client client = getClient(clientURL);
        return PortableRemoteObject.narrow(client.client,narrowTo);
    }

    /**
     * Lookup a remote object. Same as {@link #lookup(java.lang.String,java.lang.Class) lookup} except that it
     * returns a Client instance.
     * @param clientURL A url naming the client.
     * @param narrowTo The Class of the interface to which to narrow the remote object.
     * @exception NamingException Thrown if lookup fails.
     * @see #lookup(java.lang.String, java.lang.Class)
     */
    public static Client lookupClient (String clientURL, Class narrowTo) throws NamingException {
        PortableContext.Client client = getClient(clientURL);
        PortableRemoteObject.narrow(client.client,narrowTo);
        return client;
    }

    /**
     * Start up one or more servants. Supported arguments:
     * <pre>
     *     [options] servantSpec [servantSpec...]
     * </pre>
     * <a name="servantSpec"></a>
     * The servantSpec can be either of two forms:
     * <pre>
     *   Simple form:   [publishname=]class
     *   URL form:      servant://[host][:port]/class[?name=publishname]
     * </pre>
     * So, for example, here are various servantSpecs for the acme.Dynamite servant:
     * <pre>
     *   acme.Dynamite
     *   bob=acme.Dynamite
     *
     *   servant://acme.Dynamite
     *   servant://acme.Dynamite?name=bob
     *   servant://myHost/acme.Dynamite?name=bob
     *   servant://myHost:45/acme.Dynamite?name=bob
     *   servant://myHost/acme.Dynamite
     *   servant://myHost:45/acme.Dynamite
     *   servant://:45/acme.Dynamite
     * </pre>
     * If 'publishname' is not specified, the servant is not published to the name
     * server.
     * <p><table border=0>
     * <tr><td valign="top"><i><b>  Note </b></i></td><td>
     * The JNDI Context.rebind() method is used to publish servants. If bind semantics
     * are required, this main cannot be used. Instead, write a main which uses
     * {@link #startServant(java.lang.String, boolean) startServant} directly, and pass servantSpecs which
     * do <em>not</em> specify a name, and do the bind in your code:
     * <pre>
     *    Servant it = PortableContext.startServant(spec,false);
     *    Context nameContext = it.getContext().getNameContext();
     *    nameContext.bind(name,it.servant);
     * </pre>
     * </td></tr>
     * </table>
     * The supported options are:
     * <pre>
     *   -host nameServerHost    The host name to use if not specified in servantSpec.
     *   -port nameServerPort    The port to use if not specified in servantSpec.
     *   -startNameServer        Start the name server if needed. Not allowed with -host option.
     *   -verbose                Print message describing each started servant.
     * </pre>
     * @param args See description above.
     * @see #startServant
     */
    public static void main (String[] args) {

        String hostOption = null;
        String portOption = null;
        boolean startNameServer = false;
        boolean verbose = false;
        int argLen = args.length;

        // Scan args for options...

        int found = 0;
        for (int i = 0; i < argLen; i++) {
            if (args[i].equalsIgnoreCase("-host")) {
                args[i] = null;
                hostOption = args[++i];
                args[i] = null;
                found++;
            } else if (args[i].equalsIgnoreCase("-port")) {
                args[i] = null;
                portOption = args[++i];
                args[i] = null;
                found++;
            } else if (args[i].equalsIgnoreCase("-startNameServer")) {
                args[i] = null;
                startNameServer = true;
                found++;
            } else if (args[i].equalsIgnoreCase("-verbose")) {
                args[i] = null;
                verbose = true;
                found++;
            } else if (args[i].startsWith("-")) {
                usage();
            }
        }

        // Check incompatible arguments...

        if (hostOption != null && startNameServer) {
            System.out.println("\nCannot start a name server on another host. If '" + hostOption + "'");
            System.out.println("is the local host, just remove the -host argument.");
            System.exit(1);
        }

        boolean haveJRMP = false;   // _REVISIT_ REMOVE

        try {
            if (argLen > found) {

                // Now process servants...

                for (int i = 0; i < argLen; i++) {
                    if (args[i] != null) {

                        Servant servant = startServant(args[i],hostOption,portOption,startNameServer);
                        if (servant.getContext().getRuntime() == RMI_RUNTIME) haveJRMP = true;   // _REVISIT_ REMOVE

                        if (verbose) {
                            String env = servant.getContext().getRuntime() == IIOP_RUNTIME ? "IIOP" : "RMI";
                            String published = servant.servantName != null ? "and published as " + servant.servantName : "";
                            System.out.println(servant.servantClass + " started on " + env + " runtime " + published + ".");
                        }
                    }
                }

                System.out.println(HANDSHAKE);

                // If we don't have any JRMP servants, we gotta do a wait here
                // _REVISIT_ REMOVE

                if (!haveJRMP) {

                    // Wait forever...

                    Object sync = new Object();
                    synchronized (sync) { sync.wait(); }
                }
            } else {
                usage();
            }
        } catch (Throwable e) {
            System.out.println("Caught " + e);
            System.exit(1);
        }
    }

    /**
     * Startup a servant, using a name server running on the local host with
     * default port.
     * @param servantSpec The <a href="#servantSpec">servant specification</a>.
     * @param startNameServer True if the name server should be started if needed.
     * @exception ClassNotFoundException If servant class cannot be found.
     * @exception InstantiationException If servant class cannot be instantiated.
     * @exception IllegalAccessException If servant class initializer not accessible.
     * @exception NamingException If an error occurs when publishing the servant.
     * @see #startServant(java.lang.String, java.lang.String, java.lang.String, boolean)
     */
    public static Servant startServant(String servantSpec,
                                       boolean startNameServer)
        throws  ClassNotFoundException,
                InstantiationException,
                IllegalAccessException,
                RemoteException,
                NamingException {
        return startServant(servantSpec,null,null,startNameServer);
    }

    /**
     * Startup a servant.
     * @param servantSpec The <a href="#servantSpec">servant specification</a>.
     * @param defaultHost The name server host to use if not specified in servantSpec. May be
     * null (local host).
     * @param defaultPort The name server port to use if not specified in servantSpec. May be
     * null (use default for runtime).
     * @param startNameServer True if the name server should be started if needed.
     * @exception ClassNotFoundException If servant class cannot be found.
     * @exception InstantiationException If servant class cannot be instantiated.
     * @exception IllegalAccessException If servant class initializer not accessible.
     * @exception NamingException If an error occurs when publishing the servant.
     * @see #startServant(java.lang.String, boolean)
     */
    public static Servant startServant( String servantSpec,
                                        String defaultHost,
                                        String defaultPort,
                                        boolean startNameServer)
        throws  ClassNotFoundException,
                InstantiationException,
                IllegalAccessException,
                RemoteException,
                NamingException {


        int runtime = -1;
        String host = defaultHost;
        String port = defaultPort;
        String servantClass = null;
        String servantName = null;
        Remote servantInstance = null;

        // Parse the servant spec. Is it a url style spec?

        if (servantSpec.startsWith("servant://")) {

            // Yep.

            int end = servantSpec.length();
            int nameOffset = servantSpec.indexOf("?name=");
            if (nameOffset >= 0) {
                servantName = servantSpec.substring(nameOffset+6);
                end = nameOffset;
            }

            int slashOffset = servantSpec.indexOf('/',10);
            if (slashOffset >= 0) {
                servantClass = servantSpec.substring(slashOffset+1,end);
                end = slashOffset;

                int portOffset = servantSpec.indexOf(':',10);
                if (portOffset >= 0) {
                    host = servantSpec.substring(10,portOffset);
                    port = servantSpec.substring(portOffset+1,end);
                } else {
                    host = servantSpec.substring(10,slashOffset);
                }
            } else {
                servantClass = servantSpec.substring(10,end);
            }
        } else {

            // No, it's a simple spec...

            int offset = servantSpec.indexOf('=');
            if (offset < 0) {
                servantClass = servantSpec;
            } else {
                servantName = servantSpec.substring(0,offset);
                servantClass = servantSpec.substring(offset+1);
            }
        }

        // Do some cleanup...

        if (host != null && host.length() == 0) host = null;
        if (port != null && port.length() == 0) port = null;

        // Instantiate the servant...

        Class theClass = Class.forName(servantClass);
        servantInstance = (Remote) theClass.newInstance();

        // Determine runtime...

        if (findTie(servantInstance) == null) {
            runtime = RMI_RUNTIME;
        } else {
            runtime = IIOP_RUNTIME;
        }

        // Make sure we have a valid port number for the name service...

        if (port == null) {

            // Use default port...

            port = runtime == IIOP_RUNTIME ? "900" : Integer.toString(java.rmi.registry.Registry.REGISTRY_PORT);
        }

        // Get a context...

        PortableContext context = getContext(runtime,host,port,startNameServer);

        // Create and start the servant...

        Servant servant = context.startServant(servantClass,servantName,servantInstance);

        return servant;
    }


    /**
     * Return the runtime in use by this context.
     */
    public int getRuntime() {return runtime;}

    /**
     * Return the ORB in use by this context. Will be null if runtime == RMI_RUNTIME.
     */
    public ORB getORB () {return orb;}

    /**
     * Return the name context in use by this context.
     */
    public Context getNameContext() {return nameContext;}

    /**
     * Return the name server host in use by this context. Will be null if local host.
     */
    public String getNameServerHost() {return nameServerHost;}

    /**
     * Return the name server port in use by this context.
     */
    public int getNameServerPort() {return nameServerPort;}

    //____________________________________________________________________________________________
    // Implementation Methods
    //____________________________________________________________________________________________

    // REMIND: Need support for Applet.

    protected PortableContext (int runtime, boolean startNameServer) throws NamingException {
        this.runtime = runtime;
        this.startNameServer = startNameServer;
        init();
    }

    protected PortableContext (String nameServerHost,
                               String nameServerPort,
                               int runtime,
                               boolean startNameServer) throws NamingException {

        this.runtime = runtime;
        this.startNameServer = startNameServer;

        if (nameServerHost != null && nameServerHost.length() > 0) {
            this.nameServerHost = nameServerHost;
        }

        if (nameServerPort != null && nameServerPort.length() > 0) {
            this.nameServerPort = Integer.parseInt(nameServerPort);
        }

        init();
    }


    protected static Client getClient( String clientSpec) throws NamingException {

        // clientSpec:  rmi|iiop://[host][:port]/publishedName;
        //
        // "rmi://bob"
        // "rmi://myHost/bob"
        // "rmi://myHost:45/bob"
        // "rmi://:45/bob"
        //
        // "iiop://bob"
        // "iiop://myHost/bob"
        // "iiop://myHost:45/bob"
        // "iiop://:45/bob"

        int runtime = -1;
        String host = null;
        String port = null;
        String clientName = null;
        Remote clientInstance = null;

        // Parse the client spec. Is it an rmi or iiop spec?

        int startOffset = 0;

        if (clientSpec.startsWith("rmi://")) {
            runtime = RMI_RUNTIME;
            startOffset = 6;
        } else if (clientSpec.startsWith("iiop://")) {
            runtime = IIOP_RUNTIME;
            startOffset = 7;
        } else {
            throw new Error("Invalid url: " + clientSpec);
        }

        // Yep.

        int end = clientSpec.length();
        int slashOffset = clientSpec.indexOf('/',startOffset);
        if (slashOffset >= 0) {
            clientName = clientSpec.substring(slashOffset+1,end);
            end = slashOffset;

            int portOffset = clientSpec.indexOf(':',startOffset);
            if (portOffset >= 0) {
                host = clientSpec.substring(startOffset,portOffset);
                port = clientSpec.substring(portOffset+1,end);
            } else {
                host = clientSpec.substring(startOffset,slashOffset);
            }
        } else {
            clientName = clientSpec.substring(startOffset,end);
        }

        if (clientName == null) {
            throw new Error("Invalid url: " + clientSpec + ". No name specified.");
        }

        // Do some cleanup...

        if (host != null && host.length() == 0) host = null;
        if (port != null && port.length() == 0) port = null;

        // Make sure we have a valid port number for the name service...

        if (port == null) {

            // Use default port...

            port = runtime == IIOP_RUNTIME ? "900" : Integer.toString(java.rmi.registry.Registry.REGISTRY_PORT);
        }

        // Get a context...

        PortableContext context = getContext(runtime,host,port,false);

        // Lookup the client instance...

        Object temp = context.getNameContext().lookup(clientName);
        clientInstance = (Remote) PortableRemoteObject.narrow(temp,Remote.class);

        // Create the client...

        Client client = context.getClient(clientName,clientInstance);

        return client;
    }

    /** Find an RMI-IIOP Tie given a server object */
    private static Tie findTie(java.rmi.Remote obj) {
        Tie result = Util.getTie(obj);
        if (result == null) {
            result = Utility.loadTie(obj);
        }
        return result;
    }

    // _REVISIT_ Security problem with multiple Applets?? Made private for now.

    private static PortableContext getContext(  int runtime,
                                                String host,
                                                String port,
                                                boolean startNameServer) throws NamingException {
        String key = createContextKey(runtime,host,port);
        PortableContext result = (PortableContext) cache.get(key);

        if (result == null) {
            result = new PortableContext(host,port,runtime,startNameServer);
            cache.put(key,result);
        }

        return result;

    }

    public static Process startNameServer(String port, int runtime) throws IOException {

        String handshake = null;
        Vector args = new Vector();

        // Fill out the command...

        args.addElement("java");
        args.addElement("-classpath");
        args.addElement(System.getProperty("java.class.path"));

        if (is12VM) {
            String policy = System.getProperty("java.security.policy");
            if (policy != null) {
                args.addElement("-Djava.security.policy=" + policy);
            }
        }

        if (runtime == IIOP_RUNTIME) {

            args.addElement("com.sun.corba.ee.impl.naming.cosnaming.TransientNameServer");
            args.addElement("-ORBInitialPort");
            args.addElement(port);
            handshake = "Initial Naming Context:";

        } else if (runtime == RMI_RUNTIME) {


            args.addElement("javax.rmi.RMIRegistry");
            args.addElement(port);
            handshake = HANDSHAKE;

        } else {
            throw new Error("Unknown runtime: " + runtime);
        }

        // Start her up...

        return test.Util.startProcess(args,handshake);
    }

    private Client getClient(String clientName, Remote client) {
        return new Client(clientName,client);
    }

    private Servant startServant(   String servantClass,
                                    String servantName,
                                    Remote servant)
        throws  ClassNotFoundException,
                InstantiationException,
                IllegalAccessException,
                RemoteException,
                NamingException {
        Servant result = new Servant(servantClass,servantName,servant);

        // Export it if we need to...

        if (!(servant instanceof PortableRemoteObject) &&
            !(servant instanceof UnicastRemoteObject)) {

            PortableRemoteObject.exportObject(servant);
        }

        // Publish it if we need to...

        if (servantName != null) {
            nameContext.rebind(servantName,servant);
        }

        return result;
    }

    protected void init () throws NamingException {

        Hashtable nameEnv = new Hashtable();

        // First, make sure we have a valid port number for
        // the name service...

        if (nameServerPort == 0) {

            // Use default port...

            nameServerPort = runtime == IIOP_RUNTIME ? 900 : java.rmi.registry.Registry.REGISTRY_PORT;
        }

        // Are we doing IIOP?

        String[] nameServerArgs = null;

        if (runtime == IIOP_RUNTIME) {

            // Yes, so create the ORB. First, setup the
            // name server arguments...

            if (nameServerHost != null) {
                nameServerArgs = new String[4];
                nameServerArgs[2] = "-ORBInitialHost";
                nameServerArgs[3] = nameServerHost;

            } else {

                nameServerArgs = new String[2];
            }

            nameServerArgs[0] = "-ORBInitialPort";
            nameServerArgs[1] = Integer.toString(nameServerPort);

            // Now create the orb...

            orb = initORB(nameServerArgs);

            // Now setup the name server environment...

            nameEnv.put("java.naming.corba.orb", orb);
            nameEnv.put("java.naming.factory.initial", JndiConstants.COSNAMING_CONTEXT_FACTORY);

        } else if (runtime == RMI_RUNTIME) {

            // JRMP, so just setup the name server environment...

            String serverUrl = "rmi://";

            if (nameServerHost != null) {
                serverUrl += nameServerHost;
            }

            serverUrl += ":" + Integer.toString(nameServerPort);

            nameEnv.put("java.naming.factory.initial",JndiConstants.REGISTRY_CONTEXT_FACTORY);
            nameEnv.put("java.naming.provider.url",serverUrl);

        } else {
            throw new Error("Unknown runtime: " + runtime);
        }

        // Now get our name context.

        boolean gotContext = false;
        boolean retry = false;

        try {
            nameContext = new InitialContext(nameEnv);
            gotContext = true;

            if (runtime == RMI_RUNTIME) {

                // Under IIOP, we would have gotten an exception at this point if
                // the name server was not running. We need to check with JRMP...

                try {
                    nameContext.lookup("atsatt");
                } catch (javax.naming.NameNotFoundException e) {
                }
            }
        } catch (Throwable e) {

            if (e instanceof ThreadDeath) throw (ThreadDeath)e;

            String server = runtime == IIOP_RUNTIME ? "TransientNameServer" : "RMIRegistry";

            if (startNameServer && nameServerHost == null) {

                System.out.println("Starting " + server + "...");

                try {
                    startNameServer(Integer.toString(nameServerPort),runtime);
                } catch (Exception e1) {
                    throw new NamingException("Could not start name server: " + e1.toString());
                }

                if (orb != null) {

                    // We have to recreate the orb at this point, because it's
                    // connection to the name service is bad...

                    orb.shutdown(false);
                    orb = initORB(nameServerArgs);
                    nameEnv.put("java.naming.corba.orb", orb);
                }

                nameContext = new InitialContext(nameEnv);

            } else {
                throw new NamingException("Could not connect to " + server + ".");
            }
        }

        // Finally, create our key...

        key = createContextKey(runtime,nameServerHost,Integer.toString(nameServerPort));
    }

    private ORB initORB (String[] nameServerArgs) {

        // Setup the ORB properties...

        Properties props = null;

        try {
            props = System.getProperties();
        } catch (SecurityException e) {
            props = new Properties();
        }

        if (props.getProperty("org.omg.CORBA.ORBClass") == null) {
            props.put("org.omg.CORBA.ORBClass","com.sun.corba.ee.impl.orb.ORBImpl");
        }

        if (props.getProperty("org.omg.CORBA.ORBSingletonClass") == null) {
            props.put("org.omg.CORBA.ORBSingletonClass","com.sun.corba.ee.impl.orb.ORBSingleton");
        }

        // Create and return the ORB...

        return ORB.init(nameServerArgs, props);
    }

    protected static String createContextKey(int runtime, String host, String port) {
        return runtime + ":" + host + ":" + port;
    }


    private static void usage () {
        System.out.println("\nUsage: PortableContext [options] servantSpec [servantSpec...]\n");

        System.out.println("Where servantSpec can be either of two forms:\n");
        System.out.println("   Simple form:   [publishname=]class");
        System.out.println("   URL form:      servant://[host][:port]/class[?name=publishname]");

        System.out.println("\nIf 'publishname' is omitted, the servant is not published in the name server.");

        System.out.println("\nThe supported options are:\n");

        System.out.println("   -host nameServerHost    The host name to use if not specified in servantSpec.");
        System.out.println("   -port nameServerPort    The port to use if not specified in servantSpec.");
        System.out.println("   -startNameServer        Start the name server if needed. Not allowed with -host option.");
        System.out.println("   -verbose                Print message describing each started servant.");
        System.exit(1);
    }

    /**
     * Servant is a simple wrapper for rmi-iiop implementation classes which
     * provides additional context information.
     */
    public class Servant {
        /**
         * The servant class.
         */
        public String servantClass = null;

        /**
         * The name servant was published as, or null.
         */
        public String servantName = null;

        /**
         * The servant instance.
         */
        public Remote servant = null;

        /**
         * Return the context.
         */
        public PortableContext getContext() {
            return PortableContext.this;
        }

        Servant(String servantClass,
                String servantName,
                Remote servant) {
            this.servantClass = servantClass;
            this.servantName = servantName;
            this.servant = servant;
        }

        private Servant(){}
    }

    /**
     * Client is a simple wrapper for rmi-iiop stub classes which
     * provides additional context information.
     */
    public class Client {
        /**
         * The name under which this instance was found.
         */
        public String clientName = null;

        /**
         * The client instance.
         */
        public Remote client = null;

        /**
         * Return the context.
         */
        public PortableContext getContext() {
            return PortableContext.this;
        }

        Client(String clientName, Remote client) {
            this.clientName = clientName;
            this.client = client;
        }
        private Client(){}
    }
}

class RMIRegistry {

    /**
     * Main program to start a registry. <br>
     * The port number can be specified on the command line.
     */
    public static void main(String args[]) {
        // Create and install the security manager
        System.setSecurityManager(new RMISecurityManager());

        try {
            int port = Registry.REGISTRY_PORT;
            if (args.length >= 1) {
                port = Integer.parseInt(args[0]);
            }
            Registry registry = new RegistryImpl(port);
            System.out.println("Ready.");

            // prevent registry from exiting
            while (true) {
                try {
                    // The following timeout is used because a bug in the
                    // native C code for Thread.sleep() cause it to return
                    // immediately for any higher value.
                    Thread.sleep(Integer.MAX_VALUE - 1);
                } catch (InterruptedException e) {
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Port is not a number.");
        } catch (Exception e) {
            System.out.println("RegistryImpl.main: an exception occurred: " +
                               e.getMessage());
            e.printStackTrace();
        }
        System.exit(1);
    }
}
