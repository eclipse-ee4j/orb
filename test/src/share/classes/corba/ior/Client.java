/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.ior;

import java.io.PrintStream ;

import java.util.Properties ;
import java.util.NoSuchElementException ;
import java.util.List ;
import java.util.LinkedList ;
import java.util.Iterator ;
import java.util.StringTokenizer ;
import java.util.ArrayList ;

import org.omg.PortableServer.POA ;
import org.omg.PortableServer.IdAssignmentPolicyValue ;

import org.omg.CORBA.Policy ;
import org.omg.CORBA.BAD_PARAM ;
import org.omg.CORBA.INTERNAL ;
import org.omg.CORBA.OctetSeqHolder ;

import org.omg.CORBA_2_3.portable.OutputStream ;
import org.omg.CORBA_2_3.portable.InputStream ;

import org.omg.IOP.TAG_ALTERNATE_IIOP_ADDRESS ;
import org.omg.IOP.TAG_CODE_SETS ;
import org.omg.IOP.TAG_JAVA_CODEBASE ;
import org.omg.IOP.TAG_ORB_TYPE ;
import org.omg.IOP.TAG_INTERNET_IOP ;


import com.sun.corba.ee.spi.ior.Identifiable ;
import com.sun.corba.ee.spi.ior.IdentifiableFactory ;
import com.sun.corba.ee.spi.ior.IdentifiableFactoryFinder ;
import com.sun.corba.ee.spi.ior.ObjectKeyTemplate ;
import com.sun.corba.ee.spi.ior.TaggedComponent ;
import com.sun.corba.ee.spi.ior.TaggedComponentFactoryFinder ;
import com.sun.corba.ee.spi.ior.ObjectId ;
import com.sun.corba.ee.spi.ior.ObjectKey ;
import com.sun.corba.ee.spi.ior.IOR ;
import com.sun.corba.ee.spi.ior.IORFactory ;
import com.sun.corba.ee.spi.ior.ObjectAdapterId ;
import com.sun.corba.ee.spi.ior.IdentifiableContainerBase ;
import com.sun.corba.ee.spi.ior.IORTemplate ;
import com.sun.corba.ee.spi.ior.IORTemplateList ;
import com.sun.corba.ee.spi.ior.IORFactories ;
import com.sun.corba.ee.spi.ior.TaggedProfileTemplate ;

import com.sun.corba.ee.spi.ior.iiop.IIOPFactories ;
import com.sun.corba.ee.spi.ior.iiop.IIOPAddress ;
import com.sun.corba.ee.spi.ior.iiop.IIOPProfileTemplate ;
import com.sun.corba.ee.spi.ior.iiop.IIOPProfile ;
import com.sun.corba.ee.spi.ior.iiop.GIOPVersion ;

import com.sun.corba.ee.spi.activation.POANameHelper ;

import com.sun.corba.ee.spi.extension.ZeroPortPolicy ;

import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.orb.ORBVersion ;
import com.sun.corba.ee.spi.orb.ORBVersionFactory ;

import com.sun.corba.ee.spi.ior.iiop.AlternateIIOPAddressComponent ;
import com.sun.corba.ee.spi.ior.iiop.CodeSetsComponent ;
import com.sun.corba.ee.spi.ior.iiop.ORBTypeComponent ;
import com.sun.corba.ee.spi.ior.iiop.JavaCodebaseComponent ;

import com.sun.corba.ee.impl.ior.GenericTaggedProfile ;
import com.sun.corba.ee.impl.ior.GenericTaggedComponent ;
import com.sun.corba.ee.impl.ior.FreezableList ;
import com.sun.corba.ee.impl.ior.OldJIDLObjectKeyTemplate ;
import com.sun.corba.ee.impl.ior.OldPOAObjectKeyTemplate ;
import com.sun.corba.ee.impl.ior.JIDLObjectKeyTemplate ;
import com.sun.corba.ee.impl.ior.POAObjectKeyTemplate ;
import com.sun.corba.ee.impl.ior.WireObjectKeyTemplate ;
import com.sun.corba.ee.impl.ior.EncapsulationUtility ;
import com.sun.corba.ee.impl.ior.TaggedComponentFactoryFinderImpl ;
import com.sun.corba.ee.impl.ior.TaggedProfileFactoryFinderImpl ;
import com.sun.corba.ee.impl.ior.ObjectAdapterIdArray ;
import com.sun.corba.ee.impl.ior.ObjectAdapterIdNumber ;
import com.sun.corba.ee.impl.ior.ObjectReferenceTemplateImpl ;
import com.sun.corba.ee.impl.ior.ObjectKeyFactoryImpl ;

import com.sun.corba.ee.impl.encoding.CDRInputObject ;
import com.sun.corba.ee.impl.encoding.CDROutputObject;
import com.sun.corba.ee.impl.encoding.EncapsInputStream ;
import com.sun.corba.ee.impl.encoding.EncapsOutputStream ;

import com.sun.corba.ee.spi.misc.ORBConstants ;

import org.testng.annotations.Test ;
import org.testng.Assert ;

import corba.framework.TestngRunner ;

public class Client 
{
    private PrintStream out = System.out ;
    private PrintStream err = System.err ;
    private ORB orb ;

    public static void main(String args[])
    {
        System.out.println( "Starting IOR test" ) ;
        TestngRunner runner = new TestngRunner() ;
        runner.registerClass( Client.class ) ;
        runner.run() ;
        runner.systemExit() ;
    }

    public Client() {
        String[] args = null ;
        Properties props = new Properties( System.getProperties() ) ;
        props.put( "org.omg.CORBA.ORBClass", 
            "com.sun.corba.ee.impl.orb.ORBImpl" ) ;

        this.orb = (ORB)ORB.init( args, props ) ;
    }

// *************************************************
// ***************   Utilities   *******************
// *************************************************

    private void error( String msg )
    {
        Assert.fail( msg ) ;
    }
    
    private void info( String msg )
    {
        out.println( msg ) ;
    }

    private boolean equal( byte[] arr1, byte[] arr2 ) 
    {
        if ((arr1 == null) || (arr2 == null))   
            return arr1==arr2 ;
        
        int len = arr1.length ;
        if (len != arr2.length)
            return false ;

        for (int ctr = 0; ctr<len; ctr++ )
            if (arr1[ctr] != arr2[ctr])
                return false ;

        return true ;
    }
    
    private boolean equal( Object[] arr1, Object[] arr2 ) 
    {
        if ((arr1 == null) || (arr2 == null))   
            return arr1==arr2 ;
        
        int len = arr1.length ;
        if (len != arr2.length)
            return false ;

        for (int ctr = 0; ctr<len; ctr++ )
            if (!arr1[ctr].equals( arr2[ctr] ))
                return false ;

        return true ;
    }
    
    private OutputStream newOutputStream()
    {
        return new EncapsOutputStream( orb ) ;
    }

    private byte[] getBytes( OutputStream os ) 
    {
        CDROutputObject cos = (CDROutputObject)os ;
        byte[] bytes = cos.toByteArray() ;
        return bytes ;
    }

    private CDRInputObject makeInputStream( OutputStream os )
    {
        byte[] bytes = getBytes( os ) ;
        return makeInputStream( bytes ) ;
    }

    private CDRInputObject makeInputStream( byte[] data )
    {
        return new EncapsInputStream( orb, data, data.length ) ;
    }

// *************************************************
// ***************   TESTS   ***********************
// *************************************************

    class IdentifiableImpl implements Identifiable {
        private int value ;
        private int id ;

        IdentifiableImpl( int id, int value )
        { 
            this.id = id ;
            this.value = value ;
        }

        public int getId()
        {
            return id ;
        }

        public void write( OutputStream os )
        {
        }

        public int getValue()
        {
            return value ;
        }

        public boolean equals( Object obj )
        {
            if (!(obj instanceof IdentifiableImpl))
                return false ;

            IdentifiableImpl ii = (IdentifiableImpl)obj ;

            return (ii.getId() == getId()) && 
                (ii.getValue() == getValue()) ;
        }
    }

    public void checkIdentifiableIterator( Iterator iter, 
        IdentifiableImpl[] result, int index ) 
    {
        int ctr = 0 ;
        while (iter.hasNext() ) {
            Object obj = iter.next() ;
            if (ctr >= result.length)
                error( "Too many IdentifiableImpls returned by iterator" + 
                    index) ;

            if (!result[ctr].equals( obj ))
                error( "Wrong IdentifiableImpl " + result[ctr] +
                    " in iterator" + index ) ;
            ctr++ ;
        }

        if (ctr != result.length)
            error( "Too few identifiableImpls returned by iterator" + 
                index + " (ctr=" + ctr + " length=" + result.length +
                ")" ) ; 
    }

    @Test
    public void testResolve()
    {
        try {
            org.omg.CORBA.Object obj = orb.resolve_initial_references(
                "RootPOA" ) ;
            POA root = (POA)obj ;
        } catch (Exception exc) {
            error( "\tFailure to resolve root POA" ) ;
        }
    }

