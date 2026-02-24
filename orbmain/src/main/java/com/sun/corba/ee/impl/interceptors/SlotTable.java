/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2019 Payara Services Ltd.
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

package com.sun.corba.ee.impl.interceptors;

import com.sun.corba.ee.impl.corba.AnyImpl;
import com.sun.corba.ee.spi.orb.ORB;

import org.omg.CORBA.Any;
import org.omg.PortableInterceptor.InvalidSlot;

/**
 * SlotTable is used internally by PICurrent to store the slot information.
 */
public class SlotTable {
    /** The vector where all the slot data for the current thread is stored */
    private Any[] theSlotData;

    /** Required for instantiating Any object. */
    private ORB orb;

    /** The flag to check whether there are any updates in the current SlotTable.
     * The slots will be reset to null, only if this flag is set. */
    private boolean dirtyFlag;

    /**
     * The constructor instantiates an Array of Any[] of size given by slotSize
     * parameter.
     * @param orb The ORB
     * @param slotSize Size of array
     */
    SlotTable( ORB orb, int slotSize ) {
        dirtyFlag = false;
        this.orb = orb;
        theSlotData = new Any[slotSize];
    }

    /**
     * This method sets the slot data at the given slot id (index).
     * @param id Index
     * @param data Slot data
     * @throws InvalidSlot If the id is greater than the slot data size.
     */
    public void set_slot( int id, Any data ) throws InvalidSlot
    {
        // First check whether the slot is allocated
        // If not, raise the invalid slot exception
        if( id >= theSlotData.length ) {
            throw new InvalidSlot();
        }
        dirtyFlag = true;
        theSlotData[id] = data;
    }

    /**
     * This method get the slot data for the given slot id (index).
     * @param id Index
     * @return Slot data
     * @throws InvalidSlot If the id is greater than the slot data size.
     */
    public Any get_slot( int id ) throws InvalidSlot
    {
        // First check whether the slot is allocated
        // If not, raise the invalid slot exception
        if( id >= theSlotData.length ) {
            throw new InvalidSlot();
        }
        if( theSlotData[id] == null ) {
            theSlotData [id] = new AnyImpl(orb);
        }
        return theSlotData[ id ];
    }


    /**
     * This method resets all the slot data to null if dirtyFlag is set.
     */
    void resetSlots( ) {
        if( dirtyFlag == true ) {
            for( int i = 0; i < theSlotData.length; i++ ) {
                theSlotData[i] = null;
            }
        }
    }

    /**
     * This method returns the size of the allocated slots.
     * @return slot size
     */
    int getSize( ) {
        return theSlotData.length;
    }

}
    
