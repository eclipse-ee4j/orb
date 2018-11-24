/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.orb;

import java.net.SocketException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.ServerSocket;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedActionException;

import javax.management.ObjectName;

import org.omg.PortableInterceptor.ORBInitializer;
import org.omg.PortableInterceptor.ORBInitInfo;

import com.sun.corba.ee.impl.encoding.CDROutputObject;
import com.sun.corba.ee.spi.protocol.MessageMediator;
import com.sun.corba.ee.spi.transport.ContactInfo;
import com.sun.corba.ee.spi.transport.EventHandler;

import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.ior.IORTemplate;
import com.sun.corba.ee.spi.ior.ObjectKey;
import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.orb.Operation;
import com.sun.corba.ee.spi.orb.OperationFactory;
import com.sun.corba.ee.spi.orb.OperationFactoryExt;
import com.sun.corba.ee.spi.orb.ParserData;
import com.sun.corba.ee.spi.orb.ParserDataFactory;
import com.sun.corba.ee.spi.transport.Acceptor;
import com.sun.corba.ee.spi.transport.ContactInfoList;
import com.sun.corba.ee.spi.transport.ContactInfoListFactory;
import com.sun.corba.ee.spi.transport.IORToSocketInfo;
import com.sun.corba.ee.spi.transport.IIOPPrimaryToContactInfo;
import com.sun.corba.ee.spi.transport.SocketInfo;
import com.sun.corba.ee.spi.transport.TcpTimeouts;
import com.sun.corba.ee.impl.oa.poa.Policies;

import com.sun.corba.ee.impl.encoding.CodeSetComponentInfo;
import com.sun.corba.ee.impl.encoding.OSFCodeSetRegistry;
import com.sun.corba.ee.impl.legacy.connection.USLPort;
import com.sun.corba.ee.spi.logging.ORBUtilSystemException;
import com.sun.corba.ee.impl.oa.poa.BadServerIdHandler;
import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.KeyAddr;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.ProfileAddr;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.ReferenceAddr;
import com.sun.corba.ee.impl.transport.DefaultIORToSocketInfoImpl;
import com.sun.corba.ee.impl.transport.DefaultSocketFactoryImpl;
import com.sun.corba.ee.impl.transport.TcpTimeoutsImpl;
import com.sun.corba.ee.spi.transport.InboundConnectionCache;
import org.glassfish.pfl.basic.contain.Pair;
import org.glassfish.pfl.basic.func.UnaryFunction;

/**
 * Initialize the parser data for the standard ORB parser. This is used both to implement ORBDataParserImpl and to
 * provide the basic testing framework for ORBDataParserImpl.
 */
public class ParserTable {
    private static final ORBUtilSystemException wrapper = ORBUtilSystemException.self;

    // There is a serious problem here with the DefaultSocketFactory.
    // It is NOT immutable: in particular is has a setORB method, so instances
    // of DefaultSocketFactoryImpl CANNOT be shared across ORBs.
    // To clean this up, we'll simply create a new ParserTable for each call to
    // get.
    private static String MY_CLASS_NAME = ParserTable.class.getName();

    // private static final ParserTable myInstance = new ParserTable() ;

    private Operation classAction;

    public static ParserTable get(UnaryFunction<String, Class<?>> cnr) {
        // return myInstance ;
        return new ParserTable(cnr);
    }

    private ParserData[] parserData;

    public ParserData[] getParserData() {
        return parserData;
    }

    public static ObjectName testGmbalRootParentName;

    public final static String TEST_GMBAL_ROOT_PARENT_NAME = "test:pp=\"/\",type=\"Foo\",name=\"1\"";

    static {
        try {
            testGmbalRootParentName = new ObjectName(TEST_GMBAL_ROOT_PARENT_NAME);
        } catch (Exception exc) {
            testGmbalRootParentName = null;
        }
    }

