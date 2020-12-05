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

import java.util.Set ;

import org.omg.CORBA.SystemException ;

import org.omg.PortableServer.ServantActivator ;
import org.omg.PortableServer.Servant ;
import org.omg.PortableServer.ServantManager ;
import org.omg.PortableServer.ForwardRequest ;
import org.omg.PortableServer.POAPackage.WrongPolicy ;
import org.omg.PortableServer.POAPackage.ObjectNotActive ;
import org.omg.PortableServer.POAPackage.NoServant ;

import com.sun.corba.ee.impl.oa.NullServantImpl ;

import com.sun.corba.ee.spi.oa.OAInvocationInfo ;
import com.sun.corba.ee.spi.oa.NullServant ;
import com.sun.corba.ee.spi.trace.Poa;
import org.glassfish.pfl.tf.spi.annotation.InfoMethod;

/** Implementation of POARequesHandler that provides policy specific
 * operations on the POA.
 */
@Poa
public class POAPolicyMediatorImpl_R_USM extends POAPolicyMediatorBase_R {
    protected ServantActivator activator ;

    POAPolicyMediatorImpl_R_USM( Policies policies, POAImpl poa ) 
    {
        // assert policies.retainServants() 
        super( policies, poa ) ;
        activator = null ;

        if (!policies.useServantManager()) {
            throw wrapper.policyMediatorBadPolicyInFactory();
        }
    }
   
    /* This handles a rather subtle bug (4939892).  The problem is that
     * enter will wait on the entry if it is being etherealized.  When the
     * deferred state transition completes, the entry is no longer in the
     * AOM, and so we need to get a new entry, otherwise activator.incarnate
     * will be called twice, once for the old entry, and again when a new
     * entry is created.  This fix also required extending the FSM StateEngine
     * to allow actions to throw exceptions, and adding a new state in the
     * AOMEntry FSM to detect this condition.
     */
    private AOMEntry enterEntry( ActiveObjectMap.Key key )
    {
        AOMEntry result = null ;
        boolean failed ;
        do {
            failed = false ;
            result = activeObjectMap.get(key) ;

            try {
                result.enter() ;
            } catch (Exception exc) {
                failed = true ;
            }
        } while (failed) ;

        return result ;
    }

    @InfoMethod
    private void servantAlreadyActivated() { }

    @InfoMethod
    private void upcallToIncarnate() { }

    @InfoMethod
    private void incarnateFailed() { }

    @InfoMethod
    private void incarnateComplete() { }

    @InfoMethod
    private void servantAlreadyAssignedToID() { }

    @Poa
    protected java.lang.Object internalGetServant( byte[] id, 
        String operation ) throws ForwardRequest {

        poa.lock() ;
        try {
            ActiveObjectMap.Key key = new ActiveObjectMap.Key( id ) ;
            AOMEntry entry = enterEntry(key) ;
            java.lang.Object servant = activeObjectMap.getServant( entry ) ;
            if (servant != null) {
                servantAlreadyActivated() ;
                return servant ;
            }

            if (activator == null) {
                entry.incarnateFailure() ;
                throw wrapper.poaNoServantManager() ;
            }

            // Drop the POA lock during the incarnate call and
            // re-acquire it afterwards.  The entry state machine
            // prevents more than one thread from executing the
            // incarnate method at a time within the same POA.
            try {
                upcallToIncarnate() ;

                poa.unlock() ;

                servant = activator.incarnate(id, poa);

                if (servant == null) {
                    servant = new NullServantImpl(
                        omgWrapper.nullServantReturned());
                }
            } catch (ForwardRequest freq) {
                throw freq ;
            } catch (SystemException exc) {
                throw exc ;
            } catch (Throwable exc) {
                throw wrapper.poaServantActivatorLookupFailed( exc ) ;
            } finally {
                poa.lock() ;

                // servant == null means incarnate threw an exception,
                // while servant instanceof NullServant means incarnate returned a
                // null servant.  Either case is an incarnate failure to the
                // entry state machine.
                if ((servant == null) || (servant instanceof NullServant)) {
                    incarnateFailed() ;

                    // XXX Does the AOM leak in this case? Yes,
                    // but the problem is hard to fix.  There may be
                    // a number of threads waiting for the state to change
                    // from INCARN to something else, which is VALID or
                    // INVALID, depending on the incarnate result.
                    // The activeObjectMap.get() call above creates an
                    // ActiveObjectMap.Entry if one does not already exist,
                    // and stores it in the keyToEntry map in the AOM.
                    entry.incarnateFailure() ;
                } else {
                    // here check for unique_id policy, and if the servant
                    // is already registered for a different ID, then throw
                    // OBJ_ADAPTER exception, else activate it. Section 11.3.5.1
                    // 99-10-07.pdf
                    if (isUnique) {
                        // check if the servant already is associated with some id
                        if (activeObjectMap.contains((Servant)servant)) {
                            servantAlreadyAssignedToID() ;
                            entry.incarnateFailure() ;
                            throw wrapper.poaServantNotUnique() ;
                        }
                    }

                    incarnateComplete() ;

                    entry.incarnateComplete() ;
                    activateServant(key, entry, (Servant)servant);
                }
            }

            return servant ;
        } finally {
            poa.unlock() ;
        }
    }

