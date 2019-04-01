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
import org.omg.CORBA.ORB;

// Import org.omg.CosNaming classes
import org.omg.CosNaming.Binding;
import org.omg.CosNaming.BindingHolder;
import org.omg.CosNaming.BindingIteratorPOA;
import org.omg.CORBA.BAD_PARAM;

/**
 * Class BindingIteratorImpl implements the org.omg.CosNaming::BindingIterator interface, but does not implement the
 * method to retrieve the next binding in the NamingContext for which it was created. This is left to a subclass, which
 * is why this class is abstract; BindingIteratorImpl provides an implementation of the interface operations on top of
 * two subclass methods, allowing multiple implementations of iterators that differ in storage and access to the
 * contents of a NamingContext implementation.
 * <p>
 * The operation next_one() is implemented by the subclass, whereas next_n() is implemented on top of the next_one()
 * implementation. Destroy must also be implemented by the subclass.
 * <p>
 * A subclass must implement NextOne() and Destroy(); these methods are invoked from synchronized methods and need
 * therefore not be synchronized themselves.
 */
public abstract class BindingIteratorImpl extends BindingIteratorPOA {
    protected ORB orb;

    /**
     * Create a binding iterator servant. runs the super constructor.
     *
     * @param orb an ORB object.
     * @exception java.lang.Exception a Java exception.
     */
    public BindingIteratorImpl(ORB orb) throws java.lang.Exception {
        super();
        this.orb = orb;
    }

    /**
     * Return the next binding. It also returns true or false, indicating whether there were more bindings.
     *
     * @param b The Binding as an out parameter.
     * @return true if there were more bindings.
     * @exception org.omg.CORBA.SystemException One of a fixed set of CORBA system exceptions.
     * @see NextOne
     */
    public synchronized boolean next_one(org.omg.CosNaming.BindingHolder b) {
        // NextOne actually returns the next one
        return nextOneImpl(b);
    }

    /**
     * Return the next n bindings. It also returns true or false, indicating whether there were more bindings.
     *
     * @param how_many The number of requested bindings in the BindingList.
     * @param blh The BindingList as an out parameter.
     * @return true if there were more bindings.
     * @exception org.omg.CORBA.SystemException One of a fixed set of CORBA system exceptions.
     */
    public synchronized boolean next_n(int how_many, org.omg.CosNaming.BindingListHolder blh) {
        if (how_many == 0) {
            throw new BAD_PARAM(" 'how_many' parameter is set to 0 which is" + " invalid");
        }
        return list(how_many, blh);
    }

    /**
     * lists next n bindings. It returns true or false, indicating whether there were more bindings. This method has the
     * package private scope, It will be called from NamingContext.list() operation or this.next_n().
     *
     * @param how_many The number of requested bindings in the BindingList.
     * @param blh The BindingList as an out parameter.
     * @return true if there were more bindings.
     */
    public boolean list(int how_many, org.omg.CosNaming.BindingListHolder blh) {
        // Take the smallest of what's left and what's being asked for
        int numberToGet = Math.min(remainingElementsImpl(), how_many);

        // Create a resulting BindingList
        Binding[] bl = new Binding[numberToGet];
        BindingHolder bh = new BindingHolder();
        int i = 0;
        // Keep iterating as long as there are entries
        while (i < numberToGet && this.nextOneImpl(bh) == true) {
            bl[i] = bh.value;
            i++;
        }
        // Found any at all?
        if (i == 0) {
            // No
            blh.value = new Binding[0];
            return false;
        }

        // Set into holder
        blh.value = bl;

        return true;
    }

    /**
     * Destroy this BindingIterator object. The object corresponding to this object reference is destroyed.
     *
     * @exception org.omg.CORBA.SystemException One of a fixed set of CORBA system exceptions.
     * @see Destroy
     */
    public synchronized void destroy() {
        // Destroy actually destroys
        this.destroyImpl();
    }

    /**
     * Abstract method for returning the next binding in the NamingContext for which this BindingIterator was created.
     *
     * @param b The Binding as an out parameter.
     * @return true if there were more bindings.
     * @exception org.omg.CORBA.SystemException One of a fixed set of CORBA system exceptions.
     */
    protected abstract boolean nextOneImpl(org.omg.CosNaming.BindingHolder b);

    /**
     * Abstract method for destroying this BindingIterator.
     *
     * @exception org.omg.CORBA.SystemException One of a fixed set of CORBA system exceptions.
     */
    protected abstract void destroyImpl();

    /**
     * Abstract method for returning the remaining number of elements.
     *
     * @return the remaining number of elements in the iterator.
     */
    protected abstract int remainingElementsImpl();
}
