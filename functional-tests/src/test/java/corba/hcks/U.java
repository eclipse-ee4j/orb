/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2020 Payara Services Ltd.
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

//
// Created       : 2000 Nov 08 (Wed) 20:53:55 by Harold Carr.
// Last Modified : 2005 Jul 12 (Tue) 12:42:31 by Harold Carr.
//

package corba.hcks;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.List;
import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import com.sun.corba.ee.impl.oa.poa.POACurrent;

import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Policy;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.TypeCode;

import org.omg.PortableInterceptor.InvalidSlot;
import org.omg.PortableInterceptor.RequestInfo;
import org.omg.PortableInterceptor.ServerRequestInfo;

import org.omg.PortableServer.POA;
import org.omg.PortableServer.RequestProcessingPolicyValue;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.ServantManager;
import org.omg.PortableServer.ServantRetentionPolicyValue;

import com.sun.corba.ee.spi.transport.Acceptor;
import com.sun.corba.ee.spi.transport.Connection;
import com.sun.corba.ee.spi.transport.TransportManager;
import com.sun.corba.ee.spi.transport.SocketInfo;

import com.sun.corba.ee.impl.transport.AcceptorImpl;
import org.glassfish.pfl.test.JUnitReportHelper;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextHelper;

public class U
{
    private static JUnitReportHelper helper = null ;
    private static ErrorAccumulator ea ;
    private static String testName ;
    private static boolean hasError = false ;
    
    public static boolean hasError() {
        return hasError ;
    }

    // KMC: initialize the helper and ErrorAccumulator
    public static void initialize( String name ) {
        helper = new JUnitReportHelper( name ) ;
        ea = new ErrorAccumulator() ;
        testName = null ;
    }

    // KMC: all done
    public static void done() {
        if (testName != null) 
            testComplete() ;

        helper.done() ;
    }
    
    // KMC: helper support
    private static void testComplete() {
        List<ErrorAccumulator.MessageAndException> errors = ea.getTestErrors() ;
        if (errors.isEmpty()) {
            helper.pass() ;
        }  else {
            StringBuilder sb = new StringBuilder() ;
            sb.append( "Errors in test:\n" ) ;
            for (ErrorAccumulator.MessageAndException mea : errors) {
                sb.append( "    " ) ;
                sb.append( mea.toString() ) ;
                sb.append( "\n" ) ;
            }

            helper.fail( sb.toString() ) ;
        }
    }

    // KMC: helper support
    private static void testStart( Object x ) {
        if (testName != null)
            testComplete() ;
        testName = x.toString() ;
        helper.start( testName ) ;
        ea.startTest() ;
    }

    //
    // OMG Standard Constants.
    //

    public static String NameService = "NameService";
    public static String PICurrent   = "PICurrent";
    public static String POACurrent  = "POACurrent";
    public static String RootPOA     = "RootPOA";
    public static String ORBClass    = "org.omg.CORBA.ORBClass";
    public static String ORBInitializerClass =
        "org.omg.PortableInterceptor.ORBInitializerClass";

    public static int    OMGVMCID = org.omg.CORBA.OMGVMCID.value;
    public static int    SUNVMCID = 0x53550000;

    //
    // Java Standard Constants.
    //

    public static String javaNamingCorbaOrb = "java.naming.corba.orb";

    //
    // Messages.
    //

    public static String SHOULD_NOT_SEE_THIS  = "SHOULD NOT SEE THIS";
    public static String OK                   = "OK";
    public static String unexpected_exception = "unexpected exception";
    public static String _DII                 = "DII";
    public static String _DSI                 = "DSI";
    public static String from_a_idlDynamic_servant =
        "from a idlDynamic servant";
    public static String _servant             = "servant";

    //
    // POA.
    //


    public static POA getRootPOA(ORB orb)
        throws
            Exception
    {
        return (POA)orb.resolve_initial_references(RootPOA);
    }

    //
    // POACurrent.
    //

