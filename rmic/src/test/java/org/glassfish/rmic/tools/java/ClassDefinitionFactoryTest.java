/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.rmic.tools.java;

import com.meterware.simplestub.Memento;
import com.meterware.simplestub.StaticStubSupport;
import org.glassfish.rmic.BatchEnvironment;
import org.glassfish.rmic.Names;
import org.glassfish.rmic.TestUtils;
import org.glassfish.rmic.classes.covariantReturn.AnimalFinder;
import org.glassfish.rmic.classes.exceptiondetailsc.ExceptionSource;
import org.glassfish.rmic.classes.exceptiondetailsc.ExceptionSourceServantPOA;
import org.glassfish.rmic.classes.exceptiondetailsc.RmiIException;
import org.glassfish.rmic.classes.hcks.RmiII;
import org.glassfish.rmic.classes.hcks.RmiIIServantPOA;
import org.glassfish.rmic.classes.inneraccess.Rainbow;
import org.glassfish.rmic.classes.nestedClasses.TwoLevelNested;
import org.glassfish.rmic.classes.primitives.NonFinalInterface;
import org.glassfish.rmic.classes.primitives.RmiTestRemote;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.rmi.PortableRemoteObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import static org.glassfish.rmic.tools.java.ClassDefinitionFactoryTest.ClassDeclarationMatcher.declarationFor;
import static org.glassfish.rmic.tools.java.ClassDefinitionFactoryTest.MemberDefinitionMatcher.isDefinitionFor;
import static org.glassfish.rmic.tools.java.Constants.*;
import static org.glassfish.rmic.tools.java.RuntimeConstants.ACC_SUPER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public abstract class ClassDefinitionFactoryTest {
    private ByteArrayOutputStream messagesOut = new ByteArrayOutputStream();
    private Environment environment = new BatchEnvironment(messagesOut, createTestClassPath(), null);
    private ClassDefinitionFactory factory;
    private Memento memento;

    @Before
    public void setUp() throws Exception {
        memento = StaticStubSupport.install(BatchEnvironment.class, "classDefinitionFactory", factory);
        environment = new BatchEnvironment(messagesOut, createTestClassPath(), null);
    }

    @After
    public void tearDown() throws Exception {
        memento.revert();
    }

    protected ClassDefinitionFactoryTest(ClassDefinitionFactory factory) {
        this.factory = factory;
    }

    private ClassPath createTestClassPath() {
        return BatchEnvironment.createClassPath(TestUtils.getClassPathString(), null);
    }

    private ClassDefinition definitionFor(Class<?> aClass) throws IOException {
        InputStream classFileInputStream = getClass().getClassLoader().getResourceAsStream(toPath(aClass));
        return factory.loadDefinition(classFileInputStream, environment);
    }

    private String toPath(Class<?> aClass) {
        return aClass.getName().replace('.', File.separatorChar) + ".class";
    }

    private void loadNested(ClassDefinition classDefinition) {
        classDefinition.loadNested(environment);
    }

    // Name mangling is required to convert an internal class name identifier to an actual class name.
    private String getClassName(ClassDeclaration classDeclaration) {
        return Names.mangleClass(classDeclaration.getName()).toString();
    }

    @Test
    public void loadJavaLangObject() throws Exception {
        definitionFor(Object.class);
    }

    @Test
    public void classDefinition_hasDeclarationWithClassName() throws Exception {
        ClassDefinition classDefinition = definitionFor(RmiII.class);

        assertThat(getClassName(classDefinition.getClassDeclaration()), equalTo(RmiII.class.getName()));
    }

    @Test
    public void classDefinitionForInnerClass_hasDeclarationWithClassName() throws Exception {
        ClassDefinition classDefinition = definitionFor(Rainbow.getInterfaceCheckerClass());

        assertThat(getClassName(classDefinition.getClassDeclaration()), equalTo(Rainbow.getInterfaceCheckerClass().getName()));
    }

    @Test
    public void classDefinition_hasSuperclass() throws Exception {
        ClassDefinition classDefinition = definitionFor(ExceptionSourceServantPOA.class);

        assertThat(getClassName(classDefinition.getSuperClass()), equalTo(PortableRemoteObject.class.getName()));
    }

    @Test
    public void classDefinition_hasInterfaces() throws Exception {
        ClassDefinition classDefinition = definitionFor(TwoLevelNested.Level1.Level2.class);

        assertThat(getClassNames(classDefinition.getInterfaces()),
                   arrayContainingInAnyOrder(Remote.class.getName(), Cloneable.class.getName()));
    }

    private String[] getClassNames(ClassDeclaration[] declarations) {
        String[] result = new String[declarations.length];
        for (int i = 0; i < declarations.length; i++)
            result[i] = getClassName(declarations[i]);
        return result;
    }

    @Test
    public void classDefinitionForCompiledClass_hasSourceName() throws Exception {
        ClassDefinition classDefinition = definitionFor(RmiII.class);

        assertThat(classDefinition.getSource(), equalTo("RmiII.java"));
    }

    @Test
    public void classDefinitionForTopLevelClass_hasSourceName() throws Exception {
        ClassDefinition classDefinition = definitionFor(Class.forName("TopLevelClass"));

        assertThat(classDefinition.getSource(), equalTo("TopLevelClass.java"));
    }

    @Test
    public void classDefinitionForInnerClass_hasSourceName() throws Exception {
        ClassDefinition classDefinition = definitionFor(TwoLevelNested.Level1.Level2.class);

        assertThat(classDefinition.getSource(), equalTo("TwoLevelNested.java"));
    }

    @Test
    public void classDefinitionForCompiledClass_hasNoError() throws Exception {
        ClassDefinition classDefinition = definitionFor(RmiII.class);

        assertThat(classDefinition.getError(), is(false));
    }

    @Test
    public void classDefinitionForCompiledClass_hasZeroWhereValue() throws Exception {
        ClassDefinition classDefinition = definitionFor(RmiII.class);

        assertThat(classDefinition.getWhere(), equalTo(0L));
    }

    @Test
    public void classDefinitionForRmiII_hasExpectedModifiers() throws Exception {
        ClassDefinition classDefinition = definitionFor(RmiII.class);

        assertThat(classDefinition.getModifiers(), equalTo(M_ABSTRACT | M_INTERFACE | M_PUBLIC));
    }

    @Test
    public void classDefinitionForRmiIIServantPOA_hasExpectedModifiers() throws Exception {
        ClassDefinition classDefinition = definitionFor(RmiIIServantPOA.class);

        assertThat(classDefinition.getModifiers(), equalTo(ACC_SUPER | M_PUBLIC));
    }

    @Test
    public void afterLoadNested_topClassOfInnerClassMatchesOuterClass() throws Exception {
        ClassDefinition inner = definitionFor(Rainbow.getInterfaceCheckerClass());
        loadNested(inner);

        assertThat(inner.getTopClass().getName(), equalTo(definitionFor(Rainbow.class).getName()));
    }

    @Test
    public void afterLoadNested_topClassOfInner2LevelNestedClassMatchesOuterClass() throws Exception {
        ClassDefinition inner = definitionFor(TwoLevelNested.Level1.Level2.class);
        loadNested(inner);

        assertThat(inner.getTopClass().getName(), equalTo(definitionFor(TwoLevelNested.class).getName()));
    }

    @Test
    public void classDefinitionForRmiIIServantPOA_hasExpectedMemberDefinitions() throws Exception {
        ClassDefinition classDefinition = definitionFor(RmiIIServantPOA.class);

        List<MemberDefinition> memberDefinitions = new ArrayList<>();
        for (MemberDefinition member = classDefinition.getFirstMember(); member != null; member = member.getNextMember())
            if (!isStaticInitializer(member)) memberDefinitions.add(member);

        assertThat(memberDefinitions, containsInAnyOrder(allMembers(RmiIIServantPOA.class)));
    }

    private boolean isStaticInitializer(MemberDefinition member) {
        return member.getName().toString().equals("<clinit>");
    }

    @Test
    public void classDefinitionForAnimalFinder_hasExpectedMemberDefinitions() throws Exception {
        ClassDefinition classDefinition = definitionFor(AnimalFinder.class);

        List<MemberDefinition> memberDefinitions = new ArrayList<>();
        for (MemberDefinition member = classDefinition.getFirstMember(); member != null; member = member.getNextMember())
            if (!isStaticInitializer(member)) memberDefinitions.add(member);

        assertThat(memberDefinitions, containsInAnyOrder(allMembers(AnimalFinder.class)));
    }

    @Test
    public void methodDefinition_hasThrownExceptions() throws Exception {
        ClassDefinition classDefinition = definitionFor(ExceptionSource.class);

        MemberDefinition method = classDefinition.findAnyMethod(environment, Identifier.lookup("raiseUserException"));
        assertThat(method.getExceptions(environment), arrayContainingInAnyOrder(declarationFor(RemoteException.class), declarationFor(RmiIException.class)));
    }

    // It appears that only the static initializer is allowed to return an empty array for this.
    @Test
    public void methodDefinitionGetArguments_returnsNull() throws Exception {
        ClassDefinition classDefinition = definitionFor(ExceptionSource.class);

        MemberDefinition method = classDefinition.findAnyMethod(environment, Identifier.lookup("raiseUserException"));
        assertThat(method.getArguments(), nullValue());
    }

    @SuppressWarnings("unchecked")
    private Matcher<MemberDefinition>[] allMembers(Class<?> aClass) {
        List<Matcher<MemberDefinition>> matchers = new ArrayList<>();
        for (Method method : aClass.getDeclaredMethods())
            matchers.add(isDefinitionFor(method));
        for (Field field : aClass.getDeclaredFields())
            matchers.add(isDefinitionFor(field));
        for (Constructor constructor : aClass.getDeclaredConstructors())
            matchers.add(isDefinitionFor(constructor));
        return matchers.toArray(new Matcher[matchers.size()]);
    }

    @Test
    public void verifyFinalMemberDefs() throws Exception {
//        Assume.assumeTrue(factory instanceof BinaryClassFactory);
        ClassDefinition classDefinition = definitionFor(RmiTestRemote.class);
        assertThat(getMember(classDefinition, "A_DOUBLE").getMemberValueString(environment), equalTo("123.567D"));
        assertThat(getMember(classDefinition, "A_FLOAT").getMemberValueString(environment), equalTo("123.5F"));
        assertThat(getMember(classDefinition, "A_LONG").getMemberValueString(environment), equalTo("1234567L"));
        assertThat(getMember(classDefinition, "AN_INT").getMemberValueString(environment), equalTo("17"));
        assertThat(getMember(classDefinition, "A_SHORT").getMemberValueString(environment), equalTo("12"));
        assertThat(getMember(classDefinition, "A_BYTE").getMemberValueString(environment), equalTo("52"));
        assertThat(getMember(classDefinition, "A_CHAR").getMemberValueString(environment), equalTo("L'x'"));
        assertThat(getMember(classDefinition, "A_BOOLEAN").getMemberValueString(environment), equalTo("true"));
        assertThat(getMember(classDefinition, "JNDI_NAME").getMemberValueString(environment), equalTo("\"IIOP_RmiTestRemote\""));
    }

    private MemberDefinition getMember(ClassDefinition classDefinition, String name) {
        for (MemberDefinition def = classDefinition.getFirstMember(); def != null; def = def.getNextMember()) {
            if (name.equals(def.getName().toString())) return def;
        }

        throw new AssertionError("No member named " + name + " found in class " + classDefinition.getName());
    }

    @Test
    public void verifyNonFinalMemberDefs() throws Exception {
        ClassDefinition classDefinition = definitionFor(NonFinalInterface.class);
        assertThat(getMember(classDefinition, "A_DOUBLE").getMemberValueString(environment), nullValue());
        assertThat(getMember(classDefinition, "A_FLOAT").getMemberValueString(environment), nullValue());
        assertThat(getMember(classDefinition, "A_LONG").getMemberValueString(environment), nullValue());
        assertThat(getMember(classDefinition, "AN_INT").getMemberValueString(environment), nullValue());
        assertThat(getMember(classDefinition, "A_SHORT").getMemberValueString(environment), nullValue());
        assertThat(getMember(classDefinition, "A_BYTE").getMemberValueString(environment), nullValue());
        assertThat(getMember(classDefinition, "A_CHAR").getMemberValueString(environment), nullValue());
        assertThat(getMember(classDefinition, "A_BOOLEAN").getMemberValueString(environment), nullValue());
        assertThat(getMember(classDefinition, "JNDI_NAME").getMemberValueString(environment), nullValue());
    }

    static class MemberDefinitionMatcher extends TypeSafeDiagnosingMatcher<MemberDefinition> {
        private AccessibleObject member;

        private MemberDefinitionMatcher(AccessibleObject member) {
            this.member = member;
        }

        static MemberDefinitionMatcher isDefinitionFor(AccessibleObject member) {
            return new MemberDefinitionMatcher(member);
        }

        @Override
        protected boolean matchesSafely(MemberDefinition memberDefinition, Description description) {
            if (!matches(memberDefinition)) {
                description.appendValue(memberDefinition.getName());
                return false;
            }
            return true;
        }

        private boolean matches(MemberDefinition memberDefinition) {
            if (member instanceof Constructor)
                return parameterTypesMatch(((Constructor) member).getParameterTypes(), memberDefinition.getType().getArgumentTypes());
            else if (member instanceof Method)
                return isSameMethod(memberDefinition, (Method) this.member);
            else
                return getName(member).equals(memberDefinition.getName().toString());
        }

        private boolean isSameMethod(MemberDefinition memberDefinition, Method method) {
            return method.getName().equals(memberDefinition.getName().toString()) &&
                    parameterTypeMatch(method.getReturnType(), memberDefinition.getType().getReturnType()) &&
                    parameterTypesMatch(method.getParameterTypes(), memberDefinition.getType().getArgumentTypes());
        }

        private boolean parameterTypesMatch(Class<?>[] parameterTypes, Type[] memberDefinition) {
            if (parameterTypes.length != memberDefinition.length) return false;

            for (int i = 0; i < parameterTypes.length; i++)
                if (!parameterTypeMatch(parameterTypes[i], memberDefinition[i])) return false;
            return true;
        }

        private boolean parameterTypeMatch(Class<?> parameterType, Type type) {
            return parameterType.getTypeName().equals(type.toString());
        }

        private String getName(AccessibleObject member) {
            if (member instanceof Method)
                return getMethodName((Method) member);
            else if (member instanceof Field)
                return ((Field) member).getName();
            else if (member instanceof Constructor)
                return getConstructorName((Constructor) member);
            else
                return "??";
        }

        private String getMethodName(Method member) {
            return toDisplayType(member.getReturnType()) + " " + member.getName() + toParameterString(member.getParameterTypes());
        }

        private String getConstructorName(Constructor member) {
            return toDisplayType(member.getDeclaringClass()) + toParameterString(member.getParameterTypes());
        }

        private String toParameterString(Class[] parameterTypes) {
            return "(" + String.join(", ", toStringList(parameterTypes)) + ")";
        }

        private List<String> toStringList(Class<?>[] parameterTypes) {
            List<String> list = new ArrayList<>();
            for (Class<?> parameterType : parameterTypes)
                list.add(toDisplayType(parameterType));
            return list;
        }

        private String toDisplayType(Class<?> parameterType) {
            return parameterType.getTypeName();
        }

        @Override
        public void describeTo(Description description) {
            description.appendValue(getName(member));
        }
    }

    static class ClassDeclarationMatcher extends TypeSafeDiagnosingMatcher<ClassDeclaration> {
        private Class<?> classToMatch;

        private ClassDeclarationMatcher(Class<?> classToMatch) {
            this.classToMatch = classToMatch;
        }

        static ClassDeclarationMatcher declarationFor(Class<?> classToMatch) {
            return new ClassDeclarationMatcher(classToMatch);
        }

        @Override
        protected boolean matchesSafely(ClassDeclaration item, Description mismatchDescription) {
            if (item.getName().toString().equals(classToMatch.getName())) return true;

            mismatchDescription.appendText("declaration for ").appendValue(item.getName());
            return false;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("declaration for ").appendValue(classToMatch.getName());
        }
    }
}