    @Poa
    @Override
    public void returnServant() {
        poa.lock() ;
        try {
            OAInvocationInfo info = orb.peekInvocationInfo();
            // 6878245: added null check.
            if (info == null) {
                return ;
            }
            byte[] id = info.id() ;
            ActiveObjectMap.Key key = new ActiveObjectMap.Key( id ) ;
            AOMEntry entry = activeObjectMap.get( key ) ;
            entry.exit() ;
        } finally {
            poa.unlock();
        }
    }

    @Poa
    public void etherealizeAll() {      
        if (activator != null)  {
            Set<ActiveObjectMap.Key> keySet = activeObjectMap.keySet() ;

            // Copy the elements in the set to an array to avoid
            // changes in the set due to concurrent modification
            @SuppressWarnings("unchecked")
            ActiveObjectMap.Key[] keys = 
                keySet.toArray(new ActiveObjectMap.Key[keySet.size()]) ;

            for (int ctr=0; ctr<keySet.size(); ctr++) {
                ActiveObjectMap.Key key = keys[ctr] ;
                AOMEntry entry = activeObjectMap.get( key ) ;
                Servant servant = activeObjectMap.getServant( entry ) ;
                if (servant != null) {
                    boolean remainingActivations = 
                        activeObjectMap.hasMultipleIDs(entry) ;

                    // Here we etherealize in the thread that called this 
                    // method, rather than etherealizing in a new thread 
                    // as in the deactivate case.  We still inform the 
                    // entry state machine so that only one thread at a 
                    // time can call the etherealize method.
                    entry.startEtherealize( null ) ;
                    try {
                        poa.unlock() ;
                        try {
                            activator.etherealize(key.id(), poa, servant, true,
                                remainingActivations);
                        } catch (Exception exc) {
                            // ignore all exceptions
                        }
                    } finally {
                        poa.lock() ;
                        entry.etherealizeComplete() ;
                    }
                }
            }
        }
    }

    public ServantManager getServantManager() throws WrongPolicy {
        return activator;
    }

    @Poa
    public void setServantManager( 
        ServantManager servantManager ) throws WrongPolicy {

        if (activator != null) {
            throw wrapper.servantManagerAlreadySet();
        }

        if (servantManager instanceof ServantActivator) {
            activator = (ServantActivator) servantManager;
        } else {
            throw wrapper.servantManagerBadType();
        }
    }

    public Servant getDefaultServant() throws NoServant, WrongPolicy 
    {
        throw new WrongPolicy();
    }

    public void setDefaultServant( Servant servant ) throws WrongPolicy
    {
        throw new WrongPolicy();
    }

    @Poa
    private class Etherealizer extends Thread {
        private POAPolicyMediatorImpl_R_USM mediator ;
        private ActiveObjectMap.Key key ;
        private AOMEntry entry ;
        private Servant servant ;

        Etherealizer( POAPolicyMediatorImpl_R_USM mediator, 
            ActiveObjectMap.Key key, AOMEntry entry, Servant servant )
        {
            this.mediator = mediator ;
            this.key = key ;
            this.entry = entry;
            this.servant = servant;
        }

        @InfoMethod
        private void key( ActiveObjectMap.Key key ) { }

        @Poa
        @Override
        public void run() {
            key( key ) ;

            try {
                mediator.activator.etherealize( key.id(), mediator.poa, servant,
                    false, mediator.activeObjectMap.hasMultipleIDs( entry ) );
            } catch (Exception exc) {
                // ignore all exceptions
            }

            try {
                mediator.poa.lock() ;

                entry.etherealizeComplete() ;
                mediator.activeObjectMap.remove( key ) ;

                POAManagerImpl pm = (POAManagerImpl)mediator.poa.the_POAManager() ;
                POAFactory factory = pm.getFactory() ;
                factory.unregisterPOAForServant( mediator.poa, servant);
            } finally {
                mediator.poa.unlock() ;
            }
        }
    } 

    @Poa
    @Override
    public void deactivateHelper( ActiveObjectMap.Key key, AOMEntry entry, 
        Servant servant ) throws ObjectNotActive, WrongPolicy 
    {
        if (activator == null) {
            throw wrapper.poaNoServantManager();
        }
            
        Etherealizer eth = new Etherealizer( this, key, entry, servant ) ;
        entry.startEtherealize( eth ) ;
    }

    @Poa
    public Servant idToServant( byte[] id ) 
        throws WrongPolicy, ObjectNotActive
    {
        ActiveObjectMap.Key key = new ActiveObjectMap.Key( id ) ;
        AOMEntry entry = activeObjectMap.get(key);

        Servant servant = activeObjectMap.getServant( entry ) ;
        if (servant != null) {
            return servant;
        } else {
            throw new ObjectNotActive();
        }
    }
}
