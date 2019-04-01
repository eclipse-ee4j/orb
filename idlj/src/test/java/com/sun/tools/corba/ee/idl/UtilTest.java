/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.tools.corba.ee.idl;

import com.meterware.simplestub.Memento;
import com.meterware.simplestub.StaticStubSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;

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
    public void tearDown() throws Exception {
        for (Memento memento : mementos)
            memento.revert();
    }

    @Test
    public void readVersionFromDefaultPropertyFile() throws Exception {
        assertThat(Util.getVersion(), containsString("4.02"));
    }

    @Test
    public void readVersionFromUnnamedPropertyFile() throws Exception {
        assertThat(Util.getVersion(""), containsString("4.02"));
    }

    @Test
    public void retrieveMessage() throws Exception {
        assertThat(Util.getMessage("EvaluationException.not"), equalTo("bitwise not"));
    }

    @Test
    public void substituteSingleParamMessage() throws Exception {
        assertThat(Util.getMessage("InvalidArgument.1", "zork"), equalTo("Invalid argument:  zork."));
    }

    @Test
    public void substituteMultiParamMessage() throws Exception {
        assertThat(Util.getMessage("GenFileStream.1", new String[] { "zork", "foo" }), equalTo("zork could not be generated:  foo"));
    }

    @Test
    public void retrieveMessagesFromMultipleFiles() throws Exception {
        Util.registerMessageResource("com/sun/tools/corba/ee/idl/idl");
        Util.registerMessageResource("com/sun/tools/corba/ee/idl/toJavaPortable/toJavaPortable");

        assertThat(Util.getMessage("EvaluationException.not"), equalTo("bitwise not"));
        assertThat(Util.getMessage("NameModifier.TooManyPercent"), equalTo("Pattern contains more than one percent characters"));
    }

    @Test
    public void afterRetrievingMessagesFromOneFile_canRegisterAndRetrieveFromAnother() throws Exception {
        Util.registerMessageResource("com/sun/tools/corba/ee/idl/idl");
        Util.getMessage("EvaluationException.not");
        Util.registerMessageResource("com/sun/tools/corba/ee/idl/toJavaPortable/toJavaPortable");

        assertThat(Util.getMessage("NameModifier.TooManyPercent"), equalTo("Pattern contains more than one percent characters"));
    }
}