    private ParserTable(UnaryFunction<String, Class<?>> cnr) {
        classAction = OperationFactory.classAction(cnr);

        String codeSetTestString = OSFCodeSetRegistry.ISO_8859_1_VALUE + "," + OSFCodeSetRegistry.UTF_16_VALUE + "," + OSFCodeSetRegistry.ISO_646_VALUE;

        String[] debugTestData = { "subcontract", "poa", "transport" };

        USLPort[] USLPorts = { new USLPort("FOO", 2701), new USLPort("BAR", 3333) };

        TcpTimeouts testTcpTimeouts = TcpTimeouts.factory.create(2000, 6000, 20);

        TcpTimeouts defaultTcpTimeouts = TcpTimeouts.factory.create(ORBConstants.TRANSPORT_TCP_INITIAL_TIME_TO_WAIT,
                ORBConstants.TRANSPORT_TCP_MAX_TIME_TO_WAIT, ORBConstants.TRANSPORT_TCP_BACKOFF_FACTOR);

        TcpTimeouts testTcpConnectTimeouts = TcpTimeouts.factory.create(20, 60000, 100, 5000);

        TcpTimeouts defaultTcpConnectTimeouts = TcpTimeouts.factory.create(ORBConstants.TRANSPORT_TCP_CONNECT_INITIAL_TIME_TO_WAIT,
                ORBConstants.TRANSPORT_TCP_CONNECT_MAX_TIME_TO_WAIT, ORBConstants.TRANSPORT_TCP_CONNECT_BACKOFF_FACTOR,
                ORBConstants.TRANSPORT_TCP_CONNECT_MAX_SINGLE_WAIT);

        ORBInitializer[] TestORBInitializers = { null, new TestORBInitializer1(), new TestORBInitializer2() };
        Pair[] TestORBInitData = { new Pair<String, String>("foo.bar.blech.NonExistent", "dummy"),
                new Pair<String, String>(MY_CLASS_NAME + "$TestORBInitializer1", "dummy"),
                new Pair<String, String>(MY_CLASS_NAME + "$TestORBInitializer2", "dummy") };

        Acceptor[] TestAcceptors = { null, new TestAcceptor2(), new TestAcceptor1() };
        // REVISIT: The test data gets put into a Properties object where
        // order is not guaranteed. Thus the above array is in reverse.
        Pair[] TestAcceptorData = { new Pair<String, String>("foo.bar.blech.NonExistent", "dummy"),
                new Pair<String, String>(MY_CLASS_NAME + "$TestAcceptor1", "dummy"), new Pair<String, String>(MY_CLASS_NAME + "$TestAcceptor2", "dummy") };

        Pair[] TestORBInitRefData = { new Pair<String, String>("Foo", "ior:930492049394"), new Pair<String, String>("Bar", "ior:3453465785633576") };

        // Why are we not handling INITIAL_SERVICES?
        // URL testServicesURL = null ;
        // String testServicesString = "corbaloc::camelot/NameService" ;
        // try {
        // testServicesURL = new URL( testServicesString ) ;
        // } catch (Exception exc) {
        // }

        // propertyName,
        // operation,
        // fieldName, defaultValue,
        // testValue, testData (string or Pair[])
        ParserData[] pd = {
                ParserDataFactory.make(ORBConstants.DEBUG_PROPERTY, OperationFactory.listAction(",", OperationFactory.stringAction()), "debugFlags",
                        new String[0], debugTestData, "subcontract,poa,transport"),
                ParserDataFactory.make(ORBConstants.INITIAL_HOST_PROPERTY, OperationFactory.stringAction(), "ORBInitialHost", "", "Foo", "Foo"),
                ParserDataFactory.make(ORBConstants.INITIAL_PORT_PROPERTY, OperationFactory.integerAction(), "ORBInitialPort",
                        Integer.valueOf(ORBConstants.DEFAULT_INITIAL_PORT), Integer.valueOf(27314), "27314"),
                // Where did this come from?
                // ParserDataFactory.make( ORBConstants.INITIAL_PORT_PROPERTY,
                // OperationFactory.booleanAction(),
                // "ORBInitialPortInitialized", Boolean.FALSE,
                // Boolean.TRUE, "27314" ),
                ParserDataFactory.make(ORBConstants.SERVER_HOST_PROPERTY, OperationFactory.stringAction(), "ORBServerHost", "", "camelot", "camelot"),
                ParserDataFactory.make(ORBConstants.SERVER_PORT_PROPERTY, OperationFactory.integerAction(), "ORBServerPort", Integer.valueOf(0),
                        Integer.valueOf(38143), "38143"),
                ParserDataFactory.make(ORBConstants.ORB_ID_PROPERTY, OperationFactory.stringAction(), "orbId", "", "foo", "foo"),
                ParserDataFactory.make(ORBConstants.OLD_ORB_ID_PROPERTY, OperationFactory.stringAction(), "orbId", "", "foo", "foo"),
                ParserDataFactory.make(ORBConstants.ORB_SERVER_ID_PROPERTY, OperationFactory.integerAction(), "persistentServerId", Integer.valueOf(-1),
                        Integer.valueOf(1234), "1234"),
                ParserDataFactory.make(ORBConstants.ORB_SERVER_ID_PROPERTY, OperationFactory.setFlagAction(), "persistentServerIdInitialized", Boolean.FALSE,
                        Boolean.TRUE, "1234"),
                // REVISIT after switch
                // ParserDataFactory.make( ORBConstants.INITIAL_SERVICES_PROPERTY,
                // OperationFactory.URLAction(),
                // "servicesUrl", null,
                // testServicesURL, testServicesString ),
                // ParserDataFactory.make( ORBConstants.DEFAULT_INIT_REF_PROPERTY,
                // OperationFactory.stringAction(),
                // "defaultInitRef", null,
                // "Fooref", "Fooref" ),
                ParserDataFactory.make(ORBConstants.HIGH_WATER_MARK_PROPERTY, OperationFactory.integerAction(), "highWaterMark", Integer.valueOf(240),
                        Integer.valueOf(3745), "3745"),
                ParserDataFactory.make(ORBConstants.LOW_WATER_MARK_PROPERTY, OperationFactory.integerAction(), "lowWaterMark", Integer.valueOf(100),
                        Integer.valueOf(12), "12"),
                ParserDataFactory.make(ORBConstants.NUMBER_TO_RECLAIM_PROPERTY, OperationFactory.integerAction(), "numberToReclaim", Integer.valueOf(5),
                        Integer.valueOf(231), "231"),
                ParserDataFactory.make(ORBConstants.GIOP_VERSION, makeGVOperation(), "giopVersion", GIOPVersion.DEFAULT_VERSION, new GIOPVersion(2, 3), "2.3"),
                ParserDataFactory.make(ORBConstants.GIOP_FRAGMENT_SIZE, makeFSOperation(), "giopFragmentSize",
                        Integer.valueOf(ORBConstants.GIOP_DEFAULT_FRAGMENT_SIZE), Integer.valueOf(65536), "65536"),
                ParserDataFactory.make(ORBConstants.GIOP_BUFFER_SIZE, OperationFactory.integerAction(), "giopBufferSize",
                        Integer.valueOf(ORBConstants.GIOP_DEFAULT_BUFFER_SIZE), Integer.valueOf(234000), "234000"),
                ParserDataFactory.make(ORBConstants.GIOP_11_BUFFMGR, makeBMGROperation(), "giop11BuffMgr",
                        Integer.valueOf(ORBConstants.DEFAULT_GIOP_11_BUFFMGR), Integer.valueOf(1), "CLCT"),
                ParserDataFactory.make(ORBConstants.GIOP_12_BUFFMGR, makeBMGROperation(), "giop12BuffMgr",
                        Integer.valueOf(ORBConstants.DEFAULT_GIOP_12_BUFFMGR), Integer.valueOf(0), "GROW"),

                // Note that the same property is used to set two different
                // fields here. This requires that both entries use the same test
                // data, or the test will fail.
                ParserDataFactory.make(ORBConstants.GIOP_TARGET_ADDRESSING,
                        OperationFactory.compose(OperationFactory.integerRangeAction(0, 3), OperationFactory.convertIntegerToShort()),
                        "giopTargetAddressPreference", Short.valueOf(ORBConstants.ADDR_DISP_HANDLE_ALL), Short.valueOf((short) 2), "2"),
                ParserDataFactory.make(ORBConstants.GIOP_TARGET_ADDRESSING, makeADOperation(), "giopAddressDisposition", Short.valueOf(KeyAddr.value),
                        Short.valueOf((short) 2), "2"),
                ParserDataFactory.make(ORBConstants.ALWAYS_SEND_CODESET_CTX_PROPERTY, OperationFactory.booleanAction(), "alwaysSendCodeSetCtx", Boolean.TRUE,
                        Boolean.FALSE, "false"),
                ParserDataFactory.make(ORBConstants.USE_BOMS, OperationFactory.booleanAction(), "useByteOrderMarkers",
                        Boolean.valueOf(ORBConstants.DEFAULT_USE_BYTE_ORDER_MARKERS), Boolean.FALSE, "false"),
                ParserDataFactory.make(ORBConstants.USE_BOMS_IN_ENCAPS, OperationFactory.booleanAction(), "useByteOrderMarkersInEncaps",
                        Boolean.valueOf(ORBConstants.DEFAULT_USE_BYTE_ORDER_MARKERS_IN_ENCAPS), Boolean.FALSE, "false"),
                ParserDataFactory.make(ORBConstants.CHAR_CODESETS, makeCSOperation(), "charData",
                        CodeSetComponentInfo.JAVASOFT_DEFAULT_CODESETS.getCharComponent(), CodeSetComponentInfo.createFromString(codeSetTestString),
                        codeSetTestString),
                ParserDataFactory.make(ORBConstants.WCHAR_CODESETS, makeCSOperation(), "wcharData",
                        CodeSetComponentInfo.JAVASOFT_DEFAULT_CODESETS.getWCharComponent(), CodeSetComponentInfo.createFromString(codeSetTestString),
                        codeSetTestString),
                ParserDataFactory.make(ORBConstants.ALLOW_LOCAL_OPTIMIZATION, OperationFactory.booleanAction(), "allowLocalOptimization", Boolean.FALSE,
                        Boolean.TRUE, "true"),
                ParserDataFactory.make(ORBConstants.LEGACY_SOCKET_FACTORY_CLASS_PROPERTY, makeLegacySocketFactoryOperation(),
                        // No default - must be set by user if they are using
                        // legacy socket factory.
                        "legacySocketFactory", null, new TestLegacyORBSocketFactory(), MY_CLASS_NAME + "$TestLegacyORBSocketFactory"),
                ParserDataFactory.make(ORBConstants.SOCKET_FACTORY_CLASS_PROPERTY, makeSocketFactoryOperation(), "socketFactory",
                        new DefaultSocketFactoryImpl(), new TestORBSocketFactory(), MY_CLASS_NAME + "$TestORBSocketFactory"),
                ParserDataFactory.make(ORBConstants.LISTEN_SOCKET_PROPERTY, makeUSLOperation(), "userSpecifiedListenPorts", new USLPort[0], USLPorts,
                        "FOO:2701,BAR:3333"),
                ParserDataFactory.make(ORBConstants.IOR_TO_SOCKET_INFO_CLASS_PROPERTY, makeIORToSocketInfoOperation(), "iorToSocketInfo",
                        new DefaultIORToSocketInfoImpl(), new TestIORToSocketInfo(), MY_CLASS_NAME + "$TestIORToSocketInfo"),
                ParserDataFactory.make(ORBConstants.IIOP_PRIMARY_TO_CONTACT_INFO_CLASS_PROPERTY, makeIIOPPrimaryToContactInfoOperation(),
                        "iiopPrimaryToContactInfo", null, new TestIIOPPrimaryToContactInfo(), MY_CLASS_NAME + "$TestIIOPPrimaryToContactInfo"),
                ParserDataFactory.make(ORBConstants.CONTACT_INFO_LIST_FACTORY_CLASS_PROPERTY, makeContactInfoListFactoryOperation(),
                        "corbaContactInfoListFactory", null, new TestContactInfoListFactory(), MY_CLASS_NAME + "$TestContactInfoListFactory"),
                ParserDataFactory.make(ORBConstants.PERSISTENT_SERVER_PORT_PROPERTY, OperationFactory.integerAction(), "persistentServerPort",
                        Integer.valueOf(0), Integer.valueOf(2743), "2743"),
                ParserDataFactory.make(ORBConstants.PERSISTENT_SERVER_PORT_PROPERTY, OperationFactory.setFlagAction(), "persistentPortInitialized",
                        Boolean.FALSE, Boolean.TRUE, "2743"),
                ParserDataFactory.make(ORBConstants.ACTIVATED_PROPERTY, OperationFactory.booleanAction(), "serverIsORBActivated", Boolean.FALSE, Boolean.TRUE,
                        "true"),
                ParserDataFactory.make(ORBConstants.BAD_SERVER_ID_HANDLER_CLASS_PROPERTY, classAction, "badServerIdHandlerClass", null,
                        TestBadServerIdHandler.class, MY_CLASS_NAME + "$TestBadServerIdHandler"),
                ParserDataFactory.make(ORBConstants.PI_ORB_INITIALIZER_CLASS_PREFIX, makeROIOperation(), "orbInitializers", new ORBInitializer[0],
                        TestORBInitializers, TestORBInitData, ORBInitializer.class),
                ParserDataFactory.make(ORBConstants.ACCEPTOR_CLASS_PREFIX_PROPERTY, makeAcceptorInstantiationOperation(), "acceptors", new Acceptor[0],
                        TestAcceptors, TestAcceptorData, Acceptor.class),

                //
                // Socket/Channel control
                //

                // Acceptor:
                // useNIOSelector == true
                // useSelectThreadToWait = true
                // useWorkerThreadForEvent = false
                // else
                // useSelectThreadToWait = false
                // useWorkerThreadForEvent = true

                // Connection:
                // useNIOSelector == true
                // useSelectThreadToWait = true
                // useWorkerThreadForEvent = true
                // else
                // useSelectThreadToWait = false
                // useWorkerThreadForEvent = true

                ParserDataFactory.make(ORBConstants.ACCEPTOR_SOCKET_TYPE_PROPERTY, OperationFactory.stringAction(), "acceptorSocketType",
                        ORBConstants.SOCKETCHANNEL, "foo", "foo"),

                ParserDataFactory.make(ORBConstants.USE_NIO_SELECT_TO_WAIT_PROPERTY, OperationFactory.booleanAction(), "acceptorSocketUseSelectThreadToWait",
                        Boolean.TRUE, Boolean.TRUE, "true"),
                ParserDataFactory.make(ORBConstants.ACCEPTOR_SOCKET_USE_WORKER_THREAD_FOR_EVENT_PROPERTY, OperationFactory.booleanAction(),
                        "acceptorSocketUseWorkerThreadForEvent", Boolean.TRUE, Boolean.TRUE, "true"),
                ParserDataFactory.make(ORBConstants.CONNECTION_SOCKET_TYPE_PROPERTY, OperationFactory.stringAction(), "connectionSocketType",
                        ORBConstants.SOCKETCHANNEL, "foo", "foo"),
                ParserDataFactory.make(ORBConstants.USE_NIO_SELECT_TO_WAIT_PROPERTY, OperationFactory.booleanAction(), "connectionSocketUseSelectThreadToWait",
                        Boolean.TRUE, Boolean.TRUE, "true"),
                ParserDataFactory.make(ORBConstants.CONNECTION_SOCKET_USE_WORKER_THREAD_FOR_EVENT_PROPERTY, OperationFactory.booleanAction(),
                        "connectionSocketUseWorkerThreadForEvent", Boolean.TRUE, Boolean.TRUE, "true"),
                ParserDataFactory.make(ORBConstants.WAIT_FOR_RESPONSE_TIMEOUT, OperationFactory.integerAction(), "waitForResponseTimeout", 1000 * 60 * 30,
                        Integer.valueOf(1234), "1234"),
                ParserDataFactory.make(ORBConstants.DISABLE_DIRECT_BYTE_BUFFER_USE_PROPERTY, OperationFactory.booleanAction(), "disableDirectByteBufferUse",
                        Boolean.TRUE, // was Boolean.FALSE,
                        Boolean.TRUE, "true"),
                ParserDataFactory.make(ORBConstants.TRANSPORT_TCP_TIMEOUTS_PROPERTY, OperationFactoryExt.convertAction(TcpTimeoutsImpl.class), "tcpTimeouts",
                        defaultTcpTimeouts, testTcpTimeouts, "2000:6000:20"),
                ParserDataFactory.make(ORBConstants.TRANSPORT_TCP_CONNECT_TIMEOUTS_PROPERTY, OperationFactoryExt.convertAction(TcpTimeoutsImpl.class),
                        "tcpConnectTimeouts", defaultTcpConnectTimeouts, testTcpConnectTimeouts, "20:60000:100:5000"),
                ParserDataFactory.make(ORBConstants.ENABLE_JAVA_SERIALIZATION_PROPERTY, OperationFactory.booleanAction(), "enableJavaSerialization",
                        Boolean.FALSE, Boolean.FALSE, "false"),
                ParserDataFactory.make(ORBConstants.USE_REP_ID, OperationFactory.booleanAction(), "useRepId", Boolean.TRUE, Boolean.TRUE, "true"),
                ParserDataFactory.make(ORBConstants.ORB_INIT_REF_PROPERTY, OperationFactory.identityAction(), "orbInitialReferences", new Pair[0],
                        TestORBInitRefData, TestORBInitRefData, Pair.class),
                ParserDataFactory.make(ORBConstants.SHOW_INFO_MESSAGES, OperationFactory.booleanAction(), "showInfoMessages", Boolean.FALSE, Boolean.FALSE,
                        "false"),
                ParserDataFactory.make(ORBConstants.GET_SERVICE_CONTEXT_RETURNS_NULL, OperationFactory.booleanAction(), "getServiceContextReturnsNull",
                        Boolean.FALSE, Boolean.FALSE, "false"),
                ParserDataFactory.make(ORBConstants.APPSERVER_MODE, OperationFactory.booleanAction(), "isAppServerMode", Boolean.FALSE, Boolean.FALSE, "FALSE"),
                ParserDataFactory.make(ORBConstants.READ_BYTE_BUFFER_SIZE_PROPERTY, OperationFactory.integerAction(), "readByteBufferSize",
                        Integer.valueOf(ORBConstants.DEFAULT_READ_BYTE_BUFFER_SIZE), Integer.valueOf(8192), "8192"),
                ParserDataFactory.make(ORBConstants.MAX_READ_BYTE_BUFFER_SIZE_THRESHOLD_PROPERTY, OperationFactory.integerAction(),
                        "maxReadByteBufferSizeThreshold", Integer.valueOf(ORBConstants.MAX_READ_BYTE_BUFFER_SIZE_THRESHOLD), Integer.valueOf(4000000),
                        "4000000"),
                ParserDataFactory.make(ORBConstants.POOLED_DIRECT_BYTE_BUFFER_SLAB_SIZE_PROPERTY, OperationFactory.integerAction(),
                        "pooledDirectByteBufferSlabSize", Integer.valueOf(ORBConstants.DEFAULT_POOLED_DIRECT_BYTE_BUFFER_SLAB_SIZE), Integer.valueOf(1048576),
                        "1048576"),
                ParserDataFactory.make(ORBConstants.ALWAYS_ENTER_BLOCKING_READ_PROPERTY, OperationFactory.booleanAction(), "alwaysEnterBlockingRead",
                        Boolean.TRUE, Boolean.TRUE, "TRUE"),
                ParserDataFactory.make(ORBConstants.NON_BLOCKING_READ_CHECK_MESSAGE_PARSER_PROPERTY, OperationFactory.booleanAction(),
                        "nonBlockingReadCheckMessageParser", Boolean.TRUE, Boolean.TRUE, "TRUE"),
                ParserDataFactory.make(ORBConstants.BLOCKING_READ_CHECK_MESSAGE_PARSER_PROPERTY, OperationFactory.booleanAction(),
                        "blockingReadCheckMessageParser", Boolean.FALSE, Boolean.FALSE, "FALSE"),
                ParserDataFactory.make(ORBConstants.TIMING_POINTS_ENABLED, OperationFactory.booleanAction(), "timingPointsEnabled", Boolean.FALSE, Boolean.TRUE,
                        "TRUE"),
                ParserDataFactory.make(ORBConstants.USE_ENUM_DESC, OperationFactory.booleanAction(), "useEnumDesc", Boolean.TRUE, Boolean.TRUE, "TRUE"),
                ParserDataFactory.make(ORBConstants.ENV_IS_SERVER_PROPERTY, OperationFactory.booleanAction(), "environmentIsGFServer", Boolean.FALSE,
                        Boolean.TRUE, "TRUE"),
                ParserDataFactory.make(ORBConstants.NO_DEFAULT_ACCEPTORS, OperationFactory.booleanAction(), "noDefaultAcceptors", Boolean.FALSE, Boolean.TRUE,
                        "TRUE"),
                ParserDataFactory.make(ORBConstants.REGISTER_MBEANS, OperationFactory.booleanAction(), "registerMBeans", Boolean.TRUE, Boolean.FALSE, "FALSE"),
                ParserDataFactory.make(ORBConstants.FRAGMENT_READ_TIMEOUT, OperationFactory.integerAction(), "fragmentReadTimeout",
                        Integer.valueOf(ORBConstants.DEFAULT_FRAGMENT_READ_TIMEOUT), Integer.valueOf(23000), "23000"),
                ParserDataFactory.make(ORBConstants.DISABLE_ORBD_INIT_PROPERTY, OperationFactory.booleanAction(), "disableORBD", Boolean.FALSE, Boolean.TRUE,
                        "TRUE") };

        parserData = pd;
    }

