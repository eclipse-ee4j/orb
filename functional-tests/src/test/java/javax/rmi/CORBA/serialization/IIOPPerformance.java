/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
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

/*
 * @test 1.0 98/04/21
 * @summary Verify that RMI IIOP Serialization works correctly.
 *
 *
 * @compile javax\rmi\CORBA\serialization\SerializationTest.java
 * @run main  SerializationTest
 */

package javax.rmi.CORBA.serialization;

import javax.rmi.CORBA.serialization.*;
import javax.rmi.CORBA.*;
import rmic.ObjectByValue;

import java.io.*;
import java.util.Properties ;

public class IIOPPerformance extends test.Test
{
    public void run()
    {
        try {        
            Properties orbProps = new Properties() ;
            orbProps.put( "org.omg.CORBA.ORBClass", 
                          "com.sun.corba.ee.impl.orb.ORBImpl" ) ;
            orbProps.put( "org.omg.CORBA.ORBSingletonClass", 
                          "com.sun.corba.ee.impl.orb.ORBSingleton" ) ;
            org.omg.CORBA.ORB orb = 
                org.omg.CORBA.ORB.init(getArgsAsArgs(),orbProps);

            org.omg.CORBA_2_3.portable.OutputStream sos =
                (org.omg.CORBA_2_3.portable.OutputStream)orb.create_output_stream();

            TestOBV2 tobv2 = new TestOBV2();
            sos.write_value(tobv2);

            Exception exception = new Exception("Test Exception");
            sos.write_value(exception);

            Float f = new Float(1.23);
            sos.write_value(f);

            javax.rmi.CORBA.serialization.ComplexTestObjectOne test1 =
                new javax.rmi.CORBA.serialization.ComplexTestObjectOne();
            sos.write_value(test1);

            javax.rmi.CORBA.serialization.ComplexTestObjectTwo test2 =
                new javax.rmi.CORBA.serialization.ComplexTestObjectTwo();
            sos.write_value(test2);

            javax.rmi.CORBA.serialization.ComplexTestObjectTwoSubclass test2subclass =
                new javax.rmi.CORBA.serialization.ComplexTestObjectTwoSubclass();
            sos.write_value(test2subclass);

            javax.rmi.CORBA.serialization.ComplexTestObjectTwoSubclassDefaults test2subclassDefaults =
                new javax.rmi.CORBA.serialization.ComplexTestObjectTwoSubclassDefaults();
            sos.write_value(test2subclassDefaults);

            javax.rmi.CORBA.serialization.EmptyTestObject testEmptyA=
                new javax.rmi.CORBA.serialization.EmptyTestObject();
            sos.write_value(testEmptyA);

            javax.rmi.CORBA.serialization.EmptyTestObject testEmptyB=
                new javax.rmi.CORBA.serialization.EmptyTestObject();
            sos.write_value(testEmptyB);

            javax.rmi.CORBA.serialization.ComplexTestObjectOne test1b =
                new javax.rmi.CORBA.serialization.ComplexTestObjectOne();
            sos.write_value(test1b);

            Double d = new Double(3.5);
            sos.write_value(d);

            javax.rmi.CORBA.serialization.ComplexTestObjectThree test3 =
                new javax.rmi.CORBA.serialization.ComplexTestObjectThree();
            sos.write_value(test3);

            javax.rmi.CORBA.serialization.ComplexTestObjectFour test4 =
                new javax.rmi.CORBA.serialization.ComplexTestObjectFour();
            sos.write_value(test4);

            java.util.Properties props = new java.util.Properties();
            props.put("Key1","Value1");
            sos.write_value(props);

            // Test arrays
            int anIntArray[] = {9,8,7};
            sos.write_value(anIntArray);

            Object aSharedRefsArray[] = { testEmptyA, test1b, anIntArray };
            sos.write_value( aSharedRefsArray );

            // Check single dimensional primitive array...
            
            int[] array1 = {0,5,7,9,11,13};
            sos.write_value(array1);


            // Check 2 dimensional primitive array...

            long[][] array2 =   {
                {9,8,7,6,1},
                {18,4,6},
                {0,5,7,9,11,13}
            };
            sos.write_value(array2);

            // Recursive array references
            Object recursiveArray[] = {null, "Hello", null, null};
            recursiveArray[0] = recursiveArray;
            recursiveArray[2] = recursiveArray;
            recursiveArray[3] = recursiveArray;
            sos.write_value(recursiveArray);



            // Check 3 dimensional primitive array...
            short[][][] dim3 = {
                {
                    {0,8,7,6,1},
                    {1,5,7,13},
                    {2,4,6},
                },
                {
                    {3,4,10},
                    {4,5,7,9,13}
                },
                {
                    {5,4,6},
                    {6,5,8,9,11,13},
                    {7,8,7,6,1},
                    {8,8,7,6,9},
                },
            };
            sos.write_value(dim3);

            // Check single dimensional object array...
            
            ObjectByValue[] array3 =    {
                new ObjectByValue(5,10,"a","f"),
                new ObjectByValue(6,11,"b","g"),
                new ObjectByValue(7,12,"c","h"),
                new ObjectByValue(8,13,"d","i"),
                new ObjectByValue(9,14,"e","j"),
            };
            sos.write_value(array3);      
            
            // Check multi dimensional object array...

            ObjectByValue[][] array4 =   {   {
                new ObjectByValue(0,10,"a","g"),
                new ObjectByValue(0,11,"b","h"),
                new ObjectByValue(0,12,"c","i"),
            },
                                             {
                                                 new ObjectByValue(1,13,"d","j"),
                                                 new ObjectByValue(1,14,"e","k"),
                                             },
                                             {
                                                 new ObjectByValue(2,15,"f","l"),
                                             }
            };
                                
            sos.write_value(array4);

            /***************************************************************/
            /*********************** READ DATA BACK IN *********************/
            /***************************************************************/

            org.omg.CORBA_2_3.portable.InputStream sis = 
                (org.omg.CORBA_2_3.portable.InputStream)sos.create_input_stream();

            TestOBV2 _tobv2 = (TestOBV2)sis.read_value();
                        
            Exception _exception = (Exception)sis.read_value();
            if (!_exception.getMessage().equals("Test Exception"))
                throw new Error("Test Exception failed!");

            Float _f = (Float)sis.read_value();
            if (!f.equals(_f))
                throw new Error("Test Float failed!");

            javax.rmi.CORBA.serialization.ComplexTestObjectOne _test1 =
                (javax.rmi.CORBA.serialization.ComplexTestObjectOne)sis.read_value();

            if (test1.equals(_test1))
                ;
            else
                throw new Error("FAILURE!  Test1 Failed");

            javax.rmi.CORBA.serialization.ComplexTestObjectTwo _test2 =
                (javax.rmi.CORBA.serialization.ComplexTestObjectTwo)sis.read_value();

            if (test2.equals(_test2))
                ;
            else
                throw new Error("FAILURE!  Test2 Failed");

            javax.rmi.CORBA.serialization.ComplexTestObjectTwoSubclass _test2subclass =
                (javax.rmi.CORBA.serialization.ComplexTestObjectTwoSubclass)sis.read_value();

            if (test2subclass.equals(_test2subclass))
                ;
            else
                throw new Error("FAILURE!  Test2subclass Failed");

            javax.rmi.CORBA.serialization.ComplexTestObjectTwoSubclassDefaults _test2subclassDefaults =
                (javax.rmi.CORBA.serialization.ComplexTestObjectTwoSubclassDefaults)sis.read_value();

            if (test2subclassDefaults.equals(_test2subclassDefaults))
                ;
            else
                throw new Error("FAILURE!  Test2subclassDefaults Failed");

            javax.rmi.CORBA.serialization.EmptyTestObject _testEmptyA =
                (javax.rmi.CORBA.serialization.EmptyTestObject)sis.read_value();

            if (testEmptyA.equals(_testEmptyA))
                ;
            else
                throw new Error("FAILURE!  TestEmptyA Failed");

            javax.rmi.CORBA.serialization.EmptyTestObject _testEmptyB =
                (javax.rmi.CORBA.serialization.EmptyTestObject)sis.read_value();

            if (testEmptyB.equals(_testEmptyB))
                ;
            else
                throw new Error("FAILURE!  TestEmptyB Failed");

            javax.rmi.CORBA.serialization.ComplexTestObjectOne _test1b =
                (javax.rmi.CORBA.serialization.ComplexTestObjectOne)sis.read_value();

            if (test1b.equals(_test1b))
                ;
            else
                throw new Error("FAILURE!  Test1b Failed");

            Double _d = (Double)sis.read_value();

            if (d.equals(_d))
                ;
            else
                throw new Error("FAILURE!  d Failed");

            javax.rmi.CORBA.serialization.ComplexTestObjectThree _test3 =
                (javax.rmi.CORBA.serialization.ComplexTestObjectThree)sis.read_value();

            if (test3.equals(_test3))
                ;
            else
                throw new Error("FAILURE!  Test3 Failed");

            javax.rmi.CORBA.serialization.ComplexTestObjectFour _test4 =
                (javax.rmi.CORBA.serialization.ComplexTestObjectFour)sis.read_value();

            if (test4.equals(_test4))
                ;
            else
                throw new Error("FAILURE!  Test4 Failed");

            java.util.Properties _props = (java.util.Properties)sis.read_value();
            if (_props.toString().equals(props.toString()))
                ;
            else
                throw new Error("FAILURE!  props Failed");


            int _anIntArray[] = (int[])sis.read_value();

            if ((_anIntArray[0] == anIntArray[0]) &&
                (_anIntArray[1] == anIntArray[1]) &&
                (_anIntArray[2] == anIntArray[2]))
                ;
            else
                throw new Error("FAILURE!  anIntArray Failed");

            Object _aSharedRefsArray[] = (Object[])sis.read_value();

            if ((_aSharedRefsArray[0] == _testEmptyA) &&
                (_aSharedRefsArray[1] == _test1b) &&
                (_aSharedRefsArray[2] == _anIntArray))
                ;
            else
                throw new Error("FAILURE!  aSharedRefsArray[] == Failed");

            int[] array1Echo = (int[])sis.read_value();

            for (int i = 0; i < array1.length; i++) {
                if (array1[i] != array1Echo[i]) {
                    throw new Exception("HelloTest: echoArray (int[]) failed");
                }
            }

            long[][] array2Echo = (long[][])sis.read_value();
            
            for (int i = 0; i < array2.length; i++) {
                for (int j = 0; j < array2[i].length; j++) {
                    if (array2[i][j] != array2Echo[i][j]) {
                        throw new Exception("HelloTest: echoArray (int[][]) failed");
                    }
                }
            }

            // Recursive array references
            Object recursiveArrayEcho[] = (Object[])sis.read_value();
            if ((recursiveArrayEcho[0] != recursiveArrayEcho) || 
                (recursiveArrayEcho[2] != recursiveArrayEcho) || 
                (recursiveArrayEcho[3] != recursiveArrayEcho) || 
                (!((String)recursiveArrayEcho[1]).equals("Hello")))
                throw new Exception("RecursiveArray test failed!");

            short[][][] dim3Echo = (short[][][])sis.read_value();
            
            for (int i = 0; i < dim3.length; i++) {
                for (int j = 0; j < dim3[i].length; j++) {
                    for (int k = 0; k < dim3[i][j].length; k++) {
                        if (dim3[i][j][k] != dim3[i][j][k]) {
                            throw new Exception("HelloTest: echoArray (short[][][]) failed");
                        }
                    }
                }
            }

            ObjectByValue[] array3Echo = (ObjectByValue[])sis.read_value();
            for (int i = 0; i < array3.length; i++) {
                if (!array3[i].equals(array3Echo[i])) {
                    throw new Exception("HelloTest: echoArray (ObjectByValue[]) failed");
                }
            }

            ObjectByValue[][] array4Echo = (ObjectByValue[][])sis.read_value();
      
            for (int i = 0; i < array4.length; i++) {
                for (int j = 0; j < array4[i].length; j++) {
                    if (!array4[i][j].equals(array4Echo[i][j])) {
                        throw new Exception("HelloTest: echoArray (ObjectByValue[][]) failed");
                    }
                }
            }
 


        }
        catch(Throwable e)
            {
                status = new Error(e.getMessage());
                e.printStackTrace();
            }
    }

}
