/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2019 Payara Services Ltd.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.misc;

import java.security.AccessController;
import java.security.PermissionCollection;
import java.security.Policy;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.LinkedList;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;

import java.io.PrintStream ;
import java.io.IOException ;

import java.nio.ByteBuffer ;

import java.net.SocketAddress ;

import java.nio.channels.SocketChannel ;

import javax.rmi.CORBA.ValueHandler;
import javax.rmi.CORBA.ValueHandlerMultiFormat;

import org.omg.CORBA.StructMember ;
import org.omg.CORBA.TypeCode ;
import org.omg.CORBA.Any ;
import org.omg.CORBA.TCKind ;
import org.omg.CORBA.SystemException ;
import org.omg.CORBA.CompletionStatus ;
import org.omg.CORBA.TypeCodePackage.BadKind ;
import org.omg.CORBA.portable.OutputStream ;
import org.omg.CORBA.portable.InputStream ;

import com.sun.corba.ee.spi.ior.IOR ;
import com.sun.corba.ee.spi.presentation.rmi.StubAdapter ;
import com.sun.corba.ee.spi.orb.ORB ;
import com.sun.corba.ee.spi.orb.ORBVersionFactory ;
import com.sun.corba.ee.spi.protocol.ClientDelegate ;
import com.sun.corba.ee.spi.protocol.MessageMediator;
import com.sun.corba.ee.spi.transport.ContactInfoList ;
import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;
import com.sun.corba.ee.spi.ior.iiop.IIOPProfile;
import com.sun.corba.ee.spi.ior.iiop.IIOPProfileTemplate;
import com.sun.corba.ee.spi.misc.ORBClassLoader;
import com.sun.corba.ee.spi.misc.ORBConstants ;

import com.sun.corba.ee.impl.corba.CORBAObjectImpl ;
import com.sun.corba.ee.spi.logging.ORBUtilSystemException ;
import com.sun.corba.ee.spi.logging.OMGSystemException ;
import com.sun.corba.ee.impl.ior.iiop.JavaSerializationComponent;
import com.sun.corba.ee.impl.javax.rmi.CORBA.Util;

/**
 *  Handy class full of static functions that don't belong in util.Utility for pure ORB reasons.
 */
public final class ORBUtility {
    /** Utility method for working around leak in SocketChannel.open( SocketAddress )
     * method.
     * @param sa address to connect to
     * @return The opened channel
     * @throws java.io.IOException If an I/O error occurs
     * @see SocketChannel#connect(java.net.SocketAddress)
     */
    public static SocketChannel openSocketChannel( SocketAddress sa ) 
        throws IOException {

        SocketChannel sc = SocketChannel.open() ;

        try {
            sc.connect( sa ) ;
            return sc ;
        } catch (RuntimeException | IOException exc ) {
            try {
                sc.close() ;
            } catch (IOException ioe) {
                // Ignore this: close exceptions are useless.
            }

            throw exc ;
        }
        
    }

    private static final ThreadLocal<LinkedList<Byte>> encVersionThreadLocal =
        new ThreadLocal<LinkedList<Byte>>() {
            @Override
            protected LinkedList<Byte> initialValue() {
                return new LinkedList<>();
            }
        };

    public static void pushEncVersionToThreadLocalState(byte value) {
        LinkedList<Byte> stack = encVersionThreadLocal.get();
        stack.addLast(value);
    }

    public static void popEncVersionFromThreadLocalState() {
        LinkedList<Byte> stack = encVersionThreadLocal.get();
        stack.removeLast();
    }

    public static byte getEncodingVersion() {
        byte encodingVersion = ORBConstants.CDR_ENC_VERSION; // default
        LinkedList<Byte> stack = encVersionThreadLocal.get();
        if (stack.size() > 0) {
            Byte last = stack.getLast();
            // LinkedList allows a null object to be added to the list
            if (last != null) {
                encodingVersion = last.byteValue();
            }  // if null, return default CDR_ENC_VERSION
        } // if nothing on list, use default encoding
        return encodingVersion;
    }

    public static byte[] getByteBufferArray(ByteBuffer byteBuffer) {

        if (byteBuffer.hasArray()) {
            byte[] buf = new byte[byteBuffer.limit()];
            System.arraycopy(byteBuffer.array(), byteBuffer.arrayOffset(),
                             buf, 0, buf.length);
            // NOTE: Cannot simply do return byteBuffer.array() since byteBuffer
            // could be a view buffer / sliced ByteBuffer. View buffers /
            // sliced ByteBuffer will return the entired backed array.
            // Not a byte array beginning at view buffer position 0.
            return buf;
        }

        byte[] buf = new byte[byteBuffer.limit()];
        int pos = byteBuffer.position();
        byteBuffer.position(0);
        byteBuffer.get(buf);
        byteBuffer.position(pos);

        return buf;
    }

