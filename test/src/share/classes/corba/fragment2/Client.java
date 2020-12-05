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

package corba.fragment2;

import javax.rmi.PortableRemoteObject;
import org.omg.CosNaming.*;
import org.omg.CORBA.*;
import java.util.* ;
import java.rmi.RemoteException;
import java.io.*;
import com.sun.corba.ee.spi.misc.ORBConstants;

class Tester extends Thread{
    FragmentTester tester;
    int size;
    static int totalThread = 0;
    int threadID;

    public Tester(FragmentTester f, int s){
        tester = f;
        size = s;
        threadID = totalThread;
        totalThread++;
    }

    public void run() 
    {
        System.out.println("Sending array of length " + size);

        byte array[] = new byte[size];

        int i = 0;

        do {

            for (byte x = 0; x < 4; x++) {
                System.out.print("" + x + " ");
                array[i++] = x;
            }
            // System.out.println();

        } while (i < size);

        try{
            tester.verifyTransmission(array);
        } catch (Exception e) {
            System.out.println("ERROR : " + e) ;
            e.printStackTrace(System.out);
            System.exit (1);
        }

        System.out.println("testByteArray "+"ID:"+threadID+" completed normally");
    }
}


class TestCatagory{
    public String giopVersion;
    public int fragmentSize;
    public int arrayLength;
    public int threadNumber;
}


public class Client
{


    static TestCatagory testCatagory[];
    static int catagoryNumber;

    public static void setTest(){
        int data[] = {  2,1024,1024,5,
                        2,2048,2048,5,
                        2,2048,4096,5,
                        1,1024,1024,5
        };

        catagoryNumber = data.length / 4;
        testCatagory = new TestCatagory[catagoryNumber];
        for(int i=0;i<catagoryNumber;i++){
            testCatagory[i] = new TestCatagory();
            testCatagory[i].giopVersion = "1."+data[i*4];
            testCatagory[i].fragmentSize = data[i*4+1];
            testCatagory[i].arrayLength = data[i*4+2];
            testCatagory[i].threadNumber = data[i*4+3];
        }
    }   

        

    // size must be divisible by four

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

    public static void main(String args[])
    {
        for(int i=0;i<args.length;i++)
            System.out.println(args[i]);
        setTest();
        try{
            for(int i=0;i<catagoryNumber;i++){
                System.setProperty(ORBConstants.GIOP_FRAGMENT_SIZE, "" + testCatagory[i].fragmentSize);
                System.setProperty("array.length", "" + testCatagory[i].arrayLength);
                System.setProperty(ORBConstants.GIOP_VERSION, testCatagory[i].giopVersion);


                ORB orb = ORB.init(args, System.getProperties());

            /*
            org.omg.CORBA.Object objRef = 
                orb.resolve_initial_references("NameService");
            NamingContext ncRef = NamingContextHelper.narrow(objRef);
 
            NameComponent nc = new NameComponent("FragmentTester", "");
            NameComponent path[] = {nc};

            org.omg.CORBA.Object obj = ncRef.resolve(path);
            */

                org.omg.CORBA.Object obj = readObjref("IOR", orb);

                FragmentTester tester = 
                    (FragmentTester) PortableRemoteObject.narrow(obj, 
                                                                 FragmentTester.class);

            // Do the crazy work here

                int arrayLen = Integer.parseInt(System.getProperty("array.length"));

                for(int j=0;j < testCatagory[i].threadNumber;j++)
                    new Tester(tester, arrayLen).start();

            }
        } catch (Exception e) {
            System.out.println("ERROR : " + e) ;
            e.printStackTrace(System.out);
            System.exit (1);
        }
    }
}
