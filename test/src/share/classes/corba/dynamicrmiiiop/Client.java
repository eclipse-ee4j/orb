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

package corba.dynamicrmiiiop  ;

import java.rmi.Remote ;

import java.io.Serializable ;
import java.io.Externalizable ;

import javax.rmi.CORBA.Tie ;

import org.omg.CORBA.SystemException ;
import org.omg.CORBA.portable.ApplicationException ;
import org.omg.CORBA.portable.ResponseHandler ;
import org.omg.CORBA.portable.UnknownException ;
import org.omg.CORBA_2_3.portable.InputStream ;
import org.omg.CORBA_2_3.portable.OutputStream ;

import java.util.Set ;
import java.util.HashSet ;
import java.util.Properties ;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException ;

import junit.framework.TestCase ;
import junit.framework.Test ;
import junit.framework.TestResult ;
import junit.framework.TestSuite ;

import com.sun.corba.ee.spi.orb.ORB ;

import com.sun.corba.ee.spi.presentation.rmi.PresentationManager ;
import com.sun.corba.ee.spi.presentation.rmi.DynamicMethodMarshaller ;
import com.sun.corba.ee.spi.presentation.rmi.IDLNameTranslator ;
import com.sun.corba.ee.spi.presentation.rmi.StubAdapter;

import com.sun.corba.ee.spi.misc.ORBConstants ;

import com.sun.corba.ee.impl.presentation.rmi.IDLType ;

import corba.dynamicrmiiiop.testclasses.A ;
import corba.dynamicrmiiiop.testclasses.B ;
import corba.dynamicrmiiiop.testclasses.C ;
import corba.dynamicrmiiiop.testclasses.D ;
import corba.dynamicrmiiiop.testclasses.E ;
import corba.dynamicrmiiiop.testclasses.F ;
import corba.dynamicrmiiiop.testclasses.G ;
import corba.dynamicrmiiiop.testclasses.H ;
import corba.dynamicrmiiiop.testclasses.C1 ;
import corba.dynamicrmiiiop.testclasses.C2 ;
import corba.dynamicrmiiiop.testclasses.C3 ;
import corba.dynamicrmiiiop.testclasses.C4 ;
import corba.dynamicrmiiiop.testclasses.C5 ;
import corba.dynamicrmiiiop.testclasses.C6 ;

import corba.dynamicrmiiiop.testclasses.TieTest ;
import corba.dynamicrmiiiop.testclasses.TieTestImpl ;
import corba.dynamicrmiiiop.testclasses.DMMImplTestClasses ;

import com.sun.corba.ee.impl.presentation.rmi.ExceptionHandlerImpl ;

// Included to directly test makeReaderWriter, which is not
// needed in the PresentationManager API.
import com.sun.corba.ee.impl.presentation.rmi.DynamicMethodMarshallerImpl ;

import com.sun.corba.ee.impl.util.RepositoryId ;
import org.glassfish.pfl.basic.graph.Graph;
import org.glassfish.pfl.basic.graph.GraphImpl;
import org.glassfish.pfl.basic.graph.Node;
import org.glassfish.pfl.dynamic.copyobject.spi.LibraryClassLoader;
import org.glassfish.pfl.test.TestCaseTools;

public class Client extends TestCase
{
    private static final boolean DEBUG = false ;
    // private static final int REP_COUNT = 1000 ;
    // private List<TimedTest> timedTests ;
 
    public static void main( String[] args ) 
    {
        Client root = new Client() ;
        TestResult result = junit.textui.TestRunner.run(root.suite()) ;

        // reportTiming( System.out, root.timedTests ) ;

        if (result.errorCount() + result.failureCount() > 0) {
            System.out.println( "Error: failures or errrors in JUnit test" ) ;
            System.exit( 1 ) ;
        } else
            System.exit( 0 ) ;
    }

    public Client()
    {
        super() ;
        // timedTests = new ArrayList<TimedTest>() ;
    }

    public Client( String name )
    {
        super( name ) ;
        // timedTests = null ;
    }

    public static Test suite()
    {
        System.out.println( 
            "==============================================================\n" +
            "Testing Dynamic RMI-IIOP\n" +
            "==============================================================\n" 
        ) ;

        TestSuite ts = (TestSuite)TestCaseTools.makeTestSuite( Client.class ) ;

        // Add the Codegen ProxyCreator test suite if it is available.
        // It is part of the optional ORB build.
        String testName = "corba.dynamicrmiiiop.TestCodegenProxyCreator" ;
        try {
            Class cls = LibraryClassLoader.loadClass( testName ) ;
            ts.addTest( (TestSuite)TestCaseTools.makeTestSuite( cls ) ) ;
        } catch (ClassNotFoundException exc) {
            // Test not available: no op
            System.out.println( testName + " test is not available." ) ;
        }

        return ts ;
    }

    // This is provided to prevent JUnit from complaining that Client 
    // does not contain tests.
    public void testDummy()
    {
        assertTrue( true ) ;
    }

    public void testExceptionNameMangling()
    {
        Class testClass = 
            corba.dynamicrmiiiop.testclasses.exception.TestException.class ;
        String expectedId = "IDL:corba/dynamicrmiiiop/testclasses/_exception/TestEx:1.0" ;

        ExceptionHandlerImpl eh = new ExceptionHandlerImpl( 
            new Class[0] ) ;
        ExceptionHandlerImpl.ExceptionRW erw = 
            eh.getRMIExceptionRW( testClass ) ;
        assertEquals( expectedId, erw.getId() ) ;
    }

