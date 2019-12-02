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

package com.sun.corba.ee.impl.util;


import java.rmi.server.RMIClassLoader;

import java.security.AccessController;

import java.net.MalformedURLException;

import java.util.Map ;
import java.util.HashMap ;
import java.util.WeakHashMap ;
import java.util.logging.Logger;
import java.util.logging.Level;

import java.lang.ref.SoftReference ;
import java.lang.ref.ReferenceQueue ;

import com.sun.corba.ee.org.omg.CORBA.GetPropertyAction;
import com.sun.corba.ee.spi.logging.ORBUtilSystemException;

/**
 *  Utility methods for doing various method calls which are used
 *  by multiple classes
 */
public class JDKBridge {
    private static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;
    private static Logger logger = Logger.getLogger(JDKBridge.class.getName()) ;

    /**
     * Get local codebase System property (java.rmi.server.codebase).
     * May be null or a space separated array of URLS.
     */
    public static String getLocalCodebase () {
        return localCodebase;
    }
  
    /**
     * Return true if the system property "java.rmi.server.useCodebaseOnly"
     * is set, false otherwise.
     */
    public static boolean useCodebaseOnly () {
        return useCodebaseOnly;
    }
    
    // Building caches for loadClass
    //
    // There are two cases:
    // 1. loader == null
    //      In this case, we need Maps remoteCodeBase -> className -> SoftReference to Class 
    // 2. loader != null,
    //      In this case, we need Maps (weak) loader -> className -> SoftReference to Class
    //
    // We might also want to cache not found results.  This can be represented by:
    // 1. Map remoteCodeBase -> Set classname
    // 2. Map (weak) loader -> Set classname
    // But this is assuming that if a ClassLoader cannot load a class at one time, it also cannot
    // load it later! That is not always true, e.g. a new class file is added to a directory.
    // Best to avoid this!
    //
    // We reclaim soft references using a ReferenceQueue.  

    private static class LoadClassCache {
        private static Map<String,Map<String,Entry>> nullLoaderMap =
            new HashMap<String,Map<String,Entry>>() ;
        private static Map<ClassLoader,Map<String,Entry>> nonNullLoaderMap =
            new WeakHashMap<ClassLoader,Map<String,Entry>>() ;
        private static ReferenceQueue<Class> queue =
            new ReferenceQueue<Class>() ;

        private static class Entry extends SoftReference<Class> {
            String codeBase ;
            ClassLoader loader ;

            public Entry( Class cls, String codeBase, ClassLoader loader ) {
                super( cls, queue ) ;
                this.codeBase = codeBase ;
                this.loader = loader ;
            }

            @Override
            public void clear() {
                codeBase = null ;
                loader = null ;
            }
        }
 
        private static void checkQueue() {
            while (true) {
                Object obj = queue.poll() ;
                if (obj == null) {
                    return ;
                } else {
                    Entry entry = (Entry)obj ;
                    String className = entry.get().getName() ;
                    if (entry.loader == null) {
                        Map<String,Entry> mse = nullLoaderMap.get( entry.codeBase ) ;
                        mse.remove( className ) ;
                        if (mse.isEmpty()) {
                            nullLoaderMap.remove( entry.codeBase ) ;
                        }
                    } else {
                        Map<String,Entry> mse = nonNullLoaderMap.get( entry.loader ) ;
                        mse.remove( className ) ;
                        if (mse.isEmpty()) {
                            nonNullLoaderMap.remove( entry.loader ) ;
                        }
                    }
                    entry.clear() ;
                }
            } 
        }

        /** Returns Class if it is still known to be the resolution of the parameters,
         * throws ClassNotFoundException if it is still known that the class 
         * can NOT be resolved, or return null if nothing is known.
         */
        public static synchronized Class get( String className, String remoteCodebase, 
            ClassLoader loader ) throws ClassNotFoundException {
            
            checkQueue() ;

            Map<String,Entry> scm ;
            if (loader == null) {
                scm = nullLoaderMap.get( remoteCodebase ) ;
            } else {
                scm = nonNullLoaderMap.get( loader ) ;
            }

            Class cls = null ;
            if (scm != null) {
                Entry entry = scm.get( className ) ;
                if (entry != null)
                    cls = entry.get() ;
            }

            return cls ;
        }

        public static synchronized void put( String className, String remoteCodebase, 
            ClassLoader loader, Class cls ) {
            
            checkQueue() ;

            Map<String,Entry> scm ;
            if (loader == null) {
                scm = nullLoaderMap.get( remoteCodebase ) ;
                if (scm == null) {
                    scm = new HashMap<String,Entry>() ;
                    nullLoaderMap.put( remoteCodebase, scm ) ;
                }
            } else {
                scm = nonNullLoaderMap.get( loader ) ;
                if (scm == null) {
                    scm = new HashMap<String,Entry>() ;
                    nonNullLoaderMap.put( loader, scm ) ;
                }
            }

            scm.put( className, new Entry( cls, remoteCodebase, loader ) ) ;
        }
    }

