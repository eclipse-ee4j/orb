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

package com.sun.corba.ee.impl.encoding;

import java.nio.ByteBuffer;

import com.sun.corba.ee.impl.protocol.giopmsgheaders.FragmentMessage;
import com.sun.org.omg.SendingContext.CodeBase;

import com.sun.corba.ee.spi.orb.ORB;

import com.sun.corba.ee.spi.transport.Connection;

import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;

import com.sun.corba.ee.impl.protocol.giopmsgheaders.Message;

import com.sun.corba.ee.spi.logging.ORBUtilSystemException;
import com.sun.corba.ee.spi.logging.OMGSystemException;

import com.sun.corba.ee.impl.misc.ORBUtility;
import com.sun.corba.ee.spi.protocol.MessageMediator;
import com.sun.corba.ee.spi.trace.Transport;
import com.sun.corba.ee.spi.trace.MonitorRead;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteOrder;

import org.omg.CORBA.Any;
import org.omg.CORBA.TypeCode;

/**
 * @author Harold Carr
 */
@Transport
@MonitorRead
public class CDRInputObject extends org.omg.CORBA_2_3.portable.InputStream implements com.sun.corba.ee.impl.encoding.MarshalInputStream,
        org.omg.CORBA.DataInputStream, org.omg.CORBA.portable.ValueInputStream {
    private static final ORBUtilSystemException wrapper = ORBUtilSystemException.self;
    private static final OMGSystemException omgWrapper = OMGSystemException.self;
    private static final long serialVersionUID = 3654171034620991056L;

    private transient ORB orb;
    private transient CDRInputStreamBase impl;
    private transient Connection corbaConnection;
    private transient Message header;
    protected transient MessageMediator messageMediator;

    // Present only to suppress FindBugs errors
    private void readObject(ObjectInputStream is) throws IOException, ClassNotFoundException {

        orb = null;
        impl = null;
        corbaConnection = null;
        header = null;
        messageMediator = null;
    }

    private boolean unmarshaledHeader;

    public void addFragment(FragmentMessage header, ByteBuffer byteBuffer) {
        getBufferManager().processFragment(byteBuffer, header);
    }

    public void cancelProcessing(int requestId) {
        BufferManagerReadStream bufferManager = (BufferManagerReadStream) getBufferManager();
        bufferManager.cancelProcessing(requestId);
    }

    private static class InputStreamFactory {
        public static CDRInputStreamBase newInputStream(GIOPVersion version) {
            switch (version.intValue()) {
            case GIOPVersion.VERSION_1_0:
                return new CDRInputStream_1_0();
            case GIOPVersion.VERSION_1_1:
                return new CDRInputStream_1_1();
            case GIOPVersion.VERSION_1_2:
                return new CDRInputStream_1_2();
            default:
                throw wrapper.unsupportedGiopVersion(version);
            }
        }
    }

    // Required for the case when a ClientResponseImpl is
    // created with a SystemException due to a dead server/closed
    // connection with no warning. Note that the stream will
    // not be initialized in this case.
    //
    // Probably also required by ServerRequestImpl.
    //
    // REVISIT.
    public CDRInputObject() {
    }

    public CDRInputObject(CDRInputObject is) {
        impl = is.impl.dup();
        impl.setParent(this);
    }

    protected CDRInputObject(org.omg.CORBA.ORB orb, ByteBuffer byteBuffer, int size, ByteOrder byteOrder, GIOPVersion version,
            BufferManagerRead bufMgr) {

        this.orb = (ORB) orb;
        createCDRInputStream(version, byteBuffer, size, byteOrder, bufMgr);

        this.corbaConnection = null;
        this.header = null;
        this.unmarshaledHeader = false;
        this.messageMediator = null;
    }

    private void createCDRInputStream(GIOPVersion version, ByteBuffer byteBuffer, int size, ByteOrder byteOrder, BufferManagerRead bufMgr) {
        impl = InputStreamFactory.newInputStream(version);
        impl.init(orb, byteBuffer, size, byteOrder, bufMgr);
        impl.setParent(this);
    }

    protected static ByteOrder toByteOrder(boolean littleEndian) {
        return littleEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;
    }

    public CDRInputObject(ORB orb, Connection corbaConnection, ByteBuffer byteBuffer, Message header) {
        this(orb, byteBuffer, header.getSize(), toByteOrder(header.isLittleEndian()), header.getGIOPVersion(),
                createBufferManagerRead(orb, header));

        this.corbaConnection = corbaConnection;
        this.header = header;

        getBufferManager().init(header);
        setIndex(Message.GIOPMessageHeaderLength);
        setBufferLength(header.getSize());
    }

    private static BufferManagerRead createBufferManagerRead(ORB orb, Message header) {
        return BufferManagerFactory.newBufferManagerRead(header.getGIOPVersion(), header.getEncodingVersion(), orb);
    }

    // REVISIT - think about this some more.
    // This connection normally is accessed from the message mediator.
    // However, giop input needs to get code set info from the connetion
    // *before* the message mediator is available.
    public final Connection getConnection() {
        return corbaConnection;
    }

    // XREVISIT - Should the header be kept in the stream or the
    // message mediator? Or should we not have a header and
    // have the information stored in the message mediator
    // directly?
    public Message getMessageHeader() {
        return header;
    }

    private void unmarshalledHeader(Message msg) {
    }

    /**
     * Unmarshal the extended GIOP header NOTE: May be fragmented, so should not be called by the ReaderThread. See
     * CorbaResponseWaitingRoomImpl.waitForResponse. It is done there in the client thread.
     */
    @Transport
    public void unmarshalHeader() {
        // Unmarshal the extended GIOP message from the buffer.

        if (!unmarshaledHeader) {
            try {
                getMessageHeader().read(this);
                unmarshalledHeader(getMessageHeader());
                unmarshaledHeader = true;
            } catch (RuntimeException e) {
                throw e;
            }
        }
    }

    /**
     * Override the default CDR factory behavior to get the negotiated code sets from the connection.
     *
     * These are only called once per message, the first time needed.
     *
     * In the local case, there is no Connection, so use the local code sets.
     * 
     * @return The converter.
     */
    protected CodeSetConversion.BTCConverter createCharBTCConverter() {
        CodeSetComponentInfo.CodeSetContext codesets = getCodeSets();

        // If the connection doesn't have its negotiated
        // code sets by now, fall back on the defaults defined
        // in CDRInputStream.
        if (codesets == null) {
            return CodeSetConversion.impl().getBTCConverter(OSFCodeSetRegistry.ISO_8859_1, impl.getByteOrder());
        }

        OSFCodeSetRegistry.Entry charSet = OSFCodeSetRegistry.lookupEntry(codesets.getCharCodeSet());

        if (charSet == null) {
            throw wrapper.unknownCodeset(codesets.getCharCodeSet());
        }

        return CodeSetConversion.impl().getBTCConverter(charSet, getByteOrder());
    }

    protected CodeSetConversion.BTCConverter createWCharBTCConverter() {

        CodeSetComponentInfo.CodeSetContext codesets = getCodeSets();

        // If the connection doesn't have its negotiated
        // code sets by now, we have to throw an exception.
        // See CORBA formal 00-11-03 13.9.2.6.
        if (codesets == null) {
            if (getConnection().isServer()) {
                throw omgWrapper.noClientWcharCodesetCtx();
            } else {
                throw omgWrapper.noServerWcharCodesetCmp();
            }
        }

        OSFCodeSetRegistry.Entry wcharSet = OSFCodeSetRegistry.lookupEntry(codesets.getWCharCodeSet());

        if (wcharSet == null) {
            throw wrapper.unknownCodeset(codesets.getWCharCodeSet());
        }

        // For GIOP 1.2 and UTF-16, use big endian if there is no byte
        // order marker. (See issue 3405b)
        //
        // For GIOP 1.1 and UTF-16, use the byte order the stream if
        // there isn't (and there shouldn't be) a byte order marker.
        //
        // GIOP 1.0 doesn't have wchars. If we're talking to a legacy ORB,
        // we do what our old ORBs did.
        if (wcharSet == OSFCodeSetRegistry.UTF_16) {
            if (getGIOPVersion().equals(GIOPVersion.V1_2)) {
                return CodeSetConversion.impl().getBTCConverter(wcharSet, ByteOrder.BIG_ENDIAN);
            }
        }

        return CodeSetConversion.impl().getBTCConverter(wcharSet, getByteOrder());
    }

    // If we're local and don't have a Connection, use the
    // local code sets, otherwise get them from the connection.
    // If the connection doesn't have negotiated code sets
    // yet, then we use ISO8859-1 for char/string and wchar/wstring
    // are illegal.
    private CodeSetComponentInfo.CodeSetContext getCodeSets() {
        if (getConnection() == null) {
            return CodeSetComponentInfo.LOCAL_CODE_SETS;
        } else {
            return getConnection().getCodeSetContext();
        }
    }

    public CodeBase getCodeBase() {
        if (getConnection() == null) {
            return null;
        } else {
            return getConnection().getCodeBase();
        }
    }

    public CDRInputObject dup() {
        return null;
    }

    protected void dprint(String msg) {
        ORBUtility.dprint("CDRInputObject", msg);
    }

    // org.omg.CORBA.portable.InputStream
    @MonitorRead
    public final boolean read_boolean() {
        return impl.read_boolean();
    }

    @MonitorRead
    public final char read_char() {
        return impl.read_char();
    }

    @MonitorRead
    public final char read_wchar() {
        return impl.read_wchar();
    }

    @MonitorRead
    public final byte read_octet() {
        return impl.read_octet();
    }

    @MonitorRead
    public final short read_short() {
        return impl.read_short();
    }

    @MonitorRead
    public final short read_ushort() {
        return impl.read_ushort();
    }

    @MonitorRead
    public final int read_long() {
        return impl.read_long();
    }

    @MonitorRead
    public final int read_ulong() {
        return impl.read_ulong();
    }

    @MonitorRead
    public final long read_longlong() {
        return impl.read_longlong();
    }

    @MonitorRead
    public final long read_ulonglong() {
        return impl.read_ulonglong();
    }

    @MonitorRead
    public final float read_float() {
        return impl.read_float();
    }

    @MonitorRead
    public final double read_double() {
        return impl.read_double();
    }

    @MonitorRead
    public final String read_string() {
        return impl.read_string();
    }

    @MonitorRead
    public final String read_wstring() {
        return impl.read_wstring();
    }

    @MonitorRead
    public final void read_boolean_array(boolean[] value, int offset, int length) {
        impl.read_boolean_array(value, offset, length);
    }

    @MonitorRead
    public final void read_char_array(char[] value, int offset, int length) {
        impl.read_char_array(value, offset, length);
    }

    @MonitorRead
    public final void read_wchar_array(char[] value, int offset, int length) {
        impl.read_wchar_array(value, offset, length);
    }

    @MonitorRead
    public final void read_octet_array(byte[] value, int offset, int length) {
        impl.read_octet_array(value, offset, length);
    }

    @MonitorRead
    public final void read_short_array(short[] value, int offset, int length) {
        impl.read_short_array(value, offset, length);
    }

    @MonitorRead
    public final void read_ushort_array(short[] value, int offset, int length) {
        impl.read_ushort_array(value, offset, length);
    }

    @MonitorRead
    public final void read_long_array(int[] value, int offset, int length) {
        impl.read_long_array(value, offset, length);
    }

    @MonitorRead
    public final void read_ulong_array(int[] value, int offset, int length) {
        impl.read_ulong_array(value, offset, length);
    }

    @MonitorRead
    public final void read_longlong_array(long[] value, int offset, int length) {
        impl.read_longlong_array(value, offset, length);
    }

    @MonitorRead
    public final void read_ulonglong_array(long[] value, int offset, int length) {
        impl.read_ulonglong_array(value, offset, length);
    }

    @MonitorRead
    public final void read_float_array(float[] value, int offset, int length) {
        impl.read_float_array(value, offset, length);
    }

    @MonitorRead
    public final void read_double_array(double[] value, int offset, int length) {
        impl.read_double_array(value, offset, length);
    }

    @MonitorRead
    public final org.omg.CORBA.Object read_Object() {
        return impl.read_Object();
    }

    @MonitorRead
    public final TypeCode read_TypeCode() {
        return impl.read_TypeCode();
    }

    @MonitorRead
    public final Any read_any() {
        return impl.read_any();
    }

    @MonitorRead
    @SuppressWarnings({ "deprecation" })
    @Override
    public final org.omg.CORBA.Principal read_Principal() {
        return impl.read_Principal();
    }

    @MonitorRead
    @Override
    public final int read() throws java.io.IOException {
        return impl.read();
    }

    @MonitorRead
    @Override
    public final java.math.BigDecimal read_fixed() {
        return impl.read_fixed();
    }

    @MonitorRead
    @Override
    public final org.omg.CORBA.Context read_Context() {
        return impl.read_Context();
    }

    @MonitorRead
    @Override
    public final org.omg.CORBA.Object read_Object(java.lang.Class clz) {
        return impl.read_Object(clz);
    }

    @Override
    public final org.omg.CORBA.ORB orb() {
        return impl.orb();
    }

    // org.omg.CORBA_2_3.portable.InputStream
    @MonitorRead
    @Override
    public final java.io.Serializable read_value() {
        return impl.read_value();
    }

    @MonitorRead
    @Override
    public final java.io.Serializable read_value(java.lang.Class clz) {
        return impl.read_value(clz);
    }

    @MonitorRead
    @Override
    public final java.io.Serializable read_value(org.omg.CORBA.portable.BoxedValueHelper factory) {

        return impl.read_value(factory);
    }

    @MonitorRead
    @Override
    public final java.io.Serializable read_value(java.lang.String rep_id) {
        return impl.read_value(rep_id);
    }

    @MonitorRead
    @Override
    public final java.io.Serializable read_value(java.io.Serializable value) {
        return impl.read_value(value);
    }

    @MonitorRead
    @Override
    public final java.lang.Object read_abstract_interface() {
        return impl.read_abstract_interface();
    }

    @MonitorRead
    @Override
    public final java.lang.Object read_abstract_interface(java.lang.Class clz) {
        return impl.read_abstract_interface(clz);
    }
    // com.sun.corba.ee.impl.encoding.MarshalInputStream

    @MonitorRead
    public final void consumeEndian() {
        impl.consumeEndian();
    }

    public final int getPosition() {
        return impl.getPosition();
    }

    // org.omg.CORBA.DataInputStream

    @MonitorRead
    public final java.lang.Object read_Abstract() {
        return impl.read_Abstract();
    }

    @MonitorRead
    public final java.io.Serializable read_Value() {
        return impl.read_Value();
    }

    @MonitorRead
    public final void read_any_array(org.omg.CORBA.AnySeqHolder seq, int offset, int length) {
        impl.read_any_array(seq, offset, length);
    }

    @MonitorRead
    public final void read_boolean_array(org.omg.CORBA.BooleanSeqHolder seq, int offset, int length) {
        impl.read_boolean_array(seq, offset, length);
    }

    @MonitorRead
    public final void read_char_array(org.omg.CORBA.CharSeqHolder seq, int offset, int length) {
        impl.read_char_array(seq, offset, length);
    }

    @MonitorRead
    public final void read_wchar_array(org.omg.CORBA.WCharSeqHolder seq, int offset, int length) {
        impl.read_wchar_array(seq, offset, length);
    }

    @MonitorRead
    public final void read_octet_array(org.omg.CORBA.OctetSeqHolder seq, int offset, int length) {
        impl.read_octet_array(seq, offset, length);
    }

    @MonitorRead
    public final void read_short_array(org.omg.CORBA.ShortSeqHolder seq, int offset, int length) {
        impl.read_short_array(seq, offset, length);
    }

    @MonitorRead
    public final void read_ushort_array(org.omg.CORBA.UShortSeqHolder seq, int offset, int length) {
        impl.read_ushort_array(seq, offset, length);
    }

    @MonitorRead
    public final void read_long_array(org.omg.CORBA.LongSeqHolder seq, int offset, int length) {
        impl.read_long_array(seq, offset, length);
    }

    @MonitorRead
    public final void read_ulong_array(org.omg.CORBA.ULongSeqHolder seq, int offset, int length) {
        impl.read_ulong_array(seq, offset, length);
    }

    @MonitorRead
    public final void read_ulonglong_array(org.omg.CORBA.ULongLongSeqHolder seq, int offset, int length) {
        impl.read_ulonglong_array(seq, offset, length);
    }

    @MonitorRead
    public final void read_longlong_array(org.omg.CORBA.LongLongSeqHolder seq, int offset, int length) {
        impl.read_longlong_array(seq, offset, length);
    }

    @MonitorRead
    public final void read_float_array(org.omg.CORBA.FloatSeqHolder seq, int offset, int length) {
        impl.read_float_array(seq, offset, length);
    }

    @MonitorRead
    public final void read_double_array(org.omg.CORBA.DoubleSeqHolder seq, int offset, int length) {
        impl.read_double_array(seq, offset, length);
    }

    // org.omg.CORBA.portable.ValueBase
    public final String[] _truncatable_ids() {
        return impl._truncatable_ids();
    }

    // java.io.InputStream
    @MonitorRead
    @Override
    public final int read(byte b[]) throws IOException {
        return impl.read(b);
    }

    @MonitorRead
    @Override
    public final int read(byte b[], int off, int len) throws IOException {
        return impl.read(b, off, len);
    }

    @MonitorRead
    @Override
    public final long skip(long n) throws IOException {
        return impl.skip(n);
    }

    @Override
    public final int available() throws IOException {
        return impl.available();
    }

    @MonitorRead
    @Override
    public void close() throws IOException {
        impl.close();
    }

    @MonitorRead
    @Override
    public final void mark(int readlimit) {
        impl.mark(readlimit);
    }

    @MonitorRead
    @Override
    public final void reset() {
        impl.reset();
    }

    @Override
    public final boolean markSupported() {
        return impl.markSupported();
    }

    // Needed by TCUtility
    @MonitorRead
    public final java.math.BigDecimal read_fixed(short digits, short scale) {
        return impl.read_fixed(digits, scale);
    }

    public final ByteOrder getByteOrder() {
        return impl.getByteOrder();
    }

    public final int getBufferLength() {
        return impl.getBufferLength();
    }

    protected final void setBufferLength(int value) {
        impl.setBufferLength(value);
    }

    protected final void setIndex(int value) {
        impl.setIndex(value);
    }

    public final void orb(org.omg.CORBA.ORB orb) {
        impl.orb(orb);
    }

    public final GIOPVersion getGIOPVersion() {
        return impl.getGIOPVersion();
    }

    public final BufferManagerRead getBufferManager() {
        return impl.getBufferManager();
    }

    /**
     * Aligns the current position on the given octet boundary if there are enough bytes available to do so. Otherwise, it
     * just returns. This is used for some (but not all) GIOP 1.2 message headers.
     * 
     * @param octetBoundary alignment boundary.
     */
    @MonitorRead
    public void alignOnBoundary(int octetBoundary) {
        impl.alignOnBoundary(octetBoundary);
    }

    // Needed by request and reply messages for GIOP versions >= 1.2 only.
    @MonitorRead
    public void setHeaderPadding(boolean headerPadding) {
        impl.setHeaderPadding(headerPadding);
    }

    /**
     * This must be called after determining the proper ORB version, and setting it on the stream's ORB instance. It can be
     * called after reading the service contexts, since that is the only place we can get the ORB version info.
     *
     * Trying to unmarshal things requiring repository IDs before calling this will result in NullPtrExceptions.
     */
    public void performORBVersionSpecificInit() {
        // In the case of SystemExceptions, a stream is created
        // with its default constructor (and thus no impl is set).
        if (impl != null) {
            impl.performORBVersionSpecificInit();
        }
    }

    /**
     * Resets any internal references to code set converters. This is useful for forcing the CDR stream to reacquire
     * converters (probably from its subclasses) when state has changed.
     */
    public void resetCodeSetConverters() {
        impl.resetCodeSetConverters();
    }

    public void setMessageMediator(MessageMediator messageMediator) {
        this.messageMediator = messageMediator;
    }

    public MessageMediator getMessageMediator() {
        return messageMediator;
    }

    // ValueInputStream -----------------------------
    @MonitorRead
    public void start_value() {
        impl.start_value();
    }

    @MonitorRead
    public void end_value() {
        impl.end_value();
    }
}

// End of file.
