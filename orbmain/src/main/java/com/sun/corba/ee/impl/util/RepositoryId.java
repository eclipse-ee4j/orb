/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
 * Copyright (c) 2019-2020 Payara Services Ltd.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.util;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.io.IOException;
import java.lang.reflect.Method;

// Imports for using codebase URL to load class
import java.net.MalformedURLException;

import com.sun.corba.ee.impl.io.ObjectStreamClass;

import com.sun.corba.ee.impl.javax.rmi.CORBA.Util;

import com.sun.corba.ee.impl.misc.ClassInfoCache ;
import org.glassfish.pfl.basic.concurrent.SoftCache;

public class RepositoryId {
        
    // Legal IDL Identifier characters (1 = legal). Note
    // that '.' (2E) is marked as legal even though it is
    // not legal in IDL. This allows us to treat a fully
    // qualified Java name with '.' package separators
    // uniformly, and is safe because that is the only
    // legal use of '.' in a Java name.

    public static final byte[] IDL_IDENTIFIER_CHARS = {
            
        // 0 1 2 3  4 5 6 7  8 9 a b  c d e f
        0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0, // 00-0f
        0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0, // 10-1f
        0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,1,0, // 20-2f
        1,1,1,1, 1,1,1,1, 1,1,0,0, 0,0,0,0, // 30-3f
        0,1,1,1, 1,1,1,1, 1,1,1,1, 1,1,1,1, // 40-4f
        1,1,1,1, 1,1,1,1, 1,1,1,0, 0,0,0,1, // 50-5f
        0,1,1,1, 1,1,1,1, 1,1,1,1, 1,1,1,1, // 60-6f
        1,1,1,1, 1,1,1,1, 1,1,1,0, 0,0,0,0, // 70-7f
        0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0, // 80-8f
        0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0, // 90-9f
        0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0, // a0-af
        0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0, // b0-bf
        1,1,1,1, 1,1,1,1, 1,1,1,1, 1,1,1,1, // c0-cf
        0,1,1,1, 1,1,1,0, 1,1,1,1, 1,0,0,1, // d0-df
        1,1,1,1, 1,1,1,1, 1,1,1,1, 1,1,1,1, // e0-ef
        0,1,1,1, 1,1,1,0, 1,1,1,1, 1,0,0,1, // f0-ff
    };

        
    private static final long serialVersionUID = 123456789L;

    private static final String defaultServerURL = JDKBridge.getLocalCodebase() ;
    private static final boolean useCodebaseOnly = JDKBridge.useCodebaseOnly() ;

    private static final Map<Class<?>, String> classToRepStr = new WeakHashMap<>();
    private static final Map<Class<?>, String> classIDLToRepStr = new WeakHashMap<>();
    private static final Map<Class<?>, String> classSeqToRepStr = new WeakHashMap<>();

    private static final Map<String, byte[]> repStrToByteArray = new IdentityHashMap<>();
    private static final Map<String, Class<?>> repStrToClass = new SoftCache<>();

    private String repId = null;
    private boolean isSupportedFormat = true;
    private String typeString = null;
    private String versionString = null;
    private boolean isSequence = false;
    private boolean isRMIValueType = false;
    private boolean isIDLType = false;
    private String completeClassName = null;
    private String unqualifiedName = null;
    private String definedInId = null;
    private Class clazz = null;
    private String suid = null, actualSuid = null;
    private long suidLong = ObjectStreamClass.kDefaultUID, actualSuidLong = 
        ObjectStreamClass.kDefaultUID;

    // Repository ID fragments
    private static final String kSequenceKeyword = "seq";
    private static final String kValuePrefix = "RMI:";
    private static final String kIDLPrefix = "IDL:";
    private static final String kIDLNamePrefix = "omg.org/";
    private static final String kIDLClassnamePrefix = "org.omg.";
    private static final String kSequencePrefix = "[";
    private static final String kCORBAPrefix = "CORBA/";
    private static final String kArrayPrefix = kValuePrefix 
        + kSequencePrefix + kCORBAPrefix;
    private static final int kValuePrefixLength = kValuePrefix.length();
    private static final int kIDLPrefixLength = kIDLPrefix.length();
    private static final int kSequencePrefixLength = kSequencePrefix.length();
    private static final String kInterfaceHashCode = ":0000000000000000";
    private static final String kInterfaceOnlyHashStr = "0000000000000000";
    private static final String kExternalizableHashStr = "0000000000000001";

