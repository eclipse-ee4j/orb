/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2019 Payara Services Ltd.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.protocol;

import java.util.Set;

import com.sun.corba.ee.spi.oa.ObjectAdapterFactory ;

/**
 * This is a registry of all subcontract ID dependent objects.  This includes:
 * LocalClientRequestDispatcherFactory, ClientRequestDispatcher, ServerRequestDispatcher, and 
 * ObjectAdapterFactory. 
 */
public interface RequestDispatcherRegistry {

    /** Register a ClientRequestDispatcher for a particular subcontract ID.
     * The subcontract ID appears in the ObjectKey of an object reference, and is used
     * to control how a remote method invocation is processed by the ORB for a 
     * particular kind of object reference.
     * @param csc Dispatcher to register
     * @param scid ID to register with
     */
    void registerClientRequestDispatcher( ClientRequestDispatcher csc, int scid) ;

    /** Get the ClientRequestDispatcher for subcontract ID scid.
     * @param scid ID of the relevant Dispatcher
     * @return found Dispatcher
     */
    ClientRequestDispatcher getClientRequestDispatcher( int scid ) ;

    /** Register a LocalClientRequestDispatcher for a particular subcontract ID.
     * The subcontract ID appears in the ObjectKey of an object reference, and is used
     * to control how a particular kind of colocated request is processed.
     * @param csc DispatcherFactory to register
     * @param scid ID of factory
     */
    void registerLocalClientRequestDispatcherFactory( LocalClientRequestDispatcherFactory csc, int scid) ;

    /** Get the LocalClientRequestDispatcher for subcontract ID scid.
     * @param scid ID to tuse to look up
     * @return found Dispatcher
     */
    LocalClientRequestDispatcherFactory getLocalClientRequestDispatcherFactory( int scid ) ;

    /** Register a CorbaServerRequestDispatcher for a particular subcontract ID.
     * The subcontract ID appears in the ObjectKey of an object reference, and is used
     * to control how a particular kind of request is processed when received by the ORB.
     * @param ssc Dispatcher to register
     * @param scid id to register with
     */
    void registerServerRequestDispatcher( ServerRequestDispatcher ssc, int scid) ;

    /** Get the CorbaServerRequestDispatcher for subcontract ID scid.
     * @param scid ID to use to look up
     * @return Found Dispatcher
     */
    ServerRequestDispatcher getServerRequestDispatcher(int scid) ;

    /** Register a CorbaServerRequestDispatcher for handling an explicit object key name.
     * This is used for non-standard invocations such as INS and the bootstrap name service.
     * @param ssc Dispatcher to register
     * @param name Name to register with
     */
    void registerServerRequestDispatcher( ServerRequestDispatcher ssc, String name ) ;

    /** Get the CorbaServerRequestDispatcher for a particular object key.
     * @param name Name of dispatcher
     * @return The retrieved Dispatcher
     */
    ServerRequestDispatcher getServerRequestDispatcher( String name ) ;

    /** Register an ObjectAdapterFactory for a particular subcontract ID.
     * This controls how Object references are created and managed.
     * @param oaf factory to register
     * @param scid id of factory
     */
    void registerObjectAdapterFactory( ObjectAdapterFactory oaf, int scid) ;

    /** Get the ObjectAdapterFactory for a particular subcontract ID scid.
     * @param scid id of of factory
     * @return relevant factory
     */
    ObjectAdapterFactory getObjectAdapterFactory( int scid ) ;

    /** Return the set of all ObjectAdapterFactory instances that are registered.
     * @return All registered factories
     */
    Set<ObjectAdapterFactory> getObjectAdapterFactories() ;
}
