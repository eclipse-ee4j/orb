/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package ibmspace.common;

import java.io.Serializable;

public class ID implements Serializable
{
    private static int    fgNextId = 0;
    private int           fId;

    public ID ()
    {
        fId = fgNextId++;
    }

    public boolean identifies (Identifiable obj)
    {
        if ( this.equals (obj.getID()) ) {
            return true;
        } else {
            return false;
        }
    }

    public boolean equals (ID other)
    {
        if ( fId == other.fId )
            return true;
        else
            return false;
    }

}
