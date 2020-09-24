/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2020 Payara Services Ltd.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.presentation.rmi;

import com.sun.corba.ee.spi.presentation.rmi.IDLNameTranslator;
import corba.dynamicrmiiiop.testclasses.*;
import junit.framework.TestCase;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.SortedSet;
import java.util.TreeSet;

public class TestIDLNameTranslator extends TestCase {

    private static final Class[] nonRemoteInterfaces = {
        InvalidRemotes.InvalidRemote1.class, 
        InvalidRemotes.InvalidRemote2.class,
        InvalidRemotes.InvalidRemote3.class, 
        InvalidRemotes.InvalidRemote4.class,
        InvalidRemotes.InvalidRemote5.class, 
        // InvalidRemote6 has a method that declares an unchecked exception.
        // Although bad practice, this is not an error.
        // InvalidRemotes.InvalidRemote6.class,
        // InvalidRemotes.InvalidRemote7.class, 
        // InvalidRemotes.InvalidRemote8.class,
        InvalidRemotes.InvalidRemote9.class, 
        InvalidRemotes.InvalidRemote10.class,
        InvalidRemotes.InvalidRemote11.class, 
        // The following test for interfaces that inherit the 
        // same method from multiple super-interfaces.  This is supposed
        // to be illegal, but rmic allows it, so we will also allow it here.
        //InvalidRemotes.InvalidRemote12.class,
        //InvalidRemotes.InvalidRemote13.class, 
        //InvalidRemotes.InvalidRemote14.class,
        //InvalidRemotes.InvalidRemote15.class,
        InvalidRemotes.InvalidRemote16.class,        
        InvalidRemotes.InvalidRemote17.class,
        InvalidRemotes.InvalidRemote18.class,
        InvalidRemotes.InvalidRemote19.class
    };
    
    protected void setUp() {}

    protected void tearDown() {}

    public void testMultipleInterfaces()
    {
        doIDLNameTranslationTest( IDLMultipleInterfaceTest.class,
            new Class[] { 
                IDLMultipleInterfaceTest.first.class,
                IDLMultipleInterfaceTest.second.class
            }
        ) ;
    }

    public void testIDLProperties() 
    {
        doIDLNameTranslationTest(IDLPropertiesTest.class,
                                 IDLPropertiesTest.IDLProperties.class); 
    }

    public void testOverloadedMethods() 
    {
        doIDLNameTranslationTest(IDLOverloadedTest.class,
            IDLOverloadedTest.IDLOverloaded.class); 
    }
    
    public void testContainerClash() 
    {
        
       
        doIDLNameTranslationTest(new String[] { "ContainerClash1_" },
                                 ContainerClash1.class);
                                 
        doIDLNameTranslationTest(new String[] { "ContainerCLASH2_" },
                                 ContainerClash2.class);
    
        doIDLNameTranslationTest(new String[] { "J_ContainerClash3_" },
                                 _ContainerClash3.class);
                                 
        doIDLNameTranslationTest(new String[] { "J_ContainerCLASH4_" },
                                 _ContainerClash4.class);    
    }

    public void testLeadingUnderscores()
    {
        doIDLNameTranslationTest(IDLLeadingUnderscoresTest.class,
              IDLLeadingUnderscoresTest.IDLLeadingUnderscores.class);
    }

    public void testIDLCaseSensitivity()
    {
        doIDLNameTranslationTest(IDLCaseSensitivityTest.class,
              IDLCaseSensitivityTest.IDLCaseSensitivity.class);
    }

    public void testIDLKeywords()
    {
        doIDLNameTranslationTest(IDLKeywordsTest.class,
                                 IDLKeywordsTest.IDLKeywords.class);
    }

