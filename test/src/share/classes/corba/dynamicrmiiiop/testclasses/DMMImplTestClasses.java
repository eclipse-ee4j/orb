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

package corba.dynamicrmiiiop.testclasses ;

import java.rmi.Remote ;
import java.rmi.RemoteException ;

public interface DMMImplTestClasses 
{
    public interface AllRemote
    {
        AllRemote make( AllRemote arg ) throws RemoteException ;

        AllRemote make( String arg ) throws RemoteException ;
    }
    
    public interface SomeRemote
    {
        SomeRemote make( SomeRemote arg ) throws RemoteException ;

        SomeRemote make( String arg ) ;
    }
    
    public interface NoRemote
    {
        NoRemote make( NoRemote arg ) ;
    }

    public interface NoMethods
    {
    }

    public interface IDLSimpleInterface extends org.omg.CORBA.portable.IDLEntity
    {
        void noop ();
    }

    public interface IDLValue extends org.omg.CORBA.portable.ValueBase
    {
        public abstract void foo ();

        public abstract int bar ();

    } 

    public final class IDLStruct implements org.omg.CORBA.portable.IDLEntity
    {
        public int arg1 = (int)0;
        public int arg2 = (int)0;

        public IDLStruct ()
        {
        }

        public IDLStruct (int _arg1, int _arg2)
        {
            arg1 = _arg1;
            arg2 = _arg2;
        } 
    } 

    public interface IDLInterface extends org.omg.CORBA.Object, 
        org.omg.CORBA.portable.IDLEntity 
    {
        String name ();
        void name (String newName);
    } 
}
