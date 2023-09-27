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

package com.sun.corba.ee.impl.ior;

import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.CORBA_2_3.portable.OutputStream;

import com.sun.corba.ee.spi.protocol.ServerRequestDispatcher;

import com.sun.corba.ee.spi.ior.ObjectId;
import com.sun.corba.ee.spi.ior.ObjectAdapterId;
import com.sun.corba.ee.spi.ior.ObjectKeyTemplate;

import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.orb.ORBVersion;

import com.sun.corba.ee.spi.logging.IORSystemException;

/**
 * @author Ken Cavanaugh
 */
public abstract class ObjectKeyTemplateBase implements ObjectKeyTemplate {
    protected static final IORSystemException wrapper = IORSystemException.self;

    // Fixed constants for Java IDL object key template forms
    public static final String JIDL_ORB_ID = "";
    private static final String[] JIDL_OAID_STRINGS = { "TransientObjectAdapter" };
    public static final ObjectAdapterId JIDL_OAID = new ObjectAdapterIdArray(JIDL_OAID_STRINGS);

    private ORB orb;
    private ORBVersion version;
    private int magic;
    private int scid;
    private int serverid;
    private String orbid;
    private ObjectAdapterId oaid;

    private byte[] adapterId;

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ObjectKeyTemplate[magic=");
        sb.append(Integer.toHexString(magic));
        sb.append(" scid=");
        sb.append(scid);
        sb.append(" serverid=");
        sb.append(serverid);
        sb.append(" orbid=");
        sb.append(orbid);
        sb.append(" oaid=");
        sb.append(oaid.toString());
        return sb.toString();
    }

    public synchronized byte[] getAdapterId() {
        if (adapterId == null) {
            adapterId = computeAdapterId();
        }

        return (byte[]) (adapterId.clone());
    }

    private byte[] computeAdapterId() {
        // write out serverid, orbid, oaid
        ByteBuffer buff = new ByteBuffer();

        buff.append(getServerId());
        buff.append(orbid);

        buff.append(oaid.getNumLevels());
        for (String comp : oaid) {
            buff.append(comp);
        }

        buff.trimToSize();

        return buff.toArray();
    }

    /**
     * This constructor reads a complete ObjectKey (template and Id) from the stream.
     * 
     * @param orb ORB to use
     * @param magic Magic number
     * @param scid ID of the Object
     * @param serverid server ID
     * @param orbid orbid
     * @param oaid oaid
     */
    public ObjectKeyTemplateBase(ORB orb, int magic, int scid, int serverid, String orbid, ObjectAdapterId oaid) {
        this.orb = orb;
        this.magic = magic;
        this.scid = scid;
        this.serverid = serverid;
        this.orbid = orbid;
        this.oaid = oaid;

        adapterId = null;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ObjectKeyTemplateBase))
            return false;

        ObjectKeyTemplateBase other = (ObjectKeyTemplateBase) obj;

        return (magic == other.magic) && (scid == other.scid) && (serverid == other.serverid)
                && (version.equals(other.version) && orbid.equals(other.orbid) && oaid.equals(other.oaid));
    }

    public int hashCode() {
        int result = 17;
        result = 37 * result + magic;
        result = 37 * result + scid;
        result = 37 * result + serverid;
        result = 37 * result + version.hashCode();
        result = 37 * result + orbid.hashCode();
        result = 37 * result + oaid.hashCode();
        return result;
    }

    public int getSubcontractId() {
        return scid;
    }

    public int getServerId() {
        return serverid;
    }

    public String getORBId() {
        return orbid;
    }

    public ObjectAdapterId getObjectAdapterId() {
        return oaid;
    }

    public void write(ObjectId objectId, OutputStream os) {
        writeTemplate(os);
        objectId.write(os);
    }

    public void write(OutputStream os) {
        writeTemplate(os);
    }

    abstract protected void writeTemplate(OutputStream os);

    protected int getMagic() {
        return magic;
    }

    // All subclasses should set the version in their constructors.
    // Public so it can be used in a white-box test.
    public void setORBVersion(ORBVersion version) {
        this.version = version;
    }

    public ORBVersion getORBVersion() {
        return version;
    }

    protected byte[] readObjectKey(InputStream is) {
        int len = is.read_long();
        byte[] result = new byte[len];
        is.read_octet_array(result, 0, len);
        return result;
    }

    public ServerRequestDispatcher getServerRequestDispatcher(ObjectId id) {

        return orb.getRequestDispatcherRegistry().getServerRequestDispatcher(scid);
    }
}
