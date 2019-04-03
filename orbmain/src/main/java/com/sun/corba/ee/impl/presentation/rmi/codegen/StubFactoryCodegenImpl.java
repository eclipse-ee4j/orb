/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.presentation.rmi.codegen ;

import com.sun.corba.ee.impl.presentation.rmi.StubFactoryDynamicBase;
import com.sun.corba.ee.impl.presentation.rmi.StubInvocationHandlerImpl;
import com.sun.corba.ee.impl.util.Utility;
import com.sun.corba.ee.spi.logging.ORBUtilSystemException;
import com.sun.corba.ee.spi.presentation.rmi.IDLNameTranslator;
import com.sun.corba.ee.spi.presentation.rmi.PresentationManager;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.util.Map;

public class StubFactoryCodegenImpl extends StubFactoryDynamicBase  
{
    private static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    private static final String CODEGEN_KEY = "CodegenStubClass" ;
    private final PresentationManager pm ;

    public StubFactoryCodegenImpl( PresentationManager pm,
        PresentationManager.ClassData classData, ClassLoader loader ) 
    {
        super( classData, loader ) ;
        this.pm = pm ;
    }

    private Class<?> getStubClass() {
        // IMPORTANT: A get & put to classData's dictionary can occur
        //            by two or more threads in this method at the same
        //            time. Therefore, classData must be synchronized here.

        synchronized (classData) {
            final Map<String,Object> dictionary = classData.getDictionary();
            return (Class<?>) dictionary.computeIfAbsent(CODEGEN_KEY, k -> createStubClass());
        }
    }

    private Class<?> createStubClass() {
        final IDLNameTranslator nt = classData.getIDLNameTranslator() ;
        final Class<?> theClass = classData.getMyClass() ;
        final String stubClassName = Utility.dynamicStubName(theClass.getName() ) ;
        final Class<?> baseClass = CodegenStubBase.class ;
        final Class<?>[] interfaces = nt.getInterfaces() ;
        final Method[] methods = nt.getMethods() ;

        // Create a StubGenerator that generates this stub class
        final CodegenProxyCreator creator = new CodegenProxyCreator(stubClassName, baseClass, interfaces, methods);

        // Invoke creator in a doPrivileged block if there is a security manager installed.
        return System.getSecurityManager() == null
              ? createStubClass(creator)
              : AccessController.doPrivileged((PrivilegedAction<Class<?>>) () -> createStubClass(creator)
        );
    }

    private Class<?> createStubClass(CodegenProxyCreator creator) {
        return creator.create(classData.getMyClass(), pm.getDebug(), pm.getPrintStream());
    }

    public org.omg.CORBA.Object makeStub() {
        final Class<?> stubClass = getStubClass( ) ;

        CodegenStubBase stub = null ;

        try {
            // Added doPriv for issue 778
            stub = AccessController.doPrivileged(
                  (PrivilegedExceptionAction<CodegenStubBase>) () -> (CodegenStubBase) stubClass.newInstance()
            ) ;
        } catch (Exception exc) {
            wrapper.couldNotInstantiateStubClass(exc, stubClass.getName()) ;
        }
        
        InvocationHandler handler = new StubInvocationHandlerImpl(pm, classData, stub) ;

        stub.initialize( classData, handler ) ;

        return stub ;
    }
}