    public void testDefaultPackageClasses() 
    {
        Class testClass = null;
        Class testInterface = null;
        try {
            testClass = Class.forName("corba.dynamicrmiiiop.testclasses.IDLDefaultTest");
            testInterface = Class.forName("corba.dynamicrmiiiop.testclasses.IDLDefaultTest$IDLDefault");
        } catch(Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        doIDLNameTranslationTest(testClass, testInterface);
    }

    public void testInvalidInterfaces()
    {
         for(int i = 0; i < nonRemoteInterfaces.length; i++) {
            Class nonRemote = nonRemoteInterfaces[i];
            String msg = "Unexpected success for class " + nonRemote.getName() +
                " at index " + i ;

            try {
                IDLNameTranslator translator = 
                    IDLNameTranslatorImpl.get(nonRemote);
                assertTrue(msg, false);
            } catch(IllegalStateException ise) {
                // System.out.println(ise.getMessage());
            }

            // Also ensure that IDLNameTranslator rejects these interfaces.
        }
    }

    public void testIDLIdentifiers()
    {
        doIDLNameTranslationTest(IDLIdentifiersTest.class,
                                 IDLIdentifiersTest.IDLIdentifiers.class);
    }

    public void testIDLCombo1() {
        doIDLNameTranslationTest(IDLComboTest1.class,
                                 IDLComboTest1.IDLCombo.class);
    }

    private String[] getExpectedIdlNames( Class cls ) 
    {
        String[] expectedIdlNames = new String[0];
        try {
            Method idlNamesMethod = cls.getMethod("getIDLNames",
                                                        new Class[] {});
            expectedIdlNames = (String[]) 
                idlNamesMethod.invoke(null, new Object[] {});
        } catch(Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        return expectedIdlNames ;
    }

    private void doIDLNameTranslationTest(Class testClass, 
        Class[] testInterfaces)
    {
        String[] expectedIdlNames = getExpectedIdlNames( testClass ) ; 
        IDLNameTranslator nameTranslator = 
            IDLNameTranslatorImpl.get(testInterfaces);
        Method[] sortedMethods = getSortedMethods( testInterfaces );

        doIDLNameTranslationTest(expectedIdlNames, nameTranslator,
            sortedMethods );
    }

    private void doIDLNameTranslationTest(Class testClass, 
        Class testInterface)
    {
        String[] expectedIdlNames = getExpectedIdlNames( testClass ) ; 
        doIDLNameTranslationTest( expectedIdlNames, testInterface ) ;
    }

    private void doIDLNameTranslationTest(String[] expectedIdlNames, 
        Class testInterface)
    {
        IDLNameTranslator nameTranslator = 
            IDLNameTranslatorImpl.get(testInterface);
        Method[] sortedMethods = getSortedMethods(
            new Class[] { testInterface } );
      
        doIDLNameTranslationTest(expectedIdlNames, nameTranslator,
            sortedMethods );
    }

    private void doIDLNameTranslationTest(
        String[] expectedIdlNames, IDLNameTranslator nameTranslator,
        Method[] sortedMethods )
    {
        for(int i = 0; i < sortedMethods.length; i++) {
            Method m = (Method) sortedMethods[i];
            String expected = expectedIdlNames[i];
            String translatedName = nameTranslator.getIDLName(m);
            String msg = "expected '" + expected + "'" +
                " got '" + translatedName + "' " + ":" + m;

            assertEquals(msg, expected, translatedName);
            assertEquals(msg, m,
                                nameTranslator.getMethod(expected));           
        }
    }

    public void testUnicodeTranslation()
    {
        IDLNameTranslatorImpl nameTranslator = 
            (IDLNameTranslatorImpl)IDLNameTranslatorImpl.get(
                java.rmi.Remote.class);

        for( int i = Character.MIN_VALUE; i <= Character.MAX_VALUE; i++ ) {
            char c = (char) i;
            String unicode = IDLNameTranslatorImpl.charToUnicodeRepresentation(
                c);
            String msg = i + ":" + Character.toString(c) + ":" + unicode;

            // Make sure result is 5 characters long : 1 character for
            // the "U", plus four for the hex representation.
            assertEquals(msg, 5, unicode.length());

            BigInteger bigInt = new BigInteger(unicode.substring(1), 16);
            int hexValue = bigInt.intValue();
            msg = msg + ":" + hexValue;
            // Convert the hex back into a value and compare with original.
            assertEquals(msg, i, hexValue);
        }

    }

    private Method[] getSortedMethods(Class[] classes) 
    {
        SortedSet sortedMethods = new TreeSet(new MethodComparator());

        for(int classCtr = 0; classCtr < classes.length; classCtr++ ) {
            Method[] methods = classes[classCtr].getMethods();

            for(int methodCtr = 0; methodCtr < methods.length; methodCtr++) {
                Method next = methods[methodCtr];
                sortedMethods.add(next);
            }
        }

        Method[] sortedMethodArray = new Method[sortedMethods.size()];
        
        sortedMethods.toArray(sortedMethodArray);

        /** Uncomment to print method order.  Useful when
            debugging interfaces with multiple methods that have
            complex signatures
        
        System.out.println(sortedMethodArray.length + " sorted methods : ");
        for(int i = 0; i < sortedMethodArray.length; i++) {
            System.out.println(sortedMethodArray[i]);
        }
        */
        
        return sortedMethodArray;
    }

    //
    // Alphabetically sorted interface methods.
    // Method strings are composed of method name,
    // (case sensitive), followed by comma-separated list of the value
    // of Class.getName() for each parameter type.
    // 
    //
    private static class MethodComparator implements java.util.Comparator {
        public int compare(Object o1, Object o2) {
            String m1 = getMethodString((Method)o1);
            String m2 = getMethodString((Method)o2);
            return m1.compareTo(m2);
        }
        
        private String getMethodString(Method m) {
            StringBuilder methodStr = new StringBuilder(m.getName());
            Class[] params = m.getParameterTypes();
            for (Class<?> next : params) {
                methodStr.append("|");                                        
                methodStr.append(next.getName());
            }
            return methodStr.toString();
        }
    }

}