    // Value tag utility methods and constants
    public static final int kInitialValueTag= 0x7fffff00;
    public static final int kNoTypeInfo = 0;
    public static final int kSingleRepTypeInfo = 0x02;
    public static final int  kPartialListTypeInfo = 0x06;
    public static final int  kChunkedMask = 0x08;
    public static final int kPreComputed_StandardRMIUnchunked = 
        RepositoryId.computeValueTag(false, 
            RepositoryId.kSingleRepTypeInfo, false);
    public static final int kPreComputed_CodeBaseRMIUnchunked = 
        RepositoryId.computeValueTag(true, 
            RepositoryId.kSingleRepTypeInfo, false);
    public static final int kPreComputed_StandardRMIChunked = 
        RepositoryId.computeValueTag(false, 
            RepositoryId.kSingleRepTypeInfo, true);
    public static final int kPreComputed_CodeBaseRMIChunked = 
        RepositoryId.computeValueTag(true, 
            RepositoryId.kSingleRepTypeInfo, true);

    public static final int kPreComputed_StandardRMIUnchunked_NoRep = 
        RepositoryId.computeValueTag(false, RepositoryId.kNoTypeInfo, false);
    public static final int kPreComputed_CodeBaseRMIUnchunked_NoRep = 
        RepositoryId.computeValueTag(true, RepositoryId.kNoTypeInfo, false);
    public static final int kPreComputed_StandardRMIChunked_NoRep = 
        RepositoryId.computeValueTag(false, RepositoryId.kNoTypeInfo, true);
    public static final int kPreComputed_CodeBaseRMIChunked_NoRep = 
        RepositoryId.computeValueTag(true, RepositoryId.kNoTypeInfo, true);

    // Public, well known repository IDs
        
    // _REVISIT_ : A table structure with a good search routine for all of this 
    // would be more efficient and easier to maintain...

    // String
    public static final String kWStringValueVersion = "1.0"; 
    public static final String kWStringValueHash = ":"+kWStringValueVersion; 
    public static final String kWStringStubValue = "WStringValue";
    public static final String kWStringTypeStr = "omg.org/CORBA/"
        + kWStringStubValue;
    public static final String kWStringValueRepID = kIDLPrefix 
        + kWStringTypeStr + kWStringValueHash;

    // Any
    public static final String kAnyRepID = kIDLPrefix + "omg.org/CORBA/Any";

    // Class
    public static final String kClassDescValueHash = ":" 
        + Long.toHexString(
           ObjectStreamClass.getActualSerialVersionUID(
               javax.rmi.CORBA.ClassDesc.class)).toUpperCase() 
        + ":" 
        + Long.toHexString(
           ObjectStreamClass.getSerialVersionUID(
               javax.rmi.CORBA.ClassDesc.class)).toUpperCase();
    public static final String kClassDescStubValue = "ClassDesc";
    public static final String kClassDescTypeStr = 
        "javax.rmi.CORBA."+kClassDescStubValue;
    public static final String kClassDescValueRepID = 
        kValuePrefix + kClassDescTypeStr + kClassDescValueHash;

    // Object
    public static final String kObjectValueHash = ":1.0";
    public static final String kObjectStubValue = "Object";

    // Sequence
    public static final String kSequenceValueHash = ":1.0";
    public static final String kPrimitiveSequenceValueHash = ":0000000000000000";

    // Serializable
    public static final String kSerializableValueHash = ":1.0";
    public static final String kSerializableStubValue = "Serializable";

    // Externalizable
    public static final String kExternalizableValueHash = ":1.0";
    public static final String kExternalizableStubValue = "Externalizable";

    // Remote (The empty string is used for java.rmi.Remote)
    public static final String kRemoteValueHash = "";
    public static final String kRemoteStubValue = "";
    public static final String kRemoteTypeStr = "";
    public static final String kRemoteValueRepID = "";

    public static final Hashtable<String, StringBuffer> kSpecialArrayTypeStrings = new Hashtable<>();

    static {
        kSpecialArrayTypeStrings.put("CORBA.WStringValue", new StringBuffer(java.lang.String.class.getName()));
        kSpecialArrayTypeStrings.put("javax.rmi.CORBA.ClassDesc", new StringBuffer(java.lang.Class.class.getName()));
        kSpecialArrayTypeStrings.put("CORBA.Object", new StringBuffer(java.rmi.Remote.class.getName()));
    }

    public static final Hashtable<Class<?>, String> kSpecialCasesRepIDs = new Hashtable<>();