    public static boolean equalOrNull( Object obj1, Object obj2 )
    {
        if (obj1 == null)
            return obj2 == null ;
        else
            return obj1.equals( obj2 ) ;
    }

    public static void sameException( Object obj1, Object obj2 )
    {
        if ((obj1 == null) || (obj2 == null)) {
            if (obj1 == obj2)
                return ;
            else 
                fail( "Objects not the same: obj1 = " + obj1 +
                    " obj2 = " + obj2 ) ;
        }

        if (!obj1.getClass().equals( obj2.getClass() )) {
            fail( "Objects have different classes: obj1 = " + obj1 +
                " obj2 = " + obj2 ) ;

            return ;
        }
        
        if (!(obj1 instanceof Throwable) || !(obj2 instanceof Throwable)) {
            fail( "Objects are not both throwable: obj1 = " + obj1 +
                " obj2 = " + obj2 ) ;

            return ;
        }

        Throwable thr1 = (Throwable)obj1 ;
        Throwable thr2 = (Throwable)obj2 ;

        /* Don't care about matching messages: that's up to the logex code.
        boolean sameMessage = equalOrNull( thr1.getMessage(), thr2.getMessage() ) ;
        if (!sameMessage) {
            fail( "thr1 and thr2 do not have the same message: thr1 message = " 
                + thr1.getMessage() + " thr2 message = " + thr2.getMessage() ) ;
        }
        */
            
        sameException( thr1.getCause(), thr2.getCause() ) ;

        if (thr1 instanceof UnknownException) {
            UnknownException unk1 = (UnknownException)thr1 ;
            UnknownException unk2 = (UnknownException)thr2 ;

            sameException( unk1.originalEx, unk2.originalEx ) ;
        } 
        
        if (thr1 instanceof SystemException) {
            SystemException sys1 = (SystemException)thr1 ;
            SystemException sys2 = (SystemException)thr2 ;

            if (sys1.minor != sys2.minor)
                fail( "sys1 and sys2 do not have the same minor code: sys1 mc = "
                    + sys1.minor + " sys2 mc = " + sys2.minor ) ;

            int cs1 = sys1.completed.value() ;
            int cs2 = sys2.completed.value() ;

            if (cs1 != cs2)
                fail( "sys1 and sys2 do not have the same completion status: cs1 = " 
                    + cs1 + " cs2 = " + cs2 ) ;
        }
    }

    public static class ORBInitTestSuite extends TestCase
    {
        /** A simple class that allows checking of the result of a 
         * computation both in the current thread and in a new thread.
         */
        public static abstract class ThreadableTest implements Runnable
        {
            private Object expected ;
            private Object actual ;
            private String name ;

            public ThreadableTest( String name, Object expected )
            {
                this.name = name ;
                this.expected = expected ;
            }

            abstract public void run() ;
    
            protected void setActual( Object actual ) 
            {
                this.actual = actual ;
            }

            private void result()
            {
                assertSame( name, expected, actual ) ;
            }

            public void testSameThread()
            {
                actual = null ;
                run() ;
                result() ;
            }

            public void testDifferentThread()
            {
                actual = null ;
                Thread thr = new Thread( this ) ;
                thr.start() ;

                boolean done = false ;
                while (!done) {
                    try {
                        thr.join() ; 
                        done = true ;
                    } catch (InterruptedException exc) {
                        // NO-OP: just retry the join call
                    }
                }

                result() ;
            }
        }

        public static class TestStubFactoryFactoryType extends ThreadableTest 
        {
            public TestStubFactoryFactoryType( boolean expected )
            {
                super( "StubFactoryFactory", Boolean.valueOf( 
                    expected ) ) ;
            }

            public void run()
            {
                setActual( Boolean.valueOf( 
                    ORB.getStubFactoryFactory().
                        createsDynamicStubs() ) ) ;
            }
        }

        public ORBInitTestSuite()
        {
            super() ;
        }

        public ORBInitTestSuite( String name ) 
        {
            super( name ) ;
        }

        /* Tests:
         * create ORB with dynamic stubs, verify get dynamic stubs from
         *  chooser.
         * init ORBSingleton, verify no affect on chooser.
         *  chooser.
         */

        ORB makeORB( boolean useDynamic ) 
        {
            Properties props = new Properties() ;
            String[] args = null ;
            props.setProperty( ORBConstants.USE_DYNAMIC_STUB_PROPERTY,
                Boolean.toString( useDynamic ) ) ;
            return (ORB)ORB.init( args, props ) ;
        }

        public void testNull()
        {
            // no-op to keep junit happy
        }

/* Unfortunately these test do not work, because we cannot control
   the ORB type exactly here, and setting the global presentation manager
   can only happen once.  We'll omit these for now.

        public void testDynamicORB() 
        {
            ORB orb = makeORB( true ) ;
            ThreadableTest tt2 = new TestStubFactoryFactoryType( 
                true ) ;
            tt2.testSameThread() ;
            tt2.testDifferentThread() ;
        }

        public void testSingletonORB()
        {
            ORB orb = makeORB( true ) ;
            ORB sorb = (ORB)ORB.init() ;
            ThreadableTest tt2 = new TestStubFactoryFactoryType( 
                true ) ;
            tt2.testSameThread() ;
            tt2.testDifferentThread() ;
        }
*/
    }

