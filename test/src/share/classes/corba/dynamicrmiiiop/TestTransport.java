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
