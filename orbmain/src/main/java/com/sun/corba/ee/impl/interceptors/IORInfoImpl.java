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

package com.sun.corba.ee.impl.interceptors;

import java.util.Iterator ;

import org.omg.IOP.TaggedComponent;

import org.omg.CORBA.Policy;
import org.omg.CORBA.LocalObject;

import org.omg.PortableInterceptor.IORInfo;
import org.omg.PortableInterceptor.ObjectReferenceTemplate;
import org.omg.PortableInterceptor.ObjectReferenceFactory;

import com.sun.corba.ee.spi.orb.ORB ;

import com.sun.corba.ee.spi.oa.ObjectAdapter;

import com.sun.corba.ee.spi.legacy.interceptor.IORInfoExt;
import com.sun.corba.ee.spi.legacy.interceptor.UnknownType;

import com.sun.corba.ee.spi.ior.TaggedProfileTemplate;
import com.sun.corba.ee.spi.ior.TaggedComponentFactoryFinder ;

import com.sun.corba.ee.spi.logging.InterceptorsSystemException ;
import com.sun.corba.ee.spi.logging.OMGSystemException ;
import com.sun.corba.ee.spi.logging.ORBUtilSystemException ;

/**
 * IORInfoImpl is the implementation of the IORInfo class, as described
 * in orbos/99-12-02, section 7.  
 */
