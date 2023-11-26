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

package corba.dynamicrmiiiop.testclasses;

public class IDLComboTest1 {

    //
    // Set of idl names corresponding to alphabetically sorted set of
    // interface methods. See TestIDLNameTranslator for sorting details.
    //
    static final String[] IDL_NAMES = {

            "ABCDEF_0_1_2_3_4_5", "CCDcxU04E3U05E90123_0_1_2", "CCdCxU04E3U05E90123_0_1_3", "ONEWay_0_1_2_3",

            "PLANT__corba_dynamicrmiiiop_testclasses_Raises", "PLANT__corba_dynamicrmiiiop_testclasses_Raises__Union",
            // "PLANT__corba_dynamicrmiiiop_testclasses_Raises__UniU8001code",
            "PLANT__corba_dynamicrmiiiop_testclasses_Raises___Foo", "PLANT__corba_dynamicrmiiiop_testclasses_Raises___Union",

            "QQQQQ__", "QQQQQ__org_omg_boxedRMI_corba_dynamicrmiiiop_testclasses_seq1_Raises__long",
            "RRRRR__org_omg_boxedRMI_corba_dynamicrmiiiop_testclasses_seq1_Raises__Union",
            "RRRRR__org_omg_boxedRMI_corba_dynamicrmiiiop_testclasses_seq1_Raises__Union__org_omg_boxedRMI_corba_dynamicrmiiiop_testclasses_seq1_Raises___Foo__org_omg_boxedRMI_corba_dynamicrmiiiop_testclasses_seq1_Raises___Union",

            "SSSSS__corba_dynamicrmiiiop_testclasses_J_Raises", "SSSSS__corba_dynamicrmiiiop_testclasses_J_Raises__Union",
            "SSSSS__corba_dynamicrmiiiop_testclasses_J_Raises___Foo", "SSSSS__corba_dynamicrmiiiop_testclasses_J_Raises___Union",

            "SSSTT__corba_dynamicrmiiiop_testclasses_J_upackage_Foo",
            // "SSSTT__corba_dynamicrmiiiop_testclasses_dolU0024lar_Foo",
            "SSSTT__corba_dynamicrmiiiop_testclasses_typedef_Foo",

            "TTTTT__", "TTTTT__org_omg_boxedRMI_corba_dynamicrmiiiop_testclasses_seq1_J_Raises__long",

            "TTTUU__org_omg_boxedRMI_corba_dynamicrmiiiop_testclasses_J_upackage_seq1_Foo",
            // "TTTUU__org_omg_boxedRMI_corba_dynamicrmiiiop_testclasses_dolU0024lar_seq1_Foo",
            "TTTUU__org_omg_boxedRMI_corba_dynamicrmiiiop_testclasses_typedef_seq1_Foo",

            "UUUUU__org_omg_boxedRMI_corba_dynamicrmiiiop_testclasses_seq1_J_Raises__Union",

            "UUUUU__org_omg_boxedRMI_corba_dynamicrmiiiop_testclasses_seq1_J_Raises__Union__org_omg_boxedRMI_corba_dynamicrmiiiop_testclasses_seq1_J_Raises___Foo__org_omg_boxedRMI_corba_dynamicrmiiiop_testclasses_seq1_J_Raises___Union",

            "J_BU8001U0024", "J_CCMcxU04E3U05E90123_1_2_3", "J_CCmCxU04E3U05E90123_1_2_4", "abcdef___", "abcdef___long", "_get_octet",
            "_get_ZWepT_0_1_4", "onewAy_4", "_set_octet", "zWePt_1_3"

    };

    public static String[] getIDLNames() {
        return IDL_NAMES;
    }

    /**
     * RMI Interface with a set of methods that involve multiple mangling rules from the Java2IDL spec.
     */
    public interface IDLCombo extends java.rmi.Remote {

        // Differs in case with a()
        void ABCDEF() throws java.rmi.RemoteException;

        // methods that Differ in case and have illegal IDL chars
        void CCDcx\u04e3\u05E90123() throws java.rmi.RemoteException;

        void CCdCx\u04e3\u05E90123() throws java.rmi.RemoteException;

        // Clashes with IDL keyword and differs only in case with onewAy
        void ONEWay() throws java.rmi.RemoteException;