    static {
        kSpecialCasesRepIDs.put(java.lang.String.class, kWStringValueRepID);
        kSpecialCasesRepIDs.put(java.lang.Class.class, kClassDescValueRepID);
        kSpecialCasesRepIDs.put(java.rmi.Remote.class, kRemoteValueRepID);
    }

    public static final Hashtable<Class<?>, String> kSpecialCasesStubValues = new Hashtable<>();

    static {
        kSpecialCasesStubValues.put(java.lang.String.class, kWStringStubValue);
        kSpecialCasesStubValues.put(java.lang.Class.class, kClassDescStubValue);
        kSpecialCasesStubValues.put(java.lang.Object.class, kObjectStubValue);
        kSpecialCasesStubValues.put(java.io.Serializable.class, kSerializableStubValue);
        kSpecialCasesStubValues.put(java.io.Externalizable.class, kExternalizableStubValue);
        kSpecialCasesStubValues.put(java.rmi.Remote.class, kRemoteStubValue);
    }

    public static final Hashtable<Class<?>, String> kSpecialCasesVersions = new Hashtable<>();

    static {
        kSpecialCasesVersions.put(java.lang.String.class, kWStringValueHash);
        kSpecialCasesVersions.put(java.lang.Class.class, kClassDescValueHash);
        kSpecialCasesVersions.put(java.lang.Object.class, kObjectValueHash);
        kSpecialCasesVersions.put(java.io.Serializable.class, kSerializableValueHash);
        kSpecialCasesVersions.put(java.io.Externalizable.class, kExternalizableValueHash);
        kSpecialCasesVersions.put(java.rmi.Remote.class, kRemoteValueHash);
    }

    public static final Hashtable<String, Class<?>> kSpecialCasesClasses = new Hashtable<>();

    static {
        kSpecialCasesClasses.put(kWStringTypeStr, java.lang.String.class);
        kSpecialCasesClasses.put(kClassDescTypeStr, java.lang.Class.class);
        kSpecialCasesClasses.put(kRemoteTypeStr, java.rmi.Remote.class);
        kSpecialCasesClasses.put("org.omg.CORBA.WStringValue", java.lang.String.class);
        kSpecialCasesClasses.put("javax.rmi.CORBA.ClassDesc", java.lang.Class.class);

        // 6793820: need to handle classes of primitive types!
        kSpecialCasesClasses.put( "boolean", boolean.class ) ;
        kSpecialCasesClasses.put( "byte", byte.class ) ;
        kSpecialCasesClasses.put( "char", char.class ) ;
        kSpecialCasesClasses.put( "short", short.class ) ;
        kSpecialCasesClasses.put( "int", int.class ) ;
        kSpecialCasesClasses.put( "long", long.class ) ;
        kSpecialCasesClasses.put( "float", float.class ) ;
        kSpecialCasesClasses.put( "double", double.class ) ;
    }

    public static final Hashtable<Class<?>, String> kSpecialCasesArrayPrefix = new Hashtable<>();

    static {
        kSpecialCasesArrayPrefix.put( java.lang.String.class, kValuePrefix + kSequencePrefix + kCORBAPrefix);
        kSpecialCasesArrayPrefix.put( java.lang.Class.class, kValuePrefix + kSequencePrefix + "javax/rmi/CORBA/");
        kSpecialCasesArrayPrefix.put( java.lang.Object.class, kValuePrefix + kSequencePrefix + "java/lang/");
        kSpecialCasesArrayPrefix.put( java.io.Serializable.class, kValuePrefix + kSequencePrefix + "java/io/");
        kSpecialCasesArrayPrefix.put( java.io.Externalizable.class, kValuePrefix + kSequencePrefix + "java/io/");
        kSpecialCasesArrayPrefix.put( java.rmi.Remote.class, kValuePrefix + kSequencePrefix + kCORBAPrefix);
    }

    public static final Hashtable<String, String> kSpecialPrimitives = new Hashtable<>();

    static {
        kSpecialPrimitives.put("int","long");
        kSpecialPrimitives.put("long","longlong");
        kSpecialPrimitives.put("byte","octet");
    }

    /**
     * Used to convert ascii to hex.
     */
    private static final byte ASCII_HEX[] =     {
        (byte)'0',
        (byte)'1',
        (byte)'2',
        (byte)'3',
        (byte)'4',
        (byte)'5',
        (byte)'6',
        (byte)'7',
        (byte)'8',
        (byte)'9',
        (byte)'A',
        (byte)'B',
        (byte)'C',
        (byte)'D',
        (byte)'E',
        (byte)'F',
    };


    // bug fix for 4328952; to eliminate possibility of overriding this
    // in a subclass.
    public static final RepositoryIdCache cache = new RepositoryIdCache();

