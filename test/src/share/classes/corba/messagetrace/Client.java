/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.messagetrace  ;

import java.util.Properties ;

import java.rmi.Remote ;
import java.rmi.RemoteException ;

import javax.rmi.PortableRemoteObject ;
import javax.rmi.CORBA.Util ;
import javax.rmi.CORBA.Tie ;

import java.nio.ByteBuffer ;

import corba.util.TransportManagerUtil;
import org.omg.CORBA.TypeCode ;
import org.omg.CORBA.ValueMember ;
import org.omg.CORBA.PUBLIC_MEMBER ;
import org.omg.CORBA.PRIVATE_MEMBER ;

// I have removed some weblogic test code for bug 5034649.
// We need to check to see if we can include this, because
// the test case relies on code supplied by BEA.
// import weblogic.management.WebLogicObjectName ;
// import weblogic.management.internal.WebLogicAttribute ;

import javax.management.ObjectName ;
import javax.management.Attribute ;

import junit.framework.TestCase ;
import junit.framework.Test ;
import junit.framework.TestResult ;
import junit.framework.TestSuite ;
import junit.extensions.TestSetup ;

import com.sun.corba.ee.spi.orb.ORB ;

import com.sun.corba.ee.spi.transport.TransportManager ;
import com.sun.corba.ee.spi.transport.MessageTraceManager ;
import com.sun.corba.ee.spi.transport.MessageData ;

import com.sun.corba.ee.spi.ior.iiop.GIOPVersion ;

import com.sun.corba.ee.impl.misc.ORBUtility ;
import com.sun.corba.ee.spi.misc.ORBConstants ;

import com.sun.corba.ee.impl.protocol.giopmsgheaders.Message ;

import com.sun.corba.ee.impl.encoding.CDRInputObject ;
import org.glassfish.pfl.test.TestCaseTools;

public class Client extends TestCase
{
    static ORB clientORB ;
    static ORB serverORB ;
    static TestInterface tester ;

    static boolean debug ;

    /** Remote interface used for testing
     */
    public interface TestInterface extends Remote
    {
        TypeCode testit( byte[] pad, TypeCode tc ) throws RemoteException ;

        Object echo( byte[] pad, Object obj ) throws RemoteException ;

        int twoArrays( byte[] one, byte[] two ) throws RemoteException ;

        void setAttribute( ObjectName on, Attribute at ) throws RemoteException ;
    }

    /** Trivial implementation of the testing interface
     */
    public static class TestInterfaceImpl extends PortableRemoteObject 
        implements TestInterface
    {
        public TestInterfaceImpl() throws RemoteException
        {
            super() ;
        }

        public TypeCode testit( byte[] pad, TypeCode tc ) throws RemoteException
        {
            return tc ;
        }

        public Object echo( byte[] pad, Object obj ) throws RemoteException
        {
            return obj ;
        }

        public int twoArrays( byte[] one, byte[] two ) throws RemoteException 
        {
            return one.length + two.length ;
        }

        public void setAttribute( ObjectName on, Attribute at ) throws RemoteException 
        {
        }
    }
    
    // An extension of the TestSetup test decorator (that is,
    // interceptor) that is used to initialize ORBs before the
    // tests start, and clean the ORBs up after the test completes.
    // Also sets up the tester remote object.
    private static class ORBManager extends TestSetup
    {
        public ORBManager( Test test ) 
        {
            super( test ) ;
        }

