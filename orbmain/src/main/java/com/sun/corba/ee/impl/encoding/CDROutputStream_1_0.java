/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
 * Copyright (c) 2020 Payara Services Ltd.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.encoding;

import java.io.Serializable;
import java.io.IOException;

import java.lang.reflect.Method;

import java.nio.ByteBuffer;

import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedActionException;

import java.util.Map;
import java.util.HashMap;

import javax.rmi.CORBA.EnumDesc;
import javax.rmi.CORBA.ProxyDesc;
import java.lang.reflect.Proxy;

import javax.rmi.CORBA.ValueHandler;
import javax.rmi.CORBA.ValueHandlerMultiFormat;

import com.sun.org.omg.CORBA.portable.ValueHelper;
import org.omg.CORBA.CustomMarshal;
import org.omg.CORBA.TypeCodePackage.BadKind;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.Any;
import org.omg.CORBA.VM_CUSTOM;
import org.omg.CORBA.VM_TRUNCATABLE;
import org.omg.CORBA.VM_NONE;
import org.omg.CORBA.portable.IDLEntity;
import org.omg.CORBA.portable.CustomValue;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.BoxedValueHelper;

import com.sun.corba.ee.spi.transport.ByteBufferPool;

import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;
import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.ior.IORFactories;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.orb.ORBVersionFactory;
import com.sun.corba.ee.spi.orb.ClassCodeBaseHandler;

import com.sun.corba.ee.impl.corba.TypeCodeImpl;
import com.sun.corba.ee.impl.misc.CacheTable;
import com.sun.corba.ee.impl.misc.ORBUtility;
import com.sun.corba.ee.impl.misc.RepositoryIdStrings;
import com.sun.corba.ee.impl.misc.RepositoryIdUtility;
import com.sun.corba.ee.impl.misc.RepositoryIdFactory;
import com.sun.corba.ee.impl.util.Utility;
import com.sun.corba.ee.spi.logging.ORBUtilSystemException;
import com.sun.corba.ee.impl.javax.rmi.CORBA.Util;

import com.sun.corba.ee.impl.misc.ClassInfoCache;

import com.sun.corba.ee.spi.trace.*;
import org.glassfish.pfl.tf.spi.annotation.InfoMethod;

@CdrWrite
@PrimitiveWrite
public class CDROutputStream_1_0 extends CDROutputStreamBase {
    private static final int INDIRECTION_TAG = 0xffffffff;
    private static final boolean BIG_ENDIAN = false;

    protected BufferManagerWrite bufferManagerWrite;
    ByteBuffer byteBuffer;

    protected ORB orb;
    protected static final ORBUtilSystemException wrapper = ORBUtilSystemException.self;

    protected int blockSizeIndex = -1;
    protected int blockSizePosition = 0;

    protected byte streamFormatVersion;

    private static final String kWriteMethod = "write";

    // Enum cache
    Map<String, Map<String, EnumDesc>> enumCache = null;

    // Codebase cache
    // Note that a CacheTable here fails badly on read.  Why?
    // This suggests that different codebase strings with the
    // same characters are being used, but that does not explain
    // the read-side failure.
    // ALTCODEBASE
    // private CacheTable<String> codebaseCache = null;
    private Map<String, Integer> codebaseCache = null;

    // Value cache
    private CacheTable<java.lang.Object> valueCache = null;

    // Repository ID cache
    private CacheTable<String> repositoryIdCache = null;

    // Write end flag
    private int end_flag = 0;

    // Beginning with the resolution to interop issue 3526,
    // only enclosing chunked valuetypes are taken into account
    // when computing the nesting level.  However, we still need
    // the old computation around for interoperability with our
    // older ORBs.
    private int chunkedValueNestingLevel = 0;

    private boolean mustChunk = false;

    // In block marker
    protected boolean inBlock = false;

    // Last end tag position
    private int end_flag_position = 0;
    private int end_flag_index = 0;

    // ValueHandler
    private ValueHandler valueHandler = null;

    // Repository ID handlers
    private RepositoryIdUtility repIdUtil;
    private RepositoryIdStrings repIdStrs;

    // Code set converters (created when first needed)
    private CodeSetConversion.CTBConverter charConverter;
    private CodeSetConversion.CTBConverter wcharConverter;

    // REVISIT - This should be re-factored so that including whether
    // to use pool byte buffers or not doesn't need to be known.
    public void init(org.omg.CORBA.ORB orb,
                     BufferManagerWrite bufferManager,
                     byte streamFormatVersion,
                     boolean usePooledByteBuffers) {
        // ORB must not be null.  See CDROutputStream constructor.
        this.orb = (ORB) orb;

        this.bufferManagerWrite = bufferManager;
        byteBuffer = allocateBuffer(orb, bufferManager, usePooledByteBuffers);
        byteBuffer.position(0);
        this.streamFormatVersion = streamFormatVersion;

        createRepositoryIdHandlers();
    }

    static ByteBuffer allocateBuffer(org.omg.CORBA.ORB orb, BufferManagerWrite bufferManager,
                                     boolean usePooledByteBuffers) {
        int bufferSize = bufferManager.getBufferSize();
        ByteBuffer buffer;
        if (usePooledByteBuffers) {
            ByteBufferPool byteBufferPool = ((ORB) orb).getByteBufferPool();
            buffer = byteBufferPool.getByteBuffer(bufferSize);
        } else {
            // don't allocate from pool, allocate non-direct ByteBuffer
            buffer = ByteBuffer.allocate(bufferSize);
        }
        buffer.limit(bufferSize);
        return buffer;
    }

    private void createRepositoryIdHandlers() {
        repIdUtil = RepositoryIdFactory.getRepIdUtility();
        repIdStrs = RepositoryIdFactory.getRepIdStringsFactory();
    }

    public BufferManagerWrite getBufferManager() {
        return bufferManagerWrite;
    }

    public byte[] toByteArray() {
        return toByteArray(0);
    }

