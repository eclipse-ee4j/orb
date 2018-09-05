/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

//
// Created       : 2005 Jun 7 (Tue) 09:24:39 by Harold Carr.
// Last Modified : 2005 Jul 28 (Thu) 18:10:31 by Harold Carr.
//

package corba.folb;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.*;

/**
 * @author Harold Carr
 */
public abstract class Common
{
    //
    // Object Adapter names.
    //

    public static final String RFM_WITH_ADDRESSES_WITH_LABEL = 
        "TEST_RFM_WITH_ADDRESSES_WITH_LABEL";

    public static final String RFM_WITH_ADDRESSES_WITHOUT_LABEL =
        "#INTERNAL#RFM_WITH_ADDRESSES_WITHOUT_LABEL#INTERNAL#";

    public static final String POA_WITH_ADDRESSES_WITH_LABEL = 
        "#INTERNAL#POA_WITH_ADDRESSES_WITH_LABEL#INTERNAL#";

    //
    // Names for bound references.
    //

    public static final String TEST_RFM_WITH_ADDRESSES_WITH_LABEL = 
        "TEST_RFM_WITH_ADDRESSES_WITH_LABEL";

    public static final String TEST_RFM_WITH_ADDRESSES_WITHOUT_LABEL = 
        "TEST_RFM_WITH_ADDRESSES_WITHOUT_LABEL";

    public static final String GIS_POA_WITHOUT_ADDRESSES_WITHOUT_LABEL =
        "GIS_POA_WITHOUT_ADDRESSES_WITHOUT_LABEL";

    public static final String GIS_POA_WITH_ADDRESSES_WITH_LABEL =
        "GIS_POA_WITH_ADDRESSES_WITH_LABEL";
}

// End of file.
