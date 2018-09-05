/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.servicecontext;

import org.omg.CORBA.SystemException;
import org.omg.CORBA_2_3.portable.InputStream ;
import org.omg.CORBA_2_3.portable.OutputStream ;
import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;
import com.sun.corba.ee.spi.orb.ORB ;
import com.sun.corba.ee.impl.encoding.EncapsOutputStream ;
import com.sun.corba.ee.impl.encoding.OutputStreamFactory;
import com.sun.corba.ee.spi.logging.ORBUtilSystemException;

/** Base class for all ServiceContext classes.
* There is a derived ServiceContext class for each service context that
* the ORB supports.  Each subclass encapsulates the representation of
* the service context and provides any needed methods for manipulating
* the service context.  Each subclass must provide the following 
* members:
* <p>
* <ul>
* </li>a public static final int SERVICE_CONTEXT_ID that gives the OMG
* (or other) defined id for the service context.  This is needed for the
* registration mechanism defined in ServiceContexts. OMG defined 
* service context ids are taken from section 13.6.7 of ptc/98-12-04.</li>
* <li>a public constructor that takes an InputStream as its argument.</li>
* <li>Appropriate definitions of getId() and writeData().  getId() must
* return SERVICE_CONTEXT_ID.</li>
* </ul>
* <p>
* The subclass can be constructed either directly from the service context
* representation, or by reading the representation from an input stream.
* These cases are needed when the service context is created and written to
* the request or reply, and when the service context is read from the
* received request or reply.
*/
public abstract class ServiceContextBase {
    private static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    /** Simple default constructor used when subclass is constructed 
     * from its representation.
     */

    private byte[] data;

    protected ServiceContextBase() { }

    /** Stream constructor used when subclass is constructed from an
     * InputStream.  This constructor must be called by super( stream )
     * in the subclass.  After this constructor completes, the service
     * context representation can be read from in.
     * Note that the service context id has been consumed from the input
     * stream before this object is constructed.
     */
    protected ServiceContextBase(InputStream s) 
    {
        in = s;
    }

    /** Returns Service context id.  Must be overloaded in subclass.
     */
    public abstract int getId() ;

    /** Write the service context to an output stream.  This method 
     * must be used for writing the service context to a request or reply
     * header.
     */
    public synchronized void write(OutputStream s, GIOPVersion gv) throws SystemException {
        if (data == null) {
            EncapsOutputStream os = OutputStreamFactory.newEncapsOutputStream((ORB)(s.orb()), gv);   
            try {
                os.putEndian();
                writeData(os);
                data = os.toByteArray();
            } finally {
                try {
                    os.close();
                } catch (java.io.IOException e) {
                    wrapper.ioexceptionDuringStreamClose(e);
                }
            }
        }
        s.write_long(getId());
        s.write_long(data.length);
        s.write_octet_array(data, 0, data.length);
    }

    /** Writes the data used to represent the subclasses service context
     * into an encapsulation stream.  Must be overloaded in subclass.
     */
    protected abstract void writeData( OutputStream os ) ;

    /** in is the stream containing the service context representation.
     * It is constructed by the stream constructor, and available for use
     * in the subclass stream constructor.
     */
    protected InputStream in = null ;

    @Override
    public String toString() 
    {
        return "ServiceContext[ id=" + getId() + " ]" ;
    } 
}