    @Test
    public void testCorbalocIOR() {

        out.println( "Testing Corbaloc URL" ) ;

        IIOPAddress addr;
        AlternateIIOPAddressComponent iiopAddressComponent;
        String key = "NameService";
        byte[] fullKey =  key.getBytes();

        ObjectKey okey = orb.getObjectKeyFactory().create( fullKey ) ;
        ObjectKeyTemplate oktemp = okey.getTemplate() ;
        IORTemplate iortemp = IORFactories.makeIORTemplate(oktemp);

        // Make sure the standard stuff works first
        //
        org.omg.CORBA.Object ref = orb.string_to_object(
                        "corbaloc:iiop:1.2@pico:4444/NameService");
        IOR ior1 = orb.getIOR(ref,false);

        addr = IIOPFactories.makeIIOPAddress( "pico", 4444);
        IIOPProfileTemplate profileTemplate_1_2 = 
            IIOPFactories.makeIIOPProfileTemplate(orb, GIOPVersion.V1_2, addr);

        iortemp.add(profileTemplate_1_2);

        IOR ior2 = iortemp.makeIOR( orb, "", okey.getId() ) ;

        out.println("IOR1 = " + ior1.stringify());
        out.println("IOR2 = " + ior2.stringify());

        if (!ior1.equals(ior2))
            error( "The 2 IORs are different for standard IOR" ) ;
 
        // Now verify that the more convoluted/complex stuff works

        okey = orb.getObjectKeyFactory().create( fullKey ) ;
        oktemp = okey.getTemplate() ;
        iortemp = IORFactories.makeIORTemplate(oktemp);

        /*
        // This does not work since CorbalocURL.java validates the
        // GIOP version so GIOP version 2.9 throw exception
        org.omg.CORBA.Object ref = orb.string_to_object(
                        "corbaloc:iiop:1.2@pico:4444,:1.1@femto:5555,:1.0@tera:6666,:2.9@mega:7777,:1.0@tera:6667,:1.2@pico:4445,:1.1@femto:5556,:1.2@pico:4446/NameService");
        IOR ior1 = IORFactories.getIOR(ref);
        */
        
        ref = orb.string_to_object(
                        "corbaloc:iiop:1.2@pico:4444,:1.1@femto:5555,:1.0@tera:6666,:1.0@tera:6667,:1.2@pico:4445,:1.1@femto:5556,:1.2@pico:4446/NameService");
        ior1 = orb.getIOR(ref,false);

        addr = IIOPFactories.makeIIOPAddress( "pico", 4444);
        profileTemplate_1_2 = 
            IIOPFactories.makeIIOPProfileTemplate(orb, GIOPVersion.V1_2, addr);
        addr = IIOPFactories.makeIIOPAddress( "pico", 4445);
        iiopAddressComponent = IIOPFactories.makeAlternateIIOPAddressComponent(addr);
        profileTemplate_1_2.add(iiopAddressComponent);
        addr = IIOPFactories.makeIIOPAddress( "pico", 4446);
        iiopAddressComponent = IIOPFactories.makeAlternateIIOPAddressComponent(addr);
        profileTemplate_1_2.add(iiopAddressComponent);

        /*
        addr = IIOPFactories.makeIIOPAddress( "mega", 7777);
        IIOPProfileTemplate profileTemplate_2_9 =
            IIOPFactories.makeIIOPProfileTemplate(orb, new GIOPVersion(2,9), addr);
        */

        addr = IIOPFactories.makeIIOPAddress( "femto", 5555);
        IIOPProfileTemplate profileTemplate_1_1 =
            IIOPFactories.makeIIOPProfileTemplate(orb, GIOPVersion.V1_1, addr);
        addr = IIOPFactories.makeIIOPAddress( "femto", 5556);
        iiopAddressComponent = IIOPFactories.makeAlternateIIOPAddressComponent(addr);
        profileTemplate_1_1.add(iiopAddressComponent);

        List profileList_1_0 = new ArrayList();
        addr = IIOPFactories.makeIIOPAddress( "tera", 6666);
        IIOPProfileTemplate profileTemplate_1_0 =
            IIOPFactories.makeIIOPProfileTemplate(orb, GIOPVersion.V1_0, addr);
        profileList_1_0.add(profileTemplate_1_0);
        addr = IIOPFactories.makeIIOPAddress( "tera", 6667);
        profileTemplate_1_0 =
            IIOPFactories.makeIIOPProfileTemplate(orb, GIOPVersion.V1_0, addr);
        profileList_1_0.add(profileTemplate_1_0);

        iortemp.add(profileTemplate_1_2);
        // iortemp.add(profileTemplate_2_9);
        iortemp.add(profileTemplate_1_1);
        iortemp.addAll(profileList_1_0);

        ior2 = iortemp.makeIOR( orb, "", okey.getId() ) ;

        out.println("IOR1 = " + ior1.stringify());
        out.println("IOR2 = " + ior2.stringify());

        if (!ior1.equals(ior2))
            error( "The 2 IORs are different for the complex case" ) ;
    }

    @Test
    public void testZeroPortPolicy()
    {
        out.println( "Testing ZeroPortPolicy" ) ;

        POA rootpoa = null ;

        try {
            rootpoa = (POA)orb.resolve_initial_references( "RootPOA" ) ;
        } catch (org.omg.CORBA.ORBPackage.InvalidName inv) {
            error( "ZeroPortPolicy test failed with exception " + inv ) ;
        }

        Policy[] policies = { 
            rootpoa.create_id_assignment_policy( IdAssignmentPolicyValue.SYSTEM_ID ),
            ZeroPortPolicy.getPolicy() } ;

        POA testpoa = null ;

        try {
            testpoa = rootpoa.create_POA( "TestPOA", null, policies ) ;
        } catch (org.omg.PortableServer.POAPackage.AdapterAlreadyExists ex) {
            error( "ZeroPortPolicy test failed with exception " + ex ) ;
        } catch (org.omg.PortableServer.POAPackage.InvalidPolicy ip) {
            error( "ZeroPortPolicy test failed with exception " + ip ) ;
        }

        org.omg.CORBA.Object obj = null ;

        try {
            obj = testpoa.create_reference( "IDL:omg.org/CORBA/Object:1.0" ) ;
        } catch (org.omg.PortableServer.POAPackage.WrongPolicy wp ) {
            error( "ZeroPortPolicy test failed with exception " + wp ) ;
        }

        IOR ior = orb.getIOR( obj, false ) ;

        // Check that all IIOPProfileTemplate instances have their primary port set to 0
        Iterator iter = ior.iteratorById( TAG_INTERNET_IOP.value ) ;
        while (iter.hasNext()) {
            IIOPProfile prof = (IIOPProfile)iter.next() ;
            IIOPProfileTemplate temp = (IIOPProfileTemplate)prof.getTaggedProfileTemplate() ;
            if (temp.getPrimaryAddress().getPort() != 0)
                error( "\tZeroPortPolicy test failed" ) ;
        }
    }

    @Test
    public void testORBVersion() 
    {
        out.println( "Testing ORBVersion" ) ;
        if (ORBVersionFactory.getFOREIGN().getORBType() != ORBVersion.FOREIGN)
            error( "\tBad encoding for FOREIGN version" ) ;
        if (ORBVersionFactory.getOLD().getORBType() != ORBVersion.OLD)
            error( "\tBad encoding for OLD version" ) ;
        if (ORBVersionFactory.getNEW().getORBType() != ORBVersion.NEW)
            error( "\tBad encoding for NEW version" ) ;
        if (ORBVersionFactory.getNEWER().getORBType() != ORBVersion.NEWER)
            error( "\tBad encoding for NEWER version" ) ;
        if (ORBVersionFactory.getPEORB().getORBType() != ORBVersion.PEORB)
            error( "\tBad encoding for NEWER version" ) ;

        if (ORBVersionFactory.getFOREIGN().equals( ORBVersionFactory.getOLD() ))
            error( "\tFOREIGN == OLD!" ) ;

        if (ORBVersionFactory.getOLD().equals( ORBVersionFactory.getNEW() ))
            error( "\tOLD == NEW!" ) ;

        if (ORBVersionFactory.getNEW().equals( ORBVersionFactory.getNEWER() ))
            error( "\tNEW == NEWER!" ) ;

        if (!ORBVersionFactory.getNEW().equals( ORBVersionFactory.getNEW() ))
            error( "\tNEW != NEW!" ) ;
    }

    @Test
    public void testORBVersionFactory() 
    {
        out.println( "Testing ORBVersionFactory" ) ;
        byte[] test = { 0x00 } ;
        int pos = test.length - 1 ;

        test[pos] = ORBVersion.NEWER ;
        InputStream is = new EncapsInputStream( orb, test, test.length ) ;
        ORBVersion version = ORBVersionFactory.create( is ) ;
        if (!version.equals( ORBVersionFactory.getNEWER() ))
            error( "\tcreate bad version from array1" ) ;
        
        test[pos] = ORBVersion.NEWER + 50 ;
        is = new EncapsInputStream( orb, test, test.length ) ;
        version = ORBVersionFactory.create( is ) ;
        if (version.getORBType() != ORBVersion.NEWER + 50)
            error( "\tcreate bad version from array2" ) ;
        
        test[pos] = -1 ;
        is = new EncapsInputStream( orb, test, test.length ) ;
        version = ORBVersionFactory.create( is ) ;
        if (version.getORBType() != -1)
            error( "\tcreate bad version from array3" ) ;
        
        OutputStream os = newOutputStream() ;
        os.write_octet( ORBVersion.PEORB ) ;
        is = makeInputStream( os ) ;
        version = ORBVersionFactory.create( is ) ;
        if (!version.equals( ORBVersionFactory.getPEORB() ))
            error( "\tcreate bad version from input stream" ) ;

        if (!ORBVersionFactory.getORBVersion().equals( ORBVersionFactory.getPEORB() ))
            error( "\tDefault ORB version is not NEWER" ) ;
    }

    @Test
    public void testIdentifiableContainerBase1() 
    {
        out.println( "Testing IdentifiableContainerBase" ) ;

        IdentifiableImpl[] obj = {
            new IdentifiableImpl( 0, 27 ) ,
            new IdentifiableImpl( 1, 28 ) ,
            new IdentifiableImpl( 0, 29 ) ,
            new IdentifiableImpl( 0, 30 ) ,
            new IdentifiableImpl( 2, 31 ) ,
            new IdentifiableImpl( 0, 32 ) ,
            new IdentifiableImpl( 2, 33 ) 
        } ;

        IdentifiableContainerBase icb = new IdentifiableContainerBase() ;
        for (int ctr=0; ctr<obj.length; ctr++)
            icb.add( obj[ctr] ) ;

        final int NUM_TESTS = 4 ;

        Iterator[] iters = new Iterator[ NUM_TESTS ] ;
        for (int ctr=0; ctr<NUM_TESTS; ctr++ )
            iters[ctr] = icb.iteratorById( ctr ) ;

        IdentifiableImpl[][] results = {
            { obj[0], obj[2], obj[3], obj[5] },
            { obj[1] },
            { obj[4], obj[6] },
            { } 
        } ;
    
        for (int ctr=0; ctr<NUM_TESTS; ctr++ )
            checkIdentifiableIterator( iters[ctr], results[ctr], ctr ) ;
    }

