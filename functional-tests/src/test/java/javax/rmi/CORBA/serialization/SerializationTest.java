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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Properties;
import java.util.Vector;
import org.glassfish.pfl.test.JUnitReportHelper;
import rmic.ObjectByValue;
import org.omg.CORBA.WStringValueHelper;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.MARSHAL;

public class SerializationTest extends test.Test
{
    private JUnitReportHelper helper = new JUnitReportHelper( this.getClass().getName() ) ;
    private boolean first = true ;

    private void test( String name ) {
        if (first) {
            first = false ;
        } else {
            helper.pass() ;
        }

        helper.start( name ) ;
    }

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

            BitSet _bitset;

            _bitset = new BitSet(64);
            _bitset.set(10);
            _bitset.set(20);
            _bitset.set(30);
            _bitset.set(40);
            _bitset.set(50);
            _bitset.set(60);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(_bitset);
            ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(baos.toByteArray()));
            BitSet __bitset = (BitSet)ois.readObject();
                                                                                                                   

            org.omg.CORBA_2_3.portable.OutputStream sos =
                (org.omg.CORBA_2_3.portable.OutputStream)orb.create_output_stream();

            ((org.omg.CORBA_2_3.ORB)orb).register_value_factory(DateHelper.id(),
                new DateDefaultFactory());

