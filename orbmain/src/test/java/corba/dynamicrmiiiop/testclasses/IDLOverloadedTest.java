/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.dynamicrmiiiop.testclasses;

public class IDLOverloadedTest {

    public class Inner\u0300 {

        public class Extra\u0301Inner {
        }
    }

    //
    // Set of idl names corresponding to alphabetically sorted set of
    // interface methods. See TestIDLNameTranslator for sorting details.
    //
    public static final String[] IDL_NAMES = {

            "A__", "A__org_omg_boxedRMI_seq1_octet", "A__org_omg_boxedRMI_seq1_wchar", "A__org_omg_boxedRMI_seq1_double", "A__org_omg_boxedRMI_seq1_float",
            "A__org_omg_boxedRMI_seq1_long", "A__org_omg_boxedRMI_seq1_long_long",

            "A__org_omg_boxedRMI_corba_dynamicrmiiiop_testclasses_seq1_IDLOverloadedTest__InnerU0300__ExtraU0301Inner",
            "A__org_omg_boxedRMI_corba_dynamicrmiiiop_testclasses_seq1_IDLOverloadedTest__InnerU0300",

            "A__org_omg_boxedRMI_java_io_seq1_Externalizable", "A__org_omg_boxedRMI_java_io_seq1_Serializable", "A__org_omg_boxedRMI_java_lang_seq1_Boolean",
            "A__org_omg_boxedRMI_java_lang_seq1_Byte", "A__org_omg_boxedRMI_java_lang_seq1_Character", "A__org_omg_boxedRMI_javax_rmi_CORBA_seq1_ClassDesc",
            "A__org_omg_boxedRMI_java_lang_seq1_Double", "A__org_omg_boxedRMI_java_lang_seq1_Float", "A__org_omg_boxedRMI_java_lang_seq1_Integer",
            "A__org_omg_boxedRMI_java_lang_seq1_Long", "A__org_omg_boxedRMI_java_lang_seq1_Object",

            "A__org_omg_boxedRMI_java_lang_seq1_Short", "A__org_omg_boxedRMI_CORBA_seq1_WStringValue", "A__org_omg_boxedRMI_java_rmi_seq1_Remote",
            "A__org_omg_boxedRMI_javax_swing_seq1_UIDefaults__ActiveValue", "A__org_omg_boxedRMI_seq1_Object",

            "A__org_omg_boxedRMI_seq1_short", "A__org_omg_boxedRMI_seq1_boolean", "A__org_omg_boxedRMI_seq2_boolean",
            "A__org_omg_boxedRMI_corba_dynamicrmiiiop_testclasses_seq4_IDLOverloadedTest__InnerU0300__ExtraU0301Inner__org_omg_boxedRMI_CORBA_seq2_WStringValue__long",
            "A__org_omg_boxedRMI_seq16_boolean", "A__boolean", "A__octet", "A__wchar", "A__corba_dynamicrmiiiop_testclasses_IDLOverloadedTest__InnerU0300",
            "A__corba_dynamicrmiiiop_testclasses_IDLOverloadedTest__InnerU0300__ExtraU0301Inner",
            "A__org_omg_boxedIDL_corba_dynamicrmiiiop_testclasses_TestStruct", "A__double", "A__float", "A__long",
            "A__long__float__double__wchar__octet__boolean__java_io_Serializable__CORBA_WStringValue",

            "A__java_io_Externalizable", "A__java_io_Serializable", "A__java_lang_Boolean", "A__java_lang_Byte", "A__java_lang_Character",
            "A__javax_rmi_CORBA_ClassDesc", "A__java_lang_Double", "A__java_lang_Float", "A__java_lang_Integer", "A__java_lang_Long", "A__java_lang_Object",
            "A__java_lang_Short", "A__CORBA_WStringValue", "A__java_rmi_Remote", "A__javax_swing_UIDefaults__ActiveValue",

            "A__long_long", "A__Object", "A__short"

    };

    public static String[] getIDLNames() {
        return IDL_NAMES;
    }

    public interface IDLOverloaded extends java.rmi.Remote {

        void A() throws java.rmi.RemoteException;

        void A(byte[] b) throws java.rmi.RemoteException;

        void A(char[] c) throws java.rmi.RemoteException;

        void A(double[] d) throws java.rmi.RemoteException;

        void A(float[] f) throws java.rmi.RemoteException;

        void A(int[] a) throws java.rmi.RemoteException;

