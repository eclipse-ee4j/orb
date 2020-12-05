/*
 * Copyright (c) 2018, 2020 Oracle and/or its affiliates.
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

package org.glassfish.rmic;

import org.glassfish.rmic.classes.hcks.RmiIIServant;
import org.glassfish.rmic.tools.java.ClassPath;
import org.glassfish.rmic.tools.java.Identifier;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.glassfish.rmic.tools.java.Constants.F_WARNINGS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class MainTest {

    private ClassPath classPath = new ClassPath();
    private ByteArrayOutputStream out = new ByteArrayOutputStream();
    private BatchEnvironment environment;
    private Main main = new Main(out, "rmic");
    private File destDir;

    @Before
    public void setUp() throws Exception {
        destDir = Files.createTempDirectory("rmic").toFile();
        environment = new BatchEnvironment(out, classPath, destDir);
        environment.flags = F_WARNINGS;
    }

    @Test
    public void whenNoErrorsOrWarnings_displayErrorsReturnsTrue() throws Exception {
        assertThat(main.displayErrors(environment), is(true));
    }

    @Test
    public void afterOneError_displayErrorsReturnsFalse() throws Exception {
        environment.error(0, "error");

        assertThat(main.displayErrors(environment), is(false));
    }

    @Test
    public void whenNoErrorsOrWarnings_outputIsEmpty() throws Exception {
        main.displayErrors(environment);

        assertThat(getOutput(), isEmptyString());
    }

    private String getOutput() throws IOException {
        out.close();
        return out.toString();
    }

    @Test
    public void afterOneError_outputReportsOneError() throws Exception {
        reportError();

        main.displayErrors(environment);

        assertThat(getOutput(), containsString("1 error"));
    }

    private void reportError() {
        environment.error(0, "rmic.wrote", "something bad");
    }

    @Test
    public void afterThreeErrors_outputReportsNumberOfErrors() throws Exception {
        reportError();
        reportError();
        reportError();

        main.displayErrors(environment);

        assertThat(getOutput(), containsString("3 errors"));
    }

    @Test
    public void afterOneWarning_outputReportsOneWarning() throws Exception {
        reportWarning();

        main.displayErrors(environment);

        assertThat(getOutput(), containsString("1 warning"));
    }

    private void reportWarning() {
        environment.error(0, "warn.rmic.member.not.mapped", "foo", "bar");
    }

    @Test
    public void afterThreeWarningss_outputReportsNumberOfErrors() throws Exception {
        reportWarning();
        reportWarning();
        reportWarning();

        main.displayErrors(environment);

        assertThat(getOutput(), containsString("3 warnings"));
    }

    @Test
    public void afterOneErrorAndTwoWarnings_outputReportsNumbersOfBoth() throws Exception {
        reportError();
        reportWarning();
        reportWarning();

        main.displayErrors(environment);

        assertThat(getOutput(), containsString("1 error, 2 warnings"));
    }

    @Test
    public void afterTwoErrorsAndOneWarnings_outputReportsNumbersOfBoth() throws Exception {
        reportError();
        reportError();
        reportWarning();

        main.displayErrors(environment);

        assertThat(getOutput(), containsString("2 errors, 1 warning"));
    }

    @Test
    public void afterGenerateStubs_getGeneratedClassNames() throws Exception {
        main.parseArgs("-iiop", "-classpath", TestUtils.getClassPathString(), "-d", destDir.getAbsolutePath());
        environment = main.getEnv();
        Identifier identifier = Main.getClassIdentifier(environment, RmiIIServant.class.getName());
        main.generateClass(environment, identifier);

        assertThat(main.getGeneratedClassNames(environment),
                arrayContainingInAnyOrder("org.glassfish.rmic.classes.hcks._RmiII_Stub", "org.glassfish.rmic.classes.hcks._RmiIIServant_Tie"));
    }

}