    @Override
    protected byte[] toByteArray(int start) {
        byte[] it;

        it = new byte[byteBuffer.position() - start];

        byteBuffer.position(start);
        byteBuffer.get(it);

        return it;
    }

    public GIOPVersion getGIOPVersion() {
        return GIOPVersion.V1_0;
    }

    // Called by Request and Reply message. Valid for GIOP versions >= 1.2 only.
    // Illegal for GIOP versions < 1.2.
    void setHeaderPadding(boolean headerPadding) {
        throw wrapper.giopVersionError();
    }

    @CdrWrite
    protected void handleSpecialChunkBegin(int requiredSize) {
        // No-op for GIOP 1.0
    }

    @CdrWrite
    protected void handleSpecialChunkEnd() {
        // No-op for GIOP 1.0
    }

    protected final int computeAlignment(int align) {
        if (align > 1) {
            int incr = byteBuffer.position() & (align - 1);
            if (incr != 0) {
                return align - incr;
            }
        }

        return 0;
    }

    protected void alignAndReserve(int align, int n) {
        byteBuffer.position(byteBuffer.position() + computeAlignment(align));

        if (byteBuffer.position() + n > byteBuffer.limit()) {
            grow(align, n);
        }
    }

    //
    // Default implementation of grow.  Subclassers may override this.
    // Always grow the single buffer. This needs to delegate
    // fragmentation policy for IIOP 1.1.
    //
    protected void grow(int align, int n) {
        byteBuffer = bufferManagerWrite.overflow(byteBuffer, n);
    }

    public final void putEndian() throws SystemException {
        write_boolean(BIG_ENDIAN); // Java always writes big-endian
    }

    void freeInternalCaches() {
        if (codebaseCache != null) {
            // ALTCODEBASE
            // codebaseCache.done() ;
            codebaseCache.clear();
        }

        freeValueCache();

        if (repositoryIdCache != null) {
            repositoryIdCache.done();
        }
    }

    @PrimitiveWrite
    public void write_octet(byte x) {
        alignAndReserve(1, 1);
        byteBuffer.put(x);
    }

    public final void write_boolean(boolean x) {
        write_octet(x ? (byte) 1 : (byte) 0);
    }

    public void write_char(char x) {
        CodeSetConversion.CTBConverter converter = getCharConverter();

        converter.convert(x);

        // CORBA formal 99-10-07 15.3.1.6: "In the case of multi-byte encodings
        // of characters, a single instance of the char type may only
        // hold one octet of any multi-byte character encoding."
        if (converter.getNumBytes() > 1) {
            throw wrapper.invalidSingleCharCtb();
        }

        write_octet(converter.getBytes()[0]);
    }

    private void writeBigEndianWchar(char x) {
        byteBuffer.put((byte) ((x >>> 8) & 0xFF));
        byteBuffer.put((byte) (x & 0xFF));
    }

    @PrimitiveWrite
    public void write_wchar(char x) {
        // Don't allow transmission of wchar/wstring data with
        // foreign ORBs since it's against the spec.
        if (ORBUtility.isForeignORB(orb)) {
            throw wrapper.wcharDataInGiop10();
        }

        // If it's one of our legacy ORBs, do what they did:
        alignAndReserve(2, 2);

        writeBigEndianWchar(x);
    }

    @PrimitiveWrite
    public void write_short(short x) {
        alignAndReserve(2, 2);
        byteBuffer.putShort(x);
    }

    public final void write_ushort(short x) {
        write_short(x);
    }

    @PrimitiveWrite
    public void write_long(int x) {
        alignAndReserve(4, 4);
        byteBuffer.putInt(x);
    }

    public final void write_ulong(int x) {
        write_long(x);
    }

    @PrimitiveWrite
    public void write_longlong(long x) {
        alignAndReserve(8, 8);

        byteBuffer.putLong(x);
    }

    public final void write_ulonglong(long x) {
        write_longlong(x);
    }

    public final void write_float(float x) {
        write_long(Float.floatToIntBits(x));
    }

    public final void write_double(double x) {
        write_longlong(Double.doubleToLongBits(x));
    }

    public void write_string(String value) {
        writeString(value);
    }

    @PrimitiveWrite
    protected int writeString(String value) {
        if (value == null) {
            throw wrapper.nullParam();
        }

        CodeSetConversion.CTBConverter converter = getCharConverter();

        converter.convert(value);

        // A string is encoded as an unsigned CORBA long for the
        // number of bytes to follow (including a terminating null).
        // There is only one octet per character in the string.
        int len = converter.getNumBytes() + 1;

        handleSpecialChunkBegin(computeAlignment(4) + 4 + len);

        write_long(len);
        int indirection = get_offset() - 4;

        internalWriteOctetArray(converter.getBytes(), 0, converter.getNumBytes());

        // Write the null ending
        write_octet((byte) 0);

        handleSpecialChunkEnd();
        return indirection;
    }

    public void write_wstring(String value) {
        if (value == null) {
            throw wrapper.nullParam();
        }

        // Don't allow transmission of wchar/wstring data with
        // foreign ORBs since it's against the spec.
        if (ORBUtility.isForeignORB(orb)) {
            throw wrapper.wcharDataInGiop10();
        }

        // When talking to our legacy ORBs, do what they did:
        int len = value.length() + 1;

        // This will only have an effect if we're already chunking
        handleSpecialChunkBegin(4 + (len * 2) + computeAlignment(4));

        write_long(len);

        for (int i = 0; i < len - 1; i++) {
            write_wchar(value.charAt(i));
        }

        // Write the null ending
        write_short((short) 0);

        // This will only have an effect if we're already chunking
        handleSpecialChunkEnd();
    }

    // Performs no checks and doesn't tamper with chunking
    void internalWriteOctetArray(byte[] value, int offset, int length) {
        if (length == 0) return;

        alignAndReserve(1, 1);  // this gives the code the chance to do the eight-byte alignment, if needed

        int numWritten = 0;
        while (numWritten < length) {
            if (!byteBuffer.hasRemaining()) alignAndReserve(1, 1);

            int count = Math.min(length - numWritten, byteBuffer.remaining());
            byteBuffer.put(value, offset + numWritten, count);
            numWritten += count;
        }
    }

