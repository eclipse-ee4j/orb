/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.framework ;

import java.rmi.Remote ;

import javax.rmi.PortableRemoteObject ;

public class PRO {
    private PRO() {}

    public static <T> T narrow( Object obj, Class<T> cls ) {
        return cls.cast( PortableRemoteObject.narrow(
            obj, cls ) ) ;
    }

    public static <T> T toStub( Remote obj, Class<T> cls ) {
        try {
            return cls.cast( PortableRemoteObject.toStub(
                obj ) ) ;
        } catch (Exception exc) {
            throw new RuntimeException( exc ) ;
        }
    }
}
