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

package com.sun.corba.ee.spi.folb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.io.Serializable ;

import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.CORBA_2_3.portable.OutputStream;

/**
 *
 * @author ken
 */
public class ClusterInstanceInfo implements Serializable {
    private final String name ;
    private final int weight ;
    private final List<SocketInfo> endpoints ;

    public ClusterInstanceInfo( InputStream is ) {
        name = is.read_string() ;
        weight = is.read_long() ;
        int size = is.read_long() ;
        List<SocketInfo> elist = new ArrayList<SocketInfo>( size ) ;
        for (int ctr = 0; ctr<size; ctr++) {
            elist.add( new SocketInfo(is)) ;
        }
        endpoints = Collections.unmodifiableList(elist) ;
    }

    public ClusterInstanceInfo(String name, int weight,
        List<SocketInfo> endpoints) {

        this.name = name;
        this.weight = weight;
        this.endpoints = Collections.unmodifiableList( endpoints ) ;
    }

    public List<SocketInfo> endpoints() { return endpoints ; }
    public String name() { return name; }
    public int weight() { return weight; }

    public void write( OutputStream os ) {
        os.write_string( name ) ;
        os.write_long( weight );
        os.write_long( endpoints.size() ) ;
        for (SocketInfo si : endpoints) {
            si.write( os ) ;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder() ;
        sb.append( "ClusterInstanceInfo[" ) ;
        sb.append( "name=" ) ;
        sb.append( name ) ;
        sb.append( " weight=" ) ;
        sb.append( weight ) ;
        sb.append( " endpoints=" ) ;
        sb.append( endpoints.toString() ) ;
        sb.append( "]" ) ;
        return sb.toString() ;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final ClusterInstanceInfo other = (ClusterInstanceInfo) obj;

        if ((this.name == null) ?
            (other.name() != null) :
            !this.name.equals(other.name())) {

            return false;
        }

        if (this.weight != other.weight()) {
            return false;
        }

        if (this.endpoints != other.endpoints() &&
           (this.endpoints == null ||
            !this.endpoints.equals(other.endpoints()))) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 79 * hash + this.weight;
        hash = 79 * hash + (this.endpoints != null ? this.endpoints.hashCode() : 0);
        return hash;
    }
}
