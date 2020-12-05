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

//
// Created       : 1999 Mar 01 (Mon) 16:59:34 by Harold Carr.
// Last Modified : 2003 Dec 16 (Tue) 15:43:37 by Harold Carr.
//

// NOTE: all invocations occur in pairs.
// The first is direct, making it easy to step into the code using a debugger.
// The second uses the test framework for automatic testing.

package corba.hcks;

import java.rmi.MarshalException;

import corba.framework.Controller;

//
// IDL imports.
//

import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.MARSHAL;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CORBA.ORB;
import org.omg.CORBA.PERSIST_STORE;
import org.omg.CORBA.SystemException;
import org.omg.DynamicAny.DynAny;
import org.omg.DynamicAny.DynAnyFactory;
import org.omg.DynamicAny.DynAnyFactoryHelper;


//
// RMI-IIOP imports.
//


// JRMP
//import java.rmi.Naming; 
// IIOP
import javax.naming.InitialContext;

//
// Imports for specific tests.
//

import org.omg.CORBA.Any;
import org.omg.CORBA.IMP_LIMIT;
import org.omg.CORBA.OBJ_ADAPTER;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextHelper;
import org.omg.IOP.Codec;
import org.omg.IOP.CodecFactory;
import org.omg.IOP.CodecFactoryHelper;
import org.omg.IOP.Encoding;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.ForwardRequestHelper;
import org.omg.PortableInterceptor.Current;


import com.sun.corba.ee.spi.presentation.rmi.StubAdapter;
import com.sun.corba.ee.spi.presentation.rmi.StubWrapper;


//


public class Client 
{
    static {
        // System.setProperty( "corba.test.junit.helper.debug", "true" ) ;
    }

    public static String testName = Client.class.getName() ;

    public static final String baseMsg = Client.class.getName();
    public static final String main = baseMsg + ".main";

    public static String giopVersion = C.GIOP_VERSION_1_2;
    public static String buffMgrStategy = C.BUFFMGR_STRATEGY_STREAM;
    public static int fragmentSize = C.DEFAULT_FRAGMENT_SIZE;

    public static ORB orb;
    public static InitialContext initialContext;
    public static boolean isColocated;

    public static idlHEADERI ridlHEADERI;

    public static rmiiI rrmiiI1;
    public static rmiiI rrmiiI2;
    public static rmiiI rrmiiSA;
    public static rmiiI rrmiiSL;

    public static idlI ridlStaticPOA;
    public static idlI ridlDynamicPOA;
    public static idlI ridlStaticPOAStringified;
    public static idlI ridlStatic;
    public static idlI ridlStaticForDisconnect;
    public static idlI ridlStaticTie;
    public static idlI ridlDynamic;
    public static idlI ridlStaticStringified;
    public static idlSAI ridlSAI1;
    public static idlSAI ridlSAI2;
    public static idlSAI ridlSAIRaiseObjectNotExistInIncarnate;
    public static idlSAI ridlSAIRaiseSystemExceptionInIncarnate;
    public static idlSLI ridlSLI1;
    public static idlSLI ridlAlwaysForward;
    public static idlSAI ridlNonExistentDefaultServant;

    public static sendRecursiveType rsendRecursiveType;

    public static idlControllerI ridlControllerStatic;

    public static boolean testExpect = false;
    public static boolean allowLocalOptimization ;

    // debugOn/Off were added for debugging a ServiceContext scMap aliasing bug.
    private static void debugOn( String msg ) {
        com.sun.corba.ee.spi.orb.ORB spiOrb = 
            com.sun.corba.ee.spi.orb.ORB.class.cast( orb ) ;
        System.out.println( "DEBUGGING ON: " + msg ) ;
        spiOrb.setDebugFlags( "serviceContext", "interceptor", "subcontract" ) ;
    }

    private static void debugOff() {
        com.sun.corba.ee.spi.orb.ORB spiOrb = 
            com.sun.corba.ee.spi.orb.ORB.class.cast( orb ) ;
        spiOrb.clearDebugFlags( "serviceContext", "interceptor", "subcontract" ) ;
        System.out.println( "DEBUGGING OFF" ) ;
    }

    public static void main(String[] av)
    {
        try {
            U.initialize( testName ) ;
            U.setDisplayErrorsWhenTheyHappen(true);

            if (ColocatedClientServer.isColocated) {
                isColocated = true;
            } else {
                isColocated = false;
                orb = C.createORB(av, giopVersion,
                                  buffMgrStategy, fragmentSize);
                // Use the same ORB which has interceptor properties set.
                // This obviates the need to set system properties.
                initialContext = C.createInitialContext(orb);
            }

            // orb is either initilized by the createORB call, or it was
            // initialized before main was called in the ColocatedClientServer case.
            // Set the flag to true, as that is the current behavior of is_local.
            allowLocalOptimization = true ;
            
            lookupReferences();

            runTests();
        } catch (Exception e) {
            U.sopUnexpectedException(main + " : ", e);
        } finally {
            U.done() ;
        }

        if (U.hasError()) {
            System.exit(1);
        } else {
            System.exit(Controller.SUCCESS);
        }
    }

    public static void runTests()
        throws
            Exception
    {
        // Get an active ByteBuffer count (ByteBuffers actively in use by ORB)
        com.sun.corba.ee.spi.orb.ORB spiOrb = (com.sun.corba.ee.spi.orb.ORB)orb;
        int startCount = spiOrb.getByteBufferPool().activeCount();

        testExpect(); // Test the test framework.

        testMisc();

        testInterceptors();

        testServantActivator();

        testServantLocator();
            
        testSynchronousInvocations();

        testSpecialMethods();

        testOneWayInvocations();

        testUserExceptions();

        testSystemExceptions();

        testUnknownExceptions();

        testDSIInvocations();

        // These next two caused my debugger to fail at one time.
        testRMIIIOP();
        testThreadDeathHandling();

        testValueTypes(); // This does fragmentation even with grow - why?

        // report ByteBuffer active count numbers
        System.out.println("Start ByteBuffer active count: " + startCount);
        int currentCount = spiOrb.getByteBufferPool().activeCount();
        System.out.println("End ByteBuffer active count: " + currentCount);
        System.out.println("Difference between start & end ByteBuffer count: " +
                           (currentCount - startCount));
    }

