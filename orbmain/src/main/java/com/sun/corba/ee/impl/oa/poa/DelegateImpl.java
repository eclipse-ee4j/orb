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

package com.sun.corba.ee.impl.oa.poa;

import com.sun.corba.ee.spi.logging.POASystemException ;
import com.sun.corba.ee.spi.orb.ORB ;

import java.util.EmptyStackException;

import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;

public class DelegateImpl implements org.omg.PortableServer.portable.Delegate
{
    private static final POASystemException wrapper =
        POASystemException.self ;

    private ORB orb ;
    private POAFactory factory;

    public DelegateImpl(ORB orb, POAFactory factory){
        this.orb = orb ;
        this.factory = factory;
    }

    public org.omg.CORBA.ORB orb(Servant self)
    {
        return orb;
    }

    public org.omg.CORBA.Object this_object(Servant self)
    {
        byte[] oid;
        POA poa;
        try {
            oid = orb.peekInvocationInfo().id();
            poa = (POA)orb.peekInvocationInfo().oa();
            String repId = self._all_interfaces(poa,oid)[0] ;
            return poa.create_reference_with_id(oid, repId); 
        } catch (EmptyStackException notInInvocationE) { 
            //Not within an invocation context
            POAImpl defaultPOA = null;
            try {
                defaultPOA = (POAImpl)self._default_POA();
            } catch (ClassCastException exception){
                throw wrapper.defaultPoaNotPoaimpl( exception ) ;
            }

            try {
                if (defaultPOA.getPolicies().isImplicitlyActivated() ||
                    (defaultPOA.getPolicies().isUniqueIds() && 
                     defaultPOA.getPolicies().retainServants())) {
                    return defaultPOA.servant_to_reference(self);
                } else {
                    throw wrapper.wrongPoliciesForThisObject() ;
                }    
            } catch ( org.omg.PortableServer.POAPackage.ServantNotActive e) {
                throw wrapper.thisObjectServantNotActive( e ) ;
            } catch ( org.omg.PortableServer.POAPackage.WrongPolicy e) {
                throw wrapper.thisObjectWrongPolicy( e ) ;
            }
        } catch (ClassCastException e) {
            throw wrapper.defaultPoaNotPoaimpl( e ) ;
        }
    }

    public POA poa(Servant self)
    {
        try {
            return (POA)orb.peekInvocationInfo().oa();
        } catch (EmptyStackException exception){
            POA returnValue = factory.lookupPOA(self);
            if (returnValue != null) {
                return returnValue;
            }
            
            throw wrapper.noContext( exception ) ;
        }
    }

    public byte[] object_id(Servant self)
    {
        try{
            return orb.peekInvocationInfo().id();
        } catch (EmptyStackException exception){
            throw wrapper.noContext(exception) ;
        }
    }

    public POA default_POA(Servant self)
    {
        return factory.getRootPOA();
    }

    public boolean is_a(Servant self, String repId)
    {
        String[] repositoryIds = self._all_interfaces(poa(self),object_id(self));
        for ( int i=0; i<repositoryIds.length; i++ ) {
            if (repId.equals(repositoryIds[i])) {
                return true;
            }
        }

        return false;
    }

    public boolean non_existent(Servant self)
    {
        //REVISIT
        try{
            byte[] oid = orb.peekInvocationInfo().id();
            return oid == null ;
        } catch (EmptyStackException exception){
            throw wrapper.noContext(exception) ;
        }
    }

    // The get_interface() method has been replaced by get_interface_def()

    public org.omg.CORBA.Object get_interface_def(Servant Self)
    {
        throw wrapper.methodNotImplemented() ;
    }
}
