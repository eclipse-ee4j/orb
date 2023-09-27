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

package com.sun.corba.ee.spi.ior.iiop;

import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.ior.iiop.IIOPProfile;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.orb.ORBVersion;
import com.sun.corba.ee.spi.orb.ORBVersionFactory;

import com.sun.corba.ee.impl.misc.ORBUtility;
import com.sun.corba.ee.spi.misc.ORBConstants;

import org.glassfish.gmbal.ManagedData;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.Description;

@ManagedData
@Description("The maximum GIOP version supported by this IOR")
public class GIOPVersion {

    // Static fields

    public static final GIOPVersion V1_0 = new GIOPVersion((byte) 1, (byte) 0);
    public static final GIOPVersion V1_1 = new GIOPVersion((byte) 1, (byte) 1);
    public static final GIOPVersion V1_2 = new GIOPVersion((byte) 1, (byte) 2);
    public static final GIOPVersion V1_3 = new GIOPVersion((byte) 1, (byte) 3);

    // Major version 13 is used to denote special encodings.
    // Minor version 00 is unused.
    // Minor version [01-FF] specifies Java serialization encoding version.
    public static final GIOPVersion V13_XX = new GIOPVersion((byte) 13, (byte) ORBConstants.JAVA_ENC_VERSION);

    public static final GIOPVersion DEFAULT_VERSION = V1_2;

    public static final int VERSION_1_0 = 0x0100;
    public static final int VERSION_1_1 = 0x0101;
    public static final int VERSION_1_2 = 0x0102;
    public static final int VERSION_1_3 = 0x0103;
    public static final int VERSION_13_XX = (13 << 8) | ORBConstants.JAVA_ENC_VERSION;

    // Instance variables

    private byte major = (byte) 0;
    private byte minor = (byte) 0;

    // Constructor

    public GIOPVersion() {
    }

    public GIOPVersion(byte majorB, byte minorB) {
        this.major = majorB;
        this.minor = minorB;
    }

    public GIOPVersion(int major, int minor) {
        this.major = (byte) major;
        this.minor = (byte) minor;
    }

    // Accessor methods

    @ManagedAttribute
    @Description("The Major GIOP version (almost always 1)")
    public byte getMajor() {
        return this.major;
    }

    @ManagedAttribute
    @Description("The Minor GIOP version (almost always 0, 1, or 2." + " This ORB almost always uses 2")
    public byte getMinor() {
        return this.minor;
    }

    // General methods

    public boolean equals(GIOPVersion gv) {
        if (gv == null) {
            return false;
        }

        return gv.major == this.major && gv.minor == this.minor;
    }

    public boolean equals(Object obj) {
        if (obj != null && (obj instanceof GIOPVersion))
            return equals((GIOPVersion) obj);
        else
            return false;
    }

    public int hashCode() {
        return 37 * major + minor;
    }

    public boolean lessThan(GIOPVersion gv) {
        if (this.major < gv.major) {
            return true;
        } else if (this.major == gv.major) {
            if (this.minor < gv.minor) {
                return true;
            }
        }

        return false;
    }

    public int intValue() {
        return (major << 8 | minor);
    }

    public String toString() {
        return major + "." + minor;
    }

    public static GIOPVersion getInstance(byte major, byte minor) {
        switch (((major << 8) | minor)) {
        case VERSION_1_0:
            return GIOPVersion.V1_0;
        case VERSION_1_1:
            return GIOPVersion.V1_1;
        case VERSION_1_2:
            return GIOPVersion.V1_2;
        case VERSION_1_3:
            return GIOPVersion.V1_3;
        case VERSION_13_XX:
            return GIOPVersion.V13_XX;
        default:
            return new GIOPVersion(major, minor);
        }
    }

    public static GIOPVersion parseVersion(String s) {
        int dotIdx = s.indexOf('.');

        if (dotIdx < 1 || dotIdx == s.length() - 1)
            throw new NumberFormatException("GIOP major, minor, and decimal point required: " + s);

        int major = Integer.parseInt(s.substring(0, dotIdx));
        int minor = Integer.parseInt(s.substring(dotIdx + 1, s.length()));

        return GIOPVersion.getInstance((byte) major, (byte) minor);
    }

    /**
     * This chooses the appropriate GIOP version.
     *
     * @param orb ORB to use
     * @param ior IOR to use
     * @return smallest(profGIOPVersion, orbGIOPVersion).
     */
    public static GIOPVersion chooseRequestVersion(ORB orb, IOR ior) {

        GIOPVersion orbVersion = orb.getORBData().getGIOPVersion();
        IIOPProfile prof = ior.getProfile();
        GIOPVersion profVersion = prof.getGIOPVersion();

        // Check if the profile is from a legacy Sun ORB.

        ORBVersion targetOrbVersion = prof.getORBVersion();
        if (!(targetOrbVersion.equals(ORBVersionFactory.getFOREIGN())) && targetOrbVersion.lessThan(ORBVersionFactory.getNEWER())) {
            // we are dealing with a SUN legacy orb which emits 1.1 IORs,
            // in spite of being able to handle only GIOP 1.0 messages.
            return V1_0;
        }

        // Now the target has to be (FOREIGN | NEWER*)

        byte prof_major = profVersion.getMajor();
        byte prof_minor = profVersion.getMinor();

        byte orb_major = orbVersion.getMajor();
        byte orb_minor = orbVersion.getMinor();

        if (orb_major < prof_major) {
            return orbVersion;
        } else if (orb_major > prof_major) {
            return profVersion;
        } else { // both major version are the same
            if (orb_minor <= prof_minor) {
                return orbVersion;
            } else {
                return profVersion;
            }
        }
    }

    public boolean supportsIORIIOPProfileComponents() {
        return getMinor() > 0 || getMajor() > 1;
    }

    // IO methods

    public void read(org.omg.CORBA.portable.InputStream istream) {
        this.major = istream.read_octet();
        this.minor = istream.read_octet();
    }

    public void write(org.omg.CORBA.portable.OutputStream ostream) {
        ostream.write_octet(this.major);
        ostream.write_octet(this.minor);
    }
}
