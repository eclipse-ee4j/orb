/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.rmic.asm;

import org.glassfish.rmic.classes.nestedClasses.TwoLevelNested;
import org.glassfish.rmic.tools.java.Identifier;
import org.glassfish.rmic.tools.java.Type;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.equalTo;

public class TypeFactoryTest {
    private TypeFactory factory = new TypeFactory();

    @Test
    public void constructNoArgVoidMethodType() throws Exception {
        Type methodType = TypeFactory.createMethodType("()V");

        assertThat(methodType.getReturnType(), equalTo(Type.tVoid));
        assertThat(methodType.getArgumentTypes(), emptyArray());
    }

    @Test
    public void constructByteArrayToIntType() throws Exception {
        Type methodType = TypeFactory.createMethodType("([B)I");

        assertThat(methodType.getReturnType(), equalTo(Type.tInt));
        assertThat(methodType.getArgumentTypes(), arrayContaining(Type.tArray(Type.tByte)));
    }

    @Test
    public void constructAllNumericArgsToBooleanMethod() throws Exception {
        Type methodType = TypeFactory.createMethodType("(SIJFD)Z");

        assertThat(methodType.getReturnType(), equalTo(Type.tBoolean));
        assertThat(methodType.getArgumentTypes(), arrayContaining(Type.tShort, Type.tInt, Type.tLong, Type.tFloat, Type.tDouble));
    }

    @Test
    public void constructAllObjectArguments() throws Exception {
        Type methodType = TypeFactory.createMethodType("(Ljava/lang/String;Lorg/glassfish/rmic/classes/nestedClasses/TwoLevelNested$Level1;)V");

        assertThat(methodType.getReturnType(), equalTo(Type.tVoid));
        assertThat(methodType.getArgumentTypes(), arrayContaining(Type.tString, Type.tClass(Identifier.lookup(TwoLevelNested.Level1.class.getName()))));
    }

    @Test
    public void constructObjectArrayArgument() throws Exception {
        Type methodType = TypeFactory.createMethodType("([Ljava/lang/Object;)V");

        assertThat(methodType.getReturnType(), equalTo(Type.tVoid));
        assertThat(methodType.getArgumentTypes(), arrayContaining(Type.tArray(Type.tObject)));
    }

    @Test
    public void constructCharArrayArgument() throws Exception {
        Type methodType = TypeFactory.createMethodType("([C)V");

        assertThat(methodType.getReturnType(), equalTo(Type.tVoid));
        assertThat(methodType.getArgumentTypes(), arrayContaining(Type.tArray(Type.tChar)));
    }

    @Test
    public void constructMultiDimensionalArrayType() throws Exception {
        assertThat(TypeFactory.createType("[[I"), equalTo(Type.tArray(Type.tArray(Type.tInt))));
    }
}
