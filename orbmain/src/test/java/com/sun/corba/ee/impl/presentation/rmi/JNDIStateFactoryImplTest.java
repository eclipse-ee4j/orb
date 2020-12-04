/*
 * Copyright (c) 2017, 2020 Oracle and/or its affiliates.
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

package com.sun.corba.ee.impl.presentation.rmi;

import com.meterware.simplestub.Memento;
import com.meterware.simplestub.StaticStubSupport;
import com.meterware.simplestub.Stub;
import com.sun.corba.ee.spi.orb.ORB;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.omg.CosNaming.NamingContext;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.rmi.CORBA.PortableRemoteObjectDelegate;
import javax.rmi.PortableRemoteObject;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import static com.meterware.simplestub.Stub.createStrictStub;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class JNDIStateFactoryImplTest {

    private JNDIStateFactoryImpl impl = new JNDIStateFactoryImpl();
    private final Remote remote = createStrictStub(Remote.class);
    private Hashtable<String,Object> env = new Hashtable<>();
    private ORB orb = createStrictStub(JndiOrb.class);

    private List<Memento> mementos = new ArrayList<>();
    private Context context;
    private CorbaStub corbaStub;

    @After
    public void tearDown() throws Exception {
        for (Memento memento : mementos) memento.revert();
    }

    @Before
    public void setUp() throws Exception {
        context = createContextWithOrb();
        corbaStub = createStrictStub(CorbaStub.class);
    }

    abstract static class OrbContext implements Context {
        private ORB _orb;

        OrbContext(ORB _orb) {
            this._orb = _orb;
        }
    }

    private Context createContextWithOrb() throws NamingException {
        return Stub.createStrictStub(OrbContext.class, orb);
    }

    @Test
    public void whenObjectIsCorbaObject_returnIt() throws Exception {
        Object testObject = createStrictStub(org.omg.CORBA.Object.class);

        assertThat(impl.getStateToBind(testObject, null, null, env), sameInstance(testObject));
    }

    @Test
    public void whenObjectIsNotRemote_returnNull() throws Exception {
        assertThat(impl.getStateToBind(new Object(), null, null, env), nullValue());
    }

    @Test
    public void whenCannotGetOrbFromContext_returnNull() throws Exception {
        assertThat(impl.getStateToBind(remote, null, createStrictStub(Context.class), env), nullValue());
    }

    @Test
    public void whenObjectIsNotAStub_returnNull() throws Exception {
        installDelegate(createStrictStub(NoStubDelegate.class));

        assertThat(impl.getStateToBind(remote, null, context, env), nullValue());
    }

    private void installDelegate(PortableRemoteObjectDelegate delegate) throws NoSuchFieldException {
        mementos.add(StaticStubSupport.install(PortableRemoteObject.class, "proDelegate", delegate));
    }

    abstract static class NoStubDelegate implements PortableRemoteObjectDelegate {
        @Override
        public Remote toStub(Remote obj) throws NoSuchObjectException {
            return null;
        }
    }

    @Test
    public void whenObjectIsAStub_returnIt() throws Exception {
        installDelegateForStub(corbaStub);

        Object state = impl.getStateToBind(remote, null, context, env);
        assertThat(state, sameInstance(corbaStub));
    }

    private void installDelegateForStub(CorbaStub stub) throws NoSuchFieldException {
        installDelegate(createStrictStub(StubPRODelegate.class, stub));
    }

    @Test
    public void whenObjectIsAStub_connectIt() throws Exception {
        installDelegateForStub(corbaStub);

        impl.getStateToBind(remote, null, context, env);
        assertThat(corbaStub.connected, is(true));
    }

    abstract static class StubPRODelegate implements PortableRemoteObjectDelegate {
        Remote stub;

        public StubPRODelegate(Remote stub) {
            this.stub = stub;
        }

        @Override
        public Remote toStub(Remote obj) throws NoSuchObjectException {
            return stub;
        }
    }

    abstract static class CorbaStub extends javax.rmi.CORBA.Stub implements Remote {
        private boolean connected = false;

        @Override
        public void connect(org.omg.CORBA.ORB orb) throws RemoteException {
            connected = true;
        }
    }

    abstract static class JndiOrb extends ORB {
        @Override
        public org.omg.CORBA.Object string_to_object(String str) {
            return createStrictStub(NamingContext.class);
        }
    }
}
