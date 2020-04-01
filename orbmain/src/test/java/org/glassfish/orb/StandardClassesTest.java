/*
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.orb;

import java.lang.reflect.Modifier;

import org.hamcrest.Description;
import org.junit.Test;

import static org.glassfish.orb.StandardClassesTest.StandardClassMatcher.isConcreteImplementationOf;
import static org.hamcrest.MatcherAssert.assertThat;


public class StandardClassesTest {
/*
| `javax.rmi.CORBA.UtilClass` | `com.sun.corba.ee.impl.javax.rmi.CORBA.Util` | `org.glassfish.corba.orb.impl.javax.rmi.CORBA.Util` |
| `javax.rmi.CORBA.StubClass` | `com.sun.corba.ee.impl.javax.rmi.CORBA.StubDelegateImpl` | `org.glassfish.corba.orb.impl.javax.rmi.CORBA.StubDelegateImpl` |
| `javax.rmi.CORBA.PortableRemoteObjectClass` | `com.sun.corba.ee.impl.javax.rmi.CORBA.PortableRemoteObject` | `org.glassfish.corba.orb.impl.javax.rmi.CORBA.PortableRemoteObject` |
| `org.omg.CORBA.ORBClass` | `com.sun.corba.ee.impl.orb.ORBImpl` | `org.glassfish.corba.orb.impl.orb.ORBImpl` |
| `org.omg.CORBA.ORBSingletonClass` | `com.sun.corba.ee.impl.orb.ORBSingleton` | `org.glassfish.corba.orb.impl.orb.ORBSingleton` |

 */

  private final static String LEGACY_UTIL_CLASS = "com.sun.corba.ee.impl.javax.rmi.CORBA.Util";
  private final static String LEGACY_STUB_CLASS = "com.sun.corba.ee.impl.javax.rmi.CORBA.StubDelegateImpl";
  private final static String LEGACY_PRO_CLASS = "com.sun.corba.ee.impl.javax.rmi.PortableRemoteObject";
  private final static String LEGACY_ORB_CLASS = "com.sun.corba.ee.impl.orb.ORBImpl";
  private final static String LEGACY_SINGLETON = "com.sun.corba.ee.impl.orb.ORBSingleton";

  @Test
  public void orbSupportsLegacyUtilClass() {
    assertThat(LEGACY_UTIL_CLASS, isConcreteImplementationOf(javax.rmi.CORBA.UtilDelegate.class));
  }

  @Test
  public void orbSupportsLegacyStubClass() {
    assertThat(LEGACY_STUB_CLASS, isConcreteImplementationOf(javax.rmi.CORBA.StubDelegate.class));
  }

  @Test
  public void orbSupportsLegacyPortableRemoteObjectClass() {
    assertThat(LEGACY_PRO_CLASS, isConcreteImplementationOf(javax.rmi.CORBA.PortableRemoteObjectDelegate.class));
  }

  @Test
  public void orbSupportsLegacyORBClass() {
    assertThat(LEGACY_ORB_CLASS, isConcreteImplementationOf(org.omg.CORBA_2_3.ORB.class));
  }

  @Test
  public void orbSupportsLegacyORBSingletonClass() {
    assertThat(LEGACY_SINGLETON, isConcreteImplementationOf(org.omg.CORBA_2_3.ORB.class));
  }

  static class StandardClassMatcher extends org.hamcrest.TypeSafeDiagnosingMatcher<String> {
    private Class<?> requiredInterface;

    private StandardClassMatcher(Class<?> requiredInterface) {
      this.requiredInterface = requiredInterface;
    }

    public static StandardClassMatcher isConcreteImplementationOf(Class<?> requiredInterface) {
      return new StandardClassMatcher(requiredInterface);
    }

    @Override
    protected boolean matchesSafely(String className, Description description) {
      try {
        Class<?> theClass = Class.forName(className);
        return isConcrete(theClass, description) && hasRequiredInterface(theClass, description);
      } catch (ClassNotFoundException e) {
        description.appendText("is undefined class");
        return false;
      }
    }

    private boolean isConcrete(Class<?> theClass, Description description) {
      if (!Modifier.isAbstract(theClass.getModifiers())) return true;
      description.appendText("is abstract class");
      return false;
    }

    private boolean hasRequiredInterface(Class<?> theClass, Description description) {
      if (requiredInterface.isAssignableFrom(theClass)) return true;
      description.appendText("does not implement ").appendText(requiredInterface.getName());
      return false;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("concrete implementation of ").appendText(requiredInterface.getName());
    }
  }
}