    public final void write_octet_array(byte b[], int offset, int length) {
        if (b == null) {
            throw wrapper.nullParam();
        }

        // This will only have an effect if we're already chunking
        handleSpecialChunkBegin(length);

        internalWriteOctetArray(b, offset, length);

        // This will only have an effect if we're already chunking
        handleSpecialChunkEnd();
    }

    @SuppressWarnings({"deprecation"})
    public void write_Principal(org.omg.CORBA.Principal p) {
        write_long(p.name().length);
        write_octet_array(p.name(), 0, p.name().length);
    }

    @CdrWrite
    public void write_any(Any any) {
        if (any == null) {
            throw wrapper.nullParam();
        }

        write_TypeCode(any.type());
        any.write_value(parent);
    }

    @CdrWrite
    public void write_TypeCode(TypeCode tc) {
        if (tc == null) {
            throw wrapper.nullParam();
        }
        TypeCodeImpl tci;
        if (tc instanceof TypeCodeImpl) {
            tci = (TypeCodeImpl) tc;
        } else {
            tci = new TypeCodeImpl(orb, tc);
        }

        tci.write_value(parent);
    }

    @CdrWrite
    public void write_Object(org.omg.CORBA.Object ref) {
        if (ref == null) {
            IOR nullIOR = IORFactories.makeIOR(orb);
            nullIOR.write(parent);
            return;
        }

        // IDL to Java formal 01-06-06 1.21.4.2
        if (ref instanceof org.omg.CORBA.LocalObject) {
            throw wrapper.writeLocalObject();
        }

        IOR ior = orb.getIOR(ref, true);
        ior.write(parent);
    }

    // ------------ RMI related methods --------------------------

    @CdrWrite
    public void write_abstract_interface(java.lang.Object obj) {
        boolean corbaObject = false; // Assume value type.
        org.omg.CORBA.Object theObject = null;

        // Is it a CORBA.Object?

        if (obj != null && obj instanceof org.omg.CORBA.Object) {

            // Yes.

            theObject = (org.omg.CORBA.Object) obj;
            corbaObject = true;
        }

        // Write our flag...

        write_boolean(corbaObject);

        // Now write out the object...

        if (corbaObject) {
            write_Object(theObject);
        } else {
            try {
                write_value((java.io.Serializable) obj);
            } catch (ClassCastException cce) {
                if (obj instanceof java.io.Serializable) {
                    throw cce;
                } else {
                    ORBUtility.throwNotSerializableForCorba(obj.getClass().getName());
                }
            }
        }
    }

    @CdrWrite
    public void write_value(Serializable object, Class clz) {

        write_value(object);
    }

    @CdrWrite
    private void startValueChunk(boolean useChunking) {
        if (useChunking) {
            start_block();
            chunkedValueNestingLevel--;
        }

        end_flag--;
    }

    @CdrWrite
    private void endValueChunk(boolean useChunking) {
        if (useChunking) {
            end_block();
        }

        writeEndTag(useChunking);
    }

    private void writeWStringValue(String string) {

        int indirection = writeValueTag(mustChunk, true, null);

        write_repositoryId(repIdStrs.getWStringValueRepId());

        updateIndirectionTable(indirection, string);

        startValueChunk(mustChunk);
        write_wstring(string);
        endValueChunk(mustChunk);
    }

    private String getCodebase(Class cls) {
        ClassCodeBaseHandler ccbh = orb.classCodeBaseHandler();
        if (ccbh != null) {
            String result = ccbh.getCodeBase(cls);
            if (result != null) {
                return result;
            }
        }

        return Util.getInstance().getCodebase(cls);
    }

    @CdrWrite
    private void writeArray(Serializable array, Class clazz) {
        if (valueHandler == null) {
            valueHandler = ORBUtility.createValueHandler();
        }

        // Write value_tag
        int indirection = writeValueTag(mustChunk, true, getCodebase(clazz));

        // Write repository ID
        write_repositoryId(repIdStrs.createSequenceRepID(clazz));

        // Add indirection for object to indirection table
        updateIndirectionTable(indirection, array);

        callWriteValue(parent, array, streamFormatVersion);
    }

    @CdrWrite
    private void writeValueBase(org.omg.CORBA.portable.ValueBase object,
                                Class clazz) {
        // _REVISIT_ could check to see whether chunking really needed 
        mustChunk = true;

        int indirection = writeValueTag(true, true, getCodebase(clazz));

        String repId = object._truncatable_ids()[0];
        write_repositoryId(repId);

        updateIndirectionTable(indirection, object);

        startValueChunk(true);
        writeIDLValue(object, repId);
        endValueChunk(true);
    }

    // We know that object is not null, because that was checked in 
    // write_value( Serializable, String )
    @CdrWrite
    private void writeRMIIIOPValueType(Serializable object, Class clazz,
                                       ClassInfoCache.ClassInfo cinfo) {

        if (valueHandler == null) {
            valueHandler = ORBUtility.createValueHandler();
        }

        Serializable key = object;

        // Allow the ValueHandler to call writeReplace on
        // the Serializable (if the method is present)
        object = valueHandler.writeReplace(key);

        if (object != key) {
            if (object == null) {
                // If replaced value is null, write null tag and return
                write_long(0);
                return;
            }

            // write replace changed something
            if (writeIndirectionIfPossible(object)) {
                return;
            }

            clazz = object.getClass();
        }

        mustChunk |= valueHandler.isCustomMarshaled(clazz);

        // Write value_tag
        int indirection = writeValueTag(mustChunk, true, getCodebase(clazz));

        // Write rep. id
        write_repositoryId(repIdStrs.createForJavaType(clazz, cinfo));

        // Add indirection for object to indirection table.
        // If writeReplace nominated a replacement object,
        // store both the replacement and the original object in the
        // table.
        updateIndirectionTable(indirection, key);
        if (object != key) {
            updateIndirectionTable(indirection, object);
        }

        callWriteValue(parent, object, streamFormatVersion);
    }

