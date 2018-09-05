/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
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