    public static class GraphTestSuite extends TestCase
    {
        public class NodeTestImpl implements Node
        {
            private String name ;
            private Set children ;

            public NodeTestImpl( String name ) 
            {
                this.name = name ;
                children = new HashSet() ;
            }

            public void addChild( NodeTestImpl node )
            {
                children.add( node ) ;
            }

            @Override
            public String toString()
            {
                return "NodeTestImpl[" + name + "]" ;
            }

            @Override
            public boolean equals( Object obj ) 
            {
                if (this == obj)
                    return true ;

                if (!(obj instanceof NodeTestImpl))
                    return false ;

                NodeTestImpl other = (NodeTestImpl)obj ;

                return name.equals( other.name ) ;
            }

            @Override
            public int hashCode()
            {
                return name.hashCode() ;
            }

            public Set getChildren() 
            {
                return children ;
            }
        }

        public GraphTestSuite()
        {
            super() ;
        }

        public GraphTestSuite( String name ) 
        {
            super( name ) ;
        }

        public void testAdd1()
        {
            NodeTestImpl a = new NodeTestImpl( "A" ) ;
            Set expected = new HashSet() ;
            expected.add( a ) ;

            Graph g = new GraphImpl() ;
            g.add( a ) ;
            assertTrue( g.equals( expected ) ) ;
        }

        public void testAdd2()
        {
            NodeTestImpl a = new NodeTestImpl( "A" ) ;
            NodeTestImpl b = new NodeTestImpl( "B" ) ;
            Set expected = new HashSet() ;
            expected.add( a ) ;
            expected.add( b ) ;

            Graph g = new GraphImpl() ;
            g.add( a ) ;
            g.add( b ) ;
            assertTrue( g.equals( expected ) ) ;
        }

        public void testAddSame()
        {
            NodeTestImpl a = new NodeTestImpl( "A" ) ;
            NodeTestImpl b = new NodeTestImpl( "B" ) ;
            Set expected = new HashSet() ;
            expected.add( a ) ;
            expected.add( b ) ;

            Graph g = new GraphImpl() ;
            g.add( a ) ;
            g.add( b ) ;
            g.add( a ) ;
            assertTrue( g.equals( expected ) ) ;
        }

        public void testNullGraph()
        {
            Graph g = new GraphImpl() ;
            Set roots = g.getRoots() ;
            assertTrue( roots.isEmpty()) ;
        }

        public void testSingeltonGraph()
        {
            NodeTestImpl a = new NodeTestImpl( "A" ) ;
            Set expected = new HashSet() ;
            expected.add( a ) ;

            Graph g = new GraphImpl() ;
            g.add( a ) ;
            Set roots = g.getRoots() ;
            assertTrue( expected.equals( roots ) ) ;
        }

        public void testLinearGraph()
        {
            NodeTestImpl a = new NodeTestImpl( "A" ) ;
            NodeTestImpl b = new NodeTestImpl( "B" ) ;
            a.addChild( b ) ;
            Set expected = new HashSet() ;
            expected.add( a ) ;

            Graph g = new GraphImpl() ;
            g.add( b ) ;
            g.add( a ) ;
            Set roots = g.getRoots() ;
            assertTrue( expected.equals( roots ) ) ;
        }

        public void testDisconnectedLinearGraph()
        {
            NodeTestImpl a = new NodeTestImpl( "A" ) ;
            NodeTestImpl b = new NodeTestImpl( "B" ) ;
            a.addChild( b ) ;

            NodeTestImpl c = new NodeTestImpl( "C" ) ;
            NodeTestImpl d = new NodeTestImpl( "D" ) ;
            NodeTestImpl e = new NodeTestImpl( "E" ) ;
            c.addChild( d ) ;
            d.addChild( e ) ;

            Set expected = new HashSet() ;
            expected.add( a ) ;
            expected.add( c ) ;

            Graph g = new GraphImpl() ;
            g.add( a ) ;
            g.add( b ) ;
            g.add( c ) ;
            g.add( d ) ;
            g.add( e ) ;

            Set roots = g.getRoots() ;
            assertTrue( expected.equals( roots ) ) ;
        }

        public void testComplexGraph()
        {
            NodeTestImpl a = new NodeTestImpl( "A" ) ;
            NodeTestImpl b = new NodeTestImpl( "B" ) ;
            NodeTestImpl c = new NodeTestImpl( "C" ) ;
            NodeTestImpl d = new NodeTestImpl( "D" ) ;
            NodeTestImpl e = new NodeTestImpl( "E" ) ;
            NodeTestImpl f = new NodeTestImpl( "F" ) ;
            NodeTestImpl g = new NodeTestImpl( "G" ) ;
            NodeTestImpl h = new NodeTestImpl( "H" ) ;

            a.addChild( b ) ;
            c.addChild( d ) ;
            d.addChild( e ) ;
            f.addChild( d ) ;
            f.addChild( e ) ;
            g.addChild( h ) ;
            h.addChild( e ) ;
            b.addChild( h ) ;
            h.addChild( f ) ;

            Set expected = new HashSet() ;
            expected.add( a ) ;
            expected.add( c ) ;
            expected.add( g ) ;

            Graph graph = new GraphImpl() ;
            graph.add( a ) ;
            graph.add( b ) ;
            graph.add( c ) ;
            graph.add( d ) ;
            graph.add( e ) ;
            graph.add( f ) ;
            graph.add( g ) ;
            graph.add( h ) ;

            Set roots = graph.getRoots() ;
            assertTrue( expected.equals( roots ) ) ;
        }

