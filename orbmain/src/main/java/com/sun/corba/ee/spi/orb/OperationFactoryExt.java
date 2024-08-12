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

package com.sun.corba.ee.spi.orb;

import java.lang.reflect.Constructor;

import com.sun.corba.ee.spi.logging.ORBUtilSystemException;

/**
 * Provides an extension to the OperationFactory for convertAction( Class ), which takes a Class with a constructor that
 * takes a String as an argument. It uses the constructor to create an instance of the Class from its argument.
 * <p>
 * This is split off here to avoid problems with the build depending on the version of OperationFactory that is in Java
 * SE 5.0.
 */
public class OperationFactoryExt {
    private static final ORBUtilSystemException wrapper = ORBUtilSystemException.self;

    private OperationFactoryExt() {
    }

    private static class ConvertAction implements Operation {
        private Class<?> cls;
        private Constructor<?> cons;

        public ConvertAction(Class<?> cls) {
            this.cls = cls;
            try {
                cons = cls.getConstructor(String.class);
            } catch (Exception exc) {
                throw wrapper.exceptionInConvertActionConstructor(exc, cls.getName());
            }
        }

        public Object operate(Object value) {
            try {
                return cons.newInstance(value);
            } catch (Exception exc) {
                throw wrapper.exceptionInConvertAction(exc);
            }
        }

        @Override
        public String toString() {
            return "ConvertAction[" + cls.getName() + "]";
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (!(obj instanceof ConvertAction)) {
                return false;
            }

            ConvertAction other = (ConvertAction) obj;

            return toString().equals(other.toString());
        }

        @Override
        public int hashCode() {
            return toString().hashCode();
        }
    }

    public static Operation convertAction(Class<?> cls) {
        return new ConvertAction(cls);
    }
}
