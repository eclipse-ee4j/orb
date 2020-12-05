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

package glassfish;

import java.util.List;
import argparser.Pair ;
import java.io.File;
import java.util.ArrayList;
import testtools.Base;

/**
 *
 * @author ken
 */
public class GlassFishInstallation {
    private final Base base ;
    private final String installDir ;
    private final String dasNodeName ;
    private final List<? extends Pair<String,Integer>> availableNodes ;
    private final AdminCommand ac ;
    private boolean isDestroyed = false ;
    private boolean skipSetup ;

    public String installDir() {
        return installDir ;
    }

    public String dasNodeName() {
        return dasNodeName ;
    }

    public List<? extends Pair<String,Integer>> availableNodes() {
        return availableNodes ;
    }

    public String getNAName( String name ) {
        return name + "NA" ;
    }

    public AdminCommand ac() {
        return ac ;
    }

    /** Create a GlassFish installation and all of the node agents needed.
     * After the construction completes, the DAS and node agents are running,
     * but no instances have been created.
     *
     * @param installDir Installation directory for GF (contains bin).
     * @param dasNodeName Name of node used for DAS.
     * @param availableNodes List of node name, max supported instances for each
     * available node (may include DAS node).
     */
    public GlassFishInstallation( Base base, String installDir,
        String dasNodeName, List<? extends Pair<String,Integer>> availableNodes,
        boolean echoOnly, boolean skipSetup ) {
        this.base = base ;
        this.installDir = installDir ;
        if (!installDir.endsWith( File.separator )) {
             installDir += File.separator ;
        }
        this.dasNodeName = dasNodeName ;
        this.availableNodes = availableNodes ;
        this.ac = new AdminCommand( base, installDir + "glassfish/bin/asadmin",
            echoOnly ) ;
        this.skipSetup = skipSetup ;
        start() ;
    }

    public GlassFishInstallation( Base base, String installDir,
        String dasNodeName, List<Pair<String,Integer>> availableNodes ) {
        this( base, installDir, dasNodeName, availableNodes, false, false ) ;
    }

    private void checkDestroyed() {
        if (isDestroyed) {
            throw new RuntimeException( 
                "GlassFish installation has been destroyed") ;
        }
    }

    private void checkAdminCommand( boolean result ) {
        if (!result) {
            throw new RuntimeException( "Admin command failed" ) ;
        }
    }

    /** Start the domain and enable secure mode
     *
     */
    public final void start() {
        if (!skipSetup) {
            checkDestroyed() ;
            checkAdminCommand( ac.startDomain() ) ;
        }
    }

    /** Stop the domain
     *
     */
    public final void stop() {
        if (!skipSetup) {
            checkDestroyed()  ;
            checkAdminCommand( ac.stopDomain() ) ;
        }
    }

    public final void destroy() {
        stop() ;
        isDestroyed = true ;
    }

    public static class Test extends Base {
        private static final String installDir = 
            "/volumes/work/GlassFish/v3/glassfishv3/glassfish" ;
        private static final String dasNodeName = "minas" ;
        private static final List<Pair<String,Integer>> availableNodes =
            new ArrayList<Pair<String,Integer>>() ;

        static {
            availableNodes.add( new Pair<String,Integer>( "minas", 3 ) ) ;
            availableNodes.add( new Pair<String,Integer>( "hermes", 2 ) ) ;
            availableNodes.add( new Pair<String,Integer>( "apollo", 4 ) ) ;
        }

        private GlassFishInstallation gfInst = 
            new GlassFishInstallation( this, installDir, dasNodeName, 
                availableNodes, true, false ) ;

        @testtools.Test
        public void test() {
            gfInst.stop() ;
            gfInst.start() ;
            gfInst.destroy() ;
        }

        public Test( String[] args ) {
            super( args ) ;
        }
    }

    public static void main( String[] args ) {
        Test test = new Test( args ) ;
        test.run() ;
    }
}
