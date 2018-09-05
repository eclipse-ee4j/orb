/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.orbconfig;

import java.lang.reflect.Method ;
import java.lang.reflect.Array ;

import java.io.PrintStream ;

import java.util.Properties ;
import java.util.Map ;
import java.util.HashMap ;
import java.util.Set ;
import java.util.HashSet ;

import java.net.InetAddress;
import java.net.URL ;
import java.net.MalformedURLException ;
import java.net.UnknownHostException;

import java.applet.Applet ;
import java.applet.AppletContext ;
import java.applet.AppletStub ;

import java.io.File ;
import java.io.OutputStream ;
import java.io.FileOutputStream ;

import com.sun.corba.ee.spi.orb.OperationFactory ;
import com.sun.corba.ee.spi.orb.DataCollector ;
import com.sun.corba.ee.spi.orb.Operation ;
import com.sun.corba.ee.spi.orb.PropertyParser ;
import com.sun.corba.ee.spi.orb.ParserImplBase ;
import com.sun.corba.ee.spi.orb.ParserData ;
import com.sun.corba.ee.spi.orb.ORB ;
import com.sun.corba.ee.spi.orb.ORBData ;

import com.sun.corba.ee.impl.orb.ORBDataParserImpl ;
import com.sun.corba.ee.impl.orb.DataCollectorFactory ;
import com.sun.corba.ee.impl.orb.ParserTable ;

import com.sun.corba.ee.spi.misc.ORBConstants ;

import com.sun.corba.ee.spi.ior.iiop.GIOPVersion ;
import org.glassfish.pfl.basic.contain.Pair;
import org.glassfish.pfl.basic.func.NullaryFunction;
import org.glassfish.pfl.basic.func.NullaryPredicate;
import org.glassfish.pfl.test.JUnitReportHelper;
import org.glassfish.pfl.test.ObjectUtility;

public class Client 
{
    private JUnitReportHelper helper ;
    private PrintStream out ;
    private TestSession session ;
    private boolean noJavaHomeAvailable = false ;

    public static void main(String args[])
    {
        System.out.println( "Starting NewORB test" ) ;
        try{
            Properties props = new Properties( System.getProperties() ) ;
            props.put( "org.omg.CORBA.ORBClass", 
                "com.sun.corba.ee.impl.orb.ORBImpl" ) ;
            new Client( props, args, System.out ) ;
        } catch (Exception e) {
            System.out.println("ERROR : " + e) ;
            e.printStackTrace(System.out);
            System.exit (1);
        }
    }

    public Client( Properties props, String args[], PrintStream out )
    {
        this.out = System.out ;
        helper = new JUnitReportHelper( Client.class.getName() ) ;
        this.session = new TestSession( out, helper ) ;

        try {
            runTests() ;
        } finally {
            helper.done() ;
        }
    }

// *************************************************
// ***************   TESTS   ***********************
// *************************************************

    private void runTests()
    {
        testUtility() ;
        testOperations() ;
        testParser() ;
        setupDCEnvironment() ;
        testNormalDataCollector() ;
        testAppletDataCollector() ;
        testORBData() ;
        testORBServerHostAndListenOnAllInterfaces();

        // What about testing ORBConfigurator and ORBImpl?  Do we leave this to 
        // indirect testing?
        testUserConfigurator() ;

    }

    public static class TPair {
        public TPair first ;
        public int second ;
    }

    public static class Record {
        public TPair first ;
        public Record second ;
        public String third ;
    }

    public Record makeRecord1()
    {
        TPair x1 = new TPair() ;
        TPair x2 = new TPair() ;
        TPair x3 = new TPair() ;
        TPair x4 = new TPair() ;
        TPair x5 = new TPair() ;

        Record y1 = new Record() ;
        Record y2 = new Record() ;
        Record y3 = new Record() ;

        x1.first = x2 ;
        x1.second = 32 ;
        x2.first = x3 ;
        x2.second = 14 ;
        x3.first = x4 ;
        x3.second = 139 ;
        x4.first = x5 ;
        x4.second = 44 ;
        x5.first =  x1 ;
        x5.second = 21 ;

        y1.first = x1 ;
        y2.first = x1 ;
        y3.first = x3 ;

        y1.second = y2 ;
        y2.second = y3 ;
        y3.second = y1 ;

        y1.third = "Test" ;
        y2.third = "This" ;
        y3.third = "Thing" ;
        return y1 ;
    }

    public TPair makeTPair()
    {
        TPair x1 = new TPair() ;
        TPair x2 = new TPair() ;
        TPair x3 = new TPair() ;

        x1.first = x2 ;
        x1.second = 32 ;
        x2.first = x3 ;
        x2.second = 14 ;
        x3.first = x1 ;
        x3.second = 139 ;
        return x1 ;
    }

    public Record makeRecord2()
    {
        TPair x1 = makeTPair() ;
        TPair x2 = makeTPair() ;

        Record y1 = new Record() ;
        Record y2 = new Record() ;
        Record y3 = new Record() ;

        y1.first = x1 ;
        y2.first = x1 ;
        y3.first = x2 ;

        y1.second = y2 ;
        y2.second = y3 ;
        y3.second = y1 ;

        y1.third = "Test" ;
        y2.third = "This" ;
        y3.third = "Thing" ;
        return y1 ;
    }

    public Record makeRecord3()
    {
        TPair x1 = makeTPair() ;
        TPair x2 = makeTPair() ;

        Record y1 = new Record() ;
        Record y2 = new Record() ;
        Record y3 = new Record() ;

        y1.first = x1 ;
        y2.first = x2 ;
        y3.first = x2 ;

        y1.second = y2 ;
        y2.second = y3 ;
        y3.second = y1 ;

        y1.third = "Test" ;
        y2.third = "This" ;
        y3.third = "Thing" ;
        return y1 ;
    }

