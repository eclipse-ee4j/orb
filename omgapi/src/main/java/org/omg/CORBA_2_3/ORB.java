/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998, 1999 IBM Corp. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.omg.CORBA_2_3;

/**
 * A class extending <code>org.omg.CORBA.ORB</code> to make the ORB
 * portable under the OMG CORBA version 2.3 specification.
 */
public abstract class ORB extends org.omg.CORBA.ORB {

/**
 * @see <a href="package-summary.html#unimpl"><code>CORBA_2_3</code> package
 *      comments for unimplemented features</a>
 */
    public org.omg.CORBA.portable.ValueFactory register_value_factory(String id,
                                                                     org.omg.CORBA.portable.ValueFactory factory)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }
 

/**
 * @see <a href="package-summary.html#unimpl"><code>CORBA_2_3</code> package
 *      comments for unimplemented features</a>
 */
    public void unregister_value_factory(String id)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }


/**
 * @see <a href="package-summary.html#unimpl"><code>CORBA_2_3</code> package
 *      comments for unimplemented features</a>
 */
    public org.omg.CORBA.portable.ValueFactory lookup_value_factory(String id)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }
    

/**
 * @see <a href="package-summary.html#unimpl"><code>CORBA_2_3</code> package
 *      comments for unimplemented features</a>
 */
    // always return a ValueDef or throw BAD_PARAM if
     // repid does not represent a valuetype
     public org.omg.CORBA.Object get_value_def(String repid)
                               throws org.omg.CORBA.BAD_PARAM {
       throw new org.omg.CORBA.NO_IMPLEMENT();
     }


/**
 * @see <a href="package-summary.html#unimpl"><code>CORBA_2_3</code> package
 *      comments for unimplemented features</a>
 */
     public void set_delegate(java.lang.Object wrapper) {
       throw new org.omg.CORBA.NO_IMPLEMENT();
     }


}
