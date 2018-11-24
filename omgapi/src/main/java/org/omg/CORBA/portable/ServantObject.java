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
 * This class is part of the local stub API, the purpose of which is to provide high performance calls for collocated
 * clients and servers (i.e. clients and servers residing in the same Java VM). The local stub API is supported via
 * three additional methods on <code>ObjectImpl</code> and <code>Delegate</code>. ORB vendors may subclass this class to
 * return additional request state that may be required by their implementations.
 *
 * @see ObjectImpl
 * @see Delegate
 */

public class ServantObject {
    /**
     * The real servant. The local stub may cast this field to the expected type, and then invoke the operation directly.
     * Note, the object may or may not be the actual servant instance.
     */
    public java.lang.Object servant;
}
