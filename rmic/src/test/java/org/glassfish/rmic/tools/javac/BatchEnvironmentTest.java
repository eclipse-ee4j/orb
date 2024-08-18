/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2018, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.rmic.tools.javac;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

import com.meterware.simplestub.Memento;
import com.meterware.simplestub.StaticStubSupport;
import com.meterware.simplestub.SystemPropertySupport;
import java.util.ArrayList;
import java.util.List;
import org.glassfish.rmic.BatchEnvironmentError;
import org.glassfish.rmic.asm.AsmClassFactory;
import org.glassfish.rmic.tools.binaryclass.BinaryClassFactory;
import org.glassfish.rmic.tools.java.ClassDefinitionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

@SuppressWarnings("deprecation")
public class BatchEnvironmentTest {
    private static final String USE_LEGACY_PARSING_PROPERTY = "org.glassfish.rmic.UseLegacyClassParsing";
    private static final String JAVA_VERSION_PROPERTY = "java.version";
    private List<Memento> mementos = new ArrayList<>();

    @BeforeClass
    public static void ensureInitialized() {
        BatchEnvironment.getMaxSupportedClassVersion();
    }

    @Before
    public void setUp() throws Exception {
        mementos.add(SystemPropertySupport.preserve(USE_LEGACY_PARSING_PROPERTY));
        mementos.add(SystemPropertySupport.preserve(JAVA_VERSION_PROPERTY));
        mementos.add(StaticStubSupport.preserve(BatchEnvironment.class, "classDefinitionFactory"));
        System.clearProperty(USE_LEGACY_PARSING_PROPERTY);
    }

    @After
    public void tearDown() throws Exception {
        for (Memento memento : mementos) memento.revert();
    }

    @Test
    public void whenPropertyNotSet_chooseAsmParser() throws Exception {
        ClassDefinitionFactory factory = BatchEnvironment.createClassDefinitionFactory();

        assertThat(factory, instanceOf(AsmClassFactory.class));
    }

    @Test
    public void whenAsmClassesMissingOnJdk8_chooseBinaryParser() throws Exception {
        simulateAsmClassesMissing();
        simulateJdkVersion("1.8");

        ClassDefinitionFactory factory = BatchEnvironment.createClassDefinitionFactory();

        assertThat(factory, instanceOf(BinaryClassFactory.class));
    }

    private void simulateAsmClassesMissing() throws NoSuchFieldException {
        mementos.add(StaticStubSupport.install(AsmClassFactory.class, "simulateMissingASM", true));
    }

    private void simulateJdkVersion(String jdkVersion) {
        System.setProperty(JAVA_VERSION_PROPERTY, jdkVersion);
    }

    @Test
    public void whenLegacyParserRequestedOnJdk8_chooseBinaryParser() throws Exception {
        preferLegacyParser();
        simulateJdkVersion("1.8");

        ClassDefinitionFactory factory = BatchEnvironment.createClassDefinitionFactory();

        assertThat(factory, instanceOf(BinaryClassFactory.class));
    }

    private void preferLegacyParser() {
        System.setProperty(USE_LEGACY_PARSING_PROPERTY, "true");
    }

    @Test
    public void whenLegacyParserRequestedOnJdk9_chooseBinaryParser() throws Exception {
        preferLegacyParser();
        simulateJdkVersion("9");

        ClassDefinitionFactory factory = BatchEnvironment.createClassDefinitionFactory();

        assertThat(factory, instanceOf(BinaryClassFactory.class));
    }
}
