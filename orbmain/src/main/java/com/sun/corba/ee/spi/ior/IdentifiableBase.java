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

package com.sun.corba.ee.spi.ior;

import com.sun.corba.ee.impl.ior.EncapsulationUtility ;

import org.omg.CORBA_2_3.portable.OutputStream ;

/** Provide support for properly reading and writing Identifiable objects
* that are also encapsulations (tagged profiles and components).
*/
public abstract class IdentifiableBase implements Identifiable,
    WriteContents
{
    /** Write the data for this object as a CDR encapsulation.
    * This is used for writing tagged components and profiles.
    * These data types must be written out as encapsulations,
    * which means that we need to first write the data out to
    * an encapsulation stream, then extract the data and write
    * it to os as an array of octets.
    */
    final public void write( OutputStream os )
    {
        EncapsulationUtility.writeEncapsulation( (WriteContents)this, os ) ;
    }
}
