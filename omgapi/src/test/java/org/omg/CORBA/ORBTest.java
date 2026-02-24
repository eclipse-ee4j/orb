/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.omg.CORBA;

import com.meterware.simplestub.Memento;
import com.meterware.simplestub.SystemPropertySupport;
import com.meterware.simplestub.ThreadContextClassLoaderSupport;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.meterware.simplestub.Stub.createStrictStub;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;

public class ORBTest {                                                                                      
  private static final String ORBClassKey = "org.omg.CORBA.ORBClass";
  private static final String ORBSingletonClassKey = "org.omg.CORBA.ORBSingletonClass";

  private TestClassLoader classLoader = new TestClassLoader();
  private String orbClassKey = createORBStubClassName();
  private List<Memento> mementos = new ArrayList<>();

  @Before
  public void setUp() {
    mementos.add(ThreadContextClassLoaderSupport.install(classLoader));
    mementos.add(SystemPropertySupport.install(ORBSingletonClassKey, createORBStubClassName()));
  }

  private String createORBStubClassName() {
    return createStrictStub(ORB.class).getClass().getName();
  }

  @After
  public void tearDown() {
    mementos.forEach(Memento::revert);
  }

  @Test
  public void singletonOrb_isCreatedOnlyOnce() {
    final ORB singleton = ORB.init();

    assertThat(ORB.init(), sameInstance(singleton));
  }

  @Test
  public void singletonOrb_isCreatedInContextClassLoader() {
    final ORB singleton = ORB.init();

    assertThat(classLoader.loadedSingleton, is(true));
  }

  // Simplestub always loads its stubs in the same classloader as the base class, so we cannot use the orb's
  // classloader to determine who loaded it. We therefore set a flag when the classloader is asked to load
  // the orb singleton class name.
  class TestClassLoader extends ClassLoader {
    private ClassLoader parent;
    private boolean loadedSingleton;

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
      if (orbClassKey.equals(name)) loadedSingleton = true;
      return super.loadClass(name);
    }
  }


}