    @CdrWrite
    private void callWriteValue(org.omg.CORBA.portable.OutputStream parent,
                                java.io.Serializable object, byte streamFormatVersion) {
        if (valueHandler == null) {
            valueHandler = ORBUtility.createValueHandler();
        }

        boolean currentMustChunk = mustChunk;
        startValueChunk(currentMustChunk);

        if (valueHandler instanceof ValueHandlerMultiFormat) {
            ValueHandlerMultiFormat vh = (ValueHandlerMultiFormat) valueHandler;
            vh.writeValue(parent, object, streamFormatVersion);
        } else {
            valueHandler.writeValue(parent, object);
        }

        endValueChunk(currentMustChunk);
    }

    private EnumDesc getEnumDesc(String className, String enumValue) {
        EnumDesc result = null;
        Map<String, EnumDesc> map = null;

        if (enumCache == null) {
            enumCache = new HashMap<String, Map<String, EnumDesc>>();
        } else {
            map = enumCache.get(className);
        }

        if (map == null) {
            map = new HashMap<String, EnumDesc>();
            enumCache.put(className, map);
        } else {
            result = map.get(enumValue);
        }

        if (result == null) {
            result = new EnumDesc();
            result.className = className;
            result.value = enumValue;

            map.put(enumValue, result);
        }

        return result;
    }

    @CdrWrite
    public void write_value(Serializable object, String repository_id) {
        // Handle null references
        if (object == null) {
            // Write null tag and return
            write_long(0);
            return;
        }

        Class clazz = object.getClass();
        ClassInfoCache.ClassInfo cinfo = ClassInfoCache.get(clazz);
        if (cinfo.isEnum()) {
            String enumValue = ((Enum) object).name();
            if (orb.getORBData().useEnumDesc()) {
                EnumDesc desc = getEnumDesc(clazz.getName(), enumValue);
                write_value(desc, (String) null);
            } else {
                // Write value_tag
                mustChunk = false;
                int indirection = writeValueTag(mustChunk, true, getCodebase(clazz));

                // Write rep. id
                write_repositoryId(repIdStrs.createForJavaType(clazz, cinfo));
                updateIndirectionTable(indirection, object);

                // Just write the enum member name
                writeString(enumValue);
            }

            return;
        }

        if (cinfo.isProxyClass()) {
            Class[] ifaces = clazz.getInterfaces();
            ProxyDesc pd = new ProxyDesc();

            pd.interfaces = new String[ifaces.length];
            for (int i = 0; i < ifaces.length; ++i) {
                pd.interfaces[i] = ifaces[i].getName();
            }
            pd.handler = Proxy.getInvocationHandler(object);
            pd.codebase = getCodebase(object.getClass());
            write_value(pd, (String) null);
            return;
        }

        // Handle shared references
        if (writeIndirectionIfPossible(object)) {
            return;
        }

        // Save mustChunk in case a recurisive call from the ValueHandler or IDL
        // generated code calls write_value with a possibly different value of mustChunk
        boolean oldMustChunk = mustChunk;

        if (inBlock) {
            end_block();
        }

        if (cinfo.isArray()) {
            // Handle arrays
            writeArray(object, clazz);
        } else if (cinfo.isAValueBase(clazz)) {
            // Handle IDL Value types
            writeValueBase((org.omg.CORBA.portable.ValueBase) object, clazz);
        } else if (cinfo.isAIDLEntity(clazz) && !cinfo.isACORBAObject(clazz)) {
            writeIDLEntity((IDLEntity) object);
        } else if (cinfo.isAString(clazz)) {
            writeWStringValue((String) object);
        } else if (cinfo.isAClass(clazz)) {
            ClassInfoCache.ClassInfo lcinfo = ClassInfoCache.get((Class) object);
            writeClass(repository_id, (Class) object, lcinfo);
        } else {
            // RMI-IIOP value type
            writeRMIIIOPValueType(object, clazz, cinfo);
        }

        mustChunk = oldMustChunk;

        // Check to see if we need to start another block for a
        // possible outer value
        if (mustChunk) {
            start_block();
        }
    }

    public void write_value(Serializable object) {
        write_value(object, (String) null);
    }

    @SuppressWarnings({"deprecation"})
    @CdrWrite
    public void write_value(Serializable object, BoxedValueHelper factory) {
        if (object == null) {
            // Write null tag and return
            write_long(0);
            return;
        }

        // Handle shared references
        if (writeIndirectionIfPossible(object)) {
            return;
        }

        // Save mustChunk in case a recursive call from the ValueHandler or IDL
        // generated code calls write_value with a possibly different value of mustChunk
        boolean oldMustChunk = mustChunk;

        // can set mustChunk
        boolean isCustom = isCustom(object, factory);

        if (mustChunk && inBlock) {
            end_block();
        }

        int indirection = writeValueTag(mustChunk, orb.getORBData().useRepId(), getCodebase(object.getClass()));

        if (orb.getORBData().useRepId()) {
            write_repositoryId(factory.get_id());
        }

        updateIndirectionTable(indirection, object);

        boolean currentMustChunk = mustChunk;
        startValueChunk(currentMustChunk);
        if (mustChunk && isCustom) {
            ((CustomMarshal) object).marshal(parent);
        } else {
            factory.write_value(parent, object);
        }
        endValueChunk(currentMustChunk);

        mustChunk = oldMustChunk;

        // Check to see if we need to start another block for a
        // possible outer value
        if (mustChunk) {
            start_block();
        }

    }