    // Interface Rep ID Strings
    public static final String kjava_rmi_Remote = createForAnyType(java.rmi.Remote.class);
    public static final String korg_omg_CORBA_Object = createForAnyType(org.omg.CORBA.Object.class);

    // To create a RepositoryID, use code similar to the following:
    // RepositoryId.cache.getId( id );
    
    RepositoryId(){}

    RepositoryId(String aRepId){
        init(aRepId);
    }

    RepositoryId init(String aRepId) {
        this.repId = aRepId;
                
        // Special case for remote
        if (aRepId.length() == 0) {
            clazz = java.rmi.Remote.class;
            typeString = "";
            isRMIValueType = true;
            suid = kInterfaceOnlyHashStr;
            return this;
        } else if (aRepId.equals(kWStringValueRepID)) {
            clazz = java.lang.String.class;
            typeString = kWStringTypeStr;
            isIDLType = true;
            // fix where Attempting to obtain a FullValueDescription 
            // for an RMI value type with a String field causes an exception. 
            completeClassName = "java.lang.String";
            versionString = kWStringValueVersion;
            return this;                        
        } else {
            String repId = convertFromISOLatin1(aRepId);

            int firstIndex = repId.indexOf(':') ;
            if (firstIndex == -1)
                throw new IllegalArgumentException( 
                    "RepositoryId must have the form <type>:<body>" ) ;
            int secondIndex = repId.indexOf( ':', firstIndex + 1 ) ;

            if (secondIndex == -1)
                versionString = "" ;
            else 
                versionString = repId.substring(secondIndex) ;

            if (repId.startsWith(kIDLPrefix)) {
                typeString =
                    repId.substring(kIDLPrefixLength, 
                        repId.indexOf(':', kIDLPrefixLength));
                isIDLType = true;

                if (typeString.startsWith(kIDLNamePrefix))
                    completeClassName = kIDLClassnamePrefix + 
                        typeString.substring(
                            kIDLNamePrefix.length()).replace('/','.');
                else 
                    completeClassName = typeString.replace('/','.');

            } else if (repId.startsWith(kValuePrefix)) {
                typeString =
                    repId.substring(kValuePrefixLength, 
                        repId.indexOf(':', kValuePrefixLength));
                isRMIValueType = true;

                if (versionString.indexOf('.') == -1) {
                    actualSuid = versionString.substring(1);
                    suid = actualSuid;  // default if not explicitly specified
                                
                    if (actualSuid.indexOf(':') != -1){
                    // we have a declared hash also
                        int pos = actualSuid.indexOf(':')+1;
                        // actualSuid = suid.substring(pos);
                        // suid = suid.substring(0, pos-1);
                        suid = actualSuid.substring(pos);
                        actualSuid = actualSuid.substring(0, pos-1);
                    }
                } else {
                    // _REVISIT_ : Special case version failure ?
                }
            } else {
                isSupportedFormat = false;
                typeString = "" ;
            }

            if (typeString.startsWith(kSequencePrefix)) {
                isSequence = true;
            }
                
            return this;
        }
    }

    public final String getUnqualifiedName() {
        if (unqualifiedName == null){
            String className = getClassName();
            int index = className.lastIndexOf('.');
            if (index == -1){
                unqualifiedName = className;
                definedInId = "IDL::1.0";
            }
            else {
                unqualifiedName = className.substring(index);
                definedInId = "IDL:" 
                    + className.substring(0, index).replace('.','/') + ":1.0";
            }
        }
                
        return unqualifiedName;
    }

    public final String getDefinedInId() {
        if (definedInId == null){
            getUnqualifiedName();
        }
                
        return definedInId;
    }

    public final String getTypeString() {
        return typeString;
    }

    public final String getVersionString() {
        return versionString;
    }
        
    public final String getSerialVersionUID() {
        return suid;
    }

    public final String getActualSerialVersionUID() {
        return actualSuid;
    }
    public final long getSerialVersionUIDAsLong() {
        return suidLong;
    }

    public final long getActualSerialVersionUIDAsLong() {
        return actualSuidLong;
    }

    public final boolean isRMIValueType() {
        return isRMIValueType;
    }
        
    public final boolean isIDLType() {
        return isIDLType;
    }

    public final String getRepositoryId() {
        return repId;
    }

    public static byte[] getByteArray(String repStr) {
        synchronized (repStrToByteArray){
            return repStrToByteArray.get(repStr);
        }
    }