    public void testUtility() 
    {
        session.start( "Utility" ) ;

        final Record y1 = makeRecord1() ;
        final Record y2 = makeRecord1() ;
        final Record y3 = makeRecord2() ;
        final Record y4 = makeRecord3() ;

        String res = ObjectUtility.make().objectToString( y1 ) ;
        System.out.println( res ) ;

        res = ObjectUtility.make( false, false ).objectToString( y1 ) ;
        System.out.println( res ) ;

        NullaryFunction<Object> closure1 = new NullaryFunction<Object>() {
            public Object evaluate() {
                return Boolean.valueOf( ObjectUtility.equals( y1, y1 ) ) ;
            }
        } ;

        session.testForPass( "Testing structural equals: 1", closure1,
            Boolean.TRUE ) ;

        NullaryFunction<Object> closure2 = new NullaryFunction<Object>() {
            public Object evaluate() {
                return Boolean.valueOf( ObjectUtility.equals( y1, y2 ) ) ;
            }
        } ;

        session.testForPass( "Testing structural equals: 2", closure2,
            Boolean.TRUE ) ;

        NullaryFunction<Object> closure3 = new NullaryFunction<Object>() {
            public Object evaluate() {
                return Boolean.valueOf( ObjectUtility.equals( y1, y3 ) ) ;
            }
        } ;

        session.testForPass( "Testing structural equals: 3", closure3,
            Boolean.FALSE ) ;

        NullaryFunction<Object> closure4 = new NullaryFunction<Object>() {
            public Object evaluate() {
                return Boolean.valueOf( ObjectUtility.equals( y3, y4 ) ) ;
            }
        } ;

        session.testForPass( "Testing structural equals: 4", closure4,
            Boolean.FALSE ) ;

        session.end() ;
    }

    private NullaryFunction<Object> makeActionEvaluator( final Operation action,
        final Object data )
    {
        return new NullaryFunction<Object>() {
            public Object evaluate() {
                action.operate( data )  ;
                return true ;
            }
        } ;
    }

    private void expectError( final Object data, final Operation action,
        Class expectedError )
    {
        String msg = action + "(" + 
            ObjectUtility.make( true, true ).objectToString(data) +
            ") should throw " + expectedError ;

        session.testForException( msg, makeActionEvaluator( action, data ), 
            expectedError )  ;
    }

    private void expectResult( final Object data, final Operation action, 
        final Object expectedResult ) 
    {
        String msg = action + "(" + 
            ObjectUtility.make( true, true ).objectToString(data) + ") == " + 
            ObjectUtility.make( true, true ).objectToString(expectedResult) + "?" ;

        session.testForPass( msg, makeActionEvaluator( action, data ), 
            expectedResult) ;
    }

    private void testOperations() 
    {
        session.start( "Operations" ) ;

        // test indexAction
        Operation indexAction = OperationFactory.indexAction( 3 ) ;

        Integer[] data1 = { Integer.valueOf(0), Integer.valueOf(1) } ;
        expectError( data1, indexAction, 
            java.lang.IndexOutOfBoundsException.class ) ;

        Integer[] data2 = { Integer.valueOf(0), Integer.valueOf(1), 
                            Integer.valueOf(2), Integer.valueOf(3) } ;
        expectResult( data2, indexAction, Integer.valueOf(3) ) ;

        // test booleanAction
        Operation booleanAction = OperationFactory.booleanAction() ;
        expectResult( "TRUE", booleanAction, Boolean.valueOf( true ) ) ;
        expectResult( "false", booleanAction, Boolean.valueOf( false ) ) ;
        expectResult( "XXffOP2", booleanAction, Boolean.valueOf( false ) ) ;

        // test integerAction
        Operation integerAction = OperationFactory.integerAction() ;
        expectResult( "123", integerAction, Integer.valueOf( 123 ) ) ;
        expectError( "123ACE", integerAction, 
            java.lang.NumberFormatException.class ) ;

        // test stringAction
        Operation stringAction = OperationFactory.stringAction() ;
        expectResult( "This_is_a_string", stringAction, "This_is_a_string" ) ;

        // test classAction
        Operation classAction = OperationFactory.classAction( ORB.defaultClassNameResolver() ) ;
        expectResult( "com.sun.corba.ee.spi.orb.ORB", classAction,
            com.sun.corba.ee.spi.orb.ORB.class ) ;

        // test setFlagAction
        Operation setFlagAction = OperationFactory.setFlagAction() ;
        expectResult( "", setFlagAction, Boolean.valueOf( true ) ) ;

        // test URLAction
        Operation URLAction = OperationFactory.URLAction() ;
        URL testURL = null ;
        try {
            testURL = new URL( "http://www.sun.com" ) ;
        } catch (java.net.MalformedURLException exc) {}

        expectResult( "http://www.sun.com", URLAction, testURL ) ;
        // For some reason, all strings seem to work: explore later.
        // expectError( "zqxyr://somerandomstuff", URLAction,
            // java.net.MalformedURLException.class ) ;

        // test integerRangeAction
        Operation integerRangeAction = OperationFactory.integerRangeAction(
            12, 24 ) ;
        expectResult( "13", integerRangeAction, Integer.valueOf( 13 ) ) ;
        expectError( "123ACE", integerRangeAction, 
            java.lang.NumberFormatException.class ) ;
        expectError( "2", integerRangeAction, 
            org.omg.CORBA.BAD_OPERATION.class ) ;

        // test listAction
        Operation listAction = OperationFactory.listAction( ",", 
            integerAction ) ;
        String arg = "12,23,34,56,129" ;
        Object[] expectedResult = { Integer.valueOf( 12 ), Integer.valueOf( 23 ),
            Integer.valueOf( 34 ), Integer.valueOf( 56 ), Integer.valueOf( 129 ) } ;

        expectResult( arg, listAction, expectedResult ) ;

        // test sequenceAction
        Operation[] actions = { integerAction, integerAction, stringAction, 
            booleanAction } ;
        Operation sequenceAction = OperationFactory.sequenceAction( ",",
            actions ) ;

        String arg2 = "12,23,this_thing,true" ;
        Object[] expectedResult2 = { Integer.valueOf( 12 ), Integer.valueOf( 23 ),
            "this_thing", Boolean.valueOf( true ) } ;

        expectResult( arg2, sequenceAction, expectedResult2 ) ;

        // test compose
        Operation composition = OperationFactory.compose( listAction, 
            indexAction ) ;
        expectResult( arg, composition, Integer.valueOf( 56 ) ) ;

        // test mapAction
        Operation map = OperationFactory.mapAction( integerAction ) ;
        String[] strings = { "12", "23", "473", "2" } ;
        Object[] result = { Integer.valueOf( 12 ), Integer.valueOf( 23 ), Integer.valueOf( 473 ),
            Integer.valueOf( 2 ) } ;
        expectResult( strings, map, result ) ;

        session.end() ;
    }

