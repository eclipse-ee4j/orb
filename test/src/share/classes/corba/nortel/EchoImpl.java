/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.nortel;

import java.rmi.Remote ;
import java.rmi.RemoteException ;
import javax.rmi.PortableRemoteObject ;

import java.util.List ;
import java.util.ArrayList ;
import java.util.Collection ;

public class EchoImpl extends PortableRemoteObject implements Echo {
    private String name ;

    public EchoImpl( String name ) throws RemoteException {
        this.name = name ;
    }

    public Echo say( Echo echo ) {
        return echo ;
    }

    public String name() {
        return name ;
    }

    public Collection methodCollectionUserInfo( String str, UserInfo ui ) throws RemoteException {
        List result = new ArrayList() ;
        result.add( str ) ;
        result.add( ui.toString() ) ;
        return result ;
    }

    public Collection methodCollectionObject( String str, Object ui ) throws RemoteException {
        List result = new ArrayList() ;
        result.add( str ) ;
        result.add( ui.toString() ) ;
        return result ;
    }
}


