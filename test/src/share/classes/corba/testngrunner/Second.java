/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.testngrunner ;

import java.util.Iterator ;
import java.util.Properties ;
import java.util.Map ;
import java.util.List ;
import java.util.ArrayList ;

import java.io.PrintWriter ;

import org.testng.Assert ;
import org.testng.annotations.BeforeSuite ;
import org.testng.annotations.AfterSuite ;
import org.testng.annotations.Test ;
import org.testng.annotations.Configuration ;
import org.testng.annotations.ExpectedExceptions ;

import corba.framework.TestngRunner ;

public class Second {
    private void msg( String str ) {
        System.out.println( "TestngRunner.Second: " + str ) ;
    }

    @BeforeSuite
    public void setup() {
        msg( "setup called" ) ;
    }

    @Test
    public void test1() {
        msg( "test1 called" ) ;
    }

    @Test
    public void test2() {
        msg( "test2 called" ) ;
    }

    @Test
    public void test3() {
        msg( "test3 called" ) ;
        throw new RuntimeException( "Exception in test3" ) ;
    }

    @Test
    public void test4() {
        msg( "test4 called" ) ;
    }

    @Test
    public void test5() {
        msg( "test5 called" ) ;
        Assert.fail( "test5 failed" ) ;
    }

    @Test
    public void anotherTest() {
        msg( "anotherTest called" ) ;
    }
    
    @AfterSuite
    public void shutdown() {
        msg( "shutdown called" ) ;
    }
}

