/*
 * Copyright (c) 2018, 2020 Oracle and/or its affiliates.
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
