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

public class IDLDefaultTest {

    //
    // Set of idl names corresponding to alphabetically sorted set of
    // interface methods.  See TestIDLNameTranslator for sorting details.    
    //
    static final String[] IDL_NAMES = {

        "AAA__corba_dynamicrmiiiop_testclasses_Default",
        "AAA__corba_dynamicrmiiiop_testclasses_J_Default",
        "BBB__corba_dynamicrmiiiop_testclasses_Default__corba_dynamicrmiiiop_testclasses_Default__Inner__corba_dynamicrmiiiop_testclasses_Default___Inner__corba_dynamicrmiiiop_testclasses_Default___Default",
        "BBB__corba_dynamicrmiiiop_testclasses_J_Default__corba_dynamicrmiiiop_testclasses_J_Default__Inner__corba_dynamicrmiiiop_testclasses_J_Default___Inner__corba_dynamicrmiiiop_testclasses_J_Default__Default",
        "CCC__org_omg_boxedRMI_corba_dynamicrmiiiop_testclasses_seq1_Default",
        "CCC__org_omg_boxedRMI_corba_dynamicrmiiiop_testclasses_seq1_J_Default",

        "DDD__org_omg_boxedRMI_corba_dynamicrmiiiop_testclasses_seq1_Default__org_omg_boxedRMI_corba_dynamicrmiiiop_testclasses_seq1_Default__Inner__org_omg_boxedRMI_corba_dynamicrmiiiop_testclasses_seq1_Default___Inner__org_omg_boxedRMI_corba_dynamicrmiiiop_testclasses_seq1_Default___Default",
        "DDD__org_omg_boxedRMI_corba_dynamicrmiiiop_testclasses_seq1_J_Default__org_omg_boxedRMI_corba_dynamicrmiiiop_testclasses_seq1_J_Default__Inner__org_omg_boxedRMI_corba_dynamicrmiiiop_testclasses_seq1_J_Default___Inner__org_omg_boxedRMI_corba_dynamicrmiiiop_testclasses_seq1_J_Default__Default"

    };
    
    public static String[] getIDLNames() {
        return IDL_NAMES;
    }

    public interface IDLDefault extends java.rmi.Remote {
        
        void AAA(Default d) throws java.rmi.RemoteException;
        void AAA(_Default d) throws java.rmi.RemoteException;

        void BBB(Default d, Default.Inner e, Default._Inner f,
                 Default._Default g) throws java.rmi.RemoteException;
        void BBB(_Default d, _Default.Inner e, _Default._Inner f,
                 _Default.Default g) throws java.rmi.RemoteException;
       
 
        void CCC(Default[] d) throws java.rmi.RemoteException;
        void CCC(_Default[] d) throws java.rmi.RemoteException;

        void DDD(Default[] d, Default.Inner[] e, Default._Inner[] f,
                 Default._Default[] g) throws java.rmi.RemoteException;
        void DDD(_Default[] d, _Default.Inner[] e, _Default._Inner[] f,
                 _Default.Default[] g) throws java.rmi.RemoteException;

    }

}
