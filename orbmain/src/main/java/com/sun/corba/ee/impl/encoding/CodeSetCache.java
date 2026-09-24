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

package com.sun.corba.ee.impl.encoding;

import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.HashMap;

/**
 * Thread local cache of sun.io code set converters for performance.
 *
 * The thread local class contains a single reference to a Map[] containing two small HashMaps. One for CharsetEncoders
 * and one for CharsetDecoders. Charset names are drawn from the finite negotiated code-set registry, so weak-key
 * eviction only adds lookup overhead and provides no useful reclamation here.
 *
 * This is used internally by CodeSetConversion.
 */
class CodeSetCache {
    private ThreadLocal<HashMap<String, CharsetEncoder>> ctbMapLocal = new ThreadLocal<HashMap<String, CharsetEncoder>>() {
        @Override
        protected HashMap<String, CharsetEncoder> initialValue() {
            return new HashMap<String, CharsetEncoder>(4);
        }
    };

    private ThreadLocal<HashMap<String, CharsetDecoder>> btcMapLocal = new ThreadLocal<HashMap<String, CharsetDecoder>>() {
        @Override
        protected HashMap<String, CharsetDecoder> initialValue() {
            return new HashMap<String, CharsetDecoder>(4);
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
