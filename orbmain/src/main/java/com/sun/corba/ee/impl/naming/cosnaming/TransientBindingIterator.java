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

import java.util.Iterator;

// Get base implementation

// Get a hash table
import java.util.Map;

// Import general CORBA classes
import org.omg.CORBA.ORB;
// Get org.omg.CosNaming Types
import org.omg.CosNaming.Binding;
import org.omg.CosNaming.BindingType;
import org.omg.CosNaming.NameComponent;
import org.omg.PortableServer.POA;

/**
 * Class TransientBindingIterator implements the abstract methods
 * defined by BindingIteratorImpl, to use with the TransientNamingContext
 * implementation of the NamingContextImpl. The TransientBindingIterator
 * implementation receives a hash table of InternalBindingValues, and uses
 * an Enumeration to iterate over the contents of the hash table.
 * @see BindingIteratorImpl
 * @see TransientNamingContext
 */
public class TransientBindingIterator extends BindingIteratorImpl
{
    // There is only one POA used for both TransientNamingContext and
    // TransientBindingIteraor servants.
    private POA nsPOA;
    /**
     * Constructs a new TransientBindingIterator object.
     * @param orb a org.omg.CORBA.ORB object.
     * @param aTable A hashtable containing InternalBindingValues which is
     * the content of the TransientNamingContext.
     * @param thePOA the POA to use.
     * @throws java.lang.Exception a Java exception.
   */
    public TransientBindingIterator(ORB orb, 
        Map<InternalBindingKey,InternalBindingValue> aTable,
        POA thePOA )
        throws java.lang.Exception
    {
        super(orb);
        bindingMap = aTable;
        bindingIterator = aTable.values().iterator() ;
        currentSize = this.bindingMap.size();
        this.nsPOA = thePOA;
    }

    /**
   * Returns the next binding in the NamingContext. Uses the enumeration
   * object to determine if there are more bindings and if so, returns
   * the next binding from the InternalBindingValue.
   * @param b The Binding as an out parameter.
   * @return true if there were more bindings.
   */
    final public boolean nextOneImpl(org.omg.CosNaming.BindingHolder b)
    {
        // If there are more elements get the next element
        boolean hasMore = bindingIterator.hasNext();
        if (hasMore) {
            b.value = bindingIterator.next().theBinding;
            currentSize--;
        } else {
            // Return empty but marshalable binding
            b.value = new Binding(new NameComponent[0],BindingType.nobject);
        }
        return hasMore;
    }

    /**
     * Destroys this BindingIterator by disconnecting from the ORB
     * @exception org.omg.CORBA.SystemException One of a fixed set of CORBA 
     * system exceptions.
     */
    final public void destroyImpl()
    {
        // Remove the object from the Active Object Map.
        try {
            byte[] objectId = nsPOA.servant_to_id( this );
            if( objectId != null ) {
                nsPOA.deactivate_object( objectId );
            }
        } 
        catch( Exception e ) {
            NamingUtils.errprint("BindingIterator.Destroy():caught exception:");
            NamingUtils.printException(e);
        }
    }

    /**
     * Returns the remaining number of elements in the iterator.
     * @return the remaining number of elements in the iterator.   
     */
    public final int remainingElementsImpl() {
        return currentSize;
    }

    private int currentSize;
    private Map<InternalBindingKey,InternalBindingValue> bindingMap;
    private Iterator<InternalBindingValue> bindingIterator;
}