    private PropertyParser makeParser()
    {
        PropertyParser parser = new PropertyParser() ;
        parser.add( "foo.arg", OperationFactory.integerAction(), "arg" ) ;
        parser.add( "foo.flag", OperationFactory.booleanAction(), "flag" ) ;
        parser.add( "foo.str", OperationFactory.stringAction(), "str" ) ;
        
        parser.addPrefix( "foo.prefix", OperationFactory.identityAction(), 
            "prefix", Object.class ) ;

        // Action to parser <str:num>, list into Object[][]
        Operation[] inner = { OperationFactory.stringAction(), 
            OperationFactory.integerAction() } ;
        Operation innerList = OperationFactory.sequenceAction( ":", inner ) ;
        Operation outerList = OperationFactory.listAction( ",", innerList ) ;
        parser.add( "foo.list", outerList, "list" ) ;
        return parser ;
    }

    private Properties makeTestProperties() 
    {
        Properties props = new Properties() ;
        props.setProperty( "foo.arg", "273" ) ;
        props.setProperty( "foo.flag", "true" ) ;
        props.setProperty( "foo.str", "AValue" ) ;
        props.setProperty( "foo.prefix.part1", "first" ) ;
        props.setProperty( "foo.prefix.part2", "second" ) ;
        props.setProperty( "foo.prefix.part3", "third" ) ;
        props.setProperty( "foo.list", "red:0,blue:1,green:2" ) ;
        return props ;
    }

    private Map<String,Object> makeResult()
    {
        Map<String,Object> map = new HashMap<String,Object>() ;
        map.put( "arg", Integer.valueOf( 273 ) ) ;
        map.put( "flag", Boolean.valueOf( true ) ) ;
        map.put( "str", "AValue" ) ;

        // This is the order the result comes in: the order is not
        // guaranteed in this case.
        Pair[] list = { 
            new Pair<String,String>( "part3", "third" ), 
            new Pair<String,String>( "part2", "second" ), 
            new Pair<String,String>( "part1", "first" ) 
        };

        map.put( "prefix", list ) ; 

        Object[][] data = { 
            { "red", Integer.valueOf( 0 ) },
            { "blue", Integer.valueOf( 1 ) },
            { "green", Integer.valueOf( 2 ) },
        } ;

        map.put( "list", data ) ;
        return map ;
    }

    private void testParser() 
    {
        session.start( "Parser" ) ;
        
        final PropertyParser parser = makeParser() ;
        final Properties props = makeTestProperties() ;
        NullaryFunction<Object> closure  =
            new NullaryFunction<Object>() {
            public Map<String,Object> evaluate() {
                return parser.parse( props )  ; 
            }
        } ;

        Map<String,Object> expectedResult = makeResult() ;

        session.testForPass( "parser", closure, expectedResult ) ;

        session.end() ;
    }


    // test design:
    // Properties we will use:              Type:
    //      INITIAL_HOST_PROPERTY           string
    //      SERVER_HOST_PROPERTY            string
    //      ORB_INIT_REF_PROPERTY           (string,string)[] (prefix)
    //      foo.arg1                        integer
    //      foo.prefix                      (string,Foo)[]  (prefix)
    //      ORG_OMG_CORBA_PREFIX.poa.maxhold        integer
    //      SUN_PREFIX.poa.foo              Foo
    //      SUN_LC_PREFIX.pool.size         integer
    //      SUN_LC_VERSION_PREFIX.bar.foo   Foo
    // also need to write orb.properties files (and also preserve any
    // installed orb.properties files)

    private static class Foo{
        private String data ;

        public Foo( String str )
        {
            data = str ;
        }

        public boolean equals( Object obj ) 
        {
            if (this == obj)
                return true ;

            if (!(obj instanceof Foo))
                return false ;

            Foo other = (Foo)obj ;

            return other.data.equals( data ) ;
        }

        public int hashCode()
        {
            return data.hashCode() ;
        }

        public String toString()
        {
            return data ;
        }

        private static Operation getOperation()
        {
            return new Operation() {
                public Object operate( Object arg ) {
                    return new Foo( (String)arg ) ;
                }
            } ;
        }
    }

    private PropertyParser makeDCParser()
    {
        PropertyParser parser = new PropertyParser() ;
        parser.add( ORBConstants.INITIAL_HOST_PROPERTY, 
            OperationFactory.stringAction(), "initialHost" ) ;
        parser.add( ORBConstants.SERVER_HOST_PROPERTY,
            OperationFactory.stringAction(), "serverHost" ) ;
        parser.add( ORBConstants.ORB_INIT_REF_PROPERTY,
            OperationFactory.identityAction(), "initRefs" ) ;
        parser.add( "foo.ORBarg1", Foo.getOperation(), "fooArg1" ) ;

        Operation[] ops = { OperationFactory.stringAction(), Foo.getOperation() } ;
        Operation prefixOp = OperationFactory.mapSequenceAction( ops ) ;
        parser.addPrefix( "foo.prefix", prefixOp, "fooPrefix", Object.class ) ;
        parser.add( ORBConstants.CORBA_PREFIX + "poa.ORBmaxhold", 
            OperationFactory.integerAction(), "poaMaxHold" ) ;
        parser.addPrefix( ORBConstants.CORBA_PREFIX + "ORBmodule", 
            OperationFactory.stringAction(), "module", String.class ) ;
        parser.add( ORBConstants.SUN_PREFIX + "poa.ORBfoo", Foo.getOperation(),
            "poaFoo" ) ;
        parser.add( ORBConstants.SUN_PREFIX + "pool.ORBsize",
            OperationFactory.integerAction(), "poolSize" ) ;
        parser.add( ORBConstants.SUN_PREFIX + "bar.foo",
            Foo.getOperation(), "barFoo" ) ;
        return parser ;
    }

