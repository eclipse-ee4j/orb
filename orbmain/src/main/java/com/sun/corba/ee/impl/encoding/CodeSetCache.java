/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.encoding;

import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.WeakHashMap;

/**
 * Thread local cache of sun.io code set converters for performance.
 *
 * The thread local class contains a single reference to a Map[] containing two WeakHashMaps. One for CharsetEncoders
 * and one for CharsetDecoders. Constants are defined for indexing.
 *
 * This is used internally by CodeSetConversion.
 */
class CodeSetCache {
    private ThreadLocal<WeakHashMap<String, CharsetEncoder>> ctbMapLocal = new ThreadLocal<WeakHashMap<String, CharsetEncoder>>() {
        protected WeakHashMap<String, CharsetEncoder> initialValue() {
            return new WeakHashMap<String, CharsetEncoder>();
        }
    };

    private ThreadLocal<WeakHashMap<String, CharsetDecoder>> btcMapLocal = new ThreadLocal<WeakHashMap<String, CharsetDecoder>>() {
        protected WeakHashMap<String, CharsetDecoder> initialValue() {
            return new WeakHashMap<String, CharsetDecoder>();
        }
    };

    /**
     * Retrieve a CharsetDecoder from the Map using the given key.
     */
    CharsetDecoder getByteToCharConverter(String key) {
        return btcMapLocal.get().get(key);
    }

    /**
     * Retrieve a CharsetEncoder from the Map using the given key.
     */
    CharsetEncoder getCharToByteConverter(String key) {
        return ctbMapLocal.get().get(key);
    }

    /**
     * Stores the given CharsetDecoder in the thread local cache, and returns the same converter.
     */
    CharsetDecoder setConverter(String key, CharsetDecoder converter) {
        btcMapLocal.get().put(key, converter);
        return converter;
    }

    /**
     * Stores the given CharsetEncoder in the thread local cache, and returns the same converter.
     */
    CharsetEncoder setConverter(String key, CharsetEncoder converter) {
        ctbMapLocal.get().put(key, converter);
        return converter;
    }
}
