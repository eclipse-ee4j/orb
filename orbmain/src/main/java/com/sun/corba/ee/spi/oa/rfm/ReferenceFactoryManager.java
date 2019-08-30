/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.oa.rfm ;

import java.util.Map ;
import java.util.List ;
import org.glassfish.pfl.basic.contain.Pair;

import org.omg.CORBA.Policy ;

import org.omg.PortableServer.ServantLocator ;

/** ReferenceFactoryManager uses the ORB POA to create 
 * a specialized reference factory.  This is done primarily
 * so that all POAs managed here can be restarted in order
 * to be updated when parts of the ORB configuration are
 * changed.  For example, this is used in AS 9 to support
 * dynamic fail over, which requires re-configuring the
 * App server POAs whenever an instance joins or leaves
 * a cluster.
 * <p>
 * An instance of this interface can be obtained
 * from an ORB by calling resolve_initial_references( "ReferenceFactoryManager" ).
 * The ORB will have an instance of this available if it is started with the
 * property ORBConstants.USER_CONFIGURATOR_PREFIX +
 * "com.sun.corba.ee.impl.oa.rfm.ReferenceManagerConfigurator" set to a value
 * (usually we use "1" as the value, but that does not matter).
 * This will cause the configurator to set up the ORB so that the 
 * ReferenceFactoryManager is available, so long as the configurator class
 * is available.  Since this code is in the optional package, this just
 * means that the optional ORB package contents must be in the classpath.
 * <p>
 * Note that this interface is a simulated IDL local interface, but there
 * is no actual IDL for this interface.
 * <P>
 * Note that the suspend and resume methods must be called from the
 * same thread in order for the thread synchronization to be handled
 * correctly.  Calling either of the restart() methods guarantees this,
 * but disallows more complex ORB configuration changes.
 */
public interface ReferenceFactoryManager extends org.omg.CORBA.Object,
    org.omg.CORBA.portable.IDLEntity 
{
    public enum RFMState { READY, SUSPENDED }

    /** The state of the ReferenceFactoryManager.
     * 
     * @return the state
     */
    public RFMState getState();

    /** Must be called before any other operation.
     * Used to activate the ORB reference creation function.
     */
    public void activate() ;

    /** Suspend all CORBA request processing on all references created
     * by ReferenceFactory instances that were created by this
     * ReferenceFactoryManager.  This call does not return until
     * after all currently executing calls have completed.
     */
    public void suspend() ;

    /** Resume all CORBA request processing on all references created
     * by ReferenceFactory instances that were created by this
     * ReferenceFactoryManager.  
     */
    public void resume() ;

    /** Create a new reference factory with the given policies.
     * All such reference factories will be persistent.  The
     * ServantLocator is solely responsible for creating
     * servants: no internal caching will be performed.
     * The following policies are always applied here:
     * <ul>
     * <li>Servant retention policy NON_RETAIN
     * <li>Request processing policy USE_SERVANT_MANAGER
     * <LI>Lifespan policy PERSISTENT
     * </ul>
     * These policies are required because all are essential to
     * the correct functioning of this class in handling restarts.
     * It is an error for the policies list to contain any value
     * of the above 3 policies.
     * All other policies must be given explicitly in the list.
     * @param name is the name of this ReferenceFactory.  This is a
     * simple flat name, not a hierarchical name.
     * @param repositoryId is the repoid to be used when this reference factory
     * creates a new CORBA Object reference.
     * @param policies are the policies to be used to create the underlying POA.
     * @param manager the ServantLocator to use to create the factory
     * @return the resulting ReferenceFactory
     */
    public ReferenceFactory create( String name, String repositoryId, List<Policy> policies,
        ServantLocator manager ) ;

    /** Get the ReferenceFactory name from a String[] adapterName, if
     * adapterName is the name of a ReferenceFactory.  If not, return null.
     * 
     * @param adapterName the adapterName to search for
     * @return the found ReferenceFactory
     */
    public ReferenceFactory find( String[] adapterName ) ;

    /** Find the ReferenceFactory with the given name.
     * If no such ReferenceFactory exists, return {@code null}.
     * 
     * @param name the name to search for
     * @return the found ReferenceFactory
     */
    public ReferenceFactory find( String name ) ;

    /** Restart all ReferenceFactories.  
     * @param updates is a map giving the updated policies for
     * some or all of the ReferenceFactory instances in this ReferenceFactoryManager.
     * This parameter must not be null.
     */
    public void restartFactories( Map<String,Pair<ServantLocator,List<Policy>>> updates ) ;

    /** Restart all ReferenceFactories. 
     * Equivalent to calling restartFactories( new Map() ).
     */
    public void restartFactories() ;

    /** Restart all ReferenceFactories.  This is done safely, so that
     * any request against object references created from these factories
     * complete correctly.  Restart does not return until all restart
     * activity completes. This method is equivalent to:
     * <pre>
     * suspend() ;
     * try {
     *     restartFactories( updates ) ;
     * } finally {
     *     resume() ;
     * }
     * </pre>
     * @param updates is a map giving the updated policies for
     * some or all of the ReferenceFactory instances in this ReferenceFactoryManager.
     * This parameter must not be null.
     */
    public void restart( Map<String,Pair<ServantLocator,List<Policy>>> updates ) ;

    /** Restart all ReferenceFactories.  This is done safely, so that
     * any request against object references created from these factories
     * complete correctly.  Restart does not return until all restart
     * activity completes.  Equivalent to calling restart( new Map() ).
     */
    public void restart() ;

    public boolean isRfmName( String[] adapterName ) ;

}