    private String[] makeDCArgs()
    {
        // Note that the -ORBModule flag should be ignored, as this is not
        // supported for prefixes.
        String[] result = { "-ORBmaxHold", "27",
            "-ORBInitRef", "FooService=corbaloc::host.org/FooService", 
            "-ORBInitRef", "BarService=corbaloc::host.org/BarService",
            "-ORBmodule.doorTransport", "doorTransportImpl" } ;
        return result ;
    }

    private static class TestAppletStub implements AppletStub {
        private Properties parameters ;
        private URL codeBase ;
        private URL documentBase ;

        TestAppletStub(  Properties parameters, URL codeBase, 
            URL documentBase ) 
        {
            this.parameters = parameters ;
            this.codeBase = codeBase ;
            this.documentBase = documentBase ;
        }

        public void appletResize( int width, int height )
        {
        }

        public AppletContext getAppletContext() 
        {
            return null ;
        }

        public URL getCodeBase()
        {
            return codeBase ;
        }

        public URL getDocumentBase()
        {
            return documentBase ;
        }

        public String getParameter( String name )
        {
            return parameters.getProperty( name ) ;
        }

        public boolean isActive()
        {
            return false ;
        }
    }   

    // This applet needs to support getDocumentBase, getParameter, and getCodeBase
    private Applet makeDCApplet( Properties parameters, URL codeBase, 
        URL documentBase )
    {
        AppletStub stub = new TestAppletStub( parameters, codeBase, documentBase ) ;
        Applet result = new Applet() ;
        result.setStub( stub ) ;
        return result ;
    }

    private Properties makeDCProperties()
    {
        Properties result = new Properties() ;
        result.setProperty( ORBConstants.INITIAL_HOST_PROPERTY, "thisHost" ) ;
        result.setProperty( ORBConstants.SERVER_HOST_PROPERTY, "thatHost" ) ;
        result.setProperty( ORBConstants.ORB_INIT_REF_PROPERTY, 
            "NameService=corbaloc::host.org/NameService" ) ;
        result.setProperty( "foo.ORBarg1", "MyFoo" ) ;
        result.setProperty( "foo.prefix.stuff1", "AnotherFoo" ) ;
        result.setProperty( "foo.prefix.somestuff", "More Foo" ) ;
        result.setProperty( ORBConstants.CORBA_PREFIX + "ORBmodule.iiopTransport", 
            "iiopTransportImpl" ) ;
        result.setProperty( ORBConstants.CORBA_PREFIX + "poa.ORBmaxhold", "351" ) ;
        result.setProperty( ORBConstants.SUN_PREFIX + "poa.ORBfoo", "ALittleFoo" ) ;
        result.setProperty( ORBConstants.SUN_PREFIX + "pool.ORBsize", "25000" ) ;
        result.setProperty( ORBConstants.SUN_PREFIX + "bar.foo", "YetAnotherFoo" ) ;
        return result ;
    }

    private Properties makeDCAppletProperties()
    {
        Properties result = new Properties() ;
        result.setProperty( ORBConstants.ORB_INIT_REF_PROPERTY, 
            "TraderService=corbaloc::host.org/TraderService" ) ;
        result.setProperty( "foo.prefix.stuff2", "2AnotherFoo" ) ;
        result.setProperty( ORBConstants.SUN_PREFIX + "pool.ORBsize", "4000" ) ;
        return result ;
    }

    private OutputStream makeFileOutputStream( String fileName ) 
    {
        try {
            File file = new File( fileName ) ;
            FileOutputStream out = new FileOutputStream( file ) ;
            return out ;
        } catch (Exception exc) {
            System.out.println( 
                "Unexpected exception in makeFileOutputStream for " 
                + fileName + ": " + exc ) ;
            return null ;
        }
    }

    private void setDCSystemProperties()
    {
        System.setProperty( "foo.prefix.still.more.stuff", "Too much Foo" ) ;
        System.setProperty( ORBConstants.SUN_PREFIX + "pool.ORBsize", "24000" ) ;
        System.setProperty( ORBConstants.CORBA_PREFIX + "ORBmodule.soapTransport", 
            "soapTransportImpl" ) ;
    }
        
    private void setDCUserORBFile()
    {
        Properties props = new Properties() ;
        props.setProperty( "foo.prefix.somestuff", "Too much Foo" ) ;
        props.setProperty( ORBConstants.SUN_PREFIX + "poa.ORBfoo", "tinyFoo" ) ;
        props.setProperty( ORBConstants.CORBA_PREFIX + "ORBmodule.localTransport", 
            "localTransportImpl" ) ;
        OutputStream os = makeFileOutputStream( System.getProperty( "user.home" ) + 
            File.separator + "orb.properties" ) ;
        try {
            props.store( os, "New ORB test properties" ) ;
        } catch (java.io.IOException exc) {
            throw new Error( "Unexpected exception", exc ) ;
        }
    }

    private void setDCSystemORBFile()
    {
        Properties props = new Properties() ;
        props.setProperty( "foo.prefix.somestuff", "Even More Foo" ) ;
        props.setProperty( ORBConstants.CORBA_PREFIX + "poa.ORBmaxhold", "35144" ) ;
        props.setProperty( ORBConstants.CORBA_PREFIX + "ORBmodule.localTransport", 
            "localTransportStdImpl" ) ;
        OutputStream os = makeFileOutputStream( System.getProperty( "java.home" ) + 
            File.separator + "lib" + File.separator + "orb.properties" ) ;
        if (os != null) {
            try {
                props.store( os, "New ORB test properties" ) ;
            } catch (java.io.IOException exc) {
                throw new Error( "Unexpected exception", exc ) ;
            }
        } else {
            noJavaHomeAvailable = true ;
        }
    }

