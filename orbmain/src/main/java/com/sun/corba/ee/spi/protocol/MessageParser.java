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

package com.sun.corba.ee.spi.protocol;

import java.nio.ByteBuffer;

import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.trace.Transport;
import com.sun.corba.ee.spi.transport.Connection;

import com.sun.corba.ee.impl.protocol.giopmsgheaders.Message;


/**
 *
 * An interface that knows how to parse bytes into a protocol data unit.
 */
public interface MessageParser {

    @Transport
    ByteBuffer getNewBufferAndCopyOld(ByteBuffer byteBuffer);

    /**
     * Is this MessageParser expecting more data ?
     *
     * This method is typically called after a call to <code>parseBytes()</code>
     * to determine if the <code>ByteBuffer</code> which has been parsed
     * contains a partial <code>Message</code>.
     *
     * @return - <code>true</code> if more bytes are needed to construct a
     *           <code>Message</code>.  <code>false</code>, if no 
     *           additional bytes remain to be parsed into a <code>Message</code>.
     */
    boolean isExpectingMoreData();

    /**
     * If there are sufficient bytes in the <code>ByteBuffer</code> to compose a
     * <code>Message</code>, then return a newly initialized <code>Message</code>.
     * Otherwise, return null.
     *
     * When this method is first called, it is assumed that 
     * <code>ByteBuffer.position()</code> points to the location in the 
     * <code>ByteBuffer</code> where the beginning of the first
     * <code>Message</code> begins.
     * 
     * If there is no partial <code>Message</code> remaining in the 
     * <code>ByteBuffer</code> when this method exits, this method will e
     * <code>this.expectingMoreData</code> to <code>false</code>.
     * Otherwise, it will be set to <code>true</code>.
     * 
     * Callees of this method may check <code>isExpectingMoreData()</code> 
     * subsequently to determine if this <code>MessageParser</code> is expecting 
     * more data to complete a protocol data unit.  Callees may also 
     * subsequently check <code>hasMoreBytesToParse()</code> to determine if this 
     * <code>MessageParser</code> has more data to parse in the given
     * <code>ByteBuffer</code>.
     *
     * @param byteBuffer Buffer to parse
     * @param connection connection for message
     * @return <code>Message</code> if one is found in the <code>ByteBuffer</code>.
     *         Otherwise, returns null.
     */
    // REVISIT - This interface should be declared without a CorbaConnection.
    //           As a result, this interface will likely be deprecated in a
    //           future release in favor of Message parseBytes(ByteBuffer byteBuffer)
    Message parseBytes(ByteBuffer byteBuffer, Connection connection);

    /**
     * Are there more bytes to be parsed in the <code>ByteBuffer</code> given
     * to this MessageParser's <code>parseBytes</code> ?
     *
     * This method is typically called after a call to <code>parseBytes()</code>
     * to determine if the <code>ByteBuffer</code> has more bytes which need to
     * parsed into a <code>Message</code>.
     *
     * @return <code>true</code> if there are more bytes to be parsed.
     *         Otherwise <code>false</code>.
     */
    boolean hasMoreBytesToParse();

    /**
     * Set the starting position where the next message in the
     * <code>ByteBuffer</code> given to <code>parseBytes()</code> begins.
     * @param position the next starting position
     */
    void setNextMessageStartPosition(int position);

    /**
     * Get the starting position where the next message in the
     * <code>ByteBuffer</code> given to <code>parseBytes()</code> begins.
     * @return start position in the buffer of the next message
     */
    int getNextMessageStartPosition();

    /**
     * Return the suggested number of bytes needed to hold the next message
     * to be parsed.
     * @return bytes needed to hold message
     */
    int getSizeNeeded();

    /**
     * Returns the byte buffer (if any) associated with the last message returned.
     * @return the associated byte buffer
     */
    ByteBuffer getMsgByteBuffer();

    /**
     * Offers an input buffer to the parser. Position must be set to 0, and the buffer must contain at least the start
     * of a GIOP message. The parser will consume what it can and make the remainder available in {@link #getRemainderBuffer}
     * @param buffer a buffer containing at least the start of a GIOP message.
     */
    void offerBuffer(ByteBuffer buffer);

    /**
     * Returns a buffer containing whatever is left after processing the buffer provided in {@link #offerBuffer(ByteBuffer)},
     * which could be the same buffer. The buffer could also be null if all data has been consumed.
     * @return a byte buffer representing data which still needs to be processed.
     */
    ByteBuffer getRemainderBuffer();

    /**
     * Returns the full message constructed by the last call to {@link #offerBuffer(ByteBuffer)}. Will be null if
     * the last such call did not complete a message.
     * @return a complete message, wrapped in a message mediator.
     */
    MessageMediator getMessageMediator();

    /**
     * Checks for a stalled or rogue client. If in the middle of receiving a message and the time exceeds the limit,
     * will throw a communications failure exception.
     * @param timeSinceLastInput the number of milliseconds since the last input was received.
     */
    void checkTimeout(long timeSinceLastInput);

    boolean isExpectingFragments();
}
