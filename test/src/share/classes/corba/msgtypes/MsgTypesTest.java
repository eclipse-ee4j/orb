/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2020 Payara Services Ltd.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.msgtypes;

import com.sun.corba.ee.spi.misc.ORBConstants;
import corba.framework.CORBATest;
import corba.framework.Controller;
import corba.framework.Options;
import java.util.Properties;

public class MsgTypesTest extends CORBATest {
    static final int GROW = 0;
    static final int COLLECT = 1;
    static final int STREAM = 2;
    static String[] GIOP_version = { "1.0", "1.1", "1.2" };
    static String[] GIOP_strategy = { "GROW", "CLCT", "STRM" };

    private int errors = 0; // keeps the error count

    private void printBeginTest(int clientVersion,
                                int clientStrategy,
                                int serverVersion,
                                int serverStrategy)
    {
        StringBuilder output = new StringBuilder(80);

        // Pleasing aesthetics
        output.append("      ");

        output.append(GIOP_version[clientVersion]);
        output.append(" ");
        output.append(GIOP_strategy[clientStrategy]);
        output.append(" client <> ");
        output.append(GIOP_version[serverVersion]);
        output.append(" ");
        output.append(GIOP_strategy[serverStrategy]);
        output.append(" server: ");

        System.out.print(output.toString());
    }

    private String testName( boolean isServer,
        int clientVersion, int clientStrategy, int serverVersion, int serverStrategy)
    {
        StringBuilder output = new StringBuilder(80);

        output.append(GIOP_version[clientVersion]);
        output.append("_");
        output.append(GIOP_strategy[clientStrategy]);
        output.append("_to_");
        output.append(GIOP_version[serverVersion]);
        output.append("_");
        output.append(GIOP_strategy[serverStrategy]);
        if (isServer) {
            output.append( "_server" ) ;
        } else {
            output.append( "_client" ) ;
        }

        return output.toString() ;
    }

    private void printEndTest(String result)
    {
        System.out.println(result);
    }

    private void printFinishedTest(String result) {
        StringBuilder output = new StringBuilder(80);
        output.append("      ");
        output.append(result);

        System.out.println(output.toString());
    }

    protected void doTest() throws Throwable
    {
        // Pleasing aesthetics
        System.out.println();

        runLocateMsgType();
        runEarlyReply();

        runSimpleCancelRequest();
        runAbortiveCancelRequest1();
        runAbortiveCancelRequest2();

        runMessageError();
        runCloseConnection();
        runGIOPInterop();
        runTargetAddressDisp();

        // This has been commented out for a long time, and
        // it currently fails with a buffer underflow error.
        // runFragmentedReply();

        runHeaderPaddingTest();
        
        System.out.print("      Test result : " );

        if (errors > 0)
            throw new Exception("Errors detected");
    }

    public void runLocateMsgType() throws Throwable {

        Options.getClientArgs().clear();
        Options.addClientArg("LocateMsg");
        int fragmentSize = 32;

        for (int client_strategy = GROW, i = 0; i < GIOP_version.length; i++) {

            Properties clientProps = Options.getClientProperties();

            clientProps.put(ORBConstants.GIOP_FRAGMENT_SIZE, "" + fragmentSize);
            clientProps.put(ORBConstants.GIOP_VERSION, GIOP_version[i]);
            clientProps.put(ORBConstants.GIOP_11_BUFFMGR, "" + client_strategy);
            clientProps.put(ORBConstants.GIOP_12_BUFFMGR, "" + client_strategy);
            // clientProps.put(ORBConstants.DEBUG_PROPERTY, "transport,subcontract" ) ;

            for (int server_strategy = GROW, j = 0; j < GIOP_version.length; j++) {

                runTest(client_strategy, i, server_strategy, j);

                if (GIOP_version[j].equals("1.1") && server_strategy == GROW) {
                    server_strategy = STREAM; j--;
                } else if (GIOP_version[j].equals("1.2") && server_strategy == STREAM) {
                    server_strategy = GROW; j--;
                }
            }

            if (GIOP_version[i].equals("1.1") && client_strategy == GROW) {
                client_strategy = COLLECT; i--;
            } else if (GIOP_version[i].equals("1.2") && client_strategy == COLLECT) {
                client_strategy = STREAM; i--;
            } else if (GIOP_version[i].equals("1.2") && client_strategy == STREAM) {
                client_strategy = GROW; i--;
            }
        }
    }

