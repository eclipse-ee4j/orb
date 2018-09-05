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

public class ClassD extends ClassA{
    private static final long serialVersionUID = 128456789L;

    private int a;
        
    public ClassD(){
        a = 7;
    }
        
    public long getTotal(){
        return  super.getTotal() + a;
    }

    public long getOriginalTotal(){
        return super.getOriginalTotal() + 7;
    }
}
