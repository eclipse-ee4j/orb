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
 * Invalid IDL Entity Types
 */
public class InvalidEntities {

    public static final Class[] CLASSES = {
        InvalidEntity1.class, 
        InvalidEntity2.class,
        InvalidEntity3.class, 
        InvalidEntity4.class,
        InvalidEntity5.class, 
        InvalidEntity6.class
    };

    // must be a class that is a subtype of org.omg.CORBA.portable.IDLEntity
    public class InvalidEntity1 {}
    
    // must be a class that is a subtype of org.omg.CORBA.portable.IDLEntity
    public class InvalidEntity2 extends InvalidEntity1 {}

    // must be a class 
    public interface InvalidEntity3 {}
    
    public interface InvalidEntity4 extends InvalidEntity3 {}

    public interface InvalidEntity5 extends org.omg.CORBA.portable.IDLEntity {}

    public interface InvalidEntity6 extends InvalidEntity5 {}

}
