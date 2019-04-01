/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.naming.cosnaming;

// Import general CORBA classes
import org.omg.CORBA.Object;

// Import org.omg.CosNaming classes
import org.omg.CosNaming.BindingType;
import org.omg.CosNaming.BindingTypeHolder;
import org.omg.CosNaming.BindingListHolder;
import org.omg.CosNaming.BindingIteratorHolder;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.PortableServer.POA;

/**
 * This interface defines a set of methods that must be implemented by the "data store" associated with a NamingContext
 * implementation. It allows for different implementations of naming contexts that support the same API but differ in
 * storage mechanism.
 */
public interface NamingContextDataStore {
    /**
     * Method which implements binding a name to an object as the specified binding type.
     *
     * @param n a NameComponent which is the name under which the object will be bound.
     * @param obj the object reference to be bound.
     * @param bt Type of binding (as object or as context).
     * @exception org.omg.CORBA.SystemException One of a fixed set of CORBA system exceptions.
     */
    void bindImpl(NameComponent n, org.omg.CORBA.Object obj, BindingType bt) throws org.omg.CORBA.SystemException;

    /**
     * Method which implements resolving the specified name, returning the type of the binding and the bound object
     * reference. If the id and kind of the NameComponent are both empty, the initial naming context (i.e., the local root)
     * must be returned.
     *
     * @param n a NameComponent which is the name to be resolved.
     * @param bth the BindingType as an out parameter.
     * @return the object reference bound under the supplied name.
     * @exception org.omg.CORBA.SystemException One of a fixed set of CORBA system exceptions.
     */
    org.omg.CORBA.Object resolveImpl(NameComponent n, BindingTypeHolder bth) throws org.omg.CORBA.SystemException;

    /**
     * Method which implements unbinding a name.
     *
     * @return the object reference bound to the name, or null if not found.
     * @exception org.omg.CORBA.SystemException One of a fixed set of CORBA system exceptions.
     */
    org.omg.CORBA.Object unbindImpl(NameComponent n) throws org.omg.CORBA.SystemException;

    /**
     * Method which implements listing the contents of this NamingContext and return a binding list and a binding iterator.
     *
     * @param how_many The number of requested bindings in the BindingList.
     * @param bl The BindingList as an out parameter.
     * @param bi The BindingIterator as an out parameter.
     * @exception org.omg.CORBA.SystemException One of a fixed set of CORBA system exceptions.
     */
    void listImpl(int how_many, BindingListHolder bl, BindingIteratorHolder bi) throws org.omg.CORBA.SystemException;

    /**
     * Method which implements creating a new NamingContext.
     *
     * @return an object reference for a new NamingContext object implemented by this Name Server.
     * @exception org.omg.CORBA.SystemException One of a fixed set of CORBA system exceptions.
     */
    NamingContext newContextImpl() throws org.omg.CORBA.SystemException;

    /**
     * Method which implements destroying this NamingContext.
     *
     * @exception org.omg.CORBA.SystemException One of a fixed set of CORBA system exceptions.
     */
    void destroyImpl() throws org.omg.CORBA.SystemException;

    /**
     * Method which returns whether this NamingContext is empty or not.
     *
     * @return true if this NamingContext contains no bindings.
     */
    boolean isEmptyImpl();

    POA getNSPOA();
}
