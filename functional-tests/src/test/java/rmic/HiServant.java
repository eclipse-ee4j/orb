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

package rmic;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.io.IOException;

public class HiServant implements Hi {
    public String hi_0 () {
        return "Hi!";
    }
    
    public String hi_1 () throws ClassNotFoundException {
        throw new ClassNotFoundException();   
    }
    
    public String hi_2 () throws Exception {
        return "hi";   
    }
    
    public String hi_3 () throws IOException,ClassNotFoundException {
        return "hi";   
    }
    
    public String hi_4 (String in) throws IOException,ClassNotFoundException,HelloException {
        return "hi";   
    }
}
