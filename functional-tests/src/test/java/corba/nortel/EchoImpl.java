/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Eclipse Distribution License
 * v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License v. 2.0 are satisfied: GNU General Public License v2.0
 * w/Classpath exception which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause OR GPL-2.0 WITH
 * Classpath-exception-2.0
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