    public static void setByteArray(String repStr, byte[] repStrBytes) {
        synchronized (repStrToByteArray){
            repStrToByteArray.put(repStr, repStrBytes);
        }
    }

    public final boolean isSequence() {
        return isSequence;
    }

    public final boolean isSupportedFormat() {
        return isSupportedFormat;
    }

    // This method will return the classname from the typestring OR 
    // if the classname turns out to be a special class "pseudo" name, 
    // then the matching real classname is returned.
    public final String getClassName() {

        if (isRMIValueType)
            return typeString;
        else if (isIDLType)
            return completeClassName;
        else 
            return null;
    }

    // This method calls getClazzFromType() and falls back to the repStrToClass
    // cache if no class was found.  It's used where any class matching the
    // given repid is an acceptable result.
    public final Class<?> getAnyClassFromType() throws ClassNotFoundException {
        try {
            return getClassFromType();
        } catch (ClassNotFoundException cnfe) {
            Class<?> clz = repStrToClass.get(repId);
            if (clz != null)
                return clz;
            else
                throw cnfe;
        }
    }

    public final Class<?> getClassFromType() throws ClassNotFoundException {
        if (clazz != null)
            return clazz;
                
        Class<?> specialCase = (Class)kSpecialCasesClasses.get(getClassName());

        if (specialCase != null) {
            clazz = specialCase;
            return specialCase;
        } else {
            try {
                return Util.getInstance().loadClass(getClassName(), 
                    null, null);
            } catch(ClassNotFoundException cnfe) {
                if (defaultServerURL != null) {
                    try {
                        return getClassFromType(defaultServerURL);
                    } catch(MalformedURLException mue){
                        throw cnfe;
                    }
                } else {
                    throw cnfe;
                }
            }
        }
    }

    public final Class<?> getClassFromType(Class<?> expectedType, String codebase)
        throws ClassNotFoundException {
        if (clazz != null)
            return clazz;
                
        Class<?> specialCase = (Class)kSpecialCasesClasses.get(getClassName());

        if (specialCase != null){
            clazz = specialCase;
            return specialCase;
        } else {
            ClassLoader expectedTypeClassLoader = 
                (expectedType == null ? null : expectedType.getClassLoader());
            return Utility.loadClassOfType(getClassName(),
                                            codebase,
                                            expectedTypeClassLoader,
                                            expectedType,
                                            expectedTypeClassLoader);
        }

    }

    public final Class<?> getClassFromType(String url) 
        throws ClassNotFoundException, MalformedURLException {
        
        // 6793820: check special cases BEFORE going to ClassLoader.
        if (clazz != null)
            return clazz;

        Class<?> specialCase = (Class)kSpecialCasesClasses.get(getClassName());

        if (specialCase != null) {
            clazz = specialCase;
            return specialCase;
        } else {
            return Util.getInstance().loadClass(getClassName(), url, null);
        }
    }

    @Override
    public final String toString() {
        return repId;
    }

    /**
     * Checks to see if the FullValueDescription should be retrieved.
     * @param clazz The type to get description for
     * @param repositoryID The repository ID
     * @return If full description should be retrieved
     * @throws IOException If suids do not match or if the repositoryID
     * is not an RMIValueType.
     */
    public static boolean useFullValueDescription(Class clazz, 
        String repositoryID) throws IOException{

        return useFullValueDescription( clazz, ClassInfoCache.get( clazz ),
            repositoryID ) ;
    }

    /**
     * Checks to see if the FullValueDescription should be retrieved.
     * @param clazz The type to get description for
     * @param cinfo The ClassInfo for the type.
     * @param repositoryID The repository ID
     * @return If full description should be retrieved
     * @exception IOException If suids do not match or if the repositoryID
     * is not an RMIValueType.
     */
    public static boolean useFullValueDescription(Class<?> clazz, 
        ClassInfoCache.ClassInfo cinfo,
        String repositoryID) throws IOException{

        String clazzRepIDStr = createForAnyType(clazz, cinfo );

        if (clazzRepIDStr.equals(repositoryID))
            return false;

        RepositoryId targetRepid;
        RepositoryId clazzRepid;

        synchronized(cache) {
            // to avoid race condition where multiple threads could be
            // accessing this method, and their access to the cache may
            // be interleaved giving unexpected results

            targetRepid = cache.getId(repositoryID);
            clazzRepid = cache.getId(clazzRepIDStr);
        }
                
        if ((targetRepid.isRMIValueType()) && (clazzRepid.isRMIValueType())){
            if (!targetRepid.getSerialVersionUID()
                .equals(clazzRepid.getSerialVersionUID())) {

                String mssg = 
                    "Mismatched serialization UIDs : Source (Rep. ID" 
                    + clazzRepid + ") = " 
                    + clazzRepid.getSerialVersionUID() 
                    + " whereas Target (Rep. ID " + repositoryID 
                    + ") = " + targetRepid.getSerialVersionUID();
                throw new IOException(mssg);
            } else {
                return true;
            }           
        } else {
            throw new IOException(
                "The repository ID is not of an RMI value type (Expected ID = " 
                + clazzRepIDStr + "; Received ID = " + repositoryID +")");
        }
    }

