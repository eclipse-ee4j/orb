/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.omg.CORBA.portable;

/**
 * An interface with no members whose only purpose is to serve as a marker indicating that an implementing class is a
 * Java value type from IDL that has a corresponding Helper class. RMI IIOP serialization looks for such a marker to
 * perform marshalling/unmarshalling.
 **/
public interface IDLEntity extends java.io.Serializable {

}
