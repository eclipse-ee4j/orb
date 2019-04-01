/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.transport;

import com.sun.corba.ee.impl.encoding.CDRInputObject;
import com.sun.corba.ee.impl.orb.ORBVersionImpl;
import com.sun.corba.ee.impl.orb.ObjectKeyCacheEntryImpl;
import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.ior.ObjectKey;
import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.orb.ORBData;
import com.sun.corba.ee.spi.orb.ORBVersion;
import com.sun.corba.ee.spi.orb.ObjectKeyCacheEntry;
import com.sun.corba.ee.spi.protocol.MessageMediator;
import com.sun.corba.ee.spi.protocol.ServerRequestDispatcher;
import com.sun.corba.ee.spi.threadpool.NoSuchThreadPoolException;
import com.sun.corba.ee.spi.threadpool.NoSuchWorkQueueException;
import com.sun.corba.ee.spi.threadpool.ThreadPool;
import com.sun.corba.ee.spi.threadpool.ThreadPoolManager;
import com.sun.corba.ee.spi.threadpool.Work;
import com.sun.corba.ee.spi.threadpool.WorkQueue;
import com.sun.corba.ee.spi.transport.Connection;
import com.sun.corba.ee.spi.transport.EventHandler;
import com.sun.corba.ee.spi.transport.InboundConnectionCache;
import com.sun.corba.ee.spi.transport.MessageTraceManager;
import com.sun.corba.ee.spi.transport.Selector;
import com.sun.corba.ee.spi.transport.TcpTimeouts;
import com.sun.corba.ee.spi.transport.TransportManager;
import org.junit.Before;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.AbstractSelectionKey;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import static com.meterware.simplestub.Stub.createStrictStub;
import static com.meterware.simplestub.Stub.createStub;

public class TransportTestBase {
    private OrbFake orb = createStrictStub(OrbFake.class);
    private ORBDataFake orbData = createStrictStub(ORBDataFake.class);
    private SelectorProviderFake selectorProvider = createStrictStub(SelectorProviderFake.class);
    private SocketChannelFake socketChannel = createStrictStub(SocketChannelFake.class, selectorProvider);
    private ConnectionCacheFake connectionCache = createStrictStub(ConnectionCacheFake.class);
    private WorkQueueFake workQueue = createStrictStub(WorkQueueFake.class);
    private AcceptorFake acceptor;
    private ConnectionImpl connection;
    private SocketFake socket = new SocketFake();
    private List<MessageMediator> mediators = new ArrayList<MessageMediator>();
    private TcpTimeoutsFake tcpTimeouts = createStrictStub(TcpTimeoutsFake.class);
    private WaiterFake waiter = createStrictStub(WaiterFake.class);
    private TransportManagerFake transportManager = createStrictStub(TransportManagerFake.class);
    private TransportSelectorFake selector = createStrictStub(TransportSelectorFake.class);
    private ThreadPoolManagerFake threadPoolManager = createStrictStub(ThreadPoolManagerFake.class);
    private ThreadPoolFake threadPool = createStrictStub(ThreadPoolFake.class);

    final protected ORB getOrb() {
        return orb;
    }

    protected void defineRequestDispatcher(RequestDispatcher requestDispatcher) {
        ObjectKeyFake.requestDispatcher = requestDispatcher;
    }

    protected void processQueuedWork() {
        while (!workQueue.items.isEmpty()) {
            workQueue.items.remove().doWork();
        }
    }

    protected int getNumConnectionsRemoved() {
        return connectionCache.numRemoveCalls;
    }

    class BackgroundProcessor extends Thread {
        int numToProcess;

        BackgroundProcessor(int numToProcess) {
            this.numToProcess = numToProcess;
            start();

        }

        protected void waitUntilDone() throws InterruptedException {
            join(200);
        }

        @Override
        public void run() {
            while (numToProcess > 0) {
                if (workQueue.items.isEmpty())
                    Thread.yield();
                else {
                    numToProcess--;
                    Work work = workQueue.items.remove();
                    work.doWork();
                }
            }

        }
    }

