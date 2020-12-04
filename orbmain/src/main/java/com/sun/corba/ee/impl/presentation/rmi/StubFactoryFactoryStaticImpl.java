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

package com.sun.corba.ee.impl.presentation.rmi;

import javax.rmi.CORBA.Tie ;

import com.sun.corba.ee.spi.presentation.rmi.PresentationManager;

import com.sun.corba.ee.impl.util.PackagePrefixChecker;
import com.sun.corba.ee.impl.util.Utility;

import com.sun.corba.ee.spi.misc.ORBClassLoader;

import com.sun.corba.ee.spi.logging.ORBUtilSystemException ;

import com.sun.corba.ee.impl.javax.rmi.CORBA.Util;

public class StubFactoryFactoryStaticImpl extends 
    StubFactoryFactoryBase 
{
    private static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    public PresentationManager.StubFactory createStubFactory(
        String className, boolean isIDLStub, String remoteCodeBase, Class 
        expectedClass, ClassLoader classLoader)
    {
        String stubName = null ;

        if (isIDLStub) {
            stubName = Utility.idlStubName(className);
        } else {
            stubName =
                Utility.stubNameForCompiler(className);
        }

        ClassLoader expectedTypeClassLoader = 
            (expectedClass == null ? classLoader : 
            expectedClass.getClassLoader());

        // The old code was optimized to try to guess which way to load classes
        // first.  The real stub class name could either be className or 
        // "org.omg.stub." + className.  We will compute this as follows:
        // If stubName starts with a "forbidden" package, try the prefixed
        // version first, otherwise try the non-prefixed version first.
        // In any case, try both forms if necessary.

        String firstStubName = stubName ;
        String secondStubName = stubName ;

        if (PackagePrefixChecker.hasOffendingPrefix(stubName)) {
            firstStubName =
                PackagePrefixChecker.packagePrefix() + stubName;
        } else {
            secondStubName =
                PackagePrefixChecker.packagePrefix() + stubName;
        }

        Class<?> clz = null;

        try {
            clz = Util.getInstance().loadClass( firstStubName, remoteCodeBase, 
                expectedTypeClassLoader ) ;
        } catch (ClassNotFoundException e1) {
            // log only at FINE level
            wrapper.classNotFound1( e1, firstStubName ) ;
            try {
                clz = Util.getInstance().loadClass( secondStubName, remoteCodeBase, 
                    expectedTypeClassLoader ) ;
            } catch (ClassNotFoundException e2) {
                throw wrapper.classNotFound2( e2, secondStubName ) ;
            }
        }

        // XXX Is this step necessary, or should the Util.loadClass
        // algorithm always produce a valid class if the setup is correct?
        // Does the OMG standard algorithm need to be changed to include
        // this step?
        if ((clz == null) || 
            ((expectedClass != null) && !expectedClass.isAssignableFrom(clz))) {
            try {
                clz = ORBClassLoader.loadClass(className);
            } catch (Exception exc) {
                // XXX use framework
                throw new IllegalStateException("Could not load class " +
                    stubName, exc) ;
            }
        }

        return new StubFactoryStaticImpl( clz ) ;
    }

    public Tie getTie( Class cls )
    {
        Class<?> tieClass = null ;
        String className = Utility.tieName(cls.getName());

        // XXX log exceptions at FINE level
        try {
            try {
                //_REVISIT_ The spec does not specify a loadingContext parameter for
                //the following call.  Would it be useful to pass one?  
                tieClass = Utility.loadClassForClass(className, Util.getInstance().getCodebase(cls), 
                    null, cls, cls.getClassLoader());
                return (Tie) tieClass.newInstance();
            } catch (Exception err) {
                tieClass = Utility.loadClassForClass(
                    PackagePrefixChecker.packagePrefix() + className, 
                    Util.getInstance().getCodebase(cls), null, cls, cls.getClassLoader());
                return (Tie) tieClass.newInstance();
            }
        } catch (Exception err) {
            return null;    
        }

    }

    public boolean createsDynamicStubs() 
    {
        return false ;
    }
}
