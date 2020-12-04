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

package com.sun.corba.ee.spi.ior.iiop;

import com.sun.corba.ee.spi.ior.TaggedProfile ;

import com.sun.corba.ee.spi.orb.ORB ;
import com.sun.corba.ee.spi.orb.ORBVersion ;

import com.sun.corba.ee.spi.ior.iiop.GIOPVersion ;

import org.glassfish.gmbal.ManagedData ;
import org.glassfish.gmbal.Description ;
import org.glassfish.gmbal.ManagedAttribute ;

/** IIOPProfile represents an IIOP tagged profile.
* It is essentially composed of an object identifier and
* a template.  The template contains all of the 
* IIOP specific information in the profile.
* Templates are frequently shared between many different profiles,
* while the object identifiy is unique to each profile.
*/
@ManagedData
@Description( "The IIOPProfile version of a TaggedProfile" )
public interface IIOPProfile extends TaggedProfile
{
    @ManagedAttribute
    @Description( "The ORB version in use" ) 
    ORBVersion getORBVersion() ;

    /** Return the servant for this profile, if it is local 
     * AND if the OA that implements this objref supports direct access to servants 
     * outside of an invocation.
     * 
     * @return the profile servant
     */
    java.lang.Object getServant() ;

    /** Return the GIOPVersion of this profile.  Caches the result.
     * 
     * @return the GIOPVersion
     */
    GIOPVersion getGIOPVersion() ;

    /** Return the Codebase of this profile.  Caches the result.
     * 
     * @return the profile codebase
     */
    String getCodebase() ;
}
