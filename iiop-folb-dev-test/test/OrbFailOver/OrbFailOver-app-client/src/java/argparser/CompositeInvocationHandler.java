/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package argparser ;

import java.io.Serializable ;
import java.lang.reflect.InvocationHandler ;

public interface CompositeInvocationHandler extends InvocationHandler,
    Serializable
{
    /** Add an invocation handler for all methods on interface interf.
     */
    void addInvocationHandler( Class<?> interf, InvocationHandler handler ) ;

    /** Set the default invocation handler to use if none of the 
     * invocation handlers added by calls to addInvocationHandler apply.
     */
    void setDefaultHandler( InvocationHandler handler ) ;
}
