/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package testtools;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set ;
import java.util.HashSet ;
import java.lang.reflect.Method ;
import argparser.ArgParser ;
import argparser.Help ;
import argparser.DefaultValue ;
import argparser.Separator ;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/** A VERY quick-and-dirty test framework.
 *
 * @author ken
 */
public class Base {
    private final List<Method> testMethods ;
    private final List<String> currentResults ;
    // private final List<String> currentNotes ;
    private final Arguments argvals ;
    private final Set<String> includes ;
    private final Set<String> excludes ;
    private final List<Method> preMethods ;
    private final List<Method> postMethods ;

    private String current ;
    private Set<String> pass = new HashSet<String>() ;
    private Set<String> fail = new HashSet<String>() ;
    private Set<String> skip = new HashSet<String>() ;

    private interface Arguments {
        @DefaultValue( "false" )
        @Help( "Control debugging mode")
        boolean debug() ;

        @DefaultValue( "false" ) 
        @Help( "Displays the valid test case identifiers" ) 
        boolean cases() ;

        @DefaultValue( "" ) 
        @Help( "A list of test cases to include: includes everything if empty" ) 
        @Separator( "," )
        List<String> include() ;

        @DefaultValue( "" ) 
        @Help( "A list of test cases to excelude: include everything if empty" ) 
        @Separator( "," )
        List<String> exclude()  ;
    }

    private void execute( Collection<Method> methods )
        throws IllegalAccessException, IllegalArgumentException,
        InvocationTargetException {

        for (Method m : methods) {
            m.invoke( this ) ;
        }
    }

    public Base( String[] args ) {
        this( args, null ) ;
    }

    public Base(String[] args, Class<?> parserInterface) {
        this( args, parserInterface, null ) ;
    }

    public Base(String[] args, Class<?> parserInterface, Class<?> testClass ) {
        testMethods = new ArrayList<Method>() ;
        preMethods = new ArrayList<Method>() ;
        postMethods = new ArrayList<Method>() ;

        final Class<?> cls = (testClass == null) ? this.getClass() : testClass ;
        for (Method m : cls.getMethods()) {
            if (m.getDeclaringClass().equals( Base.class )
                && !this.getClass().equals( Base.class )) {
                // Skip test methods defined on this class for self test
                // unless we are actually running the self test.
                continue ;
            }

            Test anno = m.getAnnotation( Test.class ) ;
            if (anno != null) {
                if (m.getParameterTypes().length == 0) {
                    if (m.getReturnType().equals( void.class )) {
                        testMethods.add( m ) ;
                    } else {
                        msg( "Method " + m + " is annotated @Test, "
                            + "but has a non-void return type").nl() ;
                    }
                } else {
                    msg( "Method " + m + " is annotated @Test, "
                        + "but has parameters").nl() ;
                }
            }

            Pre pre = m.getAnnotation( Pre.class ) ;
            if (pre != null) {
                preMethods.add( m ) ;
            }

            Post post = m.getAnnotation( Post.class ) ;
            if (post != null) {
                postMethods.add( m ) ;
            }
        }


        Class<?>[] interfaces = (parserInterface == null)
            ? new Class<?>[]{ Arguments.class } 
            : new Class<?>[]{ Arguments.class, parserInterface } ;

        ArgParser parser = new ArgParser( Arrays.asList(interfaces)) ;
        argvals = (Arguments)parser.parse( args ) ;
        if (argvals.debug()) {
            msg( "Arguments are:\n" + argvals ).nl() ;
        }

        if (argvals.include().isEmpty()) {
            includes = new HashSet<String>() ;
            for (Method m : testMethods) {
                includes.add( getTestId( m ) ) ;
            }
        } else {
            List<String> incs = argvals.include() ;
            includes = new HashSet<String>( incs ) ;
        }

        excludes = new HashSet<String>( argvals.exclude() ) ;

        if (argvals.cases()) {
            msg( "Valid test case identifiers are:" ).nl() ;
            for (Method m : testMethods) {
                msg( "    " + getTestId( m ) ).nl() ;
            }
        }
        
        currentResults = new ArrayList<String>() ;
        // currentNotes = new ArrayList<String>() ;
    }

