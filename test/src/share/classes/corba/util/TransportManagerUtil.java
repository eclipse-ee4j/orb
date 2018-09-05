/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.util;

import com.sun.corba.ee.impl.encoding.CDRInputObject;
import com.sun.corba.ee.impl.protocol.MessageParserImpl;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.FragmentMessage;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.Message;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.Message_1_2;
import com.sun.corba.ee.impl.transport.ConnectionImpl;
import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.transport.MessageData;

import java.nio.ByteBuffer;

public class TransportManagerUtil {

    public static MessageData getMessageData(byte[][] data, ORB orb) {
        ConnectionImpl connection = new ConnectionImpl(orb) ;

        final Message[] messages = new Message[data.length] ;
        Message firstMessage = null ;
        CDRInputObject inobj = null ;

        for (int ctr=0; ctr<data.length; ctr++) {
            MessageParserImpl parser = new MessageParserImpl(orb, null);
            parser.offerBuffer(ByteBuffer.wrap(data[ctr]));
            Message message = parser.getMessageMediator().getDispatchHeader();
            ByteBuffer msgByteBuffer = parser.getMsgByteBuffer();
            if (message.getGIOPVersion().equals( GIOPVersion.V1_2 )) {
                ((Message_1_2) message).unmarshalRequestID(msgByteBuffer) ;
            }

            messages[ctr] = message;

            // Check that moreFragments == (ctr < messages.length)?

            if (inobj == null) {
                firstMessage = message;
                inobj = new CDRInputObject(orb, connection, msgByteBuffer, message) ;
                inobj.performORBVersionSpecificInit() ;
            } else {
                inobj.addFragment( (FragmentMessage) message, msgByteBuffer );
            }
        }

        // Unmarshal all the data in the first message.  This may
        // cause other fragments to be read.
        firstMessage.read( inobj ) ;

        final CDRInputObject resultObj = inobj ;

        return new MessageData() {
           public Message[] getMessages() { return messages ; }
           public CDRInputObject getStream() { return resultObj ; }
        } ;
    }

    /** Analyze the header of a message.  This provides enough information to
     * classify the message and group related messages together for use in
     * the getMessageData method.  Also, if data is a GIOP 1.2 message,
     * the result of this call will contain a valid request ID.
     */
    public static Message getMessage(byte[] data, ORB orb) {
        MessageParserImpl parser = new MessageParserImpl(orb, null);
        parser.offerBuffer(ByteBuffer.wrap(data));
        Message msg = parser.getMessageMediator().getDispatchHeader();
        if (msg.getGIOPVersion().equals( GIOPVersion.V1_2 ))
            ((Message_1_2)msg).unmarshalRequestID( parser.getMsgByteBuffer() ) ;

        return msg ;
    }
}
