/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
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

package corba.simpledynamic;

import java.util.Map ;
import java.util.HashMap ;

import java.rmi.Remote ;
import java.rmi.RemoteException ;
import javax.rmi.PortableRemoteObject ;

import com.sun.corba.ee.spi.orb.ORB ;

import com.sun.corba.ee.spi.logging.UtilSystemException ;

import corba.misc.BuckPasserAL  ;
import corba.misc.BuckPasserV  ;
import org.glassfish.pfl.basic.contain.Pair;

public class EchoImpl extends PortableRemoteObject implements Echo {
    private String name ;
    private static final UtilSystemException wrapper =
        UtilSystemException.self ;

    private static class ThrowsSysEx {
        private void readObject( java.io.ObjectInputStream is ) {
            throw wrapper.testException( 42 ) ;
        }
    }

    private static class Foo {
        private Map m ;

        public Foo( Object... args ) {
            m = new HashMap() ;
            boolean atKey = true ;
            Object key = null ;
            Object value = null ;
            for (Object obj : args) {
                if (atKey) {
                    key = obj ;
                } else {
                    value = obj ;
                    m.put( key, value ) ;
                }

                atKey = !atKey ;
            }
        }
    }

    public EchoImpl( String name ) throws RemoteException {
        this.name = name ;
    }

    public String sayHello( Object obj ) throws RemoteException {
        return "Hello " + obj ;
    }

    public Echo say( Echo echo ) {
        return echo ;
    }

    public String name() {
        return name ;
    }

    public Object testExceptionContext() throws RemoteException {
        Object d1 = new Pair<String,String>( "foo", "bar" ) ;
        Object d2 = new Pair<String,ThrowsSysEx>( "baz", new ThrowsSysEx() ) ;
        Foo f1 = new Foo( "d1", d1, "d2", d2 ) ;
        Pair<String,Foo> result = new Pair<String,Foo>( "f1", f1 ) ;
        return result ;
    }

    public int[] echo( int[] arg ) {
        return arg ;
    }

    public Object echo( Object arg ) {
        return arg ;
    }

    public BuckPasserAL echo( BuckPasserAL arg ) {
        return arg ;
    }

    public BuckPasserV echo( BuckPasserV arg ) {
        return arg ;
    }

    public BuckPasserVectorOriginal echo( BuckPasserVectorOriginal arg ) throws RemoteException {
        return arg ;
    }
}