    /**
     * @param orb The ORB
     * @param ior Interoperable object reference
     * @param gv The maximum GIOP version supported
     * @return the Java serialization encoding version.
     */
    public static byte chooseEncodingVersion(ORB orb, IOR ior, 
                                             GIOPVersion gv) {

        // Is Java serialization enabled?
        // Check the JavaSerializationComponent (tagged component)
        // in the IIOPProfile. If present, the peer ORB's GIOP is capable
        // of using Java serialization instead of CDR serialization.
        // In such a case, use Java serialization, iff the java serialization
        // versions match.

        if (orb.getORBData().isJavaSerializationEnabled() &&
               !(gv.lessThan(GIOPVersion.V1_2))) {
            IIOPProfile prof = ior.getProfile();
            IIOPProfileTemplate profTemp = 
                (IIOPProfileTemplate) prof.getTaggedProfileTemplate();
            java.util.Iterator iter = profTemp.iteratorById(
                                  ORBConstants.TAG_JAVA_SERIALIZATION_ID);
            if (iter.hasNext()) {
                JavaSerializationComponent jc = 
                    (JavaSerializationComponent) iter.next();
                byte jcVersion = jc.javaSerializationVersion();
                if (jcVersion >= ORBConstants.JAVA_ENC_VERSION) {
                    return ORBConstants.JAVA_ENC_VERSION;
                } else if (jcVersion > ORBConstants.CDR_ENC_VERSION) {
                    return jcVersion;
                } else {
                    // ERROR: encodingVersion is <= 0 (CDR_ENC_VERSION).
                    wrapper.invalidJavaSerializationVersion(jc);
                }
            }
        }
        return ORBConstants.CDR_ENC_VERSION; // default
    }

    private ORBUtility() {}

    private static ORBUtilSystemException wrapper = 
        ORBUtilSystemException.self ;
    private static OMGSystemException omgWrapper =
        OMGSystemException.self ;

    private static StructMember[] members = null;

    private synchronized static StructMember[] systemExceptionMembers (ORB orb) {
        if (members == null) {
            members = new StructMember[3];
            members[0] = new StructMember("id", orb.create_string_tc(0), null);
            members[1] = new StructMember("minor", orb.get_primitive_tc(TCKind.tk_long), null);
            members[2] = new StructMember("completed", orb.get_primitive_tc(TCKind.tk_long), null);
        }
        return members;
    }

    private static TypeCode getSystemExceptionTypeCode(ORB orb, String repID, String name) {
        synchronized (TypeCode.class) {
            return orb.create_exception_tc(repID, name, systemExceptionMembers(orb));
        }
    }

    private static boolean isSystemExceptionTypeCode(TypeCode type, ORB orb) {
        StructMember[] systemExceptionMembers = systemExceptionMembers(orb);
        try {
            return (type.kind().value() == TCKind._tk_except &&
                    type.member_count() == 3 &&
                    type.member_type(0).equal(systemExceptionMembers[0].type) &&
                    type.member_type(1).equal(systemExceptionMembers[1].type) &&
                    type.member_type(2).equal(systemExceptionMembers[2].type));
        } catch (BadKind | org.omg.CORBA.TypeCodePackage.Bounds ex) {
            return false;
        }
    }

    /**
     * Static method for writing a CORBA standard exception to an Any.
     * @param ex Exception to write
     * @param any The Any to write the SystemException into.
     */
    public static void insertSystemException(SystemException ex, Any any) {
        OutputStream out = any.create_output_stream();
        ORB orb = (ORB)(out.orb());
        String name = ex.getClass().getName();
        String repID = ORBUtility.repositoryIdOf(name);
        out.write_string(repID);
        out.write_long(ex.minor);
        out.write_long(ex.completed.value());
        any.read_value(out.create_input_stream(), 
            getSystemExceptionTypeCode(orb, repID, name));
    }

    public static SystemException extractSystemException(Any any) {
        InputStream in = any.create_input_stream();
        ORB orb = (ORB)(in.orb());
        if ( ! isSystemExceptionTypeCode(any.type(), orb)) {
            throw wrapper.unknownDsiSysex();
        }
        return ORBUtility.readSystemException(in);
    }

    private static ValueHandler vhandler = 
    		Util.getInstance().createValueHandler();
    
    /**
     * Gets the ValueHandler from Util.createValueHandler.
     * @return gets the ValueHandler
     */
    public static ValueHandler createValueHandler() {
        return vhandler;
    }