    public static void lookupReferences()
        throws
            Exception
    {
        // IDL refs.

        ridlHEADERI =  idlHEADERIHelper.narrow(U.resolve(C.idlHEADERI, orb));
        
        ridlStaticPOA =  idlIHelper.narrow(U.resolve(C.idlStaticPOA, orb));
        ridlDynamicPOA = idlIHelper.narrow(U.resolve(C.idlDynamicPOA, orb));
        ridlStatic =     idlIHelper.narrow(U.resolve(C.idlStatic, orb));
        ridlStaticForDisconnect =
            idlIHelper.narrow(U.resolve(C.idlStaticForDisconnect, orb));
        ridlStaticTie =  idlIHelper.narrow(U.resolve(C.idlStaticTie, orb));
        ridlDynamic =    idlIHelper.narrow(U.resolve(C.idlDynamic, orb));

        ridlStaticPOAStringified =
            idlIHelper.narrow(orb.string_to_object(orb.object_to_string(ridlStaticPOA)));
        ridlStaticStringified =
            idlIHelper.narrow(orb.string_to_object(orb.object_to_string(ridlStatic)));

        ridlSAI1 =       idlSAIHelper.narrow(U.resolve(C.idlSAI1, orb));
        ridlSAI2 =       idlSAIHelper.narrow(U.resolve(C.idlSAI2, orb));
        
        ridlSAIRaiseObjectNotExistInIncarnate =
            idlSAIHelper.narrow(U.resolve(C.idlSAIRaiseObjectNotExistInIncarnate, orb));
        
        ridlSAIRaiseSystemExceptionInIncarnate =
            idlSAIHelper.narrow(U.resolve(C.idlSAIRaiseSystemExceptionInIncarnate, orb));
        
        ridlSLI1 =       idlSLIHelper.narrow(U.resolve(C.idlSLI1, orb));

        ridlNonExistentDefaultServant =
           idlSAIHelper.narrow(U.resolve(C.idlNonExistentDefaultServant, orb));

        rsendRecursiveType =
           sendRecursiveTypeHelper.narrow(U.resolve(C.sendRecursiveType, orb));

        ridlControllerStatic =
            idlControllerIHelper.narrow(U.resolve(C.idlControllerStatic, orb));
        
        // RMI-IIOP refs.
        
        rrmiiI1 = (rmiiI)
            U.lookupAndNarrow(C.rmiiI1, rmiiI.class, initialContext);
        rrmiiI2 = (rmiiI)
            U.lookupAndNarrow(C.rmiiI2, rmiiI.class, initialContext);
        rrmiiSA = (rmiiI)
            U.lookupAndNarrow(C.rmiiSA, rmiiI.class, initialContext);
        rrmiiSL = (rmiiI)
            U.lookupAndNarrow(C.rmiiSL, rmiiI.class, initialContext);
    }

    public static void refreshIdlAlwaysForward ()
        throws
            Exception
    {
        ridlAlwaysForward =  
            idlSLIHelper.narrow(U.resolve(C.idlAlwaysForward, orb));
    }

    //
    // Misc.
    //

