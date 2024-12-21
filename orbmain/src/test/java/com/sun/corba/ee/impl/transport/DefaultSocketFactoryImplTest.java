package com.sun.corba.ee.impl.transport;

import com.sun.corba.ee.spi.misc.ORBConstants;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.UUID;

import static org.junit.Assert.*;

public class DefaultSocketFactoryImplTest {
    private static final String TEST_TYPE = ORBConstants.SOCKETCHANNEL;
    private static final int TEST_TIMEOUT = 1000;

    @Test
    public void testCreateSocketWithSocketChannelType() throws IOException {
        DefaultSocketFactoryImpl sf = new DefaultSocketFactoryImpl();
        String testMessage = UUID.randomUUID().toString();
        try (ServerSocket serverSocket = createServerSocket(testMessage)) {
            final InetSocketAddress address = new InetSocketAddress("localhost", serverSocket.getLocalPort());
            Socket socket = sf.createSocket(TEST_TYPE, address, TEST_TIMEOUT);
            assertNotNull(socket);
            assertTrue(socket.getTcpNoDelay());
            validateSocket(socket, testMessage);
        }
    }

    @Test
    public void testCreateSocketWithOtherType() throws IOException {
        DefaultSocketFactoryImpl sf = new DefaultSocketFactoryImpl();
        String testMessage = UUID.randomUUID().toString();
        try (ServerSocket serverSocket = createServerSocket(testMessage)) {
            final InetSocketAddress address = new InetSocketAddress("localhost", serverSocket.getLocalPort());
            Socket socket = sf.createSocket("otherType", address, TEST_TIMEOUT);
            assertNotNull(socket);
            assertTrue(socket.getTcpNoDelay());
            validateSocket(socket, testMessage);
        }
    }

    @Test(expected = SocketTimeoutException.class)
    public void testCreateSocketWithTimeoutSocketChannelType() throws IOException {
        DefaultSocketFactoryImpl sf = new DefaultSocketFactoryImpl();
        InetSocketAddress unreachableAddress = new InetSocketAddress("10.0.0.0", 8080);
        sf.createSocket(TEST_TYPE, unreachableAddress, TEST_TIMEOUT);
    }

    @Test(expected = SocketTimeoutException.class)
    public void testCreateSocketWithTimeoutOtherType() throws IOException {
        DefaultSocketFactoryImpl sf = new DefaultSocketFactoryImpl();
        InetSocketAddress unreachableAddress = new InetSocketAddress("10.0.0.0", 8080);
        sf.createSocket("otherType", unreachableAddress, TEST_TIMEOUT);
    }

    private ServerSocket createServerSocket(String message) throws IOException {
        ServerSocket serverSocket = new ServerSocket();
        serverSocket.bind(null);

        new Thread(() -> {
            try {
                Socket clientSocket = serverSocket.accept();
                OutputStream out = clientSocket.getOutputStream();
                out.write(message.getBytes());
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        return serverSocket;
    }

    private void validateSocket(Socket socket, String expectedMessage) throws IOException {
        InputStream in = socket.getInputStream();
        byte[] buffer = new byte[expectedMessage.length()];
        int read = in.read(buffer);
        assertEquals(expectedMessage, new String(buffer, 0, read));
    }
}