        public void testDynamicAdd()
        {
            NodeTestImpl a = new NodeTestImpl( "A" ) ;
            NodeTestImpl b = new NodeTestImpl( "B" ) ;
            NodeTestImpl c = new NodeTestImpl( "C" ) ;
            NodeTestImpl d = new NodeTestImpl( "D" ) ;
            NodeTestImpl e = new NodeTestImpl( "E" ) ;
            NodeTestImpl f = new NodeTestImpl( "F" ) ;
            NodeTestImpl g = new NodeTestImpl( "G" ) ;
            NodeTestImpl h = new NodeTestImpl( "H" ) ;

            a.addChild( b ) ;
            c.addChild( d ) ;
            d.addChild( e ) ;
            f.addChild( d ) ;
            f.addChild( e ) ;
            g.addChild( h ) ;
            h.addChild( e ) ;
            b.addChild( h ) ;
            h.addChild( f ) ;

            Set expected = new HashSet() ;
            expected.add( a ) ;
            expected.add( c ) ;
            expected.add( g ) ;

            Graph graph = new GraphImpl() ;
            graph.add( a ) ;
            graph.add( f ) ;
            graph.add( g ) ;
            graph.add( c ) ;

            Set roots = graph.getRoots() ;
            assertTrue( expected.equals( roots ) ) ;
        }
    }

    public static class TypeIdTestSuite  extends TestCase
    {
        // Assume interfaces A to G defined as follows:
        // A extends B, C
        // B extends D
        // C extends D
        // D extends Remote
        // E extends F
        // F extends G
        // G extends Remote
        // H extends G, B

        // C1 extends Object, implements D
        // C2 extends Object, implements B
        // C3 extends Object, implements A, B
        // C4 extends C3, implements A
        // C5 extends C4, implements E
        // C6 extends C5, implements H

        public void testSingleInterface()
        {
            doTest( D.class, D.class, new Class[0] ) ;
        }

        public void testLinearInterface()
        {
            doTest( B.class, B.class, new Class[] { D.class } ) ;
        }

        public void testDiamondInterface()
        {
            doTest( A.class, A.class, new Class[] { B.class, C.class, 
                D.class } ) ;
        }

        public void testClassSingleInterface()
        {
            doTest( C1.class, D.class, new Class[0] ) ;
        }

        public void testClassLinearInterface()
        {
            doTest( C2.class, B.class, new Class[] { D.class } ) ;
        }

        public void testClassDiamondInterface()
        {
            doTest( C3.class, A.class, new Class[] { B.class, C.class, 
                D.class } ) ;
        }

        public void testClassInheritance()
        {
            doTest( C4.class, A.class, new Class[] { B.class, C.class, 
                D.class } ) ;
        }

        public void testClassMultipleInterface()
        {
            doTest( C5.class, C5.class, new Class[] { A.class, B.class, 
                C.class, D.class, E.class, F.class, G.class} ) ;
        }

        public void testClassComplexInterface()
        {
            doTest( C6.class, C6.class, new Class[] { A.class, B.class,
                C.class, D.class, E.class, F.class, G.class, H.class } ) ;
        }

        private ORB orb = null ;
        private PresentationManager pm = null ;

        private void init()
        {
            Properties props = new Properties() ;
            props.setProperty( "org.omg.CORBA.ORBClass", 
                "com.sun.corba.ee.impl.orb.ORBImpl" ) ;
            String[] args = null ;
            orb = (ORB)org.omg.CORBA.ORB.init( args, props ) ;
            pm = ORB.getPresentationManager() ;
        }

        public TypeIdTestSuite()
        {
            super() ;
            init() ;
        }

        public TypeIdTestSuite( String name ) 
        {
            super( name ) ;
            init() ;
        }

        private Set makeSet( String[] strings, int startIndex )
        {
            Set result = new HashSet() ;
            for (int ctr=startIndex; ctr<strings.length; ctr++ )
                result.add( strings[ctr] ) ;
            return result ;
        }

        private Set makeSet( Class[] classes, int startIndex )
        {
            Set result = new HashSet() ;
            for (int ctr=startIndex; ctr<classes.length; ctr++ ) {
                String str = makeTypeId( classes[ctr] ) ;
                result.add( str ) ;
            } 
            return result ;
        }

        private String makeTypeId( Class cls ) 
        {
            return RepositoryId.createForJavaType( cls ) ;
            // return "RMI:" + cls.getName() + ":0000000000000000" ;
        }

        private void doTest( Class root, Class expectedZerothId, 
            Class[] otherIds )
        {
            PresentationManager.ClassData cdata = pm.getClassData( root ) ;
            String[] typeIds = cdata.getTypeIds() ;    
            String firstId = typeIds[0] ;
            Set rest = makeSet( typeIds, 1 ) ;

            String expectedFirstId = makeTypeId( expectedZerothId ) ;
            Set expectedRest = makeSet( otherIds, 0 ) ;

            assertEquals( "firstId = " + firstId + " expectedFirstId = " + 
                expectedFirstId, firstId, expectedFirstId ) ;

            assertTrue( "rest = " + rest + " expectedRest = " + expectedRest, 
                rest.equals( expectedRest ) ) ;
        }

    }