    private boolean isCustom(Serializable object, BoxedValueHelper factory) {
        boolean isCustom = false;
        if (factory instanceof com.sun.org.omg.CORBA.portable.ValueHelper) {
            short modifier = getTypeModifier((ValueHelper) factory);

            if (object instanceof CustomMarshal && modifier == VM_CUSTOM.value) {
                isCustom = true;
                mustChunk = true;
            } else if (modifier == VM_TRUNCATABLE.value) {
                mustChunk = true;
            }
        }
        return isCustom;
    }

    @SuppressWarnings("deprecated")
    private short getTypeModifier(ValueHelper factory) {
        short modifier;
        try {
            modifier = factory.get_type().type_modifier();
        } catch (BadKind ex) {  // tk_value_box
            modifier = VM_NONE.value;
        }
        return modifier;
    }

    /**
     * Returns the current position relative to the start of the message.
     *
     * @return current position
     */
    public int get_offset() {
        return byteBuffer.position();
    }

    @CdrWrite
    public void start_block() {
        // Save space in the buffer for block size
        write_long(0);

        // Has to happen after write_long since write_long could
        // trigger grow which is overridden by subclasses to 
        // depend on inBlock.
        inBlock = true;

        // Note that get_offset is overridden in subclasses to handle fragmentation!
        // Thus blockSizePosition and blockSizeIndex are not always the same!
        blockSizePosition = get_offset();
        blockSizeIndex = byteBuffer.position();
    }

    // Utility method which will hopefully decrease chunking complexity
    // by allowing us to end_block and update chunk lengths without
    // calling alignAndReserve.  Otherwise, it's possible to get into
    // recursive scenarios which lose the chunking state.
    protected void writeLongWithoutAlign(int x) {
        byteBuffer.putInt(x);
    }

    @InfoMethod
    private void inABlock() {
    }

    @InfoMethod
    private void blockSizePosition(int blockSize) {
    }

    @InfoMethod
    private void removingZeroLengthBlock() {
    }

    @CdrWrite
    public void end_block() {
        if (!inBlock) {
            return;
        }

        inABlock();

        inBlock = false;

        // Test to see if the block was of zero length
        // If so, remove the block instead of ending it
        // (This can happen if the last field written 
        //  in a value was another value)
        blockSizePosition(blockSizePosition);

        if (get_offset() == blockSizePosition) {
            removingZeroLengthBlock();

            // Need to assert that blockSizeIndex == bbwi.position()?  REVISIT

            byteBuffer.position(byteBuffer.position() - 4);
            blockSizeIndex = -1;
            blockSizePosition = -1;
            return;
        }

        int oldSize = byteBuffer.position();
        byteBuffer.position(blockSizeIndex - 4);

        writeLongWithoutAlign(oldSize - blockSizeIndex);

        byteBuffer.position(oldSize);
        blockSizeIndex = -1;
        blockSizePosition = -1;
    }

    public org.omg.CORBA.ORB orb() {
        return orb;
    }

    // ------------ End RMI related methods --------------------------

    @CdrWrite
    public final void write_boolean_array(boolean[] value, int offset, int length) {
        if (value == null) {
            throw wrapper.nullParam();
        }

        // This will only have an effect if we're already chunking
        handleSpecialChunkBegin(length);

        for (int i = 0; i < length; i++) {
            write_boolean(value[offset + i]);
        }

        // This will only have an effect if we're already chunking
        handleSpecialChunkEnd();
    }

    @CdrWrite
    public final void write_char_array(char[] value, int offset, int length) {
        if (value == null) {
            throw wrapper.nullParam();
        }

        // This will only have an effect if we're already chunking
        handleSpecialChunkBegin(length);

        for (int i = 0; i < length; i++) {
            write_char(value[offset + i]);
        }

        // This will only have an effect if we're already chunking
        handleSpecialChunkEnd();
    }

    @CdrWrite
    public void write_wchar_array(char[] value, int offset, int length) {
        if (value == null) {
            throw wrapper.nullParam();
        }

        // This will only have an effect if we're already chunking
        handleSpecialChunkBegin(computeAlignment(2) + (length * 2));

        for (int i = 0; i < length; i++) {
            write_wchar(value[offset + i]);
        }

        // This will only have an effect if we're already chunking
        handleSpecialChunkEnd();
    }

    @CdrWrite
    public final void write_short_array(short[] value, int offset, int length) {
        if (value == null) {
            throw wrapper.nullParam();
        }

        // This will only have an effect if we're already chunking
        handleSpecialChunkBegin(computeAlignment(2) + (length * 2));

        for (int i = 0; i < length; i++) {
            write_short(value[offset + i]);
        }

        // This will only have an effect if we're already chunking
        handleSpecialChunkEnd();
    }

    public final void write_ushort_array(short[] value, int offset, int length) {
        write_short_array(value, offset, length);
    }

    @CdrWrite
    public final void write_long_array(int[] value, int offset, int length) {
        if (value == null) {
            throw wrapper.nullParam();
        }

        // This will only have an effect if we're already chunking
        handleSpecialChunkBegin(computeAlignment(4) + (length * 4));

        for (int i = 0; i < length; i++) {
            write_long(value[offset + i]);
        }

        // This will only have an effect if we're already chunking
        handleSpecialChunkEnd();
    }

    public final void write_ulong_array(int[] value, int offset, int length) {
        write_long_array(value, offset, length);
    }

    @CdrWrite
    public final void write_longlong_array(long[] value, int offset, int length) {
        if (value == null) {
            throw wrapper.nullParam();
        }

        // This will only have an effect if we're already chunking
        handleSpecialChunkBegin(computeAlignment(8) + (length * 8));

        for (int i = 0; i < length; i++) {
            write_longlong(value[offset + i]);
        }

        // This will only have an effect if we're already chunking
        handleSpecialChunkEnd();
    }

    public final void write_ulonglong_array(long[] value, int offset, int length) {
        write_longlong_array(value, offset, length);
    }

    @CdrWrite
    public final void write_float_array(float[] value, int offset, int length) {
        if (value == null) {
            throw wrapper.nullParam();
        }

        // This will only have an effect if we're already chunking
        handleSpecialChunkBegin(computeAlignment(4) + (length * 4));

        for (int i = 0; i < length; i++) {
            write_float(value[offset + i]);
        }

        // This will only have an effect if we're already chunking
        handleSpecialChunkEnd();
    }

