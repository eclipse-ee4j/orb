/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.protocol.giopmsgheaders;

import org.omg.CORBA.SystemException;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA_2_3.portable.InputStream;

import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.ior.IORFactories ;

import com.sun.corba.ee.spi.orb.ORB;

import com.sun.corba.ee.spi.servicecontext.ServiceContexts;
import com.sun.corba.ee.spi.servicecontext.ServiceContextDefaults;
import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;

import com.sun.corba.ee.impl.encoding.CDRInputObject;
import com.sun.corba.ee.impl.encoding.CDROutputObject;

import com.sun.corba.ee.impl.misc.ORBUtility;

import com.sun.corba.ee.spi.logging.ORBUtilSystemException ;

import com.sun.corba.ee.spi.trace.Transport ;

/**
 * This implements the GIOP 1.2 Reply header.
 *
 * @author Ram Jeyaraman 05/14/2000
 * @version 1.0
 */

@Transport
public final class ReplyMessage_1_2 extends Message_1_2
        implements ReplyMessage {

    private static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    // Instance variables

    private ORB orb = null;
    private int reply_status = 0;
    private ServiceContexts service_contexts = null ;
    private IOR ior = null;
    private String exClassName = null;
    private int minorCode = 0;
    private CompletionStatus completionStatus = null;
    private short addrDisposition = KeyAddr.value; // default;
    
    // Constructors

    ReplyMessage_1_2(ORB orb) {
        this.service_contexts = ServiceContextDefaults.makeServiceContexts( orb ) ;
        this.orb = orb;
    }

    ReplyMessage_1_2(ORB orb, int _request_id, int _reply_status,
            ServiceContexts _service_contexts, IOR _ior) {
        super(Message.GIOPBigMagic, GIOPVersion.V1_2, FLAG_NO_FRAG_BIG_ENDIAN,
            Message.GIOPReply, 0);
        this.orb = orb;
        request_id = _request_id;
        reply_status = _reply_status;
        service_contexts = _service_contexts;
        if (service_contexts == null) {
            service_contexts =
                ServiceContextDefaults.makeServiceContexts(orb);
        }
        ior = _ior;
    }

    // Accessor methods

    public int getRequestId() {
        return this.request_id;
    }

    public int getReplyStatus() {
        return this.reply_status;
    }

    public short getAddrDisposition() {
        return this.addrDisposition;
    }
    
    public ServiceContexts getServiceContexts() {
        return this.service_contexts;
    }

    public SystemException getSystemException(String message) {
        return MessageBase.getSystemException(
            exClassName, minorCode, completionStatus, message, wrapper);
    }

    public IOR getIOR() {
        return this.ior;
    }

    public void setIOR( IOR ior ) {
        this.ior = ior;
    }

    // IO methods
    @Transport
    public void read(org.omg.CORBA.portable.InputStream istream) {
        super.read(istream);
        this.request_id = istream.read_ulong();
        this.reply_status = istream.read_long();
        isValidReplyStatus(this.reply_status); // raises exception on error
        this.service_contexts = ServiceContextDefaults.makeServiceContexts(
            (org.omg.CORBA_2_3.portable.InputStream)istream ) ;

        // CORBA formal 00-11-0 15.4.2.2 GIOP 1.2 body must be
        // aligned on an 8 octet boundary.
        // Ensures that the first read operation called from the stub code,
        // during body deconstruction, would skip the header padding, that was
        // inserted to ensure that the body was aligned on an 8-octet boundary.
        ((CDRInputObject)istream).setHeaderPadding(true);

        // The code below reads the reply body in some cases
        // SYSTEM_EXCEPTION & LOCATION_FORWARD & LOCATION_FORWARD_PERM &
        // NEEDS_ADDRESSING_MODE
        if (this.reply_status == SYSTEM_EXCEPTION) {

            String reposId = istream.read_string();
            this.exClassName = ORBUtility.classNameOf(reposId);
            this.minorCode = istream.read_long();
            int status = istream.read_long();

            switch (status) {
            case CompletionStatus._COMPLETED_YES:
                this.completionStatus = CompletionStatus.COMPLETED_YES;
                break;
            case CompletionStatus._COMPLETED_NO:
                this.completionStatus = CompletionStatus.COMPLETED_NO;
                break;
            case CompletionStatus._COMPLETED_MAYBE:
                this.completionStatus = CompletionStatus.COMPLETED_MAYBE;
                break;
            default:
                throw wrapper.badCompletionStatusInReply( status ) ;
            }

        } else if (this.reply_status == USER_EXCEPTION) {
            // do nothing. The client stub will read the exception from body.
        } else if ( (this.reply_status == LOCATION_FORWARD) ||
                (this.reply_status == LOCATION_FORWARD_PERM) ){
            CDRInputObject cdr = (CDRInputObject) istream;
            this.ior = IORFactories.makeIOR( orb, (InputStream)cdr ) ;
        }  else if (this.reply_status == NEEDS_ADDRESSING_MODE) {
            // read GIOP::AddressingDisposition from body and resend the
            // original request using the requested addressing mode. The
            // resending is transparent to the client program.
            this.addrDisposition = AddressingDispositionHelper.read(istream);            
        }
    }

    // Note, this writes only the header information. SystemException or
    // IOR or GIOP::AddressingDisposition may be written afterwards into the
    // reply mesg body.
    @Transport
    public void write(org.omg.CORBA.portable.OutputStream ostream) {
        super.write(ostream);
        ostream.write_ulong(this.request_id);
        ostream.write_long(this.reply_status);
        service_contexts.write(
            (org.omg.CORBA_2_3.portable.OutputStream) ostream,
            GIOPVersion.V1_2);

        // CORBA formal 00-11-0 15.4.2.2 GIOP 1.2 body must be
        // aligned on an 8 octet boundary.
        // Ensures that the first write operation called from the stub code,
        // during body construction, would insert a header padding, such that
        // the body is aligned on an 8-octet boundary.
        ((CDROutputObject)ostream).setHeaderPadding(true);
    }

    // Static methods
    public static void isValidReplyStatus(int replyStatus) {
        switch (replyStatus) {
        case NO_EXCEPTION :
        case USER_EXCEPTION :
        case SYSTEM_EXCEPTION :
        case LOCATION_FORWARD :
        case LOCATION_FORWARD_PERM :
        case NEEDS_ADDRESSING_MODE :
            break;
        default :
            throw wrapper.illegalReplyStatus() ;
        }
    }

    public void callback(MessageHandler handler)
        throws java.io.IOException
    {
        handler.handleInput(this);
    }

    @Override
    public boolean supportsFragments() {
        return true;
    }
} // class ReplyMessage_1_2