    @Before
    public void setUp() throws IOException {
        orb.data = orbData;
        orbData.transportTcpTimeouts = tcpTimeouts;
        orb.transportManager = transportManager;
        orb.threadPoolManager = threadPoolManager;
        threadPoolManager.threadPool = threadPool;
        threadPool.workQueue = workQueue;
        transportManager.selector = selector;
        selector.workQueue = workQueue;
        tcpTimeouts.waiter = waiter;
        socketChannel.socket = socket;
        acceptor = createStub(AcceptorFake.class, orb, 0, "name", "type");
    }

    protected void readFromNio(byte[] data) throws IOException {
        useNio();
        socketChannel.enqueData(data);
    }

    protected void useNio() throws IOException {
        orbData.useSelectThread = true;
        connection = new ConnectionImpl(orb, acceptor, socket);
        connection.setConnectionCache(connectionCache);
    }

    protected void collectMediatorsAsDispatched() {
        connection.dispatcher = new ConnectionImpl.Dispatcher() {
            @Override
            public boolean dispatch(MessageMediator messageMediator) {
                mediators.add( messageMediator );
                return false;
            }
        };
    }

    protected void readFromSocketWithoutChannelAndDispatch(byte[] bytes) {
        socketChannel = null;
        orbData.useSelectThread = false;
        socket.inputStream = new ByteArrayInputStream( bytes );
        connection = new ConnectionImpl(orb, acceptor, socket);
        connection.setConnectionCache(connectionCache);
    }

    protected SocketChannelFake getSocketChannel() {
        return socketChannel;
    }

    protected Queue<Work> getWorkQueue() {
        return workQueue.items;
    }

    public ConnectionImpl getConnection() {
        return connection;
    }

    protected List<MessageMediator> getMediators() {
        return mediators;
    }

    static abstract class ORBDataFake implements ORBData {
        private TcpTimeouts transportTcpTimeouts;
        private boolean useSelectThread = true;

        @Override
        public TcpTimeouts getTransportTcpTimeouts() {
            return transportTcpTimeouts;
        }

        @Override
        public boolean disableDirectByteBufferUse() {
            return true;
        }

        @Override
        public int getReadByteBufferSize() {
            return 100;
        }

        @Override
        public GIOPVersion getGIOPVersion() {
            return GIOPVersion.V1_2;
        }

        @Override
        public boolean nonBlockingReadCheckMessageParser() {
            return true;
        }

        @Override
        public boolean alwaysEnterBlockingRead() {
            return false;
        }

        @Override
        public boolean connectionSocketUseSelectThreadToWait() {
            return useSelectThread;
        }

        @Override
        public boolean connectionSocketUseWorkerThreadForEvent() {
            return true;
        }

        @Override
        public String getORBServerHost() {
            return "localhost";
        }

        @Override
        public short getGIOPTargetAddressPreference() {
            return 3; // allow all types
        }

        @Override
        public int fragmentReadTimeout() {
            return 1000;
        }

        @Override
        public int getMaxReadByteBufferSizeThreshold() {
            return 500;
        }

        @Override
        public void waitNanos(Object obj, long waitNanos) throws InterruptedException {
            ORBData.super.waitNanos(obj, waitNanos);
        }
    }

    static abstract class OrbFake extends ORB {

        private ORBDataFake data;
        private TransportManagerFake transportManager;
        private ThreadPoolManager threadPoolManager;

        @Override
        public ORBData getORBData() {
            return data;
        }

        @Override
        public TransportManager getTransportManager() {
            return transportManager;
        }

        @Override
        public ThreadPoolManager getThreadPoolManager() {
            return threadPoolManager;
        }

        @Override
        public ORBVersion getORBVersion() {
            return ORBVersionImpl.PEORB;
        }

        @Override
        public boolean orbIsShutdown() {
            return false;
        }