    @CdrWrite
    public final void write_double_array(double[] value, int offset, int length) {
        if (value == null) {
            throw wrapper.nullParam();
        }

        // This will only have an effect if we're already chunking
        handleSpecialChunkBegin(computeAlignment(8) + (length * 8));

        for (int i = 0; i < length; i++) {
            write_double(value[offset + i]);
        }

        // This will only have an effect if we're already chunking
        handleSpecialChunkEnd();
    }

    @CdrWrite
    public final void write_any_array(org.omg.CORBA.Any value[], int offset, int length) {
        for (int i = 0; i < length; i++) {
            write_any(value[offset + i]);
        }
    }

    //--------------------------------------------------------------------//
    // CDROutputStream state management.
    //

    public void writeTo(java.io.OutputStream s) throws java.io.IOException {
        byte[] tmpBuf = ORBUtility.getByteBufferArray(byteBuffer);
        s.write(tmpBuf, 0, byteBuffer.position());
    }

    public void writeOctetSequenceTo(org.omg.CORBA.portable.OutputStream s) {
        byte[] buf = ORBUtility.getByteBufferArray(byteBuffer);
        s.write_long(byteBuffer.position());
        s.write_octet_array(buf, 0, byteBuffer.position());
    }

    public final int getSize() {
        return byteBuffer.position();
    }

    public int getIndex() {
        return byteBuffer.position();
    }

