/*
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.corba.ee.impl.protocol;

import com.sun.corba.ee.impl.protocol.giopmsgheaders.FragmentMessage_1_2;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.LocateRequestMessage_1_2;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.Message;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.ReplyMessage_1_1;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.ReplyMessage_1_2;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.RequestMessage_1_0;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.RequestMessage_1_2;
import com.sun.corba.ee.impl.transport.MessageTraceManagerImpl;
import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.orb.ORBData;
import com.sun.corba.ee.spi.protocol.MessageMediator;
import com.sun.corba.ee.spi.protocol.MessageParser;
import com.sun.corba.ee.spi.transport.Connection;
import com.sun.corba.ee.spi.transport.MessageTraceManager;
import com.sun.corba.ee.spi.transport.TransportManager;
import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.COMM_FAILURE;

import java.nio.ByteBuffer;

import static com.meterware.simplestub.Stub.createStrictStub;
import static org.junit.Assert.*;

public class MessageParserTest {

    private static final int BUFFER_SIZE = 1357;  // pick a bizarre number that should not occur randomly

    private ORBDataFake orbData = createStrictStub(ORBDataFake.class);
    private ORBFake orb = createStrictStub(ORBFake.class);
    private ConnectionFake connection = createStrictStub(ConnectionFake.class);
    private TransportManagerFake transportManager = createStrictStub(TransportManagerFake.class);

    private MessageParser parser;

    @Before
    public void setUp() throws Exception {
        orb.orbData = orbData;
        orb.transportManager = transportManager;
        parser = new MessageParserImpl(orb, connection);
    }

    @Test
    public void oldwhenBufferDoesNotContainEntireHeader_requestMore() {
        byte[] header = {'G', 'I', 'O', 'P', 1, 0};
        ByteBuffer buffer = ByteBuffer.wrap(header);

        Message message = parser.parseBytes(buffer, connection);

        assertFalse(parser.hasMoreBytesToParse());
        assertTrue(parser.isExpectingMoreData());
        assertEquals(BUFFER_SIZE, parser.getSizeNeeded());
        assertEquals(0, parser.getNextMessageStartPosition());
        assertNull(message);
        assertSame(buffer, parser.getRemainderBuffer());
    }

    @Test
    public void whenBufferDoesNotContainEntireHeader_requestMoreAndDoNotCreateMediator() {
        byte[] header = {'G', 'I', 'O', 'P', 1, 0};
        ByteBuffer buffer = ByteBuffer.wrap(header);

        parser.offerBuffer(buffer);

        assertSame(buffer, parser.getRemainderBuffer());
        assertNull(parser.getMessageMediator());
        assertEquals(0, buffer.position());
    }

    @Test
    public void oldwhenBufferContainsHeaderOnly_requestMore() {
        byte[] header = {'G', 'I', 'O', 'P', 1, 1, Message.FLAG_NO_FRAG_BIG_ENDIAN, Message.GIOPCancelRequest, 0, 0, 0, 6, 1, 2};
        ByteBuffer buffer = ByteBuffer.wrap(header);

        Message message = parser.parseBytes(buffer, connection);

        assertFalse(parser.hasMoreBytesToParse());
        assertTrue(parser.isExpectingMoreData());
        assertEquals(18, parser.getSizeNeeded());
        assertEquals(0, parser.getNextMessageStartPosition());
        assertNull(message);
        assertSame(buffer, parser.getRemainderBuffer());
    }

    @Test
    public void whenBufferContainsHeaderOnly_requestMoreAndDoNotCreateMediator() {
        byte[] header = {'G', 'I', 'O', 'P', 1, 1, Message.FLAG_NO_FRAG_BIG_ENDIAN, Message.GIOPCancelRequest, 0, 0, 0, 6, 1, 2};
        ByteBuffer buffer = ByteBuffer.wrap(header);

        parser.offerBuffer(buffer);

        assertSame(buffer, parser.getRemainderBuffer());
        assertNull(parser.getMessageMediator());
        assertEquals(0, buffer.position());
    }

    @Test
    public void whenBufferIsLittleEndianAndContainsHeaderOnly_requestMoreAndDoNotCreateMediator() {
        byte[] header = {'G', 'I', 'O', 'P', 1, 1, Message.LITTLE_ENDIAN_BIT, Message.GIOPCancelRequest, 6, 0, 0, 0, 1, 2};
        ByteBuffer buffer = ByteBuffer.wrap(header);

        parser.offerBuffer(buffer);

        assertSame(buffer, parser.getRemainderBuffer());
        assertNull(parser.getMessageMediator());
        assertEquals(0, buffer.position());
    }

    @Test
    public void old_whenBufferContainsWholeMessage_consumeEntireBuffer() {
        byte[] header = {'G', 'I', 'O', 'P', 1, 2, Message.FLAG_NO_FRAG_BIG_ENDIAN, Message.GIOPReply, 0, 0, 0, 6, 1, 2, 3, 4, 5, 6};
        ByteBuffer buffer = ByteBuffer.wrap(header);

        Message message = parser.parseBytes(buffer, connection);

        assertFalse(parser.hasMoreBytesToParse());
        assertFalse(parser.isExpectingMoreData());
        assertEquals(true, message instanceof ReplyMessage_1_2);
        assertEquals(header.length, parser.getMsgByteBuffer().limit());
        assertEquals(0, parser.getRemainderBuffer().remaining());
    }

    @Test
    public void whenBufferContainsWholeMessage_consumeEntireBuffer() {
        byte[] header = {'G', 'I', 'O', 'P', 1, 2, Message.FLAG_NO_FRAG_BIG_ENDIAN, Message.GIOPReply, 0, 0, 0, 6, 1, 2, 3, 4, 5, 6};
        ByteBuffer buffer = ByteBuffer.wrap(header);

        parser.offerBuffer(buffer);
        assertNull(parser.getRemainderBuffer());
        MessageMediator mediator = parser.getMessageMediator();
        assertNotNull(mediator);
        assertTrue(mediator.getDispatchHeader() instanceof ReplyMessage_1_2);
    }

    @Test
    public void afterConsumingEntireBuffer_offerOfNullBufferClearsMessageMediator() {
        byte[] header = {'G', 'I', 'O', 'P', 1, 2, Message.FLAG_NO_FRAG_BIG_ENDIAN, Message.GIOPReply, 0, 0, 0, 6, 1, 2, 3, 4, 5, 6};
        ByteBuffer buffer = ByteBuffer.wrap(header);

        parser.offerBuffer(buffer);
        parser.offerBuffer(null);
        assertNull(parser.getMessageMediator());
    }

    @Test
    public void whenIsLittleEndianAndBufferContainsWholeMessage_consumeEntireBuffer() {
        byte[] header = {'G', 'I', 'O', 'P', 1, 2, Message.LITTLE_ENDIAN_BIT, Message.GIOPReply, 6, 0, 0, 0, 1, 2, 3, 4, 5, 6};
        ByteBuffer buffer = ByteBuffer.wrap(header);

        parser.offerBuffer(buffer);
        assertNull(parser.getRemainderBuffer());
        MessageMediator mediator = parser.getMessageMediator();
        assertNotNull(mediator);
        assertTrue(mediator.getDispatchHeader() instanceof ReplyMessage_1_2);
    }

    @Test
    public void oldwhenBufferContainsRestOfMessage_consumeEntireBuffer() {
        byte[] partMessage = {'G', 'I', 'O', 'P', 1, 0, Message.FLAG_NO_FRAG_BIG_ENDIAN, Message.GIOPRequest, 0, 0, 0, 6, 1};
        byte[] wholeMessage = {'G', 'I', 'O', 'P', 1, 0, Message.FLAG_NO_FRAG_BIG_ENDIAN, Message.GIOPRequest, 0, 0, 0, 6, 1, 2, 3, 4, 5, 6};
        ByteBuffer buffer = ByteBuffer.wrap(partMessage);
        parser.parseBytes(buffer, connection);

        buffer = ByteBuffer.wrap(wholeMessage);
        Message message = parser.parseBytes(buffer, connection);

        assertFalse(parser.hasMoreBytesToParse());
        assertFalse(parser.isExpectingMoreData());
        assertEquals(true, message instanceof RequestMessage_1_0);
        assertEquals(wholeMessage.length, parser.getMsgByteBuffer().limit());
        assertEquals(0, parser.getRemainderBuffer().remaining());
    }

    @Test
    public void whenBufferContainsRestOfMessage_consumeEntireBuffer() {
        byte[] partMessage = {'G', 'I', 'O', 'P', 1, 0, Message.FLAG_NO_FRAG_BIG_ENDIAN, Message.GIOPRequest, 0, 0, 0, 6, 1};
        byte[] wholeMessage = {'G', 'I', 'O', 'P', 1, 0, Message.FLAG_NO_FRAG_BIG_ENDIAN, Message.GIOPRequest, 0, 0, 0, 6, 1, 2, 3, 4, 5, 6};
        parser.offerBuffer(ByteBuffer.wrap(partMessage));

        parser.offerBuffer(ByteBuffer.wrap(wholeMessage));
        assertNull(parser.getRemainderBuffer());
        MessageMediator mediator = parser.getMessageMediator();
        assertNotNull(mediator);
        assertTrue(mediator.getDispatchHeader() instanceof RequestMessage_1_0);
    }

    @Test
    public void oldwhenBufferContainsWholeMessagePlusMore_consumeMessageAndLeaveMore() {
        byte[] header = {'G', 'I', 'O', 'P', 1, 1, Message.FLAG_NO_FRAG_BIG_ENDIAN, Message.GIOPReply, 0, 0, 0, 6, 1, 2, 3, 4, 5, 6, 'G'};
        ByteBuffer buffer = ByteBuffer.wrap(header);

        Message message = parser.parseBytes(buffer, connection);

        assertTrue(parser.hasMoreBytesToParse());
        assertFalse(parser.isExpectingMoreData());
        assertTrue(message instanceof ReplyMessage_1_1);
        assertEquals(18, parser.getMsgByteBuffer().limit());
        assertEquals(1, parser.getRemainderBuffer().remaining());
    }

    @Test
    public void whenBufferContainsWholeMessageAndMore_consumeMessageBytesAndLeaveRemainder() {
        byte[] header = {'G', 'I', 'O', 'P', 1, 1, Message.FLAG_NO_FRAG_BIG_ENDIAN, Message.GIOPReply, 0, 0, 0, 6,
                1, 2, 3, 4, 5, 6,
                'R', 'M', 'I'};
        ByteBuffer buffer = ByteBuffer.wrap(header);

        parser.offerBuffer(buffer);
        assertNotNull(parser.getRemainderBuffer());
        assertEquals(3, parser.getRemainderBuffer().remaining());
        assertEquals('R', parser.getRemainderBuffer().get(0));
        MessageMediator mediator = parser.getMessageMediator();
        assertNotNull(mediator);
        assertTrue(mediator.getDispatchHeader() instanceof ReplyMessage_1_1);
    }

    @Test
    public void afterConsumingMessage_offerOfPartialBufferClearsMessageMediator() {
        byte[] header = {'G', 'I', 'O', 'P', 1, 1, Message.FLAG_NO_FRAG_BIG_ENDIAN, Message.GIOPReply, 0, 0, 0, 6,
                1, 2, 3, 4, 5, 6,
                'R', 'M', 'I'};
        ByteBuffer buffer = ByteBuffer.wrap(header);

        parser.offerBuffer(buffer);
        parser.offerBuffer(parser.getRemainderBuffer());
        assertNull(parser.getMessageMediator());
    }

    @Test
    public void oldwhenBufferContainsWholeMessageNeedingFragments_consumeEntireBufferAndExpectMore() {
        byte[] header = {'G', 'I', 'O', 'P', 1, 1, Message.MORE_FRAGMENTS_BIT, Message.GIOPReply, 0, 0, 0, 6, 1, 2, 3, 4, 5, 6};
        ByteBuffer buffer = ByteBuffer.wrap(header);

        Message message = parser.parseBytes(buffer, connection);

        assertFalse(parser.hasMoreBytesToParse());
        assertTrue(parser.isExpectingMoreData());
        assertTrue(message instanceof ReplyMessage_1_1);
        assertEquals(header.length, parser.getMsgByteBuffer().limit());
    }

    @Test
    public void whenBufferContainsWholeMessageNeedingFragments_consumeEntireBuffer() {
        byte[] header = {'G', 'I', 'O', 'P', 1, 1, Message.MORE_FRAGMENTS_BIT, Message.GIOPReply, 0, 0, 0, 6, 1, 2, 3, 4, 5, 6};
        ByteBuffer buffer = ByteBuffer.wrap(header);

        parser.offerBuffer(buffer);
        MessageMediator messageMediator = parser.getMessageMediator();

        assertNull(parser.getRemainderBuffer());
        assertTrue(messageMediator.getDispatchHeader() instanceof ReplyMessage_1_1);
        assertTrue(parser.isExpectingFragments());
    }

    @Test
    public void oldwhenBufferContainsFinalFragment_consumeBuffer() {
        byte[] header = {'G', 'I', 'O', 'P', 1, 2, Message.MORE_FRAGMENTS_BIT, Message.GIOPRequest, 0, 0, 0, 6, 1, 2, 3, 4, 5, 6,
                'G', 'I', 'O', 'P', 1, 2, Message.FLAG_NO_FRAG_BIG_ENDIAN, Message.GIOPFragment, 0, 0, 0, 4, 1, 2, 3, 4, 5, 6};
        ByteBuffer buffer = ByteBuffer.wrap(header);

        Message message1 = parser.parseBytes(buffer, connection);
        Message message2 = parser.parseBytes(buffer, connection);

        assertTrue(parser.hasMoreBytesToParse());
        assertFalse(parser.isExpectingMoreData());
        assertEquals(34, parser.getNextMessageStartPosition());
        assertTrue(message1 instanceof RequestMessage_1_2);
        assertTrue(message2 instanceof FragmentMessage_1_2);
    }

    @Test
    public void whenBufferContainsFinalFragment_consumeBuffer() {
        byte[] header = {'G', 'I', 'O', 'P', 1, 2, Message.MORE_FRAGMENTS_BIT, Message.GIOPRequest, 0, 0, 0, 6, 1, 2, 3, 4, 5, 6,
                'G', 'I', 'O', 'P', 1, 2, Message.FLAG_NO_FRAG_BIG_ENDIAN, Message.GIOPFragment, 0, 0, 0, 4, 1, 2, 3, 4};
        ByteBuffer buffer = ByteBuffer.wrap(header);
        parser.offerBuffer(buffer);
        MessageMediator messageMediator1 = parser.getMessageMediator();
        parser.offerBuffer(parser.getRemainderBuffer());
        MessageMediator messageMediator2 = parser.getMessageMediator();

        assertNull(parser.getRemainderBuffer());
        assertTrue(messageMediator1.getDispatchHeader() instanceof RequestMessage_1_2);
        assertTrue(messageMediator2.getDispatchHeader() instanceof FragmentMessage_1_2);
    }

    @Test
    public void oldwhenStartPositionNonZero_startReadingFromPosition() {
        byte[] header = {0, 0, 'G', 'I', 'O', 'P', 1, 2, Message.FLAG_NO_FRAG_BIG_ENDIAN, Message.GIOPLocateRequest, 0, 0, 0, 6, 1, 2, 3, 4, 5, 6, 'G'};
        ByteBuffer buffer = ByteBuffer.wrap(header);
        buffer.position(2);
        parser.setNextMessageStartPosition(2);

        Message message = parser.parseBytes(buffer, connection);

        assertTrue(parser.hasMoreBytesToParse());
        assertEquals(20, parser.getNextMessageStartPosition());
        assertFalse(parser.isExpectingMoreData());
        assertTrue(message instanceof LocateRequestMessage_1_2);
        assertEquals(18, parser.getMsgByteBuffer().limit());
    }


    @Test
    public void whenTimedOutBetweenMessages_doNothing() {
        byte[] header = {'G', 'I', 'O', 'P', 1, 2, Message.FLAG_NO_FRAG_BIG_ENDIAN, Message.GIOPReply, 0, 0, 0, 6, 1, 2, 3, 4, 5, 6};
        ByteBuffer buffer = ByteBuffer.wrap(header);

        parser.offerBuffer(buffer);
        parser.offerBuffer(null);

        parser.checkTimeout(Integer.MAX_VALUE);
    }

    @Test
    public void whenMidBodyButNotTimedOut_doNothing() {
        byte[] header = {'G', 'I', 'O', 'P', 1, 2, Message.FLAG_NO_FRAG_BIG_ENDIAN, Message.GIOPReply, 0, 0, 0, 6, 1};
        ByteBuffer buffer = ByteBuffer.wrap(header);

        parser.offerBuffer(buffer);
        parser.offerBuffer(null);

        parser.checkTimeout(10);
    }

    @Test(expected = COMM_FAILURE.class)
    public void whenTimedOutMidHeader_throwAnException() {
        byte[] header = {'G', 'I', 'O', 'P', 1, 2};
        ByteBuffer buffer = ByteBuffer.wrap(header);

        parser.offerBuffer(buffer);

        parser.checkTimeout(Integer.MAX_VALUE);
    }

    @Test(expected = COMM_FAILURE.class)
    public void whenTimedOutMidBody_throwAnException() {
        byte[] header = {'G', 'I', 'O', 'P', 1, 2, Message.FLAG_NO_FRAG_BIG_ENDIAN, Message.GIOPReply, 0, 0, 0, 6, 1};
        ByteBuffer buffer = ByteBuffer.wrap(header);

        parser.offerBuffer(buffer);
        parser.offerBuffer(null);

        parser.checkTimeout(Integer.MAX_VALUE);
    }

    @Test(expected = COMM_FAILURE.class)
    public void whenTimedOutWhileWaitingForFragment_throwAnException() {
        byte[] header = {'G', 'I', 'O', 'P', 1, 1, Message.MORE_FRAGMENTS_BIT, Message.GIOPReply, 0, 0, 0, 6, 1, 2, 3, 4, 5, 6};
        ByteBuffer buffer = ByteBuffer.wrap(header);

        parser.offerBuffer(buffer);
        parser.offerBuffer(null);

        parser.checkTimeout(Integer.MAX_VALUE);
    }

    @Test
    public void whenToStringInvoked_stateIsReported() {
        assertTrue(parser.toString().contains("expectingMoreData=false"));
    }

    static abstract class ORBDataFake implements ORBData {
        private GIOPVersion giopVersion = GIOPVersion.V1_2;

        @Override
        public int getReadByteBufferSize() {
            return BUFFER_SIZE;
        }

        @Override
        public GIOPVersion getGIOPVersion() {
            return giopVersion;
        }
    }

    static abstract class ORBFake extends ORB {
        private ORBData orbData;
        private TransportManager transportManager;

        @Override
        public ORBData getORBData() {
            return orbData;
        }

        @Override
        public TransportManager getTransportManager() {
            return transportManager;
        }
    }

    static abstract class ConnectionFake implements Connection {
    }

    static abstract class TransportManagerFake implements TransportManager {
        private MessageTraceManager mtm = new MessageTraceManagerImpl();

        @Override
        public MessageTraceManager getMessageTraceManager() {
            return mtm;
        }
    }
}
