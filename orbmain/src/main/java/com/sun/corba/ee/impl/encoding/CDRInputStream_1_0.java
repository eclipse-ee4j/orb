/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.encoding;

import com.sun.corba.ee.impl.corba.CORBAObjectImpl;
import com.sun.corba.ee.impl.corba.PrincipalImpl;
import com.sun.corba.ee.impl.corba.TypeCodeImpl;
import com.sun.corba.ee.impl.misc.*;
import com.sun.corba.ee.impl.util.JDKBridge;
import com.sun.corba.ee.impl.util.RepositoryId;
import com.sun.corba.ee.impl.util.Utility;
import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.ior.IORFactories;
import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;
import com.sun.corba.ee.spi.logging.OMGSystemException;
import com.sun.corba.ee.spi.logging.ORBUtilSystemException;
import com.sun.corba.ee.spi.orb.ClassCodeBaseHandler;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.orb.ORBVersionFactory;
import com.sun.corba.ee.spi.presentation.rmi.PresentationDefaults;
import com.sun.corba.ee.spi.presentation.rmi.PresentationManager;
import com.sun.corba.ee.spi.presentation.rmi.StubAdapter;
import com.sun.corba.ee.spi.protocol.ClientDelegate;
import com.sun.corba.ee.spi.trace.CdrRead;
import com.sun.corba.ee.spi.trace.PrimitiveRead;
import com.sun.corba.ee.spi.transport.ByteBufferPool;
import com.sun.org.omg.SendingContext.CodeBase;
import org.glassfish.pfl.tf.spi.annotation.InfoMethod;
import org.omg.CORBA.*;
import org.omg.CORBA.TypeCodePackage.BadKind;
import org.omg.CORBA.portable.*;
import org.omg.CORBA_2_3.portable.InputStream;

import javax.rmi.CORBA.EnumDesc;
import javax.rmi.CORBA.ProxyDesc;
import javax.rmi.CORBA.Tie;
import javax.rmi.CORBA.ValueHandler;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.rmi.server.RMIClassLoader;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Arrays;
import java.util.List;
import java.lang.Object;

