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

package com.sun.corba.ee.impl.presentation.rmi ;

import com.sun.corba.ee.impl.ior.StubIORImpl ;
import com.sun.corba.ee.impl.util.JDKBridge ;
import com.sun.corba.ee.impl.util.RepositoryId ;
import com.sun.corba.ee.spi.presentation.rmi.DynamicStub ;
import com.sun.corba.ee.spi.presentation.rmi.PresentationManager ;

import java.io.IOException ;
import java.io.ObjectInputStream ;
import java.io.ObjectOutputStream ;
import java.io.Serializable ;
import java.rmi.RemoteException ;

import org.omg.CORBA.ORB ;
import org.omg.CORBA.portable.Delegate ;
import org.omg.CORBA.portable.OutputStream ;
import org.omg.CORBA_2_3.portable.ObjectImpl ;

public class DynamicStubImpl extends ObjectImpl 
    implements DynamicStub, Serializable
{
    private static final long serialVersionUID = 4852612040012087675L;

    private String[] typeIds ;
    private StubIORImpl ior ;
    private DynamicStub self = null ;  // The actual DynamicProxy for this stub.

    public void setSelf( DynamicStub self ) 
    {
        this.self = self ;
    }

    public DynamicStub getSelf()
    {
        return self ;
    }

    public DynamicStubImpl( String[] typeIds ) 
    {
        this.typeIds = typeIds ;
        ior = null ;
    }

    public void setDelegate( Delegate delegate ) 
    {
        _set_delegate( delegate ) ;
    }

    public Delegate getDelegate() 
    {
        return _get_delegate() ;
    }

    public ORB getORB()
    {
        return _orb() ;
    }

    public String[] _ids() 
    {
        return typeIds.clone() ;
    }

    public String[] getTypeIds() 
    {
        return _ids() ;
    }

    public void connect( ORB orb ) throws RemoteException 
    {
        ior = StubConnectImpl.connect( ior, self, this, orb ) ;
    }

    public boolean isLocal()
    {
        return _is_local() ;
    }

    public OutputStream request( String operation, 
        boolean responseExpected ) 
    {
        return _request( operation, responseExpected ) ; 
    }
    
    private void readObject( ObjectInputStream stream ) throws 
        IOException, ClassNotFoundException
    {
        ior = new StubIORImpl() ;
        ior.doRead( stream ) ;
    }

    private void writeObject( ObjectOutputStream stream ) throws
        IOException
    {
        if (ior == null) {
            ior = new StubIORImpl(this);
        }
        ior.doWrite( stream ) ;
    }

    public Object readResolve()
    {
        String repositoryId = ior.getRepositoryId() ;
        String cname = RepositoryId.cache.getId( repositoryId ).getClassName() ; 

        Class<?> cls = null ;

        try {
            cls = JDKBridge.loadClass( cname, null, null ) ;
        } catch (ClassNotFoundException exc) {
            Exceptions.self.readResolveClassNotFound( exc, cname ) ;
        }

        PresentationManager pm = 
            com.sun.corba.ee.spi.orb.ORB.getPresentationManager() ;
        PresentationManager.ClassData classData = pm.getClassData( cls ) ;
        InvocationHandlerFactoryImpl ihfactory = 
            (InvocationHandlerFactoryImpl)classData.getInvocationHandlerFactory() ;
        return ihfactory.getInvocationHandler( this ) ;
    }
}
