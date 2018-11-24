/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1997-1999 IBM Corp. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.tools.corba.ee.idl;

// NOTES:

/**
 * Each entry must have three ways in which it can be instantiated:
 * <ul>
 * <li>with no parameters;
 * <li>cloned from a copy of itself;
 * <li>the normal-use instantiation (usually with 2 parameters: the container and the id of the container).
 * </ul>
 **/
public interface SymtabFactory {
    AttributeEntry attributeEntry();

    AttributeEntry attributeEntry(com.sun.tools.corba.ee.idl.InterfaceEntry container, com.sun.tools.corba.ee.idl.IDLID id);

    com.sun.tools.corba.ee.idl.ConstEntry constEntry();

    com.sun.tools.corba.ee.idl.ConstEntry constEntry(com.sun.tools.corba.ee.idl.SymtabEntry container, com.sun.tools.corba.ee.idl.IDLID id);

    com.sun.tools.corba.ee.idl.NativeEntry nativeEntry();

    com.sun.tools.corba.ee.idl.NativeEntry nativeEntry(com.sun.tools.corba.ee.idl.SymtabEntry container, com.sun.tools.corba.ee.idl.IDLID id);

    com.sun.tools.corba.ee.idl.EnumEntry enumEntry();

    com.sun.tools.corba.ee.idl.EnumEntry enumEntry(com.sun.tools.corba.ee.idl.SymtabEntry container, com.sun.tools.corba.ee.idl.IDLID id);

    com.sun.tools.corba.ee.idl.ExceptionEntry exceptionEntry();

    com.sun.tools.corba.ee.idl.ExceptionEntry exceptionEntry(com.sun.tools.corba.ee.idl.SymtabEntry container, com.sun.tools.corba.ee.idl.IDLID id);

    com.sun.tools.corba.ee.idl.ForwardEntry forwardEntry();

    com.sun.tools.corba.ee.idl.ForwardEntry forwardEntry(com.sun.tools.corba.ee.idl.ModuleEntry container, com.sun.tools.corba.ee.idl.IDLID id);

    com.sun.tools.corba.ee.idl.ForwardValueEntry forwardValueEntry();

    com.sun.tools.corba.ee.idl.ForwardValueEntry forwardValueEntry(com.sun.tools.corba.ee.idl.ModuleEntry container, com.sun.tools.corba.ee.idl.IDLID id);

    com.sun.tools.corba.ee.idl.IncludeEntry includeEntry();

    com.sun.tools.corba.ee.idl.IncludeEntry includeEntry(com.sun.tools.corba.ee.idl.SymtabEntry container);

    com.sun.tools.corba.ee.idl.InterfaceEntry interfaceEntry();

    com.sun.tools.corba.ee.idl.InterfaceEntry interfaceEntry(com.sun.tools.corba.ee.idl.ModuleEntry container, com.sun.tools.corba.ee.idl.IDLID id);

    com.sun.tools.corba.ee.idl.ValueEntry valueEntry();

    com.sun.tools.corba.ee.idl.ValueEntry valueEntry(com.sun.tools.corba.ee.idl.ModuleEntry container, com.sun.tools.corba.ee.idl.IDLID id);

    com.sun.tools.corba.ee.idl.ValueBoxEntry valueBoxEntry();

    com.sun.tools.corba.ee.idl.ValueBoxEntry valueBoxEntry(com.sun.tools.corba.ee.idl.ModuleEntry container, com.sun.tools.corba.ee.idl.IDLID id);

    com.sun.tools.corba.ee.idl.MethodEntry methodEntry();

    com.sun.tools.corba.ee.idl.MethodEntry methodEntry(com.sun.tools.corba.ee.idl.InterfaceEntry container, com.sun.tools.corba.ee.idl.IDLID id);

    com.sun.tools.corba.ee.idl.ModuleEntry moduleEntry();

    com.sun.tools.corba.ee.idl.ModuleEntry moduleEntry(com.sun.tools.corba.ee.idl.ModuleEntry container, com.sun.tools.corba.ee.idl.IDLID id);

    com.sun.tools.corba.ee.idl.ParameterEntry parameterEntry();

    com.sun.tools.corba.ee.idl.ParameterEntry parameterEntry(com.sun.tools.corba.ee.idl.MethodEntry container, com.sun.tools.corba.ee.idl.IDLID id);

    com.sun.tools.corba.ee.idl.PragmaEntry pragmaEntry();

    com.sun.tools.corba.ee.idl.PragmaEntry pragmaEntry(com.sun.tools.corba.ee.idl.SymtabEntry container);

    com.sun.tools.corba.ee.idl.PrimitiveEntry primitiveEntry();

    /**
     * name can be, but is not limited to, the primitive idl type names: char, octet, short, long, etc. The reason it is not
     * limited to these is that, as an extender, you may wish to override these names. For instance, when generating Java
     * code, octet translates to byte, so there is an entry in Compile.overrideNames: &lt;"octet", "byte"&gt; and a
     * PrimitiveEntry in the symbol table for "byte".
     */
    com.sun.tools.corba.ee.idl.PrimitiveEntry primitiveEntry(String name);

    com.sun.tools.corba.ee.idl.SequenceEntry sequenceEntry();

    com.sun.tools.corba.ee.idl.SequenceEntry sequenceEntry(com.sun.tools.corba.ee.idl.SymtabEntry container, com.sun.tools.corba.ee.idl.IDLID id);

    com.sun.tools.corba.ee.idl.StringEntry stringEntry();

    com.sun.tools.corba.ee.idl.StructEntry structEntry();

    com.sun.tools.corba.ee.idl.StructEntry structEntry(com.sun.tools.corba.ee.idl.SymtabEntry container, com.sun.tools.corba.ee.idl.IDLID id);

    com.sun.tools.corba.ee.idl.TypedefEntry typedefEntry();

    com.sun.tools.corba.ee.idl.TypedefEntry typedefEntry(com.sun.tools.corba.ee.idl.SymtabEntry container, com.sun.tools.corba.ee.idl.IDLID id);

    com.sun.tools.corba.ee.idl.UnionEntry unionEntry();

    com.sun.tools.corba.ee.idl.UnionEntry unionEntry(com.sun.tools.corba.ee.idl.SymtabEntry container, com.sun.tools.corba.ee.idl.IDLID id);
} // interface SymtabFactory