    public static class DMMTestSuite extends TestCase
    {
        /* Test strategy:
         * Same test class as in the TieTestSuite?
         * Simple methods: 0,1 arg; 0,1 result; 
         * Echo test: for each kind of type, have an echo method that takes and
         * returns that type.  Verify that the DMM reads and writes these types
         * correctly (run the test for R/W arg/result).
         * Kinds:
         * int
         * Integer
         * byte
         * short
         * char
         * long
         * float
         * double
         * Remote
         * org.omg.CORBA.Object
         * subclass of org.omg.CORBA.Object
         * java.Object
         * java.io.Serializable
         * java.io.Externalizable
         * IDLEntity
         * HashMap (a value type)
         *
         * It is actually more important to verify that the 
         * DMM correctly assigns the right ReaderWriter to each type.
         */

        public DMMTestSuite()
        {
            super() ;
        }

        public DMMTestSuite( String name ) 
        {
            super( name ) ;
        }

        // This is provided to prevent JUnit from complaining that Client 
        // does not contain tests.
        public void testDummy()
        {
            assertTrue( true ) ;
        }
    }

    public static class DMMImplTestSuite extends TestCase
    {
        /*
         * It is actually more important to verify that the 
         * DMM correctly assigns the right ReaderWriter to each type.
         * To do this, we test DynamicMethodMarshalledImpl.makeReadWriter
         * directly to see that the correct ReaderWriter is created
         * by checking the name of the ReaderWriter.
         */

        public DMMImplTestSuite()
        {
            super() ;
        }

        public DMMImplTestSuite( String name ) 
        {
            super( name ) ;
        }

        private String makeRWName( String cname )
        {
            return "ReaderWriter[" + cname + "]" ;
        }

        private String makeRWName( String type, String cname ) 
        {
            return makeRWName( type + "(" + cname + ")" ) ;
        }

        private void doTest( Class cls, String cname )
        {
            DynamicMethodMarshallerImpl.ReaderWriter rw = 
                DynamicMethodMarshallerImpl.makeReaderWriter( cls ) ;
            assertEquals( makeRWName( cname ), rw.toString() ) ;
        }

        private void doClassTest( Class cls, String type ) 
        {
            DynamicMethodMarshallerImpl.ReaderWriter rw = 
                DynamicMethodMarshallerImpl.makeReaderWriter( cls ) ;
            assertEquals( makeRWName( type, cls.getName() ), 
                rw.toString() ) ;
        }

        public void testInt()
        {
            doTest( int.class, "int" ) ;
        }

        public void testByte()
        {
            doTest( byte.class, "byte" ) ;
        }

        public void testChar()
        {
            doTest( char.class, "char" ) ;
        }

        public void testShort()
        {
            doTest( short.class, "short" ) ;
        }

        public void testLong()
        {
            doTest( long.class, "long" ) ;
        }

        public void testFloat()
        {
            doTest( float.class, "float" ) ;
        }

        public void testDouble()
        {
            doTest( double.class, "double" ) ;
        }

        public void testBoolean()
        {
            doTest( boolean.class, "boolean" ) ;
        }

        public void testCORBAObject()
        {
            doTest( org.omg.CORBA.Object.class, 
                "org.omg.CORBA.Object" ) ;
        }

        public void testObject()
        {
            doTest( Object.class, "any" ) ;
        }

        public void testSerializable()
        {
            doTest( Serializable.class, "any" ) ;
        }

        public void testExternalizable()
        {
            doTest( Externalizable.class, "any" ) ;
        }

        public void testString()
        {
            doClassTest( String.class, "value" ) ;
        }

        public void testRemote()
        {
            doClassTest( Remote.class, "remote" ) ;
        }

        public void testAllRemote()
        {
            doTest( DMMImplTestClasses.AllRemote.class, 
                "abstract_interface" ) ;
        }

        public void testSomeRemote()
        {
            doClassTest( DMMImplTestClasses.SomeRemote.class, 
                "value" ) ;
        }

        public void testNoRemote()
        {
            doClassTest( DMMImplTestClasses.NoRemote.class, 
                "value" ) ;
        }

        public void testNoMethods()
        {
            doTest( DMMImplTestClasses.NoMethods.class, 
                "abstract_interface" ) ;
        }

        public void testIDLSimpleInterface()
        {
            doTest( DMMImplTestClasses.IDLSimpleInterface.class,
                "abstract_interface" ) ;
        }

        public void testIDLValue()
        {
            doTest( DMMImplTestClasses.IDLValue.class,
                "abstract_interface" ) ;
        }

        public void testIDLStruct()
        {
            doClassTest( DMMImplTestClasses.IDLStruct.class,
                "value" ) ;
        }

        public void testIDLInterface()
        {
            doClassTest( DMMImplTestClasses.IDLInterface.class,
                "org.omg.CORBA.Object" ) ;
        }
    }

    public static class IDLTypeTestSuite extends TestCase
    {
        /* Test that IDLType functions correctly.
        */

