/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
 * Copyright (c) 2022 Contributors to the Eclipse Foundation. All rights reserved.
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

package rmic;

import test.Util;
import java.io.ByteArrayOutputStream;
import java.util.Hashtable;
import org.glassfish.rmic.tools.java.MemberDefinition;
import org.glassfish.rmic.iiop.Type;
import org.glassfish.rmic.iiop.ValueType;
import org.glassfish.rmic.iiop.Constants;
import org.glassfish.rmic.iiop.CompoundType;
import org.glassfish.rmic.iiop.RemoteType;
import org.glassfish.rmic.iiop.AbstractType;
import org.glassfish.rmic.iiop.ContextStack;

import org.testng.Assert ;

import org.testng.annotations.Test ;
import org.testng.annotations.AfterGroups ;
import org.testng.annotations.BeforeGroups ;
import org.testng.annotations.AfterSuite ;
import org.testng.annotations.BeforeSuite ;

public class TestExecutor {
    private ByteArrayOutputStream out = null;
    private TestEnv env = null;
    private ContextStack stack = null;

    private void myAssert (boolean test, String message) {
        Assert.assertTrue( test, message ) ;
    }
    
    @Test
    public void testImplArgOrDataMember() throws Throwable {
        testFailure("rmic.PassImplInterface", "is a remote implementation class ", "PassImplInterface");
        testFailure("rmic.ReturnImplInterface", "is a remote implementation class ", "ReturnImplInterface");
        testFailure("rmic.PassImplValue", "is a remote implementation class ", "PassImplValue");
    }
    
    @Test
    public void testReservedMethod() throws Throwable {
        RemoteType type = (RemoteType) MapType.getType("rmic.ReservedMethod",stack);
        myAssert(type != null,"rmic.ReservedMethod failed");
    }
      
    @Test
    public void testIDLEntityValue() throws Throwable {
        ValueType type = (ValueType) MapType.getType("rmic.IDLEntityValue",stack);
        myAssert(type != null,"rmic.IDLEntityValue failed");
        CompoundType.Method[] methods = type.getMethods();
        myAssert(methods.length == 1,"rmic.IDLEntityValue does not have 1 method");
        myAssert(methods[0].getIDLName().equals("_get_foo"),"rmic.IDLEntityValue method != _get_foo");       
    }
  
    @Test
    public void testPrivateMethodCollision() throws Throwable {
        CompoundType type = (CompoundType) MapType.getType("rmic.PrivateMethodCollision",stack);
        CompoundType.Method[] methods = type.getMethods();
        for (int i = 0;i < methods.length; i++) {
            if (!methods[i].isConstructor()) {
                myAssert(methods[i].getIDLName().equals("foo"),"rmic.PrivateMethodCollision method name != foo.");
                break;
            }
        }
        CompoundType.Member[] members = type.getMembers();
        myAssert(members.length == 1,"rmic.PrivateMethodCollision does not have 1 member");
        myAssert(members[0].getIDLName().equals("foo_"),"rmic.PrivateMethodCollision member != foo_");
    }
    
    @Test
    public void testEmptyInterface() throws Throwable {
        AbstractType type = (AbstractType) MapType.getType("rmic.EmptyInterface",stack);
        myAssert(type != null,"rmic.EmptyInterface failed");
    }
   
    @BeforeGroups( { "nonconforming" } ) 
    public void ncOn() {
        env.setParseNonConforming(true);
    }

    @AfterGroups( { "nonconforming" } ) 
    public void ncOff() {
        env.setParseNonConforming(false);
    }

    @Test( groups = { "nonconforming" } ) 
    public void testInvalidNC() throws Throwable {
        testFailure("rmic.InvalidNC", "rmic.InvalidNC is not a valid class", "J_bar conflicts");
    }
    
    @Test( groups = { "nonconforming" } ) 
    public void testValueWithInvalidNC() throws Throwable {
        testFailure("rmic.ValueWithInvalidNC", "rmic.ValueWithInvalidNC contains an invalid argument", "in method foo");
    }
  
    @Test
    public void testReturnVector() throws Throwable {
        RemoteType type = (RemoteType) MapType.getType("rmic.ReturnVector",stack);
        myAssert(type != null,"rmic.ReturnVector failed");
    }
    
    @Test
    public void testThrowNCIOException() throws Throwable {
        testFailure("rmic.ThrowNCIOException", "not a valid class", "rmic.NCIOException");
    }
    
    @Test
    public void testThrowNCException() throws Throwable {
        testFailure("rmic.ThrowNCException", "not a valid class", "rmic.NCRemoteException");
    }

    @Test
    public void testPassIDLEntityException() throws Throwable {
        testFailure("rmic.PassIDLEntityException", "may not pass an exception", "org.omg.CORBA.portable.IDLEntity");
    }
    
