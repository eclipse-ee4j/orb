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

//
// Created       : 2005 Oct 05 (Wed) 13:50:17 by Harold Carr.
// Last Modified : 2005 Oct 05 (Wed) 15:17:44 by Harold Carr.
//

package corba.giopgen;

import java.rmi.RemoteException;

import java.util.Map ;
import java.util.HashMap ;

import java.io.Serializable ;

import javax.rmi.PortableRemoteObject ;

import com.sun.corba.ee.spi.logging.UtilSystemException ;
import org.glassfish.pfl.basic.contain.SPair;

public class TestServant
    extends PortableRemoteObject
    implements Test
{
    public static final String baseMsg = TestServant.class.getName();

    public TestServant()
        throws RemoteException
    {
    }

    public int echo(int x, float y, short[] z, String str, Map m )
        throws RemoteException
    {
        System.out.println(baseMsg + ".echo: " + x);
        return x;
    }

    private static UtilSystemException wrapper =
        UtilSystemException.self ;
    
    private static class ThrowsSysEx implements Serializable {
        private void readObject( java.io.ObjectInputStream is ) {
            throw wrapper.testException( 42 ) ;
        }
    }

    private static class ThrowsSimpleSysEx implements Serializable {
        private void readObject( java.io.ObjectInputStream is ) {
            throw wrapper.simpleTestException( new Exception() ) ;
        }
    }

    private static class Foo implements Serializable {
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

    public Object testExceptionContext() throws RemoteException {
        Object d1 = new SPair<String,String>( "foo", "bar" ) ;
        Object d2 = new SPair<String,ThrowsSysEx>( "baz", new ThrowsSysEx() ) ;
        Foo f1 = new Foo( "d1", d1, "d2", d2 ) ;
        SPair<String,Foo> result = new SPair<String,Foo>( "f1", f1 ) ;
        return result ;
    }

    public Object testSimpleExceptionContext() throws RemoteException {
        Object d1 = new SPair<String,String>( "foo", "bar" ) ;
        Object d2 = new SPair<String,ThrowsSimpleSysEx>( "baz", new ThrowsSimpleSysEx() ) ;
        Foo f1 = new Foo( "d1", d1, "d2", d2 ) ;
        SPair<String,Foo> result = new SPair<String,Foo>( "f1", f1 ) ;
        return result ;
    }
}

// End of file.