        private IDLType itype1 ;
        private IDLType itype2 ;
        private IDLType itype3 ;
        private IDLType itype4 ;

        private void init()
        {
            itype1 = new IDLType( Object.class, "Foo" ) ;
            itype2 = new IDLType( Object.class,
                new String[] { "first" }, "Bar" ) ;
            itype3 = new IDLType( Object.class,
                new String[] { "first", "second" }, "BazException" ) ;
            itype4 = new IDLType( Exception.class,
                new String[] { "java", "lang" }, "Exception" ) ;
        }

        public IDLTypeTestSuite()
        {
            super() ;
            init() ;
        }

        public IDLTypeTestSuite( String name ) 
        {
            super( name ) ;
            init() ;
        }

        public void testGetJavaClass()
        {
            assertEquals( itype1.getJavaClass(), Object.class ) ;
        }

        public void testGetModuleName1()
        {
            assertEquals( itype1.getModuleName(), "" ) ;
        }

        public void testGetModuleName2() 
        {
            assertEquals( itype2.getModuleName(), "first" ) ;
        }

        public void testGetModuleName3() 
        {
            assertEquals( itype3.getModuleName(), "first_second" ) ;
        }

        public void testGetExceptionName1()
        {
            assertTrue( "Actual value=" + itype1.getExceptionName(),
                itype1.getExceptionName().equals(
                "IDL:FooEx:1.0" ) ) ;
        }

        public void testGetExceptionName2()
        {
            assertTrue( "Actual value=" + itype2.getExceptionName(),
                itype2.getExceptionName().equals( 
                "IDL:first/BarEx:1.0" ) ) ;
        }

        public void testGetExceptionName3()
        {
            assertTrue( "Actual value=" + itype3.getExceptionName(),
                itype3.getExceptionName().equals( 
                "IDL:first/second/BazEx:1.0" ) ) ;
        }

        public void testGetExceptionName4()
        {
            assertTrue( "Actual value=" + itype4.getExceptionName(),
                itype4.getExceptionName().equals( 
                "IDL:java/lang/Ex:1.0" ) ) ;
        }

        public void testHasModule1()
        {
            assertFalse( itype1.hasModule() ) ;
        }

        public void testHasModule2()
        {
            assertTrue( itype2.hasModule() ) ;
        }

        public void testHasModule3()
        {
            assertTrue( itype3.hasModule() ) ;
        }

        public void testGetMemberName1()
        {
            assertEquals( itype1.getMemberName(), "Foo" ) ;
        }

        public void testGetMemberName2()
        {
            assertEquals( itype2.getMemberName(), "Bar" ) ;
        }

        public void testGetMemberName3()
        {
            assertEquals( itype3.getMemberName(), "BazException" ) ;
        }
    }

    // Methods shared by stub and tie tests.

    public static  Method getMethodByName( String mname ) 
    {
        Method[] methods = TieTest.class.getDeclaredMethods() ;
        Method method = null ;
        for (int ctr=0; ctr<methods.length; ctr++) {
            if (mname.equals( methods[ctr].getName() )) {
                method = methods[ctr] ;
                break ;
            }
        }

        return method ;
    }

    public static class ResponseHandlerImpl implements ResponseHandler
    {
        TestTransport transport = null ;

        public ResponseHandlerImpl( TestTransport transport )
        {
            this.transport = transport ;
        }

        public org.omg.CORBA.portable.OutputStream createReply() 
        {
            return transport.makeNormalReply() ;
        }

        public org.omg.CORBA.portable.OutputStream createExceptionReply() 
        {
            return transport.makeExceptionReply() ;
        }
    }

    public static class TieTestSuite extends TestCase
    {
        /* Test strategy:
         * Create a remote interface TieTest that contains all necessary 
         * methods for different argument and result patterns.
         * Assume that the DMM test has handled all of the oddball cases.
         * Give the methods unique names just to make things easier.
         * For each method, define a set of arguments and an expected result.
         * Have some methods throw exceptions:
         *  1. System Exception
         *  2. Java exception (we don't do anything at this level with other 
         *     types).
         * Create a TieTestImpl class that implements TieTest and also defines
         * the arguments to pass to a method and the expected result.
         * Each method impl checks that it received the expected args and
         * returns the expected result.
         * The client stub is simulated as follows:
         *  1. Create an input stream.
         *  2. Lookup the expected args for the test method, and write them
         *     to the input stream using the DMM.
         *  3. Get the method name from the IDLNameTranslator.
         *  4. Create a ReponseHandler that just returns an OutputStream.
         *  5. Call _invoke on the Tie.
         *  6. Check the result.
         * Test cases:
         * 0,1,2 args X 0,1 result
         * 1 arg, 1 result, throws sysex
         * 1 arg, 1 result, throws java exception
         */

        private ORB orb = null ;
        private TestTransport transport = null ;
        private PresentationManager pm = null ;
        private PresentationManager.ClassData cdata = null ;
        
        private Tie tie = null ;
        private TieTestImpl impl = null ;

        private ResponseHandler rhandler = null ;