    public static void testMisc()
        throws
            Exception
    {
        HEADER("testMisc");
        
        // --------------------------------------------------

        org.omg.CORBA.Object o =ridlStaticPOA.getAndSaveUnknownORBVersionIOR();
        boolean result = ridlStaticPOA.isIdenticalWithSavedIOR(o);
        if (! result) {
            throw new RuntimeException(C.isIdenticalWithSavedIOR);
        }
        /* 
         * Gets NoSuchMethodException isIdenticalWithSavedIOR
        U.expect(U.result, Boolean.valueOf(true),
                 ridlStaticPOA, C.isIdenticalWithSavedIOR, o);
        */

        // --------------------------------------------------
        // I know I have fragments set to 32 bytes.
        // So try to identify the fragments on the wire by their
        // contents.

        int size = 10000;
        byte[] bigBytes = new byte[size];
        byte j = 0x41; // 'A'
        // 32 == fragment size
        // 12 == GIOP header
        for (int i = 0; i < size; i+= 32 - 15) {
            bigBytes[i] = j++;
            if (j == 0x5a) {  // A-Z
                j = 0x61; // 'a'
            } else if (j == 0x7a) { // a-z
                j = 0x41; // 'A'
            }
        }
        rrmiiI1.sendBytes(bigBytes);

        // --------------------------------------------------

        // --------------------------------------------------

        // Direct marshaling works, but not through Any.
        recursiveType rType = 
            new recursiveType("a","b", new recursiveType[0]); 
        rsendRecursiveType.sendAsType(rType);
        Any rTypeAny = orb.create_any(); 
        recursiveTypeHelper.insert(rTypeAny, rType); 
        // Getting exception when marshaling Any.
        //***** rsendRecursiveType.sendAsAny(rTypeAny);


        // This used to throw a class cast exception.
        DynAnyFactory dynAnyFactory =
            DynAnyFactoryHelper.narrow(
                orb.resolve_initial_references("DynAnyFactory"));
        DynAny dynAny = dynAnyFactory.create_dyn_any(orb.create_any());
        try {
            orb.object_to_string(dynAny);
            U.sopShouldNotSeeThis("object_to_string did not throw exception");
        } catch (MARSHAL t) {
            // Expected.
            U.sop(t);
        } catch (Throwable t) {
            U.sopShouldNotSeeThis("object_to_string threw wrong exception: "
                                  + t);
        }
        /* REVISIT
          This does not find object_to_string (NoSuchMethodException).
        U.expect(U.exception, new MARSHAL(),
                 orb, C.object_to_string, dynAny);
        */

        // --------------------------------------------------
        // disconnect then do non_existent.
        // This caused null pointer at one time.

        ridlControllerStatic.action(C.disconnectRidlStaticServant);
        U.sop(ridlStaticForDisconnect._non_existent());
        U.expect(U.result, Boolean.valueOf(true),
                 ridlStaticForDisconnect, C._non_existent);

        try {
            ridlStaticForDisconnect.syncOK(C.idlStaticForDisconnect);
        } catch (Throwable t) {}
        U.expect(U.exception, new OBJECT_NOT_EXIST(),
                 ridlStaticForDisconnect, C.syncOK, C.idlStaticForDisconnect);

        // --------------------------------------------------
        // Similar to above disconnect case, but poa-based.
        // This caused null pointer at one time.
        // Then later it was changed to throw an adapter exception
        // since the default servant is not set.

        try {
            ridlNonExistentDefaultServant._non_existent();
        } catch (Throwable t) {}
        U.expect(U.exception, new OBJ_ADAPTER(),
                 ridlNonExistentDefaultServant, C._non_existent);

        // --------------------------------------------------
        // Kill server then step through following lines
        // to observe bad connection caching.

        try { ridlStaticPOA.syncOK(C.idlStaticPOA); } catch (Throwable t) {}
        try { ridlStaticPOA.syncOK(C.idlStaticPOA); } catch (Throwable t) {}
        try { ridlStaticPOA.syncOK(C.idlStaticPOA); } catch (Throwable t) {}
        try { ridlStaticPOA.syncOK(C.idlStaticPOA); } catch (Throwable t) {}


        // --------------------------------------------------
        // Cause a runtime exception to happen and be handled
        // by PI in stub's _releaseReply
        // KMC: I have removed this test since I have removed the
        //      old inheritance-based interceptor facility.
        //MyPOAORB.throwRuntimeExceptionInSendingRequestServiceContexts = true;
        //try {
            //ridlStaticPOA.syncOK(C.idlStaticPOA);
        //} catch (Throwable t) {}
        //U.expect(U.exception, new RuntimeException(),
                 //ridlStaticPOA, C.syncOK,
                 //C.idlStaticPOA);
        //MyPOAORB.throwRuntimeExceptionInSendingRequestServiceContexts = false;


        // --------------------------------------------------
        // On server side, _is_a can be any arbitrary user code.
        // Therefore, client interceptors, if present, should execute
        // to make sure proper transactions and security.

        ridlDynamicPOA._is_a("foo");


        // --------------------------------------------------
        // Uses the ORBSingleton's internal ORB to handle
        // embedded object references.

        CodecFactory codecFactory =
            CodecFactoryHelper.narrow(
                orb.resolve_initial_references("CodecFactory"));
        Encoding encoding = new Encoding((short)0, (byte)1, (byte)2);
        Codec codec = codecFactory.create_codec(encoding);
        ORB orbSingleton = ORB.init();
        Any any = orbSingleton.create_any();
        ForwardRequest fr =  
            new ForwardRequest(orb.resolve_initial_references("NameService"));
        ForwardRequestHelper.insert(any, fr);
        byte[] encoded = codec.encode(any);
        any = codec.decode(encoded);
        fr = ForwardRequestHelper.extract(any);
        NamingContext nc = NamingContextHelper.narrow(fr.forward);
        U.sop(nc.resolve(U.makeNameComponent(C.idlStaticPOA)));

        // --------------------------------------------------

        for (int i = 0; i < 3; ++i) {
            // This caused a client hang at one time in the rmi-iiop/poa cases only.
            try { 
                // debugOn( "rrmiiSA.returnObjectFromServer" ) ;
                rrmiiSA.returnObjectFromServer(false); 
                // debugOff() ;
                U.sopShouldNotSeeThis() ;
            } catch (Throwable t) {
                U.sop(t); 
                /* Get a different exception now:
                checkRmiMarshalException(
                    new NotSerializableException(),
                    // This happens when server side is fragmenting.
                    // The server just ends the fragment so the client
                    // side orb raises this exception.
                    new MARSHAL(ORBUtilSystemException.END_OF_STREAM,
                                CompletionStatus.COMPLETED_NO), t);
                */
            }

            /*
            REVISIT - says no such method.
            U.expect(U.exception, new BAD_PARAM(),
                     rrmiiSA, C.returnObjectFromServer, Boolean.valueOf(false));
            */

            // This caused server threads to wait forever for input when streaming.
            try {
                rrmiiSA.sendOneObject(new NonSerializableObject()); 
                U.sopShouldNotSeeThis() ;
            } catch (Throwable t) {
                U.sop(t); 
                /* checkRmiMarshalException(new NotSerializableException(), null, t); */
            }

            try { 
                rrmiiSA.sendTwoObjects(new SerializableObject(),
                                       new NonSerializableObject()); 
                U.sopShouldNotSeeThis() ;
            } catch (Throwable t) { 
                U.sop(t); 
                /* checkRmiMarshalException(new NotSerializableException(), null, t); */
            }
        }
    }

    //
    // ThreadDeath
    //

    public static void testThreadDeathHandling()
        throws
            Exception
    {
        HEADER("testThreadDeathHandling");

        try {
            ridlSLI1.
                throwThreadDeathInReceiveRequestServiceContexts(C.throwThreadDeathInReceiveRequestServiceContexts);
            U.sopShouldNotSeeThis();
        } catch (Throwable t) { U.sop(t);}
        try {
            ridlSLI1.
                throwThreadDeathInPreinvoke(C.throwThreadDeathInPreinvoke);
            U.sopShouldNotSeeThis();
        } catch (Throwable t) { U.sop(t);}
        try {
            ridlSLI1.
                throwThreadDeathInReceiveRequest(C.throwThreadDeathInReceiveRequest);
            U.sopShouldNotSeeThis();
        } catch (Throwable t) { U.sop(t);}
        try {
            ridlSLI1.
                throwThreadDeathInServant(C.throwThreadDeathInServant);
            U.sopShouldNotSeeThis();
        } catch (Throwable t) { U.sop(t);}
        try {
            ridlSLI1.
                throwThreadDeathInPostinvoke(C.throwThreadDeathInPostinvoke);
            U.sopShouldNotSeeThis();
        } catch (Throwable t) { U.sop(t);}
        try {
            ridlSLI1.
                throwThreadDeathInSendReply(C.throwThreadDeathInSendReply);
            U.sopShouldNotSeeThis();
        } catch (Throwable t) { U.sop(t);}
        try {
            ridlSLI1.
                throwThreadDeathInServantThenSysInPostThenSysInSendException(
             C.throwThreadDeathInServantThenSysInPostThenSysInSendException);
            U.sopShouldNotSeeThis();
        } catch (Throwable t) { U.sop(t);}
        

        try {
            rrmiiI1.throwThreadDeathInServant(C.throwThreadDeathInServant);
            U.sopShouldNotSeeThis();
        } catch (Throwable t) { U.sop(t); }
        try {
            rrmiiSA.throwThreadDeathInServant(C.throwThreadDeathInServant);
            U.sopShouldNotSeeThis();
        } catch (Throwable t) { U.sop(t); }
        try {
            rrmiiSL.throwThreadDeathInServant(C.throwThreadDeathInServant);
            U.sopShouldNotSeeThis();
        } catch (Throwable t) { U.sop(t); }
    }

