/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package pi.iorinterceptor;

import java.io.*;

import org.omg.CORBA.*;
import org.omg.PortableServer.*;

import org.omg.PortableInterceptor.*;
import org.omg.IOP.*;
import org.omg.IOP.CodecPackage.*;
import org.omg.IOP.CodecFactoryPackage.*;
import org.omg.IOP.TaggedComponent;
import org.omg.IOP.TAG_INTERNET_IOP;
import com.sun.corba.ee.spi.logging.OMGSystemException;

/**
 * Thoroughly tests IORInterceptor support.
 */
public class SampleIORInterceptor 
    extends LocalObject 
    implements IORInterceptor 
{
    private static final OMGSystemException wrapper =
        OMGSystemException.self ;

    // The name for this interceptor
    private String name;

    // Destination for all output.  This is set in the constructor, which
    // is called by ServerTestInitializer.
    private PrintStream out;

    // True if any instances of this IORInterceptor were registered, or
    // false if not;
    public static boolean registered = false;

    // True if establish_components was ever called on this interceptor,
    // or false if not
    public static boolean establishComponentsCalled = false;
    
    // True if establish_components completed successfully, or false if
    // not.
    public static boolean establishComponentsPassed = false;
    
    // Constant for fake tag values:
    public static final int FAKE_TAG_1 = 8954;
    public static final int FAKE_TAG_2 = 8955;
    
    // Constants for fake data to put in the tagged components:
    public static byte[] FAKE_DATA_1 = null;
    public static byte[] FAKE_DATA_2 = null;
    
    // constant for a profile ID that does not exist:
    public static final int INVALID_PROFILE_ID = 1234;

    public SampleIORInterceptor( String name, PrintStream out ) {
        this.name = name;
        this.out = out;
        out.println( "    - IORInterceptor " + name + " created." );
        registered = true;
    }

    public String name() {
        return name;
    }

    public void destroy() {
    }

    public void establish_components (IORInfo info) {
        out.println( "    - establish_components called." );
        establishComponentsCalled = true;
        establishComponentsPassed = true;

        try {
            out.println( "    - generating fake data using CodecFactory..." );
            // Get a Singleton ORB and create sample data to insert into
            // profiles:
            ORB initORB = ServerTestInitializer.orb;
            org.omg.CORBA.Object objRef = 
                initORB.resolve_initial_references( "CodecFactory" );
            CodecFactory codecFactory = CodecFactoryHelper.narrow( objRef );
            Codec codec = codecFactory.create_codec( new Encoding(
                (short)ENCODING_CDR_ENCAPS.value, (byte)1, (byte)2 ) );
            Any any = initORB.create_any();
            any.insert_float( (float)3.45 );

            // Create octet stream of CDR encapsulation of value.
            FAKE_DATA_1 = codec.encode_value( any );

            any.insert_string( "Hi there" );
            FAKE_DATA_2 = codec.encode_value( any );

            // Add a component to all profiles:
            out.println( "    - adding component to all profiles..." );
            TaggedComponent tcAllProfiles = new TaggedComponent( FAKE_TAG_1, 
                                                                 FAKE_DATA_1 );
            info.add_ior_component( tcAllProfiles );

            // Add a component to only the TAG_INTERNET_IOP profile:
            out.println( "    - adding component to specific profile..." );
            TaggedComponent tcSpecificProfile = 
                new TaggedComponent( FAKE_TAG_2, FAKE_DATA_2 );
            int profileId = TAG_INTERNET_IOP.value;
            info.add_ior_component_to_profile( tcSpecificProfile, profileId );

            // Add a component with the same component ID as an existing 
            // component:
            out.println( "    - adding duplicate components..." );
            info.add_ior_component_to_profile( tcSpecificProfile, profileId );

            // Add a component to a profile that does not exist:
            out.println( "    - adding component to non-existent profile..." );
            try {
                info.add_ior_component_to_profile( tcSpecificProfile, 
                                                   INVALID_PROFILE_ID );
                out.println( "      + No exception thrown" );
                establishComponentsPassed = false;
            }
            catch( BAD_PARAM e ) {
                out.println( "      + Correct exception thrown" );
                if( e.minor != wrapper.INVALID_PROFILE_ID ) {
                    out.println( "      + Incorrect minor code ( " + e.minor + 
                        ") detected." );
                    establishComponentsPassed = false;
                }
                else {
                    out.println( "      + Correct minor code." );
                }
            }
            catch( Exception e ) {
                out.println( "      + Incorrect exception thrown." );
                establishComponentsPassed = false;
            }

            // Test get_effective_policy
            out.println( "    - testing get_effective_policy..." );
            Policy policy;

            try {
                out.print( "      + ID Uniqueness policy: " );
                policy = info.get_effective_policy(
                    ID_UNIQUENESS_POLICY_ID.value );
                if( policy == null ) {
                    out.println( "policy was null!" );
                    establishComponentsPassed = false;
                }
                else if( !(policy instanceof IdUniquenessPolicy ) ) {
                    out.println( "not an id uniqueness policy!" );
                    establishComponentsPassed = false;
                }
                else {
                    IdUniquenessPolicy idUniquenessPolicy = 
                        (IdUniquenessPolicy)policy;
                    if( idUniquenessPolicy.value().value() == 
                        IdUniquenessPolicyValue._MULTIPLE_ID ) 
                    {
                        out.println( "ok" ); 
                    }
                    else {
                        out.println( "wrong policy value!" );
                        establishComponentsPassed = false;
                    }
                }

                out.print( "      + Hundred policy: " );
                policy = info.get_effective_policy( 100 );
                if( policy == null ) {
                    out.println( "policy is null!" );
                    establishComponentsPassed = false;
                }
                else if( !(policy instanceof PolicyHundred ) ) {
                    out.println( "not a 'hundred' policy!" );
                    establishComponentsPassed = false;
                }
                else {
                    PolicyHundred hundredPolicy = (PolicyHundred)policy;
                    if( hundredPolicy.getValue() == 99 ) {
                        out.println( "ok" ); 
                    }
                    else {
                        out.println( "wrong policy value!" );
                        establishComponentsPassed = false;
                    }
                }
            }
            catch( INV_POLICY e ) {
                out.println( "      + INV_POLICY thrown." );
                establishComponentsPassed = false;
            }

            // Try invalid ID:
            try {
                out.print( "      + Invalid policy: " );
                policy = info.get_effective_policy( 101 );
                if( policy == null ) {
                    out.println( "policy was null (ok)" );
                    establishComponentsPassed = true;
                }
                else {
                    out.println( "policy was not null!" );
                    establishComponentsPassed = false;
                }
            }
            catch( INV_POLICY e ) {
                out.println( 
                    "INV_POLICY thrown. (error - should return null)" );
                establishComponentsPassed = false;
            }
        }
        catch( Exception f ) {
            // Something unexpected happened - treat it as a test failure.
            out.println( "    - Invalid exception " + f + " detected." );
            f.printStackTrace();
            establishComponentsPassed = false;
        }
    }

    public void components_established( IORInfo info )
    {
    }

    public void adapter_state_changed( ObjectReferenceTemplate[] templates, 
        short state )
    {
    }

    public void adapter_manager_state_changed( int managedId, short state )
    {
    }
}