    @Test
    public void testOnlyRemote() throws Throwable {
        CompoundType type = (CompoundType) MapType.getType("rmic.OnlyRemoteServant",stack);
        myAssert(type != null,"rmic.OnlyRemoteServant failed");
    }
    
    @Test
    public void testSerialPersistent() throws Throwable {
        CompoundType type = (CompoundType) MapType.getType("test12.SerialPersistent",stack);
        CompoundType.Member[] members = type.getMembers();
        for (int i = 0;i < members.length; i++) {
            if (members[i].getName().equals("member8")) {
                myAssert(members[i].isTransient(),"test12.SerialPersistent member8 not marked transient.");
                break;
            }
        }
    }
    
    @Test
    public void testSequence() throws Throwable {
       
        // Check alpha.bravo.Charlie..
        
        CompoundType type = (CompoundType) MapType.getType("alpha.bravo.Charlie",stack);
        CompoundType.Member[] members = type.getMembers(); 
        Hashtable table = new Hashtable();
        
        for (int i = 0; i < members.length; i++) {
            Type t = members[i].getType();
            String name = t.getQualifiedIDLName(false);
            table.put(name,"");             
        }
        
        String[] names = {
            "org::omg::boxedRMI::omega::seq2_A",
            "org::omg::boxedRMI::seq2_boolean",
            "org::omg::boxedRMI::javax::rmi::CORBA::seq1_ClassDesc",
            "org::omg::boxedRMI::seq1_Object",
            "org::omg::boxedRMI::java::lang::seq1_Exception",
            "org::omg::boxedRMI::java::io::seq1_Externalizable",
            "org::omg::boxedRMI::omega::seq1_Dolphin",
            "org::omg::boxedRMI::omega::seq2_Dolphin",
            "org::omg::boxedRMI::omega::seq3_Dolphin",
            "org::omg::boxedRMI::org::omg::boxedIDL::omega::seq1_Foxtrot",
            "org::omg::boxedRMI::org::omg::boxedIDL::omega::seq2_Foxtrot",
            "org::omg::boxedRMI::omega::seq1_Golf",
            "org::omg::boxedRMI::omega::seq2_Golf",
            "org::omg::boxedRMI::omega::seq1_Hotel",
            "org::omg::boxedRMI::omega::seq2_Hotel",
            "org::omg::boxedRMI::org::omg::CORBA::portable::seq1_IDLEntity",
            "org::omg::boxedRMI::omega::seq1_India",
            "org::omg::boxedRMI::omega::seq2_India",
            "org::omg::boxedRMI::java::lang::seq1_Object",
            "org::omg::boxedRMI::seq1_long_long",
            "org::omg::boxedRMI::seq2_long_long",
            "org::omg::boxedRMI::java::rmi::seq1_Remote",
            "org::omg::boxedRMI::java::io::seq1_Serializable",
            "org::omg::boxedRMI::java::io::seq2_Serializable",
            "org::omg::boxedRMI::CORBA::seq1_WStringValue",
            "org::omg::boxedRMI::CORBA::seq2_WStringValue",
        };
        
        for (int i = 0; i < names.length; i++) {
            myAssert(table.containsKey(names[i]),"Did not find " + names[i]);
        }        
    }

    public void testMethodOverload(String typeName, String[] names, boolean[] constructor) throws Throwable {
        
        CompoundType type = (CompoundType) MapType.getType(typeName,stack);
        CompoundType.Method[] methods = type.getMethods(); 
        Hashtable table = new Hashtable();
        for (int i = 0; i < methods.length; i++) {
            String name = methods[i].getIDLName();
            table.put(name,new Boolean(methods[i].isConstructor()));             
        }

        for (int i = 0; i < names.length; i++) {
            Boolean value = (Boolean) table.get(names[i]);
            myAssert(value != null,"Did not find " + names[i] + " in "+typeName);
            myAssert(value.booleanValue() == constructor[i],"Constructor mismatch for " + names[i] + " in "+typeName);
        }        
    }
    
    @Test( groups = { "nonconforming" } ) 
    private void testMethodOverload() throws Throwable {
        testMethodOverload("rmic.MethodOverload1",rmic.MethodOverload1.IDL_NAMES,rmic.MethodOverload1.CONSTRUCTOR);
        testMethodOverload("rmic.MethodOverload2",rmic.MethodOverload2.IDL_NAMES,rmic.MethodOverload2.CONSTRUCTOR);
        testMethodOverload("rmic.MethodOverload3",rmic.MethodOverload3.IDL_NAMES,rmic.MethodOverload3.CONSTRUCTOR);
        testMethodOverload("rmic.MethodOverload4",rmic.MethodOverload4.IDL_NAMES,rmic.MethodOverload4.CONSTRUCTOR);
        testMethodOverload("rmic.MethodOverload5",rmic.MethodOverload5.IDL_NAMES,rmic.MethodOverload5.CONSTRUCTOR);
        testMethodOverload("rmic.MethodOverload6",rmic.MethodOverload6.IDL_NAMES,rmic.MethodOverload6.CONSTRUCTOR);
    }