        @Override
        public ObjectKeyCacheEntry extractObjectKeyCacheEntry(byte[] objKey) {
            return new ObjectKeyCacheEntryImpl(createStub(ObjectKeyFake.class));
        }

        @Override
        public void startingDispatch() {
        }

        @Override
        public void finishedDispatch() {
        }
    }

    static abstract class ObjectKeyFake implements ObjectKey {
        private static RequestDispatcher requestDispatcher;

        @Override
        public ServerRequestDispatcher getServerRequestDispatcher() {
            return requestDispatcher;
        }
    }

    static abstract class SelectorFake extends AbstractSelector {
        private Set<SelectionKey> selectedKeys = new HashSet<SelectionKey>();

        public SelectorFake(SelectorProvider provider) {
            super(provider);
        }

        @Override
        public int selectNow() throws IOException {
            return 0;
        }

        @Override
        protected SelectionKey register(AbstractSelectableChannel ch, int ops, Object att) {
            SelectionKeyFake selectionKey = createStrictStub(SelectionKeyFake.class, this);
            selectedKeys.add(selectionKey);
            return selectionKey;
        }

        @Override
        public int select(long timeout) throws IOException {
            return 1;
        }

        @Override
        public Set<SelectionKey> selectedKeys() {
            return selectedKeys;
        }

        @Override
        protected void implCloseSelector() throws IOException {
        }
    }

    static abstract class SelectorProviderFake extends SelectorProvider {
        @Override
        public AbstractSelector openSelector() throws IOException {
            return createStrictStub(SelectorFake.class, this);
        }
    }

    static abstract class SocketChannelFake extends SocketChannel {
        private byte[] dataWritten = new byte[0];
        private byte[] readableData;
        private int readPos;
        private ArrayList<Integer> numBytesToWrite = new ArrayList<Integer>();
        private ArrayList<Integer> numBytesToRead = new ArrayList<Integer>();
        private Socket socket;
        private boolean failConfigureBlocking;
        private boolean endOfInput;

        protected void setFailToConfigureBlocking() {
            failConfigureBlocking = true;
        }

        private int getNumBytesToWrite() {
            return numBytesToWrite.isEmpty() ? Integer.MAX_VALUE : numBytesToWrite.remove(0);
        }

        private int getNumBytesToRead() {
            return numBytesToRead.isEmpty() ? Integer.MAX_VALUE : numBytesToRead.remove(0);
        }

        protected void setNumBytesToWrite(int... numBytesToWrite) {
            for (int i : numBytesToWrite)
                this.numBytesToWrite.add(i);
        }

        public void setNumBytesToRead(int... numBytesToRead) {
            for (int i : numBytesToRead)
                this.numBytesToRead.add(i);
        }

        protected void enqueData(byte... dataToBeRead) {
            readableData = new byte[dataToBeRead.length];
            System.arraycopy(dataToBeRead, 0, readableData, 0, dataToBeRead.length);
        }

        protected SocketChannelFake(SelectorProvider provider) {
            super(provider);
        }

        @Override
        public Socket socket() {
            return socket;
        }

        @Override
        public int write(ByteBuffer src) throws IOException {
            int numBytesAvailable = src.limit() - src.position();
            int numToWrite = Math.min(numBytesAvailable, getNumBytesToWrite());
            byte[] bytesToWrite = new byte[numToWrite];
            src.get(bytesToWrite);
            byte[] written = new byte[dataWritten.length + numToWrite];
            System.arraycopy(dataWritten, 0, written, 0, dataWritten.length);
            System.arraycopy(bytesToWrite, 0, written, dataWritten.length, written.length);
            dataWritten = written;
            return numToWrite;
        }

        @Override
        protected void implConfigureBlocking(boolean block) throws IOException {
            if (failConfigureBlocking)
                throw new IOException("Test failure to configure blocking");
        }

