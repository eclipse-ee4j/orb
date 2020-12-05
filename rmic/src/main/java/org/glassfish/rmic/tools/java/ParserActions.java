/*
 * Copyright (c) 1996, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.rmic.tools.java;

import org.glassfish.rmic.tools.tree.*;

/**
 * This is the protocol by which a Parser makes callbacks
 * to the later phases of the compiler.
 * <p>
 * (As a backwards compatibility trick, Parser implements
 * this protocol, so that an instance of a Parser subclass
 * can handle its own actions.  The preferred way to use a
 * Parser, however, is to instantiate it directly with a
 * reference to your own ParserActions implementation.)
 *
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 *
 * @author      John R. Rose
 */
public interface ParserActions {
    /**
     * package declaration
     */
    void packageDeclaration(long off, IdentifierToken nm);

    /**
     * import class
     */
    void importClass(long off, IdentifierToken nm);

    /**
     * import package
     */
    void importPackage(long off, IdentifierToken nm);

    /**
     * Define class
     * @return a cookie for the class
     * This cookie is used by the parser when calling defineField
     * and endClass, and is not examined otherwise.
     */
    ClassDefinition beginClass(long off, String doc,
                               int mod, IdentifierToken nm,
                               IdentifierToken sup, IdentifierToken impl[]);


    /**
     * End class
     * @param c a cookie returned by the corresponding beginClass call
     */
    void endClass(long off, ClassDefinition c);

    /**
     * Define a field
     * @param c a cookie returned by the corresponding beginClass call
     */
    void defineField(long where, ClassDefinition c,
                     String doc, int mod, Type t,
                     IdentifierToken nm, IdentifierToken args[],
                     IdentifierToken exp[], Node val);
}
