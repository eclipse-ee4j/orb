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

package com.sun.corba.ee.spi.presentation.rmi ;

import java.io.PrintStream ;

import java.util.Map ;

import java.lang.reflect.Method ;

import javax.rmi.CORBA.Tie ;
import org.glassfish.pfl.basic.proxy.InvocationHandlerFactory;

/** Provides access to RMI-IIOP stubs and ties.  
 * Any style of stub and tie generation may be used.  
 * This includes compiler generated stubs and runtime generated stubs 
 * as well as compiled and reflective ties.  There is normally
 * only one instance of this interface per VM.  The instance
 * is obtained from the static method
 * com.sun.corba.ee.spi.orb.ORB.getPresentationManager.
 * <p>
 * Note that
 * the getClassData and getDynamicMethodMarshaller methods
 * maintain caches to avoid redundant computation.
 */
public interface PresentationManager
{
    /** Creates StubFactory and Tie instances.
     */
    public interface StubFactoryFactory
    {
        /** Return the standard name of a stub (according to the RMI-IIOP specification
         * and rmic).  This is needed so that the name of a stub is known for
         * standalone clients of the app server.
         *
         * @param className name of the class
         * @return the stub name
         */
        String getStubName( String className ) ;

        /** Create a stub factory for stubs for the interface whose type is given by
         * className.  className may identify either an IDL interface or an RMI-IIOP
         * interface.  
         * @param className The name of the remote interface as a Java class name.
         * @param isIDLStub True if className identifies an IDL stub, else false.
         * @param remoteCodeBase The CodeBase to use for loading Stub classes, if
         * necessary (may be null or unused).
         * @param expectedClass The expected stub type (may be null or unused).
         * @param classLoader The classLoader to use (may be null).
         * @return The stub factory
         */
        PresentationManager.StubFactory createStubFactory( String className, 
            boolean isIDLStub, String remoteCodeBase, Class<?> expectedClass,
            ClassLoader classLoader);

        /** Return a Tie for the given class.
         *
         * @param cls class
         * @return The tie corresponding to cls
         */
        Tie getTie( Class<?> cls ) ;

        /** Return whether or not this StubFactoryFactory creates StubFactory
         * instances that create dynamic stubs and ties.  At the top level, 
         * true indicates that rmic -iiop is not needed for generating stubs
         * or ties.
         *
         * @return true iff we are using dynamic stubs
         */
        boolean createsDynamicStubs() ;
    }

    /** Creates the actual stub needed for RMI-IIOP remote
     * references.
     */
    public interface StubFactory
    {
        /** Create a new dynamic stub.  It has the type that was
         * used to create this factory.
         *
         * @return The dynamic stub
         */
        org.omg.CORBA.Object makeStub() ;

        /** Return the repository ID information for all Stubs
         * created by this stub factory.
         *
         * @return Array of type ids, most derived type first.
         */
        String[] getTypeIds() ;
    }

    public interface ClassData 
    {
        /** Get the class used to create this ClassData instance
         *
         * @return Class of this ClassData.
         */
        Class<?> getMyClass() ;

        /** Get the IDLNameTranslator for the class used to create
         * this ClassData instance.
         *
         * @return IDLNameTranslator for the class of this ClassData
         */
        IDLNameTranslator getIDLNameTranslator() ;

        /** Return the array of repository IDs for all of the remote
         * interfaces implemented by this class.
         *
         * @return The typeids, most derived first.
         */
        String[] getTypeIds() ;

        /** Get the InvocationHandlerFactory that is used to create
         * an InvocationHandler for dynamic stubs of the type of the
         * ClassData.  
         *
         * @return InvocationHandlerFactory.
         */
        InvocationHandlerFactory getInvocationHandlerFactory() ;

        /** Get the dictionary for this ClassData instance.
         * This is used to hold class-specific information for a Class
         * in the class data.  This avoids the need to create other
         * caches for accessing the information.
         *
         * @return the dictionary.
         */
        Map<String,Object> getDictionary() ;
    }

    /** Get the ClassData for a particular class.
     * This class may be an implementation class, in which 
     * case the IDLNameTranslator handles all Remote interfaces implemented by 
     * the class.  If the class implements more than one remote interface, and not 
     * all of the remote interfaces are related by inheritance, then the type 
     * IDs have the implementation class as element 0.  
     * @param cls iClass fro which we need ClassData.
     * @return The ClassData.
     */
    ClassData getClassData( Class<?> cls ) ;

    /** Given a particular method, return a DynamicMethodMarshaller 
     * for that method.  This is used for dynamic stubs and ties.
     * @param method Method for which we need a DynamicMethodMarshaller.
     * @return The DynamicMethodMarshaller.
     */
    DynamicMethodMarshaller getDynamicMethodMarshaller( Method method ) ;

    /** Return the registered StubFactoryFactory.
     * @param isDynamic true iff we want the dynamic stub factory
     * @return static or dynamic stub factory.
     * @deprecated use {@link #getDynamicStubFactoryFactory()} or {@link #getStaticStubFactoryFactory()}
     */
    StubFactoryFactory getStubFactoryFactory( boolean isDynamic ) ;

    /** Return the registered static StubFactoryFactory.
     * @return static stub factory.
     */
    StubFactoryFactory getStaticStubFactoryFactory();

    /** Return the registered dynamic StubFactoryFactory.
     * @return dynamic stub factory.
     */
    StubFactoryFactory getDynamicStubFactoryFactory();

    /** Equivalent to getStubFactoryFactory( true ).getTie( null ).
     * Provided for compatibility with earlier versions of PresentationManager
     * as used in the app server.  The class argument is ignored in
     * the dynamic case, so this is safe.
     * @return The static tie.
     */
    Tie getTie() ;

    /** Get the correct repository ID for the given implementation
     * instance.  This is useful when using dynamic RMI with the POA.
     * @param impl implementation
     * @return repository ID string
     */
    String getRepositoryId( java.rmi.Remote impl ) ;

    /** Returns the value of the com.sun.corba.ee.ORBUseDynamicStub
     * property.
     * @return whether to use dynamic stubs.
     */
    boolean useDynamicStubs() ;

    /** Remove all internal references to Class cls from the
     *  PresentationManager. This allows ClassLoaders to
     *  be garbage collected when they are no longer needed.
     * @param cls Class to flush
     */
    void flushClass( Class<?> cls ) ;

    boolean getDebug() ;

    PrintStream getPrintStream() ;
}
