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

package com.sun.corba.ee.spi.transport;

import java.util.List;

/**
 * This interface is the "sticky manager" for IIOP failover.  The default
 * ORB does NOT contain a sticky manager.  One is registered by supplying
 * a class via the com.sun.corba.ee.transport.ORBIIOPPrimaryToContactInfoClass.
 *
 * It uses the IIOP primary host/port (with a SocketInfo.IIOP_CLEAR_TEXT type)
 * as a key to map to the last ContactInfo that resulted in successful'
 * communication.
 *
 * It mainly prevents "fallback" - if a previously failed replica comes
 * back up we do NOT want to switch back to using it - particularly in the
 * case of statefull session beans.
 *
 * Note: This assumes static lists of replicas (e.g., AS 8.1 EE).
 * This does NOT work well with LOCATION_FORWARD.  
 *
 * @author Harold Carr
 */
public interface IIOPPrimaryToContactInfo
{
    /**
     * @param primary - clear any state relating to primary.
     */
    public void reset(ContactInfo primary);

    /**
     * @param primary the key.
     * @param previous if null return true.  Otherwise, find previous in 
     * <code>contactInfos</code> and if another <code>ContactInfo</code>
     * follows it in the list then return true.  Otherwise false.
     * @param contactInfos the list of replicas associated with the
     * primary.
     * @return if there is another
     */
    public boolean hasNext(ContactInfo primary,
                           ContactInfo previous,
                           List contactInfos);

    /**
     * @param primary the key.
     * @param previous if null then map primary to failover.  If failover is
     * empty then map primary to first <code>ContactInfo</code> in contactInfos and mapped entry.
     * If failover is
     * non-empty then return failover.  If previous is non-null that
     * indicates that the previous failed.  Therefore, find previous in
     * contactInfos.  Map the <code>ContactInfo</code> following
     * previous to primary and return that <code>ContactInfo</code>.
     * @param contactInfos the list of replicas associated with the
     * primary.
     * @return the next ContactInfo
     */
    public ContactInfo next(ContactInfo primary,
                            ContactInfo previous,
                            List contactInfos);

}

// End of file.