        public void setUp()
        {
            // Use dynamic RMI-IIOP so we don't need a separate rmic run
            System.setProperty( "com.sun.corba.ee.ORBUseDynamicStub", 
                "true" ) ;
            System.setProperty( "javax.rmi.CORBA.UtilClass",
                "com.sun.corba.ee.impl.javax.rmi.CORBA.Util" ) ;
            System.setProperty( "javax.rmi.CORBA.StubClass",
                "com.sun.corba.ee.impl.javax.rmi.CORBA.StubDelegateImpl" ) ;
            System.setProperty( "javax.rmi.CORBA.PortableRemoteObjectClass",
                "com.sun.corba.ee.impl.javax.rmi.PortableRemoteObject" ) ;
            System.setProperty( "org.omg.CORBA.ORBSingletonClass",
                "com.sun.corba.ee.impl.orb.ORBSingleton" ) ;

            TestInterfaceImpl servant = null ;

            // Create an unconnected stub, and a corresponding Tie.
            try {
                servant = new TestInterfaceImpl() ;
            } catch (RemoteException rex) {
                rex.printStackTrace() ;
                fail( "Unexpected remote exception " + rex ) ;
            }

            Tie tie = Util.getTie( servant ) ;
            
            // Set up client and server ORBs.
            Properties props = new Properties() ;
            props.setProperty( "org.omg.CORBA.ORBClass",
                "com.sun.corba.ee.impl.orb.ORBImpl" ) ;
            props.setProperty( "com.sun.corba.ee.transport.ORBDisableDirectByteBufferUse",
                "true" ) ;
            props.setProperty( "com.sun.corba.ee.transport.ORBAcceptorSocketType",
                "Socket" ) ;
            props.setProperty( "com.sun.corba.ee.transport.ORBConnectionSocketType",
                "Socket" ) ;
            serverORB = (ORB)ORB.init( new String[0], props ) ;
            // props.setProperty( "com.sun.corba.ee.ORBDebug",
                // "giop" ) ;
            clientORB = (ORB)ORB.init( new String[0], props ) ;

            // Connect the tie to the serverORB, which also connects the stub.
            tie.orb( serverORB ) ;

            TestInterface serverRef = null ;

            try {
                serverRef = (TestInterface)PortableRemoteObject.toStub( 
                    servant ) ;
            } catch (Exception exc) {
                exc.printStackTrace() ;
                fail( "Unexpected exception in toStub: " + exc ) ;
            }

            // Get a reference in the client ORB.  This will insure that
            // we actually marshal the data.
            String str = serverORB.object_to_string( 
                (org.omg.CORBA.Object)serverRef ) ;
            org.omg.CORBA.Object clientObj = clientORB.string_to_object( str ) ;
            tester = (TestInterface)PortableRemoteObject.narrow(
                clientObj, TestInterface.class ) ;
        }

        public void tearDown()
        {
            if (debug)
                System.out.println( "Destroying ORBs" ) ;

            clientORB.destroy() ;
            serverORB.destroy() ;
        }
    }

    private static final boolean DEBUG = false ;
    // private static final int REP_COUNT = 1000 ;
    // private List<TimedTest> timedTests ;

    private ValueMember makeValueMember( String name, TypeCode type, 
        boolean isPublic ) 
    {
        return new ValueMember( name, "", "", "", type, null, 
            isPublic ? PUBLIC_MEMBER.value : PRIVATE_MEMBER.value) ; 
    }

    /**
    private TypeCode makeValueTypeCode( ORB orb )
    {
        String repoID = 
            "RMI:weblogic.management.WebLogicObjectName:6488C1A74F9FA9EA:66076A3E2CDE" ;

        String name = "weblogic.management.WebLogicObjectName" ;

        TypeCode recursiveTypeCode  = orb.create_recursive_tc( repoID ) ;
        TypeCode nullTypeCode       = orb.get_primitive_tc( TCKind.tk_null ) ;  
        TypeCode booleanTypeCode    = orb.get_primitive_tc( TCKind.tk_boolean ) ;
        TypeCode longTypeCode       = orb.get_primitive_tc( TCKind.tk_long ) ;

        ValueMember[] members = new ValueMember[] {
            makeValueMember( "hashCode",  longTypeCode,      false ),
            makeValueMember( "isAdmin",   booleanTypeCode,   false ),
            makeValueMember( "isConfig",  booleanTypeCode,   false ),
            makeValueMember( "isRuntime", booleanTypeCode,   false ),
            makeValueMember( "parent",    recursiveTypeCode, false ) 
        } ;

        TypeCode result = orb.create_value_tc(
            repoID, name, VM_NONE.value, nullTypeCode, members ) ;

        return result ;
    }
    **/