        private void init()
        {
            Properties props = new Properties() ;
            props.setProperty( "org.omg.CORBA.ORBClass", 
                "com.sun.corba.ee.impl.orb.ORBImpl" ) ;
            String[] args = null ;
            orb = (ORB)org.omg.CORBA.ORB.init( args, props ) ;
            transport = new TestTransport( orb ) ;
            pm = ORB.getPresentationManager() ;
            cdata = pm.getClassData( TieTest.class ) ;

            tie = pm.getTie() ;
            tie.orb( orb ) ;
            impl = new TieTestImpl() ;
            tie.setTarget( impl ) ;
            
            rhandler = new ResponseHandlerImpl( transport ) ;
        }

        public TieTestSuite()
        {
            super() ;
            init() ;
        }

        public TieTestSuite( String name ) 
        {
            super( name ) ;
            init() ;
        }

        private InputStream makeInputStream( String mname )
        {
            IDLNameTranslator nt = cdata.getIDLNameTranslator() ;
            Method method = getMethodByName( mname ) ;
            DynamicMethodMarshaller dmm = pm.getDynamicMethodMarshaller( 
                method ) ;
            Object[] args = impl.getExpectedArguments( mname ) ;

            // The bad method name test has a null wireMname,
            // but the request requires some name, so we just
            // use the java name.
            String wireMname = nt.getIDLName( method ) ;
            if (wireMname == null)
                wireMname = mname ;

            OutputStream os = transport.makeRequest( wireMname ) ;
            if ((args != null) && (dmm != null)) 
                dmm.writeArguments( os, args ) ;
            InputStream is = transport.getInputStream( os ) ;
            transport.readRequestHeader( is ) ;
            return is ;
        }

        private void checkOutputStream( Object expectedResult,
            String mname, OutputStream os )
        {
            InputStream is = transport.getInputStream( os ) ;
            Method method = getMethodByName( mname ) ;
            DynamicMethodMarshaller dmm = pm.getDynamicMethodMarshaller( 
                method ) ;

            try {
                transport.readReplyHeader( is ) ;

                assertFalse( expectedResult instanceof Exception ) ;

                if (dmm != null) {
                    Object result = dmm.readResult( is ) ;
                    assertEquals( expectedResult, result ) ;
                }
            } catch (ApplicationException ae) {
                Exception exc = dmm.readException( ae ) ;
                Client.sameException( exc, expectedResult ) ;
            }
        }

        private void checkLastError()
        {
            String err = impl.getLastError() ;
            if (err != null)
                fail( err ) ;
        }

        public void doTest( String mname ) 
        { 
            Object expectedResult = impl.getExpectedTieResult( mname ) ;
            InputStream is = makeInputStream( mname ) ;
            Method method = getMethodByName( mname ) ;
            String methodWireName = cdata.getIDLNameTranslator().getIDLName( 
                method ) ;
            try {
                OutputStream os = (OutputStream)tie._invoke( methodWireName, 
                    is, rhandler ) ;
                checkOutputStream( expectedResult, mname, os ) ;
            } catch (Exception exc) {
                Client.sameException( exc, expectedResult ) ;
            }

            checkLastError() ; 
        }

        public void testHasAByteArray() {
            doTest( "hasAByteArray" ) ;
        }

        public void testDeclaredException()
        {
            doTest( "throwsDeclaredException" ) ;
        }

        public void testException()
        {
            doTest( "throwsException" ) ;
        }

        public void testSystemException()
        {
            doTest( "throwsSystemException" ) ;
        }

        public void testJavaException()
        {
            doTest( "throwsJavaException" ) ;
        }

        public void testBadMethodName()
        {
            doTest( "foo_bar_baz" ) ;
        }

        public void testm0()
        {
            doTest( "m0" ) ;
        }

        public void testm1()
        {
            doTest( "m1" ) ;
        }

        public void testm2()
        {
            doTest( "m2" ) ;
        }

        public void testvm0()
        {
            doTest( "vm0" ) ;
        }

        public void testvm1()
        {
            doTest( "vm1" ) ;
        }

        public void testvm2()
        {
            doTest( "vm2" ) ;
        }
    }

    public static class StubTestSuite extends TestCase
    {
        /* Test strategy:
         * Create a remote interface StubTest that contains all necessary 
         * methods for different argument and result patterns.
         * Assume that the DMM test has handled all of the oddball cases.
         * Give the methods unique names just to make things easier.
         * For each method, define a set of arguments and an expected result.
         * Have some methods throw exceptions:
         *  1. System Exception
         *  2. Java exception
         *  3. Remarshal exception
         *  4. Application exception
         * We'll reuse the Tie test in the remote case.
         * The delegate is simulated as follows:
         * Most of the Delegate methods are no-ops.  We only implement
         * those methods required by the dynamic stub:
         * 1. Delegate.request( proxy, String, respExp )
         *      Just create a stream and return it.  This must contain the 
         *      method name.
         * 2. Delegate.invoke( proxy, OutputStream ) 
         *      Unmarshal args and check that they match.
         *      Construct response according to method name for the test case.
         * 3. Delegate.releaseReply( proxy, InputStream )
         *      Just set a flag that this has been called, which needs to be
         *      checked in the test case.
         * 4. Delegate.servant_preinvoke( proxy, String, Class )
         * 5. Delegate.servant_postinvoke( proxy, ServantObject ) 
         * 6. Delegate is an instance of CorbaClientDelegate
         * 7. Delegate.getContactInfoList needs to return a 
         *    CorbaContactInfoList that:
         * 8. Implement getLocalClientRequestDispatcher by returning a 
         *    LocalClientRequestDispatcher that allows the test to return either
         *    true or false for lcrd.useLocalInvocation.
         *
         * Test cases: (we have already exercised enough of the 0-1 arg/result 
         * cases)
         * 1 arg, 1 result, return result 
         *      check for correct result
         * 1 arg, 1 result, throws system exception
         *      check for correctly mapped exception 
         * 1 arg, 1 result, throws java exception
         *      check for Unknown
         * 1 arg, 1 result, throws declared exception
         * 1 arg, 1 result, throws remarshal exception once only
         * Also do these simulating the local case.  This should be just a flag.
         */