    @Test
    public void testIdentifiableContainerBase2() 
    {
        // This also indirectly tests equals on GenericIdentifiable and all tagged components
        out.println( "Testing IdentifiableContainerBase and TaggedComponentFactoryFinder" ) ;

        List cb = new LinkedList() ;
        TaggedComponentFactoryFinder finder = new TaggedComponentFactoryFinderImpl(orb) ;
        finder.registerFactory( IIOPFactories.makeAlternateIIOPAddressComponentFactory() ) ;
        finder.registerFactory( IIOPFactories.makeCodeSetsComponentFactory() ) ;
        finder.registerFactory( IIOPFactories.makeJavaCodebaseComponentFactory() ) ;
        finder.registerFactory( IIOPFactories.makeORBTypeComponentFactory() ) ;
        finder.registerFactory( IIOPFactories.makeMaxStreamFormatVersionComponentFactory() ) ;
        finder.registerFactory( IIOPFactories.makeAlternateIIOPAddressComponentFactory() ) ;

        IIOPAddress addr1 = IIOPFactories.makeIIOPAddress( "FOO", 32000 ) ;
        IIOPAddress addr2 = IIOPFactories.makeIIOPAddress( "BAR", 42000 ) ;
        byte[] data = { 23, 34, 41, 0, 7, 9, 123, 111, 97, 64 } ;

        TaggedComponent[] comps = {
            new GenericTaggedComponent( 1234, data ),
            IIOPFactories.makeJavaCodebaseComponent( "http://foo.sun.com:34567"),
            IIOPFactories.makeORBTypeComponent( 0x47653243 ),
            IIOPFactories.makeAlternateIIOPAddressComponent( addr1 ),
            IIOPFactories.makeCodeSetsComponent( orb ),
            IIOPFactories.makeAlternateIIOPAddressComponent( addr2 )
        } ;

        for (int ctr=0; ctr<comps.length; ctr++)
            cb.add( comps[ctr] ) ;

        OutputStream os = newOutputStream() ;

        EncapsulationUtility.writeIdentifiableSequence( cb, os ) ;

        InputStream is = makeInputStream( os ) ;

        List result = new LinkedList() ;

        EncapsulationUtility.readIdentifiableSequence( result, finder, is ) ;

        Iterator iter = result.iterator() ;
        int ctr = 0 ;
        while (iter.hasNext()) {
            Object obj = iter.next() ;
            if (ctr >= comps.length)
                error( "Iterator return too many component for IdentifiableContainerBase" ) ;

            if (!comps[ctr].equals( obj ))
                error( "Incorrect component read back for ctr = " + ctr ) ;
            
            ctr++ ;
        }

        if (ctr != comps.length)
            error( "Iterator returned too few components for IdentifiableContainerBase" ) ;
    }

    @Test
    public void testGenericTaggedComponent() 
    {
        out.println( "Testing GenericTaggedComponent" ) ;

        // Some random data
        int id = 424 ;
        byte[] data = { 0x12, 0x23, 0x31, 0x33, 0x7A, 0x27, 0x6B, 0x36, 0x7A } ;
        GenericTaggedComponent enc = new GenericTaggedComponent( id, data ) ;

        if (id != enc.getId())
            error( "bad id" ) ;

        if (!equal( data, enc.getData() ))
            error( "bad data" ) ;

        // Write out and read back
        OutputStream os = newOutputStream() ;
        enc.write( os ) ;
        InputStream is = makeInputStream( os ) ;

        GenericTaggedComponent newEnc = new GenericTaggedComponent( id, is ) ;

        if (!equal( data, newEnc.getData() ))
            error( "bad data read back" ) ;
    }

    @Test
    public void testIIOPAddress() 
    {
        out.println( "Testing IIOPAddress" ) ;

        // Construct and verify
        String host = "foo" ;
        int port = 1234 ;
        IIOPAddress addr = IIOPFactories.makeIIOPAddress( host, port ) ;

        if (!host.equals(addr.getHost()))
            error( "incorrect host" ) ;

        if (port != addr.getPort())
            error( "incorrect port" ) ;

        // test equals
        Object obj = new Object() ;
        if (addr.equals( obj ))
            error( "IIOPAddress equal to object" ) ;

        IIOPAddress addr2 = IIOPFactories.makeIIOPAddress( host, port ) ;
        if (!addr.equals( addr2 ))
            error( "IIOPAddress equals check failed" ) ;

        // Test for range checking on constructor
        boolean exceptionOK = false ;
        try {
            IIOPFactories.makeIIOPAddress( "FOO", -2 ) ;
        } catch (BAD_PARAM exc) {
            exceptionOK = true ;
        } catch (Throwable thr) {
            error( "Unexpected exception thrown on out-of-range port for IIOPAddress (1)" ) ;
        }
        if (!exceptionOK)
            error( "No exception thrown on out-of-range port for IIOPAddress (1)" ) ;

        exceptionOK = false ;
        try {
            IIOPFactories.makeIIOPAddress( "FOO", 65536 ) ;
        } catch (BAD_PARAM exc) {
            exceptionOK = true ;
        } catch (Throwable thr) {
            error( "Unexpected exception thrown on out-of-range port for IIOPAddress (2)" ) ;
        }
        if (!exceptionOK)
            error( "No exception thrown on out-of-range port for IIOPAddress (2)" ) ;

        exceptionOK = false ;
        try {
            IIOPFactories.makeIIOPAddress( "FOO", 130232 ) ;
        } catch (BAD_PARAM exc ) {
            exceptionOK = true ;
        } catch (Throwable thr) {
            error( "Unexpected exception thrown on out-of-range port for IIOPAddress (3)" ) ;
        }
        if (!exceptionOK)
            error( "No exception thrown on out-of-range port for IIOPAddress (3)" ) ;

        // read/write test, with port <32768 and >= 32768
        IIOPAddress[] addrs = {
            IIOPFactories.makeIIOPAddress( "FOO.SUN.COM", 23 ) ,
            IIOPFactories.makeIIOPAddress( "FOO.SUN.COM", 32768 ) ,
            IIOPFactories.makeIIOPAddress( "FOO.SUN.COM", 40151 ) ,
            IIOPFactories.makeIIOPAddress( "FOO.SUN.COM", 65535 ) 
        } ;

        OutputStream os = newOutputStream() ;

        for (int ctr=0; ctr<addrs.length; ctr++ ) 
            addrs[ctr].write( os ) ;

        InputStream is = makeInputStream( os ) ;

        for (int ctr=0; ctr<addrs.length; ctr++ ) {
            IIOPAddress testAddr = IIOPFactories.makeIIOPAddress( is ) ;
            if (!testAddr.equals( addrs[ctr] ))
                error( "Expected IIOPAddress " + addrs[ctr] + " got IIOPAddress " + testAddr ) ;
        }
    }

    @Test
    public void testIIOPProfileTemplate() 
    {
        out.println( "Testing IIOPProfileTemplate and IIOPProfile" ) ;

        int scid = ORBConstants.FIRST_POA_SCID ;
        String orbid = "ORB1" ;
        String[] ss = { "first", "second", "third" } ;
        ObjectAdapterId poaid = new ObjectAdapterIdArray( ss ) ;
        int serverid = -123 ;
        byte[] id = { 0x00, 0x00, 0x33, 0x44, 0x21, 0x23, 0x00 } ;
        ObjectId oid = IORFactories.makeObjectId( id ) ;

        POAObjectKeyTemplate temp = new POAObjectKeyTemplate( orb, scid, 
            serverid, orbid, poaid ) ;

        GIOPVersion gversion = GIOPVersion.V1_2 ;
        String host = "FOO" ;
        int port = 34567 ;
        IIOPAddress primary = IIOPFactories.makeIIOPAddress( host, port ) ;
        
        IIOPProfileTemplate ptemp = IIOPFactories.makeIIOPProfileTemplate( orb, 
            gversion, primary ) ;

        if (ptemp.getId() != TAG_INTERNET_IOP.value ) 
            error( "IIOPProfileTemplate has bad id" ) ;

        if (!ptemp.getGIOPVersion().equals( gversion ) )
            error( "Bad major version returned from IIOPProfileTemplate" ) ;

        if (!primary.equals( ptemp.getPrimaryAddress() )) 
            error( "Bad address returned from IIOPProfileTemplate:" ) ;

        IIOPProfile prof = (IIOPProfile)(ptemp.create( temp, oid )) ;

        if (prof.getTaggedProfileTemplate() != ptemp)
            error( "IIOPProfile created from template has bad object key template" ) ;

        if (!oid.equals(prof.getObjectId() ))
            error( "IIOPProfile created from template has bad object id" ) ;

        ORBTypeComponent comp = IIOPFactories.makeORBTypeComponent( 
            0x34567ABF ) ;

        try {
            ptemp.add( comp ) ;
        } catch (Throwable thr) {
            error( "Unexpected exception adding component to 1.2 IIOPProfileTemplate" ) ;
        }

        OutputStream os = newOutputStream() ;

        prof.write( os ) ;

        InputStream is = makeInputStream( os ) ;

        IdentifiableFactory fact = IIOPFactories.makeIIOPProfileFactory() ;
        IIOPProfile testProf = (IIOPProfile)fact.create( orb, is ) ;

        if (!prof.isEquivalent( testProf ))
            error( "Profile and unmarshalled copy fail isEquivalent test" ) ;

        // test that testProf has correct components in its template
        // Note: if the java.rmi.server.codebase property is set, we
        // will have a JavaCodebaseComponent here as well as the ORBTypeComponent.
        try {
            TaggedComponent tc1 = null ;
            TaggedComponent tc2 = null ;

            Iterator<TaggedComponent> iter = testProf.getTaggedProfileTemplate().iterator() ;

            if (!iter.hasNext())
                error( "No components in testProf" ) ;

            tc1 = iter.next() ;

            if (iter.hasNext()) {
                tc2 = iter.next() ;

                if (iter.hasNext())
                    error( "too many components in testProf" ) ;

                // Don't depend on iteration order
                if (tc1 instanceof ORBTypeComponent) {
                    if (!tc1.equals( comp )) 
                        error( "ORBTypeComponent in testProf does not match original" ) ;
                    if (!(tc2 instanceof JavaCodebaseComponent)) 
                        error( "Other component is not JavaCodebaseComponent" ) ;
                } else {
                    if (!tc2.equals( comp )) 
                        error( "ORBTypeComponent in testProf does not match original" ) ;
                    if (!(tc1 instanceof JavaCodebaseComponent))
                        error( "Other component is not JavaCodebaseComponent" ) ;
                }
            } else {
                if (!comp.equals( comp ))
                    error( "ORBTypeComponent in testProf does not match original" ) ;
            }
        } catch (Throwable thr) {
            error( "unexpected exception in examining testProf" ) ;
        }

        // test that 1.0 IIOPProfileTemplates do not support addition of components
        IIOPProfileTemplate ptemp2 = IIOPFactories.makeIIOPProfileTemplate( orb, 
            GIOPVersion.V1_0, primary ) ;
        
        boolean expectedException = false ;
        try {
            ptemp2.add( comp ) ;
        } catch (UnsupportedOperationException uoe) {
            expectedException = true ;
        } catch (Throwable thr) {
            error( "Component add to 1.0 IIOPProfileTemplate threw unexpected exception" ) ;
        }

        if (!expectedException)
            error( "Component add to 1.0 IIOPProfileTemplate succeeded incorrectly" ) ;
    }

