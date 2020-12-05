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
