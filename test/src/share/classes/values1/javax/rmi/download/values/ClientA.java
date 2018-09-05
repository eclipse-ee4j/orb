/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package javax.rmi.download.values;

public class ClientA extends javax.rmi.fvd.ParentClass{
    private static final long serialVersionUID = 113456789L;

    private int c, d, e;
        
    public ClientA(){
        c = 456;
        d = 31;
        e = 3109;
    }
        
    public long getTotal(){
        return  super.getTotal() + c + d + e;
    }

    public long getOriginalTotal(){
        return 456 + 31 + 3109;
    }
}
