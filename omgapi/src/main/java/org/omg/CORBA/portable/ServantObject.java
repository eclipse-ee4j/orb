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

package org.omg.CORBA.portable;

/**
 This class is part of the local stub API, the purpose of which is to provide
 high performance calls for collocated clients and servers
 (i.e. clients and servers residing in the same Java VM).
 The local stub API is supported via three additional methods on
 <code>ObjectImpl</code> and <code>Delegate</code>.
 ORB vendors may subclass this class to return additional
 request state that may be required by their implementations.
 @see ObjectImpl
 @see Delegate
*/

public class ServantObject
{
    /** The real servant. The local stub may cast this field to the expected type, and then
     * invoke the operation directly. Note, the object may or may not be the actual servant
     * instance. 
     */
    public java.lang.Object servant;
}