    private static String createHashString(Class<?> clazz) {

        ClassInfoCache.ClassInfo cinfo = ClassInfoCache.get( clazz ) ;
        if (cinfo.isInterface() || !cinfo.isASerializable(clazz))
            return kInterfaceHashCode;

        long actualLong = ObjectStreamClass.getActualSerialVersionUID(clazz);
        String hash = null;
        if (actualLong == 0)
            hash = kInterfaceOnlyHashStr;
        else if (actualLong == 1)
            hash = kExternalizableHashStr;
        else
            hash = Long.toHexString(actualLong).toUpperCase();
        while(hash.length() < 16) {
            hash = "0" + hash;
        }

        long declaredLong = ObjectStreamClass.getSerialVersionUID(clazz);
        String declared = null;
        if (declaredLong == 0)
            declared = kInterfaceOnlyHashStr;
        else if (declaredLong == 1)
            declared = kExternalizableHashStr;
        else
            declared = Long.toHexString(declaredLong).toUpperCase();
        while (declared.length() < 16) {
            declared = "0" + declared;
        }
        hash = hash + ":" + declared;

        return ":" + hash;
    }

    /**
     * Creates a repository ID for a sequence.  This is for expert users only as
     * this method assumes the object passed is an array.  If passed an object 
     * that is not an array, it will produce a rep id for a sequence of zero 
     * length.  This would be an error.
     * @param ser The Java object to create a repository ID for
     * @return Created repository ID
     **/
    public static String createSequenceRepID(java.lang.Object ser){
        return createSequenceRepID(ser.getClass());
    }

    /**
     * Creates a repository ID for a sequence.  This is for expert users only as
     * this method assumes the object passed is an array.  If passed an object 
     * that is not an array, it will produce a malformed rep id.
     * @param clazz The Java class to create a repository ID for
     * @return Created repository ID
     **/
    public static String createSequenceRepID(Class<?> clazz){
        synchronized (classSeqToRepStr) {
            String repid = classSeqToRepStr.get(clazz);
            if (repid != null)
                return repid;

            Class<?> originalClazz = clazz;

            Class<?> type = null;
            int numOfDims = 0;

            while ((type = clazz.getComponentType()) != null) {
                numOfDims++;
                clazz = type;
            }

            if (clazz.isPrimitive()) {
                repid = kValuePrefix + originalClazz.getName() 
                    + kPrimitiveSequenceValueHash;
            } else {
                StringBuilder buf = new StringBuilder();
                buf.append(kValuePrefix);
                while(numOfDims-- > 0) {
                    buf.append("[");
                }
                buf.append("L");
                buf.append(convertToISOLatin1(clazz.getName()));
                buf.append(";");
                buf.append(createHashString(clazz));
                repid = buf.toString();
            }
            classSeqToRepStr.put(originalClazz,repid);
            return repid;
        }
    }


    public static String createForSpecialCase(java.lang.Class<?> clazz){
        return createForSpecialCase( clazz, ClassInfoCache.get( clazz ) ) ;
    }

    public static String createForSpecialCase(java.lang.Class<?> clazz,
        ClassInfoCache.ClassInfo cinfo ){
        if (cinfo.isArray()) {
            return createSequenceRepID(clazz);
        } else {
            if (clazz == String.class)
                return kWStringValueRepID ;
            if (clazz == Class.class) 
                return kClassDescValueRepID ;
            if (clazz == java.rmi.Remote.class) 
                return kRemoteValueRepID ;
            return null ;
        }
    }

    public static String createForSpecialCase(java.io.Serializable ser){
        Class<?> clazz = ser.getClass();
        if (ClassInfoCache.get(clazz).isArray()) {
            return createSequenceRepID(ser);
        } else {
            return createForSpecialCase(clazz);
        }
    }
        