    @Test
    public void testSwap() throws Throwable {
       
        // Make sure that we get the correct type for
        // SwapMember.notAbstract...
        
        CompoundType swapMember = (CompoundType) MapType.getType("rmic.SwapMember",stack);
        CompoundType.Member[] members = swapMember.getMembers(); 
        myAssert(members.length==2,"SwapMember found " + members.length + " members");
        CompoundType shouldSwap = null;
        int index = 0;
        if (members[1].getName().equals("ss")) index = 1;
        else myAssert(members[0].getName().equals("ss"),"SwapMember 'ss' not found");
        shouldSwap = (CompoundType)members[index].getType();
        members = shouldSwap.getMembers(); 
        myAssert(members.length==1,"ShouldSwap found " + members.length + " members");
        CompoundType na = (CompoundType)members[0].getType();
        String naName = members[0].getName();
        myAssert(naName.equals("notAbstract"),"Found wrong method: " + naName);
        myAssert(na instanceof org.glassfish.rmic.iiop.NCInterfaceType,"Found wrong type: " + na.getClass());
    }

    @Test
    public void testTypedef() throws Throwable {
       
        // Check rmic.Typedef...
        
        CompoundType type = (CompoundType) MapType.getType("rmic.Typedef",stack);
        CompoundType.Method[] methods = type.getMethods();
        int found = 0;
        for (int i = 0; i < methods.length; i++) {
            String name = methods[i].getIDLName();
            if (name.equals("union__wchar")) {
                found++;
            } else if (name.equals("union__long_long__java_lang_Object")) {
                found++;
            }
        }
        
        myAssert(found == 2,"Did not find expected IDL method names in rmic.Typedef");
    }
 
    @Test
    public void testClass() throws Throwable {
       
        // Check java.lang.Class...
        
        CompoundType type = (CompoundType) MapType.getType("java.lang.Class",stack);
        
        String name = type.getQualifiedName();
        myAssert(name.equals("java.lang.Class"),"java.lang.Class got name: " + name);

        String idlName = type.getQualifiedIDLName(false);
        myAssert(idlName.equals("javax::rmi::CORBA::ClassDesc"),"java.lang.Class got IDL name: " + idlName);
    }

    @Test
    public void testHiServant() throws Throwable {
         
        // Check rmic.HiServant.
        
        CompoundType type = (CompoundType) MapType.getType("rmic.HiServant",stack);
        myAssert(type != null,"rmic.HiServant got null type");
        myAssert(type.isType(Constants.TYPE_IMPLEMENTATION),"rmic.HiServant got wrong type");
    }


    @Test
    public void testCorbaObject() throws Throwable {
         
        // Check java.lang.Object...
        
        CompoundType type = (CompoundType) MapType.getType("org.omg.CORBA.Object",stack);
        
        String name = type.getQualifiedName();
        myAssert(name.equals("org.omg.CORBA.Object"),"org.omg.CORBA.Object got name: " + name);

        String idlName = type.getQualifiedIDLName(false);
        myAssert(idlName.equals("Object"),"org.omg.CORBA.Object got IDL name: " + idlName);
    }

    @Test
    public void testJavaLangObject() throws Throwable {
         
        // Check java.lang.Object...
        
        CompoundType type = (CompoundType) MapType.getType("java.lang.Object",stack);
        
        String name = type.getQualifiedName();
        myAssert(name.equals("java.lang.Object"),"java.lang.Object got name: " + name);

        String idlName = type.getQualifiedIDLName(false);
        myAssert(idlName.equals("java::lang::_Object"),"java.lang.Object got IDL name: " + idlName);
    }

