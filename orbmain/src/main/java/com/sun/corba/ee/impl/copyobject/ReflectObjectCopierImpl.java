/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
 * Copyright (c) 2021 Payara Services Ltd.
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

package com.sun.corba.ee.impl.copyobject ;

import com.sun.corba.ee.impl.misc.ClassInfoCache ;
import com.sun.corba.ee.impl.util.Utility ;
import com.sun.corba.ee.spi.logging.ORBUtilSystemException;
import com.sun.corba.ee.spi.orb.ORB ;

import java.util.IdentityHashMap ;
import java.util.Map ;

import org.glassfish.pfl.basic.logex.OperationTracer;
import org.glassfish.pfl.dynamic.copyobject.impl.ClassCopier;
import org.glassfish.pfl.dynamic.copyobject.impl.ClassCopierBase;
import org.glassfish.pfl.dynamic.copyobject.impl.ClassCopierFactory;
import org.glassfish.pfl.dynamic.copyobject.impl.DefaultClassCopierFactories;
import org.glassfish.pfl.dynamic.copyobject.impl.FastCache;
import org.glassfish.pfl.dynamic.copyobject.impl.PipelineClassCopierFactory;
import org.glassfish.pfl.dynamic.copyobject.spi.ObjectCopier;
import org.glassfish.pfl.dynamic.copyobject.spi.ReflectiveCopyException;
import org.omg.CORBA.portable.Delegate ;
import org.omg.CORBA.portable.ObjectImpl ;

/** Class used to deep copy arbitrary data.  A single 
 * ReflectObjectCopierImpl
 * instance will preserve all object aliasing across multiple calls
 * to copy.
 */
public class ReflectObjectCopierImpl implements ObjectCopier {
    private static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    // Note that this class is the only part of the copyObject
    // framework that is dependent on the ORB.  This is in
    // fact specialized just for CORBA objrefs and RMI-IIOP stubs.

    // This thread local holds an ORB that is used when
    // a Remote needs to be copied, because the autoConnect
    // call requires an ORB.  We do not want to pass an ORB
    // everywhere because that would make the ClassCopier instances
    // ORB dependent, which would prevent them from being 
    // statically scoped.  Note that this is package private so that
    // ObjectCopier can access this data member.
    static final ThreadLocal localORB = new ThreadLocal() ;

    // Special ClassCopier instances needed for CORBA
    
    // For java.rmi.Remote, we need to call autoConnect, 
    // which requires an orb.
    private static ClassCopier remoteClassCopier =
        new ClassCopierBase( "remote" ) {
            public Object createCopy( Object source ) {
                ORB orb = (ORB)localORB.get() ;
                return Utility.autoConnect( source, orb, true ) ;
            }
        } ;

    private static ClassCopier identityClassCopier =
        new ClassCopierBase( "identity" ) {
            public Object createCopy( Object source ) {
                return source ;
            } 
        } ;

    // For ObjectImpl, we just make a shallow copy, since the Delegate
    // is mostly immutable.
    private static ClassCopier corbaClassCopier = 
        new ClassCopierBase( "corba" ) {
            public Object createCopy( Object source) {
                ObjectImpl oi = (ObjectImpl)source ;
                Delegate del = oi._get_delegate() ;

                try {
                    // Create a new object of the same type as source
                    ObjectImpl result = (ObjectImpl)source.getClass().newInstance() ;
                    result._set_delegate( del ) ;

                    return result ;
                } catch (Exception exc) {
                    throw wrapper.exceptionInCreateCopy( exc ) ;

                }
            }
        } ;

    private static final ClassCopierFactory specialClassCopierFactory = 
        new ClassCopierFactory() {
            public ClassCopier getClassCopier( Class cls 
            ) throws ReflectiveCopyException
            {
                ClassInfoCache.ClassInfo cinfo = ClassInfoCache.get( cls ) ;
                
                // Handle Remote: this must come before CORBA.Object,
                // since a corba Object may also be a Remote.
                if (cinfo.isARemote(cls)) {
                    return remoteClassCopier;
                }

                // Handle org.omg.CORBA.portable.ObjectImpl
                if (cinfo.isAObjectImpl(cls)) {
                    return corbaClassCopier;
                }

                // Need this case to handle TypeCode.
                if (cinfo.isAORB(cls)) {
                    return identityClassCopier ;
                }

                return null ;
            }
        } ;

    // It is very important that ccf be static.  This means that
    // ccf is shared across all instances of the object copier,
    // so that any class is analyzed only once, instead of once per 
    // copier instance.  This is worth probably 20%+ in microbenchmark 
    // performance.
    private static final PipelineClassCopierFactory ccf = 
        DefaultClassCopierFactories.getPipelineClassCopierFactory() ; 
    
    static {
        ccf.setSpecialClassCopierFactory( specialClassCopierFactory ) ;
    }

    private final Map oldToNew ;

    /** Create an ReflectObjectCopierImpl for the given ORB.
     * The orb is used for connection Remote instances.
     * @param orb ORB to use for remote instances
     */
    public ReflectObjectCopierImpl( ORB orb )
    {
        localORB.set( orb ) ;
        if (DefaultClassCopierFactories.USE_FAST_CACHE) {
            oldToNew = new FastCache(new IdentityHashMap<>());
        } else {
            oldToNew = new IdentityHashMap<>();
        }
    }

    /** Return a deep copy of obj.  Aliasing is preserved within
     * obj and between objects passed in multiple calls to the
     * same instance of ReflectObjectCopierImpl.
     */
    @Override
    public Object copy( Object obj ) throws ReflectiveCopyException
    {
        return copy( obj, false ) ;
    }

    public Object copy( Object obj, boolean debug ) throws ReflectiveCopyException
    {
        if (obj == null) {
            return null;
        }

        OperationTracer.begin( "ReflectObjectCopierImpl" ) ;
        Class<?> cls = obj.getClass() ;
        ClassCopier copier = ccf.getClassCopier( cls ) ;
        return copier.copy( oldToNew, obj) ;
    }
}