    /**
     * Creates a repository ID for a normal Java Type.  
     * @param ser The Java object to create a repository ID for
     * @return Created repository ID
     * @exception com.sun.corba.ee.impl.io.TypeMismatchException if 
     * ser implements the org.omg.CORBA.portable.IDLEntity interface 
     * which indicates it is an IDL Value type.
     **/
    public static String createForJavaType(java.io.Serializable ser)
        throws com.sun.corba.ee.impl.io.TypeMismatchException
    {
        synchronized (classToRepStr) {
            String repid = createForSpecialCase(ser);
            if (repid != null)
                return repid;
            Class<?> clazz = ser.getClass();
            repid = classToRepStr.get(clazz);

            if (repid != null)
                return repid;

            repid = kValuePrefix + convertToISOLatin1(clazz.getName()) +
                createHashString(clazz);

            classToRepStr.put(clazz, repid);
            repStrToClass.put(repid, clazz);
            return repid;
        }
    }

    public static String createForJavaType(Class<?> clz)
        throws com.sun.corba.ee.impl.io.TypeMismatchException
    {
        return createForJavaType( clz, ClassInfoCache.get( clz ) ) ;
    }

    /**
     * Creates a repository ID for a normal Java Type.
     * @param clz The Java class to create a repository ID for
     * @param cinfo ClassInfo; may be null
     * @return Created repository ID
     * @exception com.sun.corba.ee.impl.io.TypeMismatchException if 
     * ser implements the * org.omg.CORBA.portable.IDLEntity interface 
     * which indicates it is an IDL Value type.
     **/
    public static String createForJavaType(Class<?> clz, ClassInfoCache.ClassInfo cinfo )
        throws com.sun.corba.ee.impl.io.TypeMismatchException
    {
        synchronized (classToRepStr){
            String repid = createForSpecialCase(clz,cinfo);
            if (repid != null)
                return repid;

            repid = classToRepStr.get(clz);
            if (repid != null)
                return repid;

            repid = kValuePrefix + convertToISOLatin1(clz.getName()) +
                createHashString(clz);

            classToRepStr.put(clz, repid);
            repStrToClass.put(repid, clz);
            return repid;
        }
    }

    /**
     * Creates a repository ID for an IDL Java Type.
     * @param ser The IDL Value object to create a repository ID for
     * @param major The major version number
     * @param minor The minor version number
     * @return Created repository ID
     * @exception com.sun.corba.ee.impl.io.TypeMismatchException if ser does not implement the
     * org.omg.CORBA.portable.IDLEntity interface which indicates it is an IDL Value type.
     **/
    public static String createForIDLType(Class<?> ser, int major, int minor)
        throws com.sun.corba.ee.impl.io.TypeMismatchException
    {
        synchronized (classIDLToRepStr){
            String repid = classIDLToRepStr.get(ser);
            if (repid != null)
                return repid;

            repid = kIDLPrefix 
                + convertToISOLatin1(ser.getName()).replace('.','/') 
                + ":" + major + "." + minor;
            classIDLToRepStr.put(ser, repid);
            return repid;
        }
    }

    private static String getIdFromHelper(Class<?> clazz){
        try {
            Class<?> helperClazz = 
                Utility.loadClassForClass(clazz.getName()+"Helper", 
                null, clazz.getClassLoader(), clazz, clazz.getClassLoader());
            Method idMethod = helperClazz.getDeclaredMethod("id");
            return (String)idMethod.invoke(null);
        } catch (java.lang.ClassNotFoundException | java.lang.NoSuchMethodException
                | java.lang.reflect.InvocationTargetException | java.lang.IllegalAccessException cnfe) {
            throw new org.omg.CORBA.MARSHAL(cnfe.toString());
        }
    }

    public static String createForAnyType(Class<?> type ) {
        return createForAnyType( type, ClassInfoCache.get( type ) ) ;
    }

    /**
     * Createa a repository ID for the type if it is either a java type
     * or an IDL type.
     * @param type The type to create rep. id for
     * @param cinfo The ClassInfo for the type (pre-computed elsewhere to save time)
     * @return The rep. id.
     **/
    public static String createForAnyType(Class<?> type, ClassInfoCache.ClassInfo cinfo) {
        try{
            // We may re-compute the repo id more than once, but that's OK, because
            // it's always the same.
            String result = cinfo.getRepositoryId() ;
            if (result == null) {
                if (cinfo.isArray()) {
                    result = createSequenceRepID(type);
                } else if (cinfo.isAIDLEntity(type)) {
                    try{
                        result = getIdFromHelper(type);
                    } catch(Throwable t) {
                        return createForIDLType(type, 1, 0);
                    }
                } else 
                    result = createForJavaType(type, cinfo );

                cinfo.setRepositoryId( result ) ;
            }

            return result ;
        } catch(com.sun.corba.ee.impl.io.TypeMismatchException e){ 
            return null; 
        }

    }

