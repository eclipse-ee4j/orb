/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

/**
 * ClassWithZeroLengthStrings contains 5 fields of which 4 fields are zero
 * length Strings. field2 and field3 are same instance (so there is an 
 * indirection while reading field4) and field4 and field5 are same instance(
 * so there is an indirection while reading field5). The main idea behind this
 * object is to check whether the aliasing is maintaned when we do a 
 * Util.copyObject( ) by checking for validateObject( ).
 */
package corba.serialization.zerolengthstring;

import java.io.*;

public class ClassWithZeroLengthStrings implements Serializable {
    private int field1;
    private transient String field2;
    private transient String field3;
    private transient String field4;
    private transient String field5;

    public ClassWithZeroLengthStrings( ) {
        field1 = 1;
        field2 = new String("");
        field3 = field2;
        field4 = new String("");
        field5 = field4;
   }

    /**
     * We do write out all the transient String fields using the writeObject.
     */
    private void writeObject(ObjectOutputStream out) throws IOException
    {
        out.defaultWriteObject();
        out.writeObject( field2 );
        out.writeObject( field3 );
        out.writeObject( field4 );
        out.writeObject( field5 );
    }


    /**
     * We do read all the transient String fields using the readObject.
     */
    private void readObject(ObjectInputStream in) 
        throws IOException, ClassNotFoundException
    {
       in.defaultReadObject();
       field2 = (String) in.readObject( );
       field3 = (String) in.readObject( );
       field4 = (String) in.readObject( );
       field5 = (String) in.readObject( );
    }


    /**
     * Important method to check whether the structure of the object is 
     * maintained correctly after Util.copyObject( )
     */
    public boolean validateObject( ) {
       if( field1 != 1 ) {
           System.err.println( "field1 != 1" );
           return false;
       }

       // Structurally fields 2,3,4 and 5 are all Zero Length Strings
       String zeroLengthString = new String("");
       if( !field2.equals( zeroLengthString ) 
        || !field3.equals( zeroLengthString )
        || !field4.equals( zeroLengthString )
        || !field5.equals( zeroLengthString ) )
       {
           System.err.println( "if( !field2.equals( zeroLengthString)" +
               "|| !field3.equals( zeroLengthString )" +
               "|| !field4.equals( zeroLengthString ) "+
               "|| !field5.equals( zeroLengthString ) returned true" );
           return false;
       }

       // We want to make sure field2 and field3 are same instance as well
       // as field4 and field5
       if( field2 != field3 ) {
           System.err.println( "field2 != field3 returned true " );
           return false;
       }
       if( field4 != field5 ) {
           System.err.println( "field4 != field5 returned true " );
           return false;
       }
       if( field3 == field4 ) {
           System.err.println( "field3 == field4 returned true " );
           return false;
       }
       return true;
    }
}


