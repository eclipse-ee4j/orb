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

package com.sun.corba.ee.impl.presentation.rmi ;

import com.sun.corba.ee.impl.corba.CORBAObjectImpl ;
import com.sun.corba.ee.impl.ior.StubIORImpl ;
import com.sun.corba.ee.impl.util.Utility;
import com.sun.corba.ee.spi.logging.UtilSystemException ;
import com.sun.corba.ee.spi.presentation.rmi.StubAdapter;

import java.rmi.RemoteException;

import javax.rmi.CORBA.Tie;

import org.omg.CORBA.BAD_INV_ORDER;
import org.omg.CORBA.BAD_OPERATION;
import org.omg.CORBA.ORB;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.portable.Delegate;
import org.omg.CORBA.portable.ObjectImpl;

public abstract class StubConnectImpl 
{
    private static UtilSystemException wrapper = 
        UtilSystemException.self ;

    /** Connect the stub to the orb if necessary.  
    * @param ior The StubIORImpl for this stub (may be null)
    * @param proxy The externally visible stub seen by the user (may be the same as stub)
    * @param stub The stub implementation that extends ObjectImpl
    * @param orb The ORB to which we connect the stub.
    * @return The IOR
    * @throws RemoteException If an exception occurs
    */
    public static StubIORImpl connect( StubIORImpl ior, org.omg.CORBA.Object proxy, 
        org.omg.CORBA.portable.ObjectImpl stub, ORB orb ) throws RemoteException 
    {
        Delegate del = null ;

        try {
            try {
                del = StubAdapter.getDelegate( stub );
                
                if (del.orb(stub) != orb) 
                    throw wrapper.connectWrongOrb() ;
            } catch (org.omg.CORBA.BAD_OPERATION err) {    
                if (ior == null) {
                    // No IOR, can we get a Tie for this stub?
                    Tie tie = (javax.rmi.CORBA.Tie) Utility.getAndForgetTie(proxy);
                    if (tie == null) 
                        throw wrapper.connectNoTie() ;

                    // Is the tie already connected?  If it is, check that it's 
                    // connected to the same ORB, otherwise connect it.
                    ORB existingOrb = orb ;
                    try {
                        existingOrb = tie.orb();
                    } catch (BAD_OPERATION exc) { 
                        // Thrown when tie is an ObjectImpl and its delegate is not set.
                        tie.orb(orb);
                    } catch (BAD_INV_ORDER exc) { 
                        // Thrown when tie is a Servant and its delegate is not set.
                        tie.orb(orb);
                    }

                    if (existingOrb != orb) 
                        throw wrapper.connectTieWrongOrb() ;
                        
                    // Get the delegate for the stub from the tie.
                    del = StubAdapter.getDelegate( tie ) ;
                    ObjectImpl objref = new CORBAObjectImpl() ;
                    objref._set_delegate( del ) ;
                    ior = new StubIORImpl( objref ) ;
                } else {
                    // ior is initialized, so convert ior to an object, extract
                    // the delegate, and set it on ourself
                    del = ior.getDelegate( orb ) ;
                }

                StubAdapter.setDelegate( stub, del ) ;
            }
        } catch (SystemException exc) {
            throw new RemoteException("CORBA SystemException", exc );
        }

        return ior ;
    }
}
