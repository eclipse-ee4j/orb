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

package com.sun.corba.ee.impl.naming.cosnaming;

// Import general CORBA classes
import org.omg.PortableServer.POA;

// Import org.omg.CosNaming types
import org.omg.CosNaming.Binding;
import org.omg.CosNaming.BindingType;
import org.omg.CosNaming.BindingTypeHolder;
import org.omg.CosNaming.BindingListHolder;
import org.omg.CosNaming.BindingIteratorHolder;
import org.omg.CosNaming.NameComponent;

import java.util.Map;
import java.util.HashMap;

import com.sun.corba.ee.spi.logging.NamingSystemException;
import com.sun.corba.ee.spi.trace.Naming;

/**
 * Class TransientNamingContext implements the methods defined by NamingContextDataStore, and extends the
 * NamingContextImpl class to provide a servant implementation of CosNaming::NamingContext. The TransientNamingContext
 * uses a hash table to store the mappings between bindings and object references and the hash table is not persistent;
 * thereby the name "transient". This class should not be used directly; instead, the class TransientNameService should
 * be instantiated.
 * <p>
 * The keys in the hash table are InternalBindingKey objects, containing a single NameComponent and implementing the
 * proper functions, i.e., equals() and hashCode() in an efficient manner. The values in the hash table are
 * InternalBindingValues and store a org.omg.CosNaming::Binding and the object reference associated with the binding.
 * For iteration, TransientBindingIterator objects are created, which are passed a cloned copy of the hashtable. Since
 * elements are inserted and deleted and never modified, this provides stable iterators at the cost of cloning the hash
 * table.
 * <p>
 * To create and destroy object references, the TransientNamingContext uses the orb.connect() and orb.disconnect()
 * methods.
 *
 * @see NamingContextImpl
 * @see NamingContextDataStore
 * @see TransientBindingIterator
 * @see TransientNameService
 */
@Naming
public class TransientNamingContext extends NamingContextImpl implements NamingContextDataStore {
    private NamingSystemException wrapper;

    /**
     * Constructs a new TransientNamingContext object.
     * 
     * @param orb an orb object.
     * @param initial the initial naming context.
     * @param nsPOA the POA
     * @exception Exception a Java exception thrown of the base class cannot initialize.
     */
    public TransientNamingContext(com.sun.corba.ee.spi.orb.ORB orb, org.omg.CORBA.Object initial, POA nsPOA) throws java.lang.Exception {
        super(orb, nsPOA);
        this.localRoot = initial;
    }

    /**
     * Binds the object to the name component as the specified binding type. It creates a InternalBindingKey object and a
     * InternalBindingValue object and inserts them in the hash table.
     * 
     * @param n A single org.omg.CosNaming::NameComponent under which the object will be bound.
     * @param obj An object reference to be bound under the supplied name.
     * @param bt The type of the binding (i.e., as object or as context).
     * @exception org.omg.CORBA.SystemException One of a fixed set of CORBA system exceptions.
     */
    @Naming
    public final void bindImpl(NameComponent n, org.omg.CORBA.Object obj, BindingType bt) throws org.omg.CORBA.SystemException {
        InternalBindingKey key = new InternalBindingKey(n);
        NameComponent[] name = new NameComponent[1];
        name[0] = n;
        Binding b = new Binding(name, bt);
        InternalBindingValue value = new InternalBindingValue(b, null);
        value.theObjectRef = obj;
        InternalBindingValue oldValue = bindingMap.put(key, value);

        if (oldValue != null) {
            throw wrapper.transNcBindAlreadyBound();
        }
    }

    /**
     * Resolves the supplied name to an object reference and returns the type of the resolved binding. It creates a
     * InternalBindingKey and uses the key for looking up in the hash table. If nothing is found an exception is thrown,
     * otherwise the object reference is returned and the binding type set.
     * 
     * @param n a NameComponent which is the name to be resolved.
     * @param bth the BindingType as an out parameter.
     * @return the object reference bound under the supplied name, null if not found.
     * @exception org.omg.CORBA.SystemException One of a fixed set of CORBA system exceptions.
     */
    @Naming
    public final org.omg.CORBA.Object resolveImpl(NameComponent n, BindingTypeHolder bth) throws org.omg.CORBA.SystemException {
        if ((n.id.length() == 0) && (n.kind.length() == 0)) {
            bth.value = BindingType.ncontext;
            return localRoot;
        }

        // Create a key and lookup the value
        InternalBindingKey key = new InternalBindingKey(n);
        InternalBindingValue value = bindingMap.get(key);
        if (value == null) {
            return null;
        }

        // Copy out binding type and object reference
        bth.value = value.theBinding.binding_type;
        return value.theObjectRef;
    }

