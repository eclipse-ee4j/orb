/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.encoding;

import java.nio.ByteBuffer;

import com.sun.corba.ee.spi.orb.ORBData;
import com.sun.corba.ee.spi.transport.ByteBufferPool;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.logging.ORBUtilSystemException;
import com.sun.corba.ee.impl.protocol.RequestCanceledException;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.FragmentMessage;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.Message;
import com.sun.corba.ee.spi.trace.Transport;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import org.glassfish.pfl.tf.spi.annotation.InfoMethod;

@Transport
public class BufferManagerReadStream
        implements BufferManagerRead, MarkAndResetHandler {
    private static final ORBUtilSystemException wrapper =
            ORBUtilSystemException.self;

    private volatile boolean receivedCancel = false;
    private int cancelReqId = 0;

    // We should convert endOfStream to a final static dummy end node
    private boolean endOfStream = true;
    private final BufferQueue fragmentQueue = new BufferQueue();
    // REVISIT - This should go in BufferManagerRead. But, since
    //           BufferManagerRead is an interface. BufferManagerRead
    //           might ought to be an abstract class instead of an
    //           interface.
    private final ORB orb;

    BufferManagerReadStream(ORB orb) {
        this.orb = orb;
    }

    public void cancelProcessing(int requestId) {
        synchronized (fragmentQueue) {
            receivedCancel = true;
            cancelReqId = requestId;
            fragmentQueue.notify();
        }
    }

    @InfoMethod
    private void bufferMessage(String msg, int bbAddr, String tail) {}

    @Transport
    public void processFragment(ByteBuffer byteBuffer, FragmentMessage msg) {
        byteBuffer.position(msg.getHeaderLength());

        synchronized (fragmentQueue) {
            if (orb.transportDebugFlag) {
                logBufferMessage("processFragment() - queuing ByteByffer id (", byteBuffer, ") to fragment queue.");
            }
            fragmentQueue.enqueue(byteBuffer);
            endOfStream = !msg.moreFragmentsToFollow();
            fragmentQueue.notify();
        }
    }

    @InfoMethod
    private void underflowMessage(String msg, int rid) {
    }

    @Transport
    public ByteBuffer underflow(ByteBuffer byteBuffer) {

        ByteBuffer result;
        long startNanos = System.nanoTime();

        synchronized (fragmentQueue) {

            if (receivedCancel) {
                underflowMessage("underflow() - Cancel request id:", cancelReqId);
                throw new RequestCanceledException(cancelReqId);
            }

            ORBData orbData = orb.getORBData();
            int timeoutMillis = orbData.fragmentReadTimeout();
            long timeoutNanos = TimeUnit.MILLISECONDS.toNanos(timeoutMillis);
            long waitNanos = timeoutNanos;

            while (fragmentQueue.size() == 0) {

                if (endOfStream) {
                    throw wrapper.endOfStream();
                }

                boolean interrupted = false;
                try {
                    orbData.waitNanos(fragmentQueue, waitNanos);
                } catch (InterruptedException e) {
                    interrupted = true;
                }

                // be robust against spurious wakeups: only throw timeout exception if time has really elapsed
                // or if unable to measure elapsed time because the clock went backwards
                long elapsedNanos = System.nanoTime() - startNanos;
                waitNanos = elapsedNanos < 0 ? 0L : timeoutNanos - elapsedNanos;

                if (!interrupted && waitNanos <= 0 && fragmentQueue.size() == 0) {
                    throw wrapper.bufferReadManagerTimeout();
                }

                if (receivedCancel) {
                    underflowMessage("underflow() - Cancel request id after wait:", cancelReqId);
                    throw new RequestCanceledException(cancelReqId);
                }
            }

            result = fragmentQueue.dequeue();

            // VERY IMPORTANT
            // Release byteBuffer to the ByteBufferPool only if
            // this BufferManagerStream is not marked for potential restore.
            if (!markEngaged && byteBuffer != null) {
                getByteBufferPool().releaseByteBuffer(byteBuffer);
            }
        }
        return result;
    }

    @Override
    public boolean isFragmentOnUnderflow() {
        return true;
    }

    public void init(Message msg) {
        if (msg != null) {
            endOfStream = !msg.moreFragmentsToFollow();
        }
    }

    // Release any queued byteBuffers to the ByteBufferPoool
    @Transport
    public void close(ByteBuffer byteBuffer) {
        int inputBbAddress = 0;

        if (byteBuffer != null) {
            inputBbAddress = System.identityHashCode(byteBuffer);
        }
        ByteBufferPool byteBufferPool = getByteBufferPool();

        // release ByteBuffers on fragmentQueue
        synchronized (fragmentQueue) {
            // IMPORTANT: The fragment queue may have one ByteBuffer
            //            on it that's also on the CDRInputStream if
            //            this method is called when the stream is 'marked'.
            //            Thus, we'll compare the ByteBuffer passed
            //            in (from a CDRInputStream) with all ByteBuffers
            //            on the stack. If one is found to equal, it will
            //            not be released to the ByteBufferPool.

            ByteBuffer aBuffer;
            while (fragmentQueue.size() != 0) {
                aBuffer = fragmentQueue.dequeue();
                if (aBuffer != null) {
                    byteBufferPool.releaseByteBuffer(aBuffer);
                }
            }
        }
        fragmentQueue.clear();

        // release ByteBuffers on fragmentStack
        if (fragmentStack != null && fragmentStack.size() != 0) {
            // IMPORTANT: The fragment stack may have one ByteBuffer
            //            on it that's also on the CDRInputStream if
            //            this method is called when the stream is 'marked'.
            //            Thus, we'll compare the ByteBuffer passed
            //            in (from a CDRInputStream) with all ByteBuffers
            //            on the stack. If one is found to equal, it will
            //            not be released to the ByteBufferPool.

            for (ByteBuffer aBuffer : fragmentStack) {
                if (aBuffer != null) {
                    if (inputBbAddress != System.identityHashCode(aBuffer)) {
                        byteBufferPool.releaseByteBuffer(aBuffer);
                    }
                }
            }

            fragmentStack = null;
        }
    }

    private void logBufferMessage(String prefix, ByteBuffer byteBuffer, String suffix) {
        bufferMessage(prefix, System.identityHashCode(byteBuffer), suffix);
    }

    protected ByteBufferPool getByteBufferPool() {
        return orb.getByteBufferPool();
    }

    // Mark and reset handler ----------------------------------------

    private boolean markEngaged = false;

    // List of fragment ByteBufferWithInfos received since
    // the mark was engaged.
    private LinkedList<ByteBuffer> fragmentStack = null;
    private RestorableInputStream inputStream = null;

    // Original state of the stream
    private Object streamMemento = null;

    public void mark(RestorableInputStream inputStream) {
        this.inputStream = inputStream;
        markEngaged = true;

        // Get the magic Object that the stream will use to
        // reconstruct it's state when reset is called
        streamMemento = inputStream.createStreamMemento();

        if (fragmentStack != null) {
            fragmentStack.clear();
        }
    }

    // Collects fragments received since the mark was engaged.
    public void fragmentationOccured(ByteBuffer newFrament) {
        if (!markEngaged) {
            return;
        }

        if (fragmentStack == null) {
            fragmentStack = new LinkedList<ByteBuffer>();
        }

        fragmentStack.addFirst(newFrament.duplicate());
    }

    public void reset() {
        if (!markEngaged) {
            // REVISIT - call to reset without call to mark
            return;
        }

        markEngaged = false;

        // If we actually did peek across fragments, we need
        // to push those fragments onto the front of the
        // buffer queue.
        if (fragmentStack != null && fragmentStack.size() != 0) {

            synchronized (fragmentQueue) {
                for (ByteBuffer aBuffer : fragmentStack) {
                    fragmentQueue.push(aBuffer);
                }
            }

            fragmentStack.clear();
        }

        // Give the stream the magic Object to restore
        // it's state.
        inputStream.restoreInternalState(streamMemento);
    }

    public MarkAndResetHandler getMarkAndResetHandler() {
        return this;
    }
}
