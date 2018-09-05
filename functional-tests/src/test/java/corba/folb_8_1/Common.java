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
// Created       : 2002 Jul 19 (Fri) 14:47:13 by Harold Carr.
// Last Modified : 2005 Sep 28 (Wed) 14:55:27 by Harold Carr.
//

package corba.folb_8_1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.omg.CORBA.ORB;
import org.omg.CORBA.Policy;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;
import org.omg.CosNaming.*;
import org.omg.IOP.Codec;
import org.omg.IOP.CodecFactory;
import org.omg.IOP.CodecFactoryHelper;
import org.omg.IOP.Encoding;
import org.omg.IOP.ENCODING_CDR_ENCAPS;

import com.sun.corba.ee.spi.extension.ZeroPortPolicy;

/**
 * @author Harold Carr
 */
public abstract class Common
{
    public static boolean timing = false;
    public static final String FAILOVER_SUPPORT = "FAILOVER_SUPPORT";
    public static final String FAILOVER         = "FAILOVER";
    public static final String CACHE            = "CACHE";

    public static final String W = "W";
    public static final String X = "X";
    public static final String Y = "Y";
    public static final String Z = "Z";

    public static String[] socketTypes = { W,    X,    Y,    Z };
    public static int[]    socketPorts = { 3333, 4444, 5555, 0 };
    public static int[]    zero2Ports  = { 3334, 4445, 5556, 0 };
    public static HashMap socketTypeToPort = new HashMap();
    public static HashMap portToSocketType = new HashMap();
    static {
        for (int i = 0; i < socketTypes.length; i++) {
            Integer port = new Integer(socketPorts[i]);
            socketTypeToPort.put(socketTypes[i], port);
            portToSocketType.put(port, socketTypes[i]);
        }
    }
    public static final String serverName1 = "I1";
    public static final String serverName2 = "I2";
    public static final String zero1 = "zero1";
    public static final String zero2 = "zero2";

    public static POA createPOA(String name, boolean zeroPortP, ORB orb)
        throws Exception
    {
        // Get rootPOA

        POA rootPoa = (POA) orb.resolve_initial_references("RootPOA");
        rootPoa.the_POAManager().activate();

        // Create child

        List policies = new ArrayList();

        // Create child POA
        policies.add(
            rootPoa.create_lifespan_policy(LifespanPolicyValue.TRANSIENT));
        if (zeroPortP) {
            policies.add(ZeroPortPolicy.getPolicy());
        }
        Policy[] policy = (Policy[]) policies.toArray(new Policy[0]);
        POA childPoa = rootPoa.create_POA(name, null, policy);
        childPoa.the_POAManager().activate();
        return childPoa;
    }
        
    // create servant and register it with a POA
    public static org.omg.CORBA.Object createAndBind(String name,
                                                     ORB orb, POA poa)
        throws Exception
    {
        Servant servant;
        if (name.equals(Common.serverName1)) {
            servant = new IServant(orb);
        } else {
            servant = new I2Servant(orb);
        }
        byte[] id = poa.activate_object(servant);
        org.omg.CORBA.Object ref = poa.id_to_reference(id);
        Common.getNameService(orb).rebind(Common.makeNameComponent(name), ref);
        return ref;
    }

    public static NamingContext getNameService(ORB orb)
    {
        org.omg.CORBA.Object objRef = null;
        try {
            objRef = orb.resolve_initial_references("NameService");
        } catch (Exception ex) {
            System.out.println("Common.getNameService: " + ex);
            System.exit(1);
        }
        return NamingContextHelper.narrow(objRef);
    }

    public static NameComponent[] makeNameComponent(String name)
    {
        NameComponent nc = new NameComponent(name, "");
        NameComponent path[] = {nc};
        return path;
    }


    public static Codec getCodec(ORB orb)
    {
        try {
            CodecFactory codecFactory = 
                CodecFactoryHelper.narrow(orb.resolve_initial_references("CodecFactory"));
            return codecFactory.create_codec(new Encoding((short)ENCODING_CDR_ENCAPS.value, (byte)1, (byte)2));
        } catch (Exception e) {
            System.out.println("Unexpected: " + e);
            System.exit(1);
        }
        return null;
    }

    public static String[] concat(String[] a1, String[] a2)
    {
        String[] result = new String[a1.length + a2.length];

        int index = 0;
        
        for (int i = 0; i < a1.length; ++i) {
            result[index++] = a1[i];
        }

        for (int i = 0; i < a2.length; ++i) {
            result[index++] = a2[i];
        }

        /*
        System.out.println(formatStringArray(a1));
        System.out.println(formatStringArray(a2));
        System.out.println(formatStringArray(result));
        */

        return result;
    }

    public static String formatStringArray(String[] a)
    {
        String result = "";
        for (int i = 0; i < a.length; ++i) {
            result += a[i] + " ";
        }
        return result;
    }
}

// End of file.

