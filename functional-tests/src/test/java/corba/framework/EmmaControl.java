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

package corba.framework;

import java.util.Properties ;

// import com.vladium.emma.ctl.ctlCommand ;

/** Programmatic API for use in controlling emma.  This is
 * specific to emma version 2.1.  This is intended for use 
 * in a test harness that execs test programs.  Note that 
 * the execed Java program must be started with the following
 * properties:
 * <ul>
 * <li>emma.rt.control true (the default)
 * <li>emma.rt.control.host localhost (the default)
 * <li>emma.rt.control.port somePort (defaults to 47653)
 * </ul>
 * The port should be one obtained from allocatePort, but that is
 * not too important.
 * Also note that if several programs are execed, each must 
 * have a unique port.  This is yet another CORBA test framework
 * feature that prevents more than one concurrent test run per
 * machine.
 */
public class EmmaControl {
    private EmmaControl() {}

    private static final int FIRST_PORT = 47000 ;

    private static int nextPort = FIRST_PORT ;
    private static boolean DEBUG = true ;

    /** Allocate a port to be used for emma, in the writeCoverageData
     * method.
     */
    private synchronized static int allocatePort() {
        return nextPort++ ;
    }

    public synchronized static void resetPortAllocator() {
        nextPort = FIRST_PORT ;
    }

    public static int setCoverageProperties( Properties props ) {
        // Allow for both automatic and controlled output of coverage data.
        props.setProperty( "emma.coverage.out.file", Options.getEmmaFile() ) ;
        props.setProperty( "emma.coverage.out.merge", "true" ) ;
        props.setProperty( "emma.rt.control", "true" ) ;
        props.setProperty( "emma.rt.control.host", "localhost" ) ;
        int result = allocatePort() ;
        props.setProperty( "emma.rt.control.port", "" + result ) ;
        return result ;
    }

    /** Tell emma to dump the coverage data for the process listening
     * for emma command on port to the given fileName.  fileName is
     * interpreted on the client side and the new coverage data is 
     * merged into the existing file.  Emma will not dump on process
     * exit after this method is used.
     */
    public static void writeCoverageData( int port, String fileName ) {
        String[] args = new String[] {
                "-connect",
                "localhost:" + port,
                "-command", 
                "coverage.dump," + fileName + ",true,true"
            } ;

        /*
        if (DEBUG) {
            System.out.print( "Executing emma ctl command with args:" ) ;
            for (String arg : args) 
                System.out.print( " " + arg ) ;
            System.out.println() ;
        }

        ctlCommand cmd = new ctlCommand( "ctl", args ) ;
        cmd.run() ;
        */

        String command = "java emma ctl" ;
        for (String arg : args)
            command += " " + arg ;
        try {
            Runtime.getRuntime().exec( command ) ;
        } catch (Exception exc) {
            System.out.println( "Error in executing emma ctl command" + exc ) ;
            exc.printStackTrace() ;
        }
    }
}
