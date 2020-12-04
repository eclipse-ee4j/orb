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

package com.sun.corba.ee.impl.protocol.giopmsgheaders;

import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.SystemException;
import org.omg.CORBA_2_3.portable.InputStream;

import com.sun.corba.ee.spi.ior.IORFactories;

import com.sun.corba.ee.spi.orb.ORB;

import com.sun.corba.ee.spi.servicecontext.ServiceContexts;
import com.sun.corba.ee.spi.servicecontext.ServiceContextDefaults;
import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;
import com.sun.corba.ee.impl.misc.ORBUtility;
import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.impl.encoding.CDRInputObject;

import com.sun.corba.ee.spi.logging.ORBUtilSystemException ;

/**
 * This implements the GIOP 1.0 Reply header.
 *
 * @author Ram Jeyaraman 05/14/2000
 * @version 1.0
 */

public final class ReplyMessage_1_0 extends Message_1_0
        implements ReplyMessage {

    private static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    // Instance variables

    private ORB orb = null;
    private ServiceContexts service_contexts = null;
    private int request_id = (int) 0;
    private int reply_status = (int) 0;
    private IOR ior = null;
    private String exClassName = null;
    private int minorCode = (int) 0;
    private CompletionStatus completionStatus = null;

    // Constructors

    ReplyMessage_1_0(ORB orb) {
        this.service_contexts = ServiceContextDefaults.makeServiceContexts( orb ) ;
        this.orb = orb;
    }

    ReplyMessage_1_0(ORB orb, ServiceContexts _service_contexts,
            int _request_id, int _reply_status, IOR _ior) {
        super(Message.GIOPBigMagic, false, Message.GIOPReply, 0);
        this.orb = orb;
        service_contexts = _service_contexts;
        request_id = _request_id;
        reply_status = _reply_status;
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
        return KeyAddr.value;
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

    public void read(org.omg.CORBA.portable.InputStream istream) {
        super.read(istream);
        this.service_contexts = ServiceContextDefaults.makeServiceContexts(
            (org.omg.CORBA_2_3.portable.InputStream) istream);
        this.request_id = istream.read_ulong();
        this.reply_status = istream.read_long();
        isValidReplyStatus(this.reply_status); // raises exception on error

        // The code below reads the reply body in some cases
        // SYSTEM_EXCEPTION & LOCATION_FORWARD
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
                throw wrapper.badCompletionStatusInReply( status );
            }

        } else if (this.reply_status == USER_EXCEPTION) {
            // do nothing. The client stub will read the exception from body.
        } else if (this.reply_status == LOCATION_FORWARD) {
            CDRInputObject cdr = (CDRInputObject) istream;
            this.ior = IORFactories.makeIOR( orb, (InputStream)cdr ) ;
        }
    }

    // Note, this writes only the header information. SystemException or
    // IOR may be written afterwards into the reply mesg body.
    public void write(org.omg.CORBA.portable.OutputStream ostream) {
        super.write(ostream);
        service_contexts.write(
            (org.omg.CORBA_2_3.portable.OutputStream) ostream,
            GIOPVersion.V1_0);
        ostream.write_ulong(this.request_id);
        ostream.write_long(this.reply_status);
    }

    // Static methods

    public static void isValidReplyStatus(int replyStatus) {
        switch (replyStatus) {
        case NO_EXCEPTION :
        case USER_EXCEPTION :
        case SYSTEM_EXCEPTION :
        case LOCATION_FORWARD :
            break;
        default :
            throw wrapper.illegalReplyStatus();
        }
    }

    public void callback(MessageHandler handler)
        throws java.io.IOException
    {
        handler.handleInput(this);
    }
} // 