        private TieTestImpl impl = null ;
        private TestClientDelegate delegate ;
        private org.omg.CORBA.Object stub ;

        private void init()
        {
            Properties props = new Properties() ;
            props.setProperty( "org.omg.CORBA.ORBClass", 
                "com.sun.corba.ee.impl.orb.ORBImpl" ) ;
            String[] args = null ;
            ORB orb = (ORB)org.omg.CORBA.ORB.init( args, props ) ;
            TestTransport transport = new TestTransport( orb ) ;

            PresentationManager pm = ORB.getPresentationManager() ;
            PresentationManager.StubFactoryFactory sff = pm.getDynamicStubFactoryFactory() ;

            impl = new TieTestImpl() ;
            Tie tie = pm.getTie() ;
            tie.orb( orb ) ;
            tie.setTarget( impl ) ;

            ResponseHandler rhandler = new ResponseHandlerImpl( transport ) ;
            delegate = new TestClientDelegate( orb, transport, impl, tie, 
                rhandler ) ;
            Class cls = TieTest.class ;
            stub = sff.createStubFactory( cls.getName(), false, "", cls,
                cls.getClassLoader() ).makeStub() ;
            StubAdapter.setDelegate( stub, delegate ) ;
        }

        public StubTestSuite()
        {
            super() ;
            init() ;
        }

        public StubTestSuite( String name ) 
        {
            super( name ) ;
            init() ;
        }

        public void doTest( String mname ) 
        { 
            Object expectedResult = impl.getExpectedStubResult( mname ) ;
            Method method = getMethodByName( mname ) ;
            Object[] expectedArgs = impl.getExpectedArguments( mname ) ;

            try {
                Object result = method.invoke( stub, expectedArgs ) ;
                assertEquals( result, expectedResult ) ;
            } catch (InvocationTargetException ite) {
                Throwable cause = ite.getCause() ;
                Client.sameException( cause, expectedResult ) ;
            } catch (Exception ex) {
                fail( "Unexpected exception " + ex + " in doTest" ) ;
            }

            checkLastError() ; 
        }

        private void doLocalTest( String name )
        {
            delegate.startLocalTest() ;
            doTest( name ) ;
        }

        private void doRemoteTest( String name )
        {
            delegate.startRemoteTest() ;
            doTest( name ) ;
        }

        private void checkLastError()
        {
            String err = impl.getLastError() ;
            if (err != null)
                fail( err ) ;
            delegate.checkForError(); 
        }

        public void testLocalHasAByteArray() 
        {
            doLocalTest( "hasAByteArray" ) ;
        }

        public void testLocalDeclaredException()
        {
            doLocalTest( "throwsDeclaredException" ) ;
        }

        public void testLocalException()
        {
            doLocalTest( "throwsException" ) ;
        }

        public void testLocalSystemException()
        {
            doLocalTest( "throwsSystemException" ) ;
        }

        /* Commented out for now: needs further investigation
        public void testLocalJavaException()
        {
            doLocalTest( "throwsJavaException" ) ;
        }
        */

        public void testLocalm0()
        {
            doLocalTest( "m0" ) ;
        }

        public void testLocalm1()
        {
            doLocalTest( "m1" ) ;
        }

        public void testLocalm2()
        {
            doLocalTest( "m2" ) ;
        }

        public void testLocalvm0()
        {
            doLocalTest( "vm0" ) ;
        }

        public void testLocalvm1()
        {
            doLocalTest( "vm1" ) ;
        }

        public void testLocalvm2()
        {
            doLocalTest( "vm2" ) ;
        }

        public void testRemoteHasAByteArray() 
        {
            doRemoteTest( "hasAByteArray" ) ;
        }

        public void testRemoteDeclaredException()
        {
            doRemoteTest( "throwsDeclaredException" ) ;
        }

        public void testRemoteException()
        {
            doRemoteTest( "throwsException" ) ;
        }

        public void testRemoteSystemException()
        {
            doRemoteTest( "throwsSystemException" ) ;
        }

        public void testRemoteJavaException()
        {
            doRemoteTest( "throwsJavaException" ) ;
        }

        public void testRemotem0()
        {
            doRemoteTest( "m0" ) ;
        }

        public void testRemotem1()
        {
            doRemoteTest( "m1" ) ;
        }

        public void testRemotem2()
        {
            doRemoteTest( "m2" ) ;
        }

        public void testRemotevm0()
        {
            doRemoteTest( "vm0" ) ;
        }

        public void testRemotevm1()
        {
            doRemoteTest( "vm1" ) ;
        }

        public void testRemotevm2()
        {
            doRemoteTest( "vm2" ) ;
        }
    }
}