        void A(long[] a) throws java.rmi.RemoteException;

        void A(corba.dynamicrmiiiop.testclasses.IDLOverloadedTest.Inner\u0300.Extra\u0301Inner[] b) throws java.rmi.RemoteException;

        void A(corba.dynamicrmiiiop.testclasses.IDLOverloadedTest.Inner\u0300[] a) throws java.rmi.RemoteException;

        void A(java.io.Externalizable[] e) throws java.rmi.RemoteException;

        void A(java.io.Serializable[] s) throws java.rmi.RemoteException;

        void A(java.lang.Boolean[] b) throws java.rmi.RemoteException;

        void A(java.lang.Byte[] b) throws java.rmi.RemoteException;

        void A(java.lang.Character[] b) throws java.rmi.RemoteException;

        void A(java.lang.Class[] c) throws java.rmi.RemoteException;

        void A(java.lang.Double[] d) throws java.rmi.RemoteException;

        void A(java.lang.Float[] f) throws java.rmi.RemoteException;

        void A(java.lang.Integer[] i) throws java.rmi.RemoteException;

        void A(java.lang.Long[] l) throws java.rmi.RemoteException;

        void A(java.lang.Object[] o) throws java.rmi.RemoteException;

        void A(java.lang.Short[] s) throws java.rmi.RemoteException;

        void A(java.lang.String[] s) throws java.rmi.RemoteException;

        void A(java.rmi.Remote[] r) throws java.rmi.RemoteException;

        void A(javax.swing.UIDefaults.ActiveValue[] s) throws java.rmi.RemoteException;

        void A(org.omg.CORBA.Object[] o) throws java.rmi.RemoteException;

        void A(short[] s) throws java.rmi.RemoteException;

        void A(boolean[] b) throws java.rmi.RemoteException;

        void A(boolean[][] b) throws java.rmi.RemoteException;

        void A(corba.dynamicrmiiiop.testclasses.IDLOverloadedTest.Inner\u0300.Extra\u0301Inner[][][][] a, java.lang.String[][] b, int c)
                throws java.rmi.RemoteException;

        void A(boolean[][][][][][][][][][][][][][][][] b) throws java.rmi.RemoteException;

        void A(boolean z) throws java.rmi.RemoteException;

        void A(byte b) throws java.rmi.RemoteException;

        void A(char c) throws java.rmi.RemoteException;

        void A(corba.dynamicrmiiiop.testclasses.IDLOverloadedTest.Inner\u0300 d) throws java.rmi.RemoteException;

        void A(corba.dynamicrmiiiop.testclasses.IDLOverloadedTest.Inner\u0300.Extra\u0301Inner e) throws java.rmi.RemoteException;

        void A(double d) throws java.rmi.RemoteException;

        void A(float f) throws java.rmi.RemoteException;

        void A(int i) throws java.rmi.RemoteException;

        void A(int i, float f, double d, char c, byte b, boolean z, java.io.Serializable s, java.lang.String t) throws java.rmi.RemoteException;

        void A(java.io.Externalizable e) throws java.rmi.RemoteException;

        void A(java.io.Serializable s) throws java.rmi.RemoteException;

        void A(java.lang.Boolean b) throws java.rmi.RemoteException;

        void A(java.lang.Byte b) throws java.rmi.RemoteException;

        void A(java.lang.Character b) throws java.rmi.RemoteException;

        void A(java.lang.Class c) throws java.rmi.RemoteException;

        void A(java.lang.Double d) throws java.rmi.RemoteException;

        void A(java.lang.Float f) throws java.rmi.RemoteException;

        void A(java.lang.Integer i) throws java.rmi.RemoteException;

        void A(java.lang.Long l) throws java.rmi.RemoteException;

        void A(java.lang.Object o) throws java.rmi.RemoteException;

        void A(java.lang.Short s) throws java.rmi.RemoteException;

        void A(java.lang.String s) throws java.rmi.RemoteException;

        void A(java.rmi.Remote r) throws java.rmi.RemoteException;

        void A(javax.swing.UIDefaults.ActiveValue s) throws java.rmi.RemoteException;

        void A(long j) throws java.rmi.RemoteException;

        void A(org.omg.CORBA.Object o) throws java.rmi.RemoteException;

        void A(short s) throws java.rmi.RemoteException;

        void A(TestStruct t) throws java.rmi.RemoteException;
    }

}
