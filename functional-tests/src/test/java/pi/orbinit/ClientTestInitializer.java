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

package pi.orbinit;

import org.omg.PortableInterceptor.*;
import org.omg.IOP.*;
import org.omg.PortableInterceptor.ORBInitInfoPackage.*;

import java.util.*;
import java.io.*;
import org.omg.CORBA.*;

import ORBInitTest.*;

/**
 * Test initializer on the client side.  Most of the testing is done in this
 * class, and the results are stored in static publicly accessible variables 
 * that are analyzed in Client.java.
 */
public class ClientTestInitializer 
    extends LocalObject 
    implements ORBInitializer
{
    // Output stream to send information to
    public static PrintStream out = null;
    public static ORB orb = null;

    // Were we initialized the right way?
    private static boolean initializedPre = false;
    private static boolean initializedPost = false;
    private static boolean globalValid = false;
    private static boolean firstInit = true;

    // Recorded Results from the resolve_initial_references test
    public static boolean passResolveInitialReferences = false;
    public static boolean passResolveInitialReferencesInvalid = false;
    public static String resolveInitialReferencesResults = "";

    // Recorded results from the add_*_interceptor test
    public static String preAddInterceptorResult = "";
    public static boolean preAddInterceptorPass = false;
    public static String postAddInterceptorResult = "";
    public static boolean postAddInterceptorPass = false;

    // Recorded results from the get/set_slot test
    public static String getSetSlotResult = "";
    public static boolean getSetSlotPass = false;

    // "Illegal" private copy of ORBInitInfo - should not be able to access
    // after postInit, but we'll try anyway.
    private static ORBInitInfo cachedInfo = null;

    // Test reference name to use for initial references
    private static String REFERENCE_NAME = "ClientTestInitializerReference";

    /**
     * Creates a ClientTestInitializer
     */
    public ClientTestInitializer() {
        out.println( "  - ClientTestInitializer constructed." );
    }

    /**
     * Called before all references are registered
     */
    public void pre_init (org.omg.PortableInterceptor.ORBInitInfo info) {
        if( firstInit ) {
            firstInit = false;
            globalValid = true;
        }

        out.println( "  - ClientTestInitializer.pre_init called" );

        if( initializedPre ) {
            // pre was already called!
            out.println( "ERROR: pre_init() was already called!" );
            globalValid = false;
        }

        if( initializedPost ) {
            // post was already called!
            out.println( "ERROR: post_init() was already called!" );
            globalValid = false;
        }

        // Check to make sure info object is valid
        if( info == null ) {
            out.println( 
                "ERROR: supplied ORBInitInfo object to pre_init() is null" );
            globalValid = false;
        }
        else {
            if( !testInfo( info, true ) ) {
                out.println( 
                    "ERROR: Supplied ORBInitInfo object to " + 
                    "pre_init() is invalid" );
                globalValid = false;
            }
        }

        // Test interceptor registration
        testInterceptorRegistration( info, "preinit" );

        initializedPre = true;
    }

    /**
     * Called after all references are registered
     */
    public void post_init (org.omg.PortableInterceptor.ORBInitInfo info) {
        out.println( "  - ClientTestInitializer.post_init called" );

        // Hold on to ORBInitInfo for a later stage of the test.
        cachedInfo = info;

        if( !initializedPre ) {
            // pre was never called!
            out.println( "ERROR: pre_init() was never called!" );
            globalValid = false;
        }

        if( initializedPost ) {
            // post was already called!
            out.println( "ERROR: post_init() was already called!" );
            globalValid = false;
        }

        // Check to make sure info object is valid
        if( info == null ) {
            out.println( 
                "ERROR: supplied ORBInitInfo object to post_init() is null" );
        }
        else {
            if( !testInfo( info, false ) ) {
                out.println( 
                    "ERROR: Supplied ORBInitInfo object to " + 
                    "pre_init() is invalid" );
                globalValid = false;
            }
        }

        // We can do this only in post_init()
        testResolveInitialReferences( info );

        // Test interceptor registration
        testInterceptorRegistration( info, "postinit" );

        // Test that get_slot and set_slot cannot be called in PICurrent.
        testGetSetSlot( info );

        initializedPost = true;
    }

    /**
     * Returns true to the Client.java test if we were initialized 
     * appropriately.
     */
    public static boolean initializedAppropriately() {
        return globalValid;
    }

    /**
     * Run some sanity checks on the given ORBInitInfo object
     *
     * @param preInit true if this was a preInit call, false if post.
     */
    private boolean testInfo( ORBInitInfo info, boolean preInit ) {
        // Innocent until proven guilty:
        boolean infoValid = true;

        out.println( "  - Testing provided initInfo..." );

        // Check method validity at different stages of init
        // (an x indicates valid to call, a dash indicates invalid):
        // 
        // ORBInitInfo Method              pre_init        post_init
        // ------------------              --------        ---------
        // arguments                       x               x
        // orb_id                          x               x
        // codec_factory                   x               x
        //

        // Check that all attributes return valid values.
        try {
            String[] args = info.arguments();
            String id = info.orb_id();
            CodecFactory codecFactory = info.codec_factory();

            if( args == null ) {
                out.println(
                    "    - arguments is null (error)" );
                infoValid = false;
            }
            else {
                // We know the arguments "abcd" and "efgh" must be present.
                boolean abcdPresent = false;
                boolean efghPresent = false;
                for( int i = 0; i < args.length; i++ ) {
                    if( args[i].equals( "abcd" ) ) {
                        abcdPresent = true;
                    }
                    if( args[i].equals( "efgh" ) ) {
                        efghPresent = true;
                    }
                }
                if( abcdPresent && efghPresent ) {
                    out.println( 
                        "    - arguments contains 'abcd' and 'efgh'(ok)");
                }
                else {
                    out.println( 
                        "    - arguments 'abcd' and 'efgh' not found (error)");
                    infoValid = false;
                }
            }

            // We do not care if orb_id is null - just that we can call it

            // CodecFactory must be valid:
            if( id == null ) {
                out.println( "    - orb id is null (error)" );
                infoValid = false;
            }
            else {
                out.println( "    - orb id: " + id + " [non-null] (ok)" );
            }
            if( codecFactory == null ) {
                out.println( "    - codecFactory is null (error)" );
                infoValid = false;
            }
            else {
                out.println( "    - codecFactory: valid" );
            }
        }
        catch( Exception e ) {
            infoValid = false;
            out.println( 
                "    - Exception accessing attributes (error)" );
        }


        // ORBInitInfo Method              pre_init        post_init
        // ------------------              --------        ---------
        // register_initial_reference      x               -
        // resolve_initial_reference       -               x

        if( preInit ) {
            out.print( "    - Checking preInit validity: " );
            boolean preInitFailed = false;

            // Ensure register_initial_reference can be called
            try {
                info.register_initial_reference( 
                    REFERENCE_NAME, this );
            }
            catch( Exception e ) {
                infoValid = false;
                e.printStackTrace();
                out.println( 
                    "Could not call register_initial_references (error)" +
                    " Reason: " + e );
                preInitFailed = true;
            }

            // Ensure resolve_initial_references cannot be called
            try {
                info.resolve_initial_references( REFERENCE_NAME );
                infoValid = false;
                out.println( 
                    "Able to call resolve_initial_references (error)" );
                preInitFailed = true;
            }
            catch( Exception e ) {
                // expected (ok).
            }

            if( !preInitFailed ) out.println( "ok" );
        }
        else {
            out.print( "    - Checking postInit validity: " );
            boolean postInitFailed = false;

            // Ensure register_initial_reference can be called
            try {
                info.register_initial_reference( 
                    REFERENCE_NAME + "2", this );
            }
            catch( Exception e ) {
                infoValid = false;
                out.println( 
                    "Could not call register_initial_references (error)" +
                    " Reason: " + e );
                postInitFailed = true;
            }

            // Ensure resolve_initial_references can be called
            try {
                info.resolve_initial_references( "CodecFactory" );
            }
            catch( Exception e ) {
                infoValid = false;
                out.println( 
                    "Unable to call resolve_initial_references (error)" );
                postInitFailed = true;
            }

            if( !postInitFailed ) out.println( "ok" );
        }

        // These are trickier to test and require actually doing something
        // useful.  They will be hit later in the test.
        //
        // ORBInitInfo Method              pre_init        post_init
        // ------------------              --------        ---------
        // add_client_request_interceptor  x               x
        // add_server_request_interceptor  x               x
        // add_ior_interceptor             x               x
        // allocate_slot_id                x               x
        // register_policy_factory         x               x


        return infoValid;
    }

    /**
     * Called after post_init to make sure we cannot still access
     * ORBInitInfo.  Returns false if we were able to access the
     * ORBInitInfo object here, or true if not or if we did not get
     * an OBJECT_NOT_EXIST exception.  (true is pass, false is fail)
     */
    public static boolean post_post_init () {
        boolean result = true;

        out.println( "  - Testing post_post_init access to initInfo..." );

        try {
            CodecFactory codecFactory = cachedInfo.codec_factory();

            // We should not get to this point.
            result = false;
            out.println( "    - able to access (error)" );
        }
        catch( OBJECT_NOT_EXIST e ) {
            // Correct exception was thrown
            result = true;
            out.println( "    - OBJECT_NOT_EXIST thrown (ok)" );
        }
        catch( Exception e ) {
            // We should not get to this point.
            result = false;
            out.println( "    - " + e + " thrown (error)" );
        }

        return result;
    }

    /**
     * Test the resolve_initial_references call, and record results for
     * future retrieval from the test harness
     */
    private void testResolveInitialReferences( ORBInitInfo info ) {
        resolveInitialReferencesResults = 
            "Testing resolve_initial_references:\n";

        try {
            resolveInitialReferencesResults += 
                "    - Testing info.resolve_initial_references( " + 
                "\"CodecFactory\" )... ";
            org.omg.CORBA.Object objRef = 
                info.resolve_initial_references( "CodecFactory" );
            CodecFactory codecFactory = CodecFactoryHelper.narrow( objRef );
            if( objRef != null ) {
                // If we go this far, we know we can look up existing 
                // references.
                passResolveInitialReferences = true;
                resolveInitialReferencesResults +=  "passed.\n";
            }
            else {
                passResolveInitialReferences = false;
                resolveInitialReferencesResults +=  "failed. null received.\n";
            }
        }
        catch( Exception e ) {
            passResolveInitialReferences = false;
            resolveInitialReferencesResults +=  "failed: " + e + "\n";
        }

        // Ensure resolve_initial_references throws InvalidName at the
        // appropiate times (the name to be resolved is invalid).
        try {
            resolveInitialReferencesResults += 
                "    - Testing info.resolve_initial_references( " + 
                "\"CodecFactory2\" )... ";
            org.omg.CORBA.Object objRef = 
                info.resolve_initial_references( "CodecFactory2" );
            // If we got this far, the resolve did not throw an InvalidName
            // Exception as expected.
            passResolveInitialReferencesInvalid = false;
            resolveInitialReferencesResults += 
                "failed.  InvalidName not raised.\n";
        }
        catch( 
            org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName e ) 
        {
            // This is the correct IDL version of InvalidName.
            passResolveInitialReferencesInvalid = true;
            resolveInitialReferencesResults += 
                "InvalidName raised.  (passed)\n";
        }
        catch( Exception e ) {
            // This is the incorrect PIDL version of InvalidName.
            passResolveInitialReferencesInvalid = false;
            resolveInitialReferencesResults += 
                "Incorrect Exception raised: " + e + " (failed).\n";
        }

    }
    
    private void testInterceptorRegistration( ORBInitInfo info, String name ) {
        SampleClientRequestInterceptor cReqInt = 
            new SampleClientRequestInterceptor( name );
        SampleServerRequestInterceptor sReqInt = 
            new SampleServerRequestInterceptor( name );
        SampleIORInterceptor iInt = 
            new SampleIORInterceptor( name );
        String results = "";
        boolean resultOk = true;

        // Try adding a client request interceptor
        results += "    - Adding Client Request Interceptor... ";
        try {
            info.add_client_request_interceptor( cReqInt );
            results += "(ok)\n";
        }
        catch( Exception e ) {
            results += e + " (error)\n";
            resultOk = false;
        }

        // Try adding a server request interceptor
        results += "    - Adding Server Request Interceptor... ";
        try {
            info.add_server_request_interceptor( sReqInt );
            results += "(ok)\n";
        }
        catch( Exception e ) {
            results += e + " (error)\n";
            resultOk = false;
        }

        // Try adding an IOR interceptor
        results += "    - Adding IOR Interceptor... ";
        try {
            info.add_ior_interceptor( iInt );
            results += "(ok)\n";
        }
        catch( Exception e ) {
            results += e + " (error)\n";
            resultOk = false;
        }

        // Try adding a client request interceptor with a duplicate name:
        results += "    - Testing Duplicate Interceptor... ";
        try {
            info.add_client_request_interceptor( cReqInt );

            // If we get to here, it allowed us to add a duplicate name.
            resultOk = false;
            results += "No DuplicateName thrown (error)\n";
        }
        catch( DuplicateName e ) {
            // Expected.  
            results += "DuplicateName raised. (ok)\n";

        }
        catch( Exception e ) {
            // Some other exception.  Not expected.
            results += e + " (error)\n";
            resultOk = false;
        }

        // Try adding two anonymous interceptors in a row.
        SampleClientRequestInterceptor cAnonymous1 = 
            new SampleClientRequestInterceptor( "" );
        SampleClientRequestInterceptor cAnonymous2 = 
            new SampleClientRequestInterceptor( "" );

        // Try adding first anonymous interceptor
        results += "    - Testing First Anonymous Interceptor... ";
        try {
            info.add_client_request_interceptor( cAnonymous1 );

            results += "allowed (ok)\n";
        }
        catch( Exception e ) {
            // Not expected.
            results += e + " (error)\n";
            resultOk = false;
        }

        // Try adding second anonymous interceptor
        results += "    - Testing Second Anonymous Interceptor... ";
        try {
            info.add_client_request_interceptor( cAnonymous2 );

            results += "allowed (ok)\n";
        }
        catch( Exception e ) {
            // Not expected.
            results += e + " (error)\n";
            resultOk = false;
        }

        // This should be the end of this test.  If we get to this point,
        // we know all other calls were successful.
        if( name.equals( "preinit" ) ) {
            preAddInterceptorPass = resultOk;
            preAddInterceptorResult = results;
        }
        else if( name.equals( "postinit" ) ) {
            postAddInterceptorPass = resultOk;
            postAddInterceptorResult = results;
        }

    }

    private void testGetSetSlot(org.omg.PortableInterceptor.ORBInitInfo info) {
        String results = "";
        boolean resultOk = true;

        results += "    - Testing PICurrent::get_slot access... ";
        try {
            org.omg.PortableInterceptor.Current pic = 
                (org.omg.PortableInterceptor.Current)
                info.resolve_initial_references( "PICurrent" );
            pic.get_slot( 0 );

            results += "accessible.  (error)\n";
            resultOk = false;
        }
        catch( BAD_INV_ORDER e ) {
            results += "inaccessible.  (ok)\n";
        }
        catch( Exception e ) {
            results += "inaccessible, but wrong Exception (" + e + 
                " instead of BAD_INV_ORDER).  (error)\n";
            resultOk = false;
        }

        results += "    - Testing PICurrent::set_slot access... ";
        try {
            org.omg.PortableInterceptor.Current pic = 
                (org.omg.PortableInterceptor.Current)
                info.resolve_initial_references( "PICurrent" );
            pic.set_slot( 0, null );

            results += "accessible.  (error)\n";
            resultOk = false;
        }
        catch( BAD_INV_ORDER e ) {
            results += "inaccessible.  (ok)\n";
        }
        catch( Exception e ) {
            results += "inaccessible, but wrong Exception (" + e + 
                " instead of BAD_INV_ORDER).  (error)\n";
            resultOk = false;
        }

        getSetSlotPass = resultOk;
        getSetSlotResult = results;
    }
}