    public static org.omg.PortableServer.Current getPOACurrent(ORB orb)
        throws
            Exception
    {
        return (org.omg.PortableServer.Current)
            // REVISIT - need PortableServer.CurrentHelper.narrow
            org.omg.CORBA.CurrentHelper.narrow(orb.resolve_initial_references(
                POACurrent));
    }

    public static byte[] getPOACurrentObjectId(ORB orb)
        throws
            Exception
    {
        return getPOACurrent(orb).get_object_id();
    }

    public static String getPOACurrentOperation(ORB orb)
        throws
            Exception
    {
        return ((POACurrent)getPOACurrent(orb)).getOperation();
    }

    public static String getPOACurrentInfo(ORB orb)
        throws
            Exception
    {
        String op = new String(getPOACurrentOperation(orb));
        String name = new String(getPOACurrentObjectId(orb));
        return op + " " + name;
    }

    public static Policy[] createUseServantManagerPolicies(
                               POA poa,
                               ServantRetentionPolicyValue isRetain)
        throws
            Exception
    {
        Policy[] policies = new Policy[2];
        policies[0] = poa.create_servant_retention_policy(isRetain);
        policies[1] = poa.create_request_processing_policy(
                          RequestProcessingPolicyValue.USE_SERVANT_MANAGER);

        return policies;
    }

    public static POA createPOAWithServantManager(
                                POA parent,
                                String name,
                                Policy[] policies,
                                ServantManager servantManager)
        throws
            Exception
    {
        POA child = parent.create_POA(name, null, policies);
        child.the_POAManager().activate();
        child.set_servant_manager(servantManager);
        return child;
    }

    // This one is really not POA.  It is the deprecated connect API.
    public static Object createWithConnectAndBind(String               name,
                                                  org.omg.CORBA.Object servant,
                                                  ORB                  orb)
        throws
            Exception
    {
        orb.connect(servant);
        U.rebind(name, servant, orb);
        return servant;
    }

    public static org.omg.CORBA.Object 
        createWithServantAndBind (String  name,
                                  Servant servant,
                                  POA     poa,
                                  ORB     orb)
        throws
            Exception
    {
        byte[] id = name.getBytes();
        poa.activate_object_with_id(id, servant);
        org.omg.CORBA.Object ref = poa.id_to_reference(id);
        U.rebind(name, ref, orb);
        return ref;
    }

    public static org.omg.CORBA.Object createWithIdAndBind(String id,
                                                           String repoId,
                                                           POA    poa,
                                                           ORB    orb)
        throws
            Exception
    {
        org.omg.CORBA.Object ref =
            poa.create_reference_with_id(id.getBytes(), repoId);
        U.rebind(id, ref, orb);
        return ref;
    }

    public static rmiiI createRMIIPOABindAndCall(String name,
                                                 String tieClassName,
                                                 POA poa,
                                                 ORB orb,
                                                 InitialContext initialContext)
        throws
            Exception
    {
        org.omg.CORBA.Object ref = 
            createRMIPOABind(name, tieClassName, poa, orb, initialContext);

        // Do a colocated invocation.

        rmiiI rrmiiI = (rmiiI)PortableRemoteObject.narrow(ref, rmiiI.class);
        U.sop(rrmiiI.sayHello());

        return rrmiiI;
    }

    public static org.omg.CORBA.Object
        createRMIPOABind(String name,
                         String tieClassName,
                         POA poa,
                         ORB orb,
                         InitialContext initialContext)
        throws
            Exception
    {
        // Class.forName is used so there is no compile time reference
        // to the Tie class.  This makes it possible to first compile
        // the *.java files with javac then run rmic on the servant classes
        // to generate the Ties.  

        byte[] oid = name.getBytes();
        Servant servant = 
            (Servant) Class.forName(tieClassName).newInstance();
        String repositoryId = servant._all_interfaces(poa, oid)[0];
        org.omg.CORBA.Object ref =
            poa.create_reference_with_id(oid, repositoryId);

        initialContext.rebind(name, ref);
        return ref;
    }

    //
    // Naming.
    //

    // RMI-IIOP Naming.

