/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.msgtypes;

import java.rmi.RemoteException;
import javax.rmi.PortableRemoteObject;

import org.omg.CORBA.INTERNAL ;
import org.omg.CORBA.LocalObject ;

import com.sun.corba.ee.spi.transport.ConnectionCache;

import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.ior.IORFactories;
import com.sun.corba.ee.spi.ior.ObjectKey;
import com.sun.corba.ee.spi.ior.ObjectKeyTemplate;
import com.sun.corba.ee.spi.ior.IORTemplate;
import com.sun.corba.ee.spi.ior.ObjectAdapterId;
import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;
import com.sun.corba.ee.spi.ior.iiop.IIOPAddress;
import com.sun.corba.ee.spi.ior.iiop.IIOPProfileTemplate;
import com.sun.corba.ee.spi.ior.iiop.IIOPFactories;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.protocol.MessageMediator;
import com.sun.corba.ee.spi.protocol.ClientDelegate;
import com.sun.corba.ee.spi.transport.Connection;
import com.sun.corba.ee.spi.transport.ContactInfo ;
import com.sun.corba.ee.spi.transport.ContactInfoList ;
import com.sun.corba.ee.spi.presentation.rmi.StubAdapter ;
import com.sun.corba.ee.spi.servicecontext.ServiceContextDefaults ;

import com.sun.corba.ee.impl.encoding.CDROutputObject;
import com.sun.corba.ee.impl.ior.ObjectKeyFactoryImpl;
import com.sun.corba.ee.impl.ior.ObjectKeyImpl;
import com.sun.corba.ee.impl.ior.POAObjectKeyTemplate;
import com.sun.corba.ee.impl.ior.OldPOAObjectKeyTemplate;
import com.sun.corba.ee.impl.misc.ORBUtility;
import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.impl.protocol.ClientDelegateImpl;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.*;
import com.sun.corba.ee.impl.transport.ContactInfoListImpl;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitializer;


