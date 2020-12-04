/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
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

package javax.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.NoSuchObjectException;
import java.rmi.server.ExportException;
import test.ServantContext;
import test.RemoteTest;
import com.sun.corba.ee.impl.util.Utility;
import org.omg.CORBA.ORB;
import org.omg.CORBA.SystemException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import javax.rmi.CORBA.Util;
import javax.rmi.CORBA.Tie;
import javax.naming.Context;
import com.sun.org.omg.SendingContext.CodeBase;
import alpha.bravo.Multi;

import com.sun.corba.ee.spi.presentation.rmi.StubAdapter ;
import com.sun.corba.ee.spi.presentation.rmi.PresentationManager ;
import org.glassfish.pfl.test.JUnitReportHelper;

/*
 * @test
 */
public class PROTest extends RemoteTest {

    private static final String servantName     = "PROServer";
    private static final String servantClass    = "javax.rmi.PROImpl";
    private static final String[] compileEm     =   {
        "javax.rmi.PROImpl",
        "javax.rmi.PROImpl2",
        "javax.rmi.DogServer",
        "javax.rmi.ServantInner",
        "javax.rmi.ServantOuter.Inner",
        "rmic.OnlyRemoteServant",
        "javax.rmi.HashCodeImpl",
        "javax.rmi.HashCodeAImpl",
        "alpha.bravo.Multi",
    };

    private static final int TIMING_ITERATIONS = 100;

    /**
     * Return an array of fully qualified remote servant class
     * names for which ties/skels need to be generated. Return
     * empty array if none.
     */
    protected String[] getRemoteServantClasses () {
        return compileEm;  
    }

    /**
     * Append additional (i.e. after -iiop and before classes) rmic arguments
     * to 'currentArgs'. This implementation will set the output directory if
     * the OUTPUT_DIRECTORY flag was passed on the command line.
     */
    protected String[] getAdditionalRMICArgs (String[] currentArgs) {
        if (iiop) {
            String[] ourArgs = {"-always", "-keep"};
            return super.getAdditionalRMICArgs(ourArgs);
        } else {
            return super.getAdditionalRMICArgs(currentArgs);
        }
    }

    private final JUnitReportHelper helper ;
    private boolean first = true ;

    public PROTest() {
        helper = new JUnitReportHelper( this.getClass().getName() 
            + ( iiop ? "_iiop" : "_jrmp" ) ) ;
    }

    private void newTest( String name ) {
        if (first)
            first = false ;
        else
            helper.pass() ;

        helper.start( name ) ;
    }