    private void runTest(int client_strategy, int client_version, int server_strategy, int server_version) throws Exception {
        // skip non-longer support COLLECT strategy tests
        if (client_strategy == COLLECT || server_strategy == COLLECT) return;

        printBeginTest(client_version, client_strategy, server_version, server_strategy);

        Properties serverProps = Options.getServerProperties();
        serverProps.put(ORBConstants.GIOP_VERSION, GIOP_version[client_version]);
        serverProps.put(ORBConstants.GIOP_11_BUFFMGR, "" + server_strategy);
        serverProps.put(ORBConstants.GIOP_12_BUFFMGR, "" + server_strategy);
        // serverProps.put(ORBConstants.DEBUG_PROPERTY, "transport,subcontract" ) ;

        Controller server = createServer("corba.msgtypes.Server",
            testName( true, client_version, client_strategy, server_version, server_strategy));
        Controller client = createClient("corba.msgtypes.Client",
            testName( false, client_version, client_strategy, server_version, server_strategy));

        server.start();
        client.start();

        client.waitFor(60000);

        if (client.exitValue() != Controller.SUCCESS) {
            errors++;
            printEndTest("LocateMsgTest FAILED, Client exit value = " +
                         client.exitValue());
        } else {
            if (server.finished()) {
                errors++;
                printEndTest("LocateMsgTest FAILED, Server crashed");
            } else {
                printEndTest("LocateMsgTest PASSED");
            }
        }
        client.stop();
        server.stop();
    }

    public void runEarlyReply() throws Throwable {
        Options.getClientArgs().clear();
        Options.addClientArg("EarlyReply");

        int fragmentSize = 1024;
        Properties clientProps = Options.getClientProperties();
        clientProps.put(ORBConstants.GIOP_FRAGMENT_SIZE, "" + fragmentSize);

        Controller server = createServer("corba.msgtypes.Server", "runEarlyReply" );
        Controller client = createClient("corba.msgtypes.Client", "runEarlyReply" );

        server.start();
        client.start();

        client.waitFor(60000);

        if (client.exitValue() != Controller.SUCCESS) {
            errors++;
            printFinishedTest("EarlyReplyTest FAILED, Client exit value = " +
                         client.exitValue());
        } else {
            if (server.finished()) {
                errors++;
                printFinishedTest("EarlyReplyTest FAILED, Server crashed");
            } else {
                printFinishedTest("EarlyReplyTest PASSED");
            }
        }

        client.stop();
        server.stop();
    }

    public void runSimpleCancelRequest() throws Throwable {
        Options.getClientArgs().clear();
        Options.addClientArg("SimpleCancelRequest");

        int fragmentSize = 32;
        Properties clientProps = Options.getClientProperties();
        clientProps.put(ORBConstants.GIOP_FRAGMENT_SIZE, "" + fragmentSize);

        Controller server = createServer("corba.msgtypes.Server", "runSimpleCancelRequest" );
        Controller client = createClient("corba.msgtypes.Client", "runSimpleCancelRequest" );

        server.start();
        client.start();

        client.waitFor(60000);

        if (client.exitValue() != Controller.SUCCESS) {
            errors++;
            printFinishedTest("SimpleCancelRqstTest FAILED, Client exit value = " +
                         client.exitValue());
        } else {
            if (server.finished()) {
                errors++;
                printFinishedTest("SimpleCancelRqstTest FAILED, Server crashed");
            } else {
                printFinishedTest("SimpleCancelRqstTest PASSED");
            }
        }

        client.stop();
        server.stop();
    }

    public void runAbortiveCancelRequest1() throws Throwable {
        Options.getClientArgs().clear();
        Options.addClientArg("AbortiveCancelRequest1");

        int fragmentSize = 1024;
        Properties clientProps = Options.getClientProperties();
        clientProps.put(ORBConstants.GIOP_FRAGMENT_SIZE, "" + fragmentSize);

        Controller server = createServer("corba.msgtypes.Server", "runAbortiveCancelRequest1" );
        Controller client = createClient("corba.msgtypes.Client", "runAbortiveCancelRequest1" );

        server.start();
        client.start();

        client.waitFor(60000);

        if (client.exitValue() != Controller.SUCCESS) {
            errors++;
            printFinishedTest("AbortiveCancelRqTest1 FAILED, Client exit value = " +
                         client.exitValue());
        } else {
            if (server.finished()) {
                errors++;
                printFinishedTest("AbortiveCancelRqTest1 FAILED, Server crashed");
            } else {
                printFinishedTest("AbortiveCancelRqTest1 PASSED");
            }
        }

        client.stop();
        server.stop();
    }

