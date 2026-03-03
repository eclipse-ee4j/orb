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

package com.sun.corba.ee.impl.misc;

import com.sun.corba.ee.spi.orb.ORB ;

import java.io.Externalizable ;
import java.io.Serializable ;
import java.lang.reflect.Proxy ;
import java.rmi.Remote ;
import java.rmi.RemoteException ;
import java.util.Map ;
import java.util.WeakHashMap ;

import org.omg.CORBA.UserException ;
import org.omg.CORBA.portable.CustomValue ;
import org.omg.CORBA.portable.IDLEntity ;
import org.omg.CORBA.portable.ObjectImpl ;
import org.omg.CORBA.portable.Streamable ;
import org.omg.CORBA.portable.StreamableValue ;
import org.omg.CORBA.portable.ValueBase ;

/** This class caches information about classes that is somewhat expensive
 * to obtain, notably the results of isInterface(), isArray(), and isAssignableFrom.
 * A user simply calls ClassInfoCache.get( Class ) to get the information about
 * a class.
 * <P>
 * All of the isA methods on ClassInfo need to be passed the same Class that was
 * used in the get call!  This is an awkward interface, but the alternative is
 * to store the class in the ClassInfo, which would create a strong reference from
 * the value to the key, making the WeakHashMap useless.  It also appears to be
 * difficult to use a weak or soft reference here, because I can't handle the
 * case of an empty reference to the class inside the ClassInfo object.
 * If ClassInfoCache supported the methods directly, we could work around this,
 * but then we would in some case be doing multiple lookups for a class to get
 * class information, which would slow things down significantly (the get call
 * is a significant cost in the benchmarks).
 * <P>
 * XXX There is a better solution: add MORE information to the cache.  In
 * particular, use a ClassAnalyzer to construct the linearized inheritance
 * chain (order doesn't matter in this case, but we already have the implementation)
 * and implement all of the isA methods by a simple check on whether the class
 * is in the chain (or perhaps convert to Set).  We can statically fill the cache
 * with all of the basic objects (e.g. Streamable, Serializable, etc.) needs
 * in a static initializer.
 */

public class ClassInfoCache {
    // Do NOT put a strong reference to the Class in ClassInfo: we don't want to
    // pin ClassLoaders in memory!
    public static class ClassInfo {

        public static class LazyWrapper {
            Class<?> isAClass ;
            boolean initialized ;
            boolean value ;

            public LazyWrapper( Class<?> isAClass ) {
                this.isAClass = isAClass ;
                this.initialized = false ;
                this.value = false ;
            }

            synchronized boolean get( Class<?> cls ) {
                if (!initialized) {
                    initialized = true ;
                    value = isAClass.isAssignableFrom( cls ) ;
                }

                return value ;
            }
        }

        private boolean isAValueBase ;
        private boolean isAString ;
        private boolean isAIDLEntity ;

        private LazyWrapper isARemote = new LazyWrapper( 
            Remote.class ) ;
        private LazyWrapper isARemoteException = new LazyWrapper( 
            RemoteException.class ) ;
        private LazyWrapper isAUserException = new LazyWrapper( 
            UserException.class ) ;
        private LazyWrapper isAObjectImpl = new LazyWrapper( 
            ObjectImpl.class ) ;
        private LazyWrapper isAORB = new LazyWrapper( 
            ORB.class ) ;
        private LazyWrapper isAStreamable = new LazyWrapper( 
            Streamable.class ) ;
        private LazyWrapper isAStreamableValue = new LazyWrapper( 
            StreamableValue.class ) ;
        private LazyWrapper isACustomValue = new LazyWrapper( 
            CustomValue.class ) ;
        private LazyWrapper isACORBAObject = new LazyWrapper( 
            org.omg.CORBA.Object.class ) ;
        private LazyWrapper isASerializable = new LazyWrapper( 
            Serializable.class ) ;
        private LazyWrapper isAExternalizable = new LazyWrapper( 
            Externalizable.class ) ;
        private LazyWrapper isAClass = new LazyWrapper( 
            Class.class ) ;

        private String repositoryId = null ;

        private boolean isArray ;
        private boolean isEnum ;
        private boolean isInterface ;
        private boolean isProxyClass ;
        private ClassInfo superInfo ;

        ClassInfo( Class<?> cls ) {
            isArray = cls.isArray() ;
            isEnum = isEnum(cls) ;
            isInterface = cls.isInterface() ;
            isProxyClass = Proxy.isProxyClass( cls ) ;

            isAValueBase = ValueBase.class.isAssignableFrom( cls ) ;
            isAString = String.class.isAssignableFrom( cls ) ;
            isAIDLEntity = IDLEntity.class.isAssignableFrom( cls ) ;

            Class<?> superClass = cls.getSuperclass() ;
            if (superClass != null) {
                superInfo = ClassInfoCache.get( superClass ) ;
            }
        }

