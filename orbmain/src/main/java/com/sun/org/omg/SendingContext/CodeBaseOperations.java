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

package com.sun.org.omg.SendingContext;


/**
* com/sun/org/omg/SendingContext/CodeBaseOperations.java
* Generated by the IDL-to-Java compiler (portable), version "3.0"
* from rt.idl
* Thursday, May 6, 1999 1:52:08 AM PDT
*/

// Edited to leave RunTime in org.omg.CORBA

public interface CodeBaseOperations  extends org.omg.SendingContext.RunTimeOperations
{

    // Operation to obtain the IR from the sending context
    com.sun.org.omg.CORBA.Repository get_ir ();

    // Operations to obtain a URL to the implementation code
    String implementation (String x);
    String[] implementations (String[] x);

    // the same information
    com.sun.org.omg.CORBA.ValueDefPackage.FullValueDescription meta (String x);
    com.sun.org.omg.CORBA.ValueDefPackage.FullValueDescription[] metas (String[] x);

    // information
    String[] bases (String x);
} // interface CodeBaseOperations