    /**
     * Creates the correct ValueHandler.  The parameter
     * is ignored
     * @param orb ignored
     * @return The correct ValueHandler
     * @see #createValueHandler() 
     */
    public static ValueHandler createValueHandler(ORB orb) {
        return vhandler;
    }

    /**
     * Returns true if it was accurately determined that the remote ORB is
     * a foreign (non-JavaSoft) ORB.  Note:  If passed the ORBSingleton, this
     * will return false.
     * @param orb ORB to test
     * @return If the ORB is foreign
     */
    public static boolean isForeignORB(ORB orb)
    {
        if (orb == null) {
            return false;
        }

        try {
            return orb.getORBVersion().equals(ORBVersionFactory.getFOREIGN());
        } catch (SecurityException se) {
            return false;
        }
    }

    /** Unmarshal a byte array to an integer.
        Assume the bytes are in BIGENDIAN order.
        i.e. array[offset] is the most-significant-byte
        and  array[offset+3] is the least-significant-byte.
        @param array The array of bytes.
        @param offset The offset from which to start unmarshalling.
        @return Unmarshalled integer
    */
    public static int bytesToInt(byte[] array, int offset)
    {
        int b1, b2, b3, b4;

        b1 = (array[offset++] << 24) & 0xFF000000;
        b2 = (array[offset++] << 16) & 0x00FF0000;
        b3 = (array[offset++] << 8)  & 0x0000FF00;
        b4 = (array[offset++] << 0)  & 0x000000FF;

        return (b1 | b2 | b3 | b4);
    }

    /** Marshal an integer to a byte array.
        The bytes are in BIGENDIAN order.
        i.e. array[offset] is the most-significant-byte
        and  array[offset+3] is the least-significant-byte.
        @param value Integer to marshal
        @param array The array of bytes.
        @param offset The offset from which to start marshalling.
    */
    public static void intToBytes(int value, byte[] array, int offset)
    {
        array[offset++] = (byte)((value >>> 24) & 0xFF);
        array[offset++] = (byte)((value >>> 16) & 0xFF);
        array[offset++] = (byte)((value >>> 8) & 0xFF);
        array[offset++] = (byte)((value >>> 0) & 0xFF);
    }

    /** Converts an Ascii Character into Hexadecimal digit
     * @param x ASCII character to convert
     * @return Hexadecimal digit
     */
    public static int hexOf( char x )
    {
        int val;

        val = x - '0';
        if (val >=0 && val <= 9) {
            return val;
        }

        val = (x - 'a') + 10;
        if (val >= 10 && val <= 15) {
            return val;
        }

        val = (x - 'A') + 10;
        if (val >= 10 && val <= 15) {
            return val;
        }

        throw wrapper.badHexDigit() ;
    }

    // method moved from util.Utility

    /**
     * Static method for writing a CORBA standard exception to a stream.
     * @param ex Exception to write to stream
     * @param strm The OutputStream to use for marshaling.
     */
    public static void writeSystemException(SystemException ex, OutputStream strm)
    {
        String s;

        s = repositoryIdOf(ex.getClass().getName());
        strm.write_string(s);
        strm.write_long(ex.minor);
        strm.write_long(ex.completed.value());
    }

    /**
     * Static method for reading a CORBA standard exception from a stream.
     * @param strm The InputStream to use for unmarshalling.
     * @return Exception in stream
     */
    public static SystemException readSystemException(InputStream strm)
    {
        try {
            String name = classNameOf(strm.read_string());
            SystemException ex 
                = (SystemException)ORBClassLoader.loadClass(name).newInstance();
            ex.minor = strm.read_long();
            ex.completed = CompletionStatus.from_int(strm.read_long());
            return ex;
        } catch ( Exception ex ) {
            throw wrapper.unknownSysex( ex );
        }
    }

    /**
     * Get the class name corresponding to a particular repository Id.
     * This is used by the system to unmarshal (instantiate) the
     * appropriate exception class for an marshaled as the value of
     * its repository Id.
     * @param repositoryId The repository Id for which we want a class name.
     * @return Corresponding class name
     */
    public static String classNameOf(String repositoryId)
    {
        String className=null;

        className = (String) exceptionClassNames.get(repositoryId);
        if (className == null) {
            className = "org.omg.CORBA.UNKNOWN";
        }

        return className;
    }

    /**
     * Return true if this repositoryId is a SystemException.
     * @param repositoryId The repository Id to check.
     * @return if ID is a SystemException
     */
    public static boolean isSystemException(String repositoryId)
    {
        String className=null;

        className = (String) exceptionClassNames.get(repositoryId);
        return className != null ;
    }
    