    public POAObjectKeyTemplate makePOAObjectKeyTemplate( ObjectAdapterId poaid )
    {
        int scid = ORBConstants.FIRST_POA_SCID ;
        String orbid = "AVeryLongORBIdName" ;
        int serverid = -123 ;

        POAObjectKeyTemplate temp = new POAObjectKeyTemplate( orb, scid, 
            serverid, orbid, poaid ) ;

        return temp ;
    }

    public IIOPProfileTemplate makeIIOPProfileTemplate( int port )
    {
        String host = "FOO" ;
        IIOPAddress primary = IIOPFactories.makeIIOPAddress( host, port ) ;
        
        IIOPProfileTemplate ptemp = IIOPFactories.makeIIOPProfileTemplate( orb,
            GIOPVersion.V1_2, primary ) ;

        return ptemp ;
    }

    @Test
    public void testIOR() 
    {
        out.println( "Testing IOR" ) ;

        // Test IOR()
        IOR ior1 = IORFactories.makeIOR( orb ) ;
        if (!"".equals( ior1.getTypeId() ))
            error( "IOR() should have null typeid" ) ;

        Iterator iter = ior1.iterator() ;
        if (iter.hasNext())
            error( "IOR() should not have any profiles" ) ;

        // Test IOR(String, IIOPPRofileTemplate, ObjectId ) 
        String[] ss = { "foo", "bar" } ;
        ObjectAdapterId poaid = new ObjectAdapterIdArray( ss ) ;
        POAObjectKeyTemplate poktemp1 = makePOAObjectKeyTemplate( poaid ) ;
        IIOPProfileTemplate ptemp1 = makeIIOPProfileTemplate( 45671 ) ;
        String URL = "htp://foo.sun.com:9999" ;
        JavaCodebaseComponent jcomp = 
            IIOPFactories.makeJavaCodebaseComponent( URL ) ;
        ORBTypeComponent comp1 = IIOPFactories.makeORBTypeComponent( 
            0x34567ABF ) ;
        IIOPAddress addr = IIOPFactories.makeIIOPAddress( "FOO", 32451 ) ;
        AlternateIIOPAddressComponent comp2 = 
            IIOPFactories.makeAlternateIIOPAddressComponent( addr ) ;

        ptemp1.add( comp1) ;
        ptemp1.add( comp2 ) ;
        ptemp1.add( jcomp ) ;

        IORTemplate iortemp = IORFactories.makeIORTemplate( poktemp1 ) ;
        iortemp.add( ptemp1 ) ;

        byte[] id = { 0x00, 0x00, 0x33, 0x44, 0x21, 0x23, 0x00 } ;
        ObjectId oid = IORFactories.makeObjectId( id ) ;

        String typeid = "foo:bar" ;
        IOR ior3 = iortemp.makeIOR( orb, typeid, oid ) ;

        if (!ior3.getTypeId().equals( typeid ))
            error( "IOR(ORB,String,IORTemplate,ObjectId) has bad typeid" ) ;

        IIOPProfile iprof1 = (IIOPProfile)(ptemp1.create( poktemp1, oid )) ;
        
        iter = ior3.iterator() ;
        if (!iter.hasNext())
            error( "ior3 has no profiles" ) ;

        Object obj = iter.next() ;

        if (!iprof1.equals( obj ))
            error( "ior3 has wrong profile" ) ;

        if (iter.hasNext())
            error( "ior3 has too many profiles" ) ;

        // Create another IIOPProfile
        String[] ss2 = { "bar" } ;
        ObjectAdapterId poaid2 = new ObjectAdapterIdArray( ss2 ) ;
        POAObjectKeyTemplate poktemp2 = makePOAObjectKeyTemplate( poaid2 ) ;
        IIOPProfileTemplate ptemp2 = makeIIOPProfileTemplate( 36123 ) ;
        ptemp2.add( jcomp ) ;
        ptemp2.add( comp2 ) ;
        
        IIOPProfile iprof2 = (IIOPProfile)(ptemp2.create( poktemp2, oid )) ;

        // Create a GenericTaggedProfile to use as a profile
        int gid = 234 ;
        byte[] data = { 2, 14, 56, 37, 31, 42, 1, 9, 116, 57 } ;
        GenericTaggedProfile gprof = new GenericTaggedProfile( orb, gid, data ) ;
    
        // Create ior containing iprof1, iprof2, gprof
        IOR ior4 = IORFactories.makeIOR( orb, typeid ) ;
        ior4.add( iprof1 ) ;
        ior4.add( iprof2 ) ;
        ior4.add( gprof ) ;
        ior4.makeImmutable() ;

        OutputStream os = newOutputStream() ;

        ior4.write( os ) ;

        InputStream is = makeInputStream( os ) ;

        IOR ior5 = IORFactories.makeIOR( orb, is ) ;

        if (!ior4.equals( ior5 ))
            error( "Read and written IORs are different" ) ;

        // Test makeImmutable
        ior4.makeImmutable() ;
        iter = ior4.iterator() ;
        boolean correct = false ;

        try {
            iter.next() ;
            iter.remove() ;
        } catch (UnsupportedOperationException uoe) {
            correct = true ;
        } catch (Throwable t) {
            error( "Bad exception on iterator.remove() for frozen list" ) ;
        }

        if (!correct)
            error( "iterator.remove succeeded on frozen list" ) ;

        IIOPProfile prof3 = (IIOPProfile)(iter.next()) ;
        Iterator iter2 = prof3.getTaggedProfileTemplate().iterator() ;

        correct = false ;

        try {
            iter2.next() ;
            iter2.remove() ;
        } catch (UnsupportedOperationException uoe) {
            correct = true ;
        } catch (Throwable t) {
            error( "Bad exception on iterator.remove() for frozen list" ) ;
        }

        if (!correct)
            error( "iterator.remove succeeded on frozen list" ) ;
    }

    private IORTemplate makeIORTemplate( String[] oaid, int[] ports, 
        boolean addComponent )
    {
        POAObjectKeyTemplate poktemp = 
            makePOAObjectKeyTemplate( new ObjectAdapterIdArray( oaid ) ) ;

        IORTemplate iort = IORFactories.makeIORTemplate( poktemp ) ;
        
        for (int ctr=0; ctr<ports.length; ctr++) {
            TaggedProfileTemplate tptemp = makeIIOPProfileTemplate( ports[ctr] ) ;

            if ((ctr==0) && addComponent) {
                byte[] data = { 23, 34, 41, 0, 7, 9, 123, 111, 97, 64 } ;
                TaggedComponent comp = new GenericTaggedComponent( 1234, data ) ;
                tptemp.add( comp ) ;
            }

            iort.add( tptemp ) ;
        } 

        iort.makeImmutable() ;
        return iort ;
    }

    private IORTemplateList makeIORTemplateList( String[][] oaids, 
        int[][] ports, boolean addComponent ) 
    {
        IORTemplateList result = IORFactories.makeIORTemplateList() ;
        for (int ctr=0; ctr<oaids.length; ctr++)
            result.add( makeIORTemplate( oaids[ctr], ports[ctr],
                (ctr==0) && addComponent ) );
        return result ;
    }

    @Test
    public void testIORTemplateList() 
    {
        out.println( "Testing IORTemplateList" ) ;

        String[][] oaids = 
        {
            { "1", "2", "3", "4", "5", "6" },
            { "1", "3", "4", "6" },
            { "4", "5", "6" },
            { "1", "2", "3", "4", "5", "6", "7", "8" }
        } ;

        int[][] ports1  = 
        {
            { 34567, 23416, 38491, 9321, 65001 },
            { 23416, 38491, 9321, 65001 },
            { 38491, 9321, 65001 },
            { 34567, 65001 },
        } ;

        int[][] ports2  = 
        {
            { 34567, 23416, 38491, 9321, 65001 },
            { 23416, 38491, 65001 },
            { 38491, 9321, 65001 },
            { 34567, 65001, 3232 },
        } ;

        IORTemplateList iortl1 = makeIORTemplateList( oaids, ports1, false ) ;
        IORTemplateList iortl2 = makeIORTemplateList( oaids, ports1, false ) ;
        IORTemplateList iortl3 = makeIORTemplateList( oaids, ports1, true ) ;
        IORTemplateList iortl4 = makeIORTemplateList( oaids, ports2, false ) ;

        testIORFactory( iortl1, iortl2, true, true ) ;
        testIORFactory( iortl1, iortl3, false, true ) ;
        testIORFactory( iortl1, iortl4, false, false ) ;
    }

