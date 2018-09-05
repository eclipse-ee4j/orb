/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.framework ;

import java.io.File ;

import java.util.Set ;
import java.util.HashSet ;
import org.glassfish.pfl.test.JUnitReportHelper;

import org.testng.TestNG ;
import org.testng.ITestResult ;
import org.testng.ITestListener ;
import org.testng.ITestContext ;

/** Used to set up an appropriate instance of TestNG for running a test.
 * Used inside the CORBA test framework in order to generate useful reports
 * in JUnitReport format for integration with Hudson.
 */
public class TestngRunner {
    private Set<Class<?>> suiteClasses ;
    private TestNG testng ;
    private String outdirName ;
    private boolean hasFailure ;

    private class JUnitReportTestListener implements ITestListener {
        private JUnitReportHelper helper ;

        JUnitReportTestListener( String name ) {
            helper = new JUnitReportHelper( name ) ;
        }

        private void msg( String str ) {
            System.out.println( str ) ;
        }

        public void onStart( ITestContext context ) {
        }

        public void onFinish( ITestContext context ) {
            helper.done() ;
        }

        public void onTestStart( ITestResult result ) {
            helper.start( result.getName() ) ;
        }

        public void onTestSkipped( ITestResult result ) {
            helper.fail( "Test was skipped" ) ;
        }

        public void onTestFailure( ITestResult result ) {
            Throwable err = result.getThrowable() ;

            helper.fail( err ) ;
        }

        public void onTestSuccess( ITestResult result ) {
            helper.pass() ;
        }

        public void onTestFailedButWithinSuccessPercentage( ITestResult result ) {
            helper.pass() ;
        }
    }

    /** Create a new TestngRunner.
     * @param outdir The directory in which the test reports should be placed.
     */
    public TestngRunner() {
        final String propName = "junit.report.dir" ;

        String reportDir = System.getProperty( propName ) ; 
        if (reportDir == null) {
            System.setProperty( propName, "." ) ;
            reportDir = "." ;
        }

        File outdir = new File( reportDir ) ;
        if (!outdir.exists())
            throw new RuntimeException( outdir + " does not exist" ) ;

        if (!outdir.isDirectory())
            throw new RuntimeException( outdir + " is not a directory" ) ;

        outdirName = reportDir + File.separatorChar + 
            System.getProperty( "corba.test.controller.name", "default" ) ;

        File destDir = new File( outdirName ) ;
        destDir.mkdir() ;

        suiteClasses = new HashSet<Class<?>>() ;
        hasFailure = false ;
    }

    /** Register a class container TestNG annotations on test methods.
     * The test report is generated in outdir under the name <classname>.xml.
     * Note that we assume that each suite is represented by a unique class.
     */
    public void registerClass( Class<?> cls ) {
        suiteClasses.add( cls ) ;
    }

    /** Run the tests in the registered classes and generate reports.
     */
    public void run() {
        for (Class<?> cls : suiteClasses ) {
            testng = new TestNG() ;
            testng.setTestClasses( new Class<?>[] { cls } ) ;
            testng.setOutputDirectory( outdirName )  ;
            testng.setDefaultSuiteName( cls.getName() ) ;
            testng.setDefaultTestName( cls.getName() ) ;
            testng.addListener( new JUnitReportTestListener( cls.getName() ) ) ;
            testng.run() ;
            if (testng.hasFailure())
                hasFailure = true ;
        }
    }

    public boolean hasFailure() {
        return hasFailure ;
    }

    public void systemExit() {
        if (hasFailure) 
            System.exit( 1 ) ;
        else 
            System.exit( 0 ) ;
    }
}
