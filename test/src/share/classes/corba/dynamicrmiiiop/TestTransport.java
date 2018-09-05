/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.dynamicrmiiiop ; 

import org.omg.CORBA.portable.ApplicationException ;
import org.omg.CORBA_2_3.portable.InputStream ;
import org.omg.CORBA_2_3.portable.OutputStream ;

import com.sun.corba.ee.impl.encoding.CDROutputObject ;
import com.sun.corba.ee.impl.encoding.EncapsInputStream ;
import com.sun.corba.ee.impl.encoding.EncapsOutputStream ;
import com.sun.corba.ee.impl.encoding.CDRInputObject ;


import com.sun.corba.ee.spi.orb.ORB ;

public class TestTransport {
    private ORB orb ;

    public TestTransport( ORB orb ) 
    {
        this.orb = orb ;
    }

    private static final int REQUEST_HEADER = 24 ;
    private static final int NORMAL_REPLY_HEADER = 30 ;
    private static final int EXCEPTION_REPLY_HEADER = 36 ;

    public InputStream getInputStream( OutputStream os ) 
    {
        CDROutputObject cos = (CDROutputObject)os ;
        byte[] data = cos.toByteArray() ;
        return new EncapsInputStream( orb, data, data.length ) ;
    }

    public OutputStream makeRequest( String mname )
    {
        OutputStream result = new EncapsOutputStream( orb ) ;
        result.write_long( REQUEST_HEADER ) ;
        result.write_string( mname ) ;
        return result ;
    }

    public OutputStream makeNormalReply() 
    {
        OutputStream result = new EncapsOutputStream( orb ) ;
        result.write_long( NORMAL_REPLY_HEADER ) ;
        return result ;
    }

    public OutputStream makeExceptionReply()
    {
        OutputStream result = new EncapsOutputStream( orb ) ;
        result.write_long( EXCEPTION_REPLY_HEADER ) ;
        return result ;
    }
    
    public String readRequestHeader( InputStream is )
    {
        int header = is.read_long() ;
        if (header != REQUEST_HEADER)
            throw new RuntimeException( 
                "InputStream does not begin with REQUEST_HEADER" ) ;
        return is.read_string() ;
    }

    // Throw ApplicationException.  Note that this
    // must leave the stream ready to read the repo id
    // string, so we need to use mark/reset here.
    // This code is taken from CorbaClientRequestDispatcher.
    private String peekUserExceptionId(CDRInputObject inputObject)
    {
        // REVISIT - need interface for mark/reset
        inputObject.mark(Integer.MAX_VALUE);
        String result = inputObject.read_string();
        inputObject.reset();
        return result;
    }                     

    public void readReplyHeader( InputStream is ) 
        throws ApplicationException
    {
        int header = is.read_long() ;
        if (header == NORMAL_REPLY_HEADER) {
            // NO-OP
        } else if (header == EXCEPTION_REPLY_HEADER) {
            String id = peekUserExceptionId( (CDRInputObject)is ) ;
            throw new ApplicationException( id, is ) ;
        } else {
            // error
            throw new RuntimeException( "Bad reply header in test" ) ;
        }
    }
}
