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
 * This exception indicates that a request carried a null transaction context,
 * but the target object requires an activate transaction.
 */
public class TransactionRequiredException extends java.rmi.RemoteException 
{
    public TransactionRequiredException()
    {
        super();
    }

    public TransactionRequiredException(String msg)
    {
        super(msg);
    }
}

