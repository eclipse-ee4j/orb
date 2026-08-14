/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package test.suites;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;

import test.ServantContext;

import static org.junit.Assert.assertEquals;

/**
 * Reports each entry of a .tdesc suite as its own JUnit test case.
 *
 * This is an accounting layer only. Running an entry still goes through {@code test.Test}, which
 * still forks the ORBD/server/client JVMs and synchronises with them over stdout handshakes
 * exactly as before -- what changes is that a result is now attributed to a named case instead of
 * being folded into one exit code for a whole suite.
 *
 * Isolation matches the old harness. Surefire is configured with forkCount=1/reuseForks=false, so
 * each concrete subclass -- that is, each .tdesc file -- gets a fresh JVM, while the entries
 * within one file share it, which is what running {@code java test.Test -file <x>.tdesc} did.
 *
 * Subclasses supply their .tdesc as a classpath resource:
 *
 * <pre>
 * public class CorbaTdescSuiteTest extends TdescSuite {
 *     &#64;Parameters(name = "{0}")
 *     public static Collection&lt;Object[]&gt; data() throws IOException {
 *         return entries("/corba/CORBATests.tdesc");
 *     }
 * }
 * </pre>
 */
@RunWith(Parameterized.class)
public abstract class TdescSuite {

    /**
     * The entry as written in the .tdesc, e.g. "corba.ior.IORTests" or
     * "rmic.HelloTest -localservants -normic". Carried as its own parameter, and used as the
     * JUnit case name, so results stay comparable to the historical harness output by name.
     */
    @Parameter(0)
    public String name;

    /** The entry split into arguments, by the harness's own .tdesc tokenizer. */
    @Parameter(1)
    public String[] args;

    /** Relative to the working directory Surefire sets, so this resolves to target/gen. */
    private static final String OUTPUT_DIR = "gen";

    /** Applied to every entry, mirroring what the Ant run-test macro passed on the command line. */
    private static String[] baseArgs() {
        return new String[] { "-verbose", "-output", OUTPUT_DIR };
    }

    /**
     * Parse a .tdesc into JUnit parameters. Only reads and tokenizes: no ORB is created and no
     * process is launched here, so a failure during parameter discovery cannot leave servants
     * behind for the {@link #cleanup()} below to miss.
     */
    protected static Collection<Object[]> entries(String resource) throws IOException {
        InputStream in = TdescSuite.class.getResourceAsStream(resource);
        if (in == null) {
            throw new IOException("test descriptor not on the classpath: " + resource);
        }

        List<Object[]> params = new ArrayList<Object[]>();
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(in, StandardCharsets.UTF_8));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                // same filter as test.Test.runTestFile
                if (line.startsWith("//") || line.isEmpty()) {
                    continue;
                }
                // reuse the harness's tokenizer rather than reinterpreting .tdesc syntax here
                String[] args = test.Test.parseTestLine(line);
                params.add(new Object[] { describe(args), args });
            }
        } finally {
            reader.close();
        }
        return params;
    }

    /** Name a case after its test class plus any flags that distinguish repeated entries. */
    private static String describe(String[] args) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if ("-test".equals(args[i]) && i + 1 < args.length) {
                sb.insert(0, args[++i]);
            } else if (!"-test".equals(args[i])) {
                sb.append(sb.length() == 0 ? "" : " ").append(args[i]);
            }
        }
        return sb.length() == 0 ? "(empty)" : sb.toString();
    }

    /**
     * Create the output directories the harness expects, and fill in the runtime properties it
     * needs. Ant's test-init target used to do the former and test.Test.main the latter; neither
     * runs now, and JUnitReportHelper throws from its constructor if junit.report.dir is missing.
     */
    @BeforeClass
    public static void prepareRuntime() throws IOException {
        new java.io.File(OUTPUT_DIR).mkdirs();
        String reportDir = System.getProperty("junit.report.dir");
        if (reportDir != null) {
            new java.io.File(reportDir).mkdirs();
        }
        test.Test.initRuntimeProperties();
    }

    @Test
    public void runTdescEntry() {
        assertEquals("exit code for " + name, 0, test.Test.runTestLine(baseArgs(), args));
    }

    /**
     * Runs once after all entries of a suite, not once per entry: JUnit executes an inherited
     * @AfterClass after the subclass's tests complete. This reproduces the cleanup that
     * test.Test.main did in its finally block, without the System.exit that came with it.
     */
    @AfterClass
    public static void cleanup() {
        ServantContext.destroyAll();
    }
}
