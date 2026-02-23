/*
 * Copyright (c) 2020 Payara Services Limited
 * Copyright (c) 2020 Oracle and/or its affiliates.
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
package com.sun.corba.ee.impl.corba.javax;

import java.rmi.RemoteException;

import javax.rmi.CORBA.Util;

import org.junit.Test;
import org.omg.CORBA.INVALID_TRANSACTION;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.TRANSACTION_REQUIRED;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;

import static org.junit.Assert.assertTrue;

/**
 *
 * @author steve
 */
public class TestTransactionRemoteException {
    
    @Test
    public void canLoadTransactionException() {
        
        SystemException se = new TRANSACTION_ROLLEDBACK();
        RemoteException re = Util.mapSystemException(se);
        assertTrue(re.getClass().getName().endsWith("TransactionRolledbackException"));
        assertTrue(re.getCause().equals(se));
        
        se = new TRANSACTION_REQUIRED();
        re = Util.mapSystemException(se);
        assertTrue(re.getClass().getName().endsWith("TransactionRequiredException"));
        assertTrue(re.getCause().equals(se));   
        
        se = new INVALID_TRANSACTION();
        re = Util.mapSystemException(se);
        assertTrue(re.getClass().getName().endsWith("InvalidTransactionException"));
        assertTrue(re.getCause().equals(se));        
        
    }
    
}