    /**
     * Deletes the binding with the supplied name. It creates a InternalBindingKey and uses it to remove the value
     * associated with the key. If nothing is found an exception is thrown, otherwise the element is removed from the hash
     * table.
     * 
     * @param n a NameComponent which is the name to unbind
     * @return the object reference bound to the name, or null if not found.
     * @exception org.omg.CORBA.SystemException One of a fixed set of CORBA system exceptions.
     */
    @Naming
    public final org.omg.CORBA.Object unbindImpl(NameComponent n) throws org.omg.CORBA.SystemException {
        // Create a key and remove it from the hashtable
        InternalBindingKey key = new InternalBindingKey(n);
        InternalBindingValue value = bindingMap.remove(key);

        // Return what was found
        if (value == null) {
            return null;
        } else {
            return value.theObjectRef;
        }
    }

    /**
     * List the contents of this NamingContext. It creates a new TransientBindingIterator object and passes it a clone of
     * the hash table and an orb object. It then uses the newly created object to return the required number of bindings.
     * 
     * @param how_many The number of requested bindings in the BindingList.
     * @param bl The BindingList as an out parameter.
     * @param bi The BindingIterator as an out parameter.
     * @exception org.omg.CORBA.SystemException One of a fixed set of CORBA system exceptions.
     */
    @Naming
    public final void listImpl(int how_many, BindingListHolder bl, BindingIteratorHolder bi) throws org.omg.CORBA.SystemException {
        try {
            // Create a new binding iterator servant with a copy of this
            // hashtable. nsPOA is passed to the object so that it can
            // de-activate itself from the Active Object Map when
            // Binding Iterator.destroy is called.
            Map<InternalBindingKey, InternalBindingValue> copy = new HashMap<InternalBindingKey, InternalBindingValue>(bindingMap);
            TransientBindingIterator bindingIterator = new TransientBindingIterator(this.orb, copy, nsPOA);

            // Have it set the binding list
            bindingIterator.list(how_many, bl);

            byte[] objectId = nsPOA.activate_object(bindingIterator);
            org.omg.CORBA.Object obj = nsPOA.id_to_reference(objectId);

            // Get the object reference for the binding iterator servant
            org.omg.CosNaming.BindingIterator bindingRef = org.omg.CosNaming.BindingIteratorHelper.narrow(obj);

            bi.value = bindingRef;
        } catch (org.omg.CORBA.SystemException e) {
            throw e;
        } catch (Exception e) {
            // Convert to a CORBA system exception
            throw wrapper.transNcListGotExc(e);
        }
    }

    /**
     * Create a new NamingContext. It creates a new TransientNamingContext object, passing it the orb object.
     * 
     * @return an object reference for a new NamingContext object implemented by this Name Server.
     * @exception org.omg.CORBA.SystemException One of a fixed set of CORBA system exceptions.
     */
    @Naming
    public final org.omg.CosNaming.NamingContext newContextImpl() throws org.omg.CORBA.SystemException {
        try {
            // Create a new servant
            TransientNamingContext transContext = new TransientNamingContext((com.sun.corba.ee.spi.orb.ORB) orb, localRoot, nsPOA);

            byte[] objectId = nsPOA.activate_object(transContext);
            org.omg.CORBA.Object obj = nsPOA.id_to_reference(objectId);
            return org.omg.CosNaming.NamingContextHelper.narrow(obj);
        } catch (Exception e) {
            throw wrapper.transNcNewctxGotExc(e);
        }
    }

    /**
     * Destroys this NamingContext by disconnecting from the ORB.
     * 
     * @exception org.omg.CORBA.SystemException One of a fixed set of CORBA system exceptions.
     */
    @Naming
    public final void destroyImpl() throws org.omg.CORBA.SystemException {
        // Destroy the object reference by disconnecting from the ORB
        try {
            byte[] objectId = nsPOA.servant_to_id(this);
            if (objectId != null) {
                nsPOA.deactivate_object(objectId);
            }
        } catch (Exception e) {
            throw wrapper.transNcDestroyGotExc(e);
        }
    }

    /**
     * A Utility Method For Logging..
     */
    private String getName(NameComponent n) {
        return n.id + "." + n.kind;
    }

    /**
     * Return whether this NamingContext contains any bindings. It forwards this request to the hash table.
     * 
     * @return true if this NamingContext contains no bindings.
     */
    public final boolean isEmptyImpl() {
        return bindingMap.isEmpty();
    }

    // A hashtable to store the bindings
    private final Map<InternalBindingKey, InternalBindingValue> bindingMap = new HashMap<InternalBindingKey, InternalBindingValue>();

    /**
     * The local root naming context.
     */
    public org.omg.CORBA.Object localRoot;
}