    //
    // Interceptors
    //

    public static void testInterceptors()
        throws
            Exception
    {
        HEADER("testInterceptors");

        //
        // This tests that service contexts added by a reply interceptor
        // which then raises a system exception should still be seen.
        //

        try {
            ridlStaticPOA.raiseSystemExceptionInSendReply();
        } catch (Throwable t) {}
        U.expect(U.exception, new IMP_LIMIT(),
                 ridlStaticPOA, C.raiseSystemExceptionInSendReply);

        //
        // Tests server-side PICurrent (and some client-side too).
        //

        Current piCurrent = U.getPICurrent(orb);
        Any any = orb.create_any();
        any.insert_long(0);
        piCurrent.set_slot(SsPicInterceptor.sPic1ASlotId, any);
        // Do not depend on deep copy.
        any = orb.create_any();
        any.insert_long(0);
        piCurrent.set_slot(SsPicInterceptor.sPic1BSlotId, any);
        // debugOn( "null,ridlSLI1" ) ;
        U.expect(U.result, null, // REVISIT: really expecting Void.TYPE
                 ridlSLI1, C.sPic1);
        // debugOff() ;
        // The server's sets to these same slot ids should not
        // effect the client.
        C.testAndIncrementPICSlot(true, "client",
                                  SsPicInterceptor.sPic1ASlotId, 0, piCurrent);

        //
        // test effective_target (this was used when converting from
        // a type-specific version to a more efficient generic version
        // of the effective_target implementation.
        //

        ridlStaticPOA.testEffectiveTarget1();
    }

    //
    // ServantActivator
    //

    public static void testServantActivator()
        throws
            Exception
    {
        HEADER(C.ServantActivator);
        
        
        U.sop(rrmiiSA.sayHello());
        U.expect(U.result, C.helloWorld,
                 rrmiiSA, C.sayHello);
        



        U.sop(ridlSAI1.raiseForwardRequestInIncarnate(C.raiseForwardRequestInIncarnate));
        U.expect(U.result, C.raiseForwardRequestInIncarnate,
                 ridlSAI1, C.raiseForwardRequestInIncarnate,
                 C.raiseForwardRequestInIncarnate);

                  

        try {
            ridlSAIRaiseObjectNotExistInIncarnate
                .raiseObjectNotExistInIncarnate("");
        } catch (Throwable t) {}
        U.expect(U.exception, new OBJECT_NOT_EXIST(),
                 ridlSAIRaiseObjectNotExistInIncarnate,
                 C.raiseObjectNotExistInIncarnate,
                 "");

        
        try {
            ridlSAIRaiseSystemExceptionInIncarnate
                .raiseSystemExceptionInIncarnate("");
        } catch (Throwable t) {}
        U.expect(U.exception, new IMP_LIMIT(),
                 ridlSAIRaiseSystemExceptionInIncarnate,
                 C.raiseSystemExceptionInIncarnate,
                 "");


        U.sop(rrmiiSA.makeColocatedCallFromServant());
        // debugOn( "rmiiColocatedCallResult,makeColocatedCallFromServant" ) ;
        U.expect(U.result, C.rmiiColocatedCallResult,
                 rrmiiSA, C.makeColocatedCallFromServant);
        // debugOff() ;



        // Use "2" to avoid unnecessary ForwardRequest.
        // debugOn( "ridlSAI2.makeColocatedCallFromServant" ) ;
        U.sop(ridlSAI2.makeColocatedCallFromServant());
        // debugOff() ;
        // debugOn( "idlSaIlColcatedCallResut,makeColocatedCallFromServant" ) ;
        U.expect(U.result, C.idlSAI1ColocatedCallResult,
                 ridlSAI2,
                 C.makeColocatedCallFromServant);
        // debugOff() ;
    }
            
    //
    // ServantLocator
    //

    public static void testServantLocator()
        throws
            Exception
    {
        HEADER(C.ServantLocator);
        
        U.sop(rrmiiSL.sayHello());
        U.expect(U.result, C.helloWorld,
                 rrmiiSL, C.sayHello);



        U.sop(ridlSLI1.raiseForwardRequestInPreinvoke(C.raiseForwardRequestInPreinvoke));
        U.expect(U.result, C.raiseForwardRequestInPreinvoke,
                 ridlSLI1, C.raiseForwardRequestInPreinvoke,
                 C.raiseForwardRequestInPreinvoke);


        try {
            ridlSLI1.raiseObjectNotExistInPreinvoke("");
        } catch (Throwable t) {}
        U.expect(U.exception, new OBJECT_NOT_EXIST(),
                 ridlSLI1, C.raiseObjectNotExistInPreinvoke, "");



        try {
            ridlSLI1.raiseSystemExceptionInPreinvoke("");
        } catch (Throwable t) {}
        U.expect(U.exception, new IMP_LIMIT(),
                 ridlSLI1, C.raiseSystemExceptionInPreinvoke, "");


        try {
            ridlSLI1.raiseSystemExceptionInPostinvoke(
                C.raiseSystemExceptionInPostinvoke);
        } catch (Throwable t) {}
        U.expect(U.exception, new IMP_LIMIT(),
                 ridlSLI1, C.raiseSystemExceptionInPostinvoke,
                 C.raiseSystemExceptionInPostinvoke);



        try {
            ridlSLI1.raiseSystemInServantThenPOThenSE();
        } catch (Throwable t) {}
        U.expect(U.exception, new PERSIST_STORE(),
                 ridlSLI1, C.raiseSystemInServantThenPOThenSE);



        try {
            ridlSLI1.raiseUserInServantThenSystemInPOThenSE();
        } catch (Throwable t) {}
        U.expect(U.exception, new PERSIST_STORE(),
                 ridlSLI1, C.raiseUserInServantThenSystemInPOThenSE);




        // debugOn( "rrmiiSL.makeColocatedCallFromServant" ) ;
        U.sop(rrmiiSL.makeColocatedCallFromServant());
        // debugOff() ;
        U.expect(U.result, C.rmiiColocatedCallResult,
                 rrmiiSL, C.makeColocatedCallFromServant);




        U.sop(ridlSLI1.makeColocatedCallFromServant());
        U.expect(U.result, C.idlSLI1ColocatedResult,
                 ridlSLI1, C.makeColocatedCallFromServant);
    }