public class Client extends LocalObject 
    implements ORBInitializer, ClientRequestInterceptor 
{
    // These constants are defined here only for this test.
    // The new ObjectKeyTemplate code no longer uses offsets.
    static final int MAGIC_OFFSET = 0 ;
    static final int SCID_OFFSET = 4 ;
    static final int REQUEST_ID = 5;
    
    public static void main(String args[])
    {
        try{
            Properties props = new Properties(System.getProperties());
            /*
            props.setProperty("com.sun.corba.ee.ORBDebug",
                              "giop,transport,subcontract,poa");
            */
            org.omg.CORBA.ORB orb = ORB.init(args, props);

            com.sun.corba.ee.spi.orb.ORB ourORB
                = (com.sun.corba.ee.spi.orb.ORB) orb;

            System.out.println("==== Client GIOP version "
                               + ourORB.getORBData().getGIOPVersion()
                               + " with strategy "
                               + ourORB.getORBData().getGIOPBuffMgrStrategy(
                                            ourORB.getORBData().getGIOPVersion())
                               + "====");

            if (args[0].equals("LocateMsg")) {
                runLocateMsgType(ourORB);
            } else if (args[0].equals("EarlyReply")) {
                runEarlyReply(ourORB);
            } else if (args[0].equals("SimpleCancelRequest")) {
                runSimpleCancelRequest(ourORB);
            } else if (args[0].equals("AbortiveCancelRequest1")) {
                runAbortiveCancelRequest1(ourORB);
            } else if (args[0].equals("AbortiveCancelRequest2")) {
                runAbortiveCancelRequest2(ourORB);
            } else if (args[0].equals("TargetAddrDisposition")) {
                runTargetAddressDisposition(ourORB);                
            } else if (args[0].equals("CloseConnection")) {
                runCloseConnection(ourORB);
            } else if (args[0].equals("MessageError")) {
                runMessageError(ourORB);
            } else if (args[0].equals("GIOPInterop")) {
                runGIOPInterop(ourORB);
            } else if (args[0].equals("FragmentedReply")) {
                runFragmentedReply(ourORB);
            } else if (args[0].equals("HeaderPaddingTest")) {
                runHeaderPaddingTest(ourORB);                
            } else {
                System.out.println("Invalid option");
            }

        } catch (Exception e) {
            System.out.println("ERROR : " + e) ;
            e.printStackTrace(System.out);
            System.exit (1);
        }
    }

    static void runLocateMsgType(ORB orb) 
    {
        org.omg.CORBA.Object fragTestStub = getStub(orb);
        IOR ior = orb.getIOR( fragTestStub, false ) ;
        byte[] objectKey = getObjectKey(orb, ior);
        modifyObjectKey(objectKey);
        LocateRequestMessage msg = getLocateRequestMessage(orb, ior);
        MessageMediator messageMediator =
            beginRequest(orb, fragTestStub, msg);
        org.omg.CORBA.portable.OutputStream os = 
            (org.omg.CORBA.portable.OutputStream)
            messageMediator.getOutputObject();
        msg.write(os);
        messageMediator.finishSendingRequest();
        messageMediator.waitForResponse();
        switch(messageMediator.getLocateReplyHeader().getReplyStatus()) {
        case LocateReplyMessage.UNKNOWN_OBJECT :
            System.out.println("Target object is unknown");
            break;
        case LocateReplyMessage.OBJECT_FORWARD :
        case LocateReplyMessage.OBJECT_FORWARD_PERM :
            System.out.println("Location forward received");
            break;
        case LocateReplyMessage.OBJECT_HERE :
            System.out.println("Target object is available");
            break;
        default:
            System.out.println("Locate reply status is invalid");
            break;
        }
    }
         
    static void runEarlyReply(ORB orb)
        throws RemoteException, BadArrayException 
    {
        org.omg.CORBA.Object fragTestStub = getStub(orb);
        IOR ior = orb.getIOR( fragTestStub, false ) ;
        byte[] objKey = getObjectKey(orb, ior);
        modifyObjectKey(objKey);

        // construct a new IOR
        String typeId = ior.getTypeId();

        IIOPProfileTemplate iptemp = 
            (IIOPProfileTemplate)ior.getProfile().getTaggedProfileTemplate() ;
        IIOPAddress addr = iptemp.getPrimaryAddress();

        ObjectKey objectKey = orb.getObjectKeyFactory().create(objKey);
        GIOPVersion gversion = iptemp.getGIOPVersion() ;

        IIOPProfileTemplate iproftemp = 
            IIOPFactories.makeIIOPProfileTemplate(orb, gversion, addr);
        IORTemplate iortemp = IORFactories.makeIORTemplate(objectKey.getTemplate()) ;
        iortemp.add( iproftemp ) ;

        IOR newIor = iortemp.makeIOR(orb, typeId, objectKey.getId() ) ;
        
        // IOR -> CorbaContactInfoList ->ClientDelegateImpl, then set
        // new Delegate in the stub.
        ContactInfoList ccil =
            new ContactInfoListImpl(orb, newIor);
        ClientDelegate cdel = new ClientDelegateImpl( orb, ccil ) ;
        StubAdapter.setDelegate( fragTestStub, cdel ) ;

        // invoke on target
        try {
            String fragSizeStr =
                System.getProperty(ORBConstants.GIOP_FRAGMENT_SIZE);
            int fragmentSize = Integer.decode(fragSizeStr).intValue();
            testByteArray((FragmentTester) fragTestStub,
                          fragmentSize * 1000 * 3);
        } catch (org.omg.CORBA.INV_OBJREF e) {
            System.out.println("Early reply test over");
            // do nothing. This is expected.
        } catch (java.rmi.NoSuchObjectException e) {
            System.out.println("Early reply test over");
            // do nothing. This is expected.
        }
    }

    static void runSimpleCancelRequest(ORB orb) 
    {
        org.omg.CORBA.Object fragTestStub = getStub(orb);
        IOR ior = orb.getIOR( fragTestStub, false ) ;
        LocateRequestMessage msg = getLocateRequestMessage(orb, ior);
        MessageMediator messageMediator =
            beginRequest(orb, fragTestStub, msg);
        GIOPVersion requestVersion =
            GIOPVersion.chooseRequestVersion(orb, ior);
        try {
            ((Connection) messageMediator.getConnection())
                .sendCancelRequest(requestVersion, REQUEST_ID);
        } catch (IOException e) {}
        System.out.println("SimpleCancelRequestMsg sent successfully");
    }

    static void runAbortiveCancelRequest1(ORB orb) 
    {

        org.omg.CORBA.Object fragTestStub = getStub(orb);
        IOR ior = orb.getIOR( fragTestStub, false ) ;
        byte[] objectKey = getObjectKey(orb, ior);
        modifyObjectKey(objectKey);

        LocateRequestMessage msg = getLocateRequestMessage(orb, ior);
        MessageMediator messageMediator =
            beginRequest(orb, fragTestStub, msg);

        CDROutputObject os = (CDROutputObject)
            messageMediator.getOutputObject();
        // create GIOP header and write to output buffer
        os.write_long(Message.GIOPBigMagic);
        GIOPVersion requestVersion =
            GIOPVersion.chooseRequestVersion(orb, ior);
        requestVersion.write((org.omg.CORBA.portable.OutputStream)os);
        os.write_octet(Message.FLAG_NO_FRAG_BIG_ENDIAN);
        os.write_octet(Message.GIOPLocateRequest);
        os.write_ulong(0);

        // write the requestId to the output buffer
        os.write_ulong (REQUEST_ID);

        // send first fragment. This will cause the server to start the
        // worker thread and the worker thread will block to read stream.
        os.sendFirstFragment();

        // send cancel request
        try {
            ((Connection) messageMediator.getConnection())
                .sendCancelRequest(requestVersion, REQUEST_ID);
        } catch (IOException e) {}
        System.out.println("AbortiveCancelRequestMsg sent successfully");
    }

    static void runAbortiveCancelRequest2(ORB orb)
    {

        org.omg.CORBA.Object fragTestStub = getStub(orb);
        IOR ior = orb.getIOR( fragTestStub, false ) ;
        GIOPVersion requestVersion =
            GIOPVersion.chooseRequestVersion(orb, ior);
        byte encodingVersion =
            ORBUtility.chooseEncodingVersion(orb, ior, requestVersion);
        RequestMessage msg =
            MessageBase.createRequest(
                orb, requestVersion, encodingVersion,
                REQUEST_ID, true, ior, 
                KeyAddr.value, "verifyTransmission", 
                ServiceContextDefaults.makeServiceContexts( orb ), null);
        MessageMediator messageMediator =
            beginRequest(orb, fragTestStub, msg);
        CDROutputObject os = (CDROutputObject)
            messageMediator.getOutputObject();

        msg.write(os);

        // send first fragment which has the request header.
        // This will cause the server to start the
        // worker thread and the worker thread will block to read stream for
        // umarshalling method input parameters.
        os.sendFirstFragment();

        // send cancel request
        try {
            ((Connection) messageMediator.getConnection())
                .sendCancelRequest(requestVersion, REQUEST_ID);
        } catch (IOException e) {}

        try {
            // This sleep is necessary since the above fragment tranmission does
            // not block. The sleep ensure that the outcome verification happens
            // after the request cancellation takes place.
            Thread.sleep(10000);
        } catch (Exception e) {}

        try {
            FragmentTester fragTester = (FragmentTester) fragTestStub;
            boolean outcome = fragTester.verifyOutcome();
            System.out.println("AbortiveCancelRequestMsg2 finished is : " +
                outcome);
            if (outcome == false) {
                throw new RuntimeException("Test failed");
            }
        } catch (java.rmi.RemoteException e) {
            throw new RuntimeException(e.toString());
        }
    }

    static void runTargetAddressDisposition(ORB orb) 
    {

        FragmentTester frag = (FragmentTester) getStub(orb);

        try {
            testByteArray(frag, 4);
            boolean outcome = frag.verifyOutcome();
            System.out.println("ClientInvCount : " + interceptorInvocationCount);
            System.out.println("TargetAddrDisp outcome is : " + outcome);
            if (interceptorInvocationCount != 4 || outcome == false) {
                throw new RuntimeException("Test failed");
            }
            interceptorInvocationCount = 0;
        } catch (java.rmi.RemoteException e) {
            throw new RuntimeException(e.toString());
        } catch (BadArrayException e) {
            throw new RuntimeException(e.toString());               
        }
    }

    static void runCloseConnection(ORB orb) 
    {

        org.omg.CORBA.Object fragTestStub = getStub(orb);
        IOR ior = orb.getIOR( fragTestStub, false ) ;
        LocateRequestMessage msg = getLocateRequestMessage(orb, ior);
        MessageMediator messageMediator =
            beginRequest(orb, fragTestStub, msg);
        GIOPVersion requestVersion =
            GIOPVersion.chooseRequestVersion(orb, ior);
        try {
            ((Connection) messageMediator.getConnection())
                .sendCloseConnection(requestVersion);
        } catch (IOException e) {}
        System.out.println("CloseConnectionMsg sent successfully");
    }

    /**
     * This test checks if lazy body padding works properly. GIOP spec requires
     * that for versions >= 1.2, body must be aligned on a 8-octet boundary, but
     * if a body is absent in an request/reply then the header must not be
     * padded. Our ORB uses a lazy body padding technique, that inserts the
     * padding in order to align the body, only if the body is present.
     * 
     * @param orb ORB
     */
    static void runHeaderPaddingTest(ORB orb) {
        int align = ORBConstants.GIOP_12_MSG_BODY_ALIGNMENT; // 8 bytes length
        int charLength = 1;
        org.omg.CORBA.Object fragTestStub = getStub(orb);      
        CDROutputObject os = (CDROutputObject) StubAdapter.request(fragTestStub, "fooA", false); // CASE 1
        int beforePaddingIndex = os.getBufferPosition();
        os.write_char('a'); // forces padding if not already naturally aligned
        int afterPaddingIndex = os.getBufferPosition();
        int paddingLength = afterPaddingIndex-beforePaddingIndex-charLength;
        if ((paddingLength < 0) || (paddingLength > align)) {
            throw new RuntimeException("marshalling error"); // cannot happen
        }
        if ((paddingLength > 0) && (paddingLength < align)) {
            System.out.println("HeaderPaddingTest(1) completed successfully");
            return; // padding was inserted. No natural alignment.
        }
        
        // The only possibility now if for padding to be zero, because the
        // body in the previous case was likely naturally aligned. 
        // So, now force non-alignment, in order to
        // check to see if padding is inserted. This is done by calling the
        // the method 'fooAA', which has an additional character in its name,
        // that will force non-alignment.
        os = (CDROutputObject)
            StubAdapter.request(fragTestStub, "fooAA", false); // CASE 2
        beforePaddingIndex = os.getBufferPosition();
        os.write_char('a'); // forces padding if not already naturally aligned
        afterPaddingIndex = os.getBufferPosition();
        paddingLength = afterPaddingIndex-beforePaddingIndex-charLength;
        if ((paddingLength < 0) || (paddingLength > align)) {
            throw new RuntimeException("marshalling error"); // cannot happen
        }
        if ((paddingLength > 0) && (paddingLength < align)) {
            // padding was inserted. No natural alignment.
            // Previous case was a case of natural alignment.
            System.out.println("HeaderPaddingTest(2) completed successfully");
            return; 
        } else { // paddingLength == 0
            // Cannot happen. In order for this (padding == 0) to occur in both
            // cases, the header must have been forcibly padded always, 
            // which is incorrect. This indicates lazy body padding is not
            // working properly.
            throw new RuntimeException("Header padding error");
        }        
    }

    static void runMessageError(ORB orb)
    {
        org.omg.CORBA.Object fragTestStub = getStub(orb);
        IOR ior = orb.getIOR( fragTestStub, false ) ;
        LocateRequestMessage msg = getLocateRequestMessage(orb, ior);
        MessageMediator messageMediator =
            beginRequest(orb, fragTestStub, msg);
        GIOPVersion requestVersion =
            GIOPVersion.chooseRequestVersion(orb, ior);
        try {
            ((Connection) messageMediator.getConnection())
                .sendMessageError(requestVersion);
        } catch (IOException e) {}
        System.out.println("MessageError sent successfully");
    }

    public static void runGIOPInterop(ORB orb) 
    {
        org.omg.CORBA.Object fragTestStub = getStub(orb);
        IOR ior = orb.getIOR( fragTestStub, false ) ;
        ObjectKey objectKey = getObjectKeyObject(orb, ior);
        objectKey = modifyObjectKeySCID(objectKey,
                                        ORBConstants.PERSISTENT_SCID, orb);

        // Modify to use old magic.
        for (int magic = ObjectKeyFactoryImpl.MAGIC_BASE;
             magic <= ObjectKeyFactoryImpl.MAX_MAGIC; magic++) {

            System.out.println("magic: " + magic);
            objectKey = modifyObjectKeyMagic(objectKey, magic, orb);
            //ORBUtility.intToBytes(magic, objectKey, MAGIC_OFFSET);

            // construct a new IOR
            String typeId = ior.getTypeId();
            IIOPProfileTemplate iptemp = 
                (IIOPProfileTemplate)ior.getProfile().getTaggedProfileTemplate() ;
            IIOPAddress addr = iptemp.getPrimaryAddress() ;

            ObjectKey objKey = orb.getObjectKeyFactory().
                                      create(objectKey.getBytes(orb));

            IIOPProfileTemplate iproftemp =
                IIOPFactories.makeIIOPProfileTemplate(orb, GIOPVersion.V1_1, addr);
            IORTemplate iortemp = IORFactories.makeIORTemplate(objKey.getTemplate()) ;
            iortemp.add( iproftemp ) ;

            IOR newIor = iortemp.makeIOR(orb, typeId, objKey.getId() ) ;

            // create locate request message
            GIOPVersion requestVersion =
                GIOPVersion.chooseRequestVersion(orb, newIor);
            byte encodingVersion = 
                ORBUtility.chooseEncodingVersion(orb, newIor,
                                                 requestVersion);
            System.out.println("GIOP[major, minor]: " + requestVersion.getMajor() +
                               ", " + requestVersion.getMinor());
            LocateRequestMessage msg =
                MessageBase.createLocateRequest(
                    orb, requestVersion, encodingVersion,
                    REQUEST_ID, objectKey.getBytes(orb));


            int strategy = requestVersion.lessThan(GIOPVersion.V1_2) ? 0 : 1;
            MessageMediator messageMediator =
                beginRequest(orb, fragTestStub, msg, strategy);

            org.omg.CORBA.portable.OutputStream os = 
                (org.omg.CORBA.portable.OutputStream)
                messageMediator.getOutputObject();
            msg.write(os);
            messageMediator.finishSendingRequest();
            messageMediator.waitForResponse();

            switch(messageMediator.getLocateReplyHeader().getReplyStatus()) {
            case LocateReplyMessage.UNKNOWN_OBJECT :
                System.out.println("Target object is unknown");
                break;
            case LocateReplyMessage.OBJECT_FORWARD :
            case LocateReplyMessage.OBJECT_FORWARD_PERM :
                System.out.println("Location forward received");
                break;
            case LocateReplyMessage.OBJECT_HERE :
                System.out.println("Target object is available");
                break;
            default:
                System.out.println("Locate reply status is invalid");
                break;
            }
        }
    }
    
    public static void runFragmentedReply(ORB orb) 
    {
        FragmentTester fragTestRef = (FragmentTester) getStub(orb);

        try {
            fragTestRef.testFragmentedReply(false);
        } catch (Throwable t) {
            RuntimeException err = new RuntimeException( 
                "Excepion in fragmented reply test" );   
            err.initCause( t ) ;
            throw err ;
        }
    }    

    ////////////////////////////////////////////////////
    //
    // Utilities
    //

    static org.omg.CORBA.Object getStub(ORB orb)
    {
        org.omg.CORBA.Object obj = readObjref("IOR", orb);
        return (org.omg.CORBA.Object)PortableRemoteObject.narrow(obj, 
            FragmentTester.class);
    }

    static byte[] getObjectKey(ORB orb, IOR ior)
    {
        return ior.getProfile().getObjectKey().getBytes(orb);
    }

    static ObjectKey getObjectKeyObject(ORB orb, IOR ior)
    {
        return ior.getProfile().getObjectKey();
    }

    static ObjectKey modifyObjectKeySCID(ObjectKey objectKey,
                                         int scid, ORB orb) {
        ObjectKeyTemplate okTemp = objectKey.getTemplate();
        int serverId = okTemp.getServerId();
        String orbId = okTemp.getORBId();
        ObjectAdapterId objectAdapterId = okTemp.getObjectAdapterId();
        ObjectKeyTemplate newOkTemp = 
            new POAObjectKeyTemplate(orb, scid, serverId, orbId, 
                                     objectAdapterId);
        return new ObjectKeyImpl(newOkTemp, objectKey.getId());
    }

    static ObjectKey modifyObjectKeyMagic(ObjectKey objectKey,
                                          int magic, ORB orb) {
        ObjectKeyTemplate okTemp = objectKey.getTemplate();
        int serverId = okTemp.getServerId();
        int scid = okTemp.getSubcontractId();
        String orbId = okTemp.getORBId();
        ObjectAdapterId objectAdapterId = okTemp.getObjectAdapterId();
        ObjectKeyTemplate newOkTemp;
        if (magic >= ObjectKeyFactoryImpl.JAVAMAGIC_NEWER) {
            newOkTemp = new POAObjectKeyTemplate(orb, scid, serverId, orbId, 
                                                 objectAdapterId);
        } else {
            newOkTemp = new OldPOAObjectKeyTemplate(orb, magic, scid,
                                                    serverId,
                                                    1, // orbId
                                                    1); // poaId
        }
        return new ObjectKeyImpl(newOkTemp, objectKey.getId());
    }

    static void modifyObjectKey(byte[] objectKey)
    {
        // modify the object key so as to force BadServerIdHandler
        // to be invoked, instead of the usual upcall dispatch
        System.out.println("-> objectkey.length : " + objectKey.length);
        ORBUtility.intToBytes(ORBConstants.PERSISTENT_SCID, objectKey,
                              SCID_OFFSET);
    }

    static MessageMediator beginRequest(ORB orb,
        org.omg.CORBA.Object stub, Message msg)
    {
        return beginRequest(orb, stub, msg, -1);
    }

    static MessageMediator beginRequest(ORB orb,
        org.omg.CORBA.Object stub, Message msg, int strategy)
    {
        ClientDelegate delegate = (ClientDelegate)
            StubAdapter.getDelegate(stub) ;
        Iterator iterator = delegate.getContactInfoList().iterator();
        ContactInfo contactInfo;
        if (iterator.hasNext()) {
            contactInfo = (ContactInfo) iterator.next();
        } else {
            throw new RuntimeException("no next");
        }
        Connection connection = (Connection)
            contactInfo.createConnection();
        connection.setConnectionCache(new DummyConnectionCache());
        orb.getTransportManager().getSelector(0)
            .registerForEvent(connection.getEventHandler());
        connection.setState("ESTABLISHED");
        MessageMediator messageMediator = (MessageMediator)
            contactInfo.createMessageMediator(
                orb, contactInfo, connection, "locate message", false);
        CDROutputObject outputObject = null;
        if (strategy == -1) {
            outputObject =
                new CDROutputObject(orb, messageMediator, msg,
                                    messageMediator.getStreamFormatVersion());
        } else {
            outputObject =
                new CDROutputObject(orb, messageMediator, msg,
                                    messageMediator.getStreamFormatVersion(),
                                    strategy);
        }
        messageMediator.setOutputObject(outputObject);
        connection.registerWaiter(messageMediator);
        return messageMediator;
    }

    // All this, just to get a connection.
    static LocateRequestMessage getLocateRequestMessage(ORB orb, IOR ior)
    {
        byte[] objectKey = getObjectKey(orb, ior);
        GIOPVersion gv = GIOPVersion.chooseRequestVersion(orb, ior);
        byte encodingVersion = ORBUtility.chooseEncodingVersion(orb, ior, gv);
        LocateRequestMessage msg = 
            MessageBase.createLocateRequest(
                orb, gv, encodingVersion, REQUEST_ID, objectKey);
        return msg;
    }

    // size must be divisible by four
    public static void testByteArray(FragmentTester tester, int size)
        throws RemoteException, BadArrayException
    {
        System.out.println("Sending array of length " + size);

        byte array[] = new byte[size];

        int i = 0;

        do {

            for (byte x = 0; x < 4; x++) {
                //System.out.print("" + x + " ");
                array[i++] = x;
            }
            // System.out.println();

        } while (i < size);

        byte result[] = tester.verifyTransmission(array);

        if (result == null)
            throw new BadArrayException("result was null!");

        if (array.length != result.length)
            throw new BadArrayException("result length incorrect: " + result.length);

        for (i = 0; i < array.length; i++)
            if (array[i] != result[i])
                throw new BadArrayException("result mismatch at index: " + i);

        System.out.println("testByteArray completed normally");
    }

    public static org.omg.CORBA.Object readObjref(String file, org.omg.CORBA.ORB orb) 
    {
        String fil = System.getProperty("output.dir")+System.getProperty("file.separator")+file;
        try {
            java.io.DataInputStream in =
                new java.io.DataInputStream(new FileInputStream(fil));
            String ior = in.readLine();
            System.out.println("IOR: "+ior);
            return orb.string_to_object(ior);
        } catch (java.io.IOException e) {
            System.err.println("Unable to open file "+fil);
            System.exit(1);
        }
        return null;
    }

    ////////////////////////////////////////////////////
    //    
    // ORBInitializer interface implementation.
    //

    public void pre_init(ORBInitInfo info) 
    {
    }

    public void post_init(ORBInitInfo info) 
    {
        // register the interceptors.
        try {
            info.add_client_request_interceptor(this);
        } catch (org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName e) {
            throw new INTERNAL();
        }
        System.out.println("ORBInitializer.post_init completed");
    }

    ////////////////////////////////////////////////////
    //
    // implementation of the Interceptor interface.
    //

    public String name() 
    {
        return "ClientInterceptor";
    }

    public void destroy() 
    {
    }

    ////////////////////////////////////////////////////
    //    
    // implementation of the ClientInterceptor interface.
    //

    private static int interceptorInvocationCount = 0;

    public void send_request(ClientRequestInfo ri) throws ForwardRequest 
    {
        if (interceptorInvocationCount == 0 ||
                interceptorInvocationCount == 2) {
            interceptorInvocationCount++;
        }   
        System.out.println("send_request called : " + ri.operation());        
    }

    public void send_poll(ClientRequestInfo ri) 
    {
        System.out.println("send_poll called : " + ri.operation());
    }

    public void receive_reply(ClientRequestInfo ri) 
    {
        if (interceptorInvocationCount == 3) {
            interceptorInvocationCount++;        
        }        
        System.out.println("receive_reply called : " + ri.operation());
    }

    public void receive_exception(ClientRequestInfo ri) throws ForwardRequest 
    {
        System.out.println("receive_exception called : " + ri.operation());
    }

    public void receive_other(ClientRequestInfo ri) throws ForwardRequest 
    {
        if (interceptorInvocationCount == 1) {
            interceptorInvocationCount++;        
        }
        System.out.println("receive_other called : " + ri.operation());
    }
}

class DummyConnectionCache
    implements ConnectionCache
{
    public String getCacheType() { return null; }
    public void stampTime(Connection connection) {}
    public long numberOfConnections() { return 0; }
    public long numberOfIdleConnections() { return 0; }
    public long numberOfBusyConnections() { return 0; }
    public boolean reclaim() { return true; }
    public void close() {}

    public String getMonitoringName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

// End of file.

