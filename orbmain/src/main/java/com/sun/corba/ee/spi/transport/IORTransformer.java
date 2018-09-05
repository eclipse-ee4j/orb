/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.transport ;

import com.sun.corba.ee.spi.ior.IOR ;
import com.sun.corba.ee.impl.encoding.CDRInputObject ;
import com.sun.corba.ee.impl.encoding.CDROutputObject ;

/** Interface that provides operations to transorm an IOR
 * between its programmatic representation and a representation
 * in an Input or Output object.
 */
public interface IORTransformer {
    IOR unmarshal( CDRInputObject io ) ;

    void marshal( CDROutputObject oo, IOR ior ) ;
}
