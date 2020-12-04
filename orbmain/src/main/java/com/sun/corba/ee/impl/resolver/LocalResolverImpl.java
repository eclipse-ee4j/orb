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

package com.sun.corba.ee.impl.resolver ;

import com.sun.corba.ee.spi.resolver.LocalResolver ;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.TimeUnit;
import org.glassfish.pfl.basic.func.NullaryFunction;

public class LocalResolverImpl implements LocalResolver {
    ConcurrentHashMap<String,NullaryFunction<org.omg.CORBA.Object>> nameToClosure =
        new ConcurrentHashMap<String,NullaryFunction<org.omg.CORBA.Object>>() ;
    final Lock lock = new ReentrantLock();

    public org.omg.CORBA.Object resolve(String name) {
        do {
            try {
                if (lock.tryLock(500, TimeUnit.MILLISECONDS)) {
                    try {
                        NullaryFunction<org.omg.CORBA.Object> cl = nameToClosure.get(name);
                        if (cl == null) {
                            return null;
                        }
                        return (org.omg.CORBA.Object) (cl.evaluate());
                    } finally {
                        lock.unlock();
                    }
                } else {
                    // continue, try again
                }
            } catch (InterruptedException e) {
                // do nothing
            }
        } while (true);
    }

    public java.util.Set<String> list() {
        return nameToClosure.keySet() ;
    }

    public void register( String name,
        NullaryFunction<org.omg.CORBA.Object> closure ) {
        nameToClosure.put( name, closure ) ;
    }
}
