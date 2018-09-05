/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
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
