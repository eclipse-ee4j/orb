/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.interceptors;

import com.sun.corba.ee.spi.logging.InterceptorsSystemException;

import com.sun.corba.ee.spi.orb.ORB;

/**
 * SlotTableStack is the container of SlotTable instances for each thread
 */
public class SlotTableStack {
    private static final InterceptorsSystemException wrapper = InterceptorsSystemException.self;

    // Contains all the active SlotTables for each thread.
    // The List is made to behave like a stack.
    private java.util.List<SlotTable> tableContainer;

    // Keeps track of number of PICurrents in the stack.
    private int currentIndex;

    // The ORB associated with this slot table stack
    private ORB orb;

    private PICurrent current;

    /**
     * Constructs the stack. This stack must always contain at least one element so that peek never failes.
     */
    SlotTableStack(ORB orb, PICurrent current) {
        this.current = current;
        this.orb = orb;

        currentIndex = 0;
        tableContainer = new java.util.ArrayList<SlotTable>();
        pushSlotTable();
    }

    /**
     * pushSlotTable pushes a fresh Slot Table on to the stack by creating a new SlotTable and pushing that into the
     * SlotTableStack.
     */
    void pushSlotTable() {
        SlotTable table = new SlotTable(orb, current.getTableSize());

        // NOTE: Very important not to always "add" - otherwise a memory leak.
        if (currentIndex == tableContainer.size()) {
            // Add will cause the table to grow.
            tableContainer.add(currentIndex, table);
        } else if (currentIndex > tableContainer.size()) {
            throw wrapper.slotTableInvariant(currentIndex, tableContainer.size());
        } else {
            // Set will override unused slots.
            tableContainer.set(currentIndex, table);
        }
        currentIndex++;
    }

    /**
     * popSlotTable does the following 1: pops the top SlotTable in the SlotTableStack (if there is more than one)
     *
     * 2: resets the slots in the SlotTable which resets the slotvalues to null if there are any previous sets.
     */
    void popSlotTable() {
        if (currentIndex == 1) {
            // Do not pop the SlotTable, If there is only one.
            // This should not happen, But an extra check for safety.
            throw wrapper.cantPopOnlyPicurrent();
        }
        currentIndex--;
        SlotTable table = tableContainer.get(currentIndex);
        tableContainer.set(currentIndex, null); // Do not leak memory.
        table.resetSlots();
    }

    /**
     * peekSlotTable gets the top SlotTable from the SlotTableStack without popping.
     */
    SlotTable peekSlotTable() {
        SlotTable result = tableContainer.get(currentIndex - 1);
        if (result.getSize() != current.getTableSize()) {
            // stale table, so throw it away
            result = new SlotTable(orb, current.getTableSize());
            tableContainer.set(currentIndex - 1, result);
        }

        return result;
    }
}