    /**
     * Get the repository id corresponding to a particular class.
     * This is used by the system to write the
     * appropriate repository id for a system exception.
     * @param name The class name of the system exception.
     * @return Repository ID
     */
    public static String repositoryIdOf(String name)
    {
        String id;

        id = (String) exceptionRepositoryIds.get(name);
        if (id == null) {
            id = "IDL:omg.org/CORBA/UNKNOWN:1.0";
        }

        return id;
    }

    private static final Hashtable<String, String> exceptionClassNames = new Hashtable<>();
    private static final Hashtable exceptionRepositoryIds = new Hashtable();

    static {

        //
        // construct repositoryId -> className hashtable
        //
        exceptionClassNames.put("IDL:omg.org/CORBA/BAD_CONTEXT:1.0",
                                "org.omg.CORBA.BAD_CONTEXT");
        exceptionClassNames.put("IDL:omg.org/CORBA/BAD_INV_ORDER:1.0",
                                "org.omg.CORBA.BAD_INV_ORDER");
        exceptionClassNames.put("IDL:omg.org/CORBA/BAD_OPERATION:1.0",
                                "org.omg.CORBA.BAD_OPERATION");
        exceptionClassNames.put("IDL:omg.org/CORBA/BAD_PARAM:1.0",
                                "org.omg.CORBA.BAD_PARAM");
        exceptionClassNames.put("IDL:omg.org/CORBA/BAD_TYPECODE:1.0",
                                "org.omg.CORBA.BAD_TYPECODE");
        exceptionClassNames.put("IDL:omg.org/CORBA/COMM_FAILURE:1.0",
                                "org.omg.CORBA.COMM_FAILURE");
        exceptionClassNames.put("IDL:omg.org/CORBA/DATA_CONVERSION:1.0",
                                "org.omg.CORBA.DATA_CONVERSION");
        exceptionClassNames.put("IDL:omg.org/CORBA/IMP_LIMIT:1.0",
                                "org.omg.CORBA.IMP_LIMIT");
        exceptionClassNames.put("IDL:omg.org/CORBA/INTF_REPOS:1.0",
                                "org.omg.CORBA.INTF_REPOS");
        exceptionClassNames.put("IDL:omg.org/CORBA/INTERNAL:1.0",
                                "org.omg.CORBA.INTERNAL");
        exceptionClassNames.put("IDL:omg.org/CORBA/INV_FLAG:1.0",
                                "org.omg.CORBA.INV_FLAG");
        exceptionClassNames.put("IDL:omg.org/CORBA/INV_IDENT:1.0",
                                "org.omg.CORBA.INV_IDENT");
        exceptionClassNames.put("IDL:omg.org/CORBA/INV_OBJREF:1.0",
                                "org.omg.CORBA.INV_OBJREF");
        exceptionClassNames.put("IDL:omg.org/CORBA/MARSHAL:1.0",
                                "org.omg.CORBA.MARSHAL");
        exceptionClassNames.put("IDL:omg.org/CORBA/NO_MEMORY:1.0",
                                "org.omg.CORBA.NO_MEMORY");
        exceptionClassNames.put("IDL:omg.org/CORBA/FREE_MEM:1.0",
                                "org.omg.CORBA.FREE_MEM");
        exceptionClassNames.put("IDL:omg.org/CORBA/NO_IMPLEMENT:1.0",
                                "org.omg.CORBA.NO_IMPLEMENT");
        exceptionClassNames.put("IDL:omg.org/CORBA/NO_PERMISSION:1.0",
                                "org.omg.CORBA.NO_PERMISSION");
        exceptionClassNames.put("IDL:omg.org/CORBA/NO_RESOURCES:1.0",
                                "org.omg.CORBA.NO_RESOURCES");
        exceptionClassNames.put("IDL:omg.org/CORBA/NO_RESPONSE:1.0",
                                "org.omg.CORBA.NO_RESPONSE");
        exceptionClassNames.put("IDL:omg.org/CORBA/OBJ_ADAPTER:1.0",
                                "org.omg.CORBA.OBJ_ADAPTER");
        exceptionClassNames.put("IDL:omg.org/CORBA/INITIALIZE:1.0",
                                "org.omg.CORBA.INITIALIZE");
        exceptionClassNames.put("IDL:omg.org/CORBA/PERSIST_STORE:1.0",
                                "org.omg.CORBA.PERSIST_STORE");
        exceptionClassNames.put("IDL:omg.org/CORBA/TRANSIENT:1.0",
                                "org.omg.CORBA.TRANSIENT");
        exceptionClassNames.put("IDL:omg.org/CORBA/UNKNOWN:1.0",
                                "org.omg.CORBA.UNKNOWN");
        exceptionClassNames.put("IDL:omg.org/CORBA/OBJECT_NOT_EXIST:1.0",
                                "org.omg.CORBA.OBJECT_NOT_EXIST");

        // SystemExceptions from OMG Transactions Service Spec
        exceptionClassNames.put("IDL:omg.org/CORBA/INVALID_TRANSACTION:1.0",
                                "org.omg.CORBA.INVALID_TRANSACTION");
        exceptionClassNames.put("IDL:omg.org/CORBA/TRANSACTION_REQUIRED:1.0",
                                "org.omg.CORBA.TRANSACTION_REQUIRED");
        exceptionClassNames.put("IDL:omg.org/CORBA/TRANSACTION_ROLLEDBACK:1.0",
                                "org.omg.CORBA.TRANSACTION_ROLLEDBACK");

        // from portability RTF 98-07-01.txt
        exceptionClassNames.put("IDL:omg.org/CORBA/INV_POLICY:1.0",
                                "org.omg.CORBA.INV_POLICY");

        // from orbrev/00-09-01 (CORBA 2.4 Draft Specification)
        exceptionClassNames.
            put("IDL:omg.org/CORBA/TRANSACTION_UNAVAILABLE:1.0",
                                "org.omg.CORBA.TRANSACTION_UNAVAILABLE");
        exceptionClassNames.put("IDL:omg.org/CORBA/TRANSACTION_MODE:1.0",
                                "org.omg.CORBA.TRANSACTION_MODE");

        // Exception types introduced between CORBA 2.4 and 3.0
        exceptionClassNames.put("IDL:omg.org/CORBA/CODESET_INCOMPATIBLE:1.0",
                                "org.omg.CORBA.CODESET_INCOMPATIBLE");
        exceptionClassNames.put("IDL:omg.org/CORBA/REBIND:1.0",
                                "org.omg.CORBA.REBIND");
        exceptionClassNames.put("IDL:omg.org/CORBA/TIMEOUT:1.0",
                                "org.omg.CORBA.TIMEOUT");
        exceptionClassNames.put("IDL:omg.org/CORBA/BAD_QOS:1.0",
                                "org.omg.CORBA.BAD_QOS");        

        // Exception types introduced in CORBA 3.0
        exceptionClassNames.put("IDL:omg.org/CORBA/INVALID_ACTIVITY:1.0",
                                "org.omg.CORBA.INVALID_ACTIVITY");
        exceptionClassNames.put("IDL:omg.org/CORBA/ACTIVITY_COMPLETED:1.0",
                                "org.omg.CORBA.ACTIVITY_COMPLETED");
        exceptionClassNames.put("IDL:omg.org/CORBA/ACTIVITY_REQUIRED:1.0",
                                "org.omg.CORBA.ACTIVITY_REQUIRED");        

        //
        // construct className -> repositoryId hashtable
        //
        Enumeration<String> keys = exceptionClassNames.keys();
        String rId;
        String cName;

        try{
            while (keys.hasMoreElements()) {
                rId = keys.nextElement();
                cName = exceptionClassNames.get(rId);
                exceptionRepositoryIds.put(cName, rId);
            }
        } catch (NoSuchElementException e) { }
    }

