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

package corba.rogueclient;

import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;
import com.sun.corba.ee.spi.ior.iiop.IIOPAddress;
import com.sun.corba.ee.spi.ior.iiop.IIOPProfile;
import com.sun.corba.ee.spi.ior.iiop.IIOPProfileTemplate;
import com.sun.corba.ee.spi.presentation.rmi.StubAdapter;
import com.sun.corba.ee.spi.protocol.ClientDelegate;
import com.sun.corba.ee.spi.transport.ContactInfoList;
import com.sun.corba.ee.impl.misc.ORBUtility;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.Message;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.rmi.RemoteException;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import java.util.concurrent.atomic.AtomicInteger ;
import org.glassfish.pfl.test.JUnitReportHelper;

public class RogueClient implements Runnable
{
    // shared across all instances of RogueClients
    private static final boolean dprint = false;
    private static final boolean itsBigEndian = (ByteOrder.BIG_ENDIAN == ByteOrder.nativeOrder());
    private static final int NUM_ROGUE_CLIENTS = 10;
    private static final byte HEX_G = (byte)0x47;
    private static final byte HEX_I = (byte)0x49;
    private static final byte HEX_O = (byte)0x4f;
    private static final byte HEX_P = (byte)0x50;
    private static final byte[] BOGUS_BYTES = new byte[] {
        0x00,0x00,0x00,0x06,0x03,0x00,0x00,0x00,0x00,0x00,
        0x00,0x02,0x00,0x00,0x00,0x19,-0x51,-0x55,-0x35,0x00,
        0x00,0x00,0x00,0x02,0x7a,-0x24,0x1d,-0x69,0x00,0x00,
        0x00,0x08,0x00,0x00,0x00,0x01 };

    // unique to each instance of a RogueClient
    private String itsHostname = null;
    private int itsPort = 0;
    private SocketChannel itsSocketChannel = null;
    private JUnitReportHelper helper = new JUnitReportHelper( RogueClient.class.getName() ) ;
    private int createConnectionToServerCallCounter = 0 ;
    private static AtomicInteger numFailures = new AtomicInteger() ;

    private static volatile boolean useHelper = true ;
    private int clientNum;
    private static int numClients;

    public RogueClient() {
        clientNum = numClients++;
    }

    private void start( String name, int ctr ) {
        if (useHelper)
            helper.start( name + ctr ) ;

        print("RogueClient." + name + "()");
    }

    private void start( String name ) {
        if (useHelper)
            helper.start( name ) ;

        print("RogueClient." + name + "()");
    }

    private void handlePass() {
        if (useHelper)
            helper.pass() ;

        print("PASS");
    }

    private void handleException(Exception ex) throws Exception {
        numFailures.incrementAndGet() ;

        print("Unexpected exception -> " + ex);

        StackTraceElement[] ste = ex.getStackTrace();
        for (StackTraceElement aSte : ste) {
            print(aSte.toString());
        }

        helper.fail( ex ) ;

        throw ex ;
    }