    //
    // RMI-IIOP
    //

    public static void testRMIIIOP()
        throws
            Exception
    {
        HEADER("RMI-IIOP");

        U.sop(rrmiiI1.sayHello());
        U.expect(U.result, C.helloWorld,
                 rrmiiI1, C.sayHello);

        U.sop(rrmiiI2.sayHello());
        U.expect(U.result, C.helloWorld,
                 rrmiiI2, C.sayHello);
        

        byte[] bytes = new byte[100];
        U.sop(Integer.valueOf(rrmiiI1.sendBytes(bytes)));
        U.expect(U.result, Integer.valueOf(100),
                 rrmiiI1, C.sendBytes, bytes);

        U.sop(rrmiiI1.sendOneObject(new java.util.Hashtable()));
        /*
        // REVISIT:
        // Reflection code in U cannot find the method when given Hashtable.
        Object object = new Object();
        U.expect(U.result, object,
                 rrmiiI1, C.sendOneObject, object);
        */

        /*
          U.sop("equals self : " + rrmiiI1.equals(rrmiiI1));
          U.sop("equals same type : " + rrmiiI1.equals(rrmiiI2));
          U.sop("equals different type: " + rrmiiI1.equals(new Object()));
          U.sop("null sendBytes : " + rrmiiI1.sendBytes(null));
          U.sop("small sendBytes : " + rrmiiI1.sendBytes(new byte[10]));
        */
    }

    //
    // syncOK
    //

    public static void testSynchronousInvocations()
        throws
            Exception
    {
        HEADER(C.syncOK);

        U.sop(ridlStaticPOA.syncOK(C.idlStaticPOA));
        U.sop(doDynInvOfSyncOK(ridlStaticPOA, U.DII(C.idlStaticPOA)));
        U.expect(U.result, idlPOAServant.baseMsg + " " + C.idlStaticPOA,
                 ridlStaticPOA, C.syncOK, C.idlStaticPOA);
        // REVISIT - test DII
        // REVISIT - add static method invocation to U.expect.
                 
        

        U.sop(ridlDynamicPOA.syncOK(C.idlDynamicPOA));
        U.sop(doDynInvOfSyncOK(ridlDynamicPOA, U.DII(C.idlDynamicPOA)));
        U.expect(U.result, U.DSI(C.idlDynamicPOA),
                 ridlDynamicPOA, C.syncOK, C.idlDynamicPOA);
        // REVISIT - test DII

        

        U.sop(ridlStatic.syncOK(C.idlStatic));
        U.sop(doDynInvOfSyncOK(ridlStatic, U.DII(C.idlStatic)));
        U.expect(U.result, idlStaticServant.baseMsg + " " + C.idlStatic,
                 ridlStatic, C.syncOK, C.idlStatic);
        // REVISIT - test DII

        

        U.sop(ridlStaticTie.syncOK(C.idlStaticTie));
        U.expect(U.result, idlStaticServant.baseMsg + " " + C.idlStaticTie,
                 ridlStaticTie, C.syncOK, C.idlStaticTie);
        


        U.sop(ridlDynamic.syncOK(C.idlDynamic));
        U.sop(doDynInvOfSyncOK(ridlDynamic, U.DII(C.idlDynamic)));
        U.expect(U.result, U.DSI(C.idlDynamic),
                 ridlDynamic, C.syncOK, C.idlDynamic);
        // REVISIT - test DII

        

        U.sop(ridlStaticStringified.syncOK(C.idlStaticStringified));
        U.expect(U.result, 
                 idlStaticServant.baseMsg + " " + C.idlStaticStringified,
                 ridlStaticStringified, C.syncOK, C.idlStaticStringified);

    }

    //
    // Special methods.
    //

    public static void testSpecialMethods()
        throws
            Exception
    {
        testIsLocal();
        testGetInterfaceDef();
        testIsA();
        testNonExistent();
    }

    public static void testIsLocal()
        throws
            Exception
    {
        //
        // _is_local
        //
        // Note: the StubAdapter.isLocal checks are commented out because
        // isLocal requires a stub as its argument, but dynamic stubs
        // cannot be used with isLocal.

        HEADER(C._is_local);

        U.sop( StubAdapter.isLocal(ridlStaticPOA) ) ;
        StubWrapper sw = new StubWrapper( ridlStaticPOA ) ;
        U.expect(U.result, Boolean.valueOf(allowLocalOptimization && isColocated),
                 sw, "isLocal" ) ;

        U.sop( StubAdapter.isLocal(ridlStatic) ) ;
        sw = new StubWrapper( ridlStatic ) ;
        U.expect(U.result, Boolean.valueOf(allowLocalOptimization && isColocated),
                 sw, "isLocal" ) ;

        U.sop( StubAdapter.isLocal(rrmiiI1) ) ;
        sw = new StubWrapper( (org.omg.CORBA.Object)rrmiiI1 ) ;
        U.expect(U.result, Boolean.valueOf(allowLocalOptimization && isColocated),
                 sw, "isLocal" ) ;
        //if (StubAdapter.isLocal((Stub)rrmiiI1) != false) {
            //throw new Exception("should be false");
        //}

        U.sop( StubAdapter.isLocal(rrmiiSA) ) ;
        sw = new StubWrapper( (org.omg.CORBA.Object)rrmiiSA ) ;
        U.expect(U.result, Boolean.valueOf(allowLocalOptimization && isColocated),
                 sw, "isLocal" ) ;
        //if (StubAdapter.isLocal((Stub)rrmiiSA) != false) {
            //throw new Exception("should be false");
        //}

        U.sop( StubAdapter.isLocal(rrmiiSL) ) ;
        sw = new StubWrapper( (org.omg.CORBA.Object)rrmiiSL ) ;
        U.expect(U.result, Boolean.valueOf(allowLocalOptimization && isColocated),
                 sw, "isLocal" ) ;
        //if (StubAdapter.isLocal((Stub)rrmiiSL) != false) {
            //throw new Exception("should be false");
        //}
    }