    public static boolean isAbstractBase(Class<?> clazz) {
        ClassInfoCache.ClassInfo cinfo = ClassInfoCache.get( clazz ) ;
        return cinfo.isInterface() && cinfo.isAIDLEntity(clazz) 
            && !cinfo.isAValueBase(clazz) && !cinfo.isACORBAObject(clazz) ;
    }

    public static boolean isAnyRequired(Class clazz) {
        return ((clazz == java.lang.Object.class) ||
                (clazz == java.io.Serializable.class) ||
                (clazz == java.io.Externalizable.class));
    }

    public static long fromHex(String hexNumber) {
        if (hexNumber.startsWith("0x")) {
            return Long.valueOf(hexNumber.substring(2), 16);
        }
        return Long.valueOf(hexNumber, 16);
    }

    /**
     * Convert strings with illegal IDL identifier characters.
     * <p>
     * Section 5.5.7 of OBV spec.
     * @param name String to convert
     * @return Converted String
     */
    public static String convertToISOLatin1 (String name) {

        int length = name.length();
        if (length == 0) {
            return name;
        }
        StringBuilder buffer = null;

        for (int i = 0; i < length; i++) {

            char c = name.charAt(i);

            if (c > 255 || IDL_IDENTIFIER_CHARS[c] == 0) {
            
                // We gotta convert. Have we already started?

                if (buffer == null) {

                    // No, so get set up...

                    buffer = new StringBuilder(name.substring(0,i));
                }

                // Convert the character into the IDL escape syntax...
                buffer.append("\\U")
                        .append((char)ASCII_HEX[(c & 0xF000) >>> 12])
                        .append((char)ASCII_HEX[(c & 0x0F00) >>> 8])
                        .append((char)ASCII_HEX[(c & 0x00F0) >>> 4])
                        .append((char)ASCII_HEX[(c & 0x000F)]);
                        
            } else {
                if (buffer != null) {
                    buffer.append(c);
                }
            }
        }
        
        if (buffer != null) {
            name = buffer.toString();
        }
 
        return name;
    }
    
    /**
     * Convert strings with ISO Latin 1 escape sequences back to original 
     * strings.
     * <p>
     * Section 5.5.7 of OBV spec.
     */
    private static String convertFromISOLatin1 (String name) {

        int index = -1;
        StringBuilder buf = new StringBuilder(name);

        while ((index = buf.toString().indexOf("\\U")) != -1){
            String str = "0000" + buf.toString().substring(index+2, index+6);

            // Convert Hexadecimal
            byte[] buffer = new byte[(str.length() - 4) / 2];
            for (int i=4, j=0; i < str.length(); i +=2, j++) {
                buffer[j] = 
                    (byte)((Utility.hexOf(str.charAt(i)) << 4) & 0xF0);
                buffer[j] |= 
                    (byte)((Utility.hexOf(str.charAt(i+1)) << 0) & 0x0F);
            }            
            buf = new StringBuilder(delete(buf.toString(), index, index+6));
            buf.insert(index, (char)buffer[1]);
        }
        
        return buf.toString();


    }

    private static String delete(String str, int from, int to)
    {
        return str.substring(0, from) + str.substring(to, str.length());    
    }

    private static String replace(String target, String arg, String source)
    {
        int i = 0;
        i = target.indexOf(arg);

        while(i != -1)
            {
                String left = target.substring(0, i);
                String right = target.substring(i+arg.length());
                target = left+source+right;
                i = target.indexOf(arg);
            }
        return target;
    }

    public static int computeValueTag(boolean codeBasePresent, int typeInfo, 
        boolean chunkedEncoding){

        int value_tag = kInitialValueTag;
                
        if (codeBasePresent)
            value_tag = value_tag | 0x00000001;
        
        value_tag = value_tag | typeInfo;

        if (chunkedEncoding)
            value_tag = value_tag | kChunkedMask;

        return value_tag;
    }

    public static boolean isCodeBasePresent(int value_tag){
        return ((value_tag & 0x00000001) == 1);
    }

    public static int getTypeInfo(int value_tag){
        return (value_tag & 0x00000006);
    }

    public static boolean isChunkedEncoding(int value_tag){
        return ((value_tag & kChunkedMask) != 0); 
    }
        
    public static String getServerURL(){
        return defaultServerURL;
    }
}

