/*
 * Copyright (c) 2003, 2020 Oracle and/or its affiliates. All rights reserved.
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

package argparser ;

/** A utilitiy class representing a generic types Pair of elements.
 * Useful for simple data structures, returning multiple values, etc.
 * Pair<Object,Object> is similar to a cons cell.
 */
public class Pair<S,T> {
    protected S _first ;
    protected T _second ;

    public Pair( final S first, final T second ) {
	_first = first ;
	_second = second ;
    }

    public Pair( final S first ) {
	this( first, null ) ;
    }

    public Pair() {
	this( null ) ;
    }

    public S first() {
	return _first ;
    }

    public T second() {
	return _second ;
    }

    @Override
    public boolean equals( Object obj ) {
	if (obj == this) {
	    return true ;
        }

	if (!(obj instanceof Pair)) {
	    return false ;
        }

	Pair pair = Pair.class.cast( obj ) ;

	if (first() == null ? 
	    pair.first() == null : first().equals( pair.first())) {
	    return (second() == null ? 
		pair.second() == null : second().equals( pair.second())) ;
	} else {
	    return false ;
	}
    }

    @Override
    public int hashCode() {
	int result = 0 ;
	if (_first != null) {
            result ^= _first.hashCode();
        }
	if (_second != null) {
            result ^= _second.hashCode();
        }

	return result ;
    }

    @Override
    public String toString() {
	return "Pair[" + _first + "," + _second + "]" ;
    }
}