    /**
     * Returns a class instance for the specified class. 
     * @param className the name of the class
     * @param remoteCodebase a space-separated array of urls at which
     * the class might be found. May be null.
     * @param loader a ClassLoader who may be used to
     * load the class if all other methods fail.
     * @return the <code>Class</code> object representing the loaded class.
     * @exception throws ClassNotFoundException if class cannot be loaded.
     */
    public static Class loadClass (String className,
                                   String remoteCodebase,
                                   ClassLoader loader)
        throws ClassNotFoundException {
        
        // XXX GFv3 Disable use of the cache for now:
        //
        // it is caching different classes incorrectly that have
        // the same name, but different ClassLoaders.
        Class cls = null ; 
        // NOCACHE LoadClassCache.get( className, remoteCodebase, loader ) ;
        if (cls == null) {
            if (loader == null) {
                cls = loadClassM(className,remoteCodebase,useCodebaseOnly);
            } else {
                try {
                    cls = loadClassM(className,remoteCodebase,useCodebaseOnly);
                } catch (ClassNotFoundException e) {
                    //  GLASSFISH-18986   [PERF] Failed ClassLoading consuming too much logging time
                    //  limit the logger calls to finest level only
                    if (logger.isLoggable(Level.FINE)) {
                        wrapper.classNotFoundInCodebase( className, remoteCodebase ) ;
                    }
                    cls = loader.loadClass(className);
                }
            }
            // NOCACHE LoadClassCache.put( className, remoteCodebase, loader, cls ) ;
        }

        return cls ;
    }
    
    /**
     * Returns a class instance for the specified class. 
     * @param className the name of the class
     * @param remoteCodebase a space-separated array of urls at which
     * the class might be found. May be null.
     * @return the <code>Class</code> object representing the loaded class.
     * @exception throws ClassNotFoundException if class cannot be loaded.
     */
    public static Class loadClass (String className,
                                   String remoteCodebase)
        throws ClassNotFoundException {
        return loadClass(className,remoteCodebase,null);
    }
    
    /**
     * Returns a class instance for the specified class. 
     * @param className the name of the class
     * @return the <code>Class</code> object representing the loaded class.
     * @exception throws ClassNotFoundException if class cannot be loaded.
     */
    public static Class loadClass (String className)
        throws ClassNotFoundException {
        return loadClass(className,null,null);
    }

    private static final String LOCAL_CODEBASE_KEY = "java.rmi.server.codebase";
    private static final String USE_CODEBASE_ONLY_KEY = "java.rmi.server.useCodebaseOnly";
    private static String localCodebase = null;
    private static boolean useCodebaseOnly;

    static {
        setCodebaseProperties();
    }
 
    /**
     * Set the codebase and useCodebaseOnly properties. This is public
     * only for test code.
     */
    public static synchronized void setCodebaseProperties () {
        String prop = (String)AccessController.doPrivileged(
            new GetPropertyAction(LOCAL_CODEBASE_KEY));

        if (prop != null && prop.trim().length() > 0) {
            localCodebase = prop;
        }

        prop = (String)AccessController.doPrivileged(
            new GetPropertyAction(USE_CODEBASE_ONLY_KEY));

        if (prop != null && prop.trim().length() > 0) {
            useCodebaseOnly = Boolean.valueOf(prop).booleanValue();
        }
    }

    /**
     * Set the default code base. This method is here only
     * for test code.
     */
    public static synchronized void setLocalCodebase(String codebase) {
        localCodebase = codebase;    
    }
 
    private static Class loadClassM (String className, String remoteCodebase, 
        boolean useCodebaseOnly) throws ClassNotFoundException {

        try {
            return JDKClassLoader.loadClass(null,className);
        } catch (ClassNotFoundException e) {
            //  GLASSFISH-18986   [PERF] Failed ClassLoading consuming too much logging time
            //  limit the logger calls to finest level only
            if (logger.isLoggable(Level.FINE)) {
                wrapper.classNotFoundInJDKClassLoader( className, e ) ;
            }
        }

        try {
            if (!useCodebaseOnly && remoteCodebase != null) {
                return RMIClassLoader.loadClass(remoteCodebase,
                                                className);
            } else {
                return RMIClassLoader.loadClass(className);
            }
        } catch (MalformedURLException e) {
            className = className + ": " + e.toString();
        }

        throw new ClassNotFoundException(className);
    }
}