public final class IORInfoImpl 
    extends LocalObject 
    implements IORInfo, IORInfoExt
{
    // State values that determine which methods are allowed.
    // get_effective_policy, manager_id, and adapter_state are valid unless STATE_DONE

    // add_component, and add_component_to_profile are valid.
    private static final int STATE_INITIAL = 0 ; 

    // adapter_template, and R/W to current_factory are valid
    private static final int STATE_ESTABLISHED = 1 ; 

    // No methods are valid in this state
    private static final int STATE_DONE = 2 ; 

    // The current state of this object
    private int state = STATE_INITIAL ;

    // The ObjectAdapter associated with this IORInfo object.
    private transient ObjectAdapter adapter;

    private transient ORB orb ;

    private static final ORBUtilSystemException orbutilWrapper =
        ORBUtilSystemException.self ;
    private static final InterceptorsSystemException wrapper =
        InterceptorsSystemException.self ;
    private static final OMGSystemException omgWrapper =
        OMGSystemException.self ;

    /**
     * Creates a new IORInfo implementation.  This info object will establish
     * tagged components with the template for the provided IOR Template.
     */
    IORInfoImpl( ObjectAdapter adapter ) {
        this.orb = adapter.getORB() ;
        this.adapter = adapter;
    }

    /**
     * An ORB service implementation may determine what server side policy 
     * of a particular type is in effect for an IOR being constructed by 
     * calling the get_effective_policy operation.  When the IOR being 
     * constructed is for an object implemented using a POA, all Policy 
     * objects passed to the PortableServer::POA::create_POA call that 
     * created that POA are accessible via get_effective_policy.
     * <p>
     * If a policy for the given type is not known to the ORB, then this 
     * operation will raise INV_POLICY with a standard minor code of 2.
     *
     * @param type The CORBA::PolicyType specifying the type of policy to 
     *   return.
     * @return The effective CORBA::Policy object of the requested type.
     *   If the given policy type is known, but no policy of that type is 
     *   in effect, then this operation will return a nil object reference.
     */
    public Policy get_effective_policy (int type) {
        checkState( STATE_INITIAL, STATE_ESTABLISHED ) ;

        return adapter.getEffectivePolicy( type );
    }

    /**
     * A portable ORB service implementation calls this method from its 
     * implementation of establish_components to add a tagged component to 
     * the set which will be included when constructing IORs.  The 
     * components in this set will be included in all profiles.
     * <p>
     * Any number of components may exist with the same component ID.
     *
     * @param tagged_component The IOP::TaggedComponent to add
     */
    public void add_ior_component (TaggedComponent tagged_component) {
        checkState( STATE_INITIAL ) ;

        if( tagged_component == null ) nullParam();
        addIORComponentToProfileInternal( tagged_component, 
                                          adapter.getIORTemplate().iterator());
    }

    /**
     * A portable ORB service implementation calls this method from its 
     * implementation of establish_components to add a tagged component to 
     * the set which will be included when constructing IORs.  The 
     * components in this set will be included in the specified profile.
     * <p>
     * Any number of components may exist with the same component ID.
     * <p>
     * If the given profile ID does not define a known profile or it is 
     * impossible to add components to thgat profile, BAD_PARAM is raised 
     * with a minor code of TBD_BP + 3.
     *
     * @param tagged_component The IOP::TaggedComponent to add.
     * @param profile_id The IOP::ProfileId tof the profile to which this 
     *     component will be added.
     */
    public void add_ior_component_to_profile ( 
        TaggedComponent tagged_component, int profile_id ) 
    {
        checkState( STATE_INITIAL ) ;

        if( tagged_component == null ) nullParam();
        addIORComponentToProfileInternal( 
            tagged_component, adapter.getIORTemplate().iteratorById( 
            profile_id ) );
    }

    /**
     * @param type The type of the server port
     *     (see connection.ORBSocketFactory for discussion).
     * @return The listen port number for that type.
     * @throws UnknownType if no port of the given type is found.
     */
    public int getServerPort(String type)
        throws UnknownType
    {
        checkState( STATE_INITIAL, STATE_ESTABLISHED ) ;

        int port =
            orb.getLegacyServerSocketManager()
                .legacyGetTransientOrPersistentServerPort(type);
        if (port == -1) {
            throw new UnknownType();
        }
        return port;
    }

    public ObjectAdapter getObjectAdapter()
    {
        return adapter;
    }
    
    public int manager_id()
    {
        checkState( STATE_INITIAL, STATE_ESTABLISHED) ;

        return adapter.getManagerId() ;
    }

    public short state()
    {
        checkState( STATE_INITIAL, STATE_ESTABLISHED) ;

        return adapter.getState() ;
    }

    public ObjectReferenceTemplate adapter_template() 
    {
        checkState( STATE_ESTABLISHED) ;

        // At this point, the iortemp must contain only a single
        // IIOPProfileTemplate.  This is a restriction of our
        // implementation.  Also, note the the ObjectReferenceTemplate
        // is called when a certain POA is created in a certain ORB
        // in a certain server, so the server_id, orb_id, and 
        // poa_id operations must be well-defined no matter what
        // kind of implementation is used: e.g., if a POA creates
        // IORs with multiple profiles, they must still all agree
        // about this information.  Thus, we are justified in
        // extracting the single IIOPProfileTemplate to create 
        // an ObjectReferenceTemplate here.

        return adapter.getAdapterTemplate() ;
    }

    public ObjectReferenceFactory current_factory() 
    {
        checkState( STATE_ESTABLISHED) ;

        return adapter.getCurrentFactory() ;
    }

    public void current_factory( ObjectReferenceFactory factory )
    {
        checkState( STATE_ESTABLISHED) ;

        adapter.setCurrentFactory( factory ) ;
    }

    /**
     * Internal utility method to add an IOR component to the set of profiles
     * present in the iterator.
     */
    private void addIORComponentToProfileInternal( 
        TaggedComponent tagged_component, Iterator iterator )
    {
        // Convert the given IOP::TaggedComponent into the appropriate
        // type for the TaggedProfileTemplate
        TaggedComponentFactoryFinder finder = 
            orb.getTaggedComponentFactoryFinder();
        com.sun.corba.ee.spi.ior.TaggedComponent newTaggedComponent = 
            finder.create( orb, tagged_component );
        
        // Iterate through TaggedProfileTemplates and add the given tagged
        // component to the appropriate one(s).
        boolean found = false;
        while( iterator.hasNext() ) {
            found = true;
            TaggedProfileTemplate taggedProfileTemplate = 
                (TaggedProfileTemplate)iterator.next();
            taggedProfileTemplate.add( newTaggedComponent );
        }

        // If no profile was found with the given id, throw a BAD_PARAM:
        // (See orbos/00-08-06, section 21.5.3.3.)
        if( !found ) {
            throw omgWrapper.invalidProfileId() ;
        }
    }
    
    /**
     * Called when an invalid null parameter was passed.  Throws a
     * BAD_PARAM with a minor code of 1
     */
    private void nullParam() 
    {
        throw orbutilWrapper.nullParamNoComplete() ;
    }

    // REVISIT: add minor codes!

    private void checkState( int expectedState )
    {
        if (expectedState != state)
            throw wrapper.badState1( expectedState, state ) ;
    }

    private void checkState( int expectedState1, int expectedState2 )
    {
        if ((expectedState1 != state) && (expectedState2 != state))
            throw wrapper.badState2( expectedState1, expectedState2, state ) ;
    }

    void makeStateEstablished()
    {
        checkState( STATE_INITIAL ) ;

        state = STATE_ESTABLISHED ;
    }

    void makeStateDone()
    {
        checkState( STATE_ESTABLISHED ) ;

        state = STATE_DONE ;
    }
}