    /** Parse a version string such as "1.1.6" or "jdk1.2fcs" into
        a version array of integers {1, 1, 6} or {1, 2}.
        A string of "n." or "n..m" is equivalent to "n.0" or "n.0.m" respectively.
     * @param version Java version
     * @return Array of version parts
    */
    public static int[] parseVersion(String version) {
        if (version == null) {
            return new int[0];
        }
        char[] s = version.toCharArray();
        //find the maximum span of the string "n.n.n..." where n is an integer
        int start = 0;
        for (; start < s.length  && (s[start] < '0' || s[start] > '9'); ++start) {
            if (start == s.length) {
                return new int[0];
            }
        }
        int end = start + 1;
        int size = 1;
        for (; end < s.length; ++end) {
            if (s[end] == '.') {
                ++size;
            } else if (s[end] < '0' || s[end] > '9') {
                break;
            }
        }
        int[] val = new int[size];
        for (int i = 0; i < size; ++i) {
            int dot = version.indexOf('.', start);
            if (dot == -1 || dot > end) {
                dot = end;
            }
            if (start >= dot) {
                val[i] = 0;
            }   //convert equivalent to "n.0" or "n.0.m"
            else {
                val[i] =
                    Integer.parseInt(version.substring(start, dot));
            }
            start = dot + 1;
        }
        return val;
    }