    @Test
    public void testAbstract() throws Throwable {
       
        // Check alpha.bravo.Bear...
        
        CompoundType type = (CompoundType) MapType.getType("alpha.bravo.Bear",stack);
        CompoundType.Method[] methods = type.getMethods(); 
        int count = methods.length;
        
        for (int i = 0; i < count; i++) {
            String name = methods[i].getName();
            boolean isAttribute = methods[i].isAttribute();
            if (name.equals("getSize")) {
                if (!isAttribute) {
                    throw new Error("alpha.bravo.Bear method " + methods[i] + " is not an attribute " 
                        + methods[i].getAttributeName());
                }
            } else {
                if (isAttribute) {
                    throw new Error("alpha.bravo.Bear method " + methods[i] + " is an attribute " 
                        + methods[i].getAttributeName());
                }
            }
        }

        // Check rmic.AbstractObject...
        
        type = (CompoundType) MapType.getType("rmic.AbstractObject",stack);
        methods = type.getMethods(); 
        count = methods.length;
        
        for (int i = 0; i < count; i++) {
            String name = methods[i].getName();
            boolean isAttribute = methods[i].isAttribute();
            if (name.equals("getCodeBase") || name.equals("getValue")) {
                if (!isAttribute) {
                    throw new Error("rmic.AbstractObject method " + methods[i] + " is not an attribute "    
                        + methods[i].getAttributeName());
                }
            } else {
                if (isAttribute) {
                    throw new Error("rmic.AbstractObject method " + methods[i] + " is an attribute " 
                        + methods[i].getAttributeName());
                }
            }
        }
    }
    
    @Test
    public void testMangleMethodsFail() throws Throwable {
    
        testFailure("rmic.MangleMethodsFail1", "idl name for", "conflicts with");
        testFailure("rmic.MangleMethodsFail2", "idl name for", "conflicts with");
        testFailure("rmic.MangleMethodsFail3", "idl name for", "conflicts with");
    }

    private void testFailure(String clz, String errString1, String errString2) {
        testFailure(new String[]{clz},errString1,errString2);
    }
    
    private void testFailure(String[] classes, String errString1, String errString2) {
        out.reset();
        boolean failed = false;
        String[] additionalRMICArgs = {"-nowrite"};
        try {
            Util.rmic("-idl",additionalRMICArgs,classes,false,out);        
        } catch (Exception e) {
            failed = true;
        }
        
        String error = out.toString();
        if (!failed || error.indexOf(errString1) <= 0 || error.indexOf(errString2) <= 0) {
            String names = "";
            for (int i = 0; i < classes.length; i++) {
                names += (classes[i]+ " ");  
            }

            String msg = names + "did not fail as expected. Got: "+error;
            Assert.fail( msg ) ;
        }
    }


    @Test
    public void testMangleMethods() throws Throwable {
        
        // Get methods...
        
        CompoundType type = (CompoundType) MapType.getType("rmic.MangleMethods",stack);
        CompoundType.Method[] methods = type.getMethods(); 
        int count = methods.length;
        
        // Check asserts...
        
        for (int i = 0; i < count; i++) {
            
            CompoundType.Method method = methods[i];
            MemberDefinition def = method.getMemberDefinition();

            if (method.isConstructor()) continue;
            
            String[] asserts = MangleMethods.Asserts.getAsserts(def.toString());
            
            // Check attribute kind...
            
            int assertKind = getAttributeKind(asserts[1]);
            if (method.getAttributeKind() != assertKind) {
                throw new Error("Kinds do not match for " + def + ". Found " + method.getAttributeKind());
            }
            
            // Check attribute name...
            
            String assertAttrName = asserts[2];
            String attrName = method.getAttributeName();
            
            if ((assertAttrName == null || attrName == null) &&
                (assertAttrName != null || attrName != null)) {
                throw new Error("Attribute names do not match (null) for " + def + ". Found " + attrName);
            } else {
                
                if (assertAttrName == null || attrName == null) {
                    //System.out.println("Both null so match");
                } else {
                    if (!assertAttrName.equals(attrName)) {
                        throw new Error("Attribute names do not match for " + def + ". Found " + attrName);
                    }
                }
            }
            
            // Check IDL name...
            
            String assertIDLName = asserts[3];
            String idlName = method.getIDLName();
            
            if (!assertIDLName.equals(idlName)) {
                throw new Error("IDL names do not match for " + def + ". Found " + idlName);
            }   
        }
        
    }

    private int getAttributeKind(String assertKind) {
    
        if (assertKind.equalsIgnoreCase("NONE")) return Constants.ATTRIBUTE_NONE;
        if (assertKind.equalsIgnoreCase("IS")) return Constants.ATTRIBUTE_IS;
        if (assertKind.equalsIgnoreCase("GET")) return Constants.ATTRIBUTE_GET;
        if (assertKind.equalsIgnoreCase("IS_RW")) return Constants.ATTRIBUTE_IS_RW;
        if (assertKind.equalsIgnoreCase("GET_RW")) return Constants.ATTRIBUTE_GET_RW;
        if (assertKind.equalsIgnoreCase("SET")) return Constants.ATTRIBUTE_SET;
        throw new Error("Invalid assertKind: " + assertKind);        
    }
    
    @BeforeSuite() 
    public void setup() {
        out = new ByteArrayOutputStream();
        env = new TestEnv(ParseTest.createClassPath(),out);
        stack = new ContextStack(env);
    }

    @AfterSuite() 
    void cleanUp() {
        env.shutdown() ;
    }
}     
