/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.copyobject;

import com.sun.corba.ee.spi.orb.ORB;
import org.glassfish.pfl.dynamic.copyobject.spi.ObjectCopier;
import org.glassfish.pfl.dynamic.copyobject.spi.ObjectCopierFactory;
import org.glassfish.pfl.dynamic.copyobject.impl.FallbackObjectCopierImpl;

import com.sun.corba.ee.impl.copyobject.ReferenceObjectCopierImpl;
import com.sun.corba.ee.impl.copyobject.ORBStreamObjectCopierImpl;
import com.sun.corba.ee.impl.copyobject.JavaStreamORBObjectCopierImpl;
import com.sun.corba.ee.impl.copyobject.OldReflectObjectCopierImpl;
import com.sun.corba.ee.impl.copyobject.ReflectObjectCopierImpl;

public abstract class CopyobjectDefaults {
    private CopyobjectDefaults() {
    }

    /**
     * Obtain the ORB stream copier factory. Note that this version behaves differently than the others: each ObjectCopier
     * produced by the factory only preserves aliasing within a single call to copy. The others copiers all preserve
     * aliasing across all calls to copy (on the same ObjectCopier instance).
     */
    public static ObjectCopierFactory makeORBStreamObjectCopierFactory(final ORB orb) {
        return new ObjectCopierFactory() {
            public ObjectCopier make() {
                return new ORBStreamObjectCopierImpl(orb);
            }
        };
    }

    public static ObjectCopierFactory makeJavaStreamObjectCopierFactory(final ORB orb) {
        return new ObjectCopierFactory() {
            public ObjectCopier make() {
                return new JavaStreamORBObjectCopierImpl(orb);
            }
        };
    }

    private static final ObjectCopier referenceObjectCopier = new ReferenceObjectCopierImpl();

    private static ObjectCopierFactory referenceObjectCopierFactory = new ObjectCopierFactory() {
        public ObjectCopier make() {
            return referenceObjectCopier;
        }
    };

    /**
     * Obtain the reference object "copier". This does no copies: it just returns whatever is passed to it.
     */
    public static ObjectCopierFactory getReferenceObjectCopierFactory() {
        return referenceObjectCopierFactory;
    }

    /**
     * Create a fallback copier factory from the two ObjectCopierFactory arguments. This copier makes an ObjectCopierFactory
     * that creates instances of a fallback copier that first tries an ObjectCopier created from f1, then tries one created
     * from f2, if the first throws a ReflectiveCopyException.
     */
    public static ObjectCopierFactory makeFallbackObjectCopierFactory(final ObjectCopierFactory f1, final ObjectCopierFactory f2) {
        return new ObjectCopierFactory() {
            public ObjectCopier make() {
                ObjectCopier c1 = f1.make();
                ObjectCopier c2 = f2.make();
                return new FallbackObjectCopierImpl(c1, c2);
            }
        };
    }

    /**
     * Obtain the old version of the reflective copier factory. This is provided only for benchmarking purposes.
     */
    public static ObjectCopierFactory makeOldReflectObjectCopierFactory(final ORB orb) {
        return new ObjectCopierFactory() {
            public ObjectCopier make() {
                return new OldReflectObjectCopierImpl(orb);
            }
        };
    }

    /**
     * Obtain the new reflective copier factory. This is 3-4 times faster than the stream copier, and about 10% faster than
     * the old reflective copier. It should normally be used with a fallback copier, as there are some classes that simply
     * cannot be copied reflectively.
     */
    public static ObjectCopierFactory makeReflectObjectCopierFactory(final ORB orb) {
        return new ObjectCopierFactory() {
            public ObjectCopier make() {
                return new ReflectObjectCopierImpl(orb);
            }
        };
    }
}
