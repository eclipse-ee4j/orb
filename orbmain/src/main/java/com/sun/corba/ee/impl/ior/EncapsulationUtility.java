/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.ior;

import java.util.List;

import org.omg.CORBA_2_3.portable.OutputStream ;
import org.omg.CORBA_2_3.portable.InputStream ;

import com.sun.corba.ee.spi.ior.Identifiable ;
import com.sun.corba.ee.spi.ior.IdentifiableFactoryFinder ;
import com.sun.corba.ee.spi.ior.WriteContents ;

import com.sun.corba.ee.spi.orb.ORB ;

import com.sun.corba.ee.impl.encoding.CDROutputObject ;
import com.sun.corba.ee.impl.encoding.EncapsOutputStream ;
import com.sun.corba.ee.impl.encoding.OutputStreamFactory;
import com.sun.corba.ee.impl.encoding.EncapsInputStream ;
import com.sun.corba.ee.impl.encoding.EncapsInputStreamFactory;

/**
 * This static utility class contains various utility methods for reading and
 * writing CDR encapsulations.
 *
 * @author Ken Cavanaugh
 */
public final class EncapsulationUtility 
{
    private EncapsulationUtility()
    {
    }

    /** Read the count from is, then read count Identifiables from
     * is using the factory.  Add each constructed Identifiable to container.
     */
    public static <E extends Identifiable> void readIdentifiableSequence( 
        List<E> container,
        IdentifiableFactoryFinder<E> finder, InputStream istr) 
    {
        int count = istr.read_long() ;
        for (int ctr = 0; ctr<count; ctr++) {
            int id = istr.read_long() ;
            E obj = finder.create( id, istr ) ;
            container.add( obj ) ;
        }
    }

    /** Write all Identifiables that we contain to os.  The total
     * length must be written before this method is called.
     */
    public static <E extends Identifiable> void writeIdentifiableSequence( 
        List<E> container, OutputStream os) 
    {
        os.write_long( container.size() ) ;
        for (Identifiable obj : container) {
            os.write_long( obj.getId() ) ;
            obj.write( os ) ;
        }
    }

    /** Helper method that is used to extract data from an output
    * stream and write the data to another output stream.  Defined
    * as static so that it can be used in another class.
    */
    public static void writeOutputStream( OutputStream dataStream,
        OutputStream os ) 
    {
        byte[] data = ((CDROutputObject)dataStream).toByteArray() ;
        os.write_long( data.length ) ;
        os.write_octet_array( data, 0, data.length ) ;
    }

    /** Helper method to read the octet array from is, deencapsulate it, 
    * and return
    * as another InputStream.  This must be called inside the
    * constructor of a derived class to obtain the correct stream
    * for unmarshalling data.
    */
    public static InputStream getEncapsulationStream( ORB orb, InputStream is )
    {
        byte[] data = readOctets( is ) ;
        EncapsInputStream result = EncapsInputStreamFactory.newEncapsInputStream( orb, data, 
            data.length ) ;
        result.consumeEndian() ;
        return result ;
    } 

    /** Helper method that reads an octet array from an input stream.
    * Defined as static here so that it can be used in another class.
    */
    public static byte[] readOctets( InputStream is ) 
    {
        int len = is.read_ulong() ;
        byte[] data = new byte[len] ;
        is.read_octet_array( data, 0, len ) ;
        return data ;
    }

    public static void writeEncapsulation( WriteContents obj,
        OutputStream os )
    {
        EncapsOutputStream out = OutputStreamFactory.newEncapsOutputStream( (ORB)os.orb() ) ;

        out.putEndian() ;

        obj.writeContents( out ) ;

        writeOutputStream( out, os ) ;
    }
}
