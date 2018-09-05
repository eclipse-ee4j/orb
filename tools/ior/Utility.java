/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package tools.ior;

import java.util.Map;
import java.io.Serializable;
import org.omg.CORBA.Any;
import org.omg.IOP.*;
import org.omg.IOP.CodecFactoryPackage.*;
import org.omg.IIOP.Version;

/**
 * Interface to provide helpful methods to
 * EncapsHandler implementations.
 */
public interface Utility
{
    /**
     * Constants for Codec selection. See getCDREncapsCodec.
     */
    public Version GIOP_1_0 = new Version((byte)1, (byte)0);
    public Version GIOP_1_1 = new Version((byte)1, (byte)1);
    public Version GIOP_1_2 = new Version((byte)1, (byte)2);

    /**
     * If writing one's own EncapsHandler, use Codecs to
     * interpret the given byte array.
     */
    public CodecFactory getCodecFactory();

    public Codec getCDREncapsCodec(Version giopVersion)
        throws UnknownEncoding;

    /**
     * Get the ORB instance.  Useful for generating TypeCodes.
     */
    org.omg.CORBA.ORB getORB();

    /**
     * Pretty print the given byte buffer as hex with
     * ASCII interpretation on the side.
     */
    public void printBuffer(byte[] buffer, TextOutputHandler out);

    /**
     * Recursively display the fields of the given Object.
     *
     * Breaks apart array types.  All core Java classes (classes
     * with names beginning with "java") are directly printed
     * with toString.
     */
    public void recursiveDisplay(String name, Object object, TextOutputHandler out);
}