    // Common DataCollector tests:
    // getProperties raises IllegalStateException before setParser call
    // setParser may be called more than once
    // getProperties returns correct properties after setParser call
    // Testing properties in result:
    //      INITIAL_HOST_PROPERTY and INITIAL_SERVER_PROPERTY defaults
    //      -ORB arguments are converted to matching properties
    //      URL properties for applets are made absolute
    //      ORB_INIT_REF_PROPERTY correctly converted to properties
    //      Only corba properties come from system properties
    // Correct overriding:
    //      applet case:
    //          props then applet
    //      normal case:
    //          system, props, <java.home>/orb.properties,
    //              <user.home>/orb.properties, finally args
    // Verify that intersection of parser prop names are present in
    //      result iff they are available in data.

    private void testDataCollectorState( String name, PropertyParser parser,
        final DataCollector dc, boolean expectedAppletResult, 
        Properties expectedProperties )
    {
        NullaryFunction<Object> isAppletNullaryFunction =
            new NullaryFunction<Object>() {
            public Object evaluate() {
                return Boolean.valueOf( dc.isApplet() ) ;
            }
        } ;

        session.testForPass( name + "isApplet", isAppletNullaryFunction,
            Boolean.valueOf( expectedAppletResult ) ) ;

        NullaryFunction<Object> getPropertiesNullaryFunction =
            new NullaryFunction<Object>() {
            public Object evaluate() {
                return dc.getProperties() ;
            }
        } ;

        session.testForException( name + "getProperties before setParser", 
            getPropertiesNullaryFunction, IllegalStateException.class ) ;

        dc.setParser( parser ) ;

        session.testForPass( name + "getProperties after setParser", 
            getPropertiesNullaryFunction, expectedProperties ) ;
    }

    private void setupDCEnvironment()
    {
        setDCSystemProperties() ;
        setDCUserORBFile() ;
        setDCSystemORBFile() ;
    }

    private void testNormalDataCollector() 
    {
        session.start( "Normal DataCollector" ) ;

        String[] args = null ;
        DataCollector dc1 = DataCollectorFactory.create( args, null,
            "MyHost" ) ;
        PropertyParser parser = new PropertyParser() ;
        Properties props = new Properties() ;
        props.setProperty( ORBConstants.INITIAL_HOST_PROPERTY, "MyHost" ) ;
        testDataCollectorState( "1: no data, verify results: ", parser,
            dc1, false, props ) ;

        args = makeDCArgs() ;
        PropertyParser parser2 = makeDCParser() ;
        Properties parameters = makeDCProperties() ;
        DataCollector dc2 = DataCollectorFactory.create( args, parameters, "MyHost" ) ;
        props = makeDCNormalResult() ;
        testDataCollectorState( "2: no data, verify results: ", parser2,
            dc2, false, props ) ;

        session.end() ;
    }

// Expected results (in override order: later overrides earlier):
// From setDCSystemProperties: (not in applet mode)
//      "foo.prefix.still.more.stuff", "Too much Foo" (not allowed, since this is a std prefix!)
//      ORBConstants.SUN_LC_PREFIX + "pool.ORBsize", "24000" 
// From system file:
//      "foo.prefix.somestuff", "Even More Foo" 
//      ORBConstants.ORG_OMG_CORBA_PREFIX + "poa.ORBmaxhold", "35144" 
// From user home dir file:
//      "foo.prefix.somestuff", "Too much Foo" 
//      ORBConstants.SUN_PREFIX + "poa.ORBfoo", "tinyFoo" 
// From makeDCProperties():
//      ORBConstants.INITIAL_HOST_PROPERTY, "thisHost" 
//      ORBConstants.SERVER_HOST_PROPERTY, "thatHost" 
//      ORBConstants.ORB_INIT_REF_PROPERTY, "NameService=corbaloc::host.org/NameService" 
//      "foo.ORBarg1", "MyFoo" 
//      "foo.prefix.stuff1", "AnotherFoo" 
//      "foo.prefix.somestuff", "More Foo" 
//      ORBConstants.ORG_OMG_CORBA_PREFIX + "poa.ORBmaxhold", "351" 
//      ORBConstants.SUN_PREFIX + "poa.ORBfoo", "ALittleFoo" 
//      ORBConstants.SUN_LC_PREFIX + "pool.ORBsize", "25000" 
//      ORBConstants.SUN_LC_VERSION_PREFIX + "bar.foo", "YetAnotherFoo" 
// From makeDCApplet( makeDCAppletProperties(), new URL( "http://www.bar.com" ),
//      new URL( "http://www.foo.com" ) ):
//      ORBConstants.ORB_INIT_REF_PROPERTY, "TraderService=corbaloc::host.org/TraderService" 
//      "foo.prefix.stuff2", "2AnotherFoo" 
//      ORBConstants.SUN_LC_PREFIX + "pool.ORBsize", "4000" ;
// From args (normal case only):
//      ORBConstants.ORG_OMG_CORBA_PREFIX + "poa.ORBmaxhold", "27"
//      ORBConstants.ORB_INIT_REF_PROPERTY.FooService, "corbaloc::host.org/FooService" 
//      ORBConstants.ORB_INIT_REF_PROPERTY.BarService, "corbaloc::host.org/BarService" 
// 
// Expected result from the above (applet case):
//      ORBConstants.ORB_INIT_REF_PROPERTY.TraderService, "corbaloc::host.org/TraderService" 
//      "foo.prefix.stuff2", "2AnotherFoo" 
//      ORBConstants.SUN_LC_PREFIX + "pool.ORBsize", "4000" 
//      ORBConstants.ORB_INIT_REF_PROPERTY.NameService, "corbaloc::host.org/NameService" 
//      "foo.ORBarg1", "MyFoo" 
//      "foo.prefix.stuff1", "AnotherFoo" 
//      "foo.prefix.somestuff", "More Foo" 
//      ORBConstants.ORG_OMG_CORBA_PREFIX + "poa.ORBmaxhold", "351" 
//      ORBConstants.SUN_PREFIX + "poa.ORBfoo", "ALittleFoo" 
//      ORBConstants.SUN_LC_VERSION_PREFIX + "bar.foo", "YetAnotherFoo" 
//      ORBConstants.INITIAL_HOST_PROPERTY, "thisHost" 
//      ORBConstants.SERVER_HOST_PROPERTY, "thatHost" 
//
// Expected result from the above (normal case):
//      ORBConstants.ORG_OMG_CORBA_PREFIX + "poa.ORBmaxhold", "27"
//      ORBConstants.ORB_INIT_REF_PROPERTY.FooService, "corbaloc::host.org/FooService" 
//      ORBConstants.ORB_INIT_REF_PROPERTY.BarService, "corbaloc::host.org/BarService" 
//      ORBConstants.SUN_LC_PREFIX + "pool.ORBsize", "25000" 
//      ORBConstants.ORB_INIT_REF_PROPERTY.NameService, "corbaloc::host.org/NameService" 
//      "foo.ORBarg1", "MyFoo" 
//      "foo.prefix.stuff1", "AnotherFoo" 
//      "foo.prefix.somestuff", "More Foo" 
//      ORBConstants.ORG_OMG_CORBA_PREFIX + "poa.ORBmaxhold", "351" 
//      ORBConstants.SUN_PREFIX + "poa.ORBfoo", "ALittleFoo" 
//      ORBConstants.SUN_LC_VERSION_PREFIX + "bar.foo", "YetAnotherFoo" 
//      ORBConstants.INITIAL_HOST_PROPERTY, "thisHost" 
//      ORBConstants.SERVER_HOST_PROPERTY, "thatHost" 

