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
