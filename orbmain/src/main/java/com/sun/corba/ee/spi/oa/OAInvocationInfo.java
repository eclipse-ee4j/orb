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

package com.sun.corba.ee.spi.oa;

import javax.rmi.CORBA.Tie ;
import org.glassfish.pfl.dynamic.copyobject.spi.ObjectCopierFactory;
import org.omg.CORBA.portable.ServantObject;
import org.omg.PortableServer.ServantLocatorPackage.CookieHolder;


/** This class is a holder for the information required to implement POACurrent.
* It is also used for the ServantObject that is returned by _servant_preinvoke calls.
* This allows us to avoid allocating an extra object on each collocated invocation.
*/
public class OAInvocationInfo extends ServantObject {
    // This is the container object for the servant.
    // In the RMI-IIOP case, it is the RMI-IIOP Tie, and the servant is the
    // target of the Tie.
    // In all other cases, it is the same as the Servant.
    private java.lang.Object    servantContainer ;

    // These fields are to support standard OMG APIs.
    private ObjectAdapter       oa; 
    private byte[]              oid;

    // These fields are to support the Object adapter implementation.
    private CookieHolder        cookieHolder;
    private String              operation;

    // This is the copier to be used by javax.rmi.CORBA.Util.copyObject(s)
    // For the current request.
    private ObjectCopierFactory factory ;

    public OAInvocationInfo(ObjectAdapter oa, byte[] id )
    {
        this.oa = oa;
        this.oid  = id;
    }

    // Copy constructor of sorts; used in local optimization path
    public OAInvocationInfo( OAInvocationInfo info, String operation )
    {
        this.servant            = info.servant ;
        this.servantContainer   = info.servantContainer ;
        this.cookieHolder       = info.cookieHolder ;
        this.oa                 = info.oa;
        this.oid                = info.oid;
        this.factory            = info.factory ;

        this.operation          = operation;
    }

    //getters
    public ObjectAdapter    oa()                    { return oa ; }
    public byte[]           id()                    { return oid ; }
    public Object           getServantContainer()   { return servantContainer ; }

    // Create CookieHolder on demand.  This is only called by a single
    // thread, so no synchronization is needed.
    public CookieHolder     getCookieHolder()   
    { 
        if (cookieHolder == null)
            cookieHolder = new CookieHolder() ;

        return cookieHolder; 
    }

    public String           getOperation()      { return operation; }
    public ObjectCopierFactory  getCopierFactory()      { return factory; }

    //setters
    public void setOperation( String operation )    { this.operation = operation ; }
    public void setCopierFactory( ObjectCopierFactory factory )    { this.factory = factory ; } 

    public void setServant(Object servant) 
    { 
        servantContainer = servant ;
        if (servant instanceof Tie)
            this.servant = ((Tie)servant).getTarget() ;
        else
            this.servant = servant; 
    }
}
