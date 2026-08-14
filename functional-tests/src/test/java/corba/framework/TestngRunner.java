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

import java.io.File;

import java.util.Set;
import java.util.HashSet;
import org.glassfish.pfl.test.JUnitReportHelper;

import org.testng.TestNG;
import org.testng.ITestResult;
import org.testng.ITestListener;
import org.testng.ITestContext;

/**
 * Used to set up an appropriate instance of TestNG for running a test. Used inside the CORBA test framework in order to
 * generate useful reports in JUnitReport format for integration with Hudson.
 */
public class TestngRunner {
    private Set<Class<?>> suiteClasses;
    private TestNG testng;
    private String outdirName;
    private boolean hasFailure;

    private class JUnitReportTestListener implements ITestListener {
        private JUnitReportHelper helper;

        JUnitReportTestListener(String name) {
            helper = new JUnitReportHelper(name);
        }

        private void msg(String str) {
            System.out.println(str);
        }

        public void onStart(ITestContext context) {
        }

        public void onFinish(ITestContext context) {
            helper.done();
        }

        // Tracks whether helper.start() has been called for the result we are about
        // to record. TestNG does not always pair a callback with onTestStart: skips
        // never get one, and neither do failures raised outside the test method
        // itself. Calling helper.pass()/fail() in that state throws "No current test
        // set!", which used to escape the listener and kill the whole client process,
        // taking the real failure with it. Assumes sequential execution, which is how
        // this runner drives TestNG.
        private boolean started = false;

        private void ensureStarted(ITestResult result) {
            if (!started) {
                helper.start(result.getName());
                started = true;
            }
        }

        public void onTestStart(ITestResult result) {
            ensureStarted(result);
        }

        public void onTestSkipped(ITestResult result) {
            ensureStarted(result);
            helper.fail("Test was skipped");
            started = false;
        }

        public void onTestFailure(ITestResult result) {
            ensureStarted(result);
            helper.fail(result.getThrowable());
            started = false;
        }

        public void onTestSuccess(ITestResult result) {
            ensureStarted(result);
            helper.pass();
            started = false;
        }

        public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
            ensureStarted(result);
            helper.pass();
            started = false;
        }
    }

    /**
     * Create a new TestngRunner.
     * 
     * @param outdir The directory in which the test reports should be placed.
     */
    public TestngRunner() {
        final String propName = "junit.report.dir";

        String reportDir = System.getProperty(propName);
        if (reportDir == null) {
            System.setProperty(propName, ".");
            reportDir = ".";
        }

        File outdir = new File(reportDir);
        if (!outdir.exists())
            throw new RuntimeException(outdir + " does not exist");

        if (!outdir.isDirectory())
            throw new RuntimeException(outdir + " is not a directory");

        outdirName = reportDir + File.separatorChar + System.getProperty("corba.test.controller.name", "default");

        File destDir = new File(outdirName);
        destDir.mkdir();

        suiteClasses = new HashSet<Class<?>>();
        hasFailure = false;
    }

    /**
     * Register a class container TestNG annotations on test methods. The test report is generated in outdir under the name
     * <classname>.xml. Note that we assume that each suite is represented by a unique class.
     */
    public void registerClass(Class<?> cls) {
        suiteClasses.add(cls);
    }

    /**
     * Run the tests in the registered classes and generate reports.
     */
    public void run() {
        for (Class<?> cls : suiteClasses) {
            testng = new TestNG();
            testng.setTestClasses(new Class<?>[] { cls });
            testng.setOutputDirectory(outdirName);
            testng.setDefaultSuiteName(cls.getName());
            testng.setDefaultTestName(cls.getName());
            testng.addListener(new JUnitReportTestListener(cls.getName()));
            testng.run();
            if (testng.hasFailure())
                hasFailure = true;
        }
    }

    public boolean hasFailure() {
        return hasFailure;
    }

    public void systemExit() {
        if (hasFailure)
            System.exit(1);
        else
            System.exit(0);
    }
}
