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

package com.sun.corba.ee.spi.protocol;

import org.omg.CORBA.portable.ServantObject;

/**
 * @author Harold Carr
 */

public interface LocalClientRequestDispatcher {
    public boolean useLocalInvocation(org.omg.CORBA.Object self);

    public boolean is_local(org.omg.CORBA.Object self);

    /**
     * Returns a Java reference to the servant which should be used for this request. servant_preinvoke() is invoked by a
     * local stub. If a ServantObject object is returned, then its servant field has been set to an object of the expected
     * type (Note: the object may or may not be the actual servant instance). The local stub may cast the servant field to
     * the expected type, and then invoke the operation directly.
     *
     * @param self The object reference which delegated to this delegate.
     *
     * @param operation a string containing the operation name. The operation name corresponds to the operation name as it
     * would be encoded in a GIOP request.
     *
     * @param expectedType a Class object representing the expected type of the servant. The expected type is the Class
     * object associated with the operations class of the stub's interface (e.g. A stub for an interface Foo, would pass the
     * Class object for the FooOperations interface).
     *
     * @return a ServantObject object. The method may return a null value if it does not wish to support this optimization
     * (e.g. due to security, transactions, etc). The method must return null if the servant is not of the expected type.
     */
    public ServantObject servant_preinvoke(org.omg.CORBA.Object self, String operation, Class expectedType);

    public void servant_postinvoke(org.omg.CORBA.Object self, ServantObject servant);
}

// End of file.