    /** Compare two version arrays.
        Return 1, 0 or -1 if v1 is greater than, equal to, or less than v2.
     * @param v1 first version
     * @param v2 second version
     * @return 1, 0 or -1 if v1 is greater than, equal to, or less than v2.
    */
    public static int compareVersion(int[] v1, int[] v2) {
        if (v1 == null) {
            v1 = new int[0];
        }
        if (v2 == null) {
            v2 = new int[0];
        }
        for (int i = 0; i < v1.length; ++i) {
            if (i >= v2.length || v1[i] > v2[i]) {
                return 1;
            }
            if (v1[i] < v2[i]) {
                return -1;
            }
        }
        return v1.length == v2.length ? 0 : -1;
    }

    /** Compare two version strings.
        Return 1, 0 or -1 if v1 is greater than, equal to, or less than v2.
     * @param v1 first version string
     * @param v2 second version string
     * @return 1, 0 or -1 if v1 is greater than, equal to, or less than v2.
     * @see #compareVersion(int[], int[]) 
    */
    public static synchronized int compareVersion(String v1, String v2) {
        return compareVersion(parseVersion(v1), parseVersion(v2));
    }

    private static String compressClassName( String name )
    {
        // Note that this must end in . in order to be renamed correctly.
        String prefix = "com.sun.corba.ee." ;
        if (name.startsWith( prefix ) ) {
            return "(ORB)." + name.substring( prefix.length() ) ;
        } else {
            return name;
        }
    }

    // Return a compressed representation of the thread name.  This is particularly
    // useful on the server side, where there are many SelectReaderThreads, and
    // we need a short unambiguous name for such threads.
    public static String getThreadName( Thread thr ) 
    {
        if (thr == null) {
            return "null";
        }

        // This depends on the formatting in SelectReaderThread and CorbaConnectionImpl.
        // Pattern for SelectReaderThreads:
        // SelectReaderThread CorbaConnectionImpl[ <host> <post> <state>]
        // Any other pattern in the Thread's name is just returned.
        String name = thr.getName() ;
        StringTokenizer st = new StringTokenizer( name ) ;
        int numTokens = st.countTokens() ;
        if (numTokens != 5) {
            return name;
        }

        String[] tokens = new String[numTokens] ;
        for (int ctr=0; ctr<numTokens; ctr++ ) {
            tokens[ctr] = st.nextToken();
        }

        if( !tokens[0].equals("SelectReaderThread")) {
            return name;
        }

        return "SelectReaderThread[" + tokens[2] + ":" + tokens[3] + "]" ;
    }

    private static String formatStackTraceElement( StackTraceElement ste ) 
    {
        return compressClassName( ste.getClassName() ) + "." + ste.getMethodName() +
            (ste.isNativeMethod() ? "(Native Method)" :
             (ste.getFileName() != null && ste.getLineNumber() >= 0 ?
              "(" + ste.getFileName() + ":" + ste.getLineNumber() + ")" :
              (ste.getFileName() != null ?  "("+ste.getFileName()+")" : "(Unknown Source)")));
    }

    private static void printStackTrace( StackTraceElement[] trace ) 
    {
        System.out.println( "    Stack Trace:" ) ;
        // print the stack trace, ommitting the zeroth element, which is
        // always this method.
        for ( int ctr = 1; ctr < trace.length; ctr++ ) {
            System.out.print( "        >" ) ;
            System.out.println( formatStackTraceElement( trace[ctr] ) ) ;
        }
    }

    //
    // Implements all dprint calls in this package.
    //
    public static synchronized void dprint(java.lang.Object obj, String msg) {
        System.out.println(
            compressClassName( obj.getClass().getName() ) + "("  +
            getThreadName( Thread.currentThread() ) + "): " + msg);
    }

    public static synchronized void dprint(String className, String msg) {
        System.out.println(
            compressClassName( className ) + "("  +
            getThreadName( Thread.currentThread() ) + "): " + msg);
    }

    public synchronized void dprint(String msg) {
        ORBUtility.dprint(this, msg);
    }

    public static synchronized void dprintTrace(Object obj, String msg) {
        ORBUtility.dprint(obj, msg);

        Throwable thr = new Throwable() ;
        printStackTrace( thr.getStackTrace() ) ;
    }

    public static synchronized void dprint(java.lang.Object caller, 
        String msg, Throwable t) 
    { 
        System.out.println(
            compressClassName( caller.getClass().getName() ) + 
            '(' + Thread.currentThread() + "): " + msg);

        if (t != null) {
            printStackTrace(t.getStackTrace());
        }
    }

