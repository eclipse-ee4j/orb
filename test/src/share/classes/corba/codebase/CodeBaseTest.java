/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
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

package corba.codebase;

import test.Test;
import corba.framework.*;
import java.util.*;
import java.io.*;
import com.sun.corba.ee.spi.orb.ORB;

public class CodeBaseTest extends CORBATest
{
    public static final String VALUE_DIR = "values";
    public static final String STUBTIE_DIR = "stubtie";
    public static final String[] VALUES 
        = new String[] { "TestValue.java" };

        protected void doTest() throws Throwable {
        
        if (test.Test.useJavaSerialization()) {
            return;
        }

        // Generate stubs and ties in the STUBTIE_DIR off of
        // the main output directory.
        String stubTieDir = (new File(Options.getOutputDirectory() 
                                      + STUBTIE_DIR
                                      + File.separator)).getAbsolutePath();
        String valueDir = (new File(Options.getOutputDirectory() 
                                    + VALUE_DIR
                                    + File.separator)).getAbsolutePath();

        String oldOutputDir = Options.getOutputDirectory();

        Options.setRMICClasses(new String[] { "corba.codebase.Server" });
        Options.addRMICArgs("-nolocalstubs -iiop -keep -g");
        Options.setOutputDirectory(stubTieDir);
        compileRMICFiles();

        // Also generate a Serializable in a different directory
        // to test value code downloading

        Options.setJavaFiles(VALUES);
        Options.setOutputDirectory(valueDir);
        compileJavaFiles();

        Options.setOutputDirectory(oldOutputDir);

        String oldClasspath = Options.getClasspath();
        String cpWithAllClasses = 
            stubTieDir
            + File.pathSeparator
            + valueDir
            + File.pathSeparator
            + Options.getClasspath();

        Controller orbd = createORBD();
        orbd.start();

        int webServerPort = Options.getUnusedPort().getValue();

        Controller webServer = createWebServer(oldOutputDir,
                                               webServerPort);
        webServer.start();
        Options.setClasspath(oldClasspath);

        // Add the special RMI property for code downloading.
        // NOTE: Unless it ends in a slash, the RMI code assumes
        // it is a jar file!
        Properties serverProps = Options.getServerProperties();
        Properties clientProps = Options.getClientProperties();

        String baseURL = "http://localhost:"
            + webServerPort
            + "/";

        String fullCodeBase 
            = baseURL + STUBTIE_DIR + "/ "
            + baseURL + VALUE_DIR + "/";

        // First test code downloading where the client downloads the
        // stub and value classes
        serverProps.put("java.rmi.server.codebase", fullCodeBase);
        testDownloading(cpWithAllClasses,
                        oldClasspath,
                        false);

        // Now test code downloading where the server downloads the
        // value classes

        // Note:  Giving server only the codebase so it can download
        // the Tie.  It will get the info for how to download the
        // valuetype from the client.
        serverProps.put("java.rmi.server.codebase",
                        baseURL + STUBTIE_DIR + "/");
        clientProps.put("java.rmi.server.codebase", fullCodeBase);
        testDownloading(cpWithAllClasses,
                        oldClasspath,
                        true);

        orbd.stop();
        webServer.stop();
    }

    void testDownloading(String fullClasspath,
                         String shortClasspath,
                         boolean serverDownloading) throws Exception
    {
        Controller server, client;

        Properties clientProps = Options.getClientProperties();
        if (serverDownloading) {
            clientProps.put(Tester.SERVER_DOWNLOADING_FLAG, "true");
            Options.setClasspath(shortClasspath);
            server = createServer("corba.codebase.Server", "server_dl");
            Options.setClasspath(fullClasspath);
            client = createClient("corba.codebase.Client", "client_reg");
        } else {
            Options.setClasspath(fullClasspath);
            server = createServer("corba.codebase.Server", "server_reg");
            Options.setClasspath(shortClasspath);
            client = createClient("corba.codebase.Client", "client_dl");
            Options.setClasspath(fullClasspath);
        }


        Test.dprint("Testing code downloading by the " 
                    + (serverDownloading ? "server" : "client"));

        server.start();
        client.start( );

        // Note that the test framework will handle reporting if the overall
        // test failed since it will check the exit codes of the client and
        // server controllers during cleanup
        if (client.waitFor(120000) == Controller.SUCCESS)
            Test.dprint("PASSED");
        else
            Test.dprint("FAILED");

        client.stop();
        server.stop();
    }

    public Controller createWebServer(String webRootDirectory,
                                      int webServerPort)
        throws Exception
    {
        Test.dprint("Creating WebServer object...");

        Controller executionStrategy;
        if (debugProcessNames.contains("WebServer"))
            executionStrategy = new DebugExec();
        else
            executionStrategy = new ExternalExec();

        Properties props = Options.getServerProperties() ;
        int emmaPort = EmmaControl.setCoverageProperties( props ) ;

        String args[] = new String[] { 
                             "-port",
                             "" + webServerPort,
                             "-docroot",
                             webRootDirectory
                             };

        FileOutputDecorator exec =
            new FileOutputDecorator(executionStrategy);

        Hashtable extra = new Hashtable(1);

        // Make sure that starting the web server controller waits until the web server is ready
        extra.put(ExternalExec.HANDSHAKE_KEY, "Ready.");

        exec.initialize("corba.codebase.WebServer",
                        "WebServer",
                        props,
                        null,
                        args,
                        Options.getReportDirectory() + "webserver.out.txt",
                        Options.getReportDirectory() + "webserver.err.txt",
                        extra,
                        emmaPort ) ;

        controllers.add(exec);

        return exec;
    }
}