    public static final class TestBadServerIdHandler implements BadServerIdHandler {
        @Override
        public boolean equals(Object other) {
            return other instanceof TestBadServerIdHandler;
        }

        public void handle(ObjectKey objectKey) {
        }

        @Override
        public int hashCode() {
            int hash = 3;
            return hash;
        }
    }

    private Operation makeUSLOperation() {
        Operation[] siop = { OperationFactory.stringAction(), OperationFactory.integerAction() };
        Operation op2 = OperationFactory.sequenceAction(":", siop);

        Operation uslop = new Operation() {
            public Object operate(Object value) {
                Object[] values = (Object[]) value;
                String type = (String) (values[0]);
                Integer port = (Integer) (values[1]);
                return new USLPort(type, port.intValue());
            }
        };

        Operation op3 = OperationFactory.compose(op2, uslop);
        Operation listenop = OperationFactory.listAction(",", op3);
        return listenop;
    }

    public static final class TestLegacyORBSocketFactory implements com.sun.corba.ee.spi.legacy.connection.ORBSocketFactory {
        public boolean equals(Object other) {
            return other instanceof TestLegacyORBSocketFactory;
        }

        public ServerSocket createServerSocket(String type, int port) {
            return null;
        }

        public SocketInfo getEndPointInfo(org.omg.CORBA.ORB orb, IOR ior, SocketInfo socketInfo) {
            return null;
        }