    private Properties makeDCAppletResult()
    {
        Properties props = new Properties() ;
        props.setProperty( "org.omg.CORBA.ORBInitRef.TraderService", 
            "corbaloc::host.org/TraderService" ) ;
        props.setProperty( ORBConstants.SUN_PREFIX + "pool.ORBsize", "4000" ) ;
        props.setProperty( "org.omg.CORBA.ORBInitRef.NameService", "corbaloc::host.org/NameService" ) ;
        props.setProperty( "foo.ORBarg1", "MyFoo" ) ;
        props.setProperty( "foo.prefix.stuff1", "AnotherFoo" ) ;
        props.setProperty( "foo.prefix.somestuff", "More Foo" ) ;
        props.setProperty( "org.omg.CORBA.poa.ORBmaxhold", "351" ) ;
        props.setProperty( ORBConstants.SUN_PREFIX + "poa.ORBfoo", "ALittleFoo" ) ;
        props.setProperty( "com.sun.corba.ee.bar.foo", "YetAnotherFoo" ) ;
        props.setProperty( "org.omg.CORBA.ORBInitialHost", "thisHost" ) ;
        props.setProperty( "com.sun.corba.ee.ORBServerHost", "thatHost" ) ;
        props.setProperty( ORBConstants.CORBA_PREFIX + "ORBmodule.iiopTransport", 
            "iiopTransportImpl" ) ;
        props.setProperty( ORBConstants.CORBA_PREFIX + "ORBmodule.localTransport", 
            "localTransportImpl" ) ;
        return props ;
    }

    private Properties makeDCNormalResult()
    {
        Properties props = new Properties() ;
        props.setProperty( ORBConstants.CORBA_PREFIX + "poa.ORBmaxhold", "27" ) ;
        props.setProperty( ORBConstants.ORB_INIT_REF_PROPERTY + ".FooService", 
            "corbaloc::host.org/FooService" ) ;
        props.setProperty( ORBConstants.ORB_INIT_REF_PROPERTY + ".BarService", 
            "corbaloc::host.org/BarService" ) ;
        props.setProperty( "com.sun.corba.ee.pool.ORBsize", "25000" ) ;
        props.setProperty( "org.omg.CORBA.ORBInitRef.NameService", "corbaloc::host.org/NameService" ) ;
        props.setProperty( "foo.ORBarg1", "MyFoo" ) ;
        props.setProperty( "foo.prefix.stuff1", "AnotherFoo" ) ;
        props.setProperty( "foo.prefix.somestuff", "More Foo" ) ;
        props.setProperty( "org.omg.CORBA.poa.ORBmaxhold", "351" ) ;
        props.setProperty( "com.sun.corba.ee.poa.ORBfoo", "ALittleFoo" ) ;
        props.setProperty( "com.sun.corba.ee.bar.foo", "YetAnotherFoo" ) ;
        props.setProperty( "org.omg.CORBA.ORBInitialHost", "thisHost" ) ;
        props.setProperty( "com.sun.corba.ee.ORBServerHost", "thatHost" ) ;
        props.setProperty( ORBConstants.CORBA_PREFIX + "ORBmodule.iiopTransport", 
            "iiopTransportImpl" ) ;
        props.setProperty( ORBConstants.CORBA_PREFIX + "ORBmodule.soapTransport", 
            "soapTransportImpl" ) ;
        props.setProperty( ORBConstants.CORBA_PREFIX + "ORBmodule.localTransport", 
            "localTransportImpl" ) ;
        return props ;
    }

    private void testAppletDataCollector() 
    {
        session.start( "Applet DataCollector" ) ;

        Properties appProps = new Properties() ;
        URL codeBase ;
        URL documentBase ;
        try {
            codeBase = new URL( "http://www.bar.com" ) ;
            documentBase = new URL( "http://www.foo.com" ) ;
        } catch (MalformedURLException exc) {
            throw new Error( "Unexpected Exception", exc ) ;
        }

        Applet app = makeDCApplet( appProps, codeBase, documentBase ) ; 

        DataCollector dc1 = DataCollectorFactory.create( app, null,
            "MyHost" ) ;
        PropertyParser parser = new PropertyParser() ;
        Properties props = new Properties() ;
        props.setProperty( ORBConstants.INITIAL_HOST_PROPERTY, "www.bar.com" ) ;
        testDataCollectorState( "1: no data, verify results: ", parser,
            dc1, true, props ) ;

        appProps = makeDCAppletProperties() ;
        app = makeDCApplet( appProps, codeBase, documentBase ) ;
        PropertyParser parser2 = makeDCParser() ;
        Properties parameters = makeDCProperties() ;
        DataCollector dc2 = DataCollectorFactory.create( app, parameters, "MyHost" ) ;
        props = makeDCAppletResult() ;
        testDataCollectorState( "2: no data, verify results: ", parser2,
            dc2, true, props ) ;

        session.end() ;
    }

