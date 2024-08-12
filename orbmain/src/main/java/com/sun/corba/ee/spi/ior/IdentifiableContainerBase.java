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
     * Return an iterator which iterates over all contained {@link Identifiable Identifiables} with type given by id.
     * 
     * @param id id of type
     * @return Iterator of contained {@link Identifiable Identifiables}
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

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public E next() {
                E result = current;
                current = advance();
                return result;
            }

            @Override
            public void remove() {
                iter.remove();
            }
        };
    }
}