        public Socket createSocket(SocketInfo socketInfo) {
            return null;
        }
    }

    public static final class TestORBSocketFactory implements com.sun.corba.ee.spi.transport.ORBSocketFactory {
        public boolean equals(Object other) {
            return other instanceof TestORBSocketFactory;
        }

        public void setORB(ORB orb) {
        }

        public ServerSocket createServerSocket(String type, InetSocketAddress a) {
            return null;
        }

        public Socket createSocket(String type, InetSocketAddress a) {
            return null;
        }

        public void setAcceptedSocketOptions(Acceptor acceptor, ServerSocket serverSocket, Socket socket) throws SocketException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    public static final class TestIORToSocketInfo implements IORToSocketInfo {
        public boolean equals(Object other) {
            return other instanceof TestIORToSocketInfo;
        }

        public List getSocketInfo(IOR ior, List previous) {
            return null;
        }
    }

    public static final class TestIIOPPrimaryToContactInfo implements IIOPPrimaryToContactInfo {
        public void reset(ContactInfo primary) {
        }

        public boolean hasNext(ContactInfo primary, ContactInfo previous, List contactInfos) {
            return true;
        }

        public ContactInfo next(ContactInfo primary, ContactInfo previous, List contactInfos) {
            return null;
        }
    }

    public static final class TestContactInfoListFactory implements ContactInfoListFactory {
        public boolean equals(Object other) {
            return other instanceof TestContactInfoListFactory;
        }