    public static Object lookupAndNarrow(String name, 
                                         Class clazz,
                                         InitialContext initialContext)
        throws
            NamingException
    {
        return PortableRemoteObject.narrow(initialContext.lookup(name), clazz);
    }

    // IDL Naming.

    public static org.omg.CORBA.Object resolve(String name, ORB orb)
        throws 
            Exception
    {
        return getNameService(orb).resolve(makeNameComponent(name));
    }


    public static org.omg.CORBA.Object rebind(String name,
                                              org.omg.CORBA.Object ref,
                                              ORB orb)
        throws 
            Exception
    {
        getNameService(orb).rebind(makeNameComponent(name), ref);
        return ref;
    }

    public static NameComponent[] makeNameComponent(String name)
    {
        Vector result = new Vector();
        StringTokenizer tokens = new StringTokenizer(name, "/");
        while (tokens.hasMoreTokens()) {
            result.addElement(tokens.nextToken());
        }
        NameComponent path[] = new NameComponent[result.size()];
        for (int i = 0; i < result.size(); ++i) {
            path[i] = new NameComponent((String)result.elementAt(i), "");
        }
        return path;
    }

    public static NamingContext getNameService(ORB orb)
        throws
            Exception
    {
        return NamingContextHelper.narrow(
            orb.resolve_initial_references(NameService));
    }


    //
    // Interceptors.
    //

    public static org.omg.PortableInterceptor.Current getPICurrent(ORB orb)
    {
        org.omg.PortableInterceptor.Current current = null;
        try {
            current = org.omg.PortableInterceptor.CurrentHelper.narrow(
                orb.resolve_initial_references(PICurrent));
        } catch (org.omg.CORBA.ORBPackage.InvalidName e) {
            sopUnexpectedException("getPICurrent", e);
        }
        return current;
    }

    public static Any getSlot(org.omg.PortableInterceptor.Current pic,
                              int slotId)
    {
        try {
            return pic.get_slot(slotId);
        } catch (InvalidSlot e) {
            throw new RuntimeException("getSlot");
        }
    }

    public static void setSlot(org.omg.PortableInterceptor.Current pic,
                               int slotId, Any value)
    {
        try {
            pic.set_slot(slotId, value);
        } catch (InvalidSlot e) {
            throw new RuntimeException("setSlot");
        }
    }

    public static Any getSlot(RequestInfo ri, int slotId)
    {
        try {
            return ri.get_slot(slotId);
        } catch (InvalidSlot e) {
            throw new RuntimeException("getSlot");
        }
    }

    public static void setSlot(ServerRequestInfo ri, int slotId, Any value)
    {
        try {
            ri.set_slot(slotId, value);
        } catch (InvalidSlot e) {
            throw new RuntimeException("setSlot");
        }
    }

    //
    // Dynamic type support.
    //

    public static boolean isTkBoolean(Any any)
    {
        return isTk(TCKind.tk_boolean, any);
    }

    public static boolean isTkLong(Any any)
    {
        return isTk(TCKind.tk_long, any);
    }

    public static boolean isTkNull(Any any)
    {
        return isTk(TCKind.tk_null, any);
    }

    public static boolean isTk(TCKind tk, Any any) 
    {
        TypeCode typeCode = any.type();
        TCKind tcKind = typeCode.kind();
        return tcKind.equals(tk);
    }

    //
    // I/O.
    //

    public static void lf()
    {
        System.out.println();
    }

    public static void sop(java.lang.Object x)
    {
        System.out.println(x);
        System.out.flush();
    }

    public static void sop(boolean msg)
    {
        System.out.println(msg);
        System.out.flush();
    }

    // KMC: called before the start of a test case
    public static void HEADER(java.lang.Object x)
    {
        lf();
        sop("--------------------------------------------------");
        testStart( x ) ;
        sop(x);
        lf();
    }

    public static void normalExit(String msg)
    {
        U.sop(msg + ": normal exit.");
    }

    public static String unexpectedException(String msg, Throwable t)
    {
        return msg + " " + U.unexpected_exception + " " + t;
    }

