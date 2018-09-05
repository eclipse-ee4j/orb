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

//
// ATTENTION! ATTENTION! This class is NOT in the wrong directory nor in the
// wrong package.  It's directory should be javax.rmi.download and it's
// package should be javax.rmi.download.value.
//


package javax.rmi.download.values;

import javax.rmi.download.TheValue;

public class TheValueImpl implements TheValue {
    public TheValueImpl(){}

    public String sayHello(){
        return "Hello, world!";
    }
}
