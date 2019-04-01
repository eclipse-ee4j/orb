/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.ior;

import java.util.ArrayList;
import java.util.Iterator;

import com.sun.corba.ee.impl.ior.FreezableList;

/**
 * Convenience class for defining objects that contain lists of Identifiables. Mainly implements iteratorById. Also note
 * that the constructor creates the list, which here is always an ArrayList, as this is much more efficient overall for
 * short lists.
 *
 * @author Ken Cavanaugh
 */
public class IdentifiableContainerBase<E extends Identifiable> extends FreezableList<E> {
    /**
     * Create this class with an empty list of identifiables. The current implementation uses an ArrayList.
     */
    public IdentifiableContainerBase() {
        super(new ArrayList<E>());
    }

    /**
     * Return an iterator which iterates over all contained Identifiables with type given by id.
     */
    public Iterator<E> iteratorById(final int id) {
        return new Iterator<E>() {
            Iterator<E> iter = IdentifiableContainerBase.this.iterator();
            E current = advance();

            private E advance() {
                while (iter.hasNext()) {
                    E ide = iter.next();
                    if (ide.getId() == id)
                        return ide;
                }

                return null;
            }

            public boolean hasNext() {
                return current != null;
            }

            public E next() {
                E result = current;
                current = advance();
                return result;
            }

            public void remove() {
                iter.remove();
            }
        };
    }
}
