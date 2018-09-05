/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package pi.codec;

import CodecTest.TestStruct;
import CodecTest.TestStructHelper;
import CodecTest.TestUnion;
import CodecTest.TestUnionHelper;
import corba.framework.InternalProcess;
import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Properties;
import org.glassfish.pfl.test.JUnitReportHelper;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CORBA.TypeCode;
import org.omg.IOP.Codec;
import org.omg.IOP.CodecFactory;
import org.omg.IOP.CodecFactoryHelper;
import org.omg.IOP.CodecFactoryPackage.UnknownEncoding;
import org.omg.IOP.CodecPackage.FormatMismatch;
import org.omg.IOP.ENCODING_CDR_ENCAPS;
import org.omg.IOP.Encoding;

public class Client 
    implements InternalProcess 
{
    private JUnitReportHelper helper = new JUnitReportHelper( Client.class.getName() ) ;

    private ORB orb;
    private PrintStream out;
    private PrintStream err;
    private CodecFactory codecFactory;

    public static void main(String args[]) {
        try {
            (new Client()).run( System.getProperties(),
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
        // create and initialize the ORB
        Properties props = new Properties() ;
        props.put( "org.omg.CORBA.ORBClass", 
                   System.getProperty("org.omg.CORBA.ORBClass"));
        ORB orb = ORB.init(args, props);

        this.out = out;
        this.err = err;
        this.orb = orb;

        try {
            // Test the codec factory:
            testCodecFactory();

            // Test the codec:
            testCodec();
        } finally {
            helper.done() ;
        }
    }

    private void testCodecFactory() throws Exception 
    {
        out.println( "Testing CodecFactory" );
        out.println( "====================" );

        // Test that the CodecFactory is obtained through a call to
        // ORB::resolve_initial_references( "CodecFactory" )
        testReference();

        // Test that a codec is obtained from the Codec Factory, and
        // that ENCODING_CDR_ENCAPS 1.0, 1.1, 1.2 are supported.
        testValidCodecs();

        // Test that UnknownEncoding is raised if this factory cannot
        // create a Codec of the given encoding.
        testInvalidCodecs();
    }

    private void testReference() throws Exception 
    {
        helper.start( "testReference" ) ;

        try {
            out.println( "resolve_initial_references( \"CodecFactory\" ): " );

            org.omg.CORBA.Object objRef = 
                orb.resolve_initial_references("CodecFactory");

            if( objRef != null ) {
                out.println( "  - Retrieved reference." );
            } else {
                throw new RuntimeException( "null Reference from ORB." );
            }

            codecFactory = CodecFactoryHelper.narrow(objRef);

            if( objRef != null ) {
                out.println( "  - Narrowed reference." );
            } else {
                throw new RuntimeException( 
                    "CodecFactory Reference narrowed to null" );
            }

            helper.pass() ;
        } catch (Exception exc) {
            helper.fail( exc ) ;
        }
    }

    private void testValidCodecs() throws Exception 
    {
        for( int minor = 0; minor <=2; minor++ ) {
            helper.start( "testValidCodecs_" + minor ) ;

            try { 
                try {
                    out.print( "create_codec( ENCODING_CDR_ENCAPS, 1, " + 
                        minor + " ):" );
                    Encoding encoding = new Encoding( 
                        (short)ENCODING_CDR_ENCAPS.value, (byte)1, (byte)minor );
                    Codec codec = codecFactory.create_codec( encoding );

                    if( codec != null ) {
                        out.println( "valid (ok)" );
                    } else {
                        out.println( "null (error)" );
                        throw new RuntimeException( "Null Codec returned for 1." + 
                                                    minor );
                    }
                } catch( UnknownEncoding e ) {
                    out.println( "unknown (error)" );
                    throw new RuntimeException( 
                        "Could not get Codec for 1." + minor );
                }

                helper.pass() ;
            } catch (Exception exc) {
                helper.fail( exc ) ;
                throw exc ;
            }
        }
    }

    private void testInvalidCodecs() throws Exception 
    {
        for( int format = ENCODING_CDR_ENCAPS.value; 
             format <= ENCODING_CDR_ENCAPS.value + 1; format ++ ) {
            // (try 0 and 1)
            for( int major = 2; major <= 3; major+=1 ) {
                for( int minor = 3 - (major-2) * 2; 
                    minor <= 5 - (major-2) * 2; minor+=1 ) {

                    helper.start( "testInvalidCodecs_" + format + "_" + major + "_" + minor ) ;

                    // (try 2.3, 2.4, 2.5, 3.1, 3.2, 3.3)
                    try {
                        out.print( "create_codec( " + format + ", " + 
                            major + ", " + minor + " ):" );
                        Encoding encoding = new Encoding( 
                            (short)format, (byte)major, (byte)minor );
                        Codec codec = codecFactory.create_codec( encoding );

                        String msg = (codec == null) ?
                            "null (error)" :
                            "valid (error)" ;
                        out.println( msg );
                        helper.fail( msg ) ;

                        throw new RuntimeException( 
                            "No exception thrown for " + major + "." + minor );
                    } catch( UnknownEncoding e ) {
                        // We expect these versions to fail.
                        helper.pass() ;
                        out.println( "unknown (ok)" );
                    }
                } // minor loop
            } // major loop
        } // format loop

    }

    private void testCodec() 
        throws Exception 
    {
        out.println( "" );
        out.println( "Testing Codec" );
        out.println( "=============" );

        // Test that encode and decode work
        testEncodeDecode( false );

        // Test that encode_value and decode_value work
        testEncodeDecode( true );

        // Test that FormatMismatch is raised if decode or decode_value
        // is called when data in the provided octet sequence cannot be
        // decoded into an any.
        testFormatMismatch();
    }

    private void testEncodeDecode( boolean valueOnly ) throws Exception {
        if( valueOnly ) {
            out.println( "testing encode_value and decode_value:" );
        }
        else {
            out.println( "testing encode and decode:" );
        }

        // Get a codec to test with
        Codec codec = codecFactory.create_codec( 
            new Encoding( (short)ENCODING_CDR_ENCAPS.value, (byte)1, 
                          (byte)2 ) );
        
        // Try to encode a representative set from the available types in 
        // CORBA IDL.  Then, decode them to ensure they are  equal to 
        // their original values.  Note that this is not an all-inclusive
        // set.

        Any sourceAny;  // Holds the source any

        // null
        sourceAny = orb.create_any() ;
        check( codec, valueOnly, "null", sourceAny, 8, 1 );


        // short
        sourceAny =  orb.create_any() ;
        sourceAny.insert_short( (short)345 );
        check( codec, valueOnly, "short", sourceAny, 10, 4 );

        // float
        sourceAny =  orb.create_any() ;
        sourceAny.insert_float( (float)3.45 );
        check( codec, valueOnly, "float", sourceAny, 12, 8 );

        // boolean
        sourceAny =  orb.create_any() ;
        sourceAny.insert_boolean( true );
        check( codec, valueOnly, "boolean", sourceAny, 9, 2 );

        // char
        sourceAny =  orb.create_any() ;
        sourceAny.insert_char( 'w' );
        check( codec, valueOnly, "char", sourceAny, 9, 2 );

        // octet
        sourceAny =  orb.create_any() ;
        sourceAny.insert_octet( (byte)127 );
        check( codec, valueOnly, "octet", sourceAny, 9, 2 );

        // string
        sourceAny =  orb.create_any() ;
        sourceAny.insert_string( "hello, world" );
        check( codec, valueOnly, "string", sourceAny, 29, 21 );

        // struct
        TestStruct testStruct = new TestStruct( (short)10, (short)20 );
        sourceAny =  orb.create_any() ;
        TestStructHelper.insert( sourceAny, testStruct );
        check( codec, valueOnly, "struct", sourceAny, 108, 6 );

        // union
        TestUnion testUnion = new TestUnion();
        testUnion.f( (float)3.45 );
        sourceAny =  orb.create_any() ;
        TestUnionHelper.insert( sourceAny, testUnion );
        check( codec, valueOnly, "union", sourceAny, 128, 8 );

        // longlong
        sourceAny =  orb.create_any() ;
        sourceAny.insert_longlong( 1234567L );
        check( codec, valueOnly, "longlong", sourceAny, 16, 16 );

        // wstring
        sourceAny =  orb.create_any() ;
        sourceAny.insert_wstring( "hello, world" );
        check( codec, valueOnly, "wstring", sourceAny, 40, 32 );

        // fixed
        sourceAny =  orb.create_any() ;
        sourceAny.insert_fixed( new java.math.BigDecimal( "1234.5678" ) );
        check( codec, valueOnly, "fixed", sourceAny, 17, 6 );
    }

    // Does a loopback test on the given source Any by encoding with the
    // given codec, and then decoding it.  Throws a RuntimeException if
    // the decoded value does not match the encoded one or if the
    // actual length does not match the expected length.  Does not
    // trap any exceptions - just passes them through.
    private void check( Codec codec, boolean valueOnly, String type, 
                        Any source, int lenWithType, int lenWOType ) 
        throws Exception
    {
        helper.start( "encodeDecode_" + type + "_valueOnly_" + valueOnly ) ;

        try {
            byte[] octets;              // Holds the intermediate octet stream
            Any destAny;                // Holds the target any
            TypeCode tc = source.type();
            int expectedLength;     // Expected length of octet stream

            out.print( "  - " + type + ": " );
            if (valueOnly) {
                octets = codec.encode_value( source );
                expectedLength = lenWOType;
            } else {
                octets = codec.encode( source );
                expectedLength = lenWithType;
            }
            out.print( "size: " + octets.length );
            if (valueOnly) {
                destAny = codec.decode_value( octets, tc );
            } else {
                destAny = codec.decode( octets );
            }
            boolean compare = source.equal( destAny );
            out.println( " compare: " + compare );
            out.println( "    " + dumpHex( octets ) );

            if (!compare) {
                throw new RuntimeException( 
                    "" + type + " fails loopback test content comparison" );
            }

            if (octets.length != expectedLength) {
                throw new RuntimeException( 
                    "" + type + " fails loopback test length comparison: expected " +
                    expectedLength + " got " + octets.length );
            }

            helper.pass() ;
        } catch (Exception exc) {
            helper.fail( exc ) ;
            throw exc ;
        }
    }

    // Dumps the hex values in the given byte array
    private String dumpHex( byte[] octets ) {
        StringBuffer result = new StringBuffer( "" );
        for( int i = 0; i < octets.length; i++ ) {
            if( (i != 0) && ((i % 16) == 0) ) result.append( "\n    " );
            int b = octets[i];
            if( b < 0 ) b = 256 + b;
            String hex = Integer.toHexString( b );
            if( hex.length() == 1 ) {
                hex = "0" + hex;
            }
            result.append( hex + " " );
        }

        return result.toString();
    }

    private void testFormatMismatch() 
        throws Exception 
    {
        // Some invalid sequences.  Each of these sequences is invalid for
        // a different reason.  On an attempt to call decode or decode_value,
        // a FormatMismatch exception is expected.  If it is not received,
        // the test fails.

        // These are invalid for decode()
        byte[][] invalidForDecode = {
            // This is invalid because it specifies an invalid type code.
            { 0x00, 0x00, 0x00, 0x00,           // big-endian
              0x00, 0x00, 0x00, 0x7f,           // invalid type
              0x00, 0x00, 0x00, 0x00,           // extra data
              0x00, 0x00, 0x00, 0x00    
            },

            // This is invalid because it has too few octets.
            { 0x00, 0x00, 0x00, 0x00,           // big-endian
              0x00, 0x00, 0x00, 0x17,           // long long
              0x12, 0x34                        // not enough data
            }
        };

        // These are invalid for decode_value()
        byte[][] invalidForDecodeValue = {
            // This is an invalid null because it has no endian type.
            {},                                 // missing endian

            // This is an invalid long long because it has too few octets.
            { 0x00, 0x00, 0x00, 0x00,           // big-endian
              0x00, 0x00, 0x00, 0x00,           // padding
              0x12, 0x34 }                      // not enough data
        };

        // Type codes for the decode_value cases
        Any longAny =  orb.create_any() ;
        longAny.insert_longlong( 1L );
        TypeCode[] typeCodes = { 
            orb.create_any().type(),            // null TypeCode
            longAny.type()                      // long TypeCode
        };

        // Get a codec to use for these tests:
        Codec codec = codecFactory.create_codec( 
            new Encoding( (short)ENCODING_CDR_ENCAPS.value, (byte)1, 
                          (byte)2 ) );

        // Test decode()
        checkInvalid( codec, false, "decode", invalidForDecode, typeCodes );
        checkInvalid( codec, true, "decode_value", invalidForDecodeValue,
                      typeCodes );

    }

    private void checkInvalid( Codec codec, boolean valueOnly, String type,
                               byte[][] cases, TypeCode[] typeCodes ) 
        throws Exception
    {
        out.println( "Testing FormatMismatch for " + type );

        for( int i = 0; i < cases.length; i++ ) {
            helper.start( "formatMismatch_" + type + "_" + i ) ;

            out.print( "  - Case #" + i + ": " );
            byte[] data = cases[i];
            boolean pass = false;

            try {
                if (valueOnly) {
                    codec.decode_value( data, typeCodes[i] );
                } else {
                    codec.decode( data );
                }
            }
            catch( FormatMismatch e ) {
                // We got the expected exception:
                pass = true;
            }
            
            if( pass ) {
                out.println( "FormatMismatch thrown (ok)" );
                helper.pass() ;
            } else {
                out.println( "FormatMismatch not thrown (error)" );
                helper.fail( "FormatMismatch not thrown" ) ;
            }

            // Let us see what the data was:
            out.println( "    " + dumpHex( data ) );

            if( !pass ) {
                throw new RuntimeException( "FormatMismatch not thrown" );
            }
        }
    }
}