    @Test
    public void testIORTemplate() 
    {
        out.println( "Testing IORTemplate" ) ;

        String[] oaid = { "1", "2", "3", "4", "5", "6" } ;
        int[] ports  = { 34567, 23416, 38491, 9321, 65001 } ;
        IORTemplate iort = makeIORTemplate( oaid, ports, false ) ;

        // Make sure that iort and its profiles are immutable
        iort.makeImmutable() ;

        Iterator iter = iort.iterator() ;
        IIOPProfileTemplate prof = (IIOPProfileTemplate)(iter.next()) ;
        Iterator iter2 = prof.iterator() ;

        boolean correct = false ;
        try {
            iter.remove() ;
        } catch (UnsupportedOperationException err) {
            correct = true ;
        } catch (Throwable t) {
            error( "remove on Immutable IORTemplate threw wrong exception" ) ;
        }

        if (!correct)
            error( "remove on Immutable IORTemplate succeeded incorrectly" ) ;
    
        correct = false ;
        try {
            iter2.next() ;
        } catch (NoSuchElementException err) {
            correct = true ;
        } catch (Throwable t) {
            error( "remove on Immutable IIOPProfile threw wrong exception" ) ;
        }

        if (!correct)
            error( "remove on Immutable IIOPProfile succeeded incorrectly" ) ;

        // Test equals() and isEquivalent() methods

        IORTemplate iort2 = makeIORTemplate( oaid, ports, false ) ;

        IORTemplate iort3 = makeIORTemplate( oaid, ports, true ) ;
        int[] ports4  = { 34567, 23416, 38491, 9321 } ;
        IORTemplate iort4 = makeIORTemplate( oaid, ports4, false ) ;

        testIORFactory( iort, iort2, true, true ) ;
        testIORFactory( iort, iort3, false, true ) ;
        testIORFactory( iort, iort4, false, false ) ;
    }

    private void testIORFactory( 
        IORFactory iorfactory1, IORFactory iorfactory2,
        boolean equalResult, boolean equivalentResult )
    {
        if (equalResult != iorfactory1.equals( iorfactory2 ))
            error( "testIORFactory: equals did not return " + equalResult ) ;

        if (equivalentResult != iorfactory1.isEquivalent( iorfactory2 ))
            error( "testIORFactory: isEquivalent did not return " + equalResult ) ;

        String typeid = "FOO:BAR:BAZ" ;
        byte[] id = { 0x00, 0x00, 0x33, 0x44, 0x21, 0x23, 0x00 } ;
        ObjectId oid = IORFactories.makeObjectId( id ) ;

        // Test that the same results hold on the IORs created from the
        // factories.
        IOR ior1 = iorfactory1.makeIOR( orb, typeid, oid ) ;
        IOR ior2 = iorfactory2.makeIOR( orb, typeid, oid ) ;

        if (equalResult != ior1.equals( ior2 ))
            error( "testIORFactory: equals on IOR did not return " + equalResult ) ;

        if (equivalentResult != ior1.isEquivalent( ior2 ))
            error( "testIORFactory: isEquivalent on IOR did not return " + equalResult ) ;

        // Test that ior1.getIORTemplates() equals and isEquivalent to iorfactory1.
        IORTemplateList iorf = ior1.getIORTemplates() ;
        IORTemplateList iortl = convertToIORTemplateList( iorfactory1 ) ;

        if (!iorf.equals( iortl ))
            error( "testIORFactory: equals failed on result of getIORTemplates()" ) ;

        if (!iorf.isEquivalent( iortl))
            error( "testIORFactory: isEquivalent() failed on result of getIORTemplates()" ) ;
    }

    private IORTemplateList convertToIORTemplateList( IORFactory factory )
    {
        IORTemplateList result = null ;

        if (factory instanceof IORTemplateList)
            result = (IORTemplateList)factory ;
        else if (factory instanceof IORTemplate) {
            IORTemplate ftemp = IORTemplate.class.cast( factory ) ;
            IORTemplateList iortl = IORFactories.makeIORTemplateList() ;
            iortl.add( ftemp ) ;
            iortl.makeImmutable() ;
            result = iortl ;
        }

        return result ;
    }

    @Test
    public void testOldJIDLObjectKeyTemplate() 
    {
        out.println( "Testing OldJIDLObjectKeyTemplate" ) ;

        int scid = 36 ;
        int serverid = -123 ;

        OldJIDLObjectKeyTemplate temp = new OldJIDLObjectKeyTemplate( orb,
            ObjectKeyFactoryImpl.JAVAMAGIC_OLD, scid, serverid) ;
        checkOldJIDLObjectKeyTemplate( temp, scid, serverid,
            ORBVersionFactory.getOLD() ) ;

        temp = new OldJIDLObjectKeyTemplate( orb,
            ObjectKeyFactoryImpl.JAVAMAGIC_NEW, scid, serverid) ;
        checkOldJIDLObjectKeyTemplate( temp, scid, serverid,
            ORBVersionFactory.getNEW() ) ;

        boolean failed = false ;
        try {
            temp = new OldJIDLObjectKeyTemplate( orb, ObjectKeyFactoryImpl.JAVAMAGIC_NEWER, 
                scid, serverid ) ;
        } catch (INTERNAL ex) {
            failed = true ;
        } catch (Exception exc) {
            error( "Unexpected exception in creating OldJIDLObjectKeyTemplate with bad magic" ) ;
        }

        if (!failed)
            error( "OldJIDLObjectKeyTemplate succeeded with bad magic" ) ;
    } 

    private void checkOldJIDLObjectKeyTemplate( OldJIDLObjectKeyTemplate temp,
        int scid, int serverid, ORBVersion version )
    {
        if (temp.getSubcontractId() != scid)
            error( "getSubcontractId returns bad value" ) ;

        if (temp.getServerId() != serverid)
            error( "getServerId returns bad value" ) ;
           
        if (!temp.getORBVersion().equals( version ))
            error( "getORBVersion returns bad value" ) ;
    }

    @Test
    public void testJIDLObjectKeyTemplate() 
    {
        out.println( "Testing JIDLObjectKeyTemplate" ) ;

        int scid = 4 ;
        int serverid = -123 ;

        // Simple check of accessor
        JIDLObjectKeyTemplate temp = new JIDLObjectKeyTemplate( orb, scid, 
            serverid ) ;

        if (temp.getSubcontractId() != scid)
            error( "(1) getSubcontractId returns bad value" ) ;

        if (temp.getServerId() != serverid)
            error( "(1) getServerId returns bad value" ) ;
           
        if (!temp.getORBVersion().equals( ORBVersionFactory.getPEORB() ))
            error( "(1) getORBVersion returns bad value" ) ;

        // test write key method
        JIDLKeyGenerator generator = new JIDLKeyGenerator() ;
        byte[] fullKey = generator.makeKey( 
            ObjectKeyFactoryImpl.JAVAMAGIC_NEWER ) ;
        OctetSeqHolder osh = new OctetSeqHolder() ;
        CDRInputObject is = makeInputStream( fullKey ) ;
        int magic = is.read_long() ;
        scid = is.read_long() ;
        temp = new JIDLObjectKeyTemplate( orb, magic, scid, is, osh ) ;

        OutputStream os = newOutputStream() ;
        ObjectId objid = IORFactories.makeObjectId( osh.value ) ;
        temp.write( objid, os ) ;
        byte[] resultKey = getBytes( os ) ;

        if (!equal( fullKey, resultKey ))
            error( "Error in writing out object key" ) ;

        // test write template method
        os = newOutputStream() ;
        temp.write( os ) ;

        is = makeInputStream( os ) ;
        ObjectKeyTemplate newTemplate = orb.getObjectKeyFactory().createTemplate( 
            is ) ;
        if (!newTemplate.equals( temp ))
            error( "Error in writing out object key template" ) ;
    }

    private void checkIterator( String msg, Iterator iter, String values ) 
    {
        StringTokenizer st = new StringTokenizer( values ) ;
        while (st.hasMoreTokens()) {
            String elem = st.nextToken() ;
            Integer refValue = new Integer( elem ) ;

            if (!iter.hasNext())
                error( msg + ": too few elements" ) ;

            Object obj = iter.next() ;
            if (!(obj instanceof Integer))
                error( msg + ": element has wrong type" ) ;

            Integer value = (Integer)obj ;
            if (!refValue.equals(value))
                error( msg + ": got value " + value + ", expected value " +
                    refValue ) ;
        }

        if (iter.hasNext())
            error( msg + ": too many elements" ) ;
    }

    @Test
    public void testFreeezableList() 
    {
        out.println( "Testing FreezableList" ) ;

        // Create a list as a freezable list
        FreezableList flist = new FreezableList( new LinkedList() ) ;

        if (flist.isImmutable())
            error( "New FreezableList must be mutable" ) ;

        // add several elements to it
        flist.add( new Integer(1) ) ;
        flist.add( new Integer(2) ) ;
        flist.add( new Integer(3) ) ;
        flist.add( new Integer(4) ) ;
        flist.add( new Integer(5) ) ;

        // Create an Iterator
        Iterator iter = flist.iterator() ;

        // verify correct functioning of the iterator
        checkIterator( "iterator on modifiable list", iter, "1 2 3 4 5" ) ;

        // remove an element using the iterator
        iter = flist.iterator() ;
        iter.next() ;
        iter.next() ;
        iter.remove() ;

        // info( "flist = " + flist ) ;

        // verify that the list is correct
        iter = flist.iterator() ;
        checkIterator( "iterator after remove", iter, "1 3 4 5" ) ;

        // freeze the list
        flist.makeImmutable() ;

        // create an iterator
        iter = flist.iterator() ;

        // verify correct functioning of the iterator
        checkIterator( "iterator on frozen list", iter, "1 3 4 5" ) ;
         
        // try to remove an element using the iterator
        iter = flist.iterator() ;
        iter.next() ;

        boolean correct = false ;

        try {
            iter.remove() ;
        } catch (UnsupportedOperationException uoe) {
            correct = true ;
        } catch (Throwable t) {
            error( "Bad exception on iterator.remove() for frozen list" ) ;
        }

        if (!correct)
            error( "iterator.remove succeeded on frozen list" ) ;

        // Additional tests if desired:
        // - Test ListIterator
        // - Test that freezing list freezes all active iterators
    }

