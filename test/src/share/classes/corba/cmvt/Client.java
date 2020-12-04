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

import javax.rmi.PortableRemoteObject;
import org.omg.CosNaming.*;
import org.omg.CORBA.*;
import java.util.* ;
import java.rmi.RemoteException;
import java.io.*;

import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;

public class Client
{


    public static org.omg.CORBA.Object readObjref(String file, org.omg.CORBA.ORB orb) {
        String fil = System.getProperty("output.dir")+System.getProperty("file.separator")+file;
        try {
            java.io.DataInputStream in = 
                new java.io.DataInputStream(new FileInputStream(fil));
            String ior = in.readLine();
            System.out.println("IOR: "+ior);
            return orb.string_to_object(ior);
        } catch (java.io.IOException e) {
            System.err.println("Unable to open file "+fil);
            System.exit(1);
        }
        return null;
    }

    public static CustomMarshalledValueType constructCustomMarshalledValueType(int len, String r){
        CustomMarshalledValueType cmvt = null;
        try{
            cmvt = new CustomMarshalledValueType(len, (byte)r.charAt(0));
        } catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
        return cmvt;
    }

    public static java.util.Vector constructVector(int len, String r){
        java.util.Vector ht = new java.util.Vector();
        for(int i=0; i<len; i++) 
            ht.addElement(getStringObject(i,r));
        return ht;
    }

    public static java.util.Hashtable constructHashtable(int len, String r){
        java.util.Hashtable ht = new java.util.Hashtable();
        for(int i=0; i<len; i++) 
            ht.put(getStringObject(i,r), getStringObject(i,r));
        return ht;
    }

    public static java.lang.Object getStringObject(int len, String r){
        String s = new String();
        for(int i=0; i<len; i++) s+=r;
        return s;
    }

    public static void main(String args[])
    {
        int len = 250;
        String rep = "a";
        boolean doVector=false, doHashtable=true, doCMVT=true, doString=false, doLargeString=false, doHello=false;

        for (int i=0; i<args.length; i++){
            if(args[i].equals("-len")){
                len = Integer.parseInt(args[i+1]);
            }            
            if(args[i].equals("-rep")){
                rep = args[i+1];
            }    
            if(args[i].equals("-vector")){
                doVector=true;
            }    
            if(args[i].equals("-hashtable")){
                doHashtable=true;
            }    
            if(args[i].equals("-cmvt")){
                doCMVT=true;
            }          
            if(args[i].equals("-string")){
                doString=true;
            } 
            if(args[i].equals("-largeString")){
                doLargeString=true;
            }  
            if(args[i].equals("-hello")){
                doHello=true;
            }   
        }

        try{

            ORB orb = ORB.init(args, System.getProperties());

            com.sun.corba.ee.spi.orb.ORB ourORB
                = (com.sun.corba.ee.spi.orb.ORB)orb;

            System.out.println("==== Client GIOP version "
                               + ourORB.getORBData().getGIOPVersion()
                               + " with strategy "
                               + ourORB.getORBData().getGIOPBuffMgrStrategy(
                                    ourORB.getORBData().getGIOPVersion())
                               + "====");

            org.omg.CORBA.Object obj = readObjref("IOR", orb);

            GIOPCombo ref = 
                (GIOPCombo) PortableRemoteObject.narrow(obj, 
                                                            GIOPCombo.class);

            // Check various data types.
            int invalue = 1234;

            if(doHello){
                System.out.println("helloClient: Got server objref ! Invoking ...") ;
                System.out.println("ref.sayHello("+invalue+") = "+ref.sayHello(invalue));
            }

            if(doString){
                //echo String
                String string = (String)getStringObject(len,rep);
                System.out.println("String constructed for transmission");
                String stringEcho = ref.echo(string);
                System.out.println("ref.echo(string) = " + stringEcho);
                System.out.println("Echoed String equals the sent String == " +string.equals(stringEcho));
            }

            if(doLargeString){
                //echo String of 10 times the len
                String string = (String)getStringObject(10*len,rep);
                System.out.println("String constructed for transmission");
                String stringEcho = ref.echo(string);
                System.out.println("ref.echo(string) = " + stringEcho);
                System.out.println("Echoed String equals the sent String == " +string.equals(stringEcho));
            }

            if(doCMVT){
                //echo CustomMarshalledValueType containing string objects
                CustomMarshalledValueType cmvt = constructCustomMarshalledValueType(len,rep);
                System.out.println("CustomMarshalledValueType constructed for transmission");
                CustomMarshalledValueType cmvtEcho = ref.echo(cmvt);
                System.out.println("ref.echo(cmvt) = " + cmvtEcho);
                System.out.println("Echoed CustomMarshalledValueType equals the sent CustomMarshalledValueType == " +cmvt.equals(cmvtEcho));
            }

            if(doVector){
                //echo Vector containing string objects
                java.util.Vector vector = constructVector(len,rep);
                System.out.println("Vector constructed for transmission");
                java.util.Vector vectorEcho = ref.echo(vector);
                System.out.println("ref.echo(vector) = " + vectorEcho);
                System.out.println("Echoed Vector equals the sent Vector == " +vector.equals(vectorEcho));
            }

            if(doHashtable){
                //echo Hashtable containing string objects
                java.util.Hashtable ht = constructHashtable(len,rep);
                System.out.println("Hashtable constructed for transmission");
                java.util.Hashtable htEcho = ref.echo(ht);
                System.out.println("ref.echo(ht) = " + htEcho);
                System.out.println("Echoed Hashtable equals the sent Hashtable == " +ht.equals(htEcho));
            }

            System.out.println("\nhelloClient exiting ...") ;

        } catch (Exception e) {
            System.out.println("ERROR : " + e) ;
            e.printStackTrace(System.out);
            System.exit (1);
        }
    }
}
