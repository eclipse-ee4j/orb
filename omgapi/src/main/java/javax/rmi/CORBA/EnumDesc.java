/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package javax.rmi.CORBA ;

/** Class used to describe how a Java Enum is marshaled over
 * RMI-IIOP.  This is one of the proposals for OMG issue 10336, but
 * not the final approved version.
 */
public class EnumDesc implements java.io.Serializable {
    static final long serialVersionUID = -155483178780200630L ;

    /** Name of the enum constant.
     */
    public String value ;

    /** Name of the enum class.
     */
    public String className ;
}