    public static String[] concatenateStringArrays( String[] arr1, String[] arr2 ) 
    {
        String[] result = new String[ 
            arr1.length + arr2.length ] ;
        System.arraycopy(arr1, 0, result, 0, arr1.length);
        System.arraycopy(arr2, 0, result, arr1.length, arr2.length);

        return result ;
    }

    /**
     * Throws the CORBA equivalent of a java.io.NotSerializableException
     *
     * Duplicated from util/Utility for Pure ORB reasons.  There are two
     * reasons for this:
     *
     * 1) We can't introduce dependencies on the util version from outside
     * of the io/util packages since it will not exist in the pure ORB
     * build running on JDK 1.3.x.
     *
     * 2) We need to pick up the correct minor code from OMGSystemException.
     * @param className Class that is not {@link Serializable}
     */
    public static void throwNotSerializableForCorba(String className) {
        throw omgWrapper.notSerializable( className ) ;
    }

    /**
     * Returns the maximum stream format version supported by our
     * ValueHandler.
     * @return he maximum stream format version
     */
    public static byte getMaxStreamFormatVersion() {
        ValueHandler vh = Util.getInstance().createValueHandler();

        if (!(vh instanceof javax.rmi.CORBA.ValueHandlerMultiFormat)) {
            return ORBConstants.STREAM_FORMAT_VERSION_1;
        } else {
            return ((ValueHandlerMultiFormat) vh).getMaximumStreamFormatVersion();
        }
    }

    public static ClientDelegate makeClientDelegate( IOR ior )
    {
        ORB orb = ior.getORB() ;
        ContactInfoList ccil = orb.getCorbaContactInfoListFactory().create( ior ) ;
        ClientDelegate del = orb.getClientDelegateFactory().create(ccil);
        return del ;
    }

    /** This method is used to create untyped object references.
     * @param ior object to make reference of
     * @return Object pointing to the IOR
    */
    public static org.omg.CORBA.Object makeObjectReference( IOR ior )   
    {
        ClientDelegate del = makeClientDelegate( ior ) ;
        org.omg.CORBA.Object objectImpl = new CORBAObjectImpl() ;
        StubAdapter.setDelegate( objectImpl, del ) ;
        return objectImpl ;
    }

    public static void setDaemon(Thread thread)
    {
        // Catch exceptions since setDaemon can cause a
        // security exception to be thrown under netscape
        // in the Applet mode
        final Thread finalThread = thread;
        try {
            AccessController.doPrivileged(new PrivilegedAction() {
                    public java.lang.Object run() {
                        finalThread.setDaemon(true);
                        return null;
                    }
                });
        } catch (Exception e) {
            // REVISIT: Object to get static method. Ignore it.
            dprint(new Object(), "setDaemon: Exception: " + e);
        }
    }

    public static String operationNameAndRequestId(MessageMediator m)
    {
        return "op/" + m.getOperationName() + " id/" + m.getRequestId();
    }

    public static boolean isPrintable(char c)
    {
        if (Character.isJavaIdentifierStart(c)) {
            // Letters and $ _
            return true;
        }
        if (Character.isDigit(c)) {
            return true;
        }
        switch (Character.getType(c)) {
            case Character.MODIFIER_SYMBOL : return true; // ` ^
            case Character.DASH_PUNCTUATION : return true; // -
            case Character.MATH_SYMBOL : return true; // = ~ + | < >
            case Character.OTHER_PUNCTUATION : return true; // !@#%&*;':",./?
            case Character.START_PUNCTUATION : return true; // ( [ {
            case Character.END_PUNCTUATION : return true; // ) ] }
        }
        return false;
    }

    /** Given some hex data, extract it and put it into a byte buffer.
     * The data must follow the following structure:
     * <OL>
     * <LI>All characters in a line after a "#" are ignored.
     * <LI>All non-whitespace characters before a "#" are treated as hex data.
     * <LI>All whitespace is ignored.
     * <LI>Only whitespace and 0-9a-fA-F may occur before a "#" in any line.
     * <LI>Each data line must contain an even number of non-whitespace
     * characters.
     * </OL>
     * @param data data to extract
     * @return byte array containing the data
     */
    public static byte[] getBuffer( String[] data ) {
        // Estimate size of result
        int numChar = 0 ;
        for (String str : data) {
            numChar += str.length();
        }
        // Maximum result size is 1/2 the number of characters.
        // Usually smaller due to comments and white space.
        int maxSize = numChar/2 ;

        byte[] result = new byte[maxSize] ;
        int index = 0 ;
        int value = 0;
        boolean startByte = true ;

        for (String str : data ) {
            for (int ctr = 0; ctr<str.length(); ctr++) {
                char ch = str.charAt(ctr) ;
                if (!Character.isWhitespace( ch )) {
                    if (ch == '#') {
                        break;
                    } else {
                        value = 16*value + hexOf( ch ) ;
                        if (!startByte) {
                            result[index++] = (byte)value ;     
                            value = 0 ;
                        }
                        startByte = !startByte ;
                    }
                }

                if (!startByte) {
                    throw new RuntimeException();
                }
            }
        }

        return result ;
    }