    public void runAbortiveCancelRequest2() throws Throwable {
        Options.getClientArgs().clear();
        Options.addClientArg("AbortiveCancelRequest2");

        int fragmentSize = 1024;
        Properties clientProps = Options.getClientProperties();
        clientProps.put(ORBConstants.GIOP_FRAGMENT_SIZE, "" + fragmentSize);

        Properties serverProps = Options.getServerProperties();
        serverProps.put("org.omg.CORBA.ORBClass",
                        "com.sun.corba.ee.impl.orb.ORBImpl");
        serverProps.put("org.omg.PortableInterceptor.ORBInitializerClass." +
                  "corba.msgtypes.Server", "true");

        Controller server = createServer("corba.msgtypes.Server", "runAbortiveCancelRequest2" );
        Controller client = createClient("corba.msgtypes.Client", "runAbortiveCancelRequest2" );

        server.start();
        client.start();

        client.waitFor(60000);

        if (client.exitValue() != Controller.SUCCESS) {
            errors++;
            printFinishedTest("AbortiveCancelRqTest2 FAILED, Client exit value = " +
                         client.exitValue());
        } else {
            if (server.finished()) {
                errors++;
                printFinishedTest("AbortiveCancelRqTest2 FAILED, Server crashed");
            } else {
                printFinishedTest("AbortiveCancelRqTest2 PASSED");
            }
        }

        client.stop();
        server.stop();
    }

    public void runTargetAddressDisp() throws Throwable {
        Options.getClientArgs().clear();
        Options.addClientArg("TargetAddrDisposition");

        int fragmentSize = 1024;
        Properties clientProps = Options.getClientProperties();
        clientProps.put(ORBConstants.GIOP_FRAGMENT_SIZE, "" + fragmentSize);
        clientProps.put(ORBConstants.GIOP_TARGET_ADDRESSING,
                  "" + ORBConstants.ADDR_DISP_IOR);
        clientProps.put("org.omg.CORBA.ORBClass",
                        "com.sun.corba.ee.impl.orb.ORBImpl");
        clientProps.put("org.omg.PortableInterceptor.ORBInitializerClass." +
                  "corba.msgtypes.Client", "true");
                  
        Properties serverProps = Options.getServerProperties();
        serverProps.put("org.omg.CORBA.ORBClass",
                        "com.sun.corba.ee.impl.orb.ORBImpl");
        serverProps.put("org.omg.PortableInterceptor.ORBInitializerClass." +
                  "corba.msgtypes.Server", "true");
        serverProps.put(ORBConstants.GIOP_TARGET_ADDRESSING,
                  "" + ORBConstants.ADDR_DISP_OBJKEY);              
        Controller server = createServer("corba.msgtypes.Server", "runTargetAddressDisp" );
        Controller client = createClient("corba.msgtypes.Client", "runTargetAddressDisp" );

        server.start();
        client.start();

        client.waitFor(60000);

        if (client.exitValue() != Controller.SUCCESS) {
            errors++;
            printFinishedTest("TargetAddrDisposition FAILED, Client exit value = " +
                         client.exitValue());
        } else {
            if (server.finished()) {
                errors++;
                printFinishedTest("TargetAddrDisposition FAILED, Server crashed");
            } else {
                printFinishedTest("TargetAddrDisposition PASSED");
            }
        }

        client.stop();
        server.stop();
    }

    public void runCloseConnection() throws Throwable {
        Options.getClientArgs().clear();
        Options.addClientArg("CloseConnection");

        int fragmentSize = 32;
        Properties clientProps = Options.getClientProperties();
        clientProps.put(ORBConstants.GIOP_FRAGMENT_SIZE, "" + fragmentSize);

        Controller server = createServer("corba.msgtypes.Server", "runCloseConnection" );
        Controller client = createClient("corba.msgtypes.Client", "runCloseConnection" );

        server.start();
        client.start();

        client.waitFor(60000);

        if (client.exitValue() != Controller.SUCCESS) {
            errors++;
            printFinishedTest("CloseConnectionTest FAILED, Client exit value = " +
                         client.exitValue());
        } else {
            if (server.finished()) {
                errors++;
                printFinishedTest("CloseConnectionTest FAILED, Server crashed");
            } else {
                printFinishedTest("CloseConnectionTest PASSED");
            }
        }

        client.stop();
        server.stop();
    }