    public static void testGetInterfaceDef()
        throws
            Exception
    {
        //
        // _get_interface_def
        //
        
        HEADER(C._get_interface_def);
        
        getInterfaceDef(ridlStaticPOA, C.idlStaticPOA);
        getInterfaceDef(ridlDynamicPOA, C.idlDynamicPOA);
        getInterfaceDef(ridlStatic, C.idlStatic);
        getInterfaceDef(ridlStaticTie, C.idlStaticTie);
        getInterfaceDef(ridlDynamic, C.idlDynamic);
        refreshIdlAlwaysForward();
        getInterfaceDef(ridlAlwaysForward, C.idlAlwaysForward);
    }

    public static void testIsA()
        throws
            Exception
    {
        //
        // _is_a
        //
        
        HEADER(C._is_a);
        
        // REVISIT - make a call with a real repoID

        U.sop(ridlStaticPOA._is_a(C.idlStaticPOA));
        U.sop(ridlDynamicPOA._is_a(C.idlDynamicPOA));
        U.sop(ridlStatic._is_a(C.idlStatic));
        U.sop(ridlStaticTie._is_a(C.idlStaticTie));
        U.sop(ridlDynamic._is_a(C.idlDynamic));
        refreshIdlAlwaysForward();
        U.sop(ridlAlwaysForward._is_a(C.idlAlwaysForward));

        U.expect(U.result, Boolean.valueOf(false),
                 ridlStaticPOA, C._is_a, C.idlDynamic);
        U.expect(U.result, Boolean.valueOf(false),
                 ridlDynamicPOA, C._is_a, C.idlDynamicPOA);
        U.expect(U.result, Boolean.valueOf(false),
                 ridlStatic, C._is_a, C.idlStatic);
        U.expect(U.result, Boolean.valueOf(false),
                 ridlStaticTie, C._is_a, C.idlStaticTie);
        U.expect(U.result, Boolean.valueOf(false),
                 ridlDynamic, C._is_a, C.idlDynamic);
        refreshIdlAlwaysForward();
        U.expect(U.result, Boolean.valueOf(false),
                 ridlAlwaysForward, C._is_a, C.idlAlwaysForward);
    }

    public static void testNonExistent()
        throws
            Exception
    {
        //
        // _non_existent
        //
        
        HEADER(C._non_existent);
        
        U.sop(ridlStaticPOA._non_existent());
        U.sop(ridlDynamicPOA._non_existent());
        U.sop(ridlStatic._non_existent());
        U.sop(ridlStaticTie._non_existent());
        U.sop(ridlDynamic._non_existent());
        refreshIdlAlwaysForward();
        U.sop(ridlAlwaysForward._non_existent());

        U.expect(U.result, Boolean.valueOf(false),
                 ridlStaticPOA, C._non_existent);
        U.expect(U.result, Boolean.valueOf(false),
                 ridlDynamicPOA, C._non_existent);
        U.expect(U.result, Boolean.valueOf(false),
                 ridlStatic, C._non_existent);
        U.expect(U.result, Boolean.valueOf(false),
                 ridlStaticTie, C._non_existent);
        U.expect(U.result, Boolean.valueOf(false),
                 ridlDynamic, C._non_existent);
        refreshIdlAlwaysForward();
        U.expect(U.result, Boolean.valueOf(false),
                 ridlAlwaysForward, C._non_existent);
    }

    //
    // asyncOK
    //

    public static void testOneWayInvocations()
        throws
            Exception
    {
        HEADER(C.asyncOK);

        // Make this a large iteration to test for async memory leaks.
        for ( int i = 0; i < 1; i++ ) {
            byte[] data;
            
            data = new String(C.idlStaticPOA + " " + (i+1)).getBytes(C.UTF8);
            ridlStaticPOA.asyncOK(data);
            U.expect(U.result, null,
                     ridlStaticPOA, C.asyncOK, data);
            
            data = new String(C.idlDynamicPOA + " " + (i+1)).getBytes(C.UTF8);
            ridlDynamicPOA.asyncOK(data);
            U.expect(U.result, null,
                     ridlDynamicPOA, C.asyncOK, data);
            
            data = new String(C.idlStatic + " " + (i+1)).getBytes(C.UTF8);
            ridlStatic.asyncOK(data);
            U.expect(U.result, null,
                     ridlStatic, C.asyncOK, data);
            
            data = new String(C.idlStaticTie + " " + (i+1)).getBytes(C.UTF8);
            ridlStaticTie.asyncOK(data);
            U.expect(U.result, null,
                     ridlStaticTie, C.asyncOK, data);
            
            data = new String(C.idlDynamic + " " + (i+1)).getBytes(C.UTF8);
            ridlDynamic.asyncOK(data);
            U.expect(U.result, null,
                     ridlDynamic, C.asyncOK, data);
        }
    }

    //
    // throwUserException
    //

