/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.encoding;

import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.protocol.MessageMediator ;
import com.sun.corba.ee.spi.transport.TransportManager;
import com.sun.corba.ee.spi.transport.Connection;

import com.sun.corba.ee.impl.misc.ORBUtility;
import com.sun.corba.ee.spi.logging.ORBUtilSystemException;
import com.sun.corba.ee.spi.logging.OMGSystemException;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.Message;
import com.sun.corba.ee.impl.transport.MessageTraceManagerImpl;

import java.io.IOException ;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import org.omg.CORBA.Any;
import org.omg.CORBA.DataOutputStream;
import org.omg.CORBA.TypeCode;

import com.sun.corba.ee.spi.trace.CdrWrite ;
import org.omg.CORBA.portable.BoxedValueHelper;
import org.omg.CORBA.portable.ValueOutputStream;

/**
 * @author Harold Carr
 */
@CdrWrite
public class CDROutputObject extends org.omg.CORBA_2_3.portable.OutputStream
                             implements MarshalOutputStream, DataOutputStream, ValueOutputStream {

    private static final CDRInputObjectFactory INPUT_OBJECT_FACTORY = new CDRInputObjectFactory();
    protected static final ORBUtilSystemException wrapper = ORBUtilSystemException.self ;
    private static final OMGSystemException omgWrapper = OMGSystemException.self ;
    private static final long serialVersionUID = -3801946738338642735L;

    private transient CDROutputStreamBase impl;

    private transient Message header;
    private transient MessageMediator corbaMessageMediator;
    private transient Connection connection;

    // todo this is only used in a pair of legacy tests. Rewrite them as unit tests and remove this method.
    public void sendFirstFragment() {
        ByteBuffer buffer = getBufferManager().overflow(impl.getByteBuffer(), 0);
        setPrivateFieldValue(impl, "byteBuffer", buffer);
    }

    protected void setPrivateFieldValue(Object obj, String fieldName, Object value) {
        try {
            Class theClass = obj.getClass();
            setPrivateFieldValue(obj, theClass, fieldName, value);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void setPrivateFieldValue(Object obj, Class theClass, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        try {
            Field field = theClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (NoSuchFieldException e) {
            if (theClass.equals(Object.class))
                throw e;
            else
                setPrivateFieldValue(obj, theClass.getSuperclass(), fieldName, value);
        }
    }

    // This needed only to get FindBugs to shut up about transient fields.
    // Should never be called.
    private void readObject( ObjectInputStream is ) throws IOException,
        ClassNotFoundException {
        impl = null ;
        corbaMessageMediator = null ;
        connection = null ;
        throw new IllegalStateException( "Should not be called" ) ;
    }

    @CdrWrite
    private void createCDROutputStream(ORB orb, GIOPVersion version,
                                       BufferManagerWrite bufferManager,
                                       byte streamFormatVersion, boolean usePooledByteBuffers) {
        impl = OutputStreamFactory.newOutputStream(version);
        impl.init(orb, bufferManager, streamFormatVersion, usePooledByteBuffers);
        impl.setParent(this);
    }

    public CDROutputObject(ORB orb, GIOPVersion version,
                           BufferManagerWrite bufferManager,
                           byte streamFormatVersion, boolean usePooledByteBuffers) {
        createCDROutputStream( orb, version, bufferManager, streamFormatVersion, usePooledByteBuffers) ;

        this.header = null ;
        this.corbaMessageMediator = null ;
        this.connection = null ;
    }


    private CDROutputObject( ORB orb, GIOPVersion giopVersion,
                             Message header, BufferManagerWrite manager,
                             byte streamFormatVersion, MessageMediator mediator) {
        this(orb, giopVersion, manager, streamFormatVersion, usePooledBuffers(mediator)) ;

        this.header = header;
        this.corbaMessageMediator = mediator;

        getBufferManager().setOutputObject(this);
    }

    private static boolean usePooledBuffers(MessageMediator mediator) {
        return (mediator != null && mediator.getConnection() != null) && mediator.getConnection().hasSocketChannel();
    }

    public CDROutputObject(ORB orb, MessageMediator messageMediator, Message header, byte streamFormatVersion) {
        this(orb, messageMediator.getGIOPVersion(), header,
                BufferManagerFactory.newBufferManagerWrite(messageMediator.getGIOPVersion(), header.getEncodingVersion(), orb),
                streamFormatVersion, messageMediator);
    }

    // NOTE: 
    // Used in SharedCDR (i.e., must be grow).
    // Used in msgtypes test.
    public CDROutputObject(ORB orb, MessageMediator messageMediator,
                           Message header,
                           byte streamFormatVersion,
                           int strategy) {
        this(orb, messageMediator.getGIOPVersion(), header,
                BufferManagerFactory.newBufferManagerWrite(strategy, header.getEncodingVersion(), orb),
                streamFormatVersion, messageMediator);
    }

    // REVISIT 
    // Used on sendCancelRequest.
    // Used for needs addressing mode.
    public CDROutputObject(ORB orb, MessageMediator mediator, GIOPVersion giopVersion,
                           Connection connection, Message header, byte streamFormatVersion) {

        this( orb, giopVersion, header, 
              BufferManagerFactory.newBufferManagerWrite( giopVersion, header.getEncodingVersion(), orb),
              streamFormatVersion, mediator ) ;
        this.connection = connection ;
    }

    // XREVISIT
    // Header should only be in message mediator.
    // Another possibility: merge header and message mediator.
    // REVISIT - make protected once all encoding together
    public Message getMessageHeader() {
        return header;
    }

    public final void finishSendingMessage() {
        getBufferManager().sendMessage();
    }

    /*
     * Write the contents of the CDROutputStream to the specified
     * output stream.  Has the side-effect of pushing any current
     * Message onto the Message list.
     */
    public void writeTo(Connection connection)  throws java.io.IOException {
        //
        // Update the GIOP MessageHeader size field.
        //

        ByteBuffer byteBuffer = impl.getByteBuffer();

        getMessageHeader().setSize(byteBuffer, byteBuffer.position());

        ORB lorb = (ORB)orb() ;
        if (lorb != null) {
            TransportManager ctm = lorb.getTransportManager() ;
            MessageTraceManagerImpl mtm = (MessageTraceManagerImpl)ctm.getMessageTraceManager() ;
            if (mtm.isEnabled())
                mtm.recordDataSent(byteBuffer) ;
        }

        byteBuffer.flip();
        connection.write(byteBuffer);
    }

    /** overrides create_input_stream from CDROutputStream */
    public org.omg.CORBA.portable.InputStream create_input_stream() {
        // XREVISIT
        return null;
    }

    public Connection getConnection() {
        // REVISIT - only set when doing sendCancelRequest.
        if (connection != null) {
            return connection;
        }
        return corbaMessageMediator.getConnection();
    }

    // todo this is only used in a legacy test - rewrite the test as a unit test and remove this method
    public final int getBufferPosition() {
        return impl.getByteBuffer().position();
    }

    /*
     * Override the default CDR factory behavior to get the
     * negotiated code sets from the connection.
     *
     * These are only called once per message, the first time needed.
     *
     * In the local case, there is no Connection, so use the
     * local code sets.
     */
    protected CodeSetConversion.CTBConverter createCharCTBConverter() {
        CodeSetComponentInfo.CodeSetContext codesets = getCodeSets();

        // If the connection doesn't have its negotiated
        // code sets by now, fall back on the defaults defined
        // in CDRInputStream.
        if (codesets == null) {
            return CodeSetConversion.impl().getCTBConverter(OSFCodeSetRegistry.ISO_8859_1);
        }

        OSFCodeSetRegistry.Entry charSet
            = OSFCodeSetRegistry.lookupEntry(codesets.getCharCodeSet());

        if (charSet == null) {
            throw wrapper.unknownCodeset(null);
        }

        return CodeSetConversion.impl().getCTBConverter(charSet, false, false);
    }

    protected CodeSetConversion.CTBConverter createWCharCTBConverter() {

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

        OSFCodeSetRegistry.Entry wcharSet
            = OSFCodeSetRegistry.lookupEntry(codesets.getWCharCodeSet());

        if (wcharSet == null) {
            throw wrapper.unknownCodeset(null);
        }

        boolean useByteOrderMarkers
            = ((ORB)orb()).getORBData().useByteOrderMarkers();

        // With UTF-16:
        //
        // For GIOP 1.2, we can put byte order markers if we want to, and
        // use the default of big endian otherwise.  (See issue 3405b)
        //
        // For GIOP 1.1, we don't use BOMs and use the endianness of
        // the stream.
        if (wcharSet == OSFCodeSetRegistry.UTF_16) {
            if (getGIOPVersion().equals(GIOPVersion.V1_2)) {
                return CodeSetConversion.impl().getCTBConverter(wcharSet, false, useByteOrderMarkers);
            }

            if (getGIOPVersion().equals(GIOPVersion.V1_1)) {
                return CodeSetConversion.impl().getCTBConverter(wcharSet, false, false);
            }
        }

        // In the normal case, let the converter system handle it
        return CodeSetConversion.impl().getCTBConverter(wcharSet, false, useByteOrderMarkers);
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

    protected void dprint(String msg)
    {
        ORBUtility.dprint("CDROutputObject", msg);
    }

    public void setMessageMediator(MessageMediator messageMediator)
    {
        this.corbaMessageMediator = messageMediator;
    }

    public MessageMediator getMessageMediator()
    {
        return corbaMessageMediator;
    }

    public CDRInputObject createInputObject(ORB orb) {
        return createInputObject(orb, INPUT_OBJECT_FACTORY);
    }

    protected CDRInputObject createInputObject(ORB orb, InputObjectFactory factory) {
        CDRInputObject inputObject = factory.createInputObject(this, orb, impl.getByteBuffer(), getSize(), getGIOPVersion());
        impl.dereferenceBuffer();
        return inputObject;
    }

    // We can move this out somewhere later.  For now, it serves its purpose
    // to create a concrete CDR delegate based on the GIOP version.
    private static class OutputStreamFactory {
        
        public static CDROutputStreamBase newOutputStream(GIOPVersion version) {
            switch(version.intValue()) {
                case GIOPVersion.VERSION_1_0:
                    return new CDROutputStream_1_0();
                case GIOPVersion.VERSION_1_1:
                    return new CDROutputStream_1_1();
            case GIOPVersion.VERSION_1_2:
                return new CDROutputStream_1_2();
            default:
                // REVISIT - what is appropriate?  INTERNAL exceptions
                // are really hard to track later.
                throw wrapper.unsupportedGiopVersion( version ) ;
            }
        }
    }

    // org.omg.CORBA.portable.OutputStream

    public final void write_boolean(boolean value) {
        impl.write_boolean(value);
    }
    public final void write_char(char value) {
        impl.write_char(value);
    }
    public final void write_wchar(char value) {
        impl.write_wchar(value);
    }
    public final void write_octet(byte value) {
        impl.write_octet(value);
    }
    public final void write_short(short value) {
        impl.write_short(value);
    }
    public final void write_ushort(short value) {
        impl.write_ushort(value);
    }
    public final void write_long(int value) {
        impl.write_long(value);
    }
    public final void write_ulong(int value) {
        impl.write_ulong(value);
    }
    public final void write_longlong(long value) {
        impl.write_longlong(value);
    }
    public final void write_ulonglong(long value) {
        impl.write_ulonglong(value);
    }
    public final void write_float(float value) {
        impl.write_float(value);
    }
    public final void write_double(double value) {
        impl.write_double(value);
    }
    public final void write_string(String value) {
        impl.write_string(value);
    }
    public final void write_wstring(String value) {
        impl.write_wstring(value);
    }

    public final void write_boolean_array(boolean[] value, int offset, int length) {
        impl.write_boolean_array(value, offset, length);
    }
    public final void write_char_array(char[] value, int offset, int length) {
        impl.write_char_array(value, offset, length);
    }
    public final void write_wchar_array(char[] value, int offset, int length) {
        impl.write_wchar_array(value, offset, length);
    }
    public final void write_octet_array(byte[] value, int offset, int length) {
        impl.write_octet_array(value, offset, length);
    }
    public final void write_short_array(short[] value, int offset, int length) {
        impl.write_short_array(value, offset, length);
    }
    public final void write_ushort_array(short[] value, int offset, int length){
        impl.write_ushort_array(value, offset, length);
    }
    public final void write_long_array(int[] value, int offset, int length) {
        impl.write_long_array(value, offset, length);
    }
    public final void write_ulong_array(int[] value, int offset, int length) {
        impl.write_ulong_array(value, offset, length);
    }
    public final void write_longlong_array(long[] value, int offset, int length) {
        impl.write_longlong_array(value, offset, length);
    }
    public final void write_ulonglong_array(long[] value, int offset,int length) {
        impl.write_ulonglong_array(value, offset, length);
    }
    public final void write_float_array(float[] value, int offset, int length) {
        impl.write_float_array(value, offset, length);
    }
    public final void write_double_array(double[] value, int offset, int length) {
        impl.write_double_array(value, offset, length);
    }
    public final void write_Object(org.omg.CORBA.Object value) {
        impl.write_Object(value);
    }
    public final void write_TypeCode(TypeCode value) {
        impl.write_TypeCode(value);
    }
    public final void write_any(Any value) {
        impl.write_any(value);
    }

    @SuppressWarnings({"deprecation"})
    public final void write_Principal(org.omg.CORBA.Principal value) {
        impl.write_Principal(value);
    }

    @Override
    public final void write(int b) throws java.io.IOException {
        impl.write(b);
    }
    
    @Override
    public final void write_fixed(java.math.BigDecimal value) {
        impl.write_fixed(value);
    }

    @Override
    public final void write_Context(org.omg.CORBA.Context ctx,
                              org.omg.CORBA.ContextList contexts) {
        impl.write_Context(ctx, contexts);
    }

    @Override
    public final org.omg.CORBA.ORB orb() {
        return impl.orb();
    }

    // org.omg.CORBA_2_3.portable.OutputStream
    @Override
    public final void write_value(java.io.Serializable value) {
        impl.write_value(value);
    }

    @Override
    public final void write_value(java.io.Serializable value,
            java.lang.Class clz) {
        impl.write_value(value, clz);
    }

    @Override
    public final void write_value(java.io.Serializable value,
        String repository_id) {

        impl.write_value(value, repository_id);
    }

    @Override
    public final void write_value(java.io.Serializable value, BoxedValueHelper factory) {
        impl.write_value(value, factory);
    }

    @Override
    public final void write_abstract_interface(java.lang.Object obj) {
        impl.write_abstract_interface(obj);
    }

    // java.io.OutputStream
    @Override
    public final void write(byte b[]) throws IOException {
        impl.write(b);
    }

    @Override
    public final void write(byte b[], int off, int len) throws IOException {
        impl.write(b, off, len);
    }

    @Override
    public final void flush() throws IOException {
        impl.flush();
    }

    @Override
    public final void close() throws IOException {
        impl.close();
    }

    // com.sun.corba.ee.impl.encoding.MarshalOutputStream
    public final void start_block() {
        impl.start_block();
    }

    public final void end_block() {
        impl.end_block();
    }

    public final void putEndian() {
        impl.putEndian();
    }

    public void writeTo(java.io.OutputStream s)
        throws IOException 
    {
        impl.writeTo(s);
    }

    public final byte[] toByteArray() {
        return impl.toByteArray();
    }

    /**
     * Returns the contents of this stream, from the specified start index to the current output position.
     * @param start the index at which to start copying the data.
     * @return a byte array representation of part of the output.
     */
    public final byte[] toByteArray(int start) {
        return impl.toByteArray(start);
    }

    // org.omg.CORBA.DataOutputStream
    public final void write_Abstract (java.lang.Object value) {
        impl.write_Abstract(value);
    }

    public final void write_Value (java.io.Serializable value) {
        impl.write_Value(value);
    }

    public final void write_any_array(org.omg.CORBA.Any[] seq, int offset, int length) {
        impl.write_any_array(seq, offset, length);
    }

    // org.omg.CORBA.portable.ValueBase
    public final String[] _truncatable_ids() {
        return impl._truncatable_ids();
    }

    // Other
    protected final int getSize() {
        return impl.getSize();
    }

    protected final int getIndex() {
        return impl.getIndex();
    }

    protected int getRealIndex(int index) {
        // Used in indirections. Overridden by TypeCodeOutputStream.
        return index;
    }

    protected final void setIndex(int value) {
        impl.setIndex(value);
    }

    // REVISIT: was protected - but need to access from xgiop.
    public final BufferManagerWrite getBufferManager() {
        return impl.getBufferManager();
    }

    public final void write_fixed(java.math.BigDecimal bigDecimal, short digits, short scale) {
        impl.write_fixed(bigDecimal, digits, scale);
    }

    public final void writeOctetSequenceTo(org.omg.CORBA.portable.OutputStream s) {
        impl.writeOctetSequenceTo(s);
    }

    public final GIOPVersion getGIOPVersion() {
        return impl.getGIOPVersion();
    }

    public final void writeIndirection(int tag, int posIndirectedTo) {
        impl.writeIndirection(tag, posIndirectedTo);
    }

    protected final void freeInternalCaches() {
        impl.freeInternalCaches();
    }

    // Needed by request and reply messages for GIOP versions >= 1.2 only.
    public void setHeaderPadding(boolean headerPadding) {
        impl.setHeaderPadding(headerPadding);
    }

    // ValueOutputStream -----------------------------

    public void start_value(String rep_id) {
        impl.start_value(rep_id);
    }

    public void end_value() {
        impl.end_value();
    }

    protected interface InputObjectFactory {
        CDRInputObject createInputObject(CDROutputObject outputObject, ORB orb, ByteBuffer byteBuffer, int size, GIOPVersion giopVersion);
    }

    private static class CDRInputObjectFactory implements InputObjectFactory {
        @Override
        public CDRInputObject createInputObject(CDROutputObject outputObject, ORB orb, ByteBuffer byteBuffer, int size, GIOPVersion giopVersion) {
            Message messageHeader = outputObject.getMessageHeader();
            messageHeader.setSize(byteBuffer, size);
            return new CDRInputObject(orb, null, byteBuffer, messageHeader);
        }
    }
}

// End of file.