        private boolean isEnum(Class<?> cls) {
            // Issue 11681
            // This ugly method is needed because isEnum returns FALSE
            // on enum.getClass().isEnum() if enum has an abstract method,
            // which results in another subclass.  So for us, a class is an
            // enum if any superclass is java.lang.Enum.
            Class<?> current = cls ;
            while (current != null) {
                if (current.equals( Enum.class )) {
                    return true ;
                }
                current = current.getSuperclass() ;
            }

            return false ;
        }
        
        public synchronized String getRepositoryId() {
            return repositoryId ;
        }

        public synchronized void setRepositoryId( String repositoryId ) {
            this.repositoryId = repositoryId ;
        }

        public boolean isARemote( Class<?> cls ) { 
            return isARemote.get(cls) ; 
        }
        public boolean isARemoteException( Class<?> cls ) { 
            return isARemoteException.get(cls) ; 
        }
        public boolean isAUserException( Class<?> cls ) { 
            return isAUserException.get(cls) ; 
        }
        public boolean isAObjectImpl( Class<?> cls ) { 
            return isAObjectImpl.get(cls) ; 
        }
        public boolean isAORB( Class<?> cls ) { 
            return isAORB.get(cls) ; 
        }
        public boolean isAIDLEntity( Class<?> cls ) { 
            return isAIDLEntity ; 
        }
        public boolean isAStreamable( Class<?> cls ) { 
            return isAStreamable.get(cls) ; 
        }
        public boolean isAStreamableValue( Class<?> cls ) { 
            return isAStreamableValue.get(cls) ; 
        }
        public boolean isACustomValue( Class<?> cls ) { 
            return isACustomValue.get(cls) ; 
        }
        public boolean isAValueBase( Class<?> cls ) { 
            return isAValueBase ; 
        }
        public boolean isACORBAObject( Class<?> cls ) { 
            return isACORBAObject.get(cls) ; 
        }
        public boolean isASerializable( Class<?> cls ) { 
            return isASerializable.get(cls) ; 
        }
        public boolean isAExternalizable( Class<?> cls ) { 
            return isAExternalizable.get(cls) ; 
        }
        public boolean isAString( Class<?> cls ) { 
            return isAString ; 
        }
        public boolean isAClass( Class<?> cls ) { 
            return isAClass.get(cls) ; 
        }

        public boolean isArray() { return isArray ; }
        public boolean isEnum() { return isEnum ; }
        public boolean isInterface() { return isInterface ; }
        public boolean isProxyClass() { return isProxyClass ; }
        public ClassInfo getSuper() { return superInfo ; }
    }

    // This shows up as a locking hotspot in heavy marshaling tests.
    // Ideally we need a WeakConcurrentMap, which is not available in
    // the JDK (Google's MapMaker can easily construct such a class).

    /* Version using ConcurrentMap for testing ONLY
     * (This would pin Classes (and thus ClassLoaders), leading to
     * App server deployment memory leaks.
    
    private static ConcurrentMap<Class,ClassInfo> classData = 
        new ConcurrentHashMap<Class,ClassInfo>() ;

    public static ClassInfo get( Class cls ) {
        ClassInfo result = classData.get( cls ) ;
        if (result == null) {
            final ClassInfo cinfo = new ClassInfo( cls ) ;
            final ClassInfo putResult = classData.putIfAbsent( cls, cinfo ) ;
            if (putResult == null) {
                result = cinfo ;
            } else {
                result = putResult ;
            }
        }

        return result ;
    }
    */

    private static Map<Class,ClassInfo> classData = new WeakHashMap<Class,ClassInfo>() ;

    public static synchronized ClassInfo get( Class<?> cls ) {
        ClassInfo result = classData.get( cls ) ;
        if (result == null && cls != null) {
            result = new ClassInfo( cls ) ;
            classData.put( cls, result ) ;
        }

        return result ;
    }

    /** Find the class that is an enum in the superclass chain starting at cls.
     * cinfo MUST be the ClassInfo for cls.
     * @param cinfo ClassInfo for cls
     * @param cls Class which may have java.lang.Enum in its superclass chain.
     * @return A class for which isEnum() is true, or null if no such class
     * exists in the superclass chain of cls.
     */
    public static Class getEnumClass( ClassInfo cinfo, Class cls ) {
        ClassInfo currInfo = cinfo ;
        Class currClass = cls ;
        while (currClass != null) { 
            if (currClass.isEnum()) {
                break ;
            }

            currClass = currClass.getSuperclass() ;
            currInfo = currInfo.getSuper() ;
        }

        return currClass ;
    }
}