    private void printBuffer(ByteBuffer byteBuffer) {
        print("+++++++ GIOP Buffer ++++++++\n");
        print("Current position: " + byteBuffer.position());
        print("Total length : " + byteBuffer.limit() + "\n");

        char[] charBuf = new char[16];

        try {

            for (int i = 0; i < byteBuffer.position(); i += 16) {
                
                int j = 0;
                
                // For every 16 bytes, there is one line
                // of output.  First, the hex output of
                // the 16 bytes with each byte separated
                // by a space.
                while (j < 16 && j + i < byteBuffer.position()) {
                    int k = byteBuffer.get(i + j);
                    if (k < 0)
                        k = 256 + k;
                    String hex = Integer.toHexString(k);
                    if (hex.length() == 1)
                        hex = "0" + hex;
                    System.out.print(hex + " ");
                    j++;
                }
                
                // Add any extra spaces to align the
                // text column in case we didn't end
                // at 16
                while (j < 16) {
                    System.out.print("   ");
                    j++;
                }
                
                // Now output the ASCII equivalents.  Non-ASCII
                // characters are shown as periods.
                int x = 0;
                while (x < 16 && x + i < byteBuffer.position()) {
                    if (ORBUtility.isPrintable((char)byteBuffer.get(i + x)))
                        charBuf[x] = (char)byteBuffer.get(i + x);
                    else
                        charBuf[x] = '.';
                    x++;
                }
                print(new String(charBuf, 0, x));
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        print("++++++++++++++++++++++++++++++");
    }

    private void getHostnameAndPort(Tester tester)
    {
        // Get the host and port number of server
        print("RogueClient.getHostnameAndPort()");
        ClientDelegate delegate =
            (ClientDelegate)StubAdapter.getDelegate(tester);
        ContactInfoList ccil = delegate.getContactInfoList();
        IOR effectiveTargetIOR = ccil.getEffectiveTargetIOR();
        IIOPProfile iiopProfile = effectiveTargetIOR.getProfile();
        IIOPProfileTemplate iiopProfileTemplate =
            (IIOPProfileTemplate)iiopProfile.getTaggedProfileTemplate() ;
        IIOPAddress primary = iiopProfileTemplate.getPrimaryAddress() ;

        itsHostname = primary.getHost().toLowerCase();
        itsPort = primary.getPort();
        
        String testerIOR = tester.toString();
        print("\tRemote object, Tester " + testerIOR);
        print("\tCan be found at:");
        print("\tHostname -> " + itsHostname);
        print("\tPort -> " + itsPort);
        print("Successful");
    } 

    private void createConnectionToServer() throws Exception {
        start( "createConnectionToServer",
            createConnectionToServerCallCounter++ ) ;
        
        // create SocketChannel to server
        try {
            InetSocketAddress isa = new InetSocketAddress(itsHostname, itsPort);
            itsSocketChannel = ORBUtility.openSocketChannel(isa);
        }
        catch (Exception ex) {
            handleException(ex);
        }

        handlePass() ;
    }

    private void write_octet(byte[] theBuf, int index, byte theValue) {
        theBuf[index] = theValue;
    }

    private void buildGIOPHeader(byte[] theBuf, int theMessageSize)
    {
        int index = 0;

        // write GIOP string, always written big endian
        write_octet(theBuf, index++, HEX_G);
        write_octet(theBuf, index++, HEX_I);
        write_octet(theBuf, index++, HEX_O);
        write_octet(theBuf, index++, HEX_P);

        // write GIOP version 1.2, bytes 5,6
        write_octet(theBuf, index++, GIOPVersion.DEFAULT_VERSION.getMajor());
        write_octet(theBuf, index++, GIOPVersion.DEFAULT_VERSION.getMinor());

        // write endian-ness and no fragment bit (either 0x00 or 0x01)
        // byte 6, bits 0 & 1
        if (itsBigEndian) {
            write_octet(theBuf, index++, Message.FLAG_NO_FRAG_BIG_ENDIAN);
        } else {
            write_octet(theBuf, index++, Message.LITTLE_ENDIAN_BIT);
        }

        // write GIOPRequest type, byte 8
        write_octet(theBuf, index++, Message.GIOPRequest);

        // write message size
        write_message_size(theBuf, index, theMessageSize);
    }

    private void write_message_size(byte[] theBuf, int index, int theMessageSize) {
        // write message size, bytes 9,10,11,12
        if (itsBigEndian) {
            write_octet(theBuf, index++, (byte)((theMessageSize >>> 24) & 0xFF));
            write_octet(theBuf, index++, (byte)((theMessageSize >>> 16) & 0xFF));
            write_octet(theBuf, index++, (byte)((theMessageSize >>> 8) & 0xFF));
            write_octet(theBuf, index,   (byte)(theMessageSize & 0xFF));
        } else {
            write_octet(theBuf, index++, (byte)(theMessageSize & 0xFF));
            write_octet(theBuf, index++, (byte)((theMessageSize >>> 8) & 0xFF));
            write_octet(theBuf, index++, (byte)((theMessageSize >>> 16) & 0xFF));
            write_octet(theBuf, index,   (byte)((theMessageSize >>> 24) & 0xFF));
        }
    }

    private void sendData(ByteBuffer byteBuffer, int numBytesToWrite)
        throws Exception { 

        int bytesWrit;
        do {
            bytesWrit = itsSocketChannel.write(byteBuffer);
        } while (bytesWrit < numBytesToWrite);
    }

    private ByteBuffer createGIOPMessage() {

        // create a GIOP header
        byte[] request = new byte[Message.defaultBufferSize];

        // build GIOP header
        buildGIOPHeader(request, request.length - Message.GIOPMessageHeaderLength);

        // add some bogus junk to a rogue request
        for (int i = 0; i < BOGUS_BYTES.length; i++) {
            write_octet(request,
                        i+Message.GIOPMessageHeaderLength,
                        BOGUS_BYTES[i]);
        }

        ByteBuffer byteBuffer = ByteBuffer.wrap(request);
        byteBuffer.position(0);
        byteBuffer.limit(Message.GIOPMessageHeaderLength+BOGUS_BYTES.length);

        if (dprint) {
            ByteBuffer viewBuffer = byteBuffer.asReadOnlyBuffer();
            viewBuffer.position(Message.GIOPMessageHeaderLength+BOGUS_BYTES.length);
            printBuffer(viewBuffer);
        }

        return byteBuffer;
    }

    private void runValidHeaderSlowBody() throws Exception {
        start( "runValidHeaderSlowBody" ) ;

        ByteBuffer byteBuffer = createGIOPMessage();

        // send full, valid GIOP header
        ByteBuffer b = ByteBuffer.allocateDirect(Message.GIOPMessageHeaderLength);
        for (int i = 0; i < Message.GIOPMessageHeaderLength; i++) {
            b.put(byteBuffer.get(i));
        }
        b.flip();

        try {
            sendData(b, Message.GIOPMessageHeaderLength);
            
            // send message body 1 byte a time with a delay between them
            for (int i = Message.GIOPMessageHeaderLength; i < byteBuffer.limit(); i++) {
                b = ByteBuffer.allocateDirect(1);
                b.put(byteBuffer.get(i));
                b.flip();
                sendData(b, 1);
                Thread.sleep(250);
            }
            Thread.sleep(5000);
        } catch (IOException ioe) {
            // We expect Server to complain with an IOException.
            // So, we must close the connection and re-open it.
            print("\tReceived expected IOException: " + ioe.toString());
            print("\tWill attempt to re-establish connection to server..");
            try {
                itsSocketChannel.close();
            } catch (IOException ioex) {
                handleException(ioex);
                throw ioex ;
            }

            handleException( ioe ) ;
            createConnectionToServer();
            throw ioe ;
        } catch (Exception ex) {
            handleException(ex);
            throw ex ;
        }

        handlePass() ;
    }

    private void runSlowGIOPHeader() throws Exception {
        start( "runSlowGIOPHeader" ) ;

        ByteBuffer byteBuffer = createGIOPMessage();

        // send GIOP header
        try {
            // send 1 byte a time with a delay between them
            for (int i = 0; i < byteBuffer.limit(); i++) {
                ByteBuffer b = ByteBuffer.allocateDirect(1);
                b.put(byteBuffer.get(i));
                b.flip();
                sendData(b, 1);
                Thread.sleep(500);
            }
            Thread.sleep(5000);
        } catch (IOException ioe) {
            System.out.println("received exception:");
            ioe.printStackTrace();
            // We expect Server to complain with an IOException.
            // So, we must close the connection and re-open it.
            print("\tReceived expected IOException: " + ioe.toString());
            print("\tWill attempt to re-establish connection to server...");
            try {
                itsSocketChannel.close();
            } catch (IOException ioex) {
                handleException(ioex);
                throw ioex ;
            }
            createConnectionToServer();
        } catch (Exception ex) {
            handleException(ex);
            throw ex ;
        }

        handlePass() ;
    }

    private void runValidHeaderBogusLength() throws Exception {
        start( "runValidHeaderBogusLength" ) ;

        ByteBuffer byteBuffer = createGIOPMessage();
        write_message_size(byteBuffer.array(),8,byteBuffer.limit() + 50);

        try {
            // send valid header with bogus message length
            sendData(byteBuffer, byteBuffer.limit());
            Thread.sleep(10000);
        } catch (Exception ex) {
            handleException(ex);
        }

        handlePass() ;
        print("PASSED");
    }


    private void runSendMessageAndCloseConnection() throws Exception {
        start( "runSendMessageAndCloseConnection" ) ;
        
        ByteBuffer byteBuffer = createGIOPMessage();
        byteBuffer.flip();
        try {
            sendData(byteBuffer, byteBuffer.limit());
            // immediately close the channel
            itsSocketChannel.close();
        } catch (Exception ex) {
            handleException(ex);
        }

        handlePass() ;

        createConnectionToServer();
    }

    private void runRogueConnectManyTests() throws Exception {
        helper.start( "runRogueConnectManyTests" ) ;
        try {
            String message = "RogueClient.runRogueConnectManyTests()";
            print(message);
            // create a bunch of RogueClients and let them bang away
            Thread[] rogueClientThreads = new Thread[NUM_ROGUE_CLIENTS];

            for (int i = 0; i < NUM_ROGUE_CLIENTS; i++) {
                rogueClientThreads[i] = new Thread(new RogueClient());
            }

            for (Thread thread : rogueClientThreads) {
                thread.start();
            }
            
            for (Thread thread : rogueClientThreads) {
                thread.join();
            }

            print("PASSED");
        } finally {
            if (numFailures.get() == 0)
                helper.pass() ;
            else
                helper.fail( "Failed with " + numFailures.get() + " errors" ) ;
        }
    }

    private void print(String message) {
        System.out.println("Rogue Client[" + clientNum + "]: " + message);
    }

    private void runSaneTest(Tester tester)
        throws RemoteException
    {
        // call a method on the Tester object
        print("RogueClient.runSaneTest()");
        String desc = tester.getDescription();
        print("\tGot 'Tester' description: " + desc);
        print("PASSED");
    }

    @Override
    public void run() {
        try {
            print("Finding Tester ...");
            InitialContext rootContext = new InitialContext();
            print("Looking up Tester...");
            java.lang.Object tst = rootContext.lookup("Tester");
            print("Narrowing...");
            Tester tester 
                = (Tester)PortableRemoteObject.narrow(tst,
                                                      Tester.class);
            getHostnameAndPort(tester);
            createConnectionToServer();
            runSaneTest(tester);
            runValidHeaderBogusLength();
            runSaneTest(tester);
            runSlowGIOPHeader();
            runSaneTest(tester);
            runValidHeaderSlowBody();
            runSendMessageAndCloseConnection();
        } catch (org.omg.CORBA.COMM_FAILURE c) {
            StackTraceElement[] ste = c.getStackTrace();
            StringBuilder sb = new StringBuilder(256);
            for (StackTraceElement aSte : ste) {
                sb.append(aSte);
            }
            print("Received an expected org.omg.COMM_FAILURE: " + c.toString()
                    + " stack trace :\n" + sb.toString());
        } catch (Throwable t) {
            print("Unexpected throwable!!!");
            t.printStackTrace();
            helper.done() ;
            System.exit(1) ;
        } finally {
            helper.done() ;
        }
    }

    public static void main(String args[]) {
        System.out.println("Beginning test...");

        // run a single RogueClient
        RogueClient rogueClient = new RogueClient();
        try {
            Thread clientThread = new Thread(rogueClient);
            clientThread.start();
            clientThread.join();

            useHelper = false ;

            // run a bunch of RogueClients
            rogueClient.runRogueConnectManyTests();

        } catch (Exception ex) {
            ex.printStackTrace() ;
        } 

        int failures = numFailures.get() ;
        if (failures == 0) 
            System.out.println("Test finished successfully...");

        System.exit( numFailures.get() ) ;
    }
}

