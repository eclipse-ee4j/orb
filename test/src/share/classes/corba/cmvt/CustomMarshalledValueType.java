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

package corba.cmvt;

public class CustomMarshalledValueType implements java.io.Serializable{
    public byte[] body;
    public CustomMarshalledValueType(int len, byte rep){
        body = new byte[len*10];
        for(int i=0; i<body.length; i++){
            body[i]=(byte)(i);
        }
    }
    public boolean equals(Object o){
        if(o instanceof CustomMarshalledValueType){
            byte[] oBody = ((CustomMarshalledValueType)o).body;
            if(oBody.length!=this.body.length) {
                System.out.println("Different body length");
                return false;
            }
            else{
                boolean e = true;
                for(int i=0; i<oBody.length; i++) e = e && (oBody[i]==this.body[i]);
                System.out.println("Compared all body elements");
                return e;
            }
            
        }
        else return false ;
    }
    private void writeObject(java.io.ObjectOutputStream stream)
               throws java.io.IOException
    {
        stream.defaultWriteObject();
        stream.writeBoolean(true);
        
    }
    private void readObject(java.io.ObjectInputStream stream)
               throws java.io.IOException, ClassNotFoundException
    {
        stream.defaultReadObject();
        stream.readBoolean();
    }


}
