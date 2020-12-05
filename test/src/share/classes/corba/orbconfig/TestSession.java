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

package corba.orbconfig;

import java.io.PrintStream ;

import java.util.ArrayList ;
import java.util.List ;
import org.glassfish.pfl.basic.func.NullaryFunction;
import org.glassfish.pfl.test.JUnitReportHelper;
import org.glassfish.pfl.test.ObjectUtility;

/** TestSession manages running of tests and checking results within
* a test session.  If the session fails any test, the whole session 
* fails with an Error, which can be used to trigger failure in the
* CORBA test framework.  This allows complete testing on a major subsystem
* to report a series of failures, while better containing failures by
* not testing related subsystems.
*/
public class TestSession 
{
    private JUnitReportHelper helper ;
    private PrintStream out ;
    private boolean errorFlag ;
    private String sessionName ;
    private List<String> failures ;

/////////////////////////////////////////////////////////////////////////////////////
// Public interface
/////////////////////////////////////////////////////////////////////////////////////

    public TestSession( PrintStream out, JUnitReportHelper helper )
    {
        this.out = System.out ;
        this.helper = helper ;
    }

    /** Print a message indicating the start of the session.
    * Also clears the error flag.
    */
    public void start( String sessionName ) 
    {
        this.sessionName = sessionName ;
        this.errorFlag = false ;
        out.println( "Test Session " + sessionName ) ;
        helper.start( sessionName ) ;
        failures = new ArrayList<String>() ;
    }

    /** Check for errors at the end of the session.
    */
    public void end()
    {
        if (errorFlag) {
            StringBuilder sb = new StringBuilder() ;
            sb.append( "Test failed with errors:\n" ) ;
            for (String thr : failures) {
                sb.append( "    " ) ;
                sb.append( thr ) ;
                sb.append( "\n" ) ;
            }
            String msg = sb.toString() ;
            helper.fail( msg ) ;
            throw new Error( msg ) ;
        } else {
            helper.pass() ;
        }
    }

    public void testForPass( String name, NullaryFunction<Object> closure,
        Object expectedResult )
    {
        try {
            testStart( name ) ;
            Object result = closure.evaluate() ;

            if (ObjectUtility.equals( result, expectedResult))
                testPass() ;
            else {
                testFail( "Unexpected result returned" ) ;
                out.println( "\t\t\tExpected Result=" +
                    ObjectUtility.defaultObjectToString( expectedResult ) ) ;
                out.println( "\t\t\tActual   Result=" +
                    ObjectUtility.defaultObjectToString( result ) ) ;
            }
        } catch (Throwable thr) {
            testFail( "Unexpected exception " + thr ) ;
            thr.printStackTrace() ;
        }
    }
    
    public void testForException( String name, NullaryFunction<Object> closure,
        Class expectedExceptionClass )
    {
        try {
            testStart( name ) ;
            closure.evaluate();
            testFail( "NullaryFunction<Object> did not throw expected exception" ) ;
        } catch (Throwable thr) {
            if (expectedExceptionClass.isAssignableFrom( thr.getClass() ))
                testPass( "with exception " + thr ) ;
            else
                testFail( "Unexpected exception" + thr ) ;
        }
    }

/////////////////////////////////////////////////////////////////////////////////////
// Internal implementation
/////////////////////////////////////////////////////////////////////////////////////

    private void testStart( String name )
    {
        out.println( "\tTest " + name + "..." ) ;
    }

    private void testFail( String msg )
    {
        failures.add( msg ) ;
        out.println( "\t\tFAILED: " + msg ) ;
        errorFlag = true ;
    }

    private void testPass() 
    {
        testPass( null ) ;
    }

    private void testPass( String msg )
    {
        out.print( "\t\tPASSED" ) ;
        if ((msg != null) && (msg != ""))
            out.print( ": " + msg ) ;
        out.println() ;
    }
}
