/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.misc ;

import java.io.ObjectInput ;
import java.io.ObjectOutput ;
import java.io.IOException ;

public class Buck implements java.io.Externalizable {
    String name = "";

    public Buck(){}
     
    public Buck(String name) { this.name = name; }

    public String toString() { return "Buck[" + name + "]" ; } 

    public boolean equals( Object obj ) {
        if (!(obj instanceof Buck))
            return false ;

        if (obj == this)
            return true ;

        Buck other = (Buck)obj ;

        return other.name.equals( name ) ;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException { 
        name = (String) in.readObject(); 
    }

    public void writeExternal(ObjectOutput out) throws IOException {
      out.writeObject(name);
    }
}
