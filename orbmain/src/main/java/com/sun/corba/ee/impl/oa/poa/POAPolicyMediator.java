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

import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedData;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.ServantManager;
import org.omg.PortableServer.ForwardRequest;

import org.omg.PortableServer.POAPackage.ObjectAlreadyActive;
import org.omg.PortableServer.POAPackage.ServantAlreadyActive;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.NoServant;
import org.omg.PortableServer.POAPackage.WrongPolicy;
import org.omg.PortableServer.POAPackage.ObjectNotActive;

/**
 * POAPolicyMediator defines an interface to which the POA delegates all policy specific operations. This permits code
 * paths for different policies to be optimized by creating the correct code at POA creation time. Also note that as
 * much as possible, this interface does not do any concurrency control, except as noted. The POA is responsible for
 * concurrency control.
 */
@ManagedData
@Description("Handles the Policy-specific parts of the POA")
public interface POAPolicyMediator {
    /**
     * Return the policies object that was used to create this POAPolicyMediator.
     * 
     * @return The policies of this POA
     */
    @ManagedAttribute
    @Description("The policies of this POA")
    Policies getPolicies();

    /**
     * Return the subcontract ID to use in the IIOP profile in IORs created by this POAPolicyMediator's POA. This is
     * initialized according to the policies and the POA used to construct this POAPolicyMediator in the
     * POAPolicyMediatorFactory.
     * 
     * @return This POA's subcontract ID.
     */
    @ManagedAttribute
    @Description("This POA's subcontract ID")
    int getScid();

    /**
     * Return the server ID to use in the IIOP profile in IORs created by this POAPolicyMediator's POA. This is initialized
     * according to the policies and the POA used to construct this POAPolicyMediator in the POAPolicyMediatorFactory.
     * 
     * @return This POA's server ID.
     */
    @ManagedAttribute
    @Description("This POA's server ID")
    int getServerId();

    /**
     * Get the servant to use for an invocation with the given id and operation.
     * 
     * @param id the object ID for which we are requesting a servant
     * @param operation the name of the operation to be performed on the servant
     * @return the resulting Servant.
     * @throws ForwardRequest if the current ORB must forward the result.
     */
    java.lang.Object getInvocationServant(byte[] id, String operation) throws ForwardRequest;

    /**
     * Release a servant that was obtained from getInvocationServant.
     */
    void returnServant();

    /**
     * Etherealize all servants associated with this POAPolicyMediator. Does nothing if the retention policy is non-retain.
     */
    void etherealizeAll();

    /**
     * Delete everything in the active object map.
     */
    void clearAOM();

    /**
     * Return the servant manager. Will throw WrongPolicy if the request processing policy is not USE_SERVANT_MANAGER.
     * 
     * @return The current ServantManager
     * @throws WrongPolicy If the request processing policy is not USE_SERVANT_MANAGER
     */
    ServantManager getServantManager() throws WrongPolicy;

    /**
     * Set the servant manager. Will throw WrongPolicy if the request processing policy is not USE_SERVANT_MANAGER.
     * 
     * @param servantManager The ServantManager
     * @throws WrongPolicy if the request processing policy is not USE_SERVANT_MANAGER.
     */
    void setServantManager(ServantManager servantManager) throws WrongPolicy;

    /**
     * Return the default servant. Will throw WrongPolicy if the request processing policy is not USE_DEFAULT_SERVANT.
     * 
     * @return the default Servant
     * @throws NoServant if no Servant has been set
     * @throws WrongPolicy if the policy is not USE_DEFAULT_SERVANT
     */
    Servant getDefaultServant() throws NoServant, WrongPolicy;

    /**
     * Set the default servant. Will throw WrongPolicy if the request processing policy is not USE_DEFAULT_SERVANT.
     * 
     * @param servant The default Servant
     * @throws WrongPolicy if the request processing policy is not USE_DEFAULT_SERVANT.
     */
    void setDefaultServant(Servant servant) throws WrongPolicy;

    void activateObject(byte[] id, Servant servant) throws ObjectAlreadyActive, ServantAlreadyActive, WrongPolicy;

    /**
     * Deactivate the object that is associated with the given id. Returns the servant for id.
     * 
     * @param id ID of the object to deactivate
     * @return Servant for the ID
     * @throws ObjectNotActive if the object was not active
     * @throws WrongPolicy if not supported by the current policy
     */
    Servant deactivateObject(byte[] id) throws ObjectNotActive, WrongPolicy;

    /**
     * Allocate a new, unique system ID. Requires the ID assignment policy to be SYSTEM.
     * 
     * @return the new system ID
     * @throws WrongPolicy if the ID assignment policy is not SYSTEM
     */
    byte[] newSystemId() throws WrongPolicy;

    byte[] servantToId(Servant servant) throws ServantNotActive, WrongPolicy;

    Servant idToServant(byte[] id) throws ObjectNotActive, WrongPolicy;
}
