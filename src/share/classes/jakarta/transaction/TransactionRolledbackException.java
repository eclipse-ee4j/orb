/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package jakarta.transaction;

/**
 * This exception indicates that the transaction associated with processing
 * of the request has been rolled back, or marked to roll back. Thus the
 * requested operation either could not be performed or was not performed
 * because further computation on behalf of the transaction would be
 * fruitless
 */
public class TransactionRolledbackException extends java.rmi.RemoteException 
{
    public TransactionRolledbackException()
    {
        super();
    }

    public TransactionRolledbackException(String msg)
    {
        super(msg);
    }
}