        public void setORB(ORB orb) {
        }

        public ContactInfoList create(IOR ior) {
            return null;
        }
    }

    private Operation makeMapOperation(final Map map) {
        return new Operation() {
            public Object operate(Object value) {
                return map.get(value);
            }
        };
    }

    private Operation makeBMGROperation() {
        Map map = new HashMap();
        map.put("GROW", Integer.valueOf(0));
        map.put("CLCT", Integer.valueOf(1));
        map.put("STRM", Integer.valueOf(2));
        return makeMapOperation(map);
    }

    private Operation makeLegacySocketFactoryOperation() {
        Operation sfop = new Operation() {
            public Object operate(Object value) {
                String param = (String) value;

                try {
                    Class legacySocketFactoryClass = (Class) classAction.operate(param);
                    // For security reasons avoid creating an instance if
                    // this socket factory class is not one that would fail
                    // the class cast anyway.
                    if (com.sun.corba.ee.spi.legacy.connection.ORBSocketFactory.class.isAssignableFrom(legacySocketFactoryClass)) {
                        return legacySocketFactoryClass.newInstance();
                    } else {
                        throw wrapper.illegalSocketFactoryType(legacySocketFactoryClass.toString());
                    }
                } catch (Exception ex) {
                    // ClassNotFoundException, IllegalAccessException,
                    // InstantiationException, SecurityException or
                    // ClassCastException
                    throw wrapper.badCustomSocketFactory(ex, param);
                }
            }
        };

        return sfop;
    }