@CdrRead
@PrimitiveRead
public class CDRInputStream_1_0 extends CDRInputStreamBase 
    implements RestorableInputStream
{
    protected static final ORBUtilSystemException wrapper = ORBUtilSystemException.self;
    private static final OMGSystemException omgWrapper = OMGSystemException.self;
    private static final String K_READ_METHOD = "read";
    private static final int MAX_BLOCK_LENGTH = 0x7fffff00;

    protected BufferManagerRead bufferManagerRead;
    protected ByteBuffer byteBuffer;

    protected ORB orb;
    protected ValueHandler valueHandler = null;

    // Value cache
    private CacheTable<Object> valueCache = null;
    
    // Repository ID cache
    private CacheTable<String> repositoryIdCache = null;

    // codebase cache
    private CacheTable<String> codebaseCache = null;

    // Current Class Stack (repository Ids of current class being read)
    // private Stack currentStack = null;

    // Length of current chunk, or a large positive number if not in a chunk
    protected int blockLength = MAX_BLOCK_LENGTH;

    // Read end flag (value nesting depth)
    protected int end_flag = 0;

    // Beginning with the resolution to interop issue 3526 (4328?),
    // only enclosing chunked valuetypes are taken into account
    // when computing the nesting level.  However, we still need
    // the old computation around for interoperability with our
    // older ORBs.
    private int chunkedValueNestingLevel = 0;

    // Flag used to determine whether blocksize was zero
    // private int checkForNullBlock = -1;

    // In block flag
    // private boolean inBlock = false;

    // Indicates whether we are inside a value
    // private boolean outerValueDone = true;

    // Int used by read_value(Serializable) that is set by this class
    // before calling ValueFactory.read_value
    protected int valueIndirection = 0;

    // Int set by readStringOrIndirection to communicate the actual
    // offset of the string length field back to the caller
    protected int stringIndirection = 0;

    // Flag indicating whether we are unmarshalling a chunked value
    protected boolean isChunked = false;

    // Repository ID handlers
    private RepositoryIdUtility repIdUtil;
    private RepositoryIdStrings repIdStrs;

    // Code set converters (created when first needed)
    private CodeSetConversion.BTCConverter charConverter;
    private CodeSetConversion.BTCConverter wcharConverter;

    // RMI-IIOP stream format version 2 case in which we know
    // that there is no more optional data available.  If the
    // Serializable's readObject method tries to read anything,
    // we must throw a MARSHAL with the special minor code
    // so that the ValueHandler can give the correct exception
    // to readObject.  The state is cleared when the ValueHandler
    // calls end_value after the readObject method exits.
    private boolean specialNoOptionalDataState = false;

    // IMPORTANT: Do not replace 'new String("")' with "", it may result
    // in a Serialization bug. See http://bugs.java.com/view_bug.do?bug_id=4728756 for details
    @SuppressWarnings("RedundantStringConstructorCall")
    final String newEmptyString() {
        return new String("");
    }
    
    // Template method
    public CDRInputStreamBase dup() 
    {
        CDRInputStreamBase result = null ;

        try {
            result = this.getClass().newInstance();
        } catch (Exception e) {
            throw wrapper.couldNotDuplicateCdrInputStream( e ) ;
        }
        result.init(this.orb,
                byteBuffer,
                byteBuffer.limit(),
                byteBuffer.order(),
                this.bufferManagerRead);

        return result;
    }

    @Override
    void init(org.omg.CORBA.ORB orb, ByteBuffer byteBuffer, int bufferSize, ByteOrder byteOrder, BufferManagerRead bufferManager) {
        this.orb = (ORB)orb;
        this.bufferManagerRead = bufferManager;
        this.byteBuffer = byteBuffer;
        this.byteBuffer.position(0);
        this.byteBuffer.order(byteOrder);
        this.byteBuffer.limit(bufferSize);
        this.markAndResetHandler = bufferManagerRead.getMarkAndResetHandler();
    }

    // See description in CDRInputStream
    void performORBVersionSpecificInit() {
        createRepositoryIdHandlers();
    }

    private void createRepositoryIdHandlers()
    {
        repIdUtil = RepositoryIdFactory.getRepIdUtility();
        repIdStrs = RepositoryIdFactory.getRepIdStringsFactory();
    }

    public GIOPVersion getGIOPVersion() {
        return GIOPVersion.V1_0;
    }
    
    // Called by Request and Reply message. Valid for GIOP versions >= 1.2 only.
    // Illegal for GIOP versions < 1.2.
    void setHeaderPadding(boolean headerPadding) {
        throw wrapper.giopVersionError();
    }

    protected final int computeAlignment(int index, int align) {
        if (align > 1) {
            int incr = index & (align - 1);
            if (incr != 0) {
                return align - incr;
            }
        }

        return 0;
    }

    @InfoMethod
    private void notChunked() { }

    @CdrRead
    protected void checkBlockLength(int align, int dataSize) {
        // Since chunks can end at arbitrary points (though not within
        // primitive CDR types, arrays of primitives, strings, wstrings,
        // or indirections),
        // we must check here for termination of the current chunk.
        if (!isChunked) {
            notChunked() ;
            return;
        }

        // RMI-IIOP stream format version 2 case in which we know
        // that there is no more optional data available.  If the
        // Serializable's readObject method tries to read anything,
        // we must throw a MARSHAL exception with the special minor code
        // so that the ValueHandler can give the correct exception
        // to readObject.  The state is cleared when the ValueHandler
        // calls end_value after the readObject method exits.
        if (specialNoOptionalDataState) {
            throw omgWrapper.rmiiiopOptionalDataIncompatible1() ;
        }

        boolean checkForEndTag = false;

        // Are we at the end of the current chunk?  If so,
        // try to interpret the next long as a chunk length.
        // (It has to be either a chunk length, end tag,
        // or valuetag.)
        //
        // If it isn't a chunk length, blockLength will
        // remain set to maxBlockLength.
        if (blockLength == get_offset()) {

            blockLength = MAX_BLOCK_LENGTH;
            start_block();

            // What's next is either a valuetag or
            // an end tag.  If it's a valuetag, we're
            // probably being called as part of the process
            // to read the valuetag.  If it's an end tag,
            // then there isn't enough data left in
            // this valuetype to read!
            if (blockLength == MAX_BLOCK_LENGTH) {
                checkForEndTag = true;
            }

        } else if (blockLength < get_offset()) {
            // Are we already past the end of the current chunk?
            // This is always an error.
            throw wrapper.chunkOverflow() ;
        }

        // If what's next on the wire isn't a chunk length or
        // what we want to read (which can't be split across chunks)
        // won't fit in the current chunk, throw this exception.
        // This probably means that we're in an RMI-IIOP
        // Serializable's readObject method or a custom marshaled
        // IDL type is reading too much/in an incorrect order
        int requiredNumBytes = computeAlignment(byteBuffer.position(), align) + dataSize;

        if (blockLength != MAX_BLOCK_LENGTH &&
            blockLength < get_offset() + requiredNumBytes) {
            throw omgWrapper.rmiiiopOptionalDataIncompatible2() ;
        }

        // IMPORTANT - read_long() will advance the position of the ByteBuffer.
        //             Hence, in the logic below, we need to reset the position
        //             back to its original location.
        if (checkForEndTag) {
            int nextLong = read_long();
            byteBuffer.position(byteBuffer.position() - 4);

            // It was an end tag, so there wasn't enough data
            // left in the valuetype's encoding on the wire
            // to read what we wanted
            if (nextLong < 0) {
                throw omgWrapper.rmiiiopOptionalDataIncompatible3();
            }
        }
    }

    @CdrRead
    protected void alignAndCheck(int align, int n) {
        checkBlockLength(align, n);

        // WARNING: Must compute real alignment after calling
        // checkBlockLength since it may move the position
        int alignResult = computeAlignment(byteBuffer.position(), align);
        byteBuffer.position(byteBuffer.position() + alignResult);

        if (byteBuffer.position() + n > byteBuffer.limit()) {
            grow(align, n);
        }
    }

    //
    // This can be overridden....
    @CdrRead
    protected void grow(int align, int n) {
        byteBuffer = bufferManagerRead.underflow(byteBuffer);

    }

    //
    // Marshal primitives.
    //

    public final void consumeEndian() {
        ByteOrder byteOrder = read_boolean() ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;
        byteBuffer.order(byteOrder);
    }

    public final boolean read_boolean() {
        return (read_octet() != 0);
    }

    public final char read_char() {
        alignAndCheck(1, 1);

        return getConvertedChars(1, getCharConverter())[0];
    }

    @CdrRead
    public char read_wchar() {
        // Don't allow transmission of wchar/wstring data with foreign ORBs since it's against the spec.
        if (ORBUtility.isForeignORB(orb)) {
            throw wrapper.wcharDataInGiop10() ;
        }

        // If we're talking to one of our legacy ORBs, do what they did:
        return (char) byteBuffer.getShort();
    }

    @CdrRead
    public final byte read_octet() {
        alignAndCheck(1, 1);
        return byteBuffer.get();
    }

    @CdrRead
    public final short read_short() {
        alignAndCheck(2, 2);
        return byteBuffer.getShort();
    }

    public final short read_ushort() {
        return read_short();
    }

    @CdrRead
    public final int read_long() {
        alignAndCheck(4, 4);
        return byteBuffer.getInt();
    }

    public final int read_ulong() {
        return read_long();
    }

    @CdrRead
    public final long read_longlong() {
        alignAndCheck(8, 8);
        return byteBuffer.getLong();
    }

    public final long read_ulonglong() {
        return read_longlong();
    }

    public final float read_float() {
        return Float.intBitsToFloat(read_long());
    }

    public final double read_double() {
        return Double.longBitsToDouble(read_longlong());
    }

    protected final void checkForNegativeLength(int length) {
        if (length < 0) {
            throw wrapper.negativeStringLength(length);
        }
    }

    // Note that this has the side effect of setting the value of stringIndirection.
    @CdrRead
    protected final String readStringOrIndirection(boolean allowIndirection) {
        String result = "" ;

        int len = read_long();

        //
        // Check for indirection
        //
        if (allowIndirection) {
            if (len == 0xffffffff) {
                return null;
            } else {
                stringIndirection = get_offset() - 4;
            }
        }

        checkForNegativeLength(len);

        result = internalReadString(len);

        return result ;
    }

    @CdrRead
    private String internalReadString(int len) {
        // Workaround for ORBs which send string lengths of
        // zero to mean empty string.
        //
        if (len == 0) {
            return newEmptyString();
        }

        char[] result = getConvertedChars(len - 1, getCharConverter());

        // Skip over the 1 byte null
        read_octet();

        return new String(result, 0, getCharConverter().getNumChars());
    }

    public final String read_string() {
        return readStringOrIndirection(false);
    }

    @CdrRead
    public String read_wstring() {
        // Don't allow transmission of wchar/wstring data with
        // foreign ORBs since it's against the spec.
        if (ORBUtility.isForeignORB(orb)) {
            throw wrapper.wcharDataInGiop10();
        }

        int len = read_long();

        //
        // Workaround for ORBs which send string lengths of
        // zero to mean empty string.
        //
        if (len == 0) {
            return newEmptyString();
        }

        checkForNegativeLength(len);

        len--;
        char[] c = new char[len];

        for (int i = 0; i < len; i++) {
            c[i] = read_wchar();
        }

        // skip the two null terminator bytes
        read_wchar();

        return new String(c);
    }

    @CdrRead
    public final void read_octet_array(byte[] buffer, int offset, int length) {
        if ( buffer == null ) {
            throw wrapper.nullParam();
        }

        if (length == 0) {
            return;
        }

        alignAndCheck(1, 1);

        int numWritten = 0;
        while (numWritten < length) {
            if (!byteBuffer.hasRemaining()) grow(1, 1);

            int count = Math.min(length - numWritten, byteBuffer.remaining());
            byteBuffer.get(buffer, numWritten + offset, count);
            numWritten += count;
        }
    }

    @SuppressWarnings({"deprecation"})
    public org.omg.CORBA.Principal read_Principal() {
        int len = read_long();
        byte[] pvalue = new byte[len];
        read_octet_array(pvalue,0,len);

        org.omg.CORBA.Principal p = new PrincipalImpl();
        p.name(pvalue); 
        return p;
    }

    @CdrRead
    public TypeCode read_TypeCode() {
        TypeCodeImpl tc = new TypeCodeImpl(orb);
        tc.read_value(parent);
        return tc ;
    }
  
    @CdrRead
    public Any read_any() {
        Any any = null ;

        any = orb.create_any();
        TypeCodeImpl tc = new TypeCodeImpl(orb);

        // read off the typecode

        // REVISIT We could avoid this try-catch if we could peek the typecode
        // kind off this stream and see if it is a tk_value.  Looking at the
        // code we know that for tk_value the Any.read_value() below
        // ignores the tc argument anyway (except for the kind field).
        // But still we would need to make sure that the whole typecode,
        // including encapsulations, is read off.
        try {
            tc.read_value(parent);
        } catch (MARSHAL ex) {
            if (tc.kind().value() != TCKind._tk_value) {
                throw ex;
            }
            // We can be sure that the whole typecode encapsulation has been
            // read off.
        }
        // read off the value of the any
        any.read_value(parent, tc);

        return any;
    }

    @CdrRead
    public org.omg.CORBA.Object read_Object() { 
        return read_Object(null);
    }

    @InfoMethod
    private void nullIOR() { }

    @InfoMethod
    private void className( String name ) { }

    @InfoMethod
    private void stubFactory( PresentationManager.StubFactory fact ) { }

    // ------------ RMI related methods --------------------------

    // IDL to Java ptc-00-01-08 1.21.4.1
    //
    // The clz argument to read_Object can be either a stub
    // Class or the "Class object for the RMI/IDL interface type 
    // that is statically expected."
    // This functions as follows:
    // 1. If clz==null, just use the repository ID from the stub
    // 2. If clz is a stub class, just use it as a static factory.
    //    clz is a stub class iff StubAdapter.isStubClass( clz ).
    //    In addition, clz is a IDL stub class iff 
    //    IDLEntity.class.isAssignableFrom( clz ).
    // 3. If clz is an interface, use it to create the appropriate
    //    stub factory.
    @CdrRead
    public org.omg.CORBA.Object read_Object(Class clz) 
    {
        // In any case, we must first read the IOR.
        IOR ior = IORFactories.makeIOR( orb, (InputStream)parent) ;
        if (ior.isNil()) {
            nullIOR() ;
            return null;
        }

        PresentationManager.StubFactoryFactory sff = ORB.getStubFactoryFactory() ;
        String codeBase = ior.getProfile().getCodebase() ;
        PresentationManager.StubFactory stubFactory = null ;

        if (clz == null) {
            RepositoryId rid = RepositoryId.cache.getId( ior.getTypeId() ) ;
            String className = rid.getClassName() ;
            className( className ) ;
            boolean isIDLInterface = rid.isIDLType() ;

            if (className == null || className.equals( "" )) {
                stubFactory = null;
            } else {
                try {
                    stubFactory = sff.createStubFactory(className,
                        isIDLInterface, codeBase, (Class<?>) null,
                        (ClassLoader) null);
                } catch (Exception exc) {
                    stubFactory = null;
                }
            }
            stubFactory( stubFactory ) ;
        } else if (StubAdapter.isStubClass( clz )) {
            stubFactory = PresentationDefaults.makeStaticStubFactory(
                clz ) ;
            stubFactory( stubFactory ) ;
        } else {
            // clz is an interface class
            boolean isIDL = ClassInfoCache.get( clz ).isAIDLEntity(clz) ;

            stubFactory = sff.createStubFactory( clz.getName(), 
                isIDL, codeBase, clz, clz.getClassLoader() ) ;
            stubFactory( stubFactory ) ;
        }

        return internalIORToObject( ior, stubFactory, orb );
    }

    /*
     * This is used as a general utility (e.g., the PortableInterceptor
     * implementation uses it.   If stubFactory is null, the ior's
     * IIOPProfile must support getServant.
     */
    @CdrRead
    public static org.omg.CORBA.Object internalIORToObject(
        IOR ior, PresentationManager.StubFactory stubFactory, ORB orb)
    {
        java.lang.Object servant = ior.getProfile().getServant() ;
        if (servant != null ) {
            if (servant instanceof Tie) {
                String codebase = ior.getProfile().getCodebase();
                org.omg.CORBA.Object objref = (org.omg.CORBA.Object)
                    Utility.loadStub( (Tie)servant, stubFactory, codebase, 
                        false);
                    
                // If we managed to load a stub, return it, otherwise we
                // must fail...
                if (objref != null) {
                    return objref;   
                } else {
                    throw wrapper.readObjectException() ;
                }
            } else if (servant instanceof org.omg.CORBA.Object) {
                if (!(servant instanceof 
                        org.omg.CORBA.portable.InvokeHandler)) {
                    return (org.omg.CORBA.Object)servant;
                }
            } else {
                throw wrapper.badServantReadObject();
            }
        }

        ClientDelegate del = ORBUtility.makeClientDelegate( ior ) ;

        org.omg.CORBA.Object objref = null ;
        if (stubFactory == null) {
            objref = new CORBAObjectImpl();
        } else {
            try {
                objref = stubFactory.makeStub() ;
            } catch (Throwable e) {
                wrapper.stubCreateError( e ) ;

                if (e instanceof ThreadDeath) {
                    throw (ThreadDeath) e;
                }

                // Return the "default" stub...
                objref = new CORBAObjectImpl() ;            
            }
        }
        
        StubAdapter.setDelegate( objref, del ) ;
        return objref;
    }
 
    @CdrRead
    public java.lang.Object read_abstract_interface() 
    {
        return read_abstract_interface(null);
    }

    public java.lang.Object read_abstract_interface(java.lang.Class clz) 
    {
        boolean object = read_boolean();

        if (object) {
            return read_Object(clz);
        } else {
            return read_value();
        }
    }

    @CdrRead
    public Serializable read_value() 
    {
        return read_value((Class<?>)null);
    }

    @InfoMethod
    private void indirectionValue( int indir ) { }

    @CdrRead
    private Serializable handleIndirection() {
        int indirection = read_long() + get_offset() - 4;

        indirectionValue( indirection ) ;

        if (valueCache != null && valueCache.containsVal(indirection)) {

            java.io.Serializable cachedValue
                = (java.io.Serializable)valueCache.getKey(indirection);
            return cachedValue;
        } else {
            // In RMI-IIOP the ValueHandler will recognize this
            // exception and use the provided indirection value
            // to lookup a possible indirection to an object
            // currently on the deserialization stack.
            throw new IndirectionException(indirection);
        }
    }

    private String readRepositoryIds(int valueTag,
                                     Class<?> expectedType,
                                     ClassInfoCache.ClassInfo cinfo,
                                     String expectedTypeRepId) {
        return readRepositoryIds(valueTag, expectedType,
                                 cinfo, expectedTypeRepId, null);
    }

    /**
     * Examines the valuetag to see how many (if any) repository IDs
     * are present on the wire.  If no repository ID information
     * is on the wire but the expectedType or expectedTypeRepId
     * is known, it will return one of those (favoring the
     * expectedType's repId). Failing that, it uses the supplied 
     * BoxedValueHelper to obtain the repository ID, as a last resort.
     */
    private String readRepositoryIds(int valueTag,
                                     Class<?> expectedType,
                                     ClassInfoCache.ClassInfo cinfo,
                                     String expectedTypeRepId,
                                     BoxedValueHelper factory) {
        switch(repIdUtil.getTypeInfo(valueTag)) {
            case RepositoryIdUtility.NO_TYPE_INFO :
                // Throw an exception if we have no repository ID info and
                // no expectedType to work with.  Otherwise, how would we
                // know what to unmarshal?
                if (expectedType == null) {
                    if (expectedTypeRepId != null) {
                        return expectedTypeRepId;
                    } else if (factory != null) {
                        return factory.get_id();
                    } else {
                        throw wrapper.expectedTypeNullAndNoRepId( ) ;
                    }
                }
                return repIdStrs.createForAnyType(expectedType,cinfo);
            case RepositoryIdUtility.SINGLE_REP_TYPE_INFO :
                return read_repositoryId(); 
            case RepositoryIdUtility.PARTIAL_LIST_TYPE_INFO :
                return read_repositoryIds();
            default:
                throw wrapper.badValueTag( Integer.toHexString(valueTag) ) ;
        }
    }

    @CdrRead
    private Object readRMIIIOPValueType( int indirection, 
        Class<?> valueClass, String repositoryIDString ) {

        try {
            if (valueHandler == null) {
                valueHandler = ORBUtility.createValueHandler();
            }

            return valueHandler.readValue(parent, indirection, valueClass, 
                repositoryIDString, getCodeBase());
        } catch(SystemException sysEx) {
            // Just rethrow any CORBA system exceptions
            // that come out of the ValueHandler
            throw sysEx;
        } catch(Exception ex) {
            throw wrapper.valuehandlerReadException( ex ) ;
        } catch(Error e) {
            throw wrapper.valuehandlerReadError( e ) ;
        }
    }

    @InfoMethod
    private void repositoryIdString( String str ) { } 

    @InfoMethod
    private void valueClass( Class cls ) { }

    @InfoMethod
    private void noProxyInterfaces() { }

    @CdrRead
    public Serializable read_value(Class expectedType) {
        Object value = null ;
        int vType = readValueTag();
        if (vType == 0) {
            return null;
        }

        if (vType == 0xffffffff) {
            value = handleIndirection();
        } else {
            ClassInfoCache.ClassInfo cinfo = null ;
            if (expectedType != null) {
                cinfo = ClassInfoCache.get(expectedType);
            }

            int indirection = get_offset() - 4;

            // Need to save this special marker variable
            // to restore its value during recursion
            boolean saveIsChunked = isChunked;
            isChunked = repIdUtil.isChunkedEncoding(vType);

            String codebase_URL = null;
            if (repIdUtil.isCodeBasePresent(vType)) {
                codebase_URL = read_codebase_URL();
            }

            // Read repository id(s)
            String repositoryIDString = readRepositoryIds(vType, expectedType,
                cinfo, null);
            repositoryIdString( repositoryIDString ) ;

            // If isChunked was determined to be true based
            // on the valuetag, this will read a chunk length
            start_block();

            // Remember that end_flag keeps track of all nested
            // valuetypes and is used for older ORBs
            end_flag--;
            if (isChunked) {
                chunkedValueNestingLevel--;
            }

            if (repositoryIDString.equals(repIdStrs.getWStringValueRepId())) {
                value = read_wstring();
            } else if (repositoryIDString.equals(repIdStrs.getClassDescValueRepId())) {
                value = readClass();
            } else {
                Class valueClass = expectedType;

                // By this point, either the expectedType or repositoryIDString
                // is guaranteed to be non-null.
                if (valueClass == null || !repositoryIDString.equals(repIdStrs.createForAnyType(expectedType,cinfo))) {

                    valueClass = getClassFromString(repositoryIDString, codebase_URL, expectedType);
                    cinfo = ClassInfoCache.get( valueClass ) ;
                }

                valueClass( valueClass ) ;

                if (valueClass == null) {
                    // No point attempting to use value handler below, since the
                    // class information is not available.
                    // Fix for issue 1828: pass the class name for a better log
                    // message.
                    RepositoryIdInterface repositoryID = repIdStrs.getFromString(repositoryIDString);

                    throw wrapper.couldNotFindClass(repositoryID.getClassName()) ;
                }

                if (cinfo.isEnum()) {
                    final Class enumClass = ClassInfoCache.getEnumClass( cinfo, 
                        valueClass ) ;
                    String enumValue = read_string() ;
                    value = Enum.valueOf( enumClass, enumValue ) ;
                } else if (valueClass != null && cinfo.isAIDLEntity(valueClass)) {
                    value = readIDLValue(indirection, repositoryIDString,
                        valueClass, cinfo, codebase_URL);
                } else {
                    value = readRMIIIOPValueType( indirection,
                        valueClass, repositoryIDString ) ;
                }
            }

            // Skip any remaining chunks until we get to
            // an end tag or a valuetag.  If we see a valuetag,
            // that means there was another valuetype in the sender's
            // version of this class that we need to skip over.
            handleEndOfValue();

            // Read and process the end tag if we're chunking.
            // Assumes that we're at the position of the end tag
            // (handleEndOfValue should assure this)
            readEndTag();

            // Cache the valuetype that we read
            if (valueCache == null) {
                valueCache = new CacheTable<Object>("Input valueCache", orb,
                    false);
            }
            valueCache.put(value, indirection);

            // Allow for possible continuation chunk.
            // If we're a nested valuetype inside of a chunked
            // valuetype, and that enclosing valuetype has
            // more data to write, it will need to have this
            // new chunk begin after we wrote our end tag.
            isChunked = saveIsChunked;
            start_block();
        }

        // Convert an EnumDesc into the enum instance it represents
        if (value.getClass()==EnumDesc.class) {
            EnumDesc desc = EnumDesc.class.cast( value ) ;

            Class cls = null ;
            try {
                cls = JDKBridge.loadClass( desc.className, null, null ) ;
            } catch (ClassNotFoundException cnfe) {
                throw wrapper.enumClassNotFound( cnfe, desc.className ) ;
            }

            // Issue 11681: deal with enum with abstract methods.
            Class current = cls ;
            while (current != null) {
                if (current.isEnum()) {
                    break ;
                }
                current = current.getSuperclass() ;
            }

            if (current != null) {
                value = Enum.valueOf( current, desc.value ) ;
            } else {
                throw wrapper.couldNotUnmarshalEnum( desc.className,
                    desc.value ) ;
            }
        }

        // Convert ProxyDesc into the proxy instance it represents
        if (value.getClass()==ProxyDesc.class) {
            ProxyDesc desc = ProxyDesc.class.cast( value ) ;
            int numberOfInterfaces = desc.interfaces.length;

            // Write code if the number is Zero. Unusual case
            if (numberOfInterfaces==0) {
                noProxyInterfaces() ;
                return null;
            }

            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            try {
                RMIClassLoader.loadProxyClass( desc.codebase, desc.interfaces,
                    value.getClass().getClassLoader()) ;
            } catch (ClassNotFoundException cnfe) {
                throw wrapper.proxyClassNotFound( cnfe,
                    getInterfacesList(desc.interfaces)) ;
            } catch (MalformedURLException mue) {
                throw wrapper.malformedProxyUrl( mue,
                    getInterfacesList(desc.interfaces), desc.codebase) ;
            }

            Class[] list = new Class[desc.interfaces.length];
            for(int i=0; i < numberOfInterfaces; ++i) {
                try {
                    list[i] = JDKBridge.loadClass(desc.interfaces[i],
                        desc.codebase, cl);
                 } catch (ClassNotFoundException cnfe) {
                     throw wrapper.proxyClassNotFound(cnfe, desc.interfaces[i]);
                 }
            }

            try {
                value = Proxy.newProxyInstance(cl, list, desc.handler);
            } catch (IllegalArgumentException iage) {
                throw wrapper.proxyWithIllegalArgs(iage);
            } catch (NullPointerException npe) {
                throw wrapper.emptyProxyInterfaceList(npe);
            }
        }

        return (java.io.Serializable)value;
    }

    private List<String> getInterfacesList(String [] interfaces) {
        return Arrays.asList(interfaces);
    }

    @CdrRead
    @SuppressWarnings("deprecation")
    public Serializable read_value(BoxedValueHelper factory) {

        // Read value tag
        int vType = readValueTag();

        if (vType == 0) {
            return null;
        } else if (vType == 0xffffffff) { // Indirection tag
            int indirection = read_long() + get_offset() - 4;
            if (valueCache != null && valueCache.containsVal(indirection)) {
                Serializable cachedValue = 
                       (Serializable)valueCache.getKey(indirection);
                return cachedValue;
            } else {
                throw new IndirectionException(indirection);
            }
        } else {
            int indirection = get_offset() - 4;

            boolean saveIsChunked = isChunked;
            isChunked = repIdUtil.isChunkedEncoding(vType);

            java.lang.Object value = null;

            String codebase_URL = null;                 
            if (repIdUtil.isCodeBasePresent(vType)){
                codebase_URL = read_codebase_URL();
            }

            // Read repository id
            String repositoryIDString
                = readRepositoryIds(vType, null, null, null, factory);

            // Compare rep. ids to see if we should use passed helper
            if (!repositoryIDString.equals(factory.get_id())) {
                factory = Utility.getHelper(null, codebase_URL, repositoryIDString);
            }

            start_block();
            end_flag--;
            if (isChunked) {
                chunkedValueNestingLevel--;
            }
            
            if (factory instanceof com.sun.org.omg.CORBA.portable.ValueHelper) {
                value = readIDLValueWithHelper(
                    (com.sun.org.omg.CORBA.portable.ValueHelper)factory, indirection);
            } else {
                valueIndirection = indirection;  // for callback
                value = factory.read_value(parent);
            }

            handleEndOfValue();
            readEndTag();

            // Put into valueCache
            if (valueCache == null) {
                valueCache = new CacheTable<Object>("Input valueCache", orb, false);
            }
            valueCache.put(value, indirection);
        
            // allow for possible continuation chunk
            isChunked = saveIsChunked;
            start_block();

            return (java.io.Serializable)value;
        }
    }

    @SuppressWarnings({"deprecation"})
    private boolean isCustomType(@SuppressWarnings("deprecation") com.sun.org.omg.CORBA.portable.ValueHelper helper) {
        try{
            TypeCode tc = helper.get_type();
            int kind = tc.kind().value();
            if (kind == TCKind._tk_value) {
                return (tc.type_modifier() == org.omg.CORBA.VM_CUSTOM.value);
            }
        } catch(BadKind ex) {
            throw wrapper.badKind(ex) ;
        }

        return false;
    }

    // This method is actually called indirectly by 
    // read_value(String repositoryId).
    // Therefore, it is not a truly independent read call that handles
    // header information itself.
    @CdrRead
    public java.io.Serializable read_value(java.io.Serializable value) {

        // Put into valueCache using valueIndirection
        if (valueCache == null) {
            valueCache = new CacheTable<Object>("Input valueCache", orb, false);
        }
        valueCache.put(value, valueIndirection);

        if (value instanceof StreamableValue) {
            ((StreamableValue) value)._read(parent);
        } else if (value instanceof CustomValue) {
            ((CustomValue) value).unmarshal(parent);
        }
                        
        return value;
    }

    @CdrRead
    public java.io.Serializable read_value(java.lang.String repositoryId) {

        // if (inBlock)
        //    end_block();

        // Read value tag
        int vType = readValueTag();

        if (vType == 0) {
            return null;
        } else if (vType == 0xffffffff) { // Indirection tag
            int indirection = read_long() + get_offset() - 4;
            if (valueCache != null && valueCache.containsVal(indirection))
                {
                    java.io.Serializable cachedValue = 
                          (java.io.Serializable)valueCache.getKey(indirection);
                    return cachedValue;
                }
            else {
                throw new IndirectionException(indirection);
            }
        } else {
            int indirection = get_offset() - 4;

            // end_block();

            boolean saveIsChunked = isChunked;
            isChunked = repIdUtil.isChunkedEncoding(vType);

            java.lang.Object value = null;

            String codebase_URL = null;                 
            if (repIdUtil.isCodeBasePresent(vType)){
                codebase_URL = read_codebase_URL();
            }

            // Read repository id
            String repositoryIDString
                = readRepositoryIds(vType, null, null, repositoryId);

            ValueFactory factory = 
               Utility.getFactory(null, codebase_URL, orb, repositoryIDString);

            start_block();
            end_flag--;
            if (isChunked) {
                chunkedValueNestingLevel--;
            }

            valueIndirection = indirection;  // for callback
            value = factory.read_value(parent);

            handleEndOfValue();
            readEndTag();

            // Put into valueCache
            if (valueCache == null) {
                valueCache = new CacheTable<Object>("Input valueCache", orb, false);
            }
            valueCache.put(value, indirection);
        
            // allow for possible continuation chunk
            isChunked = saveIsChunked;
            start_block();

            return (java.io.Serializable)value;
        }               
    }

    @InfoMethod
    private void readClassCodebases( String codebases, String repoId ) { }

    @CdrRead
    private Class<?> readClass() {

        String codebases, classRepId;

        if (orb == null ||
            ORBVersionFactory.getFOREIGN().equals(orb.getORBVersion()) ||
            ORBVersionFactory.getNEWER().compareTo(orb.getORBVersion()) <= 0) {

            codebases = (String)read_value(java.lang.String.class);
            classRepId = (String)read_value(java.lang.String.class);
        } else {
            // Pre-Merlin/J2EE 1.3 ORBs wrote the repository ID
            // and codebase strings in the wrong order.
            classRepId = (String)read_value(java.lang.String.class);
            codebases = (String)read_value(java.lang.String.class);
        }

        readClassCodebases( codebases, classRepId ) ;

        Class<?> cl = null;

        RepositoryIdInterface repositoryID 
            = repIdStrs.getFromString(classRepId);
        
        try {
            cl = repositoryID.getClassFromType(codebases);
        } catch(ClassNotFoundException cnfe) {
            throw wrapper.cnfeReadClass( cnfe, repositoryID.getClassName() ) ;
        } catch(MalformedURLException me) {
            throw wrapper.malformedUrl( 
                me, repositoryID.getClassName(), codebases ) ;
        }

        return cl;
    }

    @SuppressWarnings({"deprecation"})
    @CdrRead
    private java.lang.Object readIDLValueWithHelper(
        com.sun.org.omg.CORBA.portable.ValueHelper helper, int indirection) 
    {
        // look for two-argument static read method
        Method readMethod;
        try {
            readMethod = helper.getClass().getDeclaredMethod(K_READ_METHOD,
                org.omg.CORBA.portable.InputStream.class, helper.get_class());
        }
        catch(NoSuchMethodException nsme) { // must be boxed value helper
            java.lang.Object result = helper.read_value(parent);
            return result;
        }

        // found two-argument read method, so must be non-boxed value...
        // ...create a blank instance
        java.lang.Object val = null;
        try {
            val = helper.get_class().newInstance();
        } catch(java.lang.InstantiationException ie) {
            throw wrapper.couldNotInstantiateHelper( ie,
                helper.get_class() ) ;
        } catch(IllegalAccessException iae){ 
            // Value's constructor is protected or private
            //
            // So, use the helper to read the value.
            //
            // NOTE : This means that in this particular case a recursive ref.
            // would fail.
            return helper.read_value(parent);
        }

        // add blank instance to cache table
        if (valueCache == null) {
            valueCache = new CacheTable<Object>("Input valueCache", orb, false);
        }
        valueCache.put(val, indirection);

        // if custom type, call unmarshal method
        if (val instanceof CustomMarshal && isCustomType(helper)) {
            ((CustomMarshal)val).unmarshal(parent);
            return val;
        }

        // call two-argument read method using reflection
        try {
            readMethod.invoke(helper, parent, val );
            return val;
        } catch(IllegalAccessException iae2) {
            throw wrapper.couldNotInvokeHelperReadMethod( iae2,
                helper.get_class() ) ;
        } catch(InvocationTargetException ite){
            throw wrapper.couldNotInvokeHelperReadMethod( ite,
                helper.get_class() ) ;
        }
    }

    @CdrRead
    private java.lang.Object readBoxedIDLEntity(Class<?> clazz, String codebase)
    {
        Class<?> cls = null ;

        try {
            ClassLoader clazzLoader = clazz.getClassLoader();

            cls = Utility.loadClassForClass(clazz.getName()+"Helper", codebase,
                clazzLoader, clazz, clazzLoader);
            final Class<?> helperClass = cls ;

            // getDeclaredMethod requires RuntimePermission 
            // accessDeclaredMembers if a different class loader is used
            // (even though the javadoc says otherwise)
            Method readMethod = null;
            try {
                readMethod = AccessController.doPrivileged(
                    new PrivilegedExceptionAction<Method>() {
                    @SuppressWarnings("unchecked")
                        public Method run() throws NoSuchMethodException {
                            return helperClass.getDeclaredMethod(K_READ_METHOD,
                                org.omg.CORBA.portable.InputStream.class ) ;
                        }
                    }
                );
            } catch (PrivilegedActionException pae) {
                // this gets caught below
                throw (NoSuchMethodException)pae.getException();
            }

            return readMethod.invoke(null, parent);
        } catch (ClassNotFoundException cnfe) {
            throw wrapper.couldNotInvokeHelperReadMethod( cnfe, cls ) ;
        } catch(NoSuchMethodException nsme) {
            throw wrapper.couldNotInvokeHelperReadMethod( nsme, cls ) ;
        } catch(IllegalAccessException iae) {
            throw wrapper.couldNotInvokeHelperReadMethod( iae, cls ) ;
        } catch(InvocationTargetException ite) {
            throw wrapper.couldNotInvokeHelperReadMethod( ite, cls ) ;
        }
    }

    @CdrRead
    @SuppressWarnings({"deprecation", "deprecation"})
    private java.lang.Object readIDLValue(int indirection, String repId, 
        Class<?> clazz, ClassInfoCache.ClassInfo cinfo, String codebase)
    {                                   
        ValueFactory factory ;

        // Always try to find a ValueFactory first, as required by the spec.
        // There are some complications here in the IDL 3.0 mapping 
        // (see 1.13.8), but basically we must always be able to override the
        // DefaultFactory or Helper mappings that are also used.  This appears
        // to be the case even in the boxed value cases.  The original code
        // only did the lookup in the case of class implementing either
        // StreamableValue or CustomValue, but abstract valuetypes only
        // implement ValueBase, and really require the use of the repId to
        // find a factory (including the DefaultFactory).
        try {
            // use new-style OBV support (factory object)
            factory = Utility.getFactory(clazz, codebase, orb, repId);
        } catch (MARSHAL marshal) {
            wrapper.marshalErrorInReadIDLValue( marshal ) ;

            // Could not get a factory, so try alternatives
            if (!cinfo.isAStreamableValue(clazz) && 
                !cinfo.isACustomValue(clazz) && cinfo.isAValueBase(clazz)) {

                // use old-style OBV support (helper object)
                BoxedValueHelper helper = Utility.getHelper(clazz, codebase, 
                    repId);
                if (helper instanceof com.sun.org.omg.CORBA.portable.ValueHelper) {
                    return readIDLValueWithHelper(
                            (com.sun.org.omg.CORBA.portable.ValueHelper) helper,
                                indirection);
                } else {
                    return helper.read_value(parent);
                }
            } else {
                // must be a boxed IDLEntity, so make a reflective call to the
                // helper's static read method...
                return readBoxedIDLEntity(clazz, codebase);
            }
        }

        // If there was no error in getting the factory, use it.
        valueIndirection = indirection;  // for callback
        return factory.read_value(parent);
    }

    @InfoMethod
    private void endTag( int endTag ) { }

    @InfoMethod
    private void chunkedNestingLevel( int nl ) { }

    @InfoMethod
    private void endFlag( int value ) { }

    /**
     * End tags are only written for chunked valuetypes.
     *
     * Before Merlin, our ORBs wrote end tags which took into account
     * all enclosing valuetypes.  This was changed by an interop resolution
     * (see details around chunkedValueNestingLevel) to only include
     * enclosing chunked types.
     *
     * ORB versioning and end tag compaction are handled here.
     */
    @CdrRead
    private void readEndTag() {
        if (isChunked) {
            // Read the end tag
            int anEndTag = read_long();
            endTag( anEndTag ) ;

            // End tags should always be negative, and the outermost
            // enclosing chunked valuetype should have a -1 end tag.
            //
            // handleEndOfValue should have assured that we were
            // at the end tag position!
            if (anEndTag >= 0) {
                throw wrapper.positiveEndTag( anEndTag, get_offset() - 4 ) ;
            }

            // If the ORB is null, or if we're sure we're talking to
            // a foreign ORB, Merlin, or something more recent, we
            // use the updated end tag computation, and are more strenuous
            // about the values.
            if (orb == null ||
                ORBVersionFactory.getFOREIGN().equals(orb.getORBVersion()) ||
                ORBVersionFactory.getNEWER().compareTo(orb.getORBVersion()) <= 0) {

                // If the end tag we read was less than what we were expecting,
                // then the sender must think it's sent more enclosing 
                // chunked valuetypes than we have.  Throw an exception.
                if (anEndTag < chunkedValueNestingLevel) {
                    throw wrapper.unexpectedEnclosingValuetype( anEndTag, chunkedValueNestingLevel );
                }

                // If the end tag is bigger than what we expected, but
                // still negative, then the sender has done some end tag
                // compaction.  We back up the stream 4 bytes so that the
                // next time readEndTag is called, it will get down here
                // again.  Even with fragmentation, we'll always be able
                // to do this.
                if (anEndTag != chunkedValueNestingLevel) {
                    byteBuffer.position(byteBuffer.position() - 4);
                }
            } else {
                // When talking to Kestrel (JDK 1.3) or Ladybird (JDK 1.3.1), we use our old
                // end tag rules and are less strict.  If the end tag
                // isn't what we expected, we back up, assuming
                // compaction.
                if (anEndTag != end_flag) {
                    byteBuffer.position(byteBuffer.position() - 4);
                }
            }

            // This only keeps track of the enclosing chunked
            // valuetypes
            chunkedValueNestingLevel++;
            chunkedNestingLevel( chunkedValueNestingLevel ) ;
        }

        // This keeps track of all enclosing valuetypes
        end_flag++;
        endFlag( end_flag ) ;
    }

    protected int get_offset() {
        return byteBuffer.position();
    }

    @InfoMethod
    private void unreadLastLong() { }

    @CdrRead
    private void start_block() {
        // if (outerValueDone)
        if (!isChunked) {
            return;
        }

        // if called from alignAndCheck, need to reset blockLength
        // to avoid an infinite recursion loop on read_long() call
        blockLength = MAX_BLOCK_LENGTH;

        blockLength = read_long();

        // Must remember where we began the chunk to calculate how far
        // along we are.  See notes above about chunkBeginPos.

        if (blockLength > 0 && blockLength < MAX_BLOCK_LENGTH) {
            blockLength += get_offset();  // _REVISIT_ unsafe, should use a Java long
        } else {
            blockLength = MAX_BLOCK_LENGTH;
            byteBuffer.position(byteBuffer.position() - 4);
        }
    }

    @InfoMethod
    private void peekNextLong( long val ) { }

    // Makes sure that if we were reading a chunked value, we end up
    // at the right place in the stream, no matter how little the
    // unmarshalling code read.
    //
    // After calling this method, if we are chunking, we should be
    // in position to read the end tag.
    @CdrRead
    private void handleEndOfValue() {
        // If we're not chunking, we don't have to worry about
        // skipping remaining chunks or finding end tags
        if (!isChunked) {
            return;
        }

        // Skip any remaining chunks
        while (blockLength != MAX_BLOCK_LENGTH) {
            end_block();
            start_block();
        }

        // Now look for the end tag

        // This is a little wasteful since we're reading
        // this long up to 3 times in the worst cases (once
        // in start_block, once here, and once in readEndTag
        //
        // Peek next long
        int nextLong = read_long();
        peekNextLong( nextLong ) ;
        byteBuffer.position(byteBuffer.position() - 4);

        // We did find an end tag, so we're done.  readEndTag
        // should take care of making sure it's the correct
        // end tag, etc.  Remember that since end tags,
        // chunk lengths, and valuetags have non overlapping
        // ranges, we can tell by the value what the longs are.
        if (nextLong < 0) {
            return;
        }

        if (nextLong == 0 || nextLong >= MAX_BLOCK_LENGTH) {

            // A custom marshaled valuetype left extra data
            // on the wire, and that data had another
            // nested value inside of it.  We've just
            // read the value tag or null of that nested value.
            //
            // In an attempt to get by it, we'll try to call
            // read_value() to get the nested value off of
            // the wire.  Afterwards, we must call handleEndOfValue
            // recursively to read any further chunks that the containing
            // valuetype might still have after the nested
            // value.
            read_value();
            handleEndOfValue();
        } else {
            // This probably means that the code to skip chunks has
            // an error, and ended up setting blockLength to something
            // other than maxBlockLength even though we weren't
            // starting a new chunk.
            throw wrapper.couldNotSkipBytes( nextLong , get_offset() ) ;
        }
    }

    @CdrRead
    private void end_block() {
        // if in a chunk, check for underflow or overflow
        if (blockLength != MAX_BLOCK_LENGTH) {
            if (blockLength == get_offset()) {
                // Chunk ended correctly
                blockLength = MAX_BLOCK_LENGTH;
            } else {
                // Skip over anything left by bad unmarshaling code (ex:
                // a buggy custom unmarshaler).  See handleEndOfValue.
                if (blockLength > get_offset()) {
                    skipToOffset(blockLength);
                } else {
                    throw wrapper.badChunkLength( blockLength, get_offset() ) ;
                }
            }
        }
    }
    
    @CdrRead
    private int readValueTag(){
        // outerValueDone = false;
        return read_long();
    }

    public org.omg.CORBA.ORB orb() {
        return orb;    
    }

    // ------------ End RMI related methods --------------------------

    public final void read_boolean_array(boolean[] value, int offset, int length) {
        for(int i=0; i < length; i++) {
            value[i+offset] = read_boolean();
        }
    }

    public final void read_char_array(char[] value, int offset, int length) {
        for(int i=0; i < length; i++) {
            value[i+offset] = read_char();
        }
    }

    public final void read_wchar_array(char[] value, int offset, int length) {
        for(int i=0; i < length; i++) {
            value[i+offset] = read_wchar();
        }
    }

    public final void read_short_array(short[] value, int offset, int length) {
        for(int i=0; i < length; i++) {
            value[i+offset] = read_short();
        }
    }

    public final void read_ushort_array(short[] value, int offset, int length) {
        read_short_array(value, offset, length);
    }

    public final void read_long_array(int[] value, int offset, int length) {
        for(int i=0; i < length; i++) {
            value[i+offset] = read_long();
        }
    }

    public final void read_ulong_array(int[] value, int offset, int length) {
        read_long_array(value, offset, length);
    }

    public final void read_longlong_array(long[] value, int offset, int length) {
        for(int i=0; i < length; i++) {
            value[i+offset] = read_longlong();
        }
    }

    public final void read_ulonglong_array(long[] value, int offset, int length) {
        read_longlong_array(value, offset, length);
    }

    public final void read_float_array(float[] value, int offset, int length) {
        for(int i=0; i < length; i++) {
            value[i+offset] = read_float();
        }
    }

    public final void read_double_array(double[] value, int offset, int length) {
        for(int i=0; i < length; i++) {
            value[i+offset] = read_double();
        }
    }

    public final void read_any_array(org.omg.CORBA.Any[] value, int offset, int length) {
        for(int i=0; i < length; i++) {
            value[i+offset] = read_any();
        }
    }

    //--------------------------------------------------------------------//
    // CDRInputStream state management.
    //


    @CdrRead
    private String read_repositoryIds() {
                
        // Read # of repository ids
        int numRepIds = read_long();
        if (numRepIds == 0xffffffff) {
            int indirection = read_long() + get_offset() - 4;
            if (repositoryIdCache != null && repositoryIdCache.containsVal(indirection)) {
                return repositoryIdCache.getKey(indirection);
            } else {
                throw wrapper.unableToLocateRepIdArray(indirection);
            }
        } else {
            // read first array element and store it as an indirection to the whole array
            int indirection = get_offset(); 
            String repID = read_repositoryId();
            if (repositoryIdCache == null) {
                repositoryIdCache = new CacheTable<String>("Input repositoryIdCache", orb, false);
            }
            repositoryIdCache.put(repID, indirection);

            // read and ignore the subsequent array elements, but put them in the
            // indirection table in case there are later indirections back to them
            for (int i = 1; i < numRepIds; i++) {
                read_repositoryId();
            }
                
            return repID;
        }
    }

    @CdrRead
    private String read_repositoryId() {
        String result = readStringOrIndirection(true);
        if (result == null) { // Indirection
            int indirection = read_long() + get_offset() - 4;

            if (repositoryIdCache != null) {
                result = repositoryIdCache.getKey(indirection);
            }
        } else {
            if (repositoryIdCache == null) {
                repositoryIdCache = new CacheTable<String>("Input repositoryIdCache", orb, false);
            }
            repositoryIdCache.put(result, stringIndirection);
        }

        if (result != null) {
            return result;
        }

        throw wrapper.badRepIdIndirection(byteBuffer.position()) ;                              // todo test this case
    }

    @CdrRead
    private String read_codebase_URL() {
        String result = readStringOrIndirection(true);
        if (result == null) { // Indirection
            int indirection = read_long() + get_offset() - 4;

            if (codebaseCache != null) {
                result = codebaseCache.getKey(indirection) ;
            }
        } else {
            if (codebaseCache == null) {
                codebaseCache = new CacheTable<String>("Input codebaseCache", orb, false);
            }
            codebaseCache.put(result, stringIndirection);
        }

        if (result != null) {
            return result;
        }

        throw wrapper.badCodebaseIndirection(byteBuffer.position()) ;                            // todo test this case
    }

    /* DataInputStream methods */

    public java.lang.Object read_Abstract () {
        return read_abstract_interface();
    }

    public java.io.Serializable read_Value () {
        return read_value();
    }

    public void read_any_array (org.omg.CORBA.AnySeqHolder seq, int offset, int length) {
        read_any_array(seq.value, offset, length);
    }

    public void read_boolean_array (org.omg.CORBA.BooleanSeqHolder seq, int offset, int length) {
        read_boolean_array(seq.value, offset, length);
    }

    public void read_char_array (org.omg.CORBA.CharSeqHolder seq, int offset, int length) {
        read_char_array(seq.value, offset, length);
    }

    public void read_wchar_array (org.omg.CORBA.WCharSeqHolder seq, int offset, int length) {
        read_wchar_array(seq.value, offset, length);
    }

    public void read_octet_array (org.omg.CORBA.OctetSeqHolder seq, int offset, int length) {
        read_octet_array(seq.value, offset, length);
    }

    public void read_short_array (org.omg.CORBA.ShortSeqHolder seq, int offset, int length) {
        read_short_array(seq.value, offset, length);
    }

    public void read_ushort_array (org.omg.CORBA.UShortSeqHolder seq, int offset, int length) {
        read_ushort_array(seq.value, offset, length);
    }

    public void read_long_array (org.omg.CORBA.LongSeqHolder seq, int offset, int length) {
        read_long_array(seq.value, offset, length);
    }

    public void read_ulong_array (org.omg.CORBA.ULongSeqHolder seq, int offset, int length) {
        read_ulong_array(seq.value, offset, length);
    }

    public void read_ulonglong_array (org.omg.CORBA.ULongLongSeqHolder seq, int offset, int length) {
        read_ulonglong_array(seq.value, offset, length);
    }

    public void read_longlong_array (org.omg.CORBA.LongLongSeqHolder seq, int offset, int length) {
        read_longlong_array(seq.value, offset, length);
    }

    public void read_float_array (org.omg.CORBA.FloatSeqHolder seq, int offset, int length) {
        read_float_array(seq.value, offset, length);
    }

    public void read_double_array (org.omg.CORBA.DoubleSeqHolder seq, int offset, int length) {
        read_double_array(seq.value, offset, length);
    }

    public java.math.BigDecimal read_fixed(short digits, short scale) {
        // digits isn't really needed here
        StringBuffer buffer = read_fixed_buffer();
        if (digits != buffer.length()) {
            throw wrapper.badFixed(digits, buffer.length());
        }
        buffer.insert(digits - scale, '.');
        return new BigDecimal(buffer.toString());
    }

    // This method is unable to yield the correct scale.
    public java.math.BigDecimal read_fixed() {
        return new BigDecimal(read_fixed_buffer().toString());
    }

    // Each octet contains (up to) two decimal digits.
    // If the fixed type has an odd number of decimal digits, then the representation
    // begins with the first (most significant) digit.
    // Otherwise, this first half-octet is all zero, and the first digit
    // is in the second half-octet.
    // The sign configuration, in the last half-octet of the representation,
    // is 0xD for negative numbers and 0xC for positive and zero values.
    private StringBuffer read_fixed_buffer() {
        StringBuffer buffer = new StringBuffer(64);
        byte doubleDigit;
        int firstDigit;
        int secondDigit;
        boolean wroteFirstDigit = false;
        boolean more = true;
        while (more) {
            doubleDigit = this.read_octet();
            firstDigit = (doubleDigit & 0xf0) >> 4;
            secondDigit = doubleDigit & 0x0f;
            if (wroteFirstDigit || firstDigit != 0) {
                buffer.append(Character.forDigit(firstDigit, 10));
                wroteFirstDigit = true;
            }
            if (secondDigit == 12) {
                // positive number or zero
                if ( ! wroteFirstDigit) {
                    // zero
                    return new StringBuffer("0.0");
                } else {
                    // positive number
                    // done
                }
                more = false;
            } else if (secondDigit == 13) {
                // negative number
                buffer.insert(0, '-');
                more = false;
            } else {
                buffer.append(Character.forDigit(secondDigit, 10));
                wroteFirstDigit = true;
            }
        }
        return buffer;
    }

    private final static String _id = "IDL:omg.org/CORBA/DataInputStream:1.0";
    private final static String[] _ids = { _id };

    public String[] _truncatable_ids() {
        if (_ids == null) {
            return null;
        }

        return _ids.clone();
    }

    public int getBufferLength() {
        return byteBuffer.limit();
    }

    public void setBufferLength(int value) {
        byteBuffer.limit(value);
    }

    public void setIndex(int value) {
        byteBuffer.position(value);
    }

    @Override
    public ByteOrder getByteOrder() {
        return byteBuffer.order();
    }

    public void orb(org.omg.CORBA.ORB orb) {
        this.orb = (ORB)orb;
    }

    public BufferManagerRead getBufferManager() {
        return bufferManagerRead;
    }

    @CdrRead
    private void skipToOffset(int offset) {                                                        // todo test this
        // Number of bytes to skip
        int len = offset - get_offset();

        int n = 0;

        while (n < len) {
            int wanted;
            int bytes;

            if (!byteBuffer.hasRemaining()) grow(1, 1);
            int avail = byteBuffer.remaining();

            wanted = len - n;
            bytes = (wanted < avail) ? wanted : avail;
            byteBuffer.position(byteBuffer.position() + bytes);
            n += bytes;
        }
    }


    // Mark and reset -------------------------------------------------

    protected MarkAndResetHandler markAndResetHandler = null;

    protected class StreamMemento {
        // These are the fields that may change after marking
        // the stream position, so we need to save them.
        private int blockLength_;
        private int end_flag_;
        private int chunkedValueNestingLevel_;
        private int valueIndirection_;
        private int stringIndirection_;
        private boolean isChunked_;
        private ValueHandler valueHandler_;
        private ByteBuffer byteBuffer_;
        private boolean specialNoOptionalDataState_;

        public StreamMemento() {
            blockLength_ = blockLength;
            end_flag_ = end_flag;
            chunkedValueNestingLevel_ = chunkedValueNestingLevel;
            valueIndirection_ = valueIndirection;
            stringIndirection_ = stringIndirection;
            isChunked_ = isChunked;
            valueHandler_ = valueHandler;
            specialNoOptionalDataState_ = specialNoOptionalDataState;
            byteBuffer_ = byteBuffer.duplicate();
        }
    }

    public java.lang.Object createStreamMemento() {
        return new StreamMemento();
    }

    public void restoreInternalState(java.lang.Object streamMemento) {

        StreamMemento mem = (StreamMemento)streamMemento;

        blockLength = mem.blockLength_;
        end_flag = mem.end_flag_;
        chunkedValueNestingLevel = mem.chunkedValueNestingLevel_;
        valueIndirection = mem.valueIndirection_;
        stringIndirection = mem.stringIndirection_;
        isChunked = mem.isChunked_;
        valueHandler = mem.valueHandler_;
        specialNoOptionalDataState = mem.specialNoOptionalDataState_;
        byteBuffer = mem.byteBuffer_;
    }

    public int getPosition() {
        return get_offset();
    }

    public void mark(int readlimit) {
        markAndResetHandler.mark(this);
    }

    public void reset() {
        markAndResetHandler.reset();
    }

    // ---------------------------------- end Mark and Reset

    // Provides a hook so subclasses of CDRInputStream can provide
    // a CodeBase.  This ultimately allows us to grab a Connection
    // instance in IIOPInputStream, the only subclass where this
    // is actually used.
    CodeBase getCodeBase() {
        return parent.getCodeBase();
    }

    /**
     * Attempts to find the class described by the given
     * repository ID string and expected type.  The first
     * attempt is to find the class locally, falling back
     * on the URL that came with the value.  The second
     * attempt is to use a URL from the remote CodeBase.
     */
    @CdrRead
    private Class<?> getClassFromString(String repositoryIDString,
                                     String codebaseURL,
                                     Class<?> expectedType)
    {
        RepositoryIdInterface repositoryID = repIdStrs.getFromString(repositoryIDString);

        ClassCodeBaseHandler ccbh = orb.classCodeBaseHandler() ;
        if (ccbh != null) {
            String className = repositoryID.getClassName() ;
            Class<?> result = ccbh.loadClass( codebaseURL, className ) ;

            if (result != null) {
                return result ;
            }
        }

        try {
            try {
                // First try to load the class locally, then use
                // the provided URL (if it isn't null)
                return repositoryID.getClassFromType(expectedType,
                                                     codebaseURL);
            } catch (ClassNotFoundException cnfeOuter) {
                
                try {
                  
                    if (getCodeBase() == null) {
                        return null; // class cannot be loaded remotely. 
                    }
                    
                    // Get a URL from the remote CodeBase and retry
                    codebaseURL = getCodeBase().implementation(repositoryIDString);
                    
                    // Don't bother trying to find it locally again if
                    // we got a null URL
                    if (codebaseURL == null) {
                        return null;
                    }
                    
                    return repositoryID.getClassFromType(expectedType,
                                                         codebaseURL);
                } catch (ClassNotFoundException cnfeInner) {
                    // Failed to load the class
                    return null;
                }
            }
        } catch (MalformedURLException mue) {
            // Always report a bad URL
            throw wrapper.malformedUrl( mue, repositoryIDString, codebaseURL ) ;
        }
    }


    // Utility method used to get chars from bytes
    char[] getConvertedChars(int numBytes,
                             CodeSetConversion.BTCConverter converter) {


        if (byteBuffer.remaining() >= numBytes) {
            // If the entire string is in this buffer,
            // just convert directly from the buffer rather than
            // allocating and copying.
            int pos = byteBuffer.position();
            char[] result = converter.getChars(byteBuffer.slice(), 0, numBytes);
            byteBuffer.position(pos + numBytes);
            return result;
        } else {
            // Stretches across buffers.  Unless we provide an
            // incremental conversion interface, allocate and
            // copy the bytes.            
            byte[] bytes = new byte[numBytes];

            // REVISIT - We should avoid getting the bytes into an array if 
            //  possible.  Extend the logic used above for the if() case , send
            //  the bytebuffer, as it is, for reading the strings. If any 
            //  string is spread across multiple messages, the logic is going 
            //  to be complex- which is, to decode strings in parts and then
            //  concatenate them in order. 
            read_octet_array(bytes, 0, bytes.length);

            return converter.getChars(bytes, 0, numBytes);
        }
    }

    protected CodeSetConversion.BTCConverter getCharConverter() {
        if (charConverter == null) {
            charConverter = parent.createCharBTCConverter();
        }
        
        return charConverter;
    }

    protected CodeSetConversion.BTCConverter getWCharConverter() {
        if (wcharConverter == null) {
            wcharConverter = parent.createWCharBTCConverter();
        }
    
        return wcharConverter;
    }

    /**
     * Aligns the current position on the given octet boundary
     * if there are enough bytes available to do so.  Otherwise,
     * it just returns.  This is used for some (but not all)
     * GIOP 1.2 message headers.
     */

    void alignOnBoundary(int octetBoundary) {
        int needed = computeAlignment(byteBuffer.position(), octetBoundary);

        if (byteBuffer.position() + needed <= byteBuffer.limit())
        {
            byteBuffer.position(byteBuffer.position() + needed);
        }
    }

    public void resetCodeSetConverters() {
        charConverter = null;
        wcharConverter = null;
    }

    @InfoMethod
    private void valueTag( int value ) { }

    @CdrRead
    public void start_value() {
        // Read value tag
        int vType = readValueTag();
        valueTag( vType ) ;

        if (vType == 0) {
            // Stream needs to go into a state where it
            // throws standard exception until end_value
            // is called.  This means the sender didn't
            // send any custom data.  If the reader here
            // tries to read more, we need to throw an
            // exception before reading beyond where
            // we're supposed to
            specialNoOptionalDataState = true;

            return;
        }

        if (vType == 0xffffffff) {
            // One should never indirect to a custom wrapper
            throw wrapper.customWrapperIndirection( );
        }

        if (repIdUtil.isCodeBasePresent(vType)) {
            throw wrapper.customWrapperWithCodebase();
        }
                        
        if (repIdUtil.getTypeInfo(vType) 
            != RepositoryIdUtility.SINGLE_REP_TYPE_INFO) {
            throw wrapper.customWrapperNotSingleRepid( );
        }


        // REVISIT - Could verify repository ID even though
        // it isn't used elsewhere
        read_repositoryId();

        // Note: isChunked should be true here.  Should have
        // been set to true in the containing value's read_value
        // method.
        
        start_block();
        end_flag--;
        chunkedValueNestingLevel--;
    }

    @CdrRead
    public void end_value() {

        if (specialNoOptionalDataState) {
            specialNoOptionalDataState = false;
            return;
        }

        handleEndOfValue();
        readEndTag();

        // Note that isChunked should still be true here.
        // If the containing valuetype is the highest 
        // chunked value, it will get set to false
        // at the end of read_value.

        // allow for possible continuation chunk
        start_block();
    }

    @Override
    @CdrRead
    public void close() throws IOException {

        // tell BufferManagerRead to release any ByteBuffers
        getBufferManager().close(byteBuffer);

        if (byteBuffer != null) {

            // release this stream's ByteBuffer to the pool
            ByteBufferPool byteBufferPool = orb.getByteBufferPool();
            byteBufferPool.releaseByteBuffer(byteBuffer);
            byteBuffer = null;
        }
    }
}