    @Test
    public void testObjectKey() 
    {
        out.println( "Testing ObjectKey" ) ;

        int scid = ORBConstants.FIRST_POA_SCID ;
        String orbid = "AnotherORB" ;
        String[] ss = { "A", "B", "C", "D", "E" } ;
        ObjectAdapterId poaid = new ObjectAdapterIdArray( ss ) ;
        int serverid = -123 ;
        byte[] oid = { 0x00, 0x00, 0x33, 0x44, 0x21, 0x23, 0x00 } ;
        ObjectId objid = IORFactories.makeObjectId( oid ) ;

        // Construct buffer containing object key
        OutputStream os = newOutputStream() ;
        os.write_long( ObjectKeyFactoryImpl.JAVAMAGIC_NEWER ) ;
        os.write_long( scid ) ;
        os.write_long( serverid ) ;
        os.write_string( orbid ) ;
        POANameHelper.write( os, ss ) ;
        os.write_long( oid.length ) ;
        os.write_octet_array( oid, 0, oid.length ) ;
        os.write_octet( (byte)(ORBVersion.PEORB) ) ;
        byte[] fullKey = getBytes( os ) ;
    
        POAObjectKeyTemplate temp = new POAObjectKeyTemplate( orb, scid, 
            serverid, orbid, poaid ) ;

        ObjectKey okey = IORFactories.makeObjectKey( temp, objid ) ;

        if (!equal( oid, okey.getId().getId() ))
            error( "Bad object id returned from object key" ) ;
        
        // it would be better to check for equals rather than =,
        // but that would require defining equals on the ObjectKeyTemplate
        // classes, which is not currently supported.
        if (temp != okey.getTemplate())
            error( "Bad template returned from Object Key" ) ;

        os = newOutputStream() ;
        okey.write( os ) ;
        byte[] arr1 = getBytes( os ) ;

        if (!equal( fullKey, arr1 ))
            error( "Failure in write" ) ;

        byte[] arr2 = okey.getBytes( orb ) ;

        if (!equal( fullKey, arr2 ))
            error( "Failure in getBytes" ) ;
    }

    @Test
    public void testObjectId() 
    {
        out.println( "Testing ObjectId" ) ;

        byte[] arr1 = { 0x23, 0x25, 0x27, 0x13 } ;
        ObjectId oid1 = IORFactories.makeObjectId( arr1 ) ;

        byte[] arr2 = { 0x23, 0x25, 0x27, 0x13, 0x44 } ;
        ObjectId oid2 = IORFactories.makeObjectId( arr2 ) ;

        byte[] arr3 = { 0x25, 0x27, 0x13, 0x44 } ;
        ObjectId oid3 = IORFactories.makeObjectId( arr3 ) ;

        Object obj = new Object() ;

        if (!equal( arr1, oid1.getId()))
            error( "getId returned bad value" ) ;

        if (oid1.equals( null ))
            error( "equals test with null failed" ) ;

        if (!oid1.equals( oid1 ))
            error( "equals test with self failed" ) ;

        ObjectId oid1c = IORFactories.makeObjectId( arr1 ) ;
        if (!oid1.equals( oid1c ))
            error( "equals test with clone failed" ) ;

        ObjectId oid1n = IORFactories.makeObjectId( null ) ;
        if (oid1.equals( oid1n ))
            error( "equals test with id with null contents failed" ) ;

        if (!oid1n.equals( oid1n ))
            error( "equals test with id with null contents against self failed" ) ;

        ObjectId oid2n = IORFactories.makeObjectId( null ) ;

        if (!oid1n.equals( oid2n ))
            error( "equals test with id with null contents against clone failed" ) ;

        if (oid1.equals( oid2 ))
            error( "equals test with a prefix oid failed" ) ;

        if (oid1.equals( oid3 ))
            error( "equals test with a different oid failed" ) ;
    }

    @Test
    public void testObjectKeyFactory() 
    {
        out.println( "Testing ObjectKeyFactory" ) ;

        testObjectKeyFactory_JIDL() ;
        testObjectKeyFactory_POA() ;
        testObjectKeyFactory_Wire() ;
    }
    
    abstract class KeyGeneratorBase 
    {
        protected int magic ;

        private Class oldClass ;
        private Class newClass ;

        public KeyGeneratorBase( Class oldClass, Class newClass )
        {
            this.oldClass = oldClass ;
            this.newClass = newClass ;
        }

        protected abstract void writeKeyBody( OutputStream os ) ;

        public abstract int getSCID() ;

        public abstract int getServerID() ;

        public abstract byte[] getOID() ;

        public final byte[] makeKey( int magic ) 
        {
            this.magic = magic ;
            OutputStream os = newOutputStream() ;
            os.write_long( magic ) ;
            os.write_long( getSCID() ) ;
            writeKeyBody( os ) ;
            byte[] oid = getOID() ;
            os.write_long( oid.length ) ;
            os.write_octet_array( oid, 0, oid.length ) ;
            if (magic == ObjectKeyFactoryImpl.JAVAMAGIC_NEWER)
                os.write_octet( (byte)(ORBVersion.PEORB) ) ;
            byte[] fullKey = getBytes( os ) ;
            return fullKey ;
            //return new CDRInputStream( orb, fullKey, fullKey.length ) ;
        }

        protected abstract void checkForOldClass( ObjectKeyTemplate temp ) ;

        protected abstract void checkForNewClass( ObjectKeyTemplate temp ) ;

        public void checkObjectKey( ObjectKey okey ) 
        {
            ObjectKeyTemplate oktemp = okey.getTemplate() ;

            switch (magic) {
                case (ObjectKeyFactoryImpl.JAVAMAGIC_OLD): 
                    if (!(oldClass.isInstance(oktemp)))
                        error( "Factory constructed wrong kind of key" ) ;

                    if (!oktemp.getORBVersion().equals( ORBVersionFactory.getOLD() ))
                        error( "Factory constructed wrong version of key" ) ;

                    checkForOldClass( oktemp ) ;

                    break ;

                case (ObjectKeyFactoryImpl.JAVAMAGIC_NEW): 
                    if (!(oldClass.isInstance(oktemp)))
                        error( "Factory constructed wrong kind of key" ) ;

                    if (!oktemp.getORBVersion().equals( ORBVersionFactory.getNEW() ))
                        error( "Factory constructed wrong version of key" ) ;

                    checkForOldClass( oktemp ) ;

                    break ;

                case (ObjectKeyFactoryImpl.JAVAMAGIC_NEWER): 
                    if (!(newClass.isInstance(oktemp)))
                        error( "Factory constructed wrong kind of key" ) ;

                    if (!oktemp.getORBVersion().equals( ORBVersionFactory.getPEORB() ))
                        error( "Factory constructed wrong version of key" ) ;

                    checkForNewClass( oktemp ) ;

                    break ;
            }

            if (oktemp.getSubcontractId() != getSCID())
                error( "Bad subcontract id" ) ;

            if (oktemp.getServerId() != getServerID())
                error( "Bad server id" ) ;
               
            if (!equal( getOID(), okey.getId().getId() ))
                error( "Bad object id" ) ;
        }
    }

    class JIDLKeyGenerator extends KeyGeneratorBase
    {
        public JIDLKeyGenerator()
        {
            super( OldJIDLObjectKeyTemplate.class, 
                JIDLObjectKeyTemplate.class ) ;
        }

        protected void writeKeyBody( OutputStream os ) 
        {
            os.write_long( getServerID() ) ;
        }

        public int getSCID()
        {
            return 1 ;
        }

        public int getServerID()
        {
            return -123 ;
        }

        public byte[] getOID()
        {
            byte[] oid = { 0x00, 0x00, 0x33, 0x44, 0x21, 0x23, 
                0x00 } ;
            return oid ;
        }

        protected void checkForOldClass( ObjectKeyTemplate oktemp )
        {
        }

        protected void checkForNewClass( ObjectKeyTemplate oktemp )
        {
        }
    } 

    @Test
    public void testObjectKeyFactory_JIDL() 
    {
        JIDLKeyGenerator generator = new JIDLKeyGenerator() ;

        out.println( "Testing ObjectKeyFactory_JIDL" ) ;

        byte[] key = generator.makeKey( 
            ObjectKeyFactoryImpl.JAVAMAGIC_OLD ) ;
        ObjectKey okey = orb.getObjectKeyFactory().create( key ) ;
        generator.checkObjectKey( okey ) ;

        key = generator.makeKey( ObjectKeyFactoryImpl.JAVAMAGIC_NEW ) ;
        okey = orb.getObjectKeyFactory().create( key ) ;
        generator.checkObjectKey( okey ) ;

        key = generator.makeKey( ObjectKeyFactoryImpl.JAVAMAGIC_NEWER ) ;
        okey = orb.getObjectKeyFactory().create( key ) ;
        generator.checkObjectKey( okey ) ;
    }

    class POAKeyGenerator extends KeyGeneratorBase
    {
        private int oldOrbid = 23 ;
        private String orbid = "ThisIsAnORB" ;

        private int oldPoaid = 21456 ;
        private String[] ss = { "1", "2", "3.3.3", "4:4;5" } ;
        private ObjectAdapterId poaid = new ObjectAdapterIdArray( ss ) ;

        public POAKeyGenerator()
        {
            super( OldPOAObjectKeyTemplate.class, 
                POAObjectKeyTemplate.class ) ;
        }

        protected void writeKeyBody( OutputStream os ) 
        {
            os.write_long( getServerID() ) ;

            if (magic == ObjectKeyFactoryImpl.JAVAMAGIC_NEWER) {
                os.write_string( orbid ) ;
                poaid.write( os ) ;
            } else {
                os.write_long( oldOrbid ) ;
                os.write_long( oldPoaid ) ;
            }
        }

        public int getSCID()
        {
            return ORBConstants.FIRST_POA_SCID + 4 ;
        }

        public int getServerID()
        {
            return -123 ;
        }

        public byte[] getOID()
        {
            byte[] oid = { 0x00, 0x00, 0x33, 0x44, 0x21, 0x23, 
                0x00 } ;
            return oid ;
        }

        protected void checkForOldClass( ObjectKeyTemplate oktemp )
        {
            OldPOAObjectKeyTemplate temp = (OldPOAObjectKeyTemplate)oktemp ;

            if (oldOrbid != Integer.parseInt( temp.getORBId() ) )
                error( "Bad orb id" ) ;
               
            ObjectAdapterId oaid = new ObjectAdapterIdNumber( oldPoaid ) ;
            if (!oaid.equals( temp.getObjectAdapterId() ) )
                error( "POAObjectKeyTemplate.getObjectAdapterId returns bad value" ) ;
        }

        protected void checkForNewClass( ObjectKeyTemplate oktemp )
        {
            POAObjectKeyTemplate temp = (POAObjectKeyTemplate)oktemp ;

            if (!orbid.equals( temp.getORBId() ))
                error( "Bad orb id" ) ;
               
            if (!poaid.equals( temp.getObjectAdapterId() ))
                error( "POAObjectKeyTemplate.getObjectAdapterId returns bad value" ) ;
        }
    } 

