/*
 * Copyright (c) 2018, 2020, Oracle and/or its affiliates.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.corba.idl;

import java.util.HashSet;

import com.meterware.simplestub.Memento;
import com.meterware.simplestub.StaticStubSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

public class UtilTest {
    private HashSet<Memento> mementos = new HashSet<>();

    @Before
    public void setUp() throws Exception {
        mementos.add(StaticStubSupport.install(Util.class, "messages", null));
        mementos.add(StaticStubSupport.preserve(Util.class, "msgResources"));
    }

    @After
    public void tearDown() {
        for (Memento memento : mementos) memento.revert();
    }

    @Test
    public void readVersionFromDefaultPropertyFile() {
        assertThat(Util.getVersion(), containsString("4.02"));
    }

    @Test
    public void readVersionFromUnnamedPropertyFile() {
        assertThat(Util.getVersion(""), containsString("4.02"));
    }

    @Test
    public void retrieveMessage() {
        assertThat(Util.getMessage("EvaluationException.not"), equalTo("bitwise not"));
    }

    @Test
    public void substituteSingleParamMessage() {
        assertThat(Util.getMessage("InvalidArgument.1", "zork"), equalTo("Invalid argument:  zork."));
    }

    @Test
    public void substituteMultiParamMessage() {
        assertThat(Util.getMessage("GenFileStream.1", new String[] {"zork", "foo"}), equalTo("zork could not be generated:  foo"));
    }

    @Test
    public void retrieveMessagesFromMultipleFiles() {
        Util.registerMessageResource("org/glassfish/corba/idl/idl");
        Util.registerMessageResource("org/glassfish/corba/idl/toJavaPortable/toJavaPortable");

        assertThat(Util.getMessage("EvaluationException.not"), equalTo("bitwise not"));
        assertThat(Util.getMessage("NameModifier.TooManyPercent"), equalTo("Pattern contains more than one percent characters"));
    }

    @Test
    public void afterRetrievingMessagesFromOneFile_canRegisterAndRetrieveFromAnother() {
        Util.registerMessageResource("org/glassfish/corba/idl/idl");
        Util.getMessage("EvaluationException.not");
        Util.registerMessageResource("org/glassfish/corba/idl/toJavaPortable/toJavaPortable");

        assertThat(Util.getMessage("NameModifier.TooManyPercent"), equalTo("Pattern contains more than one percent characters"));
    }
}
