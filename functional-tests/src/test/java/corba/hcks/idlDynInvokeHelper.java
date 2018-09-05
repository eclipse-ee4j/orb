/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

//
// Created       : 1999 Mar 01 (Mon) 16:59:34 by Harold Carr.
// Last Modified : 2002 Dec 04 (Wed) 21:44:06 by Harold Carr.
//

package corba.hcks;

import org.omg.CORBA.*;
import org.omg.CORBA.portable.UnknownException;
import org.omg.PortableServer.*;
import java.util.Properties;
import com.sun.corba.ee.impl.misc.ORBUtility;

class idlDynInvokeHelper 
{

    public static boolean dprint = false;

    public static boolean throwSystemException = true;

    static void invoke(ORB orb, ServerRequest r)
    {
        try {
            if (r.op_name().equals(C.syncOK) ) {

                // syncOK
                
                NVList nvlist = orb.create_list(0);

                Any a1 = orb.create_any();
                a1.type(ORB.init().get_primitive_tc(TCKind.tk_string));
                nvlist.add_value("arg1", a1, ARG_IN.value);

                // get values
                r.arguments(nvlist);
                String arg1 = a1.extract_string();

                U.sop(r.op_name() + "(" + arg1 + ")");

                Any __result = orb.create_any();
                __result.insert_string(U.DSI(arg1));
                r.set_result(__result);

            } else if (r.op_name().equals(C.asyncOK) ) {

                // asyncOK

                NVList nvlist = orb.create_list(0);

                Any a1 = orb.create_any();
                a1.type(idlJStringHelper.type());
                nvlist.add_value("arg1", a1, ARG_IN.value);

                r.arguments(nvlist);

                byte[] data = idlJStringHelper.extract(a1);
                U.sop(new String(data, C.UTF8));
                
                Any __return = orb.create_any();
                __return.type(orb.get_primitive_tc(TCKind.tk_void));
                r.set_result(__return);

            } else if (r.op_name().equals(C.throwUserException) ) {

                // throwUserException

                idlExampleException ex = null;
                try {
                    C.throwUserException(U.from_a_idlDynamic_servant);
                    throw new INTERNAL(U.SHOULD_NOT_SEE_THIS);
                } catch (idlExampleException e) {
                    ex = e;
                }

                Any any = orb.create_any();
                idlExampleExceptionHelper.insert(any, ex);
                r.set_exception(any);
                return;

            } else if (r.op_name().equals(C.throwSystemException) ) {

                // throwSystemException

                SystemException ex = null;
                try {
                    C.throwSystemException(U.from_a_idlDynamic_servant);
                    throw new INTERNAL(U.SHOULD_NOT_SEE_THIS);
                } catch (SystemException e) {
                    ex = e;
                }

                if (throwSystemException) {
                    throwSystemException = false;
                    throw ex;
                } else {
                    throwSystemException = true;
                    Any any = orb.create_any();
                    ORBUtility.insertSystemException(ex, any);
                    r.set_exception(any);
                    return;
                }
            } else if (r.op_name().equals(C.throwUnknownException)) {

                // throwUnknownException

                UnknownException ex = null;
                try {
                    C.throwUnknownException(U.from_a_idlDynamic_servant);
                    throw new INTERNAL(U.SHOULD_NOT_SEE_THIS);
                } catch (UnknownException e) {
                    ex = e;
                }
                        
                Any any = orb.create_any();
                ORBUtility.insertSystemException(ex, any);
                r.set_exception(any);
                return;


            } else if (r.op_name().equals(C.throwUNKNOWN)) {

                C.throwUNKNOWN(U.from_a_idlDynamic_servant);

            }

        } catch (SystemException ex) {
            // REVISIT - probably time to remove this catch clause.
            U.sop("DynamicServant.invoke SystemException: " + ex);
            throw ex;
        } catch (Exception ex) {
            U.sop("DynamicServant.invoke Exception: " + ex);
            ex.printStackTrace(System.err);
        }
    }
}

// End of file.
