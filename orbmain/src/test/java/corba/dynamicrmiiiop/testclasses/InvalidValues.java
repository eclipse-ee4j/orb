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
 * Invalid RMI/IDL Value Types
 */
public class InvalidValues {

    public static final Class[] CLASSES =  {
        InvalidValue1.class, InvalidValue2.class,
        InvalidValue3.class, InvalidValue4.class,
        InvalidValue5.class, InvalidValue6.class,
        InvalidValue7.class        
    };

    // must be a subtype of Serializable
    public class InvalidValue1 {}

    // must be a subtype of Serializable
    public class InvalidValue2 extends InvalidValue1 {}

    // must be a class
    public interface InvalidValue3 {}

    // must be a class
    public interface InvalidValue4 extends java.io.Serializable {}

    // can't implement Remote
    public class InvalidValue5 implements java.rmi.Remote {}
    
    // can't implement Remote
    public class InvalidValue6 implements java.io.Serializable,
        java.rmi.Remote {}
    
    // can't implement Remote, directly or indirectly
    public class InvalidValue7 extends InvalidValue5 {}

    
    
}
