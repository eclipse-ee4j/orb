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

//
// Created       : 2000 Nov 08 (Wed) 20:53:55 by Harold Carr.
// Last Modified : 2003 Dec 16 (Tue) 15:20:45 by Harold Carr.
//

package corba.hcks;

import org.omg.CORBA.Any;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.IMP_LIMIT;
import org.omg.CORBA.ORB;
import org.omg.CORBA.UNKNOWN;

import org.omg.CORBA.portable.UnknownException;

import org.omg.PortableInterceptor.Current;
import org.omg.PortableInterceptor.CurrentHelper;

import com.sun.corba.ee.spi.misc.ORBConstants;

import java.util.Hashtable;
import java.util.Properties;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class C
{
    // Custom classes.

    public static String MyPOAORB         = 
        C.class.getPackage().getName() + ".MyPOAORB";
    public static String MyORBInitializer = 
        C.class.getPackage().getName() + ".MyORBInitializer";

    // Names for references in naming.

    public static final String rmiiI1 = "rmiiI1";
    public static final String rmiiI2 = "rmiiI2";
    public static final String rmiiSA = "rmiiSA";
    public static final String rmiiSL = "rmiiSL";

    public static String idlHEADERI    = "idlHEADERI";
    public static String idlStaticPOA  = "idlStaticPOA";
    public static String idlDynamicPOA = "idlDynamicPOA";
    public static String idlStatic     = "idlStatic";
    public static String idlStaticForDisconnect = "idlStaticForDisconnect";
    public static String idlStaticTie  = "idlStaticTie";
    public static String idlDynamic    = "idlDynamic";
    public static String idlSAI1       = "idlSAI1";
    public static String idlSAI2       = "idlSAI2";
    public static String idlSAIRaiseObjectNotExistInIncarnate =
        "idlSAIRaiseObjectNotExistInIncarnate";
    public static String idlSAIRaiseSystemExceptionInIncarnate =
        "idlSAIRaiseSystemExceptionInIncarnate";
    public static String idlSLI1       = "idlSLI1";
    public static String idlSLI2       = "idlSLI2";
    public static String idlAlwaysForward = "idlAlwaysForward";
    public static String idlAlwaysForwardedToo = "idlAlwaysForwardedToo";

    public static String idlNonExistentDefaultServant =
        "idlNonExistentDefaultServant";

    public static String sendRecursiveType = "sendRecursiveType";

    public static String idlControllerStatic = "idlControllerStatic";

    // Operation/method names.

    public static String ServantActivator = "ServantActivator";
    public static String raiseForwardRequestInIncarnate =
        "raiseForwardRequestInIncarnate";
    public static String raiseObjectNotExistInIncarnate =
        "raiseObjectNotExistInIncarnate";
    public static String raiseSystemExceptionInIncarnate =
        "raiseSystemExceptionInIncarnate";

    public static String ServantLocator = "ServantLocator";
    public static String raiseForwardRequestInPreinvoke =
        "raiseForwardRequestInPreinvoke";
    public static String raiseObjectNotExistInPreinvoke =
        "raiseObjectNotExistInPreinvoke";
    public static String raiseSystemExceptionInPreinvoke =
        "raiseSystemExceptionInPreinvoke";
    public static String raiseSystemExceptionInPostinvoke =
        "raiseSystemExceptionInPostinvoke";
    public static String raiseUserInServantThenSystemInPOThenSE =
        "raiseUserInServantThenSystemInPOThenSE";
    public static String raiseSystemInServantThenPOThenSE = 
        "raiseSystemInServantThenPOThenSE";

    public static String throwThreadDeathInReceiveRequestServiceContexts =
        "throwThreadDeathInReceiveRequestServiceContexts";
    public static String throwThreadDeathInPreinvoke =
        "throwThreadDeathInPreinvoke";
    public static String throwThreadDeathInReceiveRequest =
        "throwThreadDeathInReceiveRequest";
    public static String throwThreadDeathInServant =
        "throwThreadDeathInServant";
    public static String throwThreadDeathInPostinvoke =
        "throwThreadDeathInPostinvoke";
    public static String throwThreadDeathInSendReply =
        "throwThreadDeathInSendReply";
    public static String throwThreadDeathInServantThenSysInPostThenSysInSendException =
        "throwThreadDeathInServantThenSysInPostThenSysInSendException";

    public static String sPic1 = "sPic1";
    public static String sPic2 = "sPic2";

    public static String makeColocatedCallFromServant =
        "makeColocatedCallFromServant";
    public static String colocatedCallFromServant =
        "colocatedCallFromServant";

    public static String sayHello              = "sayHello";
    public static String sendBytes             = "sendBytes";
    public static String sendOneObject         = "sendOneObject";
    public static String sendTwoObjects        = "sendTwoObjects";
    public static String returnObjectFromServer=
        "returnObjectFromServer";

    public static String syncOK                = "syncOK";
    public static String _get_interface_def    = "_get_interface_def";
    public static String _is_a                 = "_is_a";
    public static String _is_local             = "_is_local";
    public static String _non_existent         = "_non_existent";
    public static String asyncOK               = "asyncOK";
    public static String throwUserException    = "throwUserException";
    public static String throwSystemException  = "throwSystemException";
    public static String throwUnknownException = "throwUnknownException";
    public static String throwUNKNOWN          = "throwUNKNOWN";
    public static String raiseSystemExceptionInSendReply =
        "raiseSystemExceptionInSendReply";
    public static String testEffectiveTarget1   = "testEffectiveTarget1";
    public static String testEffectiveTarget2   = "testEffectiveTarget2";
    public static String sendValue              = "sendValue";
    public static String object_to_string       = "object_to_string";
    public static String isIdenticalWithSavedIOR = 
        "isIdenticalWithSavedIOR";


    // Controller actions.

    public static String disconnectRidlStaticServant =
        "disconnectRidlStaticServant";

    // Misc.

    public static String UTF8 = "UTF8";
    public static String idlStaticStringified = "idlStaticStringified";
    public static String helloWorld = "hello world...";

    public static int    DEFAULT_FRAGMENT_SIZE = 32;
    public static String GIOP_VERSION_1_1 = "1.1";
    public static String GIOP_VERSION_1_2 = "1.2";
    public static String BUFFMGR_STRATEGY_GROW = "0";
    public static String BUFFMGR_STRATEGY_STREAM = "2";

    public static final int minorCodeForTestExceptions = -45;

    public static String rmiiColocatedCallResult = 
        "makeColocatedCallFromServant colocatedCallFromServant makeColocatedCallFromServant makeColocatedCallFromServant makeColocatedCallFromServant makeColocatedCallFromServant makeColocatedCallFromServant";

    public static String idlSAI1ColocatedCallResult =
        "makeColocatedCallFromServant idlSAI2 colocatedCallFromServant idlSAI2 -- makeColocatedCallFromServant idlSAI2";

    public static String idlSLI1ColocatedResult =
        "makeColocatedCallFromServant idlSLI2 colocatedCallFromServant idlSLI1 -- makeColocatedCallFromServant idlSLI2";

    //
    // Initialization.
    //

    public static ORB createORB(String[] av, int fragmentSize)
    {
        return createORB(av, 
                         GIOP_VERSION_1_2, 
                         (fragmentSize > 0 ? BUFFMGR_STRATEGY_STREAM :
                                             BUFFMGR_STRATEGY_GROW),
                         fragmentSize);
    }

    public static ORB createORB(String [] av,
                                String giopVersion,
                                String buffMgrStrategy,
                                int fragmentSize)
    {
        Properties props = new Properties();
        props.put(U.ORBClass, MyPOAORB);
        props.put(U.ORBInitializerClass + "." + MyORBInitializer, "ignored");

        props.put(ORBConstants.GIOP_VERSION, giopVersion);
        props.put(ORBConstants.GIOP_11_BUFFMGR, buffMgrStrategy);
        props.put(ORBConstants.GIOP_12_BUFFMGR, buffMgrStrategy);
        if (fragmentSize > 0) {
            props.put(ORBConstants.GIOP_FRAGMENT_SIZE, "" + fragmentSize);
        }

        return ORB.init(av, props);
    }

    public static InitialContext createInitialContext(ORB orb)
        throws
            NamingException
    {
        Hashtable env = new Hashtable();                  
        env.put(U.javaNamingCorbaOrb, orb);          
        return new InitialContext(env); 
    }

    //
    // Colocation factoring.
    //

    public static String makeColocatedCallFromServant(String name, 
                                                      ORB orb, 
                                                      String delegator)
    {
        String result = null;
        try {
            result = U.getPOACurrentInfo(orb);

            idlSMI ridlSMI = null;
            ridlSMI = idlSMIHelper.narrow(U.resolve(name, orb));
            result = doCall(ridlSMI, result, orb);

        } catch (Exception e) {
            U.sopUnexpectedException(delegator + "." + 
                                     C.makeColocatedCallFromServant,
                                     e);
        }
        return result;
    }

    private static String doCall(idlSMI ridlSMI, String resultSoFar, ORB orb)
        throws
            Exception
    {
        String result = ridlSMI.colocatedCallFromServant(resultSoFar);
        String info = U.getPOACurrentInfo(orb);
        return info + " " + result;
    }

    public static String colocatedCallFromServant(String a, 
                                                  ORB orb,
                                                  String delegator)
    {
        String result = "";
        try {
            String now = U.getPOACurrentInfo(orb);
            result = now + " -- " + a;
        } catch (Exception e) {
            U.sopUnexpectedException(delegator + "." +
                                     C.colocatedCallFromServant,
                                     e);
        }
        return result;
    }

    public static void throwUserException(String message)
        throws
            idlExampleException
    {
        throw new idlExampleException(message);
    }

    public static void throwSystemException(String message)
    {
        throw new IMP_LIMIT(message, 
                            minorCodeForTestExceptions,
                            CompletionStatus.COMPLETED_NO);

    }

    public static void throwUnknownException(String message)
    {
        throw new UnknownException(new idlExampleException(message));
    }

    public static void throwUNKNOWN(String message)
    {
        throw new UNKNOWN(message,
                          minorCodeForTestExceptions,
                          CompletionStatus.COMPLETED_NO);       
    }

    //
    // Utility to help server-side PICurrent testing.
    //

    public static boolean testAndIncrementPICSlot(boolean ensure,
                                                  String message,
                                                  int id, 
                                                  int shouldBe,
                                                  ORB orb)
    {
        Current piCurrent = null;
        try {
            piCurrent = 
                CurrentHelper.narrow(
                    orb.resolve_initial_references(U.PICurrent));
        } catch (org.omg.CORBA.ORBPackage.InvalidName e) {
            U.sopShouldNotSeeThis("testAndIncrementPICSlot");
        }
        return testAndIncrementPICSlot(ensure, message,
                                       id, shouldBe, piCurrent);
    }

    public static boolean testAndIncrementPICSlot(boolean ensure,
                                                  String message,
                                                  int id,
                                                  int shouldBe,
                                                  Current piCurrent)
    {
        try {
            Any any = piCurrent.get_slot(id);
            if (U.isTkLong(any)) {
                int currentValue = any.extract_long();
                if (currentValue == shouldBe) {
                    any.insert_long(++currentValue);
                    piCurrent.set_slot(id, any);
                    return true;
                }
            }
            if (ensure) {
                U.sopShouldNotSeeThis(message);
            }
        } catch (org.omg.PortableInterceptor.InvalidSlot e) {
            U.sopShouldNotSeeThis("testAndIncrementPICSlot");
        }
        return false;
    }
}

// End of file.