    private Operation makeSocketFactoryOperation() {
        Operation sfop = new Operation() {
            public Object operate(Object value) {
                String param = (String) value;

                try {
                    Class socketFactoryClass = (Class) classAction.operate(param);
                    // For security reasons avoid creating an instance if
                    // this socket factory class is not one that would fail
                    // the class cast anyway.
                    if (com.sun.corba.ee.spi.transport.ORBSocketFactory.class.isAssignableFrom(socketFactoryClass)) {
                        return socketFactoryClass.newInstance();
                    } else {
                        throw wrapper.illegalSocketFactoryType(socketFactoryClass.toString());
                    }
                } catch (Exception ex) {
                    // ClassNotFoundException, IllegalAccessException,
                    // InstantiationException, SecurityException or
                    // ClassCastException
                    throw wrapper.badCustomSocketFactory(ex, param);
                }
            }
        };

        return sfop;
    }

    private Operation makeIORToSocketInfoOperation() {
        Operation op = new Operation() {
            public Object operate(Object value) {
                String param = (String) value;

                try {
                    Class iorToSocketInfoClass = (Class) classAction.operate(param);
                    // For security reasons avoid creating an instance if
                    // this socket factory class is not one that would fail
                    // the class cast anyway.
                    if (IORToSocketInfo.class.isAssignableFrom(iorToSocketInfoClass)) {
                        return iorToSocketInfoClass.newInstance();
                    } else {
                        throw wrapper.illegalIorToSocketInfoType(iorToSocketInfoClass.toString());
                    }
                } catch (Exception ex) {
                    // ClassNotFoundException, IllegalAccessException,
                    // InstantiationException, SecurityException or
                    // ClassCastException
                    throw wrapper.badCustomIorToSocketInfo(ex, param);
                }
            }
        };

        return op;
    }

    private Operation makeIIOPPrimaryToContactInfoOperation() {
        Operation op = new Operation() {
            public Object operate(Object value) {
                String param = (String) value;

                try {
                    Class iiopPrimaryToContactInfoClass = (Class) classAction.operate(param);
                    // For security reasons avoid creating an instance if
                    // this socket factory class is not one that would fail
                    // the class cast anyway.
                    if (IIOPPrimaryToContactInfo.class.isAssignableFrom(iiopPrimaryToContactInfoClass)) {
                        return iiopPrimaryToContactInfoClass.newInstance();
                    } else {
                        throw wrapper.illegalIiopPrimaryToContactInfoType(iiopPrimaryToContactInfoClass.toString());
                    }
                } catch (Exception ex) {
                    // ClassNotFoundException, IllegalAccessException,
                    // InstantiationException, SecurityException or
                    // ClassCastException
                    throw wrapper.badCustomIiopPrimaryToContactInfo(ex, param);
                }
            }
        };

        return op;
    }

