/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package com.sun.corba.ee.impl.misc;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.Remote;

import org.junit.Test;

public class ClassInfoCacheTest {

    interface Greeting extends Remote {
    }

    enum Colour {
        RED {
            @Override
            int shade() {
                return 1;
            }
        };

        abstract int shade();
    }

    @Test
    public void null_has_no_class_info() {
        assertNull(ClassInfoCache.get(null));
    }

    @Test
    public void a_class_gets_one_class_info() {
        assertSame(ClassInfoCache.get(String.class), ClassInfoCache.get(String.class));
    }

    @Test
    public void answers_the_type_questions() {
        ClassInfoCache.ClassInfo info = ClassInfoCache.get(Greeting.class);
        assertTrue(info.isInterface());
        assertTrue(info.isARemote(Greeting.class));
        assertFalse(info.isASerializable(Greeting.class));
        // asked twice: the second answer comes from the cached state
        assertTrue(info.isARemote(Greeting.class));

        ClassInfoCache.ClassInfo string = ClassInfoCache.get(String.class);
        assertTrue(string.isAString(String.class));
        assertTrue(string.isASerializable(String.class));
        assertFalse(string.isARemote(String.class));
    }

    @Test
    public void an_enum_constant_with_a_body_is_an_enum() {
        // Issue 11681: the constant's class is a subclass whose isEnum() is false.
        assertTrue(ClassInfoCache.get(Colour.RED.getClass()).isEnum());
    }

    @Test
    public void the_super_class_info_is_the_cached_one() {
        assertSame(ClassInfoCache.get(Number.class), ClassInfoCache.get(Integer.class).getSuper());
    }

    @Test
    public void the_cache_does_not_keep_an_unloaded_class_alive() throws Exception {
        // What the WeakHashMap was for: after an application is undeployed its
        // classes, and their loader, must be collectable.
        WeakReference<ClassLoader> loader = loadAndForget();
        for (int i = 0; i < 50 && loader.get() != null; i++) {
            System.gc();
            Thread.sleep(20);
        }
        assertNull("the class loader must have been collected", loader.get());
    }

    private static WeakReference<ClassLoader> loadAndForget() throws Exception {
        URL classes = ClassInfoCacheTest.class.getProtectionDomain().getCodeSource().getLocation();
        ClassLoader isolated = new URLClassLoader(new URL[] { classes }, null) {
            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (name.startsWith("java.")) {
                    return ClassLoader.getSystemClassLoader().loadClass(name);
                }
                return super.loadClass(name, resolve);
            }
        };
        Class<?> cls = isolated.loadClass(Payload.class.getName());
        assertTrue(cls.getClassLoader() == isolated);
        assertTrue(ClassInfoCache.get(cls).isASerializable(cls));
        return new WeakReference<>(isolated);
    }

    public static class Payload implements Serializable {
        private static final long serialVersionUID = 1L;
    }
}