    private Set makeSetFromArray( Object array )
    {
        Set result = new HashSet() ;
        if (array != null) {
            if (!array.getClass().isArray())
                throw new Error( "makeSetFromArray called with non-array argument" ) ;

            int size = Array.getLength( array ) ;
            for (int ctr=0; ctr<size; ctr++ ) {
                Object element = Array.get( array, ctr ) ;
                result.add( element ) ;
            }
        }

        return result ;
    }

    private boolean compareArraysAsSets( Object obj1, Object obj2 ) 
    {
        Set set1 = makeSetFromArray( obj1 ) ;
        Set set2 = makeSetFromArray( obj2 ) ;
        return ObjectUtility.equals( set1, set2 ) ;
    }

    // Check that two objects with the same interface return equal results
    // on all public methods that take no arguments.  If the result type 
    // of an accessor method is an array, we compare as sets, rather than
    // assuming that the order must be the same.  Also, ignore all methods
    // inherited from java.lang.Object.
    private boolean equalByAccessorMethods( Object obj1, Object obj2 ) 
    {
        ObjectUtility objutil = ObjectUtility.make( false, true, 5, 4 ) ;
        boolean result = true ;
        if (obj1.getClass() != obj2.getClass())
            throw new Error( 
                "equalByAccessorMethods can only be used for objects " +
                "of the same class" ) ;

        try {
            Class cls = obj1.getClass() ;
            Method[] publicMethods = cls.getMethods() ;
            for (int ctr=0; ctr<publicMethods.length; ctr++ ) {
                Method method = publicMethods[ctr] ;
                String name = method.getName() ;
                if (!method.getDeclaringClass().equals( Object.class ) &&
                    method.getParameterTypes().length == 0) {
                    Object value1 = method.invoke( obj1 ) ;
                    Object value2 = method.invoke( obj2 ) ;

                    boolean comparison ;
                    if (method.getReturnType().isArray())
                        comparison = compareArraysAsSets( value1, value2 ) ;
                    else
                        comparison = ObjectUtility.equals( value1, value2 ) ;

                    if (!comparison) {
                        System.out.println( 
                            "        Objects are not equal by accessor method " +
                                name + ":" ) ;
                        System.out.println( "            Value1 = \n" + 
                            objutil.objectToString( value1 ) + "\n" ) ;
                        System.out.println( "            Value2 = \n" + 
                            objutil.objectToString( value2 ) + "\n" ) ;
                        result = false ;        
                    }
                }
            }
        } catch (Exception exc) {
            throw new Error( "Error in reflective invocation", exc ) ;
        }

        return result ;
    }

    private boolean compareORBDataByInterface( ORBData od1, ORBData od2 ) 
    {
        return equalByAccessorMethods( od1, od2 ) &&
            (od1.getGIOPBuffMgrStrategy( GIOPVersion.V1_0 ) ==
                od2.getGIOPBuffMgrStrategy( GIOPVersion.V1_0 )) && 
            (od1.getGIOPBuffMgrStrategy( GIOPVersion.V1_1 ) ==
                od2.getGIOPBuffMgrStrategy( GIOPVersion.V1_1 )) && 
            (od1.getGIOPBuffMgrStrategy( GIOPVersion.V1_2 ) ==
                od2.getGIOPBuffMgrStrategy( GIOPVersion.V1_2 )) ; 
    }

    // Make properties for testing ORBData
    private Properties makeORBDataProperties() 
    {
        ParserData[] data = ParserTable.get(ORB.defaultClassNameResolver()).getParserData() ;
        Properties result = new Properties() ;
        for (int ctr=0; ctr<data.length; ctr++ )
            data[ctr].addToProperties( result ) ;
        return result ;
    }

    public static class PropertyDataCollector implements DataCollector
    {
        private Properties props ;

        public PropertyDataCollector( Properties props ) 
        {
            this.props = props ;
        }

        public boolean isApplet() { return false ; }

        public boolean initialHostIsLocal() { return false ; }

        public void setParser( PropertyParser parser ) { }

        public Properties getProperties() { return props ; }
    }

    private void testORBData() 
    {
        session.start( "ORBData" ) ;

        ORB orb = (ORB)ORB.init() ;

        Properties props = makeORBDataProperties() ;
        final ORBDataParserImpl od1 = new ORBDataParserImpl( orb, 
            new PropertyDataCollector( props ) ) ;
        od1.setTestValues() ;

        // Print out the test values 
        System.out.println( "od1 = " + 
            ObjectUtility.make( true, true ).objectToString( od1 ) ) ;

        final ORBDataParserImpl od2 = new ORBDataParserImpl( orb, 
            new PropertyDataCollector( props ) ) ;

        NullaryFunction<Object>  closure = new NullaryFunction<Object> () {
            public Object evaluate() {
                if (compareORBDataByInterface( od1, od2)) 
                    return Boolean.TRUE ;
                else {
                    System.out.println( "ORBData comparison failed." ) ;
                    // This is handled in the closure.
                    // System.out.println( "od1=" +
                        // ObjectUtility.defaultObjectToString( od1 ) ) ;
                    // System.out.println( "od2=" +
                        // ObjectUtility.defaultObjectToString( od2 ) ) ;
                    return Boolean.FALSE ;
                }
            }
        } ;

        session.testForPass( "ORBData parsing test", closure, 
            Boolean.TRUE ) ;

        session.end() ;
    }