    public static void sopUnexpectedException(String msg, Throwable t)
    {
        sop(unexpectedException(msg, t));
        t.printStackTrace(System.out);
        hasError = true ;
    }

    public static String OK(String msg)
    {
        return msg + " " + U.OK;
    }

    public static void sopOK(String msg)
    {
        sop(OK(msg));
    }

    public static void sopShouldNotSeeThis()
    {
        sop(SHOULD_NOT_SEE_THIS);
        ea.add( SHOULD_NOT_SEE_THIS, new RuntimeException() ) ;
        hasError = true ;
        throw new RuntimeException(SHOULD_NOT_SEE_THIS);
    }

    public static void sopShouldNotSeeThis(String msg)
    {
        sop(msg + " " + SHOULD_NOT_SEE_THIS);
        ea.add( msg, new RuntimeException( SHOULD_NOT_SEE_THIS ) ) ;
        hasError = true ;
        throw new RuntimeException(msg + " " + SHOULD_NOT_SEE_THIS);
    }

    public static String DII(String name)
    {
        return _DII + " " + name;
    }

    public static String DSI(String name)
    {
        return _DSI + " " + name;
    }

    public static String servant(String name)
    {
        return _servant + " " + name;
    }

    public static boolean unregisterAcceptorAndCloseConnections(
        String socketType, com.sun.corba.ee.spi.orb.ORB orb)
    {
        TransportManager transportManager =orb.getCorbaTransportManager();
        Collection acceptors = transportManager.getAcceptors();
        Iterator i = acceptors.iterator();
        while (i.hasNext()) {
            Acceptor acceptor = (Acceptor) i.next();
            if (acceptor instanceof SocketInfo) {
                SocketInfo socketInfo = (SocketInfo) acceptor;
                if (socketInfo.getType().equals(socketType)) {

                    // Close the acceptor
                    transportManager.unregisterAcceptor(acceptor);
                    sop("Closing acceptor: " + acceptor);
                    acceptor.close();

                    // Close the connection
                    Collection connections = 
                        ((com.sun.corba.ee.impl.transport.ConnectionCacheBase)transportManager.getInboundConnectionCache(acceptor)).values();
                    i = connections.iterator();
                    while (i.hasNext()) {
                        Connection connection = (Connection) i.next();
                        CloseThread closeThread = new CloseThread(connection);
                        closeThread.start();
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean registerAcceptor(String socketType,
                                           int port,
                                           com.sun.corba.ee.spi.orb.ORB orb)
    {
        Acceptor acceptor =
            new AcceptorImpl(
                orb,
                port,
                "Test",
                socketType);
        orb.getCorbaTransportManager().registerAcceptor(acceptor);
        // This initializes it and registers it with the transport manager.
        orb.getCorbaTransportManager().getAcceptors();
        return true;
    }


    ////////////////////////////////////////////////////
    //
    // Test framework utilities.
    //

    public static final int result = 0;
    public static final int exception = 1;
    public static final int exceptionEquals = 2; // Caveat: see below in switch
    public static final Object[] noArgs = {};
    public static final String newlineTab = "\n\t";
    public static final String _result = "result";
    public static final String _exception = "exception";

    public static void expect( int kind,
                              Object expected,
                              Object ref,
                              String methodName)
    {
        expect(kind, expected, ref, methodName, noArgs);
    }

    public static void expect( int kind,
                              Object expected,
                              Object ref,
                              String methodName,
                              Object arg1)
    {
        Object[] args = { arg1 };
        expect(kind, expected, ref, methodName, args);
    }

    public static void expect( int kind,
                              Object expected,
                              Object ref,
                              String methodName,
                              Object arg1,
                              Object arg2)
    {
        Object[] args = { arg1, arg2 };
        expect(kind, expected, ref, methodName, args);
    }


    public static void expect(
                              int kind,
                              Object expected,
                              Object ref,
                              String methodName,
                              Object[] args)
    {
        String baseMsg = "U.expect";

        Class clazz = ref.getClass();
        Class[] parameterTypes = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            parameterTypes[i] = args[i].getClass();
        }
        Method method;
        try {
            method = clazz.getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(baseMsg + " NoSuchMethodException "
                                       + methodName);
        } catch (SecurityException e) {
            throw new RuntimeException(baseMsg + " SecurityException");
        }

        Object result = null;
        boolean gotResult = false;

        try {
            result = method.invoke(ref, args);
            gotResult = true;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(baseMsg + " IllegalAccessException");
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(baseMsg + " IllegalArgumentException");
        } catch (InvocationTargetException e) {
            result = e.getTargetException();
        }

        switch (kind) {
        case U.result :
            if (! gotResult) {
                reportError(result,
                            result, expected, ref, methodName, args,
                            _result, _exception);
            } else if (result == null) {
                if (expected != null) {
                    // This handles oneways (and voids?).
                    reportError(null,
                                result, expected, ref, methodName, args,
                                _result, _result);
                }
            } else if (! result.equals(expected)) {
                reportError(null,
                            result, expected, ref, methodName, args,
                            _result, _result);
            }
            break;
        case exceptionEquals :
        case exception :
            if (gotResult) {
                reportError(null,
                            result, expected, ref, methodName, args,
                            _exception, _result);
            } else if (! result.getClass().isInstance(expected)) {
                reportError(result,
                            result, expected, ref, methodName,args,
                            _exception, _exception);
            } else if (kind == exceptionEquals) {

                // Caveat: only use exceptionEquals if the exceptions
                // to be compared have defined a reasonable equals.
                // Otherwise it probably defaults to Object.equals
                // which is just a reference compare.

                if (! result.equals(expected)) {
                    reportError(result,
                                result, expected, ref, methodName, args,
                                _exception, _exception);
                }
            }
            break;
        default :
            throw new RuntimeException(baseMsg + " switch default");
        }
    }

    public static void reportError( Object exception, // null if none.
                                   Object result,
                                   Object expected,
                                   Object ref,
                                   String methodName,
                                   Object[] args,
                                   String expectedType,
                                   String resultType)
    {
        StringBuffer sb = formatCall(ref, methodName, args);
        sb.append(newlineTab);
        sb.append("expected ").append(expectedType).append(": ");
        sb.append(expected);
        sb.append(newlineTab);
        sb.append("but got ").append(resultType).append(": ");
        sb.append(result);
        reportError(sb.toString(), (Throwable)exception);
    }

    public static StringBuffer formatCall(Object ref,
                                          String methodName, 
                                          Object[] args)
    {
        StringBuffer result = new StringBuffer(ref + "." + methodName + "(");
        for (int i = 0; i < args.length; i++) {
            result.append(args[i]);
            result.append(",");
        }
        if (args.length > 0) {
            result.deleteCharAt(result.length() - 1);// Get rid of extra comma.
        }
        result.append(")");
        return result;
    }

    public static boolean displayErrorsWhenTheyHappen = true;
    public static boolean printStackTrace = true;

    public static void setDisplayErrorsWhenTheyHappen(boolean b)
    {
        displayErrorsWhenTheyHappen = b;
    }

    public static void setPrintStackTrace(boolean b)
    {
        printStackTrace = b;
    }

    public static void reportError( String msg, 
                                   Throwable t)
    {
        ea.add(msg, t);
        hasError = true ;

        if (displayErrorsWhenTheyHappen) {
            reportError(printStackTrace, msg, t);
        }
    }

    public static void reportError(boolean printStackTrace,
                                   String msg,
                                   Throwable t)
    {
        ea.add( msg, t ) ;
        hasError = true ;

        sop("--------------------------------------------------");
        sop(msg);
        if (t != null && printStackTrace) {
            lf();
            t.printStackTrace(System.out);
        }
        sop("--------------------------------------------------");
    }
}

class CloseThread extends Thread
{
    Connection connection;
    CloseThread(Connection connection)
    {
        this.connection = connection;
    }
    public void run()
    {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            U.sop(e);
            System.exit(1);

        }
        U.sop("Closing connection: " + connection);
        connection.close();
    }
}

// End of file.

