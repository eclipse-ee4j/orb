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


public interface Journey extends Identifiable
{
    ID          getID ();                 // id of the journey
    Planet      getOrigin ();             // where is the fleet from?
    Planet      getDestination ();        // where is the fleet going?
    double      getPercentComplete ();    // how far has the fleet gone so far?
}