    public static void testUserExceptions()
        throws
            Exception
    {
        HEADER(C.throwUserException);

        throwUserException(ridlStaticPOA, C.idlStaticPOA);
        throwUserException(ridlDynamicPOA, C.idlDynamicPOA);
        throwUserException(ridlStatic, C.idlStatic);
        throwUserException(ridlStaticTie, C.idlStaticTie);
        throwUserException(ridlDynamic, C.idlDynamic);
    }

    public static void testSystemExceptions()
        throws
            Exception
    {
        //
        // throwSystemException
        //

        HEADER(C.throwSystemException);

        // Revisit: if SystemException removed the Exception
        // is still caught, but not in same scenario below in POA case
        throwSystemException(ridlStaticPOA, C.idlStaticPOA);
        throwSystemException(ridlDynamicPOA, C.idlDynamicPOA);
        throwSystemException(ridlStatic, C.idlStatic);
        throwSystemException(ridlStaticTie, C.idlStaticTie);
        throwSystemException(ridlDynamic, C.idlDynamic);
    }

    //
    // Unknown
    //

    public static void testUnknownExceptions()
        throws
            Exception
    {
        //
        // throwUnknownException
        //

        HEADER(C.throwUnknownException);
        
        throwUnknownException(ridlStaticPOA, C.idlStaticPOA);
        throwUnknownException(ridlDynamicPOA, C.idlDynamicPOA);
        throwUnknownException(ridlStatic, C.idlStatic);
        throwUnknownException(ridlStaticTie, C.idlStaticTie);
        throwUnknownException(ridlDynamic, C.idlDynamic);
        
        //
        // throwUnknownException
        //

        HEADER(C.throwUNKNOWN);

        throwUNKNOWN(ridlStaticPOA, C.idlStaticPOA);
        throwUNKNOWN(ridlDynamicPOA, C.idlDynamicPOA);
        throwUNKNOWN(ridlStatic, C.idlStatic);
        throwUNKNOWN(ridlStaticTie, C.idlStaticTie);
        throwUNKNOWN(ridlDynamic, C.idlDynamic);
    }

    //
    // Value types
    //

    public static void testValueTypes()
        throws
            Exception
    {
        //
        // sendValue
        //

        HEADER(C.sendValue);

        // REVISIT - add U.expect

        idlValueTypeA idlValueTypeA;
        idlValueTypeB idlValueTypeB = 
            new idlValueTypeBImpl((short)1, (short)2);
        idlValueTypeC idlValueTypeC =
            new idlValueTypeCImpl((short)1, (short)2, (short)3);
        idlValueTypeD idlValueTypeD =
            new idlValueTypeDImpl((short)1);
        idlValueTypeE idlValueTypeE =
            new idlValueTypeEImpl();
        int[] intSeq = new int[10000];
        byte[] octetArray = new byte[10000];

        idlValueTypeA = ridlStaticPOA.sendValue(idlValueTypeB, 
                                                idlValueTypeB,
                                                idlValueTypeC,
                                                idlValueTypeD,
                                                idlValueTypeE,
                                                intSeq,
                                                octetArray);
        U.sop(idlValueTypeA);
        U.sop(idlValueTypeA.equals((idlValueTypeA)idlValueTypeB));

        /*
          idlValueTypeA = ridlDynamicPOA.sendValue(idlValueTypeB,
                                                   idlValueTypeB,
                                                   idlValueTypeC,
                                                   idlValueTypeD,
                                                   idlValueTypeE,
                                                   intSeq,
                                                   octetArray);
            U.sop(idlValueTypeA);
            U.sop(idlValueTypeA.equals((idlValueTypeA)idlValueTypeB));
        */

        idlValueTypeA = ridlStatic.sendValue(idlValueTypeB, 
                                             idlValueTypeB,
                                             idlValueTypeC,
                                             idlValueTypeD,
                                             idlValueTypeE,
                                             intSeq,
                                             octetArray);

        U.sop(idlValueTypeA);
        U.sop(idlValueTypeA.equals((idlValueTypeA)idlValueTypeB));

        idlValueTypeA = ridlStaticTie.sendValue(idlValueTypeB,
                                                idlValueTypeB,
                                                idlValueTypeC,
                                                idlValueTypeD,
                                                idlValueTypeE,
                                                intSeq,
                                                octetArray);

        U.sop(idlValueTypeA);
        U.sop(idlValueTypeA.equals((idlValueTypeA)idlValueTypeB));

        /*
          idlValueTypeA = ridlDynamic.sendValue(idlValueTypeB, 
          idlValueTypeB,
          idlValueTypeC,
          idlValueTypeD,
          idlValueTypeE,
          intSeq,
          octetArray);

          U.sop(idlValueTypeA);
          U.sop(idlValueTypeA.equals((idlValueTypeA)idlValueTypeB));
        */

    }

    //
    // Operations on DSI servants collected from above.
    //

    public static void testDSIInvocations()
        throws
            Exception
    {

        //
        // DSI
        //

        HEADER(U.DSI(""));

        U.sop(ridlDynamicPOA.syncOK(C.idlDynamicPOA));
        U.sop(ridlDynamic.syncOK(C.idlDynamic));
        getInterfaceDef(ridlDynamicPOA, C.idlDynamicPOA);
        getInterfaceDef(ridlDynamic, C.idlDynamic);
        U.sop(ridlDynamicPOA._is_a(C.idlDynamicPOA));
        U.sop(ridlDynamic._is_a(C.idlDynamic));
        U.sop(ridlDynamicPOA._non_existent());
        U.sop(ridlDynamic._non_existent());
        String ddata = C.idlDynamic;
        ridlDynamicPOA.asyncOK(ddata.getBytes(C.UTF8));
        ridlDynamic.asyncOK(ddata.getBytes(C.UTF8));
        throwUserException(ridlDynamicPOA, C.idlDynamicPOA);
        throwUserException(ridlDynamic, C.idlDynamic);
        throwSystemException(ridlDynamicPOA, C.idlDynamicPOA);
        throwSystemException(ridlDynamic, C.idlDynamic);
        throwUnknownException(ridlDynamicPOA, C.idlDynamicPOA);
        throwUnknownException(ridlDynamic, C.idlDynamic);
        throwUNKNOWN(ridlDynamicPOA, C.idlDynamicPOA);
        throwUNKNOWN(ridlDynamic, C.idlDynamic);
    }

