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

package pi.iorinterceptor;

import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;
import com.sun.corba.ee.spi.ior.iiop.IIOPProfile ;
import com.sun.corba.ee.spi.ior.iiop.IIOPProfileTemplate ;
import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.ior.IORFactories;

import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.impl.encoding.EncapsInputStream;
import com.sun.corba.ee.impl.encoding.EncapsOutputStream;
import com.sun.corba.ee.impl.ior.GenericIdentifiable ;
import corba.framework.InternalProcess;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import org.glassfish.pfl.test.JUnitReportHelper;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Policy;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextHelper;
import org.omg.PortableServer.IdUniquenessPolicyValue;
import org.omg.PortableServer.POA;

public class Server 
    implements InternalProcess 
{
    JUnitReportHelper helper = new JUnitReportHelper( Server.class.getName() ) ;

    private static final String ROOT_POA = "RootPOA";

    private POA rootPOA;
    private POA policyPOA;
    
    // Set from run()
    private PrintStream out;
    private PrintStream err;
    private ORB orb;

    public static void main(String args[]) {
        try {
            (new Server()).run( System.getProperties(),
                                args, System.out, System.err, null );
        }
        catch( Exception e ) {
            e.printStackTrace( System.err );
            System.exit( 1 );
        }
    }

    public void run( Properties environment, String args[], PrintStream out,
                     PrintStream err, Hashtable extra) 
        throws Exception
    {
        try {
            this.out = out;
            this.err = err;

            out.println( "Instantiating ORB" );
            out.println( "=================" );

            // Initializer class
            String testInitializer = "pi.iorinterceptor.ServerTestInitializer";
            ServerTestInitializer.out = out;

            // create and initialize the ORB
            Properties props = new Properties() ;
            props.put( "org.omg.CORBA.ORBClass", 
                       System.getProperty("org.omg.CORBA.ORBClass"));
            props.put( ORBConstants.PI_ORB_INITIALIZER_CLASS_PREFIX +
                       testInitializer, "" );
            orb = ORB.init(args, props);
            ServerTestInitializer.orb = orb;

            // Get root POA:
            out.println( "Server retrieving root POA:" );
            rootPOA = (POA)orb.resolve_initial_references( "RootPOA" );
            rootPOA.the_POAManager().activate();

            // Create another child POA, with a set of policies.
            out.println( "Server creating child POA with some policies..." );
            Policy[] policies = new Policy[2];

            // _REVISIT_ Once our orb is CORBA 2.4 compliant, this test can
            // make use of orb.create_policy for the POA policies as well.

            // Insert two standard policies and one custom policy:
            policies[0] = rootPOA.create_id_uniqueness_policy( 
                IdUniquenessPolicyValue.MULTIPLE_ID );
            Any value = orb.create_any();
            value.insert_long( 99 );
            policies[1] = orb.create_policy( 100, value );

            policyPOA = rootPOA.create_POA( "PolicyPOA", null, policies );
            policyPOA.the_POAManager().activate();

            // Note at this point, if establish_components does not properly
            // handle exceptions, the exception will be passed here and thrown
            // before the server gets its handshake in, which will cause the
            // test to fail.  This tests whether the implementation properly
            // handles exceptions in establish_components.  If we got this
            // far, exceptions are handled properly considering the 
            // NPEIORInterceptor is registered.
            out.println( "NullPointerException handled gracefully (ok)" );
            
            // Check to make sure all interceptors are registered:
            checkRegistered();

            // Check to make sure all establish_components calls were made:
            checkEstablishComponentsCalled();

            // Check to make sure all establish_components calls all passed:
            checkEstablishComponentsPassed();
            
            // Check to make sure tagged components are inserted into IORs.
            checkTaggedComponentsPresent();
        } finally {
            helper.done() ;
                   
            //handshake:
            out.println("Server is ready.");
            out.flush();

            // wait for invocations from clients
            java.lang.Object sync = new java.lang.Object();
            synchronized (sync) {
                sync.wait();
            }
        }
    }

    // Check to make sure all interceptors were registered
    private void checkRegistered() {
        helper.start( "checkRegistered" ) ;

        try {
            out.println( "Checking if interceptors were registered..." );
            if( !NPEIORInterceptor.registered ) {
                throw new RuntimeException( 
                    "NPEIORInterceptor never registered!");
            }
            out.println( "    - NPEIORInterceptor was registered." );
            if( !SampleIORInterceptor.registered ) {
                throw new RuntimeException( 
                    "SampleIORInterceptor never registered!");
            }
            out.println( "    - SampleIORInterceptor was registered." );

            helper.pass() ;
        } catch (RuntimeException exc) {
            helper.fail( exc ) ;
            throw exc ;
        }
    }

    // Checks to make sure establish_components was called on all 
    // IORInterceptors.
    private void checkEstablishComponentsCalled() {
        helper.start( "checkEstablishComponentsCalled" ) ;

        try {
            out.println( "Checking if establish_components called..." );
            if( !NPEIORInterceptor.establishComponentsCalled ) {
                throw new RuntimeException( 
                    "NPEIORInterceptor.establish_components never called!");
            }
            out.println( "    - NPEIORInterceptor.establish_components() called.");
            if( !SampleIORInterceptor.establishComponentsCalled ) {
                throw new RuntimeException( 
                    "SampleIORInterceptor.establish_components never called!");
            }
            out.println( "    - SampleIORInterceptor.establish_components() " + 
                "called.");
            helper.pass() ;
        } catch (RuntimeException exc) {
            helper.fail( exc ) ;
            throw exc ;
        }
    }

    // Checks to make sure establish_components passed on all 
    // IORInterceptors.
    private void checkEstablishComponentsPassed() {
        helper.start( "checkEstablishComponentsPassed" ) ;

        try {
            out.println( "Checking if establish_components passed..." );
            if( !SampleIORInterceptor.establishComponentsPassed ) {
                throw new RuntimeException( 
                    "SampleIORInterceptor.establish_components did not pass!");
            }
            out.println( "    - SampleIORInterceptor.establish_components() " + 
                "passed.");

            helper.pass() ;
        } catch (RuntimeException exc) {
            helper.fail( exc ) ;
            throw exc ;
        }
    }
    
    // Creates an object and checks its IOR to make sure the tagged
    // components added by the IOR Interceptors are actually present.
    private void checkTaggedComponentsPresent() throws Exception {
        helper.start( "checkTaggedComponentsPresent" ) ;

        try {
            out.println( "Checking if tagged components are present..." );
            
            // Create an object:
            out.println( "    + Creating sample object and getting IOR..." );
            org.omg.CORBA.Object obj = createSampleObject();
            
            // Obtain the IOR for this object by writing it to an OutputStream
            // and reading it back from an input stream.
            EncapsOutputStream encapsOutputStream = new EncapsOutputStream( 
                (com.sun.corba.ee.spi.orb.ORB)orb, GIOPVersion.V1_2 );
            encapsOutputStream.write_Object( obj );
            EncapsInputStream encapsInputStream = 
                (EncapsInputStream)encapsOutputStream.create_input_stream();
            IOR ior = IORFactories.makeIOR( 
                (com.sun.corba.ee.spi.orb.ORB)orb, encapsInputStream );
            
            // Check if the appropriate tagged components are present in the IOR.
            out.println( "    + Searching for tagged components..." );
            IIOPProfile profile = ior.getProfile();
            IIOPProfileTemplate template = 
                (IIOPProfileTemplate)profile.getTaggedProfileTemplate();
            
            // The template is a List of TaggedComponent objects.  We are 
            // interested in the tagged components with the IDs 
            // SampleIORInterceptor.FAKE_TAG_1 and FAKE_TAG_2.
            // FAKE_TAG_2 should appear twice, since it is being used to test
            // that multiple tagged components with the same ID can co-exist in
            // the same profile.
            Iterator fake1 = template.iteratorById( 
                SampleIORInterceptor.FAKE_TAG_1 );
            verifyComponent( fake1, SampleIORInterceptor.FAKE_TAG_1, 
                             SampleIORInterceptor.FAKE_DATA_1, 1 );
            
            Iterator fake2 = template.iteratorById( 
                SampleIORInterceptor.FAKE_TAG_2 );
            verifyComponent( fake2, SampleIORInterceptor.FAKE_TAG_2, 
                             SampleIORInterceptor.FAKE_DATA_2, 2 );
            helper.pass() ;
        } catch (Exception exc) {
            helper.fail( exc ) ;
            throw exc ;
        }
    }

    /**
     * Ensures that the given tagged component is valid.  Throws a 
     * RuntimeException if not.
     *
     * @param components An iterator, where each element is a 
     *     GenericIdentifiable representing a tagged component with a
     *     known id.
     * @param expectedId The expected ID of the component (used primarily
     *     for debug output)
     * @param expectedData The data to verify against to make sure the
     *     tagged component was inserted properly.
     * @param numOccurrences The number of times this tagged component is
     *     expected to appear in the profile.
     */
    private void verifyComponent( Iterator components, int expectedId, 
                                  byte[] expectedData, int numOccurrences ) 
    {
        ArrayList componentList = new ArrayList();
        
        while( components.hasNext() ) {
            componentList.add( components.next() );
        }
        
        // Check to make sure numOccurrences objects found:
        if( componentList.size() != numOccurrences ) {
            String failReason = "Component ID " + expectedId + 
                         ": Incorrect number of occurrences found.  " +
                         "Expected: " + numOccurrences + 
                         ".  Found: " + componentList.size() + ".  FAIL.";
            out.println( "      - " + failReason );
            throw new RuntimeException( failReason );
            // *** FAIL ***
        }
        
        // Check each found occurrence to make sure it contains the correct
        // data.
        for( int i = 0; i < componentList.size(); i++ ) {
            GenericIdentifiable encaps = null;
            try {
                encaps = (GenericIdentifiable)componentList.get( i );
            }
            catch( ClassCastException e ) {
                String failReason = "Component ID " + expectedId +
                    ": One or more occurrences is not a " +
                    "GenericIdentifiable.  FAIL.";
                out.println( "      - " + failReason );
                throw new RuntimeException( failReason );
                // *** FAIL ***
            }
            
            byte[] data = encaps.getData();
            // Compare actual data to inserted data:
            if( Arrays.equals( data, expectedData ) ) {
                out.println( "      - Component ID " + expectedId + 
                    ": PRESENT AND VALID" );
                // *** PASS ***
            }
            else {
                String failReason = "Component ID " + expectedId + 
                    ": Present correct number of times, but one or more " +
                    "instances contain invalid data.  FAIL.";
                out.println( "      - " + failReason );
                throw new RuntimeException( failReason );
                // *** FAIL ***
            }
        }
    }

    // Creates an instance of the simple interface and binds it.
    private org.omg.CORBA.Object createSampleObject() 
        throws Exception 
    {
        // Create from the child POA, not the root POA.  The IORInterceptor
        // does not get activated for the root POA.
        
        // Set up hello object:
        return createAndBind( policyPOA, "Simple1" );
    }
    
    /**
     * Implementation borrowed from corba.socket.HelloServer test
     */
    public org.omg.CORBA.Object createAndBind (POA poa, String name)
        throws Exception
    {
        org.omg.CORBA.Object result;
        // create servant and register it with the ORB
        SimpleServant simpleRef = new SimpleServant();
      
        byte[] id = poa.activate_object(simpleRef);
        result = poa.id_to_reference(id);
      
        // get the root naming context
        org.omg.CORBA.Object objRef = 
            orb.resolve_initial_references("NameService");
        NamingContext ncRef = NamingContextHelper.narrow(objRef);
      
        // bind the Object Reference in Naming
        NameComponent nc = new NameComponent(name, "");
        NameComponent path[] = {nc};
            
        ncRef.rebind( path, result );
        
        return result;
    }
    
}