    public void setIndex(int value) {
        byteBuffer.position(value);
    }

    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }

    private void freeValueCache() {
        if (valueCache != null) {
            valueCache.done();
        }
    }

    private void updateIndirectionTable(int indirection,
                                        java.lang.Object key) {

        if (valueCache == null) {
            valueCache = new CacheTable<java.lang.Object>("Output valueCache",
                    orb, true);
        }
        valueCache.put(key, indirection);
    }

    private boolean writeIndirectionIfPossible(final Serializable object) {
        if (valueCache != null) {
            final int indir = valueCache.getVal(object);
            if (indir != -1) {
                writeIndirection(INDIRECTION_TAG, indir);
                return true;
            }
        }

        return false;
    }

    @CdrWrite
    private void write_repositoryId(String id) {
        // Use an indirection if available
        if (repositoryIdCache != null) {
            int indir = repositoryIdCache.getVal(id);
            if (indir != -1) {
                writeIndirection(INDIRECTION_TAG, indir);
                return;
            }
        }

        // Write it as a string.  Note that we have already done the
        // special case conversion of non-Latin-1 characters to escaped
        // Latin-1 sequences in RepositoryId.

        // It's not a good idea to cache them now that we can have
        // multiple code sets.
        int indirection = writeString(id);

        // Add indirection for id to indirection table
        if (repositoryIdCache == null) {
            repositoryIdCache = new CacheTable<String>("Output repositoryIdCache", orb, true);
        }
        repositoryIdCache.put(id, indirection);
    }

    @CdrWrite
    private void write_codebase(String str, int pos) {
        Integer value = null;
        if (codebaseCache != null) {
            // ALTCODEBASE
            // value = codebaseCache.getVal(str) ;
            value = codebaseCache.get(str);
        }

        if (value != null) {
            writeIndirection(INDIRECTION_TAG, value);
        } else {
            write_string(str);
            if (codebaseCache == null) {
                // ALTCODEBASE
                // codebaseCache = new CacheTable<String>("Output codebaseCache",orb,true);
                codebaseCache = new HashMap<String, Integer>();
            }

            codebaseCache.put(str, pos);
        }
    }

    @CdrWrite
    private int writeValueTag(boolean chunkIt, boolean useRepId, String codebase) {
        int indirection;
        if (chunkIt && !useRepId) {
            if (codebase == null) {
                write_long(repIdUtil.getStandardRMIChunkedNoRepStrId());
                indirection = get_offset() - 4;
            } else {
                write_long(repIdUtil.getCodeBaseRMIChunkedNoRepStrId());
                indirection = get_offset() - 4;
                write_codebase(codebase, get_offset());
            }
        } else if (chunkIt && useRepId) {
            if (codebase == null) {
                write_long(repIdUtil.getStandardRMIChunkedId());
                indirection = get_offset() - 4;
            } else {
                write_long(repIdUtil.getCodeBaseRMIChunkedId());
                indirection = get_offset() - 4;
                write_codebase(codebase, get_offset());
            }
        } else if (!useRepId) {
            if (codebase == null) {
                write_long(repIdUtil.getStandardRMIUnchunkedNoRepStrId());
                indirection = get_offset() - 4;
            } else {
                write_long(repIdUtil.getCodeBaseRMIUnchunkedNoRepStrId());
                indirection = get_offset() - 4;
                write_codebase(codebase, get_offset());
            }
        } else {
            if (codebase == null) {
                write_long(repIdUtil.getStandardRMIUnchunkedId());
                indirection = get_offset() - 4;
            } else {
                write_long(repIdUtil.getCodeBaseRMIUnchunkedId());
                indirection = get_offset() - 4;
                write_codebase(codebase, get_offset());
            }
        }
        return indirection;
    }

    @SuppressWarnings({"deprecation"})
    @CdrWrite
    private void writeIDLValue(Serializable object, String repID) {
        if (object instanceof StreamableValue) {
            ((StreamableValue) object)._write(parent);
        } else if (object instanceof CustomValue) {
            ((CustomValue) object).marshal(parent);
        } else {
            BoxedValueHelper helper = Utility.getHelper(object.getClass(), null, repID);
            boolean isCustom = false;

            if (helper instanceof com.sun.org.omg.CORBA.portable.ValueHelper &&
                    object instanceof CustomMarshal) {
                try {
                    if (((com.sun.org.omg.CORBA.portable.ValueHelper) helper)
                            .get_type().type_modifier() == VM_CUSTOM.value) {
                        isCustom = true;
                    }
                } catch (BadKind ex) {
                    throw wrapper.badTypecodeForCustomValue(ex);
                }
            }

            if (isCustom) {
                ((CustomMarshal) object).marshal(parent);
            } else {
                helper.write_value(parent, object);
            }
        }
    }

    // Handles end tag compaction...
    @CdrWrite
    private void writeEndTag(boolean chunked) {
        if (chunked) {
            if (get_offset() == end_flag_position) {
                if (byteBuffer.position() == end_flag_index) {
                    // We are exactly at the same position and index as the
                    // end of the last end tag.  Thus, we can back up over it
                    // and compact the tags.
                    byteBuffer.position(byteBuffer.position() - 4);
                } else {                                            // reg - is this even possible any more?
                    // Special case in which we're at the beginning of a new
                    // fragment, but the position is the same.  We can't back up,
                    // so we just write the new end tag without compaction.  This
                    // occurs when a value ends and calls start_block to open a
                    // continuation chunk, but it's called at the very end of
                    // a fragment.
                }
            }

            writeNestingLevel();

            // Remember the last index and position.  
            // These are only used when chunking.
            end_flag_index = byteBuffer.position();
            end_flag_position = get_offset();

            chunkedValueNestingLevel++;
        }

        // Increment the nesting level
        end_flag++;
    }

    /**
     * Handles ORB versioning of the end tag.  Should only
     * be called if chunking.
     * <p/>
     * If talking to our older ORBs (Standard Extension,
     * Kestrel, and Ladybird), write the end flag that takes
     * into account all enclosing valuetypes.
     * <p/>
     * If talking a newer or foreign ORB, or if the orb
     * instance is null, write the end flag that only takes
     * into account the enclosing chunked valuetypes.
     */
    @CdrWrite
    private void writeNestingLevel() {
        if (orb == null ||
                ORBVersionFactory.getFOREIGN().equals(orb.getORBVersion()) ||
                ORBVersionFactory.getNEWER().compareTo(orb.getORBVersion()) <= 0) {

            write_long(chunkedValueNestingLevel);
        } else {
            write_long(end_flag);
        }
    }

    @CdrWrite
    private void writeClass(String repository_id, Class clz,
                            ClassInfoCache.ClassInfo cinfo) {

        if (repository_id == null) {
            repository_id = repIdStrs.getClassDescValueRepId();
        }

        // Write value_tag
        int indirection = writeValueTag(mustChunk, true, null);
        updateIndirectionTable(indirection, clz);

        write_repositoryId(repository_id);

        startValueChunk(mustChunk);
        writeClassBody(clz, cinfo);
        endValueChunk(mustChunk);
    }

    // Pre-Merlin/J2EE 1.3 ORBs wrote the repository ID
    // and codebase strings in the wrong order.  This handles
    // backwards compatibility.
    @CdrWrite
    private void writeClassBody(Class clz, ClassInfoCache.ClassInfo cinfo) {
        if (orb == null ||
                ORBVersionFactory.getFOREIGN().equals(orb.getORBVersion()) ||
                ORBVersionFactory.getNEWER().compareTo(orb.getORBVersion()) <= 0) {

            write_value(getCodebase(clz));
            write_value(repIdStrs.createForAnyType(clz, cinfo));
        } else {
            write_value(repIdStrs.createForAnyType(clz, cinfo));
            write_value(getCodebase(clz));
        }
    }

    @CdrWrite
    private void writeIDLEntity(IDLEntity object) {
        // _REVISIT_ could check to see whether chunking really needed 
        mustChunk = true;

        String repository_id = repIdStrs.createForJavaType(object);
        final Class clazz = object.getClass();
        String codebase = getCodebase(clazz);

        int indirection = writeValueTag(true, true, codebase);
        updateIndirectionTable(indirection, object);
        write_repositoryId(repository_id);

        // Write Value chunk
        startValueChunk(true);

        // Write the IDLEntity using reflection 
        try {
            ClassLoader clazzLoader = (clazz == null ? null : clazz.getClassLoader());
            final Class helperClass = Utility.loadClassForClass(
                    clazz.getName() + "Helper", codebase, clazzLoader,
                    clazz, clazzLoader);

            // getDeclaredMethod requires RuntimePermission accessDeclaredMembers
            // if a different class loader is used (even though the javadoc says otherwise)
            Method writeMethod;
            try {
                writeMethod = AccessController.doPrivileged(
                        new PrivilegedExceptionAction<Method>() {
                            public Method run() throws NoSuchMethodException {
                                return helperClass.getDeclaredMethod(kWriteMethod,
                                        org.omg.CORBA.portable.OutputStream.class, clazz);
                            }
                        }
                );
            } catch (PrivilegedActionException pae) {
                // this gets caught below
                throw (NoSuchMethodException) pae.getException();
            }
            writeMethod.invoke(null, parent, object);
        } catch (Exception exc) {
            throw wrapper.errorInvokingHelperWrite(exc);
        }

        endValueChunk(true);
    }

    /* DataOutputStream methods */

    @CdrWrite
    public void write_Abstract(java.lang.Object value) {
        write_abstract_interface(value);
    }

    @CdrWrite
    public void write_Value(java.io.Serializable value) {
        write_value(value);
    }

    // This will stay a custom add-on until the java-rtf issue is resolved.
    // Then it should be declared in org.omg.CORBA.portable.OutputStream.
    //
    // Pads the string representation of bigDecimal with zeros to fit the given
    // digits and scale before it gets written to the stream.
    public void write_fixed(java.math.BigDecimal bigDecimal, short digits, short scale) {
        String string = bigDecimal.toString();
        String integerPart;
        String fractionPart;

        // Get rid of the sign
        if (string.charAt(0) == '-' || string.charAt(0) == '+') {
            string = string.substring(1);
        }

        // Determine integer and fraction parts
        int dotIndex = string.indexOf('.');
        if (dotIndex == -1) {
            integerPart = string;
            fractionPart = null;
        } else if (dotIndex == 0) {
            integerPart = null;
            fractionPart = string;
        } else {
            integerPart = string.substring(0, dotIndex);
            fractionPart = string.substring(dotIndex + 1);
        }

        // Pad both parts with zeros as necessary
        StringBuilder stringBuffer = new StringBuilder(digits);
        if (fractionPart != null) {
            stringBuffer.append(fractionPart);
        }
        while (stringBuffer.length() < scale) {
            stringBuffer.append('0');
        }
        if (integerPart != null) {
            stringBuffer.insert(0, integerPart);
        }
        while (stringBuffer.length() < digits) {
            stringBuffer.insert(0, '0');
        }

        // This string contains no sign or dot
        this.write_fixed(stringBuffer.toString(), bigDecimal.signum());
    }

    // This method should be remove by the java-rtf issue.
    // Right now the scale and digits information of the type code is lost.
    public void write_fixed(java.math.BigDecimal bigDecimal) {
        // This string might contain sign and/or dot
        this.write_fixed(bigDecimal.toString(), bigDecimal.signum());
    }

    // The string may contain a sign and dot
    public void write_fixed(String string, int signum) {
        int stringLength = string.length();
        // Each octet contains (up to) two decimal digits
        byte doubleDigit = 0;
        char ch;
        byte digit;

        // First calculate the length of the string without optional sign and dot
        int numDigits = 0;
        for (int i = 0; i < stringLength; i++) {
            ch = string.charAt(i);
            if (ch == '-' || ch == '+' || ch == '.') {
                continue;
            }
            numDigits++;
        }
        for (int i = 0; i < stringLength; i++) {
            ch = string.charAt(i);
            if (ch == '-' || ch == '+' || ch == '.') {
                continue;
            }
            digit = (byte) Character.digit(ch, 10);
            if (digit == -1) {
                throw wrapper.badDigitInFixed();
            }
            // If the fixed type has an odd number of decimal digits,
            // then the representation begins with the first (most significant) digit.
            // Otherwise, this first half-octet is all zero, and the first digit
            // is in the second half-octet.
            if (numDigits % 2 == 0) {
                doubleDigit |= digit;
                this.write_octet(doubleDigit);
                doubleDigit = 0;
            } else {
                doubleDigit |= (digit << 4);
            }
            numDigits--;
        }
        // The sign configuration, in the last half-octet of the representation,
        // is 0xD for negative numbers and 0xC for positive and zero values
        if (signum == -1) {
            doubleDigit |= 0xd;
        } else {
            doubleDigit |= 0xc;
        }
        this.write_octet(doubleDigit);
    }

    private final static String _id = "IDL:omg.org/CORBA/DataOutputStream:1.0";
    private final static String[] _ids = {_id};

    public String[] _truncatable_ids() {
        if (_ids == null) {
            return null;
        }

        return _ids.clone();
    }

    public void writeIndirection(int tag, int posIndirectedTo) {
        // Must ensure that there are no chunks between the tag
        // and the actual indirection value.  This isn't talked about
        // in the spec, but seems to cause headaches in our code.
        // At the very least, this method isolates the indirection code
        // that was duplicated so often.

        handleSpecialChunkBegin(computeAlignment(4) + 8);

        // write indirection tag
        write_long(tag);

        // write indirection
        // Use parent.getRealIndex() so that it can be overridden by TypeCodeOutputStreams
/*
        System.out.println("CDROutputStream_1_0 writing indirection pos " + posIndirectedTo +
                           " - real index " + parent.getRealIndex(get_offset()) + " = " +
                           (posIndirectedTo - parent.getRealIndex(get_offset())));
*/
        write_long(posIndirectedTo - parent.getRealIndex(get_offset()));

        handleSpecialChunkEnd();
    }

    protected CodeSetConversion.CTBConverter getCharConverter() {
        if (charConverter == null) {
            charConverter = parent.createCharCTBConverter();
        }

        return charConverter;
    }

    protected CodeSetConversion.CTBConverter getWCharConverter() {
        if (wcharConverter == null) {
            wcharConverter = parent.createWCharCTBConverter();
        }

        return wcharConverter;
    }

    void alignOnBoundary(int octetBoundary) {
        alignAndReserve(octetBoundary, 0);
    }

    @InfoMethod
    private void startValueInfo(String repId, int offset, int position) {
    }

    @CdrWrite
    public void start_value(String rep_id) {
        startValueInfo(rep_id, get_offset(), byteBuffer.position());

        if (inBlock) {
            end_block();
        }

        // Write value_tag
        writeValueTag(true, true, null);

        // Write rep. id
        write_repositoryId(rep_id);

        // Write Value chunk
        end_flag--;
        chunkedValueNestingLevel--;

        // Make sure to chunk the custom data
        start_block();
    }

    @InfoMethod
    private void mustChunk(boolean flag) {
    }

    @CdrWrite
    public void end_value() {
        end_block();

        writeEndTag(true);

        // Check to see if we need to start another block for a
        // possible outer value.  Since we're in the stream
        // format 2 custom type contained by another custom
        // type, mustChunk should always be true.
        //
        // Here's why we need to open a continuation chunk:
        //
        // We need to enclose the default data of the
        // next subclass down in chunks.  There won't be
        // an end tag separating the superclass optional
        // data and the subclass's default data.

        mustChunk(mustChunk);

        if (mustChunk) {
            start_block();
        }
    }

    @Override
    @CdrWrite
    public void close() throws IOException {
        // tell BufferManagerWrite to release any ByteBuffers
        getBufferManager().close();

        if (byteBuffer != null) {

            // release this stream's ByteBuffer to the pool
            ByteBufferPool byteBufferPool = orb.getByteBufferPool();
            byteBufferPool.releaseByteBuffer(byteBuffer);
            byteBuffer = null;
        }
    }

    void dereferenceBuffer() {
        byteBuffer = null;
    }
}
