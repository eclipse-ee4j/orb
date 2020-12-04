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

package corba.strm2;

import test.Test;
import corba.framework.*;
import java.util.*;
import java.io.*;
import com.sun.corba.ee.spi.orb.ORB;

public class Strm2Test extends CORBATest 
{
    public static String[] rmicClasses = { "corba.strm2.TesterImpl"};
    
    protected void compileSpecialSubdirectory(String dirName) throws Exception {
        System.out.println("      Compiling classes under " + dirName + "...");

        File outputDir = new File(Options.getOutputDirectory()
                                  + File.separator
                                  + dirName);

        if (!outputDir.mkdir())
            throw new Exception("Error making directory: "
                                + outputDir.getAbsolutePath());

        File testDir = new File(Options.getTestDirectory()
                                + File.separator
                                + dirName);

        if (!testDir.exists())
            throw new Exception("Can't find directory: "
                                + testDir.getAbsolutePath());

        // First look in the directory for all the
        // .java files and get their absolute paths
        File[] filesInDir = testDir.listFiles();
        ArrayList filesToCompile = new ArrayList(filesInDir.length);

        for (int i = 0; i < filesInDir.length; i++) {
            if (filesInDir[i].isFile() &&
                filesInDir[i].toString().endsWith(".java"))
                filesToCompile.add(filesInDir[i]);
        }

        String[] filePathsToCompile = new String[filesToCompile.size()];

        for (int i = 0; i < filePathsToCompile.length; i++) {
            File file = (File)filesToCompile.get(i);

            filePathsToCompile[i] = file.getAbsolutePath();
        }

        // Now compile them to the output directory
        javac.compile(filePathsToCompile,
                      null,
                      outputDir.getAbsolutePath(),
                      Options.getReportDirectory());
    }

    protected void doTest() throws Throwable {
        if (test.Test.useJavaSerialization()) {
            return;
        }

        Options.setRMICClasses(rmicClasses);
        Options.addRMICArgs("-nolocalstubs -iiop -keep -g");
        
        compileRMICFiles();
        compileJavaFiles();

        Controller orbd = createORBD();
        orbd.start();
        
        // This could be done in the overall makefile
        // if someone could figure it out!
        for (int i = 0; i < Versions.testableVersions.length; i++) {
            compileSpecialSubdirectory(Versions.testableVersions[i]);
        }

        Controller servers[] = new Controller[Versions.testableVersions.length];
        Controller clients[] = new Controller[Versions.testableVersions.length];

        // Add these for debugging:
        // Properties clientProps = Options.getExtraClientProperties();
        // clientProps.setProperty("com.sun.corba.ee.ORBDebug", "transport,subcontract,giop");

        // Properties serverProps = Options.getExtraServerProperties();
        // serverProps.setProperty("com.sun.corba.ee.ORBDebug", "transport,subcontract,giop");

        String oldClasspath = Options.getClasspath();
        for (int i = 0; i < Versions.testableVersions.length; i++) {
            String newClasspath = oldClasspath
                + File.pathSeparator
                + Options.getOutputDirectory()
                + Versions.testableVersions[i];

            Options.setClasspath(newClasspath);

            servers[i] = createServer("corba.strm2.Server",
                                      "server_" +
                                      Versions.testableVersions[i]);

            clients[i] = createClient("corba.strm2.Client",
                                      "client_" +
                                      Versions.testableVersions[i]);
            
            servers[i].start();
        }
        Options.setClasspath(oldClasspath);

        // Run through the clients

        for (int i = 0; i < clients.length; i++) {
            String version = Versions.testableVersions[i] ;
            System.out.println("      Running client version " + version ) ;

            clients[i].start();

            clients[i].waitFor(360000);

            clients[i].stop();
        }

        // Stop all the servers
        
        for (int i = 0; i < servers.length; i++)
            servers[i].stop();

        // Finally, stop ORBD
        orbd.stop();

        // The framework will check and report any error
        // codes from the client processes
    }
}