    @Test
    public void testObjectKeyFactory_POA() 
    {
        out.println( "Testing ObjectKeyFactory_POA" ) ;

        POAKeyGenerator generator = new POAKeyGenerator() ;

        byte[] key = generator.makeKey( 
            ObjectKeyFactoryImpl.JAVAMAGIC_OLD ) ;
        ObjectKey okey = orb.getObjectKeyFactory().create( key ) ;
        generator.checkObjectKey( okey ) ;

        key = generator.makeKey( ObjectKeyFactoryImpl.JAVAMAGIC_NEW ) ;
        okey = orb.getObjectKeyFactory().create( key ) ;
        generator.checkObjectKey( okey ) ;

        key = generator.makeKey( ObjectKeyFactoryImpl.JAVAMAGIC_NEWER ) ;
        okey = orb.getObjectKeyFactory().create( key ) ;
        generator.checkObjectKey( okey ) ;
    }

    @Test
    public void testObjectKeyFactory_Wire() 
    {
        out.println( "Testing ObjectKeyFactory_Wire" ) ;

        byte[] fullKey = { 0x12, 0x42, 0x36, 0x72, 0x44 } ;
        // InputStream is = new CDRInputStream( orb, fullKey, fullKey.length ) ;

        ObjectKey okey = orb.getObjectKeyFactory().create( fullKey ) ;
        ObjectKeyTemplate oktemp = okey.getTemplate() ;
        if (!(oktemp instanceof WireObjectKeyTemplate))
            error( "Factory constructed wrong kind of key" ) ;
        WireObjectKeyTemplate temp = (WireObjectKeyTemplate)oktemp ;

        if (!oktemp.getORBVersion().equals( ORBVersionFactory.getFOREIGN() ))
            error( "Bad ORB version" ) ;

        if (!equal( fullKey, okey.getId().getId() ))
            error( "Did not fetch correct id from object key" ) ;
    }

    @Test
    public void testOldPOAObjectKeyTemplate() 
    {
        out.println( "Testing OldPOAObjectKeyTemplate" ) ;

        int scid = 36 ;
        int serverid = -123 ;
        int orbid = 23 ;
        int poaid = 345 ;

        OldPOAObjectKeyTemplate temp = new OldPOAObjectKeyTemplate( orb,
            ObjectKeyFactoryImpl.JAVAMAGIC_OLD, scid, serverid, orbid, poaid ) ;
        checkOldPOAObjectKeyTemplate( temp, scid, serverid, orbid, poaid,
            ORBVersionFactory.getOLD() ) ;

        temp = new OldPOAObjectKeyTemplate( orb,
            ObjectKeyFactoryImpl.JAVAMAGIC_NEW, scid, serverid, orbid, poaid ) ;
        checkOldPOAObjectKeyTemplate( temp, scid, serverid, orbid, poaid,
            ORBVersionFactory.getNEW() ) ;

        boolean failed = false ;
        try {
            temp = new OldPOAObjectKeyTemplate( orb, 
                ObjectKeyFactoryImpl.JAVAMAGIC_NEWER, 
                scid, serverid, orbid, poaid ) ;
        } catch (INTERNAL ex) {
            failed = true ;
        } catch (Exception exc) {
            error( "Unexpected exception in creating OldPOAObjectKeyTemplate with bad magic" ) ;
        }

        if (!failed)
            error( "OldPOAObjectKeyTemplate succeeded with bad magic" ) ;
    } 

    private void checkOldPOAObjectKeyTemplate( OldPOAObjectKeyTemplate temp,
        int scid, int serverid, int orbid, int poaid, ORBVersion version )
    {
        if (temp.getSubcontractId() != scid)
            error( "getSubcontractId returns bad value" ) ;

        if (temp.getServerId() != serverid)
            error( "getServerId returns bad value" ) ;

        if (orbid != Integer.parseInt( temp.getORBId() ) )
            error( "getORBId returns bad value" ) ;
               
        ObjectAdapterId oaid = new ObjectAdapterIdNumber( poaid ) ;
        if (!oaid.equals( temp.getObjectAdapterId() ) )
            error( "getObjectAdapterId returns bad value" ) ;
          
        if (!temp.getORBVersion().equals( version ))
            error( "getORBVersion returns bad value" ) ;
    }

    @Test
    public void testPOAObjectKeyTemplate() 
    {
        out.println( "Testing POAObjectKeyTemplate" ) ;

        int scid = 36 ;
        int serverid = -123 ;
        String orbid = "TheORB" ;
        String[] ss = { "ABC", "DEF", "GHI", "JKL", "MNO" } ;
        ObjectAdapterId poaid = new ObjectAdapterIdArray( ss ) ;

        // Simple check of accessor
        POAObjectKeyTemplate temp = new POAObjectKeyTemplate( orb, scid, 
            serverid, orbid, poaid ) ;

        if (temp.getSubcontractId() != scid)
            error( "(1) getSubcontractId returns bad value" ) ;

        if (temp.getServerId() != serverid)
            error( "(1) getServerId returns bad value" ) ;
           
        if (!orbid.equals( temp.getORBId() ))
            error( "(1) getORBId returns bad value" ) ;
           
        if (!poaid.equals( temp.getObjectAdapterId() ))
            error( "(1) getObjectAdapterId returns bad value" ) ;
          
        if (!temp.getORBVersion().equals( ORBVersionFactory.getPEORB() ))
            error( "(1) getORBVersion returns bad value" ) ;

        // Check that object key is written correctly
        POAKeyGenerator generator = new POAKeyGenerator() ;
        byte[] fullKey = generator.makeKey( 
            ObjectKeyFactoryImpl.JAVAMAGIC_NEWER ) ;
        OctetSeqHolder osh = new OctetSeqHolder() ;
        CDRInputObject is = makeInputStream( fullKey ) ;
        int magic = is.read_long() ;
        scid = is.read_long() ;
        temp = new POAObjectKeyTemplate( orb, magic, scid, is, osh ) ;

        OutputStream os = newOutputStream() ;
        ObjectId objid = IORFactories.makeObjectId( osh.value ) ;
        temp.write( objid, os ) ;
        byte[] resultKey = getBytes( os ) ;

        if (!equal( fullKey, resultKey ))
            error( "Error in writing out object key" ) ;

        // test write template method
        os = newOutputStream() ;
        temp.write( os ) ;

        is = makeInputStream( os ) ;
        ObjectKeyTemplate newTemplate = orb.getObjectKeyFactory().createTemplate( 
            is ) ;
        if (!newTemplate.equals( temp ))
            error( "Error in writing out object key template" ) ;
    }

    @Test
    public void testTaggedProfileFactoryFinder() 
    {
        out.println( "Testing TaggedProfileFactoryFinder" ) ;

        IdentifiableFactoryFinder finder = new TaggedProfileFactoryFinderImpl(orb) ;
        finder.registerFactory( IIOPFactories.makeIIOPProfileFactory() ) ;
        // Create a couple of tagged profiles and write them to a 
        // IdentifiableContainerBase, read them back using finder,
        // and check results.

        // Create an IIOPProfile
        int scid = ORBConstants.FIRST_POA_SCID ;
        String orbid = "23" ;
        String[] ss = { "ABC", "DEF", "GHI", "JKL", "MNO" } ;
        ObjectAdapterId poaid = new ObjectAdapterIdArray( ss ) ;
        int serverid = -123 ;
        byte[] id = { 0x00, 0x00, 0x33, 0x44, 0x21, 0x23, 0x00 } ;
        ObjectId oid = IORFactories.makeObjectId( id ) ;

        POAObjectKeyTemplate temp = new POAObjectKeyTemplate( orb, scid, 
            serverid, orbid, poaid ) ;

        String host = "FOO" ;
        int port = 34567 ;
        IIOPAddress primary = IIOPFactories.makeIIOPAddress( host, port ) ;
        
        IIOPProfileTemplate ptemp = IIOPFactories.makeIIOPProfileTemplate( orb,
            GIOPVersion.V1_2, primary ) ;

        String URL = "htp://foo.sun.com:9999" ;

        JavaCodebaseComponent jcomp = 
            IIOPFactories.makeJavaCodebaseComponent( URL ) ;
        ptemp.add( jcomp ) ;

        ORBTypeComponent comp = IIOPFactories.makeORBTypeComponent( 
            0x34567ABF ) ;
        ptemp.add( comp ) ;

        IIOPProfile iprof = (IIOPProfile)(ptemp.create( temp, oid )) ;

        // Create a GenericTaggedProfile to use as a profile
        int gid = 234 ;
        byte[] data = { 2, 14, 56, 37, 31, 42, 1, 9, 116, 57 } ;
        GenericTaggedProfile gprof = new GenericTaggedProfile( orb, gid, data ) ;

        // Create an IdentifiableContainerBase containing two profiles.
        List cb = new LinkedList() ;
        cb.add( iprof ) ;
        cb.add( gprof ) ;
        
        OutputStream os = newOutputStream() ;

        EncapsulationUtility.writeIdentifiableSequence( cb, os ) ;

        InputStream is = makeInputStream( os ) ;

        List result = new LinkedList() ;

        EncapsulationUtility.readIdentifiableSequence( result, finder, is ) ;

        // Check that result consists of clones of iprof and then gprof
        Iterator iter = result.iterator() ;

        if (!iprof.equals( iter.next() ))
            error( "Expected clone of iprof" ) ;

        if (!gprof.equals( iter.next() ))
            error( "expected clone of gprof" ) ;

        if (iter.hasNext())
            error( "Too many elements in iterator" ) ;
    }

    @Test
    public void testWireObjectTemplate() 
    {
        out.println( "Testing WireObjectTemplate" ) ;

        byte[] okey = { 0x12, 0x42, 0x36, 0x72, 0x44 } ;
        CDRInputObject is = new EncapsInputStream( orb, okey, okey.length ) ;
        OctetSeqHolder osh = new OctetSeqHolder() ;

        WireObjectKeyTemplate temp = new WireObjectKeyTemplate(orb);
        osh.value = okey;

        if (temp.getServerId() != -1)
            error( "Bad server id" ) ;

        if (!equal( okey, osh.value )) 
            error( "Did not fetch correct id from object key" ) ;
        
        OutputStream os = newOutputStream() ;
        ObjectId objid = IORFactories.makeObjectId( okey ) ;
        temp.write( objid, os ) ;
        byte[] resultKey = getBytes( os ) ;

        if (!equal( okey, resultKey ))
            error( "Error in writing out object key" ) ;
    }