    public <T> T getArguments( Class<T> cls ) {
        return cls.cast( argvals ) ;
    }

    private Base msg( String str ) {
        System.out.print( str ) ;
        return this ;
    }

    private Base nl() {
        System.out.println() ;
        return this ;
    }

    private String getTestId( Method m ) {
        Test anno = m.getAnnotation( Test.class ) ;
        if (!anno.value().equals("")) {
            return anno.value() ;
        }

        String mname = m.getName() ;
        if (mname.startsWith( "test" )) {
            return mname.substring( 4 ) ;
        } else {
            return mname ;
        }
    }

    private void display( String title, List<String> strs ) {
        if (!strs.isEmpty()) {
            msg( title + ":" ).nl() ;
            for (String str : strs ) {
                msg( "\t" + str ).nl() ;
            }
        }
    }

    public int run() {
        for (Method m : testMethods) {
            currentResults.clear() ;
            // currentNotes.clear() ;

            current = getTestId( m ) ;
            if (includes.contains(current) && !excludes.contains(current)) {
                msg( "Test " + current + ": " ).nl() ;
                msg( "    Notes:" ).nl() ;
                try {
                    execute( preMethods ) ;
                    m.invoke( this ) ;
                } catch (Exception exc) {
                    fail( "Caught exception : " + exc )  ;
                    exc.printStackTrace();
                } finally {
                    try {
                        execute(postMethods);
                    } catch (Exception exc) {
                        fail( "Exception in post methods : " + exc ) ;
                        exc.printStackTrace();
                    }
                }

                if (currentResults.isEmpty()) {
                    pass.add( current ) ;
                    msg( "Test " + current + " PASSED." ).nl() ;
                } else {
                    fail.add( current )  ;
                    msg( "Test " + current + " FAILED." ).nl() ;
                }

                // display( "    Notes", currentNotes ) ;
                display( "    Results", currentResults ) ;
            } else {
                msg( "Test " + current + " SKIPPED" ).nl() ;
                skip.add( current ) ;
            }
        }

        msg( "-------------------------------------------------").nl() ;
        msg( "Results:" ).nl() ;
        msg( "-------------------------------------------------").nl() ;

        msg( "\tFAILED:").nl() ; displaySet( fail ) ;
        msg( "\tSKIPPED:").nl() ; displaySet( skip ) ;
        msg( "\tPASSED:").nl() ; displaySet( pass ) ;

        nl() ;
        msg( pass.size() + " test(s) passed; "
            + fail.size() + " test(s) failed; "
            + skip.size() + " test(s) skipped." ).nl() ;
        msg( "-------------------------------------------------").nl() ;

        return fail.size() ;
    }

    private void displaySet( Set<String> set ) {
        for (String str : set ) {
            msg( "\t\t" ).msg( str ).nl() ;
        }
    }

    public void fail( String failMessage ) {
        check( false, failMessage ) ;
    }

    public void check( boolean result, String failMessage ) {
        if (!result) {
            currentResults.add( failMessage ) ;
        }
    }

    public void note( String msg ) {
        // currentNotes.add( msg ) ;
        msg( "\t" + msg ).nl() ;
    }

    @Test
    public void testSimple() {}

    @Test
    public void testGood( ) {
        note( "this is a good test" ) ;
        note( "A second note") ;
    }

    @Test( "Bad" )
    public void badTest() {
        note( "this is a bad test" ) ;
        fail( "this test failed once" ) ;
        fail( "this test failed twice" ) ;
    }

    @Test
    public void exception() {
        throw new RuntimeException( "This test throws an exception") ;
    }

    @Test
    public boolean badReturnType() {
        return true ;
    }

    @Test
    public void hasParameters( String name ) {
    }

    public static void main( String[] args ) {
        Base base = new Base( args ) ;
        base.run() ;
    }
}
