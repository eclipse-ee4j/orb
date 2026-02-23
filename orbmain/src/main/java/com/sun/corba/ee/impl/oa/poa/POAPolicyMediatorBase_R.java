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

package com.sun.corba.ee.impl.oa.poa ;

import com.sun.corba.ee.spi.trace.Poa;

import org.glassfish.pfl.tf.spi.annotation.InfoMethod;
import org.omg.PortableServer.Servant ;
import org.omg.PortableServer.POAPackage.ObjectAlreadyActive ;
import org.omg.PortableServer.POAPackage.ObjectNotActive ;
import org.omg.PortableServer.POAPackage.ServantAlreadyActive ;
import org.omg.PortableServer.POAPackage.ServantNotActive ;
import org.omg.PortableServer.POAPackage.WrongPolicy ;

@Poa
public abstract class POAPolicyMediatorBase_R extends POAPolicyMediatorBase {
    protected ActiveObjectMap activeObjectMap ;
    
    POAPolicyMediatorBase_R( Policies policies, POAImpl poa ) 
    {
        super( policies, poa ) ;

        // assert policies.retainServants() && policies.useActiveObjectMapOnly()
        if (!policies.retainServants()) {
            throw wrapper.policyMediatorBadPolicyInFactory();
        }

        activeObjectMap = ActiveObjectMap.create(poa, !isUnique);
    }
    
    public void returnServant() 
    {
        // NO-OP
    }

    public void clearAOM() 
    {
        activeObjectMap.clear() ;
        activeObjectMap = null ;
    }

    protected Servant internalKeyToServant( ActiveObjectMap.Key key )
    {
        AOMEntry entry = activeObjectMap.get(key);
        if (entry == null) {
            return null;
        }

        return activeObjectMap.getServant( entry ) ;
    }

    protected Servant internalIdToServant( byte[] id )
    {
        ActiveObjectMap.Key key = new ActiveObjectMap.Key( id ) ;
        return internalKeyToServant( key ) ;
    }

    @Poa
    protected void activateServant( ActiveObjectMap.Key key, AOMEntry entry, Servant servant )
    {
        setDelegate(servant, key.id() );

        activeObjectMap.putServant( servant, entry ) ;

        POAManagerImpl pm = (POAManagerImpl)poa.the_POAManager() ;
        POAFactory factory = pm.getFactory() ;
        factory.registerPOAForServant(poa, servant);
    }

    @Poa
    public final void activateObject(byte[] id, Servant servant) 
        throws WrongPolicy, ServantAlreadyActive, ObjectAlreadyActive
    {
        if (isUnique && activeObjectMap.contains(servant)) {
            throw new ServantAlreadyActive();
        }
        ActiveObjectMap.Key key = new ActiveObjectMap.Key( id ) ;

        AOMEntry entry = activeObjectMap.get( key ) ;

        // Check for an ObjectAlreadyActive error
        entry.activateObject() ;

        activateServant( key, entry, servant ) ;
    }
    
    @Poa
    public Servant deactivateObject( byte[] id ) 
        throws ObjectNotActive, WrongPolicy 
    {
        ActiveObjectMap.Key key = new ActiveObjectMap.Key( id ) ;
        return deactivateObject( key ) ;
    }
    
    @Poa
    protected void deactivateHelper( ActiveObjectMap.Key key, AOMEntry entry, 
        Servant s ) throws ObjectNotActive, WrongPolicy
    {
        // Default does nothing, but the USE_SERVANT_MANAGER case
        // must handle etherealization

        activeObjectMap.remove(key);

        POAManagerImpl pm = (POAManagerImpl)poa.the_POAManager() ;
        POAFactory factory = pm.getFactory() ;
        factory.unregisterPOAForServant(poa, s);
    }

    @InfoMethod
    private void deactivatingObject( Servant s, POAImpl poa ) { }

    @Poa
    public Servant deactivateObject( ActiveObjectMap.Key key ) 
        throws ObjectNotActive, WrongPolicy {

        AOMEntry entry = activeObjectMap.get(key);
        if (entry == null) {
            throw new ObjectNotActive();
        }

        Servant s = activeObjectMap.getServant( entry ) ;
        if (s == null) {
            throw new ObjectNotActive();
        }

        deactivatingObject( s, poa ) ;

        deactivateHelper( key, entry, s ) ;

        return s ;
    }

    @Poa
    public byte[] servantToId( Servant servant ) throws ServantNotActive, WrongPolicy
    {   
        if (!isUnique && !isImplicit) {
            throw new WrongPolicy();
        }

        if (isUnique) {
            ActiveObjectMap.Key key = activeObjectMap.getKey(servant);
            if (key != null) {
                return key.id();
            }
        } 

        // assert !isUnique || (servant not in activateObjectMap)
        
        if (isImplicit) {
            try {
                byte[] id = newSystemId();
                activateObject(id, servant);
                return id;
            } catch (ObjectAlreadyActive oaa) {
                throw wrapper.servantToIdOaa(oaa);
            } catch (ServantAlreadyActive s) {
                throw wrapper.servantToIdSaa(s);
            } catch (WrongPolicy w) {
                throw wrapper.servantToIdWp(w);
            }
        }

        throw new ServantNotActive();
    }
}

