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

import org.omg.CORBA_2_3.portable.InputStream ;

import com.sun.corba.ee.impl.encoding.EncapsOutputStream ;
import com.sun.corba.ee.impl.encoding.OutputStreamFactory;
import com.sun.corba.ee.spi.orb.ORB ;


/** Base class to use for implementing TaggedComponents.  It implements
 * the getIOPComponent method using the TaggedComponent.write() method.
 * @author Ken Cavanaugh
 */
public abstract class TaggedComponentBase extends IdentifiableBase 
    implements TaggedComponent 
{
    public org.omg.IOP.TaggedComponent getIOPComponent( 
        org.omg.CORBA.ORB orb )
    {
        EncapsOutputStream os = OutputStreamFactory.newEncapsOutputStream( (ORB)orb ) ;
        os.write_ulong( getId() ) ; // Fix for 6158378
        write( os ) ;
        InputStream is = (InputStream)(os.create_input_stream() ) ;
        return org.omg.IOP.TaggedComponentHelper.read( is ) ;
    }
}