    private Operation makeContactInfoListFactoryOperation() {
        Operation op = new Operation() {
            public Object operate(Object value) {
                String param = (String) value;

                try {
                    Class contactInfoListFactoryClass = (Class) classAction.operate(param);
                    // For security reasons avoid creating an instance if
                    // this socket factory class is not one that would fail
                    // the class cast anyway.
                    if (ContactInfoListFactory.class.isAssignableFrom(contactInfoListFactoryClass)) {
                        return contactInfoListFactoryClass.newInstance();
                    } else {
                        throw wrapper.illegalContactInfoListFactoryType(contactInfoListFactoryClass.toString());
                    }
                } catch (Exception ex) {
                    // ClassNotFoundException, IllegalAccessException,
                    // InstantiationException, SecurityException or
                    // ClassCastException
                    throw wrapper.badContactInfoListFactory(ex, param);
                }
            }
        };

        return op;
    }

    private Operation makeCSOperation() {
        Operation csop = new Operation() {
            public Object operate(Object value) {
                String val = (String) value;
                return CodeSetComponentInfo.createFromString(val);
            }
        };

        return csop;
    }

    private Operation makeADOperation() {
        Operation admap = new Operation() {
            private Integer[] map = { Integer.valueOf(KeyAddr.value), Integer.valueOf(ProfileAddr.value), Integer.valueOf(ReferenceAddr.value),
                    Integer.valueOf(KeyAddr.value) };

            public Object operate(Object value) {
                int val = ((Integer) value).intValue();
                return map[val];
            }
        };

        Operation rangeop = OperationFactory.integerRangeAction(0, 3);
        Operation op1 = OperationFactory.compose(rangeop, admap);
        Operation result = OperationFactory.compose(op1, OperationFactory.convertIntegerToShort());
        return result;
    }

    private Operation makeFSOperation() {
        Operation fschecker = new Operation() {
            public Object operate(Object value) {
                int giopFragmentSize = ((Integer) value).intValue();
                if (giopFragmentSize < ORBConstants.GIOP_FRAGMENT_MINIMUM_SIZE) {
                    throw wrapper.fragmentSizeMinimum(giopFragmentSize, ORBConstants.GIOP_FRAGMENT_MINIMUM_SIZE);
                }

                if (giopFragmentSize % ORBConstants.GIOP_FRAGMENT_DIVISOR != 0)
                    throw wrapper.fragmentSizeDiv(giopFragmentSize, ORBConstants.GIOP_FRAGMENT_DIVISOR);

                return value;
            }
        };

        Operation result = OperationFactory.compose(OperationFactory.integerAction(), fschecker);
        return result;
    }

    private Operation makeGVOperation() {
        Operation gvHelper = OperationFactory.listAction(".", OperationFactory.integerAction());
        Operation gvMain = new Operation() {
            public Object operate(Object value) {
                Object[] nums = (Object[]) value;
                int major = ((Integer) (nums[0])).intValue();
                int minor = ((Integer) (nums[1])).intValue();

                return new GIOPVersion(major, minor);
            }
        };

        Operation result = OperationFactory.compose(gvHelper, gvMain);
        return result;
    }

    public static final class TestORBInitializer1 extends org.omg.CORBA.LocalObject implements ORBInitializer {
        public boolean equals(Object other) {
            return other instanceof TestORBInitializer1;
        }

        public void pre_init(ORBInitInfo info) {
        }

        public void post_init(ORBInitInfo info) {
        }
    }

    public static final class TestORBInitializer2 extends org.omg.CORBA.LocalObject implements ORBInitializer {
        public boolean equals(Object other) {
            return other instanceof TestORBInitializer2;
        }

        public void pre_init(ORBInitInfo info) {
        }

        public void post_init(ORBInitInfo info) {
        }
    }

    private Operation makeROIOperation() {
        Operation indexOp = OperationFactory.suffixAction();
        Operation op1 = OperationFactory.compose(indexOp, classAction);
        Operation mop = OperationFactory.maskErrorAction(op1);

        Operation mkinst = new Operation() {
            public Object operate(Object value) {
                final Class initClass = (Class) value;
                if (initClass == null)
                    return null;

                // For security reasons avoid creating an instance
                // if this class is one that would fail the class cast
                // to ORBInitializer anyway.
                if (org.omg.PortableInterceptor.ORBInitializer.class.isAssignableFrom(initClass)) {
                    // Now that we have a class object, instantiate one and
                    // remember it:
                    ORBInitializer initializer = null;

                    try {
                        initializer = (ORBInitializer) AccessController.doPrivileged(new PrivilegedExceptionAction() {
                            public Object run() throws InstantiationException, IllegalAccessException {
                                return initClass.newInstance();
                            }
                        });
                    } catch (PrivilegedActionException exc) {
                        // Unwrap the exception, as we don't care exc here
                        throw wrapper.orbInitializerFailure(exc.getException(), initClass.getName());
                    } catch (Exception exc) {
                        throw wrapper.orbInitializerFailure(exc, initClass.getName());
                    }

                    return initializer;
                } else {
                    throw wrapper.orbInitializerType(initClass.getName());
                }
            }
        };

        Operation result = OperationFactory.compose(mop, mkinst);

        return result;
    }

