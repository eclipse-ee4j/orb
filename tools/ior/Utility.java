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