    public void runMessageError() throws Throwable {
        Options.getClientArgs().clear();
        Options.addClientArg("MessageError");

        int fragmentSize = 32;
        Properties clientProps = Options.getClientProperties();
        clientProps.put(ORBConstants.GIOP_FRAGMENT_SIZE, "" + fragmentSize);

        Controller server = createServer("corba.msgtypes.Server", "runMessageError" );
        Controller client = createClient("corba.msgtypes.Client", "runMessageError" );

        server.start();
        client.start();

        client.waitFor(60000);

        if (client.exitValue() != Controller.SUCCESS) {
            errors++;
            printFinishedTest("MessageErrorTest FAILED, Client exit value = " +
                         client.exitValue());
        } else {
            if (server.finished()) {
                errors++;
                printFinishedTest("MessageErrorTest FAILED, Server crashed");
            } else {
                printFinishedTest("MessageErrorTest PASSED");
            }
        }

        client.stop();
        server.stop();
    }

    public void runGIOPInterop() throws Throwable {
        Options.getClientArgs().clear();
        Options.addClientArg("GIOPInterop");

        int fragmentSize = 32;
        Properties clientProps = Options.getClientProperties();
        clientProps.put(ORBConstants.GIOP_FRAGMENT_SIZE, "" + fragmentSize);

        Controller server = createServer("corba.msgtypes.Server", "runGIOPInterop" );
        Controller client = createClient("corba.msgtypes.Client", "runGIOPInterop" );

        server.start();
        client.start();

        client.waitFor(60000);

        if (client.exitValue() != Controller.SUCCESS) {
            errors++;
            printFinishedTest("GIOPInteropTest FAILED, Client exit value = " +
                         client.exitValue());
        } else {
            if (server.finished()) {
                errors++;
                printFinishedTest("GIOPInteropTest FAILED, Server crashed");
            } else {
                printFinishedTest("GIOPInteropTest PASSED");
            }
        }

        client.stop();
        server.stop();
    }
    
    public void runFragmentedReply() throws Throwable {
        Options.getClientArgs().clear();
        Options.addClientArg("FragmentedReply");

        int fragmentSize = 32;
        Properties serverProps = Options.getServerProperties();
        serverProps.put(ORBConstants.GIOP_FRAGMENT_SIZE, "" + fragmentSize);

        Controller server = createServer("corba.msgtypes.Server", "runFragmentedReply" );
        Controller client = createClient("corba.msgtypes.Client", "runFragmentedReply" );

        server.start();
        client.start();

        client.waitFor(60000);

        if (client.exitValue() != Controller.SUCCESS) {
            errors++;
            printFinishedTest("FragmentedReplyTest FAILED, Client exit value = "
                         + client.exitValue());
        } else {
            if (server.finished()) {
                errors++;
                printFinishedTest("FragmentedReplyTest FAILED, Server crashed");
            } else {
                printFinishedTest("FragmentedReplyTest PASSED");
            }
        }

        client.stop();
        server.stop();
    }
   
    private static final boolean NO_HEADER_PADDING_TEST = true ;

    // This test always passes, EXCEPT when run in the full test suite on hudson.
    // I am disabling the tests due to inability to reproduce the failure in a
    // debuggable context.
    public void runHeaderPaddingTest() throws Throwable {
        if (NO_HEADER_PADDING_TEST) {
            return ;
        }

        Options.getClientArgs().clear();
        Options.addClientArg("HeaderPaddingTest");

        Properties clientProps = Options.getClientProperties();
        clientProps.put(ORBConstants.GIOP_VERSION, GIOP_version[2]);
        clientProps.put(ORBConstants.GIOP_12_BUFFMGR, "" + GROW);

        Controller server = createServer("corba.msgtypes.Server", "runHeaderPaddingTest" );
        Controller client = createClient("corba.msgtypes.Client", "runHeaderPaddingTest" );

        server.start();
        client.start();

        client.waitFor(60000);

        if (client.exitValue() != Controller.SUCCESS) {
            errors++;
            printFinishedTest("HeaderPaddingTest FAILED, Client exit value = "
                         + client.exitValue());
        } else {
            if (server.finished()) {
                errors++;
                printFinishedTest("HeaderPaddingTest FAILED, Server crashed");
            } else {
                printFinishedTest("HeaderPaddingTest PASSED");
            }
        }

        client.stop();
        server.stop();
    }  
    
}

