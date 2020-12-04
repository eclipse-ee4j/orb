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

package com.sun.corba.ee.impl.ior ;

import java.util.List ;
import java.util.AbstractList ;

import com.sun.corba.ee.spi.ior.MakeImmutable ;

/** Simple class that delegates all List operations to 
* another list.  It also can be frozen, which means that
* a number of operations can be performed on the list,
* and then the list can be made immutable, so that no
* further changes are possible.  A FreezableList is frozen
* using the makeImmutable method.
*/
public class FreezableList<E> extends AbstractList<E> {
    private List<E> delegate = null ;
    private boolean immutable = false ;

    @Override
    public boolean equals( Object obj )
    {
        if (obj == null)
            return false ;

        if (!(obj instanceof FreezableList))
            return false ;

        FreezableList other = FreezableList.class.cast( obj ) ;

        return delegate.equals( other.delegate ) &&
            (immutable == other.immutable) ;
    }

    @Override
    public int hashCode()
    {
        return delegate.hashCode() ;
    }

    public FreezableList( List<E> delegate, boolean immutable  )
    {
        this.delegate = delegate ;
        this.immutable = immutable ;
    }

    public FreezableList( List<E> delegate )
    {
        this( delegate, false ) ;
    }

    public void makeImmutable()
    {
        immutable = true ;
    }

    public boolean isImmutable()
    {
        return immutable ;
    }

    public void makeElementsImmutable()
    {
        for (E x : this) {
            if (x instanceof MakeImmutable) {
                MakeImmutable element = MakeImmutable.class.cast( x ) ;
                element.makeImmutable() ;
            }
        }
    }

    // Methods overridden from AbstractList

    public int size()
    {
        return delegate.size() ;
    }

    public E get(int index)
    {
        return delegate.get(index) ;
    }

    @Override
    public E set(int index, E element)
    {
        if (immutable)
            throw new UnsupportedOperationException() ;

        return delegate.set(index, element) ;
    }

    @Override
    public void add(int index, E element)
    {
        if (immutable)
            throw new UnsupportedOperationException() ;

        delegate.add(index, element) ;
    }

    @Override
    public E remove(int index)
    {
        if (immutable)
            throw new UnsupportedOperationException() ;

        return delegate.remove(index) ;
    }

    // We also override subList so that the result is a FreezableList.
    @Override
    public List<E> subList(int fromIndex, int toIndex)
    {
        List<E> list = delegate.subList(fromIndex, toIndex) ;
        List<E> result = new FreezableList<E>( list, immutable ) ;
        return result ;
    }
}