    public static final class TestAcceptor1 implements Acceptor {
        public boolean equals(Object other) {
            return other instanceof TestAcceptor1;
        }

        public boolean initialize() {
            return true;
        }

        public boolean initialized() {
            return true;
        }

        public String getConnectionCacheType() {
            return "FOO";
        }

        public void setConnectionCache(InboundConnectionCache connectionCache) {
        }

        public InboundConnectionCache getConnectionCache() {
            return null;
        }

        public boolean shouldRegisterAcceptEvent() {
            return true;
        }

        public void setUseSelectThreadForConnections(boolean x) {
        }

        public boolean shouldUseSelectThreadForConnections() {
            return true;
        }

        public void setUseWorkerThreadForConnections(boolean x) {
        }

        public boolean shouldUseWorkerThreadForConnections() {
            return true;
        }

        public Socket getAcceptedSocket() {
            return null;
        }

        public void processSocket(Socket socket) {
        }

        public void close() {
        }

        public EventHandler getEventHandler() {
            return null;
        }

        public CDROutputObject createOutputObject(ORB broker, MessageMediator messageMediator) {
            return null;
        }

        public String getObjectAdapterId() {
            return null;
        }

        public String getObjectAdapterManagerId() {
            return null;
        }

        public void addToIORTemplate(IORTemplate iorTemplate, Policies policies, String codebase) {
        }

        public String getMonitoringName() {
            return null;
        }

        public ServerSocket getServerSocket() {
            return null;
        }

        public int getPort() {
            return 0;
        }

        public String getName() {
            return "";
        }

        public String getInterfaceName() {
            return "";
        }

        public String getType() {
            return "";
        }

        public boolean isLazy() {
            return false;
        }
    }

    public static final class TestAcceptor2 implements Acceptor {
        public boolean equals(Object other) {
            return other instanceof TestAcceptor2;
        }

        public boolean initialize() {
            return true;
        }

        public boolean initialized() {
            return true;
        }

        public String getConnectionCacheType() {
            return "FOO";
        }

        public void setConnectionCache(InboundConnectionCache connectionCache) {
        }

        public InboundConnectionCache getConnectionCache() {
            return null;
        }

        public boolean shouldRegisterAcceptEvent() {
            return true;
        }

        public void setUseSelectThreadForConnections(boolean x) {
        }

        public boolean shouldUseSelectThreadForConnections() {
            return true;
        }

        public void setUseWorkerThreadForConnections(boolean x) {
        }

        public boolean shouldUseWorkerThreadForConnections() {
            return true;
        }

        public Socket getAcceptedSocket() {
            return null;
        }

        public void processSocket(Socket socket) {
        }

        public void close() {
        }

        public EventHandler getEventHandler() {
            return null;
        }

        public CDROutputObject createOutputObject(ORB broker, MessageMediator messageMediator) {
            return null;
        }

        public String getObjectAdapterId() {
            return null;
        }

        public String getObjectAdapterManagerId() {
            return null;
        }

        public void addToIORTemplate(IORTemplate iorTemplate, Policies policies, String codebase) {
        }

        public String getMonitoringName() {
            return null;
        }

        public ServerSocket getServerSocket() {
            return null;
        }

        public int getPort() {
            return 0;
        }

        public String getName() {
            return "";
        }

        public String getInterfaceName() {
            return "";
        }

        public String getType() {
            return "";
        }

        public boolean isLazy() {
            return false;
        }
    }

    // REVISIT - this is a cut and paste modification of makeROIOperation.
    private Operation makeAcceptorInstantiationOperation() {
        Operation indexOp = OperationFactory.suffixAction();
        Operation op1 = OperationFactory.compose(indexOp, classAction);
        Operation mop = OperationFactory.maskErrorAction(op1);

        Operation mkinst = new Operation() {
            public Object operate(Object value) {
                final Class initClass = (Class) value;
                if (initClass == null)
                    return null;

                // For security reasons avoid creating an instance
                // if this class is one that would fail the class cast
                // to ORBInitializer anyway.
                if (Acceptor.class.isAssignableFrom(initClass)) {
                    // Now that we have a class object, instantiate one and
                    // remember it:
                    Acceptor acceptor = null;

                    try {
                        acceptor = (Acceptor) AccessController.doPrivileged(new PrivilegedExceptionAction() {
                            public Object run() throws InstantiationException, IllegalAccessException {
                                return initClass.newInstance();
                            }
                        });
                    } catch (PrivilegedActionException exc) {
                        // Unwrap the exception, as we don't care exc here
                        throw wrapper.acceptorInstantiationFailure(exc.getException(), initClass.getName());
                    } catch (Exception exc) {
                        throw wrapper.acceptorInstantiationFailure(exc, initClass.getName());
                    }

                    return acceptor;
                } else {
                    throw wrapper.acceptorInstantiationTypeFailure(initClass.getName());
                }
            }
        };

        Operation result = OperationFactory.compose(mop, mkinst);

        return result;
    }

    private Operation makeInitRefOperation() {
        return new Operation() {
            public Object operate(Object value) {
                // Object is String[] of length 2.
                String[] values = (String[]) value;
                if (values.length != 2)
                    throw wrapper.orbInitialreferenceSyntax();

                return values[0] + "=" + values[1];
            }
        };
    }
}

// End of file.