    @Test
    public void testTaggedComponents() 
    {
        out.println( "Testing TaggedComponents" ) ;

        testAlternateIIOPAddressComponent() ;
        testCodeSetsComponent() ;
        testJavaCodebaseComponent() ;
        testORBTypeComponent() ;
        // testPoliciesComponent() ;
    }

    @Test
    public void testAlternateIIOPAddressComponent() 
    {
        out.println( "\tAlternateIIOPAddressComponent" ) ;

        IIOPAddress addr = IIOPFactories.makeIIOPAddress( "FOO", 34 ) ;
        AlternateIIOPAddressComponent comp = 
            IIOPFactories.makeAlternateIIOPAddressComponent( addr ) ;
        if (!addr.equals( comp.getAddress() ))
            error( "AlternateIIOPAddressComponent returns bad address" ) ;

        if (comp.getId() != TAG_ALTERNATE_IIOP_ADDRESS.value)
            error( "AlternateIIOPAddressComponent returns bad ID" ) ;
    }

    @Test
    public void testCodeSetsComponent() 
    {
        out.println( "\tCodeSetsComponent" ) ;

        CodeSetsComponent csc = IIOPFactories.makeCodeSetsComponent( orb ) ;

        // TBD: check the returned code set component info, when there is
        // something reasonable to do here.

        if (csc.getId() != TAG_CODE_SETS.value)
            error( "CodeSetsComponet returns bad ID" ) ;
    }

    @Test
    public void testJavaCodebaseComponent() 
    {
        out.println( "\tJavaCodebaseComponent" ) ;

        String URL = "htp://foo.sun.com:9999" ;

        JavaCodebaseComponent comp = 
            IIOPFactories.makeJavaCodebaseComponent( URL ) ;

        if (!URL.equals( comp.getURLs() ))
            error( "JavaCodebaseComponent returns bad URL" ) ;

        if (comp.getId() != TAG_JAVA_CODEBASE.value)
            error( "JavaCodebaseComponent returns bad ID" ) ;
    }

    @Test
    public void testORBTypeComponent() 
    {
        out.println( "\tORBTypeComponent" ) ;

        int orbtype = 0x45464743 ;
        ORBTypeComponent comp = 
            IIOPFactories.makeORBTypeComponent( orbtype ) ;
        if (comp.getORBType() != orbtype)
            error( "ORBTypeComponent returns bad ORBType" ) ;

        if (comp.getId() != TAG_ORB_TYPE.value)
            error( "ORBTypeComponent returns bad ID" ) ;
    }

    public static final int TEST_COMP_ID = 0x2317 ;
    public static final byte[] TEST_COMP_DATA = { 12, 23, 31, 32, 53, 56, 1, 7, 9 } ;

    private void testObjectAdapterIdIterator( String[] data, int count, ObjectAdapterId poaid ) 
    {
        int numLevels = poaid.getNumLevels() ;
        if (numLevels != count)
            error( "getNumLevels() returned " + numLevels + " expected " +
                count ) ;

        Iterator iterator = poaid.iterator() ;
        int ctr = 0 ;

        while (iterator.hasNext()) {
            String current = (String)(iterator.next()) ;
            if (current.equals( data[ctr] ) )
                ctr++ ;
            else
                error( "iterator return bad element at index " + ctr +
                    ": expected " + data[ctr] + " got " + current ) ;
        }

        if (iterator.hasNext())
            error( "iterator had too many elements" ) ;

        if (ctr < count)
            error( "iterator had too few elements" ) ;
    }

    private String[] front( String[] data, int count )
    {
        String[] result = new String[ Math.min( data.length, count ) ] ;
        for (int ctr=0; ctr<result.length; ctr++ )
            result[ctr] = data[ctr] ;
        return result ;
    }

    private void testObjectAdapterIdArray( String[] data, int count ) 
    {
        System.out.println( "Testing ObjectAdapterIdArray count = " + count ) ;
        ObjectAdapterId poaid = new ObjectAdapterIdArray( front( data, count ) ) ;

        testObjectAdapterIdIterator( data, count, poaid ) ;
    }

    private void testObjectAdapterIdWrite( ObjectAdapterId poaid )
    {
        OutputStream os = newOutputStream() ;
        poaid.write( os ) ;
        
        InputStream is = makeInputStream( os ) ;
        int len = is.read_long() ;
        String[] data = new String[ len ] ;
        for (int ctr=0; ctr<len; ctr++)
            data[ctr] = is.read_string() ;

        ObjectAdapterId p2 = new ObjectAdapterIdArray( data ) ;

        if (!poaid.equals( p2 ))
            error( "Failure in ObjectAdapterId write test" ) ;
    }

    @Test
    public void testObjectAdapterId()
    {
        System.out.println( "Testing ObjectAdapterId" ) ;
        String[] data = { "first", "second", "third", "fourth", "fifth", 
            "sixth" } ;

        // create ObjectAdapterIdArray, check iterator with 0,1,4 elements
        testObjectAdapterIdArray( data, 0 ) ;
        testObjectAdapterIdArray( data, 1 ) ;
        testObjectAdapterIdArray( data, 4 ) ;

        ObjectAdapterId p1 = new ObjectAdapterIdArray( front( data, 5 ) ) ;

        // Check toString method
        String expected = "ObjectAdapterID[first/second/third/fourth/fifth]" ;
        String str1 = p1.toString() ;

        if (!expected.equals( str1 ))
            error( "ObjectAdapterIdArray.toString failed" ) ;

        // Check write: write out to stream, convert to input stream,
        // read back in as String[], construct new ObjectAdapterId and compare.
        testObjectAdapterIdWrite( p1 ) ;
    }

    @Test
    public void testObjectReferenceTemplateImpl()
    {
        System.out.println( "Testing ObjectReferenceTemplateImpl" ) ;
        
        // create IIOPProfileTemplate for testing
        int scid = ORBConstants.FIRST_POA_SCID ;
        String orbid = "ORB1" ;
        String[] ss = { "first", "second", "third" } ;
        ObjectAdapterId poaid = new ObjectAdapterIdArray( ss ) ;
        int serverid = -123 ;

        POAObjectKeyTemplate temp = new POAObjectKeyTemplate( orb, scid, 
            serverid, orbid, poaid ) ;

        String host = "FOO" ;
        int port = 34567 ;
        IIOPAddress primary = IIOPFactories.makeIIOPAddress( host, port ) ;
        
        IIOPProfileTemplate ptemp = IIOPFactories.makeIIOPProfileTemplate( orb,
            GIOPVersion.V1_2, primary ) ;
    
        IORTemplate iortemp = IORFactories.makeIORTemplate( temp ) ;
        iortemp.add( ptemp ) ;
        iortemp.makeImmutable() ;

        byte[] oid = { 0x23, 0x0, 0x0, 0x4, 0x43 } ;

        // construct ObjectReferenceTemplateImpl using IORTemplate
        ObjectReferenceTemplateImpl orti = 
            (ObjectReferenceTemplateImpl)IORFactories.makeObjectReferenceTemplate( 
                orb, iortemp ) ;

        // check server_id
        if (Integer.valueOf( orti.server_id() ).intValue() != serverid)
            error( "ObjectReferenceTemplate gave back wrong server_id" ) ;

        // check adapter_name
        if (!equal(orti.adapter_name(), ss ))   
            error( "ObjectReferenceTemplate gave back wrong adapter_name" ) ;

        // check orb_id
        if (!orti.orb_id().equals( orbid ))
            error( "ObjectReferenceTemplate gave back wrong orb_id" ) ;

        // check make_object
        org.omg.CORBA.Object oref = orti.make_object( "IDL:org/omg/CORBA/Object:1.0",
            oid ) ;
        com.sun.corba.ee.spi.ior.IOR ior = orb.getIOR( oref, false ) ;
        IIOPProfile prof2 = ior.getProfile() ;
        IIOPProfileTemplate ptemp2 = (IIOPProfileTemplate)prof2.getTaggedProfileTemplate() ;
        if (!ptemp.equals( ptemp2 ))
            error( "make_object constructed bad object reference" ) ;

        // write template out using _write
        OutputStream os = newOutputStream() ;
        orti._write( os ) ;

        // Construct ObjectReferenceTemplate using default constructor
        ObjectKeyTemplate oktemp = new JIDLObjectKeyTemplate( orb, 2, -231 ) ;
        IORTemplate iortemp2 = IORFactories.makeIORTemplate( oktemp ) ;
        ObjectReferenceTemplateImpl orti2 = 
            (ObjectReferenceTemplateImpl)IORFactories.makeObjectReferenceTemplate(
                orb, iortemp2 ) ;

        // Check that it is not equal to orti
        if (orti.equals( orti2 ))
            error( "equal failed (1)" ) ;

        if (orti2.equals( orti ))
            error( "equal failed (2)" ) ;
            
        // call _read on the template that was written out
        InputStream is = makeInputStream( os ) ;
        orti2._read( is ) ;

        // Check that it is now equal to orti 
        if (!orti2.equals( orti ))
            error( "did not read back equal ObjectReferenceTemplate" ) ;

        // check server_id
        if (Integer.valueOf( orti2.server_id() ).intValue() != serverid)
            error( "ObjectReferenceTemplate gave back wrong server_id" ) ;

        // check adapter_name
        if (!equal(orti2.adapter_name(), ss ))  
            error( "ObjectReferenceTemplate gave back wrong adapter_name" ) ;

        // check orb_id
        if (!orti2.orb_id().equals( orbid ))
            error( "ObjectReferenceTemplate gave back wrong orb_id" ) ;

        // check make_object
        oref = orti2.make_object( "IDL:org/omg/CORBA/Object:1.0",
            oid ) ;
        com.sun.corba.ee.spi.ior.IOR newior = orb.getIOR( oref, false ) ;
        prof2 = newior.getProfile() ;
        ptemp2 = (IIOPProfileTemplate)prof2.getTaggedProfileTemplate() ;
        if (!ptemp.equals( ptemp2 ))
            error( "make_object constructed bad object reference" ) ;
    }
}