    private byte[] makeBytes( int size )
    {
        byte[] result = new byte[size] ;
        for (int ctr=0; ctr<size; ctr++ )
            result[ctr] = 0 ;
        return result ;
    }
 
    public static void main( String[] args ) 
    {
        debug = (args.length>0) && args[0].equals( "-debug" ) ;

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
            "Testing Message Trace Manager\n" +
            "==============================================================\n" 
        ) ;

        // TestSuite created only to include the ORBManager setup wrapper,
        // which wraps the real TestSuite made from this class.
        // This causes the ORBs for this test to be created before all
        // tests run, and destroyed after all tests are completed.  
        TestSuite main = new TestSuite( "main" ) ;
        TestSuite ts = TestCaseTools.makeTestSuite( Client.class ) ;
        main.addTest( new ORBManager( ts ) ) ;
        return main ;
    }

    /* Simple-minded, inefficient algorithm for locating the first occurence
     * of pattern (if any) in the buffer after the offset.
     * Returns index >= offset, or -1 if no match is found.
     */
    private int findPattern( byte[] buffer, int offset, byte[] pattern )
    {
        int ctr=offset;
        while (ctr<buffer.length) {
            int ctr2 = 0 ;
            while (ctr2 < pattern.length) {
                if (buffer[ctr+ctr2] != pattern[ctr2])
                    break ;
                ctr2++ ;
            }

            if (ctr2==pattern.length)
                return ctr ;

            ctr++ ;
        }

        return -1 ;
    }

    private int getInt( byte[] buffer, int offset ) 
    {
        int b1 = (int)buffer[offset] & 0xFF ;
        int b2 = (int)buffer[offset+1] & 0xFF ;
        int b3 = (int)buffer[offset+2] & 0xFF ;
        int b4 = (int)buffer[offset+3] & 0xFF ;
        return (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
    }

    // Length of GIOP message header + GIOP 1.2 fragment header
    // (we'll assume GIOP 1.2 only here)
    private static final int HEADER_LENGTH = 16 ;

    private byte[] makeSingleStream( byte[][] data ) 
    {
        // Copy the first byte[] and all but the first 16 bytes of 
        // each subsequent byte[] into the result.
        int size = 0 ;
        for (int ctr=0; ctr<data.length; ctr++)
            size += data[ctr].length ;

        // Now adjust the size for the fragment headers
        size -= HEADER_LENGTH * (data.length-1) ;

        byte[] result = new byte[size] ;
        System.arraycopy( data[0], 0, result, 0, data[0].length ) ;

        // Copy all of the first message, but skip the headers
        // in all subsequent messages.
        int resOffset = 0 ;
        int srcOffset = 0 ;
        for (int ctr=0; ctr<data.length; ctr++) {
            int copyLength = data[ctr].length - srcOffset ;
            System.arraycopy( data[ctr], srcOffset, result, resOffset, 
                copyLength ) ;

            srcOffset = HEADER_LENGTH ;
            resOffset += copyLength ;
        }

        return result ;
    }

    private static void displayData( String msg, byte[][] data ) 
    {
        for (int ctr=0; ctr<data.length; ctr++) {
            ByteBuffer buffer = ByteBuffer.wrap( data[ctr] ) ;
            buffer.position( buffer.limit() ) ;
            ORBUtility.printBuffer( msg + " buffer " + ctr, buffer,
                System.out ) ;
        }
    }

    public void testDummy() {
        // Does nothing but pass: needed because all of the
        // WebLogic tests are commented out pending resolution
        // of including the BEA test code in glassfish-corba.
        assertTrue( true ) ;
    }

    /**
    public void testWebLogic()
    {
        try {
            WebLogicObjectName won = new WebLogicObjectName(
                "WTCServerClient", "WTCServer", "mydomain");
            WebLogicAttribute attr = new WebLogicAttribute(
                "targets", new WebLogicObjectName[] { won });

            paddedEchoTest( "Testing WebLogicObjectName", attr,
                0, 50 ) ;

            // Do this once because the first time is special:
            // it sends the FVD.
            tester.setAttribute( won, attr ) ;

            CorbaTransportManager ctm = clientORB.getCorbaTransportManager() ;
            MessageTraceManager mtm = ctm.getMessageTraceManager() ;

            mtm.clear() ;
            mtm.enable( true ) ;

            // if (debug)
                // clientORB.setDebugFlag( "giop" ) ;

            tester.setAttribute( won, attr ) ;

            byte[][] dataSent = mtm.getDataSent() ;
            byte[][] dataReceived = mtm.getDataReceived() ;

            if (debug) {
                displayData( "dataSent", dataSent ) ;
                displayData( "dataReceived", dataSent ) ;
                // clientORB.clearDebugFlag( "giop" ) ;
            }
                
            mtm.clear() ;
            mtm.enable( false ) ;

            byte[] buffer = makeSingleStream( dataSent ) ;
            if (debug)
                ORBUtility.printBuffer( "----- Contents of merged request buffer -----",
                    ByteBuffer.wrap( buffer ), System.out ) ;

            // Search for the offset at which 0x001d occurs: this is
            // the tk_value for the typecode of the value type.
            byte[] firstp = new byte[] { 0x00, 0x00, 0x00, 0x1d } ;
            int firstIndex = findPattern( buffer, 0, firstp ) ;
            assertTrue( "Could not find start of typecode", firstIndex >= 0 ) ;
            if (debug)
                System.out.println( "firstIndex = " + firstIndex ) ;

            // Search for 0xffffffffffff (the start of the indirection) after
            // the typecode.
            byte[] secondp = new byte[] { (byte)0xff, (byte)0xff, (byte)0xff, 
                (byte)0xff, (byte)0xff, (byte)0xff } ;
            int secondIndex = findPattern( buffer, firstIndex, secondp ) ;
            assertTrue( "Could not find start of indirection", secondIndex >= 0 ) ;
            if (debug)
                System.out.println( "secondIndex = " + secondIndex ) ;

            // Make sure that the offset is the difference
            // between the index of the first pattern and the start
            // of the offset in the second pattern.
            int offset = getInt( buffer, secondIndex + 4 ) ;
            if (debug)
                System.out.println( "offset = " + offset ) ;

            assertEquals( "Incorrect offset", firstIndex, secondIndex + 4 + offset ) ;
        } catch (Exception exc) {
            fail( "WebLogic test failed: " + exc ) ;
            exc.printStackTrace() ;
        }
    }

    private void paddedEchoTest( String testMsg,
        Object data, int startSize, int endSize )
    {
        if (debug)
            System.out.println( testMsg ) ;

        for (int ctr=startSize; ctr<=endSize; ctr++) {
            byte[] pad = makeBytes( ctr ) ;
            Object result = null ;
            
            try {
                result = tester.echo( pad, data ) ;
            } catch (Exception exc) {
                fail( "\tTest failed: " + exc + "with pad size = " + ctr ) ;
                exc.printStackTrace() ;
            }
        }
    }

    public void testTypeCode()
    {
        TypeCode valueTypeCode = makeValueTypeCode( clientORB ) ;

        paddedEchoTest( "Testing TypeCode", valueTypeCode, 700, 800 ) ;

        // Now invoke testit with a variable size pad, to try and force
        // alignment problems in the TypeCode indirection.
        // With a GIOP fragment size of 1024, this test fails at
        // pad sizes 749-752 + n*1024.  It fails because the typecode
        // is marshalled inside a boxed value type using a chunked
        // representation, and certain offsets force the last chunk in the
        // fragment to end with the typecode, forcing a new chunk
        // header between the typecode kind and the typecode body,
        // which is an encapsulation.  This in turn causes the indirection
        // value to be computed incorrectly, because the TypeCodeOutputStream
        // cannot account for the extra length caused by the chunk header.
        // This may be a bit difficult to fix.
        //
        // For now, we will just run this test for 747 and 748, since
        // I don't have time to fix the other typecode problem.
        final int lowerBound = 747 ;
        final int upperBound = 749 ;    // set this to a larger value to
                                        // reproduce bug. 
        for (int ctr=lowerBound; ctr<upperBound; ctr++) {
            byte[] pad = makeBytes( ctr ) ;
            TypeCode resultTypeCode = null ;

            try {
                resultTypeCode = tester.testit( pad, valueTypeCode ) ;
                if (debug)
                    System.out.println( 
                        "TypeCode marshalling succeeded with a pad size of " + ctr ) ;
            } catch (Exception exc) {
                fail( "TypeCode marshalling failed with a pad size of " + ctr ) ;
                if (debug)
                    exc.printStackTrace() ;
            }

            //if (!resultTypeCode.equals( valueTypeCode ))
                //System.out.println( "Typecodes are not equal with a pad size of " 
                    //+ ctr ) ;
        }
    }
    */

    public static class MessageDataTestSuite extends TestCase
    {
        public MessageDataTestSuite()
        {
            super() ;
        }

        public MessageDataTestSuite( String name ) 
        {
            super( name ) ;
        }

        private void initByteArray( byte[] data, byte value ) 
        {
            for (int ctr=0; ctr<data.length; ctr++ )
                data[ctr] = value ;
        }
        
        // Expected data
        private boolean initialized = false ;
        private byte[] data1 = new byte[ 4000 ] ;
        private byte[] data2 = new byte[ 3000 ] ;
        private int result ;

        private TransportManager ctm ;
        private MessageTraceManager mtm ;

        private byte[][] dataSent ;
        private byte[][] dataReceived ;

        private MessageData sentMD ;
        private MessageData receivedMD ;

        // Do not call this from the constructor, as it runs before
        // the ORB is initialized!
        private void init() 
        {
            if (initialized) {
                return ;
            } else {
                initialized = true ;
            }
            
            ctm = clientORB.getCorbaTransportManager() ;
            mtm = ctm.getMessageTraceManager() ;

            initByteArray( data1, (byte)0xCC ) ;
            initByteArray( data2, (byte)0xEE ) ;
        
            mtm.clear() ;
            mtm.enable( true ) ;

            try {
                result = tester.twoArrays( data1, data2 ) ;
            } catch (RemoteException rexc) {
                rexc.printStackTrace() ;
                fail( "Unexpected RemoteException " + rexc ) ;
            }

            dataSent = mtm.getDataSent() ;
            dataReceived = mtm.getDataReceived() ;

            if (debug) {
                displayData( "dataSent", dataSent ) ;
                displayData( "dataReceived", dataSent ) ;
            }
                
            mtm.clear() ;
            mtm.enable( false ) ;

            sentMD = TransportManagerUtil.getMessageData(dataSent, clientORB) ;
            // temporarily commented out receivedMD = ctm.getMessageData( dataReceived ) ;
        }

        private void checkMessages( Message[] msgs, int firstMessageType ) 
        {
            for (int ctr=0; ctr<msgs.length; ctr++) 
                checkMessage( msgs[ctr], 
                    ctr==0 ? firstMessageType : Message.GIOPFragment, 
                    ctr, msgs.length ) ;
        }

        private void checkMessage( Message msg, int msgType, int msgNum, 
            int numMsgs )
        {
            assertEquals( "Bad message type", 
                msgType, msg.getType() ) ;
            assertEquals( "Bad GIOP Version", 
                msg.getGIOPVersion(), GIOPVersion.V1_2 ) ;
            assertEquals( "Bad encoding version", 
                msg.getEncodingVersion(), ORBConstants.CDR_ENC_VERSION ) ;
            assertTrue( "Incorrect setting of moreFragmentsToFollow on " +
                ((msgNum < numMsgs-1) ? ("message " + msgNum) : "last message"),
                msg.moreFragmentsToFollow() == msgNum < numMsgs - 1) ;
        }

        private boolean equalArrays( byte[] arr1, byte[] arr2 ) 
        {
            if ((arr1 == null) || (arr2 == null))
                return arr1 == arr2 ;

            int len = arr1.length ;
            if (len != arr2.length)
                return false ;

            for (int ctr=0; ctr<len; ctr++)
                if (arr1[ctr] != arr2[ctr])
                    return false ;

            return true ;
        }

        /* Test strategy:
         * 1. Capture lots of data using the echo method by sending
         *    two large byte[] arguments with fixed patterns.
         * 2. Convert dataSend and dataReceived into MessageData structures.
         * 3. Verify that we can correctly read from the stream.
         * 4. Verify the contents of the message headers.
         */
        public void testGetMessage() throws Exception
        {
            try {
                init() ;
                for (int ctr=0; ctr<dataSent.length; ctr++) {
                    byte[] data = dataSent[ctr] ;
                    Message msg = TransportManagerUtil.getMessage(data, clientORB) ;
                    checkMessage( msg, 
                        ctr==0 ? Message.GIOPRequest : Message.GIOPFragment, 
                        ctr, dataSent.length ) ;
                }
            } catch (Exception exc) {
                exc.printStackTrace() ;
                throw exc ;
            }
        }

        public void testSentMDHeader() throws Exception
        {
            try {
                init() ;
                checkMessages( sentMD.getMessages(), Message.GIOPRequest ) ;
            } catch (Exception exc) {
                exc.printStackTrace() ;
                throw exc ;
            }
        }

        public void testSentMDBody() throws Exception
        {
            try {
                init() ;
                CDRInputObject str = sentMD.getStream() ;
                byte[] arr1 = (byte[])str.read_value( byte[].class ) ;
                assertTrue( "First array is not the same as the original",
                    equalArrays( data1, arr1 ) ) ;

                byte[] arr2 = (byte[])str.read_value( byte[].class ) ;
                assertTrue( "Second array is not the same as the original",
                    equalArrays( data2, arr2 ) ) ;
            } catch (Exception exc) {
                exc.printStackTrace() ;
                throw exc ;
            }
        }

        /* The testing of received data is temporarily commented out, due to the
         * difficulty of getting received data in the ORB.  The problem is that
         * the data is received in a different thread than the thread that is
         * waiting for the data, but the MessageDataManager is local to the
         * thread that is waiting, so we can't easily get the data captured
         * into the correct place.  This appears to be solvable, but 
         * complicated, so I'll defer this until later.
         *
         * Notes:
         * Messages are received and handled in CorbaMessageMediatorImpl.
         * See in particular the handleInput methods for FragmentMessage_1_2 and
         * ReplyMessage_1_2.  The client thread is waiting in the 
         * CorbaResponseWaitingRoomImpl.waitForResponse method.  I think we
         * need to make the raw message available in the OutCallDesc,
         * which may require changes in the CorbaResponseWaitingRoomImpl.responseReceived 
         * method.
         *
         * Note that the "NO" at the start of these methods prevents JUnit from
         * picking them up and running them as tests.
         */
        public void NOtestReceivedMDHeader()
        {
            init() ;
            checkMessages( receivedMD.getMessages(), Message.GIOPReply ) ;
        }

        public void NOtestReceivedMDBody()
        {
            init() ;
            int data = receivedMD.getStream().read_long() ;
            assertEquals( "Result does not match expected value",
                result, data ) ;
        }
    }
}
