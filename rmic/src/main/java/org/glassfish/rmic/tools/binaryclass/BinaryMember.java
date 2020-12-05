/*
 * Copyright (c) 1994, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.rmic.tools.binaryclass;

import org.glassfish.rmic.tools.java.ClassDeclaration;
import org.glassfish.rmic.tools.java.ClassDefinition;
import org.glassfish.rmic.tools.java.ClassNotFound;
import org.glassfish.rmic.tools.java.CompilerError;
import org.glassfish.rmic.tools.java.Environment;
import org.glassfish.rmic.tools.java.Identifier;
import org.glassfish.rmic.tools.java.MemberDefinition;
import org.glassfish.rmic.tools.java.Type;
import org.glassfish.rmic.tools.tree.*;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Vector;

/**
 * This class represents a binary member
 *
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 */
public final
class BinaryMember extends MemberDefinition {
    Expression value;
    BinaryAttribute atts;

    /**
     * Constructor
     */
    public BinaryMember(ClassDefinition clazz, int modifiers, Type type,
                        Identifier name, BinaryAttribute atts) {
        super(0, clazz, modifiers, type, name, null, null);
        this.atts = atts;

        // Was it compiled as deprecated?
        if (getAttribute(idDeprecated) != null) {
            this.modifiers |= M_DEPRECATED;
        }

        // Was it synthesized by the compiler?
        if (getAttribute(idSynthetic) != null) {
            this.modifiers |= M_SYNTHETIC;
        }
    }

    /**
     * Constructor for an inner class.
     */
    public BinaryMember(ClassDefinition innerClass) {
        super(innerClass);
    }

    /**
     * Inline allowed (currently only allowed for the constructor of Object).
     */
    public boolean isInlineable(Environment env, boolean fromFinal) {
        // It is possible for 'getSuperClass()' to return null due to error
        // recovery from cyclic inheritace.  Can this cause a problem here?
        return isConstructor() && (getClassDefinition().getSuperClass() == null);
    }

    /**
     * Get arguments
     */
    public Vector<MemberDefinition> getArguments() {
        if (isConstructor() && (getClassDefinition().getSuperClass() == null)) {
            Vector<MemberDefinition> v = new Vector<>();
            v.addElement(new LocalMember(0, getClassDefinition(), 0,
                                        getClassDefinition().getType(), idThis));
            return v;
        }
        return null;
    }

    /**
     * Get exceptions
     */
    public ClassDeclaration[] getExceptions(Environment env) {
        if ((!isMethod()) || (exp != null)) {
            return exp;
        }
        byte data[] = getAttribute(idExceptions);
        if (data == null) {
            return new ClassDeclaration[0];
        }

        try {
            BinaryConstantPool cpool = ((BinaryClass)getClassDefinition()).getConstants();
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));
            // JVM 4.7.5 Exceptions_attribute.number_of_exceptions
            int n = in.readUnsignedShort();
            exp = new ClassDeclaration[n];
            for (int i = 0 ; i < n ; i++) {
                // JVM 4.7.5 Exceptions_attribute.exception_index_table[]
                exp[i] = cpool.getDeclaration(env, in.readUnsignedShort());
            }
            return exp;
        } catch (IOException e) {
            throw new CompilerError(e);
        }
    }

    /**
     * Get documentation
     */
    public String getDocumentation() {
        if (documentation != null) {
            return documentation;
        }
        byte data[] = getAttribute(idDocumentation);
        if (data == null) {
            return null;
        }
        try {
            return documentation = new DataInputStream(new ByteArrayInputStream(data)).readUTF();
        } catch (IOException e) {
            throw new CompilerError(e);
        }
    }

    /**
     * Check if constant:  Will it inline away to a constant?
     * This override is needed to solve bug 4128266.  It is also
     * integral to the solution of 4119776.
     */
    private boolean isConstantCache = false;
    private boolean isConstantCached = false;
    public boolean isConstant() {
        if (!isConstantCached) {
            isConstantCache = isFinal()
                              && isVariable()
                              && getAttribute(idConstantValue) != null;
            isConstantCached = true;
        }
        return isConstantCache;
    }

    @Override
    public String getMemberValueString(Environment env) throws ClassNotFound {
        String value = null;

        // Prod it to setValue if it is a constant...

        getValue(env);

        // Get the value, if any...

        Node node = getValue();

        if (node != null) {
            // We don't want to change the code in CharExpression,
            // which is shared among tools, to return the right string
            // in case the type is char, so we treat it special here.
            if (getType().getTypeCode() == TC_CHAR) {
                Integer intValue = (Integer)((IntegerExpression)node).getValue();
                value = "L'" + String.valueOf((char)intValue.intValue()) + "'";
            } else {
                value = node.toString();
            }
        }
        return value;
    }


    /**
     * Get the value
     */
    public Node getValue(Environment env) {
        if (isMethod()) {
            return null;
        }
        if (!isFinal()) {
            return null;
        }
        if (getValue() != null) {
            return (Expression)getValue();
        }
        byte data[] = getAttribute(idConstantValue);
        if (data == null) {
            return null;
        }

        try {
            BinaryConstantPool cpool = ((BinaryClass)getClassDefinition()).getConstants();
            // JVM 4.7.3 ConstantValue.constantvalue_index
            Object obj = cpool.getValue(new DataInputStream(new ByteArrayInputStream(data)).readUnsignedShort());
            switch (getType().getTypeCode()) {
              case TC_BOOLEAN:
                setValue(new BooleanExpression(0, ((Number)obj).intValue() != 0));
                break;
              case TC_BYTE:
              case TC_SHORT:
              case TC_CHAR:
              case TC_INT:
                setValue(new IntExpression(0, ((Number)obj).intValue()));
                break;
              case TC_LONG:
                setValue(new LongExpression(0, ((Number)obj).longValue()));
                break;
              case TC_FLOAT:
                setValue(new FloatExpression(0, ((Number)obj).floatValue()));
                break;
              case TC_DOUBLE:
                setValue(new DoubleExpression(0, ((Number)obj).doubleValue()));
                break;
              case TC_CLASS:
                setValue(new StringExpression(0, (String)cpool.getValue(((Number)obj).intValue())));
                break;
            }
            return (Expression)getValue();
        } catch (IOException e) {
            throw new CompilerError(e);
        }
    }

    /**
     * Get a field attribute
     */
    public byte[] getAttribute(Identifier name) {
        for (BinaryAttribute att = atts ; att != null ; att = att.next) {
            if (att.name.equals(name)) {
                return att.data;
            }
        }
        return null;
    }

    public boolean deleteAttribute(Identifier name) {
        BinaryAttribute walker = null, next = null;

        boolean succeed = false;

        while (atts.name.equals(name)) {
            atts = atts.next;
            succeed = true;
        }
        for (walker = atts; walker != null; walker = next) {
            next = walker.next;
            if (next != null) {
                if (next.name.equals(name)) {
                    walker.next = next.next;
                    next = next.next;
                    succeed = true;
                }
            }
        }
        for (walker = atts; walker != null; walker = walker.next) {
            if (walker.name.equals(name)) {
                throw new InternalError("Found attribute " + name);
            }
        }

        return succeed;
    }



    /*
     * Add an attribute to a field
     */
    public void addAttribute(Identifier name, byte data[], Environment env) {
        this.atts = new BinaryAttribute(name, data, this.atts);
        // Make sure that the new attribute is in the constant pool
        ((BinaryClass)(this.clazz)).cpool.indexString(name.toString(), env);
    }

}
