package org.omg.CORBA;

import java.util.ArrayList;
import java.util.List;

import com.meterware.simplestub.Memento;
import com.meterware.simplestub.SystemPropertySupport;
import com.meterware.simplestub.ThreadContextClassLoaderSupport;
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