    //
    // Utilities.
    //

    public static String doDynInvOfSyncOK(idlI ref, String st)
    {
        org.omg.CORBA.Request r = ref._request(C.syncOK);
        r.set_return_type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string));
        org.omg.CORBA.Any _s = r.add_in_arg();
        _s.insert_string(st);
        r.invoke();
        String result = r.return_value().extract_string();
        return result;
    }

    public static void getInterfaceDef(org.omg.CORBA.Object ref, String msg)
    {
        try {
            U.sop(ref._get_interface_def());
        } catch (Exception e) {
            U.sopOK(msg + " " + e);
        }
        U.expect(U.exception, new NO_IMPLEMENT(),
                 ref, C._get_interface_def);
    }

    public static final int USER = 0;
    public static final int SYSTEM = 1;
    public static final int UnknownException = 2;
    public static final int UNKNOWN = 3;

    public static void throwUserException(idlI ref, String msg)
    {
        throwException(USER, ref, msg);
    }

    public static void throwSystemException(idlI ref, String msg)
    {
        throwException(SYSTEM, ref, msg);
    }

    public static void throwUnknownException(idlI ref, String msg)
    {
        throwException(UnknownException, ref, msg);
    }

    public static void throwUNKNOWN(idlI ref, String msg)
    {
        throwException(UNKNOWN, ref, msg);
    }

    public static void throwException(int kind, idlI ref, String msg)
    {
        try {
            switch (kind) {
            case USER :

                U.expect(U.exception, new idlExampleException(),
                         ref, C.throwUserException);

                msg = msg + "." + C.throwUserException + " - ";
                ref.throwUserException();
                break;

            case SYSTEM :

                U.expect(U.exception, new IMP_LIMIT(),
                         ref, C.throwSystemException);

                msg = msg + "." + C.throwSystemException + " - ";
                ref.throwSystemException();
                break;

            case UnknownException :

                /* REVISIT - need to better understand how 
                   UnknownException is used.

                U.expect(U.exception,
                         new UnknownException(new Throwable()),
                         ref, C.throwUnknownException);
                */

                msg = msg + "." + C.throwUnknownException + " - ";
                ref.throwUnknownException();
                break;

            case UNKNOWN :

                /* REVISIT - need to better understand how 
                   UNKNOWN is used.

                U.expect(U.exception, new UNKNOWN(),
                         ref, C.throwUnknownException);
                */

                msg = msg + "." + C.throwUNKNOWN + " - ";
                ref.throwUNKNOWN();
                break;

            default :
                throw new RuntimeException(baseMsg + ".throwException" + 
                                           " unknown kind: " + kind);
            }
            U.sopShouldNotSeeThis(msg);
        } catch (SystemException e) { 
            switch (kind) {
            case SYSTEM :
            case UnknownException :
            case UNKNOWN :
                U.sopOK(msg + "SystemException " + e);
                break;
            default :
                U.sopUnexpectedException(msg, e);
            }
        } catch (Exception e) { 
            switch (kind) {
            case USER :
                U.sopOK(msg + "Exception " + e);
                break;
            default :
                U.sopUnexpectedException(msg, e);
            }
        }
    }

    public static void HEADER(java.lang.Object x)
    {
        U.HEADER(x);
        // ridlHEADERI.HEADER((String)x);
    }

    public static void testExpect()
    {
        // REVISIT - add null return value test.
        // REVISIT - add void return value test.

        if (testExpect) {

            // _result, _result
            U.expect(U.result,
                     "bad on purpose",
                     rrmiiSA, C.sayHello);

            // _result _exception
            U.expect(U.result,
                     "phony result",
                     ridlSAIRaiseObjectNotExistInIncarnate,
                     C.raiseObjectNotExistInIncarnate,
                     "");

            // _exception _result
            U.expect(U.exception,
                     new OBJECT_NOT_EXIST(),
                     ridlSAI1, C.raiseForwardRequestInIncarnate,
                     C.raiseForwardRequestInIncarnate);

            // _exception _exception
            U.expect(U.exception,
                     new IMP_LIMIT(),
                     ridlSAIRaiseObjectNotExistInIncarnate,
                     C.raiseObjectNotExistInIncarnate,
                     "");
        }
    }

    // REVISIT: move this into U.
    // Not used.
    public static void checkSystemException(SystemException expected,
                                            int minorCode,
                                            CompletionStatus completionStatus,
                                            Throwable got)
    {
        if (! (got instanceof SystemException)) {
            throw new RuntimeException("expected SystemException but got: " 
                                       + got );
        }

        SystemException gotSex = (SystemException)got;

        if (! expected.getClass().isInstance(gotSex)) {
            throw new RuntimeException("SEX: wrong nested SystemException type, got: " + gotSex);
        }

        if (gotSex.minor != minorCode) {
            throw new RuntimeException("SEX: wrong minor code: " + gotSex.minor);
        }

        if (gotSex.completed != completionStatus) {
            throw new RuntimeException("SEX: wrong completion status: " + gotSex.completed);
        }
    }


    public static void checkRmiMarshalException(
        Throwable expected,
        SystemException alternateExpected,
        Throwable got)
    {
        if (! (got instanceof MarshalException)) {
            throw new RuntimeException("RMI: excepted java.rmi.MarshalException but got: " + got);
        }

        Throwable detail = ((MarshalException)got).detail;
        
        if (! (detail.getClass().isInstance(expected))) {
            if (! (detail.getClass().isInstance(alternateExpected))) {
                throw new RuntimeException("RMI: wrong nested exception: " +detail);
            }
            MARSHAL gotMarshal = (MARSHAL)detail;
            if (gotMarshal.minor != alternateExpected.minor) {
                throw new RuntimeException("RMI: wrong minor code: " +
                                           " expected: " + alternateExpected.minor +
                                           " got: " + gotMarshal.minor);
            }
            if (gotMarshal.completed != alternateExpected.completed) {
                throw new RuntimeException("RMI: wrong completion status: " +
                                           gotMarshal.completed);
            }
        }
    }
}

// End of file.