    /**
     * Perform the test.
     * @param context The context returned by getServantContext().
     */
    public void doTest (ServantContext context) throws Throwable {
        try {
            dprint( "test starts" ) ;
            boolean usesDynamicStubs = 
                com.sun.corba.ee.spi.orb.ORB.getPresentationManager().
                    useDynamicStubs() ;
            
            // Certain tests that depend on the absence of iiop stubs and ties
            // cannot function correctly with dynamic RMI-IIOP, since 
            // dynamic RMI-IIOP can always create any needed stub or tie.
            // We assume that JRMP is only usable when we are not using
            // dynamic RMI-IIOP.  Since we test the JRMP case in static mode
            // anyway, we'll just return here in the dynamic case for JRMP.
            if (usesDynamicStubs && !iiop)
                return ;

            // First ensure that the caches are cleared out so
            // that we can switch between IIOP and JRMP...
            
            Utility.clearCaches();
            
            // Check toStub(). First try an unconnected servant...

            PROImpl localImpl = new PROImpl();
            Remote stub = PortableRemoteObject.toStub(localImpl);
            boolean fail = false;
            
            newTest( "test_1" ) ;
            ORB defaultORB = context.getORB();
            
            if (iiop) {
                Tie tie = Util.getTie(localImpl);
                tie.orb(defaultORB);
            }
            
            stub = PortableRemoteObject.toStub(localImpl);
            if (stub == null) {
                throw new Exception ("toStub() on connected servant failed.");
            }

            // Make sure second export fails...
            newTest( "test_2" ) ;
            boolean exportFail = false;
            try {
                PortableRemoteObject.exportObject(localImpl);
            } catch (ExportException e) {
                exportFail = true;
            }

            if (!exportFail) {
                throw new Exception ("exportObject twice did not fail");
            }

            // Try narrow on local stub...
            newTest( "test_3" ) ;
            PROHello stubref = (PROHello) PortableRemoteObject.narrow(stub,PROHello.class);

            if (stubref == null) {
                throw new Exception ("narrow() failed for stub");
            }

            newTest( "test_4" ) ;
            Remote remote = context.startServant(servantClass,servantName,true,iiop);

            if (remote == null) {
                throw new Exception ("startServant() failed");
            }


            newTest( "test_5" ) ;
            PROHello objref = (PROHello) PortableRemoteObject.narrow(remote,PROHello.class);
            if (objref == null) {
                throw new Exception ("narrow() failed for remote");
            }

            newTest( "test_6" ) ;
            if (!objref.sayHello().equals(PROHello.HELLO)) {
                throw new Exception("sayHello() failed");
            }

            newTest( "test_7" ) ;
            String bark;
            Dog dogValue = objref.getDogValue ();
            if ( dogValue == null ) {
                throw new Exception ("sayHello() dogValue is null");
            }
            bark = dogValue.bark ();

            newTest( "test_8" ) ;
            Dog dogServer = objref.getDogServer ();
            if ( dogValue == null ) {
                throw new Exception ("sayHello() dogServer is null");
            }
            bark = dogServer.bark ();

            newTest( "test_9" ) ;
            boolean unexportFail = false;
            try {
                PortableRemoteObject.unexportObject(objref);
            } catch (NoSuchObjectException e) {
                unexportFail = true;
            }
            
            if (!unexportFail) {
                throw new Exception("unexport of stub succeeded!");
            }
        
            // Now get a round-trip timing...
            newTest( "test_10" ) ;

            if (verbose) {
                int count = TIMING_ITERATIONS;
                long startTime = System.currentTimeMillis();
                while (count-- > 0) {
                    String hello = objref.sayHello();
                }
                long roundTrip = (System.currentTimeMillis() - startTime) / TIMING_ITERATIONS;
                System.out.print("Round-trip time for sayHello() = "
                                 + roundTrip + " ms average over " + TIMING_ITERATIONS + " iterations.");
            }

            newTest( "test_11" ) ;
            // Now check stub streaming and connect(stub,stub)...
            
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(bos);
            os.writeObject(objref);
            
            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            ObjectInputStream is = new ObjectInputStream(bis);
            PROHello newRef = (PROHello) is.readObject();

            if (newRef == null) {
                throw new Exception("Stub streaming failed.");   
            }

            newTest( "test_12" ) ;
            fail = false;
            try {
                newRef.sayHello();
            } catch (RemoteException e) {
                if (e.getMessage().startsWith("CORBA BAD_OPERATION 0")) {
                    fail = true;
                }
            }
            if (iiop && !fail) {
                throw new Exception("sayHello on unconnected stub succeeded.");   
            }
            
            newTest( "test_13" ) ;
            // Now connect it up (stub,stub) and make sure we can call
            // methods on it...
            
            PortableRemoteObject.connect(newRef,objref);
            if (!newRef.sayHello().equals(PROHello.HELLO)) {
                throw new Exception("connect(stub,stub) failed");
            }
            
            // Now make sure that they are equal...
            
            if (newRef.hashCode() != objref.hashCode()) {
                throw new Exception("newRef.hashCode() != objref.hashCode()");
            }
            
            if (!newRef.equals(objref)) {
                throw new Exception("newRef != objref");
            }

            newTest( "test_14" ) ;
            // Try connect(impl,impl)...

            PROImpl localImpl2 = new PROImpl();
            PortableRemoteObject.connect(localImpl2,localImpl);
            try {
                PortableRemoteObject.toStub(localImpl2);
            } catch (NoSuchObjectException e) {
                throw new Exception ("connect(impl,impl) failed");
            }

            // Try connect(stub,impl)...

            newTest( "test_15" ) ;
            ObjectInputStream is2 = new ObjectInputStream(new ByteArrayInputStream(
                bos.toByteArray()));
            PROHello newRef2 = (PROHello) is2.readObject();
            PortableRemoteObject.connect(newRef2,localImpl2);
            if (!newRef.sayHello().equals(PROHello.HELLO)) {
                throw new Exception("connect(stub,impl) failed");
            }

            newTest( "test_16" ) ;
            // Try connect(impl,stub)...
            
            PROImpl localImpl3 = new PROImpl();
            PortableRemoteObject.connect(localImpl3,objref);
            try {
                PortableRemoteObject.toStub(localImpl3);
            } catch (NoSuchObjectException e) {
                throw new Exception ("connect(impl,stub) failed");
            }

            newTest( "test_17" ) ;
            // Make sure that trying to connect an already connected object
            // succeeds when the ORBs are the same...
            
            PortableRemoteObject.connect(objref,localImpl3);
            PortableRemoteObject.connect(localImpl3,objref);

            newTest( "test_18" ) ;
            // Make sure that trying to connect an already connected object fails
            // when the ORBs are different...
            
            PROImpl newLocalImpl = new PROImpl();
            if (iiop) {
                ORB newORB = ORB.init(new String[]{},null);
                Tie newTie = Util.getTie(newLocalImpl);
                newTie.orb(newORB);
            }
            PROHello newObjRef = (PROHello) PortableRemoteObject.toStub(newLocalImpl);
            
            boolean callFailed = false;
            try {
                PortableRemoteObject.connect(objref,newLocalImpl);
            } catch (RemoteException e) {
                callFailed = true;   
            }
            if (!callFailed) {
                if (iiop) {
                    throw new Exception ("Second connect(stub,impl) succeeded");
                } else {                
                    // System.out.println("REMINDER: document connect and JRMP!");                
                }
            }

            callFailed = false;
            try {
                PortableRemoteObject.connect(localImpl3,newObjRef);
            } catch (RemoteException e) {
                callFailed = true;   
            }
            if (!callFailed) {
                if (iiop) {
                    throw new Exception ("Second connect(impl,stub) succeeded");
                } else {                
                    // System.out.println("REMINDER: document connect and JRMP!");                
                }
            }

            if (iiop) {
                newTest( "test_19" ) ;
                // Now export an implementation, get a stub for it, make sure they
                // are both unconnected, then connect the stub and make sure they
                // are *both* connected...

                PROImpl theImpl = new PROImpl();
                Tie theTie = Util.getTie(theImpl);
                org.omg.CORBA.Object theStub = 
                    (org.omg.CORBA.Object)PortableRemoteObject.toStub(theImpl);
                callFailed = false;
                try {
                    theTie.orb();
                } catch (SystemException e) {
                    callFailed = true;
                }
                if (!callFailed) {
                    throw new Exception ("theTie already connected");
                }
                callFailed = false;
                try {
                    StubAdapter.getDelegate( theStub );
                } catch (SystemException e) {
                    callFailed = true;
                }
                if (!callFailed) {
                    throw new Exception ("theStub already connected");
                }

                StubAdapter.connect( theStub, defaultORB ); // Connect both!
                if (theTie.orb() != defaultORB) {
                    throw new Exception("theTie.orb() != defaultORB");
                }
                if (StubAdapter.getDelegate( theStub ).orb(theStub) != defaultORB) {
                    throw new Exception("theStub.orb() != defaultORB");
                }

                if (Utility.getAndForgetTie(theStub) != null) {
                    throw new Exception("Utility.getAndForgetTie(theStub) != null");
                }
            }
     
            // Now repeat the same test, only this time use JNDI to do the
            // connect on the stub, to insure that our updated CNCtx code
            // with "auto-connect" works correctly...

            Context nameContext = context.getNameContext();
            
            if (iiop) {
                newTest( "test_20" ) ;    
                PROImpl theImpl = new PROImpl();
                Tie theTie = Util.getTie(theImpl);
                org.omg.CORBA.Object theStub = 
                    (org.omg.CORBA.Object)PortableRemoteObject.toStub(theImpl);
                callFailed = false;
                try {
                    theTie.orb();
                } catch (SystemException e) {
                    callFailed = true;
                }
                if (!callFailed) {
                    throw new Exception ("(nameContext) theTie already connected");
                }
                callFailed = false;
                try {
                    StubAdapter.getDelegate( theStub );
                } catch (SystemException e) {
                    callFailed = true;
                }
                if (!callFailed) {
                    throw new Exception ("(nameContext) theStub already connected");
                }

                nameContext.rebind("PROTest auto-connect",theStub); // Connect both!
                
                if (theTie.orb() != defaultORB) {
                    throw new Exception("(nameContext) theTie.orb() != defaultORB");
                }
                if (StubAdapter.getDelegate( theStub ).orb(theStub) != defaultORB) {
                    throw new Exception("(nameContext) theStub.orb() != defaultORB");
                }

                if (Utility.getAndForgetTie(theStub) != null) {
                    throw new Exception("(nameContext) Utility.getAndForgetTie(theStub) != null");
                }
            }
            
            // Now unexport remote object and make sure we can no longer invoke it...
            newTest( "test_21" ) ;

            objref.unexport();
            callFailed = false;
            try {
                objref.sayHello();
            } catch (RemoteException e) {
                callFailed = true;
            }

            if (!callFailed) {
                if (iiop) {
                    throw new Exception("unexportObject() failed");
                } else {
                    // System.out.print("Warning: PortableRemoteObject.unexportObject() NOP on pre 1.2 JRMP!");
                }
            }

            // Now unexport our local instance...
            newTest( "test_22" ) ;

            PortableRemoteObject.unexportObject(localImpl);

            // Make sure we cannot unexport an object which was never exported...
            newTest( "test_23" ) ;
            
            fail = false;
            try {
                PortableRemoteObject.unexportObject(new PROImpl2()  );
            } catch (NoSuchObjectException e) {
                fail = true;
            }
            
            if (!fail) {
                if (iiop) {
                    throw new Exception ("unexportObject() on unconnected servant succeeded.");
                } else {
                    // System.out.print("Warning: unexportObject() on unconnected servant succeeded on JRMP!");
                }
            }
            
            newTest( "test_24" ) ;
            // Now make sure that trying to publish an unexported impl
            // fails...

            callFailed = false;
            try {
                nameContext.rebind("foo",new PROImpl2());
            } catch (javax.naming.NamingException e) {
                Throwable cause = e.getRootCause();
                if (cause != null) {
                    if (cause instanceof java.rmi.NoSuchObjectException) {
                        callFailed = true;
                    } else {
                        callFailed = true;
                        if (iiop) {
                            System.out.print("Warning: Publish unexported impl root cause: " + cause.getClass().getName());
                        }
                    }
                }
            } catch (Exception e) {
                callFailed = true;
                System.out.print("Warning: Publish unexported impl caught: " + e);
            }

            if (!callFailed) {
                if (iiop) {
                    throw new Exception("Publish unexported impl succeeded!");
                } else {
                    // System.out.print("Warning: Publish unexported impl succeeded on 1.1.6/JRMP!");
                }
            }

            // Now make sure that we cannot call toStub with our unexported object...
            newTest( "test_25" ) ;

            callFailed = false;
            try {
                PortableRemoteObject.toStub(localImpl);
            } catch (NoSuchObjectException e) {
                callFailed = true;
            }

            if (!callFailed) {
                if (iiop) {
                    throw new Exception("toStub on unexported impl succeeded!");
                } else {
                    // System.out.print("Warning: toStub on unexported impl succeeded on JRMP!");
                }
            }
            
            newTest( "test_26" ) ;
            // Now fire up our servant which implements an inner interface and
            // make sure we can talk to it...
     
            Remote inner = context.startServant("javax.rmi.ServantInner","inner",false,iiop);
            SInner innerRef = (SInner) 
                PortableRemoteObject.narrow(inner,SInner.class);
            if (!innerRef.echo(innerRef).equals(innerRef)) {
                throw new Exception("innerRef.echo(innerRef) != innerRef");
            }
           
            if (iiop) { // _REVISIT_ This does not work on JRMP - why not?
                newTest( "test_27" ) ;
                // Now fire up our servant which implements an outer interface and
                // make sure we can talk to it...
     
                Remote outer = context.startServant("javax.rmi.ServantOuter$Inner","outer",false,iiop);
                ServantOuter outerRef = (ServantOuter) 
                    PortableRemoteObject.narrow(outer,ServantOuter.class);
                if (!outerRef.echo(outerRef).equals(outerRef)) {
                    throw new Exception("innerRef.echo(outerRef) != outerRef");
                }
            }
            
            newTest( "test_28" ) ;
            // Make sure we can pass an IDL reference across our RMI stub...
            CodeBase cb = innerRef.getCodeBase();
            if (cb == null) {
                throw new Exception("innerRef.getCodeBase() == null");
            }
            
            newTest( "test_29" ) ;
            // Make sure we can pass a servant which only implements Remote...
            Remote r = innerRef.getOnlyRemote();
            if (r == null) {
                throw new Exception("innerRef.getOnlyRemote() == null");
            }
            
            newTest( "test_30" ) ;
            // Make sure we can get a stub for a servant which only implements
            // Remote...
            Remote onlyRemoteStub = PortableRemoteObject.toStub(
                new rmic.OnlyRemoteServant());
            if (onlyRemoteStub == null) {
                throw new Exception("onlyRemoteStub == null");
            }
            
            newTest( "test_31" ) ;
            // Hashcode regression test. Ensure that stubs for two distincts types
            // have different hashCodes. This code is (effectively) a copy of the
            // Sun East HashCodeTests.HashCode0002() method.
            
            // This is really a bogus test, as it can always fail the hashcode
            // comparison, but not be equal...
            
            nameContext.rebind("HashCode",new HashCodeImpl());
            nameContext.rebind("HashCodeA",new HashCodeAImpl());
            Object hashCodeObject = nameContext.lookup("HashCode");
            Object hashCodeAObject = nameContext.lookup("HashCodeA");
            HashCode hashCodeStub = 
                (HashCode)PortableRemoteObject.narrow(hashCodeObject,HashCode.class);
            HashCodeA hashCodeAStub = 
                (HashCodeA)PortableRemoteObject.narrow(hashCodeAObject,HashCodeA.class);        
            int hashCode = hashCodeStub.hashCode();
            int hashCodeA = hashCodeAStub.hashCode();
            
            if (hashCode == hashCodeA) {
                System.out.println("hashCode == hashCodeA ("+hashCode+")");
                if (hashCodeStub.equals(hashCodeAStub)) {
                    throw new Exception("hashCodeStub.equals(hashCodeAStub)");
                }
            }
            
            // RegisterTarget regression test.  The Stub was being cached for
            // the tie, and it's delegate was not cleared. The stub is now
            // removed from the cache by the Util.unexportObject() method.
            if (iiop) {
                newTest( "test_32" ) ;
                // Create/get tie for impl...
                
                PROImpl obj = new PROImpl();
                Tie tie = Util.getTie(obj);
                
                nameContext.rebind("RegisterTarget", obj);
                PortableRemoteObject.unexportObject(obj);
                Util.registerTarget(tie, obj);
                nameContext.rebind("RegisterTarget", obj);
                
                // Lookup it up and make sure it is alive...
                Object registerObject = nameContext.lookup("RegisterTarget");
                PROHello registerRef = (PROHello)PortableRemoteObject.narrow(registerObject, PROHello.class);
                if (!registerRef.sayHello().equals(PROHello.HELLO)) {
                    throw new Exception("RegisterTarget failed");
                }
            }
      
            // Ensure that Utility.loadStub() manages cache correctly...
            if (iiop) {
                newTest( "test_33" ) ;
                // Get a connected tie...
                Multi servant = new Multi();
                Tie tie = Util.getTie(servant);
                tie.orb(defaultORB);
                String interfaceName;
                String stubRepoId;
                
                // Load A...
                Utility.clearCaches();
                interfaceName = "alpha.bravo.A";
                stubRepoId = "RMI:alpha.bravo.A:0000000000000000" ;
                testLoadStub( servant, tie, interfaceName, stubRepoId, false ) ;

                // Load B...
                Utility.clearCaches();
                interfaceName = "alpha.bravo.B";
                stubRepoId = "RMI:alpha.bravo.B:0000000000000000" ;
                testLoadStub( servant, tie, interfaceName, stubRepoId, false ) ;

                // Load DuckBill...
                Utility.clearCaches();
                interfaceName = "alpha.bravo.DuckBill";
                stubRepoId = "RMI:alpha.bravo.DuckBill:0000000000000000" ;
                testLoadStub( servant, tie, interfaceName, stubRepoId, false ) ;

                // Reload A with onlyMostDerived, and ensure that we get Multi
                interfaceName = "alpha.bravo.A";
                stubRepoId = "RMI:alpha.bravo.Multi:0000000000000000" ;
                testLoadStub( servant, tie, interfaceName, stubRepoId, true ) ;

                // Reload A , and ensure that we still get Multi
                interfaceName = "alpha.bravo.A";
                stubRepoId = "RMI:alpha.bravo.Multi:0000000000000000" ;
                testLoadStub( servant, tie, interfaceName, stubRepoId, false ) ;
                
                // Load A...
                Utility.clearCaches();
                interfaceName = "alpha.bravo.A";
                stubRepoId = "RMI:alpha.bravo.A:0000000000000000" ;
                testLoadStub( servant, tie, interfaceName, stubRepoId, false ) ;

                // Reload A with onlyMostDerived, and ensure that we get Multi
                interfaceName = "alpha.bravo.A";
                stubRepoId = "RMI:alpha.bravo.Multi:0000000000000000" ;
                testLoadStub( servant, tie, interfaceName, stubRepoId, true ) ;
            }

            if (!first)
                helper.pass() ;
        } catch (Throwable thr) {
            helper.fail( thr ) ;
        } finally {
            helper.done() ;
        }
    }
    
    private void testLoadStub( Multi servant, Tie tie, 
        String interfaceName, String repoId, boolean flag ) throws Exception
    {
        PresentationManager.StubFactoryFactory sff = 
            com.sun.corba.ee.spi.orb.ORB.getStubFactoryFactory() ;
        PresentationManager.StubFactory stubFactory = 
            sff.createStubFactory( interfaceName, false, null, null, null ) ;
        Remote stub = Utility.loadStub( tie, stubFactory, 
            null, flag ) ;
        String actualRepoId = StubAdapter.getTypeIds( stub )[0] ;

        if (!actualRepoId.equals( repoId )) {
            throw new Exception( "Utility.loadStub: expected " + repoId +
                " got " + actualRepoId ) ;
        }
    }

}