    public static String dumpBinary( byte[] data ) {
        ByteBuffer bb = ByteBuffer.wrap( data ) ;
        StringBuffer sb = new StringBuffer() ;
        dumpBinary( sb, bb ) ;
        return sb.toString() ;
    }

    private static void dumpBinary( StringBuffer sbuf, ByteBuffer buffer ) {
        int length = buffer.position() ;
        char[] charBuf = new char[16];
        for (int i = 0; i < length; i += 16) {
            int j = 0;
            
            // For every 16 bytes, there is one line of output.  First, 
            // the hex output of the 16 bytes with each byte separated
            // by a space.
            while (j < 16 && (i + j) < length) {
                int k = buffer.get(i + j);
                if (k < 0) {
                    k = 256 + k;
                }
                String hex = Integer.toHexString(k);
                if (hex.length() == 1) {
                    hex = "0" + hex;
                }
                sbuf.append(hex).append(" ");
                j++;
            }
            
            // Add any extra spaces to align the
            // text column in case we didn't end
            // at 16
            while (j < 16) {
                sbuf.append("   ");
                j++;
            }
            
            // Now output the ASCII equivalents.  Non-ASCII
            // characters are shown as periods.
            int x = 0;
            while (x < 16 && x + i < length) {
                if (ORBUtility.isPrintable((char)buffer.get(i + x))) {
                    charBuf[x] = (char) buffer.get(i + x);
                } else {
                    charBuf[x] = '.';
                }
                x++;
            }
            sbuf.append(new String(charBuf, 0, x)).append("\n");
        }
    }


    /** Print the contents of the buffer out to the PrintStream in
    * hex and ASCII.
    * @param msg The message to use as the header for this display
    * @param buffer The ByteBuffer containing the data.  The contents
    * from 0 to buffer.position() are printed out.  Remember to set 
    * position!
    * @param ps The PrintStream to use for the display.
    */
    public static void printBuffer(String msg, 
        ByteBuffer buffer, PrintStream ps ) 
    {
        StringBuffer sbuf = new StringBuffer() ;
        int length = buffer.position() ;
        sbuf.append( "--------------------------------------------------------\n\n" ) ;
        sbuf.append(msg).append( "\n") ;
        sbuf.append( "\n" ) ;
        sbuf.append("Total length (ByteBuffer position) : ").append(length).append( "\n");
        sbuf.append("Byte Buffer capacity               : ").
            append(buffer.capacity()).append( "\n\n");

        try {
            dumpBinary( sbuf, buffer ) ;
        } catch (Throwable t) {
            t.printStackTrace();
        }

        sbuf.append( "--------------------------------------------------------\n" ) ;
        ps.println( sbuf.toString() ) ;
    }

    public static String getClassSecurityInfo(final Class cl)
    {
        // Returns a String which looks similar to:
        // PermissionCollection java.security.Permissions@1053693 ... 
        // (java.io.FilePermission <<ALL FILES>> ....)
        // (java.io.FilePermission /export0/sunwappserv/lib/- ...)
        // ... other permissions ...
        // Domain ProtectionDomain  (file:/export0/sunwappserv/lib-)
        // java.security.Permissions@141fedb (
        // (java.io.FilePermission <<ALL FILES>> ...)
        // (java.io.FilePermission /var/tmp//- ...)

        String result =
            (String)AccessController.doPrivileged(new PrivilegedAction() {
                public java.lang.Object run() {
                    StringBuilder sb = new StringBuilder(500);
                    ProtectionDomain pd = cl.getProtectionDomain();
                    Policy policy = Policy.getPolicy();
                    PermissionCollection pc = policy.getPermissions(pd);
                    sb.append("\nPermissionCollection ");
                    sb.append(pc.toString());
                    // Don't need to add 'Protection Domain' string, it's
                    // in ProtectionDomain.toString() already.
                    sb.append(pd.toString());
                    return sb.toString();
                }
            });
        return result;
    }

    public static String formatStringArray(String[] a)
    {
        if (a == null) {
            return "null";
        }

        StringBuilder result = new StringBuilder() ;
        result.append( "[" ) ;
        for (int i = 0; i < a.length; ++i) {
            result.append( a[i] ) ;
            result.append( " " ) ;
        }
        result.append( "]" ) ;
        return result.toString() ;
    }
}

// End of file.