// Start of writing phase:

            test( "writeDate" ) ;
            javax.rmi.CORBA.serialization.Date date = DateHelper.create__(orb);
            DateHelper.write(sos, date);

            test( "writeStock" ) ;
            Stock stocks[] = new Stock[2];
            stocks[0] = new StockImpl("IBM");
            stocks[1] = StockHelper.create(orb, "Sun");
            seq1_StockHelper.write(sos, stocks);

            test( "writeMARSHAL" ) ;
            SystemException sex = new MARSHAL("dummy exception", 37,
                CompletionStatus.COMPLETED_MAYBE);
            sos.write_value(sex);

            test( "writeString" ) ;
            String helloString = "Hello world!";
            WStringValueHelper.write (sos, helloString);

            test( "writeIDLValue" ) ;
            IDLValue idlv = new IDLValue();
            sos.write_value(idlv);

            test( "writeGraph" ) ;
            Graph graph = new Graph("This", 
                new Graph("is", 
                    new Graph("a", 
                        new Graph("graph",
                            new Graph("object", 
                                new Graph("test.", null))))));
            sos.write_value(graph);

            test( "writeDate2" ) ;
            java.util.Date aDate = new java.util.Date();
            sos.write_value(aDate);

            test( "writeVector" ) ;
            Vector vector1 = new Vector();
            sos.write_value(vector1);

            test( "writeStringValue" ) ;
            sos.write_value("Hey you stream code!");

            test( "writeVector2" ) ;
            sos.write_value(vector1);

            test( "writeVector3" ) ;
            Vector vector2 = new Vector();
            sos.write_value(vector2);

            test( "writeNotSerializableChild" ) ;
            NotSerializableChild notser = new NotSerializableChild();
            sos.write_value(notser);

            test( "writeIsSerializable" ) ;
            IsSerializable isser = new IsSerializable("Hey IBM!");
            sos.write_value(isser);

            test( "writeClass" ) ;
            Class clazz = java.util.Hashtable.class;
            sos.write_value(clazz);

            test( "writeString2" ) ;
            sos.write_value("I5 X1");

            test( "writeString3" ) ;
            sos.write_value("I6 X2");

            test( "writeBoolean" ) ;
            sos.write_boolean(true);

            test( "writeEmptyString" ) ;
            sos.write_value("");

            test( "writeOBJV2" ) ;
            TestOBV2 tobv2 = new TestOBV2();
            sos.write_value(tobv2);
                        
            test( "writeArrayString" ) ;
            String names[] = {"Alpha","Beta","Charlie"};
            double percents[] = {0.5,0.7};
            BudgetSummary summary = new BudgetSummary(2, names, percents);
            sos.write_value(summary);

            test( "writeOctet" ) ;
            byte b = (byte)0xBA;
            sos.write_octet(b);
            
            test( "writeLong" ) ;
            sos.write_long(45);

            test( "writeWString" ) ;
            String idlString = "An idl test string";
            sos.write_wstring(idlString);

            test( "writeStringAsValue" ) ;
            String str1 = new String("Hello, World!");
            sos.write_value(str1);

            test( "writeNullValue" ) ;
            sos.write_value(null);

            test( "writeException" ) ;
            Exception exception = new Exception("Test Exception");
            sos.write_value(exception);

            test( "writeString4" ) ;
            String str2 = new String("Corvettes Rule!");
            sos.write_value(str2);

            test( "writeAliasedStringAsValue" ) ;
            String str3 = str2;
            sos.write_value(str3);

            test( "writeCustomObject1" ) ;
            OBVTestObjectCustom obvc = new OBVTestObjectCustomImpl();
            sos.write_value(obvc);

            test( "writeCustomObject2" ) ;
            OBVTestObjectCustomHelper.write(sos, obvc);

            test( "writeCustomObject3" ) ;
            sos.write_value(obvc);

            test( "writeCustomObject4" ) ;
            OBVTestObjectCustom obvc2 = new OBVTestObjectCustomImpl();
            OBVTestObjectCustomHelper.write(sos, obvc2);

            test( "writeObjectOne" ) ;
            OBVTestObjectOne obv1 = new OBVTestObjectOneImpl();
            OBVTestObjectOneHelper.write(sos, obv1);

            test( "writeObjectShared" ) ;
            OBVTestObjectOne obv1Shared = obv1;
            sos.write_value(obv1Shared);

            test( "writeObjectValue2" ) ;
            OBVTestObjectOne obv2 = new OBVTestObjectOneImpl();
            sos.write_value(obv2);

            // Test multiple streams open at once (tests pooling)
            test( "writeMultipleStreams" ) ;
            org.omg.CORBA_2_3.portable.OutputStream sos2 =
                (org.omg.CORBA_2_3.portable.OutputStream)orb.create_output_stream();
            sos2.write_value(str1);
            sos2.write_value(obv1);
                        
            test( "writeFloat" ) ;
            Float f = new Float(1.23);
            sos.write_value(f);

            test( "writeComplexTestObjectOne" ) ;
            javax.rmi.CORBA.serialization.ComplexTestObjectOne test1 =
                new javax.rmi.CORBA.serialization.ComplexTestObjectOne();
            sos.write_value(test1);

            test( "writeComplexTestObjectTwo" ) ;
            javax.rmi.CORBA.serialization.ComplexTestObjectTwo test2 =
                new javax.rmi.CORBA.serialization.ComplexTestObjectTwo();
            sos.write_value(test2);

            test( "writeComplexTestObjectTwoSubclass" ) ;
            javax.rmi.CORBA.serialization.ComplexTestObjectTwoSubclass test2subclass =
                new javax.rmi.CORBA.serialization.ComplexTestObjectTwoSubclass();
            sos.write_value(test2subclass);

            test( "writeComplexTestObjectTwoSubclassDefaults" ) ;
            javax.rmi.CORBA.serialization.ComplexTestObjectTwoSubclassDefaults test2subclassDefaults =
                new javax.rmi.CORBA.serialization.ComplexTestObjectTwoSubclassDefaults();
            sos.write_value(test2subclassDefaults);

            test( "writeComplexTestObjectTwoSubclassDelta" ) ;
            ComplexTestObjectTwoDelta delta2 = new ComplexTestObjectTwoDelta();
            sos.write_value(delta2);

            test( "writeEmptyTestObject" ) ;
            javax.rmi.CORBA.serialization.EmptyTestObject testEmptyA=
                new javax.rmi.CORBA.serialization.EmptyTestObject();
            sos.write_value(testEmptyA);

            test( "writeEmptyTestObject2" ) ;
            javax.rmi.CORBA.serialization.EmptyTestObject testEmptyB=
                new javax.rmi.CORBA.serialization.EmptyTestObject();
            sos.write_value(testEmptyB);

            test( "writeComplexTestObjectOne" ) ;
            javax.rmi.CORBA.serialization.ComplexTestObjectOne test1b =
                new javax.rmi.CORBA.serialization.ComplexTestObjectOne();
            sos.write_value(test1b);

            test( "writeDouble" ) ;
            Double d = new Double(3.5);
            sos.write_value(d);

            test( "writeComplexTestObjectThree" ) ;
            javax.rmi.CORBA.serialization.ComplexTestObjectThree test3 =
                new javax.rmi.CORBA.serialization.ComplexTestObjectThree();
            sos.write_value(test3);

            test( "writeComplexTestObjectFour" ) ;
            javax.rmi.CORBA.serialization.ComplexTestObjectFour test4 =
                new javax.rmi.CORBA.serialization.ComplexTestObjectFour();
            sos.write_value(test4);

            test( "writeProperties" ) ;
            java.util.Properties props = new java.util.Properties();
            props.put("Key1","Value1");
            sos.write_value(props);

            test( "writeIntArray" ) ;
            // Test arrays
            int anIntArray[] = {9,8,7};
            sos.write_value(anIntArray);

            test( "writeSharedRefArray" ) ;
            Object aSharedRefsArray[] = { testEmptyA, test1b, anIntArray };
            sos.write_value( aSharedRefsArray );

            // Check single dimensional primitive array...
            
            test( "writeIntArray2" ) ;
            int[] array1 = {0,5,7,9,11,13};
            sos.write_value(array1);


            // Check 2 dimensional primitive array...

            test( "writeLongArrayArray" ) ;
            long[][] array2 =   {
                {9,8,7,6,1},
                {18,4,6},
                {0,5,7,9,11,13}
            };
            sos.write_value(array2);

            test( "writeObjectArray" ) ;
            // Recursive array references
            Object recursiveArray[] = {null, "Hello", null, null};
            recursiveArray[0] = recursiveArray;
            recursiveArray[2] = recursiveArray;
            recursiveArray[3] = recursiveArray;
            sos.write_value(recursiveArray);

            test( "writeShortArrayArrayArray" ) ;
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

            test( "writeObjectArray" ) ;
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

            test( "writeObjectArrayArray" ) ;
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
            
            // Anys
            test( "writeAny" ) ;
            ComplexTestObjectXXX xxx = new ComplexTestObjectXXX();
            sos.write_value(xxx);

            //System.out.println("offset = " + ((com.sun.corba.ee.impl.encoding.CDROutputStream)sos).get_offset());
            //System.out.println("countit = " + ((com.sun.corba.ee.impl.encoding.CDROutputStream)sos).countit);

            /***************************************************************/
            /*********************** READ DATA BACK IN *********************/
            /***************************************************************/

            test( "createInputStream" ) ;
            org.omg.CORBA_2_3.portable.InputStream sis = 
                (org.omg.CORBA_2_3.portable.InputStream)sos.create_input_stream();

            test( "readDate" ) ;
            javax.rmi.CORBA.serialization.Date _date = (javax.rmi.CORBA.serialization.Date)
                DateHelper.read(sis);
                        
            test( "readStocks" ) ;
            Stock _stocks[] = (Stock[])seq1_StockHelper.read(sis);

            test( "readSystemException" ) ;
            SystemException _sex = (SystemException)sis.read_value();
            if (!(_sex.getMessage().equals(sex.getMessage())) ||
                _sex.minor != sex.minor ||
                _sex.completed != sex.completed)
                throw new Error("Hello String failed!");

            test( "readHelloString" ) ;
            String _helloString = (String)WStringValueHelper.read(sis);
            if (!helloString.equals(_helloString))
                throw new Error("Hello String failed!");

            test( "readIdlValue" ) ;
            IDLValue _idlv = (IDLValue)sis.read_value();
            if (!idlv.equals(_idlv))
                throw new Error("IDLValue test failed!");

            test( "readGraph" ) ;
            Graph _graph = (Graph)sis.read_value();
            if (!graph.toString().equals(_graph.toString())) {
                System.out.println("graph = " + graph);
                System.out.println("_graph = " + _graph);
                throw new Error("Graph test failed!");
            }

            test( "readData2" ) ;
            java.util.Date _aDate = 
                (java.util.Date)sis.read_value();

            test( "readVector" ) ;
            Vector _vector1 = (Vector)sis.read_value();

            sis.read_value();

            test( "readSharedVector" ) ;
            Vector _sharedVector1 = (Vector)sis.read_value();
            if (_vector1 != _sharedVector1)
                throw new Error("Shared vectors failed! (vectors not shared!)");

            test( "readUnsharedVector" ) ;
            Vector _vector2 = (Vector)sis.read_value();
            if (_vector1 == _vector2)
                throw new Error("Unshared vectors failed! (vectors are shared!)");

            test( "readNotSerializableChild" ) ;
            NotSerializableChild _notser = (NotSerializableChild)sis.read_value();
            if (!notser.equals(_notser))
                throw new Error("NotSerializableChild test failed!");

            test( "readIsSerializable" ) ;
            IsSerializable _isser = (IsSerializable)sis.read_value();
            if (!isser.equals(_isser))
                throw new Error("IsSerializable test failed!");
                        
            test( "readClass" ) ;
            Class _clazz = (Class)sis.read_value();
            if (!clazz.equals(_clazz))
                throw new Error("Test Clazz failed!");

            test( "readValue1" ) ;
            sis.read_value();

            test( "readValue2" ) ;
            sis.read_value();

            test( "readBoolean" ) ;
            sis.read_boolean();

            test( "readValue3" ) ;
            sis.read_value();

            test( "readValue4" ) ;
            TestOBV2 _tobv2 = (TestOBV2)sis.read_value();
            if (!tobv2.equals(_tobv2))
                throw new Error("TestOBV2 failed!");

            test( "readBudgetSummary" ) ;
            BudgetSummary _summary = (BudgetSummary)sis.read_value();

            test( "readOctet" ) ;
            byte _b = sis.read_octet();
            if (b != _b)
                throw new Error("Test byte failed!");

            test( "readLong" ) ;
            long l = sis.read_long();
            if (l != 45)
                throw new Error("Test 1 failed!");

            test( "readIdlString" ) ;
            String _idlString = sis.read_wstring();
            if (!idlString.equals(_idlString))
                throw new Error("Test idlString failed!");

            test( "readStringAsValue" ) ;
            String _str1 = (String)sis.read_value();
            if (!str1.equals(_str1))
                throw new Error("Test str1 failed! : " + _str1);

            test( "readNullString" ) ;
            String nullString = (String)sis.read_value();
            if (nullString != null)
                throw new Error("Test nullString failed! : ");

            test( "readException" ) ;
            Exception _exception = (Exception)sis.read_value();
            if (!_exception.getMessage().equals("Test Exception"))
                throw new Error("Test Exception failed!");

            test( "readString5" ) ;
            String _str2 = (String)sis.read_value();
            if (!str2.equals(_str2))
                throw new Error("Test str2 failed!");

            test( "readString6" ) ;
            String _str3 = (String)sis.read_value();
            if (_str3 != _str2)
                throw new Error("Test str3 failed!");

            test( "readObjectCustom" ) ;
            OBVTestObjectCustom _obvc = OBVTestObjectCustomHelper.read(sis);
            if (!obvc.equals(_obvc))
                throw new Error("Test Custom OBV failed!");

            test( "readObjectCustom2" ) ;
            OBVTestObjectCustom _obvcshared2 = (OBVTestObjectCustom)sis.read_value(OBVTestObjectCustom.class);
            if (_obvc != _obvcshared2)
                throw new Error("Test Custom OBV2 shared 2 failed!");

            test( "readObjectCustom3" ) ;
            OBVTestObjectCustom _obvcshared1 = (OBVTestObjectCustom)sis.read_value();
            if (_obvc != _obvcshared1)
                throw new Error("Test Custom OBV2 shared 1 failed!");

            test( "readObjectCustom4" ) ;
            OBVTestObjectCustom _obvc2 = OBVTestObjectCustomHelper.read(sis);
            if (!obvc2.equals(_obvc2))
                throw new Error("Test Custom OBV2 failed!");
            test( "readObjectOne" ) ;
            OBVTestObjectOne _obv1 = (OBVTestObjectOne)sis.read_value();
            if (!obv1.equals(_obv1))
                throw new Error("Test OBV1 failed!");

            test( "readMultipleStreams" ) ;
            // Test multiple streams open at once
            org.omg.CORBA_2_3.portable.InputStream sis2 = 
                (org.omg.CORBA_2_3.portable.InputStream)sos2.create_input_stream();
            String _str1_sis2 = (String)sis2.read_value();
            if (!str1.equals(_str1_sis2))
                throw new Error("Test _str1_sis2 idlString failed!");

            test( "readObjectOne2" ) ;
            OBVTestObjectOne _obv1_sis2 = (OBVTestObjectOne)sis2.read_value(OBVTestObjectOne.class);
            if (!obv1.equals(_obv1_sis2))
                throw new Error("Test sis2 OBV1 failed!");
                        
            test( "readObjectOne3" ) ;
            OBVTestObjectOne _obv1Shared = OBVTestObjectOneHelper.read(sis);
            if (_obv1 != _obv1Shared)
                throw new Error("Test Shared References OBV1 failed!");

            test( "readObjectOne4" ) ;
            OBVTestObjectOne _obv2 = (OBVTestObjectOne)sis.read_value();
            if (!obv2.equals(_obv2))
                throw new Error("Test OBV2 failed!");

            test( "readFloat" ) ;
            Float _f = (Float)sis.read_value();
            if (!f.equals(_f))
                throw new Error("Test Float failed!");

            test( "readComplexObjectOne" ) ;
            javax.rmi.CORBA.serialization.ComplexTestObjectOne _test1 =
                (javax.rmi.CORBA.serialization.ComplexTestObjectOne)sis.read_value();
            if (!test1.equals(_test1))
                throw new Error("FAILURE!  Test1 Failed:"+_test1);

            test( "readComplexObjectTwo" ) ;
            javax.rmi.CORBA.serialization.ComplexTestObjectTwo _test2 =
                (javax.rmi.CORBA.serialization.ComplexTestObjectTwo)sis.read_value();
            if (!test2.equals(_test2))
                throw new Error("FAILURE!  Test2 Failed");

            test( "readComplexObjectTwoSubclass" ) ;
            javax.rmi.CORBA.serialization.ComplexTestObjectTwoSubclass _test2subclass =
                (javax.rmi.CORBA.serialization.ComplexTestObjectTwoSubclass)sis.read_value();
            if (!test2subclass.equals(_test2subclass))
                throw new Error("FAILURE!  Test2subclass Failed");

            test( "readComplexObjectTwoDefaults" ) ;
            javax.rmi.CORBA.serialization.ComplexTestObjectTwoSubclassDefaults _test2subclassDefaults =
                (javax.rmi.CORBA.serialization.ComplexTestObjectTwoSubclassDefaults)sis.read_value();
            if (!test2subclassDefaults.equals(_test2subclassDefaults))
                throw new Error("FAILURE!  Test2subclassDefaults Failed");

            test( "readComplexObjectTwoDelta" ) ;
            ComplexTestObjectTwoDelta _delta2 = (ComplexTestObjectTwoDelta)sis.read_value();
            if (!delta2.equals(_delta2)) {
                System.out.println("delta2 = " + delta2);
                System.out.println("_delta2 = " + _delta2);
                throw new Error("FAILURE!  Delta2 Failed");
            }

            test( "readEmptyObject" ) ;
            javax.rmi.CORBA.serialization.EmptyTestObject _testEmptyA =
                (javax.rmi.CORBA.serialization.EmptyTestObject)sis.read_value();
            if (!testEmptyA.equals(_testEmptyA))
                throw new Error("FAILURE!  TestEmptyA Failed");

            test( "readEmptyObject2" ) ;
            javax.rmi.CORBA.serialization.EmptyTestObject _testEmptyB =
                (javax.rmi.CORBA.serialization.EmptyTestObject)sis.read_value();
            if (!testEmptyB.equals(_testEmptyB))
                throw new Error("FAILURE!  TestEmptyB Failed");

            test( "readComplexTestObjectOneAgain" ) ;
            javax.rmi.CORBA.serialization.ComplexTestObjectOne _test1b =
                (javax.rmi.CORBA.serialization.ComplexTestObjectOne)sis.read_value();
            if (!test1b.equals(_test1b))
                throw new Error("FAILURE!  Test1b Failed");

            test( "readDouble" ) ;
            Double _d = (Double)sis.read_value();
            if (!d.equals(_d))
                throw new Error("FAILURE!  d Failed");

            test( "readComplexObjectThree" ) ;
            javax.rmi.CORBA.serialization.ComplexTestObjectThree _test3 =
                (javax.rmi.CORBA.serialization.ComplexTestObjectThree)sis.read_value();
            if (!test3.equals(_test3))
                throw new Error("FAILURE!  Test3 Failed");

            test( "readComplexObjectFour" ) ;
            javax.rmi.CORBA.serialization.ComplexTestObjectFour _test4 =
                (javax.rmi.CORBA.serialization.ComplexTestObjectFour)sis.read_value();
            if (!test4.equals(_test4))
                throw new Error("FAILURE!  Test4 Failed");

            test( "readProperties" ) ;
            java.util.Properties _props = (java.util.Properties)sis.read_value();
            if (!_props.toString().equals(props.toString()))
                throw new Error("FAILURE!  props Failed");


            test( "readIntArray" ) ;
            int _anIntArray[] = (int[])sis.read_value();
            if ((_anIntArray[0] != anIntArray[0]) ||
                (_anIntArray[1] != anIntArray[1]) ||
                (_anIntArray[2] != anIntArray[2]))
                throw new Error("FAILURE!  anIntArray Failed");

            test( "readObjectArray" ) ;
            Object _aSharedRefsArray[] = (Object[])sis.read_value();
            if ((_aSharedRefsArray[0] != _testEmptyA) ||
                (_aSharedRefsArray[1] != _test1b) ||
                (_aSharedRefsArray[2] != _anIntArray))
                throw new Error("FAILURE!  aSharedRefsArray[] == Failed");

            test( "readIntArray2" ) ;
            int[] array1Echo = (int[])sis.read_value();
            for (int i = 0; i < array1.length; i++) {
                if (array1[i] != array1Echo[i]) {
                    throw new Exception("HelloTest: echoArray (int[]) failed");
                }
            }

            test( "readLongArrayArray" ) ;
            long[][] array2Echo = (long[][])sis.read_value();
            for (int i = 0; i < array2.length; i++) {
                for (int j = 0; j < array2[i].length; j++) {
                    if (array2[i][j] != array2Echo[i][j]) {
                        throw new Exception("HelloTest: echoArray (int[][]) failed");
                    }
                }
            }

            test( "readRecursizeObjectArray" ) ;
            // Recursive array references
            Object recursiveArrayEcho[] = (Object[])sis.read_value();
            if ((recursiveArrayEcho[0] != recursiveArrayEcho) || 
                (recursiveArrayEcho[2] != recursiveArrayEcho) || 
                (recursiveArrayEcho[3] != recursiveArrayEcho) || 
                (!((String)recursiveArrayEcho[1]).equals("Hello")))
                throw new Exception("RecursiveArray test failed!");

            test( "readShortArrayArrayArray" ) ;
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

            test( "readObjectByValueArray" ) ;
            ObjectByValue[] array3Echo = (ObjectByValue[])sis.read_value();
            for (int i = 0; i < array3.length; i++) {
                if (!array3[i].equals(array3Echo[i])) {
                    throw new Exception("HelloTest: echoArray (ObjectByValue[]) failed");
                }
            }

            test( "readObjectByValueArrayArray" ) ;
            ObjectByValue[][] array4Echo = (ObjectByValue[][])sis.read_value();
            for (int i = 0; i < array4.length; i++) {
                for (int j = 0; j < array4[i].length; j++) {
                    if (!array4[i][j].equals(array4Echo[i][j])) {
                        throw new Exception("HelloTest: echoArray (ObjectByValue[][]) failed");
                    }
                }
            }

            // Anys
            test( "readAny" ) ;
            ComplexTestObjectXXX _xxx = (ComplexTestObjectXXX)sis.read_value();
            if (!_xxx.equals(xxx))
                throw new Error("Any test using xxx failed!");
        } catch (Throwable e) {
            helper.fail( e ) ;
            status = new Error(e.getMessage());
            e.printStackTrace();
        } finally {
            if (!first)
                helper.pass() ;

            helper.done() ;
        }
    }
}