    private void testUserConfigurator()
    {
        session.start( "UserConfigurator" ) ;

        Properties props = new Properties() ;
        props.put( ORBConstants.SUN_PREFIX + "ORBUserConfigurators." + 
            "corba.orbconfig.MyConfigurator", "1" ) ;
        String[] args = null ;
        ORB.init( args, props ) ;
        NullaryFunction<Object> closure = new NullaryFunction<Object>() {
            public Object evaluate() {
                return Boolean.valueOf( MyConfigurator.wasCalled ) ;
            }
        } ;

        session.testForPass( "UserConfigurator", closure, Boolean.TRUE ) ;

        session.end() ;
    }

    private void testORBServerHostAndListenOnAllInterfaces()
    {
        // three things to test here:
        // 1. ORBServerHost default value and 'listen on all interfaces' default
        //    Expected result is the IP address of the machine where the test is
        //    being run and listen on all interfaces to be true.
        // 2. ORBServer host set in a property and 'listen on all interfaces'
        //    Expected result is host as set in property and listen on all
        //    interfaces set to false.
        // 3. Verify DataCollector calls parser's complete() method only one
        //    time.

        System.out.println("\tTest ORBServerHost and Listen on all interfaces");

        // Case 1
        ORB orb = (ORB)ORB.init();
        String[] args = null ;
        DataCollector dc = DataCollectorFactory.create(args, null, "MyHost");
        ORBDataParserImpl od1 = new ORBDataParserImpl(orb, dc);
        String expected_ip;
        try
        {
            expected_ip =  InetAddress.getLocalHost().getHostAddress();
        }
        catch (UnknownHostException uhe)
        {
            String msg = "testORBServerHostAndListenOnAllInterfaces FAILED" +
                         " default ORBServerHost test, failed to get IP address: " +
                         uhe.toString();
            throw new Error(msg);
        }

        if (!expected_ip.equals(od1.getORBServerHost()))
        {
            String msg = "testORBServerHostAndListenOnAllInterfaces FAILED" +
                         " default ORBServerHost test, getORBServerHost() != " + 
                         expected_ip;
            throw new Error(msg);
        }
        if (!od1.getListenOnAllInterfaces())
        {
            String msg = "testORBServerHostAndListenOnAllInterfaces FAILED" +
                         " default getListenOnAllInterfaces test";
            throw new Error(msg);
        }

        // Case 2

        // if ORBServerHost is set to "0.0.0.0", then listen on all interfaces
        // should be true
        Properties props = new Properties();
        String allHosts = "0.0.0.0";
        props.setProperty(ORBConstants.SERVER_HOST_PROPERTY, allHosts);
        dc = DataCollectorFactory.create(args, props, "MyHost");
        od1 = new ORBDataParserImpl(orb, dc);
        if (!expected_ip.equals(od1.getORBServerHost()))
        {
            String msg = "testORBServerHostAndListenOnAllInterfaces FAILED" +
                         " ORBServerHost property = " + allHosts + 
                         " test, getORBServerHost() != " + expected_ip;
            throw new Error(msg);
        }
        if (!od1.getListenOnAllInterfaces())
        {
            String msg = "testORBServerHostAndListenOnAllInterfaces FAILED" +
                         " ORBServerHost property = " + allHosts +
                         " test, listenOnAllInterfaces should be true";
            throw new Error(msg);
        }

        // if ORBServerHost is set to a specific host, then listen on all
        // interfaces should be false
        props = new Properties();
        String expectedHost = "thatHost";
        props.setProperty(ORBConstants.SERVER_HOST_PROPERTY, expectedHost);
        dc = DataCollectorFactory.create(args, props, "MyHost");
        od1 = new ORBDataParserImpl(orb, dc);
        if (!expectedHost.equals(od1.getORBServerHost()))
        {
            String msg = "testORBServerHostAndListenOnAllInterfaces FAILED" +
                         " ORBServerHost property = " + expectedHost + 
                         " test, getORBServerHost() != " + expectedHost +
                         " getORBServerHost = " + od1.getORBServerHost();
            throw new Error(msg);
        }
        if (od1.getListenOnAllInterfaces())
        {
            String msg = "testORBServerHostAndListenOnAllInterfaces FAILED" +
                         " ORBServerHost property = " + expectedHost +
                         " test, listenOnAllInterfaces should be false";
            throw new Error(msg);
        }

        // case 3
        // Verify DataCollector calls parser's complete() only once.
        SimpleParser simpleParser = new SimpleParser(dc);
        try
        {
            simpleParser = new SimpleParser(dc);
        }
        catch (IllegalStateException ise)
        {
            String msg = "testORBServerHostAndListenOnAllInterfaces FAILED" +
                          ise.toString();
            throw new Error(msg);
        }

        if (!simpleParser.errorIfCalledTooManyTimes())
        {
            String msg = "testORBServerHostAndListenOnAllInterfaces FAILED" +
                         " SimpleParser.complete() called too many times";
            throw new Error(msg);
        }

        System.out.println("\t\tPASSED");
     }

    // A simple parser that ensures the parser framework will
    // call the overriden complete() method at most one time.
    private static class SimpleParser extends ParserImplBase
    {
        private int numberOfCompleteMethodInvokes;
        private DataCollector itsDataCollector;

        public SimpleParser(DataCollector dataCollector)
        {
            numberOfCompleteMethodInvokes = 0;
            // this will force a call to this.complete()
            init(dataCollector);
        }

        public PropertyParser makeParser()
        {
            PropertyParser parser = new PropertyParser() ;
            return parser ;
        }

        public void complete()
        {
            if (++numberOfCompleteMethodInvokes > 1)
            {
                throw new IllegalStateException("complete() called too many times" +
                                                 numberOfCompleteMethodInvokes);
            }
        }

        public boolean errorIfCalledTooManyTimes()
        {
            boolean result = false;
            try
            {
                this.complete();
            }
            catch (IllegalStateException ise)
            {
                System.out.println("\t\tIllegalStateException thrown as expected...");
                result = true;
            }
            return result;
        }
    }
}