        // class name that clashes with idl keyword + overloading
        void PLANT(Raises r) throws java.rmi.RemoteException;

        // inner class name that clashes with idl keyword, whose enclosing
        // class also clashes with idl keyword + overloading
        void PLANT(Raises.Union r) throws java.rmi.RemoteException;

        // inner class with unicode chars that need to be mangled
        // void PLANT(Raises.Uni\u8001code f) throws java.rmi.RemoteException;

        // inner class beginning with underscore, whose enclosing class
        // clashes with idl keyword + overloading
        void PLANT(Raises._Foo r) throws java.rmi.RemoteException;

        // inner class beginning with underscore, where the remainder is
        // an IDL keyword, whose enclosing class clashes with idl keyword
        // + overloading
        void PLANT(Raises._Union r) throws java.rmi.RemoteException;

        // overloading + class names that clash with keywords
        void QQQQQ() throws java.rmi.RemoteException;

        void QQQQQ(Raises[] r, int q) throws java.rmi.RemoteException;

        void RRRRR(Raises.Union[] r) throws java.rmi.RemoteException;

        void RRRRR(Raises.Union[] r, Raises._Foo[] s, Raises._Union[] t) throws java.rmi.RemoteException;

        // class name that starts with underscore, where remainder
        // clashes with idl keyword
        void SSSSS(_Raises r) throws java.rmi.RemoteException;

        // inner class that clashes with idl keyword, whose enclosing class
        // is underscore + keyword
        void SSSSS(_Raises.Union r) throws java.rmi.RemoteException;

        void SSSSS(_Raises._Foo r) throws java.rmi.RemoteException;

        void SSSSS(_Raises._Union r) throws java.rmi.RemoteException;

        // intermeidate package starting with an underscore
        void SSSTT(corba.dynamicrmiiiop.testclasses._upackage.Foo r) throws java.rmi.RemoteException;

        // subpackage name with illegal idl char
        // void SSSTT(corba.dynamicrmiiiop.testclasses.dol$lar.Foo r)
        // throws java.rmi.RemoteException;

        // intermediate package name(typedef) that clashes with idl keyword
        void SSSTT(corba.dynamicrmiiiop.testclasses.typedef.Foo r) throws java.rmi.RemoteException;

        void TTTTT() throws java.rmi.RemoteException;

        void TTTTT(_Raises[] r, int q) throws java.rmi.RemoteException;

        // intermediate package starting with an underscore
        void TTTUU(corba.dynamicrmiiiop.testclasses._upackage.Foo[] f) throws java.rmi.RemoteException;

        // subpackage name with illegal idl char
        // void TTTUU(corba.dynamicrmiiiop.testclasses.dol$lar.Foo[] r)
        // throws java.rmi.RemoteException;

        // intermediate package name(typedef) that clashes with idl keyword
        void TTTUU(corba.dynamicrmiiiop.testclasses.typedef.Foo[] f) throws java.rmi.RemoteException;

        void UUUUU(_Raises.Union[] r) throws java.rmi.RemoteException;

        void UUUUU(_Raises.Union[] r, _Raises._Foo[] s, _Raises._Union[] t) throws java.rmi.RemoteException;

        // Combo of leading underscore + illegal IDL chars
        void _B\u8001$() throws java.rmi.RemoteException;

        // methods that Differ in case and have illegal IDL chars and
        // start with underscore
        void _CCMcx\u04e3\u05E90123() throws java.rmi.RemoteException;

        void _CCmCx\u04e3\u05E90123() throws java.rmi.RemoteException;

        // Differs in case with ABCDEF() and is also overloaded
        void abcdef() throws java.rmi.RemoteException;

        void abcdef(int a) throws java.rmi.RemoteException;

        // property with attribute name that is the same as an IDL keyword.
        // rmic does not mangle these.
        int getOctet() throws java.rmi.RemoteException;

        // property that differs only in case with a method name
        int getZWepT() throws java.rmi.RemoteException;

        // Clashes with IDL keyword and differs only in case with ONEWay
        void onewAy() throws java.rmi.RemoteException;

        // property with attribute name that is the same as an IDL keyword.
        // rmic does not mangle these.
        void setOctet(int i) throws java.rmi.RemoteException;

        // method that differs only in case with a attribute name
        void zWePt() throws java.rmi.RemoteException;

    }

}
