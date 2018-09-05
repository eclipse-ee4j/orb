/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
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