        @Override
        public int read(ByteBuffer dst) throws IOException {
            if (endOfInput) return -1;
            int numBytesToRead = Math.min(getNumBytesToRead(), Math.min(dataSize(), bufferCapacity(dst)));
            if (numBytesToRead == 0) return 0;

            dst.put(readableData, readPos, numBytesToRead);
            readPos += numBytesToRead;
            return numBytesToRead;
        }

        private int bufferCapacity(ByteBuffer dst) {
            return dst.limit() - dst.position();
        }

        private int dataSize() {
            return this.readableData == null ? 0 : this.readableData.length - readPos;
        }

        protected byte[] getDataWritten() {
            return dataWritten;
        }


        public void setEndOfInput() {
            endOfInput = true;
        }
    }

    static abstract class TcpTimeoutsFake implements TcpTimeouts {
        private Waiter waiter;

        @Override
        public Waiter waiter() {
            return waiter;
        }
    }

    static abstract class ConnectionCacheFake implements InboundConnectionCache {
        private int numRemoveCalls;

        @Override
        public void stampTime(Connection connection) {
        }

        @Override
        public void remove(Connection connection) {
            numRemoveCalls++;
        }
    }

    static abstract class WaiterFake implements TcpTimeouts.Waiter {
        @Override
        public boolean isExpired() {
            return false;
        }

        @Override
        public int getTimeForSleep() {
            return 1;
        }

        @Override
        public void advance() {
        }
    }

    static abstract class SelectionKeyFake extends AbstractSelectionKey {
        private SelectorFake selector;

        protected SelectionKeyFake(SelectorFake selector) {
            this.selector = selector;
        }

        public SelectorFake selector() {
            return selector;
        }
    }

    static abstract class WorkQueueFake implements WorkQueue {
        private Queue<Work> items = new ArrayDeque<Work>();

        @Override
        public void addWork(Work aWorkItem) {
            items.offer(aWorkItem);
        }
    }

    static abstract class TransportManagerFake implements TransportManager {
        private MessageTraceManager mtm = new MessageTraceManagerImpl();
        public TransportSelectorFake selector;

        @Override
        public MessageTraceManager getMessageTraceManager() {
            return mtm;
        }

        @Override
        public Selector getSelector(int i) {
            return selector;
        }
    }

    static abstract class TransportSelectorFake implements Selector {
        public WorkQueueFake workQueue;

        @Override
        public void unregisterForEvent(EventHandler eventHandler) {
        }

        @Override
        public void registerForEvent(EventHandler eventHandler) {
            if (eventHandler instanceof Work)
                workQueue.addWork((Work) eventHandler);
        }

        @Override
        public void registerInterestOps(EventHandler eventHandler) {
        }
    }

    static abstract class ThreadPoolManagerFake implements ThreadPoolManager {
        private ThreadPool threadPool;

        @Override
        public ThreadPool getThreadPool(int numericIdForThreadpool) throws NoSuchThreadPoolException {
            return threadPool;
        }
    }

    static abstract class ThreadPoolFake implements ThreadPool {
        private WorkQueue workQueue;

        @Override
        public WorkQueue getWorkQueue(int queueId) throws NoSuchWorkQueueException {
            return workQueue;
        }
    }

    static abstract class AcceptorFake extends AcceptorBase {
        protected AcceptorFake(ORB orb, int port, String name, String type) {
            super(orb, port, name, type);
        }
    }

    private class SocketFake extends Socket {
        private InputStream inputStream = null;
        private OutputStream outputStream = null;

        public SocketChannel getChannel() {
            return socketChannel;
        }
        public InputStream getInputStream() throws IOException {
            return inputStream;
        }
        public OutputStream getOutputStream() throws IOException {
            return outputStream;
        }
    }

    protected class RequestDispatcher implements ServerRequestDispatcher {
        @Override
        public IOR locate(ObjectKey key) {
            return null;
        }

        @Override
        public void dispatch(MessageMediator messageMediator) {
            mediators.add(messageMediator);
            readParameters(messageMediator.getInputObject());
        }

        public void readParameters( CDRInputObject input ) {}
    }
}
