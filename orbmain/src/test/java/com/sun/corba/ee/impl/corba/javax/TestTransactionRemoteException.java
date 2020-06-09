/*
 * Copyright (c) 2020 Payara Services Limited
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.sun.corba.ee.impl.corba.javax;

import java.rmi.RemoteException;
import javax.rmi.CORBA.Util;
import org.junit.Test;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;

import static org.junit.Assert.assertTrue;
import org.omg.CORBA.INVALID_TRANSACTION;
import org.omg.CORBA.TRANSACTION_REQUIRED;

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
