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

package corba.dynamicrmiiiop.testclasses;

/**
 * Invalid RMI/IDL CORBA Object Reference Types
 */
public class InvalidObjRefs {

    public static final Class[] CLASSES = { InvalidObjRef1.class, InvalidObjRef2.class, InvalidObjRef3.class, InvalidObjRef4.class,
            InvalidObjRef5.class, InvalidObjRef6.class, InvalidObjRef7.class, InvalidObjRef8.class };

    // must be subtype of org.omg.CORBA.Object
    public interface InvalidObjRef1 {
    }

    // must be subtype of org.omg.CORBA.Object
    public interface InvalidObjRef2 extends InvalidObjRef1 {
    }

    // must be subtype of org.omg.CORBA.Object
    public class InvalidObjRef3 {
    }

    // must be subtype of org.omg.CORBA.Object
    public class InvalidObjRef4 extends InvalidObjRef3 {
    }

    // must be an interface
    public abstract class InvalidObjRef5 extends org.omg.CORBA.portable.ObjectImpl {
    }

    // must be an interface
    public abstract class InvalidObjRef6 extends InvalidObjRef5 {
    }

    // must be an interface
    public abstract class InvalidObjRef7 implements org.omg.CORBA.Object {
    }

    // must be an interface
    public abstract class InvalidObjRef8 extends InvalidObjRef7 {
    }

}